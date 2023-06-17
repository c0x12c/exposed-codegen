@file:OptIn(KspExperimental::class, KotlinPoetKspPreview::class, KotlinPoetKspPreview::class)

package spartan.exposed.codegen

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.getAnnotationsByType
import com.google.devtools.ksp.isAnnotationPresent
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.KSVisitorVoid
import com.google.devtools.ksp.symbol.Modifier
import com.google.devtools.ksp.validate
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asTypeName
import com.squareup.kotlinpoet.ksp.KotlinPoetKspPreview
import com.squareup.kotlinpoet.ksp.TypeParameterResolver
import com.squareup.kotlinpoet.ksp.toTypeName
import com.squareup.kotlinpoet.ksp.writeTo
import java.util.UUID
import java.util.concurrent.CopyOnWriteArrayList

class RepositorySymbolProcessor(
  private val codeGenerator: CodeGenerator,
  private val logger: KSPLogger
) : SymbolProcessor {

  companion object {
    private val uuidTypeName = UUID::class.asTypeName()
    private val entityQualifiedName = Entity::class.qualifiedName!!
  }

  private val excludedProperties = CopyOnWriteArrayList<String>()

  override fun process(resolver: Resolver): List<KSAnnotated> {
    val symbols = resolver.getSymbolsWithAnnotation(Crud::class.qualifiedName!!)
    symbols
      .filter { it is KSClassDeclaration && it.validate() }
      .forEach { it.accept(Visitor(resolver), Unit) }
    return symbols.filterNot { it.validate() }.toList()
  }

  private inner class Visitor(
    private val resolver: Resolver
  ) : KSVisitorVoid() {

    private val propertyMethods = mutableListOf<RepositoryMethod>()

    @OptIn(KotlinPoetKspPreview::class, KspExperimental::class)
    override fun visitClassDeclaration(classDeclaration: KSClassDeclaration, data: Unit) {
      val qualifiedName = classDeclaration.qualifiedName?.asString() ?: run {
        logger.error(
          "${Crud::class.simpleName} must target classes with qualified names",
          classDeclaration
        )
        return
      }

      if (!classDeclaration.isDataClass()) {
        logger.error(
          "${Crud::class.simpleName} cannot target non-data class $qualifiedName",
          classDeclaration
        )
        return
      }

      if (classDeclaration.typeParameters.any()) {
        logger.error(
          "${Crud::class.simpleName} must target data classes with no type parameters",
          classDeclaration
        )
        return
      }

      val declaration = ClassDeclaration(declaration = classDeclaration).apply {
        val noIdProperty = properties.none { property -> property.name == "id" && property.typeName == uuidTypeName }
        if (noIdProperty) {
          logger.error(
            "${Crud::class.simpleName} must contain `id` property as UUID",
            classDeclaration
          )
          return
        }

        val noEntityInterface = declaration
          .superTypes
          .map { it.toTypeName(TypeParameterResolver.EMPTY) }
          .none { it.toString().startsWith(entityQualifiedName) }

        if (noEntityInterface) {
          logger.error(
            "${Crud::class.simpleName} must implement interface ${Entity::class.simpleName}",
            classDeclaration
          )
          return
        }
      }

      classDeclaration.getAllProperties().forEach {
        it.accept(this, Unit)
      }

      val timestamp = declaration.declaration.getAnnotationsByType(Crud::class).first().timestamp
      val excludedPropertySet = excludedProperties.toSet()
      val methods = propertyMethods + listOf(
        SelectOneByIdMethod,
        DeleteByIdMethod(timestamp),
        InsertMethod(excludedPropertySet),
        ResultRowConvertMethod(excludedPropertySet),
        ResultSetConvertMethod(excludedPropertySet),
        UpdateMethod(timestamp, excludedPropertySet),
        BatchInsertMethod(excludedPropertySet)
      )
      val context = KspContext.from(resolver, logger)
      val className = classDeclaration.simpleName.asString() + "Crud"
      FileSpec
        .builder(
          packageName = declaration.packageName,
          fileName = className
        )
        .indent("  ")
        .addFileComment("!!!WARNING: File under the build folder are generated and should not be edited.")
        .apply {
          addType(
            TypeSpec
              .interfaceBuilder(className)
              .apply {
                methods.forEach { m ->
                  val imports = m.imports.toSortedSet()
                  imports.forEach { i ->
                    val lastDot = i.lastIndexOf(".")
                    addImport(i.substring(0, lastDot), i.substring(lastDot + 1))
                  }
                  m.funSpec(declaration, context).forEach { f ->
                    addFunction(f)
                  }
                }
              }
              .build()
          )
        }
        .build()
        .writeTo(codeGenerator = codeGenerator, aggregating = false)
    }

    @OptIn(KspExperimental::class)
    override fun visitPropertyDeclaration(property: KSPropertyDeclaration, data: Unit) {
      if (property.isAnnotationPresent(SelectableColumn::class)) {
        propertyMethods += SelectMethod(property)
      }
      if (property.isAnnotationPresent(ExcludeColumn::class)) {
        excludedProperties.add(property.simpleName.asString())
      }
    }
  }

  private fun KSClassDeclaration.isDataClass() =
    modifiers.contains(Modifier.DATA)
}
