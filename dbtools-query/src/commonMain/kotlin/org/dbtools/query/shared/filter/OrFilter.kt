package org.dbtools.query.shared.filter

import org.dbtools.query.shared.QueryBuilder

class OrFilter : ConjunctionFilter() {
    override fun build(queryBuilder: QueryBuilder): String = buildString {
        var isFirst = true
        for (filter in filters) {
            if (!isFirst) {
                append(" OR ")
            }
            val wrap = filter is ConjunctionFilter || filter.filter is ConjunctionFilter
            val tmp = filter.buildFilter(queryBuilder)
            if (wrap && tmp.isNotEmpty()) {
                append('(').append(tmp).append(')')
            } else {
                append(tmp)
            }
            isFirst = false
        }
    }

    override fun and(vararg filters: Filter): OrFilter {
        super.and(*filters)
        return this
    }

    override fun or(vararg filters: Filter): OrFilter {
        require(filters.isNotEmpty()) { "Must pass in at least one filter" }
        if (filter != null) {
            filter?.or(*filters)
            return this
        }

        filters.forEach { filter ->
            // Loop through filters and check for OrFilters
            when {
                filter.filter != null -> { // has sub filter or that sub filter
                    this.or(filter.filter!!)
                }

                filter is OrFilter -> { // if OrFilter add ored filters (No Parens)
                    this.filters.addAll(filter.filters)
                }

                else -> { // Else add the filter
                    this.filters.add(filter)
                }
            }
        }
        return this
    }

    companion object {
        fun create(vararg filters: Filter): OrFilter {
            val orFilter = OrFilter()
            orFilter.filter = newInstance(*filters)
            return orFilter
        }

        fun create(filter: Filter, filters: Array<Filter>): OrFilter {
            val orFilter = OrFilter()
            orFilter.filter = newInstance(filter, filters)
            return orFilter
        }

        private fun newInstance(vararg filters: Filter): OrFilter {
            require(filters.isNotEmpty()) { "Must pass in at least one filter" }
            val orFilter = OrFilter()
            orFilter.or(*filters)
            return orFilter
        }

        private fun newInstance(filter: Filter, filters: Array<Filter>): OrFilter {
            val orFilter = OrFilter()
            orFilter.or(filter)
            orFilter.or(*filters)
            return orFilter
        }
    }
}
