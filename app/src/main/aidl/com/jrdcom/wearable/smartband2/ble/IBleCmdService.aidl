package com.jrdcom.wearable.smartband2.ble;

import com.jrdcom.wearable.smartband2.ble.ICmdCallback;

/**
 * Created by rongwu.chen on 14-3-9.
 */
interface IBleCmdService {
    int getStatus();
    void requestLogin(String bleAddress, String userName, boolean auto);
    void bind();
    void unbind();
    void quickNfcBind(String bleAddress, String userName);
    void reconnect();
    void disconnect();
    void reset();
    void registerCmdCallback(ICmdCallback cb);
    void unregisterCmdCallback(ICmdCallback cb);

    void startScan();
    void stopScan();

    //boolean isSyncCompleted();

    //void notifyAlarmChanged();
    //void notifyTargetChanged();
    //void notifyProfileChanged();
    //void notifyRemoteControlChanged();
    //void notifyAntiLossChanged();

    //void fetchClockAlarm();
    //void requestEnterOta();
    //void toggleMode();

    //void setInteractiveMode(int mode);

    //int getBatteryLevel();

    //String getManufactureName();
    //String getModel();
    //String getSerialNumber();
    //String getHardwareRevision();
    //String getFirmwareRevision();

    long sendCommand(String cmd, in byte[] value);
    void resetCalcSpeedTime();
    boolean isOtaRuning();
}
