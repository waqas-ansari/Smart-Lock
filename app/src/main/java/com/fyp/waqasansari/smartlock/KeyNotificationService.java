package com.fyp.waqasansari.smartlock;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Timer;
import java.util.TimerTask;

public class KeyNotificationService extends Service {
    DataSnapshot previousDataSnapshot;
    String CurrentUser;
    String KeyData;
    String[] KeySender = new String[2];
    int count=0;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("", "Starting Service");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);

        CurrentUser = intent.getStringExtra("current-user");

        new Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        GetKey();
                    }
                }).start();
            }
        }, 0, 5000);


    }

    public void GetKey(){
        Log.d("", "Inside GetKey " + Integer.toString(count));
        Firebase.setAndroidContext(KeyNotificationService.this);
        String firebaseURL = "https://homeiot.firebaseio.com/" + CurrentUser + "/lock/available-keys";
        Firebase KeyReference = new Firebase(firebaseURL);
        KeyReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(!dataSnapshot.equals(previousDataSnapshot)) {
                    try {
                        KeyData = dataSnapshot.getValue().toString();
                    } catch (Exception e) {
                        KeyData = null;
                    }
                    if (KeyData != null) {
                        try {
                            JSONObject jsonObject = new JSONObject(KeyData);
                            for(int i=0; i<jsonObject.names().length(); i++) {
                                JSONObject value = jsonObject.getJSONObject(jsonObject.names().getString(i));
                                KeySender[0] = value.getString("sender");
                                KeySender[1] = value.getString("key");
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                        Notification notifyKey = new Notification(android.R.drawable.stat_notify_chat,
                                "Key Received",
                                System.currentTimeMillis());

                        String Title = "Tap to unlock";
                        String Details = KeySender[0] + " sends you a key. Tap to unlock";
                        Intent intent = new Intent(KeyNotificationService.this, KeyDialog.class);
                        intent.putExtra("sender-key", KeySender);
                        PendingIntent pendingIntent = PendingIntent.getActivity(KeyNotificationService.this, 0, intent, 0);
                        notifyKey.setLatestEventInfo(KeyNotificationService.this, Title, Details, pendingIntent);
                        notificationManager.notify(0, notifyKey);
                    }
                    Log.d("", "Ending...");

                    previousDataSnapshot = dataSnapshot;
                    count++;
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }

    @Override
    public void onDestroy() {
        Log.d("", "Service destroyed...");
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
