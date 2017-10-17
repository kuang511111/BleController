package com.jrdcom.wearable.smartband2.ble;


import com.jrdcom.wearable.smartband2.util.LogUtil;
import com.jrdcom.wearable.smartband2.util.ThreadPool;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by rongwu.chen on 14-1-21.
 */
public class BleCommand
{
    private final static String TAG = "ST/" + BleCommand.class.getSimpleName();
    public final static int MAGIC = 0xabba;
    public final static int HEARD_LEN = 6;
    public final static int HEARD_KEY_LEN = 2;
    public final static int HEARD_TOTAL_LEN = 8;
    public final static int ANDROID_FLAG = 0x00;
    public final static int HEARD_VERSION = 0x00;
    public final static int PKT_LENGTH_MAX = 512;
    public final static int PKT_LENGTH_MIN = 20;
    public final static byte ANDROID_DEVICE = 0x01;
    public final static int UNKNOWN = -1;
    public final static int WATCH_FACTORY_RESET = 2;

    private static int mPKT_LENGTH = PKT_LENGTH_MAX;

    private static int mSidCount;


//    // Bind Commands
//    public static byte[] makeBindCommandValue(String username, String phoneBluetoothName)
//    {
//        byte[] name = new byte[32 + 32 + 1];
//        int len = username.length();
//        System.arraycopy(username.getBytes(), 0, name, 0, len > 32 ? 32 : len);
//        byte[] buf = phoneBluetoothName.getBytes();
//        len = buf.length;//phoneBluetoothName.length();
//        System.arraycopy(buf, 0, name, 32, len > 32 ? 32 : len);
//        name[64] = ANDROID_DEVICE;
//        return name;
//    }
//
//    // Bind Commands
//    public static byte[] makeUnBindCommandBytes(String username, String phoneBluetoothName)
//    {
//        byte[] name = new byte[32 + 32 + 1];
//        int len = username.length();
//        System.arraycopy(username.getBytes(), 0, name, 0, len > 32 ? 32 : len);
//        byte[] buf = phoneBluetoothName.getBytes();
//        len = buf.length;//phoneBluetoothName.length();
//        System.arraycopy(buf, 0, name, 32, len > 32 ? 32 : len);
//        name[64] = ANDROID_DEVICE;
//        return name;
//    }
//
//    // Bind Commands
//    public static byte[] makeQuickBindCommand(String username, String phoneBluetoothName)
//    {
//
//        byte[] name = new byte[32 + 32 + 1];
//        int len = username.length();
//        System.arraycopy(username.getBytes(), 0, name, 0, len > 32 ? 32 : len);
//        byte[] buf = phoneBluetoothName.getBytes();
//        len = buf.length;//phoneBluetoothName.length();
//        System.arraycopy(buf, 0, name, 32, len > 32 ? 32 : len);
//        name[64] = ANDROID_DEVICE;
//        return createCommondPacket(CommandKeyEnum.valueOf("QUICK_BIND"), name);
//    }
//
//    public static byte[] makeLoginCommand(String username, String deviceInfo)
//    {
//        byte[] name = new byte[32 + 32 + 1];
//        int len = username == null ? 0 : username.length();
//        if (len > 0)
//            System.arraycopy(username.getBytes(), 0, name, 0, len > 32 ? 32 : len);
//
//        if (!TextUtil.isBlank(deviceInfo))
//        {
//            byte[] buf = deviceInfo.getBytes();
//            len = buf.length;//phoneBluetoothName.length();
//            System.arraycopy(buf, 0, name, 32, len > 32 ? 32 : len);
//        }
//        name[64] = ANDROID_DEVICE;
//        return createCommondPacket(CommandKeyEnum.valueOf("LOGIN"), name);
//    }

    public static byte[] createCommondPacket(int mModuleId, int mKey, byte[] values)
    {
        int len = (values == null ? 0 : values.length);
        byte[] pkt = new byte[len + HEARD_KEY_LEN];
        int index = 0;
        pkt[index++] = (byte) mModuleId;
        pkt[index++] = (byte) mKey;
        if (len > 0)
        {
            System.arraycopy(values, 0, pkt, HEARD_KEY_LEN, len);
        }
        return createHeadPacket(false, pkt);
    }

    public static byte[] createCommondPacket(CommandEnum commandEnum, byte[] values)
    {
        int len = (values == null ? 0 : values.length);
        byte[] pkt = new byte[len + HEARD_KEY_LEN];
        int index = 0;
        pkt[index++] = (byte) commandEnum.getModelId();
        pkt[index++] = (byte) commandEnum.getKeyId();
        if (len > 0)
        {
            System.arraycopy(values, 0, pkt, HEARD_KEY_LEN, len);
        }
        return createHeadPacket(false, pkt);
    }


    public static byte[] createHeadPacket(boolean isAck, byte[] cmd)
    {
        int len = (cmd == null ? 0 : cmd.length);
        byte[] pkt = new byte[HEARD_LEN + len];
        int index = 0;
        pkt[index++] = (byte) (MAGIC & 0x00ff);
        pkt[index++] = (byte) ((MAGIC >> 8) & 0x00ff);
        // Payload length
        pkt[index++] = (byte) ((len + HEARD_LEN) & 0x00ff);
        pkt[index++] = (byte) (((len + HEARD_LEN) & 0xff00) >> 8);
        // VERSION
        pkt[index++] = (byte) HEARD_VERSION & 0x0f;
//        pkt[index++] = (byte) ANDROID_FLAG;
        //DATA TYPE flag 0:request 1: response
        pkt[index++] = (byte) (isAck ? 0x01 : 0x00);
        if (len > 0)
        {
            System.arraycopy(cmd, 0, pkt, HEARD_LEN, len);
        }
        return pkt;
    }

