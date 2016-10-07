package ua.com.fielden.platform.sample.domain;

import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.from;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.Period;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.dao.QueryExecutionModel;
import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.domaintree.centre.IOrderingRepresentation.Ordering;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.entity.query.model.QueryModel;
import ua.com.fielden.platform.entity_centre.review.DynamicFetchBuilder;
import ua.com.fielden.platform.entity_centre.review.DynamicOrderingBuilder;
import ua.com.fielden.platform.entity_centre.review.DynamicQueryBuilder;
import ua.com.fielden.platform.entity_centre.review.DynamicQueryBuilder.QueryProperty;
import ua.com.fielden.platform.gis.MapUtils;
import ua.com.fielden.platform.utils.Pair;

/**
 * DAO implementation for companion object {@link IStop}.
 *
 * @author Developers
 *
 */
@EntityType(TgStop.class)
public class TgStopDao extends CommonEntityDao<TgStop> implements ITgStop {
    private final ITgMessage messageCompanion;
    private final static Logger logger = Logger.getLogger(TgStopDao.class);
    private static final int SOFT_MESSAGE_COUNT_THRESHOLD = 5;

    @Inject
    public TgStopDao(final IFilter filter, final ITgMessage messageCompanion) {
        super(filter);
        this.messageCompanion = messageCompanion;
    }

    @Override
    @SessionRequired
    protected List<TgStop> getEntitiesOnPage(final QueryExecutionModel<TgStop, ?> queryModel, final Integer pageNumber, final Integer pageCapacity) {
        return retrieveStops(queryModel.getParamValues(), messageCompanion, getEntityFactory());
    }

    public static List<TgStop> retrieveStops(final Map<String, Object> paramValues, final ITgMessage messageCompanion, final EntityFactory factory) {
        return retrieveStops(retrieveMessages(paramValues, messageCompanion), paramValues, factory);
    }

    protected static List<TgMessage> retrieveMessages(final Map<String, Object> paramValues, final ITgMessage messageCompanion) {
        System.out.println("paramValues == " + paramValues);

        logger.info("'Messages' retrieval started at " + new DateTime() + ".");
        final List<QueryProperty> qProperties = new ArrayList<>();

        final QueryProperty machineProperty = new QueryProperty(TgMessage.class, TgMessage.MACHINE_PROP_ALIAS);
        machineProperty.setValue(paramValues.get("machine")); /* TODO!  'Message.MACHINE_PROP_ALIAS + machine'? this should be checked! */
        qProperties.add(machineProperty);

        final QueryProperty orgUnitProperty = new QueryProperty(TgMessage.class, TgMessage.MACHINE_PROP_ALIAS + ".orgUnit");
        orgUnitProperty.setValue(paramValues.get("orgUnit")); /* TODO!  'Message.MACHINE_PROP_ALIAS + orgunit'? this should be checked! */
        qProperties.add(orgUnitProperty);

        final QueryProperty speedProperty = new QueryProperty(TgMessage.class, "vectorSpeed");
        speedProperty.setValue(0); // speed should be in range [0; 6] km / hour
        speedProperty.setValue2(((BigDecimal) paramValues.get("speedThreshould")).intValue());
        qProperties.add(speedProperty);

        //	final QueryProperty geoValidProperty = new QueryProperty(Message.class, "geoValid"); // TODO geoValid has been removed from Message
        //	geoValidProperty.setValue(true); // should be geoValid
        //	geoValidProperty.setValue2(false);
        //	qProperties.add(geoValidProperty);

        final QueryProperty gpsTimeProperty = new QueryProperty(TgMessage.class, "gpsTime");
        gpsTimeProperty.setValue(paramValues.get("gpsTime_from"));
        gpsTimeProperty.setValue2(paramValues.get("gpsTime_to"));
        qProperties.add(gpsTimeProperty);

        final List<Pair<String, Ordering>> orderingProps = Arrays.asList(new Pair<>(TgMessage.MACHINE_PROP_ALIAS + ".orgUnit", Ordering.ASCENDING), new Pair<>(TgMessage.MACHINE_PROP_ALIAS, Ordering.ASCENDING), new Pair<>("gpsTime", Ordering.ASCENDING));

        final QueryExecutionModel<TgMessage, EntityResultQueryModel<TgMessage>> resultQuery = from(DynamicQueryBuilder.createQuery(TgMessage.class, qProperties).model()) //
        .with(DynamicOrderingBuilder.createOrderingModel(TgMessage.class, orderingProps))//
        .with(DynamicFetchBuilder.createFetchOnlyModel(TgMessage.class, new LinkedHashSet<String>(Arrays.asList(TgMessage.MACHINE_PROP_ALIAS, TgMessage.MACHINE_PROP_ALIAS + ".orgUnit", "gpsTime", "x", "y", "vectorSpeed", "vectorAngle", "altitude"))))//
        .lightweight()
        // TODO .with(DynamicParamBuilder.buildParametersMap(enhancedType(), paramMap)).model();
        .model();
        // System.out.println(resultQuery);
        final List<TgMessage> messages = messageCompanion.getAllEntities(resultQuery);
        logger.info("'Messages' retrieval ended at " + new DateTime() + ".");
        return messages;
    }

