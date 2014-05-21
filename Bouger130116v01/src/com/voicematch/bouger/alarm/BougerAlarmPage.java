package com.voicematch.bouger.alarm;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;

import com.voicematch.bouger.R;
import com.voicematch.bouger.main.Main;
import com.voicematch.bouger.play.BluetoothChatService;
import com.voicematch.bouger.play.BougerPlay;
import com.voicematch.bouger.play.ByteQueue;

public class BougerAlarmPage extends Activity {
	public static final int MESSAGE_READ = 2;
	public static final int MESSAGE_SPEECH_REC = 6;
    public static final int MESSAGE_SPEECH_REC_ERROR = 7;
    public static final int MESSAGE_SPEECH_REC_START = 8;
    public static final int MESSAGE_SPEECH_REC_BEGIN = 9;
    public static final int MESSAGE_SPEECH_REC_END = 10;
	Vibrator vibe;
	Timer mTimer = new Timer();
//	TimerTask mTimertask;
	AudioManager am;
	Cursor cursor;
	SQLiteDatabase db;

	private static final boolean LOG_WRITE = false;
//	private ByteQueue mMsgQueue;
	public static boolean bAlarmRobot = false;
	public static boolean bReqSensorBase = false;
//	private int mSenBaseValue[] = {0, 0, 0, 0};

	// File Writer
	FileOutputStream fos = null;
	FileOutputStream fos2 = null;

//	private int mMotorPlaySpeedL = 50;
//	private int mMotorPlaySpeedR = 50;

	int ringermode = 0;
	Calendar calendar = new GregorianCalendar();
	private static final String TAG = "BluetoothChat";
	// Member object for the chat services
	BluetoothChatService mChatService = Main.mChatService;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.bougeralarmpage);

		BougerAlarmDBHelper helper = new BougerAlarmDBHelper(this);
		db = helper.getReadableDatabase();
		
