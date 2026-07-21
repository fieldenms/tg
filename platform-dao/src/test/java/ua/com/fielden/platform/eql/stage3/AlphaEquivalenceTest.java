package ua.com.fielden.platform.eql.stage3;

import com.google.common.collect.ImmutableList;
import org.junit.Test;
import ua.com.fielden.platform.eql.meta.EqlStage3TestCase;
import ua.com.fielden.platform.eql.stage3.queries.SubQueryForExists3;
import ua.com.fielden.platform.sample.domain.TgVehicle;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;

/// Tests for alpha-equivalence of stage-3 query ASTs.
///
/// There are two kinds of tests:
/// 1. The renaming is produced by transforming the *same* query with two different source-ID sequences:
///    one transformation seeds IDs from the default 0, while another seeds them from a number large enough so that
///    they don't overlap.
///    The two ASTs are then identical except that every source ID is shifted, so they must be alpha-equivalent while
///    *not* being structurally equal.
/// 2. One top-level query contains multiple nested queries which are tested for alpha-equivalence.
///    One transformation, one source-ID sequence -- this setup reflects real conditions.
///
public class AlphaEquivalenceTest extends EqlStage3TestCase {

    /// Offset for the second source-ID sequence; large enough not to overlap the first.
    ///
    private static final int SOURCE_ID_OFFSET = 10000;

    @Test
    public void a_node_is_alpha_equivalent_to_itself() {
        final var qry = qry(select(TgVehicle.class).model());
        assertAlphaEq(qry, qry);
    }

    // :::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    // : Positive -- alpha-equivalence is invariant under source-ID renaming, across binding structures.

    /// Class 1: a single source referenced by a property.
    ///
    @Test
    public void a_query_over_a_single_source_is_alpha_equivalent_to_its_renaming() {
        final var query = select(TgVehicle.class).where().prop("initDate").isNotNull().model();
        final var q1 = qry(query);
        final var q2 = qry(query, SOURCE_ID_OFFSET);
        assertNotStructEq(q1, q2);
        assertAlphaEq(q1, q2);
    }

    /// Class 2: two sources of the same type at the same level (self-join).
    /// The two sources are structurally indistinguishable, so only the ID-correspondence can align them.
    ///
    @Test
    public void a_self_join_is_alpha_equivalent_to_its_renaming() {
        final var query = select(TgVehicle.class).as("v")
                .join(TgVehicle.class).as("rv").on().prop("v.replacedBy").eq().prop("rv.id")
                .model();
        final var q1 = qry(query);
        final var q2 = qry(query, SOURCE_ID_OFFSET);
        assertNotStructEq(q1, q2);
        assertAlphaEq(q1, q2);
    }

    @Test
    public void queries_that_differ_only_in_source_aliases_are_alpha_equivalent() {
        final var query1 = select(TgVehicle.class).as("v")
                .join(TgVehicle.class).as("rv").on().prop("v.replacedBy").eq().prop("rv.id")
                .model();
        final var query2 = select(TgVehicle.class).as("rv")
                .join(TgVehicle.class).as("v").on().prop("rv.replacedBy").eq().prop("v.id")
                .model();
        final var q1 = qry(query1);
        final var q2 = qry(query2, SOURCE_ID_OFFSET);
        assertNotStructEq(q1, q2);
        assertAlphaEq(q1, q2);
    }

    /// Class 3: an uncorrelated subquery -- an independent nested binding scope.
    ///
    @Test
    public void a_query_with_an_uncorrelated_subquery_is_alpha_equivalent_to_its_renaming() {
        final var query = select(ORG1).where()
                .exists(select(ORG2).where().prop("parent").isNotNull().model())
                .model();
        final var q1 = qry(query);
        final var q2 = qry(query, SOURCE_ID_OFFSET);
        assertNotStructEq(q1, q2);
        assertAlphaEq(q1, q2);
    }

    /// Class 4: a correlated subquery -- the inner query references an enclosing source via `extProp`.
    /// This is the multi-level case that the level-list in the traversal state exists for.
    ///
    @Test
    public void a_query_with_a_correlated_subquery_is_alpha_equivalent_to_its_renaming() {
        final var query = select(ORG1).where()
                .exists(select(ORG2).where().prop("parent").eq().extProp("id").model())
                .model();
        final var q1 = qry(query);
        final var q2 = qry(query, SOURCE_ID_OFFSET);
        assertNotStructEq(q1, q2);
        assertAlphaEq(q1, q2);
    }

    // :::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    // : Sibling subqueries within a single query.
    // : The renaming tests above compare two independent compilations, which differ by a uniform source-ID offset.
    // : These instead compare two subqueries compiled *together*, so their IDs differ by whatever the compiler
    // : assigned -- a non-uniform renaming, in real surrounding context.

    /// Two identical uncorrelated subqueries, side by side, are alpha-equivalent to each other.
    ///
    @Test
    public void sibling_uncorrelated_subqueries_are_alpha_equivalent() {
        final var query = select(ORG1).where()
                .exists(select(ORG2).where().prop("parent").isNotNull().model())
                .and()
                .exists(select(ORG2).where().prop("parent").isNotNull().model())
                .model();
        final var subqueries = collectSubQueryForExists(qry(query));
        assertEquals(2, subqueries.size());
        assertNotSame(subqueries.get(0), subqueries.get(1));
        assertNotStructEq(subqueries.get(0), subqueries.get(1));
        assertAlphaEq(subqueries.get(0), subqueries.get(1));
    }

