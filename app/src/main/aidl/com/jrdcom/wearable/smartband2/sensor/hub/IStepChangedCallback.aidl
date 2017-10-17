package com.jrdcom.wearable.smartband2.sensor.hub;

/**
 * Created by rongwu.chen on 14-6-30.
 */
interface IStepChangedCallback {
    void onChangedCallback(long from, long to, int type, int steps, float calories, float distance);
}
