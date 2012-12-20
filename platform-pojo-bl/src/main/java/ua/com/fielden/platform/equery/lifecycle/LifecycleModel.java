package ua.com.fielden.platform.equery.lifecycle;

import java.awt.Color;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.Interval;
import org.joda.time.Months;
import org.joda.time.Period;
import org.joda.time.ReadablePeriod;
import org.joda.time.Weeks;
import org.joda.time.Years;

import ua.com.fielden.platform.domaintree.centre.analyses.impl.LifecycleDomainTreeManager;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.equery.lifecycle.IProperty.ITimeProperty;
import ua.com.fielden.platform.equery.lifecycle.IProperty.IValueProperty;
import ua.com.fielden.platform.equery.lifecycle.IProperty.ValueProperty;
import ua.com.fielden.platform.reflection.Finder;
import ua.com.fielden.platform.reflection.TitlesDescsGetter;
import ua.com.fielden.platform.types.ICategorizer;
import ua.com.fielden.platform.types.ICategory;
import ua.com.fielden.platform.utils.Pair;

/**
 * The lifecycle data model that consists of lifecycles for list of entities.
 *
 * @author TG Team
 *
 */
public abstract class LifecycleModel<T extends AbstractEntity> {

    private final Class<T> entityType;
    private final DateTime leftBoundary;
    private final DateTime rightBoundary;
    private final List<? extends EntityPropertyLifecycle<T>> lifecycleData;

    /**
     * List of categories information with corresponding colors.
     */
    private final List<ValuedInterval> categoriesInfoWithColors;

    /**
     * Sorted list of availability intervals (count of available entities for each period).
     */
    private final List<ValuedInterval> summaryAvailability;

    /**
     * List of uncategorized values with corresponding colors.
     */
    private final List<ValuedInterval> uncategorizedValuesWithColors;

    /**
     * Defines colors for non-categorized values. (Incremental defining)
     */
    private final transient Map<Object, Color> uncategorizedValueColors = new HashMap<Object, Color>();

    private final BigDecimal averageRelativeAvailability;

    private final Double averageSummaryAvailability;
    private final Long maxSummaryAvailability, minSummaryAvailability;

    private final ICategorizer categorizer;

    private int groupsCount, groupNumber;
    private IProgressUpdater progressUpdater;

    private transient LinkedHashMap<IProperty, Object> groupingValues;

    private transient final Logger logger = Logger.getLogger(getClass());

    /**
     * Mainly used for serialisation.
     */
    protected LifecycleModel() {
	entityType = null;
	leftBoundary = null;
	rightBoundary = null;
	lifecycleData = null;
	categoriesInfoWithColors = null;
	summaryAvailability = null;
	uncategorizedValuesWithColors = null;
	averageRelativeAvailability = null;
	averageSummaryAvailability = null;
	maxSummaryAvailability = null;
	minSummaryAvailability = null;
	categorizer = null;
	groupingValues = null;
    }

    //    /**
    //     * Creates {@link LifecycleModel} and performs all necessary calculation for lifecycle model analysis.
    //     *
    //     * @param leftBoundary
    //     * @param rightBoundary
    //     * @param lifecycleData
    //     */
    //    public LifecycleModel(final DateTime leftBoundary, final DateTime rightBoundary, final List<? extends EntityPropertyLifecycle<T>> lifecycleData, final boolean calculateSummaryAvailability) {
    //	this(leftBoundary, rightBoundary, lifecycleData, new LinkedHashMap<IProperty, Object>(), calculateSummaryAvailability);
    //    }

