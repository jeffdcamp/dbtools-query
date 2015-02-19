package org.dbtools.query.shared.filter;

import org.dbtools.query.shared.QueryBuilder;

import javax.annotation.Nonnull;
import java.util.List;

public class InFilter extends CompareFilter {

    protected boolean in;
    private QueryBuilder subQuery;

    public static InFilter create(String field, Object value) {
        InFilter filter = new InFilter();
        filter.filter = InFilter.newInstance(field, true, value);
        return filter;
    }

    public static InFilter create(String field, boolean in, Object values) {
        InFilter filter = new InFilter();
        filter.filter = InFilter.newInstance(field, in, values);
        return filter;
    }

    public static InFilter create(String field, QueryBuilder subQuery) {
        InFilter filter = new InFilter();
        filter.filter = InFilter.newInstance(field, true, subQuery);
        return filter;
    }

    public static InFilter create(String field, boolean in, QueryBuilder subQuery) {
        InFilter filter = new InFilter();
        filter.filter = InFilter.newInstance(field, in, subQuery);
        return filter;
    }

    private static InFilter newInstance(String field, boolean in, Object values) {
        return new InFilter(field, in, values);
    }

    private static InFilter newInstance(String field, boolean in, QueryBuilder subQuery) {
        return new InFilter(field, in, subQuery);
    }

    protected InFilter() {
        super();
    }

    protected InFilter(String field, boolean in, Object value) {
        super(field, null, value);
        if (value instanceof List && ((List) value).size() < 1) {
            throw new IllegalArgumentException("List must contain at least on item");
        }
        this.in = in;
        this.subQuery = null;
    }

    private InFilter(String field, boolean in, QueryBuilder subQuery) {
        super(field, null, null);
        this.in = in;
        this.subQuery = subQuery;
    }

    @Override
    public String build(@Nonnull QueryBuilder queryBuilder) {
        StringBuilder builder = new StringBuilder(field);
        if (in) {
            builder.append(" IN ");
        } else {
            builder.append(" NOT IN ");
        }
        builder.append("(");
        if (subQuery != null) {
            builder.append(subQuery.buildQuery());
        } else if (value instanceof List) {
            List list = (List) value;
            builder.append(queryBuilder.formatValue(list.get(0)));
            int count = list.size();
            for (int i = 1; i < count; i++) {
                builder.append(", ").append(queryBuilder.formatValue(list.get(i)));
            }
        } else {
            builder.append(queryBuilder.formatValue(value));
        }
        return builder.append(")").toString();
    }

    public InFilter and(String field, Object value) {
        and(InFilter.newInstance(field, true, value));
        return this;
    }

    public InFilter or(String field, Object value) {
        or(InFilter.newInstance(field, true, value));
        return this;
    }

    public InFilter and(String field, boolean in, Object value) {
        and(InFilter.newInstance(field, in, value));
        return this;
    }

    public InFilter or(String field, boolean in, Object value) {
        or(InFilter.newInstance(field, in, value));
        return this;
    }

    public InFilter and(String field, QueryBuilder queryBuilder) {
        and(InFilter.newInstance(field, true, queryBuilder));
        return this;
    }

    public InFilter or(String field, QueryBuilder queryBuilder) {
        or(InFilter.newInstance(field, true, queryBuilder));
        return this;
    }

    public InFilter and(String field, boolean in, QueryBuilder queryBuilder) {
        and(InFilter.newInstance(field, in, queryBuilder));
        return this;
    }

    public InFilter or(String field, boolean in, QueryBuilder queryBuilder) {
        or(InFilter.newInstance(field, in, queryBuilder));
        return this;
    }



    @Override
    public InFilter clone() {
        InFilter clone = (InFilter) super.clone();
        clone.in = this.in;
        clone.subQuery = this.subQuery;
        return clone;
    }
}
