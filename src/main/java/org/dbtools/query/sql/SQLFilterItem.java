package org.dbtools.query.sql;

import org.dbtools.query.shared.QueryCompareType;

public class SQLFilterItem {
    private SQLQueryBuilder sqlQueryBuilder;

    private String field;
    private Object value;
    private QueryCompareType compare;

    public SQLFilterItem(String field, Object value) {
        this.field = field;
        this.value = value;
        this.compare = QueryCompareType.EQUAL;
    }

    public SQLFilterItem(String field, QueryCompareType compare, Object value) {
        this.field = field;
        this.value = value;
        this.compare = compare;
    }

    public SQLFilterItem setSqlQueryBuilder(SQLQueryBuilder sqlQueryBuilder) {
        this.sqlQueryBuilder = sqlQueryBuilder;
        return this;
    }

    @Override
    public String toString() {
        String filter;

        String filterCompare;
        switch (compare) {
            default:
            case EQUAL:
                filterCompare = " = ";
                break;
            case NOT_EQUAL:
                filterCompare = " != ";
                break;
            case GREATERTHAN:
                filterCompare = " > ";
                break;
            case LESSTHAN:
                filterCompare = " < ";
                break;
            case GREATERTHAN_EQUAL:
                filterCompare = " >= ";
                break;
            case LESSTHAN_EQUAL:
                filterCompare = " <= ";
                break;
            case IN:
            case LIKE:
            case LIKE_IGNORECASE:
                // handled later
                filterCompare = "";
                break;
            case IS_NULL:
                filterCompare = " IS NULL ";
                break;
            case NOT_NULL:
                filterCompare = " NOT NULL ";
                break;
        }

        // put it all together
        switch (compare) {
            case LIKE:
                filter = sqlQueryBuilder.formatLikeClause(field, String.valueOf(value));
                break;
            case LIKE_IGNORECASE:
                filter = sqlQueryBuilder.formatIgnoreCaseLikeClause(field, String.valueOf(value));
                break;
            case IN:
                filter = field + " IN (" + value + ")";
                break;
            case IS_NULL:
                filter = field + " IS NULL ";
                break;
            case NOT_NULL:
                filter = field + " NOT NULL ";
                break;
            default:
                filter = field + filterCompare + value;
        }

        return filter;
    }
}