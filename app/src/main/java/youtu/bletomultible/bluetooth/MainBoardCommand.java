package youtu.bletomultible.bluetooth;

/**
 * Created by djf on 2017/4/24.
 */

public class MainBoardCommand {
    /**
     * 开始升级指令
     */
    public static byte[] START_UPDATE = {
            (byte) 0xAA, (byte) 0xAB, (byte) 0xAC, (byte) 0xAD, (byte) 0xAE,
            (byte) 0xAF, (byte) 0x00, (byte) 0x00, (byte) 0x55};
    /**
     * 获取软件版本号指令
     */
    public static byte[] GET_SOFT_VERSION = {
            (byte) 0xAA, (byte) 0x81, (byte) 0x00, (byte) 0x00, (byte) 0x00,
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x55};


    /**
     * 生成软件版本号指令
     *
     * @param code 软件版本号
     * @return 软件版本号指令
     */
    public static byte[] makeSoftVersion(int code) {
        return make3Bytes((byte) 0x11, (byte) (code / 256), (byte) (code % 256));
    }

    /**
     * 获取硬件版本号指令
     */
    public static byte[] GET_HARD_VERSION = {
            (byte) 0xAA, (byte) 0x82, (byte) 0x00, (byte) 0x00, (byte) 0x00,
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x55};


    /**
     * 生成硬件版本号指令
     *
     * @param code 硬件版本号
     * @return 硬件版本号指令
     */
    public static byte[] makeHardVersion(int code) {
        return make3Bytes((byte) 0x12, (byte) (code / 256), (byte) (code % 256));
    }


    /**
     * 生成固件升级地址指令
     *
     * @param i 固件升级地址
     * @return 固件升级地址指令
     */
    public static byte[] makeAppAddress(int i) {
        return make3Bytes((byte) 0x13, (byte) (i / 256), (byte) (i % 256));
    }

    /**
     * 生成固件升级3400地址指令
     *
     * @return 固件升级3400地址指令
     */
    public static byte[] makeAppAddress3400() {
        return make3Bytes((byte) 0x13, (byte) (0x3400 / 256), (byte) 0);
    }

    /**
     * 生成固件升级9800地址指令
     *
     * @return 固件升级9800地址指令
     */
    public static byte[] makeAppAddress9800() {
        return make3Bytes((byte) 0x13, (byte) (0x9800 / 256), (byte) 0);
    }

    /**
     * 生成固件升级数据发送次数指令
     *
     * @param l 固件升级发送次数
     * @return 固件升级发送次数指令
     */
    public static byte[] makeFileLength(int l) {
        return make3Bytes((byte) 0x14, (byte) (l / 256), (byte) (l % 256));
    }

    /**
     * 复位指令
     */
    public static byte[] RESET = {
            (byte) 0xAA, (byte) 0xFA, (byte) 0xFB, (byte) 0xFC, (byte) 0xFD,
            (byte) 0xFE, (byte) 0x00, (byte) 0x00, (byte) 0x55};

    /**
     * 生成九位字节数组指令
     *
     * @param flag  指令第二位数值
     * @param b1    指令第三位数值
     * @param b2    指令第四位数值
     * @return 九位字节数组指令
     */
    private static byte[] make3Bytes(byte flag, byte b1, byte b2) {
        return  make5Bytes(flag, b1, b2,(byte)0,(byte)0);
    }

    /**
     * 生成发送升级包数据指令
     * @param b0  升级数据序号1
     * @param b1  升级数据序号2
     * @param b2    升级数据1
     * @param b3    升级数据2
     * @return  升级数据包数据指令
     */
    public static byte[] makeWriteData(byte b0, byte b1, byte b2, byte b3) {
        return make5Bytes((byte) 0x21, b0, b1, b2, b3);
    }


    private static byte[] make5Bytes(byte flag, byte b0, byte b1, byte b2, byte b3) {
        return new byte[]{(byte) 0xAA, flag, b0, b1, b2, b3, 0x00, 0x00, 0x55};
    }
}
