package youtu.bletomultible.bluetooth;

/**
 * Created by djf on 2017/2/18.
 */

public interface BluetoothStatus {

    int STATE_NULL_WRITE_BGC = -33;
    int STATE_NULL_NOTIFY_BGC = -44;
    int STATE_NOTIFY_TRUE = -45;
    int STATE_NOTIFY_FALSE = -46;
    int STATE_DISCONNECTED = 0;
    int STATE_CONNECTING = 1;
    int STATE_CONNECTED = 2;
    int STATE_SCANING = -1;
    int STATE_SCAN_TIMEOUT = -2;
    int STATE_NOTYFY_SUCCESS = 333;
    int STATE_NOTYFY_FAILED = -333;
    int STATE_SEND_READY = 444;
    int STATE_SEND_NOT_READY = -444;
    int STATE_SEND_AND_NOTIFY_READY = 777;
    int STATE_SEND_AND_NOTIFY_NOT_READY = -777;
    int STATE_SEND_READY_AND_NOTIFY_NOT_READY = 111;
    int STATE_SEND_NOT_READY_AND_NOTIFY_READY = -111;
    int STATE_GET_NOTIFY_DATA = 666;


}
