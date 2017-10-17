package com.jrdcom.wearable.gowatch.factorytest.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.jrdcom.wearable.gowatch.factorytest.CommonTestModel;
import com.jrdcom.wearable.gowatch.factorytest.service.BleScannerManager;
import com.jrdcom.wearable.gowatch.factorytest.service.ISendCommondManager;
import com.jrdcom.wearable.mb12m.minitest.R;
import com.jrdcom.wearable.smartband2.ble.CommandEnum;
import com.jrdcom.wearable.smartband2.entity.BtDeviceEntity;
import com.jrdcom.wearable.smartband2.entity.CommondEntity;
import com.jrdcom.wearable.smartband2.entity.ErrorSendException;
import com.jrdcom.wearable.smartband2.util.Constants;
import com.jrdcom.wearable.smartband2.util.LogUtil;
import com.jrdcom.wearable.smartband2.util.MyUtils;

import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;


public class ManualTestActivity extends Activity
{
    private String TAG = "ManualTestActivity";
    private BtDeviceEntity mBtDeviceEntity;
    private int mIndex = -1;
    private CommonTestModel commonTestModel;
    private final static int CHARGER_TIME_OUT = 101;
    private String pcbaValue;


    @BindView(R.id.bt_led_start)
    Button btLed;
    @BindView(R.id.bt_vib_start)
    Button btVib;
    @BindView(R.id.bt_charger_start)
    Button btCharger;
    @BindView(R.id.bt_anti_start)
    Button btAnti;
    @BindView(R.id.bt_set_pcba_start)
    Button btSetPcba;

    @BindView(R.id.content_led_test_result)
    TextView tvLedResult;
    @BindView(R.id.content_vib_test_result)
    TextView tvVibResult;
    @BindView(R.id.content_charger_test_result)
    TextView tvChargerResult;
    @BindView(R.id.content_anti_test_result)
    TextView tvAntiResult;
    @BindView(R.id.content_set_pcba_test_result)
    TextView tvSetPcbaResult;

