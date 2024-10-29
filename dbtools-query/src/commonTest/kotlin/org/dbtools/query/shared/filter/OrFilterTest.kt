package org.dbtools.query.shared.filter

import assertk.assertFailure
import assertk.assertThat
import assertk.assertions.isEqualTo
import org.dbtools.query.sql.SQLQueryBuilder
import kotlin.test.Test
import kotlin.test.assertEquals

class OrFilterTest {
    @Test
    fun testFilter() {
        val a = RawFilter.create("A")
        val b = RawFilter.create("B")
        val c = RawFilter.create("C")
        val d = RawFilter.create("D")

        val filter1 = OrFilter.create(a)
//        assertEquals("A", filter1.buildFilter(SQLQueryBuilder()))
        assertThat(filter1.buildFilter(SQLQueryBuilder())).isEqualTo("A")

        val filter2 = OrFilter.create(a, b)
        assertEquals("A OR B", filter2.buildFilter(SQLQueryBuilder()))

        val filter3 = OrFilter.create(a, b, c)
        assertEquals("A OR B OR C", filter3.buildFilter(SQLQueryBuilder()))

        val filter4 = OrFilter.create(OrFilter.create(a, b), OrFilter.create(c, d))
        assertEquals("A OR B OR C OR D", filter4.buildFilter(SQLQueryBuilder()))

        val filter5: OrFilter = OrFilter.create(a, arrayOf(b, c, d))
        assertEquals("A OR B OR C OR D", filter5.buildFilter(SQLQueryBuilder()))
    }


    @Test
    fun testAnd() {
        val a = RawFilter.create("A")
        val b = RawFilter.create("B")
        val c = RawFilter.create("C")
        val d = RawFilter.create("D")
        val queryBuilder = SQLQueryBuilder()
        val andFilter = AndFilter.create(c, d)
        val filter = OrFilter.create(a, d)
        assertEquals("A OR D", filter.buildFilter(queryBuilder))
        filter.and(a)
        assertEquals("(A OR D) AND A", filter.buildFilter(queryBuilder))
        filter.and(b, andFilter)
        assertEquals("(A OR D) AND A AND B AND C AND D", filter.buildFilter(queryBuilder))
    }

    @Test
    fun testOr() {
        val a = RawFilter.create("A")
        val b = RawFilter.create("B")
        val c = RawFilter.create("C")
        val d = RawFilter.create("D")
        val queryBuilder = SQLQueryBuilder()
        val andFilter = AndFilter.create(c, d)
        val filter = OrFilter.create(a, d)
        assertEquals("A OR D", filter.buildFilter(queryBuilder))
        filter.or(a)
        assertEquals("A OR D OR A", filter.buildFilter(queryBuilder))
        filter.or(b, andFilter)
        assertEquals("A OR D OR A OR B OR (C AND D)", filter.buildFilter(queryBuilder))
    }

    @Test
    fun testComplexFilter() {
        val a = RawFilter.create("A")
        val b = RawFilter.create("B")
        val c = RawFilter.create("C")
        val d = RawFilter.create("D")
        val e = RawFilter.create("E")
        val f = RawFilter.create("F")
        val g = RawFilter.create("G")
        val h = RawFilter.create("H")
        val filter = OrFilter.create(
            AndFilter.create(OrFilter.create(a, b), OrFilter.create(c, d)),
            AndFilter.create(OrFilter.create(e, f), OrFilter.create(g, h))
        )
        assertEquals("((A OR B) AND (C OR D)) OR ((E OR F) AND (G OR H))", filter.buildFilter(SQLQueryBuilder()))
    }

    @Test
    fun testCreateExceptionNoArgs() {
        assertFailure {
            OrFilter.create()
        }
    }

//    @Test
//    fun testCreateExceptionNullFilter() {
//        val a = RawFilter.create("A")
//        OrFilter.create(null, arrayOf<Filter>(a))
//    }

    @Test
    fun testCreateExceptionEmptyArray() {
        assertFailure {
            val a = RawFilter.create("A")
            OrFilter.create(a, arrayOf())
        }
    }

    @Test
    fun testAndExceptionEmpty() {
        assertFailure {
            val a = RawFilter.create("A")
            OrFilter.create(a, a).and()
        }
    }

    @Test
    fun testOrExceptionEmpty() {
        assertFailure {
            val a = RawFilter.create("A")
            OrFilter.create(a, a).or()
        }
    }
}