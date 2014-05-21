package com.voicematch.bouger.main;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.widget.Toast;
import com.voicematch.bouger.R;

public class Intro extends Activity {
	public static final int MESSAGE_STATE_CHANGE = 1;
	public static final int MESSAGE_READ = 2;
	public static final int MESSAGE_WRITE = 3;
	public static final int MESSAGE_DEVICE_NAME = 4;
	public static final int MESSAGE_TOAST = 5;
	public static final int MESSAGE_SPEECH_REC = 6;
	public static final int MESSAGE_SPEECH_REC_ERROR = 7;
	public static final int MESSAGE_SPEECH_REC_START = 8;
	public static final int MESSAGE_SPEECH_REC_BEGIN = 9;
	public static final int MESSAGE_SPEECH_REC_END = 10;

	public static final int BT_DISCONNECTED = 1;
	public static final int BT_CONNECTING = 2;
	public static final int BT_CONNECTED = 3;

	public static final String DEVICE_NAME = "device_name";
	public static final String TOAST = "toast";

	// Local Bluetooth adapter
	private BluetoothAdapter mBluetoothAdapter = null;

	// Intent request codes
	private static final int REQUEST_ENABLE_BT = 2;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.intro);

		// Get local Bluetooth adapter
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		// If the adapter is null, then Bluetooth is not supported
		if (mBluetoothAdapter == null) {
			Toast.makeText(this, "Bluetooth is not available",
					Toast.LENGTH_LONG).show();
			finish();
			return;
		}

		if (!mBluetoothAdapter.isEnabled()) {
			Intent enableIntent = new Intent(
					BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
		}
	}
	
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
        //if(D) Log.d(TAG, "onActivityResult " + resultCode);
        switch (requestCode) {
		case REQUEST_ENABLE_BT:
			// When the request to enable Bluetooth returns
			if (resultCode == Activity.RESULT_OK) {
				// Bluetooth is now enabled, so set up a chat session
				Intent intent = new Intent(Intro.this, Main.class);
				startActivity(intent);
				overridePendingTransition(android.R.anim.fade_in,
						android.R.anim.fade_out);
				finish();
			} else {
				// User did not enable Bluetooth or an error occured
				Toast.makeText(this, R.string.bt_not_enabled_leaving,
						Toast.LENGTH_SHORT).show();
				finish();
			}
			break;
        }
    }

	public boolean onTouchEvent(MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			Intent intent = new Intent(Intro.this, Main.class);
			startActivity(intent);

			overridePendingTransition(android.R.anim.fade_in,
					android.R.anim.fade_out);
			finish();
			return true;
		}
		return false;
	}

	public void onBackPressed() {
		new AlertDialog.Builder(Intro.this).setTitle("종료")
				.setIcon(R.drawable.bouger_small)
				.setMessage("Bouger를 종료하시겠습니까?")
				.setPositiveButton("종료", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						finish();
					}
				})
				.setNegativeButton("취소", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
					}
				}).show();
	}
}