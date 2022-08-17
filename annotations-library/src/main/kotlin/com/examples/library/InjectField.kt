package com.examples.library

import com.examples.annotation.Enhancement

@Enhancement(InjectFieldEnhancementHandler::class)
@Target(AnnotationTarget.PROPERTY)
annotation class InjectField