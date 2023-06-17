package spartan.exposed.codegen

import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.asClassName
import java.sql.ResultSet
import java.time.OffsetDateTime

/**
 * Generates a method to convert a [ResultSet] to an entity.
 */
class ResultSetConvertMethod(
  private val excludedProperties: Set<String>
) : RepositoryMethod {

  private val resultSetClassName = ResultSet::class.asClassName()

  override val imports: List<String> = listOf(
    "org.jetbrains.exposed.sql.transactions.transaction"
  )

  override fun funSpec(declaration: ClassDeclaration, context: KspContext): List<FunSpec> {
    val typeName = declaration.typeName
    val tableName = declaration.tableClassName.simpleName
    val convertStatement = declaration
      .properties
      .filter { property -> property.name !in excludedProperties }
      .map { property ->
        val type = property.typeName
        when {
          property.enum -> {
            if (property.typeName.isNullable) {
              "${property.name} = resultSet.getString(resultSet.findColumn($tableName.${property.name}.name))?.let { ${type.copy(nullable = false)}.valueOf(it.uppercase())}"
            } else {
              "${property.name} = $type.valueOf(resultSet.getString(resultSet.findColumn($tableName.${property.name}.name)).uppercase())"
            }
          }

          property.isSameClassName(OffsetDateTime::class) -> {
            val nullableMark = if (property.typeName.isNullable) "?" else ""
            "${property.name} = (resultSet.getObject(resultSet.findColumn($tableName.${property.name}.name)) as$nullableMark java.sql.Timestamp)$nullableMark.asOffsetDateTime()"
          }

          else -> {
            "${property.name} = resultSet.getObject(resultSet.findColumn($tableName.${property.name}.name)) as ${property.typeName}"
          }
        }
      }
      .joinToString(",\n")
    return listOf(
      FunSpec
        .builder("convert")
        .returns(typeName)
        .addParameter(
          ParameterSpec
            .builder("resultSet", resultSetClassName)
            .build()
        )
        .addStatement(
          """
          |return %T(
          |   %L
          |)
          """.trimMargin("|"),
          typeName,
          convertStatement
        )
        .build()
    )
  }
}
