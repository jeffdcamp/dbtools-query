/*
 * QueryBuilder.java
 *
 * Created on November 22, 2002
 *
 * Copyright 2006 Jeff Campbell. All rights reserved. Unauthorized reproduction 
 * is a violation of applicable law. This material contains certain 
 * confidential or proprietary information and trade secrets of Jeff Campbell.
 */
package org.dbtools.query.sql;

import org.dbtools.query.shared.CompareType;
import org.dbtools.query.shared.Join;
import org.dbtools.query.shared.JoinType;
import org.dbtools.query.shared.QueryBuilder;
import org.dbtools.query.shared.QueryUtil;
import org.dbtools.query.shared.filter.AndFilter;
import org.dbtools.query.shared.filter.CompareFilter;
import org.dbtools.query.shared.filter.Filter;
import org.dbtools.query.shared.filter.RawFilter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@SuppressWarnings("unused")
public class SQLQueryBuilder extends QueryBuilder implements Cloneable {

    public static final String DEFAULT_QUERY_PARAMETER = "?";

    // NOTE: if any NEW variables are added BE SURE TO PUT IT INTO THE clone() method
    private Boolean distinct = null;
    private List<Field> fields;
    private List<String> tables;
    private List<Join> joins;
    private Filter filter;
    private List<String> groupBys;
    private List<String> orderBys;
    private String selectClause;
    private String postSelectClause;
    private String queryParameter = DEFAULT_QUERY_PARAMETER;

    public SQLQueryBuilder() {
        reset();
    }

    public static SQLQueryBuilder build() {
        return new SQLQueryBuilder();
    }

    @Override
    public SQLQueryBuilder clone() {
        Class thisClass = this.getClass();

        SQLQueryBuilder clone;
        try {
            clone = (SQLQueryBuilder) thisClass.newInstance();
        } catch (Exception e) {
            throw new IllegalStateException("Could not clone QueryBuilder", e);
        }

        // mutable.... create new objects!
        clone.distinct = this.distinct;
        clone.fields = new ArrayList<Field>(fields);
        clone.tables = new ArrayList<String>(tables);

        clone.joins = new ArrayList<Join>(this.joins);

        if (this.filter != null) {
            clone.filter = this.filter.clone();
        }

        clone.groupBys = new ArrayList<String>(groupBys);
        clone.orderBys = new ArrayList<String>(orderBys);

        // immutable.... just assign
        clone.selectClause = selectClause;
        clone.postSelectClause = postSelectClause;

        return clone;
    }

    public void reset() {
        distinct = false;
        fields = new ArrayList<Field>();
        tables = new ArrayList<String>();
        joins = new ArrayList<Join>();
        filter = null;
        groupBys = new ArrayList<String>();
        orderBys = new ArrayList<String>();

        selectClause = "";
        postSelectClause = "";
    }

    public SQLQueryBuilder apply(SQLQueryBuilder sqlQueryBuilder) {
        distinct = distinct == null ? sqlQueryBuilder.distinct : distinct;
        fields.addAll(sqlQueryBuilder.getFields());
        tables.addAll(sqlQueryBuilder.getTables());
        joins.addAll(sqlQueryBuilder.getJoins());
        if (sqlQueryBuilder.filter != null) {
            Filter filterClone = sqlQueryBuilder.filter.clone();
            if (filter == null) {
                filter = filterClone;
            } else {
                filter.and(filterClone);
            }
        }
        groupBys.addAll(sqlQueryBuilder.getGroupBys());
        orderBys.addAll(sqlQueryBuilder.getOrderBys());
        return this;
    }

    public SQLQueryBuilder distinct(boolean distinct) {
        this.distinct = distinct;
        return this;
    }

    /**
     * Adds a column to the query.
     */
    public SQLQueryBuilder field(String fieldName) {
        fields.add(new Field(fieldName));
        return this;
    }

    /**
     * Adds a column to the query.
     */
    public SQLQueryBuilder field(String fieldName, String alias) {
        fields.add(new Field(fieldName, alias));
        return this;
    }

