package spartan.exposed.codegen

import java.sql.ResultSet
import java.util.UUID
import kotlin.Boolean
import kotlin.Int
import kotlin.String
import kotlin.collections.List
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.andWhere
import org.jetbrains.exposed.sql.batchInsert
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update

public interface UserEntityCrud {
  public fun Database.byIds(ids: List<UUID>): List<UserEntity> = transaction(this) {
    UserTable.select { UserTable.id inList ids }
      .andWhere { UserTable.deletedAt.isNull() }
      .map { convert(it) }
  }

  public fun Database.byName(name: String): UserEntity? = transaction(this) {
    UserTable.select { UserTable.name eq name }
      .andWhere { UserTable.deletedAt.isNull() }
      .singleOrNull()
      ?.let { convert(it) }
  }

  public fun Database.byNames(names: List<String>): List<UserEntity> = transaction(this) {
    UserTable.select { UserTable.name inList names }
      .andWhere { UserTable.deletedAt.isNull() }
      .map { convert(it) }
  }

  public fun Database.byAge(age: Int): List<UserEntity> = transaction(this) {
    UserTable.select { UserTable.age eq age }
      .andWhere { UserTable.deletedAt.isNull() }
      .map { convert(it) }
  }

  public fun Database.byAges(ages: List<Int>): List<UserEntity> = transaction(this) {
    UserTable.select { UserTable.age inList ages }
      .andWhere { UserTable.deletedAt.isNull() }
      .map { convert(it) }
  }

  public fun Database.byId(id: UUID): UserEntity? = transaction(this) {
    UserTable.select { (UserTable.id eq id) and (UserTable.deletedAt.isNull()) }
      .singleOrNull()
      ?.let { convert(it) }
  }

  public fun Database.deleteById(id: UUID): UserEntity? = transaction(this) {
    UserTable.update({ UserTable.id eq id }) { update ->
      update[deletedAt] = java.time.Instant.now()
    }

    UserTable.select { UserTable.id eq id }
      .andWhere { UserTable.deletedAt.isNotNull() }
      .singleOrNull()
      ?.let { convert(it) }
  }

  public fun Database.insert(entity: UserEntity): UserEntity {
    transaction(this) {
      UserTable.insert {
        it[id] = entity.id
        it[name] = entity.name
        it[age] = entity.age
        it[bio] = entity.bio
        it[platform] = entity.platform.toString()
        it[updatedAt] = entity.updatedAt
        it[deletedAt] = entity.deletedAt
        it[createdAt] = entity.createdAt
      }
    }
    return entity
  }

  public fun convert(row: ResultRow): UserEntity = UserEntity(
    id = row[UserTable.id].value,
    name = row[UserTable.name],
    age = row[UserTable.age],
    bio = row[UserTable.bio],
    platform = spartan.exposed.codegen.Platform.valueOf(row[UserTable.platform].uppercase()),
    updatedAt = row[UserTable.updatedAt],
    deletedAt = row[UserTable.deletedAt],
    createdAt = row[UserTable.createdAt]
  )

  public fun convert(resultSet: ResultSet): UserEntity = UserEntity(
    id = resultSet.getObject(resultSet.findColumn(UserTable.id.name)) as java.util.UUID,
    name = resultSet.getObject(resultSet.findColumn(UserTable.name.name)) as kotlin.String,
    age = resultSet.getObject(resultSet.findColumn(UserTable.age.name)) as kotlin.Int,
    bio = resultSet.getObject(resultSet.findColumn(UserTable.bio.name)) as kotlin.String?,
    platform =
    spartan.exposed.codegen.Platform.valueOf(resultSet.getString(resultSet.findColumn(UserTable.platform.name)).uppercase()),
    updatedAt = resultSet.getObject(resultSet.findColumn(UserTable.updatedAt.name)) as
      java.time.Instant?,
    deletedAt = resultSet.getObject(resultSet.findColumn(UserTable.deletedAt.name)) as
      java.time.Instant?,
    createdAt = resultSet.getObject(resultSet.findColumn(UserTable.createdAt.name)) as
      java.time.Instant
  )

  public fun Database.update(
    id: UUID,
    name: String? = null,
    age: Int? = null,
    bio: String? = null,
    platform: Platform? = null,
  ): UserEntity? = transaction(this) {
    UserTable.update({ UserTable.id eq id }) { update ->
      name?.let { update[UserTable.name] = it }
      age?.let { update[UserTable.age] = it }
      bio?.let { update[UserTable.bio] = it }
      platform?.let { update[UserTable.platform] = it.toString() }
      update[updatedAt] = java.time.Instant.now()
    }

    UserTable.select { UserTable.id eq id }
      .singleOrNull()
      ?.let { convert(it) }
  }

  public fun Database.batchInsert(entities: List<UserEntity>, ignore: Boolean = false):
    List<UserEntity> {
    transaction(this) {
      UserTable.batchInsert(entities, ignore) { entity ->
        this[UserTable.id] = entity.id
        this[UserTable.name] = entity.name
        this[UserTable.age] = entity.age
        this[UserTable.bio] = entity.bio
        this[UserTable.platform] = entity.platform.toString()
        this[UserTable.updatedAt] = entity.updatedAt
        this[UserTable.deletedAt] = entity.deletedAt
        this[UserTable.createdAt] = entity.createdAt
      }
    }
    return entities
  }
}
