package com.back.standard.extentions

fun <T : Any> T?.getOrThrow(): T {
    return this ?: throw NoSuchElementException()
}