package com.jrdcom.wearable.smartband2.util;

import java.io.Serializable;

public class ReturnInfoStruct implements Serializable {


    private String name;
    private String hostUrl;
    private String apkUrl;
    private String version_name;
    private String size;
    private String package_name;
    private int download_count;

    private int flag;
    public int getFlag(){
       return flag;
    }
    public void setFlag(int flag){
       this.flag = flag;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public String getHostUrl() {
        return hostUrl;
    }
    public void setHostUrl(String hostUrl) {
        this.hostUrl = hostUrl;
    }

    public String getApkUrl() {
        return apkUrl;
    }
    public void setApkUrl(String apkUrl) {
        this.apkUrl = apkUrl;
    }

    public String getSize() {
        return size;
    }
    public void setSize(String size) {
        this.size = size;
    }

    public String getVersion() {
        return version_name;
    }
    public void setVersion(String version_name) {
        this.version_name = version_name;
    }

    public int getDownloadCount() {
        return download_count;
    }
    public void setDownloadNum(int download_count) {
        this.download_count = download_count;
    }

    public String getPackageName() {
        return package_name;
    }
    public void setPackageName(String package_name) {
        this.package_name = package_name;
    }

    public String toString() {
        return "name:" + name + " hostUrl:" + hostUrl + " apkUrl:" + apkUrl
                 + " size:" + size + " version_name:" + version_name + " downloadCount:" + download_count
                + " packageName:" + package_name;
    }
}
