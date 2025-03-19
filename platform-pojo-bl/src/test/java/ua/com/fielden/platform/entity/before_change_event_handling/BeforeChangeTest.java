package ua.com.fielden.platform.entity.before_change_event_handling;

import com.google.inject.Injector;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.junit.Test;
import ua.com.fielden.platform.entity.annotation.mutator.BeforeChange;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.entity.validation.IBeforeChangeEventHandler;
import ua.com.fielden.platform.entity.validation.SanitiseHtmlValidator;
import ua.com.fielden.platform.entity.validation.annotation.ValidationAnnotation;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.ioc.ApplicationInjectorFactory;
import ua.com.fielden.platform.test.CommonEntityTestIocModuleWithPropertyFactory;
import ua.com.fielden.platform.utils.IDates;
import ua.com.fielden.platform.utils.StringConverter;

import java.util.Map;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;
import static ua.com.fielden.platform.entity.meta.AbstractMetaPropertyFactory.ERR_INVALID_PROPERTY_NAME_FOR_PROP_PARAM;
import static ua.com.fielden.platform.utils.StreamUtils.typeFilter;

/**
 * A test case to ensure correct construction and invocation of {@link BeforeChange} declarations.
 *
 *
 * @author TG Team
 *
 */
public class BeforeChangeTest {
    private final Injector injector = new ApplicationInjectorFactory().add(new CommonEntityTestIocModuleWithPropertyFactory()).getInjector();
    private final EntityFactory factory = injector.getInstance(EntityFactory.class);

    @Test
    public void test_instantiation_of_BCE_handlers_during_entity_instantiation() {
        final Entity entity = factory.newByKey(Entity.class, "key");
        assertNotNull("Should have been created", entity);

        final Map<IBeforeChangeEventHandler<String>, Result> handlers = entity.<String>getProperty("property1").getValidators().get(ValidationAnnotation.BEFORE_CHANGE);
        assertThat(handlers.keySet())
                .hasExactlyElementsOfTypes(SanitiseHtmlValidator.class, BeforeChangeEventHandler.class, InvalidBeforeChangeEventHandler.class);
    }

    @Test
    public void all_BCE_handler_params_are_assigned_as_per_definition() {
        final Entity entity = factory.newByKey(Entity.class, "key");

        final Map<IBeforeChangeEventHandler<String>, Result> handlers = entity.<String>getProperty("property1").getValidators().get(ValidationAnnotation.BEFORE_CHANGE);
        final var handler = handlers.keySet().stream().mapMulti(typeFilter(BeforeChangeEventHandler.class)).findFirst().orElseThrow();
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
        final var bceHandler = handlers.keySet().stream().mapMulti(typeFilter(BeforeChangeEventHandler.class)).findFirst().orElseThrow();
        assertFalse("Should not have been invoked.", bceHandler.isInvoked());
        final InvalidBeforeChangeEventHandler ibceHandler = handlers.keySet().stream().mapMulti(typeFilter(InvalidBeforeChangeEventHandler.class)).findFirst().orElseThrow();
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
        final BeforeChangeEventHandler bceHandler = handlers.keySet().stream().mapMulti(typeFilter(BeforeChangeEventHandler.class)).findFirst().orElseThrow();

        assertTrue("Should have been invoked.", bceHandler.isInvoked());
        assertTrue("Should have been invoked.", bceHandler.getControllerParam().isInvoked());
        final InvalidBeforeChangeEventHandler ibceHandler = handlers.keySet().stream().mapMulti(typeFilter(InvalidBeforeChangeEventHandler.class)).findFirst().orElseThrow();
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
