package ua.com.fielden.platform.types;

import org.junit.Test;
import ua.com.fielden.platform.entity.query.EntityAggregates;
import ua.com.fielden.platform.persistence.types.EntityWithRichText;
import ua.com.fielden.platform.test_config.AbstractDaoTestCase;

import java.lang.reflect.Field;

import static org.junit.Assert.*;
import static ua.com.fielden.platform.dao.QueryExecutionModel.from;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;
import static ua.com.fielden.platform.test_utils.TestUtils.assertInstanceOf;
import static ua.com.fielden.platform.types.RichText.SEARCH_TEXT;

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

    @Test
    public void search_text_is_persisted_and_updated_along_with_RichText() {
        final var key = "A";
        final var entityA_1 = save(new_(EntityWithRichText.class, key).setText(RichText.fromHtml("First")));
        assertEquals("First", fetchSearchText(key));

        final var entityA_2 = save(entityA_1.setText(RichText.fromHtml("Second")));
        assertEquals("Second", fetchSearchText(key));
    }

    @Test
    public void search_text_is_persisted_even_when_not_retrieved() throws NoSuchFieldException, IllegalAccessException {
        final var richText = RichText.fromHtml("Read the fine manual");
        final var searchTerm = "%manual%";

        final var entityA = save(new_(EntityWithRichText.class, "A").setText(richText));

        // Create entity B by retrieve entity A without text.searchText.
        // Do not yield id to make entity B a new entity.
        final var entityB = co$(EntityWithRichText.class).getEntity(
                from(select(EntityWithRichText.class)
                             .where().prop("key").eq().val(entityA.getKey())
                             .yield().prop("text.formattedText").as("text.formattedText")
                             .yield().prop("text.coreText").as("text.coreText")
                             .yield().val("B").as("key")
                             .modelAsEntity(EntityWithRichText.class))
                        .model());
        final RichText text = entityB.getText();
        assertNotNull(text);
        // Can only assert that text.searchText is null via field access, because invoking method searchText() initialises the value.
        final Field fSearchText = RichText.class.getDeclaredField(SEARCH_TEXT);
        fSearchText.trySetAccessible();
        assertNull("Expected searchText not to be fetched from the database.", fSearchText.get(text));

        // Save entity B and assert that text.searchText was computed and persisted
        final var entityB_saved = save(entityB);
        assertNotEquals("Expected distinct entity identities.", entityA.getId(), entityB_saved.getId());

        // Asserting the fact of text.searchText being persisted is based on search rather than data retrieval.
        assertTrue("Expected entity B to have searchText persisted.",
                   co(EntityWithRichText.class).exists(
                           select(EntityWithRichText.class)
                                   .where()
                                   .prop("key").eq().val(entityB.getKey())
                                   .and()
                                   .prop("text").iLike().val(searchTerm)
                                   .model()));
    }

    @Override
    protected void populateDomain() {}

    private String fetchSearchText(final String key) {
        final var query = select(EntityWithRichText.class)
                .where().prop("key").eq().val(key)
                .yield().prop("text.searchText").as("searchText")
                .modelAsAggregate();

        return co(EntityAggregates.class)
                .getEntity(from(query).model())
                .get("searchText");
    }

}
