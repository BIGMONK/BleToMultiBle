package youtu.bletomultible;

import android.app.Application;
import android.content.Context;

import cc.bodyplus.sdk.ble.manger.BleConnectionManger;

/**
 * Created by djf on 2017/4/7.
 */

public class MyApplication extends Application {

    //全局上下文环境
    private static Context mContext;
    @Override
    public void onCreate() {
        super.onCreate();

        mContext=getApplicationContext();
        /**
         * 初始化程序蓝牙管理类
         */
        BleConnectionManger.getInstance().init(this);
    }

    public static Context getContext() {
        return mContext;
    }
}
