/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.jdc.db.jpa.query;

import java.sql.Time;
import java.util.Date;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jeff
 */
public class JPAQueryBuilderTest {

    public static final String P_LAST_NAME = "lastName";
    
    public JPAQueryBuilderTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
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


}