package youtu.bletomultible;

import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cc.bodyplus.sdk.ble.manger.BleConnectionInterface;
import cc.bodyplus.sdk.ble.manger.BleConnectionManger;
import cc.bodyplus.sdk.ble.manger.BleService;
import cc.bodyplus.sdk.ble.parse.BleCmdConfig;
import cc.bodyplus.sdk.ble.utils.DeviceInfo;
import youtu.bletomultible.bluetooth.BleDeviceAdapter;
import youtu.bletomultible.bluetooth.BleDeviceBean;
import youtu.bletomultible.bluetooth.InputSystemManager;
import youtu.bletomultible.bluetooth.MainBoardCommand;
import youtu.bletomultible.bluetooth.SampleGattAttributes;
import youtu.bletomultible.bluetooth.hrm.HandleBleData;
import youtu.bletomultible.bluetooth.hrm.IBleOperateCallback;
import youtu.bletomultible.bluetooth.hrm.IUpdateCallBack;
import youtu.bletomultible.bluetooth.hrm.SmctConstant;
import youtu.bletomultible.utils.LogUtils;

public class MainActivity extends AppCompatActivity implements InputSystemManager
        .BlueToothDataValuesChangedListener,
        InputSystemManager.BlueToothConnectStateEvevtListener, IBleOperateCallback,
        BleConnectionInterface {
    private HashMap<String, BleDeviceBean> devicesMap = new HashMap<>();
    private InputSystemManager inputSystemManager;

    private String TAG = this.getClass().getSimpleName();
    @BindView(R.id.devicesList)
    RecyclerView devicesRecyclerView;
    private BleDeviceAdapter adapter;
    @BindView(R.id.edittext_data)
    EditText edittext_data;
    @BindView(R.id.text_ready_data)
    TextView text_ready_data;
    @BindView(R.id.text_receive_after_send)
    TextView text_receive_after_send;
    @BindView(R.id.btn_add_data)
    Button btn_add_data;
    @BindView(R.id.btn_clear_data)
    Button btn_clear_data;
    @BindView(R.id.btn_send_data)
    Button btn_send_data;
    @BindView(R.id.btn_start_update)
    Button btn_start_update;
    @BindView(R.id.btn_get_hard_version)
    Button btn_get_hard_version;
    @BindView(R.id.btn_get_soft_version)
    Button btn_get_soft_version;
    @BindView(R.id.btn_set_soft_version)
    Button btn_set_soft_version;
    @BindView(R.id.btn_set_hard_version)
    Button btn_set_hard_version;
    @BindView(R.id.btn_set_app_addr)
    Button btn_set_app_addr;
    @BindView(R.id.btn_set_file_length)
    Button btn_set_file_length;
    @BindView(R.id.btn_set_data)
    Button btn_set_data;
    @BindView(R.id.btn_reset)
    Button btn_reset;
    @BindView(R.id.btn_select)
    Button btn_select;
    @BindView(R.id.picker)
    PickerView pickerView;
    @BindView(R.id.progress)
    TextView progress;
    private int appAddr = 0x3400;
    private String appAddrpName = "update/a.bin";
    private boolean waiting;
    int hardVersion = 1;
    int softVersion = 1;
    private int sendCount;
    private int sentCount;
    private long startTime;
    private SimpleDateFormat sdf;
    private int fileByteLength = 1;
    private String mainBoardMac;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sdf = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
        sdf.setTimeZone(TimeZone.getTimeZone("GMT+00:00"));

        View view = View.inflate(this, R.layout.activity_main, null);

        setContentView(view);
        ButterKnife.bind(this);
        devicesMap = (HashMap<String, BleDeviceBean>) getIntent().getSerializableExtra("deviceMap");

        devicesMap = new HashMap<>();
        mainBoardMac = getIntent().getStringExtra("address");
        String mDevicesName = getIntent().getStringExtra("name");
        int type = getIntent().getIntExtra("type", 1);

        LogUtils.d(TAG, "add=" + mainBoardMac + "  name=" + mDevicesName + "  type=" + type);
        BleDeviceBean ble2 = new BleDeviceBean(mDevicesName, mainBoardMac, type);
        devicesMap.put(mainBoardMac, ble2);

        inputSystemManager = InputSystemManager.getInstance();
        inputSystemManager.registerBlueToothDataValuesChangedListener(this);
        //监听蓝牙连接状态
        inputSystemManager.setBlueToothConnectStateEvevtListener(this);
        inputSystemManager.initWithContext(this, devicesMap);

        adapter = new BleDeviceAdapter(this, devicesMap);
        //设置布局管理器
        devicesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        //设置Adapter
        devicesRecyclerView.setAdapter(adapter);

        List<Integer> data = new ArrayList<Integer>();
        for (int i = 0; i <= 10; i++) {
            data.add(i);
        }

        pickerView.setData(data);
        pickerView.setOnSelectListener(new PickerView.onSelectListener() {
            @Override
            public void onSelect(Integer text) {
                Toast.makeText(MainActivity.this, "选择了: " + text,
                        Toast.LENGTH_SHORT).show();
                hardVersion = text;
                softVersion = text;
            }
        });


        BleConnectionManger.getInstance().addConnectionListener(this, true); // 注册蓝牙监听心率衣服

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        inputSystemManager.unRegisterBlueToothDataValuesChangedListener(this);
        BleConnectionManger.getInstance().removeConnectionListener(this); // 移除监听

    }

    Message message;

    @Override
    public void onBlueToothDataValuesChanged(int type, String add, byte[] values) {

        StringBuilder stringBuilder = new StringBuilder();
        if (values == null) {
            return;
        }
        for (int h = 0; h < values.length; h++) {
            stringBuilder.append(values[h] + "    ");
        }
        Log.d(TAG, "DataValuesChanged：" + add + "type=" + type + "   " + values.length
                + "  数据：" + stringBuilder.toString());

        if (type == SampleGattAttributes.MAINBOARD && values.length == 9 && values[0] == 0x55 &&
                values[8] == (byte) 0xAA) {//主控板数据
            if (values[1] == 0x01) {//数据接收
                devicesMap.get(add).setValues(values);
                handler.sendEmptyMessage(999);
            } else {//指令反馈
                message = handler.obtainMessage();
                if (message == null) {
                    message = new Message();
                }
                Bundle bundle = new Bundle();
                if (values[1] == (byte) 0xFF) {
                    //3 设置指令成功
                    //0x55 0xFF 0x4F 0x4B 0x00 0x00 0x00 0x00 0xAA
                    if (values[2] == 0x4F && values[3] == 0x4B) {
                        bundle.putString("msg", "设置指令成功");
                        if (waiting) {
                            handler.sendEmptyMessageDelayed(555, 20);
                        }
                    }
                    //4 设置指令错误
                    // 0x55 0xFF 0x45 0x52 0x52 0x00 0x00 0x00 0xAA
                    else if (values[2] == 0x45 && values[3] == 0x52 && values[4] == 0x52) {
                        bundle.putString("msg", "设置指令失败");
                    }
                } else if (values[1] == (byte) 0x81) {

                    //软件版本号
                    int codes = (values[3] < 0 ? values[3] & 255 : values[3]) * 255 +
                            (values[2] < 0 ? values[2] & 255 : values[2]);
                    bundle.putString("msg", "当前软件版本号：" + codes);

                } else if (values[1] == (byte) 0x82) {
                    //硬件版本号
                    int codes = (values[3] < 0 ? values[3] & 255 : values[3]) * 255 +
                            (values[2] < 0 ? values[2] & 255 : values[2]);
                    bundle.putString("msg", "当前硬件版本号：" + codes);
                }
                message.setData(bundle);
                message.what = 666;
                devicesMap.get(add).setValues(values);
                handler.sendMessage(message);

            }

        } else if (type == SampleGattAttributes.HAND_BAND && values[6] == 3) {   //手环就绪
            inputSystemManager.sendData(add, new byte[]{(byte) -85, (byte) 0, (byte) 4, (byte)
                    -1, (byte) 49, (byte) 0x0a, (byte) 1});
        } else if (type == SampleGattAttributes.HRM) {//心率计
            //TODO 处理心率计数据
            Log.d(TAG, "心率计有数据");
            HandleBleData.HandleData(values, this, (IUpdateCallBack) null);
        }

    }

    int recColorInde = 0;

    @Override
    public void onBlueToothConnectStateChanged(int type, String add, int state) {
        Log.d(TAG, add + "   状态     " + state);
        devicesMap.get(add).setState(state);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                adapter.notifyDataSetChanged();
            }
        });
    }

    @OnClick({R.id.btn_add_data, R.id.btn_clear_data, R.id.btn_send_data, R.id.btn_start_update,
            R.id.btn_get_soft_version, R.id.btn_get_hard_version, R.id.btn_set_soft_version,
            R.id.btn_set_hard_version, R.id.btn_set_app_addr, R.id.btn_set_file_length,
            R.id.btn_set_data, R.id.btn_reset, R.id.btn_select
            , R.id.btn_body
            , R.id.btn_body_disconnect
    })
    public void Onclick(View view) {
        switch (view.getId()) {
            case R.id.btn_body_disconnect:
                BleConnectionManger.getInstance().disconnect();
                break;
            case R.id.btn_body:
                BleConnectionManger.getInstance().autoConnectBle("2004020349");
                break;
            case R.id.btn_add_data:
                submit();
                break;
            case R.id.btn_clear_data:
                for (int i = 0; i < dataRead.length; i++) {
                    dataRead[i] = 0;
                }
                dex = 0;
                dataReadyString.delete(0, dataReadyString.length());
                text_ready_data.setText(dataReadyString.toString());
                break;
            case R.id.btn_send_data:
                break;

            //1 开始固件升级
            case R.id.btn_start_update:
                inputSystemManager.sendData(mainBoardMac,
                        MainBoardCommand.START_UPDATE);
                break;

            case R.id.btn_select:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("升级程序选择：");
                try {
                    String[] files = getAssets().list("update");

                    final String[] choice = new String[files.length];
                    for (int i = 0; i < choice.length; i++) {
                        choice[i] = "update/" + files[i];
                    }
                    builder.setSingleChoiceItems(choice, 0,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                    appAddrpName = choice[which];
                                    btn_select.setText("选择固件：" + appAddrpName);
                                    if (choice[which].contains("A")) {
                                        appAddr = 0x3400;
                                    } else {
                                        appAddr = 0x9800;
                                    }
                                    LogUtils.d(TAG, "setSingleChoiceItems which=" + which + "   " +
                                            "appAddr=0x" + Integer.toHexString(appAddr));

                                    dialog.dismiss(); // 让窗口消失

                                }
                            }

                    );
                    builder.create().show();
                } catch (IOException e) {
                    e.printStackTrace();
                }


                break;
            // 8 获取软件版本号
            case R.id.btn_get_soft_version:
                inputSystemManager.sendData(mainBoardMac,
                        MainBoardCommand.GET_SOFT_VERSION);
                break;
            // 9 获取硬件版本号
            case R.id.btn_get_hard_version:
                inputSystemManager.sendData(mainBoardMac,
                        MainBoardCommand.GET_HARD_VERSION);
                break;
            //  2 设置软件版本号
            case R.id.btn_set_soft_version:
                inputSystemManager.sendData(mainBoardMac,
                        MainBoardCommand.makeSoftVersion(softVersion));
                Toast.makeText(this, "设置软件版本号：" + softVersion, Toast
                        .LENGTH_SHORT).show();
                btn_set_soft_version.setText("设置软件版本号：" + softVersion);
                break;
            // 3 设置硬件版本号
            case R.id.btn_set_hard_version:
                inputSystemManager.sendData(mainBoardMac, MainBoardCommand.makeHardVersion
                        (hardVersion));
                Toast.makeText(this, "设置硬件版本号：" + hardVersion, Toast
                        .LENGTH_SHORT).show();
                btn_set_hard_version.setText("设置硬件版本号：" + softVersion);

                break;
            //   4 app_addr
            // 0xAA 0x13 0x00 0x00 0x00 0x00 0x00 0x00 0x55 （app_addr（0x3400或者0x9800）高位在前，低位在后）
            case R.id.btn_set_app_addr:
                inputSystemManager.sendData(mainBoardMac,
                        MainBoardCommand.makeAppAddress(appAddr));
                LogUtils.d(TAG, "升级设置地址：0x" + Integer.toHexString(appAddr));
                Toast.makeText(this, "升级设置地址：0x" + Integer.toHexString(appAddr), Toast
                        .LENGTH_SHORT).show();
                btn_set_app_addr.setText("升级设置地址:" + Integer.toHexString(appAddr));
                break;
            //  5 文件长度
            //  0xAA 0x14 0x00 0x00 0x00 0x00 0x00 0x00 0x55 （文件长度，字节数/2，高位在前，低位在后）
            case R.id.btn_set_file_length:
                //inputSystemManager.sendData(mainBoardMac,
                //new byte[]{(byte) 0xAA, (byte) 0x14, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x55});
                InputStream input = null;
                try {
                    if (TextUtils.isEmpty(appAddrpName)) {
                        Toast.makeText(this, "未选中升级程序", Toast.LENGTH_SHORT).show();
                        break;
                    }
                    input = getResources().getAssets().open(appAddrpName);
                    BufferedInputStream bis = new BufferedInputStream(input);
                    fileByteLength = bis.available() / 2 + bis.available() % 2;
                    Log.d(TAG, "LLLLLLL实际字节数=" + bis.available() +
                            "LLLLLLL除以2发送次数（每次两位）=" + fileByteLength +
                            "LLLLLLL高位=" + (byte) (fileByteLength / 256) +
                            "LLLLLLL低位=" + (byte) (fileByteLength % 256));

                    inputSystemManager.sendData(mainBoardMac, MainBoardCommand.makeFileLength
                            (fileByteLength));
                    Toast.makeText(this, "升级文件长度：" + bis.available() + "  发送次数：" +
                            fileByteLength, Toast.LENGTH_SHORT).show();
                    btn_set_file_length.setText("文件长度：" + bis.available() + "  发送次数：" +
                            fileByteLength);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;


            //  6 data写入升级包

            case R.id.btn_set_data:
                if (!isRunableRunning) {
                    new Thread(runable).start();
                } else {
                    Log.d(TAG, "isRunableRunning  = true");
                }
                break;
            // 7 强制复位
            //  0xAA 0xFA 0xFB 0xFC 0xFD 0xFE 0x00 0x00 0x55
            case R.id.btn_reset:
                inputSystemManager.sendData(mainBoardMac,
                        MainBoardCommand.RESET);
                break;
        }
    }

    byte[] dataRead = new byte[20];
    int dex = 0;
    StringBuilder dataReadyString = new StringBuilder();

    private void submit() {
        // validate
        String data = edittext_data.getText().toString().trim();
        if (TextUtils.isEmpty(data)) {
            Toast.makeText(this, "输入数据", Toast.LENGTH_SHORT).show();
            return;
        }
        byte[] bytes = data.getBytes();

        for (int i = 0; i < bytes.length; i++) {
            if (!((bytes[i] >= 30 && bytes[i] <= 39) || (bytes[i] >= 41 && bytes[i] <= 46) ||
                    (bytes[i] >= 61 && bytes[i] <= 66))) {
                Toast.makeText(this, "输入数据异常", Toast.LENGTH_SHORT).show();
                return;
            }
        }
        // TODO validate success, do something

        int dd = Integer.parseInt(data);
        dataRead[dex] = (byte) dd;
        dex++;
        dataReadyString.append(dataRead[dex] + "  ");
        text_ready_data.setText(dataReadyString.toString());

    }


    int[] colors = {Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW};
    int colorIndex = 0;
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                //写数据回复
                case 666:
                    text_receive_after_send.setText(msg.getData().getString("msg"));
                    text_receive_after_send.setTextColor(colors[colorIndex]);
                    colorIndex++;
                    if (colorIndex == 4) {
                        colorIndex = 0;
                    }
                    adapter.notifyDataSetChanged();
                    break;
                case 999:
                    adapter.notifyDataSetChanged();
                    break;
                case 555:
                    synchronized (MainActivity.class) {
                        sentCount++;
                        if (waiting) {
                            progress.setText("升级进度：" + (sendCount * 1f / fileByteLength) + "  " +
                                    "耗时：" + sdf.format(-startTime + System.currentTimeMillis()));
                        }
                        waiting = false;
                        MainActivity.class.notify();
                    }
                    break;

            }

        }
    };


    boolean isRunableRunning;
    Runnable runable = new Runnable() {
        @Override
        public void run() {
            {
                isRunableRunning = true;
                try {
                    InputStream input = getResources().getAssets().open(appAddrpName);
                    BufferedInputStream bis = new BufferedInputStream(input);
                    if (bis != null) {
                        sendCount = 0;
                        startTime = System.currentTimeMillis();
                        sentCount = 0;
                        byte[] tempbytes = new byte[2];
                        int byteread = 0;
                        // 读入多个字节到字节数组中，byteread为一次读入的字节数
                        while ((byteread = bis.read(tempbytes)) != -1) {
                            if (byteread == 1) {
                                inputSystemManager.sendData(mainBoardMac, MainBoardCommand
                                        .makeWriteData
                                                ((byte) (sendCount / 256), (byte) (sendCount % 256),
                                                        tempbytes[0], (byte) 0));
                                Log.d(TAG, byteread + " 发送数据  data=" + (byte) (sendCount / 256) +
                                        "  "
                                        + (byte) (sendCount % 256) + "  " + tempbytes[0] + "  " +
                                        0);
                            } else {
                                inputSystemManager.sendData(mainBoardMac, MainBoardCommand
                                        .makeWriteData(
                                                (byte) (sendCount / 256), (byte) (sendCount %
                                                        256), tempbytes[0], tempbytes[1]));
                                Log.d(TAG, byteread + "发送数据  data=" + (byte) (sendCount / 256) +
                                        "  " + (byte) (sendCount % 256) + "  " + tempbytes[0] + "" +
                                        "  " + tempbytes[1]);
                            }
                            sendCount++;
                            Log.d(TAG, byteread + "发送数据  sendCount=" + sendCount);
                            //TODO  等待
                            synchronized (MainActivity.class) {
                                waiting = true;
                                MainActivity.class.wait();
                            }
//                            Thread.sleep();
                        }
                        bis.close();
                        input.close();
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
                isRunableRunning = false;
            }
        }
    };

    /**
     * 心率计数据
     *
     * @param key
     * @param value
     */
    @Override
    public void bleData(final short key, final short value) {
        // TODO Auto-generated method stub
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // TODO Auto-generated method stub
                switch (key) {
//                    case SmctConstant.KEY_ECG_DATA://  26  实时ECG数据
//                        break;
//                    case SmctConstant.KEY_BLE_CONNECT_STATE://   10   蓝牙连接状态
//                        if (value == SmctConstant.VALUE_BLE_CONNECTED) {//   0 连接
//                            Toast.makeText(MainActivity.this, "蓝牙连接成功！！！", Toast.LENGTH_SHORT)
//                                    .show();
//                        } else if (value == SmctConstant.VALUE_BLE_DISCONNECTED) {// 1  断开
//                            Toast.makeText(MainActivity.this, "蓝牙连接失败！！！", Toast.LENGTH_SHORT)
//                                    .show();
//                        } else if (value == SmctConstant.VALUE_BLE_SERVICE_DISCOVERED) {// 2
// 发现服务
//                            Toast.makeText(MainActivity.this, "发现蓝牙服务！！！", Toast.LENGTH_SHORT)
//                                    .show();
//                        } else if (value == SmctConstant.VALUE_BLE_DATA_AVAILABLE) {// 3  发现数据
//                            Toast.makeText(MainActivity.this, "开始接收数据！！！", Toast.LENGTH_SHORT)
//                                    .show();
//                        }
//                        break;
//					case SmctConstant.KEY_DEVICE_ELECTRODE_DROP:
//						Toast.makeText(MainActivity.this, "电极脱落！！！", 2000).show();
//						break;
                    case SmctConstant.KEY_DEVICE_POWER_LEVEL://12
                        System.out.println(TAG + "bleData电量：" + value + "%");
                        break;
                    case SmctConstant.KEY_HEARTRATE_FROM_DEVICE://49
                        System.out.println(TAG + "bleData心率：" + value);
                        break;
//					case SmctConstant.KEY_BODY_POSE:
//						break;
                    default:
                        break;
                }
            }
        });
    }

    @Override
    public void bleData(short var1, float[] var2) {

    }

    @Override
    public void bleData(short var1, short[] var2) {

    }

    @Override
    public void bleData(int var1, byte[] var2) {

    }


    @Override
    public void bleDispatchMessage(Message msg) {
        switch (msg.what) {
            case BleService.RE_BLE_WRITE_NAME_SUCCEED:
                Toast.makeText(this, "我是修改名称的回调！", Toast.LENGTH_LONG).show();
                break;
        }
        Log.d(TAG, "BodyPlus bleDispatchMessage: what=" + msg.what + "   obj=" + msg.toString());

    }

    @Override
    public void bleDataCallBack(int code, int dm) {
        // 心率数据的回调
        if (code == BleCmdConfig.BLE_HEART_MESSAGE) {
            Log.d(TAG, "BodyPlus bleDataCallBack: code=" + code + "心率  dm=" + dm);
        } else {
            Log.d(TAG, "BodyPlus bleDataCallBack: code=" + code + "  dm=" + dm);
        }

    }

    @Override
    public void bleHeartDataError() {
        // 心率检测脱落的回调
        Log.d(TAG, "BodyPlus bleHeartDataError: ");

    }

    @Override
    public void blePowerLevel(byte data) {
        // 电量的回调 范围0-100
        Log.d(TAG, "BodyPlus bleHeartDataError: " + "电量：" + data);
    }

    @Override
    public void bleReConnectDevice(DeviceInfo device) {
        // 重连成功的回调
        Log.d(TAG, "bleReConnectDevice: " + device.toString());
    }

    @Override
    public void bleDeviceDisconnect() {
        // 连接断开的回调
        Log.d(TAG, "bleDeviceDisconnect: ");
    }

    @Override
    public void bleCoreModule(byte data) {
        // 位置状态的回调
        switch (data) {
            case 0x00: // 充电座
                Log.d(TAG, "BodyPlus bleHeartDataError: " + "位置：充电");
                break;
            case 0x01: // 上衣
                Log.d(TAG, "BodyPlus bleHeartDataError: " + "位置：服装");
                break;
            case 0x11: // 独立
                Log.d(TAG, "BodyPlus bleHeartDataError: " + "位置：独立");
                break;
        }
        Log.d(TAG, "bleCoreModule: " + data);
    }
}
