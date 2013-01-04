package ua.com.fielden.platform.javafx.gis;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.JFXPanel;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.ToolBar;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.HLineTo;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.text.Text;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.util.Duration;
import netscape.javascript.JSObject;

import org.joda.time.DateTime;
import org.joda.time.Period;

import ua.com.fielden.platform.gis.Point;
import ua.com.fielden.platform.utils.Pair;

/**
 * A swing container to contain javaFx dashboard.
 *
 * @author TG Team
 *
 */
public class GisViewPanel <P extends Point> extends JFXPanel {
    private static final long serialVersionUID = 9202827128855362320L;
    private Timeline locationUpdateTimeline;
    private double xForDragBegin, yForDragBegin;
    private Group path;
    private BorderPane root;
    private BorderPane webViewPanel;
    private Slider startSlider, endSlider;
    private WebView webView;
    private WebEngine webEngine;
    private final List<P> points;
    
    private IWorldToScreen currentTranformation;
    
    private Double get(final JSObject p, final String what) {
	final String s = p.call(what).toString();
	return new Double(s);
    }

    public Group path() {
	return path;
    }

    public List<P> points() {
	return points;
    }

    /**s
     * Creates a swing container with javaFx dashboard.
     *
     * @return
     */
    public GisViewPanel() {
	Platform.setImplicitExit(false);

	this.points = new ArrayList<>();
	// this.data = createData();

	Platform.runLater(new Runnable() {
	    @Override
	    public void run() {
	        // This method is invoked on the JavaFX thread
	        final Scene scene = createScene();
	        setScene(scene);
	    }
	});
    }

    // load the image
    // private static Image image = new Image("src/main/resources/marker.png", true); // /marker.png

//    private final static String packageName = "gis";
//    private final List<Point> pe = DataRetriever.getData("src/main/resources/gis/gis-data-sample.csv"); //       createUrl("gis-data-sample.csv")         "/home/jhou/workspace/trident-genesis/platform-ui/src/main/resources/gis/gis-data-sample.csv"
//
//    {
//	Collections.sort(pe);
//    }

    private static Group createSimplePath() {
	final Group g = new Group();
	final Path path = new Path();

//	      ['Bondi Beach', -33.890542, 151.274856, 4],
//	      ['Coogee Beach', -33.923036, 151.259052, 5],

	final MoveTo moveTo = new MoveTo();
	moveTo.setX(0.0f);
	moveTo.setY(0.0f);

	final HLineTo hLineTo = new HLineTo();
	hLineTo.setX(70.0f);

//	final QuadCurveTo quadCurveTo = new QuadCurveTo();
//	quadCurveTo.setX(120.0f);
//	quadCurveTo.setY(60.0f);
//	quadCurveTo.setControlX(100.0f);
//	quadCurveTo.setControlY(0.0f);
//
//	final LineTo lineTo = new LineTo();
//	lineTo.setX(175.0f);
//	lineTo.setY(55.0f);
//
//	final ArcTo arcTo = new ArcTo();
//	arcTo.setX(50.0f);
//	arcTo.setY(50.0f);
//	arcTo.setRadiusX(50.0f);
//	arcTo.setRadiusY(50.0f);

	path.getElements().add(moveTo);
	path.getElements().add(hLineTo);
//	path.getElements().add(quadCurveTo);
//	path.getElements().add(lineTo);
//	path.getElements().add(arcTo);
	g.getChildren().add(path);
	return g;
    }

