package spartan.exposed.codegen

/**
 * Each property annotated with [ExcludeColumn] will be dropped from code generation.
 */
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
annotation class ExcludeColumn
