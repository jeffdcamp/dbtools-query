/*
 * QueryBuilder.java
 *
 * Created on November 22, 2002
 *
 * Copyright 2006 Jeff Campbell. All rights reserved. Unauthorized reproduction 
 * is a violation of applicable law. This material contains certain 
 * confidential or proprietary information and trade secrets of Jeff Campbell.
 */
package org.dbtools.query.sql

import org.dbtools.query.shared.CompareType
import org.dbtools.query.shared.Field
import org.dbtools.query.shared.Join
import org.dbtools.query.shared.JoinType
import org.dbtools.query.shared.QueryBuilder
import org.dbtools.query.shared.Table
import org.dbtools.query.shared.filter.AndFilter
import org.dbtools.query.shared.filter.CompareFilter
import org.dbtools.query.shared.filter.Filter
import org.dbtools.query.shared.filter.RawFilter
import org.dbtools.query.shared.util.QueryUtil


@Suppress("unused")
open class SQLQueryBuilder : QueryBuilder() {
    // NOTE: if any NEW variables are added BE SURE TO PUT IT INTO THE clone() method
    private var distinct: Boolean = false
    private val fields = mutableListOf<Field>()
    private val tables = mutableListOf<Table>()
    private val joins = mutableListOf<Join>()
    private var filter: Filter? = null
    private val groupBys = mutableListOf<String>()
    private var having: Filter? = null
    private val orderBys = mutableListOf<String>()
    private var selectClause: String? = null
    private var postSelectClause: String? = null
    override var queryParameter: String = DEFAULT_QUERY_PARAMETER

    // Removed for now because deep copying of nested filters are tricky
//    fun apply(sqlQueryBuilder: SQLQueryBuilder): SQLQueryBuilder {
//        fields.addAll(sqlQueryBuilder.getFields())
//        tables.addAll(sqlQueryBuilder.getTables())
//        joins.addAll(sqlQueryBuilder.getJoins())
//
//        val otherFilter: Filter? = sqlQueryBuilder.filter
//        if (otherFilter != null) {
//            if (filter == null) {
//                filter = otherFilter.copy()
//            } else {
//                filter?.and(otherFilter)
//            }
//        }
//
//        groupBys.addAll(sqlQueryBuilder.getGroupBys())
//        val otherHaving = sqlQueryBuilder.having
//        if (otherHaving != null) {
//            if (having == null) {
//                having = otherHaving
//            } else {
//                having?.and(otherHaving)
//            }
//        }
//
//        orderBys.addAll(sqlQueryBuilder.getOrderBys())
//        return this
//    }

    /**
     * Adds a column to the query.
     */
    fun field(fieldName: String): SQLQueryBuilder {
        fields.add(Field(fieldName))
        return this
    }

    /**
     * Adds a column to the query.
     */
    fun field(fieldName: String, alias: String): SQLQueryBuilder {
        fields.add(Field(fieldName, alias))
        return this
    }

    /**
     * Adds a column to the query.
     */
    fun field(tableName: String, fieldName: String, alias: String? = null): SQLQueryBuilder {
        fields.add(Field("$tableName.$fieldName", alias))
        return this
    }

    /**
     * Adds a column to the query.
     */
    fun field(field: Field): SQLQueryBuilder {
        fields.add(field)
        return this
    }

    fun fields(vararg fieldNames: String): SQLQueryBuilder {
        fieldNames.forEach { fieldName ->
            field(fieldName)
        }
        return this
    }

    fun fields(fields: List<Field>): SQLQueryBuilder {
        fields.forEach { field(it) }
        return this
    }

    fun table(tableName: String): SQLQueryBuilder {
        tables.add(Table(tableName))
        return this
    }

    fun table(sql: SQLQueryBuilder): SQLQueryBuilder {
        tables.add(Table("($sql)"))
        return this
    }

    fun table(tableName: String, alias: String? = null): SQLQueryBuilder {
        tables.add(Table(tableName, alias))
        return this
    }

    fun join(field1: String, field2: String): SQLQueryBuilder {
        if (filter == null) {
            filter = CompareFilter.create(field1, field2)
        } else {
            filter?.and(CompareFilter.create(field1, field2))
        }
        return this
    }

    fun join(tableName: String, field1: String, field2: String): SQLQueryBuilder {
        join(JoinType.JOIN, tableName, field1, field2)
        return this
    }

    fun join(joinType: JoinType, tableName: String?, field1: String?, field2: String?): SQLQueryBuilder {
        if (tableName == null || field1 == null || field2 == null) {
            return this
        }

        joins.add(Join(joinType, tableName, CompareFilter.create(field1, field2)))
        return this
    }

    fun join(tableName: String, vararg filters: Filter): SQLQueryBuilder {
        return join(JoinType.JOIN, tableName, *filters)
    }

    fun join(joinType: JoinType, tableName: String, vararg filters: Filter): SQLQueryBuilder {
        return join(Join(joinType, tableName, AndFilter.create(*filters)))
    }

    fun join(vararg joins: Join): SQLQueryBuilder {
        this.joins.addAll(joins.toList())
        return this
    }

    fun filter(field: String, value: Any): SQLQueryBuilder {
        return filter(CompareFilter.create(field, value))
    }

    fun filter(field: String, compare: CompareType, value: Any): SQLQueryBuilder {
        return filter(CompareFilter.create(field, compare, value))
    }

    fun filter(field: String, compare: CompareType): SQLQueryBuilder {
        when (compare) {
            CompareType.IS_NULL, CompareType.NOT_NULL -> return filter(CompareFilter.create(field, compare))
            else -> throw IllegalArgumentException("Illegal 1 argument compare $compare")
        }
    }

    fun filter(filter: String): SQLQueryBuilder {
        filter(RawFilter.create(filter))
        return this
    }

