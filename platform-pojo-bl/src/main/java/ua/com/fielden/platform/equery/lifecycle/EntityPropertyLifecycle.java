package ua.com.fielden.platform.equery.lifecycle;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.Interval;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.Monitoring;
import ua.com.fielden.platform.reflection.AnnotationReflector;
import ua.com.fielden.platform.types.ICategorizer;
import ua.com.fielden.platform.types.ICategory;
import ua.com.fielden.platform.utils.Pair;

/**
 * Contains immutable information for some entity property's lifecycle.
 * 
 * @author TG Team
 * 
 */
public class EntityPropertyLifecycle<T extends AbstractEntity> implements Comparable<EntityPropertyLifecycle<T>> {
    private static final long serialVersionUID = 184435701215220995L;
    private static final Logger logger = Logger.getLogger(EntityPropertyLifecycle.class);

    private final T entity;
    private final Class<T> entityType;
    private final String propertyName;
    private final List<ValuedInterval> intervals;
    private final DateTime from;
    private final DateTime to;
    private final transient DateTime middle;
    private final ICategorizer categorizer;

    /** The scale that is used for most operations. */
    public final static int DEFAULT_SCALE = 15;
    /** Zero with {@value #DEFAULT_SCALE} scale. */
    public final static BigDecimal DEFAULT_ZERO = BigDecimal.ZERO.setScale(DEFAULT_SCALE);

    /** Scale is {@value #DEFAULT_SCALE}. */
    private final BigDecimal normalStateDuration, mainCategoriesDuration, availability;

    private final List<ValuedInterval> categoryDurations;

    /**
     * Represents a map between "Category or uncategorized value" and "summary millis" (a sum of millis of corresponding category/uncategorizedValue in lifecycle). Scale is
     * {@value #DEFAULT_SCALE}.
     */
    private transient Map<Object, BigDecimal> millisMap;

    /**
     * Mainly used for serialisation.
     */
    protected EntityPropertyLifecycle() {
        entity = null;
        entityType = null;
        propertyName = null;
        intervals = null;
        from = null;
        to = null;
        middle = null;
        categorizer = null;
        normalStateDuration = null;
        mainCategoriesDuration = null;
        availability = null;
        categoryDurations = null;
    }

    public EntityPropertyLifecycle(final T entity, final Class<T> entityType, final String propertyName, final List<? extends ValuedInterval> intervals, final DateTime from, final DateTime to) {
        this.entity = entity;
        this.entityType = entityType;
        this.propertyName = propertyName;
        this.categorizer = getCategorizer(getEntityType(), getPropertyName());
        this.intervals = new ArrayList<ValuedInterval>(normalizeAndValidate(intervals));
        this.from = from;
        this.to = to;
        this.middle = new DateTime(from.getMillis() + to.getMillis() / 2);

        final Pair<BigDecimal, BigDecimal> p = determineAvailability();
        this.normalStateDuration = p.getKey();
        this.mainCategoriesDuration = p.getValue();
        this.availability = divide(normalStateDuration, mainCategoriesDuration);

        this.categoryDurations = new ArrayList<ValuedInterval>();
        long x = 1000;
        for (final Entry<Object, BigDecimal> entry : millisMap.entrySet()) {
            if (entry.getKey() instanceof ICategory) {
                categoryDurations.add(new ValuedInterval(new DateTime(x++), ((ICategory) entry.getKey()).getName(), entry.getValue(), ((ICategory) entry.getKey()).isNormal()));
            } else {
                final String info = entry.getKey() instanceof AbstractEntity ? ((AbstractEntity) entry.getKey()).getKey() + "" : entry.getKey() + "";
                categoryDurations.add(new ValuedInterval(new DateTime(x++), info, entry.getValue(), false));
            }
        }
    }

