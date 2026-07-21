package ua.com.fielden.platform.eql.stage3;

import com.google.common.collect.ImmutableList;
import jakarta.inject.Inject;
import ua.com.fielden.platform.entity.exceptions.InvalidStateException;
import ua.com.fielden.platform.eql.stage3.operands.Prop3;
import ua.com.fielden.platform.eql.stage3.queries.AbstractQuery3;
import ua.com.fielden.platform.eql.stage3.sources.*;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static ua.com.fielden.platform.utils.CollectionUtil.append;
import static ua.com.fielden.platform.utils.CollectionUtil.dropRight;

/// Decides *alpha-equivalence* of two stage-3 ASTs: structural equivalence modulo a consistent renaming of source
/// identifiers.
///
/// A source identifier ([ISource3#id()]) is the EQL analogue of a variable name bound in a function.
/// It is *introduced* (bound) by the query that brings the source into scope -- the `select` of a query --
/// and *referenced* elsewhere, chiefly through `prop` ([Prop3#source]).
///
/// Two ASTs are alpha-equivalent if they become structurally equal once every bound source ID in one AST is consistently
/// renamed to the corresponding ID in the other AST.
/// This notion of equality is strictly weaker than structural equivalence: a renaming is alpha-equivalent to the original
/// but not structurally equal to it.
///
/// This visitor extends [AbstractStructuralEquivalenceVisitor] and overrides only the parts that concern source IDs;
/// everything else -- structure, and data such as types, operators and flags -- is compared exactly as by structural
/// equivalence.
///
/// Three kinds of node are overridden:
/// - the *reference*: the [Prop3] overload compares the referenced source under the renaming rather than by ID directly;
/// - the *binding occurrences*: the [Source3BasedOnTable] and [Source3BasedOnQueries] overloads ignore the source's
///   own ID (and the SQL alias, which is derived from it);
/// - the *binders*: [#visitQueryComponents] and the [JoinInnerNode3] overload record the sources they introduce into
///   the traversal [State] before descending into the components that may reference them (a query's `where`, yields,
///   groups and orderings; a join's `on` conditions).
///
/// ## The renaming as a scoped correspondence
///
/// [State] carries, for each of the two ASTs, a stack of *levels* -- one level per enclosing binding scope, innermost
/// last -- where each level lists the IDs bound in that scope, in binding order.
/// A reference in the first AST corresponds to one in the second when both resolve to bindings at the same position
/// within the same level.
/// See [#alphaEquivalent_] for the resolution rule.
///
/// ## Known limitations
///
/// A level pairs sources *by position*: the source bound `i`-th in one AST is taken to correspond to the source bound
/// `i`-th in the other.
/// This is a genuine, consistent bijection, but it is a *fixed* one -- binding order -- rather than any renaming that
/// would make the two ASTs structurally equal.
/// So when the correct renaming permutes the sources bound at a level, an alpha-equivalent pair is wrongly rejected (a false negative).
///
/// ```
/// -- level = [a, b]
/// select V as a join V as b on a.replacedBy = b.id where a.key = 'Z'
///
/// -- level = [p, q]
/// select V as p join V as q on q.replacedBy = p.id where q.key = 'Z'
///
/// -- index-based, incorrect renaming = {a:p, b:q}
/// -- correct renaming                = {a:q, b:p}
/// ```
///
/// A faithful implementation needs a partial bijection (a bimap) grown from the references as they are matched, rather
/// than one fixed up front by binding order.
///
/// Separately, yield aliases are still compared as data (inherited from structural equivalence) even though they are
/// arguably renameable too.
///
public class AlphaEquivalenceVisitor extends AbstractStructuralEquivalenceVisitor<AlphaEquivalenceVisitor.State> {

    // TODO Yield aliases should be renameable.

    // TODO Make protected once EQL tests are refactored using IoC.
    @Inject
    public AlphaEquivalenceVisitor() {}

