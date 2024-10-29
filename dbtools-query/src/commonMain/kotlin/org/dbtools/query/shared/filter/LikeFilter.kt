package org.dbtools.query.shared.filter

import org.dbtools.query.shared.QueryBuilder

class LikeFilter(
    field: String,
    value: Any?,
    var ignoreCase: Boolean = true,
) : CompareFilter(field, null, value) {

    override fun build(queryBuilder: QueryBuilder): String {
        val clause = if (ignoreCase) {
            queryBuilder.formatIgnoreCaseLikeClause(field.toString(), queryBuilder.formatValue(value).toString())
        } else {
            queryBuilder.formatLikeClause(field.toString(), queryBuilder.formatValue(value).toString())
        }
        return clause
    }

    override fun and(field: String, value: Any): LikeFilter {
        and(create(field, value))
        return this
    }

    fun and(field: String, value: Any, ignoreCase: Boolean): LikeFilter {
        and(create(field, value, ignoreCase))
        return this
    }

    override fun or(field: String, value: Any): LikeFilter {
        or(create(field, value))
        return this
    }

    fun or(field: String, value: Any, ignoreCase: Boolean): LikeFilter {
        or(create(field, value, ignoreCase))
        return this
    }

    companion object {
        fun create(field: String, value: Any): LikeFilter {
            val filterFormatter = LikeFilter(field, value)
            filterFormatter.filter = newInstance(field, value)
            return filterFormatter
        }

        fun create(field: String, value: Any, ignoreCase: Boolean): LikeFilter {
            val filterFormatter = LikeFilter(field, value, ignoreCase)
            filterFormatter.filter = newInstance(field, value, ignoreCase)
            return filterFormatter
        }

        private fun newInstance(field: String, value: Any): LikeFilter {
            return LikeFilter(field, value, true)
        }

        private fun newInstance(field: String, value: Any, ignoreCase: Boolean): LikeFilter {
            return LikeFilter(field, value, ignoreCase)
        }
    }
}
