package ua.com.fielden.platform.eql.stage3;

import com.google.common.collect.ImmutableList;
import jakarta.inject.Inject;
import ua.com.fielden.platform.entity.exceptions.InvalidStateException;
import ua.com.fielden.platform.eql.meta.PropType;
import ua.com.fielden.platform.eql.stage3.conditions.*;
import ua.com.fielden.platform.eql.stage3.operands.*;
import ua.com.fielden.platform.eql.stage3.operands.functions.*;
import ua.com.fielden.platform.eql.stage3.queries.*;
import ua.com.fielden.platform.eql.stage3.sources.IJoinNode3;
import ua.com.fielden.platform.eql.stage3.sources.JoinInnerNode3;
import ua.com.fielden.platform.eql.stage3.sources.JoinLeafNode3;
import ua.com.fielden.platform.eql.stage3.sources.Source3BasedOnQueries;
import ua.com.fielden.platform.eql.stage3.sundries.*;
import ua.com.fielden.platform.types.tuples.T2;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

import static ua.com.fielden.platform.types.tuples.T2.t2;

/// A unary visitor that implements the _persistent update_ of the AST.
///
/// This visitor accepts an _update function_ that decides what to do with a node.
///
/// In addition to the rules documented in [Action], the following apply:
///
/// 1. `x` is the currently visited node, `f(x, k)` is the update function, where `k` is an [Action].
///
/// 2. `f` must return by calling `k`.
///
/// 3. If `f` calls [Action#descend], the children of `x` are visited, denoted by `[c]`.
///    If any `c` in `[c]` is updated (i.e., `c_new != c_old` by reference), `x` is reconstructed with the new children.
///
///    As a reconstructed `x` is returned by `k`, `f` is able to further inspect it and even update it with [Action#update].
///    ```
///    (x, k) -> {
///      if (x instanceof SumOf3) {
///        final var newX = (SumOf3) k.descend();               // children rewritten first
///        return k.update(new IfNull3(newX, zero, newX.type)); // then rewrite the parent
///      }
///      return k.descend();
///    }
///    ```
///
/// The update is persistent because it maximises sharing -- for any updated node, only the branch leading to it from the root
/// is replaced, while the rest of the tree is shared.
///
/// ## Limitations
///
/// ### Types are carried over unchanged
///
/// Operand types, represented by [PropType], and stored by [ISingleOperand3] are carried over unchanged when a node is reconstructed.
///
/// E.g., an update that replaces all integer values by [BigDecimal] values will turn [Expression3] `1 + 2` into `1.0 + 2.0`,
/// but the reconstucted [Expression3] will retain its original type [PropType#INTEGER_PROP_TYPE].
///
/// Type checking is not this visitor's responsibility.
/// This limitation should be addressed by moving type information out of nodes into an external facility.
///
public class UpdateVisitor extends AbstractVisitor<INode3, UpdateVisitor.State> {

    public interface Action {
        /// Update the currently visited node with `newNode`, without descending further.
        ///
        INode3 update(INode3 newNode);

        /// Descend into the children of the currently visited node.
        /// If the node is a leaf, the traversal stops (equivalent to [#stop]).
        ///
        /// Returns the visited node itself when nothing beneath it changed, otherwise a reconstruction of the node.
        /// The type of the returned node is always the same as that of the currently visited node.
        ///
        INode3 descend();

        /// Stop the traversal of the current branch.
        /// E.g., if the currently visited node is [SubQuery3], it will not be updated and its children will not be processed.
        ///
        INode3 stop();
    }

    // TODO Make protected once EQL tests are refactored using IoC.
    @Inject
    public UpdateVisitor() {}

    interface State extends BiFunction<INode3, Action, INode3> {}

    @Override
    protected INode3 defaultValue(final INode3 node, final State state) {
        return state.apply(node, new Action() {
            @Override
            public INode3 update(final INode3 newNode) {
                return newNode;
            }

            @Override
            public INode3 descend() {
                // Nowhere to descend.
                return node;
            }

            @Override
            public INode3 stop() {
                return node;
            }
        });
    }

    /// Undefined for the same reason as specified in [#combine].
    ///
    @Override
    protected INode3 identity() {
        throw new InvalidStateException("Unexpected");
    }

