package ua.com.fielden.platform.entity.validation;

import com.google.inject.Injector;
import org.junit.Test;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.ioc.ApplicationInjectorFactory;
import ua.com.fielden.platform.serialisation.jackson.entities.EntityWithRichText;
import ua.com.fielden.platform.test.CommonEntityTestIocModuleWithPropertyFactory;
import ua.com.fielden.platform.types.RichText;
import ua.com.fielden.platform.types.RichTextHtmlSanitisationTest;
import ua.com.fielden.platform.types.RichTextSanitiser;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static ua.com.fielden.platform.entity.validation.UnhappyValidator.ERR_UNHAPPY_VALIDATOR;

/**
 * A test case for validation with {@link DefaultValidatorForValueTypeWithValidation}.
 *
 * @author TG Team
 * @see RichTextHtmlSanitisationTest
 */
public class RichTextValidatorTest {

    private final Injector injector = new ApplicationInjectorFactory()
            .add(new CommonEntityTestIocModuleWithPropertyFactory())
            .getInjector();
    private final EntityFactory factory = injector.getInstance(EntityFactory.class);

    @Test
    public void unsafe_RichText_fails_validation() {
        final var entity = factory.newEntity(EntityWithRichText.class);

        final RichText richText = RichText.fromHtml("<script> alert(1) </script>");
        assertThat(richText).isInstanceOf(RichText.Invalid.class);
        entity.setRichText(richText);
        assertFalse(entity.getProperty("richText").isValid());
        assertThat(entity.getProperty("richText").getFirstFailure()).isEqualTo(richText.isValid());

        entity.setRichText(RichText.fromHtml("safe"));
        assertTrue(entity.getProperty("richText").isValid());

        entity.setRichText(RichText.fromHtml("<script> alert(2) </script>"));
        assertFalse(entity.getProperty("richText").isValid());
        assertTrue(entity.getProperty("richText").getFirstFailure().getMessage().startsWith(RichTextSanitiser.STANDARD_ERROR_PREFIX));
    }

    @Test
    public void safe_RichText_passes_validation() {
        final var entity = factory.newEntity(EntityWithRichText.class);

        entity.setRichText(RichText.fromHtml("<p> hello world </p>"));
        assertTrue(entity.getProperty("richText").isValid());
    }

    @Test
    public void null_value_for_RichText_passes_validation() {
        final var entity = factory.newEntity(EntityWithRichText.class);

        entity.setRichText(RichText.fromHtml("<script> alert(1) </script>"));
        assertFalse(entity.getProperty("richText").isValid());
        assertTrue(entity.getProperty("richText").getFirstFailure().getMessage().startsWith(RichTextSanitiser.STANDARD_ERROR_PREFIX));

        entity.setRichText(null);
        assertTrue(entity.getProperty("richText").isValid());
    }

    @Test
    public void required_RichText_property_setting_invalid_value_reports_validation_error_and_not_requiredness_error() {
        final var entity = factory.newEntity(EntityWithRichText.class);
        entity.getProperty("richText").setRequired(true);

        final var invalidRichText = RichText.fromHtml("<script> alert(1) </script>");
        assertFalse(invalidRichText.isValid().isSuccessful());
        assertTrue(RichText.Invalid.class.isAssignableFrom(invalidRichText.getClass()));
        entity.setRichText(invalidRichText);
        assertFalse(entity.getProperty("richText").isValid());
        assertThat(entity.getProperty("richText").getFirstFailure()).isEqualTo(invalidRichText.isValid());
        assertTrue(entity.getProperty("richText").getFirstFailure().getMessage().startsWith(RichTextSanitiser.STANDARD_ERROR_PREFIX));
    }

    @Test
    public void required_RichText_property_setting_null_or_blank_RichText_reports_requiredness_error() {
        final var entity = factory.newEntity(EntityWithRichText.class);
        entity.getProperty("richText").setRequired(true);

        final var blankRichText = RichText.fromHtml("          ");
        assertTrue(blankRichText.isValid().isSuccessful());
        entity.setRichText(blankRichText);
        assertFalse(entity.getProperty("richText").isValid());
        assertThat(entity.getProperty("richText").getFirstFailure().getMessage()).isEqualTo("Required property [Title] is not specified for entity [Entity With Rich Text].");

        final var richText = RichText.fromHtml("<b>some bold text</b>");
        assertTrue(richText.isValid().isSuccessful());
        entity.setRichText(richText);
        assertTrue(entity.getProperty("richText").isValid());
        assertThat(richText).isEqualTo(entity.getRichText());

        entity.setRichText(null);
        assertFalse(entity.getProperty("richText").isValid());
        assertThat(entity.getProperty("richText").getFirstFailure().getMessage()).isEqualTo("Required property [Title] is not specified for entity [Entity With Rich Text].");
    }

    @Test
    public void default_RichText_validator_happens_before_any_other_BeforeChange_validation() {
        final var entity = factory.newEntity(EntityWithRichText.class);
        final var invalidRichText = RichText.fromHtml("<script> alert(1) </script>");
        entity.setUnhappyRichText(invalidRichText);
        assertFalse(entity.getProperty("unhappyRichText").isValid());
        assertThat(entity.getProperty("unhappyRichText").getFirstFailure()).isEqualTo(invalidRichText.isValid());

        final var richText = RichText.fromHtml("<b>some bold text</b>");
        assertTrue(richText.isValid().isSuccessful());
        entity.setUnhappyRichText(richText);
        assertFalse(entity.getProperty("unhappyRichText").isValid());
        assertThat(entity.getProperty("unhappyRichText").getFirstFailure().getMessage()).isEqualTo(ERR_UNHAPPY_VALIDATOR);
    }
}
