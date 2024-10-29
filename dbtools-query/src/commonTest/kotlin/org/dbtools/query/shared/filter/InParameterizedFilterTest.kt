package org.dbtools.query.shared.filter

import assertk.Assert
import assertk.assertFailure
import org.dbtools.query.sql.SQLQueryBuilder
import kotlin.test.Test
import kotlin.test.assertEquals

class InParameterizedFilterTest {
    @Test
    fun testFilter() {
        val filter1 = InParameterizedFilter.create("A", 1)
        assertEquals("A IN (?)", filter1.buildFilter(SQLQueryBuilder()))

        val filter2 = InParameterizedFilter.create("A", true, 2)
        assertEquals("A IN (?, ?)", filter2.buildFilter(SQLQueryBuilder()))

        val filter3 = InParameterizedFilter.create("A", false, 3)
        assertEquals("A NOT IN (?, ?, ?)", filter3.buildFilter(SQLQueryBuilder()))

        val queryBuilder = SQLQueryBuilder()
        queryBuilder.queryParameter = "&*&"
        val filter4 = InParameterizedFilter.create("A", false, 4)
        assertEquals("A NOT IN (&*&, &*&, &*&, &*&)", filter4.buildFilter(queryBuilder))
    }

    @Test
    fun testAndFilter() {
        val filter = InParameterizedFilter.create("A", 1).and("B", true, 2).and("C", false, 3)
        assertEquals("A IN (?) AND B IN (?, ?) AND C NOT IN (?, ?, ?)", filter.buildFilter(SQLQueryBuilder()))
    }

    @Test
    fun testOrFilter() {
        val filter = InParameterizedFilter.create("A", 1).or("B", true, 2).or("C", false, 3)
        assertEquals("A IN (?) OR B IN (?, ?) OR C NOT IN (?, ?, ?)", filter.buildFilter(SQLQueryBuilder()))
    }

    @Test
    fun testComplexFilter() {
        val filter = InParameterizedFilter.create("A", 1).and("B", 2).or("C", 3)
        assertEquals("(A IN (?) AND B IN (?, ?)) OR C IN (?, ?, ?)", filter.buildFilter(SQLQueryBuilder()))
    }

    @Test
    fun testBadNumParams() {
        assertFailure {
            InParameterizedFilter.create("A", 0)
        }
    }
}