    /**
     * Adds a column to the query.
     *
     * @return columnID (or the order in which it was added... 0 based)
     */
    public SQLQueryBuilder field(String tablename, String fieldName, String alias) {
        fields.add(new Field(tablename + "." + fieldName, alias));
        return this;
    }

    public SQLQueryBuilder fields(String... fieldNames) {
        for (String fieldName : fieldNames) {
            field(fieldName);
        }
        return this;
    }

    public SQLQueryBuilder fields(String[]... fieldNamesWithAlias) {
        for (String[] fieldNameWithAlias : fieldNamesWithAlias) {
            switch (fieldNameWithAlias.length) {
                case 1:
                    field(fieldNameWithAlias[0]);
                    break;
                case 2:
                    field(fieldNameWithAlias[0], fieldNameWithAlias[1]);
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported number of strings for fieldNameWithAlias: [" + Arrays.toString(fieldNameWithAlias) + "]");
            }
        }
        return this;
    }

    public SQLQueryBuilder table(String tableName) {
        tables.add(tableName);
        return this;
    }

    public SQLQueryBuilder table(SQLQueryBuilder sql) {
        tables.add("(" + sql.toString() + ")");
        return this;
    }

    public SQLQueryBuilder table(String tableName, String alias) {
        tables.add(tableName + " " + alias);
        return this;
    }

    public SQLQueryBuilder join(String field1, String field2) {
        if (filter == null) {
            filter = CompareFilter.create(field1, field2);
        } else {
            filter.and(CompareFilter.create(field1, field2));
        }
        return this;
    }

    public SQLQueryBuilder join(String tableName, String field1, String field2) {
        join(JoinType.JOIN, tableName, field1, field2);
        return this;
    }

    public SQLQueryBuilder join(JoinType joinType, String tableName, String field1, String field2) {
        joins.add(new Join(joinType, tableName, CompareFilter.create(field1, field2)));
        return this;
    }

    public SQLQueryBuilder join(String tableName, Filter... filters) {
        return join(JoinType.JOIN, tableName, filters);
    }

    public SQLQueryBuilder join(JoinType joinType, String tableName, Filter... filters) {
        return join(new Join(joinType, tableName, AndFilter.create(filters)));
    }

    public SQLQueryBuilder join(Join... joins) {
        this.joins.addAll(Arrays.asList(joins));
        return this;
    }

    public SQLQueryBuilder filter(String field, Object value) {
        filter(CompareFilter.create(field, value));
        return this;
    }

    public SQLQueryBuilder filter(String field, CompareType compare, Object value) {
        filter(CompareFilter.create(field, compare, value));
        return this;
    }

    public SQLQueryBuilder filter(String field, CompareType compare) {
        switch (compare) {
            case IS_NULL:
            case NOT_NULL:
                filter(CompareFilter.create(field, compare, null));
                break;
            default:
                throw new IllegalArgumentException("Illegal 1 argument compare " + compare.toString());
        }
        return this;
    }

    public SQLQueryBuilder filter(String filter) {
        filter(RawFilter.create(filter));
        return this;
    }

    public SQLQueryBuilder filter(Filter filter) {
        if (this.filter == null) {
            this.filter = filter;
        } else {
            this.filter.and(filter);
        }
        return this;
    }

    public SQLQueryBuilder groupBy(String item) {
        groupBys.add(item);
        return this;
    }

    public SQLQueryBuilder orderBy(String item) {
        orderBys.add(item);
        return this;
    }

    public SQLQueryBuilder orderBy(String... items) {
        Collections.addAll(orderBys, items);
        return this;
    }

    public SQLQueryBuilder orderBy(String item, boolean ascending) {
        String direction = ascending ? "ASC" : "DESC";
        orderBys.add(item + " " + direction);
        return this;
    }

    @Override
    public String buildQuery() {
        return buildQuery(false);
    }

