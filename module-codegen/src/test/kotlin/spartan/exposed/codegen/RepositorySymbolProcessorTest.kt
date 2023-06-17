@file:Suppress("SameParameterValue")

package spartan.exposed.codegen

import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import com.tschuchort.compiletesting.symbolProcessorProviders
import java.io.BufferedReader
import java.io.File
import java.nio.file.Path
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import strikt.api.expectThat
import strikt.assertions.contains
import strikt.assertions.isEqualTo
import strikt.assertions.isTrue

class RepositorySymbolProcessorTest {

  @TempDir
  lateinit var tempDir: Path

  @Test
  fun `entity is not a data class`() {
    val src = src(
      """
        package spartan.exposed.codegen

        import spartan.exposed.codegen.annotations.Repository
        import java.util.UUID

          @Repository
          class Hello(
            val id: UUID = UUID.randomUUID(),
            val two: Int = 123
          )
      """
    )
    val result = compile(src)
    expectThat(result.exitCode).isEqualTo(KotlinCompilation.ExitCode.COMPILATION_ERROR)
  }

  @Test
  fun `entity has type parameter`() {
    val src = src(
      """
        package spartan.exposed.codegen

        import java.util.UUID
        import spartan.exposed.codegen.annotations.Repository

          @Repository
          data class Hello<T>(
            val id: UUID = UUID.randomUUID(),
            val two: Int = 123
          )
      """
    )
    val result = compile(src)
    expectThat(result.exitCode).isEqualTo(KotlinCompilation.ExitCode.COMPILATION_ERROR)
    expectThat(result.messages).contains("@Repository must target data classes with no type parameters")
  }

  @Test
  fun `entity does not have id`() {
    val src = src(
      """
        package spartan.exposed.codegen

        import spartan.exposed.codegen.annotations.Repository

        @Repository
        data class Hello(
          val one: Int = 123
        )
      """
    )
    val result = compile(src)
    expectThat(result.exitCode).isEqualTo(KotlinCompilation.ExitCode.COMPILATION_ERROR)
    expectThat(result.messages).contains("@Repository must contain `id` property as UUID")
  }

  @Test
  fun `generate repository extension from entity - happy path`() {
    val src = srcFromResource("UserEntity.kt")
    val result = compile(src)
    expectThat(result.exitCode).isEqualTo(KotlinCompilation.ExitCode.OK)

    val generatedSrc = result.sourceFor("UserEntityCrud.kt")
    println(generatedSrc)
    generatedSrc.apply {
      expectThat(this.contains("byId")).isTrue()
      expectThat(this.contains("byAge")).isTrue()
      expectThat(this.contains("byName")).isTrue()
      expectThat(this.contains("byIds")).isTrue()
      expectThat(this.contains("insert")).isTrue()
      expectThat(this.contains("convert")).isTrue()
      expectThat(this.contains("ResultSet")).isTrue()
      expectThat(this.contains("batchInsert")).isTrue()
      expectThat(this.contains("deleteById")).isTrue()
    }

    val compiledResult = compile(src(generatedSrc))
    expectThat(compiledResult.exitCode).isEqualTo(KotlinCompilation.ExitCode.OK)
  }

  private fun compile(
    vararg source: SourceFile
  ): KotlinCompilation.Result {
    return KotlinCompilation()
      .apply {
        sources = source.toList()
        symbolProcessorProviders = listOf(RepositoryProcessorProvider())
        workingDir = tempDir.resolve("root").toFile()
        inheritClassPath = true
        verbose = false
      }
      .compile()
  }

  private fun KotlinCompilation.Result.sourceFor(fileName: String): String {
    val sources = kspGeneratedSources()
    return sources.find { it.name == fileName }
      ?.readText()
      ?: throw IllegalArgumentException("Could not find file $fileName in $sources")
  }

  private fun KotlinCompilation.Result.kspGeneratedSources(): List<File> {
    val kspDir = workingDir.resolve("ksp")
    val sourcesDir = kspDir.resolve("sources")
    val kotlinDir = sourcesDir.resolve("kotlin")
    val javaDir = sourcesDir.resolve("java")
    return kotlinDir.walk().toList() + javaDir.walk().toList()
  }

  private val KotlinCompilation.Result.workingDir: File
    get() = checkNotNull(outputDirectory.parentFile)

  private fun src(@Language("kotlin") contents: String): SourceFile {
    return SourceFile.kotlin("temp.kt", contents)
  }

  private fun srcFromResource(path: String): SourceFile {
    return SourceFile.kotlin("temp.kt", resourceText(path))
  }

  private fun resourceText(path: String): String =
    resourceBufferedReader(path).use { it.readText() }

  private fun resourceBufferedReader(path: String): BufferedReader =
    javaClass.classLoader.getResourceAsStream(path)!!.bufferedReader()
}
