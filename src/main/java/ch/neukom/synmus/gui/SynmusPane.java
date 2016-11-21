package ch.neukom.synmus.gui;

import ch.neukom.synmus.data.FileScanner;
import ch.neukom.synmus.data.beans.Album;
import ch.neukom.synmus.data.beans.Artist;
import ch.neukom.synmus.data.beans.Track;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
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
    private ObservableList<Track> trackData = FXCollections.observableArrayList(param -> new javafx.beans.Observable[]{param.getTaggedForSync()});
    private ObservableList<Artist> artistData = FXCollections.observableArrayList();
    private ObservableList<Album> albumData = FXCollections.observableArrayList();

    public SynmusPane(Stage stage) {
        setPadding(new Insets(0, 10, 0, 10));

        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Select music root");


        ListView<Artist> artistList = createArtistList(stage);
        Label artistLabel = new Label("Artists");

        ListView<Album> albumList = createAlbumList(stage);
        Label albumLabel = new Label("Albums");

        ListView<Track> trackList = createTrackList(stage);
        Label trackLabel = new Label("Tracks");

        Button selectButton = new Button("Select music root");
        selectButton.setPrefWidth(stage.getWidth() / 3);
        Button tagButton = new Button("Tag for sync");
        tagButton.setPrefWidth(stage.getWidth() / 3);
        Button untagButton = new Button("Untag for sync");
        untagButton.setPrefWidth(stage.getWidth() / 3);

        MultipleSelectionModel<Artist> artistSelection = artistList.getSelectionModel();
        artistSelection.setSelectionMode(SelectionMode.MULTIPLE);
        MultipleSelectionModel<Album> albumSelection = albumList.getSelectionModel();
        albumSelection.setSelectionMode(SelectionMode.MULTIPLE);
        MultipleSelectionModel<Track> trackSelection = trackList.getSelectionModel();
        trackSelection.setSelectionMode(SelectionMode.MULTIPLE);

        artistSelection.selectedItemProperty().addListener(getSelectionListener(artistSelection, albumSelection, trackSelection));
        albumSelection.selectedItemProperty().addListener(getSelectionListener(artistSelection, albumSelection, trackSelection));

        tagButton.setOnAction(event -> trackSelection.getSelectedItems().forEach(track -> track.setTaggedForSync(true)));
        untagButton.setOnAction(event -> trackSelection.getSelectedItems().forEach(track -> track.setTaggedForSync(false)));

        selectButton.setOnAction(event -> selectRoot(stage, chooser));
        scanning.addListener(event -> selectButton.setDisable(scanning.get()));

        VBox artist = new VBox(artistLabel, artistList);
        VBox album = new VBox(albumLabel, albumList);
        HBox artistAlbum = new HBox(artist, album);
        VBox track = new VBox(trackLabel, trackList);
        HBox buttons = new HBox(selectButton, tagButton, untagButton);
        VBox container = new VBox(artistAlbum, track, buttons);

        artistAlbum.setSpacing(10);
        container.setSpacing(10);

        this.getChildren().add(container);
    }

    private ListView<Artist> createArtistList(Stage stage) {
        ListView<Artist> artistList = new ListView<>();
        artistList.setItems(artistData);
        artistList.setPrefWidth(stage.getWidth() / 2);
        artistList.setCellFactory(params -> new ListCell<Artist>() {
            @Override
            protected void updateItem(Artist item, boolean empty) {
                super.updateItem(item, empty);
                if(item != null) {
                    Platform.runLater(() -> setText(String.format("%s (%s)", item.getName(), item.getTracks().size())));
                }
            }
        });
        return artistList;
    }

    private ListView<Album> createAlbumList(Stage stage) {
        ListView<Album> albumList = new ListView<>();
        albumList.setItems(albumData);
        albumList.setPrefWidth(stage.getWidth() / 2);
        albumList.setCellFactory(params -> new ListCell<Album>() {
            @Override
            protected void updateItem(Album item, boolean empty) {
                super.updateItem(item, empty);
                if(item != null) {
                    Platform.runLater(() -> setText(String.format("%s (%s)", item.getName(), item.getTracks().size())));
                }
            }
        });
        return albumList;
    }

    private ListView<Track> createTrackList(Stage stage) {
        ListView<Track> trackList = new ListView<>();
        trackList.setItems(trackData);
        trackList.setPrefWidth(stage.getWidth());
        trackList.setCellFactory(params -> new ListCell<Track>() {
            @Override
            protected void updateItem(Track item, boolean empty) {
                super.updateItem(item, empty);
                if(item != null) {
                    Platform.runLater(() -> setText(item.getHumanReadable()));
                }
            }
        });
        return trackList;
    }

    private <T> ChangeListener<T> getSelectionListener(MultipleSelectionModel<Artist> artistSelection,
                                                       MultipleSelectionModel<Album> albumSelection,
                                                       MultipleSelectionModel<Track> trackSelection) {
        return (observable, oldValue, newValue) -> {
            trackSelection.clearSelection();
            artistSelection.getSelectedItems().stream().flatMap(artist -> artist.getTracks().stream()).forEach(trackSelection::select);
            albumSelection.getSelectedItems().stream().flatMap(album -> album.getTracks().stream()).forEach(trackSelection::select);
        };
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

    @Override
    public void update(Observable o, Object arg) {
        Platform.runLater(() -> {
            trackData.add((Track) arg);
            FXCollections.sort(trackData);
        });
    }
}
