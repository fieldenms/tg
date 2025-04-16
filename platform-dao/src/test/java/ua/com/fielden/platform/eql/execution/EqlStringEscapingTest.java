package ua.com.fielden.platform.eql.execution;

import org.junit.Test;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.DbVersion;
import ua.com.fielden.platform.entity.query.IDbVersionProvider;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.sample.domain.TgPattern;
import ua.com.fielden.platform.sample.domain.TgPersonName;
import ua.com.fielden.platform.test.WithDbVersion;
import ua.com.fielden.platform.test_config.AbstractDaoTestCase;

import java.util.Collection;
import java.util.List;

import static org.junit.Assert.assertTrue;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.from;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;
import static ua.com.fielden.platform.test_utils.CollectionTestUtils.assertEqualByContents;

/**
 * This test suite pertains to the escaping of special characters in pattern operands of {@code like} clauses in EQL.
 */
public class EqlStringEscapingTest extends AbstractDaoTestCase {

    private final IDbVersionProvider dbVersionProvider = getInstance(IDbVersionProvider.class);

    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    // : Escaping of underscores
    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    @Test
    @WithDbVersion({DbVersion.POSTGRESQL, DbVersion.MSSQL})
    public void underscore_is_escaped_when_LIKE_pattern_is_string() {
        assertEqualsByKey(List.of("_"),
                          findAllByKeyLikeValue("_"));
        assertEqualsByKey(List.of("one_"),
                          findAllByKeyLikeValue("one_"));
        assertEqualsByKey(List.of("one_two"),
                          findAllByKeyLikeValue("one_two"));
        assertEqualsByKey(List.of("one_two"),
                          findAllByKeyILikeValue("ONE_TWO"));
        assertEqualsByKey(List.of("_", "__", "one_", "one_two", "one[_]two", "one_[]_two"),
                          findAllByKeyLikeValue("%_%"));
    }

    @Test
    @WithDbVersion({DbVersion.POSTGRESQL, DbVersion.MSSQL})
    public void underscore_is_escaped_when_LIKE_pattern_is_property() {
        assertEqualsByKey(List.of("_"),
                          findAllByKeyLikeProp("_"));
        assertEqualsByKey(List.of("one_"),
                          findAllByKeyLikeProp("one_"));
        assertEqualsByKey(List.of("one_two"),
                          findAllByKeyLikeProp("one_two"));
        assertEqualsByKey(List.of("_", "__", "one_", "one_two", "one[_]two", "one_[]_two"),
                          findAllByKeyLikeProp("%_%"));
    }

    @Test
    @WithDbVersion({DbVersion.POSTGRESQL, DbVersion.MSSQL})
    public void underscore_is_escaped_when_LIKE_pattern_is_parameter() {
        assertEqualsByKey(List.of("_"),
                          findAllByKeyLikeParam("_"));
        assertEqualsByKey(List.of("one_"),
                          findAllByKeyLikeParam("one_"));
        assertEqualsByKey(List.of("one_two"),
                          findAllByKeyLikeParam("one_two"));
        assertEqualsByKey(List.of("_", "__", "one_", "one_two", "one[_]two", "one_[]_two"),
                          findAllByKeyLikeParam("%_%"));
    }

    @Test
    @WithDbVersion({DbVersion.POSTGRESQL, DbVersion.MSSQL})
    public void underscore_is_escaped_when_LIKE_pattern_is_expression() {
        assertEqualsByKey(List.of("_"),
                          findAllByKeyLikeModel("_"));
        assertEqualsByKey(List.of("one_"),
                          findAllByKeyLikeModel("one_"));
        assertEqualsByKey(List.of("one_two"),
                          findAllByKeyLikeModel("one_two"));
        assertEqualsByKey(List.of("_", "__", "one_", "one_two", "one[_]two", "one_[]_two"),
                          findAllByKeyLikeModel("%_%"));

        assertEqualsByKey(List.of("one_two"),
                          select(TgPersonName.class).where()
                                  .prop("key").like().lowerCase().val("ONE_TWO")
                                  .model());
    }

    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    // : Escaping of square brackets
    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    @Test
    @WithDbVersion(DbVersion.MSSQL)
    public void opening_square_bracket_is_escaped_when_LIKE_pattern_is_string() {
        assertEqualsByKey(List.of("[cb]at"),
                          findAllByKeyLikeValue("[cb]at"));
        assertEqualsByKey(List.of("[c]at"),
                          findAllByKeyLikeValue("[c]at"));
        assertEqualsByKey(List.of(),
                          findAllByKeyLikeValue("[b]at"));
        assertEqualsByKey(List.of("[[]c]at"),
                          findAllByKeyLikeValue("[[]c]at"));
    }

