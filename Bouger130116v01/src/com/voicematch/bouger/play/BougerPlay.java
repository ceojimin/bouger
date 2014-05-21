package com.voicematch.bouger.play;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import android.app.Activity;
import android.app.TabActivity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.voicematch.bouger.R;
import com.voicematch.bouger.main.Main;

public class BougerPlay extends TabActivity implements OnClickListener,
		OnTabChangeListener {

	TabHost mTab;
	ImageButton BougerPlay_kid_btn, BougerPlay_kid_ran, BougerPlay_kid_voice;
	ImageButton BougerPlay_cat_btn, BougerPlay_cat_ran, BougerPlay_cat_voice;
	ImageButton Ran_play, Ran_play_2;
	LinearLayout Kid_btn, Kid_ran, Kid_voice, Cat_btn, Cat_ran, Cat_voice,
			Introduce_1, Introduce_2;
	TextView text;
//	private ByteQueue mMsgQueue;

	private static final String TAG = "BluetoothChat";
	private static final boolean D = true;

	public static final int MIC_INIT_DATA_COUNT = 10;

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
	private static final int REQUEST_ENABLE_BT = 2;

	private static final boolean LOG_WRITE = false;

	// Layout Manual
	private ImageView mImgJoystick, mImgJoyThumb, mImgJoystick2, mImgJoyThumb2;

	// Local Bluetooth adapter
	BluetoothAdapter mBluetoothAdapter = Main.mBluetoothAdapter;
	// Member object for the chat services
	BluetoothChatService mChatService = Main.mChatService;

	private int btConnectStatus = BT_DISCONNECTED;

	public static boolean bPlayRobot = false;
//	private boolean bReqSensorMin = false;
//	private boolean bReqSensorMax = false;
	public static boolean bPlaySensorBase = false;
	public static int btConnectionStatus = 0;
//	private int mSenMinValue[] = { 0, 0, 0, 0 };
//	private int mSenMaxValue[] = { 65535, 65535, 65535, 65535 };
//	private int mSenBaseValue[] = { 0, 0, 0, 0 };
//	private int mMotorPlaySpeedL = 50;
//	private int mMotorPlaySpeedR = 50;

//	TimerTask mTimertask;

	// Speech recognizer
	private static final int RECOGNIZER = 1001;
	boolean speech_on = false;

	// File Writer
	FileOutputStream fos = null;
	FileOutputStream fos2 = null;
	FileOutputStream fos3 = null;
	FileOutputStream fos4 = null;

	public void initManualView() {
		mImgJoyThumb = (ImageView) findViewById(R.id.imgJoyThumb);
		mImgJoystick = (ImageView) findViewById(R.id.imgJoystick);
		mImgJoystick.setOnTouchListener(new View.OnTouchListener() {
			//private static final int JOY_IMG_WIDTH = 250;
			//private static final int JOY_IMG_HEIGHT = 250;
			//private static final int THUMB_IMG_WIDTH = 100;
			//private static final int THUMB_IMG_HEIGHT = 100;

			public boolean onTouch(View v, MotionEvent event) {
				switch (event.getAction()) {
				case MotionEvent.ACTION_UP: {
					// Release Motor
					mImgJoyThumb.setVisibility(View.INVISIBLE);
					moveMotor((byte) 0, (byte) 0);
					Log.e(TAG, "UP");
					break;
				}
				case MotionEvent.ACTION_DOWN:
				case MotionEvent.ACTION_MOVE: {
					// Set Motor
					float x = event.getX();
					float y = event.getY();
					int bgWidth = mImgJoystick.getWidth();
					int bgHeight = mImgJoystick.getHeight();

					float velocity_linear = ((bgHeight - y) - bgHeight / 2)
							* 150 / bgHeight;
					float velocity_angular = x - bgWidth / 2;
					float k_ang = 0.3f;

					int vL = (int) (velocity_linear + k_ang * velocity_angular);
					int vR = (int) (velocity_linear - k_ang * velocity_angular);
					if (vL < -100)
						vL = -100;
					else if (vL > 100)
						vL = 100;
					if (vR < -100)
						vR = -100;
					else if (vR > 100)
						vR = 100;

					mImgJoyThumb.layout(
							(int) (x - mImgJoyThumb.getWidth() / 2),
							(int) (y - mImgJoyThumb.getHeight() / 2),
							(int) (x + mImgJoyThumb.getWidth() / 2),
							(int) (y + mImgJoyThumb.getHeight() / 2));
					mImgJoyThumb.setVisibility(View.VISIBLE);
					moveMotor((byte) vL, (byte) vR);
					Log.e(TAG, "DOWN");
					break;
				}
				}

				return true;
			}
		});
		mImgJoyThumb2 = (ImageView) findViewById(R.id.imgJoyThumb_2);
		mImgJoystick2 = (ImageView) findViewById(R.id.imgJoystick_2);
		mImgJoystick2.setOnTouchListener(new View.OnTouchListener() {
			//private static final int JOY_IMG_WIDTH = 250;
			//private static final int JOY_IMG_HEIGHT = 250;
			//private static final int THUMB_IMG_WIDTH = 100;
			//private static final int THUMB_IMG_HEIGHT = 100;

			public boolean onTouch(View v, MotionEvent event) {
				switch (event.getAction()) {
				case MotionEvent.ACTION_UP: {
					// Release Motor
					mImgJoyThumb2.setVisibility(View.INVISIBLE);
					moveMotor((byte) 0, (byte) 0);
					break;
				}
				case MotionEvent.ACTION_DOWN:
				case MotionEvent.ACTION_MOVE: {
					// Set Motor
					float x = event.getX();
					float y = event.getY();
					int bgWidth = mImgJoystick2.getWidth();
					int bgHeight = mImgJoystick2.getHeight();

					float velocity_linear = ((bgHeight - y) - bgHeight / 2)
							* 150 / bgHeight;
					float velocity_angular = x - bgWidth / 2;
					float k_ang = 0.3f;

					int vL = (int) (velocity_linear + k_ang * velocity_angular);
					int vR = (int) (velocity_linear - k_ang * velocity_angular);
					if (vL < -100)
						vL = -100;
					else if (vL > 100)
						vL = 100;
					if (vR < -100)
						vR = -100;
					else if (vR > 100)
						vR = 100;

					mImgJoyThumb2.layout(
							(int) (x - mImgJoyThumb.getWidth() / 2),
							(int) (y - mImgJoyThumb.getHeight() / 2),
							(int) (x + mImgJoyThumb.getWidth() / 2),
							(int) (y + mImgJoyThumb.getHeight() / 2));
					mImgJoyThumb2.setVisibility(View.VISIBLE);
					moveMotor((byte) vL, (byte) vR);
					break;
				}
				}

				return true;
			}
		});

	}

//    private void setRandPlaySpeed() {
//        Random rand = new Random();
//        int randL = rand.nextInt();
//        randL = randL % motorVariation; //15
//        mMotorPlaySpeedL =  motorBase + randL; //65
//        int randR = rand.nextInt();
//        randR = randR % motorVariation; //15
//        mMotorPlaySpeedR =  motorBase + randR; //65
//    }

//	private void setRandPlaySpeed() {
//	  Random rand = new Random();
//	  int randL = rand.nextInt(); //int형 표현 범위(-2147483648 ~ 2147483647)
//	  randL = randL % 15; //나머지연산자, old 20
//	  Main.mMotorPlaySpeedL =  65 + randL; //edit , old 50 
//	  int randR = rand.nextInt(); //int형 표현 범위(-2147483648 ~ 2147483647)
//	  randR = randR % 15; //나머지연산자, old 20
//	  Main.mMotorPlaySpeedR =  65 + randR; //edit , old 50 
//	  
////	  if(mMotorPlaySpeedL < 75) mMotorPlaySpeedL = 75;
////	  if(mMotorPlaySpeedR < 75) mMotorPlaySpeedR = 75;
//	}
	
//	private void setRandPlaySpeed() {
//        /*Random rand = new Random();
//        int randL = rand.nextInt();
//        randL = randL % 20;
//        mMotorPlaySpeedL =  50 + randL;
//        int randR = rand.nextInt();
//        randR = randR % 20;
//        mMotorPlaySpeedR =  50 + randR;*/
//		double rand = Math.random();
//		int ran = (int) ((rand * 6));
//
//		Log.e("랜덤", "" + ran);
//
//		int motorL = 0;
//		int motorR = 0;
//		int MOTOR_SPEED = 127;
//		switch (ran) {
//		case 0:
//			motorL = MOTOR_SPEED;
//			motorR = MOTOR_SPEED;
//			break;
//		case 1:
//			motorL = 50;
//			motorR = MOTOR_SPEED;
//			break;
//		case 2:
//			motorL = -MOTOR_SPEED;
//			motorR = MOTOR_SPEED;
//			break;
//		case 3:
//			motorL = MOTOR_SPEED;
//			motorR = 50;
//			break;
//		case 4:
//			motorL = MOTOR_SPEED;
//			motorR = -MOTOR_SPEED;
//			break;
//		case 5:
//			motorL = -MOTOR_SPEED;
//			motorR = -MOTOR_SPEED;
//			break;
//		}
//		mMotorPlaySpeedL = motorL;
//        mMotorPlaySpeedR = motorR;
//    }
	
	public void goRandomPlay() {	
		// Timer setting
        TimerTask timer3s = new TimerTask() {
            public void run() {
                Main.setRandPlaySpeed();
                if(bPlayRobot == false){
                	this.cancel();
                }
            }
        };
        Timer timer = new Timer();
        timer.schedule(timer3s, 500, 3000);	
		
//		if (LOG_WRITE) {
//			try {
//				File file = new File("/sdcard/debug.txt");
//				fos = new FileOutputStream(file);
//			} catch (Exception e) {
//				Log.i(TAG, e.getMessage());
//			}
//			try {
//				File file = new File("/sdcard/debug2.txt");
//				fos2 = new FileOutputStream(file);
//			} catch (Exception e) {
//				Log.i(TAG, e.getMessage());
//			}
//		}
	}
	
	public void initAutoView() {
		Ran_play = (ImageButton) findViewById(R.id.ran_play);
		Ran_play.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				//Timer mTimer = new Timer();
				if (bPlayRobot) {
					bPlayRobot = false;
					Ran_play.setBackgroundResource(R.drawable.ran_play);

					moveMotor((byte) 0, (byte) 0);
					//mTimer.cancel();
					//mTimertask.cancel();
				} else {
					bPlayRobot = true;
					bPlaySensorBase = true;
					Ran_play.setBackgroundResource(R.drawable.ran_stop);
					goRandomPlay();
					/*mTimertask = new TimerTask() {
						public void run() {
							int ran = rand();

							int motorL = 0;
							int motorR = 0;
							int MOTOR_SPEED = 115;

							switch (ran) {
							case 0:
								motorL = MOTOR_SPEED;
								motorR = MOTOR_SPEED;
								break;
							case 1:
								motorL = 50;
								motorR = MOTOR_SPEED;
								break;
							case 2:
								motorL = -MOTOR_SPEED;
								motorR = MOTOR_SPEED;
								break;
							case 3:
								motorL = MOTOR_SPEED;
								motorR = 50;
								break;
							case 4:
								motorL = MOTOR_SPEED;
								motorR = -MOTOR_SPEED;
								break;
							case 5:
								motorL = -MOTOR_SPEED;
								motorR = -MOTOR_SPEED;
								break;
							}

							moveMotor((byte) motorL, (byte) motorR);

						}

					};
					mTimer.schedule(mTimertask, 0, 2000); // 0초후에 Task를 실행하고
															// 2초마다 반복 해라*/
				}
			}
		});

		Ran_play_2 = (ImageButton) findViewById(R.id.ran_play_2);
		Ran_play_2.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				//Timer mTimer = new Timer();
				if (bPlayRobot) {
					bPlayRobot = false;
					Ran_play_2.setBackgroundResource(R.drawable.ran_play);

					moveMotor((byte) 0, (byte) 0);
					//mTimer.cancel();
					//mTimertask.cancel();
				} else {
					bPlayRobot = true;
					bPlaySensorBase = true;
					Ran_play_2.setBackgroundResource(R.drawable.ran_stop);
					goRandomPlay();
					/*mTimertask = new TimerTask() {
						public void run() {

							int ran = rand();

							int motorL = 0;
							int motorR = 0;
							int MOTOR_SPEED = 115;

							switch (ran) {
							case 0:
								motorL = MOTOR_SPEED;
								motorR = MOTOR_SPEED;
								break;
							case 1:
								motorL = 50;
								motorR = MOTOR_SPEED;
								break;
							case 2:
								motorL = -MOTOR_SPEED;
								motorR = MOTOR_SPEED;
								break;
							case 3:
								motorL = MOTOR_SPEED;
								motorR = 50;
								break;
							case 4:
								motorL = MOTOR_SPEED;
								motorR = -MOTOR_SPEED;
								break;
							case 5:
								motorL = -MOTOR_SPEED;
								motorR = -MOTOR_SPEED;
								break;
							}

							moveMotor((byte) motorL, (byte) motorR);

						}

					};
					mTimer.schedule(mTimertask, 0, 2000); // 0초후에 Task를 실행하고
															// 2초마다 반복 해라*/
				}
			}
		});
	}

	public void initVoiceView() {

		findViewById(R.id.voice_btn).setOnClickListener(new OnClickListener() {
			public void onClick(View v) {

				VoiceSpeech();

			}
		});
		findViewById(R.id.voice_btn_2).setOnClickListener(
				new OnClickListener() {
					public void onClick(View v) {

						VoiceSpeech();

					}
				});
	}

	public static int rand() {
		double rand = Math.random();
		int num = (int) ((rand * 6));

		Log.e("랜덤", "" + num);

		return num;
	}

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mTab = getTabHost();

		LayoutInflater inflater = LayoutInflater.from(this);
		inflater.inflate(R.layout.bougerplay, mTab.getTabContentView(), true);

		mTab.addTab(mTab.newTabSpec("tab1").setIndicator("")
				.setContent(R.id.kid));
		mTab.addTab(mTab.newTabSpec("tab2").setIndicator("")
				.setContent(R.id.cat));

		mTab.setOnTabChangedListener(this);
		mTab.setCurrentTab(0);
		onTabChanged("tab1");

		BougerPlay_kid_btn = (ImageButton) findViewById(R.id.bougerplay_kid_btn);
		BougerPlay_kid_btn.setOnClickListener(this);
		BougerPlay_kid_ran = (ImageButton) findViewById(R.id.bougerplay_kid_ran);
		BougerPlay_kid_ran.setOnClickListener(this);
		BougerPlay_kid_voice = (ImageButton) findViewById(R.id.bougerplay_kid_voice);
		BougerPlay_kid_voice.setOnClickListener(this);
		BougerPlay_cat_btn = (ImageButton) findViewById(R.id.bougerplay_cat_btn);
		BougerPlay_cat_btn.setOnClickListener(this);
		BougerPlay_cat_ran = (ImageButton) findViewById(R.id.bougerplay_cat_ran);
		BougerPlay_cat_ran.setOnClickListener(this);
		BougerPlay_cat_voice = (ImageButton) findViewById(R.id.bougerplay_cat_voice);
		BougerPlay_cat_voice.setOnClickListener(this);

		Kid_btn = (LinearLayout) findViewById(R.id.kid_btn);
		Kid_ran = (LinearLayout) findViewById(R.id.kid_ran);
		Kid_voice = (LinearLayout) findViewById(R.id.kid_voice);
		Cat_btn = (LinearLayout) findViewById(R.id.cat_btn);
		Cat_ran = (LinearLayout) findViewById(R.id.cat_ran);
		Cat_voice = (LinearLayout) findViewById(R.id.cat_voice);
		Introduce_1 = (LinearLayout) findViewById(R.id.introduce_1);
		Introduce_2 = (LinearLayout) findViewById(R.id.introduce_2);