    /// Entry point for a top-level comparison.
    ///
    public Boolean visit(final Object x, final Object y) {
        return x == y || visit(x, y, emptyState());
    }

    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    // : State and operations on it

    /// The source IDs in scope while traversing a pair of ASTs, held as two parallel stacks -- one per AST.
    /// Each stack is a list of *levels*, one per enclosing binding scope with the innermost scope last; each level
    /// lists the source IDs bound at that scope, in binding order.
    /// Levels are pushed by the binders ([#visitQueryComponents] and the [JoinInnerNode3] overload) as traversal
    /// descends, and are consulted by [#alphaEquivalent_] when a reference is compared.
    ///
    /// @param xBoundIds
    ///     Source IDs bound at each level for the first AST.
    ///     Each nested list corresponds to one level and holds, in binding order, the IDs bound at that level.
    ///     * `select(S1)` results in `[id1]`.
    ///     * `select(S1).join(S2)` results in `[id1, id2]`.
    ///     * `select(S2).join(S1)` results in `[id2, id1]`.
    /// @param yBoundIds
    ///     Source IDs bound at each level for the second AST.
    ///
    record State (List<List<Integer>> xBoundIds, List<List<Integer>> yBoundIds) {}

    /// The initial state for a top-level comparison: nothing is in scope yet.
    ///
    static State emptyState() {
        return new State(ImmutableList.of(), ImmutableList.of());
    }

    /// Pushes a new level holding the sources introduced by the join nodes `x` and `y` (one for each AST).
    ///
    private State addSources(final State state, final IJoinNode3 x, final IJoinNode3 y) {
        return addSources(state, streamSources(x), streamSources(y));
    }

    /// Pushes a new level holding the given sources (`xSources` for the first AST, `ySources` for the second).
    ///
    private State addSources(final State state, final Stream<ISource3> xSources, final Stream<ISource3> ySources) {
        return new State(append(state.xBoundIds(), xSources.map(ISource3::id).collect(toImmutableList())),
                         append(state.yBoundIds(), ySources.map(ISource3::id).collect(toImmutableList())));

    }

    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    // : Alpha-equivalence

    /// Whether source ID `x` (from the first AST) corresponds to source ID `y` (from the second) under `state`.
    ///
    private boolean alphaEquivalent(final Integer x, final Integer y, final State state) {
        return alphaEquivalent_(x, y, state.xBoundIds(), state.yBoundIds());
    }

    /// Resolves `x` and `y` against the two binding stacks (innermost level last) and returns true iff they correspond.
    ///
    /// Within a level, sources correspond *by position*: `x` and `y` correspond when they occur at the same index in
    /// their respective innermost levels.
    /// If neither is bound at the innermost level, resolution continues in the enclosing level.
    /// If exactly one is bound at a given level, the two are bound at different depths and do not correspond.
    /// Two references that reach the empty (outermost) scope are free and correspond only if they are the same ID.
    ///
    /// The stack-size guard is defensive: the two stacks are grown in lockstep, so in practice they always have equal
    /// depth by the time a reference is compared.
    ///
    private static boolean alphaEquivalent_(
            final Integer x,
            final Integer y,
            final List<List<Integer>> xBoundIds,
            final List<List<Integer>> yBoundIds)
    {
        if (xBoundIds.size() != yBoundIds.size()) {
            return false;
        }
        else if (xBoundIds.isEmpty()) {
            return x.equals(y);
        }
        final int xIndex = xBoundIds.getLast().indexOf(x);
        final int yIndex = yBoundIds.getLast().indexOf(y);
        if (xIndex >= 0 && yIndex >= 0) {
            // Both bound at the innermost level: they correspond iff they occur at the same position.
            return xIndex == yIndex;
        }
        else if (xIndex < 0 && yIndex < 0) {
            // Neither bound here: resolve against the enclosing level.
            return alphaEquivalent_(x, y, dropRight(xBoundIds, 1), dropRight(yBoundIds, 1));
        }
        else {
            // Exactly one bound here: bound at different depths, so they do not correspond.
            return false;
        }
    }

    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    // : Visitor methods

