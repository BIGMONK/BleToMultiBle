package youtu.bletomultible.bluetooth;

import android.content.Context;
import android.util.Log;
import android.view.GestureDetector;


import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by liuenbao on 1/23/16.
 */
public class InputSystemManager extends GestureDetector.SimpleOnGestureListener
        implements BlueToothLeManager.BlueToothConnectStateChangedListener, BlueToothLeManager.DataValuesChangedListener {

    private String TAG = InputSystemManager.class.getSimpleName();

    private static InputSystemManager instance;

    private BlueToothLeManager mBlueToothLeManager;
    private Context mContext;
    private HashMap<String, BleDeviceBean> devicesMap;

    public HashMap<String, Integer> getDevicesType() {
        return devicesType;
    }

    public void setDevicesType(HashMap<String, Integer> devicesType) {
        this.devicesType = devicesType;
    }

    private HashMap<String, Integer> devicesType;
    //An array of observers

    private InputSystemManager() {
        mBlueToothDataValuesChangedListeners = new LinkedList<>();
    }

    public static InputSystemManager getInstance() {
        if (instance == null) {
            instance = new InputSystemManager();
        }
        return instance;
    }

    public void startScanBle(){
        if (instance!=null&&mBlueToothLeManager!=null){
            mBlueToothLeManager.startScanBle();
        }
    }

    //Input System Public Interface begin
    //若在某个Activity里面监听该事件需要继承这些listener

    private BlueToothConnectStateEvevtListener mBlueToothConnectStateEvevtListener;


    public interface BlueToothConnectStateEvevtListener {
        void onBlueToothConnectStateChanged(int type, String add, int state);
    }

    public void setBlueToothConnectStateEvevtListener(BlueToothConnectStateEvevtListener listener) {
        mBlueToothConnectStateEvevtListener = listener;
    }

    public interface BlueToothDataValuesChangedListener {
        void onBlueToothDataValuesChanged(int type, String add, byte[] values);
    }

//    private BlueToothDataValuesChangedListener mBlueToothDataValuesChangedListener;
//
//    public void setBlueToothDataValuesChangedListener(BlueToothDataValuesChangedListener listener) {
//        mBlueToothDataValuesChangedListener = listener;
//    }

    private List<BlueToothDataValuesChangedListener> mBlueToothDataValuesChangedListeners;

    public void registerBlueToothDataValuesChangedListener(BlueToothDataValuesChangedListener listener) {
        mBlueToothDataValuesChangedListeners.add(listener);
    }

    public void unRegisterBlueToothDataValuesChangedListener(BlueToothDataValuesChangedListener listener) {
        mBlueToothDataValuesChangedListeners.remove(listener);
    }


    //注意，此处的Context一定是Activity的context
    public boolean initWithContext(Context context, HashMap<String, BleDeviceBean> devicesMap) {

        //这里的context只能是Activity的context
        this.mContext = context;
        this.devicesMap = devicesMap;
        if (devicesType == null)
            devicesType = new HashMap<>();
        for (String add : devicesMap.keySet()) {
            devicesType.put(add, devicesMap.get(add).getType());
        }

        if (mBlueToothLeManager == null)
            mBlueToothLeManager = new BlueToothLeManager(context);
//        设置监听事件
        mBlueToothLeManager.setBlueToothConnectStateChangedListener(this);
        mBlueToothLeManager.setDataValuesChangedListener(this);
        mBlueToothLeManager.initBlueToothInfo(devicesMap);
        return true;
    }

    public synchronized boolean reConnectBlueTooth(String add) {
        Log.d(TAG, "reConnectBlueTooth    " + add);
        return mBlueToothLeManager.reConnectDevice(add);
    }

    public synchronized void sendData(String add, byte[] data) {
        if (mBlueToothLeManager != null) {
            mBlueToothLeManager.sendData(add, data);
        }
    }

    //1  手环   2  主控板   3 计步器   4  三角心率计
    public synchronized void sendData(int flag, byte[] data) {
        if (mBlueToothLeManager != null) {
            for (String add : devicesMap.keySet()) {
                if (flag == devicesMap.get(add).getType()) {
                    mBlueToothLeManager.sendData(add, data);
                    break;
                }
            }
        }

    }

    public synchronized void sendData(String add, String data) {
        if (mBlueToothLeManager != null) {
            mBlueToothLeManager.sendData(add, data);
        }
    }

    public synchronized boolean setCharacteristicNotification(String add) {
        return mBlueToothLeManager.setCharacteristicNotification(add);
    }

    public synchronized void disconnectDevice(String add) {
        if (mBlueToothLeManager != null) {
            mBlueToothLeManager.disconnectDevice(add);
            Log.d(TAG, "disconnectDevice  " + add);
        }
    }

    /**
     * 状态监听
     *
     * @param add
     * @param state
     */
    @Override
    public void onBlueToothConnectState(String add, int state) {
        if (devicesMap.containsKey(add)) {
            if (!(devicesMap.get(add).getType() == SampleGattAttributes.HAND_BAND&&state==BluetoothStatus.STATE_SEND_AND_NOTIFY_READY)) {
                mBlueToothConnectStateEvevtListener.onBlueToothConnectStateChanged(devicesMap.get(add).getType(), add, state);
            }
            //断开重连
            if (state == BluetoothStatus.STATE_DISCONNECTED) {
                if (devicesMap.get(add).getType()==SampleGattAttributes.HAND_BAND){
                    isFirstUpdateTime=false;
                }
                if (!instance.reConnectBlueTooth(add)) {
                    initWithContext(mContext, devicesMap);
                }
            }
        }
    }

    /**
     * 数据监听
     *
     * @param add
     * @param values
     */
    @Override
    public void onDataValuesChanged(String add, byte[] values) {
//        if (mBlueToothDataValuesChangedListener!=null)
//        mBlueToothDataValuesChangedListener.onBlueToothDataValuesChanged(devicesType.get(add), add, values);

        if (mBlueToothDataValuesChangedListeners != null) {
            for (BlueToothDataValuesChangedListener listener : mBlueToothDataValuesChangedListeners) {
                listener.onBlueToothDataValuesChanged(devicesType.get(add), add, values);
            }
            //同步手环时间
            if (devicesMap.get(add) != null && devicesMap.get(add).getType() == SampleGattAttributes.HAND_BAND && !isFirstUpdateTime) {
                isFirstUpdateTime = true;
                devicesMap.get(add).setState(BluetoothStatus.STATE_SEND_AND_NOTIFY_READY);
                sendData(add, SampleGattAttributes.getHandBandTimeData());
                Log.d(TAG, "onBlueToothConnectState同步手环时间::" + "   " + add);
                //更新手环连接状态为就绪
                mBlueToothConnectStateEvevtListener.onBlueToothConnectStateChanged(devicesMap.get(add).getType(),
                        add, BluetoothStatus.STATE_SEND_AND_NOTIFY_READY);
            }
        }
    }

    boolean isFirstUpdateTime;
}
