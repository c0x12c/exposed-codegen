package spartan.exposed.codegen

/**
 * The type of timestamp to use for the generated code.
 */
enum class Timestamp(
  val type: String
) {
  INSTANT("java.time.Instant"),
  OFFSET_DATE_TIME("java.time.OffsetDateTime"),
  LOCAL_DATE_TIME("java.time.LocalDateTime"),
  ZONED_DATE_TIME("java.time.ZonedDateTime")
}
