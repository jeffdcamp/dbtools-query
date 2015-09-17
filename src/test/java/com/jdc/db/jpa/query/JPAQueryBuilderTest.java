package com.jdc.db.jpa.query;

import org.dbtools.query.jpa.JPAQueryBuilder;
import org.junit.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

/**
 *
 * @author Jeff
 */
public class JPAQueryBuilderTest {

    public static final String P_LAST_NAME = "lastName";
    
    public JPAQueryBuilderTest() {
    }

    /**
     * Test of reset method, of class JPAQueryBuilder.
     */
    @Test
    public void reset() {
//        System.out.println("reset");
//        JPAQueryBuilder instance = new JPAQueryBuilder();
//        instance.reset();
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
    }

    /**
     * Test of executeQuery method, of class JPAQueryBuilder.
     */
    @Test
    public void testBasicQuery() {
        String defaultVar = JPAQueryBuilder.DEFAULT_OBJ_VAR;

        // using default var
        JPAQueryBuilder qb1 = new JPAQueryBuilder();
        qb1.object("Person");
        String query1 = qb1.toString();
        
        assertEquals("SELECT "+ defaultVar +" FROM Person "+ defaultVar, query1);
        
        
        // using user var
        JPAQueryBuilder qb2 = new JPAQueryBuilder();
        qb2.object("Person", "a");
        String query2 = qb2.toString();
        
        assertEquals("SELECT a FROM Person a", query2);
    }
    
    @Test
    public void testBasicFieldQuery() {
        String defaultVar = JPAQueryBuilder.DEFAULT_OBJ_VAR;

        // using default var
        JPAQueryBuilder qb1 = new JPAQueryBuilder();
        qb1.object("Person");
        qb1.field(P_LAST_NAME);
        String query1 = qb1.toString();
        
        assertEquals("SELECT "+ defaultVar +"."+ P_LAST_NAME +" FROM Person "+ defaultVar, query1);
        
        
        // using user var
        JPAQueryBuilder qb2 = new JPAQueryBuilder();
        qb2.object("Person", "a");
        qb2.field("a", P_LAST_NAME);
        String query2 = qb2.toString();
        
        assertEquals("SELECT a."+ P_LAST_NAME +" FROM Person a", query2);
    }

    @Test
    public void testDistinct() {
        JPAQueryBuilder qb1 = new JPAQueryBuilder();
        qb1.distinct(true);
        qb1.object("Person");
        qb1.field(P_LAST_NAME);
        String var = JPAQueryBuilder.DEFAULT_OBJ_VAR;
        assertEquals("SELECT DISTINCT " + var + "." + P_LAST_NAME + " FROM Person " + var, qb1.toString());
    }

    @Test
    public void testMultiObjQuery() {
        // using default var
        JPAQueryBuilder qb1 = new JPAQueryBuilder();
        String p = qb1.object("Person");
        String s = qb1.object("Status", "ID", p, "statusID");
        String c = qb1.object("Category", "ID", p, "categoryID");

        qb1.fieldObject(p);
        qb1.field(s, "name");
        qb1.field(c, "name");

        qb1.filter(p, "ID", 5);

        String query1 = qb1.toString();

        assertEquals("SELECT "+ p +", "+ s +".name, "+ c +".name FROM Person "+ p +", Status "+ s +", Category "+ c +" WHERE o2.ID = o.statusID AND o3.ID = o.categoryID AND o.ID = 5", query1);

    }

    @Test
    public void testCompareTypeNoneFilter() {
        JPAQueryBuilder qb1 = new JPAQueryBuilder();
        qb1.object("Person", "a");

        qb1.filter("a.lastName = \"Smith\"");

        String query1 = qb1.toString();

        assertEquals("SELECT a FROM Person a WHERE a.lastName = \"Smith\"", query1);
    }

    @Test
    public void testApplyWithFilters() {
        JPAQueryBuilder<Object> qb1 = new JPAQueryBuilder<Object>();
        String p = qb1.object("Person");
        qb1.fieldObject(p);

        qb1.filter(p, "ID", "?");

        JPAQueryBuilder<Object> qb2 = new JPAQueryBuilder<Object>();
        qb2.apply(qb1);
        qb2.filter(p, "name", "?");
        assertNotEquals(qb1.buildQuery(), qb2.buildQuery());
    }
}