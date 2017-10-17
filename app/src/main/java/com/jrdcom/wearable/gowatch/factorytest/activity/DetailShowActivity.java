package com.jrdcom.wearable.gowatch.factorytest.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jrdcom.wearable.gowatch.factorytest.CommonTestModel;
import com.jrdcom.wearable.gowatch.factorytest.service.BleScannerManager;
import com.jrdcom.wearable.gowatch.factorytest.service.ISendCommondManager;
import com.jrdcom.wearable.mb12m.minitest.R;
import com.jrdcom.wearable.smartband2.entity.BtDeviceEntity;
import com.jrdcom.wearable.smartband2.util.Constants;
import com.jrdcom.wearable.smartband2.util.LogUtil;
import com.jrdcom.wearable.smartband2.util.MyUtils;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Subscriber;


public class DetailShowActivity extends Activity
{
    private String TAG = "DetailShowActivity";
    private BtDeviceEntity mBtDeviceEntity;
    private int mIndex = -1;
    private String romVersion;


    @BindView(R.id.content_bt_name)
    TextView mBtNameTv;
    @BindView(R.id.content_bt_mac)
    TextView mBtMacTv;
    @BindView(R.id.content_device_version)
    TextView mVersionTv;
    @BindView(R.id.content_device_cu)
    TextView mCuTv;
    @BindView(R.id.content_device_serial)
    TextView mSerialTv;
    @BindView(R.id.content_device_pcba)
    TextView mPcbaTv;
    @BindView(R.id.content_bt_test)
    TextView mBtTestTv;
    @BindView(R.id.content_gsensor_test)
    TextView mGsensorTv;
    @BindView(R.id.content_write_flag)
    TextView mWriteFlagTv;