    @Test
    @WithDbVersion(DbVersion.MSSQL)
    public void opening_square_bracket_is_escaped_when_LIKE_pattern_is_property() {
        assertEqualsByKey(List.of("[cb]at"),
                          findAllByKeyLikeProp("[cb]at"));
        assertEqualsByKey(List.of("[c]at"),
                          findAllByKeyLikeProp("[c]at"));
        assertEqualsByKey(List.of(),
                          findAllByKeyLikeProp("[b]at"));
        assertEqualsByKey(List.of("[[]c]at"),
                          findAllByKeyLikeProp("[[]c]at"));
    }

    @Test
    @WithDbVersion(DbVersion.MSSQL)
    public void opening_square_bracket_is_escaped_when_LIKE_pattern_is_parameter() {
        assertEqualsByKey(List.of("[cb]at"),
                          findAllByKeyLikeParam("[cb]at"));
        assertEqualsByKey(List.of("[c]at"),
                          findAllByKeyLikeParam("[c]at"));
        assertEqualsByKey(List.of(),
                          findAllByKeyLikeParam("[b]at"));
        assertEqualsByKey(List.of("[[]c]at"),
                          findAllByKeyLikeParam("[[]c]at"));
    }

    @Test
    @WithDbVersion(DbVersion.MSSQL)
    public void opening_square_bracket_is_escaped_when_LIKE_pattern_is_expression() {
        assertEqualsByKey(List.of("[cb]at"),
                          findAllByKeyLikeModel("[cb]at"));
        assertEqualsByKey(List.of("[c]at"),
                          findAllByKeyLikeModel("[c]at"));
        assertEqualsByKey(List.of(),
                          findAllByKeyLikeModel("[b]at"));
        assertEqualsByKey(List.of("[[]c]at"),
                          findAllByKeyLikeModel("[[]c]at"));
    }

    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    // : Escaping of backslash
    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    @Test
    @WithDbVersion(DbVersion.POSTGRESQL)
    public void backslash_is_not_special_when_LIKE_pattern_is_string() {
        assertEqualsByKey(List.of("\\"),
                          findAllByKeyLikeValue("\\"));
        assertEqualsByKey(List.of(),
                          findAllByKeyLikeValue("\\\\"));
        assertEqualsByKey(List.of("one\\two"),
                          findAllByKeyLikeValue("one\\two"));
        assertEqualsByKey(List.of("o\\ne"),
                          findAllByKeyLikeValue("o\\ne"));
        assertEqualsByKey(List.of(),
                          findAllByKeyLikeValue("o\ne"));
        assertEqualsByKey(List.of("one"),
                          findAllByKeyLikeValue("one"));
        assertEqualsByKey(List.of("[\\]"),
                          findAllByKeyLikeValue("[\\]"));
        assertEqualsByKey(List.of(),
                          findAllByKeyLikeValue("[\\\\]"));
    }

    @Test
    @WithDbVersion(DbVersion.POSTGRESQL)
    public void backslash_is_not_special_when_LIKE_pattern_is_parameter() {
        assertEqualsByKey(List.of("\\"),
                          findAllByKeyLikeParam("\\"));
        assertEqualsByKey(List.of(),
                          findAllByKeyLikeParam("\\\\"));
        assertEqualsByKey(List.of("one\\two"),
                          findAllByKeyLikeParam("one\\two"));
        assertEqualsByKey(List.of("o\\ne"),
                          findAllByKeyLikeParam("o\\ne"));
        assertEqualsByKey(List.of(),
                          findAllByKeyLikeParam("o\ne"));
        assertEqualsByKey(List.of("one"),
                          findAllByKeyLikeParam("one"));
        assertEqualsByKey(List.of("[\\]"),
                          findAllByKeyLikeParam("[\\]"));
        assertEqualsByKey(List.of(),
                          findAllByKeyLikeParam("[\\\\]"));
    }

    @Test
    @WithDbVersion(DbVersion.POSTGRESQL)
    public void backslash_is_not_special_when_LIKE_pattern_is_property() {
        assertEqualsByKey(List.of("\\"),
                          findAllByKeyLikeProp("\\"));
        assertEqualsByKey(List.of(),
                          findAllByKeyLikeProp("\\\\"));
        assertEqualsByKey(List.of("one\\two"),
                          findAllByKeyLikeProp("one\\two"));
        assertEqualsByKey(List.of("o\\ne"),
                          findAllByKeyLikeProp("o\\ne"));
        assertEqualsByKey(List.of(),
                          findAllByKeyLikeProp("o\ne"));
        assertEqualsByKey(List.of("one"),
                          findAllByKeyLikeProp("one"));
        assertEqualsByKey(List.of("[\\]"),
                          findAllByKeyLikeProp("[\\]"));
        assertEqualsByKey(List.of(),
                          findAllByKeyLikeProp("[\\\\]"));
    }

