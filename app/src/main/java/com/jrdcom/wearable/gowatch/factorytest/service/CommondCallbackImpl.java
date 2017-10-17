package com.jrdcom.wearable.gowatch.factorytest.service;

import com.jrdcom.wearable.smartband2.ble.CommandEnum;
import com.jrdcom.wearable.smartband2.entity.CommondEntity;

/**
 * Created by ${zhiwei.xu} on 2017/6/29.
 */

public class CommondCallbackImpl implements ICommandCallback
{
    public CommandEnum commandEnum;

    public CommondCallbackImpl(CommandEnum commandEnum)
    {
        this.commandEnum = commandEnum;
    }

    @Override
    public void onSuccess()
    {

    }

    @Override
    public void onAckSuccess()
    {

    }

    @Override
    public void onFailed(int errorCode, String errorMessage)
    {

    }

    @Override
    public void onResponse(CommondEntity entity)
    {

    }
}
