package org.dbtools.query.shared

abstract class QueryBuilder {
    abstract fun formatLikeClause(field: String, value: String): String

    abstract fun formatIgnoreCaseLikeClause(field: String, value: String): String

    abstract fun formatValue(value: Any?): Any?

    abstract val queryParameter: String

    abstract fun buildQuery(): String

    companion object {
        fun toSelectionArgs(vararg args: Any): Array<String> {
            val selectionArgs: MutableList<String> = ArrayList(args.size)
            for (o in args) {
                if (o is List<*>) {
                    for (p in o) {
                        selectionArgs.add(p.toString())
                    }
                } else {
                    selectionArgs.add(o.toString())
                }
            }
            return selectionArgs.toTypedArray<String>()
        }
    }
}
