package ua.com.fielden.platform.entity.validation;

import static java.lang.String.format;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;

import org.junit.Test;

import com.google.inject.Injector;

import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.entity.validation.test_entities.EntityWithGreaterAndMaxValidations;
import ua.com.fielden.platform.entity.validation.test_entities.EntityWithGreaterOrEqualValidation;
import ua.com.fielden.platform.entity.validation.test_entities.EntityWithMaxValidationWithPropParam;
import ua.com.fielden.platform.ioc.ApplicationInjectorFactory;
import ua.com.fielden.platform.test.CommonEntityTestIocModuleWithPropertyFactory;
import ua.com.fielden.platform.test.EntityTestIocModuleWithPropertyFactory;
import ua.com.fielden.platform.types.Money;

/**
 * A test case for validation of range properties.
 *
 * @author TG Team
 *
 */
public class GreaterEqualAndMaxPropertyValidatorTest {

    private final EntityTestIocModuleWithPropertyFactory module = new CommonEntityTestIocModuleWithPropertyFactory();
    private final Injector injector = new ApplicationInjectorFactory().add(module).getInjector();
    private final EntityFactory factory = injector.getInstance(EntityFactory.class);

    @Test
    public void integer_property_must_be_greater_than_limit() {
        final Integer limit = 0;
        final EntityWithGreaterAndMaxValidations entity = factory.newEntity(EntityWithGreaterAndMaxValidations.class);

        final MetaProperty<Integer> mpIntProp = entity.getProperty("intProp");
        entity.setIntProp(limit + 1);
        assertTrue(mpIntProp.isValid());
        entity.setIntProp(limit - 1);
        assertFalse(mpIntProp.isValid());
        assertEquals(format(GreaterValidator.ERR_VALUE_SHOULD_BE_GREATER_THAN, limit), mpIntProp.getFirstFailure().getMessage());
        entity.setIntProp(limit);
        assertFalse(mpIntProp.isValid());
        assertEquals(format(GreaterValidator.ERR_VALUE_SHOULD_BE_GREATER_THAN, limit), mpIntProp.getFirstFailure().getMessage());
    }

    @Test
    public void integer_property_must_not_exceed_max_limit() {
        final Integer limit = 300;
        final EntityWithGreaterAndMaxValidations entity = factory.newEntity(EntityWithGreaterAndMaxValidations.class);

        final MetaProperty<Integer> mpIntProp = entity.getProperty("intProp");
        entity.setIntProp(limit - 1);
        assertTrue(mpIntProp.isValid());
        entity.setIntProp(limit);
        assertTrue(mpIntProp.isValid());
        entity.setIntProp(limit + 1);
        assertFalse(mpIntProp.isValid());
        assertEquals(format(MaxValueValidator.ERR_VALUE_SHOULD_NOT_EXCEED_MAX, limit), mpIntProp.getFirstFailure().getMessage());
    }

    @Test
    public void decimal_property_must_be_greater_than_limit() {
        final BigDecimal limit = new BigDecimal("0.50");
        final EntityWithGreaterAndMaxValidations entity = factory.newEntity(EntityWithGreaterAndMaxValidations.class);

        final MetaProperty<BigDecimal> mpDecimalProp = entity.getProperty("decimalProp");
        entity.setDecimalProp(limit.add(new BigDecimal("0.01")));
        assertTrue(mpDecimalProp.isValid());
        entity.setDecimalProp(limit.subtract(new BigDecimal("0.01")));
        assertFalse(mpDecimalProp.isValid());
        assertEquals(format(GreaterValidator.ERR_VALUE_SHOULD_BE_GREATER_THAN, limit), mpDecimalProp.getFirstFailure().getMessage());
        entity.setDecimalProp(limit);
        assertFalse(mpDecimalProp.isValid());
        assertEquals(format(GreaterValidator.ERR_VALUE_SHOULD_BE_GREATER_THAN, limit), mpDecimalProp.getFirstFailure().getMessage());
    }

    @Test
    public void decimal_property_must_not_exceed_max_limit() {
        final BigDecimal limit = new BigDecimal("1.50");
        final EntityWithGreaterAndMaxValidations entity = factory.newEntity(EntityWithGreaterAndMaxValidations.class);

        final MetaProperty<BigDecimal> mpDecimalProp = entity.getProperty("decimalProp");
        entity.setDecimalProp(limit.subtract(new BigDecimal("0.01")));
        assertTrue(mpDecimalProp.isValid());
        entity.setDecimalProp(limit);
        assertTrue(mpDecimalProp.isValid());
        entity.setDecimalProp(limit.add(new BigDecimal("0.01")));
        assertFalse(mpDecimalProp.isValid());
        assertEquals(format(MaxValueValidator.ERR_VALUE_SHOULD_NOT_EXCEED_MAX, limit), mpDecimalProp.getFirstFailure().getMessage());
    }

