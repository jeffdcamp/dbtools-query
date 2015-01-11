dbtools-query
=============

Setup
=====

  Add dbtools-query dependency to your "dependencies" section of the build.gradle file.  (latest version is found in Maven Central Repo: http://search.maven.org/#search%7Cga%7C1%7Ca%3A%22dbtools-query%22)

        dependencies {
            compile 'org.dbtools:dbtools-android:<latest version>'
        }

Trouble with writing SQL in source code
=======================================

  Writing SQL in Java can be painful because you spend a lot of time placing " and + between statements and variables (especially if you have your columns mapped to static variables).  Example:

        String query = "SELECT " + MyTable.C_NAME + ", " + MyTable.C_PHONE + ", " +
                            MyTable.C_ADDRESS + " FROM " + MyTable.TABLE_NAME;

  If you forget to put in a comma or enough spaces, then you will not know there is an issue until run-time.  In order to find the problem, you will have to log the SQL text and try to hunt for the minor error.

  DBTools Query (for the same query above):

        String query = new SQLQueryBuilder()
                            .field(MyTable.C_NAME)
                            .field(MyTable.C_PHONE)
                            .field(MyTable.C_ADDRESS)
                            .table(MyTable.TABLE_NAME)
                            .toString();

Useage
======

  * Select ALL

        // "SELECT * FROM Person"

        String query = new SQLQueryBuilder()
                            .table("Person")
                            .toString();

  * Fields

        // "SELECT LastName, FirstName FROM Person"

        String query = new SQLQueryBuilder()
                            .field("LastName")
                            .field("FirstName")
                            .table("Person")
                            .toString();

        // "SELECT LastName, FirstName, Age FROM Person"

        String query = new SQLQueryBuilder()
                            .fields("LastName", "FirstName", "Age")
                            .table("Person")
                            .toString();

  * Distinct

        // "SELECT DISTINCT LastName FROM Person"

        String query = new SQLQueryBuilder()
                            .distinct(true)
                            .field("LastName")
                            .table("Person")
                            .toString();

  * Order By

        // "SELECT * FROM Car ORDER BY Name"

        String query = new SQLQueryBuilder()
                            .table("Car");
                            .orderBy("Name");
                            .toString();

  * Group By

        // "SELECT Name, Color FROM Car GROUP BY Name"

        String query = new SQLQueryBuilder()
                            .table("Car");
                            .field("Name");
                            .field("Color");
                            .groupBy("Name");
                            .toString();

  * Filter

        // "SELECT * FROM Car WHERE Car.ID = ?
        // AND Car.NAME = 'Ford' AND Car.WHEELS > 4 AND Car.IS_COOL = 1"

        String query = new SQLQueryBuilder()
                            .table("Car");
                            .filter("Car.ID", "?");
                            .filter("Car.NAME", "Ford");
                            .filter("Car.WHEELS", QueryCompareType.GREATERTHAN, 4);
                            .filter("Car.IS_COOL", true);
                            .toString();

  * Filter Or

        // "SELECT * FROM Car WHERE Car.IS_COOL = 1
        // AND (Car.ID = ? OR Car.NAME = 'Ford' OR Car.NAME = 'Chevy')
        // AND (Car.WHEELS > 4 OR Car.WHEELS <= 2)"

        String query = new SQLQueryBuilder()
                            .table("Car");
                            .filterToGroup("Car.ID", "?", 1);
                            .filterToGroup("Car.NAME", "Ford", 1);
                            .filterToGroup("Car.NAME", "Chevy", 1);
                            .filterToGroup("Car.WHEELS", QueryCompareType.GREATERTHAN, 4, 2);
                            .filterToGroup("Car.WHEELS", QueryCompareType.LESSTHAN_EQUAL, 2, 2);
                            .filter("Car.IS_COOL", true);
                            .toString();


  * Multi Table

        // "SELECT Person.*, s.name AS stat_name, c.name AS cat_name
        // FROM Person p, Status s, Category c WHERE p.ID = 5"

        String query = new SQLQueryBuilder()
                            .distinct(true)
                            .table("Person", "p");
                            .table("Status", "s");
                            .table("Category", "c");

                            field("Person.*");
                            field("s.name", "stat_name");
                            field("c.name", "cat_name");

                            filter("p.ID", 5)
                            .toString();

  * Join

        // "SELECT Name FROM Car
        // JOIN Colors ON Color.ID = Car.COLOR_ID ORDER BY Color.Name"

        String query = new SQLQueryBuilder()
                            .table("Car");
                            .join("Colors", "Color.ID", "Car.COLOR_ID");
                            .field("Name");
                            .orderBy("Color.Name")
                            .toString();


  * Multiple Joins

        // "SELECT Name FROM Car
        // JOIN Color ON Color.ID = Car.COLOR_ID
        // LEFT JOIN Owner ON Owner.ID = Car.OWNER_ID
        // WHERE Car.ID = 5 ORDER BY Color.Name"

        String query = new SQLQueryBuilder()
                            .table("Car");
                            .join("Color", "Color.ID", "Car.COLOR_ID");
                            .join(QueryJoinType.LEFT_JOIN, "Owner", "Owner.ID", "Car.OWNER_ID");
                            .field("Name");
                            .filter("Car.ID", 5);
                            .orderBy("Color.Name")
                            .toString();

  * Joins with AND

        // "SELECT Name FROM Car JOIN Colors ON Color.ID = Car.COLOR_ID
        // AND Color.COOL = 1 JOIN Make ON Car.MAKE_ID = Make.ID ORDER BY Color.Name"

        String query = new SQLQueryBuilder()
                            .table("Car");
                            .join("Colors", new SQLFilterItem("Color.ID", "Car.COLOR_ID"), new SQLFilterItem("Color.COOL", "1"));
                            in("Make", "Car.MAKE_ID", "Make.ID");
                            .field("Name");
                            .orderBy("Color.Name")
                            .toString();

  * Sub-Select

        // "SELECT * FROM Family WHERE HeadPerson IN (SELECT id FROM Person)"

        SQLQueryBuilder subSql = new SQLQueryBuilder();
        subSql.field("id");
        subSql.table("Person");

        SQLQueryBuilder sql = new SQLQueryBuilder();
        sql.table("Family");
        sql.filter("HeadPerson", QueryCompareType.IN, subSql);

  * Union

        // "(SELECT id FROM Person UNION SELECT id FROM Family)"

        SQLQueryBuilder sql1 = new SQLQueryBuilder();
        sql1.field("id");
        sql1.table("Person");

        SQLQueryBuilder sql2 = new SQLQueryBuilder();
        sql2.field("id");
        sql2.table("Family");

        String query = SQLQueryBuilder.union(sql1, sql2)); // or use unionAll(...)

  * Complex Union

        // "SELECT * FROM (SELECT id FROM Person UNION SELECT id FROM Family)"

        SQLQueryBuilder sql1 = new SQLQueryBuilder();
        sql1.field("id");
        sql1.table("Person");

        SQLQueryBuilder sql2 = new SQLQueryBuilder();
        sql2.field("id");
        sql2.table("Family");

        SQLQueryBuilder union = new SQLQueryBuilder();
        union.table(SQLQueryBuilder.union(sql1, sql2));


  * Apply one query to another Query

        // Final Query = "SELECT * FROM Car WHERE Car.ID = ?
        // AND Car.NAME = 'Ford' AND Car.WHEELS > 4 AND Car.IS_COOL = 1"
        //
        // Query1 = "SELECT * FROM Car WHERE Car.ID = ?"
        // Query2 = Additional filters

        // QUERY 1
        SQLQueryBuilder sql1 = new SQLQueryBuilder();
        sql1.table("Car");
        sql1.filter("Car.ID", "?");
        assertEquals("SELECT * FROM Car WHERE Car.ID = ?", sql1.toString());

        // QUERY 2
        SQLQueryBuilder sql2 = new SQLQueryBuilder();
        sql2.filter("Car.NAME", "Ford");
        sql2.filter("Car.WHEELS", QueryCompareType.GREATERTHAN, 4);
        sql2.filter("Car.IS_COOL", true);

        sql1.apply(sql2);


License
=======

    Copyright 2014 Jeff Campbell

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
