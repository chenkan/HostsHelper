package com.aliexpress.mobile.hostshelper.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.text.TextUtils;

import com.aliexpress.mobile.hostshelper.pojo.Host;

public class HostsUtil {

    public static final String   STR_COMMENT      = "#";
    private static final String  STR_SEPARATOR    = " ";
    private static final String  HOST_PATTERN_STR = "^\\s*(" + STR_COMMENT + "?)\\s*(\\S*)\\s*([^" + STR_COMMENT
                                                    + "]*)" + STR_COMMENT + "?(.*)$";
    private static final Pattern HOST_PATTERN     = Pattern.compile(HOST_PATTERN_STR);

    /**
     * Host对象转化为string类型
     * 
     * @param hs
     * @return
     */
    public static String toString(Host hs) {
        StringBuilder sb = new StringBuilder();

        if (hs.isCommented()) {
            sb.append(STR_COMMENT);
        }
        if (hs.getHostIp() != null) {
            sb.append(hs.getHostIp()).append(STR_SEPARATOR);
        }
        if (hs.getHostName() != null) {
            sb.append(hs.getHostName());
        }
        if (!TextUtils.isEmpty(hs.getComment())) {
            sb.append(STR_SEPARATOR).append(STR_COMMENT).append(hs.getComment());
        }
        return sb.toString();
    }

    /**
     * host string转化为Host
     * 
     * @param line
     * @return
     */
    public static Host fromString(String line) {
        Host host = new Host();

        Matcher matcher = HOST_PATTERN.matcher(line);
        String ip = null;
        String name = null;
        String comment = null;
        boolean isCommented = false;

        if (matcher.find()) {
            isCommented = !TextUtils.isEmpty(matcher.group(1));
            ip = matcher.group(2);
            name = matcher.group(3).trim();
            comment = matcher.group(4).trim();
            if (TextUtils.isEmpty(comment)) {
                comment = null;
            }
        }
        host.setHostIp(ip);
        host.setHostName(name);
        host.setComment(comment);
        host.setCommented(isCommented);

        return host;
    }

    /**
     * 修改host
     * 
     * @param ori
     * @param temp
     * @return
     */
    public static void editHost(Host ori, Host temp) {
        ori.setHostIp(temp.getHostIp());
        ori.setHostName(temp.getHostName());
        return;
    }

    /**
     * host校验:只是对ip进行简单的校验
     * 
     * @param
     * @return
     */
    public static boolean isValid(Host hs) {

        String ip = hs.getHostIp();
        String hostname = hs.getHostName();

        if (!TextUtils.isEmpty(ip) && !TextUtils.isEmpty(hostname) && (ip.split("\\.").length == 4)) return true;

        return false;
    }
}
