package org.dbtools.query.shared.filter;

import org.dbtools.query.shared.QueryBuilder;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class OrFilter extends ConjunctionFilter {

    public static OrFilter create(@Nullable Filter... filters) {
        if (filters == null) {
            return null;
        }

        OrFilter orFilter = new OrFilter();
        orFilter.filter = OrFilter.newInstance(filters);
        return orFilter;
    }

    public static OrFilter create(Filter filter, Filter[] filters) {
        OrFilter orFilter = new OrFilter();
        orFilter.filter = OrFilter.newInstance(filter, filters);
        return orFilter;
    }

    private static OrFilter newInstance(Filter... filters) {
        if (filters.length < 1) {
            throw new IllegalArgumentException("Must pass in at least one filter");
        }
        OrFilter orFilter = new OrFilter();
        orFilter.or(filters);
        return orFilter;
    }

    private static OrFilter newInstance(Filter filter, Filter[] filters) {
        if (filter == null) {
            throw new IllegalArgumentException("filter must not be null");
        }
        OrFilter orFilter = new OrFilter();
        orFilter.or(filter);
        orFilter.or(filters);
        return orFilter;
    }

    protected OrFilter() {}

    @Override
    protected String build(@Nonnull QueryBuilder queryBuilder) {
        StringBuilder builder = new StringBuilder();
        boolean isFirst = true;
        for (Filter filter : filters) {
            if (!isFirst) {
                builder.append(" OR ");
            }
            boolean wrap = filter instanceof ConjunctionFilter || filter.filter instanceof ConjunctionFilter;
            String tmp = filter.buildFilter(queryBuilder);
            if (wrap && tmp.length() >0) {
                builder.append('(').append(tmp).append(')');
            } else {
                builder.append(tmp);
            }
            isFirst = false;
        }
        return builder.toString();
    }

    @Override
    public OrFilter and(Filter... filters) {
        super.and(filters);
        return this;
    }

    @Override
    public OrFilter or(Filter... filters) {
        if (filters.length < 1) {
            throw new IllegalArgumentException("Must pass in at least one filter");
        }
        if (filter != null) {
            filter.or(filters);
            return this;
        }

        for (Filter filter : filters) { // Loop through filters and check for OrFilters
            if (filter.filter != null) { // if has sub filter or that sub filter
                this.or(filter.filter);
            } else if (filter instanceof OrFilter) { // if OrFilter add ored filters (No Parens)
                this.filters.addAll(((OrFilter) filter).filters);
            } else { // Else add the filter
                this.filters.add(filter);
            }
        }
        return this;
    }

    @Override
    public OrFilter clone() {
        return (OrFilter) super.clone();
    }
}
