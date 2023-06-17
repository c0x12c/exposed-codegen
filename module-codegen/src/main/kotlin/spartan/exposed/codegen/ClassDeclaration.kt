package spartan.exposed.codegen

import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.squareup.kotlinpoet.ksp.KotlinPoetKspPreview
import com.squareup.kotlinpoet.ksp.TypeParameterResolver
import com.squareup.kotlinpoet.ksp.toTypeName

/**
 * Wrapper for [KSClassDeclaration] to simplify access pattern and
 * optimize performance by calling each parsing method once.
 */
@OptIn(KotlinPoetKspPreview::class)
data class ClassDeclaration(
  val declaration: KSClassDeclaration
) {
  val type = declaration.asType(emptyList())
  val packageName = declaration.packageName.asString()

  /**
   * A property list of this class
   */
  val properties by lazy {
    declaration
      .getAllProperties()
      .map {
        val name = it.simpleName.getShortName()
        val type = it.type.resolve()
        val annotation = it.annotations.toList()
        val enum = (type.declaration as? KSClassDeclaration)?.classKind == ClassKind.ENUM_CLASS
        PropertyDeclaration(
          name = name,
          typeName = type.toTypeName(TypeParameterResolver.EMPTY),
          annotations = annotation,
          type = type,
          enum = enum
        )
      }
  }

  val typeName by lazy {
    type.toTypeName(TypeParameterResolver.EMPTY)
  }
}