    @Override
    @SessionRequired
    protected Pair<Integer, Integer> evalNumOfPages(final QueryModel<TgStop> model, final Map<String, Object> paramValues, final int pageCapacity) {
        return Pair.pair(1, 100 /* FIXME ? */);
    }

    /**
     * A concept of stop, that combines a chronological set of messages within a some limited area (e.g. sum of distance < 500 meters, or radius of bounds < 150 meters), when
     * machine moves a little within a relatively large period > 5 minutes.
     *
     * @author TG Team
     *
     */
    private static class StopGroup {
        private final TgMachine machine;
        private final List<TgMessage> messages = new ArrayList<>();
        private Double baryCentreX, baryCentreY;
        private int lastHardStopIndex = -1;
        private Double currX, currY;
        private double distance = 0;

        public StopGroup(final List<TgMessage> previousBeforeFirstHardStopMessages, final TgMessage firstHardStopMessage, final BigDecimal radiusThreshould) {
            extendBy(firstHardStopMessage);

            this.machine = firstHardStopMessage.getMachine();
            extendByPrefixes(firstHardStopMessage, previousBeforeFirstHardStopMessages, radiusThreshould);
        }

        /**
         * Extends (potentially) the stop by the messages which precede the first "hard stop message".
         *
         * @param theMessage
         * @param potentialPrefixes
         *            -- potential stop messages
         * @param radiusThreshould
         */
        private void extendByPrefixes(final TgMessage theMessage, final List<TgMessage> potentialPrefixes, final BigDecimal radiusThreshould) {
            if (!potentialPrefixes.isEmpty()) {
                final int size = potentialPrefixes.size();
                final TgMessage lastPotentialPrefix = potentialPrefixes.get(size - 1);

                if (!isHardStop(lastPotentialPrefix) && isCloseEnough(lastPotentialPrefix, this, radiusThreshould) && areNeighbours(lastPotentialPrefix, theMessage)) {
                    extendBy(lastPotentialPrefix);

                    this.messages.remove(lastPotentialPrefix);
                    this.messages.add(0, lastPotentialPrefix);
                    lastHardStopIndex = this.messages.size() - 1;

                    extendByPrefixes(lastPotentialPrefix, new ArrayList<TgMessage>(potentialPrefixes.subList(0, size - 1)), radiusThreshould);
                }
            }
        }

        public void extendBy(final TgMessage message) {
            checkMessage(message);

            if (machine != null) {
                if (!message.getMachine().getId().equals(machine.getId())) {
                    throw new IllegalArgumentException("Machine should be the same for all stop group messages.");
                }
            }

            this.messages.add(message);

            if (isHardStop(message)) {
                updateBarycenter(message.getX().doubleValue(), message.getY().doubleValue());
                lastHardStopIndex = this.messages.size() - 1;
            }
        }

