package spartan.exposed.codegen

import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.asTypeName
import java.util.UUID
import org.jetbrains.exposed.sql.Database

/**
 * Generate a select an entity by id.
 */
object SelectOneByIdMethod : RepositoryMethod {

  override val imports: List<String> = listOf(
    "org.jetbrains.exposed.sql.select",
    "org.jetbrains.exposed.sql.and"
  )

  override fun funSpec(declaration: ClassDeclaration, context: KspContext): List<FunSpec> {
    val typeName = declaration.typeName
    val tableClassName = declaration.tableClassName
    return listOf(
      FunSpec
        .builder("byId")
        .receiver(Database::class)
        .returns(typeName.copy(nullable = true))
        .addParameter(
          ParameterSpec
            .builder("id", UUID::class.asTypeName())
            .build()
        )
        .addStatement(
          """
          |return transaction(this) {
          | %T.select { (%T.id eq id) and (%T.deletedAt.isNull()) }
          |   .singleOrNull()
          |   ?.let { convert(it) }
          |}
          """.trimMargin(),
          tableClassName,
          tableClassName,
          tableClassName
        )
        .build()
    )
  }
}
