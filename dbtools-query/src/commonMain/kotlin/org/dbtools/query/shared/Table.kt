package org.dbtools.query.shared

data class Table(val name: String, val alias: String? = null) {
    override fun toString(): String {
        return when {
            alias.isNullOrBlank() -> name
            else -> "$name $alias"
        }
    }
}