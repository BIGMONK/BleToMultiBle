package youtu.bletomultible.bluetooth;


import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import youtu.bletomultible.utils.LogUtils;
import youtu.bletomultible.utils.UIUtils;


/**
 * Created by black on 2016/4/12.
 */
public class BluetoothLeTool implements BluetoothStatus {

    private final static String TAG = BluetoothLeTool.class.getSimpleName();

    public interface BluetoothLeDataListener {
        void onDataAvailable(String add, byte[] value);
    }

    public interface BluetoothLeDiscoveredListener {
        void onDiscovered(BluetoothGatt gatt);
    }

    public interface BluetoothLeStatusListener {
        void onBlueToothConnectState(String add, int state);
    }

    public BluetoothLeTool(Map<String, BleDeviceBean> mDeviceMap) {
        abandonList = new ArrayList<>();
        mBleAddress = new ArrayList();
        mBluetoothGattMap = new HashMap<>();
        mConnectionStateMap = new HashMap<>();
        for (String address : mDeviceMap.keySet()) {
            mConnectionStateMap.put(address, STATE_DISCONNECTED);
            mBleAddress.add(address);
        }
    }

    private ArrayList<String> abandonList;

    public void upBluetoothLeTool(Map<String, BleDeviceBean> mDeviceMap) {
        LogUtils.d(TAG,"upBluetoothLeTool");
        abandonList.clear();
        mBleAddress.clear();
        //废弃不用的
        for (String add : mBluetoothGattMap.keySet()) {
            if (!mDeviceMap.containsKey(add)) {
                abandonList.add(add);
                mBluetoothGattMap.get(add).disconnect();
                mConnectionStateMap.remove(add);
                LogUtils.d(TAG,"upBluetoothLeTool disconnect:"+add);
            }
        }
        //增加新来的
        for (String add : mDeviceMap.keySet()) {
            mBleAddress.add(add);
            if (!mBluetoothGattMap.containsKey(add)) {
                mConnectionStateMap.put(add, STATE_DISCONNECTED);
            }
        }


    }


    private BluetoothLeDataListener mBluetoothLeDataListener;
    private BluetoothLeDiscoveredListener mBluetoothLeDiscoveredListener;
    private BluetoothLeStatusListener mBluetoothLeStatusListener;
    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private ArrayList mBleAddress;
    private HashMap<String, BluetoothGatt> mBluetoothGattMap;
    private HashMap<String, Integer> mConnectionStateMap;

    public HashMap<String, Integer> getAllConnectionStateMap() {
        return mConnectionStateMap;
    }

    /**
     * 获取蓝牙设备连接状态
     *
     * @return
     */
    public int getAllConnectionState() {
        for (String add : mConnectionStateMap.keySet()) {
            if (mConnectionStateMap.get(add) != STATE_CONNECTED) {
                return STATE_DISCONNECTED;
            }
        }

        return STATE_CONNECTED;
    }


