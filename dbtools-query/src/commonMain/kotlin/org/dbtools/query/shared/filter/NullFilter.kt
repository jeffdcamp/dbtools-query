package org.dbtools.query.shared.filter

import org.dbtools.query.shared.CompareType
import org.dbtools.query.shared.QueryBuilder

class NullFilter(
    field: String,
    private var isNull: Boolean
) : CompareFilter(field, if (isNull) CompareType.IS_NULL else CompareType.NOT_NULL, null) {

    override fun build(queryBuilder: QueryBuilder): String = buildString {
        append(field)
        if (isNull) {
            append(" IS NULL")
        } else {
            append(" NOT NULL")
        }
    }

    fun and(field: String): NullFilter {
        and(create(field))
        return this
    }

    fun and(field: String, isNull: Boolean): NullFilter {
        and(create(field, isNull))
        return this
    }

    fun or(field: String): NullFilter {
        or(create(field))
        return this
    }

    fun or(field: String, isNull: Boolean): NullFilter {
        or(create(field, isNull))
        return this
    }

    companion object {
        fun create(field: String): NullFilter {
            val filterFormatter = NullFilter(field, true)
            filterFormatter.filter = newInstance(field, true)
            return filterFormatter
        }

        fun create(field: String, isNull: Boolean): NullFilter {
            val filterFormatter = NullFilter(field, isNull)
            filterFormatter.filter = newInstance(field, isNull)
            return filterFormatter
        }

        private fun newInstance(field: String, isNull: Boolean): NullFilter {
            return NullFilter(field, isNull)
        }
    }
}
