package ua.com.fielden.platform.javafx.gis;

import java.awt.event.MouseAdapter;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker.State;
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
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.text.Text;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.util.Duration;

import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import netscape.javascript.JSObject;

import org.joda.time.DateTime;
import org.joda.time.Period;

import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.gis.Point;
import ua.com.fielden.platform.pagination.IPage;
import ua.com.fielden.platform.pagination.IPageChangedListener;
import ua.com.fielden.platform.pagination.PageChangedEvent;
import ua.com.fielden.platform.pagination.PageHolder;
import ua.com.fielden.platform.swing.egi.EntityGridInspector;
import ua.com.fielden.platform.swing.review.report.analysis.grid.GridAnalysisView;
import ua.com.fielden.platform.swing.utils.SwingUtilitiesEx;
import ua.com.fielden.platform.utils.Pair;

/**
 * A swing container to contain javaFx dashboard.
 *
 * @author TG Team
 *
 */
public abstract class GisViewPanel<T extends AbstractEntity<?>, P extends Point> extends JFXPanel implements IPoint<P> {
    private static final long serialVersionUID = 9202827128855362320L;

    private static long ZOOM_DELAY = 300;
    private static double DEFAULT_PIXEL_THRESHOLD = 2.0;
    private static double DRAGGING_PIXEL_THRESHOULD = 0.0; // 30.0

    static { // use system proxy settings when standalone application
        // System.setProperty("java.net.useSystemProxies", "true");
    }

    private Timeline locationUpdateTimeline;
    private Timer timer = new Timer();
    private GisView activeView;

    private final EntityGridInspector egi;
    private final GridAnalysisView<T, ICentreDomainTreeManagerAndEnhancer> parentView;
    private WebView webView;
    private WebEngine webEngine;
    private ToggleButton road, satellite, hybrid, terrain;
    private Slider startSlider, endSlider;
    private BorderPane root;
    private StackPane webViewPanel;
    private BorderPane canvas;

    private final List<P> points;
    private final Map<Long, List<P>> entityPoints;
    private final Map<P, TrackSegment> trackSegments;
    private Group path;
    private final Map<Integer, Map<Integer, Node>> pixelNodes = new HashMap<>();

    private double xForDragBegin, yForDragBegin;
    private IWorldToScreen currentTranformation;
    private AbstractEntity<?> previousSelectedEntity;
    protected int countOfProcessed, oldCountOfProcessed, newCountOfProcessed;
    private int currentZoomDelta = 0;
    private int zoom;
    protected boolean calloutChangeShouldBeForced = true;

    public void setCalloutChangeShouldBeForced(final boolean calloutChangeShouldBeForced) {
	this.calloutChangeShouldBeForced = calloutChangeShouldBeForced;
    }

    protected Group path() {
	return path;
    }

    protected final AbstractEntity<?> selectedEntity()  {
	return parentView.getEnhancedSelectedAbstractEntity();
    }

    /**
     * Creates a swing container with javaFx dashboard.
     *
     * @return
     */
    public GisViewPanel(final GridAnalysisView<T, ICentreDomainTreeManagerAndEnhancer> parentView, final EntityGridInspector egi, final ListSelectionModel listSelectionModel, final PageHolder pageHolder) {
	this.parentView = parentView;
	this.egi = egi;
	setFocusable(false);
	Platform.setImplicitExit(false);

	this.points = new ArrayList<P>();
	this.entityPoints = new HashMap<>();
	this.trackSegments = new HashMap<>();

	Platform.runLater(new Runnable() {
	    @Override
	    public void run() {
	        // This method is invoked on the JavaFX thread
	        final Scene scene = createScene();
	        setScene(scene);
	    }
	});

	this.egi.addMouseListener(new MouseAdapter() {
	    @Override
	    public void mouseClicked(final java.awt.event.MouseEvent e) {
	        final int index = egi.rowAtPoint(e.getPoint());
	        if (index != -1) {
	            selectEntity(true); // when the user has been clicked -- callout change should be forced
	        }
	    }
	});

	listSelectionModel.addListSelectionListener(new ListSelectionListener() {
	    @Override
	    public void valueChanged(final ListSelectionEvent e) {
		if (!e.getValueIsAdjusting()) {
		    selectEntity(calloutChangeShouldBeForced);
		}
		return;
	    }
	});

	pageHolder.addPageChangedListener(new IPageChangedListener() {
	    @Override
	    public void pageChanged(final PageChangedEvent e) {
		SwingUtilitiesEx.invokeLater(new Runnable() {

		    @Override
		    public void run() {
			Platform.runLater(new Runnable() {
			    @Override
			    public void run() {
				providePoints(createPoints((IPage<AbstractEntity<?>>) e.getNewPage()), shouldFitToBounds());
				removeOldAndAddNew(webEngine, zoom(webEngine));
			    }
			});
		    }
		});
	    }
	});
    }

