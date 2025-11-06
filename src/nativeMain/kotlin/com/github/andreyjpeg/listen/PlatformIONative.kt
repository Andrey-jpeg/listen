package com.github.andreyjpeg.listen

actual fun printError(message: String) {
    println("Error: $message")
}

actual fun exitApplication(status: Int): Nothing {
    kotlin.system.exitProcess(status)
}
