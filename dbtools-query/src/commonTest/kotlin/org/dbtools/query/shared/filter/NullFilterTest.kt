package org.dbtools.query.shared.filter

import org.dbtools.query.sql.SQLQueryBuilder
import kotlin.test.Test
import kotlin.test.assertEquals

class NullFilterTest {
    @Test
    fun testFilter() {
        val filter1 = NullFilter.create("A")
        assertEquals("A IS NULL", filter1.buildFilter(SQLQueryBuilder()))

        val filter2 = NullFilter.create("A", true)
        assertEquals("A IS NULL", filter2.buildFilter(SQLQueryBuilder()))

        val filter3 = NullFilter.create("A", false)
        assertEquals("A NOT NULL", filter3.buildFilter(SQLQueryBuilder()))
    }

    @Test
    fun testAnd() {
        val filter: NullFilter = NullFilter.create("A").and("B", true).and("C", false).and("D")
        assertEquals("A IS NULL AND B IS NULL AND C NOT NULL AND D IS NULL", filter.buildFilter(SQLQueryBuilder()))
    }

    @Test
    fun testOr() {
        val filter: NullFilter = NullFilter.create("A").or("B", true).or("C", false).or("D")
        assertEquals("A IS NULL OR B IS NULL OR C NOT NULL OR D IS NULL", filter.buildFilter(SQLQueryBuilder()))
    }

    @Test
    fun testComplexFilter() {
        val filter: NullFilter = NullFilter.create("A").and("B", true).or("C", false).and("D")
        assertEquals("((A IS NULL AND B IS NULL) OR C NOT NULL) AND D IS NULL", filter.buildFilter(SQLQueryBuilder()))
    }
}