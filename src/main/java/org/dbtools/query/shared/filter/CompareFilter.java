package org.dbtools.query.shared.filter;

import org.dbtools.query.shared.QueryBuilder;
import org.dbtools.query.shared.CompareType;

import javax.annotation.Nonnull;

public class CompareFilter extends Filter {

    protected String field;
    private CompareType compareType;
    protected Object value;

    public static CompareFilter create(String field, Object value) {
        CompareFilter filterFormatter = new CompareFilter();
        filterFormatter.filter = CompareFilter.newInstance(field, value);
        return filterFormatter;
    }

    public static CompareFilter create(String field, CompareType compareType, Object value) {
        CompareFilter filterFormatter = new CompareFilter();
        filterFormatter.filter = CompareFilter.newInstance(field, compareType, value);
        return filterFormatter;
    }

    private static CompareFilter newInstance(String field, CompareType compareType, Object value) {
        CompareFilter filterFormatter;
        switch (compareType) {
            case LIKE:
                filterFormatter = LikeFilter.create(field, value, true);
                break;
            case LIKE_IGNORECASE:
                filterFormatter = LikeFilter.create(field, value, true);
                break;
            case IN:
                filterFormatter = InFilter.create(field, value);
                break;
            case IS_NULL:
                filterFormatter = NullFilter.create(field, true);
                break;
            case NOT_NULL:
                filterFormatter = NullFilter.create(field, false);
                break;
            default:
                filterFormatter = new CompareFilter(field, compareType, value);
                break;
        }
        return filterFormatter;
    }

    private static CompareFilter newInstance(String field, Object value) {
        return new CompareFilter(field, CompareType.EQUAL, value);
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
                throw new IllegalArgumentException("Invalid QueryCompareType: " + compareType);
        }
        builder.append(queryBuilder.formatValue(value));
        return builder.toString();
    }

    public CompareFilter and(String field, Object value) {
        and(CompareFilter.newInstance(field, value));
        return this;
    }

    public CompareFilter and(String field, CompareType compareType, Object value) {
        and(CompareFilter.newInstance(field, compareType, value));
        return this;
    }

    public CompareFilter or(String field, Object value) {
        or(CompareFilter.newInstance(field, value));
        return this;
    }

    public CompareFilter or(String field, CompareType compareType, Object value) {
        or(CompareFilter.newInstance(field, compareType, value));
        return this;
    }

    @Override
    public CompareFilter clone() throws CloneNotSupportedException {
        CompareFilter clone = (CompareFilter) super.clone();
        clone.field = this.field;
        clone.compareType = this.compareType;
        clone.value = this.value;
        return clone;
    }
}
