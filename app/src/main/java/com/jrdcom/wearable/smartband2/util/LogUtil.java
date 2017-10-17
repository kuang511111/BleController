package com.jrdcom.wearable.smartband2.util;

import android.os.Environment;
import android.util.Log;
import com.jrdcom.wearable.smartband2.WearableApplication;

import java.io.*;
import java.util.Date;

public class LogUtil {
    final static String TAG = LogUtil.class.getSimpleName();
    public static final boolean IS_DEBUGING = true;
    private final static int LOG_LEVEL_ERROR = 16;
    private final static int LOG_LEVEL_WARN = 8;
    private final static int LOG_LEVEL_INFO = 4;
    private final static int LOG_LEVEL_DEBUG = 2;
    private final static int LOG_LEVEL_VERBOSE = 0;
    // logcat level
    private static final int LOGCAT_LEVEL = (IS_DEBUGING) ? LOG_LEVEL_VERBOSE : 32;
    // log file level,must >=LOGCAT_LEVEL
    private static final int FILE_LOG_LEVEL = (IS_DEBUGING) ? LOG_LEVEL_INFO : 32;

    private static final boolean VERBOSE = (LOGCAT_LEVEL <= LOG_LEVEL_VERBOSE);
    private static final boolean DEBUG = (LOGCAT_LEVEL <= LOG_LEVEL_DEBUG);
    private static final boolean INFO = (LOGCAT_LEVEL <= LOG_LEVEL_INFO);
    private static final boolean WARN = (LOGCAT_LEVEL <= LOG_LEVEL_WARN);
    private static final boolean ERROR = (LOGCAT_LEVEL <= LOG_LEVEL_ERROR);
    private static final String LOG_TAG_STRING = "WearableLog";
    private static final String LOG_FILE_NAME = "Wearable.txt";
    private static final long LOG_SIZE = 5 * 1024 * 1024;
    private static final String LOG_ENTRY_FORMAT = "[%tF %tT][%s][%s]%s";
    private static PrintStream logStream;
    private static boolean initialized = false;


    public static boolean isDebug() {
        return IS_DEBUGING;
    }

    private static void d(File logFile) {
        if (DEBUG) {
            Log.d(LOG_TAG_STRING, TAG + " : Log to file : " + logFile);
        }
    }

    public static void d(String tag, String msg) {
        if (DEBUG) {
            tag = Thread.currentThread().getName() + ":" + tag;
            Log.d(LOG_TAG_STRING, tag + " : " + msg);
            if (FILE_LOG_LEVEL <= LOG_LEVEL_DEBUG)
                write("D", tag, msg, null);
        }
    }

    public static void d(String tag, String msg, Throwable error) {
        if (DEBUG) {
            tag = Thread.currentThread().getName() + ":" + tag;
            Log.d(LOG_TAG_STRING, tag + " : " + msg, error);
            if (FILE_LOG_LEVEL <= LOG_LEVEL_DEBUG)
                write("D", tag, msg, error);
        }
    }

    private static void v(File backfile) {
        if (VERBOSE) {
            Log.v(LOG_TAG_STRING, TAG + " : Create back log file : " + backfile.getName());
        }
    }

    public static void v(String tag, String msg) {
        if (DEBUG) {
            tag = Thread.currentThread().getName() + ":" + tag;
            Log.v(LOG_TAG_STRING, tag + " : " + msg);
            if (FILE_LOG_LEVEL <= LOG_LEVEL_DEBUG)
                write("V", tag, msg, null);
        }
    }


    public static void v(String tag, String msg, Throwable error) {
        if (DEBUG) {
            tag = Thread.currentThread().getName() + ":" + tag;
            Log.v(LOG_TAG_STRING, tag + " : " + msg, error);
            if (FILE_LOG_LEVEL <= LOG_LEVEL_DEBUG)
                write("V", tag, msg, error);
        }
    }

    public static void i(String tag, String msg) {
        if (INFO) {
            tag = Thread.currentThread().getName() + ":" + tag;
            Log.i(LOG_TAG_STRING, tag + " : " + msg);
            if (FILE_LOG_LEVEL <= LOG_LEVEL_INFO)
                write("I", tag, msg, null);
        }
    }

    public static void i(String tag, String msg, Throwable error) {
        if (INFO) {
            tag = Thread.currentThread().getName() + ":" + tag;
            Log.i(LOG_TAG_STRING, tag + " : " + msg, error);
            if (FILE_LOG_LEVEL <= LOG_LEVEL_INFO)
                write("I", tag, msg, error);
        }
    }

    private static void w() {
        if (WARN) {
            Log.w(LOG_TAG_STRING, "Unable to create external cache directory");
        }
    }

