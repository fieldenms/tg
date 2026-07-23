package ua.com.fielden.platform.eql.stage3;

import jakarta.annotation.Nullable;
import ua.com.fielden.platform.eql.stage3.conditions.*;
import ua.com.fielden.platform.eql.stage3.operands.*;
import ua.com.fielden.platform.eql.stage3.operands.functions.*;
import ua.com.fielden.platform.eql.stage3.queries.*;
import ua.com.fielden.platform.eql.stage3.sources.JoinInnerNode3;
import ua.com.fielden.platform.eql.stage3.sources.JoinLeafNode3;
import ua.com.fielden.platform.eql.stage3.sources.Source3BasedOnQueries;
import ua.com.fielden.platform.eql.stage3.sources.Source3BasedOnTable;
import ua.com.fielden.platform.eql.stage3.sundries.*;
import ua.com.fielden.platform.types.tuples.T2;

import java.util.Collection;
import java.util.Optional;
import java.util.SortedMap;
import java.util.function.BiFunction;
import java.util.stream.Stream;

import static ua.com.fielden.platform.utils.StreamUtils.foldLeft;
import static ua.com.fielden.platform.utils.StreamUtils.zip;

/// A visitor of EQL AST nodes that takes two trees and traverses them only if both have the same shape.
/// Specifically, at each pair of nodes -- one from tree A and another from tree B -- a visit is performed iff
/// both nodes have the same type, otherwise [#noMatch] is called.
///
/// Dispatch happens in [#visit(Object x, Object y, S state)], which switches on the runtime type of both nodes.
/// Every AST node type has a typed facade named after the node (e.g., `maxOf(MaxOf3, MaxOf3, S)`) that dispatch delegates to.
/// A facade recurses into a child node via [#visit(Object x, Object y, S state)], so all node visits go through that method,
/// which enables simple interception of all visits by overriding that method.
/// This design imposes the following requirements on each subclass:
/// 1. An overridden facade method `m(x, y, state)` must not call `super.visit(x, y, state)` to invoke the parent class method,
///    because this will result in infinite recursion.
///    Instead, the parent facade method should be called directly: `super.m(x, y, state)`.
///
///    ```
///    R prop(Prop3 x, Prop3 y, S state) {
///        super.visit(x, y, state); // Error: super.visit -> this.prop -> super.visit -> ...
///        super.prop(x, y, state);  // Correct
///    }
///    ```
/// 2. An overridden facade method should call [#visit] for sub-visits on its children.
///    This preserves [#visit] as the universal interceptor.
///
///    ```
///    // Trivial case: MaxOf.operand is ISingleOperand3 -- no concrete type, so visit() is the natural choice.
///    R maxOf(MaxOf3 x, MaxOf3 y, S state) {
///        visit(x.operand, y.operand, state);
///    }
///
///    R groupBys(GroupBys3 x, GroupBys3 y, S state) {
///        groupBy(x.groups.getFirst(), y.groups.getFirst(), state); // Incorrect
///        visit(x.groups.getFirst(), y.groups.getFirst(), state);   // Correct
///    }
///    ```
///
/// The base facades encode a default traversal:
/// - A leaf (no child node) returns [#defaultValue].
/// - A node with one or more child nodes recurses into each and folds the results with [#combine].
///   Each child node is visited with the same state.
///   I.e., state does not propagate from one child node to the next (same tree level), but only downwards.
///
/// ## Traversal of structure, not data
///
/// Traversal accounts only for the parts of a node that reference *other nodes* -- its child nodes.
/// It deliberately ignores the node's own data: booleans, strings, integers, enums, types, `Class` literals, and the like.
/// The rationale is separation of concerns.
/// This base visitor is a generic traversal skeleton, parameterised by an arbitrary result type `R`.
/// It may be reused by visitors that have nothing to do with data -- for example, collecting every [Prop3] node, or counting nodes.
/// Interpreting a node's data is the concern of a concrete visitor, expressed by overriding the relevant facade method.
///
/// ## Visitors represent operations
///
/// A visitor represents an *operation* -- a traversal of the EQL AST -- rather than data.
/// Each subclass should respect this contract by being injectable and keeping no state in its fields.
/// Anything the operation needs -- both internal state and external parameters -- belongs in the traversal state `S`,
/// which is threaded through every visit method as the `state` parameter.
///
/// Staying stateless keeps a visitor safe to share (e.g., as a singleton) and reentrant: one instance can perform many
/// independent traversals, even concurrently, because nothing about a traversal lives on the instance.
///
/// For example, a visitor that collects all nodes satisfying a predicate (an external parameter) while tracking the
/// current tree depth (internal state) models both as `S`:
///
/// ```
/// record State (Predicate<?> pred, int depth) {
///     State incDepth() { return new State(pred, depth + 1); }
/// }
/// ```
///
/// Each visit method then reads and advances this state through its `state` parameter rather than through a field.
///
/// @param <R>  type of the result produced by a visitor
/// @param <S>  type of state maintained by a visitor
///
public abstract class AbstractSameShapeVisitor<R, S> {

