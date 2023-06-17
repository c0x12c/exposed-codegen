package spartan.exposed.codegen

import java.time.Instant
import java.util.UUID
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.javatime.timestamp

enum class Platform {
  ANDROID,
  IOS
}

object UserTable : UUIDTable("users") {
  val name = text("name")
  val age = integer("phone")
  val bio = text("bio").nullable()
  val platform = text("platform")
  val updatedAt = timestamp("updated_at").nullable()
  val deletedAt = timestamp("deleted_at").nullable()
  val createdAt = timestamp("created_at")
}

@Crud("spartan.exposed.codegen.UserTable", timestamp = Timestamp.INSTANT)
data class UserEntity(
  @SelectableColumn
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
