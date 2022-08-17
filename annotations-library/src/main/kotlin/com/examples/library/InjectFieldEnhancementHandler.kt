package com.examples.library

import com.examples.annotation.ApplicationContext
import com.examples.annotation.EnhancementHandler
import java.lang.reflect.Field

//A sample logic for @InjectField annotation
object InjectFieldEnhancementHandler: EnhancementHandler {
    override fun processField(processedElement: Field, applicationContext: ApplicationContext) {
        processedElement.isAccessible = true
        processedElement.set(null, applicationContext)
        processedElement.isAccessible = false
    }
}