    fun filter(filter: Filter): SQLQueryBuilder {
        if (this.filter == null) {
            this.filter = filter
        } else {
            this.filter?.and(filter)
        }
        return this
    }

    fun groupBy(item: String): SQLQueryBuilder {
        groupBys.add(item)
        return this
    }

    fun having(field: String, value: Any): SQLQueryBuilder {
        return having(CompareFilter.create(field, value))
    }

    fun having(field: String, compare: CompareType, value: Any): SQLQueryBuilder {
        return having(CompareFilter.create(field, compare, value))
    }

    fun having(field: String, compare: CompareType): SQLQueryBuilder {
        when (compare) {
            CompareType.IS_NULL, CompareType.NOT_NULL -> return having(CompareFilter.create(field, compare))
            else -> throw IllegalArgumentException("Illegal 1 argument compare $compare")
        }
    }

    fun having(filter: String): SQLQueryBuilder {
        return having(RawFilter.create(filter))
    }

    fun having(having: Filter): SQLQueryBuilder {
        if (this.having == null) {
            this.having = having
        } else {
            this.having?.and(having)
        }
        return this
    }

    fun orderBy(item: String): SQLQueryBuilder {
        orderBys.add(item)
        return this
    }

    fun orderBy(vararg items: String): SQLQueryBuilder {
        orderBys.addAll(items.toList())
        return this
    }

    fun orderBy(item: String, ascending: Boolean): SQLQueryBuilder {
        val direction = if (ascending) "ASC" else "DESC"
        orderBys.add("$item $direction")
        return this
    }

    override fun buildQuery(): String {
        return buildQuery(false)
    }

    fun buildQuery(countOnly: Boolean): String {
        selectClause = ""
        postSelectClause = ""

        var query = StringBuilder("SELECT ")

        if (distinct) {
            query.append("DISTINCT ")
        }

        // fields
        if (countOnly) {
            query.append("count(*)")
        } else {
            if (fields.size > 0) {
                addListItems(query, fields, 0)
            } else {
                query.append("*")
            }
        }

        // save select portion
        selectClause = query.toString()

        // table names
        query = StringBuilder()
        query.append(" FROM ")
        addListItems(query, tables, 0)

        for (join in joins) {
            query.append(" ").append(join.buildJoin(this))
        }

        if (filter != null) {
            val whereClause = filter?.buildFilter(this)
            if (!whereClause.isNullOrEmpty()) {
                query.append(" WHERE ").append(whereClause)
            }
        }

        val groupBySectionCount = 0
        // add groupbys and having
        if (groupBys.size > 0 && !countOnly) {
            query.append(" GROUP BY ")
            addListItems(query, groupBys, groupBySectionCount)
            having?.let { query.append(" HAVING ").append(it.buildFilter(this)) }
        }

        val orderBySectionCount = 0
        // add orderbys
        if (orderBys.size > 0 && !countOnly) {
            query.append(" ORDER BY ")
            addListItems(query, orderBys, orderBySectionCount)
        }

        postSelectClause = query.toString()

        return selectClause + postSelectClause
    }

    override fun toString(): String {
        return buildQuery()
    }

    private fun addListItems(query: StringBuilder, list: List<*>, sectionItemCount: Int): Int {
        return addListItems(query, list, ", ", sectionItemCount)
    }

    private fun addListItems(query: StringBuilder, list: List<*>, separator: String, sectionItemCount: Int): Int {
        var newSectionCount = sectionItemCount

        for (aList in list) {
            if (newSectionCount > 0) {
                query.append(separator)
            }

            query.append(aList)

            newSectionCount++
        }

        return newSectionCount
    }

    override fun formatLikeClause(field: String, value: String): String {
        return QueryUtil.formatLikeClause(field, value)
    }

    override fun formatIgnoreCaseLikeClause(field: String, value: String): String {
        return formatLikeClause(field, value)
    }

    /**
     * Getter for property selectClause.
     *
     * @return Value of property selectClause.
     */
    fun getSelectClause(): String? {
        if (selectClause?.isEmpty() == true) {
            buildQuery()
        }

        return selectClause
    }

    override fun formatValue(value: Any?): Any? {
        if (value is Boolean) {
            return formatBoolean(value)
        }
        return value
    }

    fun formatBoolean(b: Boolean): Int {
        return if (b) 1 else 0
    }

    fun isDistinct(): Boolean {
        return distinct
    }

    fun distinct(distinct: Boolean): SQLQueryBuilder {
        this.distinct = distinct
        return this
    }

    fun getFields(): List<Field> {
        return fields
    }

    fun getTables(): List<Table> {
        return tables
    }

    fun getJoins(): List<Join> {
        return joins
    }

    fun getGroupBys(): List<String> {
        return groupBys
    }

    fun getOrderBys(): List<String> {
        return orderBys
    }

    companion object {
        const val DEFAULT_QUERY_PARAMETER: String = "?"

        fun build(): SQLQueryBuilder {
            return SQLQueryBuilder()
        }

        fun union(vararg sqlQueryBuilders: SQLQueryBuilder): String {
            return union(false, *sqlQueryBuilders)
        }

        fun unionAll(vararg sqlQueryBuilders: SQLQueryBuilder): String {
            return union(true, *sqlQueryBuilders)
        }

        private fun union(unionAll: Boolean, vararg sqlQueryBuilders: SQLQueryBuilder): String {
            val query = StringBuilder()

            query.append("(")
            var count = 0
            sqlQueryBuilders.forEach { sql ->
                if (count > 0) {
                    query.append(if (unionAll) " UNION ALL " else " UNION ")
                }

                query.append(sql.toString())

                count++
            }
            query.append(")")

            return query.toString()
        }
    }
}
