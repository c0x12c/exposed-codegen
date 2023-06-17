package spartan.exposed.codegen.test

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import java.time.Instant
import java.util.Properties
import java.util.UUID
import org.intellij.lang.annotations.Language
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.hasSize
import strikt.assertions.isEqualTo
import strikt.assertions.isNotNull
import strikt.assertions.isNull

class UserRepositoryTest {

  companion object {

    @Language("PostgreSQL")
    private val SQL_CREATE = """
      CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
      CREATE TABLE IF NOT EXISTS users
      (
        id         uuid DEFAULT uuid_generate_v4() NOT NULL,
        name       TEXT      NOT NULL,
        age        INTEGER   NOT NULL,
        bio        TEXT,
        platform   TEXT      NOT NULL,
        updated_at TIMESTAMP,
        deleted_at TIMESTAMP,
        created_at TIMESTAMP NOT NULL
      );
    """.trimIndent()

    @Language("PostgreSQL")
    private val SQL_DROP = "DROP TABLE users;"

    private val database by lazy {
      val config = HikariConfig(Properties().apply {
        setProperty("dataSourceClassName", "org.postgresql.ds.PGSimpleDataSource")
        setProperty("dataSource.user", "test")
        setProperty("dataSource.password", "test")
        setProperty("dataSource.databaseName", "test")
        setProperty("dataSource.serverName", "localhost")
        setProperty("dataSource.portNumber", "5432")
      })
      val source = HikariDataSource(config)
      Database.connect(datasource = source)
    }

    private val repository by lazy {
      UserRepository(database)
    }
  }

  private val user = UserEntity(
    id = UUID.randomUUID(),
    name = "Chan Nguyen",
    age = 30,
    bio = "Software Engineer",
    platform = Platform.IOS,
    updatedAt = null,
    deletedAt = null,
    createdAt = Instant.now()
  )

  @BeforeEach
  fun beforeEach() {
    execute(SQL_CREATE)
  }

  @AfterEach
  fun afterEach() {
    execute(SQL_DROP)
  }

  @Test
  fun `byId test`() {
    val entity = repository.insert(user)!!
    expectThat(repository.byId(entity.id)).isEqualTo(user)
  }

  @Test
  fun `byName test`() {
    val entity = repository.insert(user)!!
    expectThat(repository.byName(entity.name)).isEqualTo(user)
  }

  @Test
  fun `byNames test`() {
    val entity = repository.insert(user)!!
    val result = repository.byNames(listOf(entity.name))
    expectThat(result).hasSize(1)
    expectThat(result.first()).isEqualTo(user)
  }

  @Test
  fun `byAge test`() {
    val entity = repository.insert(user)!!
    expectThat(repository.byAge(entity.age)).isEqualTo(listOf(user))
  }

  @Test
  fun `byAges test`() {
    val entity = repository.insert(user)!!
    val result = repository.byAges(listOf(entity.age))
    expectThat(result).isEqualTo(listOf(user))
  }

  @Test
  fun `deleteById test`() {
    val entity = repository.insert(user)!!
    expectThat(repository.byId(entity.id)).isEqualTo(user)
    expectThat(repository.deleteById(entity.id)?.deletedAt).isNotNull()
    expectThat(repository.byId(entity.id)).isNull()
  }

  @Test
  fun `update test`() {
    val entity = repository.insert(user)!!
    expectThat(repository.update(entity.id, age = 18)?.age).isEqualTo(18)
    expectThat(repository.byId(entity.id)?.age).isEqualTo(18)
  }

  private fun execute(sql: String) {
    transaction(database) {
      val conn = TransactionManager.current().connection
      val statement = conn.prepareStatement(sql, false)
      statement.executeUpdate()
    }
  }
}
