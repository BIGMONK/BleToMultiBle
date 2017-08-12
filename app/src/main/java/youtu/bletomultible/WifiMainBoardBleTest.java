package youtu.bletomultible;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import youtu.bletomultible.bluetooth.BleDeviceAdapter;
import youtu.bletomultible.bluetooth.BleDeviceBean;
import youtu.bletomultible.bluetooth.InputSystemManager;
import youtu.bletomultible.bluetooth.SampleGattAttributes;
import youtu.bletomultible.bluetooth.WifiBoardBleCom;
import youtu.bletomultible.utils.LogUtils;

public class WifiMainBoardBleTest extends AppCompatActivity implements InputSystemManager
        .BlueToothDataValuesChangedListener, InputSystemManager.BlueToothConnectStateEvevtListener {

    @BindView(R.id.btn_wifi_name)
    Button btnWifiName;
    @BindView(R.id.et_wifi_name)
    EditText etWifiName;
    @BindView(R.id.btn_wifi_key)
    Button btnWifiKey;
    @BindView(R.id.et_wifi_key)
    EditText etWifiKey;
    @BindView(R.id.btn_get_ip)
    Button btnGetIp;
    @BindView(R.id.btn_get_mac)
    Button btnGetMac;
    @BindView(R.id.btn_get_client_count)
    Button btnGetClientCount;
    @BindView(R.id.btn_get_client_index)
    Button btnGetClientIndex;
    @BindView(R.id.btn_get_server_status)
    Button btnGetServerStatus;
    @BindView(R.id.btn_reset)
    Button btnReset;
    @BindView(R.id.devicesList)
    RecyclerView devicesList;
    private static final String TAG = "WifiMainBoardBleTest";
    private HashMap<String, BleDeviceBean> devicesMap;
    private BleDeviceAdapter adapter;
    private InputSystemManager inputSystemManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi_main_board_ble_test);
        ButterKnife.bind(this);

        devicesMap = (HashMap<String, BleDeviceBean>) getIntent().getSerializableExtra("deviceMap");

        devicesMap = new HashMap<>();
        String mainBoardMac = getIntent().getStringExtra("address");
        String mDevicesName = getIntent().getStringExtra("name");
        int type = getIntent().getIntExtra("type", 1);

        LogUtils.d(TAG, "add=" + mainBoardMac + "  name=" + mDevicesName + "  type=" + type);
        BleDeviceBean ble2 = new BleDeviceBean(mDevicesName, mainBoardMac, type);

        LogUtils.d(TAG, "目标設備：" + ble2.toString());

        devicesMap.put(mainBoardMac, ble2);

        inputSystemManager = InputSystemManager.getInstance();
        inputSystemManager.registerBlueToothDataValuesChangedListener(this);
        //监听蓝牙连接状态
        inputSystemManager.setBlueToothConnectStateEvevtListener(this);
        inputSystemManager.initWithContext(this, devicesMap);

        adapter = new BleDeviceAdapter(this, devicesMap);
        //设置布局管理器
        devicesList.setLayoutManager(new LinearLayoutManager(this));
        //设置Adapter
        devicesList.setAdapter(adapter);
    }

    @OnClick({R.id.btn_wifi_name, R.id.btn_wifi_key, R.id.btn_get_ip, R.id.btn_get_mac, R.id
            .btn_get_client_count, R.id.btn_get_client_index, R.id.btn_get_server_status, R.id
            .btn_reset})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_wifi_name:

                String name= etWifiName.getText().toString().trim();

                if (TextUtils.isEmpty(name)){
                    Toast.makeText(this,"wifi名称不能为空",Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!(name.getBytes().length<20)){
                    Toast.makeText(this,"wifi名称过长",Toast.LENGTH_SHORT).show();
                    return;
                }
                inputSystemManager.sendData(SampleGattAttributes.NEW_BLE_MAINBOARD,
                        WifiBoardBleCom.makeDatas(WifiBoardBleCom.Type.SetWifiName,name.getBytes())
                );
                break;
            case R.id.btn_wifi_key:
                String key = etWifiKey.getText().toString().trim();

                if (TextUtils.isEmpty(key)){
                    Toast.makeText(this,"wifi key不能为空",Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!(key.getBytes().length<20)){
                    Toast.makeText(this,"wifi key过长",Toast.LENGTH_SHORT).show();
                    return;
                }
                inputSystemManager.sendData(SampleGattAttributes.NEW_BLE_MAINBOARD,
                        WifiBoardBleCom.makeDatas(WifiBoardBleCom.Type.SetWifiKey,key.getBytes())
                );
                break;
            case R.id.btn_get_ip:

                inputSystemManager.sendData(SampleGattAttributes.NEW_BLE_MAINBOARD,
                        WifiBoardBleCom.makeDatas(WifiBoardBleCom.Type.GetBoardIp)

                );

                break;
            case R.id.btn_get_mac:
                inputSystemManager.sendData(SampleGattAttributes.NEW_BLE_MAINBOARD,
                        WifiBoardBleCom.makeDatas(WifiBoardBleCom.Type.GetBoardIp)

                );

                break;
            case R.id.btn_get_client_count:
                inputSystemManager.sendData(SampleGattAttributes.NEW_BLE_MAINBOARD,
                        WifiBoardBleCom.makeDatas(WifiBoardBleCom.Type.GetBoardIp)

                );
                break;
            case R.id.btn_get_client_index:
                inputSystemManager.sendData(SampleGattAttributes.NEW_BLE_MAINBOARD,
                        WifiBoardBleCom.makeDatas(WifiBoardBleCom.Type.GetBoardTC,new byte[]{1})
                );
                break;
            case R.id.btn_get_server_status:
                inputSystemManager.sendData(SampleGattAttributes.NEW_BLE_MAINBOARD,
                        WifiBoardBleCom.makeDatas(WifiBoardBleCom.Type.GetBoardSS)

                );

                break;
            case R.id.btn_reset:
                inputSystemManager.sendData(SampleGattAttributes.NEW_BLE_MAINBOARD,
                        WifiBoardBleCom.makeDatas(WifiBoardBleCom.Type.ResetBoard)

                );
                break;
        }
    }

    byte[] makeData() {
        return null;
    }

    StringBuilder stringBuilder = new StringBuilder();

    @Override
    public void onBlueToothDataValuesChanged(int type, String add, byte[] values) {
        if (values != null && values.length > 0) {
            for (int h = 0; h < values.length; h++) {
                stringBuilder.append(values[h] + "    ");
            }
            Log.d(TAG, "DataValuesChanged：" + add + "type=" + type + "   " + values.length
                    + "  数据：" + stringBuilder.toString());

            BleDeviceBean bleDeviceBean = devicesMap.get(add);
            if (bleDeviceBean != null) {
                bleDeviceBean.setValues(values);
                handler.sendEmptyMessage(UPDATE);
            }
        }

    }

    private final static int UPDATE = 1;
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case UPDATE:
                    adapter.notifyDataSetChanged();
                    break;
                default:
                    break;
            }
        }
    };

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
}
