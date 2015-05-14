package org.dbtools.query.shared.filter;

import org.dbtools.query.sql.SQLQueryBuilder;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class InParameterizedFilterTest {

    @Test
    public void testFilter() throws Exception {
        InParameterizedFilter filter1 = InParameterizedFilter.create("A", 1);
        assertEquals("In Implicit", "A IN (?)", filter1.buildFilter(new SQLQueryBuilder()));

        InParameterizedFilter filter2 = InParameterizedFilter.create("A", true, 2);
        assertEquals("In Explicit", "A IN (?, ?)", filter2.buildFilter(new SQLQueryBuilder()));

        InParameterizedFilter filter3 = InParameterizedFilter.create("A", false, 3);
        assertEquals("Not In", "A NOT IN (?, ?, ?)", filter3.buildFilter(new SQLQueryBuilder()));

        SQLQueryBuilder queryBuilder = new SQLQueryBuilder();
        queryBuilder.setQueryParameter("&*&");
        InParameterizedFilter filter4 = InParameterizedFilter.create("A", false, 4);
        assertEquals("Different Arg", "A NOT IN (&*&, &*&, &*&, &*&)", filter4.buildFilter(queryBuilder));
    }

    @Test
    public void testAndFilter() throws Exception {
        InParameterizedFilter filter = InParameterizedFilter.create("A", 1).and("B", true, 2).and("C", false, 3);
        assertEquals("A IN (?) AND B IN (?, ?) AND C NOT IN (?, ?, ?)", filter.buildFilter(new SQLQueryBuilder()));
    }

    @Test
    public void testOrFilter() throws Exception {
        InParameterizedFilter filter = InParameterizedFilter.create("A", 1).or("B", true, 2).or("C", false, 3);
        assertEquals("A IN (?) OR B IN (?, ?) OR C NOT IN (?, ?, ?)", filter.buildFilter(new SQLQueryBuilder()));
    }

    @Test
    public void testComplexFilter() throws Exception {
        InParameterizedFilter filter = InParameterizedFilter.create("A", 1).and("B", 2).or("C", 3);
        assertEquals("(A IN (?) AND B IN (?, ?)) OR C IN (?, ?, ?)", filter.buildFilter(new SQLQueryBuilder()));

    }

    @Test(expected = IllegalArgumentException.class)
    public void testBadNumParams() {
        InParameterizedFilter.create("A", 0);
    }

    @Test
    public void testClone() throws Exception {
        InParameterizedFilter filter = InParameterizedFilter.create("A", 5);
        InParameterizedFilter clone = filter.clone();
        assertEquals(filter.buildFilter(new SQLQueryBuilder()), clone.buildFilter(new SQLQueryBuilder()));
    }
}