    public static void w(String tag, String msg) {
        if (WARN) {
            tag = Thread.currentThread().getName() + ":" + tag;
            Log.w(LOG_TAG_STRING, tag + " : " + msg);
            if (FILE_LOG_LEVEL <= LOG_LEVEL_WARN)
                write("W", tag, msg, null);
        }
    }

    public static void w(String tag, String msg, Throwable error) {
        if (WARN) {
            tag = Thread.currentThread().getName() + ":" + tag;
            Log.w(LOG_TAG_STRING, tag + " : " + msg, error);
            if (FILE_LOG_LEVEL <= LOG_LEVEL_WARN)
                write("W", tag, msg, error);
        }
    }


    private static void e(String msg, Exception e) {
        if (ERROR) {
            Log.e(LOG_TAG_STRING, msg, e);
        }
    }

    public static void e(String tag, String msg) {
        if (ERROR) {
            tag = Thread.currentThread().getName() + ":" + tag;
            Log.e(LOG_TAG_STRING, tag + " : " + msg);
            if (FILE_LOG_LEVEL <= LOG_LEVEL_ERROR)
                write("E", tag, msg, null);
        }
    }

    public static void e(String tag, String msg, Throwable error) {
        if (ERROR) {
            tag = Thread.currentThread().getName() + ":" + tag;
            Log.e(LOG_TAG_STRING, tag + " : " + msg, error);
            if (FILE_LOG_LEVEL <= LOG_LEVEL_ERROR)
                write("E", tag, msg, error);
        }
    }

    public static void wtf(String tag, String msg) {
        if (ERROR) {
            tag = Thread.currentThread().getName() + ":" + tag;
            Log.wtf(LOG_TAG_STRING, tag + " : " + msg);
            if (FILE_LOG_LEVEL <= LOG_LEVEL_ERROR)
                write("E", tag, msg, null);
        }
    }

    public static void wtf(String tag, String msg, Throwable error) {
        if (ERROR) {
            tag = Thread.currentThread().getName() + ":" + tag;
            Log.wtf(LOG_TAG_STRING, tag + " : " + msg, error);
            if (FILE_LOG_LEVEL <= LOG_LEVEL_ERROR)
                write("E", tag, msg, error);
        }
    }

    private static void init() {
        try {
            logStream = new PrintStream(getLogFileOS());//new OutputStreamWriter(new FileOutputStream("a.txt"),"utf-8"),true);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            logStream = null;
        } catch (IOException e) {
            e.printStackTrace();
            logStream = null;
        }
    }

    private static void write(String level, String tag, String msg, Throwable error) {
        if (!initialized)
            init();
        if (logStream == null || logStream.checkError()) {
            initialized = false;
            return;
        }
        Date now = new Date();
        logStream.printf(LOG_ENTRY_FORMAT, now, now, level, tag, " : " + msg);
        logStream.println();
        if (error != null) {
            error.printStackTrace(logStream);
            logStream.println();
        }

    }

    public static void fileWrite(String fileName, byte content[]) {
        if (IS_DEBUGING) {
            PrintStream writer = null;
            try {
                writer = new PrintStream(new FileOutputStream(fileName,true));
                writer.write(content);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (writer != null) {
                    writer.close();
                }
            }
        }
    }


