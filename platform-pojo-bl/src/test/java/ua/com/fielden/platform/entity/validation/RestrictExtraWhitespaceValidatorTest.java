package ua.com.fielden.platform.entity.validation;

import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.repeat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static ua.com.fielden.platform.entity.validation.RestrictExtraWhitespaceValidator.ERR_CONTAINS_CONSECUTIVE_WHITESPACE;
import static ua.com.fielden.platform.entity.validation.RestrictExtraWhitespaceValidator.ERR_CONTAINS_CONSECUTIVE_WHITESPACE_VALUE;
import static ua.com.fielden.platform.entity.validation.RestrictExtraWhitespaceValidator.ERR_CONTAINS_LEADING_WHITESPACE;
import static ua.com.fielden.platform.entity.validation.RestrictExtraWhitespaceValidator.ERR_CONTAINS_LEADING_WHITESPACE_VALUE;
import static ua.com.fielden.platform.entity.validation.RestrictExtraWhitespaceValidator.ERR_CONTAINS_TRAILING_WHITESPACE;
import static ua.com.fielden.platform.entity.validation.RestrictExtraWhitespaceValidator.ERR_CONTAINS_TRAILING_WHITESPACE_VALUE;
import static ua.com.fielden.platform.entity.validation.RestrictNonPrintableCharactersValidator.MAX_REPORTABLE_LENGTH;

import java.util.function.BiConsumer;

import org.junit.Test;

import com.google.inject.Injector;

import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.entity.validation.test_entities.EntityWithRestrictExtraWhitespaceValidation;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.ioc.ApplicationInjectorFactory;
import ua.com.fielden.platform.test.CommonTestEntityModuleWithPropertyFactory;
import ua.com.fielden.platform.test.EntityModuleWithPropertyFactory;

/**
 * A test case for validation with {@link RestrictExtraWhitespaceValidator}.
 *
 * @author TG Team
 */
public class RestrictExtraWhitespaceValidatorTest {

    private final EntityModuleWithPropertyFactory module = new CommonTestEntityModuleWithPropertyFactory();
    private final Injector injector = new ApplicationInjectorFactory().add(module).getInjector();
    private final EntityFactory factory = injector.getInstance(EntityFactory.class);

    @Test
    public void string_property_value_cannot_contain_leading_whitespace() {
        final EntityWithRestrictExtraWhitespaceValidation entity = factory.newEntity(EntityWithRestrictExtraWhitespaceValidation.class);
        final MetaProperty<String> mp = entity.getProperty("stringProp");

        final BiConsumer<String, String> assertor = (newValue, msgValue) -> {
            entity.setStringProp(newValue);
            final Result ff = mp.getFirstFailure();
            assertNotNull("Validation should have failed.", ff);
            assertEquals(format(ERR_CONTAINS_LEADING_WHITESPACE_VALUE, msgValue), ff.getMessage());
        };

        assertor.accept(" hello", "{?}hello");
        assertor.accept("  hello", "{?}hello");
        assertor.accept("\thello", "{?}hello");
        assertor.accept("\nhello", "{?}hello");
        assertor.accept("\rhello", "{?}hello");
    }

    @Test
    public void leading_whitespace_validation_kicks_in_first() {
        final EntityWithRestrictExtraWhitespaceValidation entity = factory.newEntity(EntityWithRestrictExtraWhitespaceValidation.class);
        final MetaProperty<String> mp = entity.getProperty("stringProp");

        final BiConsumer<String, String> assertor = (newValue, msgValue) -> {
            entity.setStringProp(newValue);
            final Result ff = mp.getFirstFailure();
            assertNotNull("Validation should have failed.", ff);
            assertEquals(format(ERR_CONTAINS_LEADING_WHITESPACE_VALUE, msgValue), ff.getMessage());
        };

        assertor.accept("  hello   world  ", "{?}hello   world  ");
    }

    @Test
    public void string_property_value_cannot_contain_trailing_whitespace() {
        final EntityWithRestrictExtraWhitespaceValidation entity = factory.newEntity(EntityWithRestrictExtraWhitespaceValidation.class);
        final MetaProperty<String> mp = entity.getProperty("stringProp");

        final BiConsumer<String, String> assertor = (newValue, msgValue) -> {
            entity.setStringProp(newValue);
            final Result ff = mp.getFirstFailure();
            assertNotNull("Validation should have failed.", ff);
            assertEquals(format(ERR_CONTAINS_TRAILING_WHITESPACE_VALUE, msgValue), ff.getMessage());
        };

        assertor.accept("hello ",  "hello{?}");
        assertor.accept("hello  ", "hello{?}");
        assertor.accept("hello\t", "hello{?}");
        assertor.accept("hello\n", "hello{?}");
        assertor.accept("hello\r", "hello{?}");
    }

    @Test
    public void trailing_whitespace_validation_kicks_in_second() {
        final EntityWithRestrictExtraWhitespaceValidation entity = factory.newEntity(EntityWithRestrictExtraWhitespaceValidation.class);
        final MetaProperty<String> mp = entity.getProperty("stringProp");

        final BiConsumer<String, String> assertor = (newValue, msgValue) -> {
            entity.setStringProp(newValue);
            final Result ff = mp.getFirstFailure();
            assertNotNull("Validation should have failed.", ff);
            assertEquals(format(ERR_CONTAINS_TRAILING_WHITESPACE_VALUE, msgValue), ff.getMessage());
        };

        assertor.accept("hello   world  ", "hello   world{?}");
    }

    @Test
    public void string_property_value_cannot_contain_consecutive_whitespace() {
        final EntityWithRestrictExtraWhitespaceValidation entity = factory.newEntity(EntityWithRestrictExtraWhitespaceValidation.class);
        final MetaProperty<String> mp = entity.getProperty("stringProp");

        final BiConsumer<String, String> assertor = (newValue, msgValue) -> {
            entity.setStringProp(newValue);
            final Result ff = mp.getFirstFailure();
            assertNotNull("Validation should have failed.", ff);
            assertEquals(format(ERR_CONTAINS_CONSECUTIVE_WHITESPACE_VALUE, msgValue), ff.getMessage());
        };

        assertor.accept("hello  my     world",  "hello{?}my{?}world");
        assertor.accept("hello\n\nworld",  "hello{?}world");
    }

    @Test
    public void invalid_property_value_longer_than_the_limit_is_not_included_in_the_error_message() {
        final EntityWithRestrictExtraWhitespaceValidation entity = factory.newEntity(EntityWithRestrictExtraWhitespaceValidation.class);
        final MetaProperty<String> mp = entity.getProperty("stringProp");

        final BiConsumer<String, String> assertor = (newValue, message) -> {
            entity.setStringProp(newValue);
            final Result ff = mp.getFirstFailure();
            assertNotNull("Validation should have failed.", ff);
            assertEquals(message, ff.getMessage());
        };

        assertor.accept(" " + repeat("x", MAX_REPORTABLE_LENGTH - 1), ERR_CONTAINS_LEADING_WHITESPACE);
        assertor.accept(repeat("x", MAX_REPORTABLE_LENGTH - 1) + " ", ERR_CONTAINS_TRAILING_WHITESPACE);
        assertor.accept(repeat("x", MAX_REPORTABLE_LENGTH - 1) + "a   b", ERR_CONTAINS_CONSECUTIVE_WHITESPACE);
    }

}