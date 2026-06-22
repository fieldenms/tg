package ua.com.fielden.platform.eql.stage1;

import org.apache.commons.text.RandomStringGenerator;
import ua.com.fielden.platform.entity.exceptions.InvalidStateException;
import ua.com.fielden.platform.entity.query.EntityAggregates;
import ua.com.fielden.platform.eql.meta.query.QuerySourceInfo;
import ua.com.fielden.platform.eql.stage2.QueryComponents2;
import ua.com.fielden.platform.eql.stage2.conditions.*;
import ua.com.fielden.platform.eql.stage2.operands.*;
import ua.com.fielden.platform.eql.stage2.operands.functions.*;
import ua.com.fielden.platform.eql.stage2.queries.SourceQuery2;
import ua.com.fielden.platform.eql.stage2.queries.SubQuery2;
import ua.com.fielden.platform.eql.stage2.sources.*;
import ua.com.fielden.platform.eql.stage2.sundries.*;
import ua.com.fielden.platform.eql.stage3.conditions.ICondition3;
import ua.com.fielden.platform.eql.stage3.operands.ISingleOperand3;
import ua.com.fielden.platform.types.tuples.T2;
import ua.com.fielden.platform.utils.StreamUtils;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toCollection;
import static java.util.stream.Collectors.toSet;
import static ua.com.fielden.platform.types.tuples.T2.t2;
import static ua.com.fielden.platform.types.tuples.T2.toMap;

/// This transformation is applicable only if the query yields an aggregation.
/// Otherwise, it is a no-op.
///
/// In practice, this transformation enables aggregation over sub-queries, which is not supported in SQL Server.
///
/// ## Transformation algorithm
///
/// Given a query `Q = {S, W, O, G, Y}`, where:
/// * `S` - source
/// * `W` - where conditions
/// * `O` - order by list
/// * `G` - group by list
/// * `Y` - yields
///
/// The transformation is applicable only if `Y` contains a yield `y = Yield(operand)` such that `operand` contains an aggregate function at the level of `S`.
///
/// Note: expression `e` is said to be at the level of `S` of query `Q` with yields `Y` iff (`e` is one of the yields in `Y`) OR (there is a yield in `Y` that contains `e` AND that yield is not a sub-query).
///
/// Examples of expression at and below the level of query source `S`:
/// ```
/// -- At the level of S
/// select max(...) from S
///
/// -- Below the level of S
/// select (select max(...) from T where ...) from S
/// ```
///
/// The transformation results in a new query `Qt = {St, Wt, Ot, Gt, Yt}`, where:
///
/// `St` - a source query `{Ss, Ws, Os, Gs, Ys}`, where:
///
/// * `Ss = S` -- same source as the original query.
///
/// * `Ws = W` -- same conditions as the original query.
///
/// * `Os, Gs = empty` -- ordering and grouping apply to the outer query only.
///
/// * `Ys = collectProps(O) + collectProps(G) + collectProps(Y) + flatmap(extractAgg, Y)`
///
///   `collectProps(x)` -- all properties of `S` referenced in clause `x`.
///
///   `extractAgg(y)` -- all expressions `x` that appear in yield `y` as arguments of aggregate functions at the level of S.
///   Examples:
///   ```
///   max().prop(cost) => [prop(cost)]
///   sum().prop(labourCost).add().sum().prop(orderCost) => [prop(labourCost), prop(orderCost)]
///   ifNull().sum().prop(cost).then().val(0) => [prop(cost)]
///   ```
///
///   The core of the transformation is the extraction of arguments to aggregate functions.
///   It will materialise them as columns of source query `St` so that they can be used in the outer query `Qt`.
///
///   Collection of all properties of `S` referenced in the original `O`, `G` and `Y` is necessary because the transformed query `Qt` will no longer access `S`, but `St` instead.
///   Therefore, all referenced properties have to be yielded from `St`.
///
/// The transformation of `S` into `St` has the following effects:
///
/// 1. Affects all expressions that were referencing properties of `S`.
///
///    Adjusting them is simple: replace each reference to a property of `S` with a corresponding property of `St` (i.e., a yield in `Ys`).
///
/// 2. `St` contains extra yields corresponding to the arguments of aggregate functions contained in the original `Y`.
///
///    It is important to use them in the outer query as this is the essence of the whole transformation -- pushing the aggregated expressions down into a source query.
///    This will be achieved by transforming the original yields `Y` into `Yt`.
///
/// `Wt = empty` -- conditions are applied in `St`.
///
/// `Ot = [transform(o) for o in O]`
/// * `transform(o)` -- replace each referenced property of `S` with a corresponding property of `St`.
///
/// `Gt = [transform(g) for g in G]`
/// * `transform(g)` -- replace each referenced property of `S` with a corresponding property of `St`.
///
/// `Yt = [transform(y) for y in Y]`
/// * `transform(y)` -- replace each referenced property of `S` with a corresponding property of `St` AND replace each expression `x` matched earlier by `extractAgg(y)` with a corresponding property of `St`.

