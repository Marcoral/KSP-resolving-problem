package com.examples.annotation

import java.lang.reflect.Field

interface EnhancementHandler {
    fun processField(processedElement: Field, applicationContext: ApplicationContext) {}
    fun processClass(processedElement: Class<*>, applicationContext: ApplicationContext) {}

    //Remaining methods were skipped for brevity
}