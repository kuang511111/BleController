# BleController

1.这个应用支持一个手机同时连接多个ble设备<br>
2.连接并控制多个设备<br>
3.应用使用rxjava构建架构<br>
4.对接口进行封装，连接成功后，只需要直接调用接口就可以发送数据<br>

备注：目前发现一般android机最多一次性连接成功6个设备。<br>

详细：
BleScannerManager：这个类负责扫描过滤设备<br>

BluetoothLeService：这个服务负责建立连接，并传输数据。上报状态。<br>
每个连接保持一个这样的服务，避免数据冲突<br>

连接成功后，通过下面这个行数获取命令管理类，mIndex代表连接的索引<br>
ISendCommondManager sendCommondManager = MyUtils.getSendCommondManager(mIndex);<br>

然后我们可以实例化接口模块<br>
commonTestModel = new CommonTestModel(sendCommondManager);<br>
所有接口都封装在model里面，<br>
比如当用户点击app按钮控制设备led灯点亮时，只需要执行这一句。<br>
 commonTestModel.testLedOpen()<br>
 下面是一个联动逻辑<br>

    @OnClick(R.id.bt_led_start)
    void clickLedTest()
    {
        LogUtil.d(TAG, "clickLedTest");
        commonTestModel.testLedOpen()
                .subscribeOn(Schedulers.io())
                .delay(1000, TimeUnit.MILLISECONDS)
                .flatMap(entity ->
                {
                    LogUtil.d(TAG, "clickLedTest testLedOpen");
                    if (null == entity || !entity.isAck())
                    {
                        return Observable.error(new ErrorSendException());
                    } else
                    {
                        mBtDeviceEntity.setLedTestOk(true);
                        return commonTestModel.testLedClose();
                    }

                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<CommondEntity>()
                {
                    @Override
                    public void onCompleted()
                    {

                    }

                    @Override
                    public void onError(Throwable e)
                    {
                        LogUtil.d(TAG, "clickLedTest onError");
                        tvLedResult.setText(getString(R.string.test_fail));
                        liLed.setBackgroundColor(Color.parseColor("#9f1608"));
                    }

                    @Override
                    public void onNext(CommondEntity entity)
                    {
                        LogUtil.d(TAG, "clickLedTest onNext testLedClose");
                        if (null == entity || !entity.isAck())
                        {
                            mBtDeviceEntity.setLedTestOk(false);
                        } else
                        {
                            mBtDeviceEntity.setLedTestOk(true);
                        }
                        BleScannerManager.getMacList().set(mIndex, mBtDeviceEntity);
                        BleScannerManager.saveMacList();
                        if (mBtDeviceEntity.isLedTestOk())
                        {
                            tvLedResult.setText(getString(R.string.test_success));
                            liLed.setBackgroundColor(Color.parseColor("#00f91d"));
                        } else
                        {
                            tvLedResult.setText(getString(R.string.test_fail));
                            liLed.setBackgroundColor(Color.parseColor("#9f1608"));
                        }
                    }
                });

    }