    /**
     * This method covers special case of division of two BigDecimal numbers. If both divisor and dividend are zero - division returns zero (defined by dividend). But non-zero
     * value division by zero - illegal situation.
     * 
     * The scale of divisor and dividend should be equal.
     * 
     * @param dividend
     * @param divisor
     * @return
     */
    public static BigDecimal divide(final BigDecimal dividend, final BigDecimal divisor) {
        if (dividend.scale() != divisor.scale()) {
            throw new RuntimeException("Dividend scale = " + dividend.scale() + ", divisor scale = " + divisor.scale() + ". This is illegal situation.");
        }
        if (divisor.compareTo(BigDecimal.ZERO) == 0) { // divisor is zero (the scale does not matter)
            if (dividend.compareTo(BigDecimal.ZERO) != 0) { // dividend is not zero (the scale does not matter)
                throw new RuntimeException("Dividend = " + dividend + ", divisor = " + divisor + ". This is illegal situation.");
            } else { // dividend is zero (the scale does not matter)
                return dividend; // this is legal situation. zero / zero => zero.
            }
        } else {
            return dividend.divide(divisor, RoundingMode.HALF_UP);
        }
    }

    /**
     * Sorts and validates list of {@link ValuedInterval}s.
     * 
     * @param intervals
     * @return
     */
    public static List<? extends ValuedInterval> normalizeAndValidate(final List<? extends ValuedInterval> intervals) {
        // ensures intervals to be sorted by ascending:
        Collections.sort(intervals);

        if (!intervals.isEmpty()) {
            // neighbour intervals overlapping is not permitted (this check includes equal neighbour intervals error) :
            for (int i = 0; i < intervals.size() - 1; i++) {
                final ValuedInterval vi = intervals.get(i), vi1 = intervals.get(i + 1);
                if (vi.getInterval().overlaps(vi1.getInterval())) {
                    final String msg = "Neighbour intervals should not be overlapped. [" + vi.getInterval() + "] overlaps [" + vi1.getInterval() + "].";
                    logger.error(msg);
                    throw new RuntimeException(msg);
                }
            }
        }
        return intervals;
    }

    public String getPropertyName() {
        return propertyName;
    }

    public DateTime getFrom() {
        return from;
    }

    public DateTime getTo() {
        return to;
    }

    public List<ValuedInterval> getIntervals() {
        return Collections.unmodifiableList(intervals);
    }

    public T getEntity() {
        return entity;
    }

    public String getEntityKey() {
        return entity.getKey().toString();
    }

    public Class<T> getEntityType() {
        return entityType;
    }

    /**
     * Returns list of timeline durations (millis) by its categories/nonCategorizedValues.
     * 
     * IMPORTANT : the order is very important. So the order is [Category 1, .. , Category N, 1-st appeared value, 2-nd appeared value, .., n-th appeared value]. Categories order
     * is defined in ICategorizer.getAllCategories() method.
     * 
     * @return
     */
    public List<ValuedInterval> getCategoryDurations() {
        return Collections.unmodifiableList(categoryDurations);
    }

