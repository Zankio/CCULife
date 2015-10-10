package org.zankio.cculife.CCUService.kiki.source;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.zankio.cculife.CCUService.base.helper.ConnectionHelper;
import org.zankio.cculife.CCUService.kiki.parser.ScoreQueryParser;
import org.zankio.cculife.CCUService.kiki.ScoreQuery;
import org.zankio.cculife.SessionManager;
import org.zankio.cculife.override.Exceptions;
import org.zankio.cculife.override.LoginErrorException;

import java.io.IOException;
import javax.net.ssl.HttpsURLConnection;

public class ScoreQueryRemoteSource extends ScoreQuerySource{

    private ScoreQueryParser parser;
    private Document data;

    public ScoreQueryRemoteSource(ScoreQueryParser parser) {
        this.parser = parser;
        this.data = null;
        HttpsURLConnection.setDefaultSSLSocketFactory(ConnectionHelper.getSSLSocketFactory());
    }

    @Override
    public boolean Authenticate(SessionManager sessionManager) throws Exception {
        if (!sessionManager.isLogined())
            throw Exceptions.getNeedLoginException();

        Connection connection;
        Document document;
        String error;

        connection = Jsoup.connect("https://kiki.ccu.edu.tw/~ccmisp06/cgi-bin/Query/Query_grade.php");
        ConnectionHelper.initTimeout(connection);

        connection.data("id", sessionManager.getUserName())
                .data("password", sessionManager.getPassword());

        try {
            document = connection.post();

            if (document.select("table").size() != 0) {
                data = document;
                return true;

            } else {
                error = parser.parserError(document);

                if (error != null) {
                    if (error.contains("學號應為九碼半形數字") ||
                            error.contains("請輸入正確學號") ||
                            error.contains("密碼長度過長或過短") ||
                            error.contains("您輸入的密碼有誤"))
                    {
                        throw new LoginErrorException("帳號或密碼錯誤");
                    } else {
                        throw new LoginErrorException("未知錯誤 : " + error);
                    }
                }
                return false;
            }
        } catch (IOException e) {
            throw Exceptions.getNetworkException(e);
        }
    }

    @Override
    public ScoreQuery.Grade[] getGrades() throws Exception {
        if (data == null)
            return null;

        return parser.parserGrade(data);
    }
}
