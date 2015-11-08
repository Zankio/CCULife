package org.zankio.cculife.ui.CourseSchedule;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.zankio.cculife.CCUService.kiki.Kiki;
import org.zankio.cculife.R;
import org.zankio.cculife.ui.Base.BasePage;
import org.zankio.cculife.ui.Base.onDataLoadListener;
import org.zankio.cculife.view.LessionView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Random;

public class TimeTableWeekPage extends BasePage implements onDataLoadListener<Kiki.TimeTable> {

    private Kiki.TimeTable timeTable;
    private LinearLayout[] week;
    private float minuteToPixel = 1.5f;
    private Calendar firstClass;
    private static int[] colors = {0x3333B5E5, 0x33AA66CC, 0x3399CC00, 0x33FFBB33, 0x33FF4444};

    public static int randomColor(){
        return colors[new Random().nextInt(colors.length)];
    }

    public TimeTableWeekPage(LayoutInflater inflater, Kiki.TimeTable timeTable) {
        super(inflater);
        this.timeTable = timeTable;
        firstClass = new GregorianCalendar(0, 0, 0);
        firstClass.set(Calendar.HOUR, 7);
        firstClass.set(Calendar.MINUTE, 0);

    }

    @Override
    protected View createView() {
        return inflater.inflate(R.layout.fragment_course_timetable_week, null);
    }

    @Override
    public void initViews() {
        week = new LinearLayout[]{
                null,
                (LinearLayout) PageView.findViewById(R.id.weekMonday),
                (LinearLayout) PageView.findViewById(R.id.weekTueday),
                (LinearLayout) PageView.findViewById(R.id.weekWednesday),
                (LinearLayout) PageView.findViewById(R.id.weekThursday),
                (LinearLayout) PageView.findViewById(R.id.weekFriday),
                null};
        LinearLayout index = (LinearLayout) PageView.findViewById(R.id.weekIndex);
        TextView indexNode;
        for (int i = 1; i <= 14; i++) {
            indexNode = new TextView(inflater.getContext());
            indexNode.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) (60 * minuteToPixel)));
            indexNode.setText("" + i);
            indexNode.setGravity(Gravity.CENTER_HORIZONTAL);
            if(i % 2 == 0) indexNode.setBackgroundColor(0x11000000);
            index.addView(indexNode);
        }
        updateTimeTable();
    }

    public void updateTimeTable() {
        if(timeTable == null) return;

        for (int i = 0; i < week.length; i++) {
            if(week[i] != null)
                week[i].removeAllViews();
        }

        for (int i = 0; i < timeTable.days.length; i++) {
            if(week[i] == null) continue;

            Kiki.TimeTable.Day day = timeTable.days[i];
            for (int j = 0; j < day.classList.size(); j++) {
                Kiki.TimeTable.Class mClass;
                LinearLayout.LayoutParams layoutParams;
                LessionView lessionView;

                mClass = day.classList.get(j);

                lessionView = new LessionView(inflater.getContext());
                lessionView.setClassName(mClass.name);
                lessionView.setClassRoom(mClass.classroom);
                lessionView.setClassTeacher(mClass.teacher);
                lessionView.setStartTime(mClass.start);
                lessionView.setEndTime(mClass.end);
                lessionView.setBackgroundColor(mClass.color);

                layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) (getTimeDiffInMinute(mClass.start, mClass.end) * minuteToPixel));

                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm");

                if(j == 0)
                    layoutParams.setMargins(0, (int) (getTimeDiffInMinute(firstClass, mClass.start) * minuteToPixel), 0, 0);
                else
                    layoutParams.setMargins(0, (int) (getTimeDiffInMinute(day.classList.get(j - 1).end, mClass.start) * minuteToPixel), 0, 0);

                lessionView.setLayoutParams(layoutParams);
                new GregorianCalendar().clear();
                week[i].addView(lessionView);
            }
        }


    }

    private int getTimeDiffInMinute(Calendar a, Calendar b) {
        return (int)((b.getTimeInMillis() - a.getTimeInMillis()) / 1000 / 60);
    }

    @Override
    public void onDataLoaded(Kiki.TimeTable timeTable) {
        this.timeTable = timeTable;
        updateTimeTable();
    }
}
