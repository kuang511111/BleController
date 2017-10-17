package com.jrdcom.wearable.smartband2.entity;

import android.os.Parcel;
import android.os.Parcelable;

import com.jrdcom.wearable.smartband2.ble.CommandEnum;
import com.jrdcom.wearable.smartband2.util.MyUtils;

/**
 * Created by ${zhiwei.xu} on 2017/6/21.
 */

public class CommondEntity implements Parcelable
{
    private int mModle;
    private int mKey;
    private byte[] data;
    private boolean isAck;
    private CommandEnum commandEnum;

    public CommondEntity(int mModle, int mKey, byte[] data)
    {
        this.mModle = mModle;
        this.mKey = mKey;
        this.data = data;
        for (CommandEnum cE : CommandEnum.values())
        {
            if (cE.getKeyId() == mKey && cE.getModelId() == mModle)
            {
                this.commandEnum = cE;
                break;
            }
        }
    }

    public CommondEntity(boolean ack)
    {
        this.isAck = ack;
    }

    public CommandEnum getCommandEnum()
    {
        return commandEnum;
    }

    public void setCommandEnum(CommandEnum commandEnum)
    {
        this.commandEnum = commandEnum;
    }

    public int getmModle()
    {
        return mModle;
    }

    public void setmModle(int mModle)
    {
        this.mModle = mModle;
    }

    public int getmKey()
    {
        return mKey;
    }

    public void setmKey(int mKey)
    {
        this.mKey = mKey;
    }

    public byte[] getData()
    {
        return data;
    }

    public void setData(byte[] data)
    {
        this.data = data;
    }

    public boolean isAck()
    {
        return isAck;
    }

    public void setAck(boolean ack)
    {
        isAck = ack;
    }

    @Override
    public String toString()
    {
        return "CommondEntity{" +
                "mModle=" + mModle +
                ", mKey=" + mKey +
                ", data=" + MyUtils.byte2hexPrint(data) +
                '}';
    }

    @Override
    public int describeContents()
    {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        dest.writeInt(this.mModle);
        dest.writeInt(this.mKey);
        dest.writeByteArray(this.data);
        dest.writeByte(isAck ? (byte) 1 : (byte) 0);
        dest.writeInt(this.commandEnum == null ? -1 : this.commandEnum.ordinal());
    }

    protected CommondEntity(Parcel in)
    {
        this.mModle = in.readInt();
        this.mKey = in.readInt();
        this.data = in.createByteArray();
        this.isAck = in.readByte() != 0;
        int tmpCommandEnum = in.readInt();
        this.commandEnum = tmpCommandEnum == -1 ? null : CommandEnum.values()[tmpCommandEnum];
    }

    public static final Parcelable.Creator<CommondEntity> CREATOR = new Parcelable.Creator<CommondEntity>()
    {
        public CommondEntity createFromParcel(Parcel source)
        {
            return new CommondEntity(source);
        }

        public CommondEntity[] newArray(int size)
        {
            return new CommondEntity[size];
        }
    };
}
