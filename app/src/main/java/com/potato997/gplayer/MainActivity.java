package com.potato997.gplayer;

import android.Manifest;
import android.content.ContentUris;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class MainActivity extends AppCompatActivity {

    static List<Music> music = new ArrayList<>();
    String title;
    String path;
    String artist;
    String album;
    String cover;
    long albumId;
    ImageView album_art;
    TextView titletxt;
    TextView currentTime;
    TextView totTime;
    static int index;
    ImageButton play;
    ImageButton back;
    ImageButton forward;
    Button rnd;
    String[] dir;
    boolean isRandom = false;
    SeekBar seekBar;
    static MediaPlayer currentTrack;
    public static BackgroundService backgroundService;

    final public static Uri sArtworkUri = Uri.parse("content://media/external/audio/albumart");

    private RecyclerView mRecyclerView;
    private MyRecycleAdapter adapter;


/*
    SharedPreferences sharedPrefs;
    SharedPreferences.Editor ed;
    GsonBuilder gsonb = new GsonBuilder();
    Gson mGson = gsonb.create();
*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[] { Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE }, 0);
            }
        }

        super.onCreate(savedInstanceState);

        //sharedPrefs = getSharedPreferences("Save", Context.MODE_PRIVATE);
        //ed = sharedPrefs.edit();

        setContentView(R.layout.activity_main);

        Intent backService = new Intent(this, BackgroundService.class);
        startService(backService);

        BackgroundService.mainActivity = this;

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
        /*txtCount = (TextView) findViewById(R.id.audioCount);

        album_art = (ImageView) findViewById(R.id.album_cover);
        rnd = (Button) findViewById(R.id.rnd);*/
        seekBar = (SeekBar) findViewById(R.id.seekBar);

        setGesture();

        loadAudio();

/*
        if(sharedPrefs.contains("MusicList")){
            Toast.makeText(getApplicationContext(), "LOCAL", Toast.LENGTH_LONG).show();
            music = loadLocal();

            final Handler backgroundLoad = new Handler();
            Runnable task = new Runnable() {
                @Override
                public void run() {
                    loadAudio();
                }
            };

        } else {
            Toast.makeText(getApplicationContext(), "LOAD", Toast.LENGTH_LONG).show();
            loadAudio();
        }
*/
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
        List<Music> tempMusic = new ArrayList<>();

        ArrayList<HashMap<String, Object>> musicList = null;
        try {

            Cursor cursor = getContentResolver().query(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    null,
                    MediaStore.Audio.Media.IS_MUSIC + "!= 0",
                    null,
                    MediaStore.Audio.Media.TITLE + " ASC");

            cursor.moveToFirst();

            musicList = new ArrayList<>();

            while (cursor.moveToNext()) {
                path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
                title = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
                artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
                album = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM));
                albumId = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID));

                dir = path.split("emulated/0");

                HashMap<String, Object> mp = new HashMap<>();

                Uri uri = ContentUris.withAppendedId(sArtworkUri,
                        albumId);

                tempMusic.add(new Music(dir[1], artist, title, album, albumId, uri));

                mp.put("title", title);

                mp.put("img", uri);

                musicList.add(mp);
            }

            music = tempMusic;

            currentTrack = MediaPlayer.create(this, Uri.parse(Environment.getExternalStorageDirectory().getPath() + music.get(index).getPath()));

            adapter = new MyRecycleAdapter(this, music);

            mRecyclerView.setAdapter(adapter);

            titletxt.setText(music.get(index).getTitle());

        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), "LOAD AUDIO" + e.toString(), Toast.LENGTH_LONG).show();
        }
    }

/*
    public void save(List<Music> l){
        Gson gson = new Gson();
        String json = gson.toJson(l);
        ed.putString("MusicList", json);
        ed.commit();
    }

    public List<Music> loadLocal(){

        List <Music> tempList = new ArrayList<Music>();

        Gson gson = new Gson();
        String json = sharedPrefs.getString("MusicList", "");
        if (json.isEmpty()) {
            tempList = new ArrayList<Music>();
        } else {
            Type type = new TypeToken<List<Music>>() {
            }.getType();
            tempList = gson.fromJson(json, type);
        }
        return tempList;
    }
*/
    public void changeAlbumArt(){
        final Uri uri = ContentUris.withAppendedId(sArtworkUri,
                music.get(index).getAlbumId());

        Picasso.with(this)
                .load(uri)
                .error(R.drawable.no_art)
                .into(album_art);
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
        if(currentTrack.isPlaying())
            currentTrack.pause();

        currentTrack.seekTo(0);
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
        currentTrack.pause();
        currentTrack.seekTo(0);
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
        currentTrack = MediaPlayer.create(this ,Uri.parse(Environment.getExternalStorageDirectory().getPath() + music.get(index).getPath()));

        currentTrack.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override public void onCompletion(MediaPlayer mp) { forward(); } });

        seekBar.setMax(currentTrack.getDuration());
        seekBar.setProgress(0);
        totTime.setText(durationToTime(currentTrack.getDuration()));
        currentTrack.start();
        titletxt.setText(music.get(index).getTitle());
        play.setImageResource(R.drawable.pause);
        backgroundService.updateNoti();
    }

    public void random(View v){
        if(isRandom){
            isRandom = false;
            rnd.setText("false");
        }
        else{
            isRandom = true;
            rnd.setText("true");
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

    @Override
    protected void onStop() {
            super.onStop();
    }

    @Override
    protected  void onResume(){
        super.onResume();
    }
}