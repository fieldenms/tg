package ua.com.fielden.platform.web.gis;

import java.awt.event.MouseAdapter;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker.State;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import netscape.javascript.JSObject;

import org.apache.log4j.Logger;

import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.gis.Point;
import ua.com.fielden.platform.javafx.gis.gps.IWebViewLoadedListener;
import ua.com.fielden.platform.javafx.gis.gps.WebViewLoadEvent;
import ua.com.fielden.platform.pagination.IPage;
import ua.com.fielden.platform.pagination.IPageChangedListener;
import ua.com.fielden.platform.pagination.PageChangedEvent;
import ua.com.fielden.platform.pagination.PageHolder;
import ua.com.fielden.platform.swing.dialogs.DialogWithDetails;
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
public abstract class GisViewPanel2<T extends AbstractEntity<?>, P extends Point> extends JFXPanel /* implements IPoint<P> */{
    private static final long serialVersionUID = 9202827128855362320L;
    private static final Logger logger = Logger.getLogger(GisViewPanel2.class);

    private final EntityGridInspector egi;
    private final GridAnalysisView<T, ICentreDomainTreeManagerAndEnhancer> parentView;
    private WebView webView;
    private WebEngine webEngine;
    //    private BorderPane root;
    private StackPane webViewPanel;

    private AbstractEntity<?> previousSelectedEntity;
    private boolean mouseClicked = false;

    protected final AbstractEntity<?> selectedEntity() {
        return parentView.getEnhancedSelectedAbstractEntity();
    }

