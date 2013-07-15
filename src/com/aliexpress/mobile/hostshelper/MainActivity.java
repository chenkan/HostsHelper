package com.aliexpress.mobile.hostshelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

import com.aliexpress.mobi.hostshelper.R;
import com.aliexpress.mobile.hostshelper.host.HostsManager;
import com.aliexpress.mobile.hostshelper.pojo.Host;
import com.aliexpress.mobile.hostshelper.utils.HostsUtil;
import com.aliexpress.mobile.hostshelper.utils.TaskAsync;

/**
 * 类MainActivity.java的实现描述：Hosts helper主页面
 * 
 * @author Emily 2013-7-10 下午3:12:38
 */
public class MainActivity extends Activity {

    private int                rstCode   = 1;                           // startActivityResult code

    private ListView           lvHosts;
    private ListAdapter        hostsListAdapter;
    private List<Host>         hostsList = new ArrayList<Host>();
    private Map<Integer, Host> selectMap = new HashMap<Integer, Host>(); // 记录checkbox选中数据
    private HostsManager       hostManager;
    private ProgressBar        pb;

    private PopupWindow        pw;
    private Button             add;
    private Button             more;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 自定义页面title
        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.activity_main);
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.layout_activity_main_title_custom);

        add = (Button) findViewById(R.id.addMenu);
        more = (Button) findViewById(R.id.moreMenu);

        // 新增host
        add.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                Intent intent = new Intent();
                intent.setClass(MainActivity.this, AddEditActivity.class);
                startActivityForResult(intent, rstCode);
            }
        });
        // 更多批量操作：注释，删除
        more.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                morePop(v);
            }
        });

        pb = (ProgressBar) findViewById(R.id.listProgressbar);

        lvHosts = (ListView) findViewById(R.id.listViewHosts);
        hostsListAdapter = new ListAdapter();
        lvHosts.setAdapter(hostsListAdapter);

        hostManager = new HostsManager();
        hostsListAdapter.setHostList(hostManager.getHosts());

        // item长按
        lvHosts.setOnItemLongClickListener(new OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                initPop(arg1, arg2);
                return true;
            }

        });
    }

    /**
     * more menu
     * 
     * @param v
     * @param position
     */
    private void morePop(View v) {
        LinearLayout pv = (LinearLayout) LayoutInflater.from(MainActivity.this).inflate(R.layout.more_menu, null);

        Button btDel = (Button) pv.findViewById(R.id.delete);
        Button btCom = (Button) pv.findViewById(R.id.comment);

        pw = new PopupWindow(MainActivity.this);
        pw.setContentView(pv);
        pw.setBackgroundDrawable(this.getResources().getDrawable(R.drawable.mm_title_functionframe));
        pw.setWidth(getWindowManager().getDefaultDisplay().getWidth() / 2);
        pw.setHeight(LayoutParams.WRAP_CONTENT);
        pw.setOutsideTouchable(true);
        pw.setFocusable(true);

        pw.showAsDropDown(v);

        btDel.setOnClickListener(new Button.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                if (selectMap.size() <= 0) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setTitle("Warning").setIcon(android.R.drawable.ic_dialog_info).setMessage("Please chose one or more hosts.");
                    builder.create().show();
                    pw.dismiss();
                    return;
                }
                int count = hostsList.size();
                for (int positon = 0; positon < count; positon++) {
                    hostsList.remove(selectMap.get(positon));
                }
                new SaveAsyncTask(getBaseContext()).execute(hostsList);
                selectMap.clear();
                pw.dismiss();
            }

        });
        btCom.setOnClickListener(new Button.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                if (selectMap.size() <= 0) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setTitle("Warning").setIcon(android.R.drawable.ic_dialog_info).setMessage("Please chose one or more hosts.");
                    builder.create().show();
                    pw.dismiss();
                    return;
                }
                int c = hostsList.size();
                for (int position = 0; position < c; position++) {
                    if (selectMap.get(position) != null) {
                        hostsList.get(position).toggleComment();
                    }
                }
                new SaveAsyncTask(getBaseContext()).execute(hostsList);
                selectMap.clear();
                pw.dismiss();
            }
        });
    }

    /**
     * 长按popupwindow: edit,delete,comment
     * 
     * @param v
     * @param position
     */
    private void initPop(View v, final int position) {
        LinearLayout pv = (LinearLayout) LayoutInflater.from(MainActivity.this).inflate(R.layout.long_click, null);

        Button btEdit = (Button) pv.findViewById(R.id.edit);
        Button btDel = (Button) pv.findViewById(R.id.delete);
        Button btCom = (Button) pv.findViewById(R.id.comment);

        pw = new PopupWindow(MainActivity.this);
        pw.setBackgroundDrawable(this.getResources().getDrawable(R.drawable.group_bg));
        pw.setContentView(pv);

        pw.setWidth(getWindowManager().getDefaultDisplay().getWidth());
        pw.setHeight(90);
        pw.setOutsideTouchable(true);
        pw.setFocusable(true);

        pw.showAsDropDown(v);

        btEdit.setOnClickListener(new Button.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                Intent intent = new Intent();
                intent.putExtra("hostIp", hostsList.get(position).getHostIp());
                intent.putExtra("hostName", hostsList.get(position).getHostName());
                intent.putExtra("position", position);
                intent.setClass(MainActivity.this, AddEditActivity.class);
                startActivityForResult(intent, rstCode);
                pw.dismiss();
            }
        });
        btDel.setOnClickListener(new Button.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                hostsList.remove(position);
                new SaveAsyncTask(getBaseContext()).execute(hostsList);
                pw.dismiss();
            }

        });
        btCom.setOnClickListener(new Button.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                hostsList.get(position).toggleComment();
                new SaveAsyncTask(getBaseContext()).execute(hostsList);
                pw.dismiss();
            }

        });
    }

    /**
     * 监听 AddActivity返回结果，更新listview
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != Activity.RESULT_OK) {
            return;
        }
        if (requestCode == rstCode) {
            hostsListAdapter.setHostList(hostManager.getHosts());
        }
    }

    /**
     * hosts list customizing adapter.
     */
    private class ListAdapter extends BaseAdapter {

        class Viewholder {

            TextView hostStr;
            CheckBox checkBox;
        }

        public void setHostList(List<Host> list) {
            hostsList = list;
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return hostsList.size();
        }

        @Override
        public Object getItem(int position) {
            return hostsList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            Viewholder holder;// = new Viewholder();

            if (convertView == null) {
                convertView = MainActivity.this.getLayoutInflater().inflate(R.layout.hosts_item, null);
                holder = new Viewholder();
                holder.hostStr = (TextView) convertView.findViewById(R.id.text);
                holder.checkBox = (CheckBox) convertView.findViewById(R.id.checkbox);
                convertView.setTag(holder);
            } else {
                holder = (Viewholder) convertView.getTag();
            }

            // convertView = MainActivity.this.getLayoutInflater().inflate(R.layout.hosts_item, null);
            // holder.hostStr = (TextView) convertView.findViewById(R.id.text);
            // holder.checkBox = (CheckBox) convertView.findViewById(R.id.checkbox);

            holder.checkBox.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View arg0) {
                    if (selectMap.get(position) != null) {
                        selectMap.remove(position);
                    } else {
                        selectMap.put(position, hostsList.get(position));
                    }
                }
            });

            holder.hostStr.setText(HostsUtil.toString(hostsList.get(position)));

            if (selectMap.get(position) != null) {
                holder.checkBox.setChecked(true);
            } else {
                holder.checkBox.setChecked(false);
            }

            return convertView;
        }
    }

    /**
     * 保存hosts异步任务
     */
    public class SaveAsyncTask extends TaskAsync {

        public SaveAsyncTask(Context ctx) {
            super(ctx);
        }

        @Override
        protected void onPreExecute() {
            pb.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onPostExecute(Void result) {
            pb.setVisibility(View.GONE);
            // 异步任务完成后，触发页面更新
            hostsListAdapter.setHostList(hostManager.getHosts());
        }
    }
}
