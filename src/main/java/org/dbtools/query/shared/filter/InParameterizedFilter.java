package org.dbtools.query.shared.filter;

import org.dbtools.query.shared.QueryBuilder;

import javax.annotation.Nonnull;

public class InParameterizedFilter extends InFilter {

    private int numParams;

    public static InParameterizedFilter create(String field, int numParams) {
        InParameterizedFilter filterFormatter = new InParameterizedFilter();
        filterFormatter.filter = InParameterizedFilter.newInstance(field, numParams);
        return filterFormatter;
    }

    private static InParameterizedFilter newInstance(String field, int numParams) {
        return new InParameterizedFilter(field, numParams);
    }

    private InParameterizedFilter() {
        super();
    }

    private InParameterizedFilter(String field, int numParams) {
        super(field, numParams);
        if (numParams < 1) {
            throw new IllegalArgumentException("There must be at least 1 param for an InParameterizedFilter.");
        }
        this.numParams = numParams;
    }

    @Override
    public String build(@Nonnull QueryBuilder queryBuilder) {
        StringBuilder builder = new StringBuilder(field);
        builder.append(" IN (").append(queryBuilder.getQueryParameter());
        for (int i = 1; i < numParams; i++) {
            builder.append(", ").append(queryBuilder.getQueryParameter());
        }
        builder.append(")");
        return builder.toString();
    }

    public InParameterizedFilter and(String field, int numParams) {
        and(InParameterizedFilter.newInstance(field, numParams));
        return this;
    }

    public InParameterizedFilter or(String field, int numParams) {
        or(InParameterizedFilter.newInstance(field, numParams));
        return this;
    }

    @Override
    public InParameterizedFilter clone() throws CloneNotSupportedException {
        InParameterizedFilter clone = (InParameterizedFilter) super.clone();
        clone.numParams = this.numParams;
        return clone;
    }
}
