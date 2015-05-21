package org.zankio.cculife.WifiAutoLogin;

import java.util.Random;

import java.io.IOException;

import java.net.URL;

import android.annotation.TargetApi;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;

import android.content.Intent;
import android.content.Context;
import android.content.res.Resources;

import android.graphics.BitmapFactory;

import android.os.AsyncTask;

import org.jsoup.Jsoup;
import org.jsoup.Connection;
import org.jsoup.Connection.Response;
import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;

import org.zankio.cculife.Debug;
import org.zankio.cculife.R;

public class LoginWifi {
  final public static String LOGIN_PAGE = "http://140.123.1.53";
  final public static String WIFI_LOGIN_URL = "https://wlc.ccu.edu.tw/login.html";
  final private static String[] GOOGLE_IP = {
    "202.39.143.113",
    "202.39.143.98",
    "202.39.143.123",
    "202.39.143.118",
    "202.39.143.99",
    "202.39.143.93",
    "202.39.143.84",
    "202.39.143.89",
    "202.39.143.114",
    "202.39.143.108",
    "202.39.143.103",
    "202.39.143.109",
    "202.39.143.119",
    "202.39.143.88",
    "202.39.143.94",
    "202.39.143.104"
  };
  private String username, password;

  /**
   * @param username The account for login
   * @param password The password for login
   */
  public LoginWifi(String username, String password) {
    this.username = username;
    this.password = password;
  }

  /**
   * @return Is login success or not need to login
   */
  public boolean login(Context context) {
    Debug debug = new Debug();
    debug.info("Login wireless");
    if(username == null || password == null) {
      return false;
    }
    new LoginTask().execute(context);
    return true;
  }

  public class LoginTask extends AsyncTask<Context, Void, Boolean> {
    private Context context;
    @Override
    protected Boolean doInBackground(Context... context) {
      Debug debug = new Debug();
      this.context = context[0];
      try {
        int idx = new Random().nextInt(GOOGLE_IP.length);
        debug.info("Try connect generate_204");
        Response response = Jsoup.connect("http://" + GOOGLE_IP[idx] + "/generate_204")
          .followRedirects(false) //  Don't follow, sometime will response 200
          .execute();
        if(response.statusCode() != 204) { // Maybe we need to login
          String url = response.header("location"); // Get redirect url
          if(url != null && url.startsWith("http://140.123.1.53")) { // It is ccu wireless login
            debug.info("CCU login page");
            Response login_page = Jsoup.connect(WIFI_LOGIN_URL)
              .data("buttonClicked", "4")
              .data("redirect_url", "http://" + GOOGLE_IP[idx] + "/generate_204")
              .data("err_flag", "0")
              .data("username", username)
              .data("password", password)
              .cookies(response.cookies())
              .method(Connection.Method.POST)
              .followRedirects(true)
              .execute();
            debug.info("Login done");
            Document page = Jsoup.parse(login_page.body());
            String body = page.body().text();
            return body.contains("You can now use all our regular network services"); // If success, body will contain
          }
          else {
            return false;
          }
        }
      } catch(IOException e) {
        e.printStackTrace();
      }
      return true;
    }

    @Override
    @TargetApi(11)
    protected void onPostExecute(Boolean login_result) {
      Debug debug = new Debug();
      NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
      PendingIntent contentIntent = PendingIntent.getActivity(context, 0, new Intent(), 0);
      Resources resources = context.getResources();
      if(result.equals(true)) {
        Notification n = new Notification.Builder(context)
          .setContentTitle(resources.getString(R.string.app_name))
          .setContentText(resources.getString(R.string.wifi_login_success))
          .setSmallIcon(R.drawable.ic_launcher)
          .setContentIntent(contentIntent)
          .getNotification();
        nm.notify("CCULife_Wifi_Auto_Login", 1, n);
      } else {
        Notification n = new Notification.Builder(context)
          .setContentTitle(resources.getString(R.string.app_name))
          .setContentText(resources.getString(R.string.wifi_login_check_pw))
          .setSmallIcon(R.drawable.ic_launcher)
          .setContentIntent(contentIntent)
          .getNotification();
        nm.notify("CCULife_Wifi_Auto_Login", 1, n);
      }
    }
  }
}
