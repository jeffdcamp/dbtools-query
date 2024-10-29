package org.dbtools.query.shared

data class Field(val name: String, val alias: String? = null, val tableName: String? = null) {
    override fun toString(): String {
        val formattedName = when {
            tableName.isNullOrBlank() -> name
            else -> "$tableName.$name"
        }

        return when {
            alias.isNullOrBlank() -> formattedName
            else -> "$formattedName AS $alias"
        }
    }
}