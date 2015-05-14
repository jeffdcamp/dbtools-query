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

import org.dbtools.query.shared.QueryCompareType;
import org.dbtools.query.shared.QueryJoinType;
import org.dbtools.query.shared.QueryUtil;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.*;
import java.util.Map.Entry;

/**
 *
 * @author jeff
 */
public class SQLQueryBuilder implements Cloneable {

    public static final int NO_OR_GROUP = -1;
    public static final String DEFAULT_QUERY_PARAMETER = "?";

    // NOTE: if any NEW variables are added BE SURE TO PUT IT INTO THE clone() method
    private EntityManager entityManager = null;
    private Boolean distinct = null;
    private List<Field> fields;
    private List<String> tables;
    private List<String> tableJoins;
    private List<SQLFilterItem> joins;
    private List<SQLFilterItem> filters; // just filters ANDed together
    private Map<Integer, List<SQLFilterItem>> filtersMap;
    private List<String> andClauses; //extra and clauses
    private List<String> groupBys;
    private List<String> orderBys;
    private String selectClause;
    private String postSelectClause;
    private String queryParameter = DEFAULT_QUERY_PARAMETER;

    public SQLQueryBuilder() {
        reset();
    }

    public SQLQueryBuilder(EntityManager entityManager) {
        this.setEntityManager(entityManager);
        reset();
    }

    public static SQLQueryBuilder build() {
        return new SQLQueryBuilder();
    }

    public void close() {
        if (entityManager != null && entityManager.isOpen()) {
            entityManager.close();
        }

        entityManager = null; // NOPMD - Null is expected (forcing a new entity manager to be specified)
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

        if (entityManager != null) {
            clone.setEntityManager(entityManager);
        }

        // mutable.... create new objects!
        clone.distinct = this.distinct;
        clone.fields = new ArrayList<Field>(fields);
        clone.tables = new ArrayList<String>(tables);

        clone.tableJoins = new ArrayList<String>(tableJoins);
        clone.joins = new ArrayList<SQLFilterItem>(joins);
        clone.filters = new ArrayList<SQLFilterItem>(filters);

        // filters Map
        clone.filtersMap = new HashMap<Integer, List<SQLFilterItem>>();
        for (Entry<Integer, List<SQLFilterItem>> e : filtersMap.entrySet()) {
            List<SQLFilterItem> clonedFilters = new ArrayList<SQLFilterItem>(e.getValue());
            clone.filtersMap.put(e.getKey(), clonedFilters);
        }

        clone.andClauses = new ArrayList<String>(andClauses); //extra and clauses
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
        tableJoins = new ArrayList<String>();
        joins = new ArrayList<SQLFilterItem>();
        filters = new ArrayList<SQLFilterItem>();
        filtersMap = new HashMap<Integer, List<SQLFilterItem>>();
        andClauses = new ArrayList<String>();
        groupBys = new ArrayList<String>();
        orderBys = new ArrayList<String>();

        selectClause = "";
        postSelectClause = "";
    }

    public SQLQueryBuilder apply(SQLQueryBuilder sqlQueryBuilder) {
        distinct = distinct == null ? sqlQueryBuilder.distinct : distinct;
        fields.addAll(sqlQueryBuilder.getFields());
        tables.addAll(sqlQueryBuilder.getTables());
        tableJoins.addAll(sqlQueryBuilder.getTableJoins());
        joins.addAll(sqlQueryBuilder.getJoins());
        filters.addAll(sqlQueryBuilder.getFilters());
        filtersMap.putAll(sqlQueryBuilder.getFiltersMap());
        andClauses.addAll(sqlQueryBuilder.getAndClauses());
        groupBys.addAll(sqlQueryBuilder.getGroupBys());
        orderBys.addAll(sqlQueryBuilder.getOrderBys());
        return this;
    }

    public Query executeQuery() {
        if (entityManager != null) {
            return entityManager.createNativeQuery(this.toString());
        } else {
            System.out.println("WARNING... executeQuery called with a null entityManager.");
            return null;
        }
    }

    public Query executeQuery(int firstRow, int numberOfRows) {
        if (entityManager != null) {
            Query q = entityManager.createNativeQuery(this.toString());

            if (firstRow < 0) {
                q.getResultList();
            } else {
                q.setMaxResults(numberOfRows).setFirstResult(firstRow).getResultList();
            }

            return q;
        } else {
            System.out.println("WARNING... executeQuery called with a null entityManager.");
            return null;
        }
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
                    throw new IllegalArgumentException("Unsupported number of strings for fieldNameWithAlias: [" + fieldNameWithAlias + "]");
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
        joins.add(new SQLFilterItem(field1, QueryCompareType.EQUAL, field2).setSqlQueryBuilder(this));
        return this;
    }

    public SQLQueryBuilder join(String tableName, String field1, String field2) {
        join(QueryJoinType.JOIN, tableName, field1, field2);
        return this;
    }

    public SQLQueryBuilder join(QueryJoinType joinType, String tableName, String field1, String field2) {
        tableJoins.add(" " + joinType.getJoinText() + " " + tableName + " ON " + field1 + " = " + field2);
        return this;
    }

    public SQLQueryBuilder join(String tableName, SQLFilterItem... filterItems) {
        return join(QueryJoinType.JOIN, tableName, filterItems);
    }

