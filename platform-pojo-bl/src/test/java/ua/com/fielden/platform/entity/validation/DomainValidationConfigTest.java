package ua.com.fielden.platform.entity.validation;

import org.junit.Test;
import ua.com.fielden.platform.entity.validation.test_entities.*;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * A test case for domain validation to ensure that both direct and inherited validators are determined correctly.
 * 
 * @author TG Team
 * 
 */
public class DomainValidationConfigTest {

    @Test
    public void test_registering_validators_for_classes() {
        final var domainValidationConfig = new DomainValidationConfig();

        domainValidationConfig.setValidator(AbstractBaseClass.class, "intProp", new IntPropValidator());
        assertTrue(domainValidationConfig.getValidator(AbstractBaseClass.class, "intProp") instanceof IntPropValidator);
        assertTrue(domainValidationConfig.getValidator(SubClass1.class, "intProp") instanceof IntPropValidator);
        assertTrue(domainValidationConfig.getValidator(SubClass2.class, "intProp") instanceof IntPropValidator);
        assertTrue(domainValidationConfig.getValidator(SubClass2SubClass.class, "intProp") instanceof IntPropValidator);

        domainValidationConfig.setValidator(SubClass2.class, "bigDecimal", new BigDecimalPropValidator());
        assertNull(domainValidationConfig.getValidator(AbstractBaseClass.class, "bigDecimal"));
        assertNull(domainValidationConfig.getValidator(SubClass1.class, "bigDecimal"));
        assertTrue(domainValidationConfig.getValidator(SubClass2.class, "bigDecimal") instanceof BigDecimalPropValidator);
        assertTrue(domainValidationConfig.getValidator(SubClass2SubClass.class, "bigDecimal") instanceof BigDecimalPropValidator);
    }

    @Test
    public void test_registering_validators_for_classes2() {
        final var domainValidationConfig = new DomainValidationConfig();

        domainValidationConfig.setValidator(AbstractBaseClass.class, "intProp", new IntPropValidator());
        domainValidationConfig.setValidator(SubClass1.class, "intProp", new IntPropValidator2());
        domainValidationConfig.setValidator(SubClass1SubClass.class, "intProp", new IntPropValidator3());

        assertTrue(domainValidationConfig.getValidator(AbstractBaseClass.class, "intProp") instanceof IntPropValidator);
        assertTrue(domainValidationConfig.getValidator(SubClass1.class, "intProp") instanceof IntPropValidator2);
        assertTrue(domainValidationConfig.getValidator(SubClass1SubClass.class, "intProp") instanceof IntPropValidator3);
    }

}
