package ua.com.fielden.platform.gis;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

public class HelloWorldMain extends Application {
//    private Image image = new Image("file:marker.png"); // src/ua/com/fielden/platform/gis/
    
    /**
     * @param args -- the command line arguments
     */
    public static void main(final String[] args) {
	Application.launch(args);
    }

    @Override
    public void start(final Stage primaryStage) {
	primaryStage.setTitle("Hello World (stage title)");
	final Group root = new Group();
	final Scene scene = new Scene(root, 700, 450);
	final Button btn = new Button();
	btn.setLayoutX(100);
	btn.setLayoutY(80);
	btn.setText("Hello World (button title)");
	btn.setOnAction(new EventHandler<ActionEvent>() {
	    public void handle(final ActionEvent event) {
		System.out.println("Hello World (sysout)");
	    }
	});
	root.getChildren().add(btn);
	
        final ImageView iv1 = new ImageView(new Image("file:marker.png"));
//        iv1.setX(20);
//        iv1.setY(20);
        final HBox hBox = new HBox();
        
        hBox.getChildren().add(iv1);
        root.getChildren().add(hBox);
	
	primaryStage.setScene(scene);
	primaryStage.show();
    }
}
