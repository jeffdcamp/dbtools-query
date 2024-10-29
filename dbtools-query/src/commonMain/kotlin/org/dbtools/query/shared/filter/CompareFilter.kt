package org.dbtools.query.shared.filter

import org.dbtools.query.shared.CompareType
import org.dbtools.query.shared.QueryBuilder

open class CompareFilter(
    protected var field: String? = null,
    protected var compareType: CompareType? = null,
    protected var value: Any? = null
) : Filter() {
    override fun build(queryBuilder: QueryBuilder): String = buildString {
        append(field)
        when (compareType) {
            CompareType.EQUAL -> append(" = ")
            CompareType.NOT_EQUAL -> append(" != ")
            CompareType.LESSTHAN -> append(" < ")
            CompareType.GREATERTHAN -> append(" > ")
            CompareType.LESSTHAN_EQUAL -> append(" <= ")
            CompareType.GREATERTHAN_EQUAL -> append(" >= ")
            CompareType.NOT_NULL -> append(" NOT NULL")
            CompareType.IS_NULL -> append(" IS NULL")
            else -> error("Invalid QueryCompareType: $compareType")
        }

        if (value != null) {
            append(queryBuilder.formatValue(value))
        }
    }

    open fun and(field: String, value: Any): CompareFilter {
        and(newInstance(field, CompareType.EQUAL, value))
        return this
    }

    fun and(field: String, compareType: CompareType, value: Any): CompareFilter {
        and(newInstance(field, compareType, value))
        return this
    }

    fun and(field: String, compareType: CompareType): CompareFilter {
        and(newInstance(field, compareType))
        return this
    }

    open fun or(field: String, value: Any): CompareFilter {
        or(newInstance(field, CompareType.EQUAL, value))
        return this
    }

    fun or(field: String, compareType: CompareType, value: Any): CompareFilter {
        or(newInstance(field, compareType, value))
        return this
    }

    fun or(field: String, compareType: CompareType): CompareFilter {
        or(newInstance(field, compareType))
        return this
    }

    companion object {
        fun create(field: String, value: Any): CompareFilter {
            val filter = CompareFilter()
            filter.filter = newInstance(field, CompareType.EQUAL, value)
            return filter
        }

        fun create(field: String, compareType: CompareType, value: Any): CompareFilter {
            val filter = CompareFilter()
            filter.filter = newInstance(field, compareType, value)
            return filter
        }

        fun create(field: String, compareType: CompareType): CompareFilter {
            val filter = CompareFilter()
            filter.filter = newInstance(field, compareType)
            return filter
        }

        private fun newInstance(field: String, compareType: CompareType, value: Any): CompareFilter {
            val filter = when (compareType) {
                CompareType.LIKE -> LikeFilter.create(field, value, false)
                CompareType.LIKE_IGNORECASE -> LikeFilter.create(field, value, true)
                CompareType.IN -> InFilter.create(field, true, value)
                CompareType.NOT_IN -> InFilter.create(field, false, value)
                CompareType.IS_NULL -> NullFilter.create(field, true)
                CompareType.NOT_NULL -> NullFilter.create(field, false)
                else -> CompareFilter(field, compareType, value)
            }
            return filter
        }

        private fun newInstance(field: String, compareType: CompareType): CompareFilter {
            return when (compareType) {
//                CompareType.IS_NULL, CompareType.NOT_NULL -> newInstance(field, compareType, null)
                CompareType.IS_NULL -> NullFilter.create(field, true)
                CompareType.NOT_NULL -> NullFilter.create(field, false)
                else -> throw IllegalArgumentException("Illegal 1 argument compare $compareType")
            }
        }
    }
}
