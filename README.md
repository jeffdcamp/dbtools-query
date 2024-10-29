dbtools-query
=============

Kotlin Multiplatform library to build sql queries.

Setup
=====

  Add dbtools-query dependency to your "dependencies" section of the build.gradle file.  (latest version is found in Maven Central Repo: http://search.maven.org/#search%7Cga%7C1%7Ca%3A%22dbtools-query%22)

        dependencies {
            compile 'org.dbtools:dbtools-query:<latest version>'
        }

Trouble with writing SQL in source code
=======================================

  Writing dynamic SQL in code can be challenging (especially if you have your columns mapped to static variables).  Example:

        val query = "SELECT ${MyTable.C_NAME}, ${MyTable.C_PHONE}, ${MyTable.C_ADDRESS} FROM ${MyTable.TABLE_NAME}"

  If you forget to put in a comma or enough spaces, then you will not know there is an issue until run-time.  In order to find the problem, you will have to log the SQL text and try to hunt for the minor error.

  DBTools Query (for the same query above):

        val query = SQLQueryBuilder()
            .field(MyTable.C_NAME)
            .field(MyTable.C_PHONE)
            .field(MyTable.C_ADDRESS)
            .table(MyTable.TABLE_NAME)
            .buildQuery()

Usage
======

  * Select ALL

        // "SELECT * FROM Person"

        val query = SQLQueryBuilder()
            .table("Person")
            .buildQuery()

  * Fields

        // "SELECT LastName, FirstName FROM Person"

        val query = SQLQueryBuilder()
                            .field("LastName")
                            .field("FirstName")
                            .table("Person")
                            .buildQuery()

        // "SELECT LastName, FirstName, Age FROM Person"

        val query = SQLQueryBuilder()
                            .fields("LastName", "FirstName", "Age")
                            .table("Person")
                            .buildQuery()

  * Distinct

        // "SELECT DISTINCT LastName FROM Person"

        val query = SQLQueryBuilder()
                            .distinct(true)
                            .field("LastName")
                            .table("Person")
                            .buildQuery()

  * Order By

        // "SELECT * FROM Car ORDER BY Name"

        val query = SQLQueryBuilder()
                            .table("Car")
                            .orderBy("Name")
                            .buildQuery()

  * Group By

        // "SELECT Name, Color FROM Car GROUP BY Name"

        val query = SQLQueryBuilder()
                            .table("Car")
                            .field("Name")
                            .field("Color")
                            .groupBy("Name")
                            .buildQuery()

  * Filter

        // "SELECT * FROM Car WHERE Car.ID = ?
        // AND Car.NAME = 'Ford' AND Car.WHEELS > 4 AND Car.IS_COOL = 1"

        val query = SQLQueryBuilder()
                            .table("Car")
                            .filter("Car.ID", "?")
                            .filter("Car.NAME", "Ford".toSqlString())
                            .filter("Car.WHEELS", CompareType.GREATERTHAN, 4)
                            .filter("Car.IS_COOL", true)
                            .buildQuery()

  * Filter Or

        // "SELECT * FROM Car WHERE Car.IS_COOL = 1
        // AND (Car.ID = ? OR Car.NAME = 'Ford' OR Car.NAME = 'Chevy')
        // AND (Car.WHEELS > 4 OR Car.WHEELS <= 2)"

        val query = SQLQueryBuilder()
                            .table("Car")
                            .filter("Car.IS_COOL", true)
                            .filter(CompareFilter.create("Car.ID", "?").or("Car.NAME", "Ford".toSqlString()).or("Car.NAME", "Chevy".toSqlString()))
                            .filter(CompareFilter.create("Car.WHEELS", CompareType.GREATERTHAN, 4).or("Car.WHEELS", CompareType.LESSTHAN_EQUAL, 2))
                            .buildQuery()


  * Multiple Tables

        // "SELECT p.*, s.name AS stat_name, c.name AS cat_name
        // FROM Person p, Status s, Category c WHERE p.ID = 5"

        val query = SQLQueryBuilder()
            .distinct(true)
            .table("Person", "p")
            .table("Status", "s")
            .table("Category", "c")
    
            .field(tableName = "p", "*")
            .field("s.name", "stat_name")
            .field("c.name", "cat_name")
    
            .filter("p.ID", 5)
            .buildQuery()

  * Join

        // "SELECT Name FROM Car
        // JOIN Colors ON Color.ID = Car.COLOR_ID ORDER BY Color.Name"

        val query = SQLQueryBuilder()
                            .table("Car")
                            .join("Colors", "Color.ID", "Car.COLOR_ID")
                            .field("Name")
                            .orderBy("Color.Name")
                            .buildQuery()


  * Multiple Joins

        // "SELECT Name FROM Car
        // JOIN Color ON Color.ID = Car.COLOR_ID
        // LEFT JOIN Owner ON Owner.ID = Car.OWNER_ID
        // WHERE Car.ID = 5 ORDER BY Color.Name"

        val query = SQLQueryBuilder()
                            .table("Car")
                            .join("Color", "Color.ID", "Car.COLOR_ID")
                            .join(QueryJoinType.LEFT_JOIN, "Owner", "Owner.ID", "Car.OWNER_ID")
                            .field("Name")
                            .filter("Car.ID", 5)
                            .orderBy("Color.Name")
                            .buildQuery()

  * Joins with AND

        // "SELECT Name FROM Car JOIN Colors ON Color.ID = Car.COLOR_ID
        // AND Color.COOL = 1 JOIN Make ON Car.MAKE_ID = Make.ID ORDER BY Color.Name"

        val query = SQLQueryBuilder()
                            .table("Car")
                            .join("Colors", CompareFilter.create("Color.ID", "Car.COLOR_ID"), CompareFilter.create("Color.COOL", "1"))
                            .join("Make", "Car.MAKE_ID", "Make.ID")
                            .field("Name")
                            .orderBy("Color.Name")
                            .buildQuery()

  * Sub-Select

        // "SELECT * FROM Family WHERE HeadPerson IN (SELECT id FROM Person)"

        val subSql = SQLQueryBuilder()
        subSql.field("id")
        subSql.table("Person")

        query = SQLQueryBuilder()
        query.table("Family")
        query.filter("HeadPerson", CompareType.IN, subSql)

  * Union

        // "(SELECT id FROM Person UNION SELECT id FROM Family)"

        val sql1 = SQLQueryBuilder()
        sql1.field("id")
        sql1.table("Person")

        val sql2 = SQLQueryBuilder()
        sql2.field("id")
        sql2.table("Family")

        val query = SQLQueryBuilder.union(sql1, sql2) // or use unionAll(...)

  * Complex Union

        // "SELECT * FROM (SELECT id FROM Person UNION SELECT id FROM Family)"

        val sql1 = SQLQueryBuilder()
        sql1.field("id")
        sql1.table("Person")

        val sql2 = SQLQueryBuilder()
        sql2.field("id")
        sql2.table("Family")

        val query = SQLQueryBuilder()
        query.table(SQLQueryBuilder.union(sql1, sql2))

License
=======

    Copyright 2015-2024 Jeff Campbell

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
