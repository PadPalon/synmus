package ch.neukom.synmus.data.tagging;

import ch.neukom.synmus.data.beans.*;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.TagException;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class TagReader {
    private final Map<String, Album> albumCollection = new HashMap<>();
    private final Map<String, Artist> artistCollection = new HashMap<>();

    @Nullable
    public Track readFile(File file) {
        AudioFile audioFile;
        try {
            audioFile = AudioFileIO.read(file);
            Tag tag = audioFile.getTag();

            String artistName = "";
            String albumName = "";
            if(tag != null) {
                artistName = tag.getFirst(FieldKey.ARTIST);
                albumName = tag.getFirst(FieldKey.ALBUM);
            }

            Artist artist = artistCollection.computeIfAbsent(artistName, Artist::new);
            Album album = albumCollection.computeIfAbsent(albumName, key -> new Album(key, artist));

            Track track = new Track(tag, file, album);
            artist.addTrack(track);
            album.addTrack(track);
            return track;
        } catch (CannotReadException e) {
            return null;
        } catch (IOException | InvalidAudioFrameException | TagException | ReadOnlyFileException e) {
            throw new RuntimeException(e);
        }
    }
}
