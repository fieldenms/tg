package ua.com.fielden.platform.equery.lifecycle;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.Interval;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.equery.lifecycle.IProperty.ITimeProperty;
import ua.com.fielden.platform.equery.lifecycle.IProperty.IValueProperty;
import ua.com.fielden.platform.equery.lifecycle.LifecycleModel.GroupingPeriods;

/**
 * Represents a group of entities with summarized categories durations. (used in lifecycle reporting)
 *
 * @author TG Team
 *
 */
public abstract class Group<T extends AbstractEntity> implements IGroup<T> {
    private final IProperty property;
    private final Object value;
    private final LifecycleModel<T> parent;
    private LifecycleModel<T> model;
    /** Scale is {@link EntityPropertyLifecycle#DEFAULT_SCALE}.*/
    private final List<ValuedInterval> categoryDurations;
    private List<ValuedInterval> averageCategoryDurations;

    private final List<Comparable> entityKeys;
    private final int size;

    private final boolean timeDistributed;

    // property-distribution related:
    private final List<Integer> indexes;

    /**
     * Creates property-distribution group with key pair "property = value" and accumulated from "parent" for "indexed" lifecycles.
     *
     * @param groupingProperty
     * @param groupingValue
     * @param parent
     * @param indexes
     */
    public Group(final IValueProperty property, final Object value, final LifecycleModel<T> parent, final List<Integer> indexes) {
	this.timeDistributed = false;
	this.property = property;
	this.value = value;
	this.parent = parent;

	this.indexes = indexes;

	final List<? extends EntityPropertyLifecycle<T>> operationalLifecycles = propertyDistributionLifecycles();
	// calculate immutable category durations:
	this.categoryDurations = calculateCategoryDurations(operationalLifecycles);
	// keys of entities:
	this.entityKeys = getKeys(operationalLifecycles);
	this.size = operationalLifecycles.size();
    }

    /**
     * Creates time-distribution group for "narrowedModel".
     *
     * @param timeProperty
     * @param narrowedModel
     * @param parent
     */
    public Group(final ITimeProperty timeProperty, final LifecycleModel<T> narrowedModel, final LifecycleModel<T> parent) {
	this.timeDistributed = true;
	this.property = timeProperty;
	this.value = new Interval(narrowedModel.getLeftBoundary().getMillis(), narrowedModel.getRightBoundary().getMillis()); // narrowedModel.boundaries()
	this.parent = parent;

	this.indexes = null;

	final List<? extends EntityPropertyLifecycle<T>> operationalLifecycles = narrowedModel.getLifecycleData();
	// calculate immutable category durations:
	this.categoryDurations = calculateCategoryDurations(operationalLifecycles);
	this.entityKeys = getKeys(operationalLifecycles);
	this.size = operationalLifecycles.size();
    }

    /**
     * Returns lifecycle data for property-value distribution.
     *
     * @return
     */
    protected List<? extends EntityPropertyLifecycle<T>> propertyDistributionLifecycles(){
	final List<EntityPropertyLifecycle<T>> epls = new ArrayList<EntityPropertyLifecycle<T>>();
	for (final int index : indexes){
	    epls.add(parent.getLifecycleData().get(index));
	}
	return epls;
    }

    protected List<Comparable> getKeys(final List<? extends EntityPropertyLifecycle<T>> lifecycles){
	final List<Comparable> keys = new ArrayList<Comparable>();
	for (final EntityPropertyLifecycle<T> lifecycle : lifecycles){
	    keys.add(lifecycle.getEntity().getKey());
	}
	return keys;
    }

    /**
     * Summarizes total category durations for concrete "lifecycles".
     *
     * @param lifecycles
     * @return
     */
    protected List<ValuedInterval> calculateCategoryDurations(final List<? extends EntityPropertyLifecycle<T>> lifecycles){
	final List<ValuedInterval> categoryDurations = new ArrayList<ValuedInterval>();
	for (final EntityPropertyLifecycle<T> lifecycle : lifecycles){
	    addEntity(categoryDurations, lifecycle);
	}
	return categoryDurations;
    }


    /**
     * Accumulates lifecycle information into "categoryDurations".
     *
     * @param categoryDurations
     * @param lifecycle
     */
    protected void addEntity(final List<ValuedInterval> categoryDurations, final EntityPropertyLifecycle<T> lifecycle) {
	if (categoryDurations.isEmpty()) { // first lifecycle:
	    for (final ValuedInterval vi : lifecycle.getCategoryDurations()) {
		categoryDurations.add(new ValuedInterval(vi));
	    }
	} else {
	    for (int i = 0; i < lifecycle.getCategoryDurations().size(); i++) {
		final ValuedInterval old = categoryDurations.get(i);
		categoryDurations.set(i, old.adjustSummary(old.getSummary().add(lifecycle.getCategoryDurations().get(i).getSummary())));
	    }
	}
    }

