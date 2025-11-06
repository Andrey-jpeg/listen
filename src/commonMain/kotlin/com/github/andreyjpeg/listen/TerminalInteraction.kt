package com.github.andreyjpeg.listen

enum class InputKey {
    UP,
    DOWN,
    ENTER,
    EOF,
    UNKNOWN
}

expect fun readInputKey(): InputKey

expect fun isInteractiveTerminal(): Boolean

expect fun flushStdout()

expect fun withTerminalRawMode(block: () -> Unit): Boolean