    /// Undefined.
    /// There is no generic `combine` operation for this visitor.
    /// Instead, `combine` is specialised for each composite node type -- the node constructor.
    ///
    @Override
    protected INode3 combine(final INode3 a, final INode3 b) {
        throw new InvalidStateException("Unexpected");
    }

    // :::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    // : Operands

    @Override
    public INode3 expression(final Expression3 node, final State state) {
        return update(node, state, it -> {
            final var firstOperand = (ISingleOperand3) visit(node.firstOperand, state);
            final var otherOperands = updateAll(node.otherOperands, state);
            return firstOperand != node.firstOperand || otherOperands != node.otherOperands
                    ? new Expression3(firstOperand, otherOperands, node.type)
                    : it;
        });
    }

    @Override
    public INode3 compoundSingleOperand(final CompoundSingleOperand3 node, final State state) {
        return update(node, node.operand(), state, operand -> new CompoundSingleOperand3(operand, node.operator()));
    }

    // :::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    // : Single-operand functions

    @Override
    public INode3 absOf(final AbsOf3 node, final State state) {
        return update(node, node.operand, state, operand -> new AbsOf3(operand, node.type));
    }

    @Override
    public INode3 ceil(final Ceil3 node, final State state) {
        return update(node, node.operand, state, operand -> new Ceil3(operand, node.type));
    }

    @Override
    public INode3 floor(final Floor3 node, final State state) {
        return update(node, node.operand, state, operand -> new Floor3(operand, node.type));
    }

    @Override
    public INode3 dateOf(final DateOf3 node, final State state) {
        return update(node, node.operand, state, operand -> new DateOf3(operand, node.type));
    }

    @Override
    public INode3 dayOf(final DayOf3 node, final State state) {
        return update(node, node.operand, state, operand -> new DayOf3(operand, node.type));
    }

    @Override
    public INode3 dayOfWeekOf(final DayOfWeekOf3 node, final State state) {
        return update(node, node.operand, state, operand -> new DayOfWeekOf3(operand, node.type));
    }

    @Override
    public INode3 monthOf(final MonthOf3 node, final State state) {
        return update(node, node.operand, state, operand -> new MonthOf3(operand, node.type));
    }

    @Override
    public INode3 yearOf(final YearOf3 node, final State state) {
        return update(node, node.operand, state, operand -> new YearOf3(operand, node.type));
    }

    @Override
    public INode3 hourOf(final HourOf3 node, final State state) {
        return update(node, node.operand, state, operand -> new HourOf3(operand, node.type));
    }

    @Override
    public INode3 minuteOf(final MinuteOf3 node, final State state) {
        return update(node, node.operand, state, operand -> new MinuteOf3(operand, node.type));
    }

    @Override
    public INode3 secondOf(final SecondOf3 node, final State state) {
        return update(node, node.operand, state, operand -> new SecondOf3(operand, node.type));
    }

    @Override
    public INode3 lowerCaseOf(final LowerCaseOf3 node, final State state) {
        return update(node, node.operand, state, operand -> new LowerCaseOf3(operand, node.type));
    }

    @Override
    public INode3 upperCaseOf(final UpperCaseOf3 node, final State state) {
        return update(node, node.operand, state, operand -> new UpperCaseOf3(operand, node.type));
    }

    @Override
    public INode3 maxOf(final MaxOf3 node, final State state) {
        return update(node, node.operand, state, operand -> new MaxOf3(operand, node.type));
    }

    @Override
    public INode3 minOf(final MinOf3 node, final State state) {
        return update(node, node.operand, state, operand -> new MinOf3(operand, node.type));
    }

    @Override
    public INode3 sumOf(final SumOf3 node, final State state) {
        return update(node, node.operand, state, operand -> new SumOf3(operand, node.distinct, node.type));
    }

    @Override
    public INode3 countOf(final CountOf3 node, final State state) {
        return update(node, node.operand, state, operand -> new CountOf3(operand, node.distinct, node.type));
    }

    @Override
    public INode3 averageOf(final AverageOf3 node, final State state) {
        return update(node, node.operand, state, operand -> new AverageOf3(operand, node.distinct, node.type));
    }

    // :::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    // : Two-operand functions

