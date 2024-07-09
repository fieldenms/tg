package ua.com.fielden.platform.persistence.types;

import org.junit.Test;
import ua.com.fielden.platform.test_config.AbstractDaoTestCase;
import ua.com.fielden.platform.types.RichText;

import static org.junit.Assert.assertEquals;

public class RichTextPersistenceTest extends AbstractDaoTestCase {

    @Test
    public void rich_text_property_can_be_saved_and_retrieved() {
        final var richText = RichText.fromMarkdown("hello *world*");
        final var entity = new_(EntityWithRichText.class, "abc").setText(richText);
        final var co = co$(EntityWithRichText.class);
        co.save(entity);
        final var fetchedEntity = co.findByKey("abc");
        assertEquals(entity.getText(), fetchedEntity.getText());
        assertEquals(richText, fetchedEntity.getText());
    }

}
