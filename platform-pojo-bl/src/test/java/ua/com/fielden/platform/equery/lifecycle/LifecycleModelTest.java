package ua.com.fielden.platform.equery.lifecycle;

import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import org.joda.time.DateTime;
import org.junit.Test;

import ua.com.fielden.platform.entity.Entity;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.equery.lifecycle.IProperty.ITimeProperty;
import ua.com.fielden.platform.equery.lifecycle.IProperty.IValueProperty;
import ua.com.fielden.platform.ioc.ApplicationInjectorFactory;
import ua.com.fielden.platform.test.CommonTestEntityModuleWithPropertyFactory;
import ua.com.fielden.platform.utils.Pair;

import com.google.inject.Injector;
import com.google.inject.Module;

/**
 * Contains tests for lifecycle information and interval logic.
 *
 * @author TG Team
 *
 */
public class LifecycleModelTest {
    private final Module module = new CommonTestEntityModuleWithPropertyFactory();
    private final Injector injector = new ApplicationInjectorFactory().add(module).getInjector();
    private final EntityFactory factory = injector.getInstance(EntityFactory.class);

    public DateTime date(final int millis) {
	return new DateTime(2010, 1, 1, 0, 0, 0, millis);
    }

    /**
     * Example {@link EntityPropertyLifecycle} with some "x" offset.
     *
     * @param x
     * @return
     */
    public EntityPropertyLifecycle<Entity> createEPL(final String key, final int x) {
	final List<ValuedInterval> intervals = new ArrayList<ValuedInterval>();
	// not sorted:
	intervals.add(new ValuedInterval(date(36 + x), date(42 + x), "Value 1"));
	intervals.add(new ValuedInterval(date(2 + x), date(10 + x), "Value 2"));
	intervals.add(new ValuedInterval(date(42 + x), date(47 + x), "Value 4"));
	intervals.add(new ValuedInterval(date(10 + x), date(31 + x), "Value 2"));
	intervals.add(new ValuedInterval(date(31 + x), date(36 + x), "Value 3"));
	intervals.add(new ValuedInterval(date(47 + x), date(59 + x), "Value 2"));
	return new EntityPropertyLifecycle<Entity>(factory.newByKey(Entity.class, key), Entity.class, "monitoring", intervals, date(15), date(55));
    }

    public EntityPropertyLifecycle<Entity> createUnavailableEPL(final String key) {
	final List<ValuedInterval> intervals = new ArrayList<ValuedInterval>();
	intervals.add(new ValuedInterval(date(15), date(55), "Value 4"));
	return new EntityPropertyLifecycle<Entity>(factory.newByKey(Entity.class, key), Entity.class, "monitoring", intervals, date(15), date(55));
    }


    @Test
    public void test_summary_availability_determination() {
	final List<EntityPropertyLifecycle<Entity>> ld = new ArrayList<EntityPropertyLifecycle<Entity>>();
	ld.add(createEPL("A0001", 0));
	ld.add(createEPL("A0002", 5)); // the same lifecycle just shifted to the right on 5 millis.
	ld.add(createUnavailableEPL("A0003")); // non-"main" category defines all lifecycle.

	final LifecycleModel<Entity> lm = new LifecycleModel<Entity>(Entity.class, date(15), date(55), ld, new LinkedHashMap<IProperty, Object>(), true){
	    @Override
	    protected IGroup<Entity> createGroupByValue(final IValueProperty property, final Object value, final LifecycleModel<Entity> parent, final List<Integer> indexes) {
		return null;
	    }

	    @Override
	    protected IGroup<Entity> createGroupByModelAndPeriod(final ITimeProperty timeProperty, final LifecycleModel<Entity> narrowedModel, final LifecycleModel<Entity> parent) {
		return null;
	    }

	    @Override
	    public Pair<? extends LifecycleModel<Entity>, ? extends LifecycleModel<Entity>> split(final DateTime moment, final boolean copy, final boolean full) {
		return null;
	    }

	    @Override
	    protected LifecycleModel<Entity> copy(final LinkedHashMap<IProperty, Object> extractedGroupingValues) {
		return null;
	    }
	};
	final List<ValuedInterval> expectedSummaryAvailability = new ArrayList<ValuedInterval>();
	expectedSummaryAvailability.add(intrvl(15, 31, 2));
	expectedSummaryAvailability.add(intrvl(31, 36, 1));
	expectedSummaryAvailability.add(intrvl(36, 41, 1));
	expectedSummaryAvailability.add(intrvl(41, 42, 2));
	expectedSummaryAvailability.add(intrvl(42, 47, 1));
	expectedSummaryAvailability.add(intrvl(47, 52, 1));
	expectedSummaryAvailability.add(intrvl(52, 55, 2));
	assertEquals("Expected summary avalability is not equal to actual.", expectedSummaryAvailability, lm.getSummaryAvailability());
    }

    @Test
    public void test_average_availability_determination() {
	final List<EntityPropertyLifecycle<Entity>> ld = new ArrayList<EntityPropertyLifecycle<Entity>>();
	ld.add(createEPL("A0001", 0));
	ld.add(createEPL("A0002", 5)); // the same lifecycle just shifted to the right on 5 millis.
	ld.add(createEPL("A0003", -2));
	ld.add(createEPL("A0004", 2));
	ld.add(createEPL("A0005", 1));
	ld.add(createEPL("A0006", -1));

	ld.add(createUnavailableEPL("A0007"));

	final LifecycleModel<Entity> lm = new LifecycleModel<Entity>(Entity.class, date(15), date(55), ld, new LinkedHashMap<IProperty, Object>(), true){
	    @Override
	    protected IGroup<Entity> createGroupByValue(final IValueProperty property, final Object value, final LifecycleModel<Entity> parent, final List<Integer> indexes) {
		return null;
	    }

	    @Override
	    protected IGroup<Entity> createGroupByModelAndPeriod(final ITimeProperty timeProperty, final LifecycleModel<Entity> narrowedModel, final LifecycleModel<Entity> parent) {
		return null;
	    }

	    @Override
	    public Pair<? extends LifecycleModel<Entity>, ? extends LifecycleModel<Entity>> split(final DateTime moment, final boolean copy, final boolean full) {
		return null;
	    }

	    @Override
	    protected LifecycleModel<Entity> copy(final LinkedHashMap<IProperty, Object> extractedGroupingValues) {
		return null;
	    }

	};
	// test average summary availability:
	final Double expectedAverageSummaryAvailability = 4.5d;
	assertEquals("Expected average summary avalability is not equal to actual.", expectedAverageSummaryAvailability, lm.getAverageSummaryAvailability());
	assertEquals("Expected max summary avalability is not equal to actual.", 6L, (long) lm.getMaxSummaryAvailability());
	assertEquals("Expected min summary avalability is not equal to actual.", 1L, (long) lm.getMinSummaryAvailability());
	// test average relative availability:
	final BigDecimal expectedAverageRelativeAvailability = new BigDecimal("0.857142857142857"); // (714285)
	assertEquals("Expected average relative avalability is not equal to actual.", expectedAverageRelativeAvailability, lm.getAverageRelativeAvailability()); // 0.0000001
    }

    private ValuedInterval intrvl(final int fromMillis, final int toMillis, final Integer avail) {
	final ValuedInterval vi = new ValuedInterval(date(fromMillis), date(toMillis), avail);
	return vi;
    }

}
