package org.zankio.cculife.CCUService.kiki.source;

import org.zankio.cculife.CCUService.base.source.BaseSource;
import org.zankio.cculife.CCUService.kiki.ScoreQuery;
import org.zankio.cculife.SessionManager;

public abstract class ScoreQuerySource extends BaseSource {
    public abstract boolean Authenticate(SessionManager sessionManager) throws Exception;
    public abstract ScoreQuery.Grade[] getGrades() throws Exception;
}
