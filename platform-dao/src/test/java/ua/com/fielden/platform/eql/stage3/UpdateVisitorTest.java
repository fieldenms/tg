package ua.com.fielden.platform.eql.stage3;

import org.junit.ComparisonFailure;
import org.junit.Test;
import ua.com.fielden.platform.entity.query.EntityAggregates;
import ua.com.fielden.platform.entity.query.fluent.ITypeCast;
import ua.com.fielden.platform.entity.query.fluent.LikeOptions;
import ua.com.fielden.platform.entity.query.fluent.Limit;
import ua.com.fielden.platform.eql.exceptions.EqlStage3ProcessingException;
import ua.com.fielden.platform.eql.meta.EqlStage3TestCase;
import ua.com.fielden.platform.eql.meta.PropType;
import ua.com.fielden.platform.eql.stage3.conditions.*;
import ua.com.fielden.platform.eql.stage3.operands.*;
import ua.com.fielden.platform.eql.stage3.operands.functions.*;
import ua.com.fielden.platform.eql.stage3.queries.ResultQuery3;
import ua.com.fielden.platform.eql.stage3.queries.SourceQuery3;
import ua.com.fielden.platform.eql.stage3.queries.SubQuery3;
import ua.com.fielden.platform.eql.stage3.queries.SubQueryForExists3;
import ua.com.fielden.platform.eql.stage3.sources.*;
import ua.com.fielden.platform.eql.stage3.sundries.*;

import java.util.*;

import static java.util.Collections.newSetFromMap;
import static org.junit.Assert.*;
import static ua.com.fielden.platform.entity.query.fluent.enums.ArithmeticalOperator.ADD;
import static ua.com.fielden.platform.entity.query.fluent.enums.ArithmeticalOperator.MULT;
import static ua.com.fielden.platform.entity.query.fluent.enums.ComparisonOperator.EQ;
import static ua.com.fielden.platform.entity.query.fluent.enums.ComparisonOperator.GT;
import static ua.com.fielden.platform.entity.query.fluent.enums.DateIntervalUnit.DAY;
import static ua.com.fielden.platform.entity.query.fluent.enums.DateIntervalUnit.MONTH;
import static ua.com.fielden.platform.entity.query.fluent.enums.JoinType.IJ;
import static ua.com.fielden.platform.entity.query.fluent.enums.JoinType.LJ;
import static ua.com.fielden.platform.entity.query.fluent.enums.Quantifier.ANY;
import static ua.com.fielden.platform.eql.meta.PropType.*;
import static ua.com.fielden.platform.types.tuples.T2.t2;

/// Tests for [UpdateVisitor].
///
/// ## What is covered
///
/// 1. The [UpdateVisitor.Action] rules -- [UpdateVisitor.Action#descend], [UpdateVisitor.Action#update], [UpdateVisitor.Action#stop],
///    and their composition (updating the result of descending).
/// 2. Persistence: an update rebuilds only the branch leading from the root to the updated node, sharing everything else.
/// 3. Reconstruction: a rebuilt node reproduces all of its own data -- types, flags, operators, aliases, limits -- and
///    keeps its children in their positions.
/// 4. Traversal: every node is visited, exactly once, in every child position of every node type.
///
/// ## Strategy
///
/// *Traversal* is checked against an independent implementation.
/// [Operations#collectNodes] walks a tree through the binary [AbstractSameShapeVisitor], while [UpdateVisitor] walks it
/// through the unary [AbstractVisitor], so the two must visit the same set of nodes.
///
/// *Reconstruction* is checked by replacing a property with a structurally equal copy of itself.
/// The result must then be structurally equal to the original tree while being a different object, which forces every
/// node on the rebuilt branch to reproduce its own data: drop a `distinct` flag, an interval unit or an offset, and
/// structural equivalence fails.
/// Applied to every property of an AST in turn, this covers each child position of each node type along the way.
/// Properties are named after the position they occupy, so a failure identifies the culprit.
/// Data that structural equivalence deliberately ignores -- [Yield3#column] and a source's columns -- is asserted separately.
///
/// The ASTs are built from node constructors rather than compiled from EQL, because most node types cannot be produced
/// on demand by the compiler.
/// They are deliberately not required to make sense as queries: stage 3 does not validate types, and traversal is
/// indifferent to meaning.
///
public class UpdateVisitorTest extends EqlStage3TestCase {

    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    // : The Action protocol

