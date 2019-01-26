package com.nkrin.treclock.util

import org.mockito.ArgumentCaptor
import org.mockito.Mockito

fun <T> capture(argumentCaptor: ArgumentCaptor<T>): T = argumentCaptor.capture()

private fun <T> nullReturn(): T = null as T

fun <T> isA(type: Class<T>): T {
    Mockito.isA<T>(type)
    return nullReturn()
}

fun <T> any(): T {
    Mockito.any<T>()
    return nullReturn()
}
