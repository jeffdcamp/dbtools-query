package org.dbtools.query.shared.filter

import org.dbtools.query.shared.QueryBuilder

class RawFilter(private val filterString: String) : Filter() {
    override fun build(queryBuilder: QueryBuilder): String {
        return filterString
    }

    fun and(filterString: String): RawFilter {
        and(newInstance(filterString))
        return this
    }

    fun or(filterString: String): RawFilter {
        or(newInstance(filterString))
        return this
    }

    companion object {
        fun create(filterString: String): RawFilter {
            val rawFilterFormatter = RawFilter(filterString)
            rawFilterFormatter.filter = newInstance(filterString)
            return rawFilterFormatter
        }

        private fun newInstance(filterString: String): RawFilter {
            return RawFilter(filterString)
        }
    }
}
