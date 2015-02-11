/*
 * QueryBuilder.java
 *
 * Created on November 4, 2007
 *
 * Copyright 2007 Jeff Campbell. All rights reserved. Unauthorized reproduction 
 * is a violation of applicable law. This material contains certain 
 * confidential or proprietary information and trade secrets of Jeff Campbell.
 */
package org.dbtools.query.jpa;

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
public class JPAQueryBuilder<T> implements Cloneable {

    public static final int NO_OR_GROUP = -1;
    public static final String DEFAULT_QUERY_PARAMETER = "?";

    // NOTE: if any NEW variables are added BE SURE TO PUT IT INTO THE clone() method
    private EntityManager entityManager = null;
    private boolean distinct = false;
    private List<Field> fields;
    private List<String> objects;
    private List<String> varNames;
    private List<String> tableJoins;
    private List<JPAFilterItem> joins;
    private List<JPAFilterItem> filters; // just filters ANDed together
    private Map<Integer, List<JPAFilterItem>> filtersMap;
    private List<String> andClauses; //extra and clauses
    private List<String> groupBys;
    private List<String> orderBys;
    private String selectClause;
    private String postSelectClause;
    private String queryParameter = DEFAULT_QUERY_PARAMETER;

    public JPAQueryBuilder() {
        reset();
    }

    public JPAQueryBuilder(EntityManager entityManager) {
        this.setEntityManager(entityManager);
        reset();
    }

    public void close() {
        if (entityManager != null && entityManager.isOpen()) {
            entityManager.close();
        }

        entityManager = null; // NOPMD - Null is expected (forcing a new entity manager to be specified)
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        super.clone();

        Class thisClass = this.getClass();

        JPAQueryBuilder clone;
        try {
            clone = (JPAQueryBuilder) thisClass.newInstance();
        } catch (Exception e) {
            throw new IllegalStateException("Could not clone QueryBuilder", e);
        }

        if (entityManager != null) {
            clone.setEntityManager(entityManager);
        }

        // mutable.... create new objects!
        clone.distinct = this.distinct;
        clone.fields = new ArrayList<Field>(fields);
        clone.objects = new ArrayList<String>(objects);
        clone.varNames = new ArrayList<String>(varNames);

        clone.tableJoins = new ArrayList<String>(tableJoins);
        clone.joins = new ArrayList<JPAFilterItem>(joins);
        clone.filters = new ArrayList<JPAFilterItem>(filters);

        // filters Map
        clone.filtersMap = new HashMap<Integer, List<JPAFilterItem>>();
        for (Entry<Integer, List<JPAFilterItem>> e : filtersMap.entrySet()) {
            List<JPAFilterItem> clonedFilters = new ArrayList<JPAFilterItem>(e.getValue());
            clone.filtersMap.put(e.getKey(), clonedFilters);
        }

        clone.andClauses = new ArrayList<String>(andClauses); //extra and clauses
        clone.groupBys = new ArrayList<String>(groupBys);
        clone.orderBys = new ArrayList<String>(orderBys);

        // immutable.... just assign
        clone.selectClause = selectClause;
        clone.postSelectClause = postSelectClause;

        clone.internalVarUsed = internalVarUsed;
        clone.objectMap = new HashMap<String, String>(objectMap);

        return clone;
    }

    public final void reset() {
        fields = new ArrayList<Field>();
        objects = new ArrayList<String>();
        varNames = new ArrayList<String>();
        tableJoins = new ArrayList<String>();
        joins = new ArrayList<JPAFilterItem>();
        filters = new ArrayList<JPAFilterItem>();
        filtersMap = new HashMap<Integer, List<JPAFilterItem>>();
        andClauses = new ArrayList<String>();
        groupBys = new ArrayList<String>();
        orderBys = new ArrayList<String>();

        selectClause = "";
        postSelectClause = "";
    }

    public JPAQueryBuilder apply(JPAQueryBuilder<T> queryBuilder) {
        fields.addAll(queryBuilder.getFields());
        objects.addAll(queryBuilder.getObjects());
        tableJoins.addAll(queryBuilder.getTableJoins());
        joins.addAll(queryBuilder.getJoins());
        filters.addAll(queryBuilder.getFilters());
        filtersMap.putAll(queryBuilder.getFiltersMap());
        andClauses.addAll(queryBuilder.getAndClauses());
        groupBys.addAll(queryBuilder.getGroupBys());
        orderBys.addAll(queryBuilder.getOrderBys());
        return this;
    }

