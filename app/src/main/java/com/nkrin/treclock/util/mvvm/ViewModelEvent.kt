package com.nkrin.treclock.util.mvvm

/**
 * Abstract Event from ViewModel
 */
open class ViewModelEvent

/**
 * Generic Pending Event
 */
object Pending : ViewModelEvent()

/**
 * Having Value Success Event
 */
class Success(val value: Any? = null) : ViewModelEvent()

/**
 * Generic Failed Event
 */
data class Error(val error: Throwable) : ViewModelEvent()