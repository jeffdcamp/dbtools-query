package org.dbtools.query.shared.filter;

import org.dbtools.query.shared.QueryBuilder;
import org.dbtools.query.sql.SQLQueryBuilder;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class AndFilterTest {

    @Test
    public void testFilter() throws Exception {
        RawFilter a = RawFilter.create("A");
        RawFilter b = RawFilter.create("B");
        RawFilter c = RawFilter.create("C");
        RawFilter d = RawFilter.create("D");

        AndFilter filter1 = AndFilter.create(a);
        assertEquals("1 Item", "A", filter1.buildFilter(new SQLQueryBuilder()));

        AndFilter filter2 = AndFilter.create(a, b);
        assertEquals("2 Items", "A AND B", filter2.buildFilter(new SQLQueryBuilder()));

        AndFilter filter3 = AndFilter.create(a, b, c);
        assertEquals("3 Items", "A AND B AND C", filter3.buildFilter(new SQLQueryBuilder()));

        AndFilter filter4 = AndFilter.create(AndFilter.create(a, b), AndFilter.create(c, d));
        assertEquals("2 Ands", "A AND B AND C AND D", filter4.buildFilter(new SQLQueryBuilder()));

        AndFilter filter5 = AndFilter.create(a, new Filter[]{b, c, d});
        assertEquals("Filter Array", "A AND B AND C AND D", filter5.buildFilter(new SQLQueryBuilder()));
    }


    @Test
    public void testAnd() throws Exception {
        RawFilter a = RawFilter.create("A");
        RawFilter b = RawFilter.create("B");
        RawFilter c = RawFilter.create("C");
        RawFilter d = RawFilter.create("D");
        QueryBuilder queryBuilder = new SQLQueryBuilder();
        OrFilter orFilter = OrFilter.create(c, d);
        AndFilter filter = AndFilter.create(a, d);
        assertEquals("A AND D", filter.buildFilter(queryBuilder));
        filter.and(a);
        assertEquals("A AND D AND A", filter.buildFilter(queryBuilder));
        filter.and(b, orFilter);
        assertEquals("A AND D AND A AND B AND (C OR D)", filter.buildFilter(queryBuilder));
    }

    @Test
    public void testOr() throws Exception {
        RawFilter a = RawFilter.create("A");
        RawFilter b = RawFilter.create("B");
        RawFilter c = RawFilter.create("C");
        RawFilter d = RawFilter.create("D");
        QueryBuilder queryBuilder = new SQLQueryBuilder();
        OrFilter orFilter = OrFilter.create(c, d);
        AndFilter filter = AndFilter.create(a, d);
        assertEquals("A AND D", filter.buildFilter(queryBuilder));
        filter.or(a);
        assertEquals("(A AND D) OR A", filter.buildFilter(queryBuilder));
        filter.or(b, orFilter);
        assertEquals("(A AND D) OR A OR B OR C OR D", filter.buildFilter(queryBuilder));

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
        AndFilter filter = AndFilter.create(OrFilter.create(AndFilter.create(a, b), AndFilter.create(c, d)),
                OrFilter.create(AndFilter.create(e, f), AndFilter.create(g, h)));
        assertEquals("((A AND B) OR (C AND D)) AND ((E AND F) OR (G AND H))", filter.buildFilter(new SQLQueryBuilder()));

    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateExceptionNoArgs() {
        AndFilter.create();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateExceptionNullFilter() {
        RawFilter a = RawFilter.create("A");
        AndFilter.create(null, new Filter[]{a});
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateExceptionEmptyArray() {
        RawFilter a = RawFilter.create("A");
        AndFilter.create(a, new Filter[]{});
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAndExceptionEmpty() {
        RawFilter a = RawFilter.create("A");
        AndFilter.create(a, a).and();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testOrExceptionEmpty() {
        RawFilter a = RawFilter.create("A");
        AndFilter.create(a, a).or();
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
        AndFilter filter = AndFilter.create(OrFilter.create(AndFilter.create(a, b), AndFilter.create(c, d)),
                OrFilter.create(AndFilter.create(e, f), AndFilter.create(g, h)));
        AndFilter clone = filter.clone();
        assertEquals(filter.buildFilter(new SQLQueryBuilder()), clone.buildFilter(new SQLQueryBuilder()));


    }
}