exposed-codegen
=======
Utilizing Kotlin KSP (https://kotlinlang.org/docs/ksp-overview.html) to generate ExposedSQL
boilerplate CRUD operation methods.

### Motivation

The idea involves traversing the AST of the annotated entity class and creating the subsequent 
extension methods on the `Database` object:

- `fun insert(e: Entity): Entity?`
- `fun byId(id: UUID): Entity?`
- `fun deleteById(id: UUID): Entity?`
- `fun update(id: UUID, a: A?, b: B?....): Entity?`
- `fun convert(row: ResultRow): Entity`

For more details, see `RepositorySymbolProcessor.kt`

### Usage

To use the codegen processor, you will need to do the following:

- Include the KSP plugin in your `build.gradle` module `id("com.google.devtools.ksp")`
- Call to `ksp`, i.e `ksp(project(":core:module-codegen"))`

**module-postgresql**

```groovy
plugins {
  id("com.google.devtools.ksp")
}

dependencies {
  implementation(project(":module-codegen"))
  ksp(project(":module-codegen"))

  implementation(Libraries.Exposed.core)
  implementation(Libraries.Exposed.dao)
  implementation(Libraries.Exposed.jdbc)
  implementation(Libraries.Exposed.javaTime)
}

kotlin {
  sourceSets.main {
    kotlin.srcDir("build/generated/ksp/main/kotlin")
  }
  sourceSets.test {
    kotlin.srcDir("build/generated/ksp/test/kotlin")
  }
}

```

### Example

Given the following entity:

```kt
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

@Repository("spartan.exposed.codegen.UserTable", timestamp = Timestamp.INSTANT)
data class UserEntity(
  @Selectable
  override val id: UUID,
  @Selectable(unique = true)
  val name: String = "hello",
  @Selectable(unique = false)
  val age: Int = 100,
  val bio: String? = null,
  val platform: Platform,
  override val updatedAt: Instant? = null,
  override val deletedAt: Instant? = null,
  override val createdAt: Instant = Instant.now()
) : Entity<Instant>
```

The codegen processor will generate the following:

```kt
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

interface UserEntityCrud {
  fun Database.byIds(ids: List<UUID>): List<UserEntity> = transaction(this) {
    UserTable.select { UserTable.id inList ids }
      .andWhere { UserTable.deletedAt.isNull() }
      .map { convert(it) }
  }

  fun Database.byName(name: String): UserEntity? = transaction(this) {
    UserTable.select { UserTable.name eq name }
      .andWhere { UserTable.deletedAt.isNull() }
      .singleOrNull()
      ?.let { convert(it) }
  }

  fun Database.byNames(names: List<String>): List<UserEntity> = transaction(this) {
    UserTable.select { UserTable.name inList names }
      .andWhere { UserTable.deletedAt.isNull() }
      .map { convert(it) }
  }

  fun Database.byAge(age: Int): List<UserEntity> = transaction(this) {
    UserTable.select { UserTable.age eq age }
      .andWhere { UserTable.deletedAt.isNull() }
      .map { convert(it) }
  }

  fun Database.byAges(ages: List<Int>): List<UserEntity> = transaction(this) {
    UserTable.select { UserTable.age inList ages }
      .andWhere { UserTable.deletedAt.isNull() }
      .map { convert(it) }
  }

  fun Database.byId(id: UUID): UserEntity? = transaction(this) {
    UserTable.select { (UserTable.id eq id) and (UserTable.deletedAt.isNull()) }
      .singleOrNull()
      ?.let { convert(it) }
  }

  fun Database.deleteById(id: UUID): UserEntity? = transaction(this) {
    UserTable.update({ UserTable.id eq id }) { update ->
      update[deletedAt] = java.time.Instant.now()
    }

    UserTable.select { UserTable.id eq id }
      .andWhere { UserTable.deletedAt.isNotNull() }
      .singleOrNull()
      ?.let { convert(it) }
  }

  fun Database.insert(entity: UserEntity): UserEntity {
    val result = transaction(this) {
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
    }.resultedValues?.firstOrNull()?.let { convert(it) }
    return result
  }

  fun convert(row: ResultRow): UserEntity = UserEntity(
    id = row[UserTable.id].value,
    name = row[UserTable.name],
    age = row[UserTable.age],
    bio = row[UserTable.bio],
    platform = spartan.exposed.codegen.Platform.valueOf(row[UserTable.platform].uppercase()),
    updatedAt = row[UserTable.updatedAt],
    deletedAt = row[UserTable.deletedAt],
    createdAt = row[UserTable.createdAt]
  )

  fun convert(resultSet: ResultSet): UserEntity = UserEntity(
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

  fun Database.update(
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

  fun Database.batchInsert(entities: List<UserEntity>, ignore: Boolean = false):
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
```

To use the generated code, you can simply extend the generated interface:

```kt
package spartan.exposed

import org.jetbrains.exposed.sql.Database
import java.util.UUID

interface UserRepository {
  fun byId(id: UUID): User?
  fun insert(entity: User): User
  fun update(
    id: UUID,
    name: String? = null,
    age: Int? = null,
    bio: String? = null,
    platform: Platform? = null,
  ): User?
}

class DefaultUserRepository(
  private val db: Database
) : UserRepository, UserEntityCrud {

  override fun byId(id: UUID): User? {
    return db.byId(id)
  }

  override fun insert(entity: User): User {
    return db.insert(entity)
  }

  override fun update(id: UUID, name: String?, age: Int?, bio: String?, platform: Platform?): User? {
    return db.update(id, name, age, bio, platform)
  }
}
```
