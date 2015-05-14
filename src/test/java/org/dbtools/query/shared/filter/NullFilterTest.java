package org.dbtools.query.shared.filter;

import org.dbtools.query.sql.SQLQueryBuilder;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class NullFilterTest {

    @Test
    public void testFilter() throws Exception {
        NullFilter filter1 = NullFilter.create("A");
        assertEquals("Null Implicit", "A IS NULL", filter1.buildFilter(new SQLQueryBuilder()));

        NullFilter filter2 = NullFilter.create("A", true);
        assertEquals("Null Explicit", "A IS NULL", filter2.buildFilter(new SQLQueryBuilder()));

        NullFilter filter3 = NullFilter.create("A", false);
        assertEquals("Not Null", "A NOT NULL", filter3.buildFilter(new SQLQueryBuilder()));
    }

    @Test
    public void testAnd() throws Exception {
        NullFilter filter = NullFilter.create("A").and("B", true).and("C", false).and("D");
        assertEquals("A IS NULL AND B IS NULL AND C NOT NULL AND D IS NULL", filter.buildFilter(new SQLQueryBuilder()));
    }

    @Test
    public void testOr() throws Exception {
        NullFilter filter = NullFilter.create("A").or("B", true).or("C", false).or("D");
        assertEquals("A IS NULL OR B IS NULL OR C NOT NULL OR D IS NULL", filter.buildFilter(new SQLQueryBuilder()));
    }

    @Test
    public void testComplexFilter() throws Exception {
        NullFilter filter = NullFilter.create("A").and("B", true).or("C", false).and("D");
        assertEquals("((A IS NULL AND B IS NULL) OR C NOT NULL) AND D IS NULL", filter.buildFilter(new SQLQueryBuilder()));
    }

    @Test
    public void testClone() throws Exception {
        NullFilter filter = NullFilter.create("A").and("B", true).or("C", false);
        NullFilter clone = filter.clone();
        assertEquals(filter.buildFilter(new SQLQueryBuilder()), clone.buildFilter(new SQLQueryBuilder()));

    }
}