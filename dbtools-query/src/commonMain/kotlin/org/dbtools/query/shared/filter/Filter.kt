package org.dbtools.query.shared.filter

import org.dbtools.query.shared.QueryBuilder
import org.dbtools.query.sql.SQLQueryBuilder

abstract class Filter(
    var filter: Filter? = null
) {
    fun buildFilter(queryBuilder: QueryBuilder): String {
        return if (filter != null) {
            filter?.buildFilter(queryBuilder).orEmpty()
        } else {
            build(queryBuilder)
        }
    }

    abstract fun build(queryBuilder: QueryBuilder): String

    open fun and(vararg filters: Filter): Filter {
        require(filters.isNotEmpty()) { "Must pass in at least one filter" }
        if (filter is AndFilter) {
            (filter as AndFilter).and(*filters)
        } else {
            filter = filter?.let { AndFilter.create(it, *filters) }
        }
        return this
    }

    open fun or(vararg filters: Filter): Filter {
        require(filters.isNotEmpty()) { "Must pass in at least one filter" }
        if (filter is OrFilter) {
            (filter as OrFilter).or(*filters)
        } else {
            filter = filter?.let { OrFilter.create(it, *filters) }
        }
        return this
    }

    override fun toString(): String {
        return buildFilter(SQLQueryBuilder())
    }
}
