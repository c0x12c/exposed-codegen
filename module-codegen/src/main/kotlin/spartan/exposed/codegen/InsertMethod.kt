package spartan.exposed.codegen

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.getAnnotationsByType
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.ParameterSpec
import org.jetbrains.exposed.sql.Database

/**
 * Generate an insert method using Kotlin Exposed syntax.
 */
class InsertMethod(
  private val excludedProperties: Set<String>
) : RepositoryMethod {

  override val imports: List<String> = listOf(
    "org.jetbrains.exposed.sql.transactions.transaction",
    "org.jetbrains.exposed.sql.insert"
  )

  @OptIn(KspExperimental::class)
  override fun funSpec(declaration: ClassDeclaration, context: KspContext): List<FunSpec> {
    val typeName = declaration.typeName
    val tableQualifiedName = declaration.declaration.getAnnotationsByType(Crud::class).first().table
    val tableTypeName = ClassName.bestGuess(tableQualifiedName)
    val properties = declaration.properties.filter { property -> property.name !in excludedProperties }
    val insertStatement = properties.map { property ->
      if (property.enum) {
        if (property.typeName.isNullable) {
          "it[${property.name}] = entity.${property.name}?.toString()"
        } else {
          "it[${property.name}] = entity.${property.name}.toString()"
        }
      } else {
        "it[${property.name}] = entity.${property.name}"
      }
    }
      .joinToString("\n")
    return listOf(
      FunSpec
        .builder("insert")
        .receiver(Database::class)
        .returns(typeName.copy(nullable = true))
        .addParameter(
          ParameterSpec
            .builder("entity", typeName)
            .build()
        )
        .addStatement(
          """
          |val result = transaction(this) {
          | %T.insert {
          |   %L
          | }
          |}
          """.trimMargin("|"),
          tableTypeName,
          insertStatement
        )
        .addStatement("return result.resultedValues?.singleOrNull()?.let { convert(it) }")
        .build()
    )
  }
}
