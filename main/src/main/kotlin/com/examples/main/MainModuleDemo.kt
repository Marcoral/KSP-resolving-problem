package com.examples.main

import com.examples.annotation.ApplicationContext
import com.examples.annotation.Enhancement
import com.examples.annotation.EnhancementHandler
import com.examples.library.InjectField
import com.examples.library.LibraryAnnotationDemo
import com.examples.library.SpyClass
import java.lang.reflect.Field
import kotlin.reflect.KClass

/* Although this class is identical to LibraryAnnotationDemo from the ":annotations-library" module,
 *   a descriptor class has been generated for the latter, containing information about the elements marked with the
 *   @InjectField and @SpyClass annotations (the EnhancementProcessor class is responsible for generating this file).
 * The reason for this is that the annotations used are not part of the sourceset of this module,
 *   so the processor symbol does not detect them, even though the class of these annotations are marked with the @Enhancement annotation.
 * I'm looking for a way to force EnhancementProcessor to detect classes marked with @Enhancement annotation
 *   from the entire classpath (sources + dependencies, just like getDeclarationsFromPackage method does).
 * How to achieve this? */
@SpyClass
object MainModuleDemo {
    @InjectField
    private lateinit var context: ApplicationContext

    fun greet() {
        println(context.greetingMessage)
    }
}

class ApplicationContextImpl(override val greetingMessage: String): ApplicationContext

fun main() {
    bootstrap()
    LibraryAnnotationDemo.greet()
}

private fun bootstrap() {
    val context = ApplicationContextImpl("Hello from application context!")

    processDescriptorMethod<Field>("fields") {
        this.processField(it, context)
    }

    processDescriptorMethod<Class<*>>("classes") {
        this.processClass(it, context)
    }
}

private fun <T> processDescriptorMethod(methodName: String, action: EnhancementHandler.(T) -> Unit) {
    val descriptorClass = Class.forName("com.examples.descriptor.Descriptor")
    val descriptorInstance = descriptorClass.kotlin.objectInstance
    val map = descriptorClass.getDeclaredMethod(methodName).invoke(descriptorInstance) as Map<KClass<*>, Collection<T>>

    map.forEach { entry ->
        val enhancementHandler = entry.key.java.getDeclaredAnnotation(Enhancement::class.java)
            .enhancementHandlerClass
            .objectInstance!!

        entry.value.forEach {
            action.invoke(enhancementHandler, it)
        }
    }
}