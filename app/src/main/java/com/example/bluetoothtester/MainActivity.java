package com.example.bluetoothtester;

import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private BluetoothConnector connector;
    private ArrayList<BluetoothDevice> devices;

    private ListView lv;
    private ListView nearLv;
    private MyAdapter myAdapter;
    private MyAdapter myNearAdapter;
    private TextView tvName;
    private TextView tvAddress;
    private TextView tvUUID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        connector = BluetoothConnector.getInstance();

        if((devices = connector.getDevicesList()) != null) {
            myAdapter = new MyAdapter(this);
            myAdapter.setData(devices);

            lv = findViewById(R.id.bluetoothList);
            lv.setDivider(new ColorDrawable(Color.BLUE));
            lv.setDividerHeight(1);
            lv.setAdapter(myAdapter);

        }else {
            System.out.println("No device");
        }

//        devices = connector.getDevicesList();

        Log.d("zzf", "onCreate: new adapter");

        MyAdapter nearAdapter = new MyAdapter(this);
        nearLv = findViewById(R.id.nearbyList);
        nearLv.setDivider(new ColorDrawable(Color.YELLOW));
        nearLv.setDividerHeight(1);
        connector.getDiscoveredDevices(this, nearLv, nearAdapter);
    }

    class MyAdapter extends BaseAdapter {

        private Context mContext;

        public MyAdapter(Context mContext) {
            this.mContext = mContext;
        }

        private List<BluetoothDevice> mDataList;

        public void setData(List<BluetoothDevice> data){
            mDataList = data;
        }

        @Override
        public int getCount() {
            return mDataList.size();
        }

        @Override
        public Object getItem(int position) {
            return mDataList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null){
                convertView = getLayoutInflater().inflate(R.layout.list_item, parent,false);
            }

            String name = mDataList.get(position).getName();
            if(name == null || name.isEmpty()) {
                name = "UNKNOWN DEVICE";
            }
            tvName = convertView.findViewById(R.id.tvName);
            tvName.setText(name);

            tvAddress = convertView.findViewById(R.id.tvAddress);
            tvAddress.setText("MAC: " + mDataList.get(position).getAddress());

            if(mDataList.get(position).getUuids() != null) {
                tvUUID = convertView.findViewById(R.id.tvUUID);
                String uuids = "UUIDs List:\n";
                for (int i = 0; i < mDataList.get(position).getUuids().length; i++) {
                    uuids += mDataList.get(position).getUuids()[i] + "\n";
                }
                tvUUID.setText(uuids);
            }

            return convertView;
        }
    }
}
