package dev.datlag.burningseries.database

import app.cash.sqldelight.ColumnAdapter

object IntAdapter : ColumnAdapter<Int, Long> {
    override fun decode(databaseValue: Long): Int {
        return databaseValue.toInt()
    }

    override fun encode(value: Int): Long {
        return value.toLong()
    }
}