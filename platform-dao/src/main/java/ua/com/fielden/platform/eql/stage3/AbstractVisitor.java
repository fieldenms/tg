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
import java.util.stream.Stream;

import static ua.com.fielden.platform.utils.StreamUtils.foldLeft;

/// A visitor of EQL AST nodes that takes one tree and traverses it.
/// This is the unary counterpart to the binary [AbstractSameShapeVisitor], and shares its design.
///
/// Dispatch happens in [#visit(INode3, S)], which switches on the runtime type of the node.
/// Every AST node type has a typed facade named after the node (e.g., `maxOf(MaxOf3, S)`) that dispatch delegates to.
/// A facade recurses into a child node via [#visit(INode3, S)], so all node visits go through that method,
/// which enables simple interception of all visits by overriding that method.
/// This design imposes the following requirements on each subclass:
/// 1. An overridden facade method `m(node, state)` must not call `super.visit(node, state)` to invoke the parent class
///    method, because this will result in infinite recursion.
///    Instead, the parent facade method should be called directly: `super.m(node, state)`.
///
///    ```
///    R prop(Prop3 node, S state) {
///        super.visit(node, state); // Error: super.visit -> this.prop -> super.visit -> ...
///        super.prop(node, state);  // Correct
///    }
///    ```
/// 2. An overridden facade method should call [#visit] for sub-visits on its children.
///    This preserves [#visit] as the universal interceptor.
///
///    ```
///    // Trivial case: MaxOf.operand is ISingleOperand3 -- no concrete type, so visit() is the natural choice.
///    R maxOf(MaxOf3 node, S state) {
///        visit(node.operand, state);
///    }
///
///    R groupBys(GroupBys3 node, S state) {
///        groupBy(node.groups().getFirst(), state); // Incorrect
///        visit(node.groups().getFirst(), state);   // Correct
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
/// Just like in [AbstractSameShapeVisitor], traversal accounts only for the parts of a node that reference
/// *other nodes* -- its child nodes.
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
/// record State (Predicate<? super INode3> pred, int depth) {
///     State incDepth() { return new State(pred, depth + 1); }
/// }
/// ```
///
/// Each visit method then reads and advances this state through its `state` parameter rather than through a field.
///
/// @param <R>  type of the result produced by a visitor
/// @param <S>  type of state maintained by a visitor
///
public abstract class AbstractVisitor<R, S> {

    public R visit(final INode3 node, final S state) {
        return switch (node) {
            // Operands.
            case Prop3 it       -> prop(it, state);
            case Value3 it      -> value(it, state);
            case Expression3 it -> expression(it, state);
            case CompoundSingleOperand3 it -> compoundSingleOperand(it, state);
            // Single-operand functions.
            case AbsOf3 it       -> absOf(it, state);
            case Ceil3 it        -> ceil(it, state);
            case Floor3 it       -> floor(it, state);
            case DateOf3 it      -> dateOf(it, state);
            case DayOf3 it       -> dayOf(it, state);
            case DayOfWeekOf3 it -> dayOfWeekOf(it, state);
            case MonthOf3 it     -> monthOf(it, state);
            case YearOf3 it      -> yearOf(it, state);
            case HourOf3 it      -> hourOf(it, state);
            case MinuteOf3 it    -> minuteOf(it, state);
            case SecondOf3 it    -> secondOf(it, state);
            case LowerCaseOf3 it -> lowerCaseOf(it, state);
            case UpperCaseOf3 it -> upperCaseOf(it, state);
            case MaxOf3 it       -> maxOf(it, state);
            case MinOf3 it       -> minOf(it, state);
            case SumOf3 it       -> sumOf(it, state);
            case CountOf3 it     -> countOf(it, state);
            case AverageOf3 it   -> averageOf(it, state);
            // Two-operand functions.
            case IfNull3 it            -> ifNull(it, state);
            case RoundTo3 it           -> roundTo(it, state);
            case AddDateInterval3 it   -> addDateInterval(it, state);
            case CountDateInterval3 it -> countDateInterval(it, state);
            case ConcatOf3 it          -> concatOf(it, state);
            // Other functions.
            case Concat3 it   -> concat(it, state);
            case CaseWhen3 it -> caseWhen(it, state);
            case CountAll3 it -> countAll(it, state);
            // Queries (the sub-query is also an operand).
            case SubQuery3 it          -> subQuery(it, state);
            case SourceQuery3 it       -> sourceQuery(it, state);
            case SubQueryForExists3 it -> subQueryForExists(it, state);
            case ResultQuery3 it       -> resultQuery(it, state);
            // Conditions.
            case Conditions3 it          -> conditions(it, state);
            case ComparisonPredicate3 it -> comparisonPredicate(it, state);
            case NullPredicate3 it       -> nullPredicate(it, state);
            case LikePredicate3 it       -> likePredicate(it, state);
            case SetPredicate3 it        -> setPredicate(it, state);
            case ExistencePredicate3 it  -> existencePredicate(it, state);
            case QuantifiedPredicate3 it -> quantifiedPredicate(it, state);
            // Join nodes.
            case JoinLeafNode3 it  -> joinLeafNode(it, state);
            case JoinInnerNode3 it -> joinInnerNode(it, state);
            // Set operands.
            case OperandsBasedSet3 it -> operandsBasedSet(it, state);
            case QueryBasedSet3 it    -> queryBasedSet(it, state);
            // Sources.
            case Source3BasedOnTable it   -> sourceBasedOnTable(it, state);
            case Source3BasedOnQueries it -> sourceBasedOnQueries(it, state);
            // Sundries.
            case Yields3 it   -> yields(it, state);
            case Yield3 it    -> this.yield(it, state);
            case GroupBys3 it -> groupBys(it, state);
            case GroupBy3 it  -> groupBy(it, state);
            case OrderBys3 it -> orderBys(it, state);
            case IOrderBy3.Operand it -> orderByOperand(it, state);
            case IOrderBy3.Yield it   -> orderByYield(it, state);
            // TODO Remove once the node type hierarchy is sealed.
            default -> throw new IllegalStateException("Unexpected value: " + node);
        };
    }

