package io.felipepoliveira.fpmtoolkit.ext

import jakarta.persistence.Query

/**
 * Return all the results returned by the query operation
 */
@Suppress("UNCHECKED_CAST")
fun <T> Query.fetchAll(): MutableList<T> {
    val queryResult = this.resultList ?: return mutableListOf()
    return queryResult as MutableList<T>
}

/**
 * Return only the first result of the query operation
 */
fun <T> Query.fetchFirst(): T? {
    val result = fetchAll<T>()
    return if (result.isEmpty()) null else result[0]
}