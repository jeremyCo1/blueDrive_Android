package com.example.myapplication;

import io.github.controlwear.virtual.joystick.android.JoystickView;

import java.util.ArrayList;
import java.util.Set;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;

import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;

import android.app.ProgressDialog;

import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;

import androidx.appcompat.app.AlertDialog;


/**
 *MainActivity
 *
 * @author Alexandre Louis <Alexandre_Louis@outlook.fr>
 *
 * 08/12/2020
 */

public class MainActivity extends Activity {
	private TextView etatBluetooth;

	private boolean scan;

	private Button mBtnAppareils;
	private Button mBtnOnOff;
	private Button mBtnScan;

	private ProgressDialog mDlg;

	private ArrayList<BluetoothDevice> mlisteAppareils = new ArrayList<>();

	private BluetoothAdapter mBluetoothAdapter;
	private BluetoothClient mBluetoothClient = null;

	private static final int PERMISSION_REQUEST_FINE_LOCATION = 2;
	private static final int PERMISSION_REQUEST_COARSE_LOCATION = 3;
	private static final int LAUNCH_DEVICE_LIST_ACTIVITY = 1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

		//check if bluetooth is available or not
		if (mBluetoothAdapter == null){
			showToast("Bluetooth non disponible");
		}
		else {
			showToast("Bluetooth disponible");
		}

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			if (this.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
					== PackageManager.PERMISSION_GRANTED) {
				if (this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
						!= PackageManager.PERMISSION_GRANTED) {
					if (this.shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_COARSE_LOCATION)) {
						final AlertDialog.Builder builder = new AlertDialog.Builder(this);
						builder.setTitle("This app needs background location access");
						builder.setMessage("Please grant location access so this app can detect beacons in the background.");
						builder.setPositiveButton(android.R.string.ok, null);
						builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

							@TargetApi(23)
							@Override
							public void onDismiss(DialogInterface dialog) {
								requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
										PERMISSION_REQUEST_COARSE_LOCATION);
							}

						});
						builder.show();
					} else {
						final AlertDialog.Builder builder = new AlertDialog.Builder(this);
						builder.setTitle("Functionality limited");
						builder.setMessage("Since background location access has not been granted, this app will not be able to discover beacons in the background.  Please go to Settings -> Applications -> Permissions and grant background location access to this app.");
						builder.setPositiveButton(android.R.string.ok, null);
						builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

							@Override
							public void onDismiss(DialogInterface dialog) {
							}

						});
						builder.show();
					}

				}
			} else {
				if (!this.shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
					requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
									Manifest.permission.ACCESS_COARSE_LOCATION},
							PERMISSION_REQUEST_FINE_LOCATION);
				} else {
					final AlertDialog.Builder builder = new AlertDialog.Builder(this);
					builder.setTitle("Functionality limited");
					builder.setMessage("Since location access has not been granted, this app will not be able to discover beacons.  Please go to Settings -> Applications -> Permissions and grant location access to this app.");
					builder.setPositiveButton(android.R.string.ok, null);
					builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

						@Override
						public void onDismiss(DialogInterface dialog) {
						}

					});
					builder.show();
				}

			}
		}

		setContentView(R.layout.activity_main);

		etatBluetooth 			= findViewById(R.id.etat_bluetooth);
		mBtnAppareils 			= findViewById(R.id.btn_view_paired);

		mBtnOnOff= findViewById(R.id.onBtn);
		mBtnScan = findViewById(R.id.devicesBtn);
		Button btnKlaxon = findViewById(R.id.horn);

		Switch phares = findViewById(R.id.lights);
		Switch clignoG = findViewById(R.id.leftInd);
		Switch glignoD = findViewById(R.id.rightInd);

		SeekBar curseurVitesse = findViewById(R.id.speed);

		TextView etatConnexion = findViewById(R.id.lblConnectedDevice);

		JoystickView joystick = findViewById(R.id.joystick);


		mBluetoothAdapter	= BluetoothAdapter.getDefaultAdapter();

		mDlg 		= new ProgressDialog(this);


		mDlg.setMessage("Scan...");
		mDlg.setCancelable(false);
		mDlg.setButton(DialogInterface.BUTTON_NEGATIVE, "Annuler", new DialogInterface.OnClickListener() {
		    @Override
		    public void onClick(DialogInterface dialog, int which) {
		        dialog.dismiss();

		        mBluetoothAdapter.cancelDiscovery();
		    }
		});

		curseurVitesse.setMax(100);
		curseurVitesse.setProgress(0);

		if (mBluetoothAdapter == null) {
			showUnsupported();
		}
		else {
			System.out.println("bluetoothClient:"+mBluetoothClient);

			mBtnAppareils.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();

					if (pairedDevices == null || pairedDevices.size() == 0) {
						showToast("Pas d'appareils appairés trouvés");
					} else {
						System.out.println("bluetoothClient:"+mBluetoothClient);

						scan = false;

						ArrayList<BluetoothDevice> list = new ArrayList<>(pairedDevices);

						Intent intent = new Intent(MainActivity.this, DeviceListActivity.class);

						intent.putParcelableArrayListExtra("device.list", list);
						intent.putExtra("scan", scan);


						startActivityForResult(intent,LAUNCH_DEVICE_LIST_ACTIVITY);
					}
				}

			});

			mBtnScan.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View arg0) {
					mBluetoothAdapter.startDiscovery();
				}
			});


			mBtnOnOff.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					if (mBluetoothAdapter.isEnabled()) {
						mBluetoothAdapter.disable();

						showToast("Désactivation du Bluetooth...");

						showDisabled();
					} else {
						Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);

					    startActivityForResult(intent, 1000);
						showToast("Activation du Bluetooth...");
					}
				}
			});

			phares.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					if(mBluetoothClient != null) {
						String code = "phares\n";
						mBluetoothClient.writeChar(code);
					}
				}
			});

			clignoG.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					if(mBluetoothClient != null) {
						String code = "clignG\n";
						mBluetoothClient.writeChar(code);
					}
				}
			});

			glignoD.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					if(mBluetoothClient != null) {
						String code = "clignD\n";
						mBluetoothClient.writeChar(code);
					}
				}
			});

			btnKlaxon.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					if(mBluetoothClient != null) {
						String code = "klaxon\n";
						mBluetoothClient.writeChar(code);
					}
				}
			});

			curseurVitesse.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
				@Override
				public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
					if(mBluetoothClient != null) {
						String code;
						if(progress<10)
						{
							code = "vit  "+progress+"\n";
						} else if (progress == 100)
						{
							code = "vit"+progress+"\n";
						}
						else
						{
							code = "vit "+progress+"\n";
						}
						mBluetoothClient.writeChar(code);
					}
				}

				@Override
				public void onStartTrackingTouch(SeekBar seekBar) {

				}

				@Override
				public void onStopTrackingTouch(SeekBar seekBar) {

				}
			});

			joystick.setOnMoveListener(new JoystickView.OnMoveListener() {
				@Override
				public void onMove(int angle, int strength) {
					// do whatever you want
					String code;
					String code2;
					if(strength<10)
					{
						code = "el "+strength+"  \n";
					} else if (strength == 100)
					{
						code = "el "+strength+"\n";
					}
					else
					{
						code = "el "+strength+" \n";
					}
					if(angle<10)
					{
						code2 = "an "+angle+"  \n";
					} else if (angle >= 100)
					{
						code2 = "an "+angle+"\n";
					}
					else
					{
						code2 = "an "+angle+" \n";
					}
					if(mBluetoothClient != null) {
						mBluetoothClient.writeChar(code2);
						//mBluetoothClient.writeChar(code);
					}
				}
			});
		}


		if (mBluetoothAdapter.isEnabled()) {
			showEnabled();
		} else {
			showDisabled();
		}

		IntentFilter filter = new IntentFilter();

		filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
		filter.addAction(BluetoothDevice.ACTION_FOUND);
		filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
		filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);

		registerReceiver(mReceiver, filter);
	}


	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == LAUNCH_DEVICE_LIST_ACTIVITY && resultCode == Activity.RESULT_OK) {
			BluetoothDevice mDevice = data.getParcelableExtra("device");
			assert mDevice != null;
			mBluetoothClient = new BluetoothClient(mDevice);
			System.out.println("connected");
		}

	}

	@Override
	public void onPause() {
		if (mBluetoothAdapter != null) {
			if (mBluetoothAdapter.isDiscovering()) {
				mBluetoothAdapter.cancelDiscovery();
			}
		}

		super.onPause();
	}

	@Override
	public void onDestroy() {
		unregisterReceiver(mReceiver);

		super.onDestroy();
	}

	private void showEnabled() {
		etatBluetooth.setText("Bluetooth On");
		etatBluetooth.setTextColor(Color.BLUE);

		mBtnOnOff.setText("Désactiver");
		mBtnOnOff.setEnabled(true);
		mBtnOnOff.setCompoundDrawables(getDrawable(R.drawable.bluetooth_logo_off),null,null,null);

		mBtnAppareils.setEnabled(true);
		mBtnScan.setEnabled(true);
	}

	private void showDisabled() {
		etatBluetooth.setText("Bluetooth Off");
		etatBluetooth.setTextColor(Color.RED);

		mBtnOnOff.setText("Activer");
		mBtnOnOff.setEnabled(true);
		mBtnOnOff.setCompoundDrawables(getDrawable(R.drawable.bluetooth_logo),null,null,null);

		mBtnAppareils.setEnabled(false);
		mBtnScan.setEnabled(false);
	}

	private void showUnsupported() {
		etatBluetooth.setText("Le Bluetooth n'est pas supporté par cet appareil");

		mBtnOnOff.setText("Activer");
		mBtnOnOff.setEnabled(false);

		mBtnAppareils.setEnabled(false);
		mBtnScan.setEnabled(false);
	}

	private void showToast(String message) {
		Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
	}

	private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
	    public void onReceive(Context context, Intent intent) {
	        String action = intent.getAction();

	        if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
	        	final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);

	        	if (state == BluetoothAdapter.STATE_ON) {
	        		showToast("Autorisé");

	        		showEnabled();
	        	 }
	        } else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
				mlisteAppareils = new ArrayList<BluetoothDevice>();

				mDlg.show();
	        } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
				mDlg.dismiss();

	        	Intent newIntent = new Intent(MainActivity.this, DeviceListActivity.class);

	        	scan = true;
	        	newIntent.putParcelableArrayListExtra("device.list", mlisteAppareils);
				newIntent.putExtra("scan", scan);

				startActivity(newIntent);
	        } else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
	        	BluetoothDevice device = (BluetoothDevice) intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

				mlisteAppareils.add(device);

				showToast("Appareil trouvé: " + device.getName());
	        }
	    }
	};

}
