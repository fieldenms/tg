package ua.com.fielden.platform.entity.validation;

import static java.lang.String.format;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static ua.com.fielden.platform.entity.validation.RestrictCommasValidator.ERR_CONTAINS_COMMAS;
import static ua.com.fielden.platform.entity.validation.RestrictExtraWhitespaceValidator.ERR_CONTAINS_CONSECUTIVE_WHITESPACE_VALUE;
import static ua.com.fielden.platform.entity.validation.RestrictExtraWhitespaceValidator.ERR_CONTAINS_LEADING_WHITESPACE_VALUE;
import static ua.com.fielden.platform.entity.validation.RestrictExtraWhitespaceValidator.ERR_CONTAINS_TRAILING_WHITESPACE_VALUE;
import static ua.com.fielden.platform.entity.validation.RestrictNonPrintableCharactersValidator.ERR_CONTAINS_NON_PRINTABLE_VALUE;
import static ua.com.fielden.platform.entity.validators.RudeValidator.ERR_RUDE_MESSAGE;

import java.util.function.BiConsumer;

import org.junit.Test;

import com.google.inject.Injector;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.entity.validation.test_entities.EntityWithImplicitStringKey;
import ua.com.fielden.platform.entity.validation.test_entities.EntityWithStringKey;
import ua.com.fielden.platform.entity.validation.test_entities.EntityWithStringKeyMember;
import ua.com.fielden.platform.entity.validation.test_entities.EntityWithStringKeyMembersThatSkipDefaultStringValidation;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.ioc.ApplicationInjectorFactory;
import ua.com.fielden.platform.test.CommonTestEntityModuleWithPropertyFactory;
import ua.com.fielden.platform.test.EntityModuleWithPropertyFactory;

/**
 * A test case for validation of {@code String}-typed keys and key-members.
 *
 * @author TG Team
 *
 */
public class StringKeysValidationTest {

    private final EntityModuleWithPropertyFactory module = new CommonTestEntityModuleWithPropertyFactory();
    private final Injector injector = new ApplicationInjectorFactory().add(module).getInjector();
    private final EntityFactory factory = injector.getInstance(EntityFactory.class);

    @Test
    public void String_key_property_implicit_validators_are_applied_before_explicit_BeforeChange_handlers() {
        final EntityWithStringKey entity = factory.newEntity(EntityWithStringKey.class);
        final BiConsumer<String, String> assertor = mkAssertor(entity, "key");
        assertor.accept("  hello", format(ERR_CONTAINS_LEADING_WHITESPACE_VALUE, "{?}hello"));
        assertor.accept("hello world", ERR_RUDE_MESSAGE);
        assertor.accept("hello,world", ERR_CONTAINS_COMMAS);
    }

    @Test
    public void implicit_String_key_property_attacts_default_validation() {
        final EntityWithImplicitStringKey entity = factory.newEntity(EntityWithImplicitStringKey.class);
        final BiConsumer<String, String> assertor = mkAssertor(entity, "key");
        assertor.accept("  hello", format(ERR_CONTAINS_LEADING_WHITESPACE_VALUE, "{?}hello"));
        assertor.accept("hello\n", format(ERR_CONTAINS_NON_PRINTABLE_VALUE, "hello{?}"));
        assertor.accept("hello,world", ERR_CONTAINS_COMMAS);
    }

    @Test
    public void String_key_member_property_implicit_validators_are_applied_before_explicit_BeforeChange_handlers() {
        final EntityWithStringKeyMember entity = factory.newEntity(EntityWithStringKeyMember.class);
        final BiConsumer<String, String> assertor = mkAssertor(entity, "name");
        assertor.accept("  hello", format(ERR_CONTAINS_LEADING_WHITESPACE_VALUE, "{?}hello"));
        assertor.accept("hello world", ERR_RUDE_MESSAGE);
        assertor.accept("hello,world", ERR_CONTAINS_COMMAS);
    }

    @Test
    public void String_key_property_cannot_contain_extra_whitespace() {
        final EntityWithStringKey entity = factory.newEntity(EntityWithStringKey.class);
        final BiConsumer<String, String> assertor = mkAssertor(entity, "key");
        assertor.accept("  hello", format(ERR_CONTAINS_LEADING_WHITESPACE_VALUE, "{?}hello"));
        assertor.accept("hello  ", format(ERR_CONTAINS_TRAILING_WHITESPACE_VALUE, "hello{?}"));
        assertor.accept("hello   world", format(ERR_CONTAINS_CONSECUTIVE_WHITESPACE_VALUE, "hello{?}world"));
    }

    @Test
    public void String_key_member_property_cannot_contain_extra_whitespace() {
        final EntityWithStringKeyMember entity = factory.newEntity(EntityWithStringKeyMember.class);
        final BiConsumer<String, String> assertor = mkAssertor(entity, "name");
        assertor.accept("  hello", format(ERR_CONTAINS_LEADING_WHITESPACE_VALUE, "{?}hello"));
        assertor.accept("hello  ", format(ERR_CONTAINS_TRAILING_WHITESPACE_VALUE, "hello{?}"));
        assertor.accept("hello   world", format(ERR_CONTAINS_CONSECUTIVE_WHITESPACE_VALUE, "hello{?}world"));
    }

