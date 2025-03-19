package ua.com.fielden.platform.entity.validation;

import com.google.inject.Injector;
import org.junit.Test;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.entity.validation.annotation.ValidationAnnotation;
import ua.com.fielden.platform.entity.validation.test_entities.EntityWithMaxLengthValidation;
import ua.com.fielden.platform.ioc.ApplicationInjectorFactory;
import ua.com.fielden.platform.test.CommonEntityTestIocModuleWithPropertyFactory;
import ua.com.fielden.platform.test.EntityTestIocModuleWithPropertyFactory;
import ua.com.fielden.platform.types.Hyperlink;
import ua.com.fielden.platform.types.RichText;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.*;

/**
 * A test case for validation with {@link MaxLengthValidator}.
 *
 * @author TG Team
 *
 */
public class MaxLengthValidatorTest {

    private final EntityTestIocModuleWithPropertyFactory module = new CommonEntityTestIocModuleWithPropertyFactory();
    private final Injector injector = new ApplicationInjectorFactory().add(module).getInjector();
    private final EntityFactory factory = injector.getInstance(EntityFactory.class);

    /**
     * This test covers a situation where {@code limit} is specified and {@code length} is not.
     */
    @Test
    public void string_property_value_cannot_be_longer_than_its_limit() {
        final Integer limit = 5;
        final EntityWithMaxLengthValidation entity = factory.newEntity(EntityWithMaxLengthValidation.class);

        final MetaProperty<String> mp = entity.getProperty("propWithLimit");
        entity.setPropWithLimit("1");
        assertTrue(mp.isValid());

        entity.setPropWithLimit("12345");
        assertTrue(mp.isValid());

        entity.setPropWithLimit("123456");
        assertFalse(mp.isValid());

        assertEquals(format(MaxLengthValidator.ERR_VALUE_SHOULD_NOT_EXCEED_MAX_LENGTH, limit), mp.getFirstFailure().getMessage());
    }

    /**
     * This test covers a situation where {@code limit} is not specified and {@code length} is specified.
     */
    @Test
    public void string_property_value_cannot_be_longer_than_its_length() {
        final Integer length = 3;
        final EntityWithMaxLengthValidation entity = factory.newEntity(EntityWithMaxLengthValidation.class);

        final MetaProperty<String> mp = entity.getProperty("propWithLength");
        entity.setPropWithLength("1");
        assertTrue(mp.isValid());

        entity.setPropWithLength("123");
        assertTrue(mp.isValid());

        entity.setPropWithLength("1234");
        assertFalse(mp.isValid());

        assertEquals(format(MaxLengthValidator.ERR_VALUE_SHOULD_NOT_EXCEED_MAX_LENGTH, length), mp.getFirstFailure().getMessage());
    }

    /**
     * This test covers a situation where both {@code limit} and {@code length} are specified, but {@code limit} is greater.
     * This case demonstrates the preference for smaller of the two values.
     */
    @Test
    public void string_property_value_cannot_be_longer_than_its_smaller_length_even_if_limit_is_greater() {
        final Integer length = 3; // limit == 5
        final EntityWithMaxLengthValidation entity = factory.newEntity(EntityWithMaxLengthValidation.class);

        final MetaProperty<String> mp = entity.getProperty("propWithSmallerLengthAndGreaterLimit");
        entity.setPropWithSmallerLengthAndGreaterLimit("1");
        assertTrue(mp.isValid());

        entity.setPropWithSmallerLengthAndGreaterLimit("123");
        assertTrue(mp.isValid());

        entity.setPropWithSmallerLengthAndGreaterLimit("1234");
        assertFalse(mp.isValid());

        assertEquals(format(MaxLengthValidator.ERR_VALUE_SHOULD_NOT_EXCEED_MAX_LENGTH, length), mp.getFirstFailure().getMessage());
    }

