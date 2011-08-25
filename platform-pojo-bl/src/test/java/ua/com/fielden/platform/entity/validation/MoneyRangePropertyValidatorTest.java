package ua.com.fielden.platform.entity.validation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.validation.test_entities.EntityWithRangeProperties;
import ua.com.fielden.platform.ioc.ApplicationInjectorFactory;
import ua.com.fielden.platform.test.CommonTestEntityModuleWithPropertyFactory;
import ua.com.fielden.platform.test.EntityModuleWithPropertyFactory;
import ua.com.fielden.platform.types.Money;

import com.google.inject.Injector;

/**
 * A test case for validation of range properties.
 *
 * @author TG Team
 *
 */
public class MoneyRangePropertyValidatorTest {

    private final EntityModuleWithPropertyFactory module = new CommonTestEntityModuleWithPropertyFactory();
    private final Injector injector = new ApplicationInjectorFactory().add(module).getInjector();
    private final EntityFactory factory = injector.getInstance(EntityFactory.class);

    @Test
    public void test_money_range_validation_where_only_from_is_set() {
	final EntityWithRangeProperties entity = factory.newByKey(EntityWithRangeProperties.class, "key");
	entity.setFromMoney(new Money("12"));
	assertTrue("Should be valid", entity.getProperty("fromMoney").isValid());
	assertTrue("Should be valid", entity.getProperty("toMoney").isValid());
	assertEquals("Incorrect value", new Money("12"), entity.getFromMoney());
    }

    @Test
    public void test_money_range_validation_where_only_to_is_set() {
	final EntityWithRangeProperties entity = factory.newByKey(EntityWithRangeProperties.class, "key");
	entity.setToMoney(new Money("12"));
	assertTrue("Should be valid", entity.getProperty("fromMoney").isValid());
	assertFalse("Should not be valid", entity.getProperty("toMoney").isValid());
    }

    @Test
    public void test_money_range_validation_where_both_from_and_to_are_set_in_the_right_order() {
	final EntityWithRangeProperties entity = factory.newByKey(EntityWithRangeProperties.class, "key");
	entity.setFromMoney(new Money("12"));
	entity.setToMoney(new Money("16"));
	assertTrue("Should be valid", entity.getProperty("fromMoney").isValid());
	assertTrue("Should not be valid", entity.getProperty("toMoney").isValid());
	assertEquals("Incorrect value", new Money("12"), entity.getFromMoney());
	assertEquals("Incorrect value", new Money("16"), entity.getToMoney());
    }

    @Test
    public void test_money_range_validation_where_range_is_set_incorrectly_with_error_recovery() {
	final EntityWithRangeProperties entity = factory.newByKey(EntityWithRangeProperties.class, "key");
	entity.setFromMoney(new Money("16"));
	entity.setToMoney(new Money("12"));

	assertTrue("Should be valid", entity.getProperty("fromMoney").isValid());
	assertEquals("Incorrect value", new Money("16"), entity.getFromMoney());
	assertFalse("Should not be valid", entity.getProperty("toMoney").isValid());
	assertNull("Incorrect value", entity.getToMoney());

	entity.setFromMoney(new Money("6"));

	assertTrue("Should be valid", entity.getProperty("fromMoney").isValid());
	assertTrue("Should not be valid", entity.getProperty("toMoney").isValid());

	assertEquals("Incorrect value", new Money("6"), entity.getFromMoney());
	assertEquals("Incorrect value", new Money("12"), entity.getToMoney());
    }

}
