package ua.com.fielden.platform.entity.query.fetching;

import org.junit.Test;
import ua.com.fielden.platform.entity.exceptions.InvalidArgumentException;
import ua.com.fielden.platform.entity.query.EntityAggregates;
import ua.com.fielden.platform.entity.query.exceptions.EntityContainerInstantiationException;
import ua.com.fielden.platform.eql.retrieval.exceptions.EntityRetrievalException;
import ua.com.fielden.platform.persistence.types.EntityWithRichText;
import ua.com.fielden.platform.test_config.AbstractDaoTestCase;
import ua.com.fielden.platform.types.RichText;

import java.lang.reflect.Field;

import static org.junit.Assert.*;
import static ua.com.fielden.platform.dao.QueryExecutionModel.from;
import static ua.com.fielden.platform.entity.AbstractEntity.KEY;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;
import static ua.com.fielden.platform.types.RichText.SEARCH_TEXT;

public class EqlRichTextTest extends AbstractDaoTestCase {

    @Test
    public void RichText_property_is_interpreted_as_its_search_text() {
        final var richText = RichText.fromHtml("<b>Belief</b> as <a href='https://localhost'>anticipation</a> <i>controller</i>.");
        final var key = "1";
        save(new_(EntityWithRichText.class, key).setText(richText));

        assertTrue(co$(EntityWithRichText.class)
                           .exists(select(EntityWithRichText.class).where()
                                           .prop("key").eq().val(key)
                                           .and()
                                           .prop("text").eq().prop("text.searchText")
                                           .model()));
        assertTrue(co$(EntityWithRichText.class)
                           .exists(select(EntityWithRichText.class).where()
                                           .prop("key").eq().val(key)
                                           .and()
                                           .prop("text").iLike().val("%anticipation controller%")
                                           .model()));
    }


    @Test
    public void RichText_components_can_be_used_in_property_paths() {
        final var richText = RichText.fromHtml("<code>cons</code> <b>does not</b> evaluate its arguments in a <i>lazy</i> language.");
        final var key = "1";
        save(new_(EntityWithRichText.class, key).setText(richText));

        assertTrue(co$(EntityWithRichText.class)
                           .exists(select(EntityWithRichText.class).where()
                                           .prop("key").eq().val(key)
                                           .and()
                                           .prop( "text.coreText").isNotNull()
                                           .model()));
        assertTrue(co$(EntityWithRichText.class)
                           .exists(select(EntityWithRichText.class).where()
                                           .prop("key").eq().val(key)
                                           .and()
                                           .prop("text.formattedText").isNotNull()
                                           .model()));
        assertTrue(co$(EntityWithRichText.class)
                           .exists(select(EntityWithRichText.class).where()
                                           .prop("key").eq().val(key)
                                           .and()
                                           .prop("text.searchText").isNotNull()
                                           .model()));
    }

    @Test
    public void RichText_components_formattedText_and_coreText_can_be_used_as_values() {
        final var richText = RichText.fromHtml("<code>cons</code> <b>does not</b> evaluate its arguments in a <i>lazy</i> language.");
        final var key = "1";
        save(new_(EntityWithRichText.class, key).setText(richText));

        assertTrue(co$(EntityWithRichText.class)
                           .exists(select(EntityWithRichText.class).where()
                                           .prop("key").eq().val(key)
                                           .and()
                                           .val(richText.formattedText()).eq().prop("text.formattedText")
                                           .model()));
        assertTrue(co$(EntityWithRichText.class)
                           .exists(select(EntityWithRichText.class).where()
                                           .prop("key").eq().val(key)
                                           .and()
                                           .val(richText.coreText()).eq().prop("text.coreText")
                                           .model()));
    }

    @Test
    public void utf8_formattedText_can_be_used_as_search_criteria() {
        final var richText = RichText.fromHtml("привіт world");
        save(new_(EntityWithRichText.class, "1").setText(richText));

        final var co = co(EntityWithRichText.class);
        final EntityWithRichText entity = co.getEntity(from(select(EntityWithRichText.class).where().prop("text.formattedText").like().val("привіт%").model()).model());
        assertNotNull(entity);
        assertEquals(richText, entity.getText());
    }

    @Test
    public void utf8_coreText_can_be_used_as_search_criteria() {
        final var richText = RichText.fromHtml("привіт world");
        save(new_(EntityWithRichText.class, "1").setText(richText));

        final var co = co(EntityWithRichText.class);
        final EntityWithRichText entity = co.getEntity(from(select(EntityWithRichText.class).where().prop("text.coreText").like().val("привіт%").model()).model());
        assertNotNull(entity);
        assertEquals(richText.coreText(), entity.getText().coreText());
    }

