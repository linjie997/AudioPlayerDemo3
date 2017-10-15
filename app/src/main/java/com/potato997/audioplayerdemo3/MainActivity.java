package com.potato997.audioplayerdemo3;

import android.Manifest;
import android.content.ContentUris;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
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
    int index;
    ImageButton play;
    Button back;
    Button forward;
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
        setContentView(R.layout.activity_main);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[] { Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE }, 0);
            }
        }

        view = findViewById(android.R.id.content);
        index = 0;
        txtCount = (TextView) findViewById(R.id.audioCount);
        titletxt = (TextView) findViewById(R.id.audioTitle);
        play = (ImageButton) findViewById(R.id.play);
        back = (Button) findViewById(R.id.back);
        forward = (Button) findViewById(R.id.forward);
        album_art = (ImageView) findViewById(R.id.album_cover);
        rnd = (Button) findViewById(R.id.rnd);
        seekBar = (SeekBar) findViewById(R.id.seekBar);

        setGesture();

        try{

            Cursor cursor = getContentResolver().query(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    null,
                    null,
                    null,
                    null);

            cursor.moveToFirst();

            while (cursor.moveToNext()) {
                path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
                title = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
                artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
                album = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM));
                albumId = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID));

                if(path.endsWith(".mp3") || path.endsWith(".MP3") || path.endsWith(".flac") || path.endsWith(".FLAC") || path.endsWith("(.wav")|| path.endsWith(".WAW")
                        || path.endsWith(".ACC") || path.endsWith(".acc")){
                    dir = path.split("emulated/0");
                    track = MediaPlayer.create(MainActivity.this, Uri.parse(Environment.getExternalStorageDirectory().getPath()+ dir[1]));
                    music.add(new Music(track, artist, title, album, cover, albumId));

                    Uri uri = ContentUris.withAppendedId(sArtworkUri,
                            music.get(index).getAlbumId());
                        Picasso.with(this).load(uri).into(album_art);
                }
            }
            Collections.sort(music, new MyComparator());
            titletxt.setText("Titolo: " + music.get(index).getTitle() + "\nArtista: " + music.get(index).getArtist() + "\nAlbum: " + music.get(index).getAlbum());
        }
        catch(Exception e){
            Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_LONG).show();
        }
        txtCount.setText("\n" + (index+1) + "/" + music.size());

        seekBar.setMax(music.get(index).getTrack().getDuration());

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                music.get(index).getTrack().seekTo(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    public void setGesture(){
        view.setOnTouchListener(new OnSwipeTouchListener(MainActivity.this) {

            @Override
            public void onDoubleClick() {
                super.onDoubleClick();
                play(view);
            }

            @Override
            public void onSwipeLeft() {
                super.onSwipeLeft();
                back(view);
            }

            @Override
            public void onSwipeRight() {
                super.onSwipeLeft();
                forward(view);
            }
        });
    }

    public void play(View v){
        try{
            if(music.get(index).getTrack().isPlaying()){
                music.get(index).getTrack().pause();
                play.setImageResource(R.drawable.play);
            }
            else{
                music.get(index).getTrack().start();
                play.setImageResource(R.drawable.pause);
            }
            Uri uri = ContentUris.withAppendedId(sArtworkUri,
                    music.get(index).getAlbumId());

            Picasso.with(this).load(uri).into(album_art);
        }catch(Exception e){
            Toast.makeText(getApplicationContext(), "PLAY "+e.toString(), Toast.LENGTH_LONG).show();
        }
    }

    public void forward(View v){
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

    public void back(View v){
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
            music.get(index).getTrack().start();
            Uri uri = ContentUris.withAppendedId(sArtworkUri,
                    music.get(index).getAlbumId());
            Picasso.with(this).load(uri).into(album_art);

            if(album_art == null || album_art.getDrawable() == null){
                Picasso.with(this).load(R.drawable.no_art).into(album_art);
            }

            play.setImageResource(R.drawable.pause);
            txtCount.setText("\n" + (index+1) + "/" + music.size());
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
}