    public static void fileWrite(String fileName, String content) {
        if (IS_DEBUGING) {
            FileWriter writer = null;
            try {
                writer = new FileWriter(fileName, true); //  true:  append
                writer.write(content);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (writer != null) {
                        writer.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    public static String getDebugFileName(String debugLogFileName) {
        if (IS_DEBUGING) {
            File file, filePath;
            String debugLogFilPath = WearableApplication.getStoreFilePath();//+ Environment.getExternalStorageDirectory().getPath() + File.separatorChar + "android"+ File.separatorChar+"smartband2" + File.separatorChar ;
            filePath = new File(debugLogFilPath);
            file = new File(debugLogFilPath + debugLogFileName);
            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                try {
                    if (!filePath.exists())
                        filePath.mkdirs();
                    if (!file.exists())
                        file.createNewFile();
                    else {
                        FileInputStream is = new FileInputStream(file);
                        int len = is.available();
                        is.close();
                        if (len > LOG_SIZE) {
                            file.delete();
                            file.createNewFile();
                        }
                    }
                } catch (Exception e) {
                    Log.w(TAG, "" + file.getPath(), e);
                }
                return debugLogFilPath + debugLogFileName;
            }

        }
        return "";
    }

    public static String getDebugLogFileName() {
        return  getDebugFileName("ble_log.txt");
       /* if (IS_DEBUGING) {
            File file, filePath;
            String debugLogFilPath = WearableApplication.getStoreFilePath();//+ Environment.getExternalStorageDirectory().getPath() + File.separatorChar + "android"+ File.separatorChar+"smartband2" + File.separatorChar ;
            String debugLogFileName = String.format("ble_log.txt");
            filePath = new File(debugLogFilPath);
            file = new File(debugLogFilPath + debugLogFileName);
            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                try {
                    if (!filePath.exists())
                        filePath.mkdirs();
                    if (!file.exists())
                        file.createNewFile();
                } catch (Exception e) {
                    Log.w(TAG, "" + file.getPath(), e);
                }
                return debugLogFilPath + debugLogFileName;
            }

        }
        return "";*/
    }

    private static FileOutputStream getLogFileOS() throws IOException {
        File file = new File(WearableApplication.getStoreFilePath() + LOG_FILE_NAME);
        if (!file.exists()) {
            file.createNewFile();
            return new FileOutputStream(file,true);
        }
        FileInputStream is = new FileInputStream(file);
        int len = is.available();
        is.close();
        if (len > LOG_SIZE) {
            file.delete();
            file.createNewFile();
        }
        return new FileOutputStream(file,true);
    }


    public static void dumpHex(String tag, byte start[]) {
        if (start != null)
            dumpHex(tag, start, start.length);
    }

    public static void dumpHex(String tag, byte start[], int len) {
        final char MAP[] = {
                '.', '.', '.', '.', '.', '.', '.', '.', '.', '.', '.', '.', '.', '.', '.', '.',
                '.', '.', '.', '.', '.', '.', '.', '.', '.', '.', '.', '.', '.', '.', '.', '.',
                ' ', '!', '"', '#', '$', '%', '&', '\'', '(', ')', '*', '+', ',', '-', '.', '/',
                '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', ':', ';', '<', '=', '>', '?',
                '@', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O',
                'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', '[', '\\', ']', '^', '_',
                '`', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o',
                'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '{', '|', '}', '~', '.',
                '.', '.', '.', '.', '.', '.', '.', '.', '.', '.', '.', '.', '.', '.', '.', '.',
                '.', '.', '.', '.', '.', '.', '.', '.', '.', '.', '.', '.', '.', '.', '.', '.',
                '.', '.', '.', '.', '.', '.', '.', '.', '.', '.', '.', '.', '.', '.', '.', '.',
                '.', '.', '.', '.', '.', '.', '.', '.', '.', '.', '.', '.', '.', '.', '.', '.',
                '.', '.', '.', '.', '.', '.', '.', '.', '.', '.', '.', '.', '.', '.', '.', '.',
                '.', '.', '.', '.', '.', '.', '.', '.', '.', '.', '.', '.', '.', '.', '.', '.',
                '.', '.', '.', '.', '.', '.', '.', '.', '.', '.', '.', '.', '.', '.', '.', '.',
                '.', '.', '.', '.', '.', '.', '.', '.', '.', '.', '.', '.', '.', '.', '.', '.'
        };
        final char HEX[] = {
                '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'
        };
        if (start == null)
            return;
        byte data[] = start;
        int i, d, e = 0;
        char line[] = new char[3 * 16 + 2 + 16];

        for (i = 0; i < line.length; i++) {
            line[i] = ' ';
        }

        for (i = 0; i < len; i++) {
            e = i % 16;
            d = e * 3;
            line[d + 0] = HEX[((data[i] & 0xff) >> 4) & 0x0f];
            line[d + 1] = HEX[((data[i] & 0xff) >> 0) & 0x0f];
            line[3 * 16 + 2 + e] = MAP[data[i] & 0xff];

            if (i % 16 == 15) {
                Log.v(tag, String.format("0x%08X: %s", i & ~15, new String(line)));
            }
        }

        if (len != 0 && e != 15) {
            for (i = e + 1; i < 16; i++) {
                d = i * 3;
                line[d + 0] = ' ';
                line[d + 1] = ' ';
                line[3 * 16 + 2 + i] = ' ';
            }
            Log.v(tag, String.format("0x%08X: %s", i & ~15, new String(line)));
        }
    }

    public static byte getByte(long d, int offset) {
        byte b = (byte) ((d >> offset) & 0x00ff);
        return b;
    }

    public static byte getLowByte(int d) {
        byte b = (byte) (d & 0x00ff);
        return b;
    }

    public static byte getHigh8Byte(int d) {
        byte b = (byte) ((d >>> 8) & 0x00ff);
        return b;
    }

    public static byte getHigh16Byte(int d) {
        byte b = (byte) ((d >>> 16) & 0x00ff);
        return b;
    }

    public static byte getHigh24Byte(int d) {
        byte b = (byte) ((d >>> 24) & 0x00ff);
        return b;
    }
}