public final class AggregationQueryWrapper {

    public static final AggregationQueryWrapper INSTANCE = new AggregationQueryWrapper();

    static boolean enabled = true;

    private AggregationQueryWrapper() {}

    private static final RandomStringGenerator stringGenerator = new RandomStringGenerator.Builder().withinRange(new char[]{'a', 'z'}, new char[]{'0', '9'}).get();
    private static final Random random = new Random();

    private static Supplier<Integer> sourceIdGenerator = AggregationQueryWrapper::nextSourceId;
    private static Supplier<Stream<String>> aliasGenerator = AggregationQueryWrapper::generateAliases;

    static void setSourceIdGenerator(final Supplier<Integer> generator) {
        sourceIdGenerator = generator;
    }

    static void resetSourceIdGenerator() {
        sourceIdGenerator = AggregationQueryWrapper::nextSourceId;
    }

    static void setAliasGenerator(final Supplier<Stream<String>> generator) {
        aliasGenerator = generator;
    }

    static void resetAliasGenerator() {
        aliasGenerator = AggregationQueryWrapper::generateAliases;
    }

    public QueryComponents2 apply(final QueryComponents2 qc, final TransformationContextFromStage1To2 context) {
        if (!enabled) {
            return qc;
        }
        if (qc.maybeJoinRoot().isEmpty()) {
            return qc;
        }
        final var origJoin = qc.maybeJoinRoot().get();
        final var origWhere = qc.whereConditions();
        final var origYields = qc.yields();
        final var origGroups = qc.groups();
        final var origOrderings = qc.orderings();

        final Set<ISingleOperand2<?>> aggregated = origYields.getYields().stream()
                .flatMap(y -> extractAggregatedExpressions(y.operand()))
                .collect(toCollection(LinkedHashSet::new));
        if (aggregated.stream().allMatch(AggregationQueryWrapper::isPersistentProperty)) {
            return qc;
        }

        final var origSourceIds = streamSources(origJoin).map(ISource2::id).collect(toSet());

        final Set<Prop2> props = StreamUtils.concat(origYields.getYields().stream().map(Yield2::operand),
                                                    origGroups.groups().stream().map(GroupBy2::operand),
                                                    origOrderings.orderBys().stream().map(OrderBy2::operand).filter(Objects::nonNull))
                .flatMap(this::extractProperties)
                .filter(prop -> origSourceIds.contains(prop.source.id()))
                .filter(prop -> !aggregated.contains(prop))
                .sorted(comparing((Prop2 prop1) -> prop1.propPath).thenComparing(prop1 -> prop1.source.id()))
                .collect(toCollection(LinkedHashSet::new));

        final List<? extends T2<? extends ISingleOperand2<?>, String>> operandsAndAliases = StreamUtils.zip(
                Stream.concat(props.stream(), aggregated.stream()), aliasGenerator.get(), T2::t2)
                .toList();

        final var sJoin = origJoin;
        final var sWhere = origWhere;
        final var sGroups = GroupBys2.EMPTY_GROUP_BYS;
        final var sOrderings = OrderBys2.EMPTY_ORDER_BYS;
        final var sYields = new Yields2(operandsAndAliases.stream()
                .map(t2 -> t2.map((rand, alias) -> new Yield2(rand, alias, false)))
                .toList());
        final var sQuery = new SourceQuery2(Optional.of(sJoin), sWhere, sYields, sGroups, sOrderings, EntityAggregates.class);
        final QuerySourceInfo<?> newQuerySourceInfo = context.querySourceInfoProvider.produceQuerySourceInfoForEntityType(List.of(sQuery), EntityAggregates.class, false);

        final var topSource = new Source2BasedOnQueries(List.of(sQuery), null, sourceIdGenerator.get(), newQuerySourceInfo, false, true, context.isForCalcProp);

        // TODO Reusing AST nodes is probably not a good idea. Create copies.
        final var replacements = operandsAndAliases.stream()
                .collect(Collectors.toMap(T2::_1,
                                          t2 -> t2.map((_, alias) -> new Prop2(topSource, List.of(newQuerySourceInfo.getProps().get(alias)), false))));

        final var topConditions = Conditions2.EMPTY_CONDITIONS;
        final var topYields = new Yields2(
                origYields.getYields()
                        .stream()
                        .map(y -> replaceAll(y, replacements))
                        .toList());
        final var topGroups = new GroupBys2(
                origGroups.groups()
                        .stream()
                        .map(g -> replaceAll(g, replacements))
                        .toList());
        final var topOrders = origOrderings.updateOrderBys(
                origOrderings.orderBys()
                        .stream()
                        .map(o -> replaceAll(o, replacements))
                        .toList());
        return new QueryComponents2(Optional.of(new JoinLeafNode2(topSource)), topConditions, topYields, topGroups, topOrders);
    }

