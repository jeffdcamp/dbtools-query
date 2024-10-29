package org.dbtools.query.sql

import org.dbtools.query.shared.CompareType
import org.dbtools.query.shared.JoinType
import org.dbtools.query.shared.filter.CompareFilter
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith


/**
 * @author Jeff
 */
class SQLQueryBuilderTest {
    @Test
    fun testBasicQuery() {
        // using default var
        val qb1 = SQLQueryBuilder()
            .table("Person")
        val query1: String = qb1.buildQuery()

        assertEquals("SELECT * FROM Person", query1)
    }

    @Test
    fun testBasicFieldQuery() {
        val sql = SQLQueryBuilder()
            .table("Person")
            .field("LastName")

        val query1: String = sql.buildQuery()
        assertEquals("SELECT LastName FROM Person", query1)

        val sql2 = SQLQueryBuilder()
        sql2.table("Person")
        sql2.fields("LastName", "FirstName", "Age")
        assertEquals("SELECT LastName, FirstName, Age FROM Person", sql2.buildQuery())
    }

    @Test
    fun testDistinct() {
        val sql = SQLQueryBuilder.build()
            .distinct(true)
            .table("Person")
            .field("LastName")
        assertEquals("SELECT DISTINCT LastName FROM Person", sql.buildQuery())
    }

    @Test
    fun testMultiTable() {
        // using default var
        val sql = SQLQueryBuilder()
        sql.table("Person", "p")
        sql.table("Status", "s")
        sql.table("Category", "c")

        sql.field("Person.*")
        sql.field("s.name", "stat_name")
        sql.field("c.name", "cat_name")

        sql.filter("p.ID", 5)

        val query1: String = sql.buildQuery()

        assertEquals("SELECT Person.*, s.name AS stat_name, c.name AS cat_name FROM Person p, Status s, Category c WHERE p.ID = 5", query1)
    }

    @Test
    fun testJoins() {
        val sql = SQLQueryBuilder()
        sql.table("Car")
        sql.join("Colors", "Color.ID", "Car.COLOR_ID")
        sql.field("Name")
        sql.orderBy("Color.Name")

        assertEquals("SELECT Name FROM Car JOIN Colors ON Color.ID = Car.COLOR_ID ORDER BY Color.Name", sql.buildQuery())
    }

    @Test
    fun testJoinsWithAnd() {
        val sql = SQLQueryBuilder()
        sql.table("Car")
        sql.join("Colors", CompareFilter.create("Color.ID", "Car.COLOR_ID"), CompareFilter.create("Color.COOL", "1"))
        sql.join("Make", "Car.MAKE_ID", "Make.ID")
        sql.field("Name")
        sql.orderBy("Color.Name")

        assertEquals("SELECT Name FROM Car JOIN Colors ON Color.ID = Car.COLOR_ID AND Color.COOL = 1 JOIN Make ON Car.MAKE_ID = Make.ID ORDER BY Color.Name", sql.buildQuery())
    }

    @Test
    fun testMultiJoins() {
        val sql = SQLQueryBuilder()
        sql.table("Car")
        sql.join("Color", "Color.ID", "Car.COLOR_ID")
        sql.join(JoinType.LEFT_JOIN, "Owner", "Owner.ID", "Car.OWNER_ID")
        sql.field("Name")
        sql.filter("Car.ID", 5)

        sql.orderBy("Color.Name")

        assertEquals("SELECT Name FROM Car JOIN Color ON Color.ID = Car.COLOR_ID LEFT JOIN Owner ON Owner.ID = Car.OWNER_ID WHERE Car.ID = 5 ORDER BY Color.Name", sql.buildQuery())
    }

    @Test
    fun testQueryParam() {
        val sql = SQLQueryBuilder()
        sql.table("Car")
        sql.filter("Car.ID", "?")

        assertEquals("SELECT * FROM Car WHERE Car.ID = ?", sql.buildQuery())
    }

    @Test
    fun testFilter() {
        val sql = SQLQueryBuilder()
        sql.table("Car")
        sql.filter("Car.ID", "?")
        sql.filter("Car.NAME", "'Ford'")
        sql.filter("Car.WHEELS", CompareType.GREATERTHAN, 4)
        sql.filter("Car.IS_COOL", true)

        assertEquals("SELECT * FROM Car WHERE Car.ID = ? AND Car.NAME = 'Ford' AND Car.WHEELS > 4 AND Car.IS_COOL = 1", sql.buildQuery())
    }

