package com.potato997.gplayer;

import android.Manifest;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class MainActivity extends AppCompatActivity {

    static List<Music> music = new ArrayList<>();

    ArrayList<HashMap<String, Object>> musicList = new ArrayList<>();
    String title;
    String artist;
    String album;
    long albumId;
    TextView titletxt;
    TextView currentTime;
    TextView totTime;
    static int index;
    ImageButton play;
    ImageButton back;
    ImageButton forward;
    boolean isRandom = false;
    SeekBar seekBar;
    static MediaPlayer currentTrack;
    public static NotificationService notificationService;

    final public static Uri sArtworkUri = Uri.parse("content://media/external/audio/albumart");

    private RecyclerView mRecyclerView;
    private MyRecycleAdapter adapter;

    private Intent playIntent;
    private MusicService musicSrv;
    private boolean musicBound=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[] { Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE }, 0);
            }
        }

        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        currentTime = (TextView) findViewById(R.id.currentTime);
        totTime = (TextView) findViewById(R.id.totTime);
        play = (ImageButton) findViewById(R.id.play);
        back = (ImageButton) findViewById(R.id.back);
        forward = (ImageButton) findViewById(R.id.forward);
        titletxt = (TextView) findViewById(R.id.title);

        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                play();
            }
        });

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                back();
            }
        });

        forward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                forward();
            }
        });

        seekBar = (SeekBar) findViewById(R.id.seekBar);

        setGesture();

        loadAudio();

        Uri trackUri = ContentUris.withAppendedId(
                android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                music.get(0).getId());

        currentTrack = new MediaPlayer();

        try {
            currentTrack.setDataSource(getApplicationContext(), trackUri);
            currentTrack.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }


        adapter = new MyRecycleAdapter(this, music);

        mRecyclerView.setAdapter(adapter);

        Intent backService = new Intent(this, NotificationService.class);
        startService(backService);

        NotificationService.mainActivity = this;

        totTime.setText(durationToTime(currentTrack.getDuration()));

        seekBar.setMax(currentTrack.getDuration());

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(fromUser)
                    currentTrack.seekTo(progress);

                currentTime.setText(durationToTime(currentTrack.getCurrentPosition()));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });


        final Handler handler = new Handler();
        Runnable task = new Runnable() {
            @Override
            public void run() {
                if(currentTrack.isPlaying()){
                    currentTime.setText(durationToTime(currentTrack.getCurrentPosition()));
                    seekBar.setProgress(currentTrack.getCurrentPosition());
                }
                handler.postDelayed(this, 1000);
            }
        };
        handler.post(task);

    }

    public void loadAudio() {

        try {
            ContentResolver musicResolver = getContentResolver();
            Cursor musicCursor = musicResolver.query(android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    null, null, null, MediaStore.Audio.Media.TITLE + " ASC");

            if(musicCursor!=null && musicCursor.moveToFirst()){
                int titleColumn = musicCursor.getColumnIndex
                        (android.provider.MediaStore.Audio.Media.TITLE);
                int idColumn = musicCursor.getColumnIndex
                        (android.provider.MediaStore.Audio.Media._ID);
                int artistColumn = musicCursor.getColumnIndex
                        (android.provider.MediaStore.Audio.Media.ARTIST);
                int albumColumn = musicCursor.getColumnIndex
                        (MediaStore.Audio.Media.ALBUM);
                int albumIdColumn = musicCursor.getColumnIndex
                        (MediaStore.Audio.Media.ALBUM_ID);

                do{
                    title = musicCursor.getString(titleColumn);
                    artist = musicCursor.getString(artistColumn);
                    album = musicCursor.getString(albumColumn);
                    albumId = musicCursor.getLong(albumIdColumn);
                    long id = musicCursor.getLong(idColumn);

                    HashMap<String, Object> mp = new HashMap<>();

                    Uri albumUri = ContentUris.withAppendedId(sArtworkUri,
                            albumId);

                    music.add(new Music(artist, title, album, albumId, id, albumUri));

                    mp.put("title", title);

                    mp.put("img", albumUri);

                    musicList.add(mp);
                } while (musicCursor.moveToNext());
            }

            titletxt.setText(music.get(index).getTitle());

        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), "LOAD AUDIO" + e.toString(), Toast.LENGTH_LONG).show();
        }
    }

    private ServiceConnection musicConnection = new ServiceConnection(){

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicService.MusicBinder binder = (MusicService.MusicBinder)service;
            musicSrv = binder.getService();
            musicSrv.setList(music);
            musicBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            musicBound = false;
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        if(playIntent==null){
            playIntent = new Intent(this, MusicService.class);
            bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);
            startService(playIntent);
        }
    }

    public void setGesture(){
        findViewById(android.R.id.content).setOnTouchListener(new OnSwipeTouchListener(MainActivity.this) {

            @Override
            public void onDoubleClick() {
                super.onDoubleClick();
                play();
            }

            @Override
            public void onSwipeLeft() {
                super.onSwipeLeft();
                back();
            }

            @Override
            public void onSwipeRight() {
                super.onSwipeLeft();
                forward();
            }
        });
    }

    public void play(){

        if(currentTrack.isPlaying()){
            currentTrack.pause();
            play.setImageResource(R.drawable.play);
        }
        else{
            currentTrack.start();
            play.setImageResource(R.drawable.pause);
        }
    }

    public void forward(){
        currentTrack.reset();
        if(isRandom){
            index = (int)(Math.random()*music.size());
        }
        else{
            if(index < (music.size()-1)){
                index++;
            }else{
                index = 0;
            }
        }
        changeAudio();
    }

    public void back(){
        currentTrack.reset();
        if(isRandom){
            index = (int)(Math.random()*music.size());
        }
        else
        {
            if(index > 0){
                index--;
            }
            else{
                index = music.size()-1;
            }
        }
        changeAudio();
    }

    public void changeAudio(){

        Uri trackUri = ContentUris.withAppendedId(
                android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                music.get(index).getId());

        currentTrack = new MediaPlayer();

        try {
            currentTrack.setDataSource(getApplicationContext(), trackUri);
            currentTrack.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }

        currentTrack.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override public void onCompletion(MediaPlayer mp) { forward(); } });

        seekBar.setMax(currentTrack.getDuration());
        seekBar.setProgress(0);
        totTime.setText(durationToTime(currentTrack.getDuration()));
        currentTrack.start();
        titletxt.setText(music.get(index).getTitle());
        play.setImageResource(R.drawable.pause);
        notificationService.updateNoti();
    }

    public void random(View v){
        if(isRandom){
            isRandom = false;
        }
        else{
            isRandom = true;
        }
    }

    public static String durationToTime(long milliseconds) {
        String finalTimerString = "";
        String secondsString = "";

        // Convert total duration into time
        int hours = (int) (milliseconds / (1000 * 60 * 60));
        int minutes = (int) (milliseconds % (1000 * 60 * 60)) / (1000 * 60);
        int seconds = (int) ((milliseconds % (1000 * 60 * 60)) % (1000 * 60) / 1000);
        // Add hours if there
        if (hours > 0) {
            finalTimerString = hours + ":";
        }

        // Prepending 0 to seconds if it is one digit
        if (seconds < 10) {
            secondsString = "0" + seconds;
        } else {
            secondsString = "" + seconds;
        }

        finalTimerString = finalTimerString + minutes + ":" + secondsString;

        // return timer string
        return finalTimerString;
    }

}