package com.potato997.gplayer;

import android.app.Service;
import android.content.ContentUris;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;
import android.view.View;

import java.util.List;
import java.util.Random;

public class MusicService extends Service implements
        MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener,
        MediaPlayer.OnCompletionListener {

    public MediaPlayer player;
    public List<Music> music;
    public int index;
    private final IBinder musicBind = new MusicBinder();
    public String songTitle="";
    private boolean shuffle=false;
    private boolean isStarted = false;
    private Random rand;

    public static MainActivity mainActivity;
    public static NotificationService notificationService;
    private String currTime;

    public void onCreate(){

        super.onCreate();
        MyRecycleAdapter.musicService = this;

        notificationService.musicService = this;

        index=0;

        rand=new Random();

        player = new MediaPlayer();

        initMusicPlayer();
    }

    public void initMusicPlayer(){
        player.setWakeMode(getApplicationContext(),
                PowerManager.PARTIAL_WAKE_LOCK);
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);
        player.setOnPreparedListener(this);
        player.setOnCompletionListener(this);
        player.setOnErrorListener(this);
        player.pause();

        if(MainActivity.isRunning){

            mainActivity.play.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    play();
                }
            });

            mainActivity.forward.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    playNext();
                }
            });

            mainActivity.back.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    playPrev();
                }
            });
        }

        final Handler handler = new Handler();
        Runnable task = new Runnable() {
            @Override
            public void run() {
                if(player.isPlaying()){
                    currTime = durationToTime(player.getCurrentPosition());
                    if(MainActivity.isRunning){
                        mainActivity.currentTime.setText(currTime);
                        mainActivity.seekBar.setProgress(player.getCurrentPosition());
                    }
                }
                handler.postDelayed(this, 1000);
            }
        };
        handler.post(task);
    }

    public void play(){

        if(player.isPlaying()){
            player.pause();
            if(MainActivity.isRunning)
                mainActivity.play.setImageResource(R.drawable.play);
        }
        else{
            player.start();
            if(MainActivity.isRunning)
                mainActivity.play.setImageResource(R.drawable.pause);
        }
    }

    public void getMusic(List<Music> music){

        this.music=music;
        Music playSong = music.get(index);
        songTitle=playSong.getTitle();
        long currSong = playSong.getId();

        Uri trackUri = ContentUris.withAppendedId(
                android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                currSong);
        try{
            player.setDataSource(getApplicationContext(), trackUri);
            player.prepare();
        }
        catch(Exception e){
            Log.e("MUSIC SERVICE", "Error setting data source", e);
        }

        isStarted = true;

        mainActivity.seekBar.setMax(player.getDuration());
        mainActivity.seekBar.setProgress(player.getCurrentPosition());
        mainActivity.currentTime.setText("0:00");
    }

    public class MusicBinder extends Binder {
        MusicService getService() {
            return MusicService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return musicBind;
    }

    @Override
    public boolean onUnbind(Intent intent){
        player.stop();
        player.release();
        return false;
    }

    public void playSong(){
        player.reset();
        Music playSong = music.get(index);
        songTitle=playSong.getTitle();
        long currSong = playSong.getId();
        Uri trackUri = ContentUris.withAppendedId(
                android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                currSong);
        try{
            player.setDataSource(getApplicationContext(), trackUri);
            player.prepare();
        }
        catch(Exception e){
            Log.e("MUSIC SERVICE", "Error setting data source", e);
        }

        if(MainActivity.isRunning){
            mainActivity.seekBar.setMax(player.getDuration());
            mainActivity.seekBar.setProgress(0);
            mainActivity.totTime.setText(durationToTime(player.getDuration()));
            mainActivity.play.setImageResource(R.drawable.pause);
            mainActivity.titletxt.setText(songTitle);
            mainActivity.totTime.setText(durationToTime(player.getDuration()));
        }
        player.start();
    }

    public void setSong(int songIndex){
        index=songIndex;
        playSong();
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        mp.reset();
        playNext();
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        Log.v("MUSIC PLAYER", "Playback Error");
        mp.reset();
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        Intent notiService = new Intent(this, NotificationService.class);
        startService(notiService);
        mainActivity.totTime.setText(durationToTime(player.getDuration()));
    }

    public void seek(int posn){
        player.seekTo(posn);
        mainActivity.currentTime.setText(durationToTime(player.getCurrentPosition()));
    }

    //skip to previous track
    public void playPrev(){
        index--;
        if(index<0) index=music.size()-1;
        playSong();
    }

    //skip to next
    public void playNext(){
        if(isStarted){
            if(shuffle){
                int newSong = index;
                while(newSong==index){
                    newSong=rand.nextInt(music.size());
                }
                index=newSong;
            }
            else{
                index++;
                if(index>=music.size())
                    index=0;
            }
            playSong();
        }
    }

    @Override
    public void onDestroy() {
        stopForeground(true);
    }

    //toggle shuffle
    public void setShuffle(){
        if(shuffle) shuffle=false;
        else shuffle=true;
    }

    public String durationToTime(long milliseconds) {
        String finalTimerString = "";
        String secondsString = "";

        int hours = (int) (milliseconds / (1000 * 60 * 60));
        int minutes = (int) (milliseconds % (1000 * 60 * 60)) / (1000 * 60);
        int seconds = (int) ((milliseconds % (1000 * 60 * 60)) % (1000 * 60) / 1000);

        if (hours > 0) {
            finalTimerString = hours + ":";
        }

        if (seconds < 10) {
            secondsString = "0" + seconds;
        } else {
            secondsString = "" + seconds;
        }

        finalTimerString = finalTimerString + minutes + ":" + secondsString;

        return finalTimerString;
    }

}