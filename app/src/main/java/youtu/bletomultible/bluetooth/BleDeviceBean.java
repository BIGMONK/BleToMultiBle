package youtu.bletomultible.bluetooth;

import java.io.Serializable;
import java.util.UUID;

/**
 * Created by djf on 2017/3/17.
 */

public class BleDeviceBean implements Serializable {
    private String name;
    private String mac;
    private int type;//1  手环   2  主控板   3 计步器   4  三角心率计
    private UUID serviceUUID;
    private UUID notifyUUID;
    private UUID sendUUID;
    private UUID configUUID = SampleGattAttributes.CLIENT_CHARACTERISTIC_CONFIG;
    private byte[] values;
    private int state;

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public String getValues() {
        StringBuilder sb = new StringBuilder();
        if (values != null) {
            for (int i = 0; i < values.length; i++) {
                sb.append(values[i] + "    ");
            }
        }

        return sb.toString();
    }

    public void setValues(byte[] values) {
        this.values = values;
    }

//    //1  手环   2  主控板   3 计步器   4  三角心率计
    public BleDeviceBean(String name, String mac, int type) {
        this.name = name;
        this.mac = mac;
        this.type = type;
        if (type == SampleGattAttributes.HAND_BAND) {  //手环
            this.serviceUUID = SampleGattAttributes.HAND_BAND_SERVICE_UUID;
            this.notifyUUID = SampleGattAttributes.HAND_BAND_RECEIVE_UUID;
            this.sendUUID = SampleGattAttributes.HAND_BAND_SEND_UUID;
        } else if (type == SampleGattAttributes.MAINBOARD) {//主板
            this.serviceUUID = SampleGattAttributes.MAINBOARD_SERVICE_UUID;
            this.notifyUUID = SampleGattAttributes.MAINBOARD_RECEIVE_UUID;
            this.sendUUID = SampleGattAttributes.MAINBOARD_SEND_UUID;
        } else if (type == SampleGattAttributes.STEPER) {//计步器
            this.serviceUUID = SampleGattAttributes.STEPER_SERVICE_UUID;
            this.notifyUUID = SampleGattAttributes.STEPER_RECEIVE_UUID;
            this.sendUUID = SampleGattAttributes.STEPER_SEND_UUID;
        } else if (type == SampleGattAttributes.HRM) {//心率计
            this.serviceUUID = SampleGattAttributes.HRM_SERVICE_UUID;
            this.notifyUUID = SampleGattAttributes.HRM_RECEIVE_UUID;
            this.sendUUID = SampleGattAttributes.HRM_SEND_UUID;
        }else if (type == SampleGattAttributes.NEW_BLE_MAINBOARD) {//计步器
            this.serviceUUID = SampleGattAttributes.NEW_BLE_MAINBOARD_SERVICE_UUID;
            this.notifyUUID = SampleGattAttributes.NEW_BLE_MAINBOARD_RECEIVE_UUID;
            this.sendUUID = SampleGattAttributes.HAND_BAND_SEND_UUID;
        }
    }


    public BleDeviceBean(String name, String mac, int type, UUID serviceUUID, UUID notifyUUID, UUID sendUUID, UUID configUUID) {
        this.name = name;
        this.mac = mac;
        this.type = type;
        this.serviceUUID = serviceUUID;
        this.notifyUUID = notifyUUID;
        this.sendUUID = sendUUID;
        this.configUUID = configUUID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    /**
     * //1  手环   2  主控板   3 计步器   4  三角心率计
     * @return
     */
    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public UUID getServiceUUID() {
        return serviceUUID;
    }

    public void setServiceUUID(UUID serviceUUID) {
        this.serviceUUID = serviceUUID;
    }

    public UUID getNotifyUUID() {
        return notifyUUID;
    }

    public void setNotifyUUID(UUID notifyUUID) {
        this.notifyUUID = notifyUUID;
    }

    public UUID getSendUUID() {
        return sendUUID;
    }

    public void setSendUUID(UUID sendUUID) {
        this.sendUUID = sendUUID;
    }

    public UUID getConfigUUID() {
        return configUUID;
    }

    public void setConfigUUID(UUID configUUID) {
        this.configUUID = configUUID;
    }


    @Override
    public String toString() {

        StringBuffer sb = new StringBuffer();
        if (values != null) {
            for (int i = 0; i < values.length; i++) {
                sb.append(values[i] + "    ");
            }
        }
        return "BleDeviceBean{" +
                "name='" + name + '\'' +
                ", mac='" + mac + '\'' +
                ", type=" + type +
                ", values=" + sb.toString() +
                ", state=" + state +
                ", serviceUUID=" + serviceUUID +
                ", notifyUUID=" + notifyUUID +
                ", sendUUID=" + sendUUID +
                ", configUUID=" + configUUID +
                '}';
    }
}
