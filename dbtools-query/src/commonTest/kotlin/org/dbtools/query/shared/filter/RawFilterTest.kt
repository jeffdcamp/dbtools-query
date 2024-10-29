package org.dbtools.query.shared.filter

import org.dbtools.query.sql.SQLQueryBuilder
import kotlin.test.Test
import kotlin.test.assertEquals

class RawFilterTest {
    @Test
    fun testFilter() {
        val rawFilterFormatter = RawFilter.create("A = B")

        assertEquals("A = B", rawFilterFormatter.buildFilter(SQLQueryBuilder()))
    }

    @Test
    fun testAndFilter() {
        val rawFilterFormatter = RawFilter.create("A = B").and("C = D")

        assertEquals("A = B AND C = D", rawFilterFormatter.buildFilter(SQLQueryBuilder()))
    }

    @Test
    fun testOrFilter() {
        val rawFilterFormatter = RawFilter.create("A = B").or("C = D")

        assertEquals("A = B OR C = D", rawFilterFormatter.buildFilter(SQLQueryBuilder()))
    }

    @Test
    fun testComplexFilter() {
        val rawFilterFormatter = RawFilter.create("A = B").and("C = D").and("Y = Z").or("E = F").and("G = H").and("I = J")

        assertEquals("((A = B AND C = D AND Y = Z) OR E = F) AND G = H AND I = J", rawFilterFormatter.buildFilter(SQLQueryBuilder()))
    }
}