package com.potato997.gplayer;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentUris;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.app.NotificationCompat;
import android.widget.RemoteViews;

import java.io.IOException;

public class BackgroundService extends IntentService {

    public static MainActivity mainActivity;

    private static final String PLAY_ACTION = "com.potato997.gplayer.PLAY_ACTION";
    private static final String FORWARD_ACTION = "com.potato997.gplayer.FORWARD_ACTION";
    private static final String BACK_ACTION = "com.potato997.gplayer.BACK_ACTION";
    final public static Uri sArtworkUri = Uri.parse("content://media/external/audio/albumart");

    private NotificationManager notificationManager;
    private RemoteViews remoteView;
    private NotificationCompat.Builder nBuilder;
    Bitmap bm;

    public BackgroundService() {
        super("notification-service");
    }

    @Override
    public void onCreate() {
        super.onCreate();

        nBuilder = new NotificationCompat.Builder(getApplicationContext())
                .setContentTitle("GPlayer")
                .setSmallIcon(R.drawable.no_art);

        remoteView = new RemoteViews(getPackageName(), R.layout.notificationlayout);


        final Uri uri = ContentUris.withAppendedId(sArtworkUri,
                MainActivity.music.get(MainActivity.index).getAlbumId());

        try {
            bm = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
        } catch (IOException e) {
        }

        remoteView.setImageViewBitmap(R.id.nAlbumCover, bm);
        setListeners(remoteView);
        nBuilder.setContent(remoteView);

        notificationManager = (NotificationManager) getSystemService(getApplicationContext().NOTIFICATION_SERVICE);
        notificationManager.notify(1, nBuilder.build());

        updateNoti();

        MainActivity.backgroundService = this;
    }

    private void setListeners(RemoteViews v){

        Intent play = new Intent(this, BackgroundService.class);
        play.setAction(PLAY_ACTION);
        PendingIntent btnPlay = PendingIntent.getService(this, 0, play, 0);
        v.setOnClickPendingIntent(R.id.nPlay, btnPlay);

        Intent back = new Intent(this, BackgroundService.class);
        back.setAction(BACK_ACTION);
        PendingIntent btnBack = PendingIntent.getService(this, 0, back, 0);
        v.setOnClickPendingIntent(R.id.nBack, btnBack);

        Intent forward = new Intent(this, BackgroundService.class);
        forward.setAction(FORWARD_ACTION);
        PendingIntent btnForward = PendingIntent.getService(this, 0, forward, 0);
        v.setOnClickPendingIntent(R.id.nForward, btnForward);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if(intent.getAction() != null){
            if(intent.getAction().equals(PLAY_ACTION)){
                try { mainActivity.play();
                }
                catch (Exception e){}
            }
            else if (intent.getAction().equals(BACK_ACTION)){
                try { mainActivity.back(); }
                catch (Exception e){}
            }
            else if (intent.getAction().equals(FORWARD_ACTION)){
                try { mainActivity.forward(); }
                catch (Exception e){}
            }
            updateNoti();
        }
    }

    public void updateNoti(){

        if(MainActivity.currentTrack.isPlaying()){
            remoteView.setImageViewResource(R.id.nPlay, R.drawable.pause);
        } else {
            remoteView.setImageViewResource(R.id.nPlay, R.drawable.play);
        }

        final Uri uri = ContentUris.withAppendedId(sArtworkUri,
                MainActivity.music.get(MainActivity.index).getAlbumId());
        try {
            bm = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
        } catch (IOException e) { }

        remoteView.setImageViewBitmap(R.id.nAlbumCover, bm);

        nBuilder.setContent(remoteView);

        notificationManager = (NotificationManager) getSystemService(getApplicationContext().NOTIFICATION_SERVICE);
        notificationManager.notify(1, nBuilder.build());
    }

}
