package ua.com.fielden.platform.eql.stage3;

import org.apache.commons.text.RandomStringGenerator;
import ua.com.fielden.platform.entity.exceptions.InvalidStateException;
import ua.com.fielden.platform.entity.query.EntityAggregates;
import ua.com.fielden.platform.eql.stage2.TransformationContextFromStage2To3;
import ua.com.fielden.platform.eql.stage3.QueryComponents3;
import ua.com.fielden.platform.eql.stage3.conditions.*;
import ua.com.fielden.platform.eql.stage3.operands.*;
import ua.com.fielden.platform.eql.stage3.operands.functions.*;
import ua.com.fielden.platform.eql.stage3.queries.SourceQuery3;
import ua.com.fielden.platform.eql.stage3.queries.SubQuery3;
import ua.com.fielden.platform.eql.stage3.sources.*;
import ua.com.fielden.platform.eql.stage3.sundries.*;
import ua.com.fielden.platform.types.tuples.T2;
import ua.com.fielden.platform.utils.StreamUtils;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static java.util.Collections.unmodifiableList;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toCollection;
import static java.util.stream.Collectors.toSet;
import static ua.com.fielden.platform.types.tuples.T2.t2;
import static ua.com.fielden.platform.types.tuples.T2.toMap;
import static ua.com.fielden.platform.utils.StreamUtils.zip;

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

    public QueryComponents3 apply(final QueryComponents3 qc, final TransformationContextFromStage2To3 context) {
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

        final Set<ISingleOperand3> aggregated = origYields.getYields().stream()
                .flatMap(y -> extractAggregatedExpressions(y.operand()))
                .collect(toCollection(LinkedHashSet::new));
        if (aggregated.stream().allMatch(AggregationQueryWrapper::isPersistentProperty)) {
            return qc;
        }

        final var origSourceIds = streamSources(origJoin).map(ISource3::id).collect(toSet());

        final Set<Prop3> props = StreamUtils.concat(origYields.getYields().stream().map(Yield3::operand),
                                                    origGroups == null ? Stream.of() : origGroups.groups().stream().map(GroupBy3::operand),
                                                    origOrderings == null ? Stream.of() : origOrderings.list().stream().map(OrderBy3::operand).filter(Objects::nonNull))
                .flatMap(this::extractProperties)
                .filter(prop -> origSourceIds.contains(prop.source.id()))
                .filter(prop -> !aggregated.contains(prop))
                .sorted(comparing((Prop3 prop) -> prop.name).thenComparing(prop -> prop.source.id()))
                .collect(toCollection(LinkedHashSet::new));

        final List<? extends T2<? extends ISingleOperand3, String>> operandsAndAliases = zip(
                Stream.concat(props.stream(), aggregated.stream()), aliasGenerator.get(), T2::t2)
                .toList();

        final var sJoin = origJoin;
        final var sWhere = origWhere;
        final GroupBys3 sGroups = null;
        final OrderBys3 sOrderings = null;
        final var sYieldList_Context = createYields(operandsAndAliases, context);
        final var sYields = new Yields3(sYieldList_Context._1);
        final var context2 = sYieldList_Context._2;
        final var sQuery = new SourceQuery3(new QueryComponents3(Optional.of(sJoin), sWhere, sYields, sGroups, sOrderings), EntityAggregates.class);

        final var context3 = context2.cloneWithNextSqlId();
        final var topSource = new Source3BasedOnQueries(List.of(sQuery), sourceIdGenerator.get(), context3.sqlId);

        // TODO Reusing AST nodes is probably not a good idea. Create copies.
        final var replacements = operandsAndAliases.stream()
                .collect(Collectors.toMap(T2::_1,
                                          t2 -> t2.map((rand, alias) -> new Prop3(alias, topSource, rand.type()))));

        final Conditions3 topConditions = null;
        final var topYields = new Yields3(
                origYields.getYields()
                        .stream()
                        .map(y -> replaceAll(y, replacements))
                        .toList());
        final var topGroups = origGroups == null ? null : new GroupBys3(
                origGroups.groups()
                        .stream()
                        .map(g -> replaceAll(g, replacements))
                        .toList());
        final var topOrders = origOrderings == null ? null : origOrderings.updateOrderBys(
                origOrderings.list()
                        .stream()
                        .map(o -> replaceAll(o, replacements))
                        .toList());
        return new QueryComponents3(Optional.of(new JoinLeafNode3(topSource)), topConditions, topYields, topGroups, topOrders);
    }

    private T2<List<Yield3>, TransformationContextFromStage2To3> createYields(
            final List<? extends T2<? extends ISingleOperand3, String>> operandsAndAliases,
            final TransformationContextFromStage2To3 context)
    {
        var ctx = context;

        final var yields = new ArrayList<Yield3>(operandsAndAliases.size());
        for (final var it : operandsAndAliases) {
            final var operand = it._1;
            final var alias = it._2;
            ctx = ctx.cloneWithNextSqlId();
            yields.add(new Yield3(operand, alias, ctx.sqlId, operand.type()));
        }

        return t2(unmodifiableList(yields), ctx);
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
    private Stream<ISingleOperand3> extractAggregatedExpressions(final ISingleOperand3 node) {
        return switch (node) {
            // Aggregate functions
            case AverageOf3 it -> Stream.of(it.operand);
            case MinOf3 it -> Stream.of(it.operand);
            case MaxOf3 it -> Stream.of(it.operand);
            case SumOf3 it -> Stream.of(it.operand);
            case CountOf3 it -> Stream.of(it.operand);
            // `STRING_AGG`: only the aggregated value is a per-row expression.
            // The separator is a constant, and ordering items are irrelevant here.
            case ConcatOf3 it -> Stream.of(it.operand1);
            // `COUNT(*)` has no argument.
            case CountAll3 _ -> Stream.empty();
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
    private Stream<Prop3> extractProperties(final ISingleOperand3 node) {
        return switch (node) {
            // Skip aggregate functions, they are processed separately.
            case AverageOf3 _ -> Stream.empty();
            case MinOf3 _ -> Stream.empty();
            case MaxOf3 _ -> Stream.empty();
            case SumOf3 _ -> Stream.empty();
            case CountOf3 _ -> Stream.empty();
            case CountAll3 _ -> Stream.empty();
            // concatOf: skip the aggregated expression, but include order-by expressions.
            case ConcatOf3 it -> it.orderItems.stream().map(OrderBy3::operand).filter(Objects::nonNull).flatMap(this::extractProperties);
            // Skip subqueries.
            case SubQuery3 _ -> Stream.empty();
            case Prop3 it -> Stream.of(it);
            default -> streamChildren(node).flatMap(this::extractProperties);
        };
    }

    private Yield3 replaceAll(final Yield3 yield, final Map<? extends ISingleOperand3, Prop3> replacements) {
        return yield.setOperand(replace(yield.operand(), replacements));
    }

    private GroupBy3 replaceAll(final GroupBy3 groupBy, final Map<? extends ISingleOperand3, Prop3> replacements) {
        final var newOperand = replace(groupBy.operand(), replacements);
        return groupBy.setOperand(newOperand);
    }

    private OrderBy3 replaceAll(final OrderBy3 orderBy, final Map<? extends ISingleOperand3, Prop3> replacements) {
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
    private ISingleOperand3 replace(
            final ISingleOperand3 node,
            final Map<? extends ISingleOperand3, ? extends ISingleOperand3> replacements)
    {
        final var newNode = replacements.get(node);
        if (newNode != null) {
            return newNode;
        }

        final var replacedChildren = streamChildren(node)
                .map(child -> {
                    final var replacedChild = replace(child, replacements);
                    return replacedChild == child ? null : t2(child, replacedChild);
                })
                .filter(Objects::nonNull)
                // If some child nodes are equal, use any one as the key -- the lookup will work by hashCode and equals.
                .collect(toMap((v1, _) -> v1));
        return replaceChildren(node, replacedChildren);
    }


    /// Reconstructs `node` by replacing all of its immediate children that are contained in `replacements`.
    ///
    /// @param replacements  a mapping between old nodes to be replaced and new nodes to take their place
    ///
    private ISingleOperand3 replaceChildren(final ISingleOperand3 node, final Map<ISingleOperand3, ISingleOperand3> replacements) {
        if (replacements.isEmpty()) {
            return node;
        }

        return switch (node) {
            case SingleOperandFunction3 it -> it.setOperand(replacements.getOrDefault(it.operand, it.operand));
            case ConcatOf3 it -> it.update(
                    replacements.getOrDefault(it.operand1, it.operand1),
                    replacements.getOrDefault(it.operand2, it.operand2),
                    replaceChildren(it.orderItems, replacements));
            case TwoOperandsFunction3 it -> {
                final var newOperand1 = replacements.getOrDefault(it.operand1, it.operand1);
                final var newOperand2 = replacements.getOrDefault(it.operand2, it.operand2);
                yield it.setOperands(newOperand1, newOperand2);
            }
            case Expression3 it when streamChildren(it).anyMatch(replacements::containsKey)
                    -> it.update(replacements.getOrDefault(it.firstOperand, it.firstOperand),
                                 it.otherOperands.stream().map(item -> item.setOperand(replacements.getOrDefault(item.operand(), item.operand()))).toList());
            case Concat3 it when it.operands().stream().anyMatch(replacements::containsKey)
                    -> it.setOperands(it.operands().stream().map(rand -> replacements.getOrDefault(rand, rand)).collect(toImmutableList()));
            // For case-when, also consider immediate children within the "when" conditions.
            case CaseWhen3 it -> it.update(it.whenThenPairs().stream()
                                                   .map(t2 -> t2.map((when, then) -> t2(replaceChildren(when, replacements), replacements.getOrDefault(then, then))))
                                                   .toList(),
                                           it.elseOperand() == null ? null : replacements.getOrDefault(it.elseOperand(), it.elseOperand()),
                                           it.typeCast());
            default -> node;
        };
    }

    private List<OrderBy3> replaceChildren(
            final List<OrderBy3> orderBys,
            final Map<ISingleOperand3, ISingleOperand3> replacements)
    {
        return orderBys.stream().anyMatch(o -> o.operand() != null && replacements.containsKey(o.operand()))
                ? orderBys.stream().map(o -> o.setOperand(replacements.get(o.operand()))).collect(toImmutableList())
                : orderBys;
    }

    /// Reconstructs `condition` by replacing all of its immediate children that are contained in `replacements`.
    ///
    /// @param replacements  a mapping between old nodes to be replaced and new nodes to take their place
    ///
    private ICondition3 replaceChildren(
            final ICondition3 condition,
            final Map<ISingleOperand3, ISingleOperand3> replacements)
    {
        return switch (condition) {
            case ComparisonPredicate3 it -> it.update(replacements.getOrDefault(it.leftOperand(), it.leftOperand()),
                                                      it.operator(),
                                                      replacements.getOrDefault(it.rightOperand(), it.rightOperand()));
            case NullPredicate3 it -> it.update(replacements.getOrDefault(it.operand(), it.operand()), it.negated());
            case LikePredicate3 it -> it.update(replacements.getOrDefault(it.matchOperand(), it.matchOperand()),
                                                replacements.getOrDefault(it.patternOperand(), it.patternOperand()),
                                                it.options());
            case SetPredicate3 it -> it.update(replacements.getOrDefault(it.leftOperand(), it.leftOperand()),
                                               it.negated(),
                                               switch (it.rightOperand()) {
                                                   case QueryBasedSet3 set -> set;
                                                   case OperandsBasedSet3 set -> set.update(set.operands().stream().map(rand -> replacements.getOrDefault(rand, rand)).toList());
                                                   default -> it.rightOperand();
                                               });
            case ExistencePredicate3 it -> it; // Subquery ignored.
            case QuantifiedPredicate3 it -> it.update(replacements.getOrDefault(it.leftOperand(), it.leftOperand()),
                                                      it.operator(),
                                                      it.quantifier(),
                                                      // Subquery ignored.
                                                      it.rightOperand());
            case Conditions3 it -> it.update(it.negated(),
                                             it.allConditionsAsDnf().stream()
                                                     .map(conds -> conds.stream().map(c -> replaceChildren(c, replacements)).collect(toImmutableList()))
                                                     .collect(toImmutableList()));
            default -> condition;
        };
    }

    /// Given a composite node, returns its immediate children.
    /// [Subqueries][SubQuery3] are ignored.
    ///
    private Stream<ISingleOperand3> streamChildren(final ISingleOperand3 node) {
        return switch (node) {
            case ConcatOf3 it -> Stream.concat(Stream.of(it.operand1, it.operand2), it.orderItems.stream().map(OrderBy3::operand));
            case SingleOperandFunction3 it -> Stream.of(it.operand);
            case TwoOperandsFunction3 it -> Stream.of(it.operand1, it.operand2);
            case Expression3 it -> Stream.concat(Stream.of(it.firstOperand), it.otherOperands.stream().map(CompoundSingleOperand3::operand));
            case Concat3 it -> it.operands().stream();
            // Case-when is special: operands within conditions are not immediate children but are included.
            case CaseWhen3 it -> Stream.concat(
                    it.whenThenPairs().stream().flatMap(t2 -> t2.map((when, then) -> Stream.concat(streamChildren(when), Stream.of(then)))),
                    Optional.ofNullable(it.elseOperand()).stream());
            default -> Stream.empty();
        };
    }

    private Stream<? extends ISingleOperand3> streamChildren(final ICondition3 condition) {
        return switch (condition) {
            case ComparisonPredicate3 it -> Stream.of(it.leftOperand(), it.rightOperand());
            case NullPredicate3 it -> Stream.of(it.operand());
            case LikePredicate3 it -> Stream.of(it.matchOperand(), it.patternOperand());
            case SetPredicate3 it -> Stream.concat(
                    Stream.of(it.leftOperand()),
                    switch (it.rightOperand()) {
                        case OperandsBasedSet3 set -> set.operands().stream();
                        case QueryBasedSet3 _ -> Stream.of(); // Subquery ignored.
                        default -> Stream.of();
                    });
            case ExistencePredicate3 _ -> Stream.of(); // Subquery ignored.
            case QuantifiedPredicate3 it -> Stream.of(it.leftOperand()); // Subquery ignored.
            case Conditions3 it -> it.allConditionsAsDnf().stream().flatMap(List::stream).flatMap(this::streamChildren);
            default -> Stream.of();
        };
    }

    private Stream<ISource3> streamSources(final IJoinNode3 origJoin) {
        return switch (origJoin) {
            case JoinLeafNode3 it -> Stream.of(it.source());
            case JoinInnerNode3 it -> Stream.concat(streamSources(it.leftNode()), streamSources(it.rightNode()));
            default -> throw new InvalidStateException("Unsupported join node type: %s".formatted(origJoin.getClass().getName()));
        };
    }

    private static boolean isPersistentProperty(final ISingleOperand3 rand) {
        // All stage 3 properties are persistent.
        // Calculated properties are expanded before stage 3.
        return rand instanceof Prop3;
    }

    private static Integer nextSourceId() {
        return random.nextInt();
    }

    private static Stream<String> generateAliases() {
        return IntStream.iterate(1, i -> i + 1).mapToObj(i -> "c" + i);
    }

}
