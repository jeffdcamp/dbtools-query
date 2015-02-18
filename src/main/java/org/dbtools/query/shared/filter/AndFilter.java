package org.dbtools.query.shared.filter;


import org.dbtools.query.shared.QueryBuilder;

import javax.annotation.Nonnull;

public class AndFilter extends ConjunctionFilter {

    public static AndFilter create(Filter... formatters) {
        AndFilter andFilterFormatter = new AndFilter();
        andFilterFormatter.and(formatters);
        return andFilterFormatter;
    }

    public static AndFilter create(Filter formatter, Filter[] formatters) {
        AndFilter andFilterFormatter = new AndFilter();
        andFilterFormatter.and(formatter);
        andFilterFormatter.and(formatters);
        return andFilterFormatter;
    }

    private AndFilter() {}

    @Override
    protected String build(@Nonnull QueryBuilder queryBuilder) {
        StringBuilder builder = new StringBuilder();
        boolean isFirst = true;
        for (Filter filter : filters) {
            if (!isFirst) {
                builder.append(" AND ");
            }
            boolean wrap = filter instanceof ConjunctionFilter || filter.filter instanceof ConjunctionFilter;
            if (wrap) {
                builder.append('(');
            }
            builder.append(filter.buildFilter(queryBuilder));
            if (wrap) {
                builder.append(')');
            }
            isFirst = false;
        }
        return builder.toString();
    }

    @Override
    public AndFilter and(Filter... filters) {
        for (Filter filter : filters) { // Loop through filters and check for AndFilters
            if (filter instanceof AndFilter) { // if AndFilter add anded filters (No Parens)
                this.filters.addAll(((AndFilter) filter).filters);
            } else if (filter.filter instanceof AndFilter) { // if the subfilter is AndFilter add its anded filters
                this.filters.addAll(((AndFilter) filter.filter).filters);
            } else { // Else add the filter
                this.filters.add(filter);
            }
        }
        return this;
    }
}
