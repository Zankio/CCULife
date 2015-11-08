package org.zankio.cculife.CCUService.kiki.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class KikiDatabaseHelper extends SQLiteOpenHelper{

    public static final String TABLE_TIMETABLE = "timetable";
    public static final String TIME_COLUMN_ID = "_id";
    public static final String TIME_COLUMN_NAME = "name";
    public static final String TIME_COLUMN_TEACHER = "teacher";
    public static final String TIME_COLUMN_COLORID = "colorid";
    public static final String TIME_COLUMN_CLASSROOM = "classroom";
    public static final String TIME_COLUMN_STARTTIME = "start_time";
    public static final String TIME_COLUMN_ENDTIME = "end_time";
    public static final String TIME_COLUMN_DAYOFWEEK = "day_of_week";

    public static final String DATABASE_NAME = "kiki.db";
    public static final int DATABASE_VERSION = 1;

    public KikiDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String DATABASE_CREATE = "create table " + TABLE_TIMETABLE + "(" +
                TIME_COLUMN_ID + " integer primary key autoincrement, " +
                TIME_COLUMN_NAME + " text not null, " +
                TIME_COLUMN_CLASSROOM + " text not null, " +
                TIME_COLUMN_TEACHER + " text not null, " +
                TIME_COLUMN_STARTTIME + " int not null, " +
                TIME_COLUMN_ENDTIME + " int not null," +
                TIME_COLUMN_DAYOFWEEK + " int not null," +
                TIME_COLUMN_COLORID + " integer not null);";

        db.execSQL(DATABASE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TIMETABLE);
        onCreate(db);
    }
}
