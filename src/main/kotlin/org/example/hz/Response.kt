package org.example.hz


sealed class Response<out T> {
    companion object {
        fun <T> check(test: Boolean, error: () -> Error, process: () -> T): Response<T> {
            return if (test) Processed(process.invoke()) else error.invoke()
        }
    }
}

data class Processed<T>(val value: T) : Response<T>()
data class Error(val code: Code, val message: String? = null) : Response<Nothing>() {

    enum class Code {
        INTERNAL_ERROR,
        NOT_FOUND,
        ALREADY_CREATED,
        FORBIDDEN,
        INVALID_MEMBER_ID,
        MEMBERS_COUNT_EXCEEDED
    }
}