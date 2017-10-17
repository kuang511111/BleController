package com.jrdcom.wearable.smartband2.entity;

/**
 * Created by ${zhiwei.xu} on 2017/6/16.
 */

public class BtDeviceEntity extends BaseEntity
{
    public static final int BT_DEVICE_STATUS_IDEL = 0;
    public static final int BT_DEVICE_STATUS_FOUND = 1;
    public static final int BT_DEVICE_STATUS_CONNECTED = 2;
    public static final int BT_DEVICE_STATUS_DISCONNECTED = 3;
    public static final int BT_DEVICE_STATUS_SERVICE_OK = 4;
    public static final int BT_DEVICE_STATUS_AUTO_VAL_PASS = 5;
    public static final int BT_DEVICE_STATUS_ALL_VAL_FINISH = 6;

    private String mac;
    private int status;
    private String name;
    private int currentRssi;


    //BT TEST PROPERTY
    private boolean isBtTestOk;
    private VersionEntity versionEntity;
    private String pcbaVersion;
    private boolean isGsensorOk;
    private boolean isWriteFlagOk;
    private boolean isLedTestOk;
    private boolean isVibTestOk;
    private boolean isChargerTestOk;
    private boolean isAntiTestOk;


    public String getMac()
    {
        return mac;
    }

    public void setMac(String mac)
    {
        this.mac = mac;
    }

    public int getStatus()
    {
        return status;
    }

    public void setStatus(int status)
    {
        this.status = status;
    }

    public int getCurrentRssi()
    {
        return currentRssi;
    }

    public void setCurrentRssi(int currentRssi)
    {
        this.currentRssi = currentRssi;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }


    public boolean isBtTestOk()
    {
        return isBtTestOk;
    }

    public void setBtTestOk(boolean btTestOk)
    {
        isBtTestOk = btTestOk;
    }

    public VersionEntity getVersionEntity()
    {
        return versionEntity;
    }

    public void setVersionEntity(VersionEntity versionEntity)
    {
        this.versionEntity = versionEntity;
    }

    public String getPcbaVersion()
    {
        return pcbaVersion;
    }

    public void setPcbaVersion(String pcbaVersion)
    {
        this.pcbaVersion = pcbaVersion;
    }

    public boolean isGsensorOk()
    {
        return isGsensorOk;
    }

    public void setGsensorOk(boolean gsensorOk)
    {
        isGsensorOk = gsensorOk;
    }

    public boolean isWriteFlagOk()
    {
        return isWriteFlagOk;
    }

    public void setWriteFlagOk(boolean writeFlagOk)
    {
        isWriteFlagOk = writeFlagOk;
    }

    public boolean isLedTestOk()
    {
        return isLedTestOk;
    }

    public void setLedTestOk(boolean ledTestOk)
    {
        isLedTestOk = ledTestOk;
    }

    public boolean isVibTestOk()
    {
        return isVibTestOk;
    }

    public void setVibTestOk(boolean vibTestOk)
    {
        isVibTestOk = vibTestOk;
    }

    public boolean isChargerTestOk()
    {
        return isChargerTestOk;
    }

    public void setChargerTestOk(boolean chargerTestOk)
    {
        isChargerTestOk = chargerTestOk;
    }

    public boolean isAntiTestOk()
    {
        return isAntiTestOk;
    }

    public void setAntiTestOk(boolean antiTestOk)
    {
        isAntiTestOk = antiTestOk;
    }

    @Override
    public String getEntityId()
    {
        return mac;
    }

    @Override
    public String getEntityDefaultFileName()
    {
        return BtDeviceEntity.class.getSimpleName();
    }

    @Override
    public String toString()
    {
        return "BtDeviceEntity{" +
                "mac='" + mac + '\'' +
                ", status=" + status +
                ", name='" + name + '\'' +
                ", currentRssi=" + currentRssi +
                ", isBtTestOk=" + isBtTestOk +
                ", versionEntity=" + versionEntity +
                ", pcbaVersion='" + pcbaVersion + '\'' +
                ", isGsensorOk=" + isGsensorOk +
                ", isWriteFlagOk=" + isWriteFlagOk +
                ", isLedTestOk=" + isLedTestOk +
                ", isVibTestOk=" + isVibTestOk +
                ", isChargerTestOk=" + isChargerTestOk +
                ", isAntiTestOk=" + isAntiTestOk +
                '}';
    }
}
