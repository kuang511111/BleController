package com.jrdcom.wearable.gowatch.factorytest.activity;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v4.content.PermissionChecker;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.jrdcom.wearable.gowatch.factorytest.service.BleScannerManager;
import com.jrdcom.wearable.mb12m.minitest.R;
import com.jrdcom.wearable.smartband2.WearableApplication;
import com.jrdcom.wearable.smartband2.ble.BluetoothState;
import com.jrdcom.wearable.smartband2.cache.EntityCacheUtil;
import com.jrdcom.wearable.smartband2.entity.BtDeviceEntity;
import com.jrdcom.wearable.smartband2.util.BeepManager;
import com.jrdcom.wearable.smartband2.util.Constants;
import com.jrdcom.wearable.smartband2.util.LogUtil;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class AutoTestMainActivity extends Activity
{
    private String TAG = "AutoTestMainAct";
    private static final int EVENT_TIME_OUT_SERACH = 101;
    private static final int TIME_OUT_SERACH_ = 30 * 1000;


    private BluetoothAdapter mBluetoothAdapter;
    private static final int REQUEST_ENABLE_BT = 1;
    private List<BtDeviceEntity> mBtDeviceEntities;

    @BindView(R.id.device_list_recycler_view)
    RecyclerView recyclerView;
    BtListAdapter btListAdapter;
    private String romVersion;

    private WorkHandle mHandle;

    private ProgressDialog mProgressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_open_bluetooth);
        ButterKnife.bind(this);
        LogUtil.d(TAG, "onCreate=======>");

        romVersion = PreferenceManager.getDefaultSharedPreferences(this).getString(
                getResources().getString(R.string.key_rom_version), getString(R.string.pref_rom_version_value));

        // 初始化 Bluetooth adapter, 通过蓝牙管理器得到一个参考蓝牙适配器(API必须在以上android4.3或以上和版本)
        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        mHandle = new WorkHandle();


        // 检查设备上是否支持蓝牙
        if (mBluetoothAdapter == null)
        {
            Toast.makeText(this, R.string.error_bluetooth_not_supported, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        //注册广播
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());

        // 为了确保设备上蓝牙能使用, 如果当前蓝牙设备没启用,弹出对话框向用户要求授予权限来启用
        if (!mBluetoothAdapter.isEnabled())
        {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            return;
        }
        EntityCacheUtil.evictAll();
        mBtDeviceEntities = BleScannerManager.getMacList();
        mBtDeviceEntities.clear();

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        btListAdapter = new BtListAdapter(this);
        recyclerView.setAdapter(btListAdapter);
        startConnectDevice();

    }

    @Override
    protected void onResume()
    {
        super.onResume();
        LogUtil.d(TAG, "onResume");
        if (btListAdapter != null)
        {
            mBtDeviceEntities = BleScannerManager.getMacList();
            btListAdapter.notifyDataSetChanged();
        }
    }

    private void startConnectDevice()
    {
        LogUtil.d(TAG, "startConnectDevice");
        Intent intent = new Intent(this, BleScannerManager.class);
        intent.setAction(BleScannerManager.ACTION_TO_START_SCAN_DEVICE);
        startService(intent);

        showSearchingDialog();
        mHandle.sendEmptyMessageDelayed(EVENT_TIME_OUT_SERACH, 24 * 1000);

    }

    private void showSearchingDialog()
    {
        if (mProgressDialog == null)
        {
            mProgressDialog = ProgressDialog.show(this, null, getResources().getString(R.string.bt_searching));
        }
        mProgressDialog.show();
    }

    private void closeDialog()
    {
        if (mProgressDialog != null && mProgressDialog.isShowing())
        {
            mProgressDialog.dismiss();
        }
        if (mAlertDialog != null && mAlertDialog.isShowing())
        {
            mAlertDialog.dismiss();
        }
    }

    class WorkHandle extends Handler
    {
        @Override
        public void handleMessage(Message msg)
        {
            super.handleMessage(msg);
            int what = msg.what;
            if (EVENT_TIME_OUT_SERACH == what)
            {
                Toast.makeText(WearableApplication.getInstance(),"Found device failed!",Toast.LENGTH_LONG).show();
                closeDialog();
                finish();
            }
        }
    }

    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            final String action = intent.getAction();

            LogUtil.d(TAG, "action=" + action);

            //蓝牙是否打开
            if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action))
            {
                int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.STATE_OFF);
                if (state == BluetoothAdapter.STATE_ON)
                {
                    //蓝牙可用
                } else if (state == BluetoothAdapter.STATE_OFF)
                {
                    //蓝牙不可用
                }
            }
            //搜索到蓝牙
            if (Constants.ACTION_BLE_GATT_SERVICES_DISCOVERED.equals(action))
            {
//                发一个鸣声
                new BeepManager(AutoTestMainActivity.this).playBeepSoundAndVibrate();
                mBtDeviceEntities = BleScannerManager.getMacList();
//                mBtDeviceEntities.clear();
//                mBtDeviceEntities.addAll(btDeviceEntities);
                btListAdapter.notifyDataSetChanged();
                mHandle.removeMessages(EVENT_TIME_OUT_SERACH);
                closeDialog();

            } else if (Constants.ACTION_BLE_DATA_AVAILABLE.equals(action))
            {
                mBtDeviceEntities = BleScannerManager.getMacList();
//                mBtDeviceEntities.clear();
//                mBtDeviceEntities.addAll(btDeviceEntities);
                btListAdapter.notifyDataSetChanged();
            } else if (Constants.ACTION_BLE_AUTO_TEST_RESULT.equals(action))
            {
                mBtDeviceEntities = BleScannerManager.getMacList();
//                mBtDeviceEntities.clear();
//                mBtDeviceEntities.addAll(btDeviceEntities);
                btListAdapter.notifyDataSetChanged();
            } else if (BluetoothState.ACTION_NEED_OPEN_GPS.equals(action))
            {
                checkGPSisON();
            }
        }
    };

    private IntentFilter makeGattUpdateIntentFilter()
    {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);//蓝牙是否打开
        intentFilter.addAction(Constants.ACTION_BLE_GATT_SERVICES_DISCOVERED);//搜索到蓝牙
        intentFilter.addAction(Constants.ACTION_BLE_AUTO_TEST_RESULT);
        return intentFilter;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        // 用户选择不带开蓝牙，则关掉应用。
        if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_CANCELED)
        {
            finish();
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onDestroy()
    {
        LogUtil.d(TAG, "onDestroy");
        unregisterReceiver(mGattUpdateReceiver);
        closeDialog();
        for (int i = 0; i < 7; i++)
        {
            Intent intent = new Intent(Constants.ACTION_BLE_DISCONNECT_SERVICE + i);
            sendBroadcast(intent);
        }

        super.onDestroy();
    }


    private AlertDialog mAlertDialog;

    public void checkGPSisON()
    {
        LogUtil.d(TAG, "ble checkGPSisON");
        if (Build.VERSION.SDK_INT >= 23)
        {
            String permission = Manifest.permission.ACCESS_COARSE_LOCATION;
            if (mAlertDialog != null && mAlertDialog.isShowing())
            {
                mAlertDialog.cancel();
                mAlertDialog = null;
            }
            if (PermissionChecker.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED && isLocationProviderEnabled(this) == false)
            {
                //Toast.makeText(this, R.string.bt_scan_gps_prompt, Toast.LENGTH_LONG).show();
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("").setMessage(R.string.bt_scan_gps_prompt);
                builder.setNegativeButton(R.string.cancel_label, (dialog, which) ->
                {
                    //finish();
                    onBackPressed();
                });
                builder.setPositiveButton(R.string.str_set_bluetooth_open, (dialog, which) ->
                {
                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(intent);
                });

                mAlertDialog = builder.create();
                mAlertDialog.show();
            }
        }
    }

    public boolean isLocationProviderEnabled(final Context context)
    {
        try
        {
            LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            boolean gps = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            return gps;
        } catch (Exception e)
        {
            LogUtil.w(TAG, "", e);
        }
        return false;
    }

    @Override
    public void onBackPressed()
    {
        Intent intent = new Intent(this, BleScannerManager.class);
        intent.setAction(BleScannerManager.ACTION_TO_STOP_SCAN_DEVICE);
        startService(intent);
        super.onBackPressed();
    }


    class BtListAdapter extends RecyclerView.Adapter<BtItemHolder>
    {
        private Context context;

        public BtListAdapter(Context ctx)
        {
            this.context = ctx;
        }

        @Override
        public BtItemHolder onCreateViewHolder(ViewGroup parent, int viewType)
        {
            BtItemHolder holder = new BtItemHolder(LayoutInflater.from(
                    AutoTestMainActivity.this).inflate(R.layout.content_bluetooth_status, parent,
                    false));
//            ButterKnife.bind(holder, parent);
            return holder;
        }

        @Override
        public void onBindViewHolder(BtItemHolder holder, int position)
        {
            LogUtil.d(TAG, "onBindViewHolder position=" + position);
            holder.indexTextView.setText(position + 1 + "");
            BtDeviceEntity btDeviceEntity = mBtDeviceEntities.get(position);
            LogUtil.d(TAG, "btDeviceEntity=" + btDeviceEntity);
            int status = btDeviceEntity.getStatus();
            LogUtil.d(TAG, "status=" + status);
            holder.contentNameTextView.setText(btDeviceEntity.getName());
            holder.contentMacTextView.setText(btDeviceEntity.getMac());
            int allTestResult = 0;
            switch (status)
            {
                case BtDeviceEntity.BT_DEVICE_STATUS_IDEL:
                    holder.resultTextView.setText("Disconnected");
                    holder.linearLayoutItem.setBackgroundColor(Color.parseColor("#292421"));
                    break;
                case BtDeviceEntity.BT_DEVICE_STATUS_FOUND:
                    holder.resultTextView.setText("Connecting");
                    holder.linearLayoutItem.setBackgroundColor(Color.parseColor("#292421"));
                    break;
                case BtDeviceEntity.BT_DEVICE_STATUS_CONNECTED:
                    holder.resultTextView.setText("Connected");
                    holder.linearLayoutItem.setBackgroundColor(Color.parseColor("#292421"));
                    break;
                case BtDeviceEntity.BT_DEVICE_STATUS_DISCONNECTED:
                    holder.resultTextView.setText("Disconnected");
                    holder.linearLayoutItem.setBackgroundColor(Color.parseColor("#292421"));
                    break;
                case BtDeviceEntity.BT_DEVICE_STATUS_SERVICE_OK:
                    holder.resultTextView.setText("Get Ready");
                    holder.linearLayoutItem.setBackgroundColor(Color.parseColor("#292421"));
                    break;
                case BtDeviceEntity.BT_DEVICE_STATUS_AUTO_VAL_PASS:
                    int passCount = testDevice(mBtDeviceEntities.get(position));
                    if (passCount == 5)
                    {
                        holder.resultTextView.setText("Pass");
                        holder.linearLayoutItem.setBackgroundColor(Color.parseColor("#02a112"));
                    } else
                    {
                        holder.resultTextView.setText("" + (5 - passCount) + " Failed");
                        holder.linearLayoutItem.setBackgroundColor(Color.parseColor("#a10702"));
                    }
                    break;
                case BtDeviceEntity.BT_DEVICE_STATUS_ALL_VAL_FINISH:
                    allTestResult = checkAllTestResult(mBtDeviceEntities.get(position));
                    if (allTestResult == 9)
                    {
                        holder.resultTextView.setText("Finished Test");
                        holder.linearLayoutItem.setBackgroundColor(Color.parseColor("#FF8000"));
                    } else
                    {
                        holder.resultTextView.setText("" + (9 - allTestResult) + " Failed");
                        holder.linearLayoutItem.setBackgroundColor(Color.parseColor("#a10702"));
                    }
                    break;
                default:
                    break;
            }
        }


        @Override
        public int getItemCount()
        {
            return mBtDeviceEntities.size();
        }
    }


    private int testDevice(BtDeviceEntity btDeviceEntity)
    {
        LogUtil.d(TAG, "testDevice btDeviceEntity=" + btDeviceEntity);
        int passCount = 0;
        if (btDeviceEntity.isBtTestOk())
        {
            passCount++;
        }
        if (btDeviceEntity.getVersionEntity() != null)
        {
            if (!TextUtils.isEmpty(btDeviceEntity.getVersionEntity().mainCodeVersion)
                    && btDeviceEntity.getVersionEntity().mainCodeVersion.contains(romVersion))
            {
                passCount++;
            }
        }
        if (!TextUtils.isEmpty(btDeviceEntity.getPcbaVersion()))
        {
            passCount++;
        }
        if (btDeviceEntity.isGsensorOk())
        {
            passCount++;
        }
        if (btDeviceEntity.isWriteFlagOk())
        {
            passCount++;
        }
        return passCount;
    }

    private int checkAllTestResult(BtDeviceEntity mBtDeviceEntity)
    {
        LogUtil.d(TAG, "checkAllTestResult btDeviceEntity=" + mBtDeviceEntity);
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


    public class BtItemHolder extends RecyclerView.ViewHolder
    {
        @BindView(R.id.bt_content_layout)
        LinearLayout contentLayout;
        @BindView(R.id.bt_content_layout_item)
        LinearLayout linearLayoutItem;
        @BindView(R.id.bt_index)
        TextView indexTextView;
        @BindView(R.id.bt_content_name)
        TextView contentNameTextView;
        @BindView(R.id.bt_content_mac)
        TextView contentMacTextView;
        @BindView(R.id.bt_result)
        TextView resultTextView;

        public BtItemHolder(View view)
        {
            super(view);
            ButterKnife.bind(this, view);
        }

        @OnClick(R.id.bt_content_layout)
        void clickContentLayout()
        {
            int position = getAdapterPosition();
            LogUtil.d(TAG, "clickContentLayout position=" + position);
            mBtDeviceEntities = BleScannerManager.getMacList();
            if (mBtDeviceEntities != null && mBtDeviceEntities.size() > position)
            {
                BtDeviceEntity btDeviceEntity = mBtDeviceEntities.get(position);
                LogUtil.d(TAG, "clickContentLayout btDeviceEntity=" + btDeviceEntity);
                if (btDeviceEntity != null && (btDeviceEntity.getStatus() == BtDeviceEntity.BT_DEVICE_STATUS_CONNECTED
                        || btDeviceEntity.getStatus() == BtDeviceEntity.BT_DEVICE_STATUS_SERVICE_OK
                        || btDeviceEntity.getStatus() == BtDeviceEntity.BT_DEVICE_STATUS_AUTO_VAL_PASS))
                {
                    goToDetailShow(position);
                }
            }
        }

//        @OnClick(R.id.bt_content_layout_item)
//        void clickContentLayoutItem()
//        {
//            int position = getAdapterPosition();
//            LogUtil.d(TAG,"clickContentLayoutItem position="+position);
//            goToDetailShow(position);
//        }

    }

    private void goToDetailShow(int position)
    {
        Intent intent = new Intent(this, DetailShowActivity.class);
        intent.putExtra(Constants.DATA_CONNECT_LIST_INDEX, position);
        startActivity(intent);

    }

}