    @Test
    public void a_traversal_that_updates_nothing_returns_the_original_tree() {
        final var root = query();
        assertSame(root, operations().update(root, (x, k) -> k.descend()));
    }

    @Test
    public void every_node_is_visited_exactly_once() {
        final var root = query();
        final var visited = new ArrayList<INode3>();
        operations().update(root, (x, k) -> {
            visited.add(x);
            return k.descend();
        });
        assertSameNodes(operations().collectNodes(root, _ -> true), visited);
    }

    @Test
    public void on_a_leaf_both_descending_and_stopping_return_the_leaf() {
        final var leaf = prop3("leaf", mkSource());
        assertSame(leaf, operations().update(leaf, (x, k) -> k.descend()));
        assertSame(leaf, operations().update(leaf, (x, k) -> k.stop()));
        // A leaf can still be updated.
        final var replacement = new Value3(1, INTEGER_PROP_TYPE);
        assertSame(replacement, operations().update(leaf, (x, k) -> k.update(replacement)));
    }

    @Test
    public void stopping_does_not_descend_into_child_nodes() {
        final var subQuery = subQuery("insideTheSubQuery");
        final var prop = prop3("outsideTheSubQuery", mkSource());
        final var root = new Yields3(List.of(mkYield(prop, "y1", nextSqlId()), mkYield(subQuery, "y2", nextSqlId())));
        final var visited = new ArrayList<INode3>();
        final var result = operations().update(root, (x, k) -> {
            visited.add(x);
            return x instanceof SubQuery3 ? k.stop() : k.descend();
        });
        assertSame("Nothing was updated, so the original tree must be returned.", root, result);
        assertTrue("The stopped node itself is visited.", containsByIdentity(visited, subQuery));
        for (final var node : operations().collectNodes(subQuery, x -> x != subQuery)) {
            assertFalse("A node below the stopped one must not be visited: %s".formatted(node),
                        containsByIdentity(visited, node));
        }
        assertTrue("A node outside the stopped branch must still be visited.", containsByIdentity(visited, prop));
    }

    @Test
    public void an_update_is_not_traversed() {
        final var src = mkSource();
        final var target = prop3("target", src);
        final var insideTheReplacement = prop3("insideTheReplacement", src);
        final var replacement = new SumOf3(insideTheReplacement, false, BIGDECIMAL_PROP_TYPE);
        final var root = new MaxOf3(target, LONG_PROP_TYPE);
        final var visited = new ArrayList<INode3>();
        final var result = operations().update(root, (x, k) -> {
            visited.add(x);
            return x == target ? k.update(replacement) : k.descend();
        });
        assertSame(replacement, ((MaxOf3) result).operand);
        assertFalse("The replacement must not be visited.", containsByIdentity(visited, replacement));
        assertFalse("A child of the replacement must not be visited.", containsByIdentity(visited, insideTheReplacement));
        assertEquals("Only the root and the replaced node are visited.", identitySet(List.of(root, target)), identitySet(visited));
    }

    @Test
    public void returning_a_node_without_invoking_the_action_does_not_descend() {
        final var src = mkSource();
        final var prop = prop3("id", src);
        final var root = new MaxOf3(prop, LONG_PROP_TYPE);
        final var visited = new ArrayList<INode3>();
        final var result = operations().update(root, (x, k) -> {
            visited.add(x);
            return x;
        });
        assertSame("Returning the visited node is equivalent to stopping.", root, result);
        assertEquals(1, visited.size());
        assertSame(root, visited.getFirst());
    }

