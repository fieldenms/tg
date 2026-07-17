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
/// Every AST node type has a typed facade `visit(T, T, S)` that dispatch delegates to.
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
/// Interpreting a node's data is the concern of a concrete visitor, expressed by overriding the relevant [#visit] methods.
///
/// @param <R>  type of the result produced by a visitor
/// @param <S>  type of state maintained by a visitor
///
public abstract class AbstractSameShapeVisitor<R, S> {

    /// TODO Replace Object by a common node interface once introduced.
    ///
    public R visit(Object x, Object y, S state) {
        return switch (x) {
            // Operands.
            case Prop3 x_       -> y instanceof Prop3 y_       ? visit(x_, y_, state) : noMatch(x, y, state);
            case Value3 x_      -> y instanceof Value3 y_      ? visit(x_, y_, state) : noMatch(x, y, state);
            case Expression3 x_ -> y instanceof Expression3 y_ ? visit(x_, y_, state) : noMatch(x, y, state);
            case CompoundSingleOperand3 x_ -> y instanceof CompoundSingleOperand3 y_ ? visit(x_, y_, state) : noMatch(x, y, state);
            // Single-operand functions.
            case AbsOf3 x_       -> y instanceof AbsOf3 y_       ? visit(x_, y_, state) : noMatch(x, y, state);
            case Ceil3 x_        -> y instanceof Ceil3 y_        ? visit(x_, y_, state) : noMatch(x, y, state);
            case Floor3 x_       -> y instanceof Floor3 y_       ? visit(x_, y_, state) : noMatch(x, y, state);
            case DateOf3 x_      -> y instanceof DateOf3 y_      ? visit(x_, y_, state) : noMatch(x, y, state);
            case DayOf3 x_       -> y instanceof DayOf3 y_       ? visit(x_, y_, state) : noMatch(x, y, state);
            case DayOfWeekOf3 x_ -> y instanceof DayOfWeekOf3 y_ ? visit(x_, y_, state) : noMatch(x, y, state);
            case MonthOf3 x_     -> y instanceof MonthOf3 y_     ? visit(x_, y_, state) : noMatch(x, y, state);
            case YearOf3 x_      -> y instanceof YearOf3 y_      ? visit(x_, y_, state) : noMatch(x, y, state);
            case HourOf3 x_      -> y instanceof HourOf3 y_      ? visit(x_, y_, state) : noMatch(x, y, state);
            case MinuteOf3 x_    -> y instanceof MinuteOf3 y_    ? visit(x_, y_, state) : noMatch(x, y, state);
            case SecondOf3 x_    -> y instanceof SecondOf3 y_    ? visit(x_, y_, state) : noMatch(x, y, state);
            case LowerCaseOf3 x_ -> y instanceof LowerCaseOf3 y_ ? visit(x_, y_, state) : noMatch(x, y, state);
            case UpperCaseOf3 x_ -> y instanceof UpperCaseOf3 y_ ? visit(x_, y_, state) : noMatch(x, y, state);
            case MaxOf3 x_       -> y instanceof MaxOf3 y_       ? visit(x_, y_, state) : noMatch(x, y, state);
            case MinOf3 x_       -> y instanceof MinOf3 y_       ? visit(x_, y_, state) : noMatch(x, y, state);
            case SumOf3 x_       -> y instanceof SumOf3 y_       ? visit(x_, y_, state) : noMatch(x, y, state);
            case CountOf3 x_     -> y instanceof CountOf3 y_     ? visit(x_, y_, state) : noMatch(x, y, state);
            case AverageOf3 x_   -> y instanceof AverageOf3 y_   ? visit(x_, y_, state) : noMatch(x, y, state);
            // Two-operand functions.
            case IfNull3 x_            -> y instanceof IfNull3 y_            ? visit(x_, y_, state) : noMatch(x, y, state);
            case RoundTo3 x_           -> y instanceof RoundTo3 y_           ? visit(x_, y_, state) : noMatch(x, y, state);
            case AddDateInterval3 x_   -> y instanceof AddDateInterval3 y_   ? visit(x_, y_, state) : noMatch(x, y, state);
            case CountDateInterval3 x_ -> y instanceof CountDateInterval3 y_ ? visit(x_, y_, state) : noMatch(x, y, state);
            case ConcatOf3 x_          -> y instanceof ConcatOf3 y_          ? visit(x_, y_, state) : noMatch(x, y, state);
            // Other functions.
            case Concat3 x_   -> y instanceof Concat3 y_   ? visit(x_, y_, state) : noMatch(x, y, state);
            case CaseWhen3 x_ -> y instanceof CaseWhen3 y_ ? visit(x_, y_, state) : noMatch(x, y, state);
            case CountAll3 x_ -> y instanceof CountAll3 y_ ? visit(x_, y_, state) : noMatch(x, y, state);
            // Queries (the sub-query is also an operand).
            case SubQuery3 x_          -> y instanceof SubQuery3 y_          ? visit(x_, y_, state) : noMatch(x, y, state);
            case SourceQuery3 x_       -> y instanceof SourceQuery3 y_       ? visit(x_, y_, state) : noMatch(x, y, state);
            case SubQueryForExists3 x_ -> y instanceof SubQueryForExists3 y_ ? visit(x_, y_, state) : noMatch(x, y, state);
            case ResultQuery3 x_       -> y instanceof ResultQuery3 y_       ? visit(x_, y_, state) : noMatch(x, y, state);
            // Conditions.
            case Conditions3 x_          -> y instanceof Conditions3 y_          ? visit(x_, y_, state) : noMatch(x, y, state);
            case ComparisonPredicate3 x_ -> y instanceof ComparisonPredicate3 y_ ? visit(x_, y_, state) : noMatch(x, y, state);
            case NullPredicate3 x_       -> y instanceof NullPredicate3 y_       ? visit(x_, y_, state) : noMatch(x, y, state);
            case LikePredicate3 x_       -> y instanceof LikePredicate3 y_       ? visit(x_, y_, state) : noMatch(x, y, state);
            case SetPredicate3 x_        -> y instanceof SetPredicate3 y_        ? visit(x_, y_, state) : noMatch(x, y, state);
            case ExistencePredicate3 x_  -> y instanceof ExistencePredicate3 y_  ? visit(x_, y_, state) : noMatch(x, y, state);
            case QuantifiedPredicate3 x_ -> y instanceof QuantifiedPredicate3 y_ ? visit(x_, y_, state) : noMatch(x, y, state);
            // Join nodes.
            case JoinLeafNode3 x_  -> y instanceof JoinLeafNode3 y_  ? visit(x_, y_, state) : noMatch(x, y, state);
            case JoinInnerNode3 x_ -> y instanceof JoinInnerNode3 y_ ? visit(x_, y_, state) : noMatch(x, y, state);
            // Set operands.
            case OperandsBasedSet3 x_ -> y instanceof OperandsBasedSet3 y_ ? visit(x_, y_, state) : noMatch(x, y, state);
            case QueryBasedSet3 x_    -> y instanceof QueryBasedSet3 y_    ? visit(x_, y_, state) : noMatch(x, y, state);
            // Sources.
            case Source3BasedOnTable x_   -> y instanceof Source3BasedOnTable y_   ? visit(x_, y_, state) : noMatch(x, y, state);
            case Source3BasedOnQueries x_ -> y instanceof Source3BasedOnQueries y_ ? visit(x_, y_, state) : noMatch(x, y, state);
            // Sundries.
            case Yields3 x_   -> y instanceof Yields3 y_   ? visit(x_, y_, state) : noMatch(x, y, state);
            case Yield3 x_   -> y instanceof Yield3 y_   ? visit(x_, y_, state) : noMatch(x, y, state);
            case GroupBys3 x_ -> y instanceof GroupBys3 y_ ? visit(x_, y_, state) : noMatch(x, y, state);
            case GroupBy3 x_ -> y instanceof GroupBy3 y_ ? visit(x_, y_, state) : noMatch(x, y, state);
            case OrderBys3 x_ -> y instanceof OrderBys3 y_ ? visit(x_, y_, state) : noMatch(x, y, state);
            case OrderBy3 x_ -> y instanceof OrderBy3 y_ ? visit(x_, y_, state) : noMatch(x, y, state);
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
    /// Instead of overriding each individual [#visit], it will suffice to override just this method.
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
    protected R combine(Stream<R> stream) {
        return foldLeft(stream, identity(), this::combine);
    }

    // :::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    // : Operands

    public R visit(Prop3 x, Prop3 y, S state) {
        return defaultValue(x, y, state);
    }

    public R visit(Value3 x, Value3 y, S state) {
        return defaultValue(x, y, state);
    }

    public R visit(Expression3 x, Expression3 y, S state) {
        if (x.otherOperands.size() == y.otherOperands.size()) {
            return combine(zip(Stream.concat(Stream.of(x.firstOperand), x.otherOperands.stream()),
                               Stream.concat(Stream.of(y.firstOperand), y.otherOperands.stream()),
                               (x_, y_) -> visit(x_, y_, state)));
        }
        else {
            // TODO Reason: different structure.
            return noMatch(x, y, state);
        }
    }

    public R visit(final CompoundSingleOperand3 x, final CompoundSingleOperand3 y, final S state) {
        return visit(x.operand(), y.operand(), state);
    }

    // :::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    // : Single-operand functions -- recurse into the operand.

    public R visit(AbsOf3 x, AbsOf3 y, S state) {
        return visit(x.operand, y.operand, state);
    }

    public R visit(Ceil3 x, Ceil3 y, S state) {
        return visit(x.operand, y.operand, state);
    }

    public R visit(Floor3 x, Floor3 y, S state) {
        return visit(x.operand, y.operand, state);
    }

    public R visit(DateOf3 x, DateOf3 y, S state) {
        return visit(x.operand, y.operand, state);
    }

    public R visit(DayOf3 x, DayOf3 y, S state) {
        return visit(x.operand, y.operand, state);
    }

    public R visit(DayOfWeekOf3 x, DayOfWeekOf3 y, S state) {
        return visit(x.operand, y.operand, state);
    }

    public R visit(MonthOf3 x, MonthOf3 y, S state) {
        return visit(x.operand, y.operand, state);
    }

    public R visit(YearOf3 x, YearOf3 y, S state) {
        return visit(x.operand, y.operand, state);
    }

    public R visit(HourOf3 x, HourOf3 y, S state) {
        return visit(x.operand, y.operand, state);
    }

    public R visit(MinuteOf3 x, MinuteOf3 y, S state) {
        return visit(x.operand, y.operand, state);
    }

    public R visit(SecondOf3 x, SecondOf3 y, S state) {
        return visit(x.operand, y.operand, state);
    }

    public R visit(LowerCaseOf3 x, LowerCaseOf3 y, S state) {
        return visit(x.operand, y.operand, state);
    }

    public R visit(UpperCaseOf3 x, UpperCaseOf3 y, S state) {
        return visit(x.operand, y.operand, state);
    }

    public R visit(MaxOf3 x, MaxOf3 y, S state) {
        return visit(x.operand, y.operand, state);
    }

    public R visit(MinOf3 x, MinOf3 y, S state) {
        return visit(x.operand, y.operand, state);
    }

    public R visit(SumOf3 x, SumOf3 y, S state) {
        return visit(x.operand, y.operand, state);
    }

    public R visit(CountOf3 x, CountOf3 y, S state) {
        return visit(x.operand, y.operand, state);
    }

    public R visit(AverageOf3 x, AverageOf3 y, S state) {
        return visit(x.operand, y.operand, state);
    }

    // :::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    // : Two-operand functions -- recurse into both operands and combine the results.

    public R visit(IfNull3 x, IfNull3 y, S state) {
        return combine(visit(x.operand1, y.operand1, state), visit(x.operand2, y.operand2, state));
    }

    public R visit(RoundTo3 x, RoundTo3 y, S state) {
        return combine(visit(x.operand1, y.operand1, state), visit(x.operand2, y.operand2, state));
    }

    public R visit(AddDateInterval3 x, AddDateInterval3 y, S state) {
        return combine(visit(x.operand1, y.operand1, state), visit(x.operand2, y.operand2, state));
    }

    public R visit(CountDateInterval3 x, CountDateInterval3 y, S state) {
        return combine(visit(x.operand1, y.operand1, state), visit(x.operand2, y.operand2, state));
    }

    public R visit(ConcatOf3 x, ConcatOf3 y, S state) {
        return combine(Stream.concat(Stream.of(visit(x.operand1, y.operand1, state), visit(x.operand2, y.operand2, state)),
                                     streamAll(x.orderItems, y.orderItems, state)));
    }

    // :::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    // : Other functions

    public R visit(Concat3 x, Concat3 y, S state) {
        return visitAll(x.operands, y.operands, state);
    }

    public R visit(CaseWhen3 x, CaseWhen3 y, S state) {
        if (x.whenThenPairs().size() == y.whenThenPairs().size()
            && bothEmptyOrPresent(x.elseOperand(), y.elseOperand()))
        {
            return combine(Stream.concat(Stream.of(visitOptional(x.elseOperand(), y.elseOperand(), state)),
                                         zip(x.whenThenPairs().stream().flatMap(T2::stream),
                                             y.whenThenPairs().stream().flatMap(T2::stream),
                                             (x_, y_) -> visit(x_, y_, state))));
        }
        else {
            // TODO Reason: different structure.
            return noMatch(x, y, state);
        }
    }

    public R visit(CountAll3 x, CountAll3 y, S state) {
        return defaultValue(x, y, state);
    }

    // :::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    // : Queries

    public R visit(SubQuery3 x, SubQuery3 y, S state) {
        return visitQueryComponents(x, y, state);
    }

    public R visit(SourceQuery3 x, SourceQuery3 y, S state) {
        return visitQueryComponents(x, y, state);
    }

    public R visit(SubQueryForExists3 x, SubQueryForExists3 y, S state) {
        return visitQueryComponents(x, y, state);
    }

    public R visit(ResultQuery3 x, ResultQuery3 y, S state) {
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

    public R visit(Conditions3 x, Conditions3 y, S state) {
        return visitAllWith(x.allConditionsAsDnf(), y.allConditionsAsDnf(), state, (xConds, yConds) -> visitAll(xConds, yConds, state));
    }

    public R visit(ComparisonPredicate3 x, ComparisonPredicate3 y, S state) {
        return combine(visit(x.leftOperand(), y.leftOperand(), state), visit(x.rightOperand(), y.rightOperand(), state));
    }

    public R visit(NullPredicate3 x, NullPredicate3 y, S state) {
        return visit(x.operand(), y.operand(), state);
    }

    public R visit(LikePredicate3 x, LikePredicate3 y, S state) {
        return combine(visit(x.matchOperand(), y.matchOperand(), state), visit(x.patternOperand(), y.patternOperand(), state));
    }

    public R visit(SetPredicate3 x, SetPredicate3 y, S state) {
        return combine(visit(x.leftOperand(), y.leftOperand(), state), visit(x.rightOperand(), y.rightOperand(), state));
    }

    public R visit(ExistencePredicate3 x, ExistencePredicate3 y, S state) {
        return visit(x.subQuery(), y.subQuery(), state);
    }

    public R visit(QuantifiedPredicate3 x, QuantifiedPredicate3 y, S state) {
        return combine(visit(x.leftOperand(), y.leftOperand(), state), visit(x.rightOperand(), y.rightOperand(), state));
    }

    // :::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    // : Join nodes

    public R visit(JoinLeafNode3 x, JoinLeafNode3 y, S state) {
        return visit(x.source(), y.source(), state);
    }

    public R visit(JoinInnerNode3 x, JoinInnerNode3 y, S state) {
        return combine(Stream.of(visit(x.leftNode(), y.leftNode(), state),
                                 visit(x.rightNode(), y.rightNode(), state),
                                 visit(x.joinConditions(), y.joinConditions(), state)));
    }

    // :::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    // : Set operands

    public R visit(OperandsBasedSet3 x, OperandsBasedSet3 y, S state) {
        return visitAll(x.operands(), y.operands(), state);
    }

    public R visit(QueryBasedSet3 x, QueryBasedSet3 y, S state) {
        return visit(x.model(), y.model(), state);
    }

    // :::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    // : Sources

    public R visit(Source3BasedOnTable x, Source3BasedOnTable y, S state) {
        return defaultValue(x, y, state);
    }

    public R visit(Source3BasedOnQueries x, Source3BasedOnQueries y, S state) {
        return visitAll(x.models, y.models, state);
    }

    // :::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    // : Sundries

    protected R visit(final Yields3 xs, final Yields3 ys, final S state) {
        final SortedMap<String, Yield3> xsMap = xs.yieldsMap();
        final SortedMap<String, Yield3> ysMap = ys.yieldsMap();
        if (xsMap.size() != ysMap.size()) {
            // TODO Reason: different structure.
            return noMatch(xs, ys, state);
        }
        return combine(zip(xsMap.values(), ysMap.values(), (xYield, yYield) -> visit(xYield, yYield, state)));
    }

    protected R visit(final GroupBys3 xs, final GroupBys3 ys, final S state) {
        return visitAll(xs.groups(), ys.groups(), state);
    }

    protected R visit(final OrderBys3 xs, final OrderBys3 ys, final S state) {
        return visitAll(xs.list(), ys.list(), state);
    }

    public R visit(Yield3 x, Yield3 y, S state) {
        return visit(x.operand(), y.operand(), state);
    }

    public R visit(GroupBy3 x, GroupBy3 y, S state) {
        return visit(x.operand(), y.operand(), state);
    }

    public R visit(OrderBy3 x, OrderBy3 y, S state) {
        if (x.operand() != null && y.operand() != null) {
            return visit(x.operand(), y.operand(), state);
        }
        else if (x.yield() != null && y.yield() != null) {
            return visit(x.yield(), y.yield(), state);
        }
        else {
            // TODO Reason: Different types.
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
            // TODO Reason: Different structure.
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