    /**
     * Creates {@link LifecycleModel} and performs all necessary calculation for lifecycle model analysis.
     *
     * @param leftBoundary
     * @param rightBoundary
     * @param lifecycleData
     */
    public LifecycleModel(final Class<T> entityType, final DateTime leftBoundary, final DateTime rightBoundary, final List<? extends EntityPropertyLifecycle<T>> lifecycleData, final LinkedHashMap<IProperty, Object> groupingValues, final boolean calculateSummaryAvailability) {
	this.entityType = entityType;
	final Long curr = new DateTime().getMillis();
	//	System.out.println("1. begin => " + new DateTime());
	this.leftBoundary = leftBoundary;
	this.rightBoundary = rightBoundary;
	this.groupingValues = groupingValues;

	this.lifecycleData = new ArrayList<EntityPropertyLifecycle<T>>(normalizeAndValidate(lifecycleData));
	categorizer = this.lifecycleData.isEmpty() ? null : this.lifecycleData.get(0).getCategorizer();

	// populate colors for each sub-interval for each lifecycle:

	// Initiates appropriate calculation related to availability. Sets appropriate colors for each sub-interval for each {@link EntityPropertyLifecycle} instance inside this lifecycle model.
	categoriesInfoWithColors = new ArrayList<ValuedInterval>();
	long x = 1000;
	if (categorizer != null) {
	    for (final ICategory category : categorizer.getAllCategories()) {
		categoriesInfoWithColors.add(new ValuedInterval(new DateTime(x++), category.getName(), category.getDesc(), category.getColor()));
	    }
	}

	//	System.out.println("2. summaryAvailability = calculateSummaryAvailability() => " + new DateTime());
	this.summaryAvailability = calculateSummaryAvailability ? calculateSummaryAvailability() : new ArrayList<ValuedInterval>();
	//	System.out.println("3. averageRelativeAvailability = calculateAverageRelativeAvailability() => " + new DateTime());
	averageRelativeAvailability = calculateAverageRelativeAvailability();
	//	System.out.println("4. averageSummaryAvailability = calculateAverageSummaryAvailability() => " + new DateTime());

	// Calculates average/max/min number of available entities for time-period of this {@link LifecycleModel}.
	long sum = 0, max = -1, min = Integer.MAX_VALUE;
	for (final ValuedInterval vi : getSummaryAvailability()) {
	    if (vi.getAvailability() > max) {
		max = vi.getAvailability();
	    }
	    if (vi.getAvailability() < min) {
		min = vi.getAvailability();
	    }
	    sum += vi.getInterval().toDurationMillis() * vi.getAvailability();
	}
	maxSummaryAvailability = max;
	minSummaryAvailability = min;
	averageSummaryAvailability = new Double(sum) / boundaries().toDurationMillis();

	//	System.out.println("5. EPL colors and category durations => " + new DateTime());

	for (final EntityPropertyLifecycle<T> epl : this.lifecycleData) {
	    // enforce fractions calculation:
	    epl.getCategoryDurations();
	    for (final ValuedInterval vi : epl.getIntervals()) {
		final Pair<Color, ICategory> colorAndCategory = determineValueColor(vi.getValue());
		vi.setColor(colorAndCategory.getKey());
		vi.setCategory(colorAndCategory.getValue());
	    }
	}

	//	System.out.println("6. uncategorized values colors => " + new DateTime());
	uncategorizedValuesWithColors = new ArrayList<ValuedInterval>();
	x = 1000;
	for (final Entry<Object, Color> entry : uncategorizedValueColors.entrySet()) {
	    final String key = entry.getKey() instanceof AbstractEntity ? ((AbstractEntity) entry.getKey()).getKey() + "" : entry.getKey() + "";
	    final String desc = entry.getKey() instanceof AbstractEntity ? ((AbstractEntity) entry.getKey()).getDesc() + "" : entry.getKey() + "";
	    uncategorizedValuesWithColors.add(new ValuedInterval(new DateTime(x++), key, desc, entry.getValue()));
	}
	//	System.out.println("7. end => " + new DateTime());
	final String debugStr = "Lifecycle model created (and summary availability calculated) in " + (new DateTime().getMillis() - curr) + "ms. Entities count = " + lifecycleData.size() + ". Period = [" + leftBoundary.toDate() + "; " + rightBoundary.toDate() + "].";
	System.out.println(debugStr);
	logger.debug(debugStr);
    }

    /**
     * Sorts and validates list of {@link EntityPropertyLifecycle}s.
     *
     * @param lifecycles
     * @return
     */
    public static <T extends AbstractEntity<?>> List<? extends EntityPropertyLifecycle<T>> normalizeAndValidate(final List<? extends EntityPropertyLifecycle<T>> lifecycles) {
	final List<EntityPropertyLifecycle<T>> newLifecycles = new ArrayList<EntityPropertyLifecycle<T>>();
	newLifecycles.addAll(lifecycles);
	Collections.sort(newLifecycles);
	// TODO do some kind of validation for list : 1. same entities should cause errors? 2. ...?
	return newLifecycles;
    }

    public List<? extends EntityPropertyLifecycle<T>> getLifecycleData() {
	return Collections.unmodifiableList(lifecycleData);
    }