    @Test
    public void the_result_of_descending_can_be_updated_which_rewrites_bottom_up() {
        final var src = mkSource();
        final var prop = prop3("sumOfArgument", src, BIGDECIMAL_PROP_TYPE);
        final var replacement = prop3("materialisedColumn", src, BIGDECIMAL_PROP_TYPE);
        final var zero = new Value3(0, BIGDECIMAL_PROP_TYPE);
        final var root = new SumOf3(prop, true, BIGDECIMAL_PROP_TYPE);
        final var result = operations().update(root, (x, k) -> {
            if (x == prop) {
                return k.update(replacement);
            } else if (x instanceof SumOf3) {
                // The children are updated first, then the updated node is wrapped.
                final var newSum = (SumOf3) k.descend();
                return k.update(new IfNull3(newSum, zero, newSum.type));
            } else {
                return k.descend();
            }
        });
        final var ifNull = (IfNull3) result;
        final var sumOf = (SumOf3) ifNull.operand1;
        assertNotSame("The function must have been reconstructed with the updated operand.", root, sumOf);
        assertSame("The update of a child must survive the update of its parent.", replacement, sumOf.operand);
        assertTrue("Descending must preserve the data of the reconstructed node.", sumOf.distinct);
        assertSame(zero, ifNull.operand2);
    }

    @Test
    public void only_the_branch_leading_to_an_updated_node_is_rebuilt() {
        final var root = query();
        final var target = (Prop3) root.groups.groups().getFirst().operand();
        final var result = (ResultQuery3) assertUpdatedPreservingStructure(root, target, copyOf(target));
        assertNotSame("The group-bys hold the update, so they must be rebuilt.", root.groups, result.groups);
        assertSame("The join tree is off the updated branch.", root.maybeJoinRoot.get(), result.maybeJoinRoot.get());
        assertSame("The conditions are off the updated branch.", root.whereConditions, result.whereConditions);
        assertSame("The yields are off the updated branch.", root.yields, result.yields);
        assertSame("The orderings are off the updated branch.", root.orderings, result.orderings);
        assertSame("The untouched group-by is shared.", root.groups.groups().get(1), result.groups.groups().get(1));
        assertEquals(root.resultType, result.resultType);
    }

    @Test
    public void a_node_may_be_updated_with_a_node_of_a_different_kind() {
        final var src = mkSource();
        final var prop = prop3("operand", src, INTEGER_PROP_TYPE);
        // A leaf replaced by another leaf of a different kind.
        final var value = new Value3(42, INTEGER_PROP_TYPE);
        final var withProp = new IfNull3(prop, new Value3(0, INTEGER_PROP_TYPE), INTEGER_PROP_TYPE);
        assertSame(value, ((IfNull3) replace(withProp, prop, value)).operand1);
        // A composite replaced by a leaf.
        final var subQuery = subQuery("insideTheSubQuery");
        final var withSubQuery = new IfNull3(subQuery, new Value3(0, LONG_PROP_TYPE), LONG_PROP_TYPE);
        assertSame(prop, ((IfNull3) replace(withSubQuery, subQuery, prop)).operand1);
    }

    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    // : Reconstruction preserves node data.
    // : Each test replaces every property of an AST, one at a time, by a structurally equal copy of itself.

    @Test
    public void reconstruction_of_single_operand_functions_preserves_their_data() {
        assertEachPropUpdatedPreservingStructure(singleOperandFunctions(prop3("functionArgument", mkSource(), STRING_PROP_TYPE)));
    }

    @Test
    public void reconstruction_of_two_operand_functions_preserves_their_data() {
        assertEachPropUpdatedPreservingStructure(twoOperandFunctions(mkSource()));
    }

    @Test
    public void reconstruction_of_expressions_preserves_their_data() {
        assertEachPropUpdatedPreservingStructure(expressions(mkSource()));
    }

    @Test
    public void reconstruction_of_case_when_preserves_its_data() {
        assertEachPropUpdatedPreservingStructure(caseWhen(mkSource()));
    }

    @Test
    public void reconstruction_of_conditions_preserves_their_data() {
        assertEachPropUpdatedPreservingStructure(conditions(mkSource()));
    }

    @Test
    public void reconstruction_of_sundries_preserves_their_data() {
        final var src = mkSource();
        assertEachPropUpdatedPreservingStructure(new Yields3(List.of(
                mkYield(prop3("firstYield", src), "y1", nextSqlId()),
                mkYield(prop3("secondYield", src), "y2", nextSqlId()))));
        assertEachPropUpdatedPreservingStructure(new GroupBys3(List.of(
                new GroupBy3(prop3("firstGroup", src)),
                new GroupBy3(prop3("secondGroup", src)))));
        assertEachPropUpdatedPreservingStructure(orderBys(src));
    }

