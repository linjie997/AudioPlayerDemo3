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

public class NotificationService extends IntentService {

    public static MusicService musicService;

    private static final String PLAY_ACTION = "com.potato997.gplayer.PLAY_ACTION";
    private static final String FORWARD_ACTION = "com.potato997.gplayer.FORWARD_ACTION";
    private static final String BACK_ACTION = "com.potato997.gplayer.BACK_ACTION";
    final public static Uri sArtworkUri = Uri.parse("content://media/external/audio/albumart");

    private NotificationManager notificationManager;
    private RemoteViews remoteView;
    private NotificationCompat.Builder nBuilder;
    Bitmap bm;

    public NotificationService() {
        super("notification-service");
    }

    @Override
    public void onCreate() {
        super.onCreate();

        remoteView = new RemoteViews(getPackageName(), R.layout.notification);

        setListeners(remoteView);

        MusicService.notificationService = this;

        nBuilder = new NotificationCompat.Builder(getApplicationContext())
                .setContentTitle("GPlayer")
                .setSmallIcon(R.drawable.no_art)
                .setAutoCancel(false)
                .setContent(remoteView);

        notificationManager = (NotificationManager) getSystemService(getApplicationContext().NOTIFICATION_SERVICE);
        notificationManager.notify(1, nBuilder.build());

        updateNoti();

    }

    private void setListeners(RemoteViews v){

        Intent play = new Intent(this, NotificationService.class);
        play.setAction(PLAY_ACTION);
        PendingIntent pendingPlay = PendingIntent.getService(this, 0, play, 0);
        v.setOnClickPendingIntent(R.id.nPlay, pendingPlay);

        Intent back = new Intent(this, NotificationService.class);
        back.setAction(BACK_ACTION);
        PendingIntent pendingBack = PendingIntent.getService(this, 0, back, 0);
        v.setOnClickPendingIntent(R.id.nBack, pendingBack);

        Intent forward = new Intent(this, NotificationService.class);
        forward.setAction(FORWARD_ACTION);
        PendingIntent pendingForward = PendingIntent.getService(this, 0, forward, 0);
        v.setOnClickPendingIntent(R.id.nForward, pendingForward);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if(intent.getAction() != null){
            if(intent.getAction().equals(PLAY_ACTION)){
                try { musicService.play();
                }
                catch (Exception e){}
            }
            else if (intent.getAction().equals(BACK_ACTION)){
                try { musicService.playPrev(); }
                catch (Exception e){}
            }
            else if (intent.getAction().equals(FORWARD_ACTION)){
                try { musicService.playNext(); }
                catch (Exception e){}
            }
            updateNoti();
        }
    }

    public void updateNoti(){

        if(musicService.player.isPlaying()){
            remoteView.setImageViewResource(R.id.nPlay, R.drawable.pause);
        } else {
            remoteView.setImageViewResource(R.id.nPlay, R.drawable.play);
        }

        final Uri uri = ContentUris.withAppendedId(sArtworkUri,
                musicService.music.get(musicService.index).getAlbumId());
        try {
            bm = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
        } catch (IOException e) { }

        remoteView.setImageViewBitmap(R.id.nCover, bm);
        remoteView.setTextViewText(R.id.nTitle, musicService.songTitle);

        nBuilder.setContent(remoteView);

        notificationManager = (NotificationManager) getSystemService(getApplicationContext().NOTIFICATION_SERVICE);
        notificationManager.notify(1, nBuilder.build());
    }

}