    /// TODO Replace Object by a common node interface once introduced.
    ///
    public R visit(final Object x, final Object y, final S state) {
        return switch (x) {
            // Operands.
            case Prop3 x_       -> y instanceof Prop3 y_       ? prop(x_, y_, state) : noMatch(x, y, state);
            case Value3 x_      -> y instanceof Value3 y_      ? value(x_, y_, state) : noMatch(x, y, state);
            case Expression3 x_ -> y instanceof Expression3 y_ ? expression(x_, y_, state) : noMatch(x, y, state);
            case CompoundSingleOperand3 x_ -> y instanceof CompoundSingleOperand3 y_ ? compoundSingleOperand(x_, y_, state) : noMatch(x, y, state);
            // Single-operand functions.
            case AbsOf3 x_       -> y instanceof AbsOf3 y_       ? absOf(x_, y_, state) : noMatch(x, y, state);
            case Ceil3 x_        -> y instanceof Ceil3 y_        ? ceil(x_, y_, state) : noMatch(x, y, state);
            case Floor3 x_       -> y instanceof Floor3 y_       ? floor(x_, y_, state) : noMatch(x, y, state);
            case DateOf3 x_      -> y instanceof DateOf3 y_      ? dateOf(x_, y_, state) : noMatch(x, y, state);
            case DayOf3 x_       -> y instanceof DayOf3 y_       ? dayOf(x_, y_, state) : noMatch(x, y, state);
            case DayOfWeekOf3 x_ -> y instanceof DayOfWeekOf3 y_ ? dayOfWeekOf(x_, y_, state) : noMatch(x, y, state);
            case MonthOf3 x_     -> y instanceof MonthOf3 y_     ? monthOf(x_, y_, state) : noMatch(x, y, state);
            case YearOf3 x_      -> y instanceof YearOf3 y_      ? yearOf(x_, y_, state) : noMatch(x, y, state);
            case HourOf3 x_      -> y instanceof HourOf3 y_      ? hourOf(x_, y_, state) : noMatch(x, y, state);
            case MinuteOf3 x_    -> y instanceof MinuteOf3 y_    ? minuteOf(x_, y_, state) : noMatch(x, y, state);
            case SecondOf3 x_    -> y instanceof SecondOf3 y_    ? secondOf(x_, y_, state) : noMatch(x, y, state);
            case LowerCaseOf3 x_ -> y instanceof LowerCaseOf3 y_ ? lowerCaseOf(x_, y_, state) : noMatch(x, y, state);
            case UpperCaseOf3 x_ -> y instanceof UpperCaseOf3 y_ ? upperCaseOf(x_, y_, state) : noMatch(x, y, state);
            case MaxOf3 x_       -> y instanceof MaxOf3 y_       ? maxOf(x_, y_, state) : noMatch(x, y, state);
            case MinOf3 x_       -> y instanceof MinOf3 y_       ? minOf(x_, y_, state) : noMatch(x, y, state);
            case SumOf3 x_       -> y instanceof SumOf3 y_       ? sumOf(x_, y_, state) : noMatch(x, y, state);
            case CountOf3 x_     -> y instanceof CountOf3 y_     ? countOf(x_, y_, state) : noMatch(x, y, state);
            case AverageOf3 x_   -> y instanceof AverageOf3 y_   ? averageOf(x_, y_, state) : noMatch(x, y, state);
            // Two-operand functions.
            case IfNull3 x_            -> y instanceof IfNull3 y_            ? ifNull(x_, y_, state) : noMatch(x, y, state);
            case RoundTo3 x_           -> y instanceof RoundTo3 y_           ? roundTo(x_, y_, state) : noMatch(x, y, state);
            case AddDateInterval3 x_   -> y instanceof AddDateInterval3 y_   ? addDateInterval(x_, y_, state) : noMatch(x, y, state);
            case CountDateInterval3 x_ -> y instanceof CountDateInterval3 y_ ? countDateInterval(x_, y_, state) : noMatch(x, y, state);
            case ConcatOf3 x_          -> y instanceof ConcatOf3 y_          ? concatOf(x_, y_, state) : noMatch(x, y, state);
            // Other functions.
            case Concat3 x_   -> y instanceof Concat3 y_   ? concat(x_, y_, state) : noMatch(x, y, state);
            case CaseWhen3 x_ -> y instanceof CaseWhen3 y_ ? caseWhen(x_, y_, state) : noMatch(x, y, state);
            case CountAll3 x_ -> y instanceof CountAll3 y_ ? countAll(x_, y_, state) : noMatch(x, y, state);
            // Queries (the sub-query is also an operand).
            case SubQuery3 x_          -> y instanceof SubQuery3 y_          ? subQuery(x_, y_, state) : noMatch(x, y, state);
            case SourceQuery3 x_       -> y instanceof SourceQuery3 y_       ? sourceQuery(x_, y_, state) : noMatch(x, y, state);
            case SubQueryForExists3 x_ -> y instanceof SubQueryForExists3 y_ ? subQueryForExists(x_, y_, state) : noMatch(x, y, state);
            case ResultQuery3 x_       -> y instanceof ResultQuery3 y_       ? resultQuery(x_, y_, state) : noMatch(x, y, state);
            // Conditions.
            case Conditions3 x_          -> y instanceof Conditions3 y_          ? conditions(x_, y_, state) : noMatch(x, y, state);
            case ComparisonPredicate3 x_ -> y instanceof ComparisonPredicate3 y_ ? comparisonPredicate(x_, y_, state) : noMatch(x, y, state);
            case NullPredicate3 x_       -> y instanceof NullPredicate3 y_       ? nullPredicate(x_, y_, state) : noMatch(x, y, state);
            case LikePredicate3 x_       -> y instanceof LikePredicate3 y_       ? likePredicate(x_, y_, state) : noMatch(x, y, state);
            case SetPredicate3 x_        -> y instanceof SetPredicate3 y_        ? setPredicate(x_, y_, state) : noMatch(x, y, state);
            case ExistencePredicate3 x_  -> y instanceof ExistencePredicate3 y_  ? existencePredicate(x_, y_, state) : noMatch(x, y, state);
            case QuantifiedPredicate3 x_ -> y instanceof QuantifiedPredicate3 y_ ? quantifiedPredicate(x_, y_, state) : noMatch(x, y, state);
            // Join nodes.
            case JoinLeafNode3 x_  -> y instanceof JoinLeafNode3 y_  ? joinLeafNode(x_, y_, state) : noMatch(x, y, state);
            case JoinInnerNode3 x_ -> y instanceof JoinInnerNode3 y_ ? joinInnerNode(x_, y_, state) : noMatch(x, y, state);
            // Set operands.
            case OperandsBasedSet3 x_ -> y instanceof OperandsBasedSet3 y_ ? operandsBasedSet(x_, y_, state) : noMatch(x, y, state);
            case QueryBasedSet3 x_    -> y instanceof QueryBasedSet3 y_    ? queryBasedSet(x_, y_, state) : noMatch(x, y, state);
            // Sources.
            case Source3BasedOnTable x_   -> y instanceof Source3BasedOnTable y_   ? sourceBasedOnTable(x_, y_, state) : noMatch(x, y, state);
            case Source3BasedOnQueries x_ -> y instanceof Source3BasedOnQueries y_ ? sourceBasedOnQueries(x_, y_, state) : noMatch(x, y, state);
            // Sundries.
            case Yields3 x_   -> y instanceof Yields3 y_   ? yields(x_, y_, state) : noMatch(x, y, state);
            case Yield3 x_   -> y instanceof Yield3 y_   ? this.yield(x_, y_, state) : noMatch(x, y, state);
            case GroupBys3 x_ -> y instanceof GroupBys3 y_ ? groupBys(x_, y_, state) : noMatch(x, y, state);
            case GroupBy3 x_ -> y instanceof GroupBy3 y_ ? groupBy(x_, y_, state) : noMatch(x, y, state);
            case OrderBys3 x_ -> y instanceof OrderBys3 y_ ? orderBys(x_, y_, state) : noMatch(x, y, state);
            case OrderBy3 x_ -> y instanceof OrderBy3 y_ ? orderBy(x_, y_, state) : noMatch(x, y, state);
            // TODO Remove once the node type hierarchy is sealed.
            default -> throw new IllegalStateException("Unexpected value: " + x);
        };
    }