    public Query executeQuery() {
        return executeQuery(false);
    }

    public int executeCountQuery() {
        int count = 0;

        Query query = executeQuery(true);
        Object o = query.getSingleResult();
        if (o != null) {
            count = ((Long) o).intValue();
        }

        return count;
    }

    private Query executeQuery(boolean countOnly) {
        if (entityManager != null) {
            Query query = entityManager.createQuery(this.toString(countOnly));

            // add on any parameters
            for (JPAFilterItem stdFilter : filters) {
                if (stdFilter.isParameterFilter()) {
                    query.setParameter(stdFilter.getParamName(), stdFilter.getParamValue());
                }
            }

            return query;
        } else {
            System.out.println("WARNING... executeQuery called with a null entityManager.");
            return null;
        }
    }

    public Object getSingleResult() {
        Query q = executeQuery();
        return q.getSingleResult();
    }

    public List getResultList() {
        Query q = executeQuery();
        return q.getResultList();
    }

    public List getResultList(int firstRow, int numberOfRows) {
        Query q = executeQuery();

        List results;
        if (firstRow < 0) {
            results = q.getResultList();
        } else {
            results = q.setMaxResults(numberOfRows).setFirstResult(firstRow).getResultList();
        }

        return results;
    }

    /**
     * Adds a column to the query.
     * @return columnID (or the order in which it was added... 0 based)
     */
    public JPAQueryBuilder<T> field(String varName) {
        if (!internalVarUsed) {
            throw new IllegalStateException("Cannot call field(varName) when internal var is not being used");
        }

        field(DEFAULT_OBJ_VAR, varName);
        return this;
    }

    /**
     * Adds a column to the query.
     * @return columnID (or the order in which it was added... 0 based)
     */
    public JPAQueryBuilder<T> field(String object, String varName) {
        checkObjectForField(object);
        fields.add(new Field(object + "." + varName));
        return this;
    }

    public JPAQueryBuilder<T> fields(String... fieldNames) {
        for (String fieldName : fieldNames) {
            field(fieldName);
        }
        return this;
    }

