package io.felipepoliveira.fpmtoolkit.dao

/**
 * Represents the pagination metadata object
 */
class Pagination(
    val itemsPerPage: Int,
    val totalRecords: Long,
    val currentPage: Int,
    val totalPages: Int,
)