    /**
     * Determines the percent of entity life cycle when entity property had normal state.
     * 
     * @return
     */
    private Pair<BigDecimal, BigDecimal> determineAvailability() {
        // IMPORTANT : the order of this map is very important. So the order is [Category 1, .. , Category N, 1-st appeared value, 2-nd appeared value, .., n-th appeared value].
        // Categories order is defined in ICategorizer.getAllCategories() method.
        millisMap = new LinkedHashMap<Object, BigDecimal>();
        // add empty values of summary millis into predefined categories keys - the order is strictly defined by ICategorizer.getAllCategories() method implementation:
        for (final ICategory category : getCategorizer().getAllCategories()) {
            millisMap.put(category, DEFAULT_ZERO);
        }

        BigDecimal normalStateDuration = DEFAULT_ZERO;
        for (final ValuedInterval vi : intervals) {
            final Interval intersectionWithBoundaries = boundaries().overlap(vi.getInterval()); // used to disregard intervals outside from boundaries
            // and consider a part of interval that overlaps with boundaries.
            if (intersectionWithBoundaries != null) {
                final BigDecimal x = duration(intersectionWithBoundaries.getStart(), intersectionWithBoundaries.getEnd());
                final ICategory viCategory = getCategorizer().getCategory(vi.getValue());

                // the key for summary millis map (a sum of millis of corresponding category/uncategorizedValue in lifecycle):
                final Object key = viCategory.isUncategorized() ? vi.getValue() : viCategory;
                final BigDecimal prev = millisMap.containsKey(key) ? millisMap.get(key) : DEFAULT_ZERO;
                millisMap.put(key, prev.add(x));

                if (viCategory.isNormal()) {
                    normalStateDuration = normalStateDuration.add(x);
                }
            }
        }
        // only "main" category durations should be taken into account when calculating "allDuration". Empty period will be ignored as well as non-"main" categories too.
        BigDecimal mainCategoriesDuration = DEFAULT_ZERO;
        for (final ICategory category : getCategorizer().getMainCategories()) {
            mainCategoriesDuration = mainCategoriesDuration.add(millisMap.get(category));
        }

        // validate "main categories duration" and "availability duration".
        divide(normalStateDuration, mainCategoriesDuration);

        return new Pair<BigDecimal, BigDecimal>(normalStateDuration, mainCategoriesDuration);
    }

    /** Gets {@link BigDecimal} value (scale is {@value #DEFAULT_SCALE}) of date interval duration. */
    private static BigDecimal duration(final DateTime from, final DateTime to) {
        return new BigDecimal(to.getMillis() - from.getMillis()).setScale(DEFAULT_SCALE);
    }

    /**
     * Boundaries formed by global [left; right] dates.
     * 
     * @return
     */
    protected Interval boundaries() {
        return new Interval(from, to);
    }

    /**
     * Boundaries formed by [left interval "from date"; right interval "to date"].
     * 
     * @return
     */
    private Interval actualBoundaries() {
        return intervals.size() > 0 ? //
        new Interval(intervals.get(0).getFrom(), intervals.get(intervals.size() - 1).getTo())
                : //
                new Interval(middle, middle);
    }

    /**
     * Returns whether entity property was in <code>Category.NORMAL</code> state at specified <code>moment</code>.
     * 
     * @param categorizer
     * @param moment
     * @return
     */
    public boolean isAvailable(final DateTime moment) {
        return actualBoundaries().contains(moment) && boundaries().contains(moment) ? getCategory(moment).isNormal() : false;
    }

    /**
     * Returns entity property state category at specified <code>moment</code>.
     * 
     * @param categorizer
     * @param moment
     * @return
     */
    public ICategory getCategory(final DateTime moment) {
        return getCategorizer().getCategory(getValue(moment));
    }

    /**
     * Returns entity property state value at specified <code>moment</code>.
     * 
     * @param categorizer
     * @param moment
     * @return
     */
    public Object getValue(final DateTime moment) {
        final int index = getIntervalIndex(intervals, moment);
        if (index >= 0) {
            return intervals.get(index).getValue();
        } else {
            throw new IllegalArgumentException("Could not be used illegal date == " + moment + " for determination state info for specified date.");
        }
    }

    /**
     * Gets the percent of entity life cycle when entity property had normal state.
     * 
     * @param categorizer
     * @return
     */
    public BigDecimal getAvailability() {
        return availability;
    }

    public ICategorizer getCategorizer() {
        return categorizer;
    }

