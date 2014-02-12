package ua.com.fielden.platform.javafx.dashboard2;

import java.io.File;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.MalformedURLException;

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
import javafx.scene.shape.Arc;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import ua.com.fielden.platform.pagination.IPage;
import ua.com.fielden.platform.types.Money;

public class DashboardItemNodeSpike extends Application {
    private final static String packageName = "trafficlights";

    private static Group createTrafficLight(final IPage page) throws MalformedURLException {

	final Image image = new Image(createUrl("trafficlight.jpg"));
	final ImageView imageView = new ImageView(image);


	final Button redButton = createButton("red-texture.jpg", true, 34, 36, Color.RED);
	final Button yellowButton = createButton("yellow-texture.jpg", false, 104, 36, Color.YELLOW);
	final Button greenButton = createButton("green-texture.jpg", false, 174, 36, Color.GREEN);
	final Group group = new Group(imageView, redButton, yellowButton, greenButton);
	//group.getStylesheets().add(this.getClass().getResource("trafficlight.css").toExternalForm());
	return group;
    }

    private static String createUrl(final String imageFileName) {
	try {
	    return new File("src/main/resources/" + packageName + "/" + imageFileName).toURI().toURL().toString();
	} catch (final MalformedURLException e) {
	    e.printStackTrace();
	    throw new RuntimeException(e);
	}
    }