    @Test
    public void String_key_property_cannot_contain_non_printable_characters() {
        final EntityWithStringKey entity = factory.newEntity(EntityWithStringKey.class);
        final BiConsumer<String, String> assertor = mkAssertor(entity, "key");
        assertor.accept("hello\n", format(ERR_CONTAINS_NON_PRINTABLE_VALUE, "hello{?}"));
        assertor.accept(format("hello%c", 0), format(ERR_CONTAINS_NON_PRINTABLE_VALUE, "hello{?}"));
        assertor.accept(format("hello%c%cworld%c%c", 0, 0, 1, 1), format(ERR_CONTAINS_NON_PRINTABLE_VALUE, "hello{?}{?}world{?}{?}"));
    }

    @Test
    public void String_key_member_property_cannot_contain_non_printable_characters() {
        final EntityWithStringKeyMember entity = factory.newEntity(EntityWithStringKeyMember.class);
        final BiConsumer<String, String> assertor = mkAssertor(entity, "name");
        assertor.accept("hello\n", format(ERR_CONTAINS_NON_PRINTABLE_VALUE, "hello{?}"));
        assertor.accept(format("hello%c", 0), format(ERR_CONTAINS_NON_PRINTABLE_VALUE, "hello{?}"));
        assertor.accept(format("hello%c%cworld%c%c", 0, 0, 1, 1), format(ERR_CONTAINS_NON_PRINTABLE_VALUE, "hello{?}{?}world{?}{?}"));
    }

    @Test
    public void String_key_property_cannot_contain_commas() {
        final EntityWithStringKey entity = factory.newEntity(EntityWithStringKey.class);
        final BiConsumer<String, String> assertor = mkAssertor(entity, "key");
        assertor.accept("hello,world", ERR_CONTAINS_COMMAS);
        assertor.accept(",hello world", ERR_CONTAINS_COMMAS);
        assertor.accept("hello world,", ERR_CONTAINS_COMMAS);
    }

    @Test
    public void String_key_member_with_fully_skipped_validation_permits_non_printable_chars_consecutive_whitespace_chars_and_commas() {
        final EntityWithStringKeyMembersThatSkipDefaultStringValidation entity = factory.newEntity(EntityWithStringKeyMembersThatSkipDefaultStringValidation.class);
        final MetaProperty<String> mpMember1 = entity.getProperty("member1");

        entity.setMember1(" hello\n world,  and some  ");
        assertTrue(mpMember1.isValid());
    }

    @Test
    public void String_key_member_with_skipped_validation_for_commas_permits_commas_but_not_non_printable_chars_or_consecutive_whitespace_chars() {
        final EntityWithStringKeyMembersThatSkipDefaultStringValidation entity = factory.newEntity(EntityWithStringKeyMembersThatSkipDefaultStringValidation.class);
        final String propName = "member2";
        final BiConsumer<String, String> assertor = mkAssertor(entity, propName);

        assertor.accept("hello\n", format(ERR_CONTAINS_NON_PRINTABLE_VALUE, "hello{?}"));
        assertor.accept("  hello", format(ERR_CONTAINS_LEADING_WHITESPACE_VALUE, "{?}hello"));

        entity.setMember2("hello, world");
        assertTrue(entity.getProperty(propName).isValid());
    }

    @Test
    public void String_key_member_with_skipped_validation_for_commas_and_consecutive_whitespace_chars_restricts_only_non_printable_chars() {
        final EntityWithStringKeyMembersThatSkipDefaultStringValidation entity = factory.newEntity(EntityWithStringKeyMembersThatSkipDefaultStringValidation.class);
        final String propName = "member3";
        final BiConsumer<String, String> assertor = mkAssertor(entity, propName);

        assertor.accept("hello\n", format(ERR_CONTAINS_NON_PRINTABLE_VALUE, "hello{?}"));

        entity.setMember3("  hello,  world  ");
        assertTrue(entity.getProperty(propName).isValid());
    }

    @Test
    public void String_key_member_with_skipped_validation_for_non_printable_chars_restricts_whitespace_chars_and_commas() {
        final EntityWithStringKeyMembersThatSkipDefaultStringValidation entity = factory.newEntity(EntityWithStringKeyMembersThatSkipDefaultStringValidation.class);
        final String propName = "member4";
        final BiConsumer<String, String> assertor = mkAssertor(entity, propName);

        assertor.accept("  hello", format(ERR_CONTAINS_LEADING_WHITESPACE_VALUE, "{?}hello"));
        assertor.accept("hello,world", ERR_CONTAINS_COMMAS);

        entity.setMember4("hello\\n");
        assertTrue(entity.getProperty(propName).isValid());
    }

    /**
     * A helper factory method to create an assertor for an entity and its property.
     *
     * @param entity
     * @param propName
     * @return
     */
    private static BiConsumer<String, String> mkAssertor(final AbstractEntity<?> entity, final String propName) {
        return (newValue, errorMsg) -> {
            entity.set(propName, newValue);
            final Result ff = entity.getProperty(propName).getFirstFailure();
            assertNotNull("Validation should have failed.", ff);
            assertEquals(errorMsg, ff.getMessage());
        };
    }

}