    @Test
    fun testCompareTypeNoneFilter() {
        val sql = SQLQueryBuilder()
        sql.table("Car")
        sql.filter("Car.ID = ? AND Car.NAME = 'FORD'")

        assertEquals("SELECT * FROM Car WHERE Car.ID = ? AND Car.NAME = 'FORD'", sql.buildQuery())
    }

    @Test
    fun testOr() {
        val sql = SQLQueryBuilder()
        sql.table("Car")
        sql.filter("Car.IS_COOL", true)
        sql.filter(CompareFilter.create("Car.ID", "?").or("Car.NAME", "'Ford'").or("Car.NAME", "'Chevy'"))
        sql.filter(CompareFilter.create("Car.WHEELS", CompareType.GREATERTHAN, 4).or("Car.WHEELS", CompareType.LESSTHAN_EQUAL, 2))

        assertEquals("SELECT * FROM Car WHERE Car.IS_COOL = 1 AND (Car.ID = ? OR Car.NAME = 'Ford' OR Car.NAME = 'Chevy') AND (Car.WHEELS > 4 OR Car.WHEELS <= 2)", sql.buildQuery())
    }

    @Test
    fun testOrderBy() {
        val sql1 = SQLQueryBuilder()
        sql1.table("Car")
        sql1.orderBy("Name")
        assertEquals("SELECT * FROM Car ORDER BY Name", sql1.buildQuery())

        val sql2 = SQLQueryBuilder()
        sql2.table("Car")
        sql2.orderBy("Name", false)
        assertEquals("SELECT * FROM Car ORDER BY Name DESC", sql2.buildQuery())

        val sql3 = SQLQueryBuilder()
        sql3.table("Car")
        sql3.orderBy("Name")
        sql3.orderBy("Color")
        assertEquals("SELECT * FROM Car ORDER BY Name, Color", sql3.buildQuery())

        val sql4 = SQLQueryBuilder()
        sql4.table("Car")
        sql4.filter("WHEELS", 4)
        sql4.orderBy("Name")
        sql4.orderBy("Color")
        assertEquals("SELECT * FROM Car WHERE WHEELS = 4 ORDER BY Name, Color", sql4.buildQuery())

        val sql5 = SQLQueryBuilder()
        sql5.table("Car")
        sql5.filter("WHEELS", 4)
        sql5.orderBy("Name", "Color")
        assertEquals("SELECT * FROM Car WHERE WHEELS = 4 ORDER BY Name, Color", sql5.buildQuery())
    }

    @Test
    fun testGroupBy() {
        val sql1 = SQLQueryBuilder()
        sql1.table("Car")
        sql1.field("Name")
        sql1.field("Color")
        sql1.groupBy("Name")
        assertEquals("SELECT Name, Color FROM Car GROUP BY Name", sql1.buildQuery())
    }

//    @Test
//    fun testApply() {
//        // QUERY 1
//        val sql1 = SQLQueryBuilder()
//        sql1.table("Car")
//        sql1.filter("Car.ID", "?")
//        assertEquals("SELECT * FROM Car WHERE Car.ID = ?", sql1.buildQuery())
//
//        // QUERY 2
//        val sql2 = SQLQueryBuilder()
//        sql2.filter("Car.NAME", "'Ford'")
//        sql2.filter("Car.WHEELS", CompareType.GREATERTHAN, 4)
//        sql2.filter("Car.IS_COOL", true)
//
//        sql1.apply(sql2)
//        assertEquals("SELECT * FROM Car WHERE Car.ID = ? AND Car.NAME = 'Ford' AND Car.WHEELS > 4 AND Car.IS_COOL = 1", sql2.buildQuery())
//
//        // make sure that sql1 is not effected
//        assertNotEquals(sql1.buildQuery(), sql2.buildQuery())
//    }

//    @Test
//    fun testApplyMultiple() {
//        // TABLES
//        val tables = SQLQueryBuilder()
//        tables.table("Car")
//
//        // COLUMNS
//        val columns = SQLQueryBuilder()
//        columns.field("Car.NAME")
//        columns.field("Car.TYPE", "CAR_TYPE")
//
//        // FILTERS
//        val filters = SQLQueryBuilder()
//        filters.filter("Car.ID", "?")
//        filters.filter("Car.NAME", "'Ford'")
//        filters.filter("Car.WHEELS", CompareType.GREATERTHAN, 4)
//        filters.filter("Car.IS_COOL", true)
//
//        // TEST
//        val expectedResult = "SELECT Car.NAME, Car.TYPE AS CAR_TYPE FROM Car WHERE Car.ID = ? AND Car.NAME = 'Ford' AND Car.WHEELS > 4 AND Car.IS_COOL = 1"
//
//        // apply
//        val test = SQLQueryBuilder()
//        test.apply(tables)
//        test.apply(columns)
//        test.apply(filters)
//        assertEquals(expectedResult, test.buildQuery())
//
//        // apply in different order
//        val test2 = SQLQueryBuilder()
//        test2.apply(tables)
//        test2.apply(filters)
//        test2.apply(columns)
//        assertEquals(expectedResult, test2.buildQuery())
//
//        // apply in different order
//        val test3 = SQLQueryBuilder()
//        test3.apply(filters)
//        test3.apply(columns)
//        test3.apply(tables)
//        assertEquals(expectedResult, test3.buildQuery())
//
//        // apply in different order
//        val test4 = SQLQueryBuilder()
//        test4.apply(columns)
//        test4.apply(filters)
//        test4.apply(tables)
//        assertEquals(expectedResult, test4.buildQuery())
//    }