    private long mLastSendTimestamps;
    /**
     * 监听蓝牙连接状态和数据
     */
    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            String add = gatt.getDevice().getAddress();
            Log.i(TAG, "BluetoothGattCallback onConnectionStateChange add=" + add + "  state=" + newState);
            if (!abandonList.contains(add)) {
                mConnectionStateMap.put(add, newState);

                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    Log.i(TAG, "Connected to GATT server.");
                    // Attempts to discover services after successful connection.
                    Log.i(TAG, "Attempting to start service discovery:" +
                            mBluetoothGattMap.get(add).discoverServices());
                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    Log.i(TAG, "Disconnected from GATT server.");
                    if (mBluetoothGattMap.get(add) != null)
                        mBluetoothGattMap.get(add).close();
                }
                mBluetoothLeStatusListener.onBlueToothConnectState(add, newState);
            } else {
                Log.i(TAG, "BluetoothGattCallback onConnectionStateChange 已废弃add=" + add + "  state=" + newState);
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            Log.d(TAG, "onServicesDiscovered");
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d(TAG, "onServicesDiscovered 发现服务" + gatt.getServices().size());
                if (mBluetoothLeDiscoveredListener != null) {
                    mBluetoothLeDiscoveredListener.onDiscovered(gatt);
                }
            } else {
                Log.w(TAG, "onServicesDiscovered received: " + status);
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic,
                                         int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                byte[] bs = characteristic.getValue();
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < bs.length; i++) {
                    sb.append(bs[i] + "\t");
                }
                Log.d(TAG, "onCharacteristicRead" + sb.toString());
            }

        }


        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {
            final byte[] data = characteristic.getValue();
            String add = gatt.getDevice().getAddress();
            if (mBluetoothLeDataListener != null && !abandonList.contains(add)) {
                mBluetoothLeDataListener.onDataAvailable(add, data);
            }

        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {

            if (status == BluetoothGatt.GATT_SUCCESS) {

                Log.d(TAG, gatt.getDevice().getAddress() + "onCharacteristicWrite SUCCESS  ");
            } else {
                Log.d(TAG, "onCharacteristicWrite ERR    ");
            }
        }

        @Override
        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorRead(gatt, descriptor, status);
            Log.d(TAG, "onDescriptorRead :" + descriptor.getUuid());
        }
    };

    public void setBluetoothLeDataListener(BluetoothLeDataListener listener) {
        mBluetoothLeDataListener = listener;
    }

    public void setBluetoothLeDiscoveredListener(BluetoothLeDiscoveredListener listener) {
        mBluetoothLeDiscoveredListener = listener;
    }

    public void setBluetoothLeStatusListener(BluetoothLeStatusListener listener) {
        mBluetoothLeStatusListener = listener;
    }


    public boolean initialize() {
        // For API level 18 and above, get a reference to BluetoothAdapter through
        // BluetoothManager.
        if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager) UIUtils.getContext().getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager == null) {
                Log.e(TAG, "Unable to initialize BluetoothManager.");
                return false;
            }
        }

        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            Log.e(TAG, "Unable to obtain a BluetoothAdapter.");
            return false;
        }

        return true;
    }

    public synchronized boolean connect(final String address) {
        Log.i(TAG, "connect");
        if (mBluetoothAdapter == null || address == null) {
            Log.i(TAG, "BluetoothAdapter not initialized or unspecified address.");
            return false;
        }

        // Previously connected device.  Try to reconnect.
//        if (mBleAddress.contains(address) && mBluetoothGattMap.containsKey(address)) {
//            Log.i(TAG, "Trying to use an existing mBluetoothGatt for connection.  " + address);
//            if (mBluetoothGattMap.get(address).connect()) {
//                mConnectionStateMap.put(address, STATE_CONNECTING);
//                mBluetoothLeStatusListener.onBlueToothConnectState(address, STATE_CONNECTING);
//                return true;
//            } else {
//                return false;
//            }
//        }
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        if (device == null) {
            Log.w(TAG, "Device not found.  Unable to connect.");
            return false;
        }
        mBluetoothGattMap.put(address, device.connectGatt(UIUtils.getContext(), false, mGattCallback));
        Log.d(TAG, "Trying to create a new connection.");
        mBluetoothLeStatusListener.onBlueToothConnectState(address, STATE_CONNECTING);
        return true;
    }

    public synchronized void disconnect(String add) {
        if (mBluetoothAdapter == null || mBluetoothGattMap.get(add) == null) {
            Log.w(TAG, "BluetoothAdapter not initialized   " + add);
            return;
        }
        mBluetoothGattMap.get(add).disconnect();
        mBluetoothGattMap.remove(add);
    }

    public synchronized void close() {
        for (String add : mBluetoothGattMap.keySet()) {
            if (mBluetoothGattMap.get(add) != null) {
                mBluetoothGattMap.get(add).close();
            }
        }
        mBluetoothGattMap.clear();

    }

    public synchronized void readCharacteristic(String add, BluetoothGattCharacteristic characteristic) {
        if (mBluetoothAdapter == null || mBluetoothGattMap.get(add) == null) {
            Log.w(TAG, "BluetoothAdapter not initialized  " + add);
            return;
        }
        mBluetoothGattMap.get(add).readCharacteristic(characteristic);
    }

    public synchronized void writeCharacteristic(String add, BluetoothGattCharacteristic characteristic) {
        LogUtils.d(TAG,"writeCharacteristic  add="+add +"数据:"+characteristic.getValue().toString()
                +"服务UUID："+characteristic.getService().getUuid().toString()
                +"写通道UUID： "+characteristic.getUuid().toString()
        );
        if (mBluetoothAdapter == null || mBluetoothGattMap.get(add) == null) {
            Log.w(TAG, "BluetoothAdapter not initialized  " + add);
            return;
        }
        mBluetoothGattMap.get(add).writeCharacteristic(characteristic);
    }

    public synchronized boolean setCharacteristicNotification(String add, BluetoothGattCharacteristic characteristic, boolean enabled) {
        if (mBluetoothAdapter == null || mBluetoothGattMap.get(add) == null || characteristic == null) {
            Log.w(TAG, "BluetoothAdapter not initialized  " + add);
            return false;
        }
        if (!mBluetoothGattMap.get(add).setCharacteristicNotification(characteristic, enabled)) {
            Log.d(TAG, "setCharacteristicNotification  false  " + add);
            return false;
        }

        BluetoothGattDescriptor descriptor = characteristic.getDescriptor((SampleGattAttributes.CLIENT_CHARACTERISTIC_CONFIG));

        if (descriptor == null) {
            Log.d(TAG, String.format("getDescriptor for notify null! " + add));
            return false;
        }

        byte[] value = (enabled ? BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE : BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);

        if (!descriptor.setValue(value)) {
            Log.d(TAG, String.format("setValue for notify descriptor failed! " + add));
            return false;
        }

        if (!mBluetoothGattMap.get(add).writeDescriptor(descriptor)) {
            Log.d(TAG, String.format("writeDescriptor for notify failed " + add));
            return false;
        }
        return true;
    }
}