    @Test
    public void money_property_must_be_greater_than_limit() {
        final BigDecimal limit = new BigDecimal("-1.50");
        final EntityWithGreaterAndMaxValidations entity = factory.newEntity(EntityWithGreaterAndMaxValidations.class);

        final MetaProperty<Money> mpMoneyProp = entity.getProperty("moneyProp");
        entity.setMoneyProp(new Money(limit.add(new BigDecimal("0.01"))));
        assertTrue(mpMoneyProp.isValid());
        entity.setMoneyProp(new Money(limit.subtract(new BigDecimal("0.01"))));
        assertFalse(mpMoneyProp.isValid());
        assertEquals(format(GreaterValidator.ERR_VALUE_SHOULD_BE_GREATER_THAN, limit), mpMoneyProp.getFirstFailure().getMessage());
        entity.setMoneyProp(new Money(limit));
        assertFalse(mpMoneyProp.isValid());
        assertEquals(format(GreaterValidator.ERR_VALUE_SHOULD_BE_GREATER_THAN, limit), mpMoneyProp.getFirstFailure().getMessage());
    }

    @Test
    public void money_property_must_not_exceed_max_limit() {
        final BigDecimal limit = new BigDecimal("1.50");
        final EntityWithGreaterAndMaxValidations entity = factory.newEntity(EntityWithGreaterAndMaxValidations.class);

        final MetaProperty<Money> mpMoneyProp = entity.getProperty("moneyProp");
        entity.setMoneyProp(new Money(limit.subtract(new BigDecimal("0.01"))));
        assertTrue(mpMoneyProp.isValid());
        entity.setMoneyProp(new Money(limit));
        assertTrue(mpMoneyProp.isValid());
        entity.setMoneyProp(new Money(limit.add(new BigDecimal("0.01"))));
        assertFalse(mpMoneyProp.isValid());
        assertEquals(format(MaxValueValidator.ERR_VALUE_SHOULD_NOT_EXCEED_MAX, limit), mpMoneyProp.getFirstFailure().getMessage());
    }

    @Test
    public void integer_property_must_be_greater_than_or_equal_to_limit() {
        final Integer limit = 0;
        final EntityWithGreaterOrEqualValidation entity = factory.newEntity(EntityWithGreaterOrEqualValidation.class);

        final MetaProperty<Integer> mpIntProp = entity.getProperty("intProp");
        entity.setIntProp(limit + 1);
        assertTrue(mpIntProp.isValid());
        entity.setIntProp(limit - 1);
        assertFalse(mpIntProp.isValid());
        assertEquals(format(GreaterOrEqualValidator.ERR_VALUE_SHOULD_BE_GREATER_THAN_OR_EQUAL_TO, limit), mpIntProp.getFirstFailure().getMessage());
        entity.setIntProp(limit);
        assertTrue(mpIntProp.isValid());
    }

    @Test
    public void decimal_property_must_be_greater_than_or_equal_to_limit() {
        final BigDecimal limit = new BigDecimal("0.50");
        final EntityWithGreaterOrEqualValidation entity = factory.newEntity(EntityWithGreaterOrEqualValidation.class);

        final MetaProperty<BigDecimal> mpDecimalProp = entity.getProperty("decimalProp");
        entity.setDecimalProp(limit.add(new BigDecimal("0.01")));
        assertTrue(mpDecimalProp.isValid());
        entity.setDecimalProp(limit.subtract(new BigDecimal("0.01")));
        assertFalse(mpDecimalProp.isValid());
        assertEquals(format(GreaterOrEqualValidator.ERR_VALUE_SHOULD_BE_GREATER_THAN_OR_EQUAL_TO, limit), mpDecimalProp.getFirstFailure().getMessage());
        entity.setDecimalProp(limit);
        assertTrue(mpDecimalProp.isValid());
    }

    @Test
    public void money_property_must_be_greater_than_or_equal_to_limit() {
        final BigDecimal limit = new BigDecimal("-1.50");
        final EntityWithGreaterOrEqualValidation entity = factory.newEntity(EntityWithGreaterOrEqualValidation.class);

        final MetaProperty<Money> mpMoneyProp = entity.getProperty("moneyProp");
        entity.setMoneyProp(new Money(limit.add(new BigDecimal("0.01"))));
        assertTrue(mpMoneyProp.isValid());
        entity.setMoneyProp(new Money(limit.subtract(new BigDecimal("0.01"))));
        assertFalse(mpMoneyProp.isValid());
        assertEquals(format(GreaterOrEqualValidator.ERR_VALUE_SHOULD_BE_GREATER_THAN_OR_EQUAL_TO, limit), mpMoneyProp.getFirstFailure().getMessage());
        entity.setMoneyProp(new Money(limit));
        assertTrue(mpMoneyProp.isValid());
    }