    @BindView(R.id.item_bt_name)
    LinearLayout mBtNamelayout;
    @BindView(R.id.item_bt_mac)
    LinearLayout mBtMaclayout;
    @BindView(R.id.item_device_version)
    LinearLayout mVersionlayout;
    @BindView(R.id.item_device_cu)
    LinearLayout mCulayout;
    @BindView(R.id.item_device_serial)
    LinearLayout mSeriallayout;
    @BindView(R.id.item_device_pcba)
    LinearLayout mPcbalayout;
    @BindView(R.id.item_bt_test)
    LinearLayout mBtTestlayout;
    @BindView(R.id.item_gsensor_test)
    LinearLayout mGsensorlayout;
    @BindView(R.id.item_write_flag)
    LinearLayout mWriteFlaglayout;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_show);
        ButterKnife.bind(this);
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
        romVersion = PreferenceManager.getDefaultSharedPreferences(this).getString(
                getResources().getString(R.string.key_rom_version), getString(R.string.pref_rom_version_value));
        initView();
        ISendCommondManager sendCommondManager = MyUtils.getSendCommondManager(mIndex);
        CommonTestModel commonTestModel = new CommonTestModel(sendCommondManager);
        commonTestModel.startAutoTest(mBtDeviceEntity).subscribe(new Subscriber<BtDeviceEntity>()
        {
            @Override
            public void onCompleted()
            {
                initView();
            }

            @Override
            public void onError(Throwable e)
            {
                LogUtil.i(TAG, "startTest onError");
            }

            @Override
            public void onNext(BtDeviceEntity btDeviceEntity)
            {
                BleScannerManager.getMacList().set(mIndex, mBtDeviceEntity);
                BleScannerManager.saveMacList();
                LogUtil.i(TAG, "startTest btDeviceEntity=" + btDeviceEntity);
            }
        });


    }

    @OnClick(R.id.test_next)
    void clickNext()
    {
        Intent intent = new Intent(this, ManualTestActivity.class);
        intent.putExtra(Constants.DATA_CONNECT_LIST_INDEX, mIndex);
        startActivity(intent);
        finish();

    }

    private void initView()
    {
        if (!TextUtils.isEmpty(mBtDeviceEntity.getName()))
        {
            mBtNameTv.setText(mBtDeviceEntity.getName());
            mBtNamelayout.setBackgroundColor(Color.parseColor("#00f91d"));
        } else
        {
            mBtNameTv.setText(getString(R.string.test_fail));
            mBtNamelayout.setBackgroundColor(Color.parseColor("#9f1608"));

        }
        if (!TextUtils.isEmpty(mBtDeviceEntity.getName()))
        {
            mBtMacTv.setText(mBtDeviceEntity.getMac());
            mBtMaclayout.setBackgroundColor(Color.parseColor("#00f91d"));
        } else
        {
            mBtMacTv.setText(getString(R.string.test_fail));
            mBtMaclayout.setBackgroundColor(Color.parseColor("#9f1608"));

        }
        if (mBtDeviceEntity.getVersionEntity() != null)
        {
            if (!TextUtils.isEmpty(mBtDeviceEntity.getVersionEntity().mainCodeVersion)
                    && mBtDeviceEntity.getVersionEntity().mainCodeVersion.contains(romVersion))
            {
                mVersionTv.setText(mBtDeviceEntity.getVersionEntity().mainCodeVersion);
                mVersionlayout.setBackgroundColor(Color.parseColor("#00f91d"));
            } else
            {
                if (!TextUtils.isEmpty(mBtDeviceEntity.getVersionEntity().mainCodeVersion))
                {
                    mVersionTv.setText("Not Match!Current is:"+mBtDeviceEntity.getVersionEntity().mainCodeVersion);
                }else
                {
                    mVersionTv.setText(getString(R.string.test_fail));

                }
                mVersionlayout.setBackgroundColor(Color.parseColor("#9f1608"));

            }
            if (!TextUtils.isEmpty(mBtDeviceEntity.getVersionEntity().cuVersion))
            {
                mCuTv.setText(mBtDeviceEntity.getVersionEntity().cuVersion);
                mCulayout.setBackgroundColor(Color.parseColor("#00f91d"));
            } else
            {
                mCuTv.setText(getString(R.string.test_fail));
                mCulayout.setBackgroundColor(Color.parseColor("#9f1608"));
            }
            if (!TextUtils.isEmpty(mBtDeviceEntity.getVersionEntity().serialVersion))
            {
                mSerialTv.setText(mBtDeviceEntity.getVersionEntity().serialVersion);
                mSeriallayout.setBackgroundColor(Color.parseColor("#00f91d"));
            } else
            {
                mSerialTv.setText(getString(R.string.test_fail));
                mSeriallayout.setBackgroundColor(Color.parseColor("#9f1608"));

            }
        } else
        {
            mVersionTv.setText(getString(R.string.test_fail));
            mCuTv.setText(getString(R.string.test_fail));
            mSerialTv.setText(getString(R.string.test_fail));
            mVersionlayout.setBackgroundColor(Color.parseColor("#9f1608"));
            mCulayout.setBackgroundColor(Color.parseColor("#9f1608"));
            mSeriallayout.setBackgroundColor(Color.parseColor("#9f1608"));

        }
        if (!TextUtils.isEmpty(mBtDeviceEntity.getPcbaVersion()))
        {
            mPcbaTv.setText(mBtDeviceEntity.getPcbaVersion());
            mPcbalayout.setBackgroundColor(Color.parseColor("#00f91d"));
        } else
        {
            mPcbaTv.setText(getString(R.string.test_fail));
            mPcbalayout.setBackgroundColor(Color.parseColor("#9f1608"));
        }
        if (mBtDeviceEntity.isBtTestOk())
        {
            mBtTestTv.setText(getString(R.string.test_pass));
            mBtTestlayout.setBackgroundColor(Color.parseColor("#00f91d"));
        } else
        {
            mBtTestTv.setText(getString(R.string.test_fail));
            mBtTestlayout.setBackgroundColor(Color.parseColor("#9f1608"));
        }
        if (mBtDeviceEntity.isGsensorOk())
        {
            mGsensorTv.setText(getString(R.string.test_pass));
            mGsensorlayout.setBackgroundColor(Color.parseColor("#00f91d"));
        } else
        {
            mGsensorTv.setText(getString(R.string.test_fail));
            mGsensorlayout.setBackgroundColor(Color.parseColor("#9f1608"));

        }
        if (mBtDeviceEntity.isWriteFlagOk())
        {
            mWriteFlagTv.setText(getString(R.string.test_pass));
            mWriteFlaglayout.setBackgroundColor(Color.parseColor("#00f91d"));
        } else
        {
            mWriteFlagTv.setText(getString(R.string.test_fail));
            mWriteFlaglayout.setBackgroundColor(Color.parseColor("#9f1608"));
        }
    }


}
