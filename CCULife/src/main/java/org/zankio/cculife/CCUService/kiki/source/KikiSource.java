package org.zankio.cculife.CCUService.kiki.source;

import org.zankio.cculife.CCUService.base.source.BaseSource;
import org.zankio.cculife.CCUService.kiki.Kiki;
import org.zankio.cculife.SessionManager;

public abstract class KikiSource extends BaseSource {
    public boolean Authenticate(SessionManager sessionManager) throws Exception { return true; }
    public abstract Kiki.TimeTable getTimeTable() throws Exception;
    public abstract Kiki.Course[] getCourseList(int year, int term) throws Exception;

}
