package org.zankio.cculife.CCUService.base.SourceSwitcher;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import org.zankio.cculife.CCUService.base.source.ISource;


public class AutoNetworkSourceSwitcher implements ISwitcher {

    private Context context;
    private ISource localSource, remoteSource;

    public AutoNetworkSourceSwitcher(Context context) {
        this.context = context;
    }

    public AutoNetworkSourceSwitcher(Context context, ISource localSource, ISource remoteSource) {
        this.context = context;
        this.localSource = localSource;
        this.remoteSource = remoteSource;
    }

    public AutoNetworkSourceSwitcher setLocalSource(ISource localSource) {
        this.localSource = localSource;
        return this;
    }

    public AutoNetworkSourceSwitcher setRemoteSource(ISource remoteSource) {
        this.remoteSource = remoteSource;
        return this;
    }

    @Override
    public ISource getSource() {
        return ckeckNetworkConnection() ? remoteSource : localSource;
    }

    @Override
    public void closeSource() {
        if(localSource != null) localSource.closeSource();
        if(remoteSource != null) remoteSource.closeSource();
    }

    @Override
    public void openSource() {
        if(localSource != null) localSource.openSource();
        if(remoteSource != null) remoteSource.openSource();
    }

    private boolean ckeckNetworkConnection() {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        //Log.e("", "getTypeName:" + netInfo.getTypeName() + ", netInfo.isConnected:" + netInfo.isConnected());
        return netInfo != null && netInfo.isConnected();
    }

    public ISource getLocalSource() {
        return localSource;
    }
}
