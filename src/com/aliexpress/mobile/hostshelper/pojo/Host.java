package com.aliexpress.mobile.hostshelper.pojo;

import android.os.Parcel;
import android.os.Parcelable;

public class Host implements Parcelable {

    /**
     * ip
     */
    private String  hostIp;
    /**
     * 域名
     */
    private String  hostName;
    /**
     * 注释
     */
    private String  comment;
    /**
     * 是否被注释
     */
    private boolean isCommented;
    /**
     * 是否有效
     */
    private boolean isValid;

    public String getHostIp() {
        return hostIp;
    }

    public void setHostIp(String hostIp) {
        this.hostIp = hostIp;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public boolean isCommented() {
        return isCommented;
    }

    public void setCommented(boolean isCommented) {
        this.isCommented = isCommented;
    }

    public void toggleComment() {
        this.isCommented = !this.isCommented;
    }

    public boolean isValid() {
        return isValid;
    }

    public void setValid(boolean isValid) {
        this.isValid = isValid;
    }

    @Override
    public int describeContents() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(hostIp);
        dest.writeString(hostName);
        dest.writeString(comment);
        dest.writeByte((byte) (isCommented ? 1 : 0));
        dest.writeByte((byte) (isValid ? 1 : 0));
    }
}
