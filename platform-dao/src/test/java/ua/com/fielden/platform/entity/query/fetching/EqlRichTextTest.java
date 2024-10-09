package ua.com.fielden.platform.entity.query.fetching;

import org.junit.Test;
import ua.com.fielden.platform.entity.query.EntityAggregates;
import ua.com.fielden.platform.persistence.types.EntityWithRichText;
import ua.com.fielden.platform.test_config.AbstractDaoTestCase;
import ua.com.fielden.platform.types.RichText;

import static java.lang.String.join;
import static org.junit.Assert.*;
import static ua.com.fielden.platform.dao.QueryExecutionModel.from;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;
import static ua.com.fielden.platform.types.RichText.CORE_TEXT;
import static ua.com.fielden.platform.types.RichText.FORMATTED_TEXT;

public class EqlRichTextTest extends AbstractDaoTestCase {

    @Test
    public void RichText_property_is_interpreted_as_its_core_text() {
        final var richText = RichText.fromHtml("<code>cons</code> <b>does not</b> evaluate its arguments in a <i>lazy</i> language.");
        save(new_(EntityWithRichText.class, "1").setText(richText));

        assertTrue(co$(EntityWithRichText.class)
                           .exists(select(EntityWithRichText.class).where()
                                           .prop("text").eq().val(richText.coreText())
                                           .model()));
    }

    @Test
    public void RichText_components_can_be_used_in_property_paths() {
        final var richText = RichText.fromHtml("<code>cons</code> <b>does not</b> evaluate its arguments in a <i>lazy</i> language.");
        save(new_(EntityWithRichText.class, "1").setText(richText));

        assertTrue(co$(EntityWithRichText.class)
                           .exists(select(EntityWithRichText.class).where()
                                           .prop(join(".", "text", CORE_TEXT)).eq().val(richText.coreText())
                                           .model()));
        assertTrue(co$(EntityWithRichText.class)
                           .exists(select(EntityWithRichText.class).where()
                                           .prop(join(".", "text", FORMATTED_TEXT)).eq().val(richText.formattedText())
                                           .model()));
    }

    @Test
    public void RichText_value_is_interpreted_as_its_core_text() {
        final var richText = RichText.fromHtml("<code>cons</code> <b>does not</b> evaluate its arguments in a <i>lazy</i> language.");
        final var entityAgg = co(EntityAggregates.class).getEntity(
                from(select().yield()
                             .caseWhen().val(richText.coreText()).eq().val(richText).then().val("true").otherwise().val("false").end()
                             .as("x").modelAsAggregate())
                        .model());
        assertEquals(entityAgg.get("x"), "true");
    }

    @Test
    public void utf8_formattedText_can_be_used_as_search_criteria() {
        final var richText = RichText.fromHtml("привіт world");
        save(new_(EntityWithRichText.class, "1").setText(richText));

        final var co = co(EntityWithRichText.class);
        final EntityWithRichText entity = co.getEntity(from(select(EntityWithRichText.class).where().prop(join(".", "text", FORMATTED_TEXT)).like().val("привіт%").model()).model());
        assertNotNull(entity);
        assertEquals(richText.formattedText(), entity.getText().formattedText());
    }

    @Test
    public void utf8_coreText_can_be_used_as_search_criteria() {
        final var richText = RichText.fromHtml("привіт world");
        save(new_(EntityWithRichText.class, "1").setText(richText));

        final var co = co(EntityWithRichText.class);
        final EntityWithRichText entity = co.getEntity(from(select(EntityWithRichText.class).where().prop(join(".", "text", CORE_TEXT)).like().val("привіт%").model()).model());
        assertNotNull(entity);
        assertEquals(richText.coreText(), entity.getText().coreText());
    }

}
