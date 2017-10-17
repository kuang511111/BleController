package com.jrdcom.wearable.gowatch.factorytest.service;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.jrdcom.wearable.mb12m.minitest.R;
import com.jrdcom.wearable.smartband2.ble.BluetoothState;
import com.jrdcom.wearable.smartband2.cache.EntityCacheUtil;
import com.jrdcom.wearable.smartband2.entity.BtDeviceEntity;
import com.jrdcom.wearable.smartband2.entity.BtListEntity;
import com.jrdcom.wearable.smartband2.util.Constants;
import com.jrdcom.wearable.smartband2.util.LogUtil;

import java.util.List;


/**
 * Created by ${zhiwei.xu} on 2017/3/4.
 */

public class BleScannerManager extends Service
{
    private final String TAG = "BleScannerManager";
    private static final boolean V = false;
    private final static String action = BleScannerManager.class.getPackage() + ".";
    public static String ACTION_TO_START_SCAN_DEVICE = action + "ACTION_TO_START_SCAN_DEVICE";
    public static String ACTION_TO_STOP_SCAN_DEVICE = action + "ACTION_TO_STOP_SCAN_DEVICE";
    public static String EVENT_TARGET_DEVICE_ADDRESS = action + "EVENT_TARGET_DEVICE_ADDRESS";
    public static String PreMacAddress = "MB12-";
    private static int MAX_CONNECTED = 6;


    private Handler mHandle;
    private final int SCAN_INTERVAL = 20 * 1000;
    public static boolean isScanning = false;
    public static boolean isStopping = false;

    private BluetoothAdapter.LeScanCallback mLEScanCallback;

    private static List<BtDeviceEntity> macList;

    private HandlerThread mFilterHandlerThread;
    private Handler mFilterHandle;

    public static List<BtDeviceEntity> getMacList()
    {
        if (macList == null)
        {
            macList = EntityCacheUtil.get(new BtListEntity()).getBtDeviceEntities();
        }
        return macList;
    }

    public static void saveMacList()
    {
        EntityCacheUtil.putLocal(new BtListEntity(macList));
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }

    @Override
    public void onCreate()
    {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        LogUtil.d(TAG, "onStartCommand");
        MAX_CONNECTED = Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(this)
                .getString(getResources().getString(R.string.key_max_connect_value), "6"));

        if (intent == null)
        {
            return START_NOT_STICKY;
        }
        if (mHandle == null)
        {
            mHandle = new Handler();
        }
        if (mFilterHandlerThread == null || mFilterHandlerThread.getState() == Thread.State.TERMINATED)
        {
            mFilterHandlerThread = new HandlerThread("FilterThread");
            mFilterHandlerThread.start();
        }
        if (mFilterHandle == null)
        {
            mFilterHandle = new Handler(mFilterHandlerThread.getLooper())
            {
                @Override
                public void handleMessage(Message msg)
                {
                    super.handleMessage(msg);
                    int filterAction = msg.what;
                    if (filterAction == 101)
                    {
                        BtDeviceEntity btDeviceEntity = (BtDeviceEntity) msg.obj;
                        if (btDeviceEntity != null)
                        {
                            filterDevice(btDeviceEntity);
                        }
                    }

                }
            };
        }
        LogUtil.d(TAG, "BluetoothState.getInstance().getBleState() = " + BluetoothState.getInstance().getConnectState());
        if (!BluetoothAdapter.getDefaultAdapter().isEnabled())
        {
            LogUtil.i(TAG, "CONNECT_STATE_CONNECTED");
            return START_NOT_STICKY;
        }
        String action = intent.getAction();
        LogUtil.d(TAG, "onStartCommand action=" + action);

