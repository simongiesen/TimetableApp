package com.satsumasoftware.timetable.db;

import android.provider.BaseColumns;

public final class ClassesSchema implements BaseColumns {

    public static final String TABLE_NAME = "classes";
    public static final String COL_SUBJECT_ID = "subject_id";

    protected static final String SQL_CREATE = "CREATE TABLE " + TABLE_NAME + "( " +
            _ID + SchemaUtilsKt.INTEGER_TYPE + SchemaUtilsKt.COMMA_SEP +
            COL_SUBJECT_ID + SchemaUtilsKt.INTEGER_TYPE +
            " )";

    protected static final String SQL_DELETE =
            "DROP TABLE IF EXISTS " + TABLE_NAME;
}
