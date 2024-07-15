package ua.com.fielden.platform.entity.after_change_event_handling;

import static java.lang.String.format;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static ua.com.fielden.platform.entity.meta.AbstractMetaPropertyFactory.ERR_INVALID_PROPERTY_NAME_FOR_PROP_PARAM;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.junit.Test;

import com.google.inject.Injector;

import ua.com.fielden.platform.entity.annotation.mutator.AfterChange;
import ua.com.fielden.platform.entity.before_change_event_handling.Entity;
import ua.com.fielden.platform.entity.before_change_event_handling.EntityWithInvalidAceDefinition;
import ua.com.fielden.platform.entity.before_change_event_handling.EnumForParams;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.meta.IAfterChangeEventHandler;
import ua.com.fielden.platform.ioc.ApplicationInjectorFactory;
import ua.com.fielden.platform.test.CommonTestEntityModuleWithPropertyFactory;
import ua.com.fielden.platform.utils.IDates;
import ua.com.fielden.platform.utils.StringConverter;

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

        final IAfterChangeEventHandler<String> handler1 = entity.<String>getProperty("property1").getAceHandler();
        assertNotNull("Incorrect number of handlers.", handler1);
        assertEquals("Incorrect handler type", AfterChangeEventHandler.class, handler1.getClass());

        final IAfterChangeEventHandler<String> handler2 = entity.<String>getProperty("property2").getAceHandler();
        assertNotNull("Incorrect number of handlers.", handler2);
        assertEquals("Incorrect handler type", InvalidAfterChangeEventHandler.class, handler2.getClass());
    }

    @Test
    public void all_ACE_handler_params_are_assigned_as_per_definition() {
        final Entity entity = factory.newByKey(Entity.class, "key");

        final AfterChangeEventHandler handler = (AfterChangeEventHandler) entity.getProperty("property1").getAceHandler();
        assertNotNull("Controller parameter should not be null.", handler.getControllerParam());
        assertEquals("Incorrect parameter value.", 1, handler.getIntParam1());
        assertEquals("Incorrect parameter value.", 12, handler.getIntParam2());
        assertEquals("Incorrect parameter value.", 0.65, handler.getDblParam(), 0);
        assertEquals("Incorrect parameter value.", "property2", handler.getPropNameParam());
        final IDates dates = injector.getInstance(IDates.class);
        assertEquals("Incorrect parameter value.", StringConverter.toDate("2011-12-01 00:00:00", dates), handler.getDateParam());
        assertEquals("Incorrect parameter value.", StringConverter.toDateTime("2011-12-01 00:00:00", dates), handler.getDateTimeParam());
        assertEquals("Incorrect parameter value.", StringConverter.toMoney("12.36"), handler.getMoneyParam());
        assertEquals("Incorrect parameter value.", String.class, handler.getClassParam());
        assertEquals("Incorrect parameter value.", EnumForParams.TWO, handler.getEnumParam());
    }

    @Test
    public void instantiation_of_entity_with_ACE_that_has_invalid_propParam_fails() {
        try {
            factory.newByKey(EntityWithInvalidAceDefinition.class, "key");
            fail("Instantiation should have failed due to inalid property reference.");
        } catch (final Exception ex) {
            final String stackTrace = ExceptionUtils.getStackTrace(ex);
            assertTrue(stackTrace.contains(format(ERR_INVALID_PROPERTY_NAME_FOR_PROP_PARAM, "property2", EntityWithInvalidAceDefinition.class.getName())));
        }
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
