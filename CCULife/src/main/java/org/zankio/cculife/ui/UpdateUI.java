package org.zankio.cculife.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;

import org.zankio.cculife.ui.dialog.UpdateDialog;

public class UpdateUI extends FragmentActivity{
    private final static String TAG = "UpdataUI";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        Bundle data = new Bundle();
        data.putString("version", intent.getStringExtra("version"));
        data.putString("description", intent.getStringExtra("description"));
        DialogFragment dialog = new UpdateDialog();
        dialog.setArguments(data);
        dialog.show(getSupportFragmentManager(), TAG);

    }
}
