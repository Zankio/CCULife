package org.zankio.cculife.CCUService.base.authentication;

import android.annotation.TargetApi;
import android.net.Uri;
import android.os.Build;

import org.jsoup.Connection;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Set;

public class QueryStringAuth implements IAuth<Connection>{

    HashMap<String, String> query;

    public QueryStringAuth() {
        query = new HashMap<String, String>();
    }

    public QueryStringAuth addQueryParameter (String key, String value){
        query.put(key, value);
        return this;
    }

    public String getQueryParameter(String key) {
        return query.get(key);
    }

    @Override
    @TargetApi(11)
    public Connection Auth(Connection connection) {
        HashMap<String, String> queryString;
        Uri uri;
        Uri.Builder builder;

        uri = Uri.parse(
                connection.request().url().toString()
        );

        queryString = new HashMap<String, String>();
        for(String key : getQueryParameterNames(uri)) {
            queryString.put(key, uri.getQueryParameter(key));
        }

        for(String key : query.keySet()) {
            queryString.put(key, query.get(key));
        }

        builder = uri.buildUpon();

        builder.query(null);

        for(String key : queryString.keySet()) {
            builder.appendQueryParameter(key, queryString.get(key));
        }

        try {
            connection.request().url(new URL(builder.toString()));
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        return connection;
    }

    @TargetApi(11)
    private Set<String> getQueryParameterNames(Uri uri) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            return uri.getQueryParameterNames();
        }

        if (uri.isOpaque()) {
            throw new UnsupportedOperationException("This isn't a hierarchical URI.");
        }

        String query = uri.getEncodedQuery();
        if (query == null) {
            return Collections.emptySet();
        }

        Set<String> names = new LinkedHashSet<String>();
        int start = 0;
        do {
            int next = query.indexOf('&', start);
            int end = (next == -1) ? query.length() : next;

            int separator = query.indexOf('=', start);
            if (separator > end || separator == -1) {
                separator = end;
            }

            String name = query.substring(start, separator);
            names.add(Uri.decode(name));

            // Move start to end of name.
            start = end + 1;
        } while (start < query.length());

        return Collections.unmodifiableSet(names);
    }

}
