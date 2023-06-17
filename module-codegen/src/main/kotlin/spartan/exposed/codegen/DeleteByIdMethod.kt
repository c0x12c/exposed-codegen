package spartan.exposed.codegen

import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.asTypeName
import java.util.UUID
import org.jetbrains.exposed.sql.Database

/**
 * Generate the method to delete an entity by id.
 */
class DeleteByIdMethod(
  private val timestamp: Timestamp
) : RepositoryMethod {
  override val imports: List<String> = listOf(
    "org.jetbrains.exposed.sql.transactions.transaction",
    "org.jetbrains.exposed.sql.andWhere",
    "org.jetbrains.exposed.sql.update",
  )

  override fun funSpec(declaration: ClassDeclaration, context: KspContext): List<FunSpec> {
    val tableClassName = declaration.tableClassName

    return listOf(
      FunSpec
        .builder("deleteById")
        .receiver(Database::class)
        .returns(declaration.typeName.copy(nullable = true))
        .addParameter(
          ParameterSpec
            .builder("id", UUID::class.asTypeName())
            .build()
        )
        .addStatement(
          """
          |return transaction(this) {
          | %T.update({ %T.id eq id }) { update ->
          |   update[deletedAt] = ${timestamp.now}
          | }
          |
          | %T.select { %T.id eq id }
          |  .andWhere { %T.deletedAt.isNotNull() }
          |  .singleOrNull()
          |   ?.let { convert(it) }
          |}
          """.trimMargin("|"),
          tableClassName,
          tableClassName,
          tableClassName,
          tableClassName,
          tableClassName
        )
        .build()
    )
  }
}
