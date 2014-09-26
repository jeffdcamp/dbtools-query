package com.jdc.db.jpa.query;

import com.jdc.db.shared.query.QueryCompareType;

public class JPAFilterItem<T> {
    private JPAQueryBuilder<T> jpaQueryBuilder;
    private String field;
    private Object value;
    private QueryCompareType compare;
    private Object paramValue;
    private String paramName;
    private boolean paramFilter = false;

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
                // handled later
                filterCompare = "";
                break;
        }


        if (compare == QueryCompareType.LIKE || compare == QueryCompareType.LIKE_IGNORECASE) {
            if (value instanceof String) {
                // do nothing
            } else {
                throw new IllegalStateException("Cannot have a like clause on this filter type [String] for field [" + field + "]");
            }
            switch (compare) {
                case LIKE:
                    filter = jpaQueryBuilder.formatLikeClause(field, String.valueOf(value));
                    break;
                case LIKE_IGNORECASE:
                    filter = jpaQueryBuilder.formatIgnoreCaseLikeClause(field, String.valueOf(value));
                    break;
                default:
                    filter = field + " LIKE '%" + value + "%'";
            }

        } else if (compare == QueryCompareType.IN) {
            filter = field + " IN (" + value + ")";
        } else {
            filter = field + filterCompare + value;
        }

        return filter;
    }
}