    protected abstract Pair<List<P>, Map<Long, List<P>>> createPoints(final IPage<AbstractEntity<?>> entitiesPage);

    protected abstract void findAndSelectPoint(final AbstractEntity<?> selectedEntity, final AbstractEntity<?> unselectedEntity, final boolean forceCalloutChange);

    protected abstract boolean shouldFitToBounds();

    protected abstract AbstractEntity<?> entityToSelect(final P point);

    protected void requestFocusForEgi() {
	if (!egi.hasFocus()) {
	    SwingUtilitiesEx.invokeLater(new Runnable() {
		public void run() {
		    final boolean focused = egi.requestFocusInWindow();
		    if (!focused) {
			new Exception("Egi cannot gain focus somehow.").printStackTrace();
		    }
		}
	    });
	}
    }

    protected void selectEntity(final boolean forceCalloutChange) {
	final AbstractEntity<?> unselectedEntity = previousSelectedEntity;
	final AbstractEntity<?> selectedEntity = selectedEntity();
	Platform.runLater(new Runnable() {
	    @Override
	    public void run() {
		findAndSelectPoint(selectedEntity, unselectedEntity, forceCalloutChange);
		previousSelectedEntity = selectedEntity;
	    }
	});
    }

    private static Group createSimplePath() {
	final Group g = new Group();
	g.setCache(true); // this should improve a repainting when interacting with 'path' group (translating or other transformations)
	return g;
    }

    public enum GisView {
	ROAD, SATELLITE, HYBRID, TERRAIN
    }

    /**
     * Activates approriate view {@link GisView}.
     *
     * @param gisView
     */
    protected void activateView(final GisView gisView) {
	final ToggleButton neededButton =
		GisView.ROAD.equals(gisView) ? road :
		    GisView.SATELLITE.equals(gisView) ? satellite :
			GisView.HYBRID.equals(gisView) ? hybrid :
			    terrain;
	neededButton.setSelected(true);
	activeView = gisView;
    }

    protected GisView getActiveView() {
	return activeView;
    }

    public Scene createScene() {
	// create web engine and view
	webView = new WebView();
	webEngine = webView.getEngine();

	loadMap();
        // create map type buttons
        final ToggleGroup mapTypeGroup = new ToggleGroup();
        road = new ToggleButton("Road");
        road.setSelected(true);
        road.setToggleGroup(mapTypeGroup);
        satellite = new ToggleButton("Satellite");
        satellite.setToggleGroup(mapTypeGroup);
        hybrid = new ToggleButton("Hybrid");
        hybrid.setToggleGroup(mapTypeGroup);
        terrain = new ToggleButton("Terrain");
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
        searchBox.textProperty().addListener(new ChangeListener<String>() {
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
                            webEngine.executeScript("document.goToLocation(\""+ searchBox.getText() + "\")");
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
        webViewPanel = new StackPane();

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
		final double deltaXForPan = -(event.getSceneX() - xForDragBegin);
		final double deltaYForPan = -(event.getSceneY() - yForDragBegin);
		if ((Math.abs(deltaXForPan) + Math.abs(deltaYForPan)) / 2.0 > DRAGGING_PIXEL_THRESHOULD) {
		    panBy(deltaXForPan, deltaYForPan);
		    path.setTranslateX(path.getTranslateX() - deltaXForPan);
		    path.setTranslateY(path.getTranslateY() - deltaYForPan);

		    xForDragBegin = event.getSceneX();
		    yForDragBegin = event.getSceneY();
		    event.consume();
		}
	    }
	});

