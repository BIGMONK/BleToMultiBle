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
import android.support.annotation.NonNull;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;

import youtu.bletomultible.bluetooth.BluetoothStatus;
import youtu.bletomultible.bluetooth.SampleGattAttributes;


/**
 * Created by black on 2016/4/12.
 */
public class BlueToothLeManager extends Thread implements
        BluetoothLeTool.BluetoothLeDataListener,
        BluetoothStatus,
        BluetoothLeTool.BluetoothLeDiscoveredListener,
        BluetoothLeTool.BluetoothLeStatusListener {


    public interface BlueToothConnectStateChangedListener {
        void onBlueToothConnectState(String add, int state);
    }

    public interface DataValuesChangedListener {
        void onDataValuesChanged(String add, byte[] values);
    }

    private BlueToothConnectStateChangedListener mBlueToothConnectStateListener;
    private DataValuesChangedListener mDataValuesChangedListener;

    public void setBlueToothConnectStateChangedListener(BlueToothConnectStateChangedListener listener) {
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

    public void initBlueToothInfo(@NonNull HashMap<String, BleDeviceBean> mDeviceMap) {
        Log.d(TAG, "initBlueToothInfo");
        this.devicesMap = mDeviceMap;
        if (mBluetoothTool == null) {
            mBluetoothTool = new BluetoothLeTool(mDeviceMap);
        } else {
            mBluetoothTool.updateBluetoothLeTool(mDeviceMap);
        }


        if (!mBluetoothTool.initialize()) {
            Log.i(TAG, "Unable to initialize Bluetooth");
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

        Log.i(TAG, "mBluetoothAdapter=" + String.valueOf(mBluetoothAdapter));

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

    private void scanLeDevice(final boolean enable) {
        if (enable) {
            // Stops scanning after a pre-defined scan period.
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (mBluetoothTool.getAllConnectionState() == BluetoothStatus.STATE_DISCONNECTED) {
                        for (String add : mBluetoothTool.getAllConnectionStateMap().keySet()) {
                            if (mBluetoothTool.getAllConnectionStateMap().get(add)==BluetoothStatus.STATE_SCAN_TIMEOUT) {
                                devicesMap.get(add).setState(BluetoothStatus.STATE_SCAN_TIMEOUT);
                                mBlueToothConnectStateListener.onBlueToothConnectState(add, BluetoothStatus.STATE_SCAN_TIMEOUT);
                            }
                        }
//                        handler.postDelayed(this,10000);
                    } else if (mBluetoothTool.getAllConnectionState() == BluetoothStatus.STATE_CONNECTED) {
//                        mBluetoothAdapter.stopLeScan(mLeScanCallback);
                    }
                }
            }, 10000);
            for (String add : mBluetoothTool.getAllConnectionStateMap().keySet()) {
                devicesMap.get(add).setState(BluetoothStatus.STATE_SCANING);
                mBlueToothConnectStateListener.onBlueToothConnectState(add, BluetoothStatus.STATE_SCANING);
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
                    if (!mLeDevices.contains(device)) {
                        mLeDevices.add(device);
                        Log.d(TAG, "BlueToothLeManager onLeScan device="
                                + device.getAddress() + "   " + device.getName());
                    }
                    final String deviceName = device.getName();
                    final String deviceAdd = device.getAddress();
                    //TODO 需要判断设备连接状态
                    if (devicesMap != null && devicesMap.containsKey(deviceAdd) &&
                            (devicesMap.get(deviceAdd).getState() == BluetoothStatus.STATE_DISCONNECTED
                                    || devicesMap.get(deviceAdd).getState() == BluetoothStatus.STATE_SCANING)) {
                        Log.d(TAG + "蓝牙设备findByAdd：", deviceName + "  " + deviceAdd);
                        connectDevice(device.getAddress());
                    }
                }
            };

    /**
     * 根据MAC连接蓝牙设备
     *
     * @param address
     * @return
     */
    public boolean connectDevice(String address) {

        if (mBluetoothTool != null) {
            final boolean result = mBluetoothTool.connect(address);
            Log.i(TAG, "connectDevice result=" + result);

            return result;
        }
        return false;
    }

    public boolean reConnectDevice(String add) {

        if (mBluetoothTool != null && add != null && devicesMap.containsKey(add)) {
            final boolean result = mBluetoothTool.connect(add);
            Log.i(TAG, "reConnectDevice result=" + result);
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
            } else {
                Log.d(TAG, "mSendBluetoothGattCharacteristic == null");
            }
            mBluetoothTool.writeCharacteristic(add, mSendBGC.get(add));
        }

    }

    public void sendData(String add, String value) {
        if (mBluetoothTool.getAllConnectionStateMap().get(add) == BluetoothStatus.STATE_CONNECTED) {
            if (mSendBGC.get(add) != null) {
                mSendBGC.get(add).setValue(value);
            } else {
                Log.d(TAG, "mSendBluetoothGattCharacteristic == null");
            }
            mBluetoothTool.writeCharacteristic(add, mSendBGC.get(add));
        } else {
            Log.d(TAG, "BluetoothLeTool.STATE_DISCONNECTED");
        }

    }

    public boolean setCharacteristicNotification(String add) {
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
        Log.d(TAG, "onDiscovered   " + add);
        int notify = -333, send = -444;
        if (mBluetoothAdapter == null) {
            Log.d(TAG, "onDiscovered mBluetoothAdapter==null");
        }
        if (gatt == null) {
            Log.d(TAG, "onDiscovered gatt==null");
        }
        for (BluetoothGattService service : gatt.getServices()) {
            Log.d(TAG, "LLLLLLLLLLL发现服务" + service.getUuid());
        }
        if (this.mBluetoothAdapter != null && gatt != null) {
            Log.d(TAG, "onDiscovered this.mBluetoothAdapter != null && gatt != null");
            BleDeviceBean bleDevice = devicesMap.get(gatt.getDevice().getAddress());

            Log.d(TAG, "LLLLLLLLLLL   " + bleDevice.getMac()
                    + "  " + gatt.getDevice().getAddress() + "  "
                    + bleDevice.getServiceUUID().toString()
            );

            BluetoothGattService mGattService = gatt.getService(bleDevice.getServiceUUID());
            if (mGattService != null) {
                Log.d(TAG, "onDiscovered mGattService != null");
                BluetoothGattCharacteristic mBluetoothGattCharacteristic = mGattService.getCharacteristic(bleDevice.getNotifyUUID());
                if (mBluetoothGattCharacteristic != null) {
                    mNotifyBGC.put(gatt.getDevice().getAddress(), mBluetoothGattCharacteristic);
                    Log.d(TAG, "onDiscovered  mBluetoothGattCharacteristic != null");
                    if (gatt.setCharacteristicNotification(mBluetoothGattCharacteristic, true)) {
                        Log.d(TAG, "onDiscovered  setCharacteristicNotification true");
                        BluetoothGattDescriptor clientConfig =
                                mBluetoothGattCharacteristic.getDescriptor(bleDevice.getConfigUUID());
                        if (clientConfig != null) {
                            Log.d(TAG, "onDiscovered  clientConfig != null");
                            boolean ok = clientConfig.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                            Log.d(TAG, "setCharacteristicNotification characteristic.getUuid=" + mBluetoothGattCharacteristic.getUuid()
                                    + "clientConfig.getUuid=" + clientConfig.getUuid());
                            if (ok) {
                                if (gatt.writeDescriptor(clientConfig)) {
                                    Log.d(TAG, "onDiscovered  writeDescriptor true");
                                    notify = STATE_NOTYFY_SUCCESS;
                                } else {
                                    notify = STATE_NOTYFY_FAILED;
                                }
                            } else {
                                notify = STATE_NOTYFY_FAILED;
                            }
                        } else {
                            Log.d(TAG, "onDiscovered  clientConfig == null");
                            notify = STATE_NOTYFY_FAILED;
                        }
                    } else {
                        Log.d(TAG, "onDiscovered  setCharacteristicNotification false");
                        notify = STATE_NOTYFY_FAILED;
                    }
                } else {
                    notify = STATE_NOTYFY_FAILED;
                }

                BluetoothGattCharacteristic mSendBluetoothGattCharacteristic = mGattService.getCharacteristic(bleDevice.getSendUUID());
                if (mSendBluetoothGattCharacteristic == null) {
                    send = STATE_SEND_NOT_READY;
                } else {
                    mSendBGC.put(gatt.getDevice().getAddress(), mSendBluetoothGattCharacteristic);
                    send = STATE_SEND_READY;
                }
            }

            devicesMap.get(add).setState(send + notify);
            mBlueToothConnectStateListener.onBlueToothConnectState(bleDevice.getMac(), send + notify);
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
