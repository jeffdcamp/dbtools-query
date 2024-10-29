package org.dbtools.query.shared.filter

import org.dbtools.query.sql.SQLQueryBuilder
import kotlin.test.Test
import kotlin.test.assertEquals

class LikeFilterTest {
    @Test
    fun testFilter() {
        val filter = LikeFilter.create("A", "B", false)
        assertEquals("A LIKE B", filter.buildFilter(SQLQueryBuilder()))
        assertEquals("A LIKE B", filter.buildFilter(org.dbtools.query.sql.DerbyQueryBuilder()))
        assertEquals("A LIKE B", filter.buildFilter(org.dbtools.query.sql.FirebirdQueryBuilder()))
        assertEquals("A LIKE B", filter.buildFilter(org.dbtools.query.sql.MysqlQueryBuilder()))
        assertEquals("A LIKE B", filter.buildFilter(org.dbtools.query.sql.OracleQueryBuilder()))
        assertEquals("A LIKE B", filter.buildFilter(org.dbtools.query.sql.PostgresqlQueryBuilder()))
    }

    @Test
    fun testIgnoreCaseFilter() {
        // IgnoreCase Implicit
        val filter = LikeFilter.create("A", "B")
        // SQL
        assertEquals("A LIKE B", filter.buildFilter(SQLQueryBuilder()))
        // Derby SQL
        assertEquals( "LOWER(A) LIKE LOWER(B)", filter.buildFilter(org.dbtools.query.sql.DerbyQueryBuilder()))
        // Firebird SQL
        assertEquals( "A containing B", filter.buildFilter(org.dbtools.query.sql.FirebirdQueryBuilder()))
        // Mysql SQL
        assertEquals( "A LIKE B", filter.buildFilter(org.dbtools.query.sql.MysqlQueryBuilder()))
        // Oracle SQL
        assertEquals( "REGEXP_LIKE(A, B, 'i')", filter.buildFilter(org.dbtools.query.sql.OracleQueryBuilder()))
        // Postgresql SQL
        assertEquals( "A ilike B", filter.buildFilter(org.dbtools.query.sql.PostgresqlQueryBuilder()))

        // IgnoreCase Explicit
        val filter2 = LikeFilter.create("A", "B", true)
        assertEquals(
            filter.buildFilter(org.dbtools.query.sql.FirebirdQueryBuilder()),
            filter2.buildFilter(org.dbtools.query.sql.FirebirdQueryBuilder())
        )
    }

    @Test
    fun testAndFilter() {
        val filter = LikeFilter.create("A", "B", false).and("C", "D").and("E", "F", false)
        assertEquals("A LIKE B AND C ilike D AND E LIKE F", filter.buildFilter(org.dbtools.query.sql.PostgresqlQueryBuilder()))
    }

    @Test
    fun testOrFilter() {
        val filter = LikeFilter.create("A", "B", false).or("C", "D").or("E", "F", false)
        assertEquals("A LIKE B OR C ilike D OR E LIKE F", filter.buildFilter(org.dbtools.query.sql.PostgresqlQueryBuilder()))
    }

    @Test
    fun testComplexFilter() {
        val filter = LikeFilter.create("A", "B", false).and("C", "D").or("E", "F", false)
        assertEquals("(A LIKE B AND C ilike D) OR E LIKE F", filter.buildFilter(org.dbtools.query.sql.PostgresqlQueryBuilder()))
    }
}