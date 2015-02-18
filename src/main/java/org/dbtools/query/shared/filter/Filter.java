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
        if (filter instanceof AndFilter) {
            ((AndFilter) filter).and(filters);
        } else {
            filter = AndFilter.create(filter, filters);
        }
        return this;
    }

    public Filter or(Filter... filters) {
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

    @SuppressWarnings("CloneDoesntCallSuperClone")
    @Override
    public Filter clone() throws CloneNotSupportedException {
        Class clazz = this.getClass();
        Filter clone;
        try {
            clone = (Filter) clazz.newInstance();
        } catch (Exception e) {
            throw new IllegalStateException("Could not clone Filter", e);
        }

        clone.filter = this.filter.clone();

        return clone;
    }
}
