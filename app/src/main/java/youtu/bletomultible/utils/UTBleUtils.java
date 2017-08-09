package youtu.bletomultible.utils;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.util.SparseArray;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by djf on 2017/8/8.
 */

public class UTBleUtils {

    public static Context context;
    private static BluetoothManager bluetoothManager;
    private static BluetoothAdapter mBluetoothAdapter;

    private static Context getContext() {
        if (context == null) {
            context = UIUtils.getContext();
        }
        return context;
    }

    /**
     * 判断是否支持低功耗蓝牙
     *
     * @return
     */
    public static boolean isBleSupport() {
        // 检查是否支持ble 蓝牙
        if (!getContext().getPackageManager().hasSystemFeature(PackageManager
                .FEATURE_BLUETOOTH_LE)) {
            return false;
        }
        return true;
    }

    private static BluetoothManager getBluetoothManager() {
        if (bluetoothManager == null)
            bluetoothManager = (BluetoothManager) (getContext().getSystemService
                    (Context.BLUETOOTH_SERVICE));
        return bluetoothManager;
    }

    private static BluetoothAdapter getmBluetoothAdapter() {
        if (mBluetoothAdapter == null)
            mBluetoothAdapter = bluetoothManager.getAdapter();
        return mBluetoothAdapter;
    }

    /**
     * 判断蓝牙是否打开
     *
     * @return
     */
    public static boolean isBleOpened() {
        if (bluetoothManager == null) {
            bluetoothManager = getBluetoothManager();
        }
        if (mBluetoothAdapter == null)
            mBluetoothAdapter = getmBluetoothAdapter();
        if (mBluetoothAdapter == null) {
            return false;
        } else {
            return mBluetoothAdapter.isEnabled();
        }
    }

    /**
     * 蓝牙扫描广播包解析
     *
     * @param scanRecord
     * @return
     */
    public static SparseArray<byte[]> parseFromBytes(byte[] scanRecord) {
        if (scanRecord == null) {
            return null;
        }
        int currentPos = 0;
        SparseArray<byte[]> manufacturerData = new SparseArray<byte[]>();
        try {
            while (currentPos < scanRecord.length) {
                int length = scanRecord[currentPos++] & 0xFF;
                if (length == 0) {
                    break;
                }
                int dataLength = length - 1;
                int filedType = scanRecord[currentPos++] & 0xFF;
                switch (filedType) {
                    case 0xFF:
                        int manufacturerId = ((scanRecord[currentPos + 1] & 0xFF) << 8) +
                                (scanRecord[currentPos] & 0xFF);
                        byte[] manufacturerDataBytes = extracBytes(scanRecord, currentPos + 2,
                                dataLength);
                        manufacturerData.put(manufacturerId, manufacturerDataBytes);
                        break;
                }
                currentPos += dataLength;
            }
            return manufacturerData;
        } catch (Exception e) {
            return manufacturerData;
        }
    }

    /**
     * 字节数组复制
     *
     * @param scanRecord
     * @param start
     * @param length
     * @return
     */
    private static byte[] extracBytes(byte[] scanRecord, int start, int length) {
        byte[] bytes = new byte[length];
        System.arraycopy(scanRecord, start, bytes, 0, length);
        return bytes;
    }

    /**
     * 根据广播包判断是否包含指定UUID
     *
     * @param scanRecord
     * @return
     */
    public static boolean isFilterMyUUID(byte[] scanRecord) {
        UUID[] bleUUIDs = {UUID.fromString("00000001-0000-1000-8000-00805f9b34fb")
                , UUID.fromString("00000005-0000-1000-8000-00805f9b34fb")};
        int index = 0;
        List<UUID> scanUUIDs = parseUuids(scanRecord);
        for (int i = 0; i < bleUUIDs.length; i++) {
            UUID uuid = bleUUIDs[i];
            for (int j = 0; j < scanUUIDs.size(); j++) {
                if (uuid.equals(scanUUIDs.get(j))) ;
                index++;
                break;
            }
        }
        if (index == bleUUIDs.length) {
            return true;
        }
        return false;
    }

    /**
     * 字节数组转 字符串
     *
     * @param ucPtr
     * @return
     */
    public static String byteToChar(byte[] ucPtr) {
        final StringBuffer sb = new StringBuffer();
        for (int i = 0; i < ucPtr.length; i++) {
            char c = (char) ucPtr[i];
            sb.append(String.valueOf(c));
        }
        return sb.toString();
    }

    /**
     * 根据广播包解析出UUID
     * @param advertisedData
     * @return
     */
    public static List<UUID> parseUuids(byte[] advertisedData) {
        List<UUID> uuids = new ArrayList<>();
        ByteBuffer buffer = ByteBuffer.wrap(advertisedData).order(ByteOrder.LITTLE_ENDIAN);
        while (buffer.remaining() > 2) {
            byte length = buffer.get();
            if (length == 0) break;
            byte type = buffer.get();
            switch (type) {
                case 0x02: // Partial list of 16-bit UUIDs
                case 0x03: // Complete list of 16-bit UUIDs
                case 0x14: // List of 16-bit Service Solicitation UUIDs
                    while (length >= 2) {
                        uuids.add(UUID.fromString(String.format(
                                "%08x-0000-1000-8000-00805f9b34fb", buffer.getShort())));
                        length -= 2;
                    }
                    break;
                case 0x04: // Partial list of 32 bit service UUIDs
                case 0x05: // Complete list of 32 bit service UUIDs
                    while (length >= 4) {
                        uuids.add(UUID.fromString(String.format(
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
                        uuids.add(new UUID(msb, lsb));
                        length -= 16;
                    }
                    break;
                default:
                    buffer.position(buffer.position() + length - 1);
                    break;
            }

        }
        return uuids;
    }

}
