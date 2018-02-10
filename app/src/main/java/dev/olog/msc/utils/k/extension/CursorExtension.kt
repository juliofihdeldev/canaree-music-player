package dev.olog.msc.utils.k.extension

import android.database.Cursor

fun Cursor.getInt(column: String): Int {
    return getInt(getColumnIndex(column))
}

fun Cursor.getLong(column: String): Long {
    return getLong(getColumnIndex(column))
}


fun Cursor.getString(column: String): String {
    return getString(getColumnIndex(column))
}