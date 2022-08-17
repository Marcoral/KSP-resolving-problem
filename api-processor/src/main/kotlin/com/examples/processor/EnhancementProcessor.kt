package com.examples.processor

import com.examples.annotation.Enhancement
import com.google.devtools.ksp.closestClassDeclaration
import com.google.devtools.ksp.containingFile
import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.*
import com.google.devtools.ksp.validate
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.writeTo
import java.lang.reflect.Field
import kotlin.reflect.KClass

class EnhancementProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment) = EnhancementProcessor(environment.codeGenerator, environment.logger)
}

class EnhancementProcessor(private val codeGenerator: CodeGenerator, private val logger: KSPLogger) : SymbolProcessor {
    private val DESCRIPTOR_PACKAGE = "com.examples.descriptor"
    private val DESCRIPTOR_CLASSNAME = "Descriptor"

    private val visitors = mutableMapOf<KSClassDeclaration, EnhancementDescriptorVisitor>()

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val leftToProcess = mutableSetOf<KSAnnotated>()

        leftToProcess += resolver.processAnnotated(Enhancement::class.java.canonicalName) {
            logger.warn("Files containing classes annotated with @Enhancement: " + this.joinToString { it.containingFile!!.fileName })
            forEach {
                require(it is KSClassDeclaration)
                require(it.classKind == ClassKind.ANNOTATION_CLASS)
                leftToProcess += resolver.processEnhancementAnnotation(it)
            }
        }

        return leftToProcess.toList()
    }

    private fun Resolver.processEnhancementAnnotation(annotationClass: KSClassDeclaration) = processAnnotated(annotationClass.qualifiedName!!.asString()) {
        val visitor = visitors.computeIfAbsent(annotationClass) { EnhancementDescriptorVisitor(annotationClass) }
        forEach {
            it.accept(visitor, Unit)
        }
    }

    private fun Resolver.processAnnotated(qualifiedAnnotationName: String, action: List<KSAnnotated>.() -> Unit): List<KSAnnotated> {
        getSymbolsWithAnnotation(qualifiedAnnotationName).toList().partition {
            it.validate()
        }.also {
            action.invoke(it.first)
        }.let {
            return it.second
        }
    }

    override fun finish() {
        val descriptor = TypeSpec.objectBuilder(DESCRIPTOR_CLASSNAME)
            .addFunction(getFieldsFunction())
            .addFunction(getClassesFunction())
            .build()

        FileSpec.get(DESCRIPTOR_PACKAGE, descriptor).writeTo(codeGenerator, true)
    }

    private fun getFieldsFunction() = buildDescriptorFunction("fields", Field::class.asClassName(), {it.propertyDeclarations}, {
        val className = it.closestClassDeclaration()!!.toClassName()
        addStatement("%T::class.java.getDeclaredField(\"${it.simpleName.asString()}\"),", className)
    })

    private fun getClassesFunction() = buildDescriptorFunction("classes", Class::class.asClassName().parameterizedBy(STAR), {it.classDeclarations}, {
        addStatement("%T::class.java,", it.toClassName())
    })

    private fun <T> buildDescriptorFunction(funName: String, valueType: TypeName, referenceToDeclarations: (EnhancementDescriptorVisitor) -> Set<T>, builderAction: CodeBlock.Builder.(T) -> Unit): FunSpec {
        return buildDescriptorFunction(funName, valueType) {
            val listOfType = ClassName("kotlin.collections", "listOf")
            add("return %T(\n", ClassName("kotlin.collections", "mapOf"))
            visitors.values.map { visitor ->
                val declarations = referenceToDeclarations.invoke(visitor)
                if (declarations.isNotEmpty()) {
                    add("%T::class to %T(\n", visitor.annotationClass.toClassName(), listOfType)
                    declarations.forEach { builderAction.invoke(this, it) }
                    add("),\n")
                }
            }
            add(")")
        }.build()
    }

    private fun buildDescriptorFunction(funName: String, valueType: TypeName, bodyBuilder: CodeBlock.Builder.() -> Unit): FunSpec.Builder {
        val builder = FunSpec.builder(funName)
        val keyType = KClass::class.asTypeName().parameterizedBy(STAR)
        val collectionValueType = Collection::class.asTypeName().parameterizedBy(valueType)
        val returnType = Map::class.asTypeName().parameterizedBy(keyType, collectionValueType)
        builder.returns(returnType)

        builder.addCode(buildCodeBlock {
            withIndent {
                bodyBuilder.invoke(this)
            }
        })
        return builder
    }

    private class EnhancementDescriptorVisitor(val annotationClass: KSClassDeclaration): KSVisitorVoid() {
        val classDeclarations = mutableSetOf<KSClassDeclaration>()
        val propertyDeclarations = mutableSetOf<KSPropertyDeclaration>()

        override fun visitPropertyDeclaration(property: KSPropertyDeclaration, data: Unit) {
            this.propertyDeclarations += property
        }

        override fun visitClassDeclaration(classDeclaration: KSClassDeclaration, data: Unit) {
            this.classDeclarations += classDeclaration
        }

        //Remaining methods were skipped for brevity
    }
}