    @Test
    public void reconstruction_of_a_query_preserves_all_of_its_components() {
        final var src = mkSource();
        final var joined = mkSource();
        final var qry = new ResultQuery3(new QueryComponents3(
                Optional.of(new JoinInnerNode3(new JoinLeafNode3(src), new JoinLeafNode3(joined), IJ,
                                               cond(eq(prop3("joinLeft", src), prop3("joinRight", joined))))),
                cond(isNotNull(prop3("whereOperand", src))),
                new Yields3(List.of(mkYield(prop3("yieldOperand", src), "y1", nextSqlId()))),
                new GroupBys3(List.of(new GroupBy3(prop3("groupOperand", src)))),
                new OrderBys3(List.of(new IOrderBy3.Operand(prop3("orderOperand", src), true)), Limit.count(3), 7)),
                EntityAggregates.class);
        assertEachPropUpdatedPreservingStructure(qry);
    }

    @Test
    public void reconstruction_of_join_nodes_and_sources_preserves_their_data() {
        assertEachPropUpdatedPreservingStructure(joins(mkSource()));
    }

    @Test
    public void reconstruction_of_an_ast_containing_every_node_type_preserves_all_data() {
        assertEachPropUpdatedPreservingStructure(query());
    }

    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    // : Data that structural equivalence ignores, hence is asserted directly.

    @Test
    public void reconstruction_preserves_the_column_of_a_yield() {
        final var prop = prop3("yieldOperand", mkSource());
        final var yield = new Yield3(prop, "alias", "C_42", LONG_PROP_TYPE);
        final var result = (Yields3) replace(new Yields3(List.of(yield)), prop, copyOf(prop));
        final var updated = result.yieldsMap().get("alias");
        assertNotSame("The yield must have been reconstructed.", yield, updated);
        assertEquals(yield.column(), updated.column());
        assertEquals(yield.alias(), updated.alias());
        assertEquals(yield.type(), updated.type());
    }

    @Test
    public void a_query_based_source_keeps_its_identity_and_re_derives_its_columns() {
        final var yield = new Yield3(prop3("qty", mkSource(), INTEGER_PROP_TYPE), "qty", "C_7", INTEGER_PROP_TYPE);
        final var source = source(nextSqlId(), sourceQuery(yield));
        assertEquals(source.sqlAlias + ".C_7", source.column("qty"));
        // Renaming the yield of the underlying query must be reflected in the columns of the source.
        final var renamed = new Yield3(yield.operand(), "quantity", yield.column(), yield.type());
        final var result = (Source3BasedOnQueries) replace(source, yield, renamed);
        assertNotSame("The source must have been reconstructed.", source, result);
        assertEquals("The source identifier must be preserved.", source.id(), result.id());
        assertEquals("The SQL alias must be preserved.", source.sqlAlias, result.sqlAlias);
        assertEquals("Columns must be re-derived from the updated yields.", result.sqlAlias + ".C_7", result.column("quantity"));
        assertThrows(EqlStage3ProcessingException.class, () -> result.column("qty"));
    }

    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    // : Edge cases

    /// Empty child collections and an absent join root must not be mistaken for anything else.
    /// [UpdateVisitor] has no identity element to fold them into -- [UpdateVisitor#identity] throws -- so a facade that
    /// fell back to the inherited traversal would fail here rather than silently.
    ///
    @Test
    public void a_query_with_empty_components_is_traversed_and_returned_unchanged() {
        final var empty = new ResultQuery3(new QueryComponents3(Optional.empty(), Conditions3.empty(), Yields3.empty(), GroupBys3.empty(), OrderBys3.empty()),
                                           EntityAggregates.class);
        assertSame(empty, operations().update(empty, (x, k) -> k.descend()));
        // The same, but with something to update and still no join root.
        final var prop = prop3("yieldOperand", mkSource());
        final var qry = new ResultQuery3(new QueryComponents3(Optional.empty(),
                                                              Conditions3.empty(),
                                                              new Yields3(List.of(mkYield(prop, "y1", nextSqlId()))),
                                                              GroupBys3.empty(),
                                                              OrderBys3.empty()),
                                         EntityAggregates.class);
        final var result = (ResultQuery3) assertUpdatedPreservingStructure(qry, prop, copyOf(prop));
        assertTrue("An absent join root must stay absent.", result.maybeJoinRoot.isEmpty());
    }

