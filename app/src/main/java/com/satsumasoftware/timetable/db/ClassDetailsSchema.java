package com.satsumasoftware.timetable.db;

import android.provider.BaseColumns;

public final class ClassDetailsSchema implements BaseColumns {

    public static final String TABLE_NAME = "class_details";
    public static final String COL_ROOM = "room";
    public static final String COL_TEACHER = "teacher";

    protected static final String SQL_CREATE = "CREATE TABLE " + TABLE_NAME + "( " +
            _ID + SchemaUtilsKt.INTEGER_TYPE + SchemaUtilsKt.PRIMARY_KEY_AUTOINCREMENT + SchemaUtilsKt.COMMA_SEP +
            COL_ROOM + SchemaUtilsKt.INTEGER_TYPE + SchemaUtilsKt.COMMA_SEP +
            COL_TEACHER + SchemaUtilsKt.INTEGER_TYPE +
            " )";

    protected static final String SQL_DELETE =
            "DROP TABLE IF EXISTS " + TABLE_NAME;

}