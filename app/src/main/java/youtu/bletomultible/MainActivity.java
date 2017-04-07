package youtu.bletomultible;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import java.util.HashMap;

import youtu.bletomultible.bluetooth.BleDeviceBean;
import youtu.bletomultible.bluetooth.InputSystemManager;

public class MainActivity extends AppCompatActivity implements InputSystemManager.BlueToothDataValuesChangedListener, InputSystemManager.BlueToothConnectStateEvevtListener {
    private HashMap<String,BleDeviceBean> devicesMap = new HashMap<>();
    private String mainBoardMac;
    private InputSystemManager inputSystemManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);




        devicesMap = new HashMap<>();
        //1  手环   2  主控板   3 计步器   4  三角心率计
        String handBandMac = "D3:CB:8F:A3:20:BA";
        mainBoardMac = "D8:B0:4C:B6:50:91";
        //        mainBoardMac = "D8:B0:4C:B6:50:D8";
        //mainBoardMac = "D8:B0:4C:B6:50:A9";
        String HRMMac = "98:7B:F3:C4:D5:7E";
        String steperMac = "D3:CB:8F:A3:20:BA";
        //        BleDeviceBean ble = new BleDeviceBean("手环", handBandMac, 1);
         BleDeviceBean ble2 = new  BleDeviceBean("主控板", mainBoardMac, 2);
        //        BleDeviceBean ble3 = new BleDeviceBean("计步器", mainBoardMac, 3);
        //        BleDeviceBean ble4 = new BleDeviceBean("三角心率计", HRMMac, 4);
        //        devicesMap.put(handBandMac, ble);
        devicesMap.put(mainBoardMac, ble2);
        //        devicesMap.put(steperMac, ble3);
        //        devicesMap.put(HRMMac, ble4);

        inputSystemManager =  InputSystemManager.getInstance();
        inputSystemManager.registerBlueToothDataValuesChangedListener(this);
        //监听蓝牙连接状态
        inputSystemManager.setBlueToothConnectStateEvevtListener(this);
        inputSystemManager.initWithContext(this, devicesMap);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        inputSystemManager.unRegisterBlueToothDataValuesChangedListener(this);
    }

    @Override
    public void onBlueToothDataValuesChanged(int type, String add, byte[] values) {

    }

    @Override
    public void onBlueToothConnectStateChanged(int type, String add, int state) {

    }
}