    /// Extracts the argument expression of every aggregate function occurring at the same level as the query source
    /// (i.e., not inside a subquery) within `node`.
    /// These are the per-row expressions that the source query must materialise as columns, so that the enclosing query
    /// can aggregate over them.
    ///
    /// An aggregate function's argument is emitted as-is and is never descended into, as SQL forbids nested aggregations.
    /// Nodes that are not aggregate functions are traversed to discover aggregations nested within them.
    /// A sub-query is an exception: any aggregation it contains belongs to a deeper level and must not be extracted --
    /// this is what confines extraction to the level of the source.
    ///
    private Stream<ISingleOperand2<?>> extractAggregatedExpressions(final ISingleOperand2<? extends ISingleOperand3> node) {
        return switch (node) {
            // Aggregate functions
            case AverageOf2 it -> Stream.of(it.operand);
            case MinOf2 it -> Stream.of(it.operand);
            case MaxOf2 it -> Stream.of(it.operand);
            case SumOf2 it -> Stream.of(it.operand);
            case CountOf2 it -> Stream.of(it.operand);
            // `STRING_AGG`: only the aggregated value is a per-row expression.
            // The separator is a constant, and ordering items are irrelevant here.
            case ConcatOf2 it -> Stream.of(it.operand1);
            // `COUNT(*)` has no argument.
            case CountAll2 _ -> Stream.empty();
            default -> streamChildren(node).flatMap(this::extractAggregatedExpressions);
        };
    }

    /// Extracts properties occurring at the same level as the query source (i.e., not inside a subquery) within `node`.
    /// These are the per-row properties that the source query must materialise as columns, so that the enclosing query
    /// can reference them.
    ///
    /// Aggregate functions are never descended into as they are processed separately with [#extractAggregatedExpressions].
    /// Subqueries are also skipped as any properties within them are at a different level than the original query source.
    ///
    private Stream<Prop2> extractProperties(final ISingleOperand2<? extends ISingleOperand3> node) {
        return switch (node) {
            // Skip aggregate functions, they are processed separately.
            case AverageOf2 it -> Stream.empty();
            case MinOf2 it -> Stream.empty();
            case MaxOf2 it -> Stream.empty();
            case SumOf2 it -> Stream.empty();
            case CountOf2 it -> Stream.empty();
            case ConcatOf2 it -> Stream.empty();
            case CountAll2 _ -> Stream.empty();
            // Skip subqueries.
            case SubQuery2 _ -> Stream.empty();
            case Prop2 it -> Stream.of(it);
            default -> streamChildren(node).flatMap(this::extractProperties);
        };
    }

    private Yield2 replaceAll(final Yield2 yield, final Map<? extends ISingleOperand2<?>, Prop2> replacements) {
        return yield.setOperand(replace(yield.operand(), replacements));
    }

    private GroupBy2 replaceAll(final GroupBy2 groupBy, final Map<? extends ISingleOperand2<?>, Prop2> replacements) {
        final var newOperand = replace(groupBy.operand(), replacements);
        return groupBy.setOperand(newOperand);
    }

    private OrderBy2 replaceAll(final OrderBy2 orderBy, final Map<? extends ISingleOperand2<?>, Prop2> replacements) {
        if (orderBy.operand() != null) {
            final var newOperand = replace(orderBy.operand(), replacements);
            return orderBy.setOperand(newOperand);
        }
        else {
            return orderBy;
        }
    }

