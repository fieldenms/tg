package ua.com.fielden.platform.entity.query.fetching;

import org.junit.Test;
import ua.com.fielden.platform.persistence.types.EntityWithRichText;
import ua.com.fielden.platform.test_config.AbstractDaoTestCase;
import ua.com.fielden.platform.types.RichText;

import static java.lang.String.join;
import static org.junit.Assert.assertTrue;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;
import static ua.com.fielden.platform.types.RichText._coreText;
import static ua.com.fielden.platform.types.RichText._formattedText;

public class EqlRichTextTest extends AbstractDaoTestCase {

    @Test
    public void RichText_property_is_interpreted_as_its_core_text() {
        final var richText = RichText.fromMarkdown("`cons` **does not** evaluate its arguments in a *lazy* language.");
        save(new_(EntityWithRichText.class, "1").setText(richText));

        assertTrue(co$(EntityWithRichText.class)
                           .exists(select(EntityWithRichText.class).where()
                                           .prop("text").eq().val(richText.coreText())
                                           .model()));
    }

    @Test
    public void RichText_components_can_be_used_in_property_paths() {
        final var richText = RichText.fromMarkdown("`cons` **does not** evaluate its arguments in a *lazy* language.");
        save(new_(EntityWithRichText.class, "1").setText(richText));

        assertTrue(co$(EntityWithRichText.class)
                           .exists(select(EntityWithRichText.class).where()
                                           .prop(join(".", "text", _coreText)).eq().val(richText.coreText())
                                           .model()));
        assertTrue(co$(EntityWithRichText.class)
                           .exists(select(EntityWithRichText.class).where()
                                           .prop(join(".", "text", _formattedText)).eq().val(richText.formattedText())
                                           .model()));
    }

    @Override
    protected void populateDomain() {
        super.populateDomain();
    }

}
