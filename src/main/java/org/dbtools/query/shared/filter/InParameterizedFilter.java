package org.dbtools.query.shared.filter;

import org.dbtools.query.shared.QueryBuilder;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class InParameterizedFilter extends InFilter {

    private int numParams;

    public static InParameterizedFilter create(@Nullable String field, int numParams) {
        if (field == null) {
            return null;
        }

        InParameterizedFilter filterFormatter = new InParameterizedFilter();
        filterFormatter.filter = InParameterizedFilter.newInstance(field, true, numParams);
        return filterFormatter;
    }

    public static InParameterizedFilter create(String field, boolean in, int numParams) {
        InParameterizedFilter filterFormatter = new InParameterizedFilter();
        filterFormatter.filter = InParameterizedFilter.newInstance(field, in, numParams);
        return filterFormatter;
    }

    private static InParameterizedFilter newInstance(String field, boolean in, int numParams) {
        return new InParameterizedFilter(field, in, numParams);
    }

    protected InParameterizedFilter() {
        super();
    }

    private InParameterizedFilter(String field, boolean in, int numParams) {
        super(field, in, null);
        if (numParams < 1) {
            throw new IllegalArgumentException("There must be at least 1 param for an InParameterizedFilter.");
        }
        this.numParams = numParams;
    }

    @Override
    public String build(@Nonnull QueryBuilder queryBuilder) {
        StringBuilder builder = new StringBuilder(field);
        if (in) {
            builder.append(" IN ");
        } else {
            builder.append(" NOT IN ");
        }
        builder.append("(").append(queryBuilder.getQueryParameter());
        for (int i = 1; i < numParams; i++) {
            builder.append(", ").append(queryBuilder.getQueryParameter());
        }
        builder.append(")");
        return builder.toString();
    }

    public InParameterizedFilter and(String field, int numParams) {
        and(InParameterizedFilter.newInstance(field, true, numParams));
        return this;
    }

    public InParameterizedFilter or(String field, int numParams) {
        or(InParameterizedFilter.newInstance(field, true, numParams));
        return this;
    }

    public InParameterizedFilter and(String field, boolean in, int numParams) {
        and(InParameterizedFilter.newInstance(field, in, numParams));
        return this;
    }

    public InParameterizedFilter or(String field, boolean in, int numParams) {
        or(InParameterizedFilter.newInstance(field, in, numParams));
        return this;
    }

    @Override
    public InParameterizedFilter clone() {
        InParameterizedFilter clone = (InParameterizedFilter) super.clone();
        clone.numParams = this.numParams;
        return clone;
    }
}
