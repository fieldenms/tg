package ua.com.fielden.platform.entity;

import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import org.junit.Before;
import org.junit.Test;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.entity.validation.DomainValidationConfig;
import ua.com.fielden.platform.entity.validation.HappyValidator;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.ioc.ApplicationInjectorFactory;
import ua.com.fielden.platform.test.CommonTestEntityModuleWithPropertyFactory;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Set;

import static org.junit.Assert.assertTrue;

/**
 * A test case for an entity with byte array as a property. Ensures correct entity-like behaviour.
 *
 * @author TG Team
 *
 */
public class EntityWithByteArrayTest {

    private final Injector injector = new ApplicationInjectorFactory()
            .add(new CommonTestEntityModuleWithPropertyFactory())
            .add(new AbstractModule() {
                @Override
                protected void configure() {
                    bind(DomainValidationConfig.class).toInstance(newDomainValidationConfig());
                }
            })
            .getInjector();
    private final EntityFactory factory = injector.getInstance(EntityFactory.class);
    private EntityWithByteArray entity;

    @Before
    public void setUp() {
        entity = factory.newEntity(EntityWithByteArray.class, "key", "description");
    }

    @Test
    public void test_that_byte_array_property_is_set_and_marked_dirty() {
        entity.setByteArray(new byte[] { 1, 2, 3 });
        assertTrue("Was not set properly.", Arrays.equals(new byte[] { 1, 2, 3 }, entity.getByteArray()));
        assertTrue("Should have been recognised as dirty", entity.getProperty("byteArray").isDirty());
    }

    private static DomainValidationConfig newDomainValidationConfig() {
        final var config = new DomainValidationConfig();
        config.setValidator(EntityWithByteArray.class, "byteArray", new HappyValidator() {
            @Override
            public Result handle(final MetaProperty<Object> property, final Object newValue, final Set<Annotation> mutatorAnnotations) {
                return super.handle(property, newValue, mutatorAnnotations);
            }
        });
        return config;
    }

}
