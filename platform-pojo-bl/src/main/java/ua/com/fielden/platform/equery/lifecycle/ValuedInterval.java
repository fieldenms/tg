package ua.com.fielden.platform.equery.lifecycle;

import java.awt.Color;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.Period;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.types.ICategory;
import ua.com.fielden.platform.utils.EntityUtils;
import ua.com.fielden.platform.utils.Pair;
import ua.com.fielden.snappy.DateUtilities;

/**
 * Simple date interval with value.
 *
 * @author Jhou
 *
 */
public /* final */ class ValuedInterval implements Comparable<ValuedInterval>, Cloneable {
    private static final long serialVersionUID = -9127726690624265817L;

    private final DateTime key; // middle point of interval should be set as "key" for this entity. So comparison of two ValuedInterval's will be performed by "middle" point.
    private final String desc;
    private final DateTime from;
    private final DateTime to;
    private Color color;
    private final/* transient */Object value;

    private final Integer availability;
    private final BigDecimal summary;
    private final Boolean normal;
    private final String info;

    private ICategory category;

    private final/* transient */Interval interval;

    /**
     * Mainly used for serialisation.
     */
    protected ValuedInterval() {
	key = null;
	desc = null;
	from = null;
	to = null;
	value = null;
	availability = null;
	summary = null;
	normal = null;
	info = null;
	interval = null;
    }

    /**
     * Creates interval with known corresponding value.
     *
     * @param entityFactory
     * @param from
     * @param to
     * @param value
     * @return
     */
    public ValuedInterval(final DateTime from, final DateTime to, final Object value) {
	if (! (DateUtilities.isBefore(from, to) || EntityUtils.equalsEx(from, to)) ) {
	    throw new IllegalArgumentException("From date [" + from + "] is not before or equal to To date [" + to + "].");
	}
	this.from = from;
	// TODO null need to be handled in appropriate way in entity lifecycle logic. It should be also unit tested!
	// TODO null need to be handled in appropriate way in entity lifecycle logic. It should be also unit tested!
	// TODO null need to be handled in appropriate way in entity lifecycle logic. It should be also unit tested!
	// TODO null need to be handled in appropriate way in entity lifecycle logic. It should be also unit tested!
	// TODO null need to be handled in appropriate way in entity lifecycle logic. It should be also unit tested!
	// TODO null need to be handled in appropriate way in entity lifecycle logic. It should be also unit tested!
	// TODO null need to be handled in appropriate way in entity lifecycle logic. It should be also unit tested!
	// TODO null need to be handled in appropriate way in entity lifecycle logic. It should be also unit tested!
	// TODO null need to be handled in appropriate way in entity lifecycle logic. It should be also unit tested!
	// TODO null need to be handled in appropriate way in entity lifecycle logic. It should be also unit tested!
	// TODO null need to be handled in appropriate way in entity lifecycle logic. It should be also unit tested!
	// TODO null need to be handled in appropriate way in entity lifecycle logic. It should be also unit tested!
	this.to = (to == null) ? new DateTime(2100, 1, 1, 0, 0, 0, 0) : to;
	this.key = new DateTime((from.getMillis() + (this.to == null ? 0 : this.to.getMillis())) / 2, from.getChronology());
	this.interval = new Interval(from, this.to);

	this.desc = null;
	this.color = null;
	this.value = value;
	this.availability = null;
	this.summary = null;
	this.normal = null;
	this.info = updateInfoByValue();
    }

    /**
     * Creates colored value.
     *
     * @param entityFactory
     * @param from
     * @param to
     * @param value
     * @return
     */
    public ValuedInterval(final DateTime middlePointEntityKey, final Object value, final String desc, final Color color) {
	this.from = null;
	this.to = null;
	this.interval = null;
	this.key = middlePointEntityKey;

	this.desc = desc;
	this.color = color;
	this.value = value;
	this.availability = null;
	this.summary = null;
	this.normal = null;

	this.info = updateInfoByValueWithoutBoundaries();
    }

    /**
     * Creates value with desc.
     *
     * @param entityFactory
     * @param from
     * @param to
     * @param value
     * @return
     */
    public ValuedInterval(final DateTime middlePointEntityKey, final Object value, final BigDecimal summary, final boolean normal) {
	this.from = null;
	this.to = null;
	this.interval = null;
	this.key = middlePointEntityKey;

	this.desc = null;
	this.color = null;
	this.value = value;
	this.availability = null;
	this.summary = summary;
	this.normal = normal;

	this.info = updateInfoByValueWithoutBoundaries();
    }

    /**
     * Creates interval with known availability.
     *
     * @param entityFactory
     * @param from
     * @param to
     * @param availability
     * @return
     */
    public ValuedInterval(final DateTime from, final DateTime to, final Integer availability) {
	this.from = from;
	this.to = to;
	this.key = new DateTime((from.getMillis() + (to == null ? 0 : to.getMillis())) / 2, from.getChronology());
	this.interval = new Interval(from, to);

	this.desc = null; // "Valued Interval description";
	this.color = null;
	this.value = null;
	this.availability = availability;
	this.summary = null;
	this.normal = null;

	this.info = updateInfoByAvailability();
    }

    /**
     * Returns exact copy of ValuedInterval with overridden summary.
     *
     * @param x
     * @return
     */
    public ValuedInterval adjustSummary(final BigDecimal summary) {
	return new ValuedInterval(this, summary);
    }

    /**
     * Returns split ValuedIntervals by moment inside this interval boundaries.
     *
     * @param moment
     * @return
     */
    public Pair<? extends ValuedInterval, ? extends ValuedInterval> split(final DateTime moment) {
	return new Pair<ValuedInterval, ValuedInterval>(new ValuedInterval(this, from, moment), new ValuedInterval(this, moment, to));
    }

    /**
     * Copy constructor.
     *
     * @param vi
     */
    public ValuedInterval(final ValuedInterval vi) {
	key = vi.key;
	desc = vi.desc;
	from = vi.from;
	to = vi.to;
	color = vi.color;
	value = vi.value;

	availability = vi.availability;
	summary = vi.summary;
	normal = vi.normal;
	info = vi.info;

	category = vi.category;

	interval = vi.interval;
    }

    private ValuedInterval(final ValuedInterval vi, final BigDecimal newSummary) {
	key = vi.key;
	desc = vi.desc;
	from = vi.from;
	to = vi.to;
	color = vi.color;
	value = vi.value;

	availability = vi.availability;
	summary = newSummary;
	normal = vi.normal;
	info = vi.info;

	category = vi.category;

	interval = vi.interval;
    }

    protected ValuedInterval(final ValuedInterval vi, final DateTime from, final DateTime to) {
	key = new DateTime((from.getMillis() + (to == null ? 0 : to.getMillis())) / 2, from.getChronology());
	desc = vi.desc;
	this.from = from;
	this.to = to;
	color = vi.color;
	value = vi.value;

	availability = vi.availability;
	summary = vi.summary;
	normal = vi.normal;
	info = vi.info;

	category = vi.category;

	interval = vi.interval;
    }

    /**
     * Updates info according to {@link ValuedInterval}'s value and boundaries.
     */
    private String updateInfoByValue() {
	return updateInfoBy(value, true);
    }

    /**
     * Updates info according to {@link ValuedInterval}'s value.
     */
    private String updateInfoByValueWithoutBoundaries() {
	return updateInfoBy(value, false);
    }

    /**
     * Updates info according to {@link ValuedInterval}'s value and boundaries.
     */
    private String updateInfoByAvailability() {
	return updateInfoBy(availability, true);
    }

    public static String valueRepresentation(final Object v, final String simpleRepresentation){
	return v instanceof AbstractEntity ? ((AbstractEntity) v).getKey() + ("<br><i>" + ((AbstractEntity) v).getDesc() + "</i>") : simpleRepresentation;
    }

    /**
     * Updates info according to value and boundaries.
     */
    private String updateInfoBy(final Object value, final boolean useBoundaries) {
	final Object v = value;
	final String strValue = valueRepresentation(v, "" + v);
	if (!useBoundaries) {
	    return v instanceof AbstractEntity ? "<html>" + strValue + "</html>" : "" + strValue;
	}
	final Interval interval = new Interval(from, to);
	final DateFormat f = new SimpleDateFormat("dd/MM/yyyy hh:mma", Locale.getDefault());
	final String periodStr = (from == null ? "-\u221E" : f.format(from.toDate())) + "; " + (to == null ? "\u221E" : f.format(to.toDate()));
	return "<html>" + strValue + "<br><br>" + periodLengthStr(interval.toPeriod(), false) + "<br>" + "[<i>" + periodStr + "</i>]" + "</html>";
    }

    private final static PeriodFormatter periodFormatter = new PeriodFormatterBuilder().//
    appendYears().appendSuffix(" year", " years").appendSeparator("; ").//
    appendMonths().appendSuffix(" month", " months").appendSeparator("; ").//
    appendWeeks().appendSuffix(" week", " weeks").appendSeparator("; ").//
    appendDays().appendSuffix(" day", " days").appendSeparator("; ").//
    appendHours().appendSuffix(" hour", " hours").appendSeparator("; ").//
    appendMinutes().appendSuffix(" minute", " minutes").toFormatter();

    private final static PeriodFormatter shortPeriodFormatter = new PeriodFormatterBuilder().//
    appendYears().appendSuffix(" year", " years").appendSeparator("; ").//
    appendMonths().appendSuffix(" month", " months").toFormatter();

    public static String periodLengthStr(final Period period, final boolean shortFormat) {
	return shortFormat ? period.toString(shortPeriodFormatter) : "[<i>" + period.toString(periodFormatter) + "</i>]";
    }

    public DateTime getFrom() {
	return from;
    }

    public DateTime getTo() {
	return to;
    }

    public Object getValue() {
	return value;
    }

    public Interval getInterval() {
	return interval;
    }

    public Color getColor() {
	return color;
    }

    public String getInfo() {
	return info;
    }

    public Integer getAvailability() {
	return availability;
    }

    public BigDecimal getSummary() {
	return summary;
    }

    public Boolean getNormal() {
	return normal;
    }

    public DateTime getKey() {
	return key;
    }

    public String getDesc() {
	return desc;
    }

    // setters:
    protected void setColor(final Color color) {
	this.color = color;
    }

    /**
     * Implements comparison based on the business key implementation.
     */
    public final int compareTo(final ValuedInterval cmpTo) {
	return getKey().compareTo(cmpTo.getKey());
    }

    /**
     * Hashing is based on the business key implementation.
     */
    @Override
    public final int hashCode() {
	return (getKey() != null ? getKey().hashCode() : 0) * 23;
    }

    /**
     * Equality is based on the business key implementation.
     */
    @Override
    public final boolean equals(final Object obj) {
	if (this == obj) {
	    return true;
	}
	if (!(obj instanceof ValuedInterval)) {
	    return false;
	}
	// let's ensure that types match
	final ValuedInterval that = (ValuedInterval) obj;
	if (this.getClass() != that.getClass()) {
	    return false;
	}
	// now can compare key values
	final Object thatKey = that.getKey();
	return getKey() != null && getKey().equals(thatKey) || getKey() == null && thatKey == null;
    }

    @Override
    public String toString() {
	return getKey() != null ? getKey().toString() : null;
    }

    public boolean contains(final DateTime moment) {
	return (getInterval() == null ? new Interval(from, to) : getInterval()).contains(moment);
    }

    public ICategory getCategory() {
	return category;
    }

    public void setCategory(final ICategory category) {
	this.category = category;
    }

    @Override
    protected ValuedInterval clone() {
        return new ValuedInterval(this);
    }
}
