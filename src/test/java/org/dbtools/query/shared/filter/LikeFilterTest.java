package org.dbtools.query.shared.filter;

import org.dbtools.query.jpa.FirebirdQueryBuilder;
import org.dbtools.query.jpa.JPAQueryBuilder;
import org.dbtools.query.sql.PostgresqlQueryBuilder;
import org.dbtools.query.sql.SQLQueryBuilder;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class LikeFilterTest {

    @Test
    public void testFilter() throws Exception {
        LikeFilter filter = LikeFilter.create("A", "B", false);
        assertEquals("A LIKE B", filter.buildFilter(new JPAQueryBuilder()));
        assertEquals("A LIKE B", filter.buildFilter(new org.dbtools.query.jpa.DerbyQueryBuilder()));
        assertEquals("A LIKE B", filter.buildFilter(new org.dbtools.query.jpa.FirebirdQueryBuilder()));
        assertEquals("A LIKE B", filter.buildFilter(new org.dbtools.query.jpa.MysqlQueryBuilder()));
        assertEquals("A LIKE B", filter.buildFilter(new org.dbtools.query.jpa.OracleQueryBuilder()));
        assertEquals("A LIKE B", filter.buildFilter(new org.dbtools.query.jpa.PostgresqlQueryBuilder()));
        assertEquals("A LIKE B", filter.buildFilter(new SQLQueryBuilder()));
        assertEquals("A LIKE B", filter.buildFilter(new org.dbtools.query.sql.DerbyQueryBuilder()));
        assertEquals("A LIKE B", filter.buildFilter(new org.dbtools.query.sql.FirebirdQueryBuilder()));
        assertEquals("A LIKE B", filter.buildFilter(new org.dbtools.query.sql.MysqlQueryBuilder()));
        assertEquals("A LIKE B", filter.buildFilter(new org.dbtools.query.sql.OracleQueryBuilder()));
        assertEquals("A LIKE B", filter.buildFilter(new org.dbtools.query.sql.PostgresqlQueryBuilder()));
    }

    @Test
    public void testIgnoreCaseFilter() throws Exception {
        // IgnoreCase Implicit
        LikeFilter filter = LikeFilter.create("A", "B");
        assertEquals("JPA", "A LIKE B", filter.buildFilter(new JPAQueryBuilder()));
        assertEquals("Derby JPA", "LOWER(A) LIKE LOWER(B)", filter.buildFilter(new org.dbtools.query.jpa.DerbyQueryBuilder()));
        assertEquals("Firebird JPA", "A containing B", filter.buildFilter(new org.dbtools.query.jpa.FirebirdQueryBuilder()));
        assertEquals("Mysql JPA", "A LIKE B", filter.buildFilter(new org.dbtools.query.jpa.MysqlQueryBuilder()));
        assertEquals("Oracle JPA", "lower(A) LIKE b", filter.buildFilter(new org.dbtools.query.jpa.OracleQueryBuilder()));
        assertEquals("Postgresql JPA", "A ilike B", filter.buildFilter(new org.dbtools.query.jpa.PostgresqlQueryBuilder()));
        assertEquals("SQL", "A LIKE B", filter.buildFilter(new SQLQueryBuilder()));
        assertEquals("Derby SQL", "LOWER(A) LIKE LOWER(B)", filter.buildFilter(new org.dbtools.query.sql.DerbyQueryBuilder()));
        assertEquals("Firebird SQL", "A containing B", filter.buildFilter(new org.dbtools.query.sql.FirebirdQueryBuilder()));
        assertEquals("Mysql SQL", "A LIKE B", filter.buildFilter(new org.dbtools.query.sql.MysqlQueryBuilder()));
        assertEquals("Oracle SQL", "REGEXP_LIKE(A, B, 'i')", filter.buildFilter(new org.dbtools.query.sql.OracleQueryBuilder()));
        assertEquals("Postgresql SQL", "A ilike B", filter.buildFilter(new org.dbtools.query.sql.PostgresqlQueryBuilder()));

        // IgnoreCase Explicit
        LikeFilter filter2 = LikeFilter.create("A", "B", true);
        assertEquals(filter.buildFilter(new org.dbtools.query.sql.FirebirdQueryBuilder()),
                filter2.buildFilter(new org.dbtools.query.sql.FirebirdQueryBuilder()));
    }

    @Test
    public void testAndFilter() throws Exception {
        LikeFilter filter = LikeFilter.create("A", "B", false).and("C", "D").and("E", "F", false);
        assertEquals("A LIKE B AND C ilike D AND E LIKE F", filter.buildFilter(new PostgresqlQueryBuilder()));
    }

    @Test
    public void testOrFilter() throws Exception {
        LikeFilter filter = LikeFilter.create("A", "B", false).or("C", "D").or("E", "F", false);
        assertEquals("A LIKE B OR C ilike D OR E LIKE F", filter.buildFilter(new PostgresqlQueryBuilder()));
    }

    @Test
    public void testComplexFilter() throws Exception {
        LikeFilter filter = LikeFilter.create("A", "B", false).and("C", "D").or("E", "F", false);
        assertEquals("(A LIKE B AND C ilike D) OR E LIKE F", filter.buildFilter(new PostgresqlQueryBuilder()));
    }

    @Test
    public void testClone() throws Exception {
        LikeFilter filter = LikeFilter.create("A", "B");
        LikeFilter clone = filter.clone();
        assertEquals(filter.buildFilter(new SQLQueryBuilder()), clone.buildFilter(new SQLQueryBuilder()));
    }

    @Test
    public void testToString() throws Exception {
        LikeFilter filter = LikeFilter.create("A", "B");
        assertNotEquals(filter.buildFilter(new FirebirdQueryBuilder()), filter.toString());
    }
}