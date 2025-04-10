package ua.com.fielden.platform.entity.validation;

import static java.lang.String.format;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static ua.com.fielden.platform.entity.validation.RestrictNonPrintableCharactersValidator.ERR_CONTAINS_NON_PRINTABLE;
import static ua.com.fielden.platform.entity.validation.RestrictNonPrintableCharactersValidator.ERR_CONTAINS_NON_PRINTABLE_VALUE;
import static ua.com.fielden.platform.entity.validation.RestrictNonPrintableCharactersValidator.MAX_REPORTABLE_LENGTH;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import com.google.inject.Injector;

import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.entity.validation.test_entities.EntityWithRestrictNonPrintableCharactersValidation;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.ioc.ApplicationInjectorFactory;
import ua.com.fielden.platform.test.CommonEntityTestIocModuleWithPropertyFactory;
import ua.com.fielden.platform.test.EntityTestIocModuleWithPropertyFactory;

/**
 * A test case for validation with {@link RestrictNonPrintableCharactersValidator}.
 *
 * @author TG Team
 */
public class RestrictNonPrintableCharactersValidatorTest {

    private final EntityTestIocModuleWithPropertyFactory module = new CommonEntityTestIocModuleWithPropertyFactory();
    private final Injector injector = new ApplicationInjectorFactory().add(module).getInjector();
    private final EntityFactory factory = injector.getInstance(EntityFactory.class);

    @Test
    public void string_property_value_cannot_contain_non_printable_characters() {
        final EntityWithRestrictNonPrintableCharactersValidation entity = factory.newEntity(EntityWithRestrictNonPrintableCharactersValidation.class);
        final MetaProperty<String> mp = entity.getProperty("stringProp");

        final BiConsumer<String, String> assertor = (newValue, msgValue) -> {
            entity.setStringProp(newValue);
            final Result ff = mp.getFirstFailure();
            assertNotNull("Validation should have failed.", ff);
            assertEquals(format(ERR_CONTAINS_NON_PRINTABLE_VALUE, msgValue), ff.getMessage());
        };

        assertor.accept(format("hello %c", 0), "hello {?}");
        assertor.accept(format("%c hello %c", 0, 1), "{?} hello {?}");
        assertor.accept(format("%c%c hel%clo", 0, 1, 2), "{?}{?} hel{?}lo");
    }

    @Test
    public void string_property_value_cannot_contain_emoji_characters() {
        final EntityWithRestrictNonPrintableCharactersValidation entity = factory.newEntity(EntityWithRestrictNonPrintableCharactersValidation.class);
        final MetaProperty<String> mp = entity.getProperty("stringProp");

        final BiConsumer<String, String> assertor = (newValue, msgValue) -> {
            entity.setStringProp(newValue);
            final Result ff = mp.getFirstFailure();
            assertNotNull("Validation should have failed.", ff);
            assertEquals(format(ERR_CONTAINS_NON_PRINTABLE_VALUE, msgValue), ff.getMessage());
        };

        assertor.accept(format("That's a nice joke 😆", 0), "That's a nice joke {?}{?}"); // Emoji is represented by 2 characters, hence {?}{?} instead of {?}.
    }

    @Test
    public void string_property_value_can_contain_non_ASCII_characters() {
        final EntityWithRestrictNonPrintableCharactersValidation entity = factory.newEntity(EntityWithRestrictNonPrintableCharactersValidation.class);
        final MetaProperty<String> mp = entity.getProperty("stringProp");

        entity.setStringProp("Добрий день, ми з України!");
        assertTrue(mp.isValid());
        entity.setStringProp("你好，我们来自中国");
        assertTrue(mp.isValid());
        entity.setStringProp("Diese Mäuse ist aus Deutschland.");
        assertTrue(mp.isValid());
    }

    @Test
    public void string_property_value_can_contain_regular_space_characters() {
        final EntityWithRestrictNonPrintableCharactersValidation entity = factory.newEntity(EntityWithRestrictNonPrintableCharactersValidation.class);
        final MetaProperty<String> mp = entity.getProperty("stringProp");

        entity.setStringProp("   hello   world    ");
        assertTrue(mp.isValid());
    }

    @Test
    public void string_property_value_cannot_contain_carriage_returns_or_newlines() {
        final EntityWithRestrictNonPrintableCharactersValidation entity = factory.newEntity(EntityWithRestrictNonPrintableCharactersValidation.class);
        final MetaProperty<String> mp = entity.getProperty("stringProp");

        final BiConsumer<String, String> assertor = (newValue, msgValue) -> {
            entity.setStringProp(newValue);
            final Result ff = mp.getFirstFailure();
            assertNotNull("Validation should have failed.", ff);
            assertEquals(format(ERR_CONTAINS_NON_PRINTABLE_VALUE, msgValue), ff.getMessage());
        };

        assertor.accept("hello\nworld", "hello{?}world");
        assertor.accept("hello\rworld", "hello{?}world");
        assertor.accept("hello\r\nworld", "hello{?}{?}world");
        assertor.accept("hello world\n", "hello world{?}");
    }

    @Test
    public void invalid_property_value_longer_than_the_limit_is_not_included_in_the_error_message() {
        final EntityWithRestrictNonPrintableCharactersValidation entity = factory.newEntity(EntityWithRestrictNonPrintableCharactersValidation.class);
        final MetaProperty<String> mp = entity.getProperty("stringProp");

        final Consumer<String> assertor = newValue -> {
            entity.setStringProp(newValue);
            final Result ff = mp.getFirstFailure();
            assertNotNull("Validation should have failed.", ff);
            assertEquals(ERR_CONTAINS_NON_PRINTABLE, ff.getMessage());
        };

        assertor.accept(format("%s%c", StringUtils.repeat("x", MAX_REPORTABLE_LENGTH), 0));
        assertor.accept(format("%s%c", StringUtils.repeat("x", MAX_REPORTABLE_LENGTH - 1), 0));
    }

}