    /**
     * This test covers a situation where both {@code limit} and {@code length} are specified, but {@code limit} is smaller.
     * This case demonstrates the preference for smaller of the two values.
     */
    @Test
    public void string_property_value_cannot_be_longer_than_its_smaller_limite_even_if_length_is_greater() {
        final Integer limit = 3; // length == 5
        final EntityWithMaxLengthValidation entity = factory.newEntity(EntityWithMaxLengthValidation.class);

        final MetaProperty<String> mp = entity.getProperty("propWithGreaterLengthAndSmallerLimit");
        entity.setPropWithGreaterLengthAndSmallerLimit("1");
        assertTrue(mp.isValid());

        entity.setPropWithGreaterLengthAndSmallerLimit("123");
        assertTrue(mp.isValid());

        entity.setPropWithGreaterLengthAndSmallerLimit("1234");
        assertFalse(mp.isValid());

        assertEquals(format(MaxLengthValidator.ERR_VALUE_SHOULD_NOT_EXCEED_MAX_LENGTH, limit), mp.getFirstFailure().getMessage());
    }

    /**
     * This test covers an invalid situation where both {@code limit} and {@code length} are missing.
     */
    @Test
    public void string_property_value_cannot_be_set_to_a_non_empty_value_if_neither_limit_nor_length_are_specified() {
        final EntityWithMaxLengthValidation entity = factory.newEntity(EntityWithMaxLengthValidation.class);

        final MetaProperty<String> mp = entity.getProperty("propWithInvalidDeclaration");
        entity.setPropWithInvalidDeclaration("1");
        assertFalse(mp.isValid());

        assertEquals(MaxLengthValidator.ERR_MISSING_MAX_LENGTH, mp.getFirstFailure().getMessage());
    }

    @Test
    public void string_property_accepts_empty_values_regardless_of_max_length_validator_parameters_except_invalid_definitions() {
        final EntityWithMaxLengthValidation entity = factory.newEntity(EntityWithMaxLengthValidation.class);

        final MetaProperty<String> mpPropWithLimit = entity.getProperty("propWithLimit");
        entity.setPropWithLimit("");
        assertTrue(mpPropWithLimit.isValid());

        final MetaProperty<String> mpPropWithLength = entity.getProperty("propWithLength");
        entity.setPropWithLength("");
        assertTrue(mpPropWithLength.isValid());

        final MetaProperty<String> mpPropWithGreaterLengthAndSmallerLimit = entity.getProperty("propWithGreaterLengthAndSmallerLimit");
        entity.setPropWithGreaterLengthAndSmallerLimit("");
        assertTrue(mpPropWithGreaterLengthAndSmallerLimit.isValid());

        final MetaProperty<String> mpPropWithSmallerLengthAndGreaterLimit = entity.getProperty("propWithSmallerLengthAndGreaterLimit");
        entity.setPropWithSmallerLengthAndGreaterLimit("");
        assertTrue(mpPropWithSmallerLengthAndGreaterLimit.isValid());

        final MetaProperty<String> mpPropWithInvalidDeclaration = entity.getProperty("propWithInvalidDeclaration");
        entity.setPropWithInvalidDeclaration("");
        assertFalse(mpPropWithInvalidDeclaration.isValid());
        assertEquals(MaxLengthValidator.ERR_MISSING_MAX_LENGTH, mpPropWithInvalidDeclaration.getFirstFailure().getMessage());
    }

