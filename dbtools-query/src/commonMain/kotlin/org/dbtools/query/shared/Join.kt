package org.dbtools.query.shared

import org.dbtools.query.shared.filter.Filter

class Join(private val joinType: JoinType, private val table: String, private val filter: Filter) {
    fun buildJoin(queryBuilder: QueryBuilder): String {
        return joinType.joinText + " " + table + " ON " + filter.buildFilter(queryBuilder)
    }
}
