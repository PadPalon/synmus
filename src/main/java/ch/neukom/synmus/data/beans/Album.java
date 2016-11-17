package ch.neukom.synmus.data.beans;

import java.util.ArrayList;
import java.util.List;

public class Album implements Comparable<Album> {
    private final String name;
    private final Artist artist;

    private final List<Track> tracks = new ArrayList<>();

    public Album(String name, Artist artist) {
        this.name = name;
        this.artist = artist;
    }

    public String getName() {
        return name;
    }

    public Artist getArtist() {
        return artist;
    }

    @Override
    public int compareTo(Album o) {
        return getArtist().equals(o.getArtist()) ? getName().compareTo(o.getName()) : getArtist().compareTo(o.getArtist());
    }

    @Override
    public String toString() {
        return getName();
    }

    public void addTrack(Track track) {
        tracks.add(track);
    }

    public List<Track> getTracks() {
        return tracks;
    }
}
