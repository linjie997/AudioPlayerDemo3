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
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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
    private MusicService musicService;
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
/*
        currentTrack.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override public void onCompletion(MediaPlayer mp) { forward(); } });
*/
        adapter = new MyRecycleAdapter(this, music);

        mRecyclerView.setAdapter(adapter);

        MusicService.mainActivity = this;

        seekBar.setMax(currentTrack.getDuration());

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(fromUser)
                    musicService.seek(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
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
            musicService = binder.getService();
            musicService.getMusic(music);
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
                musicService.play();
            }

            @Override
            public void onSwipeLeft() {
                super.onSwipeLeft();
                musicService.playPrev();
            }

            @Override
            public void onSwipeRight() {
                super.onSwipeLeft();
                musicService.playNext();
            }
        });
    }
}