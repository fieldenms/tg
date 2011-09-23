package ua.com.fielden.platform.entity.before_change_event_handling;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Iterator;
import java.util.Map;

import org.junit.Test;

import ua.com.fielden.platform.entity.annotation.mutator.BeforeChange;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.validation.IBeforeChangeEventHandler;
import ua.com.fielden.platform.entity.validation.NotNullValidator;
import ua.com.fielden.platform.entity.validation.annotation.ValidationAnnotation;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.ioc.ApplicationInjectorFactory;
import ua.com.fielden.platform.test.CommonTestEntityModuleWithPropertyFactory;

import com.google.inject.Injector;

/**
 * A test case to ensure correct construction and invocation of {@link BeforeChange} declarations.
 *
 * @author TG Team
 *
 */
public class BeforeChangeTest {
    private final Injector injector = new ApplicationInjectorFactory().add(new CommonTestEntityModuleWithPropertyFactory()).getInjector();
    private final EntityFactory factory = injector.getInstance(EntityFactory.class);

    @Test
    public void test_correct_processing_of_BeforeChange_annotation_during_entity_instantiation() {
	final Entity entity = factory.newByKey(Entity.class, "key");
	assertNotNull("Should have been created", entity);

	final Map<IBeforeChangeEventHandler, Result> dfd = entity.getProperty("property").getValidators().get(ValidationAnnotation.BEFORE_CHANGE);
	assertEquals("Incorrect number of handlers.", 2, dfd.size());
	final Iterator<IBeforeChangeEventHandler> iter = dfd.keySet().iterator();
	assertEquals("Incorrect order of handlers", BeforeChangeEventHandler.class, iter.next().getClass());
	assertEquals("Incorrect order of handlers", NotNullValidator.class, iter.next().getClass());
    }
}
