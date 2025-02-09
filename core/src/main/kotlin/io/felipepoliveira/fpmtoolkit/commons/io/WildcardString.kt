package io.felipepoliveira.fpmtoolkit.commons.io

import java.io.IOException
import java.io.InputStream

class WildcardString(
    /**
     * The text that generated the WildcardString
     */
    private val inputText: String
) {

    /**
     * Create a WildcardString instance based on the content of the given input stream
     */
    constructor(inputStream: InputStream) : this(String(inputStream.readAllBytes()))

    /**
     * Store the processed input text reference
     */
    private val processedInputText: String? = null

    /**
     * Store the wildcards with its values
     */
    private val wildcardsAndValues: MutableMap<String, String?> = mutableMapOf()

    /**
     * Load the text of the wildcard string
     */
    init {
        loadWildcardsFromInputText(inputText)
    }


    /**
     * Load the wildcards map based on the input text
     */
    private fun loadWildcardsFromInputText(inputText: String) {
        val regex = Regex("\\{\\{(.*?)}}")
        val wildcards =  regex.findAll(inputText).map { it.groupValues[1] }.toList()
        wildcards.forEach{ w -> wildcardsAndValues[w] = null}
    }

    fun add(wildcard: String, value: String): WildcardString {

        // check if the given wildcard was loaded in the wildcard string
        if (!wildcardsAndValues.containsKey(wildcard)) {
            throw IllegalArgumentException(
                "Wildcard `$wildcard` does not exists on the given context. " +
                    "Loaded: ${wildcardsAndValues.keys.joinToString(", ")}"
            )
        }

        // include the value into the wildcards
        wildcardsAndValues[wildcard] = value

        return this
    }

    /**
     * Return the processed content of the wildcard text. If the parameter strict is true this method
     * will throw an exception to any of the wildcards is not set
     */
    fun toString(strict: Boolean = true): String {
        // if the strict mode is set, throw an exception if any wildcard is not set
        if (strict) {
            val notSetWildcards = wildcardsAndValues.values.filter { v -> v == null }
            if (notSetWildcards.isNotEmpty()) {
                throw IOException("Found wildcards that was not set: ${notSetWildcards.joinToString(", ")}")
            }
        }

        //
        var processedInputText = String(inputText.toCharArray())
        for (entry in wildcardsAndValues) {
            processedInputText = processedInputText.replace("{{${entry.key}}}", entry.value ?: "")
        }

        return processedInputText
    }

    override fun toString(): String = toString(true)

}