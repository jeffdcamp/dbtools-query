package org.dbtools.query.shared.filter

import org.dbtools.query.shared.QueryBuilder

class InParameterizedFilter(
    field: String? = null,
    isIn: Boolean = false,
    private var numParams: Int = 0
) : InFilter(field, isIn) {

    init {
        if (field != null) {
            require(numParams >= 1) { "There must be at least 1 param for an InParameterizedFilter." }
        }
    }

    override fun build(queryBuilder: QueryBuilder): String = buildString {
        append(field)
        if (isIn) {
            append(" IN ")
        } else {
            append(" NOT IN ")
        }
        append("(").append(queryBuilder.queryParameter)
        @Suppress("UnusedPrivateProperty") // todo remove i?
        for (i in 1 until numParams) {
            append(", ").append(queryBuilder.queryParameter)
        }
        append(")")
    }

    fun and(field: String, numParams: Int): InParameterizedFilter {
        and(newInstance(field, true, numParams))
        return this
    }

    fun or(field: String, numParams: Int): InParameterizedFilter {
        or(newInstance(field, true, numParams))
        return this
    }

    fun and(field: String, isIn: Boolean, numParams: Int): InParameterizedFilter {
        and(newInstance(field, isIn, numParams))
        return this
    }

    fun or(field: String, isIn: Boolean, numParams: Int): InParameterizedFilter {
        or(newInstance(field, isIn, numParams))
        return this
    }

    companion object {
        fun create(field: String, numParams: Int): InParameterizedFilter {
            val filterFormatter = InParameterizedFilter()
            filterFormatter.filter = newInstance(field, true, numParams)
            return filterFormatter
        }

        fun create(field: String, isIn: Boolean, numParams: Int): InParameterizedFilter {
            val filterFormatter = InParameterizedFilter()
            filterFormatter.filter = newInstance(field, isIn, numParams)
            return filterFormatter
        }

        private fun newInstance(field: String, isIn: Boolean, numParams: Int): InParameterizedFilter {
            return InParameterizedFilter(field, isIn, numParams)
        }
    }
}