    @Override
    public boolean isTimeDistributed() {
	return timeDistributed;
    }

    @Override
    public List<Comparable> getEntityKeys() {
	return entityKeys;
    }

    @Override
    public int size(){
	return size;
    }

    private static String periodRepresentation(final Interval interval, final ITimeProperty timeProperty) {
	final DateTime d = interval.getStart();
	if (GroupingPeriods.YEAR.equals(timeProperty)) {
	    return d.year().getAsText();
	} else if (GroupingPeriods.MONTH.equals(timeProperty)) {
	    return d.monthOfYear().getAsText() + " " + d.year().getAsText();
	} else if (GroupingPeriods.FORTNIGHT.equals(timeProperty)) {
	    return (d.weekOfWeekyear().get() / 2) + " fortnight (" + periodRepresentation(interval, GroupingPeriods.MONTH) + ")";
	} else if (GroupingPeriods.WEEK.equals(timeProperty)) {
	    return d.weekOfWeekyear().getAsText() + " week (" + periodRepresentation(interval, GroupingPeriods.MONTH) + ")";
	} else if (GroupingPeriods.DAY.equals(timeProperty)) {
	    return d.dayOfMonth().getAsText() + " " + periodRepresentation(interval, GroupingPeriods.MONTH);
	} else {
	    throw new UnsupportedOperationException("Grouping period [" + timeProperty.getTitle() + "] is not supported yet.");
	}
    }

    @Override
    public boolean equals(final Object obj) {
	if (this == obj) {
	    return true;
	}
	if (!(obj instanceof Group)) {
	    return false;
	}
	// let's ensure that types match
	final Group<?> that = (Group<?>) obj;
	// now can compare key values
	final Object thatValue = that.getValue();
	return getValue() != null && getValue().equals(thatValue) || getValue() == null && thatValue == null;
    }

    /**
     * Hashing is based on the business key implementation.
     */
    @Override
    public final int hashCode() {
	return (getValue() != null ? getValue().hashCode() : 0) * 23;
    }

    /**
     * Creates grouping values from parent values and current grouping "property" and "value".
     *
     * @return
     */
    protected LinkedHashMap<IProperty, Object> extractGroupingValues(){
	final LinkedHashMap<IProperty, Object> thisGroupingValues = new LinkedHashMap<IProperty, Object>(parent.getGroupingValues());
	thisGroupingValues.put(property, value);
	return thisGroupingValues;
    }

    @Override
    public IProperty getProperty() {
        return property;
    }

    @Override
    public Object getValue() {
        return value;
    }

    @Override
    public LifecycleModel<T> getParent() {
        return parent;
    }

    @Override
    public LifecycleModel<T> getModel() {
	if (model == null){
	    model = initiateLifecycleModel();
	}
        return model;
    }

    /** Returns arithmetic average category durations for this group's entities. */
    private List<ValuedInterval> getAverageCategoryDurations() {
	if (averageCategoryDurations == null) {
	    averageCategoryDurations = new ArrayList<ValuedInterval>();
	    for (final ValuedInterval summaryDuration : getCategoryDurations()) {
		averageCategoryDurations.add(summaryDuration.adjustSummary(EntityPropertyLifecycle.divide(summaryDuration.getSummary(), new BigDecimal(entityKeys.size()).setScale(EntityPropertyLifecycle.DEFAULT_SCALE))));
	    }
	}
	return averageCategoryDurations;
    }

    /** Summarized category durations for group entities. */
    protected List<ValuedInterval> getCategoryDurations() {
	return categoryDurations;
    }

    @Override
    public List<ValuedInterval> getCategoryDurations(final boolean total) {
	return total ? getCategoryDurations() : getAverageCategoryDurations();
    }

    @Override
    public String getInfo(){
	return getInfo(getValue(), getProperty());
    }

    public static String getInfo(final Object value, final IProperty property){
	if (property instanceof ITimeProperty){
	    return periodRepresentation((Interval) value, (ITimeProperty)property);
	}
	return value + "";
    }

    /**
     * Extracts {@link LifecycleModel} from this group.
     *
     * @return
     */
    protected abstract LifecycleModel<T> initiateLifecycleModel();
}