	webViewPanel.setOnMouseReleased(new EventHandler<MouseEvent>() {
	    @Override
	    public void handle(final MouseEvent event) {
		final double deltaXForPan = -(event.getSceneX() - xForDragBegin);
		final double deltaYForPan = -(event.getSceneY() - yForDragBegin);
		panBy(deltaXForPan, deltaYForPan);
		path.setTranslateX(path.getTranslateX() - deltaXForPan);
		path.setTranslateY(path.getTranslateY() - deltaYForPan);

		xForDragBegin = event.getSceneX();
		yForDragBegin = event.getSceneY();

		removeOldAndAddNew(webEngine, zoom(webEngine));
		// System.out.println("setOnMouseReleased: xForDragBegin = " + xForDragBegin + "   yForDragBegin = " + yForDragBegin);
	    }
	});

	webViewPanel.setOnScroll(new EventHandler<ScrollEvent>() {
	    @Override
	    public void handle(final ScrollEvent event) {
		final int supposedCurrentZoomDelta = currentZoomDelta + (event.getDeltaY() > 0 ? 1 : -1);
		timer.cancel();
		timer = new Timer();

		final int zoom = zoom(webEngine);
		final int maxZoomLevel = activeView.equals(GisView.ROAD) ? 21 : activeView.equals(GisView.TERRAIN) ? 15 : 19 /* SATELLITE, HYBRID */;
		final int targetZoom = (zoom + supposedCurrentZoomDelta < 0) ? 0 : (zoom + supposedCurrentZoomDelta > maxZoomLevel) ? maxZoomLevel : (zoom + supposedCurrentZoomDelta);

		if (targetZoom != zoom) {
		    currentZoomDelta += event.getDeltaY() > 0 ? 1 : -1;
		    // System.out.println("currentZoomDelta == " + currentZoomDelta);
		    final TimerTask zoomGestureFinishedTask = new TimerTask() {
			@Override
			public void run() {
			    Platform.runLater(new Runnable() {
				public void run() {
				    final DateTime st = new DateTime();
				    final double cursorPixelX = event.getX();
				    final double cursorPixelY = event.getY();
				    final double centrePixelX = webView.getWidth() / 2.0;
				    final double centrePixelY = webView.getHeight() / 2.0;

				    panBy(-(centrePixelX - cursorPixelX), -(centrePixelY - cursorPixelY));
				    webEngine.executeScript("document.setZoom(" + targetZoom + ")");
				    panBy(centrePixelX - cursorPixelX, centrePixelY - cursorPixelY);
				    final Period p = new Period(st, new DateTime());
				    System.out.println("Zooming by " + currentZoomDelta + " steps in " + p.getSeconds() + " sec " + p.getMillis() + " millis");

				    removeOldAndAddNew(webEngine, zoom(webEngine));
				    currentZoomDelta = 0;
				}
			    });

			}
		    };
		    timer.schedule(zoomGestureFinishedTask, ZOOM_DELAY);
		}
	    }
	});

        ////////////////////////////////

        // webViewPanel.setCenter(webView);
	webViewPanel.getChildren().add(webView);
	canvas = new BorderPane();
	webViewPanel.getChildren().add(canvas);

	root.setCenter(webViewPanel);
        root.setTop(toolBar);

        path = createSimplePath();
        canvas.getChildren().add(path);

        final Scene scene = new Scene(root,1100,600, Color.web("#666970"));

