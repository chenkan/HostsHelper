package com.aliexpress.mobile.hostshelper;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.aliexpress.mobi.hostshelper.R;
import com.aliexpress.mobile.hostshelper.host.HostsManager;
import com.aliexpress.mobile.hostshelper.pojo.Host;
import com.aliexpress.mobile.hostshelper.utils.HostsUtil;
import com.aliexpress.mobile.hostshelper.utils.TaskAsync;

public class AddEditActivity extends Activity {

    private HostsManager hostManager = new HostsManager();
    private List<Host>   hostsList   = new ArrayList<Host>();
    private ProgressBar  pb;
    private EditText     hostIp;
    private EditText     hostName;
    private boolean      editFlag    = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.add_item);

        hostIp = (EditText) findViewById(R.id.ip_address);
        hostName = (EditText) findViewById(R.id.host_name);

        editFlag = false;
        final Host temp = new Host();
        Intent intent = getIntent();
        String hostIpStr = intent.getStringExtra("hostIp");
        String hostNameStr = intent.getStringExtra("hostName");
        if (hostIpStr != null && hostNameStr != null) {
            hostIp.setText(hostIpStr);
            hostName.setText(hostNameStr);
            editFlag = true;
        }

        if (editFlag) {
            getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.layout_activity_edit_title_custom);
        } else {
            getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.layout_activity_add_title_custom);
        }

        final int position = intent.getIntExtra("position", 0);

        Button saveBut = (Button) findViewById(R.id.but_save);
        Button cancelBut = (Button) findViewById(R.id.but_cancel);
        pb = (ProgressBar) findViewById(R.id.addWaiting);

        saveBut.setOnClickListener(new Button.OnClickListener() {

            @Override
            public void onClick(View v) {
                hostsList = hostManager.getHosts();

                if (editFlag) {
                    temp.setHostIp(hostIp.getText().toString());
                    temp.setHostName(hostName.getText().toString());
                    if (HostsUtil.isValid(temp)) {
                        HostsUtil.editHost(hostsList.get(position), temp);
                        new AddEditAsyncTask(getBaseContext()).execute(hostsList);
                    } else {
                        AlertDialog.Builder builder = new AlertDialog.Builder(AddEditActivity.this);
                        builder.setTitle("Error").setIcon(android.R.drawable.ic_dialog_alert).setMessage("Please input valid host.");
                        builder.create().show();

                    }
                } else {
                    Host addHost = new Host();
                    addHost.setHostIp(hostIp.getText().toString());
                    addHost.setHostName(hostName.getText().toString());
                    if (HostsUtil.isValid(addHost)) {
                        addHost.setCommented(false);
                        addHost.setValid(true);
                        hostsList.add(addHost);
                        new AddEditAsyncTask(getBaseContext()).execute(hostsList);
                    } else {
                        AlertDialog.Builder builder = new AlertDialog.Builder(AddEditActivity.this);
                        builder.setTitle("Error").setIcon(android.R.drawable.ic_dialog_alert).setMessage("Please input valid host.");
                        builder.create().show();
                    }

                }

            }
        });

        cancelBut.setOnClickListener(new Button.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                AddEditActivity.this.finish();
            }
        });
    }

    public class AddEditAsyncTask extends TaskAsync {

        public AddEditAsyncTask(Context ctx) {
            super(ctx);
        }

        @Override
        protected void onPreExecute() {
            pb.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onPostExecute(Void result) {
            pb.setVisibility(View.GONE);
            setResult(Activity.RESULT_OK);
            AddEditActivity.this.finish();
        }
    }
}
