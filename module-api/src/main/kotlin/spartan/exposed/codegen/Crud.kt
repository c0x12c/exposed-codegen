package spartan.exposed.codegen

/**
 * Entity class annotated with this annotation will generate boilerplate CRUD methods.
 * The name of interface being generated will be the name of the entity class with `Crud` suffix.
 * See `module-test` for example.
 *
 * @param table The fully qualified class name of the table. For example: `com.company.UserTable`
 * @param timestamp The type of timestamp to use for the entity. For example: `Timestamp.INSTANT`
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class Crud(
  val table: String,
  val timestamp: Timestamp
)
