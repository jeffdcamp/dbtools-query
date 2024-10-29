package org.dbtools.query.shared.filter

import assertk.assertFailure
import org.dbtools.query.sql.SQLQueryBuilder
import kotlin.test.Test
import kotlin.test.assertEquals

class InFilterTest {
    @Test
    fun testFilter() {
        val filter1 = InFilter.create("A", "B")
        assertEquals("A IN (B)", filter1.buildFilter(SQLQueryBuilder()))

        val filter2 = InFilter.create("A", listOf("B", "C", "D"))
        assertEquals("A IN (B, C, D)", filter2.buildFilter(SQLQueryBuilder()))

        val filter3 = InFilter.create("A", null.toString())
        assertEquals("A IN (null)", filter3.buildFilter(SQLQueryBuilder()))

        val filter4 = InFilter.create("A", false, "B")
        assertEquals("A NOT IN (B)", filter4.buildFilter(SQLQueryBuilder()))

        val filter5 = InFilter.create("A", false, listOf("B", "C", "D"))
        assertEquals("A NOT IN (B, C, D)", filter5.buildFilter(SQLQueryBuilder()))

        val filter6 = InFilter.create("A", false, null.toString())
        assertEquals("A NOT IN (null)", filter6.buildFilter(SQLQueryBuilder()))

        val subQuery = SQLQueryBuilder().field("Z").table("Y")
        val filter7 = InFilter.create("A", subQuery)
        assertEquals("A IN (SELECT Z FROM Y)", filter7.buildFilter(SQLQueryBuilder()))

        val filter8 = InFilter.create("A", true, subQuery)
        assertEquals("A IN (SELECT Z FROM Y)", filter8.buildFilter(SQLQueryBuilder()))

        val filter9 = InFilter.create("A", false, subQuery)
        assertEquals("A NOT IN (SELECT Z FROM Y)", filter9.buildFilter(SQLQueryBuilder()))
    }

    @Test
    fun testAndFilter() {
        val subQuery = SQLQueryBuilder().field("Z").table("Y")
        val filter: InFilter = InFilter.create("A", false, "B").and("A", true, listOf("B", "C", "D")).and("A", null.toString()).and("A", subQuery)
        assertEquals("A NOT IN (B) AND A IN (B, C, D) AND A IN (null) AND A IN (SELECT Z FROM Y)", filter.buildFilter(SQLQueryBuilder()))
    }

    @Test
    fun testOrFilter() {
        val subQuery = SQLQueryBuilder().field("Z").table("Y")
        val filter: InFilter = InFilter.create("A", "B").or("A", true, listOf("B", "C", "D")).or("A", false, null.toString()).or("C", "D").or("A", subQuery)
        assertEquals("A IN (B) OR A IN (B, C, D) OR A NOT IN (null) OR C IN (D) OR A IN (SELECT Z FROM Y)", filter.buildFilter(SQLQueryBuilder()))
    }

    @Test
    fun testComplexFilter() {
        val subQuery = SQLQueryBuilder().field("Z").table("Y")
        val filter = InFilter.create("A", "B").or("A", false, listOf("B", "C", "D")).or("A", true, subQuery).and("A", null.toString())
            .and("A", false, subQuery)
        assertEquals(
            "(A IN (B) OR A NOT IN (B, C, D) OR A IN (SELECT Z FROM Y)) AND A IN (null) AND A NOT IN (SELECT Z FROM Y)",
            filter.buildFilter(SQLQueryBuilder())
        )
    }

    @Test
    fun testBadArgument() {
        assertFailure {
            InFilter("A", true, emptyList<Any>())
            error("Should have thrown an exception")
        }
    }

//    @Test
//    fun testClone() {
//        val subQuery: JPAQueryBuilder<*> = JPAQueryBuilder<Any?>()
//        subQuery.`object`("Y", "Y")
//        subQuery.field("Y", "Z")
//        val filter: InFilter = InFilter.create("A", "B").and("A", subQuery)
//        val clone = filter.clone()
//        assertEquals(filter.buildFilter(JPAQueryBuilder<Any?>()), clone.buildFilter(JPAQueryBuilder<Any?>()))
//    }
}