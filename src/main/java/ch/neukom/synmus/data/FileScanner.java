package ch.neukom.synmus.data;

import ch.neukom.synmus.data.beans.Track;
import ch.neukom.synmus.data.tagging.TagReader;

import java.io.File;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

public class FileScanner extends Observable implements Callable<Set<Track>> {
    private final File rootFile;
    private final TagReader tagReader;

    public FileScanner(File rootFile) {
        this.rootFile = rootFile;
        this.tagReader = new TagReader();
    }

    @Override
    public Set<Track> call() throws Exception {
        return scanFile(rootFile);
    }

    private Set<Track> scanFile(File file) {
        Set<Track> fileData = new TreeSet<>();
        if(file.isFile()) {
            Track track = tagReader.readFile(file);
            if(track != null) {
                setChanged();
                notifyObservers(track);
                clearChanged();
                fileData.add(track);
            }
        } else if (file.isDirectory()) {
            Set<Track> directoryData = Arrays.stream(file.listFiles())
                    .flatMap(childFile -> scanFile(childFile).stream())
                    .collect(Collectors.toSet());
            fileData.addAll(directoryData);
        }
        return fileData;
    }
}
