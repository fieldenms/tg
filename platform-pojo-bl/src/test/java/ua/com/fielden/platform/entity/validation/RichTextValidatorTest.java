package ua.com.fielden.platform.entity.validation;

import com.google.inject.Injector;
import org.junit.Test;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.ioc.ApplicationInjectorFactory;
import ua.com.fielden.platform.serialisation.jackson.entities.EntityWithRichText;
import ua.com.fielden.platform.test.CommonEntityTestIocModuleWithPropertyFactory;
import ua.com.fielden.platform.types.RichText;
import ua.com.fielden.platform.types.RichTextHtmlSanitisationTest;

import static org.junit.Assert.*;

/**
 * A test case for validation with {@link RichTextValidator}.
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

        entity.setRichText(RichText.fromHtml("<script> alert(1) </script>"));
        assertFalse(entity.getProperty("richText").isValid());
        assertTrue(entity.getProperty("richText").getFirstFailure().getMessage().startsWith(RichTextValidator.ERR_PREFIX_UNSAFE_INPUT));

        entity.setRichText(RichText.fromHtml("safe"));
        assertTrue(entity.getProperty("richText").isValid());

        entity.setRichText(RichText.fromHtml("<script> alert(2) </script>"));
        assertFalse(entity.getProperty("richText").isValid());
        assertTrue(entity.getProperty("richText").getFirstFailure().getMessage().startsWith(RichTextValidator.ERR_PREFIX_UNSAFE_INPUT));
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
        assertTrue(entity.getProperty("richText").getFirstFailure().getMessage().startsWith(RichTextValidator.ERR_PREFIX_UNSAFE_INPUT));

        entity.setRichText(null);
        assertTrue(entity.getProperty("richText").isValid());
    }

}
