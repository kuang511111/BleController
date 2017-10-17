package com.jrdcom.wearable.gowatch.factorytest.service;

import android.annotation.SuppressLint;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.jrdcom.wearable.gowatch.factorytest.CommonTestModel;
import com.jrdcom.wearable.smartband2.ble.NtfContext;
import com.jrdcom.wearable.smartband2.entity.BtDeviceEntity;
import com.jrdcom.wearable.smartband2.entity.CommondEntity;
import com.jrdcom.wearable.smartband2.util.Constants;
import com.jrdcom.wearable.smartband2.util.GattAttributes;
import com.jrdcom.wearable.smartband2.util.LogUtil;
import com.jrdcom.wearable.smartband2.util.MyUtils;

import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import rx.Subscriber;
import rx.schedulers.Schedulers;

/**
 * Service for managing connection and data communication with a GATT server
 * hosted on a given Bluetooth LE device.
 */
@SuppressLint("NewApi")
public class BluetoothLeService3 extends Service implements ISendCommondManager
{
    private final static String TAG = BluetoothLeService3.class.getSimpleName();
    private static BluetoothLeService3 mBluetoothLeService;
    private Object mLock = new Object();

    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private String mBluetoothDeviceAddress;
    private BluetoothGatt mBluetoothGatt;
    private int mConnectionState = STATE_DISCONNECTED;

    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTING = 1;
    private static final int STATE_CONNECTED = 2;
    public int RSSI_VALUE;
    public final static UUID UUID_HEART_RATE_MEASUREMENT = UUID.fromString(GattAttributes.HEART_RATE_MEASUREMENT);
    public final static UUID UUID_A2 = UUID.fromString("bfcce9a2-e479-11e3-ac10-0800200c9a66");

    // Implements callback methods for GATT events that the app cares about. For
    // example,
    // connection change and services discovered.
    private MyBlueCallback mGattCallback;
    private int currentBluetoothStatus;
    private NtfContext mNtfContext;
    private String mAddress;
    private int reconnectTime = 0;
    private int MAX_RECONNECT_TIME = 2;
    private BluetoothGattCharacteristic mCmdRxChar, mCmdTxChar;
    private static final int INDEX = 3;

    private Handler mHandle;

    private BtDeviceEntity btDeviceEntity;
    private CommondCallbackImpl commandCallback;
    private List<byte[]> mPkg = new LinkedList<>();

    private CommonTestModel commonTestModel;
    private boolean mNotificationEnabled = false;
    private Timer mCmdQueueTimer;
    private TimerTask mCmdQueueTimerTask;

    private BtDeviceEntity getCurrentBtDevice()
    {
        if (BleScannerManager.getMacList().size() > INDEX)
        {
            BtDeviceEntity btDeviceEntity = BleScannerManager.getMacList().get(INDEX);
            return btDeviceEntity;
        } else
        {
            return new BtDeviceEntity();
        }
    }

    private CmdCommond mCallback = new CmdCommond()
    {
        @Override
        public void onSuccess()
        {
            LogUtil.d(TAG, "onSuccess");
            stopTimeoutCal();
            if (commandCallback != null)
            {
                sendDataToTarget();
                commandCallback.onSuccess();
            }
        }

        @Override
        public void onAckSuccess()
        {
            LogUtil.d(TAG, "onAckSuccess");
            stopTimeoutCal();
            if (commandCallback != null)
            {
                commandCallback.onAckSuccess();
            }

        }

        @Override
        public void onFailed(int errorCode, String errorMessage)
        {
            LogUtil.d(TAG, "onFailed errorCode=" + errorCode + " errorMessage=" + errorMessage);

            stopTimeoutCal();
            if (commandCallback != null)
            {
                commandCallback.onFailed(errorCode, errorMessage);
            }
        }

        @Override
        public void onResponse(int modelId, int keyId, byte[] data)
        {
            LogUtil.d(TAG, "onResponse modelId=" + String.format("0x%x", modelId) + " keyId=" + String.format("0x%x", keyId));
            LogUtil.d(TAG, "onResponse ===>" + MyUtils.byte2hexPrint(data));
            Schedulers.io().createWorker().schedule(() ->
            {
                if (commandCallback != null
                        && commandCallback.commandEnum.getModelId() == modelId
                        && commandCallback.commandEnum.getKeyId() == keyId)
                {
                    CommondEntity commondEntity = new CommondEntity(modelId, keyId, data);
                    commandCallback.onResponse(commondEntity);
                } else
                {
                    CommondEntity commondEntity = new CommondEntity(modelId, keyId, data);
                    if (commondEntity.getCommandEnum() != null)
                    {
                        LogUtil.d(TAG, "onResponse broadcastData===>" + commondEntity.getCommandEnum().name());
                        broadcastData(commondEntity);
                    }


                }
            });
        }
    };

    private void broadcastData(CommondEntity commondEntity)
    {
        Intent intent = new Intent(Constants.ACTION_BLE_DATA_AVAILABLE);
        intent.putExtra(Constants.DATA_EXTRA_INFO, commondEntity);
        intent.putExtra(Constants.DATA_CONNECT_MAC_ADDRESS, mAddress);
        intent.putExtra(Constants.DATA_CONNECT_LIST_INDEX, INDEX);
        sendBroadcast(intent);
    }

    public static BluetoothLeService3 getInstance()
    {
        if (mBluetoothLeService == null)
        {
            LogUtil.e(TAG, "Unable to initialize BluetoothLeService." + INDEX);
        }
        return mBluetoothLeService;
    }

    public static void setmBluetoothLeService(BluetoothLeService3 mBluetoothLeService)
    {
        BluetoothLeService3.mBluetoothLeService = mBluetoothLeService;
    }

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            String action = intent.getAction();
            LogUtil.d(TAG, "action=" + action);
            if (Constants.ACTION_BLE_DISCONNECT_SERVICE3.equals(action))
            {
                disconnect();
            }
        }
    };

    @Override
    public void onCreate()
    {
        super.onCreate();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Constants.ACTION_BLE_DISCONNECT_SERVICE3);
        this.registerReceiver(mBroadcastReceiver, intentFilter);
        initialize();
    }

    @Override
    public void onDestroy()
    {
        this.unregisterReceiver(mBroadcastReceiver);
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        if (intent == null)
        {
            return flags;
        }
        mHandle = new Handler();
        String action = intent.getAction();
        LogUtil.d(TAG, "action=" + action);
        reconnectTime = 0;
        if ((Constants.ACTION_BLE_CONNECT_SERVICE + INDEX).equals(action))
        {
            mAddress = intent.getStringExtra(Constants.DATA_CONNECT_MAC_ADDRESS);
            LogUtil.d(TAG, "mAddress=" + mAddress);
            if (!TextUtils.isEmpty(mAddress))
            {
                if (mAddress.equals(getCurrentBtDevice().getMac()))
                {
                    connect(mAddress);
                }
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }


    private void displayGattServices(List<BluetoothGattService> services)
    {
        if (services == null || services.isEmpty())
        {
            return;
        }
        for (BluetoothGattService service : services)
        {
            LogUtil.d(TAG, "service.getUuid: " + service.getUuid().toString());
            if (service.getUuid().equals(Constants.UUID_TRUSTED_DATA_SERVICE))
            {
                mCmdRxChar = service.getCharacteristic(Constants.UUID_TRUSTED_DATA_NOTIFY);
                mCmdTxChar = service.getCharacteristic(Constants.UUID_TRUSTED_DATA_WRITE);
                getCurrentBtDevice().setStatus(BtDeviceEntity.BT_DEVICE_STATUS_SERVICE_OK);
                setCharacteristicNotification(mCmdRxChar, true);
                getCurrentBtDevice().setStatus(BtDeviceEntity.BT_DEVICE_STATUS_CONNECTED);
                broadcastUpdate(Constants.ACTION_BLE_GATT_SERVICES_DISCOVERED);
            }
        }
    }


    private void startTest()
    {
        commonTestModel.startAutoTest(getCurrentBtDevice()).subscribe(new Subscriber<BtDeviceEntity>()
        {
            @Override
            public void onCompleted()
            {
                getCurrentBtDevice().setStatus(BtDeviceEntity.BT_DEVICE_STATUS_AUTO_VAL_PASS);
                broadcastUpdate(Constants.ACTION_BLE_AUTO_TEST_RESULT);
            }

            @Override
            public void onError(Throwable e)
            {
                LogUtil.i(TAG, "startTest onError");
            }

            @Override
            public void onNext(BtDeviceEntity btDeviceEntity)
            {
                LogUtil.i(TAG, "startTest btDeviceEntity=" + btDeviceEntity);
            }
        });
    }

    private void broadcastUpdate(final String action)
    {
        final Intent intent = new Intent(action);
        intent.putExtra(Constants.DATA_CONNECT_MAC_ADDRESS, mAddress);
        intent.putExtra(Constants.DATA_CONNECT_LIST_INDEX, INDEX);
        BleScannerManager.getMacList().set(INDEX, getCurrentBtDevice());
        BleScannerManager.saveMacList();
        sendBroadcast(intent);
    }

    /**
     * Initializes a reference to the local Bluetooth adapter.
     *
     * @return Return true if the initialization is successful.
     */
    public boolean initialize()
    {
        // For API level 18 and above, get a reference to BluetoothAdapter
        // through
        // BluetoothManager.
        mBluetoothLeService = this;
        if (mBluetoothManager == null)
        {
            mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager == null)
            {
                LogUtil.e(TAG, "Unable to initialize BluetoothManager.");
                return false;
            }
        }

        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null)
        {
            LogUtil.e(TAG, "Unable to obtain a BluetoothAdapter.");
            return false;
        }
        commonTestModel = new CommonTestModel(this);
        return true;
    }

    /**
     * Connects to the GATT server hosted on the Bluetooth LE device.
     *
     * @param address The device address of the destination device.
     * @return Return true if the connection is initiated successfully. The
     * connection result is reported asynchronously through the
     * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     * callback.
     */
    public boolean connect(final String address)
    {
        reconnectTime++;
        LogUtil.d(TAG, "connect ===> address=" + address);
        if (mBluetoothAdapter == null || address == null)
        {
            LogUtil.w(TAG, "BluetoothAdapter not initialized or unspecified address.");
            return false;
        }

        // Previously connected device. Try to reconnect. (先前连接的设备。 尝试重新连接)
        if (mBluetoothDeviceAddress != null && address.equals(mBluetoothDeviceAddress) && mBluetoothGatt != null)
        {
            LogUtil.d(TAG, "Trying to use an existing mBluetoothGatt for connection.");
            if (mBluetoothGatt.connect())
            {
                mConnectionState = STATE_CONNECTING;
                return true;
            } else
            {
                return false;
            }
        }

        final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        if (device == null)
        {
            LogUtil.w(TAG, "Device not found.  Unable to connect.");
            return false;
        }
        // We want to directly connect to the device, so we are setting the
        // autoConnect
        // parameter to false.
        if (mGattCallback == null)
        {
            mGattCallback = new MyBlueCallback();
        }
        mBluetoothGatt = device.connectGatt(this, false, mGattCallback);
        LogUtil.d(TAG, "Trying to create a new connection.");
        mBluetoothDeviceAddress = address;
        mConnectionState = STATE_CONNECTING;
        return true;
    }

    /**
     * Disconnects an existing connection or cancel a pending connection. The
     * disconnection result is reported asynchronously through the
     * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     * callback.
     */
    public void disconnect()
    {
        if (getCurrentBtDevice().getStatus() != BtDeviceEntity.BT_DEVICE_STATUS_ALL_VAL_FINISH)
        {
            getCurrentBtDevice().setStatus(BtDeviceEntity.BT_DEVICE_STATUS_IDEL);
        }
        if (mBluetoothAdapter == null)
        {
            LogUtil.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        if (mBluetoothGatt == null)
        {
            LogUtil.w(TAG, "mBluetoothGatt not initialized");
            return;
        }
        mBluetoothGatt.disconnect();
        mBluetoothGatt.close();
        close();
    }

    private void clearConnectCache()
    {
        if (getCurrentBtDevice().getStatus() != BtDeviceEntity.BT_DEVICE_STATUS_ALL_VAL_FINISH)
        {
            getCurrentBtDevice().setStatus(BtDeviceEntity.BT_DEVICE_STATUS_IDEL);
        }
        if (mBluetoothAdapter == null)
        {
            LogUtil.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        if (mBluetoothGatt == null)
        {
            LogUtil.w(TAG, "mBluetoothGatt not initialized");
            return;
        }
        mBluetoothGatt.disconnect();
        mBluetoothGatt.close();
    }

    /**
     * After using a given BLE device, the app must call this method to ensure
     * resources are released properly.
     */
    public void close()
    {
        if (mBluetoothGatt == null)
        {
            return;
        }
        mBluetoothGatt.close();
        mGattCallback = null;
        mBluetoothGatt = null;
        mCmdRxChar = null;
        mCmdTxChar = null;
        stopSelf();
    }


    /**
     * Request a read on a given {@code BluetoothGattCharacteristic}. The read
     * result is reported asynchronously through the
     * {@code BluetoothGattCallback#onCharacteristicRead(android.bluetooth.BluetoothGatt, android.bluetooth
     * .BluetoothGattCharacteristic, int)}
     * callback.
     *
     * @param characteristic The characteristic to read from.
     */
    public void readCharacteristic(BluetoothGattCharacteristic characteristic)
    {
        if (mBluetoothAdapter == null || mBluetoothGatt == null)
        {
            LogUtil.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.readCharacteristic(characteristic);
    }

    /**
     * Enables or disables notification on a give characteristic.
     *
     * @param characteristic Characteristic to act on.
     * @param enabled        If true, enable notification. False otherwise.
     */
    public void setCharacteristicNotification(BluetoothGattCharacteristic characteristic, boolean enabled)
    {
        if (mBluetoothAdapter == null || mBluetoothGatt == null)
        {
            LogUtil.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.setCharacteristicNotification(characteristic, enabled);
        BluetoothGattDescriptor descriptor = characteristic.getDescriptor(UUID
                .fromString(GattAttributes.CLIENT_CHARACTERISTIC_CONFIG));
        if (descriptor != null)
        {
            System.out.println("write descriptor");
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            mBluetoothGatt.writeDescriptor(descriptor);
        }
    }

    /**
     * Retrieves a list of supported GATT services on the connected device. This
     * should be invoked only after {@code BluetoothGatt#discoverServices()}
     * completes successfully.
     *
     * @return A {@code List} of supported services.
     */
    public List<BluetoothGattService> getSupportedGattServices()
    {
        if (mBluetoothGatt == null)
            return null;
        return mBluetoothGatt.getServices();
    }

    /**
     * Read the RSSI for a connected remote device.
     */
    public boolean getRssiVal()
    {
        if (mBluetoothGatt == null)
            return false;

        return mBluetoothGatt.readRemoteRssi();
    }

    class MyBlueCallback extends BluetoothGattCallback
    {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState)
        {
            String intentAction;
            currentBluetoothStatus = newState;
            LogUtil.w(TAG, "MSG =======status:" + status + " newState=" + newState);
            if (newState == BluetoothProfile.STATE_CONNECTED)
            {
                intentAction = Constants.ACTION_BLE_GATT_CONNECTED;
                mConnectionState = STATE_CONNECTED;
                getCurrentBtDevice().setStatus(BtDeviceEntity.BT_DEVICE_STATUS_CONNECTED);
                broadcastUpdate(intentAction);
                LogUtil.d(TAG, "Connected to GATT server.");
                // Attempts to discover services after successful connection.
                mBluetoothGatt.discoverServices();

            } else if (newState == BluetoothProfile.STATE_DISCONNECTED)
            {
                intentAction = Constants.ACTION_BLE_GATT_DISCONNECTED;
                mConnectionState = STATE_DISCONNECTED;
                LogUtil.d(TAG, "Disconnected from GATT server.");
                if (getCurrentBtDevice().getStatus() != BtDeviceEntity.BT_DEVICE_STATUS_ALL_VAL_FINISH)
                {
                    getCurrentBtDevice().setStatus(BtDeviceEntity.BT_DEVICE_STATUS_DISCONNECTED);
                }
                broadcastUpdate(intentAction);
                if (status == 133 && reconnectTime < MAX_RECONNECT_TIME)
                {
                    mHandle.postDelayed(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            connect(mAddress);
                        }
                    }, 2000);
                } else
                {
                    disconnect();
                }
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status)
        {
            LogUtil.d(TAG, "onServicesDiscovered");
            if (status == BluetoothGatt.GATT_SUCCESS && currentBluetoothStatus == BluetoothProfile.STATE_CONNECTED)
            {
                LogUtil.d(TAG, "onServicesDiscovered found service success");
                displayGattServices(mBluetoothGatt.getServices());

            } else
            {
                LogUtil.d(TAG, "onServicesDiscovered received: " + status);
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status)
        {
            LogUtil.d(TAG, "onCharacteristicRead");
            if (status == BluetoothGatt.GATT_SUCCESS && currentBluetoothStatus == BluetoothProfile.STATE_CONNECTED)
            {
//                broadcastUpdate(Constants.ACTION_BLE_DATA_AVAILABLE, characteristic.getValue());
            }
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status)
        {
            LogUtil.d(TAG, "onDescriptorWriteonDescriptorWrite = " + status + ", descriptor =" + descriptor.getUuid().toString());
            if (status == BluetoothGatt.GATT_SUCCESS)
            {
                if (mNotificationEnabled == false)
                {
                    mNotificationEnabled = true;
                    BluetoothGattDescriptor descriptorRxChar = mCmdRxChar.getDescriptor(UUID
                            .fromString(GattAttributes.CLIENT_CHARACTERISTIC_CONFIG));
                    if (descriptorRxChar.getUuid().equals(descriptor.getUuid()))
                    {
                        LogUtil.d(TAG, "onDescriptorWrite onNotificationEnabled uuid=" + descriptor.getUuid());
                    }
                    startTest();
                }
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic)
        {
            LogUtil.d(TAG, "onCharacteristicChanged");

            if (characteristic.getUuid().equals(UUID_A2) && currentBluetoothStatus == BluetoothProfile.STATE_CONNECTED)
            {
                byte[] data = characteristic.getValue();
                if (data != null)
                {
                    LogUtil.i(TAG, "MSG Receive==>" + MyUtils.byte2hexPrint(data));
                    if (mNtfContext == null || mNtfContext.isComplete())
                    {
                        mNtfContext = new NtfContext(data);
                        if (mNtfContext.getPkt() == null)
                        {
                            mNtfContext = null;
                            return;
                        }
                    } else
                    {
                        mNtfContext.addPktSeg(data);
                    }

                    if ((mNtfContext.isComplete()) && (mNtfContext.getPkt() != null))
                    {
                        if (mNtfContext.isAckPkt())
                        {
                            if (mCallback != null)
                            {
                                mCallback.onAckSuccess();
                            }
                        } else
                        {
                            if (mCallback != null)
                            {
                                mCallback.onResponse(mNtfContext.mModelId, mNtfContext.mKeyId, mNtfContext.getmValuePkt());
                            }
                        }
                    }
                }
            }
        }

        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status)
        {
            System.out.println("rssi = " + rssi);
            RSSI_VALUE = rssi;
        }

        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status)
        {
            LogUtil.d(TAG, "onCharacteristicWrite");
            if (status == BluetoothGatt.GATT_SUCCESS)
            {
                if (characteristic.getUuid().equals(mCmdTxChar.getUuid()))
                {
                    if (mCallback != null)
                    {
                        mCallback.onSuccess();
                    }
                } else
                {
                    if (mCallback != null)
                    {
                        mCallback.onFailed(ICommandCallback.COMMOND_SEND_WRITE_FAILED, "onCharacteristicWrite Failed!");
                    }
                    LogUtil.d(TAG, characteristic.getUuid().toString());
                }
            }
        }
    }

    public void startTimeoutCal()
    {
        if (mCmdQueueTimer == null)
        {
            mCmdQueueTimer = new Timer("mCmdQueueTimer");
            mCmdQueueTimerTask = new TimerTask()
            {
                @Override
                public void run()
                {
                    if (mCallback != null)
                    {
                        mCallback.onFailed(ICommandCallback.COMMOND_SEND_TIME_OUT, "send commond time 8s");
                    }
                    if (mCmdQueueTimer != null)
                    {
                        mCmdQueueTimer.cancel();
                        mCmdQueueTimer.purge();
                        mCmdQueueTimer = null;
                    }
                    LogUtil.d(TAG, "mCmdQueueTimer reset mCmdQueue");
                }
            };
            mCmdQueueTimer.schedule(mCmdQueueTimerTask, 1000 * 8);
        }
    }

    public void stopTimeoutCal()
    {
        if (mCmdQueueTimer != null)
        {
            mCmdQueueTimer.cancel();
            mCmdQueueTimer.purge();
            mCmdQueueTimer = null;
        }
    }

    public boolean sendDataToTarget(byte[] pkg, CommondCallbackImpl callback)
    {
        try
        {
            synchronized (mLock)
            {
                makeCommondPkg(pkg);
                byte[] pktSeg = mPkg.get(0);
                if (pktSeg == null)
                {
                    return false;
                }
                startTimeoutCal();
                commandCallback = callback;
                if (mCmdTxChar == null || mBluetoothGatt == null)
                {
                    LogUtil.v(TAG, "Failed to sendDataToTarget " + mCmdTxChar);
                    return false;
                }
                mCmdTxChar.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
                boolean ret = mCmdTxChar.setValue(pktSeg);
                if (ret != true)
                {
                    LogUtil.v(TAG, "Failed to set mCmdTxChar value");
                    if (mCallback != null)
                        mCallback.onFailed(ICommandCallback.COMMOND_SEND_WRITE_FAILED, "Failed to set mCmdTxChar value");
                    return ret;
                }

                ret = mBluetoothGatt.writeCharacteristic(mCmdTxChar);
                LogUtil.v(TAG, "ready to sendDataToTarget result :" + ret);
                LogUtil.i(TAG, "MSG Send==>" + MyUtils.byte2hexPrint(pktSeg));
                if (mPkg.size() > 0)
                {
                    mPkg.remove(0);
                }
                if (ret != true)
                {
                    LogUtil.v(TAG, "Failed to write mCmdTxChar " + mCmdTxChar.getUuid());
                    if (mCallback != null)
                        mCallback.onFailed(ICommandCallback.COMMOND_SEND_WRITE_FAILED, "Failed to write mCmdTxChar");
                    return ret;
                }
            }
        } catch (NullPointerException e)
        {
            LogUtil.v(TAG, "Failed to write mCmdTxChar,NullPointerException", e);
            if (mCallback != null)
                mCallback.onFailed(ICommandCallback.COMMOND_SEND_WRITE_FAILED, "Failed to write mCmdTxChar,NullPointerException");
            return false;
        }
        return true;
    }

    private void makeCommondPkg(byte[] pkg)
    {
        mPkg.clear();
        int count = (int) Math.ceil((double) pkg.length / 20);
        LogUtil.d(TAG,"makeCommondPkg count="+count);
        for (int i = 0; i < count; i++)
        {
            int length = 20;
            if (pkg.length >= 20 * (i + 1))
            {
                length = 20;
            } else
            {
                length = pkg.length % 20;
                if (length == 0)
                {
                    length = 20;
                }
            }
            byte[] value = new byte[length];
            System.arraycopy(pkg, 20 * i, value, 0, length);
            mPkg.add(value);
        }

    }

    private boolean sendDataToTarget()
    {
        try
        {
            synchronized (mLock)
            {
                if (mPkg.size() == 0)
                {
                    return false;
                }
                startTimeoutCal();
                byte[] data = mPkg.get(0);
                if (data == null)
                {
                    return false;
                }
                if (mCmdTxChar == null || mBluetoothGatt == null)
                {
                    LogUtil.v(TAG, "Failed to sendDataToTarget " + mCmdTxChar);
                    return false;
                }
                mCmdTxChar.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
                boolean ret = mCmdTxChar.setValue(data);
                if (ret != true)
                {
                    LogUtil.v(TAG, "Failed to set mCmdTxChar value");
                    if (mCallback != null)
                        mCallback.onFailed(ICommandCallback.COMMOND_SEND_WRITE_FAILED, "Failed to set mCmdTxChar value");
                    return ret;
                }

                ret = mBluetoothGatt.writeCharacteristic(mCmdTxChar);
                LogUtil.v(TAG, "ready to sendDataToTarget result :" + ret);
                LogUtil.i(TAG, "MSG Send==>" + MyUtils.byte2hexPrint(data));
                if (mPkg.size() > 0)
                {
                    mPkg.remove(0);
                }
                if (ret != true)
                {
                    LogUtil.v(TAG, "Failed to write mCmdTxChar " + mCmdTxChar.getUuid());
                    if (mCallback != null)
                        mCallback.onFailed(ICommandCallback.COMMOND_SEND_WRITE_FAILED, "Failed to write mCmdTxChar");
                    return ret;
                }
            }
        } catch (NullPointerException e)
        {
            LogUtil.v(TAG, "Failed to write mCmdTxChar,NullPointerException", e);
            if (mCallback != null)
                mCallback.onFailed(ICommandCallback.COMMOND_SEND_WRITE_FAILED, "Failed to write mCmdTxChar,NullPointerException");
            return false;
        }
        return true;
    }

}
