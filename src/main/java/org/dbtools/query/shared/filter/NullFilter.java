package org.dbtools.query.shared.filter;

import org.dbtools.query.shared.QueryBuilder;

import javax.annotation.Nonnull;

public class NullFilter extends CompareFilter {

    private boolean isNull;

    public static NullFilter create(String field) {
        NullFilter filterFormatter = new NullFilter();
        filterFormatter.filter = NullFilter.newInstance(field);
        return filterFormatter;
    }

    public static NullFilter create(String field, boolean isNull) {
        NullFilter filterFormatter = new NullFilter();
        filterFormatter.filter = NullFilter.newInstance(field, isNull);
        return filterFormatter;
    }

    private static NullFilter newInstance(String field) {
        return new NullFilter(field, true);
    }

    private static NullFilter newInstance(String field, boolean isNull) {
        return new NullFilter(field, isNull);
    }

    private NullFilter() {
        super();
    }

    private NullFilter(String field, boolean isNull) {
        super(field, null, null);
        this.isNull = isNull;
    }

    @Override
    public String build(@Nonnull QueryBuilder queryBuilder) {
        StringBuilder builder = new StringBuilder(field);
        if (isNull) {
            builder.append(" IS NULL");
        } else {
            builder.append(" NOT NULL");
        }
        return builder.toString();
    }

    public NullFilter and(String field) {
        and(NullFilter.create(field));
        return this;
    }

    public NullFilter and(String field, boolean isNull) {
        and(NullFilter.create(field, isNull));
        return this;
    }

    public NullFilter or(String field) {
        or(NullFilter.create(field));
        return this;
    }

    public NullFilter or(String field, boolean isNull) {
        or(NullFilter.create(field, isNull));
        return this;
    }

    @Override
    public NullFilter clone() throws CloneNotSupportedException {
        NullFilter clone = (NullFilter) super.clone();
        clone.isNull = this.isNull;
        return clone;
    }
}
