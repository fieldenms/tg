package ua.com.fielden.platform.entity.matcher;

import java.util.List;

import ua.com.fielden.platform.basic.IValueMatcher;
import ua.com.fielden.platform.dao.factory.DaoFactory;
import ua.com.fielden.platform.entity.meta.PropertyDescriptor;
import ua.com.fielden.platform.equery.fetch;
import ua.com.fielden.platform.test.DbDrivenTestCase;
import ua.com.fielden.platform.test.domain.entities.Wagon;
import ua.com.fielden.platform.test.domain.entities.WagonClass;
import ua.com.fielden.platform.test.domain.entities.WagonClassCompatibility;
import ua.com.fielden.platform.test.domain.entities.WoStatusRequiredField;
import ua.com.fielden.platform.test.domain.entities.WorkOrder;

/**
 * Test the ability of the ValueMatcherFactory to correctly provide value matchers.
 *
 * @author TG Team
 *
 */
public class DaoValueMatcherFactoryTest extends DbDrivenTestCase {
    private DaoFactory daoFactory = config.getInjector().getInstance(DaoFactory.class);

    public void test_instantiation() {
	final IValueMatcherFactory vmFactory = new ValueMatcherFactory(daoFactory, entityFactory);
	System.out.println("SETTING : " + entityFactory);

	final IValueMatcher<?> matcher = vmFactory.getValueMatcher(Wagon.class, "wagonClass");
	assertNotNull("Should have constructed a value matcher.", matcher);
	assertEquals("Incorrect number of matching values.", 2, matcher.findMatches("W%").size());
	assertTrue("New matcher should not have been created.", matcher == vmFactory.getValueMatcher(Wagon.class, "wagonClass"));
    }

    public void test_matcher_usage_with_model() {
	config.getHibernateUtil().getSessionFactory().getCurrentSession().close();
	final IValueMatcherFactory vmFactory = new ValueMatcherFactory(daoFactory, entityFactory);

	final IValueMatcher<WagonClass> matcher = (IValueMatcher<WagonClass>) vmFactory.getValueMatcher(Wagon.class, "wagonClass");
	//matcher.setQueryModel(select(WagonClass.class).with("compatibles", select(WagonClassCompatibility.class).model()));
	matcher.setFetchModel(new fetch(WagonClass.class).with("compatibles", new fetch(WagonClassCompatibility.class)));
	assertNotNull("Should have constructed a value matcher.", matcher);
	List<WagonClass> result = matcher.findMatches("W%");
	assertEquals("Incorrect number of matching values.", 2, result.size());
	assertEquals("Incorrect wagon class.", "WA1", result.get(0).getKey());

	assertEquals("Incorrect compatibles for wagon class.", 0, result.get(0).getCompatibles().size());

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

    public void test_instantiation_error_handling() {
	final IValueMatcherFactory vmFactory = new ValueMatcherFactory(daoFactory, entityFactory);
	try {
	    vmFactory.getValueMatcher(Wagon.class, "serialNo");
	    fail("Matcher creation exception is expected due to incorrect property type -- String.");
	} catch (final Exception e) {
	}

	try {
	    vmFactory.getValueMatcher(Wagon.class, "non-existing-property");
	    fail("Matcher creation exception is expected due to incorrect property name.");
	} catch (final Exception e) {
	}
    }

    public void test_property_descriptor_matcher() {
	final IValueMatcherFactory vmFactory = new ValueMatcherFactory(daoFactory, entityFactory);
	final IValueMatcher<PropertyDescriptor<WorkOrder>> matcher = (IValueMatcher<PropertyDescriptor<WorkOrder>>) vmFactory.getValueMatcher(WoStatusRequiredField.class, "requiredProperty");
	assertNotNull("Value matcher for property descriptor should have been created.", matcher);
	assertEquals("Incorrect number of matches for '*'.", 6, matcher.findMatches("*").size());
	assertEquals("Incorrect number of matches for capital 'W'.", 3, matcher.findMatches("W%").size());
	assertEquals("Incorrect number of matches for lower case 'w'.", 3, matcher.findMatches("w%").size());

    }

    @Override
    protected String[] getDataSetPathsForInsert() {
	return new String[] { "src/test/resources/data-files/dao-value-matcher-test-case.flat.xml" };
    }
}
