package org.dbtools.query.shared.ext

fun String.toSqlString(): String {
    val formattedSql = this.replace("'", "''")
    return "'$formattedSql'"
}