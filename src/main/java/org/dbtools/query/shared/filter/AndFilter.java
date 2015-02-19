package org.dbtools.query.shared.filter;


import org.dbtools.query.shared.QueryBuilder;

import javax.annotation.Nonnull;

public class AndFilter extends ConjunctionFilter {

    public static AndFilter create(Filter... filters) {
        AndFilter andFilter = new AndFilter();
        andFilter.filter = AndFilter.newInstance(filters);
        return andFilter;
    }

    public static AndFilter create(Filter filter, Filter[] filters) {
        AndFilter andFilter = new AndFilter();
        andFilter.filter = AndFilter.newInstance(filter, filters);
        return andFilter;
    }

    private static AndFilter newInstance(Filter... filters) {
        if (filters.length < 1) {
            throw new IllegalArgumentException("Must pass in at least one filter");
        }
        AndFilter andFilterFormatter = new AndFilter();
        andFilterFormatter.and(filters);
        return andFilterFormatter;
    }

    private static AndFilter newInstance(Filter filter, Filter[] filters) {
        if (filter == null) {
            throw new IllegalArgumentException("filter must not be null");
        }
        AndFilter andFilterFormatter = new AndFilter();
        andFilterFormatter.and(filter);
        andFilterFormatter.and(filters);
        return andFilterFormatter;
    }

    protected AndFilter() {}

    @Override
    protected String build(@Nonnull QueryBuilder queryBuilder) {
        StringBuilder builder = new StringBuilder();
        boolean isFirst = true;
        for (Filter filter : filters) {
            if (!isFirst) {
                builder.append(" AND ");
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
    public AndFilter and(Filter... filters) {
        if (filters.length < 1) {
            throw new IllegalArgumentException("Must pass in at least one filter");
        }
        if (filter != null) {
            filter.and(filters);
            return this;
        }

        for (Filter filter : filters) { // Loop through filters and check for AndFilters
            if (filter.filter != null) { // if has sub filter and that sub filter
                this.and(filter.filter);
            } else if (filter instanceof AndFilter) { // if AndFilter add anded filters (No Parens)
                this.filters.addAll(((AndFilter) filter).filters);
            } else { // Else add the filter
                this.filters.add(filter);
            }
        }
        return this;
    }

    @Override
    public AndFilter or(Filter... filters) {
        super.or(filters);
        return this;
    }

    @Override
    public AndFilter clone() {
        return (AndFilter) super.clone();
    }
}
