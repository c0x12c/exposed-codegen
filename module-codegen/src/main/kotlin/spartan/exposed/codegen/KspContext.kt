package spartan.exposed.codegen

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver

/**
 * A context for code generation which captures the [Resolver] and [KSPLogger]
 * https://kotlinlang.org/docs/ksp-overview.html
 */
interface KspContext {
  val resolver: Resolver
  val logger: KSPLogger

  companion object {

    fun from(
      resolver: Resolver,
      logger: KSPLogger
    ): KspContext {
      return object : KspContext {
        override val resolver: Resolver = resolver
        override val logger: KSPLogger = logger
      }
    }
  }
}
