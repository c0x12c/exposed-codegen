package spartan.exposed.codegen

/**
 * Each property annotated with [SelectableColumn] will generate two extension methods:
 * - The first method is used to either "Select a single item or null for a unique column" or
 *   "Select a list of items or return empty for a non-unique column".
 * - The second method is designed to "Select a list of items by using a list parameter"
 *
 * @param unique Indicate that the column is unique or not
 */
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
annotation class SelectableColumn(
  val unique: Boolean = true
)