//		mMsgQueue = new ByteQueue();
//		mMsgQueue.ensureCapacity(4096);

		if (D)
			Log.e(TAG, "+++ ON CREATE +++");

		// If the adapter is null, then Bluetooth is not supported
		if (mBluetoothAdapter == null) {
			Toast.makeText(this, "Bluetooth is not available",
					Toast.LENGTH_LONG).show();
			finish();
			return;
		}
	}

	public void onStart() {

		super.onStart();
		if (D)
			Log.e(TAG, "++ ON START ++");

		// If BT is not on, request that it be enabled. // setupChat() will
		// then be called during onActivityResult
		if (!mBluetoothAdapter.isEnabled()) {
			Intent enableIntent = new Intent(
					BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableIntent, REQUEST_ENABLE_BT); // Otherwise,
																		// setup
																		// the
																		// chat
																		// session
		} else {
			if (mChatService == null)
				setupChat();
		}
	}

	@Override
	public synchronized void onResume() {
		super.onResume();
		if (D)
			Log.e(TAG, "+ ON RESUME +");

		if (mChatService != null) {
			// Only if the state is STATE_NONE, do we know that we haven't
			// started already
			if (mChatService.getState() == BluetoothChatService.STATE_NONE) {
				// Start the Bluetooth chat services
				mChatService.start();
			}
		}
	}

	private void setupChat() {
		Log.d(TAG, "setupChat()");

		// Initialize the BluetoothChatService to perform bluetooth connections
		// Timer setting
		TimerTask timer100ms = new TimerTask() {
			public void run() {
				if (btConnectStatus == BT_CONNECTED) {
					byte[] buff = { 0x02, 'S', (byte) 0xAD, 0x03 };
					sendMessage(buff, 4);
				}
			}
		};
		Timer timer = new Timer();
		timer.schedule(timer100ms, 500, 100);
	}

	@Override
	public synchronized void onPause() {
		super.onPause();
		if (D)
			Log.e(TAG, "- ON PAUSE -");
	}

	private void sendMessage(byte[] buff, int len) {
		// Check that we're actually connected before trying anything
		Log.e(TAG, "mChatService : " + len);
		// Check that there's actually something to send
		if (len > 0) {
			// Get the message bytes and tell the BluetoothChatService to write
			mChatService.write(buff);
		}
	}

	private Boolean isValidProtocol(byte[] buff, int len) {
		byte checksum = 0;
		for (int i = 1; i < len - 1; i++) {
			checksum += buff[i];
		}

		if ((checksum & 0x00ff) == 0)
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

	public void moveMotor(byte left, byte right) {
		byte[] buff = new byte[6];
		buff[0] = 0x02;
		buff[1] = 'M';
		buff[2] = left;
		buff[3] = right;
		buff[4] = checksum(buff, 6);
		buff[5] = 0x03;
		sendMessage(buff, 6);
	}

	// The Handler that gets information back from the BluetoothChatService
	private final Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MESSAGE_STATE_CHANGE:
				switch (msg.arg1) {
				case BluetoothChatService.STATE_CONNECTED:
					btConnectStatus = BT_CONNECTED;
					break;
				case BluetoothChatService.STATE_CONNECTING:
					btConnectStatus = BT_CONNECTING;
					break;
				case BluetoothChatService.STATE_LISTEN:
				case BluetoothChatService.STATE_NONE:
					btConnectStatus = BT_DISCONNECTED;
					break;
				}
				break;
			/*case MESSAGE_READ:
				int len = (int) msg.arg1;
				byte[] msgBuf = (byte[]) msg.obj;
				for (int i = 0; i < len; i++) {
					mMsgQueue.insert(msgBuf[i]);
				}
				if (LOG_WRITE) {
					if (bPlayRobot) {
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
						if (bPlayRobot) {
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

						int sen1_l = (readBuf[7] & 0x00ff);
						int sen1_h = (readBuf[8] << 8) & 0xff00;
						int sen2_l = (readBuf[9] & 0x00ff);
						int sen2_h = (readBuf[10] << 8) & 0xff00;
						int sen3_l = (readBuf[11] & 0x00ff);
						int sen3_h = (readBuf[12] << 8) & 0xff00;
						int sen4_l = (readBuf[13] & 0x00ff);
						int sen4_h = (readBuf[14] << 8) & 0xff00;

						int[] sen = { 0, 0, 0, 0 };
						sen[0] = sen1_h + sen1_l;
						sen[1] = sen2_h + sen2_l;
						sen[2] = sen3_h + sen3_l;
						sen[3] = sen4_h + sen4_l;

						if (bReqSensorBase) {
							bReqSensorBase = false;
							for (int i = 0; i < 4; i++) {
								mSenBaseValue[i] = sen[i];
							}
						}

						if (bPlayRobot) {
							Boolean[] bClief = { false, false, false, false };
							for (int i = 0; i < 4; i++) {
								if (sen[i] < mSenBaseValue[i] - 200) {
									bClief[i] = true;
								}
							}
							Boolean[] bWall = { false, false, false, false };
							for (int i = 0; i < 4; i++) {
								if (sen[i] > mSenBaseValue[i] + 500) {
									bWall[i] = true;
								}
							}

							int MOTOR_SPEED = 100; // 모터의 최대 속도를 100으로 설정한다.
							int motorL = 0;
							int motorR = 0;
							if (bClief[0] && bClief[2] && bClief[3]) { // Bouger가
																		// 들려있으면
																		// 정지
								motorL = 0;
								motorR = 0;
							} else if (bClief[3]) { // 후면이 낭떠러지이면
								motorL = MOTOR_SPEED;
								motorR = MOTOR_SPEED;
							} else if (bClief[0] && bClief[2]) { // 전면이 낭떠러지이면
								motorL = -MOTOR_SPEED;
								motorR = -MOTOR_SPEED;
							} else if (bClief[0]) { // 우측 전면이 낭떠러지이면
								motorL = -MOTOR_SPEED;
								motorR = MOTOR_SPEED / 2;
							} else if (bClief[2]) { // 좌측 전면이 낭떠러지이면
								motorL = MOTOR_SPEED / 2;
								motorR = -MOTOR_SPEED;
							} else if (bWall[1]) { // 전면이 벽이면
								motorL = -MOTOR_SPEED;
								motorR = -MOTOR_SPEED;
							} else {
								motorL = mMotorPlaySpeedL;
								motorR = mMotorPlaySpeedR;
							}

							moveMotor((byte) motorL, (byte) motorR);
						}
					}
				}
				break;*/
			case MESSAGE_TOAST:
				Toast.makeText(getApplicationContext(),
						msg.getData().getString(TOAST), Toast.LENGTH_SHORT)
						.show();
				break;
			}
		}
	};

	public void onActivityResult(int requestCode, int resultCode, Intent data) {

		int motorL = 0;
		int motorR = 0;

		int MOTOR_SPEED = 127; // 모터의 최대 속도를 127로 설정한다.

		if (requestCode == RECOGNIZER && resultCode == Activity.RESULT_OK) {
			// returned data is a list of matches to the speech input
			ArrayList<String> result = data
					.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS); // 음성인식결과
																				// 저장할
																				// ArrayList
																				// result
			for (int x = result.size() - 1; x >= 0; x--) {
				System.out.println("word: " + result.get(x));
			}

			Log.d("SPEECH", "size:" + result.size());
			String condition;

			boolean play[] = new boolean[7];
			for (int x = 0; x < play.length; x++) {
				play[x] = false;
			}

			for (int i = result.size() - 1; i >= 0; i--) {

				Log.d("SPEECH", "str:" + i + ":" + result.get(i));

				condition = result.get(i);
				System.out.println(condition);
				String words[] = condition.split(" ");
				synchronized (this) {
					for (int j = 0; j < words.length; j++) {

						String word = words[j];

						if (word.equals("left")
								|| word.equals(getString(R.string.left_korean))) {
							if (!play[0]) {			
								motorR = MOTOR_SPEED;
								motorL = -50;
								moveMotor((byte)motorL, (byte)motorR);
								try {
									Thread.sleep(3000);
								} catch (InterruptedException e) {
									e.printStackTrace();
								}
								motorL = 0;
								motorR = 0;
								moveMotor((byte)motorL, (byte)motorR);
								play[0] = true;
							}
							break;
						}
						if (word.equals("right")
								|| word.equals(getString(R.string.right_korean))) {
							if (!play[1]) {
								motorR = -50;
								motorL = MOTOR_SPEED;
								moveMotor((byte)motorL, (byte)motorR);
								try {
									Thread.sleep(3000);
								} catch (InterruptedException e) {
									e.printStackTrace();
								}
								motorR = 0;
								motorL = 0;
								moveMotor((byte)motorL, (byte)motorR);
								play[1] = true;

							}
							break;
						}
						if (word.equals("run")
								|| word.equals(getString(R.string.run_korean))
								|| word.equals(getString(R.string.run_korean_mom))) {
							if (!play[4]) {
								motorR = MOTOR_SPEED;
								motorL = MOTOR_SPEED;
								moveMotor((byte)motorL, (byte)motorR);
								try {
									Thread.sleep(3000);
								} catch (InterruptedException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
								motorR = 0;
								motorL = 0;
								moveMotor((byte)motorL, (byte)motorR);
								play[4] = true;
							}
							break;
						}
						if (word.equals("back")
								|| word.equals(getString(R.string.back_korean))
								|| word.equals(getString(R.string.back_korean_2))
								|| word.equals(getString(R.string.back_korean_dad))) {
							if (!play[5]) {
								motorR = -MOTOR_SPEED;
								motorL = -MOTOR_SPEED;
								moveMotor((byte)motorL, (byte)motorR);
								try {
									Thread.sleep(3000);
								} catch (InterruptedException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
								motorR = 0;
								motorL = 0;
								moveMotor((byte)motorL, (byte)motorR);
								play[5] = true;
							}
							break;
						}
						if (word.equals("trololo")) {
							motorR = MOTOR_SPEED;
							motorL = -MOTOR_SPEED;
							moveMotor((byte)motorL, (byte)motorR);
						}
					}
				}
			}
			if (speech_on)
				VoiceSpeech();
			moveMotor((byte)motorL, (byte)motorR);
		}

	}

	public void VoiceSpeech() {

		try {
			Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
			intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
					RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH);
			intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 100);

			startActivityForResult(intent, RECOGNIZER);

		} catch (Exception e) {
			Toast.makeText(getApplicationContext(), "No Speech support",
					Toast.LENGTH_LONG).show();
		}

	}

	public void onClick(View v) {

		switch (v.getId()) {
		case R.id.bougerplay_kid_btn:
			initManualView();
			Kid_btn.setVisibility(LinearLayout.VISIBLE);
			Kid_ran.setVisibility(LinearLayout.INVISIBLE);
			Kid_voice.setVisibility(LinearLayout.INVISIBLE);
			Introduce_1.setVisibility(LinearLayout.INVISIBLE);
			v.setBackgroundResource(R.drawable.on_btn);
			BougerPlay_kid_ran.setBackgroundResource(R.drawable.off_ran);
			BougerPlay_kid_voice.setBackgroundResource(R.drawable.off_voice);
			break;
		case R.id.bougerplay_kid_ran:
			initAutoView();
			Kid_btn.setVisibility(LinearLayout.INVISIBLE);
			Kid_ran.setVisibility(LinearLayout.VISIBLE);
			Kid_voice.setVisibility(LinearLayout.INVISIBLE);
			Introduce_1.setVisibility(LinearLayout.INVISIBLE);
			v.setBackgroundResource(R.drawable.on_ran);
			BougerPlay_kid_btn.setBackgroundResource(R.drawable.off_btn);
			BougerPlay_kid_voice.setBackgroundResource(R.drawable.off_voice);
			break;
		case R.id.bougerplay_kid_voice:
			initVoiceView();
			Kid_btn.setVisibility(LinearLayout.INVISIBLE);
			Kid_ran.setVisibility(LinearLayout.INVISIBLE);
			Kid_voice.setVisibility(LinearLayout.VISIBLE);
			Introduce_1.setVisibility(LinearLayout.INVISIBLE);
			v.setBackgroundResource(R.drawable.on_voice);
			BougerPlay_kid_btn.setBackgroundResource(R.drawable.off_btn);
			BougerPlay_kid_ran.setBackgroundResource(R.drawable.off_ran);
			break;
		case R.id.bougerplay_cat_btn:
			initManualView();
			Cat_btn.setVisibility(LinearLayout.VISIBLE);
			Cat_ran.setVisibility(LinearLayout.INVISIBLE);
			Cat_voice.setVisibility(LinearLayout.INVISIBLE);
			Introduce_2.setVisibility(LinearLayout.INVISIBLE);
			v.setBackgroundResource(R.drawable.on_btn);
			BougerPlay_cat_ran.setBackgroundResource(R.drawable.off_ran);
			BougerPlay_cat_voice.setBackgroundResource(R.drawable.off_voice);
			break;
		case R.id.bougerplay_cat_ran:
			initAutoView();
			Cat_btn.setVisibility(LinearLayout.INVISIBLE);
			Cat_ran.setVisibility(LinearLayout.VISIBLE);
			Cat_voice.setVisibility(LinearLayout.INVISIBLE);
			Introduce_2.setVisibility(LinearLayout.INVISIBLE);
			v.setBackgroundResource(R.drawable.on_ran);
			BougerPlay_cat_btn.setBackgroundResource(R.drawable.off_btn);
			BougerPlay_cat_voice.setBackgroundResource(R.drawable.off_voice);
			break;
		case R.id.bougerplay_cat_voice:
			initVoiceView();
			Cat_btn.setVisibility(LinearLayout.INVISIBLE);
			Cat_ran.setVisibility(LinearLayout.INVISIBLE);
			Cat_voice.setVisibility(LinearLayout.VISIBLE);
			Introduce_2.setVisibility(LinearLayout.INVISIBLE);
			v.setBackgroundResource(R.drawable.on_voice);
			BougerPlay_cat_btn.setBackgroundResource(R.drawable.off_btn);
			BougerPlay_cat_ran.setBackgroundResource(R.drawable.off_ran);
			break;

		}
	}

	public void onTabChanged(String tabId) {
		if (tabId.equals("tab1")) {
			mTab.getTabWidget().getChildAt(0)
					.setBackgroundResource(R.drawable.kid_on);
			mTab.getTabWidget().getChildAt(1)
					.setBackgroundResource(R.drawable.cat_off);
		} else if (tabId.equals("tab2")) {
			mTab.getTabWidget().getChildAt(0)
					.setBackgroundResource(R.drawable.kid_off);
			mTab.getTabWidget().getChildAt(1)
					.setBackgroundResource(R.drawable.cat_on);
		}
	}

}
