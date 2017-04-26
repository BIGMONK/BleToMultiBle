package youtu.bletomultible.bluetooth;

/**
 * Created by djf on 2017/4/24.
 */

public class MainBoardCommand {
    /**
     * 开始升级指令
     */
    public static byte[] START_UPDATE = {(byte) 0xAA, (byte) 0xAB, (byte) 0xAC, (byte) 0xAD,
            (byte) 0xAE, (byte) 0xAF, 0x00, 0x00, 0x55};
    /**
     * 获取软件版本号
     */
    public static byte[] GET_SOFT_VERSION = {(byte) 0xAA, (byte) 0x81, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00, 0x55};

    /**
     * 设置软件版本号
     */
    public static byte[] makeSoftVersion(byte h, byte l) {
        return make3Bytes((byte) 0x11, h, l);
    }

    /**
     * 设置软件版本号
     *
     * @param code
     * @return
     */
    public static byte[] makeSoftVersion(int code) {
        return make3Bytes((byte) 0x11, (byte) (code / 256), (byte) (code % 256));
    }

    /**
     * 获取硬件版本号
     */
    public static byte[] GET_HARD_VERSION = {(byte) 0xAA, (byte) 0x82, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00, 0x55};

    /**
     * 设置硬件版本号
     *
     * @param h
     * @param l
     * @return
     */
    public static byte[] makeHardVersion(byte h, byte l) {
        return make3Bytes((byte) 0x12, h, l);
    }

    /**
     * 设置硬件版本号
     *
     * @param code
     * @return
     */
    public static byte[] makeHardVersion(int code) {
        return make3Bytes((byte) 0x12, (byte) (code / 256), (byte) (code % 256));
    }

    /**
     * 固件升级地址指令，app_addr（0x3400或者0x9800）高位在前，低位在后
     *
     * @param h
     * @param l
     * @return
     */
    public static byte[] makeAppAddr(byte h, byte l) {
        return make3Bytes((byte) 0x13, h, l);
    }

    /**
     * 固件升级地址指令
     *
     * @param i
     * @return
     */
    public static byte[] makeAppAddress(int i) {
        return makeAppAddr((byte) (i / 256), (byte) (i % 256));
    }

    public static byte[] makeAppAddress34(){
        return makeAppAddr((byte) (0x3400 / 256), (byte) 0);
    }
     public static byte[] makeAppAddress98(){
        return makeAppAddr((byte) (0x9800 / 256), (byte) 0);
    }

    /**
     * 固件长度/2 ,发送次数
     *
     * @param l
     * @return
     */
    public static byte[] makeFileLength(int l) {
        return make3Bytes((byte) 0x14, (byte) (l / 256), (byte) (l % 256));
    }

    /**
     * 复位指令
     */
    public static byte[] RESET = {(byte) 0xAA, (byte) 0xFA, (byte) 0xFB, (byte) 0xFC, (byte)
            0xFD, (byte) 0xFE, 0x00, 0x00, 0x55};

    public static byte[] make3Bytes(byte flag, byte b1, byte b2) {
        return new byte[]{(byte) 0xAA, flag, b1, b2,
                0x00, 0x00, 0x00, 0x00, 0x55};
    }


    /**
     * @param bytes
     * @return
     */
    public static byte[] makeWriteData(byte[] bytes) {
        if (bytes == null || bytes.length != 4) {
            return null;
        }
        return make5Bytes((byte) 0x21, bytes[0], bytes[1], bytes[2], bytes[3]);
    }

    public static byte[] makeWriteData(byte b0, byte b1, byte b2, byte b3) {
        return make5Bytes((byte) 0x21, b0, b1, b2, b3);
    }

    public static byte[] make5Bytes(byte flag, byte b0, byte b1, byte b2, byte b3) {
        return new byte[]{(byte) 0xAA, flag, b0, b1, b2, b3, 0x00, 0x00, 0x55};
    }
}
