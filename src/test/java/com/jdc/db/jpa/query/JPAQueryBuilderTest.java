package com.jdc.db.jpa.query;

import org.junit.*;

import static org.junit.Assert.assertEquals;

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
        qb1.addObject("Person");
        String query1 = qb1.toString();
        
        assertEquals("SELECT "+ defaultVar +" FROM Person "+ defaultVar, query1);
        
        
        // using user var
        JPAQueryBuilder qb2 = new JPAQueryBuilder();
        qb2.addObject("Person", "a");
        String query2 = qb2.toString();
        
        assertEquals("SELECT a FROM Person a", query2);
    }
    
    @Test
    public void testBasicFieldQuery() {
        String defaultVar = JPAQueryBuilder.DEFAULT_OBJ_VAR;

        // using default var
        JPAQueryBuilder qb1 = new JPAQueryBuilder();
        qb1.addObject("Person");
        qb1.addField(P_LAST_NAME);
        String query1 = qb1.toString();
        
        assertEquals("SELECT "+ defaultVar +"."+ P_LAST_NAME +" FROM Person "+ defaultVar, query1);
        
        
        // using user var
        JPAQueryBuilder qb2 = new JPAQueryBuilder();
        qb2.addObject("Person", "a");
        qb2.addField("a", P_LAST_NAME);
        String query2 = qb2.toString();
        
        assertEquals("SELECT a."+ P_LAST_NAME +" FROM Person a", query2);
    }

    @Test
    public void testMultiObjQuery() {
        // using default var
        JPAQueryBuilder qb1 = new JPAQueryBuilder();
        String p = qb1.addObject("Person");
        String s = qb1.addObject("Status", "ID", p, "statusID");
        String c = qb1.addObject("Category", "ID", p, "categoryID");

        qb1.addFieldObject(p);
        qb1.addField(s, "name");
        qb1.addField(c, "name");

        qb1.addFilter(p, "ID", 5);

        String query1 = qb1.toString();

        assertEquals("SELECT "+ p +", "+ s +".name, "+ c +".name FROM Person "+ p +", Status "+ s +", Category "+ c +" WHERE o2.ID = o.statusID AND o3.ID = o.categoryID AND o.ID = 5", query1);

    }

}