        private void updateDistance(final double x, final double y) {
            if (currX != null && currY != null) {
                distance = distance + MapUtils.calcDistance(new BigDecimal(currX), new BigDecimal(currY), new BigDecimal(x), new BigDecimal(y));
            }
            currX = x;
            currY = y;
        }

        protected TgMessage getLastHardStopMessage() {
            return messages.get(lastHardStopIndex);
        }

        protected TgMessage firstMessage() {
            return messages.get(0);
        }

        protected TgMessage lastMessage() {
            return messages.get(messages.size() - 1);
        }

        /**
         * Finds a simple "barycentre of vertices". See http://gis.stackexchange.com/questions/22739/how-to-find-the-center-of-geometry-of-an-object for more details.
         *
         * @param x
         * @param y
         */
        private void updateBarycenter(final double x, final double y) {
            if (baryCentreX == null) {
                baryCentreX = x;
                baryCentreY = y;
            } else {
                baryCentreX = (baryCentreX + x) / 2.0;
                baryCentreY = (baryCentreY + y) / 2.0;
            }
        }

        protected void checkMessage(final TgMessage messageToAdd) {
            if (messageToAdd == null) {
                throw new IllegalArgumentException("Message should be definitely not 'null' for stop group calculation.");
            }
            if (messageToAdd.getMachine() == null) {
                throw new IllegalArgumentException("Machine should be definitely known for stop group calculation.");
            }
        }

        public TgMachine getMachine() {
            return machine;
        }

        public long getDuration() {
            return messages.get(messages.size() - 1).getGpsTime().getTime() - messages.get(0).getGpsTime().getTime();
        }

        public BigDecimal getDurationInMinutes() {
            // Period p = new Period(getDuration());
            return new BigDecimal(getDuration() * 1.0 / 60000.0);
        }

        public double radius() {
            double radius = 0;
            final Iterator<TgMessage> iterator = messages.iterator();
            while (iterator.hasNext()) {
                final TgMessage m = iterator.next();
                final double d = distance(m, this);
                if (radius < d) {
                    radius = d;
                }
            }
            return radius;
        }

        @Override
        public String toString() {
            return "Machine [" + machine + "] stop at [" + messages.get(0).getGpsTime() + "; " + messages.get(messages.size() - 1).getGpsTime() + "]:\n" + //
                    "Duration = " + new Period(getDuration()) + "\n" + //
                    "Radius = " + radius();
        }

        /**
         * Calculates the index of the message after "last soft stop" message, that should be included to the stop.
         *
         * @param currentIndex
         * @return
         */
        private int calculateToIndex0(final int currentIndex) {
            final int toIndex;
            if (currentIndex <= messages.size() - 1) { // the message exists (with index currentIndex)
                if (areNeighbours(messages.get(currentIndex - 1), messages.get(currentIndex))) {
                    toIndex = calculateToIndex0(currentIndex + 1);
                } else {
                    toIndex = currentIndex;
                }
            } else { // no message exists (with index currentIndex)
                toIndex = currentIndex;
            }
            return toIndex;
        }

        protected void closeAndAdd(final List<StopGroup> stops) {
            // The beginning and ending of stop should be reduced to one
            // "hard stop message" and several "soft stop messages".
            // The count of "soft stop messages" should be less or equal to
            // SOFT_MESSAGE_COUNT_THRESHOLD. Soft stop messages should be sequential
            // "neighbours", which means that the distance between them <= 250 meters.
            // This guarantees that anomalies will not be included into the stop.
            // Also the distance between message and stop barycentre should be
            // <= than given threshold.

            final int toIndex = calculateToIndex0(lastHardStopIndex + 1);
            final int maxToIndex = lastHardStopIndex + 1 + SOFT_MESSAGE_COUNT_THRESHOLD;
            final int toIndexFinal = toIndex > maxToIndex ? maxToIndex : toIndex;

            final List<TgMessage> newMessages = new ArrayList<TgMessage>(messages.subList(0, toIndexFinal));
            messages.clear();
            messages.addAll(newMessages);
            // update a distance in this stop
            for (final TgMessage m : messages) {
                updateDistance(m.getX().doubleValue(), m.getY().doubleValue());
            }

            stops.add(this);
        }

