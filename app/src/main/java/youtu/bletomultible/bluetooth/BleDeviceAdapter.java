package youtu.bletomultible.bluetooth;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import youtu.bletomultible.R;

/**
 * Created by djf on 2017/3/17.
 */

public class BleDeviceAdapter extends RecyclerView.Adapter<BleDeviceAdapterHolder> {

    private Context mContext;
    private HashMap<String, BleDeviceBean> datas;
    private List< BleDeviceBean> list;

    public BleDeviceAdapter(Context mContext, HashMap<String,  BleDeviceBean> datas) {
        this.mContext = mContext;
        this.datas = datas;
        list = new ArrayList<>();
        for (String add : datas.keySet()) {
            list.add(datas.get(add));
        }

        System.out.println(
                "LLLLLLLLLL   " + list.size()
        );
    }

    @Override
    public  BleDeviceAdapterHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_device, parent, false);
         BleDeviceAdapterHolder holder = new  BleDeviceAdapterHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder( BleDeviceAdapterHolder holder, int position) {

        holder.name.setText("name:"+list.get(position).getName());
        holder.mac.setText("mac:"+list.get(position).getMac());
        switch (list.get(position).getType()) {
            case 1:
                holder.type.setText("手环");
                break;
            case 2:
                holder.type.setText("主控板");
                break;
            case 3:
                holder.type.setText("计步器");
                break;
            case 4:
                holder.type.setText("三角心率计");

                break;
        }
        holder.serviceUUID.setText("serviceUUID:"+list.get(position).getSendUUID().toString());
        holder.notifyUUID.setText("notifyUUID:"+list.get(position).getNotifyUUID().toString());
        holder.configUUID.setText("configUUID:"+list.get(position).getConfigUUID().toString());
        holder.sendUUID.setText("sendUUID:"+list.get(position).getSendUUID().toString());
        holder.values.setText("values:"+list.get(position).getValues());
        holder.state.setText("state:"+list.get(position).getState()+"");
    }

    @Override
    public int getItemCount() {
        return datas.size();
    }
}
