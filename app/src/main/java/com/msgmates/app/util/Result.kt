package com.msgmates.app.util

/**
 * A generic result wrapper for operations that can succeed or fail
 */
sealed class Result<out T> {
    data class Success<out T>(val data: T) : Result<T>()
    data class Error(val exception: Throwable) : Result<Nothing>()
    object Loading : Result<Nothing>()
}

/**
 * Extension function to check if result is success
 */
fun <T> Result<T>.isSuccess(): Boolean = this is Result.Success

/**
 * Extension function to check if result is error
 */
fun <T> Result<T>.isError(): Boolean = this is Result.Error

/**
 * Extension function to check if result is loading
 */
fun <T> Result<T>.isLoading(): Boolean = this is Result.Loading

/**
 * Extension function to get data from success result
 */
fun <T> Result<T>.getDataOrNull(): T? = when (this) {
    is Result.Success -> data
    else -> null
}

/**
 * Extension function to get exception from error result
 */
fun <T> Result<T>.getExceptionOrNull(): Throwable? = when (this) {
    is Result.Error -> exception
    else -> null
}

/**
 * Extension function to map success data
 */
inline fun <T, R> Result<T>.map(transform: (T) -> R): Result<R> = when (this) {
    is Result.Success -> Result.Success(transform(data))
    is Result.Error -> this
    is Result.Loading -> this
}

/**
 * Extension function to map error exception
 */
inline fun <T> Result<T>.mapError(transform: (Throwable) -> Throwable): Result<T> = when (this) {
    is Result.Success -> this
    is Result.Error -> Result.Error(transform(exception))
    is Result.Loading -> this
}
