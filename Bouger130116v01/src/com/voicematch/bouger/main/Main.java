package com.voicematch.bouger.main;

import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.Toast;

import com.voicematch.bouger.R;
import com.voicematch.bouger.alarm.BougerAlarm;
import com.voicematch.bouger.alarm.BougerAlarmPage;
import com.voicematch.bouger.play.BluetoothChatService;
import com.voicematch.bouger.play.BougerPlay;
import com.voicematch.bouger.play.ByteQueue;
import com.voicematch.bouger.play.Trilateration;

public class Main extends Activity implements OnClickListener {
	// Debugging
	private static final String TAG = "BluetoothChat";
	private static final boolean D = true;
	private static final boolean ENABLE_SPEECH_REC = false;

	public static final int MIC_INIT_DATA_COUNT = 10;
	
	private static final boolean LOG_WRITE = false;
	public static ByteQueue mMsgQueue;
	public static int mSenBaseValue[] = {0, 0, 0, 0};

	// File Writer
	FileOutputStream fos = null;
	FileOutputStream fos2 = null;
	
	public static int mMotorPlaySpeedL = 50;
	public static int mMotorPlaySpeedR = 50;

	// Message types sent from the BluetoothChatService Handler
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

	// Key names received from the BluetoothChatService Handler
	public static final String DEVICE_NAME = "device_name";
	public static final String TOAST = "toast";

	// Intent request codes
	private static final int REQUEST_CONNECT_DEVICE = 1;
	private static final int REQUEST_ENABLE_BT = 2;
	private static final int SPEECH_REC = 3;

	ImageButton bluetooth;
	
	private final Context mainContext = this;
	
	// Speech Recognition
    SpeechRecognition mSpeechRecognition;
    
    private ArrayList<String> mResult;						//음성인식 결과 저장할 list

	// Name of the connected device
	private String mConnectedDeviceName = null;
	// Local Bluetooth adapter
	public static BluetoothAdapter mBluetoothAdapter = null;
	// Member object for the chat services
	public static BluetoothChatService mChatService = null;

	private int btConnectStatus = BT_DISCONNECTED;

	public static int btConnectionStatus = 0;
	
	private String mMACAddress = "00:19:01:21:D2:6D";

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		ImageButton BougerPlay = (ImageButton) findViewById(R.id.BougerPlay);
		BougerPlay.setOnClickListener(this);
		ImageButton BougerAlarm = (ImageButton) findViewById(R.id.BougerAlarm);
		BougerAlarm.setOnClickListener(this);
		bluetooth = (ImageButton) findViewById(R.id.connect);
		bluetooth.setOnClickListener(this);
		
		mMsgQueue = new ByteQueue();
        mMsgQueue.ensureCapacity(4096);

		// Get local Bluetooth adapter
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

		// If the adapter is null, then Bluetooth is not supported
		if (mBluetoothAdapter == null) {
			Toast.makeText(this, "Bluetooth is not available",
					Toast.LENGTH_LONG).show();
			finish();
			return;
		}
		
