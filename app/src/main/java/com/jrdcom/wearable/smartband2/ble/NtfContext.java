package com.jrdcom.wearable.smartband2.ble;

import com.jrdcom.wearable.smartband2.util.LogUtil;
import com.jrdcom.wearable.smartband2.util.MyUtils;

/**
 * Created by ${zhiwei.xu} on 2017/6/6.
 */

public class NtfContext
{
    private final String TAG = "BleNtfContext";
    private byte[] mPkt;
    private byte[] mValuePkt;
    private int mLen; // packet length
    private boolean mAck;
    private int mOffset;
    public int mModelId;
    public int mKeyId;


    public NtfContext(byte[] pkt)
    {
        int magic = BleCommand.getMagic(pkt);
        if (magic != BleCommand.MAGIC)
        {
            LogUtil.w(TAG, "magic is not match at new NtfContext()");
            return;
        }
        if (pkt.length < BleCommand.HEARD_TOTAL_LEN)
        {
            LogUtil.w(TAG, "pkt length is less than HEARD_TOTAL_LEN at new NtfContext() " + pkt.length);
//                LogUtil.dumpHexWrite(TAG, pkt);
            return;
        }
        mAck = BleCommand.getAck(pkt);
        mLen = BleCommand.getLen(pkt);
        LogUtil.w(TAG, "pkt length=" + pkt.length + " mAck=" + mAck + " mLe= " + mLen);
        if (mLen < BleCommand.HEARD_TOTAL_LEN)
        {
//                LogUtil.dumpHexWrite(TAG, pkt);
            return;
        }
        int offset = BleCommand.HEARD_LEN;
        mModelId = pkt[offset++] & 0xff;
        mKeyId = (pkt[offset++] & 0xff);

        mPkt = new byte[mLen];
        int lenTmp = pkt.length > mPkt.length ? mPkt.length : pkt.length;
        System.arraycopy(pkt, 0, mPkt, 0, lenTmp);
        mOffset = pkt.length;
    }

    public boolean isComplete()
    {
        if (mPkt == null) return true;
        boolean ret = mOffset >= mLen;
        return ret;
    }

    public void clear()
    {
        LogUtil.w(TAG, "time out clear NtfContext sid = " + " ,mOffset=" + mOffset);
        mPkt = null;
        mValuePkt = null;
        mOffset = 0;
        mAck = false;
        mLen = 0;

    }

    public boolean isAckPkt()
    {
        return (/*mLen == 0 && mCrc == 0x0000 &&*/ mAck);
    }

    public void addPktSeg(byte[] pkt)
    {
        int len = 0;
        if ((mPkt != null) && (pkt != null))
        {

            if (pkt.length + mOffset <= mPkt.length)
            {
                len = pkt.length;
            } else
            {
                len = mPkt.length - mOffset;
            }
            System.arraycopy(pkt, 0, mPkt, mOffset, len);
            mOffset += len;
        }

    }


    public byte[] getPkt()
    {
        return mPkt;
    }

    public byte[] getmValuePkt()
    {
        int len = mLen - BleCommand.HEARD_TOTAL_LEN;
        if (len > 0)
        {
            if (mValuePkt != null && mValuePkt.length == len)
            {
                return mValuePkt;
            } else
            {
                mValuePkt = new byte[len];
                System.arraycopy(mPkt, (BleCommand.HEARD_TOTAL_LEN), mValuePkt, 0, len);
            }
        }
        return mValuePkt;
    }

    @Override
    public String toString()
    {
        return "NtfContext{" +
                "mPkt=" + MyUtils.byte2hexPrint(mPkt) +
                ", mValuePkt=" + MyUtils.byte2hexPrint(mValuePkt) +
                ", mLen=" + mLen +
                ", mAck=" + mAck +
                ", mOffset=" + mOffset +
                ", mModelId=" + mModelId +
                ", mKeyId=" + mKeyId +
                '}';
    }
}
