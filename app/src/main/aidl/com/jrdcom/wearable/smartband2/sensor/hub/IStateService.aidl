package com.jrdcom.wearable.smartband2.sensor.hub;

import com.jrdcom.wearable.smartband2.sensor.hub.ICarryChangedCallback;
import com.jrdcom.wearable.smartband2.sensor.hub.IMotionChangedCallback;
import com.jrdcom.wearable.smartband2.sensor.hub.IPostureChangedCallback;
import com.jrdcom.wearable.smartband2.sensor.hub.ITransportChangedCallback;
import com.jrdcom.wearable.smartband2.sensor.hub.IStepChangedCallback;


/**
 * Created by rongwu.chen on 14-6-30.
 */
interface IStateService {

    boolean sensorHubPresent();

    int motionState();
    int carryState();
    int postureState();
    int transportState();

    void registerCarryChangedCallback(ICarryChangedCallback callback);
    void unregisterCarryChangedCallback(ICarryChangedCallback callback);

    void registerMotionChangedCallback(IMotionChangedCallback callback);
    void unregisterMotionChangedCallback(IMotionChangedCallback callback);

    void registerPostureChangedCallback(IPostureChangedCallback callback);
    void unregisterPostureChangedCallback(IPostureChangedCallback callback);

    void registerTransportChangedCallback(ITransportChangedCallback callback);
    void unregisterTransportChangedCallback(ITransportChangedCallback callback);

    /* Perdometer */
    void setUserProfile(int gender, int age, int weight, int height);

    void enableStepCount(boolean enable);
    void syncStepCount(boolean enable);

    void registerStepChangedCallback(IStepChangedCallback callback);
    void unregisterStepChangedCallback(IStepChangedCallback callback);
}
