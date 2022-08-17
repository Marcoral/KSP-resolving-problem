package com.examples.annotation

import kotlin.reflect.KClass

@Target(AnnotationTarget.ANNOTATION_CLASS)
annotation class Enhancement(val enhancementHandlerClass: KClass<out EnhancementHandler>)