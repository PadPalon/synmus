package ch.neukom.synmus.data.beans;

import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;

import java.io.File;

public class Track implements Comparable<Track> {
    private final Tag tag;
    private final File location;
    private final Album album;

    public Track(Tag tag, File location, Album album) {
        this.tag = tag;
        this.location = location;
        this.album = album;
    }

    public Tag getTag() {
        return tag;
    }

    public File getLocation() {
        return location;
    }

    public String getTitle() {
        if(tag != null) {
            return tag.getFirst(FieldKey.TITLE);
        } else {
            return "";
        }
    }

    public Album getAlbum() {
        return album;
    }

    public Artist getArtist() {
        return album.getArtist();
    }

    public String getHumanReadable() {
        if(tag != null) {
            return String.format("%s - %s - %s", tag.getFirst(FieldKey.TITLE), tag.getFirst(FieldKey.ALBUM), tag.getFirst(FieldKey.ARTIST));
        } else {
            return toString();
        }
    }

    @Override
    public String toString() {
        return getLocation().getAbsolutePath();
    }

    @Override
    public int compareTo(Track o) {
        return getAlbum().equals(o.getAlbum()) ? getTitle().compareTo(o.getTitle()) : getAlbum().compareTo(o.getAlbum());
    }
}
