package com.aliexpress.mobile.hostshelper.utils;

import java.util.List;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.aliexpress.mobile.hostshelper.host.HostsManager;
import com.aliexpress.mobile.hostshelper.pojo.Host;

public class TaskAsync extends AsyncTask<List<Host>, Void, Void> {

    private Context context;

    public TaskAsync(Context ctx) {
        init(ctx);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected Void doInBackground(List<Host>... params) {
        HostsManager hostManager = new HostsManager();
        hostManager.saveHosts(context, params[0]);
        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        super.onPostExecute(result);
        Log.d("alibaba", "Task fully executed");
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
        Log.d("alibaba", "Task cancelled");
    }

    public void init(Context appContext) {
        context = appContext;
    }
}