    /// The visitor identifies nodes by reference, so a node that is structurally equal to the updated one is left alone.
    ///
    @Test
    public void a_structurally_equal_sibling_of_an_updated_node_is_left_untouched() {
        final var src = mkSource();
        final var target = prop3("p", src);
        final var twin = copyOf(target);
        final var qry = new ResultQuery3(
                new QueryComponents3(Optional.of(new JoinLeafNode3(src)), Conditions3.empty(),
                                     new Yields3(List.of(mkYield(new SumOf3(target, true, BIGDECIMAL_PROP_TYPE), "y1", nextSqlId()),
                                                         mkYield(new SumOf3(twin, true, BIGDECIMAL_PROP_TYPE), "y2", nextSqlId()))),
                                     GroupBys3.empty(), OrderBys3.empty()),
                EntityAggregates.class);
        assertTrue("Precondition: the two properties are structurally equal.", operations().structEq(target, twin));
        final var replacement = prop3("materialisedColumn", src);
        final var result = (ResultQuery3) replace(qry, target, replacement);
        assertEquals(1, occurrences(result, replacement));
        assertEquals(0, occurrences(result, target));
        assertEquals("The twin must survive.", 1, occurrences(result, twin));
        assertSame("The yield holding the twin is off the updated branch, hence shared.",
                   qry.yields.yieldsMap().get("y2"), result.yields.yieldsMap().get("y2"));
    }

    @Test
    public void updates_in_different_branches_are_all_applied() {
        final var src = mkSource();
        final var first = prop3("firstTarget", src);
        final var second = prop3("secondTarget", src);
        final var untouched = mkYield(prop3("untouched", src), "y3", nextSqlId());
        final var qry = new ResultQuery3(
                new QueryComponents3(Optional.of(new JoinLeafNode3(src)), Conditions3.empty(),
                                     new Yields3(List.of(mkYield(new MaxOf3(first, LONG_PROP_TYPE), "y1", nextSqlId()),
                                                         mkYield(new MinOf3(second, LONG_PROP_TYPE), "y2", nextSqlId()),
                                                         untouched)),
                                     GroupBys3.empty(), OrderBys3.empty()),
                EntityAggregates.class);
        final var firstReplacement = prop3("firstColumn", src);
        final var secondReplacement = prop3("secondColumn", src);
        final var result = (ResultQuery3) operations().update(qry, (x, k) -> {
            if (x == first) {
                return k.update(firstReplacement);
            } else if (x == second) {
                return k.update(secondReplacement);
            } else {
                return k.descend();
            }
        });
        assertEquals(0, occurrences(result, first));
        assertEquals(1, occurrences(result, firstReplacement));
        assertEquals(0, occurrences(result, second));
        assertEquals(1, occurrences(result, secondReplacement));
        assertSame("The branch without an update is shared.", untouched, result.yields.yieldsMap().get("y3"));
    }

    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    // : Assertions

    /// Updates `oldNode` to `newNode` within `root`, descending everywhere else.
    ///
    private static INode3 replace(final INode3 root, final INode3 oldNode, final INode3 newNode) {
        return operations().update(root, (x, k) -> x == oldNode ? k.update(newNode) : k.descend());
    }

    /// The number of nodes in the tree rooted at `root` that are `node` itself.
    ///
    private static int occurrences(final INode3 root, final INode3 node) {
        return operations().collectNodes(root, x -> x == node).size();
    }

    /// Asserts that updating `oldNode` to `newNode` within `root` puts the replacement in place of the replaced node,
    /// and that the branch leading to it is rebuilt.
    ///
    private static INode3 assertUpdated(final INode3 root, final INode3 oldNode, final INode3 newNode) {
        assertEquals("Precondition: the replaced node must occur in the tree exactly once.", 1, occurrences(root, oldNode));
        final var result = replace(root, oldNode, newNode);
        assertNotSame("The branch leading to the update must be rebuilt, hence a new root.", root, result);
        assertEquals("The replaced node must be gone.", 0, occurrences(result, oldNode));
        assertEquals("The replacement must take its place.", 1, occurrences(result, newNode));
        return result;
    }

