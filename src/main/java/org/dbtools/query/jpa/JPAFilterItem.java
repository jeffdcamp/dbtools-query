package org.dbtools.query.jpa;

import org.dbtools.query.shared.QueryCompareType;

public class JPAFilterItem<T> {
    private JPAQueryBuilder<T> jpaQueryBuilder;
    private String field;
    private Object value;
    private QueryCompareType compare;
    private Object paramValue;
    private String paramName;
    private boolean paramFilter = false;

    public JPAFilterItem(String filter) {
        this.field = filter;
        this.value = null;
        this.compare = QueryCompareType.NONE;
    }

    public JPAFilterItem(String field, QueryCompareType compare, Object value) {
        this.field = field;
        this.value = value;
        this.compare = compare;
    }

    public boolean isParameterFilter() {
        return paramFilter;
    }

    public String getParamName() {
        return paramName;
    }

    public Object getParamValue() {
        return paramValue;
    }

    public JPAFilterItem<T> setJpaQueryBuilder(JPAQueryBuilder<T> jpaQueryBuilder) {
        this.jpaQueryBuilder = jpaQueryBuilder;
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
            case NONE:
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
                filter = jpaQueryBuilder.formatLikeClause(field, String.valueOf(value));
                break;
            case LIKE_IGNORECASE:
                filter = jpaQueryBuilder.formatIgnoreCaseLikeClause(field, String.valueOf(value));
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
            case NONE:
                filter = field;
                break;
            default:
                filter = field + filterCompare + value;
        }

        return filter;
    }
}