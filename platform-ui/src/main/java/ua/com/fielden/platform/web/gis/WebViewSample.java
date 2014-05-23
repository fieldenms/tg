package ua.com.fielden.platform.web.gis;

import java.io.IOException;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

public class WebViewSample extends Application {
    @Override
    public void start(final Stage stage) throws Exception {
	final StackPane root = new StackPane();

	final WebView view = new WebView();
	final WebEngine engine = view.getEngine();
	engine.load(GisViewPanel2.class.getResource("map.html").toString());
	root.getChildren().add(view);
	final Scene scene = new Scene(root, 800, 600);
	stage.setScene(scene);
	stage.show();
    }

    public static void main(final String[] args) throws IOException {
	Application.launch(args);
    }
}