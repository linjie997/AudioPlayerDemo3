package com.potato997.audioplayerdemo3;

import android.graphics.Bitmap;
import android.media.MediaPlayer;

/**
 * Created by Laptop01 on 09/10/2017.
 */

public class Music {
    private MediaPlayer track;
    private String artist;
    private String title;
    private String album;
    private String cover;
    private long albumId;
    private Bitmap bitmap;

    public Music (MediaPlayer track, String artist, String title, String album, String cover, long albumId, Bitmap bitmap){
        this.track = track;
        this.artist = artist;
        this.title = title;
        this.album = album;
        this.cover = cover;
        this.albumId = albumId;
        this.bitmap = bitmap;
    }

    public MediaPlayer getTrack() {
        return track;
    }

    public String getArtist() {
        return artist;
    }

    public String getTitle() {
        return title;
    }

    public String getAlbum() {
        return album;
    }

    public String getCover(){
        return cover;
    }

    public long getAlbumId(){
        return albumId;
    }

    public Bitmap getBitmap() { return bitmap; }
}
