package org.dbtools.query.shared.filter;

import org.dbtools.query.jpa.JPAQueryBuilder;
import org.dbtools.query.shared.QueryBuilder;
import org.dbtools.query.sql.SQLQueryBuilder;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;

public class InFilterTest {

    @Test
    public void testFilter() throws Exception {
        InFilter filter1 = InFilter.create("A", "B");
        assertEquals("Normal Test", "A IN (B)", filter1.buildFilter(new SQLQueryBuilder()));

        InFilter filter2 = InFilter.create("A", Arrays.asList("B", "C", "D"));
        assertEquals("Test List", "A IN (B, C, D)", filter2.buildFilter(new SQLQueryBuilder()));

        InFilter filter3 = InFilter.create("A", (Object)null);
        assertEquals("Test Null", "A IN (null)", filter3.buildFilter(new SQLQueryBuilder()));

        InFilter filter4 = InFilter.create("A", false, "B");
        assertEquals("Normal Test", "A NOT IN (B)", filter4.buildFilter(new SQLQueryBuilder()));

        InFilter filter5 = InFilter.create("A", false, Arrays.asList("B", "C", "D"));
        assertEquals("Test List", "A NOT IN (B, C, D)", filter5.buildFilter(new SQLQueryBuilder()));

        InFilter filter6 = InFilter.create("A", false, (Object)null);
        assertEquals("Test Null", "A NOT IN (null)", filter6.buildFilter(new SQLQueryBuilder()));

        QueryBuilder subQuery = new SQLQueryBuilder().field("Z").table("Y");
        InFilter filter7 = InFilter.create("A", subQuery);
        assertEquals("Test QueryBuilder Implicit", "A IN (SELECT Z FROM Y)", filter7.buildFilter(new SQLQueryBuilder()));

        InFilter filter8 = InFilter.create("A", true, subQuery);
        assertEquals("Test QueryBuilder Explicit", "A IN (SELECT Z FROM Y)", filter8.buildFilter(new SQLQueryBuilder()));

        InFilter filter9 = InFilter.create("A", false, subQuery);
        assertEquals("Test QueryBuilder", "A NOT IN (SELECT Z FROM Y)", filter9.buildFilter(new SQLQueryBuilder()));

    }

    @Test
    public void testAndFilter() throws Exception {
        QueryBuilder subQuery = new SQLQueryBuilder().field("Z").table("Y");
        InFilter filter = InFilter.create("A", false, "B").and("A", true, Arrays.asList("B", "C", "D")).and("A", (Object) null).and("A", subQuery);
        assertEquals("A NOT IN (B) AND A IN (B, C, D) AND A IN (null) AND A IN (SELECT Z FROM Y)", filter.buildFilter(new SQLQueryBuilder()));
    }

    @Test
    public void testOrFilter() throws Exception {
        QueryBuilder subQuery = new SQLQueryBuilder().field("Z").table("Y");
        InFilter filter = InFilter.create("A", "B").or("A", true, Arrays.asList("B", "C", "D")).or("A", false, (Object) null).or("C", "D").or("A", subQuery);
        assertEquals("A IN (B) OR A IN (B, C, D) OR A NOT IN (null) OR C IN (D) OR A IN (SELECT Z FROM Y)", filter.buildFilter(new SQLQueryBuilder()));
    }

    @Test
    public void testComplexFilter() throws Exception {
        QueryBuilder subQuery = new SQLQueryBuilder().field("Z").table("Y");
        InFilter filter = InFilter.create("A", "B").or("A", false, Arrays.asList("B", "C", "D")).or("A", true, subQuery).and("A", (Object) null)
                .and("A", false, subQuery);
        assertEquals("(A IN (B) OR A NOT IN (B, C, D) OR A IN (SELECT Z FROM Y)) AND A IN (null) AND A NOT IN (SELECT Z FROM Y)",
                filter.buildFilter(new SQLQueryBuilder()));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBadArgument() throws Exception {
        new InFilter("A", true, new ArrayList<Object>());
    }

    @Test
    public void testClone() throws Exception {
        JPAQueryBuilder subQuery = new JPAQueryBuilder();
        subQuery.object("Y", "Y");
        subQuery.field("Y", "Z");
        InFilter filter = InFilter.create("A", "B").and("A", subQuery);
        InFilter clone = filter.clone();
        assertEquals(filter.buildFilter(new JPAQueryBuilder()), clone.buildFilter(new JPAQueryBuilder()));
    }

}