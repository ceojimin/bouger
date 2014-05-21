package com.voicematch.bouger.alarm;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.voicematch.bouger.R;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SimpleAdapter;

public class BougerAlarm extends ListActivity {
	Cursor cursor;
	Map<String, String> data;
	List<Map<String, String>> myList;
	SQLiteDatabase db;
	SimpleAdapter adapter;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.bougeralarm);

		BougerAlarmDBHelper helper = new BougerAlarmDBHelper(this);
		db = helper.getReadableDatabase();

		myList = new ArrayList<Map<String, String>>();

		cursor = db
				.rawQuery(
						"SELECT * FROM bougeralarmTable ORDER BY hour,minute ASC",
						null);

		while (cursor.moveToNext()) {
			Log.i("COUNT", "" + cursor.getCount());
			data = new HashMap<String, String>();
			data.put("alarmtime",
					cursor.getString(1) + " : " + cursor.getString(2));
			data.put("alarmonoff", "알람 설정");
			myList.add(data);
			Log.i("db", cursor.getString(1) + " : " + cursor.getString(2));
		}

		adapter = new SimpleAdapter(this, myList,
				R.layout.list, new String[] {
						"alarmtime", "alarmonoff" }, new int[] {
						R.id.time, R.id.set });

		setListAdapter(adapter);

		// db.close();

		findViewById(R.id.add).setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				finish();
				Intent intent = new Intent(BougerAlarm.this,
						BougerAlarmAdd.class);
				startActivity(intent);
				overridePendingTransition(android.R.anim.fade_in,
						android.R.anim.fade_out);
			}
		});
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);

		// 클릭 시 리스너 동작
		l.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Log.i("LIST", myList.get(position).toString());
				final int p = position;
				new AlertDialog.Builder(BougerAlarm.this)
						.setTitle("알람 삭제")
						.setIcon(R.drawable.bouger_small)
						.setMessage("알람을 삭제하시겠습니까?")
						.setPositiveButton("삭제",
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int which) {
										HashMap getMap = new HashMap();
										getMap = (HashMap) myList.get(p);

										// 항목의 element 확인
										Log.i("SIZE",
												""
														+ getMap.get(
																"alarmtime")
																.toString()
																.length());
										Log.i("[count] before DELETE", ""
												+ cursor.getCount());
										if (getMap.get("alarmtime").toString()
												.length() == 5) {
											Log.i("before DELETE",
													""
															+ getMap.get(
																	"alarmtime")
																	.toString()
																	.charAt(0));
											Log.i("before DELETE",
													""
															+ getMap.get(
																	"alarmtime")
																	.toString()
																	.charAt(4));
											db.execSQL("DELETE FROM bougeralarmTable WHERE hour = "
													+ getMap.get("alarmtime")
															.toString()
															.charAt(0)
													+ " AND minute = "
													+ getMap.get("alarmtime")
															.toString()
															.charAt(4));
										} else if (getMap.get("alarmtime")
												.toString().length() == 6) {
											Log.i("before DELETE",
													""
															+ getMap.get(
																	"alarmtime")
																	.toString()
																	.charAt(0)
															+ getMap.get(
																	"alarmtime")
																	.toString()
																	.charAt(1));
											Log.i("before DELETE",
													""
															+ getMap.get(
																	"alarmtime")
																	.toString()
																	.charAt(4)
															+ getMap.get(
																	"alarmtime")
																	.toString()
																	.charAt(5));
											db.execSQL("DELETE FROM bougeralarmTable WHERE hour = "
													+ getMap.get("alarmtime")
															.toString()
															.charAt(0)
													+ " AND minute = "
													+ getMap.get("alarmtime")
															.toString()
															.charAt(4)
													+ getMap.get("alarmtime")
															.toString()
															.charAt(5)
													+ " OR hour = "
													+ getMap.get("alarmtime")
															.toString()
															.charAt(0)
													+ getMap.get("alarmtime")
															.toString()
															.charAt(1)
													+ " AND minute = "
													+ getMap.get("alarmtime")
															.toString()
															.charAt(5));
										} else if (getMap.get("alarmtime")
												.toString().length() == 7) {
											Log.i("before DELETE",
													""
															+ getMap.get(
																	"alarmtime")
																	.toString()
																	.charAt(0)
															+ getMap.get(
																	"alarmtime")
																	.toString()
																	.charAt(1));
											Log.i("before DELETE",
													""
															+ getMap.get(
																	"alarmtime")
																	.toString()
																	.charAt(5)
															+ getMap.get(
																	"alarmtime")
																	.toString()
																	.charAt(6));
											db.execSQL("DELETE FROM bougeralarmTable WHERE hour = "
													+ getMap.get("alarmtime")
															.toString()
															.charAt(0)
													+ getMap.get("alarmtime")
															.toString()
															.charAt(1)
													+ " AND minute = "
													+ getMap.get("alarmtime")
															.toString()
															.charAt(5)
													+ getMap.get("alarmtime")
															.toString()
															.charAt(6));
										}

										// 리스트 삭제
										myList.remove(p);
										adapter.notifyDataSetChanged();

										// 지워졌는지 확인 작업
										cursor = db
												.rawQuery(
														"SELECT * FROM bougeralarmTable",
														null);
										Log.i("after DELETE",
												"" + cursor.getCount());
									}
								})
						.setNegativeButton("취소",
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int which) {
									}
								}).show();
			}
		});
	}

	public void onBackPressed() {
		super.onBackPressed();
		overridePendingTransition(android.R.anim.fade_in,
				android.R.anim.fade_out);
		db.close();
	}
}