    @Test
    public void utf8_searchText_can_be_used_as_search_criteria() {
        final var richText = RichText.fromHtml("привіт world");
        save(new_(EntityWithRichText.class, "1").setText(richText));

        final var co = co(EntityWithRichText.class);
        final EntityWithRichText entity = co.getEntity(from(select(EntityWithRichText.class).where().prop("text.searchText").like().val("привіт%").model()).model());
        assertNotNull(entity);
        assertEquals(richText, entity.getText());
    }

    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    // : Yielding into RichText properties
    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    @Test
    public void yielding_null_into_RichText_property_results_in_it_being_assigned_null_value() {
        final var query = select()
                .yield().val(null).as("text")
                .modelAsEntity(EntityWithRichText.class);
        final var entity = co(EntityWithRichText.class).getEntity(from(query).model());
        assertNotNull(entity);
        assertNull(entity.getText());
    }

    @Test
    public void RichText_property_cannot_be_used_as_a_yield_alias() {
        // Entity instantiation fails due to an attempt to set value "hello" into property "text" that has type RichText.
        // This happens because EQL doesn't know that the yield with alias "text" should be treated as a component-typed value,
        // and treats it as a primitive.
        final var query = select()
                .yield().val("hello").as("text")
                .modelAsEntity(EntityWithRichText.class);
        assertThrows(EntityContainerInstantiationException.class,
                     () -> co(EntityWithRichText.class).getAllEntities(from(query).model()));
    }

    @Test
    public void top_level_query_fails_if_contains_implicitly_added_yield_for_RichText_property_when_source_query_doesnt_yield_all_RichText_components_01() {
        // Implicitly added yields contain "text.coreText" and "text.formattedText", while the source query yields only
        // "text.coreText", which causes an error during resolution of "text.formattedText".
        final var sourceQuery = select().
                yield().val("hello").as("text.coreText").
                modelAsEntity(EntityWithRichText.class);
        final var query = select(sourceQuery).modelAsEntity(EntityWithRichText.class);
        assertThrows(EntityRetrievalException.class,
                     () -> co(EntityWithRichText.class).getAllEntities(from(query).model()));
    }

    @Test
    public void top_level_query_fails_if_contains_implicitly_added_yield_for_RichText_property_when_source_query_doesnt_yield_all_RichText_components_02() {
        // Implicitly added yields contain "text.coreText" and "text.formattedText", while the source query yields only
        // "text.formattedText", which causes an error during resolution of "text.coreText".
        final var sourceQuery = select().
                yield().val("hello").as("text.formattedText").
                modelAsEntity(EntityWithRichText.class);
        final var query = select(sourceQuery)
                .modelAsEntity(EntityWithRichText.class);
        assertThrows(EntityRetrievalException.class,
                     () -> co(EntityWithRichText.class).getAllEntities(from(query).model()));
    }

    @Test
    public void top_level_query_fails_if_it_contains_implicitly_added_yield_for_RichText_property_when_source_query_doesnt_yield_all_RichText_components_03() {
        // Implicitly added yields contain "text.coreText" and "text.formattedText", while the source query yields only
        // "text", which causes an error during resolution of both "text.coreText" and "text.formattedText".
        final var sourceQuery = select().
                yield().val("hello").as("text").
                modelAsEntity(EntityWithRichText.class);
        final var query = select(sourceQuery)
                .modelAsEntity(EntityWithRichText.class);
        assertThrows(EntityRetrievalException.class,
                     () -> co(EntityWithRichText.class).getAllEntities(from(query).model()));
    }

    @Test
    public void resulting_entities_cannot_be_instantiated_if_top_level_query_yields_only_coreText() {
        // Fails due to absence of text.formattedText and text.searchText yield aliases in the top-level query.
        // Error occurs during container instantiation, in RichText constructor, because unfetched components are null.
        final var query = select().
                yield().val("без унікодів капут").as("text.coreText").
                modelAsEntity(EntityWithRichText.class);
        assertThrows(InvalidArgumentException.class,
                     () -> co(EntityWithRichText.class).getAllEntities(from(query).model()));
    }

    @Test
    public void resulting_entities_cannot_be_instantiated_if_top_level_query_yields_only_formattedText() {
        final var query = select().
                yield().val("без унікодів капут").as("text.formattedText").
                modelAsEntity(EntityWithRichText.class);
        assertThrows(InvalidArgumentException.class,
                     () -> co(EntityWithRichText.class).getAllEntities(from(query).model()));
    }

    @Test
    public void resulting_entities_cannot_be_instantiated_if_top_level_query_yields_only_searchText() {
        final var query = select().
                yield().val("без унікодів капут").as("text.searchText").
                modelAsEntity(EntityWithRichText.class);
        assertThrows(InvalidArgumentException.class,
                     () -> co(EntityWithRichText.class).getAllEntities(from(query).model()));
    }

    @Test
    public void resulting_entities_can_be_instantiated_if_top_level_query_yields_into_formattedText_and_coreText() {
        final var richText = RichText.fromHtml("<code>cons</code> без унікодів капут <b>does not</b> evaluate its arguments in a <i>lazy</i> language.");
        final var query = select().
                yield().val(richText.formattedText()).as("text.formattedText").
                yield().val(richText.coreText()).as("text.coreText").
                modelAsEntity(EntityWithRichText.class);
        final var entity = co(EntityWithRichText.class).getEntity(from(query).model());
        assertNotNull(entity);
        assertEquals(richText, entity.getText());
    }

