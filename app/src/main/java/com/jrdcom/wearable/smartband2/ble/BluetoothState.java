package com.jrdcom.wearable.smartband2.ble;

/**
 * Created by ${zhiwei.xu} on 2017/6/7.
 */

public class BluetoothState
{
    private static BluetoothState bluetoothState;
    private int connectState;

    public static final String ACTION = "com.jrdcom.wearable.smartband2.ble";
    public static final String ACTION_NEED_OPEN_GPS = ACTION+"stbleservice.ACTION_NEED_OPEN_GPS";


    private BluetoothState()
    {

    }

    public static BluetoothState getInstance()
    {
        if (bluetoothState == null)
        {
            synchronized (BluetoothState.class)
            {
                if (bluetoothState == null)
                {
                    bluetoothState = new BluetoothState();
                }
            }
        }
        return bluetoothState;
    }

    public int getConnectState()
    {
        return connectState;
    }

    public void setConnectState(int connectState)
    {
        this.connectState = connectState;
    }
}