		if(mChatService == null || mChatService.getState() != BluetoothChatService.STATE_CONNECTED){
			bluetooth.setBackgroundResource(R.drawable.connect);
		} else if(mChatService.getState() == BluetoothChatService.STATE_CONNECTED){
			bluetooth.setBackgroundResource(R.drawable.disconnect);
		}
		
	}

	@Override
	public void onStart() {
		super.onStart();
		if (D)
			Log.e(TAG, "++ ON START ++");

		// If BT is not on, request that it be enabled.
		// setupChat() will then be called during onActivityResult
		if (!mBluetoothAdapter.isEnabled()) {
			Intent enableIntent = new Intent(
					BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
			// Otherwise, setup the chat session
		} else {
			if (mChatService == null)
				setupChat();
		}
	}

	@Override
	public synchronized void onResume() {
		super.onResume();
		mChatService.setIndexOfMessages(MESSAGE_STATE_CHANGE, MESSAGE_READ, MESSAGE_DEVICE_NAME, MESSAGE_TOAST);
		mChatService.setDeviceNameString(DEVICE_NAME);
		mChatService.setToastString(TOAST);
		if (D)
			Log.e(TAG, "+ ON RESUME +");

		// Performing this check in onResume() covers the case in which BT was
		// not enabled during onStart(), so we were paused to enable it...
		// onResume() will be called when ACTION_REQUEST_ENABLE activity
		// returns.
		if (mChatService != null) {
			// Only if the state is STATE_NONE, do we know that we haven't
			// started already
			if (mChatService.getState() == BluetoothChatService.STATE_NONE) {
				// Start the Bluetooth chat services
				mChatService.start();
				setupChat();
			}else if(mChatService.getState() == BluetoothChatService.STATE_LISTEN){
				setupChat();
			}
		}
	}

	private void setupChat() {
		Log.d(TAG, "setupChat()");

		// Initialize the BluetoothChatService to perform bluetooth connections
		mChatService = new BluetoothChatService(this, mHandler);
		mChatService.setIndexOfMessages(MESSAGE_STATE_CHANGE, MESSAGE_READ, MESSAGE_DEVICE_NAME, MESSAGE_TOAST);
		mChatService.setDeviceNameString(DEVICE_NAME);
		mChatService.setToastString(TOAST);

		// Timer setting
		TimerTask timer100ms = new TimerTask() {
			public void run() {
				if (btConnectStatus == BT_CONNECTED) {
					byte[] buff = {0x02, 'S', (byte)0xAD, 0x03};
	        		sendMessage(buff, 4);
				}
			}
		};
		Timer timer = new Timer();
		timer.schedule(timer100ms, 500, 100);
		
		// start speech recognition
    	if(ENABLE_SPEECH_REC)
    		mSpeechRecognition = new SpeechRecognition(mainContext, mHandler);
		
		if (mChatService.getState() != BluetoothChatService.STATE_CONNECTED) {
			BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(mMACAddress);
			mChatService.connect(device);
		}
	}

	@Override
	public synchronized void onPause() {
		super.onPause();
		if (D)
			Log.e(TAG, "- ON PAUSssssE -");
	}

	/**
	 * Sends a message.
	 * 
	 * @param message
	 *            A string of text to send.
	 */
	private void sendMessage(byte[] buff, int len) {
		// Check that we're actually connected before trying anything
		if (mChatService.getState() != BluetoothChatService.STATE_CONNECTED)
			return;

		// Check that there's actually something to send
		if (len > 0) {
			// Get the message bytes and tell the BluetoothChatService to write
			mChatService.write(buff);
		}
	}
	
	private Boolean isValidProtocol(byte[] buff, int len) {
        byte checksum = 0;
        for (int i = 1 ; i < len-1 ; i++) {
            checksum += buff[i];
        }
        
        if( (checksum&0x00ff) == 0 )
            return true;
        else
            return false;
    }

	private byte checksum(byte[] buff, int len) {
		byte checksum = 0;
		for (int i = 1; i < len - 2; i++) {
			checksum += buff[i];
		}

		checksum = (byte) ~checksum;
		checksum++;

		return checksum;
	}

	private void moveMotor(byte left, byte right) {
		byte[] buff = new byte[6];
		buff[0] = 0x02;
		buff[1] = 'M';
		buff[2] = left;
		buff[3] = right;
		buff[4] = checksum(buff, 6);
		buff[5] = 0x03;
		sendMessage(buff, 6);
	}

	public static void setRandPlaySpeed() {
		Random rand = new Random();
		int randL = rand.nextInt(); //int형 표현 범위(-2147483648 ~ 2147483647)
		randL = randL % 15; //나머지연산자, old 20
		mMotorPlaySpeedL =  65 + randL; //edit , old 50 
		int randR = rand.nextInt(); //int형 표현 범위(-2147483648 ~ 2147483647)
		randR = randR % 15; //나머지연산자, old 20
		mMotorPlaySpeedR =  65 + randR; //edit , old 50 

		//if(mMotorPlaySpeedL < 75) mMotorPlaySpeedL = 75;
		//if(mMotorPlaySpeedR < 75) mMotorPlaySpeedR = 75;
	}
	
	// The Handler that gets information back from the BluetoothChatService
	private final Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MESSAGE_STATE_CHANGE:
				if (D)
					Log.i(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
				switch (msg.arg1) {
				case BluetoothChatService.STATE_CONNECTED:
					btConnectStatus = BT_CONNECTED;
					break;
				case BluetoothChatService.STATE_CONNECTING:
					Toast.makeText(getApplicationContext(), "연결 중 입니다.",
							Toast.LENGTH_SHORT).show();
					btConnectStatus = BT_CONNECTING;
					break;
				case BluetoothChatService.STATE_LISTEN:
				case BluetoothChatService.STATE_NONE:
					btConnectStatus = BT_DISCONNECTED;
					break;
				}
				break;
			case MESSAGE_DEVICE_NAME:
				// save the connected device's name
				mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
				Toast.makeText(getApplicationContext(),
						mConnectedDeviceName + "에 연결되었습니다.", Toast.LENGTH_SHORT)
						.show();
				bluetooth.setBackgroundResource(R.drawable.disconnect);
				break;
			case MESSAGE_TOAST:
				Toast.makeText(getApplicationContext(),
						msg.getData().getString(TOAST), Toast.LENGTH_LONG)
						.show();
				break;				
			case MESSAGE_READ:
				int len = (int) msg.arg1;
				
				byte[] msgBuf = (byte[]) msg.obj;
				for (int i = 0; i < len; i++) {
					mMsgQueue.insert(msgBuf[i]);
				}
				if (LOG_WRITE) {
					if (BougerAlarmPage.bAlarmRobot || BougerPlay.bPlayRobot) {
						try {
							String sensorData = new String();
							for (int i = 0; i < len; i++) {
								sensorData += String
										.valueOf((int) (msgBuf[i] & 0x00ff))
										+ ", ";
							}
							sensorData += "\r\n";
							fos2.write(sensorData.getBytes());
						} catch (Exception e) {
							Log.i(TAG, e.getMessage());
						}
					}
				}

				final int STATUS_PACKET_LENGTH = 18;
				byte[] readBuf = new byte[STATUS_PACKET_LENGTH];
				while (mMsgQueue.size() >= STATUS_PACKET_LENGTH) {
					byte stxCandidate = mMsgQueue.getFront();
					if (stxCandidate == 0x02) {
						readBuf[0] = stxCandidate;
						for (int i = 1; i < STATUS_PACKET_LENGTH; i++) {
							readBuf[i] = mMsgQueue.getFront();
						}
						break;
					}
				}

				if (readBuf[0] == 0x02 && readBuf[1] == 'S'
						&& readBuf[17] == 0x03) {
					if (LOG_WRITE) {
						if (BougerAlarmPage.bAlarmRobot || BougerPlay.bPlayRobot) {
							try {
								String sensorData = new String();
								for (int i = 0; i < STATUS_PACKET_LENGTH; i++) {
									sensorData += String
											.valueOf((int) (readBuf[i] & 0x00ff))
											+ ", ";
								}
								if (isValidProtocol(readBuf,
										STATUS_PACKET_LENGTH)) {
									sensorData += "[O]";
								} else {
									sensorData += "[X]";
								}
								sensorData += "\r\n";
								fos.write(sensorData.getBytes());
							} catch (Exception e) {
								Log.i(TAG, e.getMessage());
							}
						}
					}

					if (isValidProtocol(readBuf, STATUS_PACKET_LENGTH)) {
						int bat = (int) (readBuf[15]);

						int mic1 = (int) (readBuf[4] & 0xff);
						int mic2 = (int) (readBuf[5] & 0xff);
						int mic3 = (int) (readBuf[6] & 0xff);

						Trilateration tri = new Trilateration();
						tri.initPoints(-40, 50, 40, 50, 0, -50);
						double angle = tri.getAngle_Deg(mic1, mic2, mic3);
						angle = angle * 180 / 3.14;

						int sen1_l = (readBuf[7]&0x00ff);
                        int sen1_h = (readBuf[8]<<8)&0xff00;
                        int sen2_l = (readBuf[9]&0x00ff);
                        int sen2_h = (readBuf[10]<<8)&0xff00;
                        int sen3_l = (readBuf[11]&0x00ff);
                        int sen3_h = (readBuf[12]<<8)&0xff00;
                        int sen4_l = (readBuf[13]&0x00ff);
                        int sen4_h = (readBuf[14]<<8)&0xff00;

                        int[] sen = {0, 0, 0, 0};
                        sen[0] = sen1_h + sen1_l;
                        sen[1] = sen2_h + sen2_l;
                        sen[2] = sen3_h + sen3_l;
                        sen[3] = sen4_h + sen4_l;
						
						Log.i("sen", ""+sen[0]);
						Log.i("sen2", ""+sen[1]);
						Log.i("sen3", ""+sen[2]);
						Log.i("sen4", ""+sen[3]);

						if (BougerAlarmPage.bReqSensorBase || BougerPlay.bPlaySensorBase) {
							BougerAlarmPage.bReqSensorBase = false;
							BougerPlay.bPlaySensorBase = false;
							for (int i = 0; i < 4; i++) {
								mSenBaseValue[i] = sen[i];
								//Log.i("mSenBaseValue", ""+mSenBaseValue[i]);
								Log.i("mSenBaseValue", ""+BougerPlay.bPlaySensorBase);
							}
						}					
						
						if (BougerAlarmPage.bAlarmRobot || BougerPlay.bPlayRobot) {
							Log.i("len", "=========="+BougerAlarmPage.bAlarmRobot);
							Log.i("lenplay", "=========="+BougerPlay.bPlayRobot);
							Boolean[] bClief = { false, false, false, false };
							Log.i("bclief", bClief[0] + " " + bClief[1] + " " + bClief[2] + " " + bClief[3]);
							for (int i = 0; i < 4; i++) {
								Log.i("들어감", "들어감");
								if (sen[i] < mSenBaseValue[i] - 200) {
									bClief[i] = true;
									Log.i("bclief", ""+bClief[i]);
								}
							}
							Boolean[] bWall = { false, false, false, false };
							for (int i = 0; i < 4; i++) {
								if (sen[i] > mSenBaseValue[i] + 500) {
									bWall[i] = true;
									Log.i("bwall", ""+bWall[i]);
								}
							}

							int MOTOR_SPEED = 100; // 모터의 최대 속도를 127로 설정한다.
							int motorL = 0;
							int motorR = 0;
							if (bClief[0] && bClief[2] && bClief[3]) { // Bouger가 들려있으면 정지
								motorL = 0;
								motorR = 0;
								Log.i("clief1", bClief[0] + " " + bClief[1] + " " + bClief[2]);
							} else if (bClief[3]) { // 후면이 낭떠러지이면
								motorL = MOTOR_SPEED;
								motorR = MOTOR_SPEED;
								Log.i("clief2", " " + bClief[3]);
							} else if (bClief[0] && bClief[2]) { // 전면이 낭떠러지이면
								motorL = -MOTOR_SPEED;
								motorR = -MOTOR_SPEED;
								Log.i("clief3", bClief[0] + " " + bClief[2]);
							} else if (bClief[0]) { // 우측 전면이 낭떠러지이면
								motorL = -MOTOR_SPEED;
								motorR = MOTOR_SPEED / 2;
								Log.i("clief4", " " + bClief[0]);
							} else if (bClief[2]) { // 좌측 전면이 낭떠러지이면
								motorL = MOTOR_SPEED / 2;
								motorR = -MOTOR_SPEED;
								Log.i("clief5", " " + bClief[2]);
							} else if (bWall[1]) { // 전면이 벽이면
								motorL = -MOTOR_SPEED;
								motorR = -MOTOR_SPEED;
								Log.i("wall", " " + bWall[1]);
							} else {								
								motorL = mMotorPlaySpeedL;
								motorR = mMotorPlaySpeedR;
								Log.i("mMotorPlaySpeedL", ""+mMotorPlaySpeedL);
								Log.i("mMotorPlaySpeedR", ""+mMotorPlaySpeedR);
							}

							moveMotor((byte) motorL, (byte) motorR);
							Log.i("motorL", ""+motorL);
							Log.i("motorR", ""+motorR);
						}
					}
				}
				break;
			case MESSAGE_SPEECH_REC:
				break;
			case MESSAGE_SPEECH_REC_ERROR:
				switch (msg.arg1) {
				case SpeechRecognizer.ERROR_AUDIO:
					Toast.makeText(Main.this, "오디오 입력 중 오류가 발생했습니다.", Toast.LENGTH_LONG).show();
					break;
				case SpeechRecognizer.ERROR_CLIENT:
					Toast.makeText(Main.this, "단말에서 오류가 발생했습니다.", Toast.LENGTH_LONG).show();
					break;
				case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
					Toast.makeText(Main.this, "권한이 없습니다.", Toast.LENGTH_LONG).show();
					break;
				case SpeechRecognizer.ERROR_NETWORK:
				case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
					Toast.makeText(Main.this, "네트워크 오류가 발생했습니다.", Toast.LENGTH_LONG).show();
					break;
				case SpeechRecognizer.ERROR_NO_MATCH:
					Toast.makeText(Main.this, "일치하는 항목이 없습니다.", Toast.LENGTH_LONG).show();
					break;
				case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
					Toast.makeText(Main.this, "음성인식 서비스가 과부하 되었습니다.", Toast.LENGTH_LONG).show();
					break;
				case SpeechRecognizer.ERROR_SERVER:
					Toast.makeText(Main.this, "서버에서 오류가 발생했습니다.", Toast.LENGTH_LONG).show();
					break;
				case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
					Toast.makeText(Main.this, "입력이 없습니다.", Toast.LENGTH_LONG).show();
					break;
				}
				break;
			case MESSAGE_SPEECH_REC_START:
				break;
			case MESSAGE_SPEECH_REC_BEGIN:
				break;
			case MESSAGE_SPEECH_REC_END:
				break;
			}
		}
	};

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (D)
			Log.d(TAG, "onActivityResult " + resultCode);
		switch (requestCode) {
		case REQUEST_CONNECT_DEVICE:
			// When DeviceListActivity returns with a device to connect
			if (resultCode == Activity.RESULT_OK) {
				connectDevice(data);
			}
			break;
		case REQUEST_ENABLE_BT:
			// When the request to enable Bluetooth returns
			if (resultCode == Activity.RESULT_OK) {
				// Bluetooth is now enabled, so set up a chat session
				setupChat();
			} else {
				// User did not enable Bluetooth or an error occured
				Toast.makeText(this, R.string.bt_not_enabled_leaving,
						Toast.LENGTH_SHORT).show();
				finish();
			}
			break;
		case SPEECH_REC:
            // When DeviceListActivity returns with a device to connect
            if (resultCode == Activity.RESULT_OK) {
    			mResult = data.getStringArrayListExtra(SpeechRecognizer.RESULTS_RECOGNITION);		//인식된 데이터 list 받아옴.
    			String[] result = new String[mResult.size()];										//배열생성. 다이얼로그에서 출력하기 위해
    			mResult.toArray(result);															//	list 배열로 변환
            }
            break;
		}
	}

	private void connectDevice(Intent data) {
		// Get the device MAC address
		String address = data.getExtras().getString(
				DeviceListActivity.EXTRA_DEVICE_ADDRESS);
		// Get the BLuetoothDevice object
		BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
		// Attempt to connect to the device
		mChatService.connect(device);
	}

	public void onClick(View v) {
		if (v.getId() == R.id.BougerPlay) {
			Main.this.onPause();
			Intent newActivity1 = new Intent(Main.this, BougerPlay.class);
			Main.this.startActivity(newActivity1);
			overridePendingTransition(android.R.anim.fade_in,
					android.R.anim.fade_out);
		} else if (v.getId() == R.id.BougerAlarm) {
			Main.this.onPause();
			Intent newActivity2 = new Intent(Main.this, BougerAlarm.class);
			Main.this.startActivity(newActivity2);
			overridePendingTransition(android.R.anim.fade_in,
					android.R.anim.fade_out);
		} else if (v.getId() == R.id.connect) {
			Main.this.onPause();
			if(mChatService.getState() != BluetoothChatService.STATE_CONNECTED){
				startActivityForResult(new Intent(this,
						DeviceListActivity.class), REQUEST_CONNECT_DEVICE);
			} else if(mChatService.getState() == BluetoothChatService.STATE_CONNECTED){
				new AlertDialog.Builder(Main.this)
				.setTitle("연결 해제")
				.setIcon(R.drawable.bouger_small)
				.setMessage(
						"연결을 해제하시면 알람이 울릴 때 Bouger가 움직이지 않습니다.\n계속하시겠습니까?")
				.setPositiveButton("확인",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int which) {
								bluetooth.setBackgroundResource(R.drawable.connect);
								if (mChatService != null) {
									mChatService.stop();
									Toast.makeText(Main.this,
											"Bouger와 연결이 해제되었습니다.",
											Toast.LENGTH_LONG).show();
								}
							}
						})
				.setNegativeButton("취소",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int which) {
							}
						}).show();
			}
		}
	}

	public void onBackPressed() {
		new AlertDialog.Builder(Main.this).setTitle("종료")
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