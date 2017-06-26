package youtu.bletomultible.utils;

import android.util.Log;

import  youtu.bletomultible.BuildConfig;

/**
 * Created by djf on 2016/11/17.
 * <p>
 * 打印日志
 */
public class LogUtils {
    /**
     * 日志输出级别NONE
     */
    public static final int LEVEL_NONE = 0;
    /**
     * 日志输出级别V
     */
    public static final int LEVEL_VERBOSE = 1;
    /**
     * 日志输出级别D
     */
    public static final int LEVEL_DEBUG = 2;
    /**
     * 日志输出级别I
     */
    public static final int LEVEL_INFO = 3;
    /**
     * 日志输出级别W
     */
    public static final int LEVEL_WARN = 4;
    /**
     * 日志输出级别E
     */
    public static final int LEVEL_ERROR = 5;

    /**
     * 日志输出时的TAG
     */
    private static String mTag = "LogUtils";
    /**
     * 是否允许输出log
     */
    private static int mDebuggable = LEVEL_ERROR;



    /**
     * 以级别为 d 的形式输出LOG
     */
    public static void v(String msg) {
        if (mDebuggable >= LEVEL_VERBOSE&& BuildConfig.DEBUG) {
            Log.v(mTag, msg);
        }
    }

    /**
     * 以级别为 d 的形式输出LOG
     */
    public static void v(String TAG, String msg) {
        if (mDebuggable >= LEVEL_VERBOSE&& BuildConfig.DEBUG) {
            Log.v(TAG, msg);
        }
    }

    /**
     * 以级别为 d 的形式输出LOG
     */
    public static void d(String msg) {
        if (mDebuggable >= LEVEL_DEBUG&& BuildConfig.DEBUG) {
            Log.d(mTag, msg);
        }
    }

    /**
     * 以级别为 d 的形式输出LOG
     */
    public static void d(String TAG, String msg) {
        if (mDebuggable >= LEVEL_DEBUG&& BuildConfig.DEBUG) {
            Log.d(TAG, msg);
        }
    }

    /**
     * 以级别为 i 的形式输出LOG
     */
    public static void i(String msg) {
        if (mDebuggable >= LEVEL_INFO&& BuildConfig.DEBUG) {
            Log.i(mTag, msg);
        }
    }

    /**
     * 以级别为 i 的形式输出LOG
     */
    public static void i(String TAG, String msg) {
        if (mDebuggable >= LEVEL_INFO&& BuildConfig.DEBUG) {
            Log.i(TAG, msg);
        }
    }

    /**
     * 以级别为 w 的形式输出LOG
     */
    public static void w(String msg) {
        if (mDebuggable >= LEVEL_WARN&& BuildConfig.DEBUG) {
            Log.w(mTag, msg);
        }
    }

    /**
     * 以级别为 w 的形式输出LOG
     */
    public static void w(String TAG, String msg) {
        if (mDebuggable >= LEVEL_WARN&& BuildConfig.DEBUG) {
            Log.w(TAG, msg);
        }
    }

    /**
     * 以级别为 w 的形式输出Throwable
     */
    public static void w(Throwable tr) {
        if (mDebuggable >= LEVEL_WARN&& BuildConfig.DEBUG) {
            Log.w(mTag, "", tr);
        }
    }

    /**
     * 以级别为 w 的形式输出LOG信息和Throwable
     */
    public static void w(String TAG, String msg, Throwable tr) {
        if (mDebuggable >= LEVEL_WARN && null != msg&& BuildConfig.DEBUG) {
            Log.w(TAG, msg, tr);
        }
    }

    /**
     * 以级别为 e 的形式输出LOG
     */
    public static void e(String msg) {
        if (mDebuggable >= LEVEL_ERROR&& BuildConfig.DEBUG) {
            Log.e(mTag, msg);
        }
    }

    /**
     * 以级别为 e 的形式输出LOG
     */
    public static void e(String TAG, String msg) {
        if (mDebuggable >= LEVEL_ERROR&& BuildConfig.DEBUG) {
            Log.e(TAG, msg);
        }
    }

    /**
     * 以级别为 e 的形式输出Throwable
     */
    public static void e(Throwable tr) {
        if (mDebuggable >= LEVEL_ERROR&& BuildConfig.DEBUG) {
            Log.e(mTag, "", tr);
        }
    }

    /**
     * 以级别为 e 的形式输出LOG信息和Throwable
     */
    public static void e(String TAG, String msg, Throwable tr) {
        if (mDebuggable >= LEVEL_ERROR && null != msg&& BuildConfig.DEBUG) {
            Log.e(TAG, msg, tr);
        }
    }
}


