package ua.com.fielden.platform.types;

import org.junit.Test;
import ua.com.fielden.platform.persistence.types.EntityWithRichText;
import ua.com.fielden.platform.test_config.AbstractDaoTestCase;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static ua.com.fielden.platform.test_utils.TestUtils.assertInstanceOf;

public class RichTextPersistenceTest extends AbstractDaoTestCase {

    @Test
    public void rich_text_property_can_be_saved_and_retrieved() {
        final var co = co$(EntityWithRichText.class);
        final var richText = RichText.fromHtml("hello <b> world </b>");
        co.save(new_(EntityWithRichText.class, "abc").setText(richText));
        final var fetchedEntity = co.findByKey("abc");
        assertInstanceOf(RichText.Persisted.class, fetchedEntity.getText());
        assertEquals(fetchedEntity.getText(), richText);

        assertEquals(fetchedEntity.getText(), co.save(fetchedEntity.setKey("cba")).getText());
    }

    @Test
    public void RichText_property_can_be_assigned_a_persisted_RichText_value_and_saved_afterwards() {
        final var entity1 = save(new_(EntityWithRichText.class, "one").setText(RichText.fromHtml("text one")));
        final var entity2 = save(new_(EntityWithRichText.class, "two").setText(RichText.fromHtml("text two")));
        entity1.setText(entity2.getText());
        assertEquals(entity2.getText(), save(entity1).getText());
    }

    @Test
    public void utf8_text_can_be_saved_and_retrieved() {
        final var richText = RichText.fromHtml("привіт <b> world </b>");
        final var entity = save(new_(EntityWithRichText.class, "one").setText(richText));

        final var co = co(EntityWithRichText.class);
        co.save(entity);
        final var fetchedEntity = co.findByKey("one");
        assertEquals(fetchedEntity.getText(), richText);
    }

    @Override
    protected void populateDomain() {}

}