    /*
    # Operations on EQL AST nodes

    The code below implements operations on a subset of EQL AST nodes -- operands, represented by [ISingleOperand2].
    TODO...
    */

    /// Reconstructs the tree rooted at `node` by replacing all reachable nodes that are contained in `replacements`.
    ///
    /// @param replacements  a mapping between old nodes to be replaced and new nodes to take their place
    ///
    private ISingleOperand2<?> replace(
            final ISingleOperand2<?> node,
            final Map<? extends ISingleOperand2<?>, ? extends ISingleOperand2<?>> replacements)
    {
        final var newNode = replacements.get(node);
        if (newNode != null) {
            return newNode;
        }

        final var replacedChildren = streamChildren(node)
                .<T2<ISingleOperand2, ISingleOperand2<? extends ISingleOperand3>>> map(child -> {
                    final var replacedChild = replace(child, replacements);
                    return replacedChild == child ? null : t2(child, replacedChild);
                })
                .filter(Objects::nonNull)
                .collect(toMap());
        return replaceChildren(node, replacedChildren);
    }


    /// Reconstructs `node` by replacing all of its immediate children that are contained in `replacements`.
    ///
    /// @param replacements  a mapping between old nodes to be replaced and new nodes to take their place
    ///
    private ISingleOperand2<?> replaceChildren(final ISingleOperand2<?> node, final Map<ISingleOperand2, ISingleOperand2<? extends ISingleOperand3>> replacements) {
        if (replacements.isEmpty()) {
            return node;
        }

        return switch (node) {
            case SingleOperandFunction2<?> it -> it.setOperand(replacements.getOrDefault(it.operand, it.operand));
            case ConcatOf2 it -> it.update(
                    replacements.getOrDefault(it.operand1, it.operand1),
                    replacements.getOrDefault(it.operand2, it.operand2),
                    replaceChildren(it.orderItems, replacements));
            case TwoOperandsFunction2<?> it -> {
                final var newOperand1 = replacements.getOrDefault(it.operand1, it.operand1);
                final var newOperand2 = replacements.getOrDefault(it.operand2, it.operand2);
                yield it.setOperands(newOperand1, newOperand2);
            }
            case Expression2 it when streamChildren(it).anyMatch(replacements::containsKey)
                    -> new Expression2(replacements.getOrDefault(it.first, it.first),
                                       it.items.stream().map(item -> item.setOperand(replacements.getOrDefault(item.operand(), item.operand()))).toList());
            case Concat2 it when it.operands().stream().anyMatch(replacements::containsKey)
                    -> it.setOperands(it.operands().stream().map(rand -> replacements.getOrDefault(rand, rand)).collect(toImmutableList()));
            // For case-when, also consider immediate children within the "when" conditions.
            case CaseWhen2 it -> it.update(it.whenThenPairs().stream()
                                                   .map(t2 -> t2.map((when, then) -> t2(replaceChildren(when, replacements), replacements.getOrDefault(then, then))))
                                                   .toList(),
                                           it.elseOperand() == null ? null : replacements.getOrDefault(it.elseOperand(), it.elseOperand()),
                                           it.typeCast());
            default -> node;
        };
    }

    private List<OrderBy2> replaceChildren(
            final List<OrderBy2> orderBys,
            final Map<ISingleOperand2, ISingleOperand2<? extends ISingleOperand3>> replacements)
    {
        return orderBys.stream().anyMatch(o -> o.operand() != null && replacements.containsKey(o.operand()))
                ? orderBys.stream().map(o -> o.setOperand(replacements.get(o.operand()))).collect(toImmutableList())
                : orderBys;
    }

