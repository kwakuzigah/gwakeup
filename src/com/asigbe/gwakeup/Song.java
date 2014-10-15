package com.asigbe.gwakeup;

/**
 * This class represents a song with his title, album and artist.
 * 
 * @author Delali Zigah
 */
public final class Song {

    /** title of the song **/
    public final String title;
    /** album of the song **/
    public final String album;
    /** artist of the song **/
    public final String artist;

    /**
     * Creates a new song.
     */
    public Song(String album, String artist, String title) {
	super();
	this.album = (album == null) ? "" : album;
	this.artist = (artist == null) ? "" : artist;
	this.title = (title == null) ? "" : title;
    }

    /**
     * Returns a formatted text description of the song.
     */
    public CharSequence getformattedDescription() {
	return this.artist + " " + this.album + "\n" + this.title;
    }
}