    public enum GroupingPeriods implements ITimeProperty {
	YEAR("__YEAR", "Year", "years", Years.ONE), //
	MONTH("__MONTH", "Month", "months", Months.ONE), //
	FORTNIGHT("__FORTNIGHT", "Fortnight", "two weeks", Weeks.TWO), //
	WEEK("__WEEK", "Week", "weeks", Weeks.ONE), //
	DAY("__DAY", "Day", "days", Days.ONE);

	private final String propertyName, title, desc;
	private final ReadablePeriod period;

	private GroupingPeriods(final String propertyName, final String title, final String desc, final ReadablePeriod period) {
	    this.propertyName = propertyName;
	    this.title = title;
	    this.desc = desc;
	    this.period = period;
	}

	public String getPropertyName() {
	    return propertyName;
	}

	@Override
	public String getTitle() {
	    return title;
	}

	@Override
	public String getDesc() {
	    return desc;
	}

	@Override
	public ReadablePeriod getPeriod() {
	    return period;
	}

	@Override
	public String toString() {
	    return getTitle();
	}
    }

    private ValueProperty create(final Class<T> clazz, final String propertyName) {
	if ("".equals(propertyName)) { // high-level entity:
	    final Pair<String, String> td = TitlesDescsGetter.getEntityTitleAndDesc(clazz);
	    return new ValueProperty(propertyName, td.getKey(), "<html>Distribute by high-level <b><i>" + td.getValue() + "</i></b></html>");
	} else {
	    return new ValueProperty(propertyName, TitlesDescsGetter.getTitleAndDesc(propertyName, clazz).getKey(), "<html>Distribute by <i>"
		    + TitlesDescsGetter.removeHtmlTag(TitlesDescsGetter.getFullTitleAndDesc(propertyName, clazz).getValue()) + "</i></html>");
	}
    }

    /**
     * Makes groups from lifecycles.
     *
     * @param propertyName
     *            - the name of property (dot-notation-expression) by which grouping should be performed. If empty - separate group for each entity.
     * @return
     */
    public List<IGroup<T>> groupBy(final String propertyName) {
	final GroupingPeriods period = LifecycleDomainTreeManager.getGroupingPeriod(propertyName);
	System.out.println("\t\tGrouping by [" + propertyName + "]");
	if (period != null) {
	    return groupBy(period);
	}

	final ValueProperty vp = create(entityType, propertyName);
	// maps grouped value and a list of lifecycle numbers:
	final Map<Object, List<Integer>> groupedData = new HashMap<Object, List<Integer>>();
	if (getLifecycleData().size() > 0) {
	    // initiate groupedData:
	    for (int i = 0; i < getLifecycleData().size(); i++) {
		final EntityPropertyLifecycle<T> lifecycle = getLifecycleData().get(i);
		final Object value;
		try {
		    value = StringUtils.isEmpty(propertyName) ? lifecycle.getEntity() : Finder.findFieldValueByName(lifecycle.getEntity(), propertyName);
		} catch (final Exception e) {
		    e.printStackTrace();
		    throw new RuntimeException(e);
		}
		if (!groupedData.containsKey(value)) {
		    groupedData.put(value, new ArrayList<Integer>());
		}
		groupedData.get(value).add(i);
	    }
	    // assemble indexes into full immutable groups:
	    final List<IGroup<T>> groups = new ArrayList<IGroup<T>>();
	    for (final Entry<Object, List<Integer>> entry : groupedData.entrySet()){
		groups.add(createGroupByValue(vp, entry.getKey(), this, entry.getValue()));
	    }
	    return groups;
	}
	return new ArrayList<IGroup<T>>();
    }

    private static DateTime addAndPossiblyTruncate(final DateTime date, final ITimeProperty timeProperty) {
	final DateTime next = date.withPeriodAdded(timeProperty.getPeriod(), 1);
	if (GroupingPeriods.YEAR.equals(timeProperty)) {
	    return next.year().roundFloorCopy();
	} else if (GroupingPeriods.MONTH.equals(timeProperty)) {
	    return next.monthOfYear().roundFloorCopy();
	} else if (GroupingPeriods.FORTNIGHT.equals(timeProperty)) {
	    return next.weekOfWeekyear().roundFloorCopy();
	} else if (GroupingPeriods.WEEK.equals(timeProperty)) {
	    return next.weekOfWeekyear().roundFloorCopy();
	} else if (GroupingPeriods.DAY.equals(timeProperty)) {
	    return next.dayOfYear().roundFloorCopy();
	} else {
	    throw new UnsupportedOperationException("Grouping period [" + timeProperty.getTitle() + "] is not supported yet.");
	}
    }

