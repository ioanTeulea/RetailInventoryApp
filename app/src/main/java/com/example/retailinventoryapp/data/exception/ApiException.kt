package com.example.retailinventoryapp.data.exception


class ApiException(
    val code: Int = 0,
    message: String? = null,
    cause: Throwable? = null
) : Exception(message, cause) {

    override fun toString(): String {
        return "ApiException(code=$code, message=$message)"
    }
}