    public JPAQueryBuilder<T> fields(String[]... fieldNamesWithAlias) {
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

    public int fieldObject(String object) {
        fields.add(new Field(object));
        return fields.size() - 1;
    }

    private void checkObjectForField(String objectName) {
        if (!objectMap.containsKey(objectName)) {
            throw new IllegalArgumentException("object named [" + objectName
                    + "] does not exist.  Be sure to call object(objectClassName) before adding fields for this object.");
        }
    }
    public static final String DEFAULT_OBJ_VAR = "o";
    private boolean internalVarUsed = false;
    private Map<String, String> objectMap = new HashMap<String, String>();

    public String object(String objectClassName) {
        String varNameForObject = DEFAULT_OBJ_VAR;

        if (objectMap.size() > 0) {
            varNameForObject += objectMap.size() + 1;
        }
        object(objectClassName, varNameForObject);

        return varNameForObject;
    }

    public String object(String objectClassName, String varNameForObject) {
        if (varNameForObject.equals(DEFAULT_OBJ_VAR)) {
            internalVarUsed = true;
        }

        varNames.add(varNameForObject);
        objects.add(objectClassName + " " + varNameForObject);

        objectMap.put(varNameForObject, objectClassName);

        return varNameForObject;
    }

    public String object(String objectClassName, String joinField, String joinToObjectName, String joinToObjectField) {
        String varNameForObject = object(objectClassName);

        // join
        join(varNameForObject, joinField, joinToObjectName, joinToObjectField);

        return varNameForObject;
    }

    public String object(String objectClassName, String varNameForObject, String joinField, String joinToObjectName, String joinToObjectField) {
        object(objectClassName, varNameForObject);

        // join
        join(varNameForObject, joinField, joinToObjectName, joinToObjectField);

        return varNameForObject;
    }

    public JPAQueryBuilder<T> join(String field, String field2) {
        joins.add(new JPAFilterItem<T>(field, QueryCompareType.EQUAL, field2).setJpaQueryBuilder(this));
        return this;
    }

    public JPAQueryBuilder<T> join(String objName1, String field, String objName2, String field2) {
        joins.add(new JPAFilterItem<T>(objName1 + '.' + field, QueryCompareType.EQUAL, objName2 + '.' + field2).setJpaQueryBuilder(this));
        return this;
    }

    public JPAQueryBuilder<T> join(String tableName, String field1, String field2) {
        join(QueryJoinType.JOIN, tableName, field1, field2);
        return this;
    }

    public JPAQueryBuilder<T> join(QueryJoinType joinType, String tableName, String field1, String field2) {
        tableJoins.add(" " + joinType.getJoinText() + " " + tableName + " ON " + field1 + " = " + field2);
        return this;
    }

    public JPAQueryBuilder<T> join(String tableName, JPAFilterItem<T>... filterItems) {
        return join(QueryJoinType.JOIN, tableName, filterItems);
    }

    public JPAQueryBuilder<T> join(QueryJoinType joinType, String tableName, JPAFilterItem<T>... filterItems) {
        StringBuilder sb = new StringBuilder();
        sb.append(" ").append(joinType.getJoinText()).append(" ").append(tableName).append(" ON ");

        int count = 0;
        for (JPAFilterItem<T> item : filterItems) {
            if (count > 0) {
                sb.append(" AND ");
            }
            sb.append(item);
            count++;
        }

        tableJoins.add(sb.toString());
        return this;
    }


    private List<JPAFilterItem> getFilters(int orGroupKey) {
        // get the filters for the given OR key
        List<JPAFilterItem> filters;
        if (orGroupKey == NO_OR_GROUP) {
            filters = this.filters;
        } else {
            filters = filtersMap.get(orGroupKey);
            if (filters == null) {
                filters = new ArrayList<JPAFilterItem>();
                filtersMap.put(orGroupKey, filters);
            }
        }

        return filters;
    }

    private String getOnlyVarName() {
        if (varNames.size() == 1) {
            return varNames.get(0);
        } else if (varNames.size() > 1) {
            throw new IllegalStateException("Cannot determine which objectVarName to use. (There are more than one object in this query).  "
                    + "Check your filters to be sure they specify which object to filter on.");
        } else {
            throw new IllegalStateException("There are no objects!");
        }
    }

    public JPAQueryBuilder<T> filter(String varName, Object value) {
        filter(getOnlyVarName(), varName, QueryCompareType.EQUAL, value);
        return this;
    }

    public JPAQueryBuilder<T> filter(String objectVarName, String varName, Object value) {
        filter(objectVarName, varName, QueryCompareType.EQUAL, value);
        return this;
    }

    public JPAQueryBuilder<T> filter(String varName, QueryCompareType compare, Object value) {
        filterToGroup(getOnlyVarName(), varName, compare, value, NO_OR_GROUP);
        return this;
    }

    public JPAQueryBuilder<T> filter(String objectVarName, String varName, QueryCompareType compare, Object value) {
        filterToGroup(objectVarName, varName, compare, value, NO_OR_GROUP);
        return this;
    }

    public JPAQueryBuilder<T> filter(String field, QueryCompareType compare) {
        switch (compare) {
            case IS_NULL:
            case NOT_NULL:
            case NONE:
                filterToGroup(field, compare, null, NO_OR_GROUP);
                break;
            default:
                throw new IllegalArgumentException("Illegal 1 argument compare " + compare.toString());
        }

        return this;
    }

    public JPAQueryBuilder<T> filter(String filter) {
        filterToGroup(filter, QueryCompareType.NONE, null, NO_OR_GROUP);
        return this;
    }

    public JPAQueryBuilder<T> filterToGroup(String varName, QueryCompareType compare, Object value, int orGroupKey) {
        filterToGroup(getOnlyVarName(), varName, compare, value, orGroupKey);
        return this;
    }

    public JPAQueryBuilder<T> filterToGroup(String objectVarName, String varName, QueryCompareType compare, Object value, int orGroupKey) {
        // get the filters for the given OR key
        List<JPAFilterItem> filters = getFilters(orGroupKey);

        switch (compare) {
            case LIKE:
            case LIKE_IGNORECASE:
            case IN:
                filters.add(new JPAFilterItem<T>(objectVarName + "." + varName, compare, value).setJpaQueryBuilder(this));
                break;
            case NONE:
                filters.add(new JPAFilterItem<T>(varName, compare, value).setJpaQueryBuilder(this));
                break;
            default:
                if (value instanceof String && !value.equals(queryParameter)) {
                    filters.add(new JPAFilterItem<T>(objectVarName + "." + varName, compare, formatString((String) value)).setJpaQueryBuilder(this));
                } else if (value instanceof Boolean) {
                    filters.add(new JPAFilterItem<T>(objectVarName + "." + varName, compare, formatBoolean((Boolean) value)).setJpaQueryBuilder(this));
                } else {
                    filters.add(new JPAFilterItem<T>(objectVarName + "." + varName, compare, value).setJpaQueryBuilder(this));
                }
        }
        return this;
    }

    public JPAQueryBuilder<T> groupBy(String varName) {
        groupBys.add(DEFAULT_OBJ_VAR + "." + varName);
        return this;
    }

    public JPAQueryBuilder<T> groupBy(String objectVarName, String varName) {
        groupBys.add(objectVarName + "." + varName);
        return this;
    }

    public JPAQueryBuilder<T> orderBy(String varName) {
        orderBys.add(DEFAULT_OBJ_VAR + "." + varName);
        return this;
    }

    public JPAQueryBuilder<T> orderBy(String varName, boolean ascending) {
        String direction = ascending ? "ASC" : "DESC";
        orderBys.add(DEFAULT_OBJ_VAR + "." + varName + " " + direction);
        return this;

    }

    public JPAQueryBuilder<T> orderBy(String objectVarName, String varName) {
        orderBy(objectVarName, varName, true);
        return this;
    }

    public JPAQueryBuilder<T> orderBy(String objectVarName, String varName, boolean ascending) {
        String direction = ascending ? "ASC" : "DESC";
        orderBys.add(objectVarName + "." + varName + " " + direction);
        return this;
    }

    public JPAQueryBuilder<T> andClause(String c) {
        andClauses.add(c);
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
                if (objects.size() == 1) {
                    List<Field> tempFields = new ArrayList<Field>();
                    tempFields.add(new Field(varNames.get(0)));
                    addListItems(query, tempFields, 0);
                } else {
                    throw new IllegalStateException("There must be at least 1 field if there is more than 1 object");
                }
            }
        }

        // save select portion
        selectClause = query.toString();

        // table names
        query = new StringBuilder();
        query.append(" FROM ");
        addListItems(query, objects, 0);
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
        for (Entry<Integer, List<JPAFilterItem>> e : filtersMap.entrySet()) {
            if (filterGroupCount > 0) {
                query.append(" AND ");
            }

            query.append("(");
            addListItems(query, e.getValue(), " OR ", 0);
            query.append(")");
            filterGroupCount++;
        }

        if (andClauses.size() > 0) {
            whereItemSectionCount = addListItems(query, andClauses, " AND ", whereItemSectionCount);
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

    public String toString(boolean countOnly) {
        return buildQuery(countOnly);
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

    private static int filterParamCount = 0;



    public String formatLikeClause(String column, String value) {
        return QueryUtil.formatLikeClause(column, value);
    }

    public String formatIgnoreCaseLikeClause(String column, String value) {
        return formatLikeClause(column, value);
    }

    /** Getter for property selectClause.
     * @return Value of property selectClause.
     *
     */
    public java.lang.String getSelectClause() {
        if (selectClause.length() == 0) {
            buildQuery();
        }

        return selectClause;
    }

    /** Getter for property postSelectClause.
     * @return Value of property postSelectClause.
     *
     */
    public java.lang.String getPostSelectClause() {
        return postSelectClause;
    }

    public EntityManager getEntityManager() {
        return entityManager;
    }

    public void setEntityManager(EntityManager dbManager) {
        this.entityManager = dbManager;
    }

    public static String formatString(String str) {
        return formatString(str, true);
    }

    public static String formatString(String str, boolean wrap) {
        return QueryUtil.formatString(str, wrap);
    }

    public int formatBoolean(Boolean b) {
        return b ? 1 : 0;
    }

    /**
     * Create count(*) query based on existing Builder tables and filters that have already been added to this query.
     * @return
     */
    public int getCount() {
        int count = -1;

        Query q = getEntityManager().createQuery(buildQuery(true));
        count = ((Long) q.getSingleResult()).intValue();

        return count;
    }

    public int getCount(String objectClassName) {
        return getCount(getEntityManager(), objectClassName);
    }

    public static int getCount(EntityManager em, String objectClassName) {
        if (em == null) {
            throw new IllegalArgumentException("entityManager cannot be null");
        }

        int count = -1;

        Query q = em.createQuery("SELECT count(*) FROM " + objectClassName);

        count = ((Long) q.getSingleResult()).intValue();

        return count;
    }

    public int getCountFiltered(String objectClassName, String fieldName, String filterString, boolean ignoreCase) {
        return getCountFiltered(getEntityManager(), objectClassName, fieldName, filterString, ignoreCase);
    }

    public static int getCountFiltered(EntityManager em, String objectClassName, String fieldName, String filterString, boolean ignoreCase) {
        if (em == null) {
            throw new IllegalArgumentException("entityManager cannot be null");
        }

        int count = -1;

        Query q = null;
        if (ignoreCase) {
            q = em.createQuery("SELECT count(*) FROM " + objectClassName + " o WHERE " + QueryUtil.formatLikeClause("o." + fieldName, filterString));
        } else {
            q = em.createQuery("SELECT count(*) FROM " + objectClassName + " o WHERE o." + fieldName + " = '" + filterString + "'");
        }

        count = ((Long) q.getSingleResult()).intValue();

        return count;
    }

    public int getCountFiltered(String objectClassName, String fieldName, int filterID) {
        return getCountFiltered(getEntityManager(), objectClassName, fieldName, filterID);
    }

    public static int getCountFiltered(EntityManager em, String objectClassName, String fieldName, int filterID) {
        if (em == null) {
            throw new IllegalArgumentException("entityManager cannot be null");
        }

        int count = -1;

        Query q = em.createQuery("SELECT count(*) FROM " + objectClassName + " o WHERE o." + fieldName + " = " + filterID);

        count = ((Long) q.getSingleResult()).intValue();

        return count;
    }

    public List<T> findRecordsByValue(String className, String column, int value) {
        JPAQueryBuilder qb = new JPAQueryBuilder(getEntityManager());
        qb.object(className);
        qb.filter(column, value);

        Query q = qb.executeQuery();

        List<T> items = null;
        if (q != null) {
            items = q.getResultList();
        }

        return items;
    }

    public List<T> findRecordsByValue(String className, String column, String value) {
        JPAQueryBuilder qb = new JPAQueryBuilder(getEntityManager());
        qb.object(className);
        qb.filter(column, value);

        Query q = qb.executeQuery();

        List<T> items = null;
        if (q != null) {
            items = q.getResultList();
        }

        return items;
    }

    public List<T> findRecordsByValue(String className, String column, Date value) {
        JPAQueryBuilder qb = new JPAQueryBuilder(getEntityManager());
        qb.object(className);
        qb.filter(column, value);

        Query q = qb.executeQuery();

        List<T> items = null;
        if (q != null) {
            items = q.getResultList();
        }

        return items;
    }

    /**
     * Find record based on given filter.
     * @return
     */
    public List<T> findRecords() {
        JPAQueryBuilder qb = new JPAQueryBuilder(getEntityManager());

        Query q = qb.executeQuery();

        return q.getResultList();
    }

    public T findRecordByValue(String className, String column, int value) {
        JPAQueryBuilder qb = new JPAQueryBuilder(getEntityManager());
        qb.object(className);
        qb.filter(column, value);

        return getSingleResultWOException(qb.executeQuery());
    }

    public T findRecordByValue(String className, String column, String value) {
        JPAQueryBuilder qb = new JPAQueryBuilder(getEntityManager());
        qb.object(className);
        qb.filter(column, value);

        return getSingleResultWOException(qb.executeQuery());
    }

    public T findRecordByValue(String className, String column, Date value) {
        JPAQueryBuilder qb = new JPAQueryBuilder(getEntityManager());
        qb.object(className);
        qb.filter(column, value);

        return getSingleResultWOException(qb.executeQuery());
    }

    /**
     * Find record based on given filter.
     * @return
     */
    public T findRecord() {
        JPAQueryBuilder qb = new JPAQueryBuilder(getEntityManager());

        return getSingleResultWOException(qb.executeQuery());
    }

    public List<Field> getFields() {
        return fields;
    }

    public List<String> getObjects() {
        return objects;
    }

    public List<String> getVarNames() {
        return varNames;
    }

    public List<String> getTableJoins() {
        return tableJoins;
    }

    public List<JPAFilterItem> getJoins() {
        return joins;
    }

    public List<JPAFilterItem> getFilters() {
        return filters;
    }

    public Map<Integer, List<JPAFilterItem>> getFiltersMap() {
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

    public String getQueryParameter() {
        return queryParameter;
    }

    private T getSingleResultWOException(Query q) {
        if (q == null) {
            throw new IllegalArgumentException("q cannot be null");
        }

        T item = null;

        List<T> items = q.getResultList();
        if (items.size() == 1) {
            item = items.get(0);
        }

        return item;


    }
}
