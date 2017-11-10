package com.potato997.gplayer;

import android.net.Uri;

/**
 * Created by Laptop01 on 09/10/2017.
 */

public class Music {
    private String artist;
    private String title;
    private String album;
    private long albumId;
    private long id;
    private Uri albumUri;

    public Music (String artist, String title, String album, long albumId, long id, Uri albumUri){
        this.artist = artist;
        this.title = title;
        this.album = album;
        this.albumId = albumId;
        this.id = id;
        this.albumUri = albumUri;
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

    public long getId() { return id; }

    public long getAlbumId(){
        return albumId;
    }

    public Uri getAlbumUri() { return albumUri; }
}
