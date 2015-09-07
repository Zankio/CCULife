package org.zankio.cculife.CCUService.kiki;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.zankio.cculife.CCUService.kiki.parser.KikiParser;
import org.zankio.cculife.CCUService.kiki.source.KikiLocalSource;
import org.zankio.cculife.CCUService.kiki.source.KikiRemoteSource;
import org.zankio.cculife.CCUService.kiki.source.KikiSource;
import org.zankio.cculife.CCUService.base.SourceSwitcher.AutoNetworkSourceSwitcher;
import org.zankio.cculife.CCUService.base.SourceSwitcher.ISwitcher;
import org.zankio.cculife.CCUService.base.SourceSwitcher.SingleSourceSwitcher;
import org.zankio.cculife.CCUService.ecourse.Ecourse;
import org.zankio.cculife.SessionManager;
import org.zankio.cculife.override.Net;
import org.zankio.cculife.override.NetworkErrorException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;

public class Kiki {

    public ISwitcher sourceSwitcher;
    public int OFFLINE_MODE = -1;

    public Kiki(Context context) throws Exception {
        KikiLocalSource kikiLocalSource;
        KikiRemoteSource kikiRemoteSource;
        SessionManager sessionManager;
        SharedPreferences preferences;

        sessionManager = SessionManager.getInstance(context);
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
        OFFLINE_MODE = sessionManager.isSave() &&
                preferences.getBoolean("offline_enable", true)
                ? 1 : 0;

        kikiRemoteSource = new KikiRemoteSource(this, new KikiParser());

        try {
            kikiRemoteSource.Authenticate(sessionManager);
        } catch (NetworkErrorException e) { }

        if (OFFLINE_MODE > 0) {
            kikiLocalSource = new KikiLocalSource(this, context);
            kikiRemoteSource.setLocalSource(kikiLocalSource);
            sourceSwitcher = new AutoNetworkSourceSwitcher(context, kikiLocalSource, kikiRemoteSource);
        } else {
            sourceSwitcher = new SingleSourceSwitcher(kikiRemoteSource);
        }

    }

    public KikiSource getSource() {
        return (KikiSource) sourceSwitcher.getSource();
    }

    public TimeTable getTimeTable() throws Exception {
        return getSource().getTimeTable();
    }

    //ToDo Day-Class to Class-Day

    public class TimeTable {

        public Day[] days;

        public TimeTable() {
            days = new Day[7];
            for (int i = 0; i < 7; i++) {
                days[i] = new Day();
            }
        }

        public class Day {
            public ArrayList<Class> classList;

            public Day() {
                classList = new ArrayList<Class>();
            }
        }

        public class Class {
            public String name;
            public String classroom;
            public String teacher;
            public Calendar start;
            public Calendar end;
            public int color;
            public int colorid;
        }

        public void sort() {
            Comparator<Class> comparator = new Comparator<Class>() {
                public int compare(Class a, Class b) {
                    return a.start.compareTo(b.start);
                }
            };

            for (int i = 0; i < 7; i++) {
                Collections.sort(days[i].classList, comparator);
            }
        }
    }

    public Ecourse getEcourseCourse(int year, int term, Ecourse ecourse) throws Exception {
        Course[] courses = getCourseList(year, term);

        return null;
    }

    public static class Course {
        public String CourseID;
        public String ClassID;
        public String Name;
        public String Teacher;
        public int Credit;
        public String CreditType;
        public Time[] Time;
        public String ClassRoom;
        public String OutlineLink;
        public int term;
        public int year;

        public String getEcourseID() {
            if ("".equals(OutlineLink)) return null;
            if (year > 0 && term > 0)
                OutlineLink = OutlineLink
                        .replaceAll("([\\?&])year=\\d+", "$1year=" + year)
                        .replaceAll("([\\?&])term=\\d+", "$1term=" + term);

            Connection connection;
            String location, result;

            connection = Jsoup.connect(OutlineLink).timeout(Net.CONNECT_TIMEOUT);
            connection.followRedirects(false);
            org.zankio.cculife.CCUService.base.helper.ConnectionHelper.initSSLSocketFactory(connection, org.zankio.cculife.CCUService.base.helper.ConnectionHelper.getSSLSocketFactory());


            try {
                location = connection.execute().header("Location");
                if (location != null) {
                    result = Uri.parse(location).getQueryParameter("courseid");
                    if (result != null)
                        return String.format("%d_%d_%s", year, term, result);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
        public static class Time {
            public static Time prase(String data) {
                return null;
            }
        }
    }
    public Course[] getCourseList() throws Exception {
        return getCourseList(-1, -1);
    }
    public Course[] getCourseList(int year, int term) throws Exception {
        return getSource().getCourseList(year, term);
    }
}
