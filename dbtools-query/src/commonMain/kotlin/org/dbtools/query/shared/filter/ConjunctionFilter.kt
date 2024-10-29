package org.dbtools.query.shared.filter

abstract class ConjunctionFilter(val filters: MutableList<Filter> = mutableListOf()) : Filter()
