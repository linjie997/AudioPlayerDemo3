package com.potato997.audioplayerdemo3;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.widget.RemoteViews;

/**
 * Created by linjie on 22/10/2017.
 */

public class MyNotification extends Notification {

    private Context parent;
    private NotificationManager nManager;
    private NotificationCompat.Builder nBuilder;
    private RemoteViews remoteView;

    public MyNotification(Context parent) {
        this.parent = parent;
        nBuilder = new NotificationCompat.Builder(parent)
                .setContentTitle("GPlayer")
                .setSmallIcon(R.drawable.no_art)
                .setOngoing(true);

        remoteView = new RemoteViews(parent.getPackageName(), R.layout.notificationlayout);

        //set the button listeners
        setListeners(remoteView);
        nBuilder.setContent(remoteView);

        nManager = (NotificationManager) parent.getSystemService(Context.NOTIFICATION_SERVICE);
        nManager.notify(2, nBuilder.build());
    }

    public void setListeners(RemoteViews view){

        Intent play = new Intent(parent,NotificationReturnSlot.class);
        play.putExtra("DO", "play");
        PendingIntent btnPlay = PendingIntent.getActivity(parent, 0, play, 0);
        view.setOnClickPendingIntent(R.id.nPlay, btnPlay);

        Intent back = new Intent(parent, NotificationReturnSlot.class);
        back.putExtra("DO", "back");
        PendingIntent btnBack = PendingIntent.getActivity(parent, 1, back, 0);
        view.setOnClickPendingIntent(R.id.nBack, btnBack);

        Intent forward = new Intent(parent, NotificationReturnSlot.class);
        back.putExtra("DO", "forward");
        PendingIntent btnForward = PendingIntent.getActivity(parent, 1, forward, 0);
        view.setOnClickPendingIntent(R.id.nForward, btnForward);
    }

    public void notificationCancel() {
        nManager.cancel(2);
    }
}