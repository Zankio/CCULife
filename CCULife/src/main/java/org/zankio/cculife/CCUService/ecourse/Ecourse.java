package org.zankio.cculife.CCUService.ecourse;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import org.zankio.cculife.CCUService.base.SourceSwitcher.AutoNetworkSourceSwitcher;
import org.zankio.cculife.CCUService.base.SourceSwitcher.ISwitcher;
import org.zankio.cculife.CCUService.base.SourceSwitcher.SingleSourceSwitcher;
import org.zankio.cculife.CCUService.ecourse.model.Homework;
import org.zankio.cculife.CCUService.ecourse.parser.EcourseParser;
import org.zankio.cculife.CCUService.ecourse.source.EcourseLocalSource;
import org.zankio.cculife.CCUService.ecourse.source.EcourseRemoteSource;
import org.zankio.cculife.CCUService.ecourse.source.EcourseSource;
import org.zankio.cculife.CCUService.kiki.Kiki;
import org.zankio.cculife.SessionManager;
import org.zankio.cculife.override.Exceptions;
import org.zankio.cculife.override.NetworkErrorException;

import java.io.IOException;

public class Ecourse {
    private ISwitcher sourceSwitcher;

    public Course nowCourse = null;
    public int OFFLINE_MODE = 0;

    public Ecourse(Context context) throws Exception {
        EcourseRemoteSource ecourseRemoteSource;
        EcourseLocalSource ecourseLocalSource;
        SharedPreferences preferences;
        SessionManager sessionManager = SessionManager.getInstance(context);

        preferences = PreferenceManager.getDefaultSharedPreferences(context);
        OFFLINE_MODE = sessionManager.isSave() &&
                preferences.getBoolean("offline_enable", true)
                ? Integer.valueOf(preferences.getString("offline_mode", "1")) : -1;

        ecourseRemoteSource = new EcourseRemoteSource(this, new EcourseParser());

        try {
            ecourseRemoteSource.Authenticate(sessionManager);
            // if(OFFLINE_MODE == 0) syncAll();
        } catch (NetworkErrorException e) {
            ecourseRemoteSource.setSessionManager(sessionManager);
        }

        if (OFFLINE_MODE < 0) {
            sourceSwitcher = new SingleSourceSwitcher(ecourseRemoteSource);
        } else {
            ecourseLocalSource = new EcourseLocalSource(this, context);
            ecourseRemoteSource.setLocalStorage(ecourseLocalSource);
            sourceSwitcher = new AutoNetworkSourceSwitcher(context, ecourseLocalSource, ecourseRemoteSource);
        }

    }

    public void openSource() {
        sourceSwitcher.openSource();
    }

    public void closeSource() {
        sourceSwitcher.closeSource();
    }

    public void switchCourse(Course course) {
        EcourseSource source = getSource();

        if(source == null) return;

        source.switchCourse(course);
        nowCourse = course;
    }

