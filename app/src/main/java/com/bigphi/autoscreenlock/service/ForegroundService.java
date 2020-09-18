package com.bigphi.autoscreenlock.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.transition.Visibility;
import android.util.Log;
import android.widget.RemoteViews;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.bigphi.autoscreenlock.MainActivity;
import com.bigphi.autoscreenlock.R;
import com.bigphi.autoscreenlock.constants.Handlers;
import com.bigphi.autoscreenlock.constants.IntentConstant;
import com.bigphi.autoscreenlock.intent.CustomIntents;
import com.bigphi.autoscreenlock.receiver.NotificationActionReceiver;
import com.bigphi.autoscreenlock.utils.ForegroundNotification;
import com.bigphi.autoscreenlock.utils.NotifyConstants;

import static com.bigphi.autoscreenlock.MainActivity.LOCK_SCREEN;
import static com.bigphi.autoscreenlock.constants.Handlers.serviceToActivity;

public class ForegroundService extends Service {

    private int timeInMinute;
    private Notification notification;
    private Notification.Builder notificationBuilder;
    private NotificationCompat.Builder notificationCompactBuilder;
    public NotificationManager notificationManager;
    private CountDownTimer countDownTimer;
    private RemoteViews notificationLayout;
    private RemoteViews notificationLayoutExpanded;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        setupNotification();
        init();
    }

    private void init() {
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        setupNotificationActionHandler();
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent.getAction();
        if(CustomIntents.STOP_TIMER.equalsIgnoreCase(action)){
            stopThisService();
        }else {
            timeInMinute = intent.getIntExtra("time", 0);
            startTimer();
        }
        return START_NOT_STICKY;
    }

    private void stopThisService() {
        countDownTimer.cancel();
        stopForeground(true);
        stopSelf();
    }

    private void startTimer() {
        long futureMillis = getFutureMillis();
        countDownTimer = new CountDownTimer(futureMillis, 1000) {
            public void onTick(long millisUntilFinished) {
                String timeToUpdate = getTimeToUpdate(millisUntilFinished);
                updateNotification(timeToUpdate);
            }

            public void onFinish() {
                sendHandlerMessage();
            }
        };
        countDownTimer.start();
    }

    private String getTimeToUpdate(long millisUntilFinished) {
        long minute = (millisUntilFinished/1000) / 60;
        long second = (millisUntilFinished /1000) % 60;
        return ForegroundNotification.convertToPrintableText(minute, second);
    }

    private void updateNotification(String text) {
        notificationLayout.setTextViewText(R.id.remaining_time, text);
        notificationLayoutExpanded.setTextViewText(R.id.remaining_time, text);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationBuilder.setCustomContentView(notificationLayout);
            notificationBuilder.setCustomBigContentView(notificationLayoutExpanded);
            notification = notificationBuilder.build();
        } else {

            notificationCompactBuilder.setCustomContentView(notificationLayout);
            notificationCompactBuilder.setCustomBigContentView(notificationLayoutExpanded);

            notificationCompactBuilder.setContentText(text);
            notification = notificationCompactBuilder.build();
        }
        notificationManager.notify(NotifyConstants.NOTIFICATION_ID, notification);
    }

    private void sendHandlerMessage() {
        Message message = new Message();
        message.what = LOCK_SCREEN;
        serviceToActivity.sendMessage(message);
    }

    private long getFutureMillis() {
        return timeInMinute * 60 * 1000L;
    }

    private void setupNotification() {
        Intent intent=new Intent(getApplicationContext(), MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(),1,intent,0);

        notificationLayout = new RemoteViews(getPackageName(), R.layout.notification_small);
        notificationLayoutExpanded = new RemoteViews(getPackageName(), R.layout.notification_large);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationBuilder = ForegroundNotification.getNotificationAboveOrio(pendingIntent, getApplicationContext(), NotifyConstants.CHANNEL_ID);
            notificationBuilder.setCustomContentView(notificationLayout);
            notificationBuilder.setCustomBigContentView(notificationLayoutExpanded);
            notification = notificationBuilder.build();

        } else {
            notificationCompactBuilder = ForegroundNotification.getNotificationForBelowOrio(pendingIntent, getApplicationContext(), NotifyConstants.CHANNEL_ID);
            notificationCompactBuilder.setCustomContentView(notificationLayout);
            notificationCompactBuilder.setCustomBigContentView(notificationLayoutExpanded);
            notification = notificationCompactBuilder.build();
        }

        setListners();
        startForeground(NotifyConstants.NOTIFICATION_ID, notification);
    }

    private void setListners() {
        Intent resetTimer = new Intent(getApplicationContext(), NotificationActionReceiver.class).
                setAction(CustomIntents.STOP_NOTIFICATION_TIMER);
        Intent killApp = new Intent(getApplicationContext(), NotificationActionReceiver.class).
                setAction(CustomIntents.KILL_APP);

        PendingIntent resetTimerPendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, resetTimer, PendingIntent.FLAG_UPDATE_CURRENT);
        notificationLayoutExpanded.setOnClickPendingIntent(R.id.reset_timer, resetTimerPendingIntent);

        PendingIntent killAppPendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, killApp, PendingIntent.FLAG_UPDATE_CURRENT);
        notificationLayoutExpanded.setOnClickPendingIntent(R.id.kill_app, killAppPendingIntent);
    }

    private void setupNotificationActionHandler() {
        Handlers.receiverToService = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(@NonNull Message msg) {
                int what = msg.what;
                if(IntentConstant.RESET_TIMER == what) {
                    countDownTimer.cancel();
                    updateNotification("00:00");

                } else if( IntentConstant.KILL_APP == what){
                    stopThisService();
                }
                return true;
            }

        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e("LOG", "DESTROY");
    }
}