    @Override
    public INode3 ifNull(final IfNull3 node, final State state) {
        return update(node, node.operand1, node.operand2, state,
                      (operand1, operand2) -> new IfNull3(operand1, operand2, node.type));
    }

    @Override
    public INode3 roundTo(final RoundTo3 node, final State state) {
        return update(node, node.operand1, node.operand2, state,
                      (operand1, operand2) -> new RoundTo3(operand1, operand2, node.type));
    }

    @Override
    public INode3 addDateInterval(final AddDateInterval3 node, final State state) {
        return update(node, node.operand1, node.operand2, state,
                      (operand1, operand2) -> new AddDateInterval3(operand1, node.intervalUnit, operand2, node.type));
    }

    @Override
    public INode3 countDateInterval(final CountDateInterval3 node, final State state) {
        // operand1 is the period end date, operand2 -- the period start date.
        return update(node, node.operand1, node.operand2, state,
                      (operand1, operand2) -> new CountDateInterval3(node.intervalUnit, operand1, operand2, node.type));
    }

    @Override
    public INode3 concatOf(final ConcatOf3 node, final State state) {
        return update(node, state, it -> {
            final var operand1 = (ISingleOperand3) visit(node.operand1, state);
            final var operand2 = (ISingleOperand3) visit(node.operand2, state);
            final var orderItems = updateAll(node.orderItems, state);
            return operand1 != node.operand1 || operand2 != node.operand2 || orderItems != node.orderItems
                    ? new ConcatOf3(operand1, operand2, node.type, orderItems)
                    : it;
        });
    }

    // :::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    // : Other functions

    @Override
    public INode3 concat(final Concat3 node, final State state) {
        return updateAll(node, node.operands, state, operands -> new Concat3(operands, node.type));
    }

    @Override
    public INode3 caseWhen(final CaseWhen3 node, final State state) {
        return update(node, state, it -> {
            final var whenThenPairs = node.whenThenPairs();
            final var newWhenThenPairs = ImmutableList.<T2<ICondition3, ISingleOperand3>>builderWithExpectedSize(whenThenPairs.size());
            var updated = false;
            for (final var pair : whenThenPairs) {
                final var when = (ICondition3) visit(pair._1, state);
                final var then = (ISingleOperand3) visit(pair._2, state);
                if (when != pair._1 || then != pair._2) {
                    updated = true;
                    newWhenThenPairs.add(t2(when, then));
                }
                else {
                    newWhenThenPairs.add(pair);
                }
            }
            // `elseOperand()` builds a new Optional on each call, hence the local variable to compare against.
            final var maybeElseOperand = node.elseOperand();
            final var newMaybeElseOperand = updateOptional(maybeElseOperand, state);
            return updated || newMaybeElseOperand != maybeElseOperand
                    ? new CaseWhen3(newWhenThenPairs.build(), newMaybeElseOperand.orElse(null), node.typeCast().orElse(null), node.type)
                    : it;
        });
    }

    // :::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    // : Queries

    @Override
    public INode3 subQuery(final SubQuery3 node, final State state) {
        return updateQuery(node, state, components -> new SubQuery3(components, node.type()));
    }

    @Override
    public INode3 sourceQuery(final SourceQuery3 node, final State state) {
        return updateQuery(node, state, components -> new SourceQuery3(components, node.resultType));
    }

    @Override
    public INode3 subQueryForExists(final SubQueryForExists3 node, final State state) {
        return updateQuery(node, state, SubQueryForExists3::new);
    }

    @Override
    public INode3 resultQuery(final ResultQuery3 node, final State state) {
        return updateQuery(node, state, components -> new ResultQuery3(components, node.resultType));
    }

    // :::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    // : Conditions

    @Override
    public INode3 conditions(final Conditions3 node, final State state) {
        return update(node, state, it -> {
            final var dnf = node.allConditionsAsDnf();
            final var newDnf = ImmutableList.<List<? extends ICondition3>>builderWithExpectedSize(dnf.size());
            var updated = false;
            for (final var conjunction : dnf) {
                final var newConjunction = updateAll(conjunction, state);
                updated = updated || newConjunction != conjunction;
                newDnf.add(newConjunction);
            }
            return updated ? new Conditions3(node.negated(), newDnf.build()) : it;
        });
    }

