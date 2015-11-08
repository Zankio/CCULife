package org.zankio.cculife.CCUService.base.authentication;

import org.jsoup.Connection;

import java.util.HashMap;

public class CookieAuth implements IAuth<Connection> {

    HashMap<String, String> cookies;

    {
        cookies = new HashMap<String, String>();
    }

    public CookieAuth setCookie(String key, String value) {
        cookies.put(key, value);
        return this;
    }

    public void clear() {
        cookies.clear();
    }

    public CookieAuth setCookie(Connection connection, String key) {
        String value = connection.response().cookie(key);
        if(value != null)
            cookies.put(key, value);
        return this;
    }

    public String getCookie(String key) {
        return cookies.get(key);
    }

    public Connection Auth(Connection connection) {
        connection.cookies(cookies);
        return connection;
    }
}