    /// As [#assertUpdated], additionally asserting that the tree is structurally unchanged.
    /// This holds iff `newNode` is structurally equal to `oldNode` *and* every reconstructed node on the way reproduced
    /// all of its own data.
    ///
    private static INode3 assertUpdatedPreservingStructure(final INode3 root, final INode3 oldNode, final INode3 newNode) {
        assertTrue("Precondition: the replacement must be structurally equal to the replaced node.",
                   operations().structEq(oldNode, newNode));
        final var result = assertUpdated(root, oldNode, newNode);
        if (!operations().structEq(root, result)) {
            throw new ComparisonFailure("The tree must be structurally unchanged after updating [%s].".formatted(oldNode),
                                        root.toString(), result.toString());
        }
        return result;
    }

    /// Replaces each property of `root`, one at a time, by a structurally equal copy of itself, asserting that the tree
    /// comes back structurally unchanged.
    ///
    private static void assertEachPropUpdatedPreservingStructure(final INode3 root) {
        final var props = operations().collectNodesOfType(root, Prop3.class);
        assertFalse("The AST must contain properties to update.", props.isEmpty());
        for (final var prop : props) {
            assertUpdatedPreservingStructure(root, prop, copyOf(prop));
        }
    }

    /// Asserts that both lists hold the same nodes, by reference and without duplicates.
    ///
    private static void assertSameNodes(final List<INode3> expected, final List<INode3> actual) {
        final Set<INode3> expectedSet = identitySet(expected);
        final Set<INode3> actualSet = identitySet(actual);
        assertEquals("A node must not be visited more than once.", actual.size(), actualSet.size());
        for (final var node : expectedSet) {
            assertTrue("Node was not visited: %s".formatted(node), actualSet.contains(node));
        }
        for (final var node : actualSet) {
            assertTrue("Node does not belong to the tree: %s".formatted(node), expectedSet.contains(node));
        }
    }

    /// Nodes have no hash code, so identity is the only usable basis for a set of them.
    ///
    private static Set<INode3> identitySet(final List<INode3> nodes) {
        final Set<INode3> set = newSetFromMap(new IdentityHashMap<>());
        set.addAll(nodes);
        return set;
    }

    private static boolean containsByIdentity(final List<INode3> nodes, final INode3 node) {
        return nodes.stream().anyMatch(x -> x == node);
    }

    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    // : AST builders

    /// A table source with fresh identifiers.
    ///
    private static Source3BasedOnTable mkSource() {
        return source(VEHICLE, nextSqlId());
    }

    /// A property node.
    /// Its column is arbitrary, as these tests generate no SQL; `name` records the position that the property occupies,
    /// so that a failure points at the node type that mishandled it.
    ///
    private static Prop3 prop3(final String name, final ISource3 source, final PropType type) {
        return new Prop3(name, source.id(), name.toUpperCase() + "_", type);
    }

    private static Prop3 prop3(final String name, final ISource3 source) {
        return prop3(name, source, LONG_PROP_TYPE);
    }

    /// A structurally equal copy of `prop`, which is a distinct object.
    ///
    private static Prop3 copyOf(final Prop3 prop) {
        return new Prop3(prop.name, prop.sourceId, prop.column, prop.type);
    }