    /**
     * Creates property-distribution group with key pair "groupingProperty = groupingValue" and accumulated from "parent" for "indexed" lifecycles.
     *
     * @param groupingProperty
     * @param groupingValue
     * @param parent
     * @param indexes
     * @return
     */
    protected abstract IGroup<T> createGroupByValue(final IValueProperty property, final Object value, LifecycleModel<T> parent, List<Integer> indexes);

    /**
     * Creates time-distribution group for "narrowedModel".
     *
     * @param timeProperty
     * @param narrowedModel
     * @param parent
     * @return
     */
    protected abstract IGroup<T> createGroupByModelAndPeriod(final ITimeProperty timeProperty, final LifecycleModel<T> narrowedModel, final LifecycleModel<T> parent);

    /**
     * Makes groups from lifecycles.
     *
     * @param period
     *            - indicates a period by which "time grouping" should be performed.
     * @return
     */
    private List<IGroup<T>> groupBy(final ITimeProperty timeProperty) {
	final Interval boundaries = boundaries();
	final List<DateTime> groupingDates = new ArrayList<DateTime>();
	if (!Period.ZERO.equals(timeProperty.getPeriod().toPeriod())){
	    DateTime next = addAndPossiblyTruncate(boundaries().getStart(), timeProperty);
	    while (boundaries.contains(next)) {
		groupingDates.add(next);
		next = addAndPossiblyTruncate(next, timeProperty);
	    }
	}
	groupsCount = groupingDates.size();
	groupNumber = 0;
	return groupBy(timeProperty, this, /* true */ false);
    }

    /**
     * Extracts a part of {@link LifecycleModel} for specified boundaries.
     *
     * @param boundaries
     * @return
     */
    public LifecycleModel<T> extract(final Interval boundaries, final LinkedHashMap<IProperty, Object> extractedGroupingValues){
	if (!boundaries().contains(boundaries)){
	    throw new RuntimeException("Could not be extracted [" + boundaries + "] from [" + boundaries() + "].");
	} else if (boundaries().equals(boundaries)){
	    return copy(extractedGroupingValues);
	} else {
	    if (!boundaries.getStart().equals(boundaries().getStart())){
		return split(boundaries.getStart(), true, true).getValue().extract(boundaries, extractedGroupingValues);
	    } else {
		return split(boundaries.getEnd(), true, true).getKey().copy(extractedGroupingValues);
	    }
	}
    }

    protected abstract LifecycleModel<T> copy(final LinkedHashMap<IProperty, Object> extractedGroupingValues);

