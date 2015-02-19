package org.dbtools.query.shared.filter;


import org.dbtools.query.shared.QueryBuilder;

import javax.annotation.Nonnull;

public class LikeFilter extends CompareFilter {

    protected boolean ignoreCase;

    public static LikeFilter create(String field, Object value) {
        LikeFilter filterFormatter = new LikeFilter();
        filterFormatter.filter = LikeFilter.newInstance(field, value);
        return filterFormatter;
    }

    public static LikeFilter create(String field, Object value, boolean ignoreCase) {
        LikeFilter filterFormatter = new LikeFilter();
        filterFormatter.filter = LikeFilter.newInstance(field, value, ignoreCase);
        return filterFormatter;
    }

    private static LikeFilter newInstance(String field, Object value) {
        return new LikeFilter(field, value, true);
    }

    private static LikeFilter newInstance(String field, Object value, boolean ignoreCase) {
        return new LikeFilter(field, value, ignoreCase);
    }

    protected LikeFilter() {
        super();
    }

    private LikeFilter(String field, Object value, boolean ignoreCase) {
        super(field, null, value);
        this.ignoreCase = ignoreCase;
    }

    @Override
    public String build(@Nonnull QueryBuilder queryBuilder) {
        String clause;
        if (ignoreCase) {
            clause = queryBuilder.formatIgnoreCaseLikeClause(field, String.valueOf(queryBuilder.formatValue(value)));
        } else {
            clause = queryBuilder.formatLikeClause(field, String.valueOf(queryBuilder.formatValue(value)));
        }
        return clause;
    }

    public LikeFilter and(String field, Object value) {
        and(LikeFilter.create(field, value));
        return this;
    }

    public LikeFilter and(String field, Object value, boolean ignoreCase) {
        and(LikeFilter.create(field, value, ignoreCase));
        return this;
    }

    public LikeFilter or(String field, Object value) {
        or(LikeFilter.create(field, value));
        return this;
    }

    public LikeFilter or(String field, Object value, boolean ignoreCase) {
        or(LikeFilter.create(field, value, ignoreCase));
        return this;
    }

    @Override
    public LikeFilter clone() {
        LikeFilter clone = (LikeFilter) super.clone();
        clone.ignoreCase = this.ignoreCase;
        return clone;
    }
}
