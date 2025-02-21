package ua.com.fielden.platform.entity.query.fetching;

import org.junit.Test;
import ua.com.fielden.platform.entity.exceptions.InvalidArgumentException;
import ua.com.fielden.platform.entity.query.EntityAggregates;
import ua.com.fielden.platform.entity.query.exceptions.EntityContainerInstantiationException;
import ua.com.fielden.platform.entity.query.exceptions.EqlException;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils;
import ua.com.fielden.platform.persistence.types.EntityWithRichText;
import ua.com.fielden.platform.test_config.AbstractDaoTestCase;
import ua.com.fielden.platform.types.RichText;

import static graphql.Assert.assertNotNull;
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

    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    // : Yielding into RichText properties
    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    @Test
    public void RichText_property_used_as_yield_alias_in_source_query_can_be_resolved_in_top_level_query() {
        // In general, if P is a RichText property, then prop(P) is interepreted as P.coreText.
        // In this test case, the source query yields into "text" instead of "text.coreText".
        // Nevertheless, prop("text") in the top-level query (which gets interpreted as "text.coreText") can be resolved
        // against "text" from the source query.
        // This can be used in synthetic entity model definitions, enabling the use of yield().X.as("richText") instead of
        // yield().X.as("richText.coreText").
        // Although, ultimately, both "coreText" and "formattedText" would have to be yielded into in a top-level query.
        final var sourceQuery = select().
                yield().val("без унікодів капут").as("text").
                modelAsEntity(EntityWithRichText.class);
        final var query = select(sourceQuery)
                .yield().prop("text").as("str")
                .modelAsAggregate();
        final var entity = co(EntityAggregates.class).getEntity(from(query).model());
        assertNotNull(entity);
        assertEquals("без унікодів капут", entity.get("str"));
    }

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
        assertThrows(EqlException.class,
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
        assertThrows(EqlException.class,
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
        assertThrows(EqlException.class,
                     () -> co(EntityWithRichText.class).getAllEntities(from(query).model()));
    }

    @Test
    public void resulting_entities_cannot_be_instantiated_if_top_level_query_doesnt_yield_into_all_RichText_components_01() {
        // Fails due to absence of text.formattedText yield alias in the top-level query.
        // Error occurs during container instantiation, in RichText constructor, because formattedText is null.
        final var query = select().
                yield().val("без унікодів капут").as("text.coreText").
                modelAsEntity(EntityWithRichText.class);
        assertThrows(InvalidArgumentException.class,
                     () -> co(EntityWithRichText.class).getAllEntities(from(query).model()));
    }

    @Test
    public void resulting_entities_cannot_be_instantiated_if_top_level_query_doesnt_yield_into_all_RichText_components_02() {
        // Fails due to absence of text.coreText yield in the top-level query.
        // Error occurs during container instantiation, in RichText constructor, because coreText is null.
        final var query = select().
                yield().val("без унікодів капут").as("text.formattedText").
                modelAsEntity(EntityWithRichText.class);
        assertThrows(InvalidArgumentException.class,
                     () -> co(EntityWithRichText.class).getAllEntities(from(query).model()));
    }

    @Test
    public void resulting_entities_can_be_instantiated_if_top_level_query_yields_into_all_RichText_components() {
        final var richText = RichText.fromHtml("<code>cons</code> без унікодів капут <b>does not</b> evaluate its arguments in a <i>lazy</i> language.");
        final var query = select().
                yield().val(richText.formattedText()).as("text.formattedText").
                yield().val(richText.coreText()).as("text.coreText").
                modelAsEntity(EntityWithRichText.class);
        final var entity = co(EntityWithRichText.class).getEntity(from(query).model());
        assertNotNull(entity);
        assertEquals(richText, entity.getText());
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

        final var entity = co(EntityAggregates.class).getEntity(EntityQueryUtils.from(query).model());
        assertNotNull(entity);
        assertEquals("привіт world", entity.get("str"));
    }

    @Test
    public void caseWhen_with_unicode_result_in_one_branch_produces_unicode_string_when_modelled_as_aggregates_01() {
        save(new_(EntityWithRichText.class, "1").setText(RichText.fromHtml("привіт")));

        final var query = select(EntityWithRichText.class)
                .yield().caseWhen().prop("key").isNotNull().then().prop("text").otherwise().val("hello").end().as("str")
                .modelAsAggregate();

        final var entity = co(EntityAggregates.class).getEntity(EntityQueryUtils.from(query).model());
        assertNotNull(entity);
        assertEquals("привіт", entity.get("str"));
    }

    @Test
    public void caseWhen_with_unicode_result_in_one_branch_produces_unicode_string_when_modelled_as_aggregates_02() {
        save(new_(EntityWithRichText.class, "1").setText(RichText.fromHtml("привіт")));

        final var query = select(EntityWithRichText.class)
                .yield().caseWhen().prop("key").isNotNull().then().prop("text").otherwise().val(null).end().as("str")
                .modelAsAggregate();

        final var entity = co(EntityAggregates.class).getEntity(EntityQueryUtils.from(query).model());
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

        final var entity = co(EntityAggregates.class).getEntity(EntityQueryUtils.from(query).model());
        assertNotNull(entity);
        assertEquals("привіт world", entity.get("str"));
    }

    @Test
    public void RichText_property_with_unicode_is_lowercased_correctly() {
        save(new_(EntityWithRichText.class, "1").setText(RichText.fromHtml("Мова Програмування")));

        final var query = select(EntityWithRichText.class)
                .yield().lowerCase().prop("text").as("str")
                .modelAsAggregate();

        final var entity = co(EntityAggregates.class).getEntity(EntityQueryUtils.from(query).model());
        assertNotNull(entity);
        assertEquals("мова програмування", entity.get("str"));
    }

    @Test
    public void RichText_property_with_unicode_is_uppercased_correctly() {
        save(new_(EntityWithRichText.class, "1").setText(RichText.fromHtml("Мова Програмування")));

        final var query = select(EntityWithRichText.class)
                .yield().upperCase().prop("text").as("str")
                .modelAsAggregate();

        final var entity = co(EntityAggregates.class).getEntity(EntityQueryUtils.from(query).model());
        assertNotNull(entity);
        assertEquals("МОВА ПРОГРАМУВАННЯ", entity.get("str"));
    }

}
