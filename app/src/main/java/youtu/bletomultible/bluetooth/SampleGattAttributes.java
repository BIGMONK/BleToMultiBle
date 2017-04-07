package youtu.bletomultible.bluetooth;

import java.util.UUID;

/**
 * Created by black on 2016/4/12.
 */
public class SampleGattAttributes {

    //1  手环   2  主控板   3 计步器   4  三角心率计
    public static int HAND_BAND = 1;
    public static int MAINBOARD = 2;
    public static int STEPER =3;
    public static int HRM = 4;
    public static String HAND_BAND_NAME = "手环";
    public static String MAINBOARD_NAME ="主控板" ;
    public static String HRM_NAME = "计步器";
    public static String STEPER_NAME = "三角心率计";

    //通用参数
    public static UUID CLIENT_CHARACTERISTIC_CONFIG = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
    //主控板  连接就能收到数据  发送数据格式：AA 02 06 00 00 00 00 00 55  //9位数组
    public static UUID MAINBOARD_SERVICE_UUID = UUID.fromString("0003cdd0-0000-1000-8000-00805f9b0131");
    public static UUID MAINBOARD_RECEIVE_UUID = UUID.fromString("0003cdd1-0000-1000-8000-00805f9b0131");
    public static UUID MAINBOARD_SEND_UUID = UUID.fromString("0003cdd2-0000-1000-8000-00805f9b0131");
    //手环参数
    public static byte[] START_HEART_MEASURE = {(byte) -85, (byte) 0, (byte) 4, (byte) -1, (byte) 49, (byte) 0x0a, (byte) 1};//心率实时测量
    public static byte[] STOP_HEART_MEASURE ={(byte) -85, (byte) 0, (byte) 4, (byte) -1, (byte) 49, (byte) 0x0a, (byte) 0};//心率停止测量
    public static UUID HAND_BAND_SERVICE_UUID = UUID.fromString("6e400001-b5a3-f393-e0a9-e50e24dcca9e");
    public static UUID HAND_BAND_RECEIVE_UUID = UUID.fromString("6e400003-b5a3-f393-e0a9-e50e24dcca9e");
    public static UUID HAND_BAND_SEND_UUID = UUID.fromString("6e400002-b5a3-f393-e0a9-e50e24dcca9e");

    // 计步器
    public static UUID STEPER_SERVICE_UUID = UUID.fromString("6e40ffe1-b5a3-f393-e0a9-e50e24dcca9e");
    public static UUID STEPER_RECEIVE_UUID = UUID.fromString("6e40ffe3-b5a3-f393-e0a9-e50e24dcca9e");
    public static UUID STEPER_SEND_UUID = UUID.fromString("6e40ffe2-b5a3-f393-e0a9-e50e24dcca9e");  //

    //三角心率计
    public static UUID HRM_SERVICE_UUID = UUID.fromString("0000FFE0-0000-1000-8000-00805f9b34fb");
    public static UUID HRM_RECEIVE_UUID = UUID.fromString("0000ffe2-0000-1000-8000-00805f9b34fb");
    public static UUID HRM_SEND_UUID = UUID.fromString("0000ffe3-0000-1000-8000-00805f9b34fb");



}
