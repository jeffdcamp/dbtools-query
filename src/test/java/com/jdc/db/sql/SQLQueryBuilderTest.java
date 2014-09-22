package com.jdc.db.sql;

import com.jdc.db.shared.query.QueryCompareType;
import com.jdc.db.shared.query.QueryJoinType;
import com.jdc.db.sql.query.SQLQueryBuilder;
import org.junit.*;

import static org.junit.Assert.assertEquals;

/**
 * @author Jeff
 */
public class SQLQueryBuilderTest {

    public static final String C_LAST_NAME = "lastName";

    public SQLQueryBuilderTest() {
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

    @Test
    public void testBasicQuery() {
        // using default var
        SQLQueryBuilder qb1 = new SQLQueryBuilder();
        qb1.table("Person");
        String query1 = qb1.toString();

        assertEquals("SELECT * FROM Person", query1);
    }

    @Test
    public void testBasicFieldQuery() {
        SQLQueryBuilder sql = new SQLQueryBuilder();
        sql.table("Person");
        sql.field("LastName");
        String query1 = sql.toString();
        assertEquals("SELECT LastName FROM Person", query1);

        SQLQueryBuilder sql2 = new SQLQueryBuilder();
        sql2.table("Person");
        sql2.fields("LastName", "FirstName", "Age");
        assertEquals("SELECT LastName, FirstName, Age FROM Person", sql2.toString());
    }

    @Test
    public void testDistinct() {
        SQLQueryBuilder sql = new SQLQueryBuilder();
        sql.distinct(true);
        sql.table("Person");
        sql.field("LastName");
        assertEquals("SELECT DISTINCT LastName FROM Person", sql.toString());
    }

    @Test
    public void testMultiTable() {
        // using default var
        SQLQueryBuilder sql = new SQLQueryBuilder();
        sql.table("Person", "p");
        sql.table("Status", "s");
        sql.table("Category", "c");

        sql.field("Person.*");
        sql.field("s.name", "stat_name");
        sql.field("c.name", "cat_name");

        sql.addFilter("p.ID", 5);

        String query1 = sql.toString();

        assertEquals("SELECT Person.*, s.name AS stat_name, c.name AS cat_name FROM Person p, Status s, Category c WHERE p.ID = 5", query1);
    }

    @Test
    public void testJoins() {
        SQLQueryBuilder sql = new SQLQueryBuilder();
        sql.table("Car");
        sql.join("Colors", "Color.ID", "Car.COLOR_ID");
        sql.field("Name");
        sql.orderBy("Color.Name");

        assertEquals("SELECT Name FROM Car JOIN Colors ON Color.ID = Car.COLOR_ID ORDER BY Color.Name", sql.toString());
    }

    @Test
    public void testMultiJoins() {
        SQLQueryBuilder sql = new SQLQueryBuilder();
        sql.table("Car");
        sql.join("Color", "Color.ID", "Car.COLOR_ID");
        sql.join(QueryJoinType.LEFT_JOIN, "Owner", "Owner.ID", "Car.OWNER_ID");
        sql.field("Name");
        sql.addFilter("Car.ID", 5);

        sql.orderBy("Color.Name");

        assertEquals("SELECT Name FROM Car JOIN Color ON Color.ID = Car.COLOR_ID LEFT JOIN Owner ON Owner.ID = Car.OWNER_ID WHERE Car.ID = 5 ORDER BY Color.Name", sql.toString());
    }

    @Test
    public void testQueryParam() {
        SQLQueryBuilder sql = new SQLQueryBuilder();
        sql.table("Car");
        sql.addFilter("Car.ID", "?");

        assertEquals("SELECT * FROM Car WHERE Car.ID = ?", sql.toString());
    }

    @Test
    public void testFilter() {
        SQLQueryBuilder sql = new SQLQueryBuilder();
        sql.table("Car");
        sql.addFilter("Car.ID", "?");
        sql.addFilter("Car.NAME", "Ford");
        sql.addFilter("Car.WHEELS", QueryCompareType.GREATERTHAN, 4);
        sql.addFilter("Car.IS_COOL", true);

        assertEquals("SELECT * FROM Car WHERE Car.ID = ? AND Car.NAME = 'Ford' AND Car.WHEELS > 4 AND Car.IS_COOL = 1", sql.toString());
    }

    @Test
    public void testOr() {
        SQLQueryBuilder sql = new SQLQueryBuilder();
        sql.table("Car");
        sql.addFilterToGroup("Car.ID", "?", 1);
        sql.addFilterToGroup("Car.NAME", "Ford", 1);
        sql.addFilterToGroup("Car.NAME", "Chevy", 1);
        sql.addFilterToGroup("Car.WHEELS", QueryCompareType.GREATERTHAN, 4, 2);
        sql.addFilterToGroup("Car.WHEELS", QueryCompareType.LESSTHAN_EQUAL, 2, 2);
        sql.addFilter("Car.IS_COOL", true);

        assertEquals("SELECT * FROM Car WHERE Car.IS_COOL = 1 AND (Car.ID = ? OR Car.NAME = 'Ford' OR Car.NAME = 'Chevy') AND (Car.WHEELS > 4 OR Car.WHEELS <= 2)", sql.toString());
    }

    @Test
    public void testOrderBy() {
        SQLQueryBuilder sql1 = new SQLQueryBuilder();
        sql1.table("Car");
        sql1.orderBy("Name");
        assertEquals("SELECT * FROM Car ORDER BY Name", sql1.toString());

        SQLQueryBuilder sql2 = new SQLQueryBuilder();
        sql2.table("Car");
        sql2.orderBy("Name", false);
        assertEquals("SELECT * FROM Car ORDER BY Name DESC", sql2.toString());

        SQLQueryBuilder sql3 = new SQLQueryBuilder();
        sql3.table("Car");
        sql3.orderBy("Name");
        sql3.orderBy("Color");
        assertEquals("SELECT * FROM Car ORDER BY Name, Color", sql3.toString());

        SQLQueryBuilder sql4 = new SQLQueryBuilder();
        sql4.table("Car");
        sql4.addFilter("WHEELS", 4);
        sql4.orderBy("Name");
        sql4.orderBy("Color");
        assertEquals("SELECT * FROM Car WHERE WHEELS = 4 ORDER BY Name, Color", sql4.toString());
    }

    @Test
    public void testGroupBy() {
        SQLQueryBuilder sql1 = new SQLQueryBuilder();
        sql1.table("Car");
        sql1.field("Name");
        sql1.field("Color");
        sql1.groupBy("Name");
        assertEquals("SELECT Name, Color FROM Car GROUP BY Name", sql1.toString());
    }

    @Test
    public void testApply() {
        // QUERY 1
        SQLQueryBuilder sql1 = new SQLQueryBuilder();
        sql1.table("Car");
        sql1.addFilter("Car.ID", "?");
        assertEquals("SELECT * FROM Car WHERE Car.ID = ?", sql1.toString());

        // QUERY 2
        SQLQueryBuilder sql2 = new SQLQueryBuilder();
        sql2.addFilter("Car.NAME", "Ford");
        sql2.addFilter("Car.WHEELS", QueryCompareType.GREATERTHAN, 4);
        sql2.addFilter("Car.IS_COOL", true);

        sql1.apply(sql2);
        assertEquals("SELECT * FROM Car WHERE Car.ID = ? AND Car.NAME = 'Ford' AND Car.WHEELS > 4 AND Car.IS_COOL = 1", sql1.toString());
    }
}