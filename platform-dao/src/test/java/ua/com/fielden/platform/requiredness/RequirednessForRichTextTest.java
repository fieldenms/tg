package ua.com.fielden.platform.requiredness;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import ua.com.fielden.platform.entity.validation.annotation.ValidationAnnotation;
import ua.com.fielden.platform.persistence.types.EntityWithRichText;
import ua.com.fielden.platform.test_config.AbstractDaoTestCase;
import ua.com.fielden.platform.types.RichText;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class RequirednessForRichTextTest extends AbstractDaoTestCase {

    @Test
    public void RichText_value_is_invalid_if_property_is_required_and_core_text_is_blank_01() {
        // This test makes the RichText property under test required dynamically
        final var blankRichText = RichText.fromHtml("<p></p>");
        assertTrue(StringUtils.isBlank(blankRichText.coreText()));

        final var entity = new_(EntityWithRichText.class, "1").setText(blankRichText);
        final var mpText = entity.getProperty("text");
        assertFalse(mpText.isRequired());
        assertTrue(mpText.isValid());
        assertTrue(entity.isValid().isSuccessful());

        mpText.setRequired(true);
        assertFalse(entity.isValid().isSuccessful());
        assertFalse(mpText.revalidate(false).isSuccessful());
        assertFalse(mpText.isValid());
        assertFalse(mpText.getValidationResult(ValidationAnnotation.REQUIRED).isSuccessful());
    }

    @Test
    public void RichText_value_is_invalid_if_property_is_required_and_core_text_is_blank_02() {
        // This test makes the RichText property under test required dynamically
        final var blankRichText = RichText.fromHtml(" \n   <br>  \n  \n ");
        assertTrue(StringUtils.isBlank(blankRichText.coreText()));

        final var entity = new_(EntityWithRichText.class, "1").setText(blankRichText);
        final var mpText = entity.getProperty("text");
        assertFalse(mpText.isRequired());
        assertTrue(mpText.isValid());
        assertTrue(entity.isValid().isSuccessful());

        mpText.setRequired(true);
        assertFalse(entity.isValid().isSuccessful());
        assertFalse(mpText.revalidate(false).isSuccessful());
        assertFalse(mpText.isValid());
        assertFalse(mpText.getValidationResult(ValidationAnnotation.REQUIRED).isSuccessful());
    }

}