    public SQLQueryBuilder join(QueryJoinType joinType, String tableName, SQLFilterItem... filterItems) {
        StringBuilder sb = new StringBuilder();
        sb.append(" ").append(joinType.getJoinText()).append(" ").append(tableName).append(" ON ");

        int count = 0;
        for (SQLFilterItem item : filterItems) {
            if (count > 0) {
                sb.append(" AND ");
            }
            sb.append(item);
            count++;
        }

        tableJoins.add(sb.toString());
        return this;
    }

    private List<SQLFilterItem> getFilters(int orGroupKey) {
        // get the filters for the given OR key
        List<SQLFilterItem> filters;
        if (orGroupKey == NO_OR_GROUP) {
            filters = this.filters;
        } else {
            filters = filtersMap.get(orGroupKey);
            if (filters == null) {
                filters = new ArrayList<SQLFilterItem>();
                filtersMap.put(orGroupKey, filters);
            }
        }

        return filters;
    }

    public SQLQueryBuilder filter(String field, Object value) {
        filterToGroup(field, value, NO_OR_GROUP);
        return this;
    }

    public SQLQueryBuilder filterToGroup(String field, Object value, int orGroupKey) {
        filterToGroup(field, QueryCompareType.EQUAL, value, orGroupKey);
        return this;
    }

    public SQLQueryBuilder filter(String field, QueryCompareType compare, Object value) {
        filterToGroup(field, compare, value, NO_OR_GROUP);
        return this;
    }

    public SQLQueryBuilder filter(String field, QueryCompareType compare) {
        switch (compare) {
            case IS_NULL:
            case NOT_NULL:
                filterToGroup(field, compare, null, NO_OR_GROUP);
                break;
            default:
                throw new IllegalArgumentException("Illegal 1 argument compare " + compare.toString());
        }

        return this;
    }

    public SQLQueryBuilder filterToGroup(String field, QueryCompareType compare, Object value, int orGroupKey) {
        // get the filters for the given OR key
        List<SQLFilterItem> filters = getFilters(orGroupKey);

        switch (compare) {
            case LIKE:
            case LIKE_IGNORECASE:
            case IN:
                filters.add(new SQLFilterItem(field, compare, value).setSqlQueryBuilder(this));
                break;
            default:
                if (value instanceof String && !value.equals(queryParameter)) {
                    filters.add(new SQLFilterItem(field, compare, formatString((String) value)).setSqlQueryBuilder(this));
                } else if (value instanceof Boolean) {
                    filters.add(new SQLFilterItem(field, compare, formatBoolean((Boolean) value)).setSqlQueryBuilder(this));
                } else {
                    filters.add(new SQLFilterItem(field, compare, value).setSqlQueryBuilder(this));
                }
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

    public SQLQueryBuilder andClause(String c) {
        andClauses.add(c);
        return this;
    }

    public SQLQueryBuilder distinct(boolean distinct) {
        this.distinct = distinct;
        return this;
    }

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
        addListItems(query, tableJoins, "", 0);

        // add filters
        if (joins.size() > 0 || filters.size() > 0 || filtersMap.size() > 0 || andClauses.size() > 0) {
            query.append(" WHERE ");
        }

        int whereItemSectionCount = 0;

        if (joins.size() > 0) {
            whereItemSectionCount = addListItems(query, joins, " AND ", whereItemSectionCount);
        }

        if (filters.size() > 0) {
            whereItemSectionCount = addListItems(query, filters, " AND ", whereItemSectionCount);
        }

        if (whereItemSectionCount > 0 && !filtersMap.entrySet().isEmpty()) {
            query.append(" AND ");
        }
        int filterGroupCount = 0;
        for (Entry<Integer, List<SQLFilterItem>> e : filtersMap.entrySet()) {
            if (filterGroupCount > 0) {
                query.append(" AND ");
            }

            query.append("(");
            addListItems(query, e.getValue(), " OR ", 0);
            query.append(")");
            filterGroupCount++;
        }

        if (andClauses.size() > 0) {
            addListItems(query, andClauses, " AND ", whereItemSectionCount);
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

    public static String[] toSelectionArgs(Object... args) {
        int size = args.length;
        String[] selectionArgs = new String[size];
        for (int i = 0; i < size; i++) {
            selectionArgs[i] = String.valueOf(args[i]);
        }

        return selectionArgs;
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

    public EntityManager getEntityManager() {
        return entityManager;
    }

    public void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public String formatString(String str) {
        return formatString(str, true);
    }

    public String formatString(String str, boolean wrap) {
        return QueryUtil.formatString(str, wrap);
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

    public List<Field> getFields() {
        return fields;
    }

    public List<String> getTables() {
        return tables;
    }

    public List<String> getTableJoins() {
        return tableJoins;
    }

    public List<SQLFilterItem> getJoins() {
        return joins;
    }

    public List<SQLFilterItem> getFilters() {
        return filters;
    }

    public Map<Integer, List<SQLFilterItem>> getFiltersMap() {
        return filtersMap;
    }

    public List<String> getAndClauses() {
        return andClauses;
    }

    public List<String> getGroupBys() {
        return groupBys;
    }

    public List<String> getOrderBys() {
        return orderBys;
    }
}
