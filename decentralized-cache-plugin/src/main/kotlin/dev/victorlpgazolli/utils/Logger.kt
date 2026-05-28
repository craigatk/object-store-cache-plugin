package dev.victorlpgazolli.utils


interface Logger {
    fun log(
        logTag: String,
        context: String,
        message: String
    )
}
class SimpleLogger: Logger {
    override fun log(
        logTag: String,
        context: String,
        message: String
    ) {
        println(
            "$logTag [$context] $message"
        )
    }
}
class QuietLogger: Logger {
    override fun log(
        logTag: String,
        context: String,
        message: String
    ) {}
}