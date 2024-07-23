package ua.com.fielden.platform.entity.before_change_event_handling;

import static java.lang.String.format;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static ua.com.fielden.platform.entity.meta.AbstractMetaPropertyFactory.ERR_INVALID_PROPERTY_NAME_FOR_PROP_PARAM;

import java.util.Iterator;
import java.util.Map;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.junit.Test;

import com.google.inject.Injector;

import ua.com.fielden.platform.entity.annotation.mutator.BeforeChange;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.entity.validation.IBeforeChangeEventHandler;
import ua.com.fielden.platform.entity.validation.annotation.ValidationAnnotation;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.ioc.ApplicationInjectorFactory;
import ua.com.fielden.platform.test.CommonTestEntityModuleWithPropertyFactory;
import ua.com.fielden.platform.utils.IDates;
import ua.com.fielden.platform.utils.StringConverter;

/**
 * A test case to ensure correct construction and invocation of {@link BeforeChange} declarations.
 *
 *
 * @author TG Team
 *
 */
public class BeforeChangeTest {
    private final Injector injector = new ApplicationInjectorFactory().add(new CommonTestEntityModuleWithPropertyFactory()).getInjector();
    private final EntityFactory factory = injector.getInstance(EntityFactory.class);

    @Test
    public void test_instantiation_of_BCE_handlers_during_entity_instantiation() {
        final Entity entity = factory.newByKey(Entity.class, "key");
        assertNotNull("Should have been created", entity);

        final Map<IBeforeChangeEventHandler<String>, Result> handlers = entity.<String>getProperty("property1").getValidators().get(ValidationAnnotation.BEFORE_CHANGE);
        assertEquals("Incorrect number of handlers.", 2, handlers.size());
        final Iterator<IBeforeChangeEventHandler<String>> iter = handlers.keySet().iterator();
        assertEquals("Incorrect order of handlers", BeforeChangeEventHandler.class, iter.next().getClass());
        assertEquals("Incorrect order of handlers", InvalidBeforeChangeEventHandler.class, iter.next().getClass());
    }

    @Test
    public void all_BCE_handler_params_are_assigned_as_per_definition() {
        final Entity entity = factory.newByKey(Entity.class, "key");

        final Map<IBeforeChangeEventHandler<String>, Result> handlers = entity.<String>getProperty("property1").getValidators().get(ValidationAnnotation.BEFORE_CHANGE);
        final Iterator<IBeforeChangeEventHandler<String>> iter = handlers.keySet().iterator();
        final BeforeChangeEventHandler handler = (BeforeChangeEventHandler) iter.next();
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
        assertEquals("Incorrect parameter value.", EnumForParams.ONE, handler.getEnumParam());
    }

    @Test
    public void instantiation_of_entity_with_BCE_that_has_invalid_propParam_fails() {
        try {
            factory.newByKey(EntityWithInvalidBceDefinition.class, "key");
            fail("Instantiation should have failed due to inalid property reference.");
        } catch (final Exception ex) {
            final String stackTrace = ExceptionUtils.getStackTrace(ex);
            assertTrue(stackTrace.contains(format(ERR_INVALID_PROPERTY_NAME_FOR_PROP_PARAM, "property2", EntityWithInvalidBceDefinition.class.getName())));
        }
    }

    @Test
    public void instantiation_of_entity_with_invalid_enum_params_for_one_of_its_BCE_handlers_should_fail() {
        try {
            factory.newByKey(EntityWithInvalidEnumParam.class, "key");
            fail();
        } catch (final Exception ex) {
            assertEquals(ExceptionUtils.getRootCause(ex).getMessage(), "Value [INVALID] is not of type [ua.com.fielden.platform.entity.before_change_event_handling.EnumForParams].");
        }
    }

    @Test
    public void test_BCE_handlers_invocation_when_assigning_value_null() {
        final Entity entity = factory.newByKey(Entity.class, "key");
        entity.setProperty1(null);
        // test validation error
        final MetaProperty<String> mp = entity.getProperty("property1");
        final Result result = mp.getFirstFailure();
        assertNotNull("There should be a validation error.", result);
        assertEquals("Incorrect validation error message.", "Property cannot be null.", result.getMessage());
        // test that other validators have not been even invoked.
        final Map<IBeforeChangeEventHandler<String>, Result> handlers = entity.<String>getProperty("property1").getValidators().get(ValidationAnnotation.BEFORE_CHANGE);
        final Iterator<IBeforeChangeEventHandler<String>> iter = handlers.keySet().iterator();
        final BeforeChangeEventHandler bceHandler = (BeforeChangeEventHandler) iter.next();
        assertFalse("Should not have been invoked.", bceHandler.isInvoked());
        final InvalidBeforeChangeEventHandler ibceHandler = (InvalidBeforeChangeEventHandler) iter.next(); // get the second BCE event handler
        assertFalse("Should not have been invoked.", ibceHandler.isInvoked());
    }

    @Test
    public void test_BCE_handlers_invocation_when_assigning_correct_value() {
        final Entity entity = factory.newByKey(Entity.class, "key");
        entity.setProperty1("valid value");
        // test validation error
        final MetaProperty<String> mp = entity.getProperty("property1");
        final Result result = mp.getFirstFailure();
        assertNull("There should be no validation error.", result);
        // test invocation of validators
        final Map<IBeforeChangeEventHandler<String>, Result> handlers = entity.<String>getProperty("property1").getValidators().get(ValidationAnnotation.BEFORE_CHANGE);
        final Iterator<IBeforeChangeEventHandler<String>> iter = handlers.keySet().iterator();
        final BeforeChangeEventHandler bceHandler = (BeforeChangeEventHandler) iter.next();
        assertTrue("Should have been invoked.", bceHandler.isInvoked());
        assertTrue("Should have been invoked.", bceHandler.getControllerParam().isInvoked());
        final InvalidBeforeChangeEventHandler ibceHandler = (InvalidBeforeChangeEventHandler) iter.next(); // get the second BCE event handler
        assertTrue("Should have been invoked.", ibceHandler.isInvoked());
    }

    @Test
    public void test_instantiation_of_entity_with_invalid_BCE_handler_declaration() {
        try {
            factory.newByKey(EntityWithInvalidBceHandler.class, "key");
            fail("Instantiation should have failed due to invalid BCE handler declaration.");
        } catch (final Exception e) {
        }
    }
}
