package com.example.myapplication;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.pm.ActivityInfo;
import android.os.Bundle;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

/**
 * DeviceListActivity
 *
 * @author Alexandre Louis <Alexandre_Louis@outlook.fr>
 *
 * 08/12/2020
 */
public class DeviceListActivity extends Activity {
	private DeviceListAdapter mAdapter;
	private ArrayList<BluetoothDevice> mDeviceList;


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		
		setContentView(R.layout.activity_paired_devices);

		boolean scan = (boolean) Objects.requireNonNull(getIntent().getExtras()).get("scan");

		mDeviceList		= getIntent().getExtras().getParcelableArrayList("device.list");

		ListView listView = findViewById(R.id.lv_paired);

		mAdapter		= new DeviceListAdapter(this);

		BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

		List<BluetoothDevice> knownDevices = new ArrayList<>(bluetoothAdapter.getBondedDevices());
		ArrayAdapter<BluetoothDevice> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, knownDevices);
		listView.setAdapter(adapter);
		listView.setOnItemClickListener( deviceListListener );

		mAdapter.setData(mDeviceList);
		mAdapter.setListener(new DeviceListAdapter.OnPairButtonClickListener() {			
			@Override
			public void onPairButtonClick(int position) {
				BluetoothDevice device = mDeviceList.get(position);
				
				if (device.getBondState() == BluetoothDevice.BOND_BONDED) {
					unpairDevice(device);
				} else {
					showToast("Appairage...");
					
					pairDevice(device);
				}
			}

		});

		if(scan)
		{
			listView.setAdapter(mAdapter);
		}


		registerReceiver(mPairReceiver, new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED)); 
	}

	ListView.OnItemClickListener deviceListListener = new ListView.OnItemClickListener() {
		@Override public void onItemClick(AdapterView<?> adapter, View view, int arg2, long rowId) {
			BluetoothDevice device = mDeviceList.get( (int) rowId );
			Intent intent = new Intent();
			intent.putExtra("device",device);
			DeviceListActivity.this.setResult(Activity.RESULT_OK, intent);
		}
	};

	@Override
	public void onDestroy() {
		unregisterReceiver(mPairReceiver);
		
		super.onDestroy();
	}
	
	
	private void showToast(String message) {
		Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
	}
	
    private void pairDevice(BluetoothDevice device) {
        try {
            Method method = device.getClass().getMethod("createBond", (Class[]) null);
            method.invoke(device, (Object[]) null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void unpairDevice(BluetoothDevice device) {
        try {
            Method method = device.getClass().getMethod("removeBond", (Class[]) null);
            method.invoke(device, (Object[]) null);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private final BroadcastReceiver mPairReceiver = new BroadcastReceiver() {
	    public void onReceive(Context context, Intent intent) {
	        String action = intent.getAction();
	        
	        if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {	        	
	        	 final int state 		= intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.ERROR);
	        	 final int prevState	= intent.getIntExtra(BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE, BluetoothDevice.ERROR);
	        	 
	        	 if (state == BluetoothDevice.BOND_BONDED && prevState == BluetoothDevice.BOND_BONDING) {
	        		 showToast("Appairé");
	        	 } else if (state == BluetoothDevice.BOND_NONE && prevState == BluetoothDevice.BOND_BONDED){
	        		 showToast("Oublié");
	        	 }
	        	 
	        	 mAdapter.notifyDataSetChanged();
	        }
	    }
	};
}