        if (ACTION_TO_START_SCAN_DEVICE.equals(action))
        {
            EntityCacheUtil.evictAll();
            getMacList();
            if (Build.VERSION.SDK_INT >= 23)
            {
                boolean gpsIsEnable = isLocationProviderEnabled(this);
                if (!gpsIsEnable)
                {
                    LogUtil.d(TAG, "sendBroadcast ACTION_NEED_OPEN_GPS");
                    sendBroadcast(new Intent(BluetoothState.ACTION_NEED_OPEN_GPS));
                }
            }
//            String address = intent.getStringExtra(EVENT_TARGET_DEVICE_ADDRESS);
//            if (TextUtils.isEmpty(address))
//            {
//                return START_NOT_STICKY;
//            }
            LogUtil.i(TAG, "ACTION_TO_START_SCAN_DEVICE");
            mHandle.removeCallbacks(mStartRunnable);
            mHandle.removeCallbacks(mStopRunnable);
            mStartRunnable.run();


        } else if (ACTION_TO_STOP_SCAN_DEVICE.equals(action))
        {
            LogUtil.i(TAG, "ACTION_TO_STOP_SCAN_DEVICE");
            mHandle.removeCallbacks(mStartRunnable);
            mHandle.removeCallbacks(mStopRunnable);
            mStopRunnable.run();
        }
        return super.onStartCommand(intent, flags, startId);
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


    private void startConnectBle(String mDeviceAddress)
    {

        LogUtil.d(TAG, "startConnectBle==============>>>>>>mDeviceAddress=" + mDeviceAddress);

    }

    @Override
    public void onDestroy()
    {
        LogUtil.i(TAG, "onDestroy");
        mHandle = null;
        mStopRunnable.run();
        super.onDestroy();
    }

    private StopRunnable mStopRunnable = new StopRunnable();
    private StartRunnable mStartRunnable = new StartRunnable();

    private class StopRunnable implements Runnable
    {
        @Override
        public void run()
        {
            if (mHandle == null)
            {
                return;
            }
            // TODO Auto-generated method stub
            if (V) LogUtil.v(TAG, "yxy this is stopRunnable");
            stopLeScan();
            mHandle.removeCallbacks(mStartRunnable);
            mHandle.removeCallbacks(mStopRunnable);
        }
    }

    private class StartRunnable implements Runnable
    {
        @Override
        public void run()
        {
            // TODO Auto-generated method stub
            if (mHandle == null)
            {
                return;
            }
            LogUtil.d(TAG, "yxy this is startRunnable");
            if (!BluetoothAdapter.getDefaultAdapter().isEnabled())
            {
                return;
            }
            startLeScan();
            mHandle.postDelayed(mStopRunnable, SCAN_INTERVAL);
        }
    }


    private void startBtScan()
    {
        if (V) LogUtil.v(TAG, "[MSG] reScan startBtScan");
        try
        {
            if (BluetoothAdapter.getDefaultAdapter().isEnabled())
            {
                BluetoothAdapter.getDefaultAdapter().startDiscovery();
                mHandle.postDelayed(() ->
                {
                    if (BluetoothAdapter.getDefaultAdapter().isDiscovering())
                    {
                        BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                        LogUtil.d(TAG, "cancelDiscovery");
                    }
                }, 2000);
            }

        } catch (NullPointerException e)
        {
            LogUtil.w(TAG, "", e);
        }
    }

    private void startLeScan()
    {
        LogUtil.d(TAG, "[MSG] reLEScan");
        try
        {
            final BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
            if (BluetoothAdapter.getDefaultAdapter().isEnabled())
            {
                stopLeScan();
                isScanning = true;
//                startBtScan();
                mLEScanCallback = new LeScanCallback();
                BluetoothAdapter.getDefaultAdapter().startLeScan(mLEScanCallback);
            }

        } catch (NullPointerException e)
        {
            LogUtil.w(TAG, "", e);
        }
    }

