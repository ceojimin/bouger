package com.voicematch.bouger.alarm;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

class BougerAlarmDBHelper extends SQLiteOpenHelper {
	public BougerAlarmDBHelper(Context context) {
		super(context, "bougeralarm.db", null, 1);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// 알람 DB 생성
		db.execSQL("CREATE TABLE bougeralarmTable ( id INTEGER ,"
				+ " hour INTEGER, " + " minute INTEGER, " + " type INTEGER);");
	}

	@Override
	public void onOpen(SQLiteDatabase db) {
		super.onOpen(db);
		Log.i("DB OPEN", "DB OPEN OK");
	}

	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {// db 업데이트
		db.execSQL("DROP TABLE IF EXISTS bougeralarmTable");

		onCreate(db);
	}
}