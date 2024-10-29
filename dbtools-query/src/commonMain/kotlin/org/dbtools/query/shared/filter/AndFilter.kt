package org.dbtools.query.shared.filter

import org.dbtools.query.shared.QueryBuilder

class AndFilter : ConjunctionFilter() {
    override fun build(queryBuilder: QueryBuilder): String = buildString {
        var isFirst = true
        filters.forEach { filter ->
            if (!isFirst) {
                append(" AND ")
            }
            val wrap: Boolean = filter is ConjunctionFilter || filter is ConjunctionFilter
            val tmp: String = filter.build(queryBuilder)
            if (wrap && tmp.isNotEmpty()) {
                append('(').append(tmp).append(')')
            } else {
                append(tmp)
            }
            isFirst = false
        }
    }

    override fun and(vararg filters: Filter): AndFilter {
        require(filters.isNotEmpty()) { "Must pass in at least one filter" }
        if (filter != null) {
            filter?.and(*filters)
            return this
        }

        // Loop through filters and check for AndFilters
        filters.forEach { filter ->
            when {
                filter.filter != null -> { // has sub filter and that sub filter
                    filter.filter?.let { this.and(it) }
                }

                filter is AndFilter -> { // if AndFilter add anded filters (No Parens)
                    this.filters.addAll(filter.filters)
                }

                else -> { // Else add the filter
                    this.filters.add(filter)
                }
            }
        }
        return this
    }

    override fun or(vararg filters: Filter): AndFilter {
        super.or(*filters)
        return this
    }

    companion object {
        fun create(vararg filters: Filter): AndFilter {
            val andFilter = AndFilter()
            andFilter.filter = newInstance(*filters)
            return andFilter
        }

        fun create(filter: Filter, filters: Array<Filter>): AndFilter {
            val andFilter = AndFilter()
            andFilter.filter = newInstance(filter, filters)
            return andFilter
        }

        fun create2(filter: Filter, filters: Array<Filter>): AndFilter {
            val andFilter = AndFilter()
            andFilter.filter = newInstance(filter, filters)
            return andFilter
        }

        private fun newInstance(vararg filters: Filter): AndFilter {
            require(filters.isNotEmpty()) { "Must pass in at least one filter" }
            val andFilterFormatter = AndFilter()
            andFilterFormatter.and(*filters)
            return andFilterFormatter
        }

        private fun newInstance(filter: Filter, filters: Array<Filter>): AndFilter {
            val andFilterFormatter = AndFilter()
            andFilterFormatter.and(filter)
            andFilterFormatter.and(*filters)
            return andFilterFormatter
        }
    }
}
