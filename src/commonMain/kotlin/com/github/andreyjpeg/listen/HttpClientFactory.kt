package com.github.andreyjpeg.listen

import io.ktor.client.HttpClient

internal expect fun createHttpClient(): HttpClient
