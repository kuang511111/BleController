package com.jrdcom.wearable.smartband2.ble;

/**
 * Created by ${zhiwei.xu} on 2017/6/6.
 */

public interface BleCommandCallback
{
    void onCommonAckCallback(int modelId, int keyId);
    void onCommonCallBack(int modelId, int keyId, byte[] value);
}