    @BindView(R.id.item_led_test)
    LinearLayout liLed;
    @BindView(R.id.item_vib_test)
    LinearLayout liVib;
    @BindView(R.id.item_charger_test)
    LinearLayout liCharger;
    @BindView(R.id.item_anti_test)
    LinearLayout liAnti;
    @BindView(R.id.item_set_pcba_test)
    LinearLayout liSetpcba;


    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            String action = intent.getAction();
            LogUtil.d(TAG, "action=" + action);
            if (Constants.ACTION_BLE_DATA_AVAILABLE == action)
            {
                int index = intent.getIntExtra(Constants.DATA_CONNECT_LIST_INDEX, -1);
                String mac = intent.getStringExtra(Constants.DATA_CONNECT_MAC_ADDRESS);
                LogUtil.d(TAG, "index=" + index + " mac=" + mac);
                CommondEntity commondEntity = intent.getParcelableExtra(Constants.DATA_EXTRA_INFO);
                if (index == mIndex && mac.equals(mBtDeviceEntity.getMac()) && commondEntity != null)
                {
                    if (commondEntity.getmKey() == CommandEnum.FACTORY_TEST_CHARGER.getKeyId() &&
                            commondEntity.getmModle() == CommandEnum.FACTORY_TEST_CHARGER.getModelId())
                    {
                        testChargerSuccess();
                    }
                }
            }
        }
    };


    private Handler mHandle = new Handler()
    {
        @Override
        public void handleMessage(Message msg)
        {
            super.handleMessage(msg);
            int what = msg.what;
            if (CHARGER_TIME_OUT == what)
            {
                testChargerFailed();
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manual_test);
        ButterKnife.bind(this);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Constants.ACTION_BLE_DATA_AVAILABLE);
        this.registerReceiver(mBroadcastReceiver, intentFilter);


        LogUtil.d(TAG, "onCreate=======>");
        Intent intent = getIntent();
        if (intent != null)
        {
            mIndex = intent.getIntExtra(Constants.DATA_CONNECT_LIST_INDEX, -1);
        }
        if (mIndex == -1)
        {
            finish();
            return;
        }
        List<BtDeviceEntity> btDeviceEntities = BleScannerManager.getMacList();
        if (btDeviceEntities != null && btDeviceEntities.size() > mIndex)
        {
            mBtDeviceEntity = btDeviceEntities.get(mIndex);
        }
        if (mBtDeviceEntity == null)
        {
            finish();
            return;
        }
        LogUtil.d(TAG, "mBtDeviceEntity=" + mBtDeviceEntity);
        initView();
        ISendCommondManager sendCommondManager = MyUtils.getSendCommondManager(mIndex);
        if (sendCommondManager == null)
        {
            finish();
            return;
        }
        pcbaValue = PreferenceManager.getDefaultSharedPreferences(this).getString(
                getResources().getString(R.string.key_set_pcba), getString(R.string.pref_set_pcba_value));
//        liSetpcba.setVisibility(View.GONE);
        commonTestModel = new CommonTestModel(sendCommondManager);


    }

    @Override
    protected void onDestroy()
    {
        this.unregisterReceiver(mBroadcastReceiver);
        super.onDestroy();
    }

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

    @OnClick(R.id.bt_vib_start)
    void clickVibTest()
    {
        LogUtil.d(TAG, "clickVibTest");
        commonTestModel.testVibOpen()
                .subscribeOn(Schedulers.io())
                .delay(1000, TimeUnit.MILLISECONDS)
                .flatMap(entity ->
                {
                    LogUtil.d(TAG, "clickVibTest testVibOpen");
                    if (null == entity || !entity.isAck())
                    {
                        return Observable.error(new ErrorSendException());
                    } else
                    {
                        mBtDeviceEntity.setVibTestOk(true);
                        return commonTestModel.testVibClose();
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
                        LogUtil.d(TAG, "clickVibTest onError");
                        tvVibResult.setText(getString(R.string.test_fail));
                        liVib.setBackgroundColor(Color.parseColor("#9f1608"));
                    }

                    @Override
                    public void onNext(CommondEntity entity)
                    {
                        LogUtil.d(TAG, "clickVibTest onNext testVibClose");
                        if (null == entity || !entity.isAck())
                        {
                            mBtDeviceEntity.setVibTestOk(false);
                        } else
                        {
                            mBtDeviceEntity.setVibTestOk(true);
                        }
                        BleScannerManager.getMacList().set(mIndex, mBtDeviceEntity);
                        BleScannerManager.saveMacList();
                        if (mBtDeviceEntity.isVibTestOk())
                        {
                            tvVibResult.setText(getString(R.string.test_success));
                            liVib.setBackgroundColor(Color.parseColor("#00f91d"));
                        } else
                        {
                            tvVibResult.setText(getString(R.string.test_fail));
                            liVib.setBackgroundColor(Color.parseColor("#9f1608"));
                        }
                    }
                });

    }

    @OnClick(R.id.bt_charger_start)
    void clickChargerTest()
    {
        mHandle.sendEmptyMessageDelayed(CHARGER_TIME_OUT, 30 * 1000);
        tvChargerResult.setText("Wait...");
        Toast.makeText(this, "Please insert the charger!", Toast.LENGTH_SHORT).show();

    }

    @OnClick(R.id.bt_set_pcba_start)
    void clickSetPcbaTest()
    {
        LogUtil.d(TAG, "clickSetPcbaTest");
        commonTestModel.testSetPcba(pcbaValue)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<CommondEntity>()
                {
                    @Override
                    public void onCompleted()
                    {}

                    @Override
                    public void onError(Throwable e)
                    {
                        LogUtil.d(TAG, "clickSetPcbaTest onError");
                        tvSetPcbaResult.setText(getString(R.string.test_fail));
                        liSetpcba.setBackgroundColor(Color.parseColor("#9f1608"));
                    }

                    @Override
                    public void onNext(CommondEntity entity)
                    {
                        LogUtil.d(TAG, "clickSetPcbaTest onNext testSetPcba");
                        if (null == entity || !entity.isAck())
                        {
                            tvSetPcbaResult.setText(getString(R.string.test_success));
                            liSetpcba.setBackgroundColor(Color.parseColor("#00f91d"));
                        } else
                        {
                            tvSetPcbaResult.setText(getString(R.string.test_success));
                            liSetpcba.setBackgroundColor(Color.parseColor("#00f91d"));
                        }
                    }
                });

    }

    private void testChargerSuccess()
    {
        mHandle.removeMessages(CHARGER_TIME_OUT);
        mBtDeviceEntity.setChargerTestOk(true);
        BleScannerManager.getMacList().set(mIndex, mBtDeviceEntity);
        BleScannerManager.saveMacList();

        tvChargerResult.setText(getString(R.string.test_success));
        liCharger.setBackgroundColor(Color.parseColor("#00f91d"));

    }

    private void testChargerFailed()
    {
        tvChargerResult.setText(getString(R.string.test_fail));
        liCharger.setBackgroundColor(Color.parseColor("#9f1608"));
    }

    @OnClick(R.id.bt_anti_start)
    void clickAntiTest()
    {
        commonTestModel.testAntiActive()
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
                        LogUtil.d(TAG, "clickVibTest onError");
                        tvAntiResult.setText(getString(R.string.test_fail));
                        liAnti.setBackgroundColor(Color.parseColor("#9f1608"));
                        finishTest();
                    }

                    @Override
                    public void onNext(CommondEntity entity)
                    {
                        LogUtil.d(TAG, "clickVibTest onNext testVibClose");
                        if (null == entity || !entity.isAck())
                        {
                            mBtDeviceEntity.setAntiTestOk(false);
                        } else
                        {
                            mBtDeviceEntity.setAntiTestOk(true);
                        }
//                BtListEntity btListEntity = EntityCacheUtil.get(new BtListEntity());
//                btListEntity.getBtDeviceEntities().set(mIndex, mBtDeviceEntity);
//                EntityCacheUtil.putLocal(btListEntity);
                        if (mBtDeviceEntity.isAntiTestOk())
                        {
                            tvAntiResult.setText(getString(R.string.test_success));
                            liAnti.setBackgroundColor(Color.parseColor("#00f91d"));
                        } else
                        {
                            tvAntiResult.setText(getString(R.string.test_fail));
                            liAnti.setBackgroundColor(Color.parseColor("#9f1608"));
                        }
                        finishTest();
                    }
                });
    }

    private void finishTest()
    {
        LogUtil.d(TAG, "finishTest");
        Intent intent = new Intent(Constants.ACTION_BLE_DISCONNECT_SERVICE + mIndex);
        this.sendBroadcast(intent);
        int testCount = checkTestResult();
        if (testCount == 9)
        {
            mBtDeviceEntity.setStatus(BtDeviceEntity.BT_DEVICE_STATUS_ALL_VAL_FINISH);
            BleScannerManager.getMacList().set(mIndex, mBtDeviceEntity);
            BleScannerManager.saveMacList();
            AlertDialog alertDialog = new AlertDialog.Builder(ManualTestActivity.this)
                    .setTitle("Success")
                    .setMessage("All tests passed!")
                    .setPositiveButton("Finished", (dialog, which) ->
                    {
                        dialog.dismiss();
                        finish();
                    }).show();
        } else
        {
            mBtDeviceEntity.setStatus(BtDeviceEntity.BT_DEVICE_STATUS_ALL_VAL_FINISH);
            BleScannerManager.getMacList().set(mIndex, mBtDeviceEntity);
            BleScannerManager.saveMacList();
            AlertDialog alertDialog = new AlertDialog.Builder(ManualTestActivity.this)
                    .setTitle("Failed")
                    .setMessage((9 - testCount) + " tests failed!")
                    .setPositiveButton("Finished", (dialog, which) ->
                    {
                        dialog.dismiss();
                        finish();
                    }).show();

        }
    }

    int checkTestResult()
    {
        LogUtil.d(TAG, "btDeviceEntity=" + mBtDeviceEntity);
        int passCount = 0;
        if (mBtDeviceEntity.isBtTestOk())
        {
            passCount++;
        }
        if (mBtDeviceEntity.getVersionEntity() != null)
        {
            if (!TextUtils.isEmpty(mBtDeviceEntity.getVersionEntity().mainCodeVersion))
            {
                passCount++;
            }
        }
        if (!TextUtils.isEmpty(mBtDeviceEntity.getPcbaVersion()))
        {
            passCount++;
        }
        if (mBtDeviceEntity.isGsensorOk())
        {
            passCount++;
        }
        if (mBtDeviceEntity.isWriteFlagOk())
        {
            passCount++;
        }
        if (mBtDeviceEntity.isLedTestOk())
        {
            passCount++;
        }
        if (mBtDeviceEntity.isVibTestOk())
        {
            passCount++;
        }
        if (mBtDeviceEntity.isChargerTestOk())
        {
            passCount++;
        }
        if (mBtDeviceEntity.isAntiTestOk())
        {
            passCount++;
        }

        return passCount;
    }

    @OnClick(R.id.test_finish)
    void clickBtPass()
    {
        finishTest();
    }

    private void initView()
    {


    }


}
