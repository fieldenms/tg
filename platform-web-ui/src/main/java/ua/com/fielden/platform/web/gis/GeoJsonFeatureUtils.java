package ua.com.fielden.platform.web.gis;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.Calculated;
import ua.com.fielden.platform.reflection.AnnotationReflector;
import ua.com.fielden.platform.reflection.Finder;
import ua.com.fielden.platform.reflection.TitlesDescsGetter;

public abstract class GeoJsonFeatureUtils {
    private final static Logger logger = Logger.getLogger(GeoJsonFeatureUtils.class);
    
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
    
    
    /**
     * Converts a couple of entities into list of features representing them.
     *
     * @param entities
     * @return
     */
    public abstract List<String> convertToGeoJsonFeatures(final List<AbstractEntity<?>> entities, final ICentreDomainTreeManagerAndEnhancer cdtmae);
}
