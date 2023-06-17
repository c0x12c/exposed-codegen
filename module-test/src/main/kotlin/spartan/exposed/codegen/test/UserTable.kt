package spartan.exposed.codegen.test

import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.javatime.timestamp

object UserTable : UUIDTable("users") {
  val name = text("name")
  val age = integer("age")
  val bio = text("bio").nullable()
  val platform = text("platform")
  val updatedAt = timestamp("updated_at").nullable()
  val deletedAt = timestamp("deleted_at").nullable()
  val createdAt = timestamp("created_at")
}


