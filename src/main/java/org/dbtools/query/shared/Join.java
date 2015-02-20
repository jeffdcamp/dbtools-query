package org.dbtools.query.shared;

import org.dbtools.query.shared.filter.Filter;

import javax.annotation.Nonnull;

public class Join {

    private final JoinType joinType;
    private final String table;
    private final Filter filter;

    public Join(JoinType joinType, String table, Filter filter) {
        this.joinType = joinType;
        this.table = table;
        this.filter = filter;
    }

    public String buildJoin(@Nonnull QueryBuilder queryBuilder) {
        return joinType.getJoinText() + " " + table + " ON " + filter.buildFilter(queryBuilder);
    }
}