    /// Returns a default value.
    /// This method is called on leaf nodes.
    /// It is useful when the same logic is applicable to many leaf node types.
    /// Instead of overriding each individual facade method, it will suffice to override just this method.
    ///
    protected abstract R defaultValue(INode3 node, S state);

    /// The identity element for [#combine]: `combine(x, identity()) = combine(identity(), x) = x`.
    ///
    /// It exists to make the traversal utilities generic over the result type `R`, and serves two purposes:
    /// - the seed of [#combine(Stream)], and hence the result for a node with no children (an empty stream);
    /// - the result of [#visitNullable] when the child is absent -- a missing child imposes no constraint,
    ///   so it contributes the neutral value.
    ///
    protected abstract R identity();

    /// Combines the results obtained for the several children of a node.
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

    public R prop(final Prop3 node, final S state) {
        return defaultValue(node, state);
    }

    public R value(final Value3 node, final S state) {
        return defaultValue(node, state);
    }

    public R expression(final Expression3 node, final S state) {
        return combine(Stream.concat(Stream.of(visit(node.firstOperand, state)),
                                     node.otherOperands.stream().map(operand -> visit(operand, state))));
    }

    public R compoundSingleOperand(final CompoundSingleOperand3 node, final S state) {
        return visit(node.operand(), state);
    }

    // :::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    // : Single-operand functions -- recurse into the operand.

    public R absOf(final AbsOf3 node, final S state) {
        return visit(node.operand, state);
    }

    public R ceil(final Ceil3 node, final S state) {
        return visit(node.operand, state);
    }

    public R floor(final Floor3 node, final S state) {
        return visit(node.operand, state);
    }

    public R dateOf(final DateOf3 node, final S state) {
        return visit(node.operand, state);
    }

    public R dayOf(final DayOf3 node, final S state) {
        return visit(node.operand, state);
    }

    public R dayOfWeekOf(final DayOfWeekOf3 node, final S state) {
        return visit(node.operand, state);
    }

    public R monthOf(final MonthOf3 node, final S state) {
        return visit(node.operand, state);
    }

    public R yearOf(final YearOf3 node, final S state) {
        return visit(node.operand, state);
    }

    public R hourOf(final HourOf3 node, final S state) {
        return visit(node.operand, state);
    }

    public R minuteOf(final MinuteOf3 node, final S state) {
        return visit(node.operand, state);
    }

    public R secondOf(final SecondOf3 node, final S state) {
        return visit(node.operand, state);
    }

    public R lowerCaseOf(final LowerCaseOf3 node, final S state) {
        return visit(node.operand, state);
    }

    public R upperCaseOf(final UpperCaseOf3 node, final S state) {
        return visit(node.operand, state);
    }

    public R maxOf(final MaxOf3 node, final S state) {
        return visit(node.operand, state);
    }

    public R minOf(final MinOf3 node, final S state) {
        return visit(node.operand, state);
    }

    public R sumOf(final SumOf3 node, final S state) {
        return visit(node.operand, state);
    }

    public R countOf(final CountOf3 node, final S state) {
        return visit(node.operand, state);
    }

    public R averageOf(final AverageOf3 node, final S state) {
        return visit(node.operand, state);
    }

    // :::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    // : Two-operand functions -- recurse into both operands and combine the results.

    public R ifNull(final IfNull3 node, final S state) {
        return combine(visit(node.operand1, state), visit(node.operand2, state));
    }

    public R roundTo(final RoundTo3 node, final S state) {
        return combine(visit(node.operand1, state), visit(node.operand2, state));
    }

    public R addDateInterval(final AddDateInterval3 node, final S state) {
        return combine(visit(node.operand1, state), visit(node.operand2, state));
    }

    public R countDateInterval(final CountDateInterval3 node, final S state) {
        return combine(visit(node.operand1, state), visit(node.operand2, state));
    }

