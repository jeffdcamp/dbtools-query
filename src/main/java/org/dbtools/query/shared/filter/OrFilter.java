package org.dbtools.query.shared.filter;

import org.dbtools.query.shared.QueryBuilder;

import javax.annotation.Nonnull;

public class OrFilter extends ConjunctionFilter {

    public static OrFilter create(Filter... filters) {
        OrFilter orFilterFormatter = new OrFilter();
        orFilterFormatter.or(filters);
        return orFilterFormatter;
    }

    public static OrFilter create(Filter filter, Filter[] filters) {
        OrFilter orFilterFormatter = new OrFilter();
        orFilterFormatter.or(filter);
        orFilterFormatter.or(filters);
        return orFilterFormatter;
    }

    private OrFilter() {}

    @Override
    protected String build(@Nonnull QueryBuilder queryBuilder) {
        StringBuilder builder = new StringBuilder();
        boolean isFirst = true;
        for (Filter filter : filters) {
            if (!isFirst) {
                builder.append(" OR ");
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
    public OrFilter or(Filter... filters) {
        for (Filter filter : filters) { // Loop through filters and check for OrFilters
            if (filter instanceof OrFilter) { // if OrFilter add ored filters (No Parens)
                this.filters.addAll(((OrFilter) filter).filters);
            } else if (filter.filter instanceof OrFilter) { // if the subfilter is OrFilter add its ored filters
                this.filters.addAll(((OrFilter) filter.filter).filters);
            } else { // Else add the filter
                this.filters.add(filter);
            }
        }
        return this;
    }
}
