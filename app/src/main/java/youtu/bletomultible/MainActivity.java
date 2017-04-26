package youtu.bletomultible;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
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
import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import youtu.bletomultible.bluetooth.BleDeviceAdapter;
import youtu.bletomultible.bluetooth.BleDeviceBean;
import youtu.bletomultible.bluetooth.InputSystemManager;
import youtu.bletomultible.bluetooth.MainBoardCommand;
import youtu.bletomultible.bluetooth.SampleGattAttributes;
import youtu.bletomultible.bluetooth.hrm.HandleBleData;
import youtu.bletomultible.bluetooth.hrm.IBleOperateCallback;
import youtu.bletomultible.bluetooth.hrm.IUpdateCallBack;
import youtu.bletomultible.bluetooth.hrm.SmctConstant;

public class MainActivity extends AppCompatActivity implements InputSystemManager
        .BlueToothDataValuesChangedListener,
        InputSystemManager.BlueToothConnectStateEvevtListener, IBleOperateCallback {
    private HashMap<String, BleDeviceBean> devicesMap = new HashMap<>();
    private String mainBoardMac;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        devicesMap = new HashMap<>();
        //1  手环   2  主控板   3 计步器   4  三角心率计
//        mainBoardMac = "D8:B0:4C:BA:CB:0F";
//        BleDeviceBean ble2 = new BleDeviceBean("蓝牙设备", mainBoardMac, 2);
//        devicesMap.put(mainBoardMac, ble2);

        String hrmMac="98:7B:F3:C4:D5:7E";
        BleDeviceBean ble4 = new BleDeviceBean("蓝牙设备", hrmMac, 4);
        devicesMap.put(hrmMac, ble4);

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
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        inputSystemManager.unRegisterBlueToothDataValuesChangedListener(this);
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
        Log.d(TAG,  "DataValuesChanged："+add +"type="+type +"  "+ values.length
                + "  数据："+ stringBuilder.toString());

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
                handler.sendMessage(message);
                devicesMap.get(add).setValues(values);
                handler.sendEmptyMessage(999);
            }

        } else if (type == SampleGattAttributes.HAND_BAND && values[6] == 3) {   //手环就绪
            inputSystemManager.sendData(add, new byte[]{(byte) -85, (byte) 0, (byte) 4, (byte)
                    -1, (byte) 49, (byte) 0x0a, (byte) 1});
        } else if (type == SampleGattAttributes.HRM) {//心率计
            //TODO 处理心率计数据

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
            R.id.btn_set_data, R.id.btn_reset
    })
    public void Onclick(View view) {
        switch (view.getId()) {
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
                        MainBoardCommand.makeSoftVersion(128));
                break;
            // 3 设置硬件版本号
            case R.id.btn_set_hard_version:
                inputSystemManager.sendData(mainBoardMac, MainBoardCommand.makeHardVersion(2));
                break;
            //   4 app_addr
            // 0xAA 0x13 0x00 0x00 0x00 0x00 0x00 0x00 0x55 （app_addr（0x3400或者0x9800）高位在前，低位在后）
            case R.id.btn_set_app_addr:
                inputSystemManager.sendData(mainBoardMac,
                        MainBoardCommand.makeAppAddress(0x9800));
                break;
            //  5 文件长度
            //  0xAA 0x14 0x00 0x00 0x00 0x00 0x00 0x00 0x55 （文件长度，字节数/2，高位在前，低位在后）
            case R.id.btn_set_file_length:
                //inputSystemManager.sendData(mainBoardMac,
                //new byte[]{(byte) 0xAA, (byte) 0x14, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x55});
                InputStream input = null;
                try {
//                    input = getResources().getAssets().open("master_control_B.bin");
                    input = getResources().getAssets().open("ctr_min_test_V1.1.hex");
                    BufferedInputStream bis = new BufferedInputStream(input);
                    int fileByteLength = bis.available() / 2 + bis.available() % 2;
                    Log.d(TAG, "LLLLLLL实际字节数=" + bis.available() +
                            "LLLLLLL除以2发送次数（每次两位）=" + fileByteLength +
                            "LLLLLLL高位=" + (byte) (fileByteLength / 256) +
                            "LLLLLLL低位=" + (byte) (fileByteLength % 256));

                    inputSystemManager.sendData(mainBoardMac, MainBoardCommand.makeFileLength
                            (fileByteLength));
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
                    break;
                case 999:
                    adapter.notifyDataSetChanged();
                    devicesRecyclerView.setBackgroundColor(colors[recColorInde]);
                    recColorInde++;
                    if (recColorInde == 4) {
                        recColorInde = 0;
                    }
                    break;
            }

        }
    };


    public void write4Bytes(byte[] bytes) {
        if (bytes.length < 4) {
            Log.d(TAG, "write3Bytes err");
            return;
        }
        inputSystemManager.sendData(mainBoardMac,
                new byte[]{(byte) 0xAA, 0x21, bytes[0], bytes[1],
                        bytes[2], bytes[3], 0x00, 0x00, 0x55});
    }

    boolean isRunableRunning;
    Runnable runable = new Runnable() {
        @Override
        public void run() {
            isRunableRunning = true;
            try {
//                InputStream input = getResources().getAssets().open("master_control_B.bin");
                InputStream input = getResources().getAssets().open("ctr_min_test_V1.1.hex");
                BufferedInputStream bis = new BufferedInputStream(input);
                int fileByteLength = bis.available();
                if (bis != null) {
                    byte[] bs = new byte[fileByteLength];
                    bis.read(bs);
                    int sendCount = 0;
                    int i = 0;
                    while (i < bs.length) {
                        if (i + 1 == bs.length) {
                            inputSystemManager.sendData(mainBoardMac, MainBoardCommand.makeWriteData
                                    ((byte) (sendCount / 256), (byte) (sendCount % 256),
                                            bs[i], (byte) 0));
                            Log.d(TAG, "LLLLLLL  data=" + (byte) (sendCount / 256) + "  "
                                    + (byte) (sendCount % 256) + "  " + bs[i] + "  " + 0);
                        } else {
                            inputSystemManager.sendData(mainBoardMac, MainBoardCommand
                                    .makeWriteData(
                                            (byte) (sendCount / 256), (byte) (sendCount %
                                                    256), bs[i], bs[i + 1]));
                            Log.d(TAG, "LLLLLLL  data=" + (byte) (sendCount / 256) + "  "
                                    + (byte) (sendCount % 256) + "  " + bs[i] + "  " + bs[i + 1]);
                        }
                        i++;
                        i++;
                        sendCount++;
                        SystemClock.sleep(50);
                    }
                    bis.close();
                    input.close();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
            isRunableRunning = false;
        }
    };

    /**
     * 心率计数据
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
//                        } else if (value == SmctConstant.VALUE_BLE_SERVICE_DISCOVERED) {// 2   发现服务
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
                        System.out.println(TAG + "bleData心率：" + value );
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
}
