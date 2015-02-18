package org.dbtools.query.shared.filter;

import org.dbtools.query.shared.QueryBuilder;

import javax.annotation.Nonnull;

public class InFilter extends CompareFilter {

    public static InFilter create(String field, Object value) {
        InFilter filterFormatter = new InFilter();
        filterFormatter.filter = InFilter.newInstance(field, value);
        return filterFormatter;
    }

    private static InFilter newInstance(String field, Object value) {
        return new InFilter(field, value);
    }

    protected InFilter() {
        super();
    }

    protected InFilter(String field, Object value) {
        super(field, null, value);
    }

    @Override
    public String build(@Nonnull QueryBuilder queryBuilder) {
        return field + " IN (" + queryBuilder.formatValue(value) + ")";
    }

    public InFilter and(String field, Object value) {
        and(InFilter.newInstance(field, value));
        return this;
    }

    public InFilter or(String field, Object value) {
        or(InFilter.newInstance(field, value));
        return this;
    }
}
