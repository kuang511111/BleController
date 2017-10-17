package com.jrdcom.wearable.smartband2.util;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.TreeSet;


public class SharedPrefsUtil
{
    public final static String SPNAME = "MOVEBOND2_BT_Test";

    public static void putValue(Context context, String key, int value)
    {
        SharedPreferences.Editor sp = context.getSharedPreferences(SPNAME, Context.MODE_PRIVATE).edit();
        sp.putInt(key, value);
        sp.commit();
    }

    public static void putValue(Context context, String key, boolean value)
    {
        SharedPreferences.Editor sp = context.getSharedPreferences(SPNAME, Context.MODE_PRIVATE).edit();
        sp.putBoolean(key, value);
        sp.commit();
    }

    public static void putValue(Context context, String key, String value)
    {
        SharedPreferences.Editor sp = context.getSharedPreferences(SPNAME, Context.MODE_PRIVATE).edit();
        sp.putString(key, value);
        sp.commit();
    }

    public static void putValue(Context context, String key, TreeSet<String> value)
    {
        SharedPreferences.Editor sp = context.getSharedPreferences(SPNAME, Context.MODE_PRIVATE).edit();
        sp.putStringSet(key, value);
        sp.commit();
    }

    public static int getValue(Context context, String key, int defValue)
    {
        SharedPreferences sp = context.getSharedPreferences(SPNAME, Context.MODE_PRIVATE);
        int value = sp.getInt(key, defValue);
        return value;
    }

    public static boolean getValue(Context context, String key, boolean defValue)
    {
        SharedPreferences sp = context.getSharedPreferences(SPNAME, Context.MODE_PRIVATE);
        boolean value = sp.getBoolean(key, defValue);
        return value;
    }

    public static String getValue(Context context, String key, String defValue)
    {
        SharedPreferences sp = context.getSharedPreferences(SPNAME, Context.MODE_PRIVATE);
        String value = sp.getString(key, defValue);
        return value;
    }

    public static TreeSet<String> getValue(Context context, String key)
    {
        SharedPreferences sp = context.getSharedPreferences(SPNAME, Context.MODE_PRIVATE);
        TreeSet<String> defValue = new TreeSet<>();
        TreeSet<String> value = (TreeSet<String>) sp.getStringSet(key, defValue);
        return value;
    }
}
