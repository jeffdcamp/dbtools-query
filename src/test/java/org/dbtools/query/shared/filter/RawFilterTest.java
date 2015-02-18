package org.dbtools.query.shared.filter;

import org.dbtools.query.sql.SQLQueryBuilder;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class RawFilterTest {

    @Test
    public void testFilter() {
        RawFilter rawFilterFormatter = RawFilter.create("A = B");

        assertEquals("A = B", rawFilterFormatter.buildFilter(new SQLQueryBuilder()));
    }

    @Test
    public void testAndFilter() {
        RawFilter rawFilterFormatter = RawFilter.create("A = B").and("C = D");

        assertEquals("A = B AND C = D", rawFilterFormatter.buildFilter(new SQLQueryBuilder()));
    }

    @Test
    public void testOrFilter() {
        RawFilter rawFilterFormatter = RawFilter.create("A = B").or("C = D");

        assertEquals("A = B OR C = D", rawFilterFormatter.buildFilter(new SQLQueryBuilder()));
    }

    @Test
    public void testComplexFilter() {
        RawFilter rawFilterFormatter = RawFilter.create("A = B").and("C = D").and("Y = Z").or("E = F").and("G = H").and("I = J");

        assertEquals("((A = B AND C = D AND Y = Z) OR E = F) AND G = H AND I = J", rawFilterFormatter.buildFilter(new SQLQueryBuilder()));
    }

}