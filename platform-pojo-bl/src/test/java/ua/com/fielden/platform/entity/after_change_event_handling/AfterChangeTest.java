package ua.com.fielden.platform.entity.after_change_event_handling;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

import ua.com.fielden.platform.entity.annotation.mutator.AfterChange;
import ua.com.fielden.platform.entity.before_change_event_handling.Entity;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.meta.IAfterChangeEventHandler;
import ua.com.fielden.platform.ioc.ApplicationInjectorFactory;
import ua.com.fielden.platform.test.CommonTestEntityModuleWithPropertyFactory;
import ua.com.fielden.platform.utils.StringConverter;

import com.google.inject.Injector;

/**
 * A test case to ensure correct construction and invocation of {@link AfterChange} declarations.
 *
 *
 * @author TG Team
 *
 */
public class AfterChangeTest {
    private final Injector injector = new ApplicationInjectorFactory().add(new CommonTestEntityModuleWithPropertyFactory()).getInjector();
    private final EntityFactory factory = injector.getInstance(EntityFactory.class);

    @Test
    public void test_instanciation_of_ACE_handlers_during_entity_instantiation() {
	final Entity entity = factory.newByKey(Entity.class, "key");
	assertNotNull("Should have been created", entity);

	final IAfterChangeEventHandler handler1 = entity.getProperty("property1").getAceHandler();
	assertNotNull("Incorrect number of handlers.", handler1);
	assertEquals("Incorrect handler type", AfterChangeEventHandler.class, handler1.getClass());

	final IAfterChangeEventHandler handler2 = entity.getProperty("property2").getAceHandler();
	assertNotNull("Incorrect number of handlers.", handler2);
	assertEquals("Incorrect handler type", InvalidAfterChangeEventHandler.class, handler2.getClass());
    }

    @Test
    public void test_parameterisation_of_ACE_handler() {
	final Entity entity = factory.newByKey(Entity.class, "key");

	final AfterChangeEventHandler handler = (AfterChangeEventHandler) entity.getProperty("property1").getAceHandler();
	assertNotNull("Controller parameter should not be null.", handler.getControllerParam());
	assertEquals("Incorrect parameter value.", 1, handler.getIntParam1());
	assertEquals("Incorrect parameter value.", 12, handler.getIntParam2());
	assertEquals("Incorrect parameter value.", 0.65, handler.getDblParam(), 0);
	assertEquals("Incorrect parameter value.", StringConverter.toDate("2011-12-01 00:00:00"), handler.getDateParam());
	assertEquals("Incorrect parameter value.", StringConverter.toDateTime("2011-12-01 00:00:00"), handler.getDateTimeParam());
	assertEquals("Incorrect parameter value.", StringConverter.toMoney("12.36"), handler.getMoneyParam());
    }

    @Test
    public void test_ACE_handler_invocation_when_assigning_value_null() {
	final Entity entity = factory.newByKey(Entity.class, "key");
	entity.setProperty1(null);

	final AfterChangeEventHandler handler = (AfterChangeEventHandler) entity.getProperty("property1").getAceHandler();
	assertFalse("Should not have been invoked due to validation failure.", handler.isInvoked());
    }

    @Test
    public void test_ACE_handler_invocation_when_assigning_correct_value() {
	final Entity entity = factory.newByKey(Entity.class, "key");
	entity.setProperty1("valid value");

	final AfterChangeEventHandler handler = (AfterChangeEventHandler) entity.getProperty("property1").getAceHandler();
	assertTrue("Should have been invoked.", handler.isInvoked());
    }

    @Test
    public void test_instantiation_of_entity_with_invalid_ACE_handler_declaration() {
	try {
	    factory.newByKey(EntityWithInvalidAceHandler.class, "key");
	    fail("Instantiation should have failed due to invalid ACE handler declaration.");
	} catch (final Exception e) {
	}
    }
}
