package com.examples.library

import com.examples.annotation.Enhancement

@Enhancement(SpyClassEnhancementHandler::class)
@Target(AnnotationTarget.CLASS)
annotation class SpyClass