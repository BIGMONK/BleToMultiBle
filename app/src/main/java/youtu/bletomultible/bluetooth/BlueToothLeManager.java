package youtu.bletomultible.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Handler;

import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;

import youtu.bletomultible.utils.LogUtils;


/**
 * Created by black on 2016/4/12.
 */
public class BlueToothLeManager extends Thread implements
        BluetoothLeTool.BluetoothLeDataListener,
        BluetoothStatus,
        BluetoothLeTool.BluetoothLeDiscoveredListener,
        BluetoothLeTool.BluetoothLeStatusListener {


    private FileWriter writer;

    public interface BlueToothConnectStateChangedListener {
        void onBlueToothConnectState(String add, int state);
    }

    public interface DataValuesChangedListener {
        void onDataValuesChanged(String add, byte[] values);
    }

    private BlueToothConnectStateChangedListener mBlueToothConnectStateListener;
    private DataValuesChangedListener mDataValuesChangedListener;

    public void setBlueToothConnectStateChangedListener(BlueToothConnectStateChangedListener
                                                                listener) {
        mBlueToothConnectStateListener = listener;
    }

    public void setDataValuesChangedListener(DataValuesChangedListener listener) {
        mDataValuesChangedListener = listener;
    }

    private final String TAG = BlueToothLeManager.class.getSimpleName();
    private Context mContext;
    private BluetoothAdapter mBluetoothAdapter;
    private ArrayList<BluetoothDevice> mLeDevices;
    private Runnable mRunnable;
    private HashMap<String, BleDeviceBean> devicesMap;
    private BluetoothLeTool mBluetoothTool;
    private HashMap<String, BluetoothGattCharacteristic> mNotifyBGC;
    private HashMap<String, BluetoothGattCharacteristic> mSendBGC;
    private int mCharProp;

    public BlueToothLeManager(Context pContext) {
        mContext = pContext;
        handler = new Handler();
        this.mNotifyBGC = new HashMap<>();
        this.mSendBGC = new HashMap<>();
    }


    Handler handler;

    public void initBlueToothInfo(HashMap<String, BleDeviceBean> mDeviceMap) {
        LogUtils.d(TAG, "initBlueToothInfo");
        this.devicesMap = mDeviceMap;
        if (mBluetoothTool == null)
            mBluetoothTool = new BluetoothLeTool(mDeviceMap);
        else {
            mBluetoothTool.upBluetoothLeTool(mDeviceMap);
        }

        if (!mBluetoothTool.initialize()) {
            LogUtils.i(TAG, "Unable to initialize Bluetooth");
        }
        mBluetoothTool.setBluetoothLeDataListener(this);
        mBluetoothTool.setBluetoothLeDiscoveredListener(this);
        mBluetoothTool.setBluetoothLeStatusListener(this);

        handler.removeCallbacksAndMessages(null);
        //打开蓝牙
        if (blueToothInit()) {
            //扫描蓝牙
            mRunnable = new Runnable() {
                @Override
                public void run() {
                    scanLeDevice(true);
                }
            };
            handler.postDelayed(mRunnable, 0);
        }
    }

    private boolean blueToothInit() {
        if (mLeDevices == null)
            mLeDevices = new ArrayList<BluetoothDevice>();
        else mLeDevices.clear();

        if (!mContext.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            return false;
        }

        final BluetoothManager bluetoothManager =
                (BluetoothManager) mContext.getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        LogUtils.d(TAG, "mBluetoothAdapter=" + String.valueOf(mBluetoothAdapter));

        if (mBluetoothAdapter == null) {
            return false;
        }

        if (!mBluetoothAdapter.isEnabled()) {
            if (!mBluetoothAdapter.isEnabled()) {
                mBluetoothAdapter.enable();
            }
        }

        return true;
    }

    public void startScanBle() {
        if (mLeScanCallback != null
                && mBluetoothAdapter != null) {
            LogUtils.d(TAG, "startScanBle");
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
        }
    }

    private void scanLeDevice(final boolean enable) {
        if (enable) {
            // Stops scanning after a pre-defined scan period.
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (mBluetoothTool.getAllConnectionState() == BluetoothStatus
                            .STATE_DISCONNECTED) {
                        for (String add : mBluetoothTool.getAllConnectionStateMap().keySet()) {
                            if (mBluetoothTool.getAllConnectionStateMap().get(add) ==
                                    BluetoothStatus.STATE_DISCONNECTED && devicesMap.keySet()
                                    .contains(add)) {
                                devicesMap.get(add).setState(BluetoothStatus.STATE_SCAN_TIMEOUT);
                                mBlueToothConnectStateListener.onBlueToothConnectState(add,
                                        BluetoothStatus.STATE_SCAN_TIMEOUT);
                            }
                        }
                    } else if (mBluetoothTool.getAllConnectionState() == BluetoothStatus
                            .STATE_CONNECTED) {
                        mBluetoothAdapter.stopLeScan(mLeScanCallback);
                    }
                }
            }, 10000);
            for (String add : mBluetoothTool.getAllConnectionStateMap().keySet()) {
                LogUtils.d("scanLeDevice" + add);
                if (devicesMap.containsKey(add)) {
                    devicesMap.get(add).setState(BluetoothStatus.STATE_SCANING);
                    mBlueToothConnectStateListener.onBlueToothConnectState(add, BluetoothStatus
                            .STATE_SCANING);
                }
            }
            mBluetoothAdapter.startLeScan(mLeScanCallback);
        } else {
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
        }
    }

    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {
                @Override
                public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
                    if (mLeDevices != null && !mLeDevices.contains(device)) {
                        mLeDevices.add(device);
                        LogUtils.d(TAG, "BlueToothLeManager onLeScan device="
                                + device.getAddress() + "   " + device.getName());
                    }
                    String deviceName = device.getName();
                    String deviceAdd = device.getAddress();
                    //TODO 需要判断设备连接状态
                    if (devicesMap != null && devicesMap.containsKey(deviceAdd)) {
                        LogUtils.d(TAG, "蓝牙设备findByAdd：" + deviceName + "  " + deviceAdd + "  " +
                                "state=" + devicesMap.get(deviceAdd).getState());
                        if ((devicesMap.get(deviceAdd).getState() == BluetoothStatus
                                .STATE_DISCONNECTED
                                || devicesMap.get(deviceAdd).getState() == BluetoothStatus
                                .STATE_SCANING
                                || devicesMap.get(deviceAdd).getState() == BluetoothStatus
                                .STATE_SCAN_TIMEOUT
                        )) {
                            connectDevice(device.getAddress());
                        } else if (devicesMap.get(deviceAdd).getState() == BluetoothStatus
                                .STATE_CONNECTING) {

                        }
                    }
                }
            };

    /**
     * 根据MAC连接蓝牙设备
     *
     * @param address
     * @return
     */
    public synchronized boolean connectDevice(String address) {

        if (mBluetoothTool != null) {
            final boolean result = mBluetoothTool.connect(address);
            LogUtils.i(TAG, "connectDevice result=" + result);

            return result;
        }
        return false;
    }

    public boolean reConnectDevice(String add) {

        if (mBluetoothTool != null && add != null) {
            final boolean result = mBluetoothTool.connect(add);
            LogUtils.i(TAG, "reConnectDevice result=" + result);
            return result;
        }
        return false;
    }


    public void disconnectDevice(String add) {
        if (add != null)
            mBluetoothTool.disconnect(add);
    }

    public void sendData(String add, byte[] value) {
        if (mBluetoothTool.getAllConnectionStateMap().get(add) == BluetoothStatus.STATE_CONNECTED) {
            if (mSendBGC.get(add) != null) {
                mSendBGC.get(add).setValue(value);
//                if (value != null && value.length == 9) {
//                    try {
//                        if (value.equals(MainBoardCommand.START_UPDATE)) {
//                            File file = new File("/mnt/external_sd/升级日志.txt");
//                            if (!file.exists()) {
//                                file.createNewFile();
//                            }
//                            writer = new FileWriter(file.getAbsoluteFile());
//                        }
//                        for (int i = 0; i < value.length; i++) {
//                            if (writer==null)
//                                writer = new FileWriter("/mnt/external_sd/升级日志.txt");
//                            writer.write(value[i] + "  ");
//                            if (i == value.length - 1) {
//                                writer.write("\n");
//                            }
//                        }
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                        try {
//                            if (writer != null)
//                                writer.close();
//                        } catch (IOException e1) {
//                            e1.printStackTrace();
//                        }
//                    }
//                }

                mBluetoothTool.writeCharacteristic(add, mSendBGC.get(add));
            } else {
                LogUtils.d(TAG, "mSendBluetoothGattCharacteristic == null");
                mBlueToothConnectStateListener.onBlueToothConnectState(add, BluetoothStatus
                        .STATE_NULL_WRITE_BGC);
            }
        } else {
            LogUtils.d(TAG, "BluetoothLeTool.STATE_DISCONNECTED  " + add);
        }

    }

    public synchronized void sendData(String add, String value) {
        LogUtils.d(TAG, "sendData  add=" + add + "  value=" + value);

        if (mBluetoothTool.getAllConnectionStateMap().get(add) == BluetoothStatus.STATE_CONNECTED) {
            if (mSendBGC.get(add) != null) {
                mSendBGC.get(add).setValue(value);
                mBluetoothTool.writeCharacteristic(add, mSendBGC.get(add));
            } else {
                LogUtils.d(TAG, "mSendBluetoothGattCharacteristic == null");
                mBlueToothConnectStateListener.onBlueToothConnectState(add, BluetoothStatus
                        .STATE_NULL_WRITE_BGC);
            }
        } else {
            LogUtils.d(TAG, "BluetoothLeTool.STATE_DISCONNECTED" + add);
        }

    }

    public synchronized boolean setCharacteristicNotification(String add) {
        return mBluetoothTool.setCharacteristicNotification(add, mNotifyBGC.get(add), true);


    }

    long lastTime, currentTime;

    public void onDataAvailable(String add, byte[] value) {

        if (mBluetoothAdapter.isDiscovering()) {
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
            handler.removeCallbacks(mRunnable);
        }
        if (mDataValuesChangedListener != null) {
            //TODO 限制心率计更新频率
            if (devicesMap.get(add).getType() == SampleGattAttributes.HRM) {
                currentTime = System.currentTimeMillis();
                if (currentTime - lastTime < 2000) {
                    return;
                } else {
                    mDataValuesChangedListener.onDataValuesChanged(add, value);
                    lastTime = currentTime;
                }
            } else {
                mDataValuesChangedListener.onDataValuesChanged(add, value);
            }
        }

    }

    public void onDiscovered(BluetoothGatt gatt) {
        String add = gatt.getDevice().getAddress();
        LogUtils.d(TAG, "onDiscovered   " + add);
        int notify = -333, send = -444;
        if (mBluetoothAdapter == null) {
            LogUtils.d(TAG, "onDiscovered mBluetoothAdapter==null");
        }
        if (gatt == null) {
            LogUtils.d(TAG, "onDiscovered gatt==null");
        }
        if (this.mBluetoothAdapter != null && gatt != null) {
            LogUtils.d(TAG, "onDiscovered this.mBluetoothAdapter != null && gatt != null");
            BleDeviceBean bleDevice = devicesMap.get(gatt.getDevice().getAddress());

            LogUtils.d(TAG, "LLLLLLLLLLL   " + bleDevice.getMac()
                    + "  " + gatt.getDevice().getAddress() + "  "
                    + bleDevice.getServiceUUID().toString()
            );
            BluetoothGattService mGattService = gatt.getService(bleDevice.getServiceUUID());

            for (int i = 0; i < gatt.getServices().size(); i++) {
                LogUtils.d(TAG, "LLLLLL服务的uuid="+i+"  " + gatt.getServices().get(i).getUuid());
            }
            if (mGattService != null) {
                LogUtils.d(TAG, "onDiscovered mGattService != null");
                BluetoothGattCharacteristic mBluetoothGattCharacteristic = mGattService
                        .getCharacteristic(bleDevice.getNotifyUUID());
                if (mBluetoothGattCharacteristic != null) {
                    mNotifyBGC.put(gatt.getDevice().getAddress(), mBluetoothGattCharacteristic);
                    LogUtils.d(TAG, "onDiscovered  mBluetoothGattCharacteristic != null");
                    if (gatt.setCharacteristicNotification(mBluetoothGattCharacteristic, true)) {
                        LogUtils.d(TAG, "onDiscovered  setCharacteristicNotification true");
                        BluetoothGattDescriptor clientConfig =
                                mBluetoothGattCharacteristic.getDescriptor(bleDevice
                                        .getConfigUUID());
                        if (clientConfig != null) {
                            LogUtils.d(TAG, "onDiscovered  clientConfig != null");
                            boolean ok = clientConfig.setValue(BluetoothGattDescriptor
                                    .ENABLE_NOTIFICATION_VALUE);
                            LogUtils.d(TAG, "setCharacteristicNotification characteristic" +
                                    ".getUuid=" + mBluetoothGattCharacteristic.getUuid()
                                    + "clientConfig.getUuid=" + clientConfig.getUuid());
                            if (ok) {
                                if (gatt.writeDescriptor(clientConfig)) {
                                    LogUtils.d(TAG, "onDiscovered  writeDescriptor true");
                                    notify = STATE_NOTYFY_SUCCESS;
                                } else {
                                    notify = STATE_NOTYFY_FAILED;
                                }
                            } else {
                                notify = STATE_NOTYFY_FAILED;
                            }
                        } else {
                            LogUtils.d(TAG, "onDiscovered  clientConfig == null");
                            notify = STATE_NOTYFY_FAILED;
                        }
                    } else {
                        LogUtils.d(TAG, "onDiscovered  setCharacteristicNotification false");
                        notify = STATE_NOTYFY_FAILED;
                    }
                } else {
                    notify = STATE_NOTYFY_FAILED;
                }

                BluetoothGattCharacteristic mSendBluetoothGattCharacteristic = mGattService
                        .getCharacteristic(bleDevice.getSendUUID());
                if (mSendBluetoothGattCharacteristic == null) {
                    send = STATE_SEND_NOT_READY;
                } else {
                    mSendBGC.put(gatt.getDevice().getAddress(), mSendBluetoothGattCharacteristic);
                    send = STATE_SEND_READY;
                }
            }
            if (devicesMap.keySet().contains(add))
                devicesMap.get(add).setState(send + notify);
            mBlueToothConnectStateListener.onBlueToothConnectState(bleDevice.getMac(), send +
                    notify);
        }

    }

    @Override
    public void onBlueToothConnectState(String add, int state) {
        BleDeviceBean deviceBean = devicesMap.get(add);
        if (deviceBean != null) {
            devicesMap.get(add).setState(state);
            mBlueToothConnectStateListener.onBlueToothConnectState(add, state);
        }
    }

}
