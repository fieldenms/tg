package ua.com.fielden.platform.entity.matcher;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetch;

import java.util.List;

import org.junit.Ignore;
import org.junit.Test;

import ua.com.fielden.platform.basic.IValueMatcher;
import ua.com.fielden.platform.basic.IValueMatcherWithFetch;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.entity.meta.PropertyDescriptor;
import ua.com.fielden.platform.sample.domain.TgBogieClass;
import ua.com.fielden.platform.sample.domain.TgWagon;
import ua.com.fielden.platform.sample.domain.TgWagonClass;
import ua.com.fielden.platform.sample.domain.TgWagonClassCompatibility;
import ua.com.fielden.platform.sample.domain.TgWoStatusRequiredField;
import ua.com.fielden.platform.sample.domain.TgWorkOrder;
import ua.com.fielden.platform.test_config.AbstractDaoTestCase;

/**
 * Test the ability of the ValueMatcherFactory to correctly provide value matchers.
 *
 * @author TG Team
 *
 */
@Deprecated
public class DaoValueMatcherFactoryTest extends AbstractDaoTestCase {
    private ICompanionObjectFinder coFinder = getInstance(ICompanionObjectFinder.class);
    private EntityFactory entityFactory = getInstance(EntityFactory.class);

    @Test
    public void test_instantiation() {
        final IValueMatcherFactory vmFactory = new ValueMatcherFactory(coFinder, entityFactory);

        final IValueMatcher<?> matcher = vmFactory.getValueMatcher(TgWagon.class, "wagonClass");
        assertNotNull("Should have constructed a value matcher.", matcher);
        assertEquals("Incorrect number of matching values.", 2, matcher.findMatches("W%").size());
        assertTrue("New matcher should not have been created.", matcher == vmFactory.getValueMatcher(TgWagon.class, "wagonClass"));
    }

    @Test
    @Ignore("Fails due to incorrect fetch model that is used in already reprecated matcher factory.")
    public void test_matcher_usage_with_model() {
        final IValueMatcherFactory vmFactory = new ValueMatcherFactory(coFinder, entityFactory);

        final IValueMatcherWithFetch<TgWagonClass> matcher = (IValueMatcherWithFetch<TgWagonClass>) vmFactory.getValueMatcher(TgWagon.class, "wagonClass");
        //matcher.setQueryModel(select(WagonClass.class).with("compatibles", select(WagonClassCompatibility.class).model()));
        matcher.setFetch(fetch(TgWagonClass.class).with("compatibles", fetch(TgWagonClassCompatibility.class)));
        assertNotNull("Should have constructed a value matcher.", matcher);
        List<TgWagonClass> result = matcher.findMatches("W%");
        assertEquals("Incorrect number of matching values.", 2, result.size());
        assertEquals("Incorrect wagon class.", "WA1", result.get(0).getKey());
        assertEquals("Incorrect compatibles for wagon class.", 0, result.get(0).getCompatibles().size());

        result = matcher.findMatchesWithModel("W%", 1);
        assertEquals("Incorrect number of matching values.", 2, result.size());
        assertEquals("Incorrect wagon class.", "WA1", result.get(0).getKey());
        assertEquals("Incorrect compatibles for wagon class.", 1, result.get(0).getCompatibles().size());
        assertEquals("Incorrect bogie class.", "BO1", result.get(0).getCompatibles().iterator().next().getBogieClass().getKey());
    }

    @Test
    public void test_instantiation_error_handling() {
        final IValueMatcherFactory vmFactory = new ValueMatcherFactory(coFinder, entityFactory);
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
        final IValueMatcherFactory vmFactory = new ValueMatcherFactory(coFinder, entityFactory);
        final IValueMatcher<PropertyDescriptor<TgWorkOrder>> matcher = (IValueMatcher<PropertyDescriptor<TgWorkOrder>>) vmFactory.getValueMatcher(TgWoStatusRequiredField.class, "requiredProperty");
        assertNotNull("Value matcher for property descriptor should have been created.", matcher);
        assertEquals("Incorrect number of matches for '*'.", 17, matcher.findMatches("%").size());
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

}