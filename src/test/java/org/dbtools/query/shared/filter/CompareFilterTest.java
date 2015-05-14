package org.dbtools.query.shared.filter;

import org.dbtools.query.shared.CompareType;
import org.dbtools.query.sql.FirebirdQueryBuilder;
import org.dbtools.query.sql.SQLQueryBuilder;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class CompareFilterTest {

    @Test
    public void testFilter() throws Exception {
        // Test Equal implicit
        CompareFilter filter1 = CompareFilter.create("A", "B");
        assertEquals("Equal Implicit", "A = B", filter1.buildFilter(new SQLQueryBuilder()));

        // Test Equal explicit
        CompareFilter filter2 = CompareFilter.create("A", CompareType.EQUAL, "B");
        assertEquals("Equal explicit", "A = B", filter2.buildFilter(new SQLQueryBuilder()));

        // Test Not Equal
        CompareFilter filter3 = CompareFilter.create("A", CompareType.NOT_EQUAL, "B");
        assertEquals("Not Equal", "A != B", filter3.buildFilter(new SQLQueryBuilder()));

        // Test Less Than
        CompareFilter filter4 = CompareFilter.create("A", CompareType.LESSTHAN, "B");
        assertEquals("Less Than", "A < B", filter4.buildFilter(new SQLQueryBuilder()));

        // Test Greater Than
        CompareFilter filter5 = CompareFilter.create("A", CompareType.GREATERTHAN, "B");
        assertEquals("Greater Than", "A > B", filter5.buildFilter(new SQLQueryBuilder()));

        // Test Less Than Equal
        CompareFilter filter6 = CompareFilter.create("A", CompareType.LESSTHAN_EQUAL, "B");
        assertEquals("Less Than Equal", "A <= B", filter6.buildFilter(new SQLQueryBuilder()));

        // Test Greater Than Equal
        CompareFilter filter7 = CompareFilter.create("A", CompareType.GREATERTHAN_EQUAL, "B");
        assertEquals("Greater Than Equal", "A >= B", filter7.buildFilter(new SQLQueryBuilder()));

        // Test Like
        CompareFilter filter8 = CompareFilter.create("A", CompareType.LIKE, "B");
        assertEquals("Like", "A LIKE B", filter8.buildFilter(new SQLQueryBuilder()));

        // Test Like Ignorecase
        CompareFilter filter9 = CompareFilter.create("A", CompareType.LIKE_IGNORECASE, "B");
        assertEquals("Like Ignorecase", "A LIKE B", filter9.buildFilter(new SQLQueryBuilder()));
        assertEquals("Like Ignorecase Firebird", "A containing B", filter9.buildFilter(new FirebirdQueryBuilder()));

        // Test In
        CompareFilter filter10 = CompareFilter.create("A", CompareType.IN, "B");
        assertEquals("In", "A IN (B)", filter10.buildFilter(new SQLQueryBuilder()));

        // Test Not In
        CompareFilter filter15 = CompareFilter.create("A", CompareType.NOT_IN, "B");
        assertEquals("In", "A NOT IN (B)", filter15.buildFilter(new SQLQueryBuilder()));

        // Test Is Null no value
        CompareFilter filter11 = CompareFilter.create("A", CompareType.IS_NULL);
        assertEquals("Is Null no value", "A IS NULL", filter11.buildFilter(new SQLQueryBuilder()));

        // Test Is Null value
        CompareFilter filter12 = CompareFilter.create("A", CompareType.IS_NULL, "B");
        assertEquals("Is Null value", "A IS NULL", filter12.buildFilter(new SQLQueryBuilder()));

        // Test Not Null no value
        CompareFilter filter13 = CompareFilter.create("A", CompareType.NOT_NULL);
        assertEquals("Not Null no value", "A NOT NULL", filter13.buildFilter(new SQLQueryBuilder()));

        // Test Not Null value
        CompareFilter filter14 = CompareFilter.create("A", CompareType.NOT_NULL, "B");
        assertEquals("Not Null value", "A NOT NULL", filter14.buildFilter(new SQLQueryBuilder()));
    }

    @Test
    public void testAndFilter() throws Exception {
        CompareFilter filter = CompareFilter.create("A", "B").and("C", "D").and("E", CompareType.IS_NULL).and("F", CompareType.GREATERTHAN_EQUAL, "G");
        assertEquals("A = B AND C = D AND E IS NULL AND F >= G", filter.buildFilter(new SQLQueryBuilder()));
    }

    @Test
    public void testOrFilter() throws Exception {
        CompareFilter filter = CompareFilter.create("A", "B").or("C", "D").or("E", CompareType.IS_NULL).or("F", CompareType.GREATERTHAN_EQUAL, "G");
        assertEquals("A = B OR C = D OR E IS NULL OR F >= G", filter.buildFilter(new SQLQueryBuilder()));
    }

    @Test
    public void testComplexFilter() throws Exception {
        CompareFilter filter = CompareFilter.create("A", "B").or("C", "D").or("E", CompareType.IS_NULL).and("F", CompareType.GREATERTHAN_EQUAL, "G")
                .or("H", "I");
        assertEquals("((A = B OR C = D OR E IS NULL) AND F >= G) OR H = I", filter.buildFilter(new SQLQueryBuilder()));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBadComparison() throws Exception {
        CompareFilter.create("A", CompareType.EQUAL);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBadComparisonAnd() throws Exception {
        CompareFilter.create("A", "B").and("C", CompareType.EQUAL);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBadComparisonOr() throws Exception {
        CompareFilter.create("A", "B").or("C", CompareType.EQUAL);
    }

    @Test(expected = IllegalStateException.class)
    public void testBadBuild() throws Exception {
        new CompareFilter("A", CompareType.IS_NULL, "B").build(new SQLQueryBuilder());
    }

    @Test
    public void testClone() throws Exception {
        CompareFilter filter = CompareFilter.create("A", CompareType.LIKE_IGNORECASE, "B");
        CompareFilter clone = filter.clone();
        assertEquals(filter.buildFilter(new SQLQueryBuilder()), clone.buildFilter(new SQLQueryBuilder()));
        assertNotEquals(filter.buildFilter(new SQLQueryBuilder()), clone.buildFilter(new FirebirdQueryBuilder()));
    }
}