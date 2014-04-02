package ua.com.fielden.platform.entity.validation;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import ua.com.fielden.platform.entity.validation.test_entities.AbstractBaseClass;
import ua.com.fielden.platform.entity.validation.test_entities.DoublePropValidator;
import ua.com.fielden.platform.entity.validation.test_entities.IntPropValidator;
import ua.com.fielden.platform.entity.validation.test_entities.IntPropValidator2;
import ua.com.fielden.platform.entity.validation.test_entities.IntPropValidator3;
import ua.com.fielden.platform.entity.validation.test_entities.SubClass1;
import ua.com.fielden.platform.entity.validation.test_entities.SubClass1SubClass;
import ua.com.fielden.platform.entity.validation.test_entities.SubClass2;
import ua.com.fielden.platform.entity.validation.test_entities.SubClass2SubClass;
import ua.com.fielden.platform.test.CommonTestEntityModuleWithPropertyFactory;
import ua.com.fielden.platform.test.EntityModuleWithPropertyFactory;

/**
 * A test case for domain validation to ensure that both direct and inherited validators are determined correctly.
 * 
 * @author TG Team
 * 
 */
public class DomainValidationConfigTest {

    @Test
    public void test_registering_validators_for_classes() {
        final EntityModuleWithPropertyFactory module = new CommonTestEntityModuleWithPropertyFactory();
        module.getDomainValidationConfig().setValidator(AbstractBaseClass.class, "intProp", new IntPropValidator());
        assertTrue(module.getDomainValidationConfig().getValidator(AbstractBaseClass.class, "intProp") instanceof IntPropValidator);
        assertTrue(module.getDomainValidationConfig().getValidator(SubClass1.class, "intProp") instanceof IntPropValidator);
        assertTrue(module.getDomainValidationConfig().getValidator(SubClass2.class, "intProp") instanceof IntPropValidator);
        assertTrue(module.getDomainValidationConfig().getValidator(SubClass2SubClass.class, "intProp") instanceof IntPropValidator);

        module.getDomainValidationConfig().setValidator(SubClass2.class, "doubleProp", new DoublePropValidator());
        assertNull(module.getDomainValidationConfig().getValidator(AbstractBaseClass.class, "doubleProp"));
        assertNull(module.getDomainValidationConfig().getValidator(SubClass1.class, "doubleProp"));
        assertTrue(module.getDomainValidationConfig().getValidator(SubClass2.class, "doubleProp") instanceof DoublePropValidator);
        assertTrue(module.getDomainValidationConfig().getValidator(SubClass2SubClass.class, "doubleProp") instanceof DoublePropValidator);
    }

    @Test
    public void test_registering_validators_for_classes2() {
        final EntityModuleWithPropertyFactory module = new CommonTestEntityModuleWithPropertyFactory();
        module.getDomainValidationConfig().setValidator(AbstractBaseClass.class, "intProp", new IntPropValidator());
        module.getDomainValidationConfig().setValidator(SubClass1.class, "intProp", new IntPropValidator2());
        module.getDomainValidationConfig().setValidator(SubClass1SubClass.class, "intProp", new IntPropValidator3());

        assertTrue(module.getDomainValidationConfig().getValidator(AbstractBaseClass.class, "intProp") instanceof IntPropValidator);
        assertTrue(module.getDomainValidationConfig().getValidator(SubClass1.class, "intProp") instanceof IntPropValidator2);
        assertTrue(module.getDomainValidationConfig().getValidator(SubClass1SubClass.class, "intProp") instanceof IntPropValidator3);
    }

}
