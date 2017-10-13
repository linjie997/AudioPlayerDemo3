package com.potato997.audioplayerdemo3;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.io.FileDescriptor;
import java.util.ArrayList;
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
    String[] dir;
    View view;

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

                if(path.endsWith(".mp3") || path.endsWith(".MP3")){
                    dir = path.split("emulated/0");
                    track = MediaPlayer.create(MainActivity.this, Uri.parse(Environment.getExternalStorageDirectory().getPath()+ dir[1]));
                    music.add(new Music(track, artist, title, album, cover, albumId));

                    Uri uri = ContentUris.withAppendedId(sArtworkUri,
                            music.get(index).getAlbumId());

                    Picasso.with(this).load(uri).into(album_art);
                }
            }
            titletxt.setText("Titolo: " + music.get(index).getTitle() + "\nArtista: " + music.get(index).getArtist() + "\nAlbum: " + music.get(index).getAlbum());
        }
        catch(Exception e){
            Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_LONG).show();
        }
        txtCount.setText("\n" + (index+1) + "/" + music.size());
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
                //changeAudio();
                music.get(index).getTrack().start();
                play.setImageResource(R.drawable.pause);
            }
            Uri uri = ContentUris.withAppendedId(sArtworkUri,
                    music.get(index).getAlbumId());

            Picasso.with(this).load(uri).into(album_art);
            titletxt.setText("Titolo: " + music.get(index).getTitle() + "\nArtista: " + music.get(index).getArtist() + "\nAlbum: " + music.get(index).getAlbum());
        }catch(Exception e){
            Toast.makeText(getApplicationContext(), "PLAY "+e.toString(), Toast.LENGTH_LONG).show();
        }
    }

    public void forward(View v){
        try{
            music.get(index).getTrack().pause();
            music.get(index).getTrack().seekTo(0);
            if(index < (music.size()-1)){
                index++;
            }else{
                index = 0;
            }
            changeAudio();
            titletxt.setText("Titolo: " + music.get(index).getTitle() + "\nArtista: " + music.get(index).getArtist() + "\nAlbum: " + music.get(index).getAlbum());
        }
        catch(Exception e){
            Toast.makeText(getApplicationContext(), "FORWARD "+e.toString(), Toast.LENGTH_LONG).show();
        }
    }

    public void back(View v){
        try{
            music.get(index).getTrack().pause();
            music.get(index).getTrack().seekTo(0);
            if(index > 0){
                index--;
            }
            else{
                index = music.size()-1;
            }
            changeAudio();
            titletxt.setText("Titolo: " + music.get(index).getTitle() + "\nArtista: " + music.get(index).getArtist() + "\nAlbum: " + music.get(index).getAlbum());
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
            play.setImageResource(R.drawable.pause);
            txtCount.setText("\n" + (index+1) + "/" + music.size());
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), "CHANGE "+e.toString(), Toast.LENGTH_LONG).show();
        }
    }

    public void random(View v){
        music.get(index).getTrack().pause();
        music.get(index).getTrack().seekTo(0);
        index = (int)(Math.random()*music.size());
        titletxt.setText("Titolo: " + music.get(index).getTitle() + "\nArtista: " + music.get(index).getArtist() + "\nAlbum: " + music.get(index).getAlbum());
        changeAudio();
    }
}