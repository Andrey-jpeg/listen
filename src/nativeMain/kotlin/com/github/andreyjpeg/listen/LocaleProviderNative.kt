package com.github.andreyjpeg.listen

import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSLocale
import platform.Foundation.NSLocaleCountryCode
import platform.Foundation.currentLocale

@OptIn(ExperimentalForeignApi::class)
actual fun currentCountryCode(): String? {
    val locale = NSLocale.currentLocale()
    val country = locale.objectForKey(NSLocaleCountryCode) as? String
    return country?.takeIf { it.isNotBlank() }?.uppercase()
}
