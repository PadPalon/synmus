package ch.neukom.synmus;

import ch.neukom.synmus.gui.SynmusPane;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        stage.setTitle("Synmus");

        SynmusPane synmus = new SynmusPane(stage);
        stage.setScene(new Scene(synmus, 600, 400));
        stage.show();
    }
}
