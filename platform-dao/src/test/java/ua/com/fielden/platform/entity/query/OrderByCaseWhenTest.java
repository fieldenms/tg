package ua.com.fielden.platform.entity.query;

import org.junit.Test;
import ua.com.fielden.platform.sample.domain.TgPersonName;
import ua.com.fielden.platform.test_config.AbstractDaoTestCase;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static ua.com.fielden.platform.dao.QueryExecutionModel.from;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;

/// Verifies that EQL `orderBy` accepts a `caseWhen` expression as an ordering term.
/// Mirrors the SQL:
/// ```sql
/// SELECT *
/// FROM   personname_
/// where key like '%text%'
/// ORDER BY
///     CASE WHEN key = 'text' THEN 0
///          WHEN key LIKE 'text%' THEN 1
///          ELSE 2
///     END,
///     key
/// ```
/// This class owns the whole `TgPersonName` population, so the query can be run unfiltered — just like `SELECT *`.
///
public class OrderByCaseWhenTest extends AbstractDaoTestCase {

    // An exact match (`text`), two prefix matches (`textbook`, `textual`), and two non-matches (`alpha`, `zeta`).
    private static final List<String> KEYS = List.of("text", "textbook", "textual", "non-text", "alpha", "zeta");

    @Test
    public void orderBy_supports_a_caseWhen_expression_as_an_ordering_term() {
        final var query = select(TgPersonName.class)
                .where().prop("key").like().val("%text%")
                .orderBy()
                    .caseWhen().prop("key").eq().val("text").then().val(0)
                               .when().prop("key").like().val("text%").then().val(1)
                               .otherwise().val(2).endAsInt().asc()
                    .prop("key").asc()
                .model();

        final var actual = co(TgPersonName.class).getAllEntities(from(query).model())
                .stream().map(TgPersonName::getKey).toList();

        // Priority 0: the exact match; priority 1: prefix matches (then by key); priority 2: the rest (then by key).
        // This differs from plain key-ascending order (alpha, text, textbook, textual, zeta),
        // so the assertion fails if the caseWhen term is dropped or mis-evaluated.
        assertEquals(List.of("text", "textbook", "textual", "non-text"), actual);
    }

    @Override
    protected void populateDomain() {
        super.populateDomain();

        if (useSavedDataPopulationScript()) {
            return;
        }

        KEYS.stream().map(key -> new_(TgPersonName.class, key)).forEach(this::save);
    }

}