    /// Reconstructs `condition` by replacing all of its immediate children that are contained in `replacements`.
    ///
    /// @param replacements  a mapping between old nodes to be replaced and new nodes to take their place
    ///
    private ICondition2<? extends ICondition3> replaceChildren(
            final ICondition2<? extends ICondition3> condition,
            final Map<ISingleOperand2, ISingleOperand2<? extends ISingleOperand3>> replacements)
    {
        return switch (condition) {
            case ComparisonPredicate2 it -> it.update(replacements.getOrDefault(it.leftOperand(), it.leftOperand()),
                                                      it.operator(),
                                                      replacements.getOrDefault(it.rightOperand(), it.rightOperand()));
            case NullPredicate2 it -> it.update(replacements.getOrDefault(it.operand(), it.operand()), it.negated());
            case LikePredicate2 it -> it.update(replacements.getOrDefault(it.matchOperand(), it.matchOperand()),
                                                replacements.getOrDefault(it.patternOperand(), it.patternOperand()),
                                                it.options());
            case SetPredicate2 it -> it.update(replacements.getOrDefault(it.leftOperand(), it.leftOperand()),
                                               it.negated(),
                                               switch (it.rightOperand()) {
                                                   case QueryBasedSet2 set -> set;
                                                   case OperandsBasedSet2 set -> set.update(set.operands().stream().map(rand -> replacements.getOrDefault(rand, rand)).toList());
                                                   default -> it.rightOperand();
                                               });
            case ExistencePredicate2 it -> it; // Subquery ignored.
            case QuantifiedPredicate2 it -> it.update(replacements.getOrDefault(it.leftOperand(), it.leftOperand()),
                                                      it.operator(),
                                                      it.quantifier(),
                                                      // Subquery ignored.
                                                      it.rightOperand());
            case Conditions2 it -> it.update(it.negated(),
                                             it.dnf().stream()
                                                     .map(conds -> conds.stream().map(c -> replaceChildren(c, replacements)).collect(toImmutableList()))
                                                     .collect(toImmutableList()));
            default -> condition;
        };
    }

    /// Given a composite node, returns its immediate children.
    /// [Subqueries][SubQuery2] are ignored.
    ///
    private Stream<ISingleOperand2<?>> streamChildren(final ISingleOperand2<?> node) {
        return switch (node) {
            case ConcatOf2 it -> Stream.concat(Stream.of(it.operand1, it.operand2), it.orderItems.stream().map(OrderBy2::operand));
            case SingleOperandFunction2<?> it -> Stream.of(it.operand);
            case TwoOperandsFunction2<?> it -> Stream.of(it.operand1, it.operand2);
            case Expression2 it -> Stream.concat(Stream.of(it.first), it.items.stream().map(CompoundSingleOperand2::operand));
            case Concat2 it -> it.operands().stream();
            // Case-when is special: operands within conditions are not immediate children but are included.
            case CaseWhen2 it -> Stream.concat(
                    it.whenThenPairs().stream().flatMap(t2 -> t2.map((when, then) -> Stream.concat(streamChildren(when), Stream.of(then)))),
                    Optional.ofNullable(it.elseOperand()).stream());
            default -> Stream.empty();
        };
    }

    private Stream<? extends ISingleOperand2<?>> streamChildren(final ICondition2<? extends ICondition3> condition) {
        return switch (condition) {
            case ComparisonPredicate2 it -> Stream.of(it.leftOperand(), it.rightOperand());
            case NullPredicate2 it -> Stream.of(it.operand());
            case LikePredicate2 it -> Stream.of(it.matchOperand(), it.patternOperand());
            case SetPredicate2 it -> Stream.concat(
                    Stream.of(it.leftOperand()),
                    switch (it.rightOperand()) {
                        case OperandsBasedSet2 set -> set.operands().stream();
                        case QueryBasedSet2 _ -> Stream.of(); // Subquery ignored.
                        default -> Stream.of();
                    });
            case ExistencePredicate2 _ -> Stream.of(); // Subquery ignored.
            case QuantifiedPredicate2 it -> Stream.of(it.leftOperand()); // Subquery ignored.
            case Conditions2 it -> it.dnf().stream().flatMap(List::stream).flatMap(this::streamChildren);
            default -> Stream.of();
        };
    }

    private Stream<ISource2<?>> streamSources(final IJoinNode2<?> origJoin) {
        return switch (origJoin) {
            case JoinLeafNode2 it -> Stream.of(it.source());
            case JoinInnerNode2 it -> Stream.concat(streamSources(it.leftNode()), streamSources(it.rightNode()));
            default -> throw new InvalidStateException("Unsupported join node type: %s".formatted(origJoin.getClass().getName()));
        };
    }

    private static boolean isPersistentProperty(final ISingleOperand2<?> rand) {
        return rand instanceof Prop2 prop && !prop.getPath().getLast().hasExpression();
    }

    private static Integer nextSourceId() {
        return random.nextInt();
    }

    private static Stream<String> generateAliases() {
        return IntStream.iterate(1, i -> i + 1).mapToObj(i -> "c" + i);
    }

}