    public static ICategorizer getCategorizer(final Class<?> entityType, final String propertyName) {
        final Class<? extends ICategorizer> categorizerType = AnnotationReflector.getPropertyAnnotation(Monitoring.class, entityType, propertyName).value();
        try {
            return categorizerType.getConstructor(new Class[] {}).newInstance();
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Returns entity name with availability information in single string. (e.g.: "A1234, 67.8%", "A1234,100.0%", "A1234,  7.8%" - information with layout)
     * 
     * @return
     */
    public String getInfo(final boolean withAvailability) {
        return getEntityKey() + (withAvailability ? "," + strPercentage(roundedPercents(getAvailability())) : "");
    }

    /**
     * 
     * 
     * @param number
     * @return
     */
    public static String strPercentage(final Double number) {
        return (number == 100.0 ? "" : number < 10 ? "  " : " ") + number + "%";
    }

    /**
     * Returns rounded value of percents. E.g. : 0.987654321 -> 98,8 (%)
     * 
     * @param availability
     * @return
     */
    public static double roundedPercents(final BigDecimal availability) {
        return Math.round(availability.multiply(new BigDecimal(1000.0)).doubleValue()) / 10.0;
    }

    /**
     * Implements comparison based on the business key implementation.
     */
    public final int compareTo(final EntityPropertyLifecycle<T> cmpTo) {
        return getEntityKey().compareTo(cmpTo.getEntityKey());
    }

    /**
     * Hashing is based on the business key implementation.
     */
    @Override
    public final int hashCode() {
        return (getEntityKey() != null ? getEntityKey().hashCode() : 0) * 23;
    }

    /**
     * Equality is based on the business key implementation.
     */
    @Override
    public final boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof EntityPropertyLifecycle)) {
            return false;
        }
        // let's ensure that types match
        final EntityPropertyLifecycle<T> that = (EntityPropertyLifecycle<T>) obj;
        if (this.getClass() != that.getClass()) {
            return false;
        }
        // now can compare key values
        final Object thatKey = that.getEntityKey();
        return getEntityKey() != null && getEntityKey().equals(thatKey) || getEntityKey() == null && thatKey == null;
    }

    @Override
    public String toString() {
        return getEntityKey() != null ? getEntityKey().toString() : null;
    }

    public BigDecimal getNormalStateDuration() {
        return normalStateDuration;
    }

    public BigDecimal getMainCategoriesDuration() {
        return mainCategoriesDuration;
    }

    /**
     * Splits lifecycle information by <code>moment</code>. Moment should be inside boundary interval.
     * 
     * @param moment
     * @param copy
     *            - indicates whether split information should be copied.
     * @return
     */
    public Pair<? extends EntityPropertyLifecycle<T>, ? extends EntityPropertyLifecycle<T>> split(final DateTime moment, final boolean copy) {
        final Pair<List<ValuedInterval>, List<ValuedInterval>> leftRight = split(intervals, moment, boundaries(), copy);
        final EntityPropertyLifecycle<T> left = new EntityPropertyLifecycle<T>(entity, entityType, propertyName, leftRight.getKey(), from, moment);
        final EntityPropertyLifecycle<T> right = new EntityPropertyLifecycle<T>(entity, entityType, propertyName, leftRight.getValue(), moment, to);
        return new Pair<EntityPropertyLifecycle<T>, EntityPropertyLifecycle<T>>(left, right);
    }

    /**
     * Splits lifecycle information by <code>moment</code>. Moment should be inside boundary interval.
     * 
     * @param moment
     * @return
     */
    public static <T extends ValuedInterval> Pair<List<T>, List<T>> split(final List<T> intervals, final DateTime moment, final Interval boundaries) {
        return split(intervals, moment, boundaries, true);
    }

    /**
     * Splits lifecycle information by <code>moment</code>. Moment should be inside boundary interval.
     * 
     * @param moment
     * @param copy
     *            - indicates whether split information should be copied.
     * @return
     */
    public static <T extends ValuedInterval> Pair<List<T>, List<T>> split(final List<T> intervals, final DateTime moment, final Interval boundaries, final boolean copy) {
        if (!boundaries.contains(moment)) {
            throw new IllegalArgumentException("The [" + moment + "] should be inside boundaries of lifecycle data [" + boundaries + "]. ");
        }
        final int containingIntervalIndex = getIntervalIndex(intervals, moment);
        final boolean found = containingIntervalIndex >= 0;

        final List<T> leftIntervals = new ArrayList<T>(), rightIntervals = new ArrayList<T>();
        if (found) {
            // Concrete interval containing "moment" was found.
            // For now we should find also other neighbour intervals containing "moment". (they should be "neighbours" because intervals are sorted!)
            // Let's go to the left and to the right from founded index:
            final SortedMap<Integer, Pair<T, T>> splitIntervals = new TreeMap<Integer, Pair<T, T>>();
            splitIntervals.put(containingIntervalIndex, (Pair<T, T>) intervals.get(containingIntervalIndex).split(moment));
            // comparator for the moment:
            final Comparator<ValuedInterval> comparator = getComparator(moment);
            // go to the left:
            int currentIndex = containingIntervalIndex - 1;
            while (currentIndex >= 0 && comparator.compare(intervals.get(currentIndex), null) == 0) {
                splitIntervals.put(currentIndex, (Pair<T, T>) intervals.get(currentIndex).split(moment));
                currentIndex--;
            }
            // go to the right:
            currentIndex = containingIntervalIndex + 1;
            while (currentIndex < intervals.size() && comparator.compare(intervals.get(currentIndex), null) == 0) {
                splitIntervals.put(currentIndex, (Pair<T, T>) intervals.get(currentIndex).split(moment));
                currentIndex++;
            }
            // max and min indexes of intervals containing "moment":
            final int minSplittedIntervalsIndex = splitIntervals.firstKey(), //
            maxSplittedIntervalsIndex = splitIntervals.lastKey();
            if (minSplittedIntervalsIndex != maxSplittedIntervalsIndex) {
                System.err.println("\t\tThere are more than one interval to be split inside a couple of intervals between [" + boundaries + "] . Indexes == ["
                        + minSplittedIntervalsIndex + "; " + maxSplittedIntervalsIndex + "]");
            }

            for (int i = 0; i < intervals.size(); i++) {
                if (minSplittedIntervalsIndex <= i && i <= maxSplittedIntervalsIndex) {
                    leftIntervals.add(splitIntervals.get(i).getKey());
                    rightIntervals.add(splitIntervals.get(i).getValue());
                } else if (i < minSplittedIntervalsIndex) {
                    leftIntervals.add(copy ? (T) intervals.get(i).clone() : intervals.get(i));
                } else if (maxSplittedIntervalsIndex < i) {
                    rightIntervals.add(copy ? (T) intervals.get(i).clone() : intervals.get(i));
                }
            }
        } else { // no interval was found - moment is in empty space.
            for (int i = 0; i < intervals.size(); i++) {
                if (i < -containingIntervalIndex - 1) { // here (-containingIntervalIndex - 1) is "insertion point", the first interval index after founded "empty space".
                    leftIntervals.add(copy ? (T) intervals.get(i).clone() : intervals.get(i));
                } else { // >= -containingIntervalIndex - 1
                    rightIntervals.add(copy ? (T) intervals.get(i).clone() : intervals.get(i));
                }
            }
        }
        return new Pair<List<T>, List<T>>(leftIntervals, rightIntervals);
    }

    /**
     * Returns interval index containing specified <code>moment</code>.
     * 
     * @param categorizer
     * @param moment
     * @return
     */
    protected static int getIntervalIndex(final List<? extends ValuedInterval> intervals, final DateTime moment) {
        return Collections.binarySearch(intervals, null, getComparator(moment));
    }

    /**
     * Comparator for searching {@link ValuedInterval} by moment containing it.
     * 
     * @param moment
     * @return
     */
    private static Comparator<ValuedInterval> getComparator(final DateTime moment) {
        if (moment == null) {
            throw new NullPointerException("Null date could not be used for interval searching.");
        }
        return new Comparator<ValuedInterval>() {
            @Override
            public int compare(final ValuedInterval o1, final ValuedInterval o2) {
                return o1.contains(moment) ? 0 : o1.getKey().compareTo(moment);
            }
        };
    }
}
