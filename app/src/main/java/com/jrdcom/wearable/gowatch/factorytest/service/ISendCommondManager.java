package com.jrdcom.wearable.gowatch.factorytest.service;

/**
 * Created by ${zhiwei.xu} on 2017/6/23.
 */

public interface ISendCommondManager
{
    boolean sendDataToTarget(byte[] pktSeg, CommondCallbackImpl callback);
}