    /**
     * Makes groups from lifecycles.
     * <br><br>
     * IMPORTANT : recursive implementation commented out and used cycled grouping. This is caused by :<br><br>
     *
     * IMPORTANT : Recursive implementation of "hard-weight memory methods" could not be resolved by GC correctly!
     * This is caused by absence of "tail recursion" in JVM 6, so ALL intermediately used memory holds on stack! (it increases memory consumption very hardly)
     *
     * @param period
     *            - indicates a period by which "time grouping" should be performed.
     * @param copy - indicates whether split information should be copied.
     * @return
     */
    private List<IGroup<T>> groupBy(final ITimeProperty timeProperty, final LifecycleModel<T> parent, final boolean copy) {
	//	final Interval interval = boundaries();
	//	final DateTime next = addAndPossiblyTruncate(interval.getStart(), timeProperty);
	//
	//	final List<IGroup<T>> groups = new ArrayList<IGroup<T>>();
	//	if (Period.ZERO.equals(timeProperty.getPeriod().toPeriod()) || !interval.contains(next)) {
	//	    groups.add(createGroupByModelAndPeriod(timeProperty, this, parent));
	//	} else {
	//	    // The first two parts of continuum will be copied:
	//	    final Pair<? extends LifecycleModel<T>, ? extends LifecycleModel<T>> leftRight = split(next, copy, false);
	//	    if (progressUpdater != null){
	//		groupNumber++;
	//		progressUpdater.updateProgress("Grouping..." + EntityPropertyLifecycle.strPercentage(EntityPropertyLifecycle.roundedPercents(EntityPropertyLifecycle.divide(new BigDecimal(groupNumber).setScale(15), new BigDecimal(groupsCount).setScale(15)))));
	//		leftRight.getValue().groupNumber = groupNumber;
	//		leftRight.getValue().groupsCount = groupsCount;
	//		leftRight.getValue().progressUpdater = progressUpdater;
	//	    }
	//	    groups.add(createGroupByModelAndPeriod(timeProperty, leftRight.getKey(), parent));
	//	    // No copy process should be done - only periods cutting:
	//	    groups.addAll(leftRight.getValue().groupBy(timeProperty, parent, false));
	//	}
	//	return groups;

	if (Period.ZERO.equals(timeProperty.getPeriod().toPeriod())){
	    throw new RuntimeException("Could not be grouped by empty period.");
	}

	final Interval interval = boundaries();
	final List<IGroup<T>> groups = new ArrayList<IGroup<T>>();
	LifecycleModel<T> currentModel = this;

	DateTime next = addAndPossiblyTruncate(interval.getStart(), timeProperty);
	while (interval.contains(next)) {
	    // The first two parts of continuum will be copied:
	    final Pair<? extends LifecycleModel<T>, ? extends LifecycleModel<T>> leftRight = currentModel.split(next, copy, false);
	    if (progressUpdater != null) {
		groupNumber++;
		progressUpdater.updateProgress("Distributing..."
			+ EntityPropertyLifecycle.strPercentage(EntityPropertyLifecycle.roundedPercents(EntityPropertyLifecycle.divide(new BigDecimal(groupNumber).setScale(15), new BigDecimal(groupsCount).setScale(15)))));
		final LifecycleModel<T> value = leftRight.getValue();
		value.groupNumber = groupNumber;
		value.groupsCount = groupsCount;
		value.progressUpdater = progressUpdater;
	    }
	    groups.add(createGroupByModelAndPeriod(timeProperty, leftRight.getKey(), parent));

	    next = addAndPossiblyTruncate(next, timeProperty);
	    currentModel = leftRight.getValue();
	}
	// next is out of boundaries:
	groups.add(createGroupByModelAndPeriod(timeProperty, currentModel, parent));
	return groups;
    }


    public DateTime getLeftBoundary() {
	return leftBoundary;
    }

    public DateTime getRightBoundary() {
	return rightBoundary;
    }

    /**
     * Determines a color for a value based on its category, or generates new color if it is non-categorized. New color generation depends on other previously added non-categorized
     * value colors.
     *
     * @param value
     * @return
     */
    private Pair<Color, ICategory> determineValueColor(final Object value) {
	final ICategory category = categorizer.getCategory(value);
	return new Pair<Color, ICategory>(category.isUncategorized() ? generateColor(value) : category.getColor(), category);
    }

    /**
     * Generates random color for value depending on previously generated colors.
     *
     * @param value
     * @return
     */
    private Color generateColor(final Object value) {
	if (!uncategorizedValueColors.containsKey(value)) {
	    uncategorizedValueColors.put(value, randomColor());
	}
	return uncategorizedValueColors.get(value);
    }

    /**
     * Returns a pseudo-random Color.
     *
     * @return a pseudo-random Color
     */
    private Color randomColor() {
	final Random rand = new Random();
	return new Color(rand.nextInt(256), rand.nextInt(256), rand.nextInt(256));
    }

    protected Map<Object, Color> getUncategorizedValueColors() {
	return Collections.unmodifiableMap(uncategorizedValueColors);
    }

    public List<ValuedInterval> getSummaryAvailability() {
	return summaryAvailability;
    }

    /**
     * Calculates a number of available entities at all "change" moments (when at least one entity changed its state).
     *
     * @return
     */
    private List<ValuedInterval> calculateSummaryAvailability() {
	final SortedMap<DateTime, Integer> summaryAvailability = new TreeMap<DateTime, Integer>();
	// put left boundary availability:
	summaryAvailability.put(getLeftBoundary(), calculateSummaryAvailability(getLeftBoundary()));
	// put availability in all nodes:
	for (final EntityPropertyLifecycle<T> epl : lifecycleData) {
	    for (final ValuedInterval vi : epl.getIntervals()) {
		if (boundaries().contains(vi.getFrom().getMillis()) && !summaryAvailability.containsKey(vi.getFrom())) {
		    summaryAvailability.put(vi.getFrom(), calculateSummaryAvailability(vi.getFrom()));
		}
	    }
	}
	// reconfiguring in form of pairs : <Interval> -> <Integer>
	final List<ValuedInterval> sa = new ArrayList<ValuedInterval>();
	DateTime from = summaryAvailability.firstKey();
	for (final DateTime date : summaryAvailability.keySet()) {
	    if (!from.equals(date)) {
		final DateTime to = date;
		sa.add(new ValuedInterval(from, to, summaryAvailability.get(from)));
		from = to;
	    }
	}
	// last availability period:
	sa.add(new ValuedInterval(from, getRightBoundary(), summaryAvailability.get(from)));
	return sa;
    }

