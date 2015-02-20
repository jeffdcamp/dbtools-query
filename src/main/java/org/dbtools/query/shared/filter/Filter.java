package org.dbtools.query.shared.filter;

import org.dbtools.query.shared.QueryBuilder;
import org.dbtools.query.sql.SQLQueryBuilder;

import javax.annotation.Nonnull;

public abstract class Filter implements Cloneable {

    protected Filter filter = null;

    public String buildFilter(@Nonnull QueryBuilder queryBuilder) {
        if (filter != null) {
            return filter.buildFilter(queryBuilder);
        } else {
            return build(queryBuilder);
        }
    }

    protected abstract String build(@Nonnull QueryBuilder queryBuilder);

    public Filter and(Filter... filters) {
        if (filters.length < 1) {
            throw new IllegalArgumentException("Must pass in at least one filter");
        }
        if (filter instanceof AndFilter) {
            filter.and(filters);
        } else {
            filter = AndFilter.create(filter, filters);
        }
        return this;
    }

    public Filter or(Filter... filters) {
        if (filters.length < 1) {
            throw new IllegalArgumentException("Must pass in at least one filter");
        }
        if (filter instanceof OrFilter) {
            ((OrFilter) filter).or(filters);
        } else {
            filter = OrFilter.create(filter, filters);
        }
        return this;
    }

    @Override
    public String toString() {
        return buildFilter(new SQLQueryBuilder());
    }

    @Override
    @SuppressWarnings("CloneDoesntCallSuperClone")
    public Filter clone() {
        Class clazz = this.getClass();
        Filter clone;
        try {
            clone = (Filter) clazz.newInstance();
        } catch (Exception e) {
            throw new IllegalStateException("Could not clone Filter", e);
        }

        if (this.filter != null) {
            clone.filter = this.filter.clone();
        }

        return clone;
    }
}