    @Test
    fun testIsNullQuery() {
        val sql = SQLQueryBuilder()
        sql.table("Person")
        sql.filter("id", CompareType.IS_NULL)

        assertEquals("SELECT * FROM Person WHERE id IS NULL", sql.buildQuery().trim { it <= ' ' })
    }

    @Test
    fun testNotNullQuery() {
        val sql = SQLQueryBuilder()
        sql.table("Person")
        sql.filter("id", CompareType.NOT_NULL)

        assertEquals("SELECT * FROM Person WHERE id NOT NULL", sql.buildQuery().trim { it <= ' ' })
    }

    @Test
    fun testSubSelectTableQuery() {
        val subSql = SQLQueryBuilder()
        subSql.field("id")
        subSql.table("Person")

        val sql = SQLQueryBuilder()
        sql.table(subSql)

        assertEquals("SELECT * FROM (SELECT id FROM Person)", sql.buildQuery())
    }

    @Test
    fun testSubSelectInQuery() {
        val subSql = SQLQueryBuilder()
        subSql.field("id")
        subSql.table("Person")

        val sql = SQLQueryBuilder()
        sql.table("Family")
        sql.filter("HeadPerson", CompareType.IN, subSql)

        assertEquals("SELECT * FROM Family WHERE HeadPerson IN (SELECT id FROM Person)", sql.buildQuery())
    }

    @Test
    fun testInQuery() {
        val sql = SQLQueryBuilder()
        sql.table("Family")
        sql.filter("HeadPerson", CompareType.IN, listOf(1, 2, "\"C\""))

        assertEquals("SELECT * FROM Family WHERE HeadPerson IN (1, 2, \"C\")", sql.buildQuery())
    }

    @Test
    fun testUnionQuery() {
        val sql1 = SQLQueryBuilder()
        sql1.field("id")
        sql1.table("Person")

        val sql2 = SQLQueryBuilder()
        sql2.field("id")
        sql2.table("Family")

        assertEquals("(SELECT id FROM Person UNION SELECT id FROM Family)", SQLQueryBuilder.union(sql1, sql2))
    }

    @Test
    fun testUnionAllQuery() {
        val sql1 = SQLQueryBuilder()
        sql1.field("id")
        sql1.table("Person")

        val sql2 = SQLQueryBuilder()
        sql2.field("id")
        sql2.table("Family")

        assertEquals("(SELECT id FROM Person UNION ALL SELECT id FROM Family)", SQLQueryBuilder.unionAll(sql1, sql2))
    }

