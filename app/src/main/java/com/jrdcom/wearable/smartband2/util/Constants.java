
package com.jrdcom.wearable.smartband2.util;

import java.util.UUID;

public class Constants
{
    public final static UUID UUID_TRUSTED_DATA_SERVICE = UUID.fromString("bfcce9a0-e479-11e3-ac10-0800200c9a66");
    public final static UUID UUID_TRUSTED_DATA_WRITE = UUID.fromString("bfcce9a1-e479-11e3-ac10-0800200c9a66");
    public final static UUID UUID_TRUSTED_DATA_NOTIFY = UUID.fromString("bfcce9a2-e479-11e3-ac10-0800200c9a66");

    public static final int ROM_VER_LEN = 20;
    public static final String KEY = Constants.class.getPackage().getName() + ".";

    public static final String SCAN_RESULT_LIST = KEY + "SCAN_RESULT_LIST";
    public final static String ACTION_BLE_CONNECT_SERVICE = KEY + "ACTION_BLE_CONNECT_SERVICE";
    public final static String ACTION_BLE_CONNECT_SERVICE0 = KEY + "ACTION_BLE_CONNECT_SERVICE0";
    public final static String ACTION_BLE_CONNECT_SERVICE1 = KEY + "ACTION_BLE_CONNECT_SERVICE1";
    public final static String ACTION_BLE_CONNECT_SERVICE2 = KEY + "ACTION_BLE_CONNECT_SERVICE2";
    public final static String ACTION_BLE_CONNECT_SERVICE3 = KEY + "ACTION_BLE_CONNECT_SERVICE3";
    public final static String ACTION_BLE_CONNECT_SERVICE4 = KEY + "ACTION_BLE_CONNECT_SERVICE4";
    public final static String ACTION_BLE_CONNECT_SERVICE5 = KEY + "ACTION_BLE_CONNECT_SERVICE5";
    public final static String ACTION_BLE_CONNECT_SERVICE6 = KEY + "ACTION_BLE_CONNECT_SERVICE6";
    public final static String ACTION_BLE_CONNECT_SERVICE7 = KEY + "ACTION_BLE_CONNECT_SERVICE7";

    public final static String ACTION_BLE_DISCONNECT_SERVICE = KEY + "ACTION_BLE_DISCONNECT_SERVICE";
    public final static String ACTION_BLE_DISCONNECT_SERVICE0 = KEY + "ACTION_BLE_DISCONNECT_SERVICE0";
    public final static String ACTION_BLE_DISCONNECT_SERVICE1 = KEY + "ACTION_BLE_DISCONNECT_SERVICE1";
    public final static String ACTION_BLE_DISCONNECT_SERVICE2 = KEY + "ACTION_BLE_DISCONNECT_SERVICE2";
    public final static String ACTION_BLE_DISCONNECT_SERVICE3 = KEY + "ACTION_BLE_DISCONNECT_SERVICE3";
    public final static String ACTION_BLE_DISCONNECT_SERVICE4 = KEY + "ACTION_BLE_DISCONNECT_SERVICE4";
    public final static String ACTION_BLE_DISCONNECT_SERVICE5 = KEY + "ACTION_BLE_DISCONNECT_SERVICE5";
    public final static String ACTION_BLE_DISCONNECT_SERVICE6 = KEY + "ACTION_BLE_DISCONNECT_SERVICE6";
    public final static String ACTION_BLE_DISCONNECT_SERVICE7 = KEY + "ACTION_BLE_DISCONNECT_SERVICE7";


    public final static String DATA_CONNECT_MAC_ADDRESS = KEY + "DATA_CONNECT_MAC_ADDRESS";
    public final static String DATA_CONNECT_LIST_INDEX = KEY + "DATA_CONNECT_LIST_INDEX";
    public final static String ACTION_BLE_GATT_CONNECTED = KEY + "ACTION_BLE_GATT_CONNECTED";
    public final static String ACTION_BLE_GATT_DISCONNECTED = KEY + "ACTION_BLE_GATT_DISCONNECTED";
    public final static String ACTION_BLE_GATT_SERVICES_DISCOVERED = KEY + "ACTION_BLE_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_BLE_AUTO_TEST_RESULT = KEY + "ACTION_BLE_AUTO_TEST_RESULT";
    public final static String ACTION_BLE_DATA_AVAILABLE = KEY + "ACTION_BLE_DATA_AVAILABLE";
    public final static String DATA_EXTRA_INFO = KEY + "DATA_EXTRA_INFO";
    public final static String DATA_BYTE_INFO = KEY + "DATA_BYTE_INFO";


}