    /**
     * Calculates a number of available (with <code>Category.NORMAL</code> state) entities at the specified <code>moment</code>.
     *
     * @return
     */
    private Integer calculateSummaryAvailability(final DateTime moment) {
	int i = 0;
	for (final EntityPropertyLifecycle<T> epl : lifecycleData) {
	    if (epl.isAvailable(moment)) {
		i++;
	    }
	}
	return i;
    }

    /**
     * Gets average value of entities availability for all entities in this {@link LifecycleModel}.
     *
     * @return
     */
    public BigDecimal getAverageRelativeAvailability() {
	return averageRelativeAvailability;
    }

    /**
     * Calculates average value of entities availability for all entities in this {@link LifecycleModel}.
     *
     * @return
     */
    private BigDecimal calculateAverageRelativeAvailability() {
	BigDecimal normalStateDuration = EntityPropertyLifecycle.DEFAULT_ZERO;
	BigDecimal mainCategoriesDuration = EntityPropertyLifecycle.DEFAULT_ZERO;
	for (final EntityPropertyLifecycle<T> epl : getLifecycleData()) {
	    normalStateDuration = normalStateDuration.add(epl.getNormalStateDuration());
	    mainCategoriesDuration = mainCategoriesDuration.add(epl.getMainCategoriesDuration());
	}
	return EntityPropertyLifecycle.divide(normalStateDuration, mainCategoriesDuration);
    }

    /**
     * Gets Average number of available entities for time-period of this {@link LifecycleModel}.
     *
     * @return
     */
    public Double getAverageSummaryAvailability() {
	return averageSummaryAvailability;
    }

    /**
     * Gets Maximum number of available entities for time-period of this {@link LifecycleModel}.
     *
     * @return
     */

    public Long getMaxSummaryAvailability() {
	return maxSummaryAvailability;
    }

    /**
     * Gets Minimum number of available entities for time-period of this {@link LifecycleModel}.
     *
     * @return
     */
    public Long getMinSummaryAvailability() {
	return minSummaryAvailability;
    }

    public Interval boundaries() {
	return new Interval(leftBoundary, rightBoundary);
    }

    public List<ValuedInterval> getUncategorizedValuesWithColors() {
	return uncategorizedValuesWithColors;
    }

    public List<ValuedInterval> getCategoriesInfoWithColors() {
	return categoriesInfoWithColors;
    }

    /**
     * Splits lifecycle information by <code>moment</code>. Moment should be inside boundary interval.
     *
     * @param moment
     * @param copy - indicates whether split information should be copied.
     * @return
     */
    public abstract Pair<? extends LifecycleModel<T>, ? extends LifecycleModel<T>> split(final DateTime moment, final boolean copy, final boolean full);

    public void setProgressUpdater(final IProgressUpdater progressUpdater) {
	this.progressUpdater = progressUpdater;
    }

    public IProgressUpdater getProgressUpdater() {
	return progressUpdater;
    }

    /**
     * Represents a list of grouping values, that define this lifecycle model.
     * <br><br>
     * For example if it is empty - top lifecycle model.<br>
     * If it == "{ ("station",J23) }" - subset of top model with property "station"==J23.<br>
     * If it == "{ ("station",J23), ("___years", 2009) }" - subset of top model with property "station"==J23 for sub-period of 2009 year.<br>
     * If it == "{ ("station",J23), ("___years", 2009), ("model",MERC), }" - subset of top model with property "station"==J23 for sub-period of 2009 year and "model"==MERC.<br>
     *
     * @return
     */
    public LinkedHashMap<IProperty, Object> getGroupingValues() {
	if (groupingValues == null){
	    groupingValues = new LinkedHashMap<IProperty, Object>();
	}
	return groupingValues;
    }
}
