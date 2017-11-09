package com.potato997.gplayer;

import android.net.Uri;

/**
 * Created by Laptop01 on 09/10/2017.
 */

public class Music {
    private String path;
    private String artist;
    private String title;
    private String album;
    private long albumId;
    private Uri uri;

    public Music (String path , String artist, String title, String album, long albumId, Uri uri){
        this.path = path;
        this.artist = artist;
        this.title = title;
        this.album = album;
        this.albumId = albumId;
        this.uri = uri;
    }

    public String getPath(){ return path; }

    public String getArtist() {
        return artist;
    }

    public String getTitle() {
        return title;
    }

    public String getAlbum() {
        return album;
    }


    public long getAlbumId(){
        return albumId;
    }

    public Uri getUri() { return uri; }
}