    @Test
    @WithDbVersion(DbVersion.POSTGRESQL)
    public void backslash_is_not_special_when_LIKE_pattern_is_expression() {
        assertEqualsByKey(List.of("one\\two"),
                          select(TgPersonName.class).where()
                                  .prop("key").like().concat().val("one").with().val("\\").with().val("two").end()
                                  .model());

        assertEqualsByKey(List.of("\\", "one\\two", "o\\ne", "[\\]"),
                          select(TgPersonName.class).where()
                                  .prop("key").like()
                                      .concat().val("%")
                                      .with().caseWhen().val(1).isNotNull().then().val("\\").end()
                                      .with().val("%")
                                      .end()
                                  .model());
    }

    @Override
    protected void populateDomain() {
        save(new_(TgPersonName.class, "one"));

        // Underscores
        save(new_(TgPersonName.class, "_"));
        save(new_(TgPersonName.class, "x"));
        save(new_(TgPersonName.class, "__"));
        save(new_(TgPersonName.class, "xx"));
        save(new_(TgPersonName.class, "one_"));
        save(new_(TgPersonName.class, "one."));
        save(new_(TgPersonName.class, "one_two"));
        save(new_(TgPersonName.class, "one.two"));

        // Brackets
        save(new_(TgPersonName.class, "cat"));
        save(new_(TgPersonName.class, "[c]at"));
        save(new_(TgPersonName.class, "bat"));
        save(new_(TgPersonName.class, "[cb]at"));
        save(new_(TgPersonName.class, "[[]c]at"));

        // Underscores and brackets
        save(new_(TgPersonName.class, "one[_]two"));
        save(new_(TgPersonName.class, "one[.]two"));
        save(new_(TgPersonName.class, "one_[]_two"));

        // Backslash
        save(new_(TgPersonName.class, "\\"));
        save(new_(TgPersonName.class, "one\\two"));
        save(new_(TgPersonName.class, "o\\ne"));
        save(new_(TgPersonName.class, "[\\]"));

        // Patterns
        List.of("_",
                "one_",
                "one_two",
                "%_%",
                "[cb]at",
                "[c]at",
                "[b]at",
                "[[]c]at",
                "\\",
                "\\\\",
                "one\\two",
                "o\\ne",
                "o\ne",
                "one",
                "[\\]",
                "[\\\\]")
                .forEach(key -> save(new_composite(TgPattern.class, key)));

    }

    private Collection<TgPersonName> findAllByKeyLikeValue(final String key) {
        final var query = select(TgPersonName.class).where().prop("key").like().val(key).model();
        return co(TgPersonName.class).getAllEntities(from(query).model());
    }

    private Collection<? extends AbstractEntity<String>> findAllByKeyILikeValue(final String key) {
        final var query = select(TgPersonName.class).where().prop("key").iLike().val(key).model();
        return co(TgPersonName.class).getAllEntities(from(query).model());

    }

    private Collection<? extends AbstractEntity<String>> findAllByKeyLikeProp(final String key) {
        assertTrue("Pattern doesn't exist: %s".formatted(key),
                   co(TgPattern.class).entityWithKeyExists(key));

        final var query = select(TgPersonName.class)
                .where()
                .prop("key")
                .like()
                .model(select(TgPattern.class).where().prop(TgPattern.PATTERN).eq().val(key).yield().prop(TgPattern.PATTERN).modelAsPrimitive())
                .model();
        return co(TgPersonName.class).getAllEntities(from(query).model());
    }

    private Collection<? extends AbstractEntity<String>> findAllByKeyLikeParam(final String key) {
        final var query = select(TgPersonName.class)
                .where()
                .prop("key")
                .like()
                .param("pattern")
                .model();
        return co(TgPersonName.class).getAllEntities(from(query).with("pattern", key).model());
    }

    private Collection<? extends AbstractEntity<String>> findAllByKeyLikeModel(final String key) {
        final var query = select(TgPersonName.class)
                .where()
                .prop("key")
                .like()
                .model(select(select().yield().val(key).as("value").modelAsAggregate()).yield().prop("value").modelAsPrimitive())
                .model();
        return co(TgPersonName.class).getAllEntities(from(query).with("pattern", key).model());
    }

    /**
     * Asserts that a collection of keys is equal by contents to a collection of entities, irrespective of order.
     * Each specified key must map exactly to one entity, and vice versa.
     */
    private static <K extends Comparable<?>> void assertEqualsByKey(
            final Collection<K> keys,
            final Collection<? extends AbstractEntity<K>> entities)
    {
        assertEqualByContents(keys, entities.stream().map(AbstractEntity::getKey).toList());
    }

    /**
     * Retrieves all entities with the specified query, and calls {@link #assertEqualsByKey(Collection, Collection)}.
     */
    @SuppressWarnings("unchecked")
    private <K extends Comparable<?>> void assertEqualsByKey(
            final Collection<K> keys,
            final EntityResultQueryModel query)
    {
        final var entities = co(query.getResultType()).getAllEntities(from(query).model());
        assertEqualsByKey(keys, entities);
    }

}