    private static Node createNode(final String textureImageFileName, final boolean isLighting) throws MalformedURLException {
	final Circle background = new Circle(0, 0, 27);
	background.setFill(new ImagePattern(new Image(createUrl(textureImageFileName)), 0, 0, 9, 10, false));

	final Distant light = new Distant();
	// light.setAzimuth(-315.0f);
	//light.setAzimuth(-135.0f);
	light.setAzimuth(-115.0f);

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
    public void start(final Stage stage) throws MalformedURLException {
//	final Button redButton = createButton("red-texture.jpg", false, 50, 60);
//	final Button yellowButton = createButton("yellow-texture.jpg", false, 150, 60);
//	final Button greenButton = createButton("green-texture.jpg", false, 250, 60);
	final Button redLightButton = createButton("red-texture.jpg", true, 50, 160, Color.RED);
	final Button yellowLightButton = createButton("yellow-texture.jpg", true, 150, 160, Color.YELLOW);
	final Button greenLightButton = createButton("green-texture.jpg", true, 250, 160, Color.GREEN);

	final Circle background = new Circle(200, 200, 40);
	background.setFill(new ImagePattern(new Image(createUrl("yellow-texture.jpg")), 0, 0, 9, 10, false));

	final Group trafficLight = createTrafficLight(null);
	trafficLight.setTranslateX(150);
	trafficLight.setTranslateY(50);
	final Runnable a = new Runnable() {
	    @Override
	    public void run() {
	    }
	};

	final SentinelModel model = new SentinelModel("1", "2", "3");
	model.getGreenLightingModel().setCountAndMoney(new BigInteger("3"), new Money(new BigDecimal(0.0)));
	final SentinelView trafficLights = new SentinelView(model, a, a, a, 23, 7, 0, 0);
	trafficLights.setTranslateX(0);
	trafficLights.setTranslateY(0);

	final Group group = new Group(/*trafficLight, redButton, yellowButton, greenButton, */redLightButton, yellowLightButton, greenLightButton, trafficLights, trafficLight, createDashboardNode());
	//group.getStylesheets().add(this.getClass().getResource("trafficlight.css").toExternalForm());
        final Scene scene = new Scene(group, 600, 450);
        //scene.setFill(Color.DARKSLATEBLUE);

        stage.setTitle("Welcome to JavaFX!");
        stage.setScene(scene);
        stage.sizeToScene();
        stage.show();
    }

    private Shape createSettingsShape() {
	final Circle circle = new Circle(20);
	Shape shape = circle;
	for (int i = 0; i < 7; i++) {
	    final Rectangle r = new Rectangle(0, 0, 56, 6);
	    r.setRotate(i * 45);
	    r.setTranslateX(-56.0 / 2);
	    r.setTranslateY(-6.0 / 2);
	    shape = Shape.union(shape, r);
	}
	shape = Shape.subtract(shape, new Circle(10));

	shape.setFill(Color.WHITE);
	shape.setScaleX(.3);
	shape.setScaleY(.3);
	return shape;
    }

    private Shape createRefreshShape() {
	final Circle circle = new Circle(20);
	Shape shape = circle;
	shape = Shape.subtract(shape, new Circle(10));

	final Arc a = new Arc(0, 0, 20, 20, 90, 45);
	a.setType(ArcType.ROUND);
	shape = Shape.subtract(shape, a);

	final Polygon p = new Polygon(0, 0, 0, 10, -10, 0, 0, -10, 0, 0);
	p.setTranslateY(-15.0);
	shape = Shape.union(shape, p);

	final Arc a2 = new Arc(0, 0, 20, 20, 270, 45);
	a2.setType(ArcType.ROUND);
	shape = Shape.subtract(shape, a2);

	final Polygon p2 = new Polygon(0, 0, 0, 10, -10, 0, 0, -10, 0, 0);
	p2.setTranslateY(+15.0);
	p2.setRotate(180.0);
	p2.setTranslateX(+10.0);
	shape = Shape.union(shape, p2);

	shape.setFill(Color.WHITE);
	shape.setScaleX(.3);
	shape.setScaleY(.3);
	return shape;
    }

    private Group createDashboardNode() {
	final Color lightgrey = Color.LIGHTGREY.interpolate(Color.WHITE, 0.7);
	final Color lightgreyStroke = lightgrey.darker().darker().darker(); // Color.LIGHTGREY;

	final Rectangle mainArea = new Rectangle();
	mainArea.setX(200);
	mainArea.setY(200);
	mainArea.setWidth(260);
	mainArea.setHeight(200);
	mainArea.setArcWidth(10);
	mainArea.setArcHeight(10);
	mainArea.setFill(Color.TRANSPARENT);
	mainArea.setStrokeWidth(0.7);
	mainArea.setStroke(lightgreyStroke);

	final Rectangle rectArea = new Rectangle();
	rectArea.setX(200);
	rectArea.setY(200);
	rectArea.setWidth(260);
	rectArea.setHeight(60);
	rectArea.setArcWidth(10);
	rectArea.setArcHeight(10);
	// rectArea.setFill(Color.LIGHTGREY);

	final Rectangle toRemove = new Rectangle();
	toRemove.setX(200);
	toRemove.setY(230);
	toRemove.setWidth(260);
	toRemove.setHeight(60);
	// toRemove.setFill(Color.LIGHTGREY);

	final Shape topTitleArea = Shape.subtract(rectArea, toRemove);
	topTitleArea.setFill(lightgrey.darker().darker().darker());
	// topTitleArea.setStrokeWidth(.7);
	// topTitleArea.setStroke(Color.LIGHTGREY);

	final Text titleNode = new Text("Budget vs. Estimate");
	titleNode.setX(210);
	titleNode.setY(220);
	titleNode.setFont(Font.font("Monospaced", FontWeight.EXTRA_BOLD, FontPosture.REGULAR, 14.0));
	titleNode.setFill(/* Color.DARKORANGE */ Color.WHITE);

	final SentinelModel model = new SentinelModel("Estimate > Budget by 20%", "Estimate > Budget by 10%", "Estimate does not exceed Budget by more than 10% -- normal situation");
//	model.getGreenLightingModel().setCount(100);
//	 model.getGreenLightingModel().setMoney(15700.0);
//	 model.getRedLightingModel().setCount(14);
//	 model.getRedLightingModel().setMoney(1000.0);
//	 model.getYellowLightingModel().setCount(25);
//	 model.getYellowLightingModel().setMoney(10000.0);
	final Runnable a = new Runnable() {
	    @Override
	    public void run() {
		System.out.println("bla");
	    }
	};
	final SentinelView trafficLights = new SentinelView(model, a, a, a, 30, 10, 0, 0);
	trafficLights.setTranslateX(200);
	trafficLights.setTranslateY(360);
//	trafficLights.setTranslateX(0);
//	trafficLights.setTranslateY(0);

	final Shape settingsShape = createSettingsShape();
	settingsShape.setTranslateX(420);
	settingsShape.setTranslateY(215);

	final Shape refreshShape = createRefreshShape();
	refreshShape.setTranslateX(445);
	refreshShape.setTranslateY(215);

	final Text redAndYellowSumNode = new Text("$11,000");
	redAndYellowSumNode.setX(315);
	redAndYellowSumNode.setY(280);
	redAndYellowSumNode.setFont(Font.font("Monospaced", FontWeight.EXTRA_BOLD, FontPosture.REGULAR, 37.0));
	redAndYellowSumNode.setFill(/* Color.DARKORANGE */ Color.LIGHTGREY);

	final Group dashboardNode = new Group(topTitleArea, mainArea, titleNode, trafficLights, settingsShape, refreshShape, redAndYellowSumNode);
	return dashboardNode;
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
		try {
		    return createNode(textureImageFileName, isLighting);
		} catch (final MalformedURLException e) {
		    e.printStackTrace();
		    return null;
		}
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
