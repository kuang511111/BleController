package com.jrdcom.wearable.smartband2.entity;

import com.jrdcom.wearable.smartband2.util.LogUtil;

/**
 * Created by ${zhiwei.xu} on 2017/6/26.
 */
public class VersionEntity{

    private static final String defautSerialVersion = "000000000000000";


    public String cuVersion;
    public String mainCodeVersion;
    public String serialVersion;

    public VersionEntity(String version)
    {
        LogUtil.d("VersionEntity","device_info="+version);
        String mVer[] = version.split("@");
        if (mVer != null && mVer.length == 3)
        {
            this.cuVersion=mVer[0];
            this.mainCodeVersion=mVer[1];
            this.serialVersion=mVer[2];
        }
        if (mVer != null && mVer.length == 2) {
            this.cuVersion=mVer[0];
            this.mainCodeVersion=mVer[1];
            this.serialVersion=defautSerialVersion;
        }
    }

    @Override
    public String toString()
    {
        return "VersionEntity{" +
                "cuVersion='" + cuVersion + '\'' +
                ", mainCodeVersion='" + mainCodeVersion + '\'' +
                ", serialVersion='" + serialVersion + '\'' +
                '}';
    }
}
