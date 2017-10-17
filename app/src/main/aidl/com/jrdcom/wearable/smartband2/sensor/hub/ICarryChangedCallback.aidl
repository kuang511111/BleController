package com.jrdcom.wearable.smartband2.sensor.hub;

/**
 * Created by rongwu.chen on 14-6-30.
 */
interface ICarryChangedCallback {
    void onStateChanged(int previous, int current);
}
