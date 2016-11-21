package ch.neukom.synmus.data.beans;

import javafx.beans.property.SimpleBooleanProperty;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;

import java.io.File;
import java.io.Serializable;

public class Track implements Comparable<Track>, Serializable {
    private final transient Tag tag;
    private final File location;
    private final transient Album album;

    private SimpleBooleanProperty taggedForSync = new SimpleBooleanProperty();

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
        String humanReadable;
        if(tag != null) {
            humanReadable = String.format("%s - %s - %s", tag.getFirst(FieldKey.TITLE), tag.getFirst(FieldKey.ALBUM), tag.getFirst(FieldKey.ARTIST));
        } else {
            humanReadable = toString();
        }

        if(isTaggedForSync()) {
            humanReadable = "* ".concat(humanReadable);
        }

        return humanReadable;
    }

    public SimpleBooleanProperty getTaggedForSync() {
        return taggedForSync;
    }

    public Boolean isTaggedForSync() {
        return taggedForSync.get();
    }

    public void setTaggedForSync(Boolean taggedForSync) {
        this.taggedForSync.set(taggedForSync);
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
