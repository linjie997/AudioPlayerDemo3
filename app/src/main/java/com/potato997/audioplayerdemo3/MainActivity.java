package com.potato997.audioplayerdemo3;

import android.Manifest;
import android.content.ContentUris;
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
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    List<Music> music = new ArrayList<>();
    MediaPlayer track;
    String title;
    String path;
    String artist;
    String album;
    String cover;
    long albumId;
    ImageView album_art;
    TextView txtCount;
    TextView titletxt;
    TextView currentTime;
    TextView totTime;
    int index;
    ImageButton play;
    ImageButton back;
    ImageButton forward;
    Button rnd;
    String[] dir;
    boolean isRandom = false;
    View view;
    SeekBar seekBar;

    final public static Uri sArtworkUri = Uri
            .parse("content://media/external/audio/albumart");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[] { Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE }, 0);
            }
        }

        setContentView(R.layout.activity_main);

        view = findViewById(android.R.id.content);
        index = 0;
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

        totTime.setText(durationToTime(music.get(index).getTrack().getDuration()));

        seekBar.setMax(music.get(index).getTrack().getDuration());

        final Handler handler = new Handler();
        Runnable task = new Runnable() {
            @Override
            public void run() {
                if(music.get(index).getTrack().isPlaying()){
                    currentTime.setText(durationToTime(music.get(index).getTrack().getCurrentPosition()));
                    seekBar.setProgress(music.get(index).getTrack().getCurrentPosition());
                }
                handler.postDelayed(this, 1000);
            }
        };
        handler.post(task);

        MyNotification nPanel = new MyNotification(this);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(fromUser)
                    music.get(index).getTrack().seekTo(progress);

                currentTime.setText(durationToTime(music.get(index).getTrack().getCurrentPosition()));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    public void loadAudio() {
        ArrayList<HashMap<String, String>> musicList = null;
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
                track = MediaPlayer.create(MainActivity.this, Uri.parse(Environment.getExternalStorageDirectory().getPath() + dir[1]));
                track.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override public void onCompletion(MediaPlayer mp) { forward(); } });

                music.add(new Music(track, artist, title, album, cover, albumId));
                HashMap<String, String> mp = new HashMap<String, String>();
                mp.put("title", title);
                mp.put("album_id", Long.toString(albumId));
                musicList.add(mp);
            }

            ListAdapter adapter = new SimpleAdapter(MainActivity.this, musicList, R.layout.view_music_list, new String[]{"title"},
                    new int[]{R.id.track_title});

            ListView listView = (ListView) findViewById(R.id.list);
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    music.get(index).getTrack().pause();
                    music.get(index).getTrack().seekTo(0);
                    index = position;
                    play.setImageResource(R.drawable.pause);
                    changeAudio();
                }
            });
            listView.setAdapter(adapter);

            titletxt.setText("Titolo: " + music.get(index).getTitle() + "\nArtista: " + music.get(index).getArtist() + "\nAlbum: " + music.get(index).getAlbum());
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_LONG).show();
        }
    }

    public void changeAlbumArt(){
        final Uri uri = ContentUris.withAppendedId(sArtworkUri,
                music.get(index).getAlbumId());

        Picasso.with(this)
                .load(uri)
                .error(R.drawable.no_art)
                .into(album_art);
    }

    public void setGesture(){
        view.setOnTouchListener(new OnSwipeTouchListener(MainActivity.this) {

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
        try{
            if(music.get(index).getTrack().isPlaying()){
                music.get(index).getTrack().pause();
                play.setImageResource(R.drawable.play);
            }
            else{
                music.get(index).getTrack().start();
                play.setImageResource(R.drawable.pause);
            }
        }catch(Exception e){
            Toast.makeText(getApplicationContext(), "PLAY "+e.toString(), Toast.LENGTH_LONG).show();
        }
    }

    public void forward(){
        try{
            music.get(index).getTrack().pause();
            music.get(index).getTrack().seekTo(0);
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
        catch(Exception e){
            Toast.makeText(getApplicationContext(), "FORWARD "+e.toString(), Toast.LENGTH_LONG).show();
        }
    }

    public void back(){
        try{
            music.get(index).getTrack().pause();
            music.get(index).getTrack().seekTo(0);
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
        catch(Exception e){
            Toast.makeText(getApplicationContext(), "BACK "+e.toString(), Toast.LENGTH_LONG).show();
        }
    }

    public void changeAudio(){
        try {
            seekBar.setMax(music.get(index).getTrack().getDuration());
            seekBar.setProgress(0);
            totTime.setText(durationToTime(music.get(index).getTrack().getDuration()));
            music.get(index).getTrack().start();
            titletxt.setText("Titolo: " + music.get(index).getTitle() + "\nArtista: " + music.get(index).getArtist() + "\nAlbum: " + music.get(index).getAlbum());

        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), "CHANGE "+e.toString(), Toast.LENGTH_LONG).show();
        }
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

    public String durationToTime(long milliseconds) {
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