package org.zankio.cculife.services;

import android.app.IntentService;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.koushikdutta.ion.Ion;

import org.zankio.cculife.ui.UpdateUI;
import org.zankio.cculife.ui.dialog.UpdateDialog;

public class CheckUpdateService extends IntentService {
    private static String GITHUB_RELEASE_URL = "https://api.github.com/repos/zankio/CCULife/releases";

    public CheckUpdateService() {
        super("CheckUpdateService");
    }

    private String getVersion() {
        String version = "";
        try {
            PackageManager pm = getPackageManager();
            PackageInfo info = pm.getPackageInfo(getPackageName(), 0);
            version = info.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return version;
    }

    private boolean hasNewer(String current, String lastest) {
        String[] cur = current.split("\\.");
        String[] last = lastest.split("\\.");
        for (int i = 0; i < last.length; i++) {
            if (i >= cur.length) return true;
            int cmp;
            if ((cmp = Integer.parseInt(last[i]) - Integer.parseInt(cur[i])) > 0) {
                return true;
            } else if (cmp < 0) {
                return false;
            }
        }
        return false;
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        try {
            JsonArray releases = Ion.with(this).load(GITHUB_RELEASE_URL).asJsonArray().get();
            JsonObject lastest = releases.get(0).getAsJsonObject();
            String tagname = lastest.get("tag_name").getAsString().substring(1);
            String description = lastest.get("body").getAsString();
            String version = getVersion();
            if (tagname != version) {
                if (hasNewer(version, tagname)) {
                    Intent update = new Intent(this, UpdateUI.class);
                    update.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    update.putExtra("version", tagname);
                    update.putExtra("description", description);

                    startActivity(update);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
