package spartan.exposed.codegen.test

import java.util.UUID
import org.jetbrains.exposed.sql.Database

class UserRepository(
  private val db: Database
) : UserEntityCrud {

  fun byName(name: String): UserEntity? {
    return db.byName(name)
  }

  fun byNames(names: List<String>): List<UserEntity> {
    return db.byNames(names)
  }

  fun byAge(age: Int): List<UserEntity> {
    return db.byAge(age)
  }

  fun byAges(ages: List<Int>): List<UserEntity> {
    return db.byAges(ages)
  }

  fun byId(id: UUID): UserEntity? {
    return db.byId(id)
  }

  fun deleteById(id: UUID): UserEntity? {
    return db.deleteById(id)
  }

  fun insert(entity: UserEntity): UserEntity? {
    return db.insert(entity)
  }

  fun update(
    id: UUID,
    name: String? = null,
    age: Int? = null,
    bio: String? = null,
    platform: Platform? = null,
  ): UserEntity? {
    return db.update(id, name, age, bio, platform)
  }
}
