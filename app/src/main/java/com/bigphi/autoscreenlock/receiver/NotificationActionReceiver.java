package com.bigphi.autoscreenlock.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Message;

import com.bigphi.autoscreenlock.constants.Handlers;
import com.bigphi.autoscreenlock.constants.IntentConstant;
import com.bigphi.autoscreenlock.intent.CustomIntents;

public class NotificationActionReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if(CustomIntents.STOP_NOTIFICATION_TIMER.equalsIgnoreCase(action)){

            Message message = new Message();
            message.what = IntentConstant.RESET_TIMER;
            Handlers.receiverToService.sendMessage(message);

        } else if(CustomIntents.KILL_APP.equalsIgnoreCase(action)){

            Message message = new Message();
            message.what = IntentConstant.KILL_APP;
            Handlers.receiverToService.sendMessage(message);
        }
    }
}
