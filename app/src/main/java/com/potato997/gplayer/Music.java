package com.potato997.gplayer;

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

    public Music (MediaPlayer track, String artist, String title, String album, String cover, long albumId){
        this.track = track;
        this.artist = artist;
        this.title = title;
        this.album = album;
        this.cover = cover;
        this.albumId = albumId;
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

}
