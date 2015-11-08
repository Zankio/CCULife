package org.zankio.cculife.CCUService.kiki;


import android.content.Context;

import org.zankio.cculife.CCUService.base.SourceSwitcher.ISwitcher;
import org.zankio.cculife.CCUService.base.SourceSwitcher.SingleSourceSwitcher;
import org.zankio.cculife.CCUService.kiki.parser.ScoreQueryParser;
import org.zankio.cculife.CCUService.kiki.source.ScoreQueryRemoteSource;
import org.zankio.cculife.CCUService.kiki.source.ScoreQuerySource;
import org.zankio.cculife.SessionManager;


// Data http://140.123.30.107/~ccmisp06/cgi-bin/Query/
public class ScoreQuery {

    private ISwitcher sourceSwitcher;

    public ScoreQuery(Context context) throws Exception {
        ScoreQueryRemoteSource scoreQueryRemoteSource;
        scoreQueryRemoteSource = new ScoreQueryRemoteSource(new ScoreQueryParser());
        scoreQueryRemoteSource.Authenticate(SessionManager.getInstance(context));
        sourceSwitcher = new SingleSourceSwitcher(scoreQueryRemoteSource);
    }

    private ScoreQuerySource getSource() {
        return (ScoreQuerySource) sourceSwitcher.getSource();
    }

    public Grade[] getGrades() throws Exception {
        return getSource().getGrades();
    }

    public static class Grade {

        public Score[] Scores;
        public String Grade;
        public String Description;
    }

    public static class Score {
        public String CoruseID;
        public String ClassID;
        public String CourseName;
        public String CreditType;
        public String Credit;
        public String Score;
    }
}
