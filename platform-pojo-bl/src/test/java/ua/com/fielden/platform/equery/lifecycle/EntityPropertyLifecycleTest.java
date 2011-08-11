package ua.com.fielden.platform.equery.lifecycle;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.junit.Test;

import ua.com.fielden.platform.entity.Entity;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.equery.lifecycle.EntityPropertyLifecycle;
import ua.com.fielden.platform.equery.lifecycle.ValuedInterval;
import ua.com.fielden.platform.test.CommonTestEntityModuleWithPropertyFactory;
import ua.com.fielden.platform.utils.Pair;

import com.google.inject.Guice;
import com.google.inject.Injector;

/**
 * Test for {@link EntityPropertyLifecycle} logic.
 *
 * @author Jhou
 *
 */
public class EntityPropertyLifecycleTest {
    private final Injector injector = Guice.createInjector(new CommonTestEntityModuleWithPropertyFactory());
    private final EntityFactory factory = injector.getInstance(EntityFactory.class);

    private EntityPropertyLifecycle<Entity> epl;

    private List<ValuedInterval> createIntervals(final boolean withInnerIntersection, final boolean withEqualIntervals){
	final List<ValuedInterval> intervals = new ArrayList<ValuedInterval>();
	// not sorted:
	intervals.add(new ValuedInterval(date(withInnerIntersection ? 34 : 36), date(42), "Value 1"));
	intervals.add(new ValuedInterval(date(2), date(10), "Value 2"));
	intervals.add(new ValuedInterval(date(42), date(47), "Value 4"));
	intervals.add(new ValuedInterval(date(20), date(29), "Value 2"));

	if (withEqualIntervals){
	    // non-unique interval should be ignored during lifecycle creation:
	    intervals.add(new ValuedInterval(date(20), date(29), "Value 2"));
	}

	intervals.add(new ValuedInterval(date(31), date(withInnerIntersection ? 38 : 36), "Value 3"));
	intervals.add(new ValuedInterval(date(47), date(59), "Value 2"));
	return intervals;
    }


    public EntityPropertyLifecycle<Entity> createEPL(final boolean withInnerIntersection, final boolean withEqualIntervals) {
	return new EntityPropertyLifecycle<Entity>(factory.newByKey(Entity.class, "entityKey"), Entity.class, "monitoring", createIntervals(withInnerIntersection, withEqualIntervals), date(15), date(55));
    }

    public EntityPropertyLifecycle<Entity> createEPL() {
	return createEPL(false, false);
    }

    @Test
    public void test_categorizer_initialization() {
	epl = createEPL();
	assertNotNull("Categorizer should not be null.", epl.getCategorizer());
    }

    @Test
    public void test_value_determination() {
	epl = createEPL();

	assertEquals("Should be equal.", "Value 2", epl.getValue(date(2)));
	assertEquals("Should be equal.", "Value 2", epl.getValue(date(7)));
	assertEquals("Should be equal.", "Value 2", epl.getValue(date(9)));

	boolean exceptionWasThrown = false;
	try {
	    epl.getValue(date(10));
	} catch (final Exception e) {
	    exceptionWasThrown = true;
	}
	assertTrue("Exception should be thrown.", exceptionWasThrown);

	assertEquals("Should be equal.", "Value 3", epl.getValue(date(31)));
	assertEquals("Should be equal.", "Value 3", epl.getValue(date(35)));
	assertEquals("Should be equal.", "Value 1", epl.getValue(date(36)));
	assertEquals("Should be equal.", "Value 4", epl.getValue(date(42)));
	assertEquals("Should be equal.", "Value 2", epl.getValue(date(47)));
	assertEquals("Should be equal.", "Value 2", epl.getValue(date(55)));

	exceptionWasThrown = false;
	try {
	    epl.getValue(date(59));
	} catch (final Exception e) {
	    exceptionWasThrown = true;
	}
	assertTrue("Exception should be thrown.", exceptionWasThrown);

    }

    @Test
    public void test_categorization_and_availability_determination() {
	epl = createEPL();

	assertFalse("Should not be available. Outside of left boundary!", epl.isAvailable(date(2)));
	assertFalse("Should not be available. Outside of left boundary!", epl.isAvailable(date(7)));
	assertFalse("Should not be available. Outside of left boundary!", epl.isAvailable(date(9)));
	assertFalse("Should not be available. Outside of left boundary!", epl.isAvailable(date(10)));

	boolean exceptionWasThrown = false;
	try {
	    epl.isAvailable(date(29));
	} catch (final Exception e) {
	    exceptionWasThrown = true;
	}
	assertTrue("Exception should be thrown.", exceptionWasThrown);

	exceptionWasThrown = false;
	try {
	    epl.isAvailable(date(30));
	} catch (final Exception e) {
	    exceptionWasThrown = true;
	}
	assertTrue("Exception should be thrown.", exceptionWasThrown);

	assertFalse("Should be not available.", epl.isAvailable(date(31)));
	assertFalse("Should be not available.", epl.isAvailable(date(35)));
	assertTrue("Should be available.", epl.isAvailable(date(36)));
	assertFalse("Should be not available.", epl.isAvailable(date(42)));
	assertTrue("Should be available.", epl.isAvailable(date(47)));
	assertFalse("Should not be available. Outside of right boundary!", epl.isAvailable(date(55)));
	assertFalse("Should not be available. Outside of right boundary!", epl.isAvailable(date(59)));
    }

