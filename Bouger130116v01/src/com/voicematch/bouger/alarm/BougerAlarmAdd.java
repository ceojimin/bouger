package com.voicematch.bouger.alarm;

import java.util.Calendar;
import java.util.GregorianCalendar;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore.Audio;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TimePicker;
import android.widget.TimePicker.OnTimeChangedListener;
import android.widget.Toast;

import com.voicematch.bouger.R;

public class BougerAlarmAdd extends Activity implements OnClickListener,
		OnTimeChangedListener {
	AlarmManager am;
	GregorianCalendar calendar;
	TimePicker time;
	static long nowTime;
	Cursor cursor;
	SQLiteDatabase db;
	AudioManager audioManager;
	ImageButton bougeralarmrepeat, bougeralarmtype, store, cancel;
	ArrayAdapter<CharSequence> arraylist;
	boolean initspinner;
	static final String[] repeatarray = new String[] { "한 번", "매일", "매주" };
	static final String[] typearray = new String[] { "벨소리", "진동", "진동 및 벨소리",
			"무음" };
	int repeatselection, typeselection = 0;
	int repeat = 0;
	static int type = 1;
	static int no = 0;
	static Ringtone alarmRingtone;
	RingtoneManager ringtoneManager = new RingtoneManager(this);
	LinearLayout l;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.bougeralarmadd);

		BougerAlarmDBHelper helper = new BougerAlarmDBHelper(this);
		db = helper.getReadableDatabase();

		Uri defaulturi = RingtoneManager.getActualDefaultRingtoneUri(getApplicationContext() ,RingtoneManager.TYPE_ALARM);
		//Uri defaulturi = Uri.withAppendedPath(Audio.Media.EXTERNAL_CONTENT_URI, "1");
		alarmRingtone = ringtoneManager.getRingtone(this, defaulturi);

		am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
		calendar = new GregorianCalendar();
		Log.i("alarmtime", calendar.getTime().toString());

		time = (TimePicker) findViewById(R.id.time_picker);
		time.setCurrentHour(calendar.get(Calendar.HOUR_OF_DAY));
		time.setCurrentMinute(calendar.get(Calendar.MINUTE));
		time.setOnTimeChangedListener(this);
		nowTime = calendar.getTimeInMillis();

		bougeralarmrepeat = (ImageButton) findViewById(R.id.repeat);
		bougeralarmtype = (ImageButton) findViewById(R.id.type);
		store = (ImageButton) findViewById(R.id.store);
		cancel = (ImageButton) findViewById(R.id.cancel);
		bougeralarmrepeat.setOnClickListener(this);
		bougeralarmtype.setOnClickListener(this);
		store.setOnClickListener(this);
		cancel.setOnClickListener(this);		
	}

	public void setAlarm() {
		int count = 0;
		int hour, minute = 0;
		long alarmTime;		

		cursor = db.rawQuery("SELECT * FROM bougeralarmTable", null);

		int n = cursor.getCount();
		int h = calendar.get(Calendar.HOUR_OF_DAY);
		int m = calendar.get(Calendar.MINUTE);
		Log.i("TIME", "" + h + " : " + m);

		db.execSQL("insert into bougeralarmTable values('" + n + 1 + "', '" + h
				+ "', '" + m + "', '" + type + "' );");

		Log.i("TYPE", "" + type);

		cursor = db.rawQuery("SELECT * FROM bougeralarmTable", null);

		while (cursor.moveToNext()) {
			PendingIntent[] sender = new PendingIntent[cursor.getCount()];
			Intent pi = new Intent(getApplicationContext(),
					BougerAlarmPage.class);

			sender[count] = PendingIntent.getActivity(getApplicationContext(),
					count, pi, 0);
			hour = cursor.getInt(1);
			minute = cursor.getInt(2);

			if (calendar.get(Calendar.HOUR_OF_DAY) < hour) {
				calendar.setTimeInMillis(System.currentTimeMillis());
				calendar.set(Calendar.HOUR_OF_DAY, hour);
				calendar.set(Calendar.MINUTE, minute);
				calendar.set(Calendar.SECOND, 0);
				calendar.set(Calendar.MILLISECOND, 0);
				alarmTime = calendar.getTimeInMillis();
				if (repeat == 0) {
					am.set(AlarmManager.RTC_WAKEUP, alarmTime, sender[count]);
					Toast.makeText(this, "알람이 설정되었습니다.", Toast.LENGTH_LONG).show();
				} else if (repeat == 1) {
					am.setRepeating(AlarmManager.RTC_WAKEUP, alarmTime, 24*60*60*1000, sender[count]);
					Toast.makeText(this, "알람이 매일 울립니다.", Toast.LENGTH_LONG).show();
				} else{
					am.setRepeating(AlarmManager.RTC_WAKEUP, alarmTime, 7*24*60*60*1000, sender[count]);
					Toast.makeText(this, "알람이 매주 울립니다.", Toast.LENGTH_LONG).show();
				}
			} else if (calendar.get(Calendar.HOUR_OF_DAY) == hour) {
				if (calendar.get(Calendar.MINUTE) <= minute) {
					calendar.setTimeInMillis(System.currentTimeMillis());
					calendar.set(Calendar.HOUR_OF_DAY, hour);
					calendar.set(Calendar.MINUTE, minute);
					calendar.set(Calendar.SECOND, 0);
					calendar.set(Calendar.MILLISECOND, 0);
					alarmTime = calendar.getTimeInMillis();
					Log.i("MANY", "" + alarmTime);
					if (repeat == 0) {
						am.set(AlarmManager.RTC_WAKEUP, alarmTime, sender[count]);
						Toast.makeText(this, "알람이 설정되었습니다.", Toast.LENGTH_LONG).show();
					} else if (repeat == 1) {
						am.setRepeating(AlarmManager.RTC_WAKEUP, alarmTime, 24*60*60*1000, sender[count]);
						Toast.makeText(this, "알람이 매일 울립니다.", Toast.LENGTH_LONG).show();
					} else{
						am.setRepeating(AlarmManager.RTC_WAKEUP, alarmTime, 7*24*60*60*1000, sender[count]);
						Toast.makeText(this, "알람이 매주 울립니다.", Toast.LENGTH_LONG).show();
					}
				}
			}
			count++;
		}

		no = 1;
		am.set(AlarmManager.RTC_WAKEUP, 0, pendingIntent());
		db.close();
	}

	private PendingIntent pendingIntent() {
		Intent i = new Intent(getApplicationContext(),
				BougerAlarmReceiver.class);
		PendingIntent pi = PendingIntent.getBroadcast(BougerAlarmAdd.this, 0,
				i, 0);
		return pi;
	}

	/*
	 * private PendingIntent pendingIntent2() { Intent i = new
	 * Intent(getApplicationContext(), BougerAlarmPage.class); PendingIntent pi2
	 * = PendingIntent.getActivity(BougerAlarmAdd.this, 0, i, 0); return pi2; }
	 */

	public void onClick(View v) {
		// TODO Auto-generated method stub
		if (v.getId() == R.id.repeat) {			
			new AlertDialog.Builder(this)
					.setTitle("반복설정")
					.setSingleChoiceItems(repeatarray, repeatselection,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int which) {
									// TODO Auto-generated method stub
									repeatselection = which;
								}
							})
					.setPositiveButton("확인",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int which) {
									// TODO Auto-generated method stub
									AlertDialog alert = (AlertDialog) dialog;
									ListView lv = (ListView) alert
											.getListView();
									int check = lv.getCheckedItemPosition();

									if (check == 0) {
										repeat = 0;
									} else if (check == 1) {
										repeat = 1;
									} else {
										repeat = 2;
									}
								}
							})
					.setNegativeButton("취소",
							new DialogInterface.OnClickListener() {

								public void onClick(DialogInterface dialog,
										int which) {
									// TODO Auto-generated method stub

								}
							}).show();
		}

		if (v.getId() == R.id.type) {			
			new AlertDialog.Builder(this)
					.setTitle("알람방식")
					.setSingleChoiceItems(typearray, typeselection,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int which) {
									// TODO Auto-generated method stub
									typeselection = which;
								}
							})
					.setPositiveButton("확인",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int which) {
									// TODO Auto-generated method stub
									AlertDialog alert = (AlertDialog) dialog;
									ListView lv = (ListView) alert
											.getListView();
									int check = lv.getCheckedItemPosition();

									if (check == 0) {
										type = 1;
									} else if (check == 1) {
										type = 2;
									} else if (check == 2) {
										type = 3;
									} else {
										type = 4;
									}
								}
							})
					.setNegativeButton("취소",
							new DialogInterface.OnClickListener() {

								public void onClick(DialogInterface dialog,
										int which) {
									// TODO Auto-generated method stub

								}
							}).show();
		}

		if (v.getId() == R.id.store) {
			if (nowTime > calendar.getTimeInMillis()) {
				Toast.makeText(this, "입력한 시간은 현재 시간보다 이전입니다.\n다시 설정해주세요!",
						Toast.LENGTH_LONG).show();
			} else {
				setAlarm();
				finish();
			}
			if (nowTime <= calendar.getTimeInMillis()) {
				Intent intent = new Intent(BougerAlarmAdd.this,
						BougerAlarm.class);
				startActivity(intent);
				overridePendingTransition(android.R.anim.fade_in,
						android.R.anim.fade_out);
				finish();
			}
		}

		if (v.getId() == R.id.cancel) {
			finish();
			Toast.makeText(BougerAlarmAdd.this, "취소되었습니다.", Toast.LENGTH_LONG)
					.show();
			Intent intent = new Intent(BougerAlarmAdd.this, BougerAlarm.class);
			startActivity(intent);
			overridePendingTransition(android.R.anim.fade_in,
					android.R.anim.fade_out);
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		if (resultCode != RESULT_OK) {
			return;
		}

		if (requestCode == 0) {
			// 선택된 알람음의 Uri 가져오기
			Uri pickedUri = intent
					.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_DEFAULT_URI);
			Log.e("test", "pickedUri is : " + pickedUri.toString());

			// Uri 를 가지고 재생시킬 멜로디에 접근 & RingtoneManager에 전달
			alarmRingtone = ringtoneManager.getRingtone(this, pickedUri);
		}
	}

	public void onBackPressed() {
		super.onBackPressed();
		overridePendingTransition(android.R.anim.fade_in,
				android.R.anim.fade_out);
		finish();
		Intent intent = new Intent(BougerAlarmAdd.this, BougerAlarm.class);
		startActivity(intent);
		overridePendingTransition(android.R.anim.fade_in,
				android.R.anim.fade_out);
	}

	public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
		// TODO Auto-generated method stub
		calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
				calendar.get(Calendar.DAY_OF_MONTH), hourOfDay, minute);
		Log.i("HelloAlarmActivity", calendar.getTime().toString());
	}
}