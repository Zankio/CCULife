package org.zankio.cculife.CCUService.kiki.source;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.zankio.cculife.CCUService.kiki.Kiki;
import org.zankio.cculife.CCUService.kiki.database.KikiDatabaseHelper;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import static org.zankio.cculife.ui.CourseSchedule.TimeTableWeekPage.randomColor;

public class KikiLocalSource extends KikiSource {

    private KikiDatabaseHelper databaseHelper;
    private SQLiteDatabase database;
    private Kiki kiki;

    private String timetableColumn[] = {
            KikiDatabaseHelper.TIME_COLUMN_NAME,
            KikiDatabaseHelper.TIME_COLUMN_TEACHER,
            KikiDatabaseHelper.TIME_COLUMN_CLASSROOM,
            KikiDatabaseHelper.TIME_COLUMN_STARTTIME,
            KikiDatabaseHelper.TIME_COLUMN_ENDTIME,
            KikiDatabaseHelper.TIME_COLUMN_DAYOFWEEK,
            KikiDatabaseHelper.TIME_COLUMN_COLORID
    };

    public KikiLocalSource(Kiki kiki, Context context) {
        databaseHelper = new KikiDatabaseHelper(context);
        this.database = databaseHelper.getWritableDatabase();
        this.kiki = kiki;
    }

    public void clearData() {

    }

    @Override
    public void closeSource() {
        database.close();
    }

    @Override
    public void openSource() {
        this.database = databaseHelper.getWritableDatabase();
    }

    public void storeTimeTable(Kiki.TimeTable timeTable) {
        if(database == null || !database.isOpen() || database.isReadOnly()) return;
        database.delete(KikiDatabaseHelper.TABLE_TIMETABLE, null, null);

        ContentValues values = new ContentValues();

        Kiki.TimeTable.Day[] days = timeTable.days;
        for (int i = 0; i < days.length; i++) {
            Kiki.TimeTable.Day day = days[i];
            ArrayList<Kiki.TimeTable.Class> classList = day.classList;
            for (Kiki.TimeTable.Class mClass : classList) {
                values.clear();
                values.put(KikiDatabaseHelper.TIME_COLUMN_NAME, mClass.name);
                values.put(KikiDatabaseHelper.TIME_COLUMN_CLASSROOM, mClass.classroom);
                values.put(KikiDatabaseHelper.TIME_COLUMN_TEACHER, mClass.teacher);
                values.put(KikiDatabaseHelper.TIME_COLUMN_STARTTIME, formatClassTime(mClass.start));
                values.put(KikiDatabaseHelper.TIME_COLUMN_ENDTIME, formatClassTime(mClass.end));
                values.put(KikiDatabaseHelper.TIME_COLUMN_DAYOFWEEK, i);
                values.put(KikiDatabaseHelper.TIME_COLUMN_COLORID, mClass.colorid);

                database.insert(KikiDatabaseHelper.TABLE_TIMETABLE, null, values);
            }
        }
    }

    @Override
    public Kiki.TimeTable getTimeTable() throws Exception {

        Cursor cursor;
        Kiki.TimeTable result = kiki.new TimeTable();
        Kiki.TimeTable.Class mClass;
        int dayOfWeek, size;
        int[] colors;

        cursor = database.query(
                KikiDatabaseHelper.TABLE_TIMETABLE,
                timetableColumn,
                null, null, null, null, null);


        cursor.moveToFirst();
        size = cursor.getCount();
        colors = new int[size];
        for (int i = 0; i < colors.length; i++) colors[i] = -1;
        while (!cursor.isAfterLast()) {
            mClass = cursorToClass(cursor, result);
            if (colors.length <= mClass.colorid || colors[mClass.colorid] < 0)
                colors[mClass.colorid] = randomColor();
            mClass.color = colors[mClass.colorid];

            dayOfWeek = cursor.getInt(5);

            result.days[dayOfWeek].classList.add(mClass);

            cursor.moveToNext();
        }
        cursor.close();
        result.sort();

        return size > 0 ? result : null;
    }

    private Kiki.TimeTable.Class cursorToClass(Cursor cursor, Kiki.TimeTable timeTable) {
        Kiki.TimeTable.Class mClass = timeTable.new Class();
        mClass.name = cursor.getString(0);
        mClass.teacher = cursor.getString(1);
        mClass.classroom = cursor.getString(2);
        mClass.start = parseClassTime(cursor.getString(3));
        mClass.end = parseClassTime(cursor.getString(4));
        mClass.colorid = cursor.getInt(6);

        return mClass;
    }

    @Override
    public Kiki.Course[] getCourseList(int year, int term) throws Exception {
        throw new Exception("未支援離線資料");
    }

    public String formatClassTime(Calendar calendar) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm");
        return simpleDateFormat.format(calendar.getTime());
    }

    public Calendar parseClassTime(String time) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm");
        int hour, minute;
        Date date;
        Calendar calendar = new GregorianCalendar(0, 0, 0);
        try {
            calendar.setTime(simpleDateFormat.parse(time));
            hour = calendar.get(Calendar.HOUR_OF_DAY);
            minute = calendar.get(Calendar.MINUTE);

            calendar.clear();
            calendar.set(0, 0, 0);
            calendar.set(Calendar.HOUR, hour);
            calendar.set(Calendar.MINUTE, minute);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return calendar;
    }
}
