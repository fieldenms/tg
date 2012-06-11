package ua.com.fielden.platform.entity.matcher;

import java.util.List;

import org.junit.Test;

import ua.com.fielden.platform.basic.IValueMatcher;
import ua.com.fielden.platform.dao.factory.DaoFactory;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.meta.PropertyDescriptor;
import ua.com.fielden.platform.sample.domain.TgBogieClass;
import ua.com.fielden.platform.sample.domain.TgWagon;
import ua.com.fielden.platform.sample.domain.TgWagonClass;
import ua.com.fielden.platform.sample.domain.TgWagonClassCompatibility;
import ua.com.fielden.platform.sample.domain.TgWoStatusRequiredField;
import ua.com.fielden.platform.sample.domain.TgWorkOrder;
import ua.com.fielden.platform.test.AbstractDomainDrivenTestCase;
import ua.com.fielden.platform.test.PlatformTestDomainTypes;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetch;

/**
 * Test the ability of the ValueMatcherFactory to correctly provide value matchers.
 *
 * @author TG Team
 *
 */
public class DaoValueMatcherFactoryTest extends AbstractDomainDrivenTestCase {
    private DaoFactory daoFactory = getInstance(DaoFactory.class);
    private EntityFactory entityFactory = getInstance(EntityFactory.class);

    @Test
    public void test_instantiation() {
	final IValueMatcherFactory vmFactory = new ValueMatcherFactory(daoFactory, entityFactory);

	final IValueMatcher<?> matcher = vmFactory.getValueMatcher(TgWagon.class, "wagonClass");
	assertNotNull("Should have constructed a value matcher.", matcher);
	assertEquals("Incorrect number of matching values.", 2, matcher.findMatches("W%").size());
	assertTrue("New matcher should not have been created.", matcher == vmFactory.getValueMatcher(TgWagon.class, "wagonClass"));
    }

    @Test
    public void test_matcher_usage_with_model() {
	final IValueMatcherFactory vmFactory = new ValueMatcherFactory(daoFactory, entityFactory);

	final IValueMatcher<TgWagonClass> matcher = (IValueMatcher<TgWagonClass>) vmFactory.getValueMatcher(TgWagon.class, "wagonClass");
	//matcher.setQueryModel(select(WagonClass.class).with("compatibles", select(WagonClassCompatibility.class).model()));
	matcher.setFetchModel(fetch(TgWagonClass.class).with("compatibles", fetch(TgWagonClassCompatibility.class)));
	assertNotNull("Should have constructed a value matcher.", matcher);
	List<TgWagonClass> result = matcher.findMatches("W%");
	assertEquals("Incorrect number of matching values.", 2, result.size());
	assertEquals("Incorrect wagon class.", "WA1", result.get(0).getKey());

	assertEquals("Incorrect compatibles for wagon class.", 1, result.get(0).getCompatibles().size());

//	try {
//	    assertEquals("Incorrect compatibles for wagon class.", 1, result.get(0).getCompatibles().size());
//	    fail("Suppose to fail -- values are fetched without a query model");
//	} catch (final Exception e) {
//	}

	result = matcher.findMatchesWithModel("W%");
	assertEquals("Incorrect number of matching values.", 2, result.size());
	assertEquals("Incorrect wagon class.", "WA1", result.get(0).getKey());
	assertEquals("Incorrect compatibles for wagon class.", 1, result.get(0).getCompatibles().size());
	assertEquals("Incorrect bogie class.", "BO1", result.get(0).getCompatibles().iterator().next().getBogieClass().getKey());
    }

    @Test
    public void test_instantiation_error_handling() {
	final IValueMatcherFactory vmFactory = new ValueMatcherFactory(daoFactory, entityFactory);
	try {
	    vmFactory.getValueMatcher(TgWagon.class, "serialNo");
	    fail("Matcher creation exception is expected due to incorrect property type -- String.");
	} catch (final Exception e) {
	}

	try {
	    vmFactory.getValueMatcher(TgWagon.class, "non-existing-property");
	    fail("Matcher creation exception is expected due to incorrect property name.");
	} catch (final Exception e) {
	}
    }

    @Test
    public void test_property_descriptor_matcher() {
	final IValueMatcherFactory vmFactory = new ValueMatcherFactory(daoFactory, entityFactory);
	final IValueMatcher<PropertyDescriptor<TgWorkOrder>> matcher = (IValueMatcher<PropertyDescriptor<TgWorkOrder>>) vmFactory.getValueMatcher(TgWoStatusRequiredField.class, "requiredProperty");
	assertNotNull("Value matcher for property descriptor should have been created.", matcher);
	assertEquals("Incorrect number of matches for '*'.", 7, matcher.findMatches("*").size());
	assertEquals("Incorrect number of matches containing 'Co'.", 3, matcher.findMatches("%Co%").size());
	assertEquals("Incorrect number of matches ending with 'Cost'.", 3, matcher.findMatches("%Cost").size());

    }

    @Override
    protected void populateDomain() {
	final TgWagonClass wa1 = save(new_(TgWagonClass.class, "WA1", "desc1").setNumberOfBogies(5).setNumberOfWheelsets(2).setTonnage(50));
	final TgWagonClass wa2 = save(new_(TgWagonClass.class, "WA2", "desc2").setNumberOfBogies(2).setNumberOfWheelsets(2).setTonnage(100));

	final TgBogieClass bo1 = save(new_(TgBogieClass.class, "BO1", "desc1").setTonnage(50));

	save(new_composite(TgWagonClassCompatibility.class, wa1, bo1).setStatus("A"));

	save(new_(TgWagon.class, "WAGON1", "desc1").setWagonClass(wa1).setSerialNo("SN_1"));
	save(new_(TgWagon.class, "WAGON2", "desc2").setWagonClass(wa2).setSerialNo("SN_2"));
	save(new_(TgWagon.class, "WAGON3", "desc3").setWagonClass(wa1).setSerialNo("SN_3"));
    }

    @Override
    protected List<Class<? extends AbstractEntity<?>>> domainEntityTypes() {
	return PlatformTestDomainTypes.entityTypes;
    }
}