//		mMsgQueue = new ByteQueue();
//        mMsgQueue.ensureCapacity(4096);

		int hour = calendar.get(Calendar.HOUR_OF_DAY);
		int minute = calendar.get(Calendar.MINUTE);

		cursor = db.rawQuery("SELECT * FROM bougeralarmTable WHERE hour = '"
				+ hour + "' AND minute = '" + minute + "'", null);
		cursor.moveToFirst();

		am = (AudioManager) getSystemService(AUDIO_SERVICE);

		findViewById(R.id.alarmbg).setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (cursor.getInt(3) == 1) {
					if (ringermode == 1) {
						am.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
					} else if (ringermode == 2) {
						am.setRingerMode(AudioManager.RINGER_MODE_SILENT);
					}
					BougerAlarmAdd.alarmRingtone.stop();
					alarmRandomStop();
				} else if (cursor.getInt(3) == 2) {
					vibe.cancel();
					alarmRandomStop();
				} else if (cursor.getInt(3) == 3) {
					BougerAlarmAdd.alarmRingtone.stop();
					vibe.cancel();
					alarmRandomStop();
				} else {
					alarmRandomStop();
				}

				// notification이 설정되어 있을 때 알람을 끔과 동시에 notification 해제
				if (BougerAlarmAdd.no == 1) {
					BougerAlarmReceiver.nm.cancel(1234);
				}
				
				db.close();
				finish();
			}
		});

		// if (hour == cursor.getInt(1) && minute == cursor.getInt(2)) {
		if (cursor.getInt(3) == 1) {
			if (am.getRingerMode() == AudioManager.RINGER_MODE_VIBRATE) {
				am.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
				ringermode = 1;
			} else if (am.getRingerMode() == AudioManager.RINGER_MODE_SILENT) {
				am.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
				ringermode = 2;
			}
			BougerAlarmAdd.alarmRingtone.play();
			alarmRandomPlay();
		} else if (cursor.getInt(3) == 2) {
			long[] pattern = new long[] { 800, 2000 };

			vibe = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
			vibe.vibrate(pattern, 0);
			alarmRandomPlay();
		} else if (cursor.getInt(3) == 3) {
			BougerAlarmAdd.alarmRingtone.play();
			long[] pattern = new long[] { 800, 2000 };

			vibe = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
			vibe.vibrate(pattern, 0);
			alarmRandomPlay();
		} else {
			alarmRandomPlay();
		}
		// }

		// 화면이 꺼져 있든 lock 되어 있든 알람이 울릴 때 화면이 가장 상위에 보이도록 함.
		getWindow().addFlags(
				WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
						| WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
						| WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
						| WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

		/*
		 * Context c = this; MediaPlayer mp = MediaPlayer.create(c, R.raw.h8);
		 * // mp.setVolume(volume, volume); mp.start();
		 */
	}

	private void sendMessage(byte[] buff, int len) {
		// Check that we're actually connected before trying anything
		Log.e(TAG, "mChatService : " + len);

		if (mChatService.getState() != BluetoothChatService.STATE_CONNECTED)
			Log.e(TAG, "not connected!!");

		// Check that there's actually something to send
		if (len > 0) {
			// Get the message bytes and tell the BluetoothChatService to write
			// Log.e(TAG, "buff : " + buff.toString());
			// Log.e(TAG, "len : " + len);
			mChatService.write(buff);
			Log.e(TAG, "dd");
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
	
//  private void setRandPlaySpeed() {
//  Random rand = new Random();
//  int randL = rand.nextInt();
//  randL = randL % motorVariation; //15
//  mMotorPlaySpeedL =  motorBase + randL; //65
//  int randR = rand.nextInt();
//  randR = randR % motorVariation; //15
//  mMotorPlaySpeedR =  motorBase + randR; //65
//}

//private void setRandPlaySpeed() {
//Random rand = new Random();
//int randL = rand.nextInt(); //int형 표현 범위(-2147483648 ~ 2147483647)
//randL = randL % 15; //나머지연산자, old 20
//Main.mMotorPlaySpeedL =  65 + randL; //edit , old 50 
//int randR = rand.nextInt(); //int형 표현 범위(-2147483648 ~ 2147483647)
//randR = randR % 15; //나머지연산자, old 20
//Main.mMotorPlaySpeedR =  65 + randR; //edit , old 50 
//
////if(mMotorPlaySpeedL < 75) mMotorPlaySpeedL = 75;
////if(mMotorPlaySpeedR < 75) mMotorPlaySpeedR = 75;
//}

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
	
	public void alarmRandomPlay() {
		bAlarmRobot = true;
		bReqSensorBase = true;
		
		// Timer setting
        TimerTask timer3s = new TimerTask() {
            public void run() {
                Main.setRandPlaySpeed();
                if(bAlarmRobot == false){
                	this.cancel();
                }
            }
        };
        Timer timer = new Timer();
        timer.schedule(timer3s, 500, 3000);	
		
		if (LOG_WRITE) {
			try {
				File file = new File("/sdcard/debug.txt");
				fos = new FileOutputStream(file);
			} catch (Exception e) {
				Log.i(TAG, e.getMessage());
			}
			try {
				File file = new File("/sdcard/debug2.txt");
				fos2 = new FileOutputStream(file);
			} catch (Exception e) {
				Log.i(TAG, e.getMessage());
			}
		}
	}

	public void alarmRandomStop() {
		bAlarmRobot = false;
		//Main.timer.cancel();
		//Main.timer3s.cancel();	
		// 기능을 비활성화 할 때는 모터를 정지한다.
		moveMotor((byte) 0, (byte) 0);
		
		if (LOG_WRITE) {
			// Close debug.txt
			try {
				fos.close();
			} catch (Exception e) {
				Log.i(TAG, e.getMessage());
			}
			try {
				fos2.close();
			} catch (Exception e) {
				Log.i(TAG, e.getMessage());
			}
		}
		
			
		//mTimer.cancel();
		//mTimertask.cancel();
	}	

	public void onBackPressed() {
		super.onBackPressed();
		if (cursor.getInt(3) == 1) {
			BougerAlarmAdd.alarmRingtone.stop();
			alarmRandomStop();
		} else if (cursor.getInt(3) == 2) {
			vibe.cancel();
			alarmRandomStop();
		} else if (cursor.getInt(3) == 3) {
			BougerAlarmAdd.alarmRingtone.stop();
			vibe.cancel();
			alarmRandomStop();
		} else {
			alarmRandomStop();
		}

		// notification이 설정되어 있을 때 알람을 끔과 동시에 notification 해제
		if (BougerAlarmAdd.no == 1) {
			BougerAlarmReceiver.nm.cancel(1234);
		}
	}
}