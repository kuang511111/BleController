package com.jrdcom.wearable.smartband2.entity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ${zhiwei.xu} on 2017/6/19.
 */

public class BtListEntity extends BaseEntity
{
    private final String id = "BtListEntity";
    private List<BtDeviceEntity> btDeviceEntities;


    public BtListEntity(List<BtDeviceEntity> deviceEntities)
    {
        btDeviceEntities = deviceEntities;
    }

    public BtListEntity()
    {

    }


    public String getId()
    {
        return id;
    }

    public List<BtDeviceEntity> getBtDeviceEntities()
    {
        if (btDeviceEntities == null)
        {
            btDeviceEntities = new ArrayList<>();
        }
        return btDeviceEntities;
    }

    public void setBtDeviceEntities(List<BtDeviceEntity> btDeviceEntities)
    {
        this.btDeviceEntities = btDeviceEntities;
    }

    @Override
    public String getEntityId()
    {
        return id;
    }

    @Override
    public String getEntityDefaultFileName()
    {
        return BtListEntity.class.getSimpleName();
    }
}
