package ch.neukom.synmus;

import ch.neukom.synmus.gui.SynmusPane;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    public static final int WIDTH = 1000;
    public static final int HEIGHT = 800;

    @Override
    public void start(Stage stage) throws Exception {
        stage.setTitle("Synmus");
        stage.setWidth(WIDTH);
        stage.setHeight(HEIGHT);
        SynmusPane synmus = new SynmusPane(stage);
        stage.setScene(new Scene(synmus));
        stage.show();
    }
}
