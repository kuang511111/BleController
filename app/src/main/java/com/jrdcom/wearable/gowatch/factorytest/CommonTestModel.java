package com.jrdcom.wearable.gowatch.factorytest;

import android.text.TextUtils;

import com.jrdcom.wearable.gowatch.factorytest.service.CommondCallbackImpl;
import com.jrdcom.wearable.gowatch.factorytest.service.ISendCommondManager;
import com.jrdcom.wearable.smartband2.ble.BleCommand;
import com.jrdcom.wearable.smartband2.ble.CommandEnum;
import com.jrdcom.wearable.smartband2.entity.BtDeviceEntity;
import com.jrdcom.wearable.smartband2.entity.CommondEntity;
import com.jrdcom.wearable.smartband2.entity.ErrorSendException;
import com.jrdcom.wearable.smartband2.entity.VersionEntity;
import com.jrdcom.wearable.smartband2.util.LogUtil;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by ${zhiwei.xu} on 2017/6/23.
 */

public class CommonTestModel
{
    private final static String TAG = "CommonTestModel";
    private ISendCommondManager iSendCommondManager;

    public CommonTestModel(ISendCommondManager iSendCommondManager)
    {
        this.iSendCommondManager = iSendCommondManager;
    }

    public Observable<BtDeviceEntity> startAutoTest(BtDeviceEntity btDeviceEntity)
    {
        LogUtil.d(TAG, "startAutoTest");
        return Observable.create(subscriber ->
        {
            testBtStart()
                    .subscribeOn(Schedulers.io())
                    .flatMap(entity ->
                    {
                        LogUtil.d(TAG, "startAutoTest testBtStart");
                        if (null == entity || !entity.isAck())
                        {
                            return Observable.error(new ErrorSendException());
                        } else
                        {
                            btDeviceEntity.setBtTestOk(true);
                            return testVersion();
                        }
                    })
                    .flatMap(entity ->
                    {
                        LogUtil.d(TAG, "startAutoTest testVersion");
                        if (null == entity || entity.getData() == null)
                        {
                            return Observable.error(new ErrorSendException());
                        } else
                        {
                            String ver = new String(entity.getData()).trim();
                            VersionEntity versionEntity = new VersionEntity(ver);
                            btDeviceEntity.setVersionEntity(versionEntity);
                            return testPcba();
                        }
                    })
                    .flatMap(entity ->
                    {
                        LogUtil.d(TAG, "startAutoTest testPcba");
                        if (null == entity || entity.getData() == null)
                        {
                            return Observable.error(new ErrorSendException());
                        } else
                        {
                            String ver = new String(entity.getData()).trim();
                            if (!TextUtils.isEmpty(ver) && ver.length() > 15)
                            {
                                ver = ver.substring(0,15);
                            }
                            btDeviceEntity.setPcbaVersion(ver);
                            return testWriteFlag();
                        }
                    })
                    .flatMap(entity ->
                    {
                        LogUtil.d(TAG, "startAutoTest testWriteFlag");
                        if (null == entity || !entity.isAck())
                        {
                            return Observable.error(new ErrorSendException());
                        } else
                        {
                            btDeviceEntity.setWriteFlagOk(true);
                            return testGsensor();
                        }
                    })
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Subscriber<CommondEntity>()
                    {
                        @Override
                        public void onCompleted()
                        {
                            LogUtil.d(TAG, "startAutoTest onCompleted");
                        }

                        @Override
                        public void onError(Throwable e)
                        {
                            LogUtil.d(TAG, "startAutoTest onError");

                            subscriber.onError(e);
                            subscriber.onCompleted();
                        }

                        @Override
                        public void onNext(CommondEntity entity)
                        {
                            LogUtil.d(TAG, "startAutoTest onNext");
                            LogUtil.d(TAG, "startAutoTest testGsensor");
                            btDeviceEntity.setGsensorOk(true);
                            subscriber.onNext(btDeviceEntity);
                            subscriber.onCompleted();
                        }
                    });
        });
    }


    public Observable<CommondEntity> testVersion()
    {
        LogUtil.d(TAG, "testVersion");

        return Observable.create(subscriber ->
        {
            byte[] getVersionValue = BleCommand.createCommondPacket(CommandEnum.FACTORY_TEST_GET_VER_INFO, null);
            CommondCallbackImpl callback = new CommondCallbackImpl(CommandEnum.FACTORY_TEST_GET_VER_INFO)
            {
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
                    subscriber.onError(new ErrorSendException(errorMessage, errorCode));
                    subscriber.onCompleted();

                }

                @Override
                public void onResponse(CommondEntity entity)
                {
                    subscriber.onNext(entity);
                    subscriber.onCompleted();
                }
            };
            iSendCommondManager.sendDataToTarget(getVersionValue, callback);
        });

    }

    public Observable<CommondEntity> testPcba()
    {
        LogUtil.d(TAG, "testPcba");

        return Observable.create(subscriber ->
        {
            byte[] getVersionValue = BleCommand.createCommondPacket(CommandEnum.FACTORY_TEST_GET_PCBA, null);
            CommondCallbackImpl callback = new CommondCallbackImpl(CommandEnum.FACTORY_TEST_GET_PCBA)
            {
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
                    subscriber.onError(new ErrorSendException(errorMessage, errorCode));
                    subscriber.onCompleted();

                }

                @Override
                public void onResponse(CommondEntity entity)
                {
                    subscriber.onNext(entity);
                    subscriber.onCompleted();

                }
            };
            iSendCommondManager.sendDataToTarget(getVersionValue, callback);
        });

    }

    public Observable<CommondEntity> testWriteFlag()
    {
        LogUtil.d(TAG, "testWriteFlag");

        return Observable.create(subscriber ->
        {
            byte[] getVersionValue = BleCommand.createCommondPacket(CommandEnum.FACTORY_TEST_WRITE_FLAG, new byte[]{0x01});
            CommondCallbackImpl callback = new CommondCallbackImpl(CommandEnum.FACTORY_TEST_WRITE_FLAG)
            {
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
                    subscriber.onError(new ErrorSendException(errorMessage, errorCode));
                    subscriber.onCompleted();

                }

                @Override
                public void onResponse(CommondEntity entity)
                {
                    subscriber.onNext(new CommondEntity(true));
                    subscriber.onCompleted();

                }
            };
            iSendCommondManager.sendDataToTarget(getVersionValue, callback);
        });

    }

    public Observable<CommondEntity> testBtStart()
    {
        LogUtil.d(TAG, "testBtStart");

        return Observable.create(subscriber ->
        {
            byte[] getVersionValue = BleCommand.createCommondPacket(CommandEnum.FACTORY_TEST_BT, new byte[]{0x01});
            CommondCallbackImpl callback = new CommondCallbackImpl(CommandEnum.FACTORY_TEST_BT)
            {
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
                    subscriber.onError(new ErrorSendException(errorMessage, errorCode));
                    subscriber.onCompleted();

                }

                @Override
                public void onResponse(CommondEntity entity)
                {
                    subscriber.onNext(new CommondEntity(true));
                    subscriber.onCompleted();

                }
            };
            iSendCommondManager.sendDataToTarget(getVersionValue, callback);
        });

    }

    public Observable<CommondEntity> testGsensor()
    {
        LogUtil.d(TAG, "testBtStart");

        return Observable.create(subscriber ->
        {
            byte[] getVersionValue = BleCommand.createCommondPacket(CommandEnum.FACTORY_TEST_G_SENSOR, new byte[]{0x01});
            CommondCallbackImpl callback = new CommondCallbackImpl(CommandEnum.FACTORY_TEST_G_SENSOR)
            {
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
                    subscriber.onError(new ErrorSendException(errorMessage, errorCode));
                    subscriber.onCompleted();

                }

                @Override
                public void onResponse(CommondEntity entity)
                {
                    subscriber.onNext(new CommondEntity(true));
                    subscriber.onCompleted();

                }
            };
            iSendCommondManager.sendDataToTarget(getVersionValue, callback);
        });

    }

    public Observable<CommondEntity> testLedOpen()
    {
        LogUtil.d(TAG, "testLedOpen");

        return Observable.create(subscriber ->
        {
            byte[] getVersionValue = BleCommand.createCommondPacket(CommandEnum.FACTORY_TEST_LED_OPEN, null);
            CommondCallbackImpl callback = new CommondCallbackImpl(CommandEnum.FACTORY_TEST_LED_OPEN)
            {
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
                    subscriber.onError(new ErrorSendException(errorMessage, errorCode));
                    subscriber.onCompleted();

                }

                @Override
                public void onResponse(CommondEntity entity)
                {
                    subscriber.onNext(new CommondEntity(true));
                    subscriber.onCompleted();

                }
            };
            iSendCommondManager.sendDataToTarget(getVersionValue, callback);
        });

    }

    public Observable<CommondEntity> testLedClose()
    {
        LogUtil.d(TAG, "testLedClose");

        return Observable.create(subscriber ->
        {
            byte[] getVersionValue = BleCommand.createCommondPacket(CommandEnum.FACTORY_TEST_LED_CLOSE, null);
            CommondCallbackImpl callback = new CommondCallbackImpl(CommandEnum.FACTORY_TEST_LED_CLOSE)
            {
                @Override
                public void onSuccess()
                {}

                @Override
                public void onAckSuccess()
                {}

                @Override
                public void onFailed(int errorCode, String errorMessage)
                {
                    subscriber.onError(new ErrorSendException(errorMessage, errorCode));
                    subscriber.onCompleted();
                }

                @Override
                public void onResponse(CommondEntity entity)
                {
                    subscriber.onNext(new CommondEntity(true));
                    subscriber.onCompleted();
                }
            };
            iSendCommondManager.sendDataToTarget(getVersionValue, callback);
        });
    }

    public Observable<CommondEntity> testVibOpen()
    {
        LogUtil.d(TAG, "testVibOpen");

        return Observable.create(subscriber ->
        {
            byte[] getVersionValue = BleCommand.createCommondPacket(CommandEnum.FACTORY_TEST_VIB_OPEN, null);
            CommondCallbackImpl callback = new CommondCallbackImpl(CommandEnum.FACTORY_TEST_VIB_OPEN)
            {
                @Override
                public void onSuccess()
                {}

                @Override
                public void onAckSuccess()
                {}

                @Override
                public void onFailed(int errorCode, String errorMessage)
                {
                    subscriber.onError(new ErrorSendException(errorMessage, errorCode));
                    subscriber.onCompleted();
                }

                @Override
                public void onResponse(CommondEntity entity)
                {
                    subscriber.onNext(new CommondEntity(true));
                    subscriber.onCompleted();
                }
            };
            iSendCommondManager.sendDataToTarget(getVersionValue, callback);
        });
    }

    public Observable<CommondEntity> testVibClose()
    {
        LogUtil.d(TAG, "testVibClose");

        return Observable.create(subscriber ->
        {
            byte[] getVersionValue = BleCommand.createCommondPacket(CommandEnum.FACTORY_TEST_VIB_CLOSE, null);
            CommondCallbackImpl callback = new CommondCallbackImpl(CommandEnum.FACTORY_TEST_VIB_CLOSE)
            {
                @Override
                public void onSuccess()
                {}

                @Override
                public void onAckSuccess()
                {}

                @Override
                public void onFailed(int errorCode, String errorMessage)
                {
                    subscriber.onError(new ErrorSendException(errorMessage, errorCode));
                    subscriber.onCompleted();
                }

                @Override
                public void onResponse(CommondEntity entity)
                {
                    subscriber.onNext(new CommondEntity(true));
                    subscriber.onCompleted();
                }
            };
            iSendCommondManager.sendDataToTarget(getVersionValue, callback);
        });
    }

    public Observable<CommondEntity> testAntiActive()
    {
        LogUtil.d(TAG, "testAntiActive");

        return Observable.create(subscriber ->
        {
            byte[] getVersionValue = BleCommand.createCommondPacket(CommandEnum.FACTORY_TEST_ANTI, null);
            CommondCallbackImpl callback = new CommondCallbackImpl(CommandEnum.FACTORY_TEST_ANTI)
            {
                @Override
                public void onSuccess()
                {}

                @Override
                public void onAckSuccess()
                {}

                @Override
                public void onFailed(int errorCode, String errorMessage)
                {
                    subscriber.onError(new ErrorSendException(errorMessage, errorCode));
                    subscriber.onCompleted();
                }

                @Override
                public void onResponse(CommondEntity entity)
                {
                    subscriber.onNext(new CommondEntity(true));
                    subscriber.onCompleted();
                }
            };
            iSendCommondManager.sendDataToTarget(getVersionValue, callback);
        });
    }

    public Observable<CommondEntity> testSetPcba(String pcba)
    {
        LogUtil.d(TAG, "testAntiActive");
        return Observable.create(subscriber ->
        {
            byte[] getVersionValue = BleCommand.createCommondPacket(CommandEnum.FACTORY_TEST_SET_PCBA, pcba.getBytes());
            CommondCallbackImpl callback = new CommondCallbackImpl(CommandEnum.FACTORY_TEST_SET_PCBA)
            {
                @Override
                public void onSuccess()
                {}

                @Override
                public void onAckSuccess()
                {}

                @Override
                public void onFailed(int errorCode, String errorMessage)
                {
                    subscriber.onError(new ErrorSendException(errorMessage, errorCode));
                    subscriber.onCompleted();
                }

                @Override
                public void onResponse(CommondEntity entity)
                {
                    subscriber.onNext(new CommondEntity(true));
                    subscriber.onCompleted();
                }
            };
            iSendCommondManager.sendDataToTarget(getVersionValue, callback);
        });
    }


}