    public Scene createScene() {
	// create web engine and view
	webView = new WebView();
	webEngine = webView.getEngine();

//	webView.setOnScroll(new EventHandler<ScrollEvent>() {
//	    @Override
//	    public void handle(final ScrollEvent event) {
//		event.getEventType().equals(ScrollEvent.SCROLL);
//		final double deltaY = event.getDeltaY();
//		final int deltaYDiscrete = (int) (deltaY / 40.0);
//		final String zoomScript = deltaYDiscrete > 0 ? "document.zoomIn()" : "document.zoomOut()";
//		for (int i = 0; i < Math.abs(deltaYDiscrete); i++) {
//		    webEngine.executeScript(zoomScript);
//		    removeOldAndAddNew(webEngine, zoom(webEngine));
//		}
//	    }
//	});
//
//	webView.setOnDragDetected(new EventHandler<MouseEvent>() {
//	    @Override
//	    public void handle(final MouseEvent event) {
//		xForDragBegin = event.getSceneX();
//		yForDragBegin = event.getSceneY();
//		// webEngine.executeScript("document.panBy(" + deltaXForPan + ", " + deltaYForPan + ")");
//		webView.startFullDrag();
//		// webView.startDragAndDrop(TransferMode.ANY);
//		System.out.println("setOnDragDetected " + event);
//	    }
//
//	});
//	// fujhsdfuhsdfuhsdfhisdfjhiosdfsdf
//
//	webView.setOnDragOver(new EventHandler<DragEvent>() {
//	    @Override
//	    public void handle(final DragEvent event) {
//		System.out.println("setOnDragOver " + event);
//	    }
//
//	});
//
//
//	webView.setOnDragDropped(new EventHandler<DragEvent>() {
//	    @Override
//	    public void handle(final DragEvent event) {
//		System.out.println("setOnDragDropped " + event);
//	    }
//
//	});
//
//	webView.setOnDragDone(new EventHandler<DragEvent>() {
//	    @Override
//	    public void handle(final DragEvent event) {
//		System.out.println("setOnDragDone " + event);
//	    }
//
//	});
//
//	webView.setOnMouseDragReleased(new EventHandler<MouseDragEvent>() {
//	    @Override
//	    public void handle(final MouseDragEvent event) {
//		final int deltaXForPan = -(int) (event.getSceneX() - xForDragBegin),
//			  deltaYForPan = -(int) (event.getSceneY() - yForDragBegin);
//		webEngine.executeScript("document.panBy(" + deltaXForPan + ", " + deltaYForPan + ")");
//		System.out.println("deltaXForPan == " + deltaXForPan + ", deltaYForPan == " + deltaYForPan + ", setOnMouseDragReleased " + event);
//
//		path.setTranslateX(path.getTranslateX() - deltaXForPan);
//		path.setTranslateY(path.getTranslateY() - deltaYForPan);
//	    }
//	});
//
//	webView.setOnMouseClicked(new EventHandler<MouseEvent>() {
//	    @Override
//	    public void handle(final MouseEvent event) {
//		if (event.getClickCount() == 2) {
//		    final List<Point> pe = new DataRetriever().getData("src/main/resources/gis/gis-data-sample.csv"); //       createUrl("gis-data-sample.csv")         "/home/jhou/workspace/trident-genesis/platform-ui/src/main/resources/gis/gis-data-sample.csv"
//		    Collections.sort(pe);
//		    GisViewPanel.this.points.clear();
//		    // TODO GisViewPanel.this.points.addAll(pe);
//
//		    webEngine.executeScript("document.setCenter(48.93061,24.96296)"); // -34.028249, 151.157507
//		    removeOldAndAddNew(webEngine, zoom(webEngine));
//		}
//	    }
//	});

	webEngine.load(GisViewPanel.class.getResource("googlemap.html").toString());
        // create map type buttons
        final ToggleGroup mapTypeGroup = new ToggleGroup();
        final ToggleButton road = new ToggleButton("Road");
        road.setSelected(true);
        road.setToggleGroup(mapTypeGroup);
        final ToggleButton satellite = new ToggleButton("Satellite");
        satellite.setToggleGroup(mapTypeGroup);
        final ToggleButton hybrid = new ToggleButton("Hybrid");
        hybrid.setToggleGroup(mapTypeGroup);
        final ToggleButton terrain = new ToggleButton("Terrain");
        terrain.setToggleGroup(mapTypeGroup);
        mapTypeGroup.selectedToggleProperty().addListener(
                            new ChangeListener<Toggle>() {
            public void changed(
                    final ObservableValue<? extends Toggle> observableValue,
                    final Toggle toggle, final Toggle toggle1) {
                if (road.isSelected()) {
                    webEngine.executeScript("document.setMapTypeRoad()");
                } else if (satellite.isSelected()) {
                    webEngine.executeScript("document.setMapTypeSatellite()");
                } else if (hybrid.isSelected()) {
                    webEngine.executeScript("document.setMapTypeHybrid()");
                } else if (terrain.isSelected()) {
                    webEngine.executeScript("document.setMapTypeTerrain()");
                }
            }
        });
        // add map source toggles
        final ToggleGroup mapSourceGroup = new ToggleGroup();
        final ToggleButton google = new ToggleButton("Google");
        google.setSelected(true);
        google.setToggleGroup(mapSourceGroup);
        // listen to selected source
        mapSourceGroup.selectedToggleProperty().addListener(
                            new ChangeListener<Toggle>() {
            public void changed(
                    final ObservableValue<? extends Toggle> observableValue,
                    final Toggle toggle, final Toggle toggle1) {
                terrain.setDisable(true);
                if (google.isSelected()) {
                    terrain.setDisable(false);
                    webEngine.load(getClass().getResource("googlemap.html").toString());
                }
                mapTypeGroup.selectToggle(road);
            }
        });
        // add search
        final TextField searchBox = new TextField("Lviv");

        //searchBox.setColumns(12);
        searchBox.setPromptText("Search");
        searchBox./*rawT*/textProperty().addListener(new ChangeListener<String>() {
            public void changed(
                    final ObservableValue<? extends String> observableValue,
                    final String s, final String s1) {
                // delay location updates to we don't go too fast file typing
                if (locationUpdateTimeline!=null) {
		    locationUpdateTimeline.stop();
		}
                locationUpdateTimeline = new Timeline();
                locationUpdateTimeline.getKeyFrames().add(
                    new KeyFrame(new Duration(400),
                            new EventHandler<ActionEvent>() {
                        public void handle(final ActionEvent actionEvent) {
                            webEngine.executeScript("document.goToLocation(\""+
                                    searchBox.getText() /*RawT*/ +"\")");
                        }
                    })
                );
                locationUpdateTimeline.play();
            }
        });
        final Button zoomIn = new Button("Zoom In");
        zoomIn.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(final ActionEvent actionEvent) {
		webEngine.executeScript("document.zoomIn(); document.map.getZoom()");
		removeOldAndAddNew(webEngine, zoom(webEngine));
            }
        });
        final Button fitToBounds = new Button("Fit To Bounds");
        fitToBounds.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(final ActionEvent actionEvent) {
        	fitToBounds();
		removeOldAndAddNew(webEngine, zoom(webEngine));
            }
        });
        final Button zoomOut = new Button("Zoom Out");
        zoomOut.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(final ActionEvent actionEvent) {
        	webEngine.executeScript("document.zoomOut()");
		removeOldAndAddNew(webEngine, zoom(webEngine));
            }
        });

        startSlider = new Slider(0, 1, 0.0);
        startSlider.setBlockIncrement(0.001);
	startSlider.valueProperty().addListener(new ChangeListener<Number>() {
	    public void changed(final ObservableValue<? extends Number> ov, final Number old_val, final Number new_val) {
		removeOldAndAddNew(webEngine, zoom(webEngine));
	    }
	});

	endSlider = new Slider(0, 1, /* 0.08 */ 1.0);
        endSlider.setBlockIncrement(0.001);
	endSlider.valueProperty().addListener(new ChangeListener<Number>() {
	    public void changed(final ObservableValue<? extends Number> ov, final Number old_val, final Number new_val) {
		removeOldAndAddNew(webEngine, zoom(webEngine));
	    }
	});

        // create toolbar
        final ToolBar toolBar = new ToolBar();
        toolBar.getStyleClass().add("map-toolbar");
        toolBar.getItems().addAll(
                road, satellite, hybrid, terrain,
                createSpacer(),
                // google, startSlider, endSlider, //yahoo, bing,
                // createSpacer(),
                //new Label("Location:"), searchBox,
                zoomIn, fitToBounds, zoomOut);
        // create root
        root = new BorderPane();
        root.getStyleClass().add("map");
        webViewPanel = new BorderPane();

        ///////////////////// Dragging and scrolling logic ////////////
	webView.setDisable(true); // need to disable web view as it steals mouse events

	webViewPanel.setOnMousePressed(new EventHandler<MouseEvent>() {
	    @Override
	    public void handle(final MouseEvent event) {
		xForDragBegin = event.getSceneX();
		yForDragBegin = event.getSceneY();
		//System.out.println("setOnMousePressed: xForDragBegin = " + xForDragBegin + "   yForDragBegin = " + yForDragBegin);
	    }

	});

	webViewPanel.setOnMouseDragged(new EventHandler<MouseEvent>() {
	    @Override
	    public void handle(final MouseEvent event) {
		final int deltaXForPan = -(int) (event.getSceneX() - xForDragBegin);
		final int deltaYForPan = -(int) (event.getSceneY() - yForDragBegin);
		webEngine.executeScript("document.panBy(" + deltaXForPan + ", " + deltaYForPan + ")");

		//System.out.println("deltaXForPan == " + deltaXForPan + ", deltaYForPan == " + deltaYForPan + ", setOnMouseDragReleased " + event);

		path.setTranslateX(path.getTranslateX() - deltaXForPan);
		path.setTranslateY(path.getTranslateY() - deltaYForPan);

		xForDragBegin = event.getSceneX();
		yForDragBegin = event.getSceneY();
		event.consume();
	    }
	});

	webViewPanel.setOnMouseReleased(new EventHandler<MouseEvent>() {
	    @Override
	    public void handle(final MouseEvent event) {
		xForDragBegin = event.getSceneX();
		yForDragBegin = event.getSceneY();
		//System.out.println("setOnMouseReleased: xForDragBegin = " + xForDragBegin + "   yForDragBegin = " + yForDragBegin);
	    }

	});

	webViewPanel.setOnScroll(new EventHandler<ScrollEvent>() {
	    @Override
	    public void handle(final ScrollEvent event) {
		final double deltaY = event.getDeltaY();
		final int deltaYDiscrete = (int) (deltaY / 40.0);
		final String zoomScript = deltaYDiscrete > 0 ? "document.zoomIn()" : "document.zoomOut()";
		for (int zoomSteps = 0; zoomSteps < Math.abs(deltaYDiscrete); zoomSteps++) {
		    webEngine.executeScript(zoomScript);
		    removeOldAndAddNew(webEngine, zoom(webEngine));
		}
	    }
	});

        ////////////////////////////////

        webViewPanel.setCenter(webView);
        root.setCenter(webViewPanel);
        root.setTop(toolBar);

        path = createSimplePath();
        webViewPanel.getChildren().add(path);
        final Scene scene = new Scene(root,1100,600, Color.web("#666970"));
        scene.getStylesheets().add("/webmap/WebMap.css");
	return scene;
    }

    private Node createSpacer() {
        final Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        return spacer;
    }

    static { // use system proxy settings when standalone application
        // System.setProperty("java.net.useSystemProxies", "true");
    }

    public static void main(final String[] args){
        Application.launch(args);
    }

    protected void removeOldAndAddNew(final WebEngine webEngine, final int zoom) {
	final DateTime start = new DateTime();
	System.err.println("REMOVING OLD AND ADDING NEW...");
	
	updateTransformation();
	
	if (path != null) {
	    webViewPanel.getChildren().remove(path);
	}

	this.path = new Group();
	// addPoint(webEngine, path, -34.028249, 151.157507);
	// addLineTo(webEngine, path, -33.950198, 151.259302);
	// addLineTo(webEngine, path, -33.923036, 151.259052);
	// addLineTo(webEngine, path, -33.890542, 151.274856);
	// addLineTo(webEngine, path, -33.80010128657071, 151.28747820854187);

	if (points.size() > 0) {
	    addPoint(webEngine, points.get(0), points.size());
	}
	for (int i = 1 + (int)(startSlider.getValue() * points.size()); i < (int)(endSlider.getValue() * points.size()); i++) {
	    addLine(webEngine, points.get(i-1), points.get(i), points.size(), zoom > 15);
	}
	webViewPanel.getChildren().add(this.path);
	final Period pd = new Period(start, new DateTime());
	System.err.println("REMOVING OLD AND ADDING NEW...done in " + pd.getSeconds() + " s " + pd.getMillis() + " ms");
    }

    protected Color getColor(final P start) {
	final int speed = start.getSpeed();
	final double maxSpeed = 80.0;
	// return Color.hsb(0, speed / maxSpeed, 1.0);
	return Color.hsb(120.0 - (120.0 * speed / maxSpeed), 1.0, 1.0); // 0 (green, hue=120) to 80 (red, hue=0) km/hour
    }
    
    protected String getTooltip(final P point) {
	return point.toString();
    }
    
    private static Point2D googleConvertWorld2Pixel(final WebEngine webEngine, final double latitude, final double longitude) {
	final JSObject point = (JSObject) webEngine.executeScript("document.convertPoint(" + latitude + ", " + longitude + ")");
	return new Point2D.Double((Double) point.getMember("x"), (Double) point.getMember("y"));
    }
    
    protected void updateTransformation() {
	final JSObject northEast = (JSObject) webEngine.executeScript("document.getNorthEast()");
	final JSObject southWest = (JSObject) webEngine.executeScript("document.getSouthWest()");
	
	System.err.println("northEast == " + northEast);
	System.err.println("southWest == " + southWest);
	
//	currentTranformation = new WorldToScreenAffineTransformation(webView.getWidth(), webView.getHeight(), 
//		get(southWest, "lng"), get(southWest, "lat"), get(northEast, "lng"), get(northEast, "lat"));
	currentTranformation = new WorldToScreenMercatorProjection(webView.getWidth(), webView.getHeight(), zoom(webEngine), 
		get(southWest, "lng"), get(southWest, "lat"), get(northEast, "lng"), get(northEast, "lat"));
    }
    
    private Point2D convertWorld2Pixel(final WebEngine webEngine, final double latitude, final double longitude) {
	// System.err.println("longitude = " + longitude + " latitude = " + latitude);
	final Point2D p = currentTranformation.world2pixelXY(longitude, latitude); // googleConvertWorld2Pixel(webEngine, latitude, longitude);
	// System.err.println("trasformed = " + p);
	return p;
    }

    protected Point2D addPoint(final WebEngine webEngine, final P point, final int size) {
	return convertWorld2Pixel(webEngine, point.getLatitude(), point.getLongitude());
    }

    private Pair<Double, Double> addLine(final WebEngine webEngine, final P start, final P end, final int size, final boolean drawSpeedValues) {
	final Point2D s = convertWorld2Pixel(webEngine, start.getLatitude(), start.getLongitude());
	final Double x0 = s.getX();
	final Double y0 = s.getY();
	final Point2D e = convertWorld2Pixel(webEngine, end.getLatitude(), end.getLongitude());
	final Double x = e.getX();
	final Double y = e.getY();

	if (drawLines()) {
	    final Path p = new Path();
	    p.setStrokeWidth(3.0);

	    p.getElements().add(new MoveTo(x0, y0));
	    p.getElements().add(new LineTo(x, y));

	    final Stop[] stops2 = new Stop[] { new Stop(0.0, getColor(start)), new Stop(1.0, getColor(end)) };
	    final LinearGradient lg1 = new LinearGradient(x0, y0, x, y, false, CycleMethod.NO_CYCLE, stops2);
	    p.setStroke(lg1);

	    this.path.getChildren().add(p);
	}

//	if (end.getSpeed() < 5) {
//	    // simply displays ImageView the image as is
//	    final ImageView iv1 = new ImageView(image);
//	    iv1.setX(x - 8);
//	    iv1.setY(y - 8);
//	    this.path.getChildren().add(iv1);
//	}

	if (drawSpeedValues) {
	    final Text text = new Text(end.getSpeed() + "");
	    text.setX(x);
	    text.setY(y);
	    // text.setFill(/*getColor(end.getSpeed()) */Color.VIOLET);
	    text.setStroke(/*getColor(end.getSpeed()) */Color.BLACK);
	    // text.setStrokeWidth(2);
	    this.path.getChildren().add(text);
	}

	addPoint(webEngine, end, size);

	return new Pair<Double, Double>(x, y);
    }

    protected boolean drawLines() {
	return true;
    }

    private int zoom(final WebEngine webEngine) {
	return (Integer) webEngine.executeScript("document.map.getZoom()");
    }
    
    protected void fitToBounds() {
	// fit all coordinates to the bounds calculated by existing points
	webEngine.executeScript("document.viewBounds = new google.maps.LatLngBounds()");
	for (final P point : points) {
	    webEngine.executeScript("document.viewBounds.extend(new google.maps.LatLng(" + point.getLatitude() + "," + point.getLongitude() + "))");
	}
	webEngine.executeScript("document.map.fitBounds(document.viewBounds)");
    }

    public void providePoints(final List<P> points, final boolean fitToBounds) {
	Platform.runLater(new Runnable() {
	    @Override
	    public void run() {
		GisViewPanel.this.points.clear();
		GisViewPanel.this.points.addAll(points);
		if (fitToBounds) {
		    fitToBounds();
		}
		// actually add a markers
		removeOldAndAddNew(webEngine, zoom(webEngine));
	    }
	});
    }
}
