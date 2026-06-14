package com.gantlab.satori.domain.util

sealed class DataResult<out T> {
    data class Success<out T>(val data: T) : DataResult<T>()
    data class Error(val message: String, val throwable: Throwable? = null) : DataResult<Nothing>()
    object Loading : DataResult<Nothing>()
}