    @Override
    public INode3 comparisonPredicate(final ComparisonPredicate3 node, final State state) {
        return update(node, node.leftOperand(), node.rightOperand(), state,
                      (leftOperand, rightOperand) -> new ComparisonPredicate3(leftOperand, node.operator(), rightOperand));
    }

    @Override
    public INode3 nullPredicate(final NullPredicate3 node, final State state) {
        return update(node, node.operand(), state, operand -> new NullPredicate3(operand, node.negated()));
    }

    @Override
    public INode3 likePredicate(final LikePredicate3 node, final State state) {
        return update(node, node.matchOperand(), node.patternOperand(), state,
                      (matchOperand, patternOperand) -> new LikePredicate3(matchOperand, patternOperand, node.options()));
    }

    @Override
    public INode3 setPredicate(final SetPredicate3 node, final State state) {
        return update(node, node.leftOperand(), node.rightOperand(), state,
                      (leftOperand, rightOperand) -> new SetPredicate3(leftOperand, node.negated(), rightOperand));
    }

    @Override
    public INode3 existencePredicate(final ExistencePredicate3 node, final State state) {
        return update(node, node.subQuery(), state, subQuery -> new ExistencePredicate3(node.negated(), subQuery));
    }

    @Override
    public INode3 quantifiedPredicate(final QuantifiedPredicate3 node, final State state) {
        return update(node, node.leftOperand(), node.rightOperand(), state,
                      (leftOperand, rightOperand) ->
                                 new QuantifiedPredicate3(leftOperand, node.operator(), node.quantifier(), rightOperand));
    }

    // :::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    // : Join nodes

    @Override
    public INode3 joinLeafNode(final JoinLeafNode3 node, final State state) {
        return update(node, node.source(), state, JoinLeafNode3::new);
    }

    @Override
    public INode3 joinInnerNode(final JoinInnerNode3 node, final State state) {
        return update(node, state, it -> {
            final var leftNode = (IJoinNode3) visit(node.leftNode(), state);
            final var rightNode = (IJoinNode3) visit(node.rightNode(), state);
            final var joinConditions = (Conditions3) visit(node.joinConditions(), state);
            return leftNode != node.leftNode() || rightNode != node.rightNode() || joinConditions != node.joinConditions()
                    ? new JoinInnerNode3(leftNode, rightNode, node.joinType(), joinConditions)
                    : it;
        });
    }

    // :::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    // : Set operands

    @Override
    public INode3 operandsBasedSet(final OperandsBasedSet3 node, final State state) {
        return updateAll(node, node.operands(), state, OperandsBasedSet3::new);
    }

    @Override
    public INode3 queryBasedSet(final QueryBasedSet3 node, final State state) {
        return update(node, node.model(), state, QueryBasedSet3::new);
    }

    // :::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    // : Sources

    @Override
    public INode3 sourceBasedOnQueries(final Source3BasedOnQueries node, final State state) {
        return updateAll(node, node.models, state, node::update);
    }

    // :::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    // : Sundries

    @Override
    protected INode3 yields(final Yields3 node, final State state) {
        // `getYields()` returns a view of the underlying map, hence the copy into a list.
        // Reconstruction from a list re-derives the alias-to-yield map, which accounts for an updated yield alias.
        final var yields = ImmutableList.copyOf(node.getYields());
        return updateAll(node, yields, state, Yields3::new);
    }

    @Override
    protected INode3 groupBys(final GroupBys3 node, final State state) {
        return updateAll(node, node.groups(), state, GroupBys3::new);
    }

    @Override
    protected INode3 orderBys(final OrderBys3 node, final State state) {
        return updateAll(node, node.list(), state, list -> new OrderBys3(list, node.limit(), node.offset()));
    }

    @Override
    public INode3 yield(final Yield3 node, final State state) {
        return update(node, node.operand(), state,
                         operand -> new Yield3(operand, node.alias(), node.column(), node.type()));
    }

    @Override
    public INode3 groupBy(final GroupBy3 node, final State state) {
        return update(node, node.operand(), state, GroupBy3::new);
    }