    @Test
    public void string_property_accepts_nulls_regardless_of_max_length_validator_parameters() {
        final EntityWithMaxLengthValidation entity = factory.newEntity(EntityWithMaxLengthValidation.class);

        final MetaProperty<String> mpPropWithLimit = entity.getProperty("propWithLimit");
        entity.setPropWithLimit("");
        entity.setPropWithLimit(null);
        assertTrue(mpPropWithLimit.isValid());

        final MetaProperty<String> mpPropWithLength = entity.getProperty("propWithLength");
        entity.setPropWithLength("");
        entity.setPropWithLength(null);
        assertTrue(mpPropWithLength.isValid());

        final MetaProperty<String> mpPropWithGreaterLengthAndSmallerLimit = entity.getProperty("propWithGreaterLengthAndSmallerLimit");
        entity.setPropWithGreaterLengthAndSmallerLimit("");
        entity.setPropWithGreaterLengthAndSmallerLimit(null);
        assertTrue(mpPropWithGreaterLengthAndSmallerLimit.isValid());

        final MetaProperty<String> mpPropWithSmallerLengthAndGreaterLimit = entity.getProperty("propWithSmallerLengthAndGreaterLimit");
        entity.setPropWithSmallerLengthAndGreaterLimit("");
        entity.setPropWithSmallerLengthAndGreaterLimit(null);
        assertTrue(mpPropWithSmallerLengthAndGreaterLimit.isValid());

        final MetaProperty<String> mpPropHyperlink = entity.getProperty("propHyperlink");
        entity.setPropHyperlink(new Hyperlink("https://www.google.com"));
        entity.setPropHyperlink(null);
        assertTrue(mpPropHyperlink.isValid());

        final MetaProperty<String> mpPropWithInvalidDeclaration = entity.getProperty("propWithInvalidDeclaration");
        entity.setPropWithInvalidDeclaration("");
        entity.setPropWithInvalidDeclaration(null);
        assertTrue(mpPropWithInvalidDeclaration.isValid());
    }

    @Test
    public void hyperlink_properties_can_have_their_value_validated_for_length() {
        final EntityWithMaxLengthValidation entity = factory.newEntity(EntityWithMaxLengthValidation.class);
        final MetaProperty<String> mpPropHyperlink = entity.getProperty("propHyperlink");
        final String tooLongUri = "https://www.gooooooooooooooooooooooooooooooooooooooogle.com";
        assertTrue(tooLongUri.length() > 30);
        entity.setPropHyperlink(new Hyperlink(tooLongUri));
        assertFalse(mpPropHyperlink.isValid());
        assertEquals(format(MaxLengthValidator.ERR_VALUE_SHOULD_NOT_EXCEED_MAX_LENGTH, 30), mpPropHyperlink.getFirstFailure().getMessage());

        final String uri = "https://www.google.com";
        entity.setPropHyperlink(new Hyperlink(uri));
        assertTrue(mpPropHyperlink.isValid());
    }

    @Test
    public void RichText_searchText_cannot_be_longer_than_limit() {
        final int maxLength = 27;
        final var entity = factory.newEntity(EntityWithMaxLengthValidation.class);
        final var mpRichText = entity.getProperty("richText");

        // Try to assign RichText where searchText's length is equal to the limit.
        final var richText = RichText.fromHtml("<a href=\"https://www.domain.com\">link</a>");
        assertEquals(maxLength, RichText.makeSearchText(richText).length());
        entity.setRichText(richText);
        assertTrue(mpRichText.isValid());
        assertTrue(entity.getRichText().coreText().length() > maxLength);

        // Try to assign RichText that is longer than the limit.
        final var longRichText = RichText.fromHtml("<a href=\"https://www.domain.com\">link+</a>");
        assertEquals(maxLength + 1, RichText.makeSearchText(longRichText).length());
        entity.setRichText(longRichText);
        assertFalse(mpRichText.isValid());
        assertEquals(MaxLengthValidator.ERR_VALUE_SHOULD_NOT_EXCEED_MAX_LENGTH.formatted(maxLength), mpRichText.getFirstFailure().getMessage());
    }

    @Test
    public void RichText_properties_attain_MaxLengthValidator_by_default() {
        final int maxLength = 27;
        final var entity = factory.newEntity(EntityWithMaxLengthValidation.class);
        final MetaProperty<RichText> mp = entity.getProperty("richTextPropWithoutExplicitMaxLengthValidator");

        // Assert the presence of default validators and their order.
        final var validators = mp.getValidators().get(ValidationAnnotation.BEFORE_CHANGE).keySet().stream().toList();
        assertThat(validators).hasSize(2);
        assertThat(validators.getFirst()).isInstanceOf(DefaultValidatorForValueTypeWithValidation.class);
        assertThat(validators.getLast()).isInstanceOf(MaxLengthValidator.class);

        // Try to assign RichText where searchText's length is equal to the limit.
        final var richText = RichText.fromHtml("<a href=\"https://www.domain.com\">link</a>");
        assertEquals(maxLength, RichText.makeSearchText(richText).length());
        mp.setValue(richText);
        assertTrue(mp.isValid());
        assertTrue(mp.getValue().coreText().length() > maxLength);

        // Try to assign RichText that is longer than the limit.
        final RichText longRichText = RichText.fromHtml("<a href=\"https://www.domain.com\">link+</a>");
        assertEquals(maxLength + 1, RichText.makeSearchText(longRichText).length());
        mp.setValue(longRichText);
        assertFalse(mp.isValid());
        assertEquals(MaxLengthValidator.ERR_VALUE_SHOULD_NOT_EXCEED_MAX_LENGTH.formatted(maxLength), mp.getFirstFailure().getMessage());
    }

