package com.examples.library

import com.examples.annotation.ApplicationContext
import com.examples.annotation.Enhancement
import com.examples.annotation.EnhancementHandler
import java.lang.reflect.Field
import kotlin.reflect.KClass

/*
 * This class is just to show that for the field marked @InjectField and class marked @SpyClass
 *  will be listed in a generated Descriptor class.
 * The problem is that such annotated elements, will not be detected in the "main" module
 *  (see MainModuleDemo) i. e. @InjectField and @SpyClass annotation classes will not be resolved when asked about symbols annotated with @Enhancement */
@SpyClass
object LibraryAnnotationDemo {
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