    @Override
    public INode3 orderByOperand(final IOrderBy3.Operand node, final State state) {
        return update(node, node.operand(), state, operand -> new IOrderBy3.Operand(operand, node.isDesc()));
    }

    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    // : Utilities

    /// Generic implementation for nodes of any shape.
    /// `descend` is applied only if `node` itself was not updated, and must return `node` if none of its children
    /// was updated.
    ///
    protected <N extends INode3> INode3 update(final N node, final State state, final Function<N, INode3> descend) {
        return state.apply(node, new Action() {
            @Override
            public INode3 update(final INode3 newNode) {
                return newNode;
            }

            @Override
            public INode3 descend() {
                return descend.apply(node);
            }

            @Override
            public INode3 stop() {
                return node;
            }
        });
    }

    /// Generic implementation for arity-1 nodes.
    ///
    @SuppressWarnings("unchecked")
    protected <N extends INode3, C extends INode3> INode3 update(
            final N node,
            final C child,
            final State state,
            final Function<C, N> mkNode)
    {
        return update(node, state, it -> {
            final var newChild = (C) visit(child, state);
            return newChild != child ? mkNode.apply(newChild) : it;
        });
    }

    /// Generic implementation for arity-2 nodes.
    ///
    @SuppressWarnings("unchecked")
    protected <N extends INode3, C1 extends INode3, C2 extends INode3> INode3 update(
            final N node,
            final C1 child1,
            final C2 child2,
            final State state,
            final BiFunction<C1, C2, N> mkNode)
    {
        return update(node, state, it -> {
            final var newChild1 = (C1) visit(child1, state);
            final var newChild2 = (C2) visit(child2, state);
            return newChild1 != child1 || newChild2 != child2 ? mkNode.apply(newChild1, newChild2) : it;
        });
    }

    /// Generic implementation for nodes whose children form a list.
    ///
    protected <N extends INode3, C extends INode3> INode3 updateAll(
            final N node,
            final List<C> children,
            final State state,
            final Function<List<C>, N> mkNode)
    {
        return update(node, state, it -> {
            final var newChildren = updateAll(children, state);
            return newChildren != children ? mkNode.apply(newChildren) : it;
        });
    }

    /// Updates each node in `nodes`, returning `nodes` itself if none of them was updated.
    /// Callers rely on this identity to detect updates.
    ///
    @SuppressWarnings("unchecked")
    protected <C extends INode3> List<C> updateAll(final List<C> nodes, final State state) {
        final var newNodes = ImmutableList.<C>builderWithExpectedSize(nodes.size());
        var updated = false;
        for (final var node : nodes) {
            final var newNode = (C) visit(node, state);
            updated = updated || newNode != node;
            newNodes.add(newNode);
        }
        return updated ? newNodes.build() : nodes;
    }

    /// Updates the node in `maybeNode`, returning `maybeNode` itself if the node is absent or was not updated.
    /// Callers rely on this identity to detect updates.
    ///
    @SuppressWarnings("unchecked")
    protected <C extends INode3> Optional<C> updateOptional(final Optional<C> maybeNode, final State state) {
        if (maybeNode.isEmpty()) {
            return maybeNode;
        }
        else {
            final var node = maybeNode.get();
            final var newNode = (C) visit(node, state);
            return newNode != node ? Optional.of(newNode) : maybeNode;
        }
    }

    /// Generic implementation for query nodes.
    ///
    protected <N extends AbstractQuery3> INode3 updateQuery(
            final N node,
            final State state,
            final Function<QueryComponents3, N> mkNode)
    {
        return update(node, state, it -> {
            final var maybeJoinRoot = updateOptional(node.maybeJoinRoot, state);
            final var whereConditions = (Conditions3) visit(node.whereConditions, state);
            final var yields = (Yields3) visit(node.yields, state);
            final var groups = (GroupBys3) visit(node.groups, state);
            final var orderings = (OrderBys3) visit(node.orderings, state);
            if (maybeJoinRoot != node.maybeJoinRoot || whereConditions != node.whereConditions
                || yields != node.yields || groups != node.groups || orderings != node.orderings)
            {
                return mkNode.apply(new QueryComponents3(maybeJoinRoot, whereConditions, yields, groups, orderings));
            }
            else {
                return it;
            }
        });
    }

}
