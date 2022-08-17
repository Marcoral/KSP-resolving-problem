package com.examples.library

import com.examples.annotation.ApplicationContext
import com.examples.annotation.EnhancementHandler

//A sample logic for @SpyClass annotation
object SpyClassEnhancementHandler: EnhancementHandler {
    override fun processClass(processedElement: Class<*>, applicationContext: ApplicationContext) {
        println("Class ${processedElement.simpleName} is annotated with @SpyClass!")
    }
}