    /// Called by a visitor when two nodes being matched have different shape.
    ///
    /// This could mean:
    /// * Nodes have different types (e.g, `x` is [Prop3] and `y` is [Value3]).
    /// * One node is present, while the other is absent (e.g., only one of the queries has a group-by component).
    ///   The absent node is represented by null.
    /// * Nodes have different structure.
    ///   E.g., `x` and `y` are [Yields3], but `x` has more/less yields than `y`.
    ///
    /// One of `x` or `y` may be null, but not both.
    /// The types of `x` and `y` can be expected to be the same.
    /// The set of possible types consists of all EQL AST node types plus "container types" that may hold nodes (e.g., collections).
    ///
    protected abstract R noMatch(@Nullable Object x, @Nullable Object y, S state);

    /// Returns a default value.
    /// This method is called on leaf nodes.
    /// It is useful when the same logic is applicable to many leaf node types.
    /// Instead of overriding each individual facade method, it will suffice to override just this method.
    ///
    protected abstract R defaultValue(Object x, Object y, S state);

    /// The identity element for [#combine]: `combine(x, identity()) = combine(identity(), x) = x`.
    ///
    /// It exists to make the traversal utilities generic over the result type `R`, and serves two purposes:
    /// - the seed of [#combine(Stream)], and hence the result for a node with no children (an empty stream);
    /// - the result of [#visitNullable] when both children are absent -- two missing children impose no constraint,
    ///   so they contribute the neutral value (e.g., `true` for equality).
    ///
    protected abstract R identity();

