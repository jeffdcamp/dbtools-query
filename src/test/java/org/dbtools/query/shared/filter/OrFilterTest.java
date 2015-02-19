package org.dbtools.query.shared.filter;

import org.dbtools.query.shared.QueryBuilder;
import org.dbtools.query.sql.SQLQueryBuilder;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class OrFilterTest {

    @Test
    public void testFilter() throws Exception {
        RawFilter a = RawFilter.create("A");
        RawFilter b = RawFilter.create("B");
        RawFilter c = RawFilter.create("C");
        RawFilter d = RawFilter.create("D");

        OrFilter filter1 = OrFilter.create(a);
        assertEquals("1 Item", "A", filter1.buildFilter(new SQLQueryBuilder()));

        OrFilter filter2 = OrFilter.create(a, b);
        assertEquals("2 Items", "A OR B", filter2.buildFilter(new SQLQueryBuilder()));

        OrFilter filter3 = OrFilter.create(a, b, c);
        assertEquals("3 Items", "A OR B OR C", filter3.buildFilter(new SQLQueryBuilder()));

        OrFilter filter4 = OrFilter.create(OrFilter.create(a, b), OrFilter.create(c, d));
        assertEquals("2 Ands", "A OR B OR C OR D", filter4.buildFilter(new SQLQueryBuilder()));

        OrFilter filter5 = OrFilter.create(a, new Filter[]{b, c, d});
        assertEquals("Filter Array", "A OR B OR C OR D", filter5.buildFilter(new SQLQueryBuilder()));
    }


    @Test
    public void testAnd() throws Exception {
        RawFilter a = RawFilter.create("A");
        RawFilter b = RawFilter.create("B");
        RawFilter c = RawFilter.create("C");
        RawFilter d = RawFilter.create("D");
        QueryBuilder queryBuilder = new SQLQueryBuilder();
        AndFilter andFilter = AndFilter.create(c, d);
        OrFilter filter = OrFilter.create(a, d);
        assertEquals("A OR D", filter.buildFilter(queryBuilder));
        filter.and(a);
        assertEquals("(A OR D) AND A", filter.buildFilter(queryBuilder));
        filter.and(b, andFilter);
        assertEquals("(A OR D) AND A AND B AND C AND D", filter.buildFilter(queryBuilder));
    }

    @Test
    public void testOr() throws Exception {
        RawFilter a = RawFilter.create("A");
        RawFilter b = RawFilter.create("B");
        RawFilter c = RawFilter.create("C");
        RawFilter d = RawFilter.create("D");
        QueryBuilder queryBuilder = new SQLQueryBuilder();
        AndFilter andFilter = AndFilter.create(c, d);
        OrFilter filter = OrFilter.create(a, d);
        assertEquals("A OR D", filter.buildFilter(queryBuilder));
        filter.or(a);
        assertEquals("A OR D OR A", filter.buildFilter(queryBuilder));
        filter.or(b, andFilter);
        assertEquals("A OR D OR A OR B OR (C AND D)", filter.buildFilter(queryBuilder));

    }

    @Test
    public void testComplexFilter() throws Exception {
        RawFilter a = RawFilter.create("A");
        RawFilter b = RawFilter.create("B");
        RawFilter c = RawFilter.create("C");
        RawFilter d = RawFilter.create("D");
        RawFilter e = RawFilter.create("E");
        RawFilter f = RawFilter.create("F");
        RawFilter g = RawFilter.create("G");
        RawFilter h = RawFilter.create("H");
        OrFilter filter = OrFilter.create(AndFilter.create(OrFilter.create(a, b), OrFilter.create(c, d)),
                AndFilter.create(OrFilter.create(e, f), OrFilter.create(g, h)));
        assertEquals("((A OR B) AND (C OR D)) OR ((E OR F) AND (G OR H))", filter.buildFilter(new SQLQueryBuilder()));

    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateExceptionNoArgs() {
        OrFilter.create();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateExceptionNullFilter() {
        RawFilter a = RawFilter.create("A");
        OrFilter.create(null, new Filter[]{a});
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateExceptionEmptyArray() {
        RawFilter a = RawFilter.create("A");
        OrFilter.create(a, new Filter[]{});
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAndExceptionEmpty() {
        RawFilter a = RawFilter.create("A");
        OrFilter.create(a, a).and();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testOrExceptionEmpty() {
        RawFilter a = RawFilter.create("A");
        OrFilter.create(a, a).or();
    }

    @Test
    public void testClone() throws Exception {
        RawFilter a = RawFilter.create("A");
        RawFilter b = RawFilter.create("B");
        RawFilter c = RawFilter.create("C");
        RawFilter d = RawFilter.create("D");
        RawFilter e = RawFilter.create("E");
        RawFilter f = RawFilter.create("F");
        RawFilter g = RawFilter.create("G");
        RawFilter h = RawFilter.create("H");
        OrFilter filter = OrFilter.create(AndFilter.create(OrFilter.create(a, b), OrFilter.create(c, d)),
                AndFilter.create(OrFilter.create(e, f), OrFilter.create(g, h)));
        OrFilter clone = filter.clone();
        assertEquals(filter.buildFilter(new SQLQueryBuilder()), clone.buildFilter(new SQLQueryBuilder()));


    }
}