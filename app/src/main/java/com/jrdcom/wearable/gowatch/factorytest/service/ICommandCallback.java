package com.jrdcom.wearable.gowatch.factorytest.service;

import com.jrdcom.wearable.smartband2.entity.CommondEntity;

/**
 * Created by ${zhiwei.xu} on 2017/6/20.
 */

public interface ICommandCallback
{
    public static final int COMMOND_SEND_SUCCESS = 0;
    public static final int COMMOND_SEND_WRITE_FAILED = 1;
    public static final int COMMOND_SEND_TIME_OUT = 2;


    void onSuccess();

    void onAckSuccess();

    void onFailed(int errorCode, String errorMessage);

    void onResponse(CommondEntity entity);
}