        public BigDecimal getDistance() {
            return new BigDecimal(distance);
        }

        public Double baryCentreX() {
            return baryCentreX;
        }

        public Double baryCentreY() {
            return baryCentreY;
        }

        private DateTime secHourOfLastDay() {
            final DateTime lastDay = new DateTime(to());
            return lastDay.withHourOfDay(2).withMinuteOfHour(0);
        }

        private boolean isNightStop() {
            return getDuration() / 60000 >= 180 /* more that 3 hours */&& new Interval(new DateTime(from()), new DateTime(to())).contains(secHourOfLastDay());
        }

        public Date from() {
            return firstMessage().getGpsTime();
        }

        public Date to() {
            return lastMessage().getGpsTime();
        }
    }

    private static List<TgStop> retrieveStops(final List<TgMessage> messages, final Map<String, Object> paramValues, final EntityFactory factory) {
        logger.info("'Messages => stop groups' processing started at " + new DateTime() + ".");
        // IMPORTANT: 'messages' should be ordered by 'machine' and 'gpsTime' ASC

        TgMachine currMachine = null;

        // this list should contain a current version of accumulated stops
        final List<StopGroup> stops = new ArrayList<>();
        // this is a currently processed stop (not closed)
        StopGroup currStop = null;
        TgMessage previousMessage = null;

        final BigDecimal radiusThreshould = (BigDecimal) paramValues.get("radiusThreshould");
        final Iterator<TgMessage> iter = messages.iterator();
        final List<TgMessage> allMessages = new ArrayList<>(messages);
        while (iter.hasNext()) {
            final TgMessage processingMessage = iter.next();

            final Pair<StopGroup, TgMachine> currStopAndMachine = processMessage(processingMessage, previousMessage, currStop, stops, currMachine, radiusThreshould, allMessages);

            previousMessage = processingMessage;
            currStop = currStopAndMachine.getKey();
            currMachine = currStopAndMachine.getValue();
        }
        if (currStop != null) {
            // close the last stop and add it into established list of stops
            currStop.closeAndAdd(stops);
        }

        logger.info("'Messages => stop groups' processing ended at " + new DateTime() + ".");
        return createStops(stops, paramValues, factory);
    }

    private final static PeriodFormatter periodFormatter = new PeriodFormatterBuilder().//
    appendYears().appendSuffix(" рік", " років").appendSeparator("; ").//
    appendMonths().appendSuffix(" місяць", " місяців").appendSeparator("; ").//
    appendWeeks().appendSuffix(" тиждень", " тижнів").appendSeparator("; ").//
    appendDays().appendSuffix(" день", " днів").appendSeparator("; ").//
    appendHours().appendSuffix(" година", " годин").appendSeparator("; ").//
    appendMinutes().appendSuffix(" хвилина", " хвилин").// appendSeparator("; ").//
    // appendSeconds().appendSuffix(" second", " seconds").
    toFormatter();

    public final static PeriodFormatter periodFormatter() {
        return periodFormatter;
    }

