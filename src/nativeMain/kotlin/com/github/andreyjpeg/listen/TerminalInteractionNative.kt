package com.github.andreyjpeg.listen

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.alloc
import kotlinx.cinterop.get
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.sizeOf
import kotlinx.cinterop.set
import platform.posix.ECHO
import platform.posix.EOF
import platform.posix.ICANON
import platform.posix.TCSANOW
import platform.posix.VMIN
import platform.posix.VTIME
import platform.posix.fflush
import platform.posix.getchar
import platform.posix.isatty
import platform.posix.memcpy
import platform.posix.stdout
import platform.posix.tcgetattr
import platform.posix.tcsetattr
import platform.posix.termios
import platform.posix.STDIN_FILENO

@OptIn(ExperimentalForeignApi::class)
actual fun readInputKey(): InputKey {
    val first = getchar()
    if (first == EOF) return InputKey.EOF

    return when (first) {
        10, 13 -> InputKey.ENTER
        27 -> readEscapeSequence()
        else -> InputKey.UNKNOWN
    }
}

@OptIn(ExperimentalForeignApi::class)
private fun readEscapeSequence(): InputKey {
    val second = getchar()
    if (second == EOF) return InputKey.UNKNOWN
    if (second != 91) return InputKey.UNKNOWN

    return when (getchar()) {
        65 -> InputKey.UP
        66 -> InputKey.DOWN
        else -> InputKey.UNKNOWN
    }
}

@OptIn(ExperimentalForeignApi::class)
actual fun isInteractiveTerminal(): Boolean = isatty(STDIN_FILENO) == 1

@OptIn(ExperimentalForeignApi::class)
actual fun flushStdout() {
    fflush(stdout)
}

@OptIn(ExperimentalForeignApi::class)
actual fun withTerminalRawMode(block: () -> Unit): Boolean {
    if (!isInteractiveTerminal()) return false

    var enabled = false

    memScoped {
        val fd = STDIN_FILENO
        val original = alloc<termios>()
        if (tcgetattr(fd, original.ptr) != 0) {
            return@memScoped
        }

        val raw = alloc<termios>()
        memcpy(raw.ptr, original.ptr, sizeOf<termios>().toULong())
        raw.c_lflag = raw.c_lflag and ICANON.toULong().inv()
        raw.c_lflag = raw.c_lflag and ECHO.toULong().inv()
        raw.c_cc[VMIN.toInt()] = 1.toUByte()
        raw.c_cc[VTIME.toInt()] = 0.toUByte()

        if (tcsetattr(fd, TCSANOW, raw.ptr) != 0) {
            return@memScoped
        }

        enabled = true

        try {
            block()
        } finally {
            tcsetattr(fd, TCSANOW, original.ptr)
        }
    }

    return enabled
}
