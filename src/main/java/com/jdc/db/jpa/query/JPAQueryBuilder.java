/*
 * QueryBuilder.java
 *
 * Created on November 4, 2007
 *
 * Copyright 2007 Jeff Campbell. All rights reserved. Unauthorized reproduction 
 * is a violation of applicable law. This material contains certain 
 * confidential or proprietary information and trade secrets of Jeff Campbell.
 */
package com.jdc.db.jpa.query;

import com.jdc.db.sql.query.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.persistence.EntityManager;
import javax.persistence.Query;

/**
 *
 * @author  jeff
 */
public class JPAQueryBuilder implements Cloneable {

    public static final int NO_OR_GROUP = -1;
    // NOTE: if any NEW variables are added BE SURE TO PUT IT INTO THE clone() method

    private EntityManager entityManager = null;
    private List<Field> fields;
    private List<String> objects;
    private List<String> varNames;
    private List<FilterItem> joins;
    private List<FilterItem> stdFilters; // just filters ANDed together

    private Map<Integer, List<FilterItem>> filtersMap;
    private List<String> andClauses; //extra and clauses

    private List<String> groupBys;
    private List<String> orderBys;
    private String selectClause;
    private String postSelectClause;

    /** Creates a new instance of QueryMaker */
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
    public Object clone() {
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
        clone.fields = new ArrayList<Field>(fields);
        clone.objects = new ArrayList<String>(objects);
        clone.varNames = new ArrayList<String>(varNames);

        clone.joins = new ArrayList<FilterItem>(joins);
        clone.stdFilters = new ArrayList<FilterItem>(stdFilters);

        // filters Map
        clone.filtersMap = new HashMap<Integer, List<FilterItem>>();
        Iterator<Entry<Integer, List<FilterItem>>> itr = filtersMap.entrySet().iterator();
        while (itr.hasNext()) {
            Entry<Integer, List<FilterItem>> e = itr.next();

            List<FilterItem> clonedFilters = new ArrayList<FilterItem>(e.getValue());
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
        fields = new ArrayList<Field>();
        objects = new ArrayList<String>();
        varNames = new ArrayList<String>();
        joins = new ArrayList<FilterItem>();
        stdFilters = new ArrayList<FilterItem>();
        filtersMap = new HashMap<Integer, List<FilterItem>>();
        andClauses = new ArrayList<String>();
        groupBys = new ArrayList<String>();
        orderBys = new ArrayList<String>();

        selectClause = "";
        postSelectClause = "";
    }

    public Query executeQuery() {
        if (entityManager != null) {
            return entityManager.createQuery(this.toString());
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

    /**
     * Adds a column to the query
     * @return columnID (or the order in which it was added... 0 based)
     */
    public int addField(String fieldName) {
        fields.add(new Field(fieldName));
        return fields.size() - 1;
    }

    /**
     * Adds a column to the query
     * @return columnID (or the order in which it was added... 0 based)
     */
    public int addField(String fieldName, String alias) {
        fields.add(new Field(fieldName, alias));
        return fields.size() - 1;
    }

    /**
     * Adds a column to the query
     * @return columnID (or the order in which it was added... 0 based)
     */
    public int addField(String tablename, String fieldName, String alias) {
        fields.add(new Field(tablename + "." + fieldName, alias));
        return fields.size() - 1;
    }

    public void addObject(String objectName, String varName) {
        varNames.add(varName);
        objects.add(objectName +" "+ varName);
    }

    public void addJoin(String field, String field2) {
        joins.add(new FilterItem(field, QueryCompareType.EQUAL, field2));
    }

    private List<FilterItem> getFilters(int orGroupKey) {
        // get the filters for the given OR key
        List<FilterItem> filters;
        if (orGroupKey == NO_OR_GROUP) {
            filters = stdFilters;
        } else {
            filters = filtersMap.get(orGroupKey);
            if (filters == null) {
                filters = new ArrayList<FilterItem>();
                filtersMap.put(orGroupKey, filters);
            }
        }

        return filters;
    }

    private String getOnlyVarName() {
        if (varNames.size() == 1) {
            return varNames.get(0);
        } else if (varNames.size() > 1) {
            throw new IllegalStateException("There are more than one object!");
        } else {
            throw new IllegalStateException("There are no objects!");
        }
    }
    
    public void addFilter(String field, String value) {
        addFilter(getOnlyVarName(), field, QueryCompareType.EQUAL, value);
    }
    
    public void addFilter(String varName, String field, String value) {
        addFilter(varName, field, QueryCompareType.EQUAL, value);
    }

    public void addFilter(String field, QueryCompareType compare, String value) {
        addFilter(getOnlyVarName(), field, compare, value, NO_OR_GROUP);
    }
    
    public void addFilter(String varName, String field, QueryCompareType compare, String value) {
        addFilter(varName, field, compare, value, NO_OR_GROUP);
    }

    public void addFilter(String field, QueryCompareType compare, String value, int orGroupKey) {
        addFilter(getOnlyVarName(), field, compare, value, orGroupKey);
    }
    
    public void addFilter(String varName, String field, QueryCompareType compare, String value, int orGroupKey) {
        // get the filters for the given OR key
        List<FilterItem> filters = getFilters(orGroupKey);

        if (compare != QueryCompareType.LIKE && compare != QueryCompareType.LIKE_IGNORECASE) {
            filters.add(new FilterItem(varName +"."+ field, compare, formatString(value)));
        } else {
            filters.add(new FilterItem(varName +"."+ field, compare, value));
        }
    }

    public void addFilter(String field, int value) {
        addFilter(getOnlyVarName(), field, value, NO_OR_GROUP);
    }
    
    public void addFilter(String varName, String field, int value) {
        addFilter(varName, field, value, NO_OR_GROUP);
    }

    public void addFilter(String field, int value, int orGroupKey) {
        addFilter(getOnlyVarName(), field, value, orGroupKey);
    }
    
    public void addFilter(String varName, String field, int value, int orGroupKey) {
        // get the filters for the given OR key
        List<FilterItem> filters = getFilters(orGroupKey);

        filters.add(new FilterItem(varName +"."+ field, QueryCompareType.EQUAL, Integer.toString(value)));
    }

    public void addFilter(String field, QueryCompareType compare, int value) {
        addFilter(getOnlyVarName(), field, compare, value, NO_OR_GROUP);
    }
    
    public void addFilter(String varName, String field, QueryCompareType compare, int value) {
        addFilter(varName, field, compare, value, NO_OR_GROUP);
    }

    public void addFilter(String field, QueryCompareType compare, int value, int orGroupKey) {
        addFilter(getOnlyVarName(), field, compare, value, orGroupKey);
    }
    
    public void addFilter(String varName, String field, QueryCompareType compare, int value, int orGroupKey) {
        // get the filters for the given OR key
        List<FilterItem> filters = getFilters(orGroupKey);

        filters.add(new FilterItem(varName +"."+ field, compare, Integer.toString(value)));
    }

    public void addFilterTimeStamp(String field, Date value) {
        addFilterTimeStamp(getOnlyVarName(), field, QueryCompareType.EQUAL, value);
    }
    
    public void addFilterTimeStamp(String varName, String field, Date value) {
        addFilterTimeStamp(varName, field, QueryCompareType.EQUAL, value);
    }

    public void addFilterTimeStamp(String field, QueryCompareType compare, Date value) {
        addFilterTimeStamp(getOnlyVarName(), field, compare, value, NO_OR_GROUP);
    }
    
    public void addFilterTimeStamp(String varName, String field, QueryCompareType compare, Date value) {
        addFilterTimeStamp(varName, field, compare, value, NO_OR_GROUP);
    }

    public void addFilterTimeStamp(String field, QueryCompareType compare, Date value, int orGroupKey) {
        addFilterTimeStamp(getOnlyVarName(), field, compare, value, orGroupKey);
    }
    
    public void addFilterTimeStamp(String varName, String field, QueryCompareType compare, Date value, int orGroupKey) {
        // get the filters for the given OR key
        List<FilterItem> filters = getFilters(orGroupKey);

        filters.add(new FilterItem(varName +"."+ field, compare, formatDateTime(value)));
    }

    public void addFilterDateOnly(String field, Date value) {
        addFilterDateOnly(getOnlyVarName(), field, QueryCompareType.EQUAL, value);
    }
    
    public void addFilterDateOnly(String varName, String field, Date value) {
        addFilterDateOnly(varName, field, QueryCompareType.EQUAL, value);
    }

    public void addFilterDateOnly(String field, QueryCompareType compare, Date value) {
        addFilterDateOnly(getOnlyVarName(), field, compare, value, NO_OR_GROUP);
    }
    
    public void addFilterDateOnly(String varName, String field, QueryCompareType compare, Date value) {
        addFilterDateOnly(varName, field, compare, value, NO_OR_GROUP);
    }

    public void addFilterDateOnly(String field, QueryCompareType compare, Date value, int orGroupKey) {
        addFilterDateOnly(getOnlyVarName(), field, compare, value, orGroupKey);
    }
    
    public void addFilterDateOnly(String varName, String field, QueryCompareType compare, Date value, int orGroupKey) {
        // get the filters for the given OR key
        List<FilterItem> filters = getFilters(orGroupKey);

        filters.add(new FilterItem(varName +"."+ field, compare, formatDate(value)));
    }

    public void addGroupBy(String item) {
        groupBys.add(item);
    }

    public void addOrderBy(String item) {
        orderBys.add(item);
    }

    public void addAndCalause(String c) {
        andClauses.add(c);
    }

    public String buildQuery() {
        selectClause = "";
        postSelectClause = "";

        StringBuffer query = new StringBuffer("SELECT ");
        containsItems = false;

        // fields
        if (fields.size() > 0) {
            addListItems(query, fields);
        } else {
            if (objects.size() == 1) {
                List<Field> tempFields = new ArrayList<Field>();
                tempFields.add(new Field(varNames.get(0)));
                addListItems(query, tempFields);
            } else {
                throw new IllegalStateException("There must be at least 1 field if there is more than 1 object");
            }
        }

        // save select portion
        selectClause = query.toString();

        // table names
        query = new StringBuffer();
        query.append(" FROM ");
        containsItems = false;
        addListItems(query, objects);

        // add filters
        if (joins.size() > 0 || stdFilters.size() > 0 || filtersMap.size() > 0 || andClauses.size() > 0) {
            query.append(" WHERE ");
            containsItems = false;
        }

        if (joins.size() > 0) {
            addListItems(query, joins, " AND ");
        }

        if (stdFilters.size() > 0) {
            addListItems(query, stdFilters, " AND ");
        }

        Iterator<Entry<Integer, List<FilterItem>>> itr = filtersMap.entrySet().iterator();
        while (itr.hasNext()) {
            Entry<Integer, List<FilterItem>> e = itr.next();

            query.append("(");
            addListItems(query, e.getValue(), " OR ");
            query.append(")");
        }

        if (andClauses.size() > 0) {
            addListItems(query, andClauses, " AND ");
        }

        // add groupbys
        if (groupBys.size() > 0) {
            query.append(" GROUP BY ");
            containsItems = false;
            addListItems(query, groupBys);
        }

        // add groupbys
        if (orderBys.size() > 0) {
            query.append(" ORDER BY ");
            containsItems = false;
            addListItems(query, orderBys);
        }

        postSelectClause = query.toString();

        return selectClause + postSelectClause;
    }

    @Override
    public String toString() {
        return buildQuery();
    }

    private void addListItems(StringBuffer query, List list) {
        addListItems(query, list, ", ");
    }
    private boolean containsItems = false;

    private void addListItems(StringBuffer query, List list, String seperator) {
        for (int i = 0; i < list.size(); i++) {
            if (containsItems) {
                query.append(seperator);
            }

            query.append(list.get(i));

            if (!containsItems) {
                containsItems = true;
            }
        }
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

    private class FilterItem {

        private String field;
        private String value;
        private QueryCompareType compare;

        public FilterItem(String field, QueryCompareType compare, String value) {
            this.field = field;
            this.value = value;
            this.compare = compare;
        }

        @Override
        public String toString() {
            String filter = "";

            String filterCompare = " = ";
            switch (compare) {
                default:
                case EQUAL:
                    filterCompare = " = ";
                    break;
                case GREATERTHAN:
                    filterCompare = " > ";
                    break;
                case LESSTHAN:
                    filterCompare = " < ";
                    break;
                case GREATERTHAN_EQUAL:
                    filterCompare = " >= ";
                    break;
                case LESSTHAN_EQUAL:
                    filterCompare = " <= ";
                    break;
                case LIKE:
                case LIKE_IGNORECASE:
                    filterCompare = "";
                    break;
            }

            if (compare != QueryCompareType.LIKE && compare != QueryCompareType.LIKE_IGNORECASE) {
                filter = field + filterCompare + value;
            } else {
                switch (compare) {
                    case LIKE:
                        filter = formatLikeClause(field, value);
                        break;
                    case LIKE_IGNORECASE:
                        filter = formatIgnoreCaseLikeClause(field, value);
                        break;
                    default:
                        filter = field + " LIKE '%" + value + "%'";
                }

            }

            return filter;
        }
    }

    public String formatLikeClause(String column, String value) {
        boolean before = false;
        if (value.indexOf('*') == 0) {
            before = true;
            value = value.substring(1);
        }
        boolean after = false;
        if (value.lastIndexOf('*') == (value.length() - 1)) {
            after = true;
            value = value.substring(0, value.length() - 1);
        }
        StringBuffer select = new StringBuffer(column);
        if (before == after) // default or two explicit stars
        {
            select.append(" LIKE '%");
            select.append(formatString(value, false));
            select.append("%'");
        } else if (before) {
            select.append(" LIKE '%");
            select.append(formatString(value, false));
            select.append("'");
        } else // if endStar
        {
            select.append(" LIKE '");
            select.append(formatString(value, false));
            select.append("%'");
        }
        return select.toString();
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

    public static final String formatString(String str) {
        return formatString(str, true);
    }

    public static String formatString(String str, boolean wrap) {
        int length = str.length();
        char[] input = str.toCharArray();
        StringBuffer temp = new StringBuffer(length + 2);

        if (wrap) {
            temp.append('\'');
        }  // opening quote

        for (int i = 0; i < length; i++) {
            if (input[i] == '\'') {
                temp.append('\'');
            }  // make the quote a literal

            temp.append(input[i]);
        }

        if (wrap) {
            temp.append('\'');
        }  // closing quote

        return temp.toString();
    }

    public static String formatDate(java.util.Date date) {
        if (date == null || date.getTime() == 0) {
            return "''";
        }

        return dateFormat.format(date);
    }

    public static String formatDateTime(java.util.Date date) {
        if (date == null || date.getTime() == 0) {
            return "''";
        }

        return dateTimeFormat.format(date);
    }

    public static String formatTime(java.sql.Time time) {
        if (time == null || time.getTime() == 0) {
            return "''";
        }

        return timeFormat.format(time);
    }
    private static SimpleDateFormat dateFormat = new SimpleDateFormat("''yyyy-MM-dd 00:00:00''");
    private static SimpleDateFormat dateTimeFormat = new SimpleDateFormat("''yyyy-MM-dd HH:mm:ss''");
    private static SimpleDateFormat timeFormat = new SimpleDateFormat("''HH:mm:ss''");

    public static String formatBoolean(Boolean value) {
        return value.booleanValue() ? "1" : "0";
    }
}