    /// Combines the results obtained for the several children of a node.
    /// For example, a structural-equality visitor combines with logical *and*.
    /// Expected to be associative and to have [#identity] as its identity element, so that [#combine(Stream)] is
    /// well-defined regardless of grouping.
    /// Note: this does not short-circuit -- both arguments are always evaluated by the caller.
    ///
    protected abstract R combine(R a, R b);

    /// Folds [#combine] over the results for a node's children, seeded with [#identity].
    /// A node with no children (an empty stream) therefore yields [#identity].
    ///
    protected R combine(final Stream<R> stream) {
        return foldLeft(stream, identity(), this::combine);
    }

    // :::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    // : Operands

    public R prop(final Prop3 x, final Prop3 y, final S state) {
        return defaultValue(x, y, state);
    }

    public R value(final Value3 x, final Value3 y, final S state) {
        return defaultValue(x, y, state);
    }

    public R expression(final Expression3 x, final Expression3 y, final S state) {
        if (x.otherOperands.size() == y.otherOperands.size()) {
            return combine(zip(Stream.concat(Stream.of(x.firstOperand), x.otherOperands.stream()),
                               Stream.concat(Stream.of(y.firstOperand), y.otherOperands.stream()),
                               (x_, y_) -> visit(x_, y_, state)));
        }
        else {
            return noMatch(x, y, state);
        }
    }

