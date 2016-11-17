package ch.neukom.synmus.data.beans;

import java.util.ArrayList;
import java.util.List;

public class Artist implements Comparable<Artist> {
    private final String name;

    private final List<Track> tracks = new ArrayList<>();

    public Artist(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public int compareTo(Artist o) {
        return getName().compareTo(o.getName());
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
