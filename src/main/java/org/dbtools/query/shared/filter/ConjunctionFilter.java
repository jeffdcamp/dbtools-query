package org.dbtools.query.shared.filter;

import java.util.ArrayList;
import java.util.List;

public abstract class ConjunctionFilter extends Filter {
    protected final List<Filter> filters = new ArrayList<Filter>();

    @Override
    public ConjunctionFilter clone() throws CloneNotSupportedException {
        ConjunctionFilter clone = (ConjunctionFilter) super.clone();
        for(Filter filter : this.filters) {
            clone.filters.add(filter.clone());
        }
        return clone;
    }
}
