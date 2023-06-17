package spartan.exposed.codegen

import java.time.temporal.Temporal
import java.util.UUID

/**
 * An entity contract with a UUID id and timestamps.
 */
interface Entity<T : Temporal> {
  val id: UUID
  val updatedAt: T?
  val deletedAt: T?
  val createdAt: T
}
