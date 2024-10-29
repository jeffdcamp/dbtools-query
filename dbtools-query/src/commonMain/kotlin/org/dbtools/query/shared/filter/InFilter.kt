package org.dbtools.query.shared.filter

import org.dbtools.query.shared.QueryBuilder

open class InFilter : CompareFilter {
    protected var isIn: Boolean = false
    private var subQuery: QueryBuilder? = null

    constructor() : super()

    constructor(field: String?, isIn: Boolean, value: Any? = null) : super(field, null, value) {
        require(!(value is List<*> && value.size < 1)) { "List must contain at least on item" }
        this.isIn = isIn
        this.subQuery = null
    }

    constructor(field: String?, isIn: Boolean, subQuery: QueryBuilder) : super(field, null, null) {
        this.isIn = isIn
        this.subQuery = subQuery
    }

    override fun build(queryBuilder: QueryBuilder): String = buildString {
        append(field)
        if (isIn) {
            append(" IN ")
        } else {
            append(" NOT IN ")
        }
        append("(")
        val currentSubQuery = subQuery
        if (currentSubQuery != null) {
            append(currentSubQuery.buildQuery())
        } else if (value is List<*>) {
            val list = value as List<*>
            append(queryBuilder.formatValue(list[0]))
            val count = list.size
            for (i in 1 until count) {
                append(", ").append(queryBuilder.formatValue(list[i]))
            }
        } else {
            append(queryBuilder.formatValue(value))
        }
        return append(")").toString()
    }

    override fun and(field: String, value: Any): InFilter {
        and(newInstance(field, true, value))
        return this
    }

    override fun or(field: String, value: Any): InFilter {
        or(newInstance(field, true, value))
        return this
    }

    fun and(field: String, isIn: Boolean, value: Any): InFilter {
        and(newInstance(field, isIn, value))
        return this
    }

    fun or(field: String, isIn: Boolean, value: Any): InFilter {
        or(newInstance(field, isIn, value))
        return this
    }

    fun and(field: String, queryBuilder: QueryBuilder): InFilter {
        and(newInstance(field, true, queryBuilder))
        return this
    }

    fun or(field: String, queryBuilder: QueryBuilder): InFilter {
        or(newInstance(field, true, queryBuilder))
        return this
    }

    fun and(field: String, isIn: Boolean, queryBuilder: QueryBuilder): InFilter {
        and(newInstance(field, isIn, queryBuilder))
        return this
    }

    fun or(field: String, isIn: Boolean, queryBuilder: QueryBuilder): InFilter {
        or(newInstance(field, isIn, queryBuilder))
        return this
    }

    companion object {
        fun create(field: String, value: Any): InFilter {
            val filter = InFilter()
            filter.filter = newInstance(field, true, value)
            return filter
        }

        fun create(field: String, isIn: Boolean, values: Any): InFilter {
            val filter = InFilter()
            filter.filter = newInstance(field, isIn, values)
            return filter
        }

        fun create(field: String, subQuery: QueryBuilder): InFilter {
            val filter = InFilter()
            filter.filter = newInstance(field, true, subQuery)
            return filter
        }

        fun create(field: String, isIn: Boolean, subQuery: QueryBuilder): InFilter {
            val filter = InFilter()
            filter.filter = newInstance(field, isIn, subQuery)
            return filter
        }

        private fun newInstance(field: String, isIn: Boolean, values: Any): InFilter {
            return InFilter(field, isIn, values)
        }

        private fun newInstance(field: String, isIn: Boolean, subQuery: QueryBuilder): InFilter {
            return InFilter(field, isIn, subQuery)
        }
    }
}
