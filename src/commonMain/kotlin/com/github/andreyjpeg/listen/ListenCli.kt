package com.github.andreyjpeg.listen

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.PrintMessage
import com.github.ajalt.clikt.core.main
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.convert
import kotlinx.coroutines.runBlocking

fun listenMain(args: Array<String>) {
    ListenCommand().main(args)
}

class ListenCommand(
    private val resolverFactory: () -> SongLinkResolver = { SongLinkResolver() }
) : CliktCommand(
    name = "listen"
) {

    private val targetPlatformOption by option(
        "-p",
        "--platform",
        help = "Target platform for the converted link. Supported options: ${StreamingPlatform.supportedOptions()}"
    ).convert { input ->
        try {
            StreamingPlatform.fromKey(input)
        } catch (ex: IllegalArgumentException) {
            exitWithError(ex.message ?: "Unsupported platform: $input")
        }
    }

    private val sourceUrl by argument(
        name = "SOURCE_URL",
        help = "Song URL to translate."
    )

    override fun run() {
        val resolver = resolverFactory()

        try {
            val results = try {
                runBlocking { resolver.resolveAll(sourceUrl) }
            } catch (ex: SongLinkResolverException) {
                exitWithError(ex.message ?: "Failed to query song.link.")
            }

            if (results.isEmpty()) {
                exitWithError("Song link does not provide streaming URLs for the provided source.")
            }

            if (targetPlatformOption != null) {
                val targetPlatform = targetPlatformOption!!
                val result = results.firstOrNull { it.targetPlatform == targetPlatform }
                    ?: exitWithError("Song link does not provide a ${targetPlatform.displayName} URL")
                copyResultToClipboard(result)
            } else {
                presentSelection(results)
            }
        } finally {
            resolver.close()
        }
    }

    private fun presentSelection(results: List<SongLinkResult>) {
        if (results.size == 1) {
            copyResultToClipboard(results.first())
            return
        }

        if (!isInteractiveTerminal()) {
            presentSelectionFallback(results)
            return
        }

        var selected: SongLinkResult? = null
        val rawModeEnabled = withTerminalRawMode {
            var selectedIndex = 0
            renderSelection(results, selectedIndex)

            while (selected == null) {
                when (readInputKey()) {
                    InputKey.UP -> {
                        selectedIndex = if (selectedIndex == 0) results.lastIndex else selectedIndex - 1
                        renderSelection(results, selectedIndex)
                    }

                    InputKey.DOWN -> {
                        selectedIndex = (selectedIndex + 1) % results.size
                        renderSelection(results, selectedIndex)
                    }

                    InputKey.ENTER, InputKey.EOF -> {
                        clearSelectionDisplay()
                        selected = results[selectedIndex]
                    }

                    else -> {
                        // Ignore unsupported keys
                    }
                }
            }
        }

        if (!rawModeEnabled || selected == null) {
            presentSelectionFallback(results)
            return
        }

        copyResultToClipboard(selected!!)
    }

    private fun renderSelection(results: List<SongLinkResult>, selectedIndex: Int) {
        val builder = StringBuilder()
        builder.append(ANSI_CLEAR_SCREEN)
        builder.appendLine("Use UP/DOWN to choose a platform, press Enter to copy.")

        results.forEachIndexed { index, result ->
            val isSelected = index == selectedIndex
            val pointer = if (isSelected) "${ANSI_BRIGHT_WHITE}>${ANSI_RESET}" else " "

            if (isSelected) {
                builder.append(ANSI_BOLD)
            }

            builder.append(pointer)
            builder.append(' ')
            builder.append(platformColor(result.targetPlatform))
            builder.append(result.targetPlatform.displayName)
            builder.append(ANSI_RESET)

            if (isSelected) {
                builder.append(ANSI_BOLD)
            }

            builder.append("  ")
            builder.append(result.convertedUrl)

            if (isSelected) {
                builder.append(ANSI_RESET)
            }

            builder.append('\n')
        }

        print(builder.toString())
        flushStdout()
    }

    private fun clearSelectionDisplay() {
        print(ANSI_CLEAR_SCREEN)
        flushStdout()
    }

    private fun platformColor(platform: StreamingPlatform): String = when (platform) {
        StreamingPlatform.APPLE_MUSIC -> ANSI_MAGENTA
        StreamingPlatform.SPOTIFY -> ANSI_GREEN
        StreamingPlatform.YOUTUBE_MUSIC -> ANSI_RED
        StreamingPlatform.YOUTUBE -> ANSI_RED
        StreamingPlatform.AMAZON_MUSIC -> ANSI_YELLOW
        StreamingPlatform.DEEZER -> ANSI_CYAN
        StreamingPlatform.TIDAL -> ANSI_BLUE
    }

    private fun presentSelectionFallback(results: List<SongLinkResult>) {
        println("Available streaming platforms:")
        results.forEachIndexed { index, result ->
            println("${index + 1}. ${result.targetPlatform.displayName} - ${result.convertedUrl}")
        }

        val defaultIndex = 1
        while (true) {
            print("Select a platform [1-${results.size}] (press Enter for ${results[defaultIndex - 1].targetPlatform.displayName}): ")
            val input = readlnOrNull()?.trim() ?: ""
            val selectedIndex = when {
                input.isEmpty() -> defaultIndex
                else -> input.toIntOrNull()
            }

            if (selectedIndex != null && selectedIndex in 1..results.size) {
                val selectedResult = results[selectedIndex - 1]
                copyResultToClipboard(selectedResult)
                return
            }

            println("Invalid selection. Please enter a number between 1 and ${results.size}.")
        }
    }

    private fun copyResultToClipboard(result: SongLinkResult) {
        val copyAttempt = runCatching { copyToClipboard(result.convertedUrl) }
        if (copyAttempt.isSuccess) {
            println("${result.targetPlatform.displayName} URL copied to clipboard.")
        } else {
            val reason = copyAttempt.exceptionOrNull()?.message ?: "unknown error"
            printError("Failed to copy to clipboard: $reason")
        }

        println(result.convertedUrl)
    }

    private fun exitWithError(message: String): Nothing {
        printError(message)
        exitApplication(1)
    }

    companion object {
        private const val ANSI_RESET = "\u001B[0m"
        private const val ANSI_CLEAR_SCREEN = "\u001B[2J\u001B[H"
        private const val ANSI_BOLD = "\u001B[1m"
        private const val ANSI_BRIGHT_WHITE = "\u001B[97m"
        private const val ANSI_GREEN = "\u001B[32m"
        private const val ANSI_RED = "\u001B[31m"
        private const val ANSI_MAGENTA = "\u001B[35m"
        private const val ANSI_YELLOW = "\u001B[33m"
        private const val ANSI_CYAN = "\u001B[36m"
        private const val ANSI_BLUE = "\u001B[34m"
    }
}
