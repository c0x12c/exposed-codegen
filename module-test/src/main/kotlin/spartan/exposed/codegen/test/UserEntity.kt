package spartan.exposed.codegen.test

import java.time.Instant
import java.util.UUID
import spartan.exposed.codegen.Entity
import spartan.exposed.codegen.Crud
import spartan.exposed.codegen.SelectableColumn
import spartan.exposed.codegen.Timestamp

@Crud("spartan.exposed.codegen.test.UserTable", timestamp = Timestamp.INSTANT)
data class UserEntity(
  override val id: UUID,
  @SelectableColumn(unique = true)
  val name: String = "hello",
  @SelectableColumn(unique = false)
  val age: Int = 100,
  val bio: String? = null,
  val platform: Platform,
  override val updatedAt: Instant? = null,
  override val deletedAt: Instant? = null,
  override val createdAt: Instant = Instant.now()
) : Entity<Instant>
