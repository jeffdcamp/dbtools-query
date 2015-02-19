package org.dbtools.query.shared.filter;

import org.dbtools.query.shared.QueryBuilder;
import org.dbtools.query.shared.CompareType;

import javax.annotation.Nonnull;

public class CompareFilter extends Filter {

    protected String field;
    private CompareType compareType;
    protected Object value;

    public static CompareFilter create(String field, Object value) {
        CompareFilter filter = new CompareFilter();
        filter.filter = CompareFilter.newInstance(field, CompareType.EQUAL, value);
        return filter;
    }

    public static CompareFilter create(String field, CompareType compareType, Object value) {
        CompareFilter filter = new CompareFilter();
        filter.filter = CompareFilter.newInstance(field, compareType, value);
        return filter;
    }

    public static CompareFilter create(String field, CompareType compareType) {
        CompareFilter filter = new CompareFilter();
        filter.filter = CompareFilter.newInstance(field, compareType);
        return filter;
    }

    private static CompareFilter newInstance(String field, CompareType compareType, Object value) {
        CompareFilter filter;
        switch (compareType) {
            case LIKE:
                filter = LikeFilter.create(field, value, false);
                break;
            case LIKE_IGNORECASE:
                filter = LikeFilter.create(field, value, true);
                break;
            case IN:
                filter = InFilter.create(field, true, value);
                break;
            case NOT_IN:
                filter = InFilter.create(field, false, value);
                break;
            case IS_NULL:
                filter = NullFilter.create(field, true);
                break;
            case NOT_NULL:
                filter = NullFilter.create(field, false);
                break;
            default:
                filter = new CompareFilter(field, compareType, value);
        }
        return filter;
    }

    private static CompareFilter newInstance(String field, CompareType compareType) {
        CompareFilter filter;
        switch (compareType) {
            case IS_NULL:
            case NOT_NULL:
                filter = CompareFilter.newInstance(field, compareType, null);
                break;
            default:
                throw new IllegalArgumentException("Illegal 1 argument compare " + compareType.toString());
        }
        return filter;
    }

    protected CompareFilter() {
    }

    protected CompareFilter(String field, CompareType compareType, Object value) {
        this.field = field;
        this.compareType = compareType;
        this.value = value;
    }

    @Override
    public String build(@Nonnull QueryBuilder queryBuilder) {
        StringBuilder builder = new StringBuilder(field);
        switch (compareType) {
            case EQUAL:
                builder.append(" = ");
                break;
            case NOT_EQUAL:
                builder.append(" != ");
                break;
            case LESSTHAN:
                builder.append(" < ");
                break;
            case GREATERTHAN:
                builder.append(" > ");
                break;
            case LESSTHAN_EQUAL:
                builder.append(" <= ");
                break;
            case GREATERTHAN_EQUAL:
                builder.append(" >= ");
                break;
            default:
                throw new IllegalStateException("Invalid QueryCompareType: " + compareType);
        }
        builder.append(queryBuilder.formatValue(value));
        return builder.toString();
    }

    public CompareFilter and(String field, Object value) {
        and(CompareFilter.newInstance(field, CompareType.EQUAL, value));
        return this;
    }

    public CompareFilter and(String field, CompareType compareType, Object value) {
        and(CompareFilter.newInstance(field, compareType, value));
        return this;
    }

    public CompareFilter and(String field, CompareType compareType) {
        and(CompareFilter.newInstance(field, compareType));
        return this;
    }

    public CompareFilter or(String field, Object value) {
        or(CompareFilter.newInstance(field, CompareType.EQUAL, value));
        return this;
    }

    public CompareFilter or(String field, CompareType compareType, Object value) {
        or(CompareFilter.newInstance(field, compareType, value));
        return this;
    }

    public CompareFilter or(String field, CompareType compareType) {
        or(CompareFilter.newInstance(field, compareType));
        return this;
    }


    @Override
    public CompareFilter clone() {
        CompareFilter clone = (CompareFilter) super.clone();
        clone.field = this.field;
        clone.compareType = this.compareType;
        clone.value = this.value;
        return clone;
    }
}
