package org.dbtools.query.shared.filter;

import org.dbtools.query.shared.QueryBuilder;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class RawFilter extends Filter {
    private String filterString;

    public static RawFilter create(@Nullable String filterString) {
        if (filterString == null) {
            return null;
        }

        RawFilter rawFilterFormatter = new RawFilter();
        rawFilterFormatter.filter = newInstance(filterString);
        return rawFilterFormatter;
    }

    private static RawFilter newInstance(String filterString) {
        return new RawFilter(filterString);
    }

    protected RawFilter() {
    }

    private RawFilter(String filterString) {
        this.filterString = filterString;
    }

    @Override
    public String build(@Nonnull QueryBuilder queryBuilder) {
        return filterString;
    }

    public RawFilter and(String filterString) {
        and(RawFilter.newInstance(filterString));
        return this;
    }

    public RawFilter or(String filterString) {
        or(RawFilter.newInstance(filterString));
        return this;
    }

    @Override
    public RawFilter clone() {
        RawFilter clone = (RawFilter) super.clone();
        clone.filterString = this.filterString;
        return clone;
    }
}
