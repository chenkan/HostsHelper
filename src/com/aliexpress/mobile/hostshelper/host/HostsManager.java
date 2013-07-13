package com.aliexpress.mobile.hostshelper.host;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeoutException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;

import android.content.Context;
import android.util.Log;

import com.aliexpress.mobile.hostshelper.pojo.Host;
import com.aliexpress.mobile.hostshelper.utils.HostsUtil;
import com.stericson.RootTools.RootTools;
import com.stericson.RootTools.exceptions.RootDeniedException;
import com.stericson.RootTools.execution.CommandCapture;

public class HostsManager {

    private static final String TAG               = "HostsManager";

    private static final String UTF_8             = "UTF-8";
    private static final String HOSTS_FILE_NAME   = "hosts";
    private static final String HOSTS_FILE_PATH   = "/system/etc/" + HOSTS_FILE_NAME;

    private static final String LINE_SEPARATOR    = System.getProperty("line.separator", "\n");
    private static final String MOUNT_TYPE_RO     = "ro";
    private static final String MOUNT_TYPE_RW     = "rw";
    private static final String COMMAND_RM        = "rm -f";
    private static final String COMMAND_CHOWN     = "chown 0:0";
    private static final String COMMAND_CHMOD_644 = "chmod 644";

    /**
     * 从hosts文件中读取当前数据
     * 
     * @return
     */
    public List<Host> getHosts() {
        List<Host> hosts = new ArrayList<Host>();
        LineIterator it = null;
        try {
            it = FileUtils.lineIterator(new File(HOSTS_FILE_PATH), UTF_8);
            while (it.hasNext()) {
                Host host = HostsUtil.fromString(it.nextLine());
                // 若host为null 或者 host中的ip和name均为“”，那么就不记录hosts中。
                if (host != null
                    && (!host.getHostIp().equalsIgnoreCase("") || !host.getHostName().equalsIgnoreCase(""))) {
                    hosts.add(host);
                }
            }
        } catch (IOException e) {
            Log.e(TAG, "I/O error while opening hosts file", e);
        } finally {
            if (it != null) {
                LineIterator.closeQuietly(it);
            }
        }
        return hosts;
    }

    /**
     * 保存hosts
     */
    public synchronized boolean saveHosts(Context appContext, List<Host> hostsList) {
        if (!RootTools.isAccessGiven()) {
            Log.w(TAG, "Can't get root access");
            return false;
        }

        // Step 1: Create temporary hosts file in /data/data/project_package/files/hosts
        if (!createTempHostsFile(appContext, hostsList)) {
            Log.w(TAG, "Can't create temporary hosts file");
            return false;
        }

        String tmpFile = String.format(Locale.US, "%s/%s", appContext.getFilesDir().getAbsolutePath(), HOSTS_FILE_NAME);
        String backupFile = String.format(Locale.US, "%s.bak", tmpFile);

        // Step 2: Get canonical path for /etc/hosts (it could be a symbolic link)
        String hostsFilePath = HOSTS_FILE_PATH;
        File hostsFile = new File(HOSTS_FILE_PATH);
        if (hostsFile != null && hostsFile.exists()) {
            try {
                if (FileUtils.isSymlink(hostsFile)) {
                    hostsFilePath = hostsFile.getCanonicalPath();
                }
            } catch (IOException e1) {
                Log.e(TAG, "", e1);
            }
        } else {
            Log.w(TAG, "Hosts file was not found in filesystem");
        }

        try {
            // Step 3: Create backup of current hosts file (if any)
            RootTools.remount(hostsFilePath, MOUNT_TYPE_RW);
            runRootCommand(COMMAND_RM, backupFile);
            RootTools.copyFile(hostsFilePath, backupFile, false, true);

            // Step 4: Replace hosts file with generated file
            runRootCommand(COMMAND_RM, hostsFilePath);
            RootTools.copyFile(tmpFile, hostsFilePath, false, true);

            // Step 5: Give proper rights
            runRootCommand(COMMAND_CHOWN, hostsFilePath);
            runRootCommand(COMMAND_CHMOD_644, hostsFilePath);

            // Step 6: Delete local file
            appContext.deleteFile(HOSTS_FILE_NAME);
        } catch (Exception e) {
            Log.e(TAG, "", e);
            return false;
        } finally {
            RootTools.remount(hostsFilePath, MOUNT_TYPE_RO);
        }
        return true;
    }

    /**
     * 创建临时host文件
     */
    private boolean createTempHostsFile(Context appContext, List<Host> hostslist) {
        OutputStreamWriter writer = null;
        try {
            FileOutputStream out = appContext.openFileOutput(HOSTS_FILE_NAME, Context.MODE_PRIVATE);
            writer = new OutputStreamWriter(out);

            for (Host host : hostslist) {
                writer.append(HostsUtil.toString(host)).append(LINE_SEPARATOR);
            }
            writer.flush();
        } catch (IOException e) {
            Log.e(TAG, "", e);
            return false;
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    Log.e(TAG, "Error while closing writer", e);
                }
            }
        }
        return true;
    }

    /**
     * 执行命令
     */
    private void runRootCommand(String command, String uniqueArg) throws InterruptedException, IOException,
                                                                 TimeoutException, RootDeniedException {
        CommandCapture cmd = new CommandCapture(0, String.format(Locale.US, "%s %s", command, uniqueArg));
        RootTools.getShell(true).add(cmd).waitForFinish();
    }

}
