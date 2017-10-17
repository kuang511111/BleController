package com.jrdcom.wearable.smartband2;

import android.app.Application;
import android.os.Environment;
import android.util.Log;

import com.jrdcom.wearable.smartband2.cache.EntityCacheUtil;
import com.tencent.bugly.crashreport.CrashReport;

import java.io.File;

public class WearableApplication extends Application
{

    private static WearableApplication mContext = null;
    private static String storeFilePath;

    public static String getStoreFilePath()
    {
        if (storeFilePath == null)
        {
            storeFilePath = Environment.getExternalStorageDirectory().getPath() + File.separatorChar + "android"
                    + File.separatorChar + "data"+ + File.separatorChar + "movebond2M";
        }
        //   /storage/emulated/0/android/data/movebond2M/
        Log.i("WearableApplication","storeFilePath="+storeFilePath);
        return storeFilePath;
    }

    @Override
    public void onCreate()
    {
        super.onCreate();
        mContext = this;
        CrashReport.initCrashReport(getApplicationContext());
        EntityCacheUtil.getInstance();
    }

    public static Application getInstance()
    {
        return mContext;
    }


}