    private synchronized void stopLeScan()
    {
        LogUtil.d(TAG, "[MSG] stopLeScan");
        try
        {
            isScanning = false;
            BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
            if (adapter.isDiscovering())
            {
                adapter.cancelDiscovery();
                LogUtil.d(TAG, "cancelDiscovery");
            }
            if (mLEScanCallback != null)
            {
                adapter.stopLeScan(mLEScanCallback);
                LogUtil.d(TAG, "stopLeScan");
                mLEScanCallback = null;
            }
            isStopping = false;
        } catch (Exception e)
        {
            LogUtil.w(TAG, "", e);
        }
    }


    class LeScanCallback implements BluetoothAdapter.LeScanCallback
    {
        @Override
        public synchronized void onLeScan(BluetoothDevice device, int rssi, byte[] bytes)
        {
            try
            {
                LogUtil.i(TAG, "onScan device=" + device.getAddress() + " name=" + device.getName() + " rssi=" + rssi);
                BtDeviceEntity btDeviceEntity = new BtDeviceEntity();
                btDeviceEntity.setName(device.getName());
                btDeviceEntity.setMac(device.getAddress());
                btDeviceEntity.setCurrentRssi(rssi);
                Message message = Message.obtain();
                message.what = 101;
                message.obj = btDeviceEntity;
                if (mFilterHandle != null)
                {
                    mFilterHandle.sendMessage(message);
                }
            } catch (Exception e)
            {
                LogUtil.e(TAG, "", e);
            }
        }
    }


    private void filterDevice(BtDeviceEntity btDeviceEntity)
    {
        String deviceName = PreferenceManager.getDefaultSharedPreferences(this).getString(
                getResources().getString(R.string.key_device_name), getString(R.string.device_name));
        int currentSize = macList.size();
        if (macList.size() >= MAX_CONNECTED)
        {
            if (isStopping = false)
            {
                isStopping = true;
                mHandle.post(mStopRunnable);
            } else
            {
                return;
            }
        }
        LogUtil.d(TAG, "filterDevice currentSize=" + currentSize +" deviceName="+deviceName);
        if (!TextUtils.isEmpty(btDeviceEntity.getName())
                && !TextUtils.isEmpty(btDeviceEntity.getMac())
                && btDeviceEntity.getName().contains(deviceName)
                && btDeviceEntity.getCurrentRssi() > -80)
        {
            for (BtDeviceEntity bt : macList)
            {
                if (btDeviceEntity.getMac().equals(bt.getMac()))
                {
                    return;
                }
            }
            //debug mHandle.post(mStopRunnable);
            btDeviceEntity.setStatus(BtDeviceEntity.BT_DEVICE_STATUS_FOUND);
            macList.add(btDeviceEntity);
            EntityCacheUtil.putLocal(new BtListEntity(macList));
            LogUtil.d(TAG, "filterDevice macList.size()=" + macList.size());

            if (macList.size() > currentSize)
            {
                int index = macList.size();
//                if (index == 1)
//                {
//                    startBtService(0, macList.get(0).getMac());
//                } else if (index == 2)
//                {
//                    startBtService(1, macList.get(1).getMac());
//                } else if (index == 3)
//                {
//                    startBtService(2, macList.get(2).getMac());
//                } else if (index == 4)
//                {
//                    startBtService(3, macList.get(3).getMac());
//                } else if (index == 5)
//                {
//                    startBtService(4, macList.get(4).getMac());
//                } else if (index == 6)
//                {
//                    startBtService(7, macList.get(5).getMac());
//                } else if (index == 7)
//                {
//                    startBtService(6, macList.get(6).getMac());
//                } else if (index == 8)
//                {
//                    startBtService(7, macList.get(7).getMac());
//                }

                if (macList.size() >= MAX_CONNECTED)
                {
                    mHandle.post(mStopRunnable);
                }
                if (index == MAX_CONNECTED)
                {
                    if (MAX_CONNECTED >= 1)
                    {
                        mHandle.postDelayed(() -> startBtService(0, macList.get(0).getMac()), 1000);
                    }
                    if (MAX_CONNECTED >= 2)
                    {
                        mHandle.postDelayed(() -> startBtService(1, macList.get(1).getMac()), 2 * 1000);
                    }
                    if (MAX_CONNECTED >= 3)
                    {
                        mHandle.postDelayed(() -> startBtService(2, macList.get(2).getMac()), 3 * 1000);
                    }
                    if (MAX_CONNECTED >= 4)
                    {
                        mHandle.postDelayed(() -> startBtService(3, macList.get(3).getMac()), 4 * 1000);
                    }
                    if (MAX_CONNECTED >= 5)
                    {
                        mHandle.postDelayed(() -> startBtService(4, macList.get(4).getMac()), 5 * 1000);
                    }
                    if (MAX_CONNECTED >= 6)
                    {
                        mHandle.postDelayed(() -> startBtService(5, macList.get(5).getMac()), 6 * 1000);
                    }
                    if (MAX_CONNECTED >= 7)
                    {
                        mHandle.postDelayed(() -> startBtService(6, macList.get(6).getMac()), 7 * 1000);
                    }
                    if (MAX_CONNECTED >= 8)
                    {
                        mHandle.postDelayed(() -> startBtService(7, macList.get(7).getMac()), 8 * 1000);
                    }


                }
            }
        }
    }