    /// Two identical correlated subqueries, side by side, are alpha-equivalent: each binds its own `ORG2` (distinct
    /// IDs, matched via the bijection) while both reference the one enclosing `ORG1` (a free reference, matched by
    /// identity).
    ///
    @Test
    public void sibling_correlated_subqueries_are_alpha_equivalent() {
        final var query = select(ORG1).where()
                .exists(select(ORG2).where().prop("parent").eq().extProp("id").model())
                .and().exists(select(ORG2).where().prop("parent").eq().extProp("id").model())
                .model();
        final var subqueries = collectSubQueryForExists(qry(query));
        assertEquals(2, subqueries.size());
        assertNotSame(subqueries.get(0), subqueries.get(1));
        assertNotStructEq(subqueries.get(0), subqueries.get(1));
        assertAlphaEq(subqueries.get(0), subqueries.get(1));
    }

    // :::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    // : Fixed binding order.
    // : Within a level, sources are paired by position: the i-th source bound in one AST is taken to correspond to the
    // : i-th bound in the other.
    // : Test cases below nest a self-join inside an EXISTS subquery, which yields NULL rather than its sources: the two
    // : joined sources are then referenced only by the `on` condition, so they are free to permute.

    /// True positive: the renaming that aligns the two subqueries respects binding order.
    /// Each binds the referencing source first and the referenced source second (`on first.replacedBy = second.id`),
    /// so positional pairing -- `{first:first, second:second}` -- is exactly the aligning renaming.
    ///
    @Test
    public void self_joins_whose_renaming_respects_binding_order_are_alpha_equivalent() {
        final var query = select(ORG1).where()
                .exists(select(TgVehicle.class).as("a")
                                .join(TgVehicle.class).as("b").on().prop("a.replacedBy").eq().prop("b.id")
                                .model())
                .and()
                .exists(select(TgVehicle.class).as("p")
                                .join(TgVehicle.class).as("q").on().prop("p.replacedBy").eq().prop("q.id")
                                .model())
                .model();
        final var subqueries = collectSubQueryForExists(qry(query));
        assertEquals(2, subqueries.size());
        assertNotStructEq(subqueries.get(0), subqueries.get(1));
        assertAlphaEq(subqueries.get(0), subqueries.get(1));
    }

    /// Known limitation -- a false negative of fixed binding order.
    /// These two subqueries *are* alpha-equivalent: the first binds the referencing source first (`on a.replacedBy = b.id`),
    /// the second binds it second (`on q.replacedBy = p.id`), so the correct renaming permutes the level to `{a:q, b:p}`.
    /// Positional pairing instead commits to binding order -- `{a:p, b:q}` -- and so wrongly reports them as not
    /// alpha-equivalent.
    ///
    @Test
    public void self_joins_whose_renaming_permutes_sources_are_wrongly_rejected() {
        final var query = select(ORG1).where()
                .exists(select(TgVehicle.class).as("a")
                                .join(TgVehicle.class).as("b").on().prop("a.replacedBy").eq().prop("b.id")
                                .model())
                .and()
                .exists(select(TgVehicle.class).as("p")
                                .join(TgVehicle.class).as("q").on().prop("q.replacedBy").eq().prop("p.id")
                                .model())
                .model();
        final var subqueries = collectSubQueryForExists(qry(query));
        assertEquals(2, subqueries.size());
        assertNotAlphaEq(subqueries.get(0), subqueries.get(1));
    }

    // :::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    // : Negative -- alpha-equivalence must not over-match.

    /// Sanity: structurally different queries (a different property) are not alpha-equivalent.
    ///
    @Test
    public void queries_referencing_different_properties_are_not_alpha_equivalent() {
        final var q1 = select(TgVehicle.class).where().prop("initDate").isNotNull().model();
        final var q2 = select(TgVehicle.class).where().prop("station").isNotNull().model();
        assertNotAlphaEq(qry(q1), qry(q2));
    }

    /// The alpha-specific discriminator: the same shape, but the referenced source is bound at a *different level*.
    /// One subquery references an enclosing source (`extProp("id")`); the other references its own source (`prop("id")`).
    /// A correct level-aware comparison must reject this; a comparison that merely ignored IDs would wrongly accept it.
    ///
    @Test
    public void an_outer_reference_is_not_alpha_equivalent_to_an_inner_reference() {
        final var outerRef = select(ORG1).where()
                .exists(select(ORG2).where().prop("parent").eq().extProp("id").model())
                .model();
        final var innerRef = select(ORG1).where()
                .exists(select(ORG2).where().prop("parent").eq().prop("id").model())
                .model();
        assertNotAlphaEq(qry(outerRef), qry(innerRef));
    }

    // :::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    // : Helpers

    /// Collects every [SubQueryForExists3] node within `root` (included).
    ///
    private List<SubQueryForExists3> collectSubQueryForExists(final Object root) {
        final var collector = new AbstractCollectingVisitor<SubQueryForExists3>() {
            @Override
            public List<SubQueryForExists3> visit(final SubQueryForExists3 x, final SubQueryForExists3 y, final Void state) {
                return combine(ImmutableList.of(x), super.visit(x, y, state));
            }
        };
        return collector.collect(root);
    }

}