    public R concatOf(final ConcatOf3 node, final S state) {
        return combine(Stream.concat(Stream.of(visit(node.operand1, state), visit(node.operand2, state)),
                                     node.orderItems.stream().map(item -> visit(item, state))));
    }

    // :::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    // : Other functions

    public R concat(final Concat3 node, final S state) {
        return visitAll(node.operands(), state);
    }

    public R caseWhen(final CaseWhen3 node, final S state) {
        return combine(Stream.concat(node.whenThenPairs().stream().flatMap(T2::stream).map(operand -> visit(operand, state)),
                                     node.elseOperand().map(operand -> visit(operand, state)).stream()));
    }

    public R countAll(final CountAll3 node, final S state) {
        return defaultValue(node, state);
    }

    // :::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    // : Queries

    public R subQuery(final SubQuery3 node, final S state) {
        return visitQueryComponents(node, state);
    }

    public R sourceQuery(final SourceQuery3 node, final S state) {
        return visitQueryComponents(node, state);
    }

    public R subQueryForExists(final SubQueryForExists3 node, final S state) {
        return visitQueryComponents(node, state);
    }

    public R resultQuery(final ResultQuery3 node, final S state) {
        return visitQueryComponents(node, state);
    }

    /// Visits the node-valued [AbstractQuery3] components shared by all query types.
    ///
    protected R visitQueryComponents(final AbstractQuery3 node, final S state) {
        return combine(Stream.of(visitOptional(node.maybeJoinRoot, state),
                                 visit(node.whereConditions, state),
                                 visit(node.yields, state),
                                 visit(node.groups, state),
                                 visit(node.orderings, state)));
    }

    // :::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    // : Conditions

    public R conditions(final Conditions3 node, final S state) {
        return combine(node.allConditionsAsDnf().stream().flatMap(Collection::stream).map(cond -> visit(cond, state)));
    }

    public R comparisonPredicate(final ComparisonPredicate3 node, final S state) {
        return combine(visit(node.leftOperand(), state), visit(node.rightOperand(), state));
    }

    public R nullPredicate(final NullPredicate3 node, final S state) {
        return visit(node.operand(), state);
    }

    public R likePredicate(final LikePredicate3 node, final S state) {
        return combine(visit(node.matchOperand(), state), visit(node.patternOperand(), state));
    }

    public R setPredicate(final SetPredicate3 node, final S state) {
        return combine(visit(node.leftOperand(), state), visit(node.rightOperand(), state));
    }

    public R existencePredicate(final ExistencePredicate3 node, final S state) {
        return visit(node.subQuery(), state);
    }

    public R quantifiedPredicate(final QuantifiedPredicate3 node, final S state) {
        return combine(visit(node.leftOperand(), state), visit(node.rightOperand(), state));
    }

    // :::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    // : Join nodes

    public R joinLeafNode(final JoinLeafNode3 node, final S state) {
        return visit(node.source(), state);
    }

    public R joinInnerNode(final JoinInnerNode3 node, final S state) {
        return combine(Stream.of(visit(node.leftNode(), state),
                                 visit(node.rightNode(), state),
                                 visit(node.joinConditions(), state)));
    }

    // :::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    // : Set operands

    public R operandsBasedSet(final OperandsBasedSet3 node, final S state) {
        return visitAll(node.operands(), state);
    }

    public R queryBasedSet(final QueryBasedSet3 node, final S state) {
        return visit(node.model(), state);
    }

    // :::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    // : Sources

    public R sourceBasedOnTable(final Source3BasedOnTable node, final S state) {
        return defaultValue(node, state);
    }

    public R sourceBasedOnQueries(final Source3BasedOnQueries node, final S state) {
        return visitAll(node.models, state);
    }

    // :::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    // : Sundries

    protected R yields(final Yields3 node, final S state) {
        return combine(node.yieldsMap().values().stream().map(yield -> visit(yield, state)));
    }

    protected R groupBys(final GroupBys3 node, final S state) {
        return visitAll(node.groups(), state);
    }

    protected R orderBys(final OrderBys3 node, final S state) {
        return visitAll(node.list(), state);
    }

    public R yield(final Yield3 node, final S state) {
        return visit(node.operand(), state);
    }

    public R groupBy(final GroupBy3 node, final S state) {
        return visit(node.operand(), state);
    }

    public R orderByOperand(final IOrderBy3.Operand node, final S state) {
        return visit(node.operand(), state);
    }

    public R orderByYield(final IOrderBy3.Yield node, final S state) {
        return defaultValue(node, state);
    }

    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    // : Utilities

    protected R visitAll(final Collection<? extends INode3> nodes, final S state) {
        return combine(nodes.stream().map(node -> visit(node, state)));
    }

    protected R visitNullable(final @Nullable INode3 node, final S state) {
        return node != null ? visit(node, state) : identity();
    }

    protected R visitOptional(final Optional<? extends INode3> maybeNode, final S state) {
        return visitNullable(maybeNode.orElse(null), state);
    }

}
