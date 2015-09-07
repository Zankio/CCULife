package org.zankio.cculife.CCUService.base.helper;

import android.content.Context;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.zankio.cculife.CCUService.base.authentication.IAuth;
import org.zankio.cculife.override.Net;

import javax.net.ssl.SSLSocketFactory;
public class ConnectionHelper {

    IAuth<Connection> auth;
    private final static int CONNECT_TIMEOUT = Net.CONNECT_TIMEOUT;
    private static SSLSocketFactory sslSocketFoctory;

    public ConnectionHelper() { }

    public ConnectionHelper(IAuth<Connection> auth) {
        this.auth = auth;
    }

    public static void setSSLSocketFactory(SSLSocketFactory sslSocketFoctory) {
        ConnectionHelper.sslSocketFoctory = sslSocketFoctory;
    }

    public static SSLSocketFactory getSSLSocketFactory() {
        if (sslSocketFoctory == null) {
            // Todo
        }
        return sslSocketFoctory;
    }

    public Connection create(String url) {
        Connection connection = Jsoup.connect(url);
        return init(connection);
    }

    public Connection init(Connection connection) {
        ConnectionHelper.initAuth(connection, auth);
        ConnectionHelper.initTimeout(connection);
        ConnectionHelper.initSSLSocketFactory(connection, ConnectionHelper.getSSLSocketFactory());
        return connection;
    }

    public static Connection initTimeout(Connection connection) {
        connection.timeout(CONNECT_TIMEOUT);
        return connection;
    }

    public static Connection initAuth(Connection connection, IAuth<Connection> auth) {
        if(auth != null) auth.Auth(connection);
        return connection;
    }

    public static Connection initSSLSocketFactory(Connection connection, SSLSocketFactory sslSocketFoctory) {
        connection.request().setSSLSocketFactory(sslSocketFoctory);
        return connection;
    }
}