    public R compoundSingleOperand(final CompoundSingleOperand3 x, final CompoundSingleOperand3 y, final S state) {
        return visit(x.operand(), y.operand(), state);
    }

    // :::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    // : Single-operand functions -- recurse into the operand.

    public R absOf(final AbsOf3 x, final AbsOf3 y, final S state) {
        return visit(x.operand, y.operand, state);
    }

    public R ceil(final Ceil3 x, final Ceil3 y, final S state) {
        return visit(x.operand, y.operand, state);
    }

    public R floor(final Floor3 x, final Floor3 y, final S state) {
        return visit(x.operand, y.operand, state);
    }

    public R dateOf(final DateOf3 x, final DateOf3 y, final S state) {
        return visit(x.operand, y.operand, state);
    }

    public R dayOf(final DayOf3 x, final DayOf3 y, final S state) {
        return visit(x.operand, y.operand, state);
    }

    public R dayOfWeekOf(final DayOfWeekOf3 x, final DayOfWeekOf3 y, final S state) {
        return visit(x.operand, y.operand, state);
    }

    public R monthOf(final MonthOf3 x, final MonthOf3 y, final S state) {
        return visit(x.operand, y.operand, state);
    }

    public R yearOf(final YearOf3 x, final YearOf3 y, final S state) {
        return visit(x.operand, y.operand, state);
    }

    public R hourOf(final HourOf3 x, final HourOf3 y, final S state) {
        return visit(x.operand, y.operand, state);
    }

    public R minuteOf(final MinuteOf3 x, final MinuteOf3 y, final S state) {
        return visit(x.operand, y.operand, state);
    }

    public R secondOf(final SecondOf3 x, final SecondOf3 y, final S state) {
        return visit(x.operand, y.operand, state);
    }

    public R lowerCaseOf(final LowerCaseOf3 x, final LowerCaseOf3 y, final S state) {
        return visit(x.operand, y.operand, state);
    }

    public R upperCaseOf(final UpperCaseOf3 x, final UpperCaseOf3 y, final S state) {
        return visit(x.operand, y.operand, state);
    }

    public R maxOf(final MaxOf3 x, final MaxOf3 y, final S state) {
        return visit(x.operand, y.operand, state);
    }

    public R minOf(final MinOf3 x, final MinOf3 y, final S state) {
        return visit(x.operand, y.operand, state);
    }

    public R sumOf(final SumOf3 x, final SumOf3 y, final S state) {
        return visit(x.operand, y.operand, state);
    }

    public R countOf(final CountOf3 x, final CountOf3 y, final S state) {
        return visit(x.operand, y.operand, state);
    }

    public R averageOf(final AverageOf3 x, final AverageOf3 y, final S state) {
        return visit(x.operand, y.operand, state);
    }

    // :::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    // : Two-operand functions -- recurse into both operands and combine the results.

    public R ifNull(final IfNull3 x, final IfNull3 y, final S state) {
        return combine(visit(x.operand1, y.operand1, state), visit(x.operand2, y.operand2, state));
    }

    public R roundTo(final RoundTo3 x, final RoundTo3 y, final S state) {
        return combine(visit(x.operand1, y.operand1, state), visit(x.operand2, y.operand2, state));
    }

    public R addDateInterval(final AddDateInterval3 x, final AddDateInterval3 y, final S state) {
        return combine(visit(x.operand1, y.operand1, state), visit(x.operand2, y.operand2, state));
    }

    public R countDateInterval(final CountDateInterval3 x, final CountDateInterval3 y, final S state) {
        return combine(visit(x.operand1, y.operand1, state), visit(x.operand2, y.operand2, state));
    }

    public R concatOf(final ConcatOf3 x, final ConcatOf3 y, final S state) {
        return combine(Stream.concat(Stream.of(visit(x.operand1, y.operand1, state), visit(x.operand2, y.operand2, state)),
                                     streamAll(x.orderItems, y.orderItems, state)));
    }