    @Test
    public void string_properties_attain_MaxLengthValidator_by_default() {
        final int maxLength = 52;
        final var entity = factory.newEntity(EntityWithMaxLengthValidation.class);
        final MetaProperty<String> mp = entity.getProperty("stringPropWithoutExplicitMaxLengthValidator");

        // Assert the presence of default validators and their order.
        final var validators = mp.getValidators().get(ValidationAnnotation.BEFORE_CHANGE).keySet().stream().toList();
        assertThat(validators).hasSize(2);
        assertThat(validators.getFirst()).isInstanceOf(MaxLengthValidator.class);
        assertThat(validators.getLast()).isInstanceOf(SanitiseHtmlValidator.class);

        // Try to assign text with length equal to the limit.
        final var text = "a".repeat(maxLength);
        assertEquals(maxLength, text.length());
        entity.setStringPropWithoutExplicitMaxLengthValidator(text);
        assertTrue(mp.isValid());

        // Try to assign text that is longer than the limit.
        final var longText = "a".repeat(maxLength + 1);
        assertEquals(maxLength + 1, longText.length());
        entity.setStringPropWithoutExplicitMaxLengthValidator(longText);
        assertFalse(mp.isValid());
        assertEquals(MaxLengthValidator.ERR_VALUE_SHOULD_NOT_EXCEED_MAX_LENGTH.formatted(maxLength), mp.getFirstFailure().getMessage());
    }

    @Test
    public void hyperlink_properties_attain_MaxLengthValidator_by_default() {
        final int maxLength = 27;
        final var entity = factory.newEntity(EntityWithMaxLengthValidation.class);
        final MetaProperty<Hyperlink> mp = entity.getProperty("hyperlinkPropWithoutExplicitMaxLengthValidator");

        // Assert the presence of default validators and their order.
        final var validators = mp.getValidators().get(ValidationAnnotation.BEFORE_CHANGE).keySet().stream().toList();
        assertThat(validators).hasSize(1);
        assertThat(validators.getFirst()).isInstanceOf(MaxLengthValidator.class);

        // Try to assign hyperlink with length equal to the limit.
        final var hyperlink = new Hyperlink("https://www.domain.com/res1");
        assertEquals(maxLength, hyperlink.value.length());
        entity.setHyperlinkPropWithoutExplicitMaxLengthValidator(hyperlink);
        assertTrue(mp.isValid());

        // Try to assign hyperlink that is longer than the limit.
        final var longHyperlink = new Hyperlink("https://www.domain.com/res21");
        assertEquals(maxLength + 1, longHyperlink.value.length());
        entity.setHyperlinkPropWithoutExplicitMaxLengthValidator(longHyperlink);
        assertFalse(mp.isValid());
        assertEquals(MaxLengthValidator.ERR_VALUE_SHOULD_NOT_EXCEED_MAX_LENGTH.formatted(maxLength), mp.getFirstFailure().getMessage());
    }

    @Test
    public void error_is_thrown_if_MaxLengthValidator_is_used_for_property_of_unsupported_type() {
        final var entity = factory.newEntity(EntityWithMaxLengthValidation.class);
        assertThatThrownBy(() -> entity.setIntProp(42)).hasMessage("Validator [MaxLengthValidator] is not applicable to properties of type [Integer].");
    }

}
