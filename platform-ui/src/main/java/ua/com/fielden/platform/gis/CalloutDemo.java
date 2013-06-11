package ua.com.fielden.platform.gis;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.VPos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import ua.com.fielden.platform.javafx.gis.Callout;

public class CalloutDemo extends Application {
//    private Image image = new Image("file:marker.png"); // src/ua/com/fielden/platform/gis/
    private Callout currentCallout;

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
	btn.setLayoutX(400);
	btn.setLayoutY(380);
	btn.setText("->");
	btn.setOnAction(new EventHandler<ActionEvent>() {
	    public void handle(final ActionEvent event) {
		System.out.println("Hello World (sysout)");
	    }
	});
	root.getChildren().add(btn);


	// add callout:
	final Group infoNode = new Group();

	final Text textNode = new Text("BC8866AA\ngfdsufhgui\nhfruefyuierf\ngfuiwefui\nhfuirewfhiurew\nehfwuifhguiewf".trim());
	textNode.setFill(Color.BLACK);
	textNode.textOriginProperty().set(VPos.TOP);

	infoNode.getChildren().add(textNode);

	root.getChildren().add(currentCallout = new Callout(btn, infoNode, scene, root, new Runnable() {
	    @Override
	    public void run() {
		if (currentCallout != null) {
		    root.getChildren().remove(currentCallout);
		    currentCallout = null;
		}
	    }
	}));

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