    /**
     * Creates a swing container with javaFx dashboard.
     *
     * @return
     */
    public GisViewPanel2(final GridAnalysisView<T, ICentreDomainTreeManagerAndEnhancer> parentView, final EntityGridInspector egi, final ListSelectionModel listSelectionModel, final PageHolder pageHolder) {
        this.parentView = parentView;
        this.egi = egi;
        setFocusable(false);
        Platform.setImplicitExit(false);

        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                // This method is invoked on the JavaFX thread
                final Scene scene = createScene();
                setScene(scene);
            }
        });

        listSelectionModel.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(final ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    // logger.info("ListSelectionListener valueChanged");
                    if (mouseClicked) {
                        // logger.info("mouseClicked = false");
                        mouseClicked = false;
                    } else {
                        // TODO
                        // selectEntity(calloutChangeShouldBeForced);
                    }
                }
                return;
            }
        });

        this.egi.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(final java.awt.event.MouseEvent e) {
                // logger.info("\tmousePressed " + e);
                // logger.info("mouseClicked = true");
                mouseClicked = true;
            }

            @Override
            public void mouseClicked(final java.awt.event.MouseEvent e) {
                final int index = egi.rowAtPoint(e.getPoint());
                if (index != -1) {
                    selectEntity(true);
                }
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

                                // providePoints(createPoints((IPage<AbstractEntity<?>>) e.getNewPage()), shouldFitToBounds());
                                // removeOldAndAddNew(webEngine, zoom(webEngine));
                                logger.info("Converting entities to geo json...");
                                // final String geoJsonFeatures = convertToGeoJson((IPage<AbstractEntity<?>>) e.getNewPage());
                                final List<String> geoJsonFeatures = convertToGeoJsonFeatures((IPage<AbstractEntity<?>>) e.getNewPage());

                                logger.info("Saving to geo json file...");
                                try {
                                    final PrintWriter out = new PrintWriter("geo.json");
                                    // out.println("var geoJsonFeatures = " + geoJsonFeatures);
                                    out.close();
                                } catch (final Exception ex) {
                                    ex.printStackTrace();
                                }

                                executeScript("geoJsonOverlay.clearLayers();");
                                executeScript("markersClusterGroup.clearLayers();");
                                logger.info("Executing script [geoJsonFeatures = {...};]...");
                                // executeScript("geoJsonFeatures = " + geoJsonFeatures + ";", false);
                                // executeScript("geoJsonOverlay.addData(geoJsonFeatures);");
                                int i = 0;
                                logger.info("Adding features [" + geoJsonFeatures.size() + "]...");
                                for (final String feature : geoJsonFeatures) {
                                    i++;
                                    // logger.info("Adding feature [" + i + " / " + geoJsonFeatures.size() + "]...");
                                    executeScript("geoJsonOverlay.addData(" + feature + ");", false);
                                }
                                executeScript("markersClusterGroup.addLayer(geoJsonOverlay);");
                                executeScript("map.fitBounds(markersClusterGroup.getBounds());");
                                logger.info("Scripts have been executed.");
                            }
                        });
                    }
                });
            }
        });
    }

    private static List<String> convertToGeoJsonFeatures(final IPage<AbstractEntity<?>> newPage) {
        final List<AbstractEntity<?>> entities = newPage.data();
        logger.info("Converting to geo json [" + entities.size() + "] entities...");
        // final StringBuilder sb = new StringBuilder();
        final List<String> features = new ArrayList<>();
        final Iterator<AbstractEntity<?>> iter = entities.iterator();

        if (iter.hasNext()) {
            // at least one:
            final AbstractEntity<?> first = iter.next();
            features.add(createPointFeature(first));

            if (iter.hasNext()) {
                // at least two:
                final AbstractEntity<?> second = iter.next();
                features.add(createPointFeature(second));

                while (iter.hasNext()) {
                    features.add(createPointFeature(iter.next()));
                }

                features.add(createLineStringFeature(entities)); // TODO MultiLineString for different machines and / or different parts of track
            }

        }
        return features;
    }

    private static String convertToGeoJson(final IPage<AbstractEntity<?>> newPage) {
        final List<AbstractEntity<?>> entities = newPage.data();
        logger.info("Converting to geo json [" + entities.size() + "] entities...");
        final StringBuilder sb = new StringBuilder();
        final Iterator<AbstractEntity<?>> iter = entities.iterator();

        if (iter.hasNext()) {
            // at least one:
            final AbstractEntity<?> first = iter.next();
            sb.append(createPointFeature(first));

            if (iter.hasNext()) {
                // at least two:
                final AbstractEntity<?> second = iter.next();
                sb.append("," + createPointFeature(second));

                while (iter.hasNext()) {
                    sb.append("," + createPointFeature(iter.next()));
                }

                sb.append("," + createLineStringFeature(entities)); // TODO MultiLineString for different machines and / or different parts of track
            }

        }
        return "[" + sb.toString() + "]";
    }

    private static String createLineStringFeature(final List<AbstractEntity<?>> entities) {
        return "{" + "\"type\": \"Feature\"," + "\"geometry\": " + createLineStringGeometry(entities) + "," + "\"properties\": " + createProperties(entities) + "}";
    }

    private static String createProperties(final List<AbstractEntity<?>> entities) {
        return "{" + "\"popupContent\": " + entityToString((AbstractEntity<?>) entities.get(0).get("machine")) + // TODO
                "}";
    }

    private static String createLineStringGeometry(final List<AbstractEntity<?>> entities) {
        return "{" + "\"type\": \"LineString\"," + "\"coordinates\": " + createCoordinates(entities) + "}";
    }

    private static String createCoordinates(final List<AbstractEntity<?>> entities) {
        final StringBuilder sb = new StringBuilder();
        final Iterator<AbstractEntity<?>> iter = entities.iterator();
        sb.append(createCoordinates(iter.next()));
        sb.append("," + createCoordinates(iter.next()));
        while (iter.hasNext()) {
            sb.append("," + createCoordinates(iter.next()));
        }
        return "[" + sb.toString() + "]";
    }

    private static String createPointFeature(final AbstractEntity<?> entity) {
        return "{" + "\"type\": \"Feature\"," + "\"geometry\": " + createPointGeometry(entity) + "," + "\"properties\": " + createProperties(entity) + "}";
    }

    private static String createProperties(final AbstractEntity<?> entity) {
        return "{" + "\"popupContent\": " + dateToString((Date) entity.get("gpsTime")) + "," + "\"vectorAngle\": " + integerToString((Integer) entity.get("vectorAngle")) + ","
                + "\"vectorSpeed\": " + integerToString((Integer) entity.get("vectorSpeed")) + "}";
    }

    private final static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

    private static String dateToString(final Date date) {
        return "\"" + sdf.format(date) + "\"";
    }

    private static String entityToString(final AbstractEntity<?> entity) {
        return "\"" + entity.toString() + "\"";
    }

    private static String createPointGeometry(final AbstractEntity<?> entity) {
        return "{" + "\"type\": \"Point\"," + "\"coordinates\": " + createCoordinates(entity) + "}";
    }

    private static String createCoordinates(final AbstractEntity<?> entity) {
        return "[" + bigDecimalToString((BigDecimal) entity.get("x")) + "," + bigDecimalToString((BigDecimal) entity.get("y")) + ","
                + integerToString((Integer) entity.get("altitude")) + "]";
    }

    private static String integerToString(final Integer integer) {
        return integer.toString();
    }

    private static String bigDecimalToString(final BigDecimal bigDecimal) {
        return bigDecimal.toPlainString();
    }

    protected abstract Pair<List<P>, Map<Long, List<P>>> createPoints(final IPage<AbstractEntity<?>> entitiesPage);

    //protected abstract void findAndSelectPoint(final AbstractEntity<?> selectedEntity, final AbstractEntity<?> unselectedEntity, final boolean forceCalloutChange);

    protected abstract boolean shouldFitToBounds();

    protected abstract AbstractEntity<?> entityToSelect(final P point);

    protected void requestFocusForEgi() {
        if (!egi.hasFocus()) {
            SwingUtilitiesEx.invokeLater(new Runnable() {
                @Override
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
        logger.info("select entity...[forceCalloutChange == " + forceCalloutChange + "]");
        final AbstractEntity<?> unselectedEntity = previousSelectedEntity;
        final AbstractEntity<?> selectedEntity = selectedEntity();
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                // TODO findAndSelectPoint(selectedEntity, unselectedEntity, forceCalloutChange);
                previousSelectedEntity = selectedEntity;
            }
        });
        logger.info("select entity...refered to JFX thread");
    }

    public Scene createScene() {
        // create web engine and view
        webView = new WebView();
        webEngine = webView.getEngine();

        webEngine.getLoadWorker().stateProperty().addListener(new ChangeListener<State>() {
            @Override
            public void changed(final ObservableValue ov, final State oldState, final State newState) {
                if (newState == State.SUCCEEDED) {
                    afterMapLoaded();
                    fireWebViewLoadEvent(new WebViewLoadEvent(GisViewPanel2.this, State.SUCCEEDED));
                } else if (newState == State.FAILED) {
                    webEngine.reload();
                    fireWebViewLoadEvent(new WebViewLoadEvent(GisViewPanel2.this, State.FAILED));

                    // invokeErrorDialog(new Exception("Web view page loading has been failed."));
                } else {
                    // other states like SCHEDULLED or READY or RUNNING are not interesting
                }
            }
        });

        loadMap();

        //        final Button fitToBounds = new Button("Fit To Bounds");
        //        fitToBounds.setOnAction(new EventHandler<ActionEvent>() {
        //            public void handle(final ActionEvent actionEvent) {
        //                fitToBounds();
        //            }
        //        });

        //        // create toolbar
        //        final ToolBar toolBar = new ToolBar();
        //        toolBar.getStyleClass().add("map-toolbar");
        //        toolBar.getItems().addAll(createSpacer(), fitToBounds);

        //        // create root
        //        root = new BorderPane();
        //        root.getStyleClass().add("map");

        webViewPanel = new StackPane();

        ///////////////////// Dragging and scrolling logic ////////////
        // TODO !!!!!!!!!!!!!!!!!!!!!!!!!!!!!                     webView.setDisable(true); // need to disable web view as it steals mouse events

        ////////////////////////////////

        // webViewPanel.setCenter(webView);
        webViewPanel.getChildren().add(webView);
        //        canvas = new BorderPane();
        //        webViewPanel.getChildren().add(canvas);

        //        root.setCenter(webViewPanel);
        //        root.setTop(toolBar);

        final Scene scene = new Scene(webViewPanel, /*1100, 600, */Color.web("#666970"));

        return scene;
    }

    protected void afterMapLoaded() {
        // executeScript("document.checkLoadedScripts()");
    }

    private void loadMap() {
        try {
            webEngine.load(GisViewPanel2.class.getResource("map.html").toString());
        } catch (final Exception e) {
            e.printStackTrace();
            invokeErrorDialog(e);
        }
    }

    private Node createSpacer() {
        final Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        return spacer;
    }

    //    @Override
    //    public Color getColor(final P start) {
    //        final int speed = start.getSpeed();
    //        final double maxSpeed = 80.0;
    //        // return Color.hsb(0, speed / maxSpeed, 1.0);
    //        // return Color.hsb(120.0 - (120.0 * speed / maxSpeed), 1.0, 1.0); // 0 (green, hue=120) to 80 (red, hue=0) km/hour
    //        return Color.hsb(240.0 + ((360.0 - 240.0) * speed / maxSpeed), 1.0, 1.0); // 0 (dark blue, hue=? 120) to 80 (red, hue=0) km/hour
    //    }
    //
    //    @Override
    //    public String getTooltip(final P point) {
    //        return point.toString();
    //    }

    protected void fitToBounds() {
        // TODO provide map-independent implementation
        // TODO provide map-independent implementation
        // TODO provide map-independent implementation
        // TODO provide map-independent implementation
        // TODO provide map-independent implementation
        // TODO provide map-independent implementation
        // TODO provide map-independent implementation

        //        if (points != null && !points.isEmpty()) {
        //            // fit all coordinates to the bounds calculated by existing points
        //            executeScript("document.viewBounds = new google.maps.LatLngBounds()");
        //            for (final P point : points) {
        //                executeScript("document.viewBounds.extend(new google.maps.LatLng(" + point.getLatitude() + "," + point.getLongitude() + "))");
        //            }
        //            executeScript("document.map.fitBounds(document.viewBounds)");
        //        }
    }

    /**
     * Executes javaScript script. If execution fails -- it reports that in dialog window (there is a need to reload all centre).
     *
     * @param jsString
     * @return
     */
    protected Object executeScript(final String jsString) {
        return executeScript(jsString, true);
    }

    /**
     * Executes javaScript script. If execution fails -- it reports that in dialog window (there is a need to reload all centre).
     *
     * @param jsString
     * @return
     */
    protected Object executeScript(final String jsString, final boolean log) {
        try {
            if (log) {
                logger.info("Executing script [" + jsString + "]...");
            }
            return webEngine.executeScript(jsString);
        } catch (final Exception e) {
            e.printStackTrace();

            invokeErrorDialog(e);
            return null;
            // FIXME executeScript(jsString);
        }
    }

    private void invokeErrorDialog(final Exception e) {
        SwingUtilitiesEx.invokeLater(new Runnable() {
            @Override
            public void run() {
                new DialogWithDetails(null, "Перевантажте центр (перервано звязок з інтернетом)", e).setVisible(true);
            }
        });
    }

    /**
     * Provides completely new points to this view panel.
     *
     * Includes: 1. Sorting new points in natural ordering to ensure a correct order of they on a track etc. 2. Remove old resources (VERY IMPORTANT) 3. Fit to bounds if necessary.
     * 4. Add new nodes, callouts and other stuff that correspond to new points. 5. Select an entity if it was selected before.
     *
     * @param points
     * @param fitToBounds
     */
    protected void providePoints(final Pair<List<P>, Map<Long, List<P>>> pointsAndEntityPoints, final boolean fitToBounds) {
        // TODO provide similar implementation
        //        freeResources();
        //
        //        final List<P> points = pointsAndEntityPoints.getKey();
        //
        //        // This should order points in its natural ordering (see compareTo() method in concrete Point implementation)
        //        Collections.sort(points);
        //
        //        this.points.addAll(points);
        //        this.entityPoints.putAll(pointsAndEntityPoints.getValue());
        //
        //        if (fitToBounds) {
        //            fitToBounds();
        //        }
    }

    /**
     * Clears all caches for nodes as well as the models (called 'points').
     */
    protected void freeResources() {
        // TODO provide similar implementation
        //        pixelNodes.clear();
        //        trackSegments.clear();
        //
        //        entityPoints.clear();
        //        points.clear();
        //
        //        if (path != null) {
        //            path.getChildren().clear();
        //        }
    }

    public WebEngine getWebEngine() {
        return webEngine;
    }

    //    @Override
    //    public final void clickedAction(final P point) {
    //        parentView.deselectAll();
    // TODO provide similar implementation
    //
    //        final AbstractEntity<?> entityToSelect = entityToSelect(point);
    //
    //        parentView.selectEntities(Arrays.<AbstractEntity<?>> asList(entityToSelect));
    //        parentView.bringToView(entityToSelect);
    //
    //        requestFocusForEgi();
    //    }

    public void addWebViewLoadListener(final IWebViewLoadedListener listener) {
        listenerList.add(IWebViewLoadedListener.class, listener);
    }

    public void removeWebViewLoadListener(final IWebViewLoadedListener listener) {
        listenerList.remove(IWebViewLoadedListener.class, listener);
    }

    private void fireWebViewLoadEvent(final WebViewLoadEvent e) {
        SwingUtilitiesEx.invokeLater(new Runnable() {
            @Override
            public void run() {
                for (final IWebViewLoadedListener listener : listenerList.getListeners(IWebViewLoadedListener.class)) {
                    listener.webViewLoaded(e);
                }
            }
        });
    }

    private static Double get(final JSObject p, final String what) {
        final String s = p.call(what).toString();
        return new Double(s);
    }
}
