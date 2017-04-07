package youtu.bletomultible.bluetooth;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import youtu.bletomultible.R;


/**
 * Created by djf on 2017/3/17.
 */

public class BleDeviceAdapterHolder extends RecyclerView.ViewHolder {
    public TextView name;
    public TextView mac;
    public TextView type;//1  手环   2  主控板   3 计步器   4  三角心率计
    public TextView serviceUUID;
    public TextView notifyUUID;
    public TextView sendUUID;
    public TextView configUUID;
    public TextView values;
    public TextView state;
    public BleDeviceAdapterHolder(View itemView) {
        super(itemView);
        name = (TextView) itemView.findViewById(R.id.name);
        mac = (TextView) itemView.findViewById(R.id.mac);
        type = (TextView) itemView.findViewById(R.id.type);
        serviceUUID = (TextView) itemView.findViewById(R.id.serviceUUID);
        notifyUUID = (TextView) itemView.findViewById(R.id.notifyUUID);
        sendUUID = (TextView) itemView.findViewById(R.id.sendUUID);
        configUUID = (TextView) itemView.findViewById(R.id.configUUID);
        values = (TextView) itemView.findViewById(R.id.values);
        state = (TextView) itemView.findViewById(R.id.state);

    }
}
