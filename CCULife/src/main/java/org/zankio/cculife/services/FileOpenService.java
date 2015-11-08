package org.zankio.cculife.services;

import android.annotation.TargetApi;
import android.app.IntentService;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.webkit.MimeTypeMap;

import java.io.File;

public class FileOpenService extends IntentService {

    public FileOpenService() {
        super("FileOpenService");
    }

    @TargetApi(Build.VERSION_CODES.FROYO)
    @Override
    protected void onHandleIntent(Intent intent) {
        NotificationManager mNotifyManager;
        MimeTypeMap map;
        Bundle data;

        int id;
        String ext;
        String type;
        String path;
        String filename;

        map = MimeTypeMap.getSingleton();
        mNotifyManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        data = intent.getExtras();
        filename = data.getString("filename");
        path = data.getString("path");
        id = data.getInt("id");

        File file = new File(path, filename);
        if (file.exists()) {
            String[] names = file.getName().split("\\.");
            if (names.length > 0)
                ext = names[names.length - 1];
            else
                ext = MimeTypeMap.getFileExtensionFromUrl(file.getName());

            type = map.getMimeTypeFromExtension(ext);

            if (type == null)
                type = "*/*";

            intent = new Intent();
            intent.setAction(Intent.ACTION_VIEW);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setDataAndType(Uri.fromFile(file), type);
            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivity(intent);
            }
        }
        mNotifyManager.cancel(id);

    }
}
