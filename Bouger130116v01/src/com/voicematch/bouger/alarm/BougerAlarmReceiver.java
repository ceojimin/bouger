package com.voicematch.bouger.alarm;

import com.voicematch.bouger.R;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.provider.MediaStore.Audio;
import android.widget.Toast;

public class BougerAlarmReceiver extends BroadcastReceiver {
	private int YOURAPP_NOTIFICATION_ID;   
	static NotificationManager nm;
	
    @Override  
    public void onReceive(Context context, Intent intent) {   
        showNotification(context, R.drawable.alarm, "알람!!", "지금 이러고 있을 시간 없다.");
    }    
 
    private void showNotification(Context context, int statusBarIconID, String statusBarTextID, String detailedTextID) {
    	Intent contentIntent = new Intent(context, BougerAlarm.class);
        PendingIntent theappIntent = PendingIntent.getActivity(context, 0, contentIntent, PendingIntent.FLAG_UPDATE_CURRENT); 
        
        CharSequence from = "Bouger 알람";  
        CharSequence message = "알람 설정 상태입니다.";      
 
        Notification notif = new Notification(statusBarIconID, null, 0); 
        //notif.sound = Uri.withAppendedPath(Audio.Media.INTERNAL_CONTENT_URI, "7");//ringURI; 
        notif.flags = Notification.FLAG_INSISTENT; 
        notif.setLatestEventInfo(context, from, message, theappIntent);  
        notif.ledARGB = Color.GREEN; 
        nm = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE); 
        nm.notify(1234, notif);  
    }   
}