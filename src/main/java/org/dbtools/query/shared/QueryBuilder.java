package org.dbtools.query.shared;

public abstract class QueryBuilder {
    public abstract String formatLikeClause(String field, String value);

    public abstract String formatIgnoreCaseLikeClause(String field, String value);

    public abstract Object formatValue(Object value);

    public abstract String getQueryParameter();
}