    /// A chain of every single-operand function.
    /// `distinct` is set on the three functions that carry it, so that a reconstruction dropping it is detected.
    ///
    private static ISingleOperand3 singleOperandFunctions(final ISingleOperand3 operand) {
        var x = operand;
        x = new UpperCaseOf3(x, STRING_PROP_TYPE);
        x = new LowerCaseOf3(x, STRING_PROP_TYPE);
        x = new DateOf3(x, DATE_PROP_TYPE);
        x = new DayOf3(x, INTEGER_PROP_TYPE);
        x = new DayOfWeekOf3(x, INTEGER_PROP_TYPE);
        x = new MonthOf3(x, INTEGER_PROP_TYPE);
        x = new YearOf3(x, INTEGER_PROP_TYPE);
        x = new HourOf3(x, INTEGER_PROP_TYPE);
        x = new MinuteOf3(x, INTEGER_PROP_TYPE);
        x = new SecondOf3(x, INTEGER_PROP_TYPE);
        x = new AbsOf3(x, INTEGER_PROP_TYPE);
        x = new Ceil3(x, INTEGER_PROP_TYPE);
        x = new Floor3(x, INTEGER_PROP_TYPE);
        x = new MaxOf3(x, INTEGER_PROP_TYPE);
        x = new MinOf3(x, INTEGER_PROP_TYPE);
        x = new AverageOf3(x, true, BIGDECIMAL_PROP_TYPE);
        x = new CountOf3(x, true, INTEGER_PROP_TYPE);
        x = new SumOf3(x, true, BIGDECIMAL_PROP_TYPE);
        return x;
    }

    /// Every two-operand function, with a distinct property in each operand position, so that a reconstruction that
    /// swaps operands is detected.
    ///
    private static ISingleOperand3 twoOperandFunctions(final ISource3 src) {
        return new IfNull3(
                new RoundTo3(prop3("roundToLeft", src), prop3("roundToRight", src), BIGDECIMAL_PROP_TYPE),
                new AddDateInterval3(prop3("intervalToAdd", src), DAY,
                                     new CountDateInterval3(MONTH,
                                                            prop3("periodEnd", src, DATETIME_PROP_TYPE),
                                                            prop3("periodStart", src, DATETIME_PROP_TYPE),
                                                            INTEGER_PROP_TYPE),
                                     DATETIME_PROP_TYPE),
                BIGDECIMAL_PROP_TYPE);
    }

    /// An [Expression3] over [CompoundSingleOperand3] items, one of which is a [Concat3] and the other a [ConcatOf3]
    /// with order items.
    ///
    private static ISingleOperand3 expressions(final ISource3 src) {
        final var concat = new Concat3(List.of(prop3("concatFirst", src, STRING_PROP_TYPE),
                                               prop3("concatSecond", src, STRING_PROP_TYPE),
                                               new Value3("-", STRING_PROP_TYPE)),
                                       STRING_PROP_TYPE);
        final var concatOf = new ConcatOf3(prop3("concatOfOperand", src, STRING_PROP_TYPE),
                                           new Value3(",", STRING_PROP_TYPE),
                                           STRING_PROP_TYPE,
                                           List.of(new IOrderBy3.Operand(prop3("concatOfOrderItem", src), true)));
        return new Expression3(prop3("expressionFirstOperand", src, STRING_PROP_TYPE),
                               List.of(new CompoundSingleOperand3(concat, ADD),
                                       new CompoundSingleOperand3(concatOf, MULT)),
                               STRING_PROP_TYPE);
    }

    /// A [CaseWhen3] with a type cast, an `else` operand, and properties inside its `when` conditions.
    ///
    private static CaseWhen3 caseWhen(final ISource3 src) {
        return new CaseWhen3(
                List.of(t2(cond(isNotNull(prop3("firstWhenOperand", src))), prop3("firstThenOperand", src)),
                        t2(cond(gt(prop3("secondWhenLeft", src), prop3("secondWhenRight", src))), prop3("secondThenOperand", src))),
                prop3("elseOperand", src),
                ITypeCast.AsInteger.AS_INTEGER,
                INTEGER_PROP_TYPE);
    }

