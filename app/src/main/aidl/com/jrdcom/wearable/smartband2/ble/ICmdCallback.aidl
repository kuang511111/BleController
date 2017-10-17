package com.jrdcom.wearable.smartband2.ble;

/**
 * Created by rongwu.chen on 14-3-9.
 */
interface ICmdCallback {
    void onCurrentState(int state);
    void onStartBinding(String address);
    void onScanDeviceFound(String name, String address, int rssi);
    void onBindResult(String name, String address, boolean entered);
    void onLoginResult(String name, String address, boolean entered);

    //void onStartSynchronize();
    //void onSynchronizeComplete();

    //void onModeChanged(int mode);

    //void onBatteryLevelChanged(int level);

    //void onOtaEnter();
}
