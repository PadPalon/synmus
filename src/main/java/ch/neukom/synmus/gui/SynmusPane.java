package ch.neukom.synmus.gui;

import ch.neukom.synmus.data.FileScanner;
import ch.neukom.synmus.data.beans.Album;
import ch.neukom.synmus.data.beans.Artist;
import ch.neukom.synmus.data.beans.Track;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

public class SynmusPane extends StackPane implements Observer {

    private Set<Track> tracks;

    private SimpleBooleanProperty scanning = new SimpleBooleanProperty(false);
    private ObservableList<Track> trackData = FXCollections.observableArrayList();
    private ObservableList<Artist> artistData = FXCollections.observableArrayList();
    private ObservableList<Album> albumData = FXCollections.observableArrayList();

    public SynmusPane(Stage stage) {
        this.setAlignment(Pos.CENTER);
        this.setPadding(new Insets(25, 25, 25, 25));

        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Select music root");

        Button selectButton = new Button("Select music root");
        selectButton.setOnAction(event -> selectRoot(stage, chooser));
        scanning.addListener(event -> selectButton.setDisable(scanning.get()));

        ListView<Artist> artistList = new ListView<>();
        artistList.setItems(artistData);
        artistList.setMaxHeight(100);
        artistList.setCellFactory(params -> new ListCell<Artist>() {
            @Override
            protected void updateItem(Artist item, boolean empty) {
                super.updateItem(item, empty);
                if(item != null) {
                    setText(String.format("%s (%s)", item.getName(), item.getTracks().size()));
                }
            }
        });
        Label artistLabel = new Label("Artists", artistList);

        ListView<Album> albumList = new ListView<>();
        albumList.setMaxHeight(100);
        albumList.setItems(albumData);
        albumList.setCellFactory(params -> new ListCell<Album>() {
            @Override
            protected void updateItem(Album item, boolean empty) {
                super.updateItem(item, empty);
                if(item != null) {
                    setText(String.format("%s (%s)", item.getName(), item.getTracks().size()));
                }
            }
        });
        Label albumLabel = new Label("Albums", albumList);

        ListView<Track> trackList = new ListView<>();
        trackList.setItems(trackData);
        trackList.setMaxHeight(100);
        trackList.setCellFactory(params -> new ListCell<Track>() {
            @Override
            protected void updateItem(Track item, boolean empty) {
                super.updateItem(item, empty);
                if(item != null) {
                    setText(item.getHumanReadable());
                }
            }
        });
        Label trackLabel = new Label("Tracks", trackList);

        MultipleSelectionModel<Artist> artistSelection = artistList.getSelectionModel();
        artistSelection.setSelectionMode(SelectionMode.MULTIPLE);
        MultipleSelectionModel<Album> albumSelection = albumList.getSelectionModel();
        albumSelection.setSelectionMode(SelectionMode.MULTIPLE);
        MultipleSelectionModel<Track> trackSelection = trackList.getSelectionModel();
        trackSelection.setSelectionMode(SelectionMode.MULTIPLE);

        artistSelection.selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            trackSelection.clearSelection();
            artistSelection.getSelectedItems().stream().flatMap(artist -> artist.getTracks().stream()).forEach(trackSelection::select);
            albumSelection.getSelectedItems().stream().flatMap(album -> album.getTracks().stream()).forEach(trackSelection::select);
        });
        albumSelection.selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            trackSelection.clearSelection();
            artistSelection.getSelectedItems().stream().flatMap(artist -> artist.getTracks().stream()).forEach(trackSelection::select);
            albumSelection.getSelectedItems().stream().flatMap(album -> album.getTracks().stream()).forEach(trackSelection::select);
        });

        VBox box = new VBox(selectButton, artistLabel, artistList, albumLabel, albumList, trackLabel, trackList);
        box.setAlignment(Pos.CENTER);
        this.getChildren().add(box);
    }

    @Override
    public void update(Observable o, Object arg) {
        Platform.runLater(() -> {
            trackData.add((Track) arg);
            FXCollections.sort(trackData);
        });
    }

    private void selectRoot(Stage stage, DirectoryChooser chooser) {
        File file = chooser.showDialog(stage);
        if (!scanning.get() && file != null && file.isDirectory()) {
            scanRoot(file);
        }
    }

    private void scanRoot(File file) {
        scanning.set(true);

        trackData.clear();
        artistData.clear();
        albumData.clear();

        ExecutorService executor = Executors.newCachedThreadPool();
        FileScanner scanner = new FileScanner(file);
        scanner.addObserver(this);
        Future<Set<Track>> scannerFuture = executor.submit(scanner);

        new Thread(() -> {
            try {
                tracks = scannerFuture.get();
                albumData.addAll(gatherAlbumData());
                artistData.addAll(gatherArtistData());
                scanning.set(false);
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }).start();

        executor.shutdown();
    }

    private List<Album> gatherAlbumData() {
        return tracks.stream()
                .map(Track::getAlbum)
                .distinct()
                .collect(Collectors.toList());
    }

    private List<Artist> gatherArtistData() {
        return albumData.stream()
                .map(Album::getArtist)
                .distinct()
                .collect(Collectors.toList());
    }
}
