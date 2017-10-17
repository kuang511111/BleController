package com.jrdcom.wearable.gowatch.factorytest.service;

/**
 * Created by ${zhiwei.xu} on 2017/6/20.
 */

public interface CmdCommond
{
    void onSuccess();

    void onAckSuccess();

    void onFailed(int errorCode,String errorMessage);

    void onResponse(int modelId, int keyId, byte[] data);
}