    public void syncAll() {
        if (OFFLINE_MODE < 0) return;

        Course[] courses;
        Announce[] announces;
        try {
            courses = getCourses();
            for(Course course: courses) {
                announces = course.getAnnounces();
                for(Announce announce : announces) {
                    announce.getContent();
                }
                //course.getFiles();
                course.getScore();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public EcourseSource getSource() {
        return (EcourseSource) this.sourceSwitcher.getSource();
    }

    public Course[] getCourses() throws Exception {
        return getSource().getCourse();
    }

    //Todo impl debug mode
    public Course[] getCourses(int year, int term, Kiki kiki) throws Exception {
        return getSource().getCourse(year, term, kiki);
    }


    public class Course {
        public String courseid;
        public String id;
        public String name;
        public String teacher;
        public int notice;
        public int homework;
        public int exam;
        public boolean warning;
        private FileList[] files;
        private Announce[] announces;
        private Scores[] scores;
        private Ecourse ecourse;
        private Homework[] homeworks;

        public Course(Ecourse content) {
            this.setEcourse(content);
        }

        public Course(String courseid, String id, String name, String teacher, int notice, int homework, int exam, String warning){
            this.courseid = courseid;
            this.id = id;
            this.name = name;
            this.teacher = teacher;
            this.notice = notice;
            this.exam = exam;
            this.warning = !warning.equals("--");
            this.homework = homework;
        }



        public Scores[] getScore() throws Exception {
            if (scores != null) return scores;

            Ecourse eco = getEcourse();
            EcourseSource ecourseSource;
            ecourseSource = eco.getSource();

            eco.switchCourse(this);
            try {
                this.scores = ecourseSource.getScore(this);
            } catch (IOException e) {
                throw Exceptions.getNetworkException(e);
            }

            return this.scores;
        }

        public Classmate[] getClassmate() throws Exception {

            Ecourse eco = getEcourse();
            EcourseSource ecourseSource;
            ecourseSource = eco.getSource();

            eco.switchCourse(this);

            try {
                return ecourseSource.getClassmate(this);
            } catch (IOException e) {
                throw Exceptions.getNetworkException(e);
            }
        }

        public Announce[] getAnnounces() throws Exception {
            if (this.announces != null) return this.announces;

            Ecourse eco = getEcourse();
            EcourseSource ecourseSource;
            ecourseSource = eco.getSource();

            eco.switchCourse(this);

            try {
                this.announces = ecourseSource.getAnnounces(this);
                if(OFFLINE_MODE == 1) {

                    final Announce[] sync = this.announces;
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            syncAnnounceContent(sync);
                        }
                    }).start();

                }
                return this.announces;
            } catch (IOException e) {
                throw Exceptions.getNetworkException(e);
            }
        }

        private void syncAnnounceContent(Announce[] announces) {
            if(!(sourceSwitcher instanceof AutoNetworkSourceSwitcher)) return;

            EcourseLocalSource ecourseLocalSource;
            ecourseLocalSource = (EcourseLocalSource) ((AutoNetworkSourceSwitcher)sourceSwitcher).getLocalSource();

            if(ecourseLocalSource == null || ecourseLocalSource.equals(getSource())) return;

            if(announces != null) {
                for (Announce announce : announces) {
                    if(!ecourseLocalSource.hasAnnounceContent(announce))
                        announce.getContent();
                }
            }
        }

        public FileList[] getFiles() throws Exception {
            if (this.files != null) return this.files;

            Ecourse eco = getEcourse();
            EcourseSource ecourseSource;
            ecourseSource = eco.getSource();

            eco.switchCourse(this);

            this.files = ecourseSource.getFiles(this);

            return this.files;
        }

        public Homework[] getHomework() throws Exception {
            if (this.homeworks != null) return this.homeworks;
            Ecourse eco = getEcourse();
            EcourseSource ecourseSource;
            ecourseSource = eco.getSource();

            eco.switchCourse(this);

            this.homeworks = ecourseSource.getHomework(this);

            return this.homeworks;
        }

        public Ecourse getEcourse() {
            return ecourse;
        }

        public void setEcourse(Ecourse ecourse) {
            this.ecourse = ecourse;
        }

    }

    public class File{
        public String Name;
        public String URL;
        public String Size;
    }

    public class FileList {
        public String Name;
        public File[] Files;
    }

    public class Announce {
        public String url;
        public String Date;
        public String Title;
        public String Content = null;
        public String important;
        public int browseCount;
        public boolean isnew;
        protected Ecourse ecourse;
        protected Course course;

        public Announce(Ecourse ecourse, Course course) {this.ecourse = ecourse; this.course = course;}

        public String getCourseID() {
            return this.course.courseid;
        }

        public String getContent() {
            if (this.Content != null) return this.Content;

            EcourseSource ecourseSource;
            ecourseSource = ecourse.getSource();
            ecourseSource.switchCourse(course);

            try {
                this.Content = ecourseSource.getAnnounceContent(this);

            } catch (Exception e) {
                e.printStackTrace();
                return e.getMessage();
            }

            return this.Content;
        }
    }

    public class Classmate {
        public String Name;
        public String StudentId;
        public String Department;
        public String Gender;
    }


    public class Scores {
        public String courseid;
        public Score[] scores;
        public String Name;
        public String Score;
        public String Rank;
    }

    public class Score {
        public String courseid;
        public String Name;
        public String Score;
        public String Rank;
        public String Percent;
    }
}