    @Test
    public void test_incorrect_intersected_intervals_exception_throwing() {
	boolean exceptionWasThrown = false;
	try {
	    createEPL(true, false);
	} catch (final Exception e) {
	    exceptionWasThrown = true;
	}
	assertTrue("Exception should be thrown.", exceptionWasThrown);
    }

    @Test
    public void test_incorrect_equal_intervals_exception_throwing() {
	boolean exceptionWasThrown = false;
	try {
	    createEPL(false, true);
	} catch (final Exception e) {
	    exceptionWasThrown = true;
	}
	assertTrue("Exception should be thrown.", exceptionWasThrown);
    }

    @Test
    public void test_availability_calculation() {
	epl = createEPL();
	assertEquals("Availability calculated incorrectly.", new BigDecimal(29 - 20 + 42 - 36 + 55 - 47).setScale(EntityPropertyLifecycle.DEFAULT_SCALE).divide(new BigDecimal(29 - 20 + 42 - 31 + 55 - 47).setScale(EntityPropertyLifecycle.DEFAULT_SCALE), RoundingMode.HALF_UP), epl.getAvailability()); // 0.000000001
    }

    @Test
    public void test_fractions_calculation() {
	epl = createEPL();
	epl.getAvailability();

	final List<ValuedInterval> fractions = new ArrayList<ValuedInterval>();
	long x = 1000;
	fractions.add(new ValuedInterval(new DateTime(x++), "1", new BigDecimal(23L).setScale(EntityPropertyLifecycle.DEFAULT_SCALE), true));
	fractions.add(new ValuedInterval(new DateTime(x++), "1", new BigDecimal(5L).setScale(EntityPropertyLifecycle.DEFAULT_SCALE), false));
	fractions.add(new ValuedInterval(new DateTime(x++), "1", new BigDecimal(5L).setScale(EntityPropertyLifecycle.DEFAULT_SCALE), false));

	assertEquals("Fractions calculated incorrectly.", fractions, epl.getCategoryDurations());

	for (int i = 0; i < fractions.size(); i++) {
	    assertEquals("Concrete fraction calculated incorrectly.", fractions.get(i).getSummary(), epl.getCategoryDurations().get(i).getSummary());
	}
    }

    @Test
    public void test_valued_intervals_time_splitting() {
	final Interval boundaries = new Interval(date(15), date(55));

	final List<ValuedInterval> simpleIntervals = createIntervals(false, false);
	Collections.sort(simpleIntervals); // valuedIntervals should be sorted, if not, results will be unpredictable.

	DateTime current;
	//  splitting outside of the boundaries :
	try {
	    EntityPropertyLifecycle.split(simpleIntervals, current = date(5), boundaries);
	    fail("Could not be splitted by moment = " + current + " outside boundaries = " + boundaries + ".");
	} catch (final Exception e) {
	}
	try {
	    EntityPropertyLifecycle.split(simpleIntervals, current = date(55), boundaries);
	    fail("Could not be splitted by moment = " + current + " outside boundaries = " + boundaries + ".");
	} catch (final Exception e) {
	}
	try {
	    EntityPropertyLifecycle.split(simpleIntervals, current = date(59), boundaries);
	    fail("Could not be splitted by moment = " + current + " outside boundaries = " + boundaries + ".");
	} catch (final Exception e) {
	}

	// empty space splitting :
	assertEquals("Should be equal.", splitByIndex(simpleIntervals, 1), EntityPropertyLifecycle.split(simpleIntervals, current = date(15), boundaries));
	assertEquals("Should be equal.", splitByIndex(simpleIntervals, 1), EntityPropertyLifecycle.split(simpleIntervals, current = date(17), boundaries));
	assertEquals("Should be equal.", splitByIndex(simpleIntervals, 2), EntityPropertyLifecycle.split(simpleIntervals, current = date(29), boundaries));
	assertEquals("Should be equal.", splitByIndex(simpleIntervals, 2), EntityPropertyLifecycle.split(simpleIntervals, current = date(30), boundaries));

	// splitting moment is the left end of some interval (near empty space) :
	current = date(20);
	assertEquals("Should be equal.", splitByFoundedIndex(simpleIntervals, 1, current), EntityPropertyLifecycle.split(simpleIntervals, current, boundaries));
	current = date(31);
	assertEquals("Should be equal.", splitByFoundedIndex(simpleIntervals, 2, current), EntityPropertyLifecycle.split(simpleIntervals, current, boundaries));

	// splitting moment is the left end of some interval (near other interval) :
	current = date(36);
	assertEquals("Should be equal.", splitByFoundedIndex(simpleIntervals, 3, current), EntityPropertyLifecycle.split(simpleIntervals, current, boundaries));
	current = date(42);
	assertEquals("Should be equal.", splitByFoundedIndex(simpleIntervals, 4, current), EntityPropertyLifecycle.split(simpleIntervals, current, boundaries));
	current = date(47);
	assertEquals("Should be equal.", splitByFoundedIndex(simpleIntervals, 5, current), EntityPropertyLifecycle.split(simpleIntervals, current, boundaries));

	// splitting moment is inside of some interval :
	current = date(25);
	assertEquals("Should be equal.", splitByFoundedIndex(simpleIntervals, 1, current), EntityPropertyLifecycle.split(simpleIntervals, current, boundaries));
	current = date(33);
	assertEquals("Should be equal.", splitByFoundedIndex(simpleIntervals, 2, current), EntityPropertyLifecycle.split(simpleIntervals, current, boundaries));
	current = date(39);
	assertEquals("Should be equal.", splitByFoundedIndex(simpleIntervals, 3, current), EntityPropertyLifecycle.split(simpleIntervals, current, boundaries));
	current = date(44);
	assertEquals("Should be equal.", splitByFoundedIndex(simpleIntervals, 4, current), EntityPropertyLifecycle.split(simpleIntervals, current, boundaries));
	current = date(51);
	assertEquals("Should be equal.", splitByFoundedIndex(simpleIntervals, 5, current), EntityPropertyLifecycle.split(simpleIntervals, current, boundaries));
    }

