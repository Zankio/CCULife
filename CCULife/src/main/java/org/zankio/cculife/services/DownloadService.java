package org.zankio.cculife.services;

import org.zankio.cculife.override.Net;
import org.zankio.cculife.R;

import android.annotation.TargetApi;
import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.widget.Toast;
import android.os.Environment;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;

import com.koushikdutta.ion.ProgressCallback;

import com.koushikdutta.ion.Ion;
import com.koushikdutta.async.future.FutureCallback;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.io.PrintStream;
import java.io.FileOutputStream;
import java.security.KeyManagementException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.KeyManagerFactory;

import java.io.File;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.concurrent.Future;
import java.util.concurrent.ExecutionException;

import javax.net.ssl.HttpsURLConnection;

import java.util.logging.Level;
import java.util.logging.Logger;

public class DownloadService extends IntentService {
    public enum State { Downloading, Finished, Error};
    static ArrayList<Integer> notifyID;
    static TrustManagerFactory tmf;
    static int total = 0;

    public DownloadService() {
        super("DownloadService");
    }

    public void onCreate() {
        super.onCreate();
    }

    public static void downloadFile(Context context, String url, String filename) {
        Intent download = new Intent(context, DownloadService.class);
        Bundle data = new Bundle();
        data.putString("url", url);
        data.putString("filename", filename);
        download.putExtras(data);

        context.startService(download);
    }


    private PendingIntent generateOpenFilePendingIntent(int id, String path, String filename, String state) {
        Bundle data;
        Intent notifyIntent;

        notifyIntent = new Intent(this, FileOpenService.class);

        data = new Bundle();
        data.putInt("id", id);
        data.putString("path", path);
        data.putString("filename", filename);
        data.putString("state", state);

        notifyIntent.putExtras(data);
        return PendingIntent.getService(this, id, notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    @TargetApi(Build.VERSION_CODES.FROYO)
    @Override
    protected void onHandleIntent(Intent intent) {
        final NotificationManager mNotifyManager;
        final NotificationCompat.Builder mBuilder;
        final PendingIntent notifyFinishIntent;
        final PendingIntent notifyErrorIntent;
        final int currentId = total++;
        notifyID.add(currentId);

        Bundle data;

        String filename, url;
        File path;
        initSSL();

        data = intent.getExtras();
        filename = data.getString("filename");
        url = data.getString("url");

        path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        mNotifyManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);


        mBuilder = new NotificationCompat.Builder(this);
        DownloadService.notify(this, mNotifyManager, mBuilder, currentId, filename);
        notifyFinishIntent = generateOpenFilePendingIntent(currentId, path.getAbsolutePath(), filename, "finish");
        notifyErrorIntent = generateOpenFilePendingIntent(currentId, path.getAbsolutePath(), filename, "error");

        try {
            Ion.with(this)
                .load(url)
                .progress(new ProgressCallback() {
                    @Override
                    public void onProgress(long downloaded, long total) {
                        DownloadService.notify(DownloadService.this, mNotifyManager, mBuilder, currentId, (int) total, (int) downloaded);
                    }
                })
                .write(new File(path, filename))
                .setCallback(new FutureCallback<File>() {
                    @Override
                    public void onCompleted(Exception e, File file) {
                        if (e != null)
                            DownloadService.notify(DownloadService.this, mNotifyManager, mBuilder, currentId, State.Error, notifyErrorIntent);
                        else
                            DownloadService.notify(DownloadService.this, mNotifyManager, mBuilder, currentId, State.Finished, notifyFinishIntent);
                    }
                }).get();
        } catch (Exception e) {
            DownloadService.notify(this, mNotifyManager, mBuilder, currentId, State.Error, notifyErrorIntent);
        }
    }
    private static void notify (Context context, NotificationManager mNotifyManager, NotificationCompat.Builder builder, int id, String filename) {
        builder.setContentTitle(context.getResources().getString(R.string.download_file) + filename)
                .setContentText(context.getResources().getString(R.string.downloading))
                .setSmallIcon(R.drawable.sicon)
                .setOngoing(true);
        mNotifyManager.notify(id, builder.build());
    }

    private static void notify(Context context, NotificationManager mNotifyManager, NotificationCompat.Builder builder, int id, int total, int downloaded) {
        builder.setProgress(total, downloaded, false);
        mNotifyManager.notify(id, builder.build());
    }
    private static void notify(Context context, NotificationManager mNotifyManager, NotificationCompat.Builder builder, int id, DownloadService.State state) {
        notify(context, mNotifyManager, builder, id, state, null);
    }
    private static void notify(Context context, NotificationManager mNotifyManager, NotificationCompat.Builder builder, int id, DownloadService.State state, PendingIntent pendingIntent) {
        switch (state) {
            case Finished:
                builder.setContentText(context.getResources().getString(R.string.download_complete));
                break;
            case Error:
                builder.setContentText(context.getResources().getString(R.string.download_error));
                break;
        }
        builder.setProgress(0, 0, false);
        builder.setOngoing(false);
        if (pendingIntent != null) builder.setContentIntent(pendingIntent);
        mNotifyManager.notify(id, builder.build());
        notifyID.remove(Integer.valueOf(id));
    }

    @Override
    public void onDestroy() {
        NotificationManager mNotifyManager;
        mNotifyManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        for(int id: notifyID) {
            mNotifyManager.cancel(id);
        }
    }

    public void initSSL() {

        try {

            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            InputStream caInput = new BufferedInputStream(this.getAssets().open("ssl.crt"));
            Certificate ca;

            try {
                ca = cf.generateCertificate(caInput);
            } finally {
                caInput.close();
            }

            // Create a KeyStore containing our trusted CAs
            String keyStoreType = KeyStore.getDefaultType();
            KeyStore keyStore = KeyStore.getInstance(keyStoreType);
            keyStore.load(null, null);
            keyStore.setCertificateEntry("ca", ca);

            // Create a TrustManager that trusts the CAs in our KeyStore
            String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
            tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
            tmf.init(keyStore);

            // Create an SSLContext that uses our TrustManager
            SSLContext ssl_context = SSLContext.getInstance("TLS");
            ssl_context.init(null, tmf.getTrustManagers(), null);
            Ion.getDefault(this).getHttpClient().getSSLSocketMiddleware().setTrustManagers(tmf.getTrustManagers());
            Ion.getDefault(this).getHttpClient().getSSLSocketMiddleware().setSSLContext(ssl_context);


        } catch (CertificateException ignored) {
        } catch (KeyStoreException ignored) {
        } catch (KeyManagementException ignored) {
        } catch (NoSuchAlgorithmException ignored) {
        } catch (IOException ignored) {
        } catch (Exception ignored) {
        }
    }

}
