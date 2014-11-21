package ua.com.fielden.platform.web.gis;

import java.awt.event.MouseAdapter;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker.State;
import javafx.embed.swing.JFXPanel;
import javafx.event.EventDispatcher;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import netscape.javascript.JSObject;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.centre.impl.CentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.Calculated;
import ua.com.fielden.platform.javafx.gis.gps.IWebViewLoadedListener;
import ua.com.fielden.platform.javafx.gis.gps.WebViewLoadEvent;
import ua.com.fielden.platform.pagination.IPage;
import ua.com.fielden.platform.pagination.IPageChangedListener;
import ua.com.fielden.platform.pagination.PageChangedEvent;
import ua.com.fielden.platform.pagination.PageHolder;
import ua.com.fielden.platform.reflection.AnnotationReflector;
import ua.com.fielden.platform.reflection.Finder;
import ua.com.fielden.platform.reflection.TitlesDescsGetter;
import ua.com.fielden.platform.serialisation.api.ISerialiser;
import ua.com.fielden.platform.serialisation.api.SerialiserEngines;
import ua.com.fielden.platform.swing.dialogs.DialogWithDetails;
import ua.com.fielden.platform.swing.egi.EntityGridInspector;
import ua.com.fielden.platform.swing.utils.SwingUtilitiesEx;
import ua.com.fielden.platform.web.gis.gps.GpsGridAnalysisModel2;
import ua.com.fielden.platform.web.gis.gps.GpsGridAnalysisView2;

/**
 * A swing container to contain javaFx dashboard.
 *
 * @author TG Team
 *
 */
public abstract class GisViewPanel2<T extends AbstractEntity<?>> extends JFXPanel /* implements IPoint<P> */{
    private static final long serialVersionUID = 9202827128855362320L;
    private static final Logger logger = Logger.getLogger(GisViewPanel2.class);

