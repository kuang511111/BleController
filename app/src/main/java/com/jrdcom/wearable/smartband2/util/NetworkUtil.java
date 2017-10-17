package com.jrdcom.wearable.smartband2.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

public class NetworkUtil {
	
	public static boolean checkNetworkConnection(Context context) {
		final ConnectivityManager connMgr = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		final android.net.NetworkInfo wifi = connMgr
				.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		final android.net.NetworkInfo mobile = connMgr
				.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
		if ((wifi != null && wifi.isConnected())
				|| (mobile != null && mobile.isAvailable())){ // change for pr763252
			return true;
		} else {
			return false;
		}
	}
	
	public static boolean checkNetworkConnectionWifiOnly(Context context) {
		final ConnectivityManager connMgr = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		final android.net.NetworkInfo wifi = connMgr
				.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		if (wifi != null && wifi.isConnected()){
			return true;
		} else {
			return false;
		}
	}
	
	public static String getLocalMacAddress(Context context) { 
        WifiManager wifi = (WifiManager)context.getSystemService(Context.WIFI_SERVICE); 
        WifiInfo info = wifi.getConnectionInfo(); 
        return info.getMacAddress(); 
   } 
}