    @Test
    public void test_valued_intervals_time_splitting_in_case_of_intersecting_intervals() {
	final Interval boundaries = new Interval(date(15), date(55));

	final List<ValuedInterval> intervalsWithInnerIntersection = createIntervals(true, false);
	Collections.sort(intervalsWithInnerIntersection); // valuedIntervals should be sorted, if not, results will be unpredictable.

	// splitting moment is inside of some intervals (more than one) :
	DateTime current = date(36);
	Pair<List<ValuedInterval>, List<ValuedInterval>> expected = new Pair<List<ValuedInterval>, List<ValuedInterval>>(
		Arrays.asList(new ValuedInterval(date(2), date(10), "Value 2"), //
				new ValuedInterval(date(20), date(29), "Value 2"), //
				new ValuedInterval(date(31), date(36), "Value 3"), //
				new ValuedInterval(date(34), date(36), "Value 1")), //
		Arrays.asList(new ValuedInterval(date(36), date(38), "Value 3"), //
				new ValuedInterval(date(36), date(42), "Value 1"), //
				new ValuedInterval(date(42), date(47), "Value 4"), //
				new ValuedInterval(date(47), date(59), "Value 2")
				));
	assertEquals("Should be equal.", expected, EntityPropertyLifecycle.split(intervalsWithInnerIntersection, current, boundaries));

	// splitting moment is the left end of some interval that intersects with other :
	current = date(34);
	expected = new Pair<List<ValuedInterval>, List<ValuedInterval>>(
		Arrays.asList(new ValuedInterval(date(2), date(10), "Value 2"), //
				new ValuedInterval(date(20), date(29), "Value 2"), //
				new ValuedInterval(date(31), date(34), "Value 3"), //
				new ValuedInterval(date(34), date(34), "Value 1")), //
		Arrays.asList(new ValuedInterval(date(34), date(38), "Value 3"), //
				new ValuedInterval(date(34), date(42), "Value 1"), //
				new ValuedInterval(date(42), date(47), "Value 4"), //
				new ValuedInterval(date(47), date(59), "Value 2")
				));
	assertEquals("Should be equal.", expected, EntityPropertyLifecycle.split(intervalsWithInnerIntersection, current, boundaries));
	// splitting moment is the right end of some interval that intersects with other : (but right end is not inclusive!!!)
	current = date(38);
	assertEquals("Should be equal.", splitByFoundedIndex(intervalsWithInnerIntersection, 3, current), EntityPropertyLifecycle.split(intervalsWithInnerIntersection, current, boundaries));
    }


    private static <T extends ValuedInterval> Pair<List<T>, List<T>> splitByFoundedIndex(final List<T> list, final int foundedIndex, final DateTime moment){
	final Pair<T, T> founded = (Pair<T, T>) list.get(foundedIndex).split(moment);
	final List<T> left = new ArrayList<T>(list.subList(0, foundedIndex));
	left.add(founded.getKey());
	final List<T> right = new ArrayList<T>();
	right.add(founded.getValue());
	right.addAll(new ArrayList<T>(list.subList(foundedIndex + 1, list.size())));
	return new Pair<List<T>, List<T>>(left, right);
    }

    private static <T> Pair<List<T>, List<T>> splitByIndex(final List<T> list, final int index){
	return new Pair<List<T>, List<T>>(list.subList(0, index), list.subList(index, list.size()));
    }

    private DateTime date(final int millis) {
	return new DateTime(2010, 1, 1, 0, 0, 0, millis);
    }
}
