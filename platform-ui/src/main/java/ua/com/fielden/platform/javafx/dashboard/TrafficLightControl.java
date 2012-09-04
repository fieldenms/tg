package ua.com.fielden.platform.javafx.dashboard;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Skin;
import javafx.scene.effect.Bloom;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.InnerShadow;
import javafx.scene.effect.Light.Distant;
import javafx.scene.effect.Lighting;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import ua.com.fielden.platform.pagination.IPage;
import ua.com.fielden.platform.selectioncheckbox.SelectionCheckBoxPanel.IAction;

public class TrafficLightControl extends Application {

    public static Group createTrafficLight(final IPage page) {
	final Image image = new Image("trafficlight.jpg");
	final ImageView imageView = new ImageView(image);

	final Button redButton = createButton("red-texture.jpg", true, 34, 36, Color.RED);
	final Button yellowButton = createButton("yellow-texture.jpg", false, 104, 36, Color.YELLOW);
	final Button greenButton = createButton("green-texture.jpg", false, 174, 36, Color.GREEN);
	final Group group = new Group(imageView, redButton, yellowButton, greenButton);
	//group.getStylesheets().add(this.getClass().getResource("trafficlight.css").toExternalForm());
	return group;
    }


    private static Node createNode(final String textureImageFileName, final boolean isLighting) {
	final Circle background = new Circle(0, 0, 27);
	background.setFill(new ImagePattern(new Image(textureImageFileName), 0, 0, 9, 10, false));

	final Distant light = new Distant();
	light.setAzimuth(-315.0f);// -135.0f);
	final Lighting l = new Lighting();
	l.setLight(light);
	l.setSurfaceScale(5.0f);
	if (!isLighting) {
	    background.setEffect(l);
	} else {
	    final Bloom bloom = new Bloom();
	    bloom.setInput(l);
	    bloom.setThreshold(0.0);
	    background.setEffect(bloom);
	}

//        final DropShadow ds = new DropShadow();
//        ds.setInput(l);
//
//        ds.setColor(Color.rgb(254, 235, 66, 0.3));
//        ds.setOffsetX(5);
//        ds.setOffsetY(5);
//        ds.setRadius(5);
//        ds.setSpread(0.2);
//        background.setEffect(ds);
	return background;
    }

    @Override
    public void start(final Stage stage) {
//	final Button redButton = createButton("red-texture.jpg", false, 50, 60);
//	final Button yellowButton = createButton("yellow-texture.jpg", false, 150, 60);
//	final Button greenButton = createButton("green-texture.jpg", false, 250, 60);
	final Button redLightButton = createButton("red-texture.jpg", true, 50, 160, Color.RED);
	final Button yellowLightButton = createButton("yellow-texture.jpg", true, 150, 160, Color.YELLOW);
	final Button greenLightButton = createButton("green-texture.jpg", true, 250, 160, Color.GREEN);


	final Circle background = new Circle(200, 200, 40);
	background.setFill(new ImagePattern(new Image("yellow-texture.jpg"), 0, 0, 9, 10, false));

//	final Group trafficLight = createTrafficLight(null);
//	trafficLight.setTranslateX(0);
//	trafficLight.setTranslateY(0);
	final IAction a = new IAction() {
	    @Override
	    public void action() {
	    }
	};

	final TrafficLightsModel model = new TrafficLightsModel();
	model.getGreenLightingModel().setLighting(true);
	final TrafficLights trafficLights = new TrafficLights(model, a, a, a);
	trafficLights.setTranslateX(0);
	trafficLights.setTranslateY(0);

	final Group group = new Group(/*trafficLight, redButton, yellowButton, greenButton, */redLightButton, yellowLightButton, greenLightButton, trafficLights);
	//group.getStylesheets().add(this.getClass().getResource("trafficlight.css").toExternalForm());
        final Scene scene = new Scene(group, 600, 450);
        //scene.setFill(Color.DARKSLATEBLUE);

        stage.setTitle("Welcome to JavaFX!");
        stage.setScene(scene);
        stage.sizeToScene();
        stage.show();
    }

    private static Button createButton(final String textureImageFileName, final boolean isLighting, final double x, final double y, final Color selectionColor) {
	final Button redButton = new Button();
	redButton.setOnMousePressed(new EventHandler<MouseEvent>() {
	    @Override
	    public void handle(final MouseEvent event) {
		System.out.println("==========================");
		// redButton.getSkin().getNode().setEffect(new DropShadow(BlurType.GAUSSIAN, Color.BROWN, 15, 0,0,0));
		redButton.layoutXProperty().set(redButton.layoutXProperty().get() + 1); // add(10);
	    }
	});
	redButton.setOnMouseReleased(new EventHandler<MouseEvent>() {
	    @Override
	    public void handle(final MouseEvent event) {
		System.out.println("-----------------------");
		// redButton.getSkin().getNode().setEffect(new DropShadow(BlurType.GAUSSIAN, Color.BROWN, 15, 0,0,0));
		redButton.layoutXProperty().set(redButton.layoutXProperty().get() - 1); // add(10);
	    }
	});
	redButton.setOnMouseEntered(new EventHandler<MouseEvent>() {
	    @Override
	    public void handle(final MouseEvent event) {
		System.out.println("++++++++++++++++");
		redButton./*getSkin().getNode().*/setEffect(new InnerShadow(BlurType.GAUSSIAN, selectionColor, 15, 0,0,0));
	    }
	});
	redButton.setOnMouseExited(new EventHandler<MouseEvent>() {
	    @Override
	    public void handle(final MouseEvent event) {
		System.out.println("++++++++++++++++");
		redButton./*getSkin().getNode().*/setEffect(null);
	    }
	});
	redButton.setSkin(new Skin<Button>() {
	    @Override
	    public void dispose() {
	    }

	    @Override
	    public Node getNode() {
		return createNode(textureImageFileName, isLighting);
	    }

	    @Override
	    public Button getSkinnable() {
		return redButton;
	    }
	});
	redButton.setTranslateX(x);
	redButton.setTranslateY(y);
	return redButton;
    }

    public static void main(final String[] args) {
        Application.launch(args);
    }
}
