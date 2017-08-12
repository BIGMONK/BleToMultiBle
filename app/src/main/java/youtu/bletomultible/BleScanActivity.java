package youtu.bletomultible;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Message;
import android.os.ParcelUuid;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import cc.bodyplus.sdk.ble.manger.BleConnectionInterface;
import cc.bodyplus.sdk.ble.utils.BleUtils;
import cc.bodyplus.sdk.ble.utils.DeviceInfo;
import cc.bodyplus.sdk.ble.utils.MyBleDevice;
import youtu.bletomultible.bluetooth.SampleGattAttributes;
import youtu.bletomultible.utils.LogUtils;
import youtu.bletomultible.utils.UTBleUtils;

import static cc.bodyplus.sdk.ble.manger.BleService.RE_BOND_DEVICE;
import static cc.bodyplus.sdk.ble.manger.BleService.RE_SCAN_DEVICE;


public class BleScanActivity extends AppCompatActivity implements BleConnectionInterface {
    private static final int REQUEST_ENABLE_BT = 1;

    private ListView deviceList;
    private Button button;
    //	private BleOperateFunction mBleOperate;
    private LeDeviceListAdapter mLeDeviceListAdapter;
    protected boolean isScanning;
    protected BluetoothAdapter mBluetoothAdapter;
    private String TAG = this.getClass().getSimpleName();
    private HashMap<String, BluetoothDevice> hashMap = new HashMap<>();
    // Device scan callback.
    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback
            () {
        @Override
        public void onLeScan(final BluetoothDevice device, final int rssi, final byte[]
                scanRecord) {
            if (!hashMap.containsKey(device.getAddress())) {
                hashMap.put(device.getAddress(), device);
                StringBuilder sb = new StringBuilder();
                if (scanRecord == null) {
                    sb.append("null");
                } else if (scanRecord.length == 0) {
                    sb.append("empty");
                } else {
                    sb.append(device.getName() + "  " + device.getAddress() + "  scanRecord长度：" +
                            scanRecord.length + "  内容：");
                    for (byte b : scanRecord) {
                        sb.append(b + " ");
                    }
                }

                LogUtils.d(TAG, "发现解析设备：" + device.getName() + "  " + device.getAddress() + "  ");
                ParcelUuid[] us = device.getUuids();
                if (us != null)
                    for (int i = 0; i < us.length; i++) {
                        LogUtils.d(TAG, "解析设备自带UUID：" + us[i].toString());

                    }
                //BleUtils bodyplus SDK工具类
                List<UUID> uuids = UTBleUtils.parseUuids(scanRecord);
                for (int i = 0; i < uuids.size(); i++) {
                    LogUtils.d(TAG, "工具解析设备:" + device.getName()
                            + "  UUID：" + i + "  " + uuids.get(i).toString());
                }
//                if (UTBleUtils.isFilterMyUUID(scanRecord)) {
                SparseArray<byte[]> recodeArray = UTBleUtils.parseFromBytes(scanRecord);
                for (int i = 0; i < recodeArray.size(); i++) {
                    int key = recodeArray.keyAt(i);
                    byte[] values = recodeArray.get(key);
                    LogUtils.d(TAG, "工具解析设备:" + device.getName()
                            + "广播包数据 i=" + i + "  key=" + key + "  values=" + BleUtils
                            .byteToChar(values));
                }

                byte[] b = recodeArray.get(0xffff);
                if (b != null && b.length > 0) {
                    LogUtils.d(TAG, "解析设备号1：" + UTBleUtils.byteToChar(b) + "  " + UTBleUtils
                            .isFilterMyUUID(scanRecord));
                }
                Log.e(TAG, "--------------------------------");
//                }

            }

            Log.d(TAG, "LeScanCallback:  Name:" + device.getName()
                    + "\nMAC:" + device.getAddress());
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Bledevices leDevice = new Bledevices();
                    leDevice.device = device;
                    leDevice.singal = rssi;
                    leDevice.scanRecord = scanRecord;
                    mLeDeviceListAdapter.addDevice(leDevice);
                    mLeDeviceListAdapter.notifyDataSetChanged();
                }
            });
        }
    };

    public boolean isBleSupport() {
        // 检查当前手机是否支持ble 蓝牙,如果不支持退出程序
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            return false;
        }
        if (mBluetoothAdapter == null) {
            return false;
        }
        return true;
    }

    public boolean checkBleIsOpen() {
        return mBluetoothAdapter.isEnabled();
    }

    @SuppressWarnings("deprecation")
    public void BleScanDevice(boolean enable) {
        if (enable) {
            isScanning = true;
            mBluetoothAdapter.startLeScan(mLeScanCallback);
        } else {
            isScanning = false;
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ble_scan);

        deviceList = (ListView) findViewById(R.id.device_list);
        button = (Button) findViewById(R.id.button);

//        BleConnectionManger.getInstance().addConnectionListener(this, false); // 注册蓝牙监听心率衣服
//        BleConnectionManger.getInstance().autoConnectBle("2004020349");

        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context
                .BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        mBluetoothAdapter.enable();
        mLeDeviceListAdapter = new LeDeviceListAdapter();
        deviceList.setAdapter(mLeDeviceListAdapter);

        button.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                if (isScanning) {
                    BleScanDevice(false);
                    button.setText("Start scan");
                } else {
                    mLeDeviceListAdapter.clear();
                    BleScanDevice(true);
                    button.setText("Stop scan");
                }
            }
        });


        deviceList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                final Bledevices bleDevice = mLeDeviceListAdapter.getDevice(position);
                if (bleDevice == null)
                    return;
                final String add = bleDevice.device.getAddress();
                if (isScanning) {
                    BleScanDevice(false);
                }

                AlertDialog.Builder builder = new AlertDialog.Builder(BleScanActivity.this);

                builder.setTitle("蓝牙设备类型选择：");//1  手环   2  主控板   3 计步器   4  三角心率计
                String[] choice = new String[]{SampleGattAttributes.HAND_BAND_NAME
                        , SampleGattAttributes.MAINBOARD_NAME
                        , SampleGattAttributes.STEPER_NAME
                        , SampleGattAttributes.HRM_NAME
                        , SampleGattAttributes.NEW_BLE_MAINBOARD_NAME
                };
                final int[] choiceType = {SampleGattAttributes.HAND_BAND
                        , SampleGattAttributes.MAINBOARD
                        , SampleGattAttributes.STEPER
                        , SampleGattAttributes.HRM
                        , SampleGattAttributes.NEW_BLE_MAINBOARD//新主控板和计步器一样
                };
                builder.setSingleChoiceItems(choice, 0,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = null;
                                if (choiceType[which] == SampleGattAttributes.NEW_BLE_MAINBOARD) {
                                    intent = new Intent(BleScanActivity.this, WifiMainBoardBleTest
                                            .class);
                                } else if (choiceType[which] == SampleGattAttributes.MAINBOARD) {
                                    intent = new Intent(BleScanActivity.this, MainActivity
                                            .class);
                                }
                                if (intent != null) {
                                    intent.putExtra("address", add);
                                    intent.putExtra("name", bleDevice.device.getName() + "");
                                    intent.putExtra("type", choiceType[which]);
                                    intent.putExtra("scanRecord", bleDevice.scanRecord);
                                    startActivity(intent);
                                }
                                dialog.dismiss(); // 让窗口消失
                            }
                        }

                );
                builder.create().show();

            }
        });
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        if (!isBleSupport()) {
            Toast.makeText(this, "blue tooth not support", Toast.LENGTH_SHORT).show();
            finish();
        }

        if (!checkBleIsOpen()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        } else {
            BleScanDevice(true);
            if (isScanning) {
                button.setText("Stop scan");
            }
        }
    }

    @Override
    protected void onDestroy() {
//        BleConnectionManger.getInstance().removeConnectionListener(this); // 移除监听蓝牙心率衣服
        super.onDestroy();
        BleScanDevice(false);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // User chose not to enable Bluetooth.
        if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_CANCELED) {
            finish();
            return;
        } else if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_OK) {
            BleScanDevice(true);
            if (isScanning) {
                button.setText("Stop scan");
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        mLeDeviceListAdapter.clear();
    }


    @Override
    public void bleDispatchMessage(Message msg) {
        switch (msg.what) {
            case RE_SCAN_DEVICE:
                ArrayList<MyBleDevice> myBleDevices = (ArrayList<MyBleDevice>) msg.obj;
                for (int i = 0; i < myBleDevices.size(); i++) {
                    Log.d(TAG, "BodyPlus bleDispatchMessage:搜索结果msg.what= " + msg.what
                            + " myBleDevices.get(" + i + ")=" + "  DeviceName=" + myBleDevices
                            .get(i).getDeviceName()
                            + "  MacAddress=" + myBleDevices.get(i).getMacAddress()
                            + "  DeviceSn=" + myBleDevices.get(i).getDeviceSn()
                            + "  Rssi=" + myBleDevices.get(i).getRssi());
                }
                break;
            case RE_BOND_DEVICE:
                Toast.makeText(this, "连接成功！", Toast.LENGTH_LONG).show();
                DeviceInfo deviceInfo = (DeviceInfo) msg.obj; // 这是由搜索连接得到的 DeviceInfo对象，该对象中有完整的信息
                Log.d(TAG, "BodyPlus bleDispatchMessage:连接成功 msg.what= " + msg.what
                        + "  deviceInfo: "
                        + " DeviceName=" + deviceInfo.bleName
                        + " 硬件版本=" + deviceInfo.hwVn
                        + " 固件版本=" + deviceInfo.swVn
                        + " 序列号=" + deviceInfo.sn);
                break;
        }
    }

    @Override
    public void bleDataCallBack(int code, int dm) {
        Log.d(TAG, "BodyPlus bleDataCallBack: code=" + code + "  dm=" + dm);
    }

    @Override
    public void bleHeartDataError() {
        Log.d(TAG, "BodyPlus bleHeartDataError: ");
    }

    @Override
    public void blePowerLevel(byte data) {
        Log.d(TAG, "BodyPlus blePowerLevel: " + data);
    }

    @Override
    public void bleReConnectDevice(DeviceInfo device) {
        DeviceInfo deviceInfo = device; // 这是由自动重连得到的
        // DeviceInfo对象，该对象中没有硬件版本信息（重连的时候不需要读取硬件版本信息，可以从已绑定信息中获取）
        Toast.makeText(this, "我是自动连接上的回调", Toast.LENGTH_LONG).show();
        Log.d(TAG, "BodyPlus bleReConnectDevice: " + device.toString());
    }

    @Override
    public void bleDeviceDisconnect() {
        Toast.makeText(this, "我是断开连接的回调", Toast.LENGTH_LONG).show();
        Log.d(TAG, "BodyPlus bleDeviceDisconnect: ");
    }

    @Override
    public void bleCoreModule(byte data) {
        Log.d(TAG, "BodyPlus bleCoreModule: " + data);
    }

    // Adapter for holding devices found through scanning.
    private class LeDeviceListAdapter extends BaseAdapter {
        private ArrayList<Bledevices> mLeDevices;
        private LayoutInflater mInflator;

        public LeDeviceListAdapter() {
            super();
            mLeDevices = new ArrayList<Bledevices>();
            mInflator = BleScanActivity.this.getLayoutInflater();
        }

        public void addDevice(Bledevices dev) {
            int i = 0;
            int listSize = mLeDevices.size();
            for (i = 0; i < listSize; i++) {
                if (mLeDevices.get(i).device.equals(dev.device)) {
                    mLeDevices.get(i).singal = dev.singal;
                    break;
                }
            }

            if (i >= listSize) {
                mLeDevices.add(dev);
            }

        }

        public Bledevices getDevice(int position) {
            return mLeDevices.get(position);
        }

        public void clear() {
            mLeDevices.clear();
        }

        @Override
        public int getCount() {
            return mLeDevices.size();
        }

        @Override
        public Object getItem(int i) {
            return mLeDevices.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            ViewHolder viewHolder;
            // General ListView optimization code.
            if (view == null) {
                view = mInflator.inflate(R.layout.listitem_device, null);
                viewHolder = new ViewHolder(view);
                view.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) view.getTag();
            }

            Bledevices bleDevice = mLeDevices.get(i);
            final String deviceName = bleDevice.device.getName();
            if (deviceName != null && deviceName.length() > 0) {
                viewHolder.deviceName.setText(deviceName);
            } else {
                viewHolder.deviceName.setText("Unknow device");
            }
            viewHolder.deviceAddress.setText(bleDevice.device.getAddress());
            viewHolder.deviceSignal.setText("" + bleDevice.singal + "dBm");

            StringBuilder sb = new StringBuilder();
            byte[] scanRecord = bleDevice.scanRecord;
            if (scanRecord == null) {
                sb.append("null");
            } else if (scanRecord.length == 0) {
                sb.append("empty");
            } else {
                sb.append("scanRecord长度：" + scanRecord.length + "  内容：");
                for (byte b : scanRecord) {
                    sb.append(b + " ");
                }
            }
            viewHolder.scanRecord.setText(sb.toString());
            return view;
        }
    }

    static class ParsedAd {
        byte flags;
        ArrayList<UUID> uuids = null;
        String localName;
        short manufacturer;

        public ParsedAd() {
            uuids = new ArrayList<>();
        }

        @Override
        public String toString() {
            return "ParsedAd{" +
                    "flags=" + flags +
                    ", uuids=" + uuids +
                    ", localName='" + localName + '\'' +
                    ", manufacturer=" + manufacturer +
                    '}';
        }
    }

    public static ParsedAd parseData(byte[] adv_data) {
        ParsedAd parsedAd = new ParsedAd();
        ByteBuffer buffer = ByteBuffer.wrap(adv_data);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        while (buffer.remaining() > 2) {
            byte length = buffer.get();
            if (length == 0)
                break;
            byte type = buffer.get();
            length -= 1;
            switch (type) {
                case 0x01: // Flags
                    parsedAd.flags = buffer.get();
                    length--;
                    break;
                case 0x02: // Partial list of 16-bit UUIDs
                case 0x03: // Complete list of 16-bit UUIDs
                case 0x14: // List of 16-bit Service Solicitation UUIDs
                    while (length >= 2) {
                        parsedAd.uuids.add(UUID.fromString(String.format(
                                "%08x-0000-1000-8000-00805f9b34fb", buffer.getShort())));
                        length -= 2;
                    }
                    break;
                case 0x04: // Partial list of 32 bit service UUIDs
                case 0x05: // Complete list of 32 bit service UUIDs
                    while (length >= 4) {
                        parsedAd.uuids.add(UUID.fromString(String.format(
                                "%08x-0000-1000-8000-00805f9b34fb", buffer.getInt())));
                        length -= 4;
                    }
                    break;
                case 0x06: // Partial list of 128-bit UUIDs
                case 0x07: // Complete list of 128-bit UUIDs
                case 0x15: // List of 128-bit Service Solicitation UUIDs
                    while (length >= 16) {
                        long lsb = buffer.getLong();
                        long msb = buffer.getLong();
                        parsedAd.uuids.add(new UUID(msb, lsb));
                        length -= 16;
                    }
                    break;
                case 0x08: // Short local device name
                case 0x09: // Complete local device name
                    byte sb[] = new byte[length];
                    buffer.get(sb, 0, length);
                    length = 0;
                    parsedAd.localName = new String(sb).trim();
                    break;
                case (byte) 0xFF: // Manufacturer Specific Data
                    parsedAd.manufacturer = buffer.getShort();
                    length -= 2;
                    break;
                default: // skip
                    break;
            }
            if (length > 0) {
                buffer.position(buffer.position() + length);
            }
        }
        return parsedAd;
    }

    class ViewHolder {
        TextView deviceName;
        TextView deviceAddress;
        TextView deviceSignal;
        TextView scanRecord;

        public ViewHolder(View view) {
            deviceAddress = (TextView) view.findViewById(R.id.device_address);
            deviceName = (TextView) view.findViewById(R.id.device_name);
            deviceSignal = (TextView) view.findViewById(R.id.signal);
            scanRecord = (TextView) view.findViewById(R.id.scanrecord);
        }
    }

    public class Bledevices {
        BluetoothDevice device;
        int singal;
        byte[] scanRecord;
    }
}
