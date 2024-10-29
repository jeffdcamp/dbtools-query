package org.dbtools.query.shared.filter

import assertk.assertFailure
import org.dbtools.query.shared.CompareType
import org.dbtools.query.sql.SQLQueryBuilder
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class CompareFilterTest {
    @Test
    fun testFilter() {
        // Test Equal implicit
        val filter1 = CompareFilter.create("A", "B")
        assertEquals("A = B", filter1.buildFilter(SQLQueryBuilder()))

        // Test Equal explicit
        val filter2 = CompareFilter.create("A", CompareType.EQUAL, "B")
        assertEquals( "A = B", filter2.buildFilter(SQLQueryBuilder()))

        // Test Not Equal
        val filter3 = CompareFilter.create("A", CompareType.NOT_EQUAL, "B")
        assertEquals("A != B", filter3.buildFilter(SQLQueryBuilder()))

        // Test Less Than
        val filter4 = CompareFilter.create("A", CompareType.LESSTHAN, "B")
        assertEquals("A < B", filter4.buildFilter(SQLQueryBuilder()))

        // Test Greater Than
        val filter5 = CompareFilter.create("A", CompareType.GREATERTHAN, "B")
        assertEquals( "A > B", filter5.buildFilter(SQLQueryBuilder()))

        // Test Less Than Equal
        val filter6 = CompareFilter.create("A", CompareType.LESSTHAN_EQUAL, "B")
        assertEquals( "A <= B", filter6.buildFilter(SQLQueryBuilder()))

        // Test Greater Than Equal
        val filter7 = CompareFilter.create("A", CompareType.GREATERTHAN_EQUAL, "B")
        assertEquals( "A >= B", filter7.buildFilter(SQLQueryBuilder()))

        // Test Like
        val filter8 = CompareFilter.create("A", CompareType.LIKE, "B")
        assertEquals( "A LIKE B", filter8.buildFilter(SQLQueryBuilder()))

        // Test Like Ignorecase
        val filter9 = CompareFilter.create("A", CompareType.LIKE_IGNORECASE, "B")
        assertEquals( "A LIKE B", filter9.buildFilter(SQLQueryBuilder()))
        assertEquals( "A containing B", filter9.buildFilter(org.dbtools.query.sql.FirebirdQueryBuilder()))

        // Test In
        val filter10 = CompareFilter.create("A", CompareType.IN, "B")
        assertEquals( "A IN (B)", filter10.buildFilter(SQLQueryBuilder()))

        // Test Not In
        val filter15 = CompareFilter.create("A", CompareType.NOT_IN, "B")
        assertEquals( "A NOT IN (B)", filter15.buildFilter(SQLQueryBuilder()))

        // Test Is Null no value
        val filter11 = CompareFilter.create("A", CompareType.IS_NULL)
        assertEquals( "A IS NULL", filter11.buildFilter(SQLQueryBuilder()))

        // Test Is Null value
        val filter12 = CompareFilter.create("A", CompareType.IS_NULL, "B")
        assertEquals("A IS NULL", filter12.buildFilter(SQLQueryBuilder()))

        // Test Not Null no value
        val filter13 = CompareFilter.create("A", CompareType.NOT_NULL)
        assertEquals("A NOT NULL", filter13.buildFilter(SQLQueryBuilder()))

        // Test Not Null value
        val filter14 = CompareFilter.create("A", CompareType.NOT_NULL, "B")
        assertEquals("A NOT NULL", filter14.buildFilter(SQLQueryBuilder()))
    }

    @Test
    fun testAndFilter() {
        val filter = CompareFilter.create("A", "B").and("C", "D").and("E", CompareType.IS_NULL).and("F", CompareType.GREATERTHAN_EQUAL, "G")
        assertEquals("A = B AND C = D AND E IS NULL AND F >= G", filter.buildFilter(SQLQueryBuilder()))
    }

    @Test
    fun testOrFilter() {
        val filter = CompareFilter.create("A", "B").or("C", "D").or("E", CompareType.IS_NULL).or("F", CompareType.GREATERTHAN_EQUAL, "G")
        assertEquals("A = B OR C = D OR E IS NULL OR F >= G", filter.buildFilter(SQLQueryBuilder()))
    }

    @Test
    fun testComplexFilter() {
        val filter = CompareFilter.create("A", "B").or("C", "D").or("E", CompareType.IS_NULL).and("F", CompareType.GREATERTHAN_EQUAL, "G")
            .or("H", "I")
        assertEquals("((A = B OR C = D OR E IS NULL) AND F >= G) OR H = I", filter.buildFilter(SQLQueryBuilder()))
    }

    @Test
    fun testBadComparison() {
        assertFailure {
            CompareFilter.create("A", CompareType.EQUAL)
        }
    }

    @Test
    fun testBadComparisonAnd() {
        assertFailure {
            CompareFilter.create("A", "B").and("C", CompareType.EQUAL)
        }
    }

    @Test
    fun testBadComparisonOr() {
        assertFailure {
            CompareFilter.create("A", "B").or("C", CompareType.EQUAL)
        }
    }

    @Test
    fun testBadBuild() {
        CompareFilter("A", CompareType.IS_NULL, "B").build(SQLQueryBuilder())
    }
}