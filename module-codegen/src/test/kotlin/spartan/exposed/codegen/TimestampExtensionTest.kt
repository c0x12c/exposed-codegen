package spartan.exposed.codegen

import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo

class TimestampExtensionTest {

  @Test
  fun `now should generate correct types`() {
    expectThat(Timestamp.INSTANT.now).isEqualTo("java.time.Instant.now()")
    expectThat(Timestamp.OFFSET_DATE_TIME.now).isEqualTo("java.time.OffsetDateTime.now()")
    expectThat(Timestamp.LOCAL_DATE_TIME.now).isEqualTo("java.time.LocalDateTime.now()")
    expectThat(Timestamp.ZONED_DATE_TIME.now).isEqualTo("java.time.ZonedDateTime.now()")
  }
}