    @Override
    protected Boolean noMatch(final Object x, final Object y, final State state) {
        return false;
    }

    /// Never expected to be reached: this visitor overrides [Prop3] and the source nodes, and structural equivalence
    /// handles every other leaf, so a fall-through to the default signals an unhandled node type.
    ///
    @Override
    protected Boolean defaultValue(final Object x, final Object y, final State state) {
        throw new IllegalStateException("No default");
    }

    /// A property reference is equivalent when it has the same type and name and its source corresponds under the
    /// renaming.
    ///
    @Override
    public Boolean visit(final Prop3 x, final Prop3 y, final State state) {
        return Objects.equals(x.type, y.type)
               && x.name.equals(y.name)
               // ID-equality takes precedence over alpha-equivalence because all IDs within an AST are unique.
               && (x.source.id().equals(y.source.id()) || alphaEquivalent(x.source.id(), y.source.id(), state));
    }

    /// Binds the joined sources before comparing the `ON` conditions, which may reference them.
    /// The left and right nodes and the join type are compared in the enclosing scope.
    ///
    @Override
    public Boolean visit(final JoinInnerNode3 x, final JoinInnerNode3 y, final State state) {
        return visit(x.leftNode(), y.leftNode(), state)
               && visit(x.rightNode(), y.rightNode(), state)
               && Objects.equals(x.joinType(), y.joinType())
               && visit(x.joinConditions(), y.joinConditions(), addSources(state, x, y));
    }

    /// A table source is a binding occurrence: its ID -- and the SQL alias derived from it -- is renameable and so is
    /// not compared; only the table matters.
    ///
    @Override
    public Boolean visit(final Source3BasedOnTable x, final Source3BasedOnTable y, final State state) {
        return Objects.equals(x.tableName, y.tableName);
    }

    /// A query-based source is a binding occurrence: its ID and SQL alias are renameable and so are not compared; only
    /// the underlying models matter.
    ///
    @Override
    public Boolean visit(final Source3BasedOnQueries x, final Source3BasedOnQueries y, final State state) {
        return visitAll(x.models, y.models, state);
    }

    /// Binds the query's sources before comparing the components that may reference them.
    /// The join tree is visited in the enclosing scope, since it *introduces* the sources.
    /// The join `on` conditions, `where` conditions, yields, groups and orderings are then visited in a new scope that has those sources in it.
    ///
    @Override
    protected Boolean visitQueryComponents(final AbstractQuery3 x, final AbstractQuery3 y, final State state) {
        // TODO Optimise: new state is built twice, once for join `on` conditions, once for the rest of query components.
        final State newState;
        return Objects.equals(x.resultType, y.resultType)
               && visitOptional(x.maybeJoinRoot, y.maybeJoinRoot, state)
               && visitNullable(x.whereConditions, y.whereConditions, (newState = addSources(state, streamSources(x), streamSources(y))))
               && visit(x.yields, y.yields, newState)
               && visitNullable(x.groups, y.groups, newState)
               && visitNullable(x.orderings, y.orderings, newState);
    }

    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    // : Utilities

    /// The sources introduced directly by a query -- i.e. the leaves of its join tree.
    ///
    private Stream<ISource3> streamSources(final AbstractQuery3 query) {
        return query.maybeJoinRoot.map(this::streamSources).orElseGet(Stream::of);
    }

    /// The source leaves reachable from a join node.
    ///
    private Stream<ISource3> streamSources(final IJoinNode3 join) {
        return switch (join) {
            case JoinLeafNode3 (var source) -> Stream.of(source);
            case JoinInnerNode3 (var left, var right, var _, var _) -> Stream.concat(streamSources(left), streamSources(right));
            default -> throw new InvalidStateException("Unexpected node type: %s".formatted(join.getClass().getName()));
        };
    }

}
