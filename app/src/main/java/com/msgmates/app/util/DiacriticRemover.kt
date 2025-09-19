package com.msgmates.app.util

object DiacriticRemover {

    private val diacriticMap = mapOf(
        'ç' to 'c', 'Ç' to 'C',
        'ğ' to 'g', 'Ğ' to 'G',
        'ı' to 'i', 'İ' to 'I',
        'ö' to 'o', 'Ö' to 'O',
        'ş' to 's', 'Ş' to 'S',
        'ü' to 'u', 'Ü' to 'U'
    )

    /**
     * Removes Turkish diacritics from text for search purposes
     * @param text Input text
     * @return Text with diacritics removed
     */
    fun removeDiacritics(text: String): String {
        return text.map { char ->
            diacriticMap[char] ?: char
        }.joinToString("")
    }

    /**
     * Normalizes text for search (lowercase + remove diacritics)
     * @param text Input text
     * @return Normalized text for search
     */
    fun normalizeForSearch(text: String): String {
        return removeDiacritics(text.lowercase())
    }

    /**
     * Checks if two texts match when normalized (case and diacritic insensitive)
     * @param text1 First text
     * @param text2 Second text
     * @return True if texts match when normalized
     */
    fun matches(text1: String, text2: String): Boolean {
        return normalizeForSearch(text1) == normalizeForSearch(text2)
    }

    /**
     * Checks if search query matches text (contains match)
     * @param query Search query
     * @param text Text to search in
     * @return True if query matches text
     */
    fun containsMatch(query: String, text: String): Boolean {
        val normalizedQuery = normalizeForSearch(query)
        val normalizedText = normalizeForSearch(text)
        return normalizedText.contains(normalizedQuery)
    }
}