    /// A negated [Conditions3] whose disjunctive normal form has two conjunctions, together covering every predicate type.
    ///
    private static Conditions3 conditions(final ISource3 src) {
        return new Conditions3(true, List.<List<? extends ICondition3>>of(
                List.of(new ComparisonPredicate3(prop3("comparisonLeft", src), GT, prop3("comparisonRight", src)),
                        new NullPredicate3(prop3("nullPredicateOperand", src), true),
                        new LikePredicate3(prop3("likeMatchOperand", src, STRING_PROP_TYPE),
                                           new Value3("%a%", STRING_PROP_TYPE),
                                           LikeOptions.options().negated().caseInsensitive().build())),
                List.of(new SetPredicate3(prop3("setPredicateLeft", src), true,
                                          new OperandsBasedSet3(List.of(prop3("setElement", src), new Value3(1, LONG_PROP_TYPE)))),
                        new SetPredicate3(prop3("querySetPredicateLeft", src), false,
                                          new QueryBasedSet3(subQuery("insideTheQueryBasedSet"))),
                        new ExistencePredicate3(true, subQueryForExists("insideTheExistsSubQuery")),
                        new QuantifiedPredicate3(prop3("quantifiedLeft", src), EQ, ANY, subQuery("insideTheQuantifiedSubQuery")))));
    }

    /// A join tree with both join types, over table sources and a query-based source.
    ///
    private static IJoinNode3 joins(final ISource3 main) {
        final var joined = mkSource();
        final var union = source(nextSqlId(),
                                 sourceQuery(new Yield3(prop3("firstUnionYield", mkSource(), LONG_PROP_TYPE), "u", "C_1", LONG_PROP_TYPE)),
                                 sourceQuery(new Yield3(prop3("secondUnionYield", mkSource(), LONG_PROP_TYPE), "u", "C_1", LONG_PROP_TYPE)));
        return new JoinInnerNode3(
                new JoinLeafNode3(main),
                new JoinInnerNode3(new JoinLeafNode3(joined), new JoinLeafNode3(union), LJ,
                                   cond(eq(prop3("joinedId", joined), prop3("unionRef", union)))),
                IJ,
                cond(eq(prop3("mainId", main), prop3("joinedRef", joined))));
    }

    private static OrderBys3 orderBys(final ISource3 src) {
        return new OrderBys3(List.of(new IOrderBy3.Operand(prop3("firstOrderOperand", src), true),
                                     new IOrderBy3.Operand(prop3("secondOrderOperand", src), false),
                                     new IOrderBy3.Yield("y1", "C_1", true)),
                             Limit.count(10), 5);
    }

    private static SourceQuery3 sourceQuery(final Yield3 yield) {
        return new SourceQuery3(new QueryComponents3(Optional.of(new JoinLeafNode3(mkSource())), Conditions3.empty(),
                                                    new Yields3(List.of(yield)), GroupBys3.empty(), OrderBys3.empty()),
                                EntityAggregates.class);
    }

    private static SubQuery3 subQuery(final String propName) {
        final var src = mkSource();
        return new SubQuery3(new QueryComponents3(Optional.of(new JoinLeafNode3(src)), Conditions3.empty(),
                                                 new Yields3(List.of(mkYield(prop3(propName, src), "sq", nextSqlId()))),
                                                 GroupBys3.empty(), OrderBys3.empty()),
                             LONG_PROP_TYPE);
    }

    private static SubQueryForExists3 subQueryForExists(final String propName) {
        final var src = mkSource();
        return new SubQueryForExists3(new QueryComponents3(Optional.of(new JoinLeafNode3(src)),
                                                          cond(isNotNull(prop3(propName, src))),
                                                          Yields3.empty(), GroupBys3.empty(), OrderBys3.empty()));
    }

    /// A query that contains every stage-3 node type.
    ///
    private static ResultQuery3 query() {
        final var src = mkSource();
        return new ResultQuery3(
                new QueryComponents3(
                        Optional.of(joins(src)),
                        conditions(src),
                        new Yields3(List.of(
                                mkYield(singleOperandFunctions(prop3("functionArgument", src, STRING_PROP_TYPE)), "y1", nextSqlId()),
                                mkYield(twoOperandFunctions(src), "y2", nextSqlId()),
                                mkYield(expressions(src), "y3", nextSqlId()),
                                mkYield(caseWhen(src), "y4", nextSqlId()),
                                mkYield(subQuery("insideTheYieldedSubQuery"), "y5", nextSqlId()),
                                new Yield3(new CountAll3(), "y6", nextSqlId(), INTEGER_PROP_TYPE))),
                        new GroupBys3(List.of(new GroupBy3(prop3("firstGroup", src)), new GroupBy3(prop3("secondGroup", src)))),
                        orderBys(src)),
                EntityAggregates.class);
    }

}