    // :::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    // : Other functions

    public R concat(final Concat3 x, final Concat3 y, final S state) {
        return visitAll(x.operands, y.operands, state);
    }

    public R caseWhen(final CaseWhen3 x, final CaseWhen3 y, final S state) {
        if (x.whenThenPairs().size() == y.whenThenPairs().size()
            && bothEmptyOrPresent(x.elseOperand(), y.elseOperand()))
        {
            return combine(Stream.concat(Stream.of(visitOptional(x.elseOperand(), y.elseOperand(), state)),
                                         zip(x.whenThenPairs().stream().flatMap(T2::stream),
                                             y.whenThenPairs().stream().flatMap(T2::stream),
                                             (x_, y_) -> visit(x_, y_, state))));
        }
        else {
            return noMatch(x, y, state);
        }
    }

    public R countAll(final CountAll3 x, final CountAll3 y, final S state) {
        return defaultValue(x, y, state);
    }

    // :::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    // : Queries

    public R subQuery(final SubQuery3 x, final SubQuery3 y, final S state) {
        return visitQueryComponents(x, y, state);
    }

    public R sourceQuery(final SourceQuery3 x, final SourceQuery3 y, final S state) {
        return visitQueryComponents(x, y, state);
    }

    public R subQueryForExists(final SubQueryForExists3 x, final SubQueryForExists3 y, final S state) {
        return visitQueryComponents(x, y, state);
    }

    public R resultQuery(final ResultQuery3 x, final ResultQuery3 y, final S state) {
        return visitQueryComponents(x, y, state);
    }

    /// Visits the six [AbstractQuery3] components shared by all query types.
    ///
    protected R visitQueryComponents(final AbstractQuery3 x, final AbstractQuery3 y, final S state) {
        return combine(Stream.of(visitOptional(x.maybeJoinRoot, y.maybeJoinRoot, state),
                                 visitNullable(x.whereConditions, y.whereConditions, state),
                                 visit(x.yields, y.yields, state),
                                 visitNullable(x.groups, y.groups, state),
                                 visitNullable(x.orderings, y.orderings, state)));
    }

    // :::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    // : Conditions

    public R conditions(final Conditions3 x, final Conditions3 y, final S state) {
        return visitAllWith(x.allConditionsAsDnf(), y.allConditionsAsDnf(), state, (xConds, yConds) -> visitAll(xConds, yConds, state));
    }

    public R comparisonPredicate(final ComparisonPredicate3 x, final ComparisonPredicate3 y, final S state) {
        return combine(visit(x.leftOperand(), y.leftOperand(), state), visit(x.rightOperand(), y.rightOperand(), state));
    }

    public R nullPredicate(final NullPredicate3 x, final NullPredicate3 y, final S state) {
        return visit(x.operand(), y.operand(), state);
    }

    public R likePredicate(final LikePredicate3 x, final LikePredicate3 y, final S state) {
        return combine(visit(x.matchOperand(), y.matchOperand(), state), visit(x.patternOperand(), y.patternOperand(), state));
    }

    public R setPredicate(final SetPredicate3 x, final SetPredicate3 y, final S state) {
        return combine(visit(x.leftOperand(), y.leftOperand(), state), visit(x.rightOperand(), y.rightOperand(), state));
    }

    public R existencePredicate(final ExistencePredicate3 x, final ExistencePredicate3 y, final S state) {
        return visit(x.subQuery(), y.subQuery(), state);
    }

    public R quantifiedPredicate(final QuantifiedPredicate3 x, final QuantifiedPredicate3 y, final S state) {
        return combine(visit(x.leftOperand(), y.leftOperand(), state), visit(x.rightOperand(), y.rightOperand(), state));
    }

    // :::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    // : Join nodes

    public R joinLeafNode(final JoinLeafNode3 x, final JoinLeafNode3 y, final S state) {
        return visit(x.source(), y.source(), state);
    }