    private static List<TgStop> createStops(final List<StopGroup> stopGroups, final Map<String, Object> paramValues, final EntityFactory factory) {
        logger.info("'Stop groups => stops' migration started at " + new DateTime() + ".");
        final List<TgStop> stops = new ArrayList<>();

        final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");

        for (final StopGroup stopGroup : stopGroups) {
            final TgStop stop = factory.newPlainEntity(TgStop.class, null);

            final Date from = stopGroup.from();
            final Date to = stopGroup.to();
            final String fromString = dateFormat.format(from);
            final String toString = dateFormat.format(to);
            stop.setKey("" + stopGroup.getMachine() + " " + fromString + "=>" + toString);

            // final Stop stop = getEntityFactory().newByKey(Stop.class, "" + stopGroup.getLastHardStopMessage());
            stop.setMachineResult(stopGroup.getMachine());
            // stop.setGpsTime(stopGroup.getLastHardStopMessage().getGpsTime());
            stop.setOrgUnitResult(stopGroup.getMachine().getOrgUnit());

            final Interval interval = new Interval(new DateTime(from), new DateTime(to));

            stop.setDurationInMinutesResult(stopGroup.getDurationInMinutes());
            stop.setPeriodString(interval.toPeriod().toString(periodFormatter));
            stop.setDistance(stopGroup.getDistance());
            stop.setRadius(new BigDecimal(stopGroup.radius()));
            stop.setNightStopResult(stopGroup.isNightStop());

            stop.setStopTimeFrom(from);
            stop.setStopTimeTo(to);
            stop.setStopTimeFromString(fromString);
            stop.setStopTimeToString(toString);

            stop.setBaryCentreX(new BigDecimal(stopGroup.baryCentreX()));
            stop.setBaryCentreY(new BigDecimal(stopGroup.baryCentreY()));

            stop.setMessages(new LinkedHashSet<TgMessage>(stopGroup.messages));
            stop.setMessagesString(/* stop.getMessages() */tooString(stopGroup.messages));

            stops.add(stop);
        }
        logger.info("'Stop groups => stops' migration ended at " + new DateTime() + ".");
        return filter(stops, paramValues, Arrays.asList("durationInMinutes", "nightStop"));
    }

    public static <T extends AbstractEntity> String tooString(final Collection<T> entities) {
        final StringBuilder sb = new StringBuilder("");// <html>");
        for (final T e : entities) {
            sb.append(e.toString() + "<br>");
        }
        return sb.toString(); // + "</html>";
    }

    protected static Pair<StopGroup, TgMachine> processMessage(final TgMessage processingMessage, final TgMessage previousMessage, final StopGroup currStop, final List<StopGroup> stops, final TgMachine currMachine, final BigDecimal radiusThreshould, final List<TgMessage> allMessages) {
        if (anotherMachineAppeared(currMachine, processingMessage.getMachine())) {
            if (currStop == null) {
                return processMessage(processingMessage, previousMessage, currStop, stops, processingMessage.getMachine(), radiusThreshould, allMessages);
            } else {
                // close stop and add it into established list of stops (it does not matter how close 'processingMessage' is -- it is from another machine!)
                currStop.closeAndAdd(stops);
                return processMessage(processingMessage, previousMessage, null, stops, processingMessage.getMachine(), radiusThreshould, allMessages);
            }
        } else if (currStop == null) {
            if (isHardStop(processingMessage)) {
                // create and open a new stop group
                final int currIndex = allMessages.indexOf(processingMessage);
                final int leftIndex = currIndex - SOFT_MESSAGE_COUNT_THRESHOLD < 0 ? 0 : currIndex - SOFT_MESSAGE_COUNT_THRESHOLD;
                final List<TgMessage> previousBeforeFirstHardStopMessages = allMessages.subList(leftIndex, currIndex);
                return new Pair<>(new StopGroup(previousBeforeFirstHardStopMessages, processingMessage, radiusThreshould), currMachine);
            } else {
                // just ignore "processing message"
                logger.info("A message [" + processingMessage + "] has been ignored as 'too remote' from a last stop.");
                return new Pair<>(null, currMachine); // no stopping group is associated
            }
        } else {
            if (isCloseEnough(processingMessage, currStop, radiusThreshould)) {
                currStop.extendBy(processingMessage);
                return new Pair<>(currStop, currMachine);
            } else {
                // close stop and add it into established list of stops
                currStop.closeAndAdd(stops);

                // redo the same process for 'processingMessage'
                return processMessage(processingMessage, previousMessage, null, stops, currMachine, radiusThreshould, allMessages);
            }
        }
    }

