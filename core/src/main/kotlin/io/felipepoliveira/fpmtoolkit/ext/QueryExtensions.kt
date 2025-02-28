package io.felipepoliveira.fpmtoolkit.ext

import io.felipepoliveira.fpmtoolkit.dao.Pagination
import jakarta.persistence.Query
import kotlin.math.ceil

/**
 * Return all the results returned by the query operation
 */
@Suppress("UNCHECKED_CAST")
fun <T> Query.fetchAll(): MutableList<T> {
    val queryResult = this.resultList ?: return mutableListOf()
    return queryResult as MutableList<T>
}

fun <T> Query.fetchAllPaginated(itemsPerPage: Int, currentPage: Int): MutableList<T> {
    this.maxResults = itemsPerPage
    this.firstResult = itemsPerPage * (currentPage - 1)
    return fetchAll()
}

/**
 * Return only the first result of the query operation
 */
fun <T> Query.fetchFirst(): T? {
    val result = fetchAll<T>()
    return if (result.isEmpty()) null else result[0]
}

fun Query.fetchPagination(itemsPerPage: Int): Pagination {
    val countOfRecords = fetchFirst<Int>() ?: throw Exception("Expected an query that returned an Integer but returned null")
    val countOfPages =  ceil((countOfRecords.toDouble() / itemsPerPage))
    return Pagination(itemsPerPage, countOfRecords.toLong(), 1)
}