    public R joinInnerNode(final JoinInnerNode3 x, final JoinInnerNode3 y, final S state) {
        return combine(Stream.of(visit(x.leftNode(), y.leftNode(), state),
                                 visit(x.rightNode(), y.rightNode(), state),
                                 visit(x.joinConditions(), y.joinConditions(), state)));
    }

    // :::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    // : Set operands

    public R operandsBasedSet(final OperandsBasedSet3 x, final OperandsBasedSet3 y, final S state) {
        return visitAll(x.operands(), y.operands(), state);
    }

    public R queryBasedSet(final QueryBasedSet3 x, final QueryBasedSet3 y, final S state) {
        return visit(x.model(), y.model(), state);
    }

    // :::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    // : Sources

    public R sourceBasedOnTable(final Source3BasedOnTable x, final Source3BasedOnTable y, final S state) {
        return defaultValue(x, y, state);
    }

    public R sourceBasedOnQueries(final Source3BasedOnQueries x, final Source3BasedOnQueries y, final S state) {
        return visitAll(x.models, y.models, state);
    }

    // :::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    // : Sundries

    protected R yields(final Yields3 xs, final Yields3 ys, final S state) {
        final SortedMap<String, Yield3> xsMap = xs.yieldsMap();
        final SortedMap<String, Yield3> ysMap = ys.yieldsMap();
        if (xsMap.size() != ysMap.size()) {
            return noMatch(xs, ys, state);
        }
        return combine(zip(xsMap.values(), ysMap.values(), (xYield, yYield) -> visit(xYield, yYield, state)));
    }

    protected R groupBys(final GroupBys3 xs, final GroupBys3 ys, final S state) {
        return visitAll(xs.groups(), ys.groups(), state);
    }

    protected R orderBys(final OrderBys3 xs, final OrderBys3 ys, final S state) {
        return visitAll(xs.list(), ys.list(), state);
    }

    public R yield(final Yield3 x, final Yield3 y, final S state) {
        return visit(x.operand(), y.operand(), state);
    }

    public R groupBy(final GroupBy3 x, final GroupBy3 y, final S state) {
        return visit(x.operand(), y.operand(), state);
    }

    public R orderBy(final OrderBy3 x, final OrderBy3 y, final S state) {
        if (x.operand() != null && y.operand() != null) {
            return visit(x.operand(), y.operand(), state);
        }
        else if (x.yield() != null && y.yield() != null) {
            return visit(x.yield(), y.yield(), state);
        }
        else {
            return noMatch(x, y, state);
        }
    }

    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    // : Utilities

    protected <X> Stream<R> streamAll(final Collection<? extends X> xs, final Collection<? extends X> ys, final S state) {
        return streamAllWith(xs, ys, state, (x, y) -> visit(x, y, state));
    }

    protected <X> Stream<R> streamAllWith(final Collection<? extends X> xs, final Collection<? extends X> ys, final S state, final BiFunction<X, X, R> fn) {
        if (xs.size() != ys.size()) {
            return Stream.of(noMatch(xs, ys, state));
        }
        return zip(xs, ys, fn);
    }

    protected <X> R visitAll(final Collection<? extends X> xs, final Collection<? extends X> ys, final S state) {
        return combine(streamAll(xs, ys, state));
    }

    protected <X> R visitAllWith(final Collection<? extends X> xs, final Collection<? extends X> ys, final S state, final BiFunction<X, X, R> fn) {
        return combine(streamAllWith(xs, ys, state, fn));
    }

    protected <X> R visitNullable(final @Nullable X x, final @Nullable X y, final S state) {
        if (x != null && y != null) {
            return visit(x, y, state);
        }
        else if (x == null && y == null) {
            return identity();
        }
        else {
            return noMatch(x, y, state);
        }
    }

    protected <X> R visitOptional(final Optional<X> maybeX, final Optional<X> maybeY, final S state) {
        return visitNullable(maybeX.orElse(null), maybeY.orElse(null), state);
    }

    protected <X> boolean bothEmptyOrPresent(final Optional<X> maybeX, final Optional<X> maybeY) {
        return maybeX.isPresent() == maybeY.isPresent();
    }

}