    @Test
    public void max_limit_as_property_with_missing_value_fails_validation_with_meaningful_error() {
        final EntityWithMaxValidationWithPropParam entity = factory.newEntity(EntityWithMaxValidationWithPropParam.class);
        assertNull(entity.getMaxLimitProp());

        final MetaProperty<BigDecimal> mpInitProp = entity.getProperty("intProp");
        entity.setIntProp(42);
        assertFalse(mpInitProp.isValid());
        assertEquals(MaxValueValidator.ERR_LIMIT_VALUE_COULD_NOT_BE_DETERMINED, mpInitProp.getFirstFailure().getMessage());
    }

    @Test
    public void value_must_not_exceed_max_limit_as_property() {
        final EntityWithMaxValidationWithPropParam entity = factory.newEntity(EntityWithMaxValidationWithPropParam.class);
        entity.setMaxLimitProp(new BigDecimal("1.50"));
        assertEquals(new BigDecimal("1.50"), entity.getMaxLimitProp());

        final MetaProperty<BigDecimal> mpInitProp = entity.getProperty("intProp");
        entity.setIntProp(2);
        assertFalse(mpInitProp.isValid());
        assertEquals(format(MaxValueValidator.ERR_VALUE_SHOULD_NOT_EXCEED_MAX, entity.getMaxLimitProp()), mpInitProp.getFirstFailure().getMessage());

        entity.setIntProp(1);
        assertTrue(mpInitProp.isValid());
    }

    @Test
    public void adjusting_max_limit_as_property_revalidates_dependent_properties() {
        final EntityWithMaxValidationWithPropParam entity = factory.newEntity(EntityWithMaxValidationWithPropParam.class);
        entity.setMaxLimitProp(new BigDecimal("1.50"));
        assertEquals(new BigDecimal("1.50"), entity.getMaxLimitProp());
        
        final MetaProperty<BigDecimal> mpInitProp = entity.getProperty("intProp");
        entity.setIntProp(2);
        assertFalse(mpInitProp.isValid());
        assertEquals(format(MaxValueValidator.ERR_VALUE_SHOULD_NOT_EXCEED_MAX, entity.getMaxLimitProp()), mpInitProp.getFirstFailure().getMessage());
        
        entity.setMaxLimitProp(new BigDecimal("2.00"));
        assertTrue(mpInitProp.isValid());
    }

    @Test
    public void max_limit_constant_takes_precedence_over_limit_as_property() {
        final EntityWithMaxValidationWithPropParam entity = factory.newEntity(EntityWithMaxValidationWithPropParam.class);
        entity.setMaxLimitProp(new BigDecimal("1.50"));
        assertEquals(new BigDecimal("1.50"), entity.getMaxLimitProp());
        
        final MetaProperty<BigDecimal> mpMoneyProp = entity.getProperty("moneyProp");
        entity.setMoneyProp(Money.of("42.00"));
        assertTrue(mpMoneyProp.isValid());
    }

    @Test
    public void GreaterValidator_supports_custom_error_meesage() {
        final EntityWithGreaterAndMaxValidations entity = factory.newEntity(EntityWithGreaterAndMaxValidations.class);
        entity.setDecimalPropWithGreaterAndGreaterOrEqValidators(new BigDecimal("-1.50"));
        
        final MetaProperty<BigDecimal> mp = entity.getProperty("decimalPropWithGreaterAndGreaterOrEqValidators");
        assertFalse(mp.isValid());
        assertEquals("Custom error message for GreaterValidator: limit is 0.00.", mp.getFirstFailure().getMessage());
    }

    @Test
    public void GreaterOrEqualValidator_supports_custom_error_meesage() {
        final EntityWithGreaterAndMaxValidations entity = factory.newEntity(EntityWithGreaterAndMaxValidations.class);
        entity.setDecimalPropWithGreaterAndGreaterOrEqValidators(new BigDecimal("0.50"));
        
        final MetaProperty<BigDecimal> mp = entity.getProperty("decimalPropWithGreaterAndGreaterOrEqValidators");
        assertFalse(mp.isValid());
        assertEquals("Custom error message for GreaterOrEqualValidator: limit is 1.00.", mp.getFirstFailure().getMessage());
    }

    @Test
    public void MaxValueValidator_supports_custom_error_meesage() {
        final EntityWithGreaterAndMaxValidations entity = factory.newEntity(EntityWithGreaterAndMaxValidations.class);
        entity.setDecimalPropWithGreaterAndGreaterOrEqValidators(new BigDecimal("20"));
        
        final MetaProperty<BigDecimal> mp = entity.getProperty("decimalPropWithGreaterAndGreaterOrEqValidators");
        assertFalse(mp.isValid());
        assertEquals("Custom error message for MaxValueValidator: limit is 10.50.", mp.getFirstFailure().getMessage());
    }

}