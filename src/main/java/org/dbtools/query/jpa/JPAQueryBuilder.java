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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author jeff
 */
public class JPAQueryBuilder<T> extends QueryBuilder implements Cloneable {

    public static final String DEFAULT_QUERY_PARAMETER = "?";

    // NOTE: if any NEW variables are added BE SURE TO PUT IT INTO THE clone() method
    private boolean distinct = false;
    private List<Field> fields;
    private List<String> objects;
    private List<String> varNames;
    private List<Join> joins;
    private Filter filter;
    private List<String> groupBys;
    private List<String> orderBys;
    private String selectClause;
    private String postSelectClause;
    private String queryParameter = DEFAULT_QUERY_PARAMETER;

    public JPAQueryBuilder() {
        reset();
    }

    @Override
    public Object clone() {
        Class thisClass = this.getClass();

        JPAQueryBuilder clone;
        try {
            clone = (JPAQueryBuilder) thisClass.newInstance();
        } catch (Exception e) {
            throw new IllegalStateException("Could not clone QueryBuilder", e);
        }

        // mutable.... create new objects!
        clone.distinct = this.distinct;
        clone.fields = new ArrayList<Field>(fields);
        clone.objects = new ArrayList<String>(objects);
        clone.varNames = new ArrayList<String>(varNames);

        clone.joins = new ArrayList<Join>(this.joins);

        if (this.filter != null) {
            clone.filter = this.filter.clone();
        }

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
        joins = new ArrayList<Join>();
        filter = null;
        groupBys = new ArrayList<String>();
        orderBys = new ArrayList<String>();

        selectClause = "";
        postSelectClause = "";
    }

    public JPAQueryBuilder apply(JPAQueryBuilder<T> queryBuilder) {
        fields.addAll(queryBuilder.getFields());
        objects.addAll(queryBuilder.getObjects());
        joins.addAll(queryBuilder.getJoins());
        if (filter == null) {
            this.filter = queryBuilder.filter;
        } else {
            if (queryBuilder.filter != null) {
                this.filter.and(queryBuilder.filter);
            }
        }
        groupBys.addAll(queryBuilder.getGroupBys());
        orderBys.addAll(queryBuilder.getOrderBys());
        return this;
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
                    throw new IllegalArgumentException("Unsupported number of strings for fieldNameWithAlias: [" + Arrays.toString(fieldNameWithAlias) + "]");
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
        filter(CompareFilter.create(field, field2));
        return this;
    }

    public JPAQueryBuilder<T> join(String objName1, String field, String objName2, String field2) {
        filter(CompareFilter.create(objName1 + '.' + field, objName2 + '.' + field2));
        return this;
    }

    public JPAQueryBuilder<T> join(String tableName, String field1, String field2) {
        return join(JoinType.JOIN, tableName, field1, field2);
    }

    public JPAQueryBuilder<T> join(JoinType joinType, String tableName, String field1, String field2) {
        return join(new Join(joinType, tableName, CompareFilter.create(field1, field2)));
    }

    public JPAQueryBuilder<T> join(String tableName, Filter... filters) {
        return join(JoinType.JOIN, tableName, filters);
    }

    public JPAQueryBuilder<T> join(JoinType joinType, String tableName, Filter... filters) {
        return join(new Join(joinType, tableName, AndFilter.create(filters)));
    }

    public JPAQueryBuilder<T> join(Join... joins) {
        this.joins.addAll(Arrays.asList(joins));
        return this;
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
        return filter(getOnlyVarName(), varName, CompareType.EQUAL, value);
    }

    public JPAQueryBuilder<T> filter(String objectVarName, String varName, Object value) {
        return filter(objectVarName, varName, CompareType.EQUAL, value);
    }

    public JPAQueryBuilder<T> filter(String varName, CompareType compare, Object value) {
        return filter(getOnlyVarName(), varName, compare, value);
    }

    public JPAQueryBuilder<T> filter(String field, CompareType compare) {
        switch (compare) {
            case IS_NULL:
            case NOT_NULL:
                return filter(field, compare, null);
            default:
                throw new IllegalArgumentException("Illegal 1 argument compare " + compare.toString());
        }
    }

    public JPAQueryBuilder<T> filter(String filter) {
        return filter(RawFilter.create(filter));
    }

    public JPAQueryBuilder<T> filter(String objectVarName, String varName, CompareType compare, Object value) {
        return filter(CompareFilter.create(objectVarName + "." + varName, compare, value));
    }

    public JPAQueryBuilder<T> filter(Filter filter) {
        if (this.filter == null) {
            this.filter = filter;
        } else {
            this.filter.and(filter);
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

        for(Join join : joins) {
            query.append(" ").append(join.buildJoin(this));
        }

        // add filters
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

    public List<Field> getFields() {
        return fields;
    }

    public List<String> getObjects() {
        return objects;
    }

    public List<String> getVarNames() {
        return varNames;
    }

    public List<Join> getJoins() {
        return joins;
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

    public void setQueryParameter(String queryParameter) {
        this.queryParameter = queryParameter;
    }

    public boolean isDistinct() {
        return distinct;
    }

    public JPAQueryBuilder<T> distinct(boolean distinct) {
        this.distinct = distinct;
        return this;
    }
}