    public String buildQuery(boolean countOnly) {
        selectClause = "";
        postSelectClause = "";

        StringBuilder query = new StringBuilder("SELECT ");

        if (distinct) {
            query.append("DISTINCT ");
        }

        // fields
        if (countOnly) {
            query.append("count(*)");
        } else {
            if (fields.size() > 0) {
                addListItems(query, fields, 0);
            } else {
                query.append("*");
            }
        }

        // save select portion
        selectClause = query.toString();

        // table names
        query = new StringBuilder();
        query.append(" FROM ");
        addListItems(query, tables, 0);

        for (Join join : joins) {
            query.append(" ").append(join.buildJoin(this));
        }

        if (filter != null) {
            query.append(" WHERE ").append(filter.buildFilter(this));
        }

        int groupBySectionCount = 0;
        // add groupbys
        if (groupBys.size() > 0 && !countOnly) {
            query.append(" GROUP BY ");
            addListItems(query, groupBys, groupBySectionCount);
        }

        int orderBySectionCount = 0;
        // add orderbys
        if (orderBys.size() > 0 && !countOnly) {
            query.append(" ORDER BY ");
            addListItems(query, orderBys, orderBySectionCount);
        }

        postSelectClause = query.toString();

        return selectClause + postSelectClause;
    }

    @Override
    public String toString() {
        return buildQuery();
    }

    private int addListItems(StringBuilder query, List list, int sectionItemCount) {
        return addListItems(query, list, ", ", sectionItemCount);
    }

    private int addListItems(StringBuilder query, List list, String separator, int sectionItemCount) {
        int newSectionCount = sectionItemCount;

        for (Object aList : list) {
            if (newSectionCount > 0) {
                query.append(separator);
            }

            query.append(aList);

            newSectionCount++;
        }

        return newSectionCount;
    }

    private class Field {

        private String name;
        private String alias;

        public Field(String name) {
            this.name = name;
        }

        public Field(String name, String alias) {
            this.name = name;
            this.alias = alias;
        }

        @Override
        public String toString() {
            String fieldStr = name;

            if (alias != null && !alias.equals("")) {
                fieldStr += " AS " + alias;
            }

            return fieldStr;
        }
    }

    public String formatLikeClause(String column, String value) {
        return QueryUtil.formatLikeClause(column, value);
    }

    public String formatIgnoreCaseLikeClause(String column, String value) {
        return formatLikeClause(column, value);
    }

    /**
     * Getter for property selectClause.
     *
     * @return Value of property selectClause.
     */
    public java.lang.String getSelectClause() {
        if (selectClause.length() == 0) {
            buildQuery();
        }

        return selectClause;
    }

    public static String union(SQLQueryBuilder... sqlQueryBuilders) {
        return union(false, sqlQueryBuilders);
    }

    public static String unionAll(SQLQueryBuilder... sqlQueryBuilders) {
        return union(true, sqlQueryBuilders);
    }

    private static String union(boolean unionAll, SQLQueryBuilder... sqlQueryBuilders) {
        if (sqlQueryBuilders == null) {
            return "";
        }

        StringBuilder query = new StringBuilder();

        query.append("(");
        int count = 0;
        for (SQLQueryBuilder sql : sqlQueryBuilders) {
            if (count > 0) {
                query.append(unionAll ? " UNION ALL " : " UNION ");
            }

            query.append(sql.toString());

            count++;
        }
        query.append(")");

        return query.toString();
    }

    /**
     * Getter for property postSelectClause.
     *
     * @return Value of property postSelectClause.
     */
    public java.lang.String getPostSelectClause() {
        return postSelectClause;
    }

    @Override
    public Object formatValue(Object value) {
        if (value instanceof Boolean) {
            return formatBoolean((Boolean) value);
        }
        return value;
    }

    public int formatBoolean(Boolean b) {
        return b ? 1 : 0;
    }

    public String getQueryParameter() {
        return queryParameter;
    }

    public void setQueryParameter(String queryParameter) {
        this.queryParameter = queryParameter;
    }

    public boolean isDistinct() {
        return distinct;
    }

    public SQLQueryBuilder distinct(Boolean distinct) {
        this.distinct = distinct;
        return this;
    }

    public List<Field> getFields() {
        return fields;
    }

    public List<String> getTables() {
        return tables;
    }

    public List<Join> getJoins() {
        return joins;
    }

    public Filter getFilter() {
        return filter;
    }

    public List<String> getGroupBys() {
        return groupBys;
    }

    public List<String> getOrderBys() {
        return orderBys;
    }
}
