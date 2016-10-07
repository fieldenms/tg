package ua.com.fielden.platform.web.gis.gps.stop;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;

import javax.swing.ListSelectionModel;

import tg.main.menu.tablecode.MiMessage;
import tg.tablecode.Message;
import tg.tablecode.Stop;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.centre.impl.CentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.pagination.PageHolder;
import ua.com.fielden.platform.serialisation.api.ISerialiser;
import ua.com.fielden.platform.swing.egi.EntityGridInspector;
import ua.com.fielden.platform.web.gis.GisViewPanel2;
import ua.com.fielden.platform.web.gis.gps.GpsGridAnalysisModel2;
import ua.com.fielden.platform.web.gis.gps.GpsGridAnalysisView2;

public class StopGisViewPanel2 extends GisViewPanel2<Stop> {
    private final ICentreDomainTreeManagerAndEnhancer defaultMessagesCdtme;

    public StopGisViewPanel2(final GpsGridAnalysisView2<Stop, ?> parentView, final EntityGridInspector egi, final ListSelectionModel listSelectionModel, final PageHolder pageHolder) {
        super(parentView, egi, listSelectionModel, pageHolder);
        defaultMessagesCdtme = createDefaultMessagesCdtme(this.parentView().getModel().getSerialiser());
    }

    @Override
    protected String mapPage() {
        return "http://localhost:1691/gis/stop.html";
    }

    @Override
    protected List<String> convertToGeoJsonFeatures(final List<AbstractEntity<?>> entities) {
        return convertToGeoJsonFeatures2(entities, //
                (e) -> {
                    final Stop stop = (Stop) e;
                    final List<AbstractEntity<?>> messages = sortAndConvert(stop.getMessages());
                    return createTrackFeatures(messages, this.parentView().getModel(), createCircleFeature(stop), defaultMessagesCdtme);
                },

                //                (e) -> createFeature(e, //
                //                        entity -> createFeatures(entity, this.parentView().getModel()) //
                //                ), //
                (es) -> null // no 'grouping feature' is needed
        );
    }

    //    private static String createFeatures(final AbstractEntity<?> entity, final GpsGridAnalysisModel2<Stop> model) {
    //        // final Iterator<Coordinate> coordIter = sortedCoords(entity).iterator();
    //        final Stop stop = (Stop) entity;
    //        final List<AbstractEntity<?>> messages = sortAndConvert(stop.getMessages());
    //
    //        return "[" //
    //                   // + createCircleFeature(stop) + "," // TODO
    //                + createTrackFeature(messages, model) //
    //                + "]";
    //    }

    private String createCircleFeature(final Stop stop) {
        final Function<AbstractEntity<?>, String> createCircleCoordsFun = createCircleCoordsFun();
        final Function<AbstractEntity<?>, String> createGeometry = createCircleGeometryFun(createCircleCoordsFun);
        final Function<AbstractEntity<?>, String> createProperties = createCirclePropertiesFun(this.parentView().getModel().getCdtme(), Stop.class);
        return "{" //
                + "\"type\": \"Feature\"," //
                + "\"id\": \"stop_" + UUID.randomUUID() /*stop.getId().toString()*/+ "\"," // the identification will be done by AbstractEntity's "id" property for the main entity (e.g. Machine, Message or GeoFenceEvent)
                + "\"geometry\": " + createGeometry.apply(stop) + "," //
                + "\"properties\": " + createProperties.apply(stop) //
                + "}"; //
    }

    protected static Function<AbstractEntity<?>, String> createCircleGeometryFun(final Function<AbstractEntity<?>, String> createCircleCoordsFun) {
        //        return entity -> "{" //
        //                + "\"type\": \"Point\"," //
        //                + "\"coordinates\": " + createCircleCoordsFun.apply(entity) //
        //                + "}"; //
        return entity -> "{" //
                + "\"type\": \"Polygon\"," //
                + "\"coordinates\": " + createCircleCoordsFun.apply(entity) //
                + "}"; //
    }

    protected static <T extends AbstractEntity<?>> Function<AbstractEntity<?>, String> createCirclePropertiesFun(final ICentreDomainTreeManagerAndEnhancer stopsCdtme, final Class<?> stopsType) {
        return entity -> "{" //
                + "\"popupContent\": " + popupText(entity, stopsCdtme, stopsType) + "," //
                + "\"what\": " + "\"circle\"" //
                + "}"; //
    }

    protected static Function<AbstractEntity<?>, String> createCircleCoordsFun() {
        return entity -> {
            final BigDecimal baryCentreX = (BigDecimal) entity.get("baryCentreX");
            final BigDecimal baryCentreY = (BigDecimal) entity.get("baryCentreY");
            final BigDecimal radius = (BigDecimal) entity.get("radius");
            final double coefficient = 0.000013411; // meters to long/lat distance
            final double r = radius.doubleValue() * coefficient; // TODO find out appropriate coefficient
            final StringBuilder sb = new StringBuilder();

            final int n = 30;
            // sb.append(coords(baryCentreX, baryCentreY));
            for (int i = 0; i < n; i++) {
                final double t = 2 * Math.PI * i / n;
                final double x = baryCentreX.doubleValue() + r * Math.cos(t);
                final double y = baryCentreY.doubleValue() + r * Math.sin(t);
                sb.append((i == 0 ? "" : ",") + coords(new BigDecimal(x), new BigDecimal(y)));
            }

            return "[[" + sb.toString() + "]]";
        };
        //        return entity -> "[" //
        //                + bigDecimalToString((BigDecimal) entity.get("baryCentreX")) + "," //
        //                + bigDecimalToString((BigDecimal) entity.get("baryCentreY")) //
        //                + "]";
    }

    private static String coords(final BigDecimal x, final BigDecimal y) {
        return "[" //
                + bigDecimalToString(x) + "," //
                + bigDecimalToString(y) //
                + "]";
    }

    private static List<String> createTrackFeatures(final List<AbstractEntity<?>> messages, final GpsGridAnalysisModel2<Stop> model, final String additionalFeature, final ICentreDomainTreeManagerAndEnhancer defaultMessagesCdtme) {
        final Function<AbstractEntity<?>, String> createMessageCoords = createMessageCoordsFun();
        final List<String> features = convertToGeoJsonFeatures(messages, //
                (e) -> createFeature(e, //
                        createMessageGeometryFun(createMessageCoords), //
                        createMessagePropertiesFun(createMessageCoords, defaultMessagesCdtme, Message.class) //
                ), //
                (es) -> createLineStringFeature(es, createMessageCoords) //
        );
        features.add(additionalFeature);
        return features;
    }

    private static ICentreDomainTreeManagerAndEnhancer createDefaultMessagesCdtme(final ISerialiser serialiser) {
        final CentreDomainTreeManagerAndEnhancer c = new CentreDomainTreeManagerAndEnhancer(serialiser, new HashSet<Class<?>>() {
            {
                add(Message.class);
            }
        });
        // initialise checkedProperties tree to make it more predictable in getting meta-info from "checkedProperties"
        c.getFirstTick().checkedProperties(Message.class);
        c.getSecondTick().checkedProperties(Message.class);

        return MiMessage.createCentreConfigurator().configCentre(c);
    }

    private static List<AbstractEntity<?>> sortAndConvert(final Set<Message> messages) {
        // TODO sort??// TODO sort??
        // TODO sort??
        // TODO sort??
        // TODO sort??
        // TODO sort??
        // TODO sort??

        return new ArrayList<AbstractEntity<?>>() { // TODO sort??
            {
                addAll(messages);
            }
        };
    }
}