    private void startBtService(int index, String mac)
    {
        if (index == 0)
        {
            Intent intent = new Intent(Constants.ACTION_BLE_CONNECT_SERVICE0);
            intent.setClass(BleScannerManager.this, BluetoothLeService.class);
            intent.putExtra(Constants.DATA_CONNECT_MAC_ADDRESS, mac);
            startService(intent);
        } else if (index == 1)
        {
            Intent intent = new Intent(Constants.ACTION_BLE_CONNECT_SERVICE1);
            intent.setClass(BleScannerManager.this, BluetoothLeService1.class);
            intent.putExtra(Constants.DATA_CONNECT_MAC_ADDRESS, mac);
            startService(intent);
        } else if (index == 2)
        {
            Intent intent = new Intent(Constants.ACTION_BLE_CONNECT_SERVICE2);
            intent.setClass(BleScannerManager.this, BluetoothLeService2.class);
            intent.putExtra(Constants.DATA_CONNECT_MAC_ADDRESS, mac);
            startService(intent);
        } else if (index == 3)
        {
            Intent intent = new Intent(Constants.ACTION_BLE_CONNECT_SERVICE3);
            intent.setClass(BleScannerManager.this, BluetoothLeService3.class);
            intent.putExtra(Constants.DATA_CONNECT_MAC_ADDRESS, mac);
            startService(intent);
        } else if (index == 4)
        {
            Intent intent = new Intent(Constants.ACTION_BLE_CONNECT_SERVICE4);
            intent.setClass(BleScannerManager.this, BluetoothLeService4.class);
            intent.putExtra(Constants.DATA_CONNECT_MAC_ADDRESS, mac);
            startService(intent);
        } else if (index == 5)
        {
            Intent intent = new Intent(Constants.ACTION_BLE_CONNECT_SERVICE5);
            intent.setClass(BleScannerManager.this, BluetoothLeService5.class);
            intent.putExtra(Constants.DATA_CONNECT_MAC_ADDRESS, mac);
            startService(intent);
        } else if (index == 6)
        {
            Intent intent = new Intent(Constants.ACTION_BLE_CONNECT_SERVICE6);
            intent.setClass(BleScannerManager.this, BluetoothLeService6.class);
            intent.putExtra(Constants.DATA_CONNECT_MAC_ADDRESS, mac);
            startService(intent);
        } else if (index == 7)
        {
            Intent intent = new Intent(Constants.ACTION_BLE_CONNECT_SERVICE7);
            intent.setClass(BleScannerManager.this, BluetoothLeService7.class);
            intent.putExtra(Constants.DATA_CONNECT_MAC_ADDRESS, mac);
            startService(intent);
        }


    }
}
