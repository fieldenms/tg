package ua.com.fielden.platform.entity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.entity.validation.HappyValidator;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.ioc.ApplicationInjectorFactory;
import ua.com.fielden.platform.test.CommonTestEntityModuleWithPropertyFactory;
import ua.com.fielden.platform.test.EntityModuleWithPropertyFactory;

import com.google.inject.Injector;

/**
 * A test case for an entity with byte array as a property. Ensures correct entity-like behaviour.
 *
 * @author TG Team
 *
 */
public class EntityWithByteArrayTest {
    private boolean observed = false; // used
    private final EntityModuleWithPropertyFactory module = new CommonTestEntityModuleWithPropertyFactory();
    {
	module.getDomainValidationConfig().setValidator(EntityWithByteArray.class, "byteArray", new HappyValidator() {
	    @Override
	    public Result validate(final MetaProperty property, final Object newValue, final Object oldValue, final Set<Annotation> mutatorAnnotations) {

		return super.validate(property, newValue, oldValue, mutatorAnnotations);
	    }
	});
    }

    private final Injector injector = new ApplicationInjectorFactory().add(module).getInjector();
    private final EntityFactory factory = injector.getInstance(EntityFactory.class);
    private EntityWithByteArray entity;

    @Before
    public void setUp() {
	observed = false;
	entity = factory.newEntity(EntityWithByteArray.class, "key", "description");
    }

    @Test
    public void test_that_byte_array_property_is_observed_set_and_marked_dirty() {
	entity.addPropertyChangeListener("byteArray", new PropertyChangeListener() {
	    @Override
	    public void propertyChange(final PropertyChangeEvent event) {
		observed = true;
	    }
	});
	entity.setByteArray(new byte[] {1, 2, 3});
	assertEquals("Property should have been observed.", true, observed);
	assertTrue("Was not set properly.", Arrays.equals(new byte[] {1, 2, 3}, entity.getByteArray()));
	assertTrue("Should have been recognised as dirty", entity.getProperty("byteArray").isDirty());
    }


}
