package com.jrdcom.wearable.smartband2.util;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.util.Log;

import com.jrdcom.wearable.mb12m.minitest.R;

import java.io.IOException;
import java.io.InputStream;

public class VersionUtil {
 
	//public static String firmwareVersionCode = "2.0.1";

    public static String getFirmwareVersion(Context context) {
        try {
            InputStream is = context.getResources().openRawResource(R.raw.boomband_firmware);
            byte buf[] = new byte[Constants.ROM_VER_LEN];
            is.read(buf, 0, buf.length);
            is.close();
            return new String(buf);
        } catch (IOException e) {
            Log.e(VersionUtil.class.getName(), "", e);
            return null;
        }
    }

	public static String getApkVersionName(Context ctx) {
		try {
			String strPackageName = ctx.getPackageName();
			PackageManager pm = ctx.getPackageManager();
			PackageInfo pi = pm.getPackageInfo(strPackageName, 0);

			int nSep = pi.versionName.indexOf('-');
			int nSepZ = pi.versionName.indexOf(0);

			if (nSep != -1) {
				if (nSepZ != -1) {
					if (nSep < nSepZ)
						return pi.versionName.substring(0, nSep);
					else
						return pi.versionName.substring(0, nSepZ);
				} else {
					return pi.versionName.substring(0, nSep);
				}
			}

			return pi.versionName;
			
		} catch (NameNotFoundException e) {
			return "";
		}
	}

}
