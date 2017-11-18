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

    //media player
    public MediaPlayer player;
    //song list
    public List<Music> music;
    //current position
    public int index;
    //binder
    private final IBinder musicBind = new MusicBinder();
    //title of current song
    public String songTitle="";
    //notification id
    private static final int NOTIFY_ID=1;
    //shuffle flag and random
    private boolean shuffle=false;
    private Random rand;

    public static MainActivity mainActivity;
    public static NotificationService notificationService;
    private String currTime;

    public void onCreate(){
        //create the service
        super.onCreate();
        MyRecycleAdapter.musicService = this;
        notificationService.musicService = this;
        //initialize position
        index=0;
        //random
        rand=new Random();
        //create player
        player = new MediaPlayer();
        //initialize
        initMusicPlayer();
    }

    public void initMusicPlayer(){
        //set player properties
        player.setWakeMode(getApplicationContext(),
                PowerManager.PARTIAL_WAKE_LOCK);
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);
        //set listeners
        player.setOnPreparedListener(this);
        player.setOnCompletionListener(this);
        player.setOnErrorListener(this);

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

        final Handler handler = new Handler();
        Runnable task = new Runnable() {
            @Override
            public void run() {
                if(player.isPlaying()){
                    currTime = durationToTime(player.getCurrentPosition());
                    mainActivity.currentTime.setText(currTime);
                    mainActivity.seekBar.setProgress(player.getCurrentPosition());
                }
                handler.postDelayed(this, 1000);
            }
        };
        handler.post(task);
    }

    public void play(){

        if(player.isPlaying()){
            pausePlayer();
            mainActivity.play.setImageResource(R.drawable.play);
        }
        else{
            player.start();
            mainActivity.play.setImageResource(R.drawable.pause);
        }
    }

    public void getMusic(List<Music> themusic){
        music=themusic;

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
    }

    //binder
    public class MusicBinder extends Binder {
        MusicService getService() {
            return MusicService.this;
        }
    }

    //activity will bind to service
    @Override
    public IBinder onBind(Intent intent) {
        return musicBind;
    }

    //release resources when unbind
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

        mainActivity.titletxt.setText(songTitle);

        mainActivity.seekBar.setMax(player.getDuration());
        mainActivity.seekBar.setProgress(0);
        mainActivity.totTime.setText(durationToTime(player.getDuration()));
        mainActivity.play.setImageResource(R.drawable.pause);
        notificationService.updateNoti();
    }

    //set the song
    public void setSong(int songIndex){
        index=songIndex;
        playSong();
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        //check if playback has reached the end of a track
        //if(player.getCurrentPosition()>0){
            mp.reset();
            playNext();
        //}
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        Log.v("MUSIC PLAYER", "Playback Error");
        mp.reset();
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        //start playback
        mp.start();

        Intent notiService = new Intent(this, NotificationService.class);
        startService(notiService);
    }

    //playback methods
    public int getPosn(){
        return player.getCurrentPosition();
    }

    public int getDur(){
        return player.getDuration();
    }

    public boolean isPng(){
        return player.isPlaying();
    }

    public void pausePlayer(){
        player.pause();
    }

    public void seek(int posn){
        player.seekTo(posn);
    }

    public void go(){
        player.start();
    }

    //skip to previous track
    public void playPrev(){
        index--;
        if(index<0) index=music.size()-1;
        playSong();
    }

    //skip to next
    public void playNext(){
        if(shuffle){
            int newSong = index;
            while(newSong==index){
                newSong=rand.nextInt(music.size());
            }
            index=newSong;
        }
        else{
            index++;
            if(index>=music.size()) index=0;
        }
        playSong();
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