    @Test
    public void searchText_can_be_yielded_explicitly_as_a_value_of_some_property() {
        save(new_(EntityWithRichText.class, "1").setText(RichText.fromHtml("hello")));

        final var someProperty = "searchText";
        final var query = select(EntityWithRichText.class)
                          .where().prop(KEY).eq().val("1")
                          .yield().prop("text.searchText").as(someProperty)
                          .modelAsAggregate();
       final var agg = co(EntityAggregates.class).getEntity(from(query).model());
       assertEquals("hello", agg.get(someProperty));
    }

    @Test
    public void searchText_can_be_yielded_explicitly_as_a_RichText_component_but_this_is_meaningless() throws NoSuchFieldException, IllegalAccessException {
        save(new_(EntityWithRichText.class, "1").setText(RichText.fromHtml("hello")));

        // Yielding .prop("text.searchText").as("text.searchText") works.
        // However, it is meaningless because it is impossible to either assign or access the searchText component for an instance of RichText.
        final var query = select(EntityWithRichText.class)
                          .where().prop(KEY).eq().val("1")
                          .yield().prop("text.formattedText").as("text.formattedText")
                          .yield().prop("text.coreText").as("text.coreText")
                          .yield().prop("text.searchText").as("text.searchText")
                          .modelAsEntity(EntityWithRichText.class);
        final var entity = co(EntityWithRichText.class).getEntity(from(query).model());
        final RichText text = entity.getText();
        assertNotNull(text);
        // Can only access text.searchText via field.
        final Field fSearchText = RichText.class.getDeclaredField(SEARCH_TEXT);
        fSearchText.trySetAccessible();
        assertNull("Expected searchText to remain null even though it was yielded.", fSearchText.get(text));
    }

    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    // : Using RichText properties in EQL expressions
    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    @Test
    public void concatenation_of_unicode_RichText_with_ascii_string_produces_unicode_string_when_modelled_as_aggregates() {
        save(new_(EntityWithRichText.class, "1").setText(RichText.fromHtml("привіт")));

        final var query = select(EntityWithRichText.class)
                .yield().concat().prop("text").with().val(" world").end().as("str")
                .modelAsAggregate();

        final var entity = co(EntityAggregates.class).getEntity(from(query).model());
        assertNotNull(entity);
        assertEquals("привіт world", entity.get("str"));
    }

    @Test
    public void caseWhen_with_unicode_result_in_one_branch_produces_unicode_string_when_modelled_as_aggregates_01() {
        save(new_(EntityWithRichText.class, "1").setText(RichText.fromHtml("привіт")));

        final var query = select(EntityWithRichText.class)
                .yield().caseWhen().prop("key").isNotNull().then().prop("text").otherwise().val("hello").end().as("str")
                .modelAsAggregate();

        final var entity = co(EntityAggregates.class).getEntity(from(query).model());
        assertNotNull(entity);
        assertEquals("привіт", entity.get("str"));
    }

    @Test
    public void caseWhen_with_unicode_result_in_one_branch_produces_unicode_string_when_modelled_as_aggregates_02() {
        save(new_(EntityWithRichText.class, "1").setText(RichText.fromHtml("привіт")));

        final var query = select(EntityWithRichText.class)
                .yield().caseWhen().prop("key").isNotNull().then().prop("text").otherwise().val(null).end().as("str")
                .modelAsAggregate();

        final var entity = co(EntityAggregates.class).getEntity(from(query).model());
        assertNotNull(entity);
        assertEquals("привіт", entity.get("str"));
    }

    @Test
    public void concatenation_of_RichText_property_that_contains_unicode_with_ascii_property_results_in_unicode_string() {
        save(new_(EntityWithRichText.class, "1")
                     .setText(RichText.fromHtml("привіт"))
                     .setPlainText(" world"));

        final var query = select(EntityWithRichText.class)
                .yield().concat().prop("text").with().prop("plainText").end().as("str")
                .modelAsAggregate();

        final var entity = co(EntityAggregates.class).getEntity(from(query).model());
        assertNotNull(entity);
        assertEquals("привіт world", entity.get("str"));
    }

    @Test
    public void RichText_property_with_unicode_is_lowercased_correctly() {
        save(new_(EntityWithRichText.class, "1").setText(RichText.fromHtml("Мова Програмування")));

        final var query = select(EntityWithRichText.class)
                .yield().lowerCase().prop("text").as("str")
                .modelAsAggregate();

        final var entity = co(EntityAggregates.class).getEntity(from(query).model());
        assertNotNull(entity);
        assertEquals("мова програмування", entity.get("str"));
    }

    @Test
    public void RichText_property_with_unicode_is_uppercased_correctly() {
        save(new_(EntityWithRichText.class, "1").setText(RichText.fromHtml("Мова Програмування")));

        final var query = select(EntityWithRichText.class)
                .yield().upperCase().prop("text").as("str")
                .modelAsAggregate();

        final var entity = co(EntityAggregates.class).getEntity(from(query).model());
        assertNotNull(entity);
        assertEquals("МОВА ПРОГРАМУВАННЯ", entity.get("str"));
    }

}
