package ua.com.fielden.platform.entity.validation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static ua.com.fielden.platform.entity.validation.RestrictCommasValidator.ERR_CONTAINS_COMMAS;

import java.util.function.Consumer;

import org.junit.Test;

import com.google.inject.Injector;

import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.entity.validation.test_entities.EntityWithRestrictCommasValidation;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.ioc.ApplicationInjectorFactory;
import ua.com.fielden.platform.test.CommonTestEntityModuleWithPropertyFactory;
import ua.com.fielden.platform.test.EntityModuleWithPropertyFactory;

/**
 * A test case for validation with {@link RestrictCommasValidator}.
 *
 * @author TG Team
 */
public class RestrictCommasValidatorTest {

    private final EntityModuleWithPropertyFactory module = new CommonTestEntityModuleWithPropertyFactory();
    private final Injector injector = new ApplicationInjectorFactory().add(module).getInjector();
    private final EntityFactory factory = injector.getInstance(EntityFactory.class);

    @Test
    public void string_property_value_cannot_contain_commas() {
        final EntityWithRestrictCommasValidation entity = factory.newEntity(EntityWithRestrictCommasValidation.class);
        final MetaProperty<String> mp = entity.getProperty("stringProp");

        final Consumer<String> assertor = newValue -> {
            entity.setStringProp(newValue);
            final Result ff = mp.getFirstFailure();
            assertNotNull("Validation should have failed.", ff);
            assertEquals(ERR_CONTAINS_COMMAS, ff.getMessage());
        };

        assertor.accept(",");
        assertor.accept("commas,are");
        assertor.accept("commas,are,scary,,");
    }

}