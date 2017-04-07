package youtu.bletomultible;

import android.app.Application;
import android.content.Context;

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
    }

    public static Context getContext() {
        return mContext;
    }
}
