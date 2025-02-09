package io.felipepoliveira.fpmtoolkit.commons.io

import io.felipepoliveira.fpmtoolkit.commons.i18n.I18nRegion
import java.io.InputStream

fun getResourceAsInputStream(resourcePath: String): InputStream {
    return  object {}.javaClass.getResourceAsStream(resourcePath) ?: throw Exception("Resource not found at: $resourcePath")
}

/**
 * Get a resource that is named in regional classification (for example: welcome.pt_br.html, welcome.en_us.html, etc.).
 * For this method to work the user should set the path with a `{{region}}` wildcard that will be replaced with the
 * lowercase representation of the given region parameter. For example:
 * ```kotlin
 * getLocalizedResourceAsInputStream("/mails/features/users/welcome/welcome.{{region}}.html", I18nRegion.PT_BR)
 * ```
 */
fun getLocalizedResourceAsInputStream(resourcePath: String, region: I18nRegion): InputStream {
    return getResourceAsInputStream(resourcePath.replace("{{region}}", region.toString().lowercase()))
}