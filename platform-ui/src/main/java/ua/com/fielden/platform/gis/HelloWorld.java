package ua.com.fielden.platform.gis;

import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class HelloWorld extends Application {

    @Override
    public void start(final Stage stage) {
        final Scene scene = new Scene(new Group(new Text(25, 25, "Hello World!")));

        stage.setTitle("Welcome to JavaFX!");
        stage.setScene(scene);
        stage.sizeToScene();
        stage.show();
    }

    public static void main(final String[] args) {
        Application.launch(args);
    }
}
