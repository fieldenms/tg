package ua.com.fielden.platform.entity.validation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.validation.test_entities.EntityWithRangeProperties;
import ua.com.fielden.platform.ioc.ApplicationInjectorFactory;
import ua.com.fielden.platform.test.CommonEntityTestIocModuleWithPropertyFactory;
import ua.com.fielden.platform.test.EntityTestIocModuleWithPropertyFactory;

import com.google.inject.Injector;

import java.math.BigDecimal;

/**
 * A test case for validation of range properties.
 * 
 * @author TG Team
 * 
 */
public class BigDecimalRangePropertyValidatorTest {

    private final EntityTestIocModuleWithPropertyFactory module = new CommonEntityTestIocModuleWithPropertyFactory();
    private final Injector injector = new ApplicationInjectorFactory().add(module).getInjector();
    private final EntityFactory factory = injector.getInstance(EntityFactory.class);

    @Test
    public void test_double_range_validation_where_only_from_is_set() {
        final EntityWithRangeProperties entity = factory.newByKey(EntityWithRangeProperties.class, "key");
        entity.setFromNumber(new BigDecimal("12.5"));
        assertTrue("Should be valid", entity.getProperty("fromNumber").isValid());
        assertTrue("Should be valid", entity.getProperty("toNumber").isValid());
        assertEquals("Incorrect value", new BigDecimal("12.5"), entity.getFromNumber());
    }

    @Test
    public void test_double_range_validation_where_only_to_is_set() {
        final EntityWithRangeProperties entity = factory.newByKey(EntityWithRangeProperties.class, "key");
        entity.setToNumber(new BigDecimal("12.5"));
        assertTrue("Should be valid", entity.getProperty("fromNumber").isValid());
        assertFalse("Should not be valid", entity.getProperty("toNumber").isValid());
    }

    @Test
    public void test_double_range_validation_where_both_from_and_to_are_set_in_the_right_order() {
        final EntityWithRangeProperties entity = factory.newByKey(EntityWithRangeProperties.class, "key");
        entity.setFromNumber(new BigDecimal("12.5"));
        entity.setToNumber(new BigDecimal("16.5"));
        assertTrue("Should be valid", entity.getProperty("fromNumber").isValid());
        assertTrue("Should be valid", entity.getProperty("toNumber").isValid());
        assertEquals("Incorrect value", new BigDecimal("12.5"), entity.getFromNumber());
        assertEquals("Incorrect value", new BigDecimal("16.5"), entity.getToNumber());
    }

    @Test
    public void test_double_range_validation_where_range_is_set_incorrectly_with_error_recovery() {
        final EntityWithRangeProperties entity = factory.newByKey(EntityWithRangeProperties.class, "key");
        entity.setFromNumber(new BigDecimal("16.5"));
        entity.setToNumber(new BigDecimal("12.5"));

        assertTrue("Should be valid", entity.getProperty("fromNumber").isValid());
        assertEquals("Incorrect value", new BigDecimal("16.5"), entity.getFromNumber());
        assertFalse("Should not be valid", entity.getProperty("toNumber").isValid());
        assertNull("Incorrect value", entity.getToNumber());

        entity.setFromNumber(new BigDecimal("6.5"));

        assertTrue("Should be valid", entity.getProperty("fromNumber").isValid());
        assertTrue("Should not be valid", entity.getProperty("toNumber").isValid());

        assertEquals("Incorrect value", new BigDecimal("6.5"), entity.getFromNumber());
        assertEquals("Incorrect value", new BigDecimal("12.5"), entity.getToNumber());
    }

}
