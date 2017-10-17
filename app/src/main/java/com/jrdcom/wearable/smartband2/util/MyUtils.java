package com.jrdcom.wearable.smartband2.util;

import android.content.Context;
import android.os.Environment;

import com.jrdcom.wearable.gowatch.factorytest.service.BluetoothLeService;
import com.jrdcom.wearable.gowatch.factorytest.service.BluetoothLeService1;
import com.jrdcom.wearable.gowatch.factorytest.service.BluetoothLeService2;
import com.jrdcom.wearable.gowatch.factorytest.service.BluetoothLeService3;
import com.jrdcom.wearable.gowatch.factorytest.service.BluetoothLeService4;
import com.jrdcom.wearable.gowatch.factorytest.service.BluetoothLeService5;
import com.jrdcom.wearable.gowatch.factorytest.service.BluetoothLeService6;
import com.jrdcom.wearable.gowatch.factorytest.service.BluetoothLeService7;
import com.jrdcom.wearable.gowatch.factorytest.service.ISendCommondManager;
import com.jrdcom.wearable.smartband2.WearableApplication;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class MyUtils
{
    public static String regCu = "^MB12-[0-9a-zA-Z\\-\\*]{5,10}";
    public static String romReg = "^[VM]{1}[0-9]{1}[\\.]{1}[0-9]{1}[\\.]{1}[0-9]{1}$";

    //把字符串内容保存到指定路径
    //name格式——"hello.text"
    public synchronized static void saveStr2File(Context context, String content, String path, String name)
    {
//        String filePath = null;
//        boolean hasSDCard = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
//        if (hasSDCard) { // SD卡根目录下的path文件夹hello.text
//            filePath = Environment.getExternalStorageDirectory().toString() + File.separator +path + File.separator + name;
//        } else  // 系统下载缓存根目录的path文件夹hello.text
//            filePath = Environment.getDownloadCacheDirectory().toString() + File.separator +path + File.separator + name;

        String filePath = path + File.separator + name;
        try
        {
            File file = new File(filePath);
            if (!file.exists())
            {
                File dir = new File(file.getParent());
                dir.mkdirs();
                file.createNewFile();
            }
            FileOutputStream outStream = new FileOutputStream(file);
            outStream.write(content.getBytes());
            outStream.close();
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /*参考下面*/
    public synchronized static String getMB12EchoPath(Context context)
    {
        String cachePath = null;
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())
                || !Environment.isExternalStorageRemovable())
        {
            cachePath = WearableApplication.getStoreFilePath() + "MB12Echo";
        } else
        {
            cachePath = context.getCacheDir().getPath() + File.separator + "MB12Echo";
        }
        return cachePath;
    }

    /*当SD卡存在或者SD卡不可被移除的时候
    就调用getExternalCacheDir()方法来获取缓存路径
    否则就调用getCacheDir()方法来获取缓存路径。
    前者获取到的就是 /sdcard/Android/data/<application package>/cache 这个路径
    而后者获取到的是 /data/data/<application package>/cache 这个路径*/
    public static String getDiskCacheDir(Context context)
    {
        String cachePath = null;
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())
                || !Environment.isExternalStorageRemovable())
        {
            cachePath = context.getExternalCacheDir().getPath();
        } else
        {
            cachePath = context.getCacheDir().getPath();
        }
        return cachePath;
    }

    //格式化当前时间
    public static String getTimeFormat()
    {
        //输出结果：20170307(141558)
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        String timeStr = sdf.format(date);
        String time = timeStr.substring(0, 8) + "(" + timeStr.substring(8) + ")";
        return time;
    }

    /*删除一个文件夹和里面的所有的文件*/
    public static void deleteAll(File file)
    {
        if (!file.exists())
        {
            return;
        }
        if (file.isFile())
        {
            file.delete();
            return;
        }

        if (file.isDirectory())
        {
            File[] childFiles = file.listFiles();
            if (childFiles == null || childFiles.length == 0)
            {
                file.delete();
                return;
            }

            for (int i = 0; i < childFiles.length; i++)
            {
                deleteAll(childFiles[i]);
            }
            file.delete();
        }
    }

    public static boolean match(String regex, String str)
    {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(str);
        return matcher.matches();
    }

    public static int getMagic(byte[] pkt)
    {
        int m = 0;
        m = (0xff & ((int) pkt[0])) | (0xff00 & ((int) pkt[1] << 8));
        return m;
    }


    public static int getLen(byte[] pkt)
    {
        int m = 0;
        m = ((int) pkt[3] & 0xff) << 8 | ((int) pkt[2] & 0xff);
        return m;
    }

    public static boolean getAck(byte[] pkt)
    {
        boolean m = pkt[5] == 1;
        return m;
    }

    public static String byte2hexPrint(byte b[])
    {
        if (b == null)
        {
            LogUtil.d("byte2hexPrint", "Argument b ( byte array ) is null! ");
            return "";
//            throw new IllegalArgumentException("Argument b ( byte array ) is null! ");
        }
        StringBuffer hs = new StringBuffer("");
        String stmp = "";
        for (int n = 0; n < b.length; n++)
        {
            stmp = Integer.toHexString(b[n] & 0xff);
            hs.append((stmp.length() == 1) ? "0" + stmp : stmp);
            hs.append(" ");
        }
        return hs.toString().toUpperCase();
    }

    public static String byte2hex(byte b[])
    {
        if (b == null)
        {
            throw new IllegalArgumentException(
                    "Argument b ( byte array ) is null! ");
        }
        StringBuffer hs = new StringBuffer("");
        String stmp = "";
        for (int n = 0; n < b.length; n++)
        {
            stmp = Integer.toHexString(b[n] & 0xff);
            hs.append((stmp.length() == 1) ? "0" + stmp : stmp);
        }
        return hs.toString().toUpperCase();
    }

    public static ISendCommondManager getSendCommondManager(int position)
    {
        if (position == 0)
        {
            return BluetoothLeService.getInstance();
        } else if (position == 1)
        {
            return BluetoothLeService1.getInstance();
        } else if (position == 2)
        {
            return BluetoothLeService2.getInstance();
        } else if (position == 3)
        {
            return BluetoothLeService3.getInstance();
        } else if (position == 4)
        {
            return BluetoothLeService4.getInstance();
        } else if (position == 5)
        {
            return BluetoothLeService5.getInstance();
        } else if (position == 6)
        {
            return BluetoothLeService6.getInstance();
        } else if (position == 7)
        {
            return BluetoothLeService7.getInstance();
        }else
        {
            return null;
        }
    }
}
