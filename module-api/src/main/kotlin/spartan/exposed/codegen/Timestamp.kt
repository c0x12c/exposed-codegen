package spartan.exposed.codegen

import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneOffset

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

fun OffsetDateTime.asEpochMillis(): Long {
  return toInstant().toEpochMilli()
}

fun Long.asOffsetDateTime(): OffsetDateTime {
  return Instant.ofEpochMilli(this).atOffset(ZoneOffset.UTC)
}

fun java.sql.Timestamp.asOffsetDateTime(): OffsetDateTime {
  val instant = Instant.ofEpochMilli(this.time)
  return OffsetDateTime.ofInstant(instant, ZoneOffset.UTC)
}
