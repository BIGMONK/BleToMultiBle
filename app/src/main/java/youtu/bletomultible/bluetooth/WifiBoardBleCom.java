package youtu.bletomultible.bluetooth;

import android.support.annotation.NonNull;

/**
 * Created by djf on 2017/8/12.
 * wifi主板蓝牙指令
 */


public class WifiBoardBleCom {

    /**
     * 根据指令类型和数据包生成要发送的数据包
     *
     * @param type  指令类型
     * @param data2 附加数据（字节数组）
     * @return
     */
    public static byte[] makeDatas(Type type, @NonNull byte[] data2) {
        byte[] data1 = new byte[]{(byte) type.getValue()};
        if (data2 == null || data2.length == 0) {
            return data1;
        }
        if (type == Type.SetWifiName || type == Type.SetWifiKey || type == Type.GetBoardTC) {
            byte[] data3 = new byte[data1.length + data2.length];
            System.arraycopy(data1, 0, data3, 0, data1.length);
            System.arraycopy(data2, 0, data3, data1.length, data2.length);
            return data3;
        } else {
            return data1;
        }
    }


    /**
     * 根据指令类型和数据包生成要发送的数据包
     *
     * @param type 指令类型
     * @return
     */
    public static byte[] makeDatas(Type type) {
        return new byte[type.getValue()];
    }


    /**
     * 根据指令类型和数据包生成要发送的数据包
     *
     * @param type 指令类型
     * @param ss   附加数据(字符串)
     * @return
     */
    public static byte[] makeDatas(Type type, @NonNull String ss) {
        return makeDatas(type, ss.getBytes());
    }

    /**
     * wifi板子蓝牙指令类型
     */
    public enum Type {
        /**
         * 设置wifi名称
         */
        SetWifiName(1),
        /**
         * 设置wifi密码
         */
        SetWifiKey(2),
        /**
         * 获取主板ip
         */GetBoardIp(3),
        /**
         * 获取主板mac
         */GetBoardMac(4),
        /**
         * 获取主板客户端数量
         */GetBoardCC(5),
        /**
         * 获取主板客户端ip
         */GetBoardTC(6),
        /**
         * 获取主板服务器状态
         */GetBoardSS(7),
        /**
         * 重置主板
         */ResetBoard(8);

        /**
         * 指令数值
         */
        private int i;


        Type(int i) {
            this.i = i;
        }

        /**
         * 获取枚举对象数值
         *
         * @return
         */
        public int getValue() {
            return this.i;
        }

    }

}