    /**
     * Returns <code>true</code> if a new machine appeared comparing to a previous one.
     *
     * @param machine
     * @param newMachine
     * @return
     */
    private static boolean anotherMachineAppeared(final TgMachine machine, final TgMachine newMachine) {
        return machine == null || !machine.getId().equals(newMachine.getId());
    }

    private static boolean isHardStop(final TgMessage processingMessage) {
        return processingMessage.getVectorSpeed() == 0;
    }

    private static boolean isCloseEnough(final TgMessage message, final StopGroup stop, final BigDecimal radiusThreshould) {
        return distance(message, stop) < radiusThreshould.doubleValue();
    }

    private static double distance(final TgMessage message, final StopGroup stop) {
        return MapUtils.calcDistance(message.getX(), message.getY(), new BigDecimal(stop.baryCentreX()), new BigDecimal(stop.baryCentreY()));
    }

    private static boolean areNeighbours(final TgMessage m1, final TgMessage m2) {
        return !anotherMachineAppeared(m1.getMachine(), m2.getMachine()) && MapUtils.calcDistance(m1.getX(), m1.getY(), m2.getX(), m2.getY()) <= 250;
    }

    private static <T extends AbstractEntity> boolean predicate(final T entity, final Map<String, Object> paramValues, final List<String> properties) {
        for (final Map.Entry<String, Object> paramValue : paramValues.entrySet()) {
            final String criteriaPropertyName = paramValue.getKey();
            final Object pValue = paramValue.getValue();
            if (pValue != null && conforms(criteriaPropertyName, properties)) {
                if (criteriaPropertyName.endsWith("_from")) {
                    final String propertyName = criteriaPropertyName.substring(0, criteriaPropertyName.lastIndexOf("_from"));
                    final Object value = entity.get(preprocess(propertyName));
                    if (value instanceof Comparable) {
                        final Comparable<Object> comp = (Comparable<Object>) value;
                        if (!(comp.compareTo(pValue) >= 0)) {
                            return false;
                        }
                    }
                } else if (criteriaPropertyName.endsWith("_to")) {
                    final String propertyName = criteriaPropertyName.substring(0, criteriaPropertyName.lastIndexOf("_to"));
                    final Object value = entity.get(preprocess(propertyName));
                    if (value instanceof Comparable) {
                        final Comparable<Object> comp = (Comparable<Object>) value;
                        if (!(comp.compareTo(pValue) < 0)) {
                            return false;
                        }
                    }
                } else if (criteriaPropertyName.endsWith("_is")) {
                    final String propertyName = criteriaPropertyName.substring(0, criteriaPropertyName.lastIndexOf("_is"));
                    final String criteriaPropertyName_Not = propertyName + "_not";
                    final boolean is = (boolean) pValue;
                    final boolean isNot = (boolean) paramValues.get(criteriaPropertyName_Not);
                    final Object value = entity.get(preprocess(propertyName));
                    if (value instanceof Boolean) {
                        final boolean act = (boolean) value;
                        if (is && !isNot && !act || !is && isNot && act) {
                            return false;
                        }
                    }
                } else if (criteriaPropertyName.endsWith("_to")) {
                    // disregard
                }
            }
        }
        return true;
    }

    private static boolean conforms(final String criteriaPropertyName, final List<String> properties) {
        for (final String p : properties) {
            if (criteriaPropertyName.startsWith(p)) {
                return true;
            }
        }
        return false;
    }

    private static String preprocess(final String propertyName) {
        return propertyName.equals("durationInMinutes") ? "durationInMinutesResult" : propertyName.equals("nightStop") ? "nightStopResult"
                : propertyName.equals("stopsCount") ? "stopsCountResult" : propertyName;
    }

    public static <T extends AbstractEntity> List<T> filter(final List<T> entities, final Map<String, Object> paramValues, final List<String> properties) {
        final List<T> filtered = new ArrayList<T>();
        for (final T entity : entities) {
            if (predicate(entity, paramValues, properties)) {
                filtered.add(entity);
            }
        }
        return filtered;
    }
}