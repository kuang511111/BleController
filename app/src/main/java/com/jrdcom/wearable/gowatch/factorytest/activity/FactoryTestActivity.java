package com.jrdcom.wearable.gowatch.factorytest.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.jrdcom.wearable.mb12m.minitest.R;

public class FactoryTestActivity extends Activity
{
    private final static String TAG = "FactoryTestActivity";
    private Button mAutoButton;
    //    private Button mManuButton;
    private TextView MiniVersion;
    private final int WHAT_ENABLE_BT_TIME_OUT = 101;
    private final int WHAT_DISABLE_BT_TIME_OUT = 102;


    private BluetoothAdapter mBluetoothAdapter;
    private boolean mScanning;
    private Handler mHandler;
    private TextView mTestModelTv;
    private ProgressDialog progressDialog;
    private boolean isResetingOffBT = false;
    private boolean isResetingOnBT = false;

    private static final int REQUEST_ENABLE_BT = 1;
    // 10秒后停止查找搜索.
    private static final long SCAN_PERIOD = 10000;

    public static final String AUTO = "auto";

    public static String testModel = "0";


    private BroadcastReceiver mReceiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            String action = intent.getAction();
            if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action))
            {
                int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,
                        BluetoothAdapter.STATE_OFF);
                if (state == BluetoothAdapter.STATE_ON)
                {
                    //蓝牙可用
                    Toast.makeText(context, R.string.Bluetooth_Enabled, Toast.LENGTH_SHORT).show();
                    mAutoButton.setEnabled(true);
//                    mManuButton.setEnabled(true);
                    if (isResetingOnBT)
                    {
                        mHandler.removeMessages(WHAT_ENABLE_BT_TIME_OUT);
                        mHandler.postDelayed(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                startTest(true);

                            }
                        }, 1000);

                    }
                } else if (state == BluetoothAdapter.STATE_OFF)
                {
                    //蓝牙不可用
                    Toast.makeText(context, R.string.Bluetooth_Disabled, Toast.LENGTH_SHORT).show();
                    mAutoButton.setEnabled(false);
                    if (isResetingOffBT)
                    {
                        mHandler.removeMessages(WHAT_DISABLE_BT_TIME_OUT);
                        mHandler.postDelayed(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                startRestBT();

                            }
                        }, 1000);
                    }
//                    mManuButton.setEnabled(false);
                }
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_factorytest);

        mHandler = new Handler()
        {
            @Override
            public void handleMessage(Message msg)
            {
                super.handleMessage(msg);
                int what = msg.what;
                if (what == WHAT_ENABLE_BT_TIME_OUT)
                {
                    isResetingOnBT = false;
                    isResetingOffBT = false;
                    closeDialog();
                    Toast.makeText(FactoryTestActivity.this, "Open bluetooth failed!", Toast.LENGTH_SHORT).show();

                } else if (what == WHAT_DISABLE_BT_TIME_OUT)
                {
                    isResetingOnBT = false;
                    isResetingOffBT = false;
                    closeDialog();
                    Toast.makeText(FactoryTestActivity.this, "Close bluetooth failed!", Toast.LENGTH_SHORT).show();

                }

            }
        };

        // 检查当前手机是否支持ble 蓝牙,如果不支持退出程序
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE))
        {
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            finish();
        }

        // 初始化 Bluetooth adapter, 通过蓝牙管理器得到一个参考蓝牙适配器(API必须在以上android4.3或以上和版本)
        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        // 检查设备上是否支持蓝牙
        if (mBluetoothAdapter == null)
        {
            Toast.makeText(this, R.string.error_bluetooth_not_supported, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        mAutoButton = (Button) findViewById(R.id.test_auto);
//        mManuButton = (Button) findViewById(R.id.test_manual);

        MiniVersion = (TextView) findViewById(R.id.mini_version);
        MiniVersion.setText("MB12 v" + getAppVerion());

        mTestModelTv = (TextView) findViewById(R.id.mini_test_model);//add on 2017.2.11
        //注册一个广播，监听蓝牙是否可用。
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(mReceiver, filter);

        showSettingDialog();
    }

    //打开设置的对话面板
    private void showSettingDialog()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(FactoryTestActivity.this);
        builder.setMessage(R.string.dialog_config_setting);
        builder.setPositiveButton(getResources().getString(R.string.dialog_cu_ref_ok).toUpperCase(),
                (dialogInterface, i) ->
                {
                    Intent intent = new Intent(FactoryTestActivity.this, Settings.class);
                    startActivity(intent);
                });
        builder.setOnCancelListener(arg0 -> FactoryTestActivity.this.finish());
        builder.create().show();
    }

    private String getAppVerion()
    {
        String appVersion = new String();
        PackageManager manager = this.getPackageManager();
        try
        {
            PackageInfo info = manager.getPackageInfo(this.getPackageName(), 0);
            appVersion = info.versionName; // 版本名
        } catch (NameNotFoundException e)
        {
            e.printStackTrace();
        }
        return appVersion;
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        /*--------------- add start 2017.2.11 ---------------*/
        mTestModelTv.setText(getString(R.string.test_model_mmi));

        // 为了确保设备上蓝牙能使用, 如果当前蓝牙设备没启用,弹出对话框向用户要求授予权限来启用
        if (!mBluetoothAdapter.isEnabled())
        {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            return;
        }
    }


    @Override
    public void onStart()
    {
        super.onStart();
        if (!mBluetoothAdapter.isEnabled())
        {
            mAutoButton.setEnabled(false);
        }
    }

    @Override
    protected void onPause()
    {
        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.settings, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int id = item.getItemId();

        if (id == R.id.settings)
        {
            Intent intent = new Intent(this, Settings.class);
            startActivity(intent);
            return true;
        }
        return false;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
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
    public void onDestroy()
    {
        unregisterReceiver(mReceiver);
        closeDialog();
        super.onDestroy();
    }

    private void closeDialog()
    {
        if (progressDialog != null && progressDialog.isShowing())
        {
            progressDialog.dismiss();
        }
    }

    public void onClick(View view)
    {
        switch (view.getId())
        {
            case R.id.test_auto:
                startRestBT();
                break;
        }
    }

    private void startRestBT()
    {
        isResetingOnBT = false;
        isResetingOffBT = false;
        if (mBluetoothAdapter.isEnabled())
        {
            mBluetoothAdapter.disable();
            progressDialog = ProgressDialog.show(this, null, getResources().getString(R.string.bt_resetting));
            isResetingOffBT = true;
            mHandler.sendEmptyMessageDelayed(WHAT_DISABLE_BT_TIME_OUT, 5000);
        } else
        {
            mBluetoothAdapter.enable();
            isResetingOnBT = true;
            mHandler.sendEmptyMessageDelayed(WHAT_ENABLE_BT_TIME_OUT, 10000);
        }
    }

    //打开测试页面
    private void startTest(boolean auto)
    {
        closeDialog();
        Intent intent = new Intent(this, AutoTestMainActivity.class);
        intent.putExtra(AUTO, true);
        startActivity(intent);
    }

}
