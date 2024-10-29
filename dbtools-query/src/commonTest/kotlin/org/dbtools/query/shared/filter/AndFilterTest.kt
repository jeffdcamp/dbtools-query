package org.dbtools.query.shared.filter

import assertk.assertFailure
import assertk.assertThat
import assertk.assertions.isEqualTo
import org.dbtools.query.sql.SQLQueryBuilder
import kotlin.test.Test
import kotlin.test.assertEquals

class AndFilterTest {
    @Test
    fun testFilter() {
        val a = RawFilter.create("A")
        val b = RawFilter.create("B")
        val c = RawFilter.create("C")
        val d = RawFilter.create("D")

        val filter1 = AndFilter.create(a)
        assertThat(filter1.buildFilter(SQLQueryBuilder())).isEqualTo("A")

        val filter2 = AndFilter.create(a, b)
        assertEquals("A AND B", filter2.buildFilter(SQLQueryBuilder()))

        val filter3 = AndFilter.create(a, b, c)
        assertEquals( "A AND B AND C", filter3.buildFilter(SQLQueryBuilder()))

        val filter4 = AndFilter.create(AndFilter.create(a, b), AndFilter.create(c, d))
        assertEquals("A AND B AND C AND D", filter4.buildFilter(SQLQueryBuilder()))

        val filter5: AndFilter = AndFilter.create(a, arrayOf(b, c, d))
        assertEquals("A AND B AND C AND D", filter5.buildFilter(SQLQueryBuilder()))
    }

    @Test
    fun testAnd() {
        val a = RawFilter.create("A")
        val b = RawFilter.create("B")
        val c = RawFilter.create("C")
        val d = RawFilter.create("D")
        val queryBuilder = SQLQueryBuilder()
        val orFilter = OrFilter.create(c, d)
        val filter = AndFilter.create(a, d)
        assertEquals("A AND D", filter.buildFilter(queryBuilder))
        filter.and(a)
        assertEquals("A AND D AND A", filter.buildFilter(queryBuilder))
        filter.and(b, orFilter)
        assertEquals("A AND D AND A AND B AND (C OR D)", filter.buildFilter(queryBuilder))
    }

    @Test
    fun testOr() {
        val a = RawFilter.create("A")
        val b = RawFilter.create("B")
        val c = RawFilter.create("C")
        val d = RawFilter.create("D")
        val queryBuilder = SQLQueryBuilder()
        val orFilter = OrFilter.create(c, d)
        val filter = AndFilter.create(a, d)
        assertEquals("A AND D", filter.buildFilter(queryBuilder))
        filter.or(a)
        assertEquals("(A AND D) OR A", filter.buildFilter(queryBuilder))
        filter.or(b, orFilter)
        assertEquals("(A AND D) OR A OR B OR C OR D", filter.buildFilter(queryBuilder))
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
        val filter = AndFilter.create(
            OrFilter.create(AndFilter.create(a, b), AndFilter.create(c, d)),
            OrFilter.create(AndFilter.create(e, f), AndFilter.create(g, h))
        )
        assertEquals("((A AND B) OR (C AND D)) AND ((E AND F) OR (G AND H))", filter.buildFilter(SQLQueryBuilder()))
    }

    @Test
    fun testCreateExceptionNoArgs() {
        assertFailure {
            AndFilter.create()
        }
    }

    @Test
    fun testCreateExceptionEmptyArray() {
        assertFailure {
            val a = RawFilter.create("A")
            AndFilter.create(a, arrayOf())
        }
    }

    @Test
    fun testAndExceptionEmpty() {
        assertFailure {
            val a = RawFilter.create("A")
            AndFilter.create(a, a).and()
        }
    }

    @Test
    fun testOrExceptionEmpty() {
        assertFailure {
            val a = RawFilter.create("A")
            AndFilter.create(a, a).or()
        }
    }
}
