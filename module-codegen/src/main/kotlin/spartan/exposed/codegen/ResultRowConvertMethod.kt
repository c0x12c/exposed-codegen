package spartan.exposed.codegen

import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.asClassName
import org.jetbrains.exposed.sql.ResultRow

/**
 * Generates a method to convert a [ResultRow] to an entity.
 */
class ResultRowConvertMethod(
  private val excludedProperties: Set<String>
) : RepositoryMethod {

  private val resultRowClassName = ResultRow::class.asClassName()

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
        if (property.enum) {
          if (property.typeName.isNullable) {
            "${property.name} = row[$tableName.${property.name}]?.let { ${type.copy(nullable = false)}.valueOf(it.uppercase())}"
          } else {
            "${property.name} = $type.valueOf(row[$tableName.${property.name}].uppercase())"
          }
        } else {
          if (property.name == "id") {
            "${property.name} = row[$tableName.${property.name}].value"
          } else {
            "${property.name} = row[$tableName.${property.name}]"
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
            .builder("row", resultRowClassName)
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
