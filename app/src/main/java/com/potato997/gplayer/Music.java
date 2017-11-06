package com.potato997.gplayer;

/**
 * Created by Laptop01 on 09/10/2017.
 */

public class Music {
    private String path;
    private String artist;
    private String title;
    private String album;
    private String cover;
    private long albumId;

    public Music (String path , String artist, String title, String album, String cover, long albumId){
        this.path = path;
        this.artist = artist;
        this.title = title;
        this.album = album;
        this.cover = cover;
        this.albumId = albumId;
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

    public String getCover(){
        return cover;
    }

    public long getAlbumId(){
        return albumId;
    }


}
