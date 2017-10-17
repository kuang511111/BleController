package com.jrdcom.wearable.smartband2.ble;

/**
 * Created by ${zhiwei.xu} on 2017/6/6.
 */

public enum CommandEnum
{
    FACTORY_TEST_GET_VER_INFO(0x57, 0x72),
    FACTORY_TEST_CHARGER(0x57, 0x08),
    FACTORY_TEST_BT(0x42, 0x01),
    FACTORY_TEST_WRITE_FLAG(0x42, 0x06),
    FACTORY_TEST_GET_PCBA(0x42, 0x07),
    FACTORY_TEST_G_SENSOR(0x42, 0x0A),
    FACTORY_TEST_LED_OPEN(0x42, 0x12),
    FACTORY_TEST_LED_CLOSE(0x42, 0x14),
    FACTORY_TEST_VIB_OPEN(0x42, 0x13),
    FACTORY_TEST_VIB_CLOSE(0x42, 0x15),
    FACTORY_TEST_ANTI(0x42, 0x17),
    FACTORY_TEST_ELECTRICITY(0x42, 0x18),
    FACTORY_TEST_SET_VERSION(0x42, 0x19),
    FACTORY_TEST_SET_CU(0x42, 0x1A),
    FACTORY_TEST_SET_PCBA(0x42, 0x1B),
    FACTORY_TEST_DEBUG(0x42, 0x99);


    int modelId;
    int keyId;

    CommandEnum(int mId, int kId)
    {
        modelId = mId;
        keyId = kId;
    }

    public int getModelId()
    {
        return modelId;
    }

//    public void setModelId(int modelId)
//    {
//        this.modelId = modelId;
//    }

    public int getKeyId()
    {
        return keyId;
    }

//    public void setKeyId(int keyId)
//    {
//        this.keyId = keyId;
//    }
}
