package org.dbtools.query.shared.ext

import assertk.assertThat
import assertk.assertions.isEqualTo
import kotlin.test.Test

class StringExtTest {
    @Test
    fun testBasicSqlString() {
        assertThat("Jeff".toSqlString()).isEqualTo("'Jeff'")
    }

    @Test
    fun testQuoteSqlString() {
        assertThat("Jeff's".toSqlString()).isEqualTo("'Jeff''s'")
    }
}