    private final EntityGridInspector egi;
    private final GpsGridAnalysisView2<T, ?> parentView;
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
    public GisViewPanel2(final GpsGridAnalysisView2<T, ?> parentView, final EntityGridInspector egi, final ListSelectionModel listSelectionModel, final PageHolder pageHolder, final ISerialiser serialiser /*, final EntityFactory entityFactory*/) {
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
                                // final List<String> geoJsonFeatures = convertToGeoJsonFeatures(((IPage<AbstractEntity<?>>) e.getNewPage()).data());

                                try {
                                    final PrintWriter out0 = new PrintWriter("entities.js");
                                    //                                    final TgObjectMapper tgObjectMapper = new TgObjectMapper(sdf, entityFactory, () -> new ArrayList<Class<?>>()) {
                                    //                                        private static final long serialVersionUID = 1L;
                                    //
                                    //                                        @Override
                                    //                                        protected void registerAbstractEntitySerialiser() {
                                    //                                            addSerialiser(AbstractEntity.class, new AbstractEntityToGeoJsonSerialiser());
                                    //                                        }
                                    //                                    };
                                    //                                    final String entitiesString = tgObjectMapper.writeValueAsString(((IPage<AbstractEntity<?>>) e.getNewPage()).data());
                                    final String entitiesString = serialiser.serialise(((IPage<AbstractEntity<?>>) e.getNewPage()).data(), SerialiserEngines.JACKSON).toString();

                                    logger.info("Saving entities to geo json file...");
                                    out0.println(entitiesString);
                                    logger.info("Saving entities to geo json file...done");

                                    executeScript("gisComponent.initReload();");
                                    executeScript("gisComponent.clearAll();");

                                    executeScript("gisComponent.promoteEntitiesString('" + entitiesString + "');", false);

                                    executeScript("gisComponent._markerCluster.setShouldFitToBounds(" + shouldFitToBounds() + "); ");
                                    executeScript("gisComponent._markerCluster.getGisMarkerClusterGroup().addLayer(gisComponent._geoJsonOverlay);");
                                    executeScript("gisComponent.finishReload();");
                                    logger.info("Scripts have been executed.");

                                    out0.close();

                                    //                                    final PrintWriter out = new PrintWriter("geo.json");
                                    //                                    out.println("define([], function() { " //
                                    //                                            + " var Initialiser = function() { " //
                                    //                                            + " this._geoJsonFeatures = " + convertToGeoJson(geoJsonFeatures) + "; " //
                                    //                                            + " this.geoJsonFeatures = function() { " //
                                    //                                            + " return this._geoJsonFeatures; " + " }; " //
                                    //                                            + " }; " //
                                    //                                            + " return Initialiser; " //
                                    //                                            + " }); ");
                                    //                                    out.close();

                                } catch (final Exception ex) {
                                    ex.printStackTrace();
                                }

                                try {
                                    final PrintWriter out0 = new PrintWriter("entityCentre.js");
                                    //                                    final TgObjectMapper tgObjectMapper = new TgObjectMapper(entityFactory, () -> new ArrayList<Class<?>>()) {
                                    //
                                    //                                        private static final long serialVersionUID = 1L;
                                    //
                                    //                                        @Override
                                    //                                        protected void registerAbstractEntitySerialiser() {
                                    //                                            addSerialiser(AbstractEntity.class, new AbstractEntityToGeoJsonSerialiser());
                                    //                                        }
                                    //                                    };
                                    final CentreDomainTreeManagerAndEnhancer entityCentre = (CentreDomainTreeManagerAndEnhancer) GisViewPanel2.this.parentView().getModel().getCdtme();
                                    //                                    final String entityCentreString = tgObjectMapper.writeValueAsString(entityCentre);
                                    final String entityCentreString = serialiser.serialise(entityCentre, SerialiserEngines.JACKSON).toString();

                                    logger.info("Saving entityCentre to geo json file...");
                                    out0.println(entityCentreString);
                                    logger.info("Saving entityCentre to geo json file...done");

                                    executeScript("gisComponent.promoteEntityCentreString('" + entityCentreString + "');", false);
                                    logger.info("Scripts have been executed.");

                                    out0.close();
                                } catch (final Exception ex) {
                                    ex.printStackTrace();
                                }

                                //                                executeScript("gisComponent.initReload();");
                                //                                executeScript("gisComponent.clearAll();");
                                //                                int i = 0;
                                //                                final int featuresSize = geoJsonFeatures.size();
                                //                                logger.info("Adding features [" + featuresSize + "]...");
                                //                                for (final String feature : geoJsonFeatures) {
                                //                                    i++;
                                //                                    if (i % 50 == 0 || i == featuresSize) {
                                //                                        logger.info("Adding feature [" + i + " / " + featuresSize + "]...");
                                //                                    }
                                //                                    executeScript("gisComponent._geoJsonOverlay.addData(" + feature + ");", false);
                                //                                }
                                //                                executeScript("gisComponent._markerCluster.setShouldFitToBounds(" + shouldFitToBounds() + "); ");
                                //                                executeScript("gisComponent._markerCluster.getGisMarkerClusterGroup().addLayer(gisComponent._geoJsonOverlay);");
                                //                                executeScript("gisComponent.finishReload();");
                                //                                logger.info("Scripts have been executed.");
                            }
                        });
                    }
                });
            }
        });
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////// GENERIC GEOJSON FACTORIES ///////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Converts a couple of entities into list of features representing them.
     *
     * @param entities
     * @return
     */
    protected abstract List<String> convertToGeoJsonFeatures(final List<AbstractEntity<?>> entities);

    /**
     * Converts a couple of entities into list of features representing them. One entity -- one feature, + one summary feature for all entities.
     *
     * @param entities
     * @param createFeature
     * @param createSummaryFeature
     * @return
     */
    protected static List<String> convertToGeoJsonFeatures(final List<AbstractEntity<?>> entities, //
            final Function<AbstractEntity<?>, String> createFeature, //
            final Function<List<AbstractEntity<?>>, String> createSummaryFeature) {

        logger.info("Converting to geo json [" + entities.size() + "] entities...");
        final List<String> features = new ArrayList<>();
        final Iterator<AbstractEntity<?>> iter = entities.iterator();

        if (iter.hasNext()) {
            // at least one:
            final AbstractEntity<?> first = iter.next();
            final String firstFeature = createFeature.apply(first);
            if (firstFeature != null) {
                features.add(firstFeature);
            }

            if (iter.hasNext()) {
                // at least two:
                final AbstractEntity<?> second = iter.next();
                final String secondFeature = createFeature.apply(second);
                if (secondFeature != null) {
                    features.add(secondFeature);
                }

                while (iter.hasNext()) {
                    final String nextFeature = createFeature.apply(iter.next());
                    if (nextFeature != null) {
                        features.add(nextFeature);
                    }
                }

                final String lineStringFeature = createSummaryFeature.apply(entities); // LineString
                if (lineStringFeature != null) {
                    features.add(lineStringFeature); // TODO MultiLineString for different machines and / or different parts of track
                }
            }
        }
        return features;
    }

    /**
     * Converts a couple of entities into list of features representing them. One entity -- multiple features, + one summary feature for all entities.
     *
     * @param entities
     * @param createFeatures
     * @param createSummaryFeature
     * @return
     */
    protected static List<String> convertToGeoJsonFeatures2(final List<AbstractEntity<?>> entities, //
            final Function<AbstractEntity<?>, List<String>> createFeatures, //
            final Function<List<AbstractEntity<?>>, String> createSummaryFeature) {

        logger.info("Converting to geo json [" + entities.size() + "] entities...");
        final List<String> features = new ArrayList<>();
        final Iterator<AbstractEntity<?>> iter = entities.iterator();

        if (iter.hasNext()) {
            // at least one:
            final AbstractEntity<?> first = iter.next();
            features.addAll(createFeatures.apply(first));

            if (iter.hasNext()) {
                // at least two:
                final AbstractEntity<?> second = iter.next();
                features.addAll(createFeatures.apply(second));

                while (iter.hasNext()) {
                    features.addAll(createFeatures.apply(iter.next()));
                }

                final String lineStringFeature = createSummaryFeature.apply(entities); // LineString
                if (lineStringFeature != null) {
                    features.add(lineStringFeature); // TODO MultiLineString for different machines and / or different parts of track
                }
            }
        }
        return features;
    }

    /**
     * Composes a couple of features into single geojson array.
     *
     * @param geoJsonFeatures
     * @return
     */
    protected static String convertToGeoJson(final List<String> geoJsonFeatures) {
        final StringBuilder sb = new StringBuilder();
        final Iterator<String> iter = geoJsonFeatures.iterator();

        if (iter.hasNext()) {
            // at least one:
            final String first = iter.next();
            sb.append(first);

            while (iter.hasNext()) {
                sb.append("," + iter.next());
            }
        }
        return "[" + sb.toString() + "]";
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////// UTILITIES FOR CREATING SUMMARY FEATURE //////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    protected static String createLineStringFeature(final List<AbstractEntity<?>> entities, final Function<AbstractEntity<?>, String> createCoordinates) { // Message entity
        return "{" //
                + "\"type\": \"Feature\"," //
                + "\"id\": \"track-line-string-id\"," // the identification is not necessary
                + "\"geometry\": " + createLineStringGeometry(entities, createCoordinates) + "," //
                + "\"properties\": " + createProperties(entities) //
                + "}";
    }

    protected static String createProperties(final List<AbstractEntity<?>> entities) { // Message entity
        return "{" //
                + "\"popupContent\": " + entityToString((AbstractEntity<?>) entities.get(0).get("machine")) + // TODO
                "}";
    }

    protected static String createLineStringGeometry(final List<AbstractEntity<?>> entities, final Function<AbstractEntity<?>, String> createCoordinates) { // Message entity
        return "{" //
                + "\"type\": \"LineString\"," //
                + "\"coordinates\": " + createCoordinates(entities, createCoordinates) //
                + "}";
    }

    protected static String createCoordinates(final Collection<AbstractEntity<?>> entities, final Function<AbstractEntity<?>, String> createCoordinates) {
        final StringBuilder sb = new StringBuilder();
        final Iterator<AbstractEntity<?>> iter = entities.iterator();
        sb.append(createCoordinates.apply(iter.next()));
        sb.append("," + createCoordinates.apply(iter.next()));
        while (iter.hasNext()) {
            sb.append("," + createCoordinates.apply(iter.next()));
        }
        return "[" + sb.toString() + "]";
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////// UTILITIES FOR CREATING FEATURE //////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    protected static String createFeature(final AbstractEntity<?> entity, final Function<AbstractEntity<?>, String> createGeometry, final Function<AbstractEntity<?>, String> createProperties) {
        return "{" //
                + "\"type\": \"Feature\"," //
                + "\"id\": \"" + entity.getId().toString() + "\"," // the identification will be done by AbstractEntity's "id" property for the main entity (e.g. Machine, Message or GeoFenceEvent)
                + "\"geometry\": " + createGeometry.apply(entity) + "," //
                + "\"properties\": " + createProperties.apply(entity) //
                + "}"; //
    }

    protected static String createFeature(final AbstractEntity<?> entity, final Function<AbstractEntity<?>, String> createFeatures) {
        return "{" //
                + "\"type\": \"FeatureCollection\"," //
                + "\"features\": " + createFeatures.apply(entity) //
                + "}"; //
    }

    protected static Function<AbstractEntity<?>, String> createMessageGeometryFun(final Function<AbstractEntity<?>, String> createMessageCoordsFun) {
        return entity -> "{" //
                + "\"type\": \"Point\"," //
                + "\"coordinates\": " + createMessageCoordsFun.apply(entity) //
                + "}"; //
    }

    protected static <T extends AbstractEntity<?>> Function<AbstractEntity<?>, String> createMessagePropertiesFun(final Function<AbstractEntity<?>, String> createMessageCoordsFun, final ICentreDomainTreeManagerAndEnhancer messagesCdtme, final Class<?> messageType) {
        return entity -> "{" //
                + "\"popupContent\": " + popupText(entity, messagesCdtme, messageType) //
                + (entity.get("vectorAngle") == null ? "" : ("," + "\"vectorAngle\": " + integerToString((Integer) entity.get("vectorAngle"))))//
                + (entity.get("vectorSpeed") == null ? "" : ("," + "\"vectorSpeed\": " + integerToString((Integer) entity.get("vectorSpeed"))))//
                + "}"; //
    }

    protected static Function<AbstractEntity<?>, String> createMessageCoordsFun() {
        return entity -> "[" //
                + bigDecimalToString((BigDecimal) entity.get("x")) + "," //
                + bigDecimalToString((BigDecimal) entity.get("y")) //
                + (entity.get("altitude") == null ? "" : ("," + integerToString((Integer) entity.get("altitude"))))//
                + "]";
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////// VALUES CONVERSION ///////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private final static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

    protected static String dateToString(final Date date) {
        return "\"" + sdf.format(date) + "\"";
    }

    protected static String entityToString(final AbstractEntity<?> entity) {
        return "\"" + entity.toString() + "\"";
    }

    protected static String integerToString(final Integer integer) {
        return integer.toString();
    }

    protected static String bigDecimalToString(final BigDecimal bigDecimal) {
        return bigDecimal.toPlainString();
    }

    protected static String stringToString(final String str) {
        return "\"" + str + "\"";
    }

    private static String valueToString(final Object object) {
        if (object == null) {
            return "";
        } else if (object instanceof Date) {
            return sdf.format((Date) object);
        } else if (object instanceof BigDecimal) {
            return integerToString(((BigDecimal) object).intValue());
        } else if (object instanceof Boolean) {
            return booleanToString((Boolean) object);
        } else {
            return object.toString();
        }
    }

    private static String booleanToString(final Boolean bool) {
        return bool ? "&#x2714" : "&#x2718";
    }

    protected static <T extends AbstractEntity<?>> String popupText(final AbstractEntity<?> entity, final ICentreDomainTreeManagerAndEnhancer cdtme, final Class<?> entityType) {
        // final GpsGridAnalysisModel2<T> model = this.parentView.getModel();
        final StringBuilder popupText = new StringBuilder();
        for (final String resultProp : cdtme.getSecondTick().checkedProperties(entityType)) {
            final String property = StringUtils.isEmpty(resultProp) ? AbstractEntity.KEY : resultProp;
            final Class<?> enhancedType = cdtme.getEnhancer().getManagedType(entityType);
            if (!AnnotationReflector.isAnnotationPresent(Finder.findFieldByName(enhancedType, property), Calculated.class)) {
                // TODO
                // TODO
                // TODO can be calc -- except Calc AGGREGATION_EXPRESSION!
                // TODO
                // TODO
                // TODO
                popupText.append("" + TitlesDescsGetter.getTitleAndDesc(property, enhancedType).getKey() + ": " + valueToString(entity.get(property)) + "<br>");
            }
        }
        return "\"" + popupText.toString() + "\"";
    }

    protected static <T extends AbstractEntity<?>> String popupText(final AbstractEntity<?> entity, final GpsGridAnalysisModel2<T> model) {
        return popupText(entity, model.getCdtme(), model.getEntityType());
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////// OTHER STUFF /////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    protected boolean shouldFitToBounds() {
        return this.parentView.getModel().getFitToBounds();
    }

    protected boolean isFirstQuery() {
        return this.parentView.getModel().isFirstQuery();
    }

    // protected abstract AbstractEntity<?> entityToSelect(final P point);

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

        final EventDispatcher originalDispatcher = webView.getEventDispatcher();
        webView.setEventDispatcher(new MyEventDispatcher(originalDispatcher));

        webView.addEventFilter(ScrollEvent.SCROLL, new EventHandler<ScrollEvent>() {
            @Override
            public void handle(final ScrollEvent e) {
                if (Math.abs(e.getDeltaY()) >= 20.0) {
                    e.consume();
                    final ScrollEvent adjustedEvent = new ScrollEvent(e.getEventType(), e.getX(), e.getY(), e.getScreenX(), e.getScreenY(), e.isShiftDown(), //
                    e.isControlDown(), e.isAltDown(), e.isMetaDown(), e.isDirect(), e.isInertia(), e.getDeltaX(), //
                    e.getDeltaY() / e.getMultiplierY(), // here the value for y delta is turning back to 1.0 or -1.0 instead of multiplied 40.0 or -40.0
                    e.getTotalDeltaX(), e.getTotalDeltaY(), //
                    e.getMultiplierX(), e.getMultiplierY(), // these values do not make any changes
                    e.getTextDeltaXUnits(), e.getTextDeltaX(), e.getTextDeltaYUnits(), e.getTextDeltaY(), e.getTouchCount(), e.getPickResult());
                    webView.fireEvent(adjustedEvent);
                }
            }
        });

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
        // TODO webView.setDisable(true); // need to disable web view as it steals mouse events

        ////////////////////////////////

        // webViewPanel.setCenter(webView);
        webViewPanel.getChildren().add(webView);
        //        canvas = new BorderPane();
        //        webViewPanel.getChildren().add(canvas);

        //        root.setCenter(webViewPanel);
        //        root.setTop(toolBar);

        final JSObject jsobj = (JSObject) webEngine.executeScript("window");
        jsobj.setMember("java", new Js2JavaBridge());

        final Scene scene = new Scene(webViewPanel, /*1100, 600, */Color.web("#666970"));

        return scene;
    }

    protected void afterMapLoaded() {
        //executeScript("if (!document.getElementById('FirebugLite')){E = document['createElement' + 'NS'] && document.documentElement.namespaceURI;E = E ? document['createElement' + 'NS'](E, 'script') : document['createElement']('script');E['setAttribute']('id', 'FirebugLite');E['setAttribute']('src', 'https://getfirebug.com/' + 'firebug-lite.js' + '#startOpened');E['setAttribute']('FirebugLite', '4');(document['getElementsByTagName']('head')[0] || document['getElementsByTagName']('body')[0]).appendChild(E);E = new Image;E['setAttribute']('src', 'https://getfirebug.com/' + '#startOpened');}");
        // executeScript("document.checkLoadedScripts()");

        // executeScript("document.goToLocation(\"Lviv\")");
    }

    protected final void loadMap() {
        try {
            webEngine.load(mapPage());
        } catch (final Exception e) {
            e.printStackTrace();
            invokeErrorDialog(e);
        }
    }

    // GisViewPanel2.class.getResource("main.html").toString()
    protected abstract String mapPage();

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

    protected void invokeErrorDialog(final Exception e) {
        SwingUtilitiesEx.invokeLater(new Runnable() {
            @Override
            public void run() {
                new DialogWithDetails(null, "Перевантажте центр (перервано звязок з інтернетом)", e).setVisible(true);
            }
        });
    }

    //    /**
    //     * Provides completely new points to this view panel.
    //     *
    //     * Includes: 1. Sorting new points in natural ordering to ensure a correct order of they on a track etc. 2. Remove old resources (VERY IMPORTANT) 3. Fit to bounds if necessary.
    //     * 4. Add new nodes, callouts and other stuff that correspond to new points. 5. Select an entity if it was selected before.
    //     *
    //     * @param points
    //     * @param fitToBounds
    //     */
    //    protected void providePoints(final Pair<List<P>, Map<Long, List<P>>> pointsAndEntityPoints, final boolean fitToBounds) {
    //        // TODO provide similar implementation
    //        //        freeResources();
    //        //
    //        //        final List<P> points = pointsAndEntityPoints.getKey();
    //        //
    //        //        // This should order points in its natural ordering (see compareTo() method in concrete Point implementation)
    //        //        Collections.sort(points);
    //        //
    //        //        this.points.addAll(points);
    //        //        this.entityPoints.putAll(pointsAndEntityPoints.getValue());
    //        //
    //        //        if (fitToBounds) {
    //        //            fitToBounds();
    //        //        }
    //    }

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

    protected GpsGridAnalysisView2<T, ?> parentView() {
        return parentView;
    }
}
