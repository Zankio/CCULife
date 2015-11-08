package org.zankio.cculife.CCUService.ecourse.source;

import org.zankio.cculife.CCUService.base.source.BaseSource;
import org.zankio.cculife.CCUService.ecourse.Ecourse;
import org.zankio.cculife.CCUService.ecourse.model.Homework;
import org.zankio.cculife.CCUService.kiki.Kiki;
import org.zankio.cculife.SessionManager;

public abstract class EcourseSource extends BaseSource {

    public boolean Authenticate(SessionManager sessionManager) throws Exception { return true; }

    public void switchCourse(Ecourse.Course course) {}

    public abstract Ecourse.Course[] getCourse() throws Exception;

    public abstract Ecourse.Scores[] getScore(Ecourse.Course course) throws Exception;

    public abstract Ecourse.Classmate[] getClassmate(Ecourse.Course course) throws Exception;

    public abstract Ecourse.Announce[] getAnnounces(Ecourse.Course course) throws Exception;

    public abstract String getAnnounceContent(Ecourse.Announce announce) throws Exception;

    public abstract Ecourse.FileList[] getFiles(Ecourse.Course course) throws Exception;

    public abstract Homework[] getHomework(Ecourse.Course course) throws Exception;

    public Ecourse.Course[] getCourse(int year, int term, Kiki kiki) throws Exception {
        return getCourse();
    }

    public abstract void getHomeworkContent(Homework homework) throws Exception;
}