	return scene;
    }

    protected void afterMapLoaded() {
    }

    protected void loadMap() {
	webEngine.getLoadWorker().stateProperty().addListener(new ChangeListener<State>() {
	    public void changed(final ObservableValue ov, final State oldState, final State newState) {
		if (newState == State.SUCCEEDED) {
		    afterMapLoaded();
		}
	    }
	});
	webEngine.load(GisViewPanel.class.getResource("googlemap.html").toString());
    }

    private Node createSpacer() {
        final Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        return spacer;
    }

    protected boolean drawSpeedValues(final int zoom) {
	return zoom > 15;
    }

    /**
     * A current zoom at some stage. Should be strictly used for addPoint or addLine purposes.
     *
     * @return
     */
    protected int getZoom() {
	return zoom;
    }

    protected void removeOldAndAddNew(final WebEngine webEngine, final int zoom) {
	this.zoom = zoom;

	final DateTime start0 = new DateTime();
	DateTime start = new DateTime();
	System.err.println("REMOVING OLD AND ADDING NEW...");

	updateTransformation();

	Period pd = new Period(start, new DateTime());
	System.out.println("UPDATED transformation: done in " + pd.getSeconds() + " s " + pd.getMillis() + " ms");
	start = new DateTime();

	if (path != null) {
	    path.setTranslateX(0.0);
	    path.setTranslateY(0.0);
	}
	pixelNodes.clear();

	countOfProcessed = 0;
	newCountOfProcessed = 0;
	oldCountOfProcessed = 0;
	if (points.size() > 0) {
	    addPoint(webEngine, points.get(0));
	}
	for (int i = 1 + (int)(startSlider.getValue() * points.size()); i < (int)(endSlider.getValue() * points.size()); i++) {
	    addPoint(webEngine, points.get(i));
	}
	pd = new Period(start, new DateTime());
	System.out.println("PROCESSED nodes: " + countOfProcessed + " (new = " + newCountOfProcessed + "; old = " + oldCountOfProcessed + ") done in " + pd.getSeconds() + " s " + pd.getMillis() + " ms");
	start = new DateTime();

	countOfProcessed = 0;
	newCountOfProcessed = 0;
	oldCountOfProcessed = 0;
	for (int i = 1 + (int)(startSlider.getValue() * points.size()); i < (int)(endSlider.getValue() * points.size()); i++) {
	    addLine(webEngine, points.get(i-1), points.get(i), points.size(), drawSpeedValues(zoom));
	}

	pd = new Period(start, new DateTime());
	System.out.println("PROCESSED segments: " + countOfProcessed + " (new = " + newCountOfProcessed + "; old = " + oldCountOfProcessed + ") done in " + pd.getSeconds() + " s " + pd.getMillis() + " ms");
	start = new DateTime();

	pd = new Period(start0, new DateTime());
	System.err.println("REMOVING OLD AND ADDING NEW...done in " + pd.getSeconds() + " s " + pd.getMillis() + " ms");
    }

    @Override
    public Color getColor(final P start) {
	final int speed = start.getSpeed();
	final double maxSpeed = 80.0;
	// return Color.hsb(0, speed / maxSpeed, 1.0);
	// return Color.hsb(120.0 - (120.0 * speed / maxSpeed), 1.0, 1.0); // 0 (green, hue=120) to 80 (red, hue=0) km/hour
	return Color.hsb(240.0 + ((360.0 - 240.0) * speed / maxSpeed), 1.0, 1.0); // 0 (dark blue, hue=? 120) to 80 (red, hue=0) km/hour
    }

    @Override
    public String getTooltip(final P point) {
	return point.toString();
    }

    private static Point2D googleConvertWorld2Pixel(final WebEngine webEngine, final double latitude, final double longitude) {
	final JSObject point = (JSObject) webEngine.executeScript("document.convertPoint(" + latitude + ", " + longitude + ")");
	return new Point2D.Double((Double) point.getMember("x"), (Double) point.getMember("y"));
    }

    protected void updateTransformation() {
	final JSObject northEast = (JSObject) webEngine.executeScript("document.getNorthEast()");
	final JSObject southWest = (JSObject) webEngine.executeScript("document.getSouthWest()");

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

    protected Point2D addPoint(final WebEngine webEngine, final P point) {
	return convertWorld2Pixel(webEngine, point.getLatitude(), point.getLongitude());
    }

    protected class TrackSegment extends Path {
	private final MoveTo startPoint;
	private final LineTo lineToEnd;
	private final P start, end;

	public TrackSegment(final P start, final P end) {
	    super();
	    this.start = start;
	    this.end = end;
	    this.startPoint = new MoveTo();
	    this.lineToEnd = new LineTo();

	    setStrokeWidth(3.0);

	    getElements().add(startPoint);
	    getElements().add(lineToEnd);
	}

	public void update(final Point2D xY0, final Point2D xY) {
	    startPoint.setX(xY0.getX());
	    startPoint.setY(xY0.getY());
	    lineToEnd.setX(xY.getX());
	    lineToEnd.setY(xY.getY());

	    final Stop[] stops2 = new Stop[] { new Stop(0.0, getColor(start)), new Stop(1.0, getColor(end)) };
	    final LinearGradient lg1 = new LinearGradient(xY0.getX(), xY0.getY(), xY.getX(), xY.getY(), false, CycleMethod.NO_CYCLE, stops2);
	    setStroke(lg1);
	}
    }

//    private double sqr(final double x) {
//	return x * x;
//    }
//
//    protected double dist(final Point2D prevXY, final Point2D xY) {
//	if (prevXY == null) {
//	    return 1000;
//	}
//	return Math.sqrt(sqr(xY.getX() - prevXY.getX()) + sqr(xY.getY() - prevXY.getY()));
//    }

    /**
     * Extracts a pixel (int x; int y) from the (double x; double y) values.
     *
     */
    protected Pair<Integer, Integer> getPixel(final double x, final double y) {
	return new Pair<>(new Double(Math.floor(x)).intValue(), new Double(Math.floor(y)).intValue()); // upper left corner of the pixel is used
    }

    protected boolean isPixelBusy(final Pair<Integer, Integer> pixel) {
	return getPixelNode(pixel) != null;
    }

    private Node getPixelNode(final Pair<Integer, Integer> pixel) {
	final Map<Integer, Node> nodesInY = pixelNodes.get(pixel.getKey());
	return nodesInY == null ? null : nodesInY.get(pixel.getValue());
    }

    protected Node putPixelNode(final Pair<Integer, Integer> pixel, final Node node) {
	final Map<Integer, Node> nodesInY = pixelNodes.get(pixel.getKey());
	if (nodesInY == null) {
	    pixelNodes.put(pixel.getKey(), new HashMap<Integer, Node>());
	}
	pixelNodes.get(pixel.getKey()).put(pixel.getValue(), node);
	return node;
    }

    protected double pixelThreashould() {
	return DEFAULT_PIXEL_THRESHOLD;
    }

    protected Pair<Double, Double> addLine(final WebEngine webEngine, final P start, final P end, final int size, final boolean drawSpeedValues) {
	final Point2D xY0 = convertWorld2Pixel(webEngine, start.getLatitude(), start.getLongitude());
	final Point2D xY = convertWorld2Pixel(webEngine, end.getLatitude(), end.getLongitude());

	final TrackSegment cachedTrackSegment = trackSegments.get(end);
	final Pair<Integer, Integer> pixel = getPixel((xY0.getX() + xY.getX()) / 2.0, (xY0.getY() + xY.getY()) / 2.0);
	if (drawLines(start, end) && (inside(xY0) || inside(xY)) && !isPixelBusy(pixel)) {
	    countOfProcessed++;

	    if (cachedTrackSegment != null) {
		oldCountOfProcessed++;
		cachedTrackSegment.update(xY0, xY);
		cachedTrackSegment.setVisible(true);
		// cachedTrackSegment.toBack();
		putPixelNode(pixel, cachedTrackSegment);
	    } else {
		newCountOfProcessed++;
		final TrackSegment newTrackSegment = new TrackSegment(start, end);
		newTrackSegment.update(xY0, xY);
		newTrackSegment.setVisible(true);
		trackSegments.put(end, newTrackSegment);
		this.path.getChildren().add(0, newTrackSegment);
		// newTrackSegment.toBack();
		putPixelNode(pixel, newTrackSegment);
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
		text.setX(xY.getX());
		text.setY(xY.getY());
		// text.setFill(/*getColor(end.getSpeed()) */Color.VIOLET);
		text.setStroke(/*getColor(end.getSpeed()) */Color.BLACK);
		// text.setStrokeWidth(2);
		this.path.getChildren().add(text);
	    }
	} else {
	    if (cachedTrackSegment != null) {
		cachedTrackSegment.setVisible(false);
	    }
	}
	return new Pair<Double, Double>(xY.getX(), xY.getY());
    }

    protected boolean inside(final Point2D xY) {
	return xY.getX() >= -webView.getWidth() && xY.getX() <= 2 * webView.getWidth() &&
		xY.getY() >= -webView.getHeight() && xY.getY() <= 2 * webView.getHeight();
    }

    protected boolean drawLines(final P start, final P end) {
	return true;
    }

    protected int zoom(final WebEngine webEngine) {
	return (Integer) webEngine.executeScript("document.map.getZoom()");
    }

    protected void fitToBounds() {
	if (points != null && !points.isEmpty()) {
	    // fit all coordinates to the bounds calculated by existing points
	    webEngine.executeScript("document.viewBounds = new google.maps.LatLngBounds()");
	    for (final P point : points) {
		webEngine.executeScript("document.viewBounds.extend(new google.maps.LatLng(" + point.getLatitude() + "," + point.getLongitude() + "))");
	    }
	    webEngine.executeScript("document.map.fitBounds(document.viewBounds)");
	}
    }

    /**
     * Provides completely new points to this view panel.
     *
     * Includes:
     * 1. Sorting new points in natural ordering to ensure a correct order of they on a track etc.
     * 2. Remove old resources (VERY IMPORTANT)
     * 3. Fit to bounds if necessary.
     * 4. Add new nodes, callouts and other stuff that correspond to new points.
     * 5. Select an entity if it was selected before.
     *
     * @param points
     * @param fitToBounds
     */
    protected void providePoints(final Pair<List<P>, Map<Long, List<P>>> pointsAndEntityPoints, final boolean fitToBounds) {
	freeResources();

	final List<P> points = pointsAndEntityPoints.getKey();

	// This should order points in its natural ordering (see compareTo() method in concrete Point implementation)
	Collections.sort(points);

	this.points.addAll(points);
	this.entityPoints.putAll(pointsAndEntityPoints.getValue());

	if (fitToBounds) {
	    fitToBounds();
	}
    }

    /**
     * Clears all caches for nodes as well as the models (called 'points').
     */
    protected void freeResources() {
	pixelNodes.clear();
	trackSegments.clear();

	entityPoints.clear();
	points.clear();

	if (path != null) {
	    path.getChildren().clear();
	}
    }

    protected static <P> void extendEntityPointsBy(final Map<Long, List<P>> entityPoints, final AbstractEntity entity, final P point) {
	final List<P> list = entityPoints.get(entity.getId());
	if (list == null) {
	    entityPoints.put(entity.getId(), new ArrayList<P>());
	}
	entityPoints.get(entity.getId()).add(point);
    }

    protected P getCorrespondingPoint(final Long entityId) {
	if (entityId == null) {
	    throw new IllegalArgumentException("An id for entity should exist.");
	}
	final List<P> list = entityPoints.get(entityId);
	return list == null ? null : list.get(list.size() - 1); // get the last point in the list
    }

    public P getCorrespondingPoint(final AbstractEntity entity) {
	return entity == null ? null : getCorrespondingPoint(entity.getId());
    }

    public WebEngine getWebEngine() {
	return webEngine;
    }

    protected void panBy(final double deltaXForPan, final double deltaYForPan) {
	System.err.println("document.panBy(" + deltaXForPan + ", " + deltaYForPan + ")");
	webEngine.executeScript("document.panBy(" + deltaXForPan + ", " + deltaYForPan + ")");
    }

    public void centerBy(final double longitude, final double latitude) {
	webEngine.executeScript("document.setCenter(" + latitude + "," + longitude + ")");
    }

    @Override
    public final void clickedAction(final P point) {
	parentView.deselectAll();

	final AbstractEntity<?> entityToSelect = entityToSelect(point);

	parentView.selectEntities(Arrays.<AbstractEntity<?>> asList(entityToSelect));
	parentView.bringToView(entityToSelect);

	requestFocusForEgi();
    }

    private static Double get(final JSObject p, final String what) {
	final String s = p.call(what).toString();
	return new Double(s);
    }
}
