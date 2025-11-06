package com.github.andreyjpeg.listen

import platform.posix.EOF
import platform.posix.fputs
import platform.posix.pclose
import platform.posix.popen

@OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)
actual fun copyToClipboard(text: String) {
    val pipe = popen("pbcopy", "w") ?: error("Failed to access clipboard: pbcopy is unavailable")

    try {
        val written = fputs(text, pipe)
        if (written == EOF) {
            error("Failed to write to clipboard")
        }
    } finally {
        pclose(pipe)
    }
}