    @Test
    fun testComplexUnionQuery() {
        val sql1 = SQLQueryBuilder()
        sql1.field("id")
        sql1.table("Person")

        val sql2 = SQLQueryBuilder()
        sql2.field("id")
        sql2.table("Family")

        val union = SQLQueryBuilder()
        union.table(SQLQueryBuilder.union(sql1, sql2))

        assertEquals("SELECT * FROM (SELECT id FROM Person UNION SELECT id FROM Family)", union.buildQuery())
    }

    @Test
    fun testCloneBasicQuery() {
        // using default var
        val qb1 = SQLQueryBuilder()
        qb1.table("Person")
        val query1: String = qb1.buildQuery()

        assertEquals("SELECT * FROM Person", query1)

//        assertEquals("SELECT * FROM Person", qb1.clone().buildQuery())
    }

//    @Test
//    fun testCloneBasicFieldQuery() {
//        val sql = SQLQueryBuilder()
//        sql.table("Person")
//        sql.field("LastName")
//        val query1: String = sql.buildQuery()
//        assertEquals("SELECT LastName FROM Person", query1)
//
//        // clone
////        assertEquals("SELECT LastName FROM Person", sql.clone().buildQuery())
//
//        val sql2 = SQLQueryBuilder()
//        sql2.table("Person")
//        sql2.fields("LastName", "FirstName", "Age")
//        assertEquals("SELECT LastName, FirstName, Age FROM Person", sql2.buildQuery())
//
//        // clone
////        assertEquals("SELECT LastName, FirstName, Age FROM Person", sql2.clone().buildQuery())
//    }

//    @Test
//    fun testApplyColumns() {
//        val sql1 = SQLQueryBuilder()
//        sql1.table("person")
//        sql1.field("name")
//        println("sql1a: " + sql1.buildQuery())
//
//        val sql2 = SQLQueryBuilder()
//        sql2.apply(sql1)
//        sql2.filter("phone", "?")
//        println("sql1b: " + sql1.buildQuery())
//        println("sql2: " + sql2.buildQuery())
//
//        assertNotEquals(sql1.buildQuery(), sql2.buildQuery())
//    }
//
//    @Test
//    fun testApplyWithFilters() {
//        val sql1 = SQLQueryBuilder()
//        sql1.table("person")
//        sql1.field("name")
//        sql1.filter("name", "?")
//        println("sql1a: " + sql1.buildQuery())
//
//        val sql2 = SQLQueryBuilder()
//        sql2.apply(sql1)
//        sql2.filter("id", "?")
//        println("sql1b: " + sql1.buildQuery())
//        println("sql2: " + sql2.buildQuery())
//
//        assertNotEquals(sql1.buildQuery(), sql2.buildQuery())
//    }

    @Test
    fun testHaving() {
        val sql = SQLQueryBuilder()
        sql.table("Car")
        sql.having("wheels", 4)
        assertEquals("SELECT * FROM Car", sql.buildQuery())
        sql.groupBy("make")
        assertEquals("SELECT * FROM Car GROUP BY make HAVING wheels = 4", sql.buildQuery())
        sql.having("color", CompareType.NOT_EQUAL, "\"red\"")
        assertEquals("SELECT * FROM Car GROUP BY make HAVING wheels = 4 AND color != \"red\"", sql.buildQuery())
        sql.having("value", CompareType.NOT_NULL)
        assertEquals("SELECT * FROM Car GROUP BY make HAVING wheels = 4 AND color != \"red\" AND value NOT NULL", sql.buildQuery())
        sql.having("working = 1")
        assertEquals("SELECT * FROM Car GROUP BY make HAVING wheels = 4 AND color != \"red\" AND value NOT NULL AND working = 1", sql.buildQuery())
    }

    @Test
    fun testHavingException() {
        assertFailsWith(IllegalArgumentException::class) {
            val sql = SQLQueryBuilder()
            sql.having("Word", CompareType.NOT_EQUAL)
        }
    }

//    @Test
//    fun testApplyWithHaving() {
//        val sql1 = SQLQueryBuilder()
//        sql1.table("person")
//        sql1.field("name")
//        sql1.groupBy("test")
//        sql1.having("name", "?")
//
//        val sql2 = SQLQueryBuilder()
//        sql2.apply(sql1)
//        sql2.having("id", "?")
//        assertNotEquals(sql1.buildQuery(), sql2.buildQuery())
//    }

//    companion object {
//        const val C_LAST_NAME: String = "lastName"
//    }
}