    public static int getMagic(byte[] pkt)
    {
        int m = 0;
        m = (0xff & ((int) pkt[0])) | (0xff00 & ((int) pkt[1] << 8));
        return m;
    }


    public static int getLen(byte[] pkt)
    {
        int m = 0;
        m = ((int) pkt[3] & 0xff) << 8 | ((int) pkt[2] & 0xff);
        return m;
    }

    public static int getCurrentSeqCount()
    {
        mSidCount = mSidCount & 0x0000FFFF;
        if (mSidCount >= 0x0000FFFF)
        {
            mSidCount = 0x0;
        }
        return mSidCount;
    }

    public static int getNewSeqCount()
    {
//        mSidCount = ++mSidCount & 0x0000FFFF;
        mSidCount = 0x00;
        return getCurrentSeqCount();
    }

    public static int getSidByPktInfo(byte[] pkt)
    {
        int m = 0;
        int cmdModelId = (pkt[6] & 0xff) << 24 & 0xFF000000;
        int kId = (pkt[7] & 0xff) << 16 & 0x00FF0000;
        m = cmdModelId | kId | getCurrentSeqCount();
        return m;
    }


    public static boolean getAck(byte[] pkt)
    {
        boolean m = pkt[5] == 1;
        return m;
    }

    public static void dispatchBleCommandCallback(final BleCommandCallback cb, byte[] pkt)
    {
        LogUtil.d(TAG, getTimeString() + "Receive a Pkt from Ble Device ---------->");
        LogUtil.dumpHex(TAG, pkt);
//        LogUtil.dumpHex(TAG,pkt);
        if (cb == null || pkt.length < HEARD_LEN) return;
        int magic = getMagic(pkt);
        if (magic != MAGIC)
        {
            LogUtil.w(TAG, "Magic is not match");
            return;
        }
        int length = getLen(pkt);
        LogUtil.w(TAG, "length=" + length);

        boolean isAck = getAck(pkt);
        int sid = getSidByPktInfo(pkt);
        LogUtil.w(TAG, "received sid=" + String.format("sid = 0x%x", sid));

        if (length != pkt.length || length == 0 || length < 8)
        {
            LogUtil.w(TAG, "Pkt length is not match ");
            return;
        }

        int offset = HEARD_LEN;
        int cmdModelId = pkt[offset++] & 0xff;
        int kId = (pkt[offset++] & 0xff);

        if (length == 0 && isAck)
        {
            if (length > HEARD_TOTAL_LEN)
            {
                final byte[] kVal;
                kVal = new byte[length - HEARD_TOTAL_LEN];
                System.arraycopy(pkt, offset, kVal, 0, length - HEARD_TOTAL_LEN);
                cb.onCommonAckCallback(cmdModelId, kId);
            } else
            {
                cb.onCommonAckCallback(cmdModelId, kId);
            }
            LogUtil.i(TAG, "->Get an ack pkt");
            return;
        }
        LogUtil.d(TAG, "offset=" + offset);
//        do
        {
            int kLen = length - offset;
            LogUtil.d(TAG, "kLen=" + kLen);
            boolean found = false;
            for (final CommandEnum keyEnum : CommandEnum.values())
            {
                if ((keyEnum.getModelId() == cmdModelId) && (keyEnum.getKeyId() == kId))
                {
                    LogUtil.d(TAG, "MSG " + String.format(" mModuleId = 0x%x", cmdModelId) + String.format(" eventId = 0x%x", kId));
                    final byte[] kVal;
                    if (kLen > 0)
                    {
                        kVal = new byte[kLen];
                        System.arraycopy(pkt, HEARD_TOTAL_LEN, kVal, 0, kLen);
                        if (kLen <= 32)
                        {
                            LogUtil.dumpHex(TAG, kVal);
                        }
                    } else
                    {
                        kVal = null;
                    }
                    LogUtil.i(TAG, getTimeString() + "--------------->>>>>>>   Get Event ID : " + keyEnum.name() + " len = " + kLen);
                    try
                    {
                        found = true;
                        LogUtil.d(TAG, String.format("Receive(0x%x):(0x%x) " + keyEnum.name() + " len = %d", sid, kId, kLen));
                        ThreadPool.getCachedThreadPool().execute(() -> cb.onCommonCallBack(cmdModelId, kId, kVal));
                    } catch (Exception e)
                    {
                        LogUtil.w(TAG, "dispatchBleCommand key Uncaught Exception:" + e.toString(), e);
                    }
                    break;
                }
            }
            if (found == false)
            {
                LogUtil.w(TAG, String.format("Receive unknown event id:(0x%x,0x%x)", cmdModelId, kId));
            }
            offset += kLen;
            LogUtil.d(TAG, "offset=" + offset);
        }
//        while (offset < length);
    }

    public static String getTimeString(long timestamp)
    {
        Date date = new Date(timestamp);
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss .SSS ");//yyyy-MM-dd
        return formatter.format(date);
    }

    public static String getTimeString()
    {
        Long timestamp = System.currentTimeMillis();
        Date date = new Date(timestamp);
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss .SSS ");//yyyy-MM-dd
        return formatter.format(date);
    }

    public static int getValueMaxLength()
    {
        return mPKT_LENGTH - HEARD_LEN - HEARD_KEY_LEN;
    }
}



