package ua.com.fielden.platform.eql.stage3;

import jakarta.annotation.Nullable;
import ua.com.fielden.platform.entity.query.DbVersion;
import ua.com.fielden.platform.entity.query.EntityAggregates;
import ua.com.fielden.platform.eql.stage2.TransformationContextFromStage2To3;
import ua.com.fielden.platform.eql.stage2.TransformationResultFromStage2To3;
import ua.com.fielden.platform.eql.stage3.conditions.*;
import ua.com.fielden.platform.eql.stage3.operands.*;
import ua.com.fielden.platform.eql.stage3.operands.functions.*;
import ua.com.fielden.platform.eql.stage3.queries.SourceQuery3;
import ua.com.fielden.platform.eql.stage3.queries.SubQuery3;
import ua.com.fielden.platform.eql.stage3.sources.JoinLeafNode3;
import ua.com.fielden.platform.eql.stage3.sources.Source3BasedOnQueries;
import ua.com.fielden.platform.eql.stage3.sundries.*;
import ua.com.fielden.platform.types.tuples.T2;
import ua.com.fielden.platform.utils.StreamUtils;

import java.util.*;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static java.util.Collections.unmodifiableList;
import static ua.com.fielden.platform.eql.stage2.TransformationResultFromStage2To3.skipTransformation;
import static ua.com.fielden.platform.types.tuples.T2.t2;
import static ua.com.fielden.platform.types.tuples.T2.toMap;
import static ua.com.fielden.platform.utils.StreamUtils.zip;

/// Transforms a query that yields an aggregation by materialising the aggregated expression (the aggregate function's argument)
/// in a source query created on top of the original source.
///
/// This transformation is applicable only if the query yields or orders by an aggregation.
/// Otherwise, it is a no-op.
/// In particular, group-by operands alone do not make a query eligible, regardless of their complexity.
/// It is also skipped if a yield or an order-by contains a subquery that would not be materialised (see Limitations).
///
/// This transformation applies only to SQL Server.
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
/// The transformation is applicable iff a query yields or orders by an aggregation.
/// I.e., `Y` contains a yield `y = Yield(operand)` (or `O` contains an order-by with an operand) such that `operand` contains an aggregate function at the level of `S`.
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
/// A query that yields an aggregation has the following properties:
/// * `G` may be empty or may contain non-aggregate expressions (e.g., `prop`).
/// * `Y` contains at least one aggregation.
/// * For each `y` in `Y`, if `y` is NOT an aggregation and references `S`, it may do so only through an expression from `G`.
///   ```
///   .groupBy().prop("key")
///   .yield().prop("key")
///   .yield().lowerCase().prop("key")
///   .yield().prop("id") -- Invalid
///   ```
/// * For each `o` in `O`, if `o` is NOT an aggregation and references `S`, it may do so only through an expression from `G`.
///   ```
///   .groupBy().prop("key")
///   .orderBy().prop("key").asc()
///             .lowerCase().prop("key").asc()
///             .prop("id").asc() -- Invalid
///   ```
///
/// Based on these properties, it can be seen that any reference to `S` is either within an aggregation or within an expression in `G`.
///
/// The transformation consists of the following steps:
/// 1. Materialise all expressions that are aggregated over and all expressions in `G`.
/// 2. Replace all original expressions with their materialised counterparts.
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
/// * `Ys = flatmap(extractAgg, Y) + G + flatmap(extractAgg, O)` -- everything that has to be materialised.
///
///   `extractAgg(node)` -- all expressions `x` that appear in `node` as arguments of aggregate functions at the level of S.
///   Examples:
///   ```
///   maxOf().prop(cost) => [prop(cost)]
///   sumOf().prop(labourCost).add().sumOf().prop(orderCost) => [prop(labourCost), prop(orderCost)]
///   ifNull().sumOf().prop(cost).then().val(0) => [prop(cost)]
///   ```
///
///   Group-by expressions in `G` also have to be materialised as they may reference `S`.
///
///   Aggregate arguments in `O` are materialised for the same reason as those in `Y`: the outer order-by aggregates
///   over columns of the source query, not over the original source.
///
/// `Wt = empty` -- conditions are applied in `St`.
///
/// `Ot = [transform(o) for o in O]`
///
/// `Gt = [transform(g) for g in G]`
///
/// `Yt = [transform(y) for y in Y]`
///
/// `transform(x)` -- replaces all expressions in `x` that were materialised in the source query `St`.
///
/// ## Limitations
///
/// ### 1. Subqueries are not analysed
///
/// The transformation does not descend into subqueries -- neither for aggregation scanning nor during the replacement operation.
/// Therefore, the transformation will be skipped if materialisation requires analysing components of a subquery.
/// Conditions that make such analysis necessary are described in sub-sections below.
///
/// We decide to skip the transformation in such cases because the subqueries cannot be rewritten to account for the
/// new query structure: any reference to the original source within it would become dangling after the
/// transformation, as the original source moves into the source query.
///
/// SQL Server can evaluate such an untransformed query unless it genuinely requires materialisation (an aggregate
/// function's argument or a group-by operand contains a subquery), in which case SQL Server rejects it natively --
/// the same outcome as in the absence of this transformation.
///
/// A subquery need not be analysed when it satisfies one of the following:
/// * it is the group-by expression;
/// * it is used within an argument to an aggregation function.
///   E.g., `sumOf().model(Q)`, `sumOf().beginExpr().model(Q).add().val(1).endExpr()`
///
/// #### 1.1. A subquery as a function of the group-by expression
///
/// When a subquery acts as a scalar function of the group-by expression, it must be analysed so that the materialised
/// group-by expression can be replaced.
///
/// ```
/// .groupBy().prop("key")
/// // Subquery as a function: prop("key") -> countAll
/// .yield().model(select(S).where().prop(X).eq().extProp("key").yield().countAll().model()).as("count")
/// ```
///
/// If the transformation applied, it would have to produce the following:
///
/// ```
/// // Option 1: Replace references to the materialised prop("key")
/// .groupBy().prop("c1") // "key" materialised as "c1"
/// .yield().model(select(S).where().prop(X).eq().extProp("c1").yield().countAll().model()).as("count")
///
/// // Option 2: Materialise the whole subquery
/// .groupBy().prop("c1")
/// .yield().prop("c2")
/// ```
///
/// #### 1.2. A subquery contains an aggregation that binds to the enclosing query being transformed
///
/// An aggregation can be syntactically located somewhere deep within a subquery's `where`, but semantically bound to
/// another query above it.
/// Implementing this identification of semantic bindings is a complex task.
///
/// Here is an example:
///
/// ```java
/// select(Vehicle.class).as("v") // (1)
/// .groupBy().prop("model")
/// .yield().prop("model").as("model")
/// // Count all Fuel Usage records dated after the earliest Fuel Usage within a group.
/// .yield().model(select(FuelUsage.class) // (2)
///                .where()
///                // Although this minOf aggregation is syntactically within (2), it semantically binds to (1).
///                .prop("date").gt().expr(expr().minOf().model(select(FuelUsage.class) // (3)
///                                                             .where().prop("vehicle").eq().prop("v.id")
///                                                             // This minOf is simple -- binds to (3).
///                                                             .yield().minOf().prop("date")
///                                                             .modelAsPrimitive())
///                                        .model())
///                .yield().countAll()
///                .modelAsPrimitive())
///     .as("n")
/// .modelAsAggregate();
/// ```
///
public final class AggregateOperandMaterialiser {

    public static final AggregateOperandMaterialiser INSTANCE = new AggregateOperandMaterialiser();

    private AggregateOperandMaterialiser() {}

    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    // : Ad-hoc configuration for testing purposes.

    static boolean enabled = true;

    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    public TransformationResultFromStage2To3<QueryComponents3> apply(final QueryComponents3 qc, final TransformationContextFromStage2To3 context) {
        if (!enabled) {
            return skipTransformation(context);
        }
        if (context.dbVersion() != DbVersion.MSSQL) {
            return skipTransformation(context);
        }
        if (qc.maybeJoinRoot().isEmpty() || qc.yields().isEmpty()) {
            return skipTransformation(context);
        }
        final var origJoin = qc.maybeJoinRoot().get();
        final var origWhere = qc.whereConditions();
        final var origYields = qc.yields();
        final var origGroups = qc.groups();
        final var origOrderings = qc.orderings();

        final List<ISingleOperand3> yieldAndOrderingOperands = StreamUtils.concat(
                        origYields.getYields().stream().map(Yield3::operand),
                        IOrderBy3.onlyOperands(origOrderings.list().stream()).map(IOrderBy3.Operand::operand))
                .toList();
        // The transformation is applicable only to queries that yield or order by an aggregation.
        // In particular, group-by operands alone do not trigger the transformation, regardless of their complexity.
        if (yieldAndOrderingOperands.stream().noneMatch(this::containsAggregation)) {
            return skipTransformation(context);
        }

        final var operations = context.operations();

        // Collect operands to materialise, deduplicating by alpha-equivalence.
        final List<ISingleOperand3> operandsToMaterialise = dedupByAlphaEq(
                StreamUtils.concat(
                        yieldAndOrderingOperands.stream().flatMap(this::extractAggregatedExpressions),
                        origGroups.groups().stream().map(GroupBy3::operand)),
                operations);
        if (operandsToMaterialise.isEmpty() || operandsToMaterialise.stream().allMatch(AggregateOperandMaterialiser::isPersistentProperty)) {
            return skipTransformation(context);
        }

        // A node is materialised when it is alpha-equivalent to one of the operands to materialise.
        final Predicate<ISingleOperand3> isMaterialised =
                node -> operandsToMaterialise.stream().anyMatch(rand -> operations.alphaEq(rand, node));
        // A subquery in a yield or an order-by that is not materialised would keep referencing the original source,
        // which the outer query no longer accesses (the replacement operation does not descend into subqueries).
        // Skip the transformation to preserve such references (see Limitations in the class documentation).
        if (yieldAndOrderingOperands.stream().anyMatch(rand -> hasUnmaterialisedSubQuery(rand, isMaterialised))) {
            return skipTransformation(context);
        }

        final List<? extends T2<? extends ISingleOperand3, String>> operandsAndAliases = zip(operandsToMaterialise.stream(), generateAliases(), T2::t2).toList();

        // New source query components.
        final var sJoin = origJoin;
        final var sWhere = origWhere;
        final GroupBys3 sGroups = GroupBys3.empty();
        final OrderBys3 sOrderings = OrderBys3.empty();
        final var createYieldsResult = createYields(operandsAndAliases, context);
        final var sYields = new Yields3(createYieldsResult.item);
        final var context2 = createYieldsResult.updatedContext;
        final var sQuery = new SourceQuery3(new QueryComponents3(Optional.of(sJoin), sWhere, sYields, sGroups, sOrderings), EntityAggregates.class);

        // Transformed input query components (called "top" although it is not necessarily a top-level query).
        final var context3 = context2.cloneWithNextSqlId();
        final var topSource = new Source3BasedOnQueries(List.of(sQuery), context3.gen().nextSourceId(), context3.sqlId);

        final Replacements replacements = node -> {
            for (final var it : operandsAndAliases) {
                if (operations.alphaEq(it._1, node)) {
                    final var alias = it._2;
                    final var type = it._1.type();
                    return () -> new Prop3(alias, topSource, type);
                }
            }
            return null;
        };

        final Conditions3 topConditions = Conditions3.empty();
        final var topYields = new Yields3(
                origYields.getYields()
                        .stream()
                        .map(y -> replaceAll(y, replacements))
                        .toList());
        final var topGroups = new GroupBys3(
                origGroups.groups()
                        .stream()
                        .map(g -> replaceAll(g, replacements))
                        .toList());
        final var topOrders = origOrderings.updateOrderBys(
                origOrderings.list()
                        .stream()
                        .map(o -> replaceAll(o, replacements))
                        .toList());
        return new TransformationResultFromStage2To3<>(new QueryComponents3(Optional.of(new JoinLeafNode3(topSource)), topConditions, topYields, topGroups, topOrders), context3);
    }

    /// Creates yields from `operandsAndAliases`, updating context to generate a new SQL id for each yield.
    ///
    /// @param operandsAndAliases  a list of pairs `(operand, alias)`. Each `operand` is yielded under `alias`.
    ///
    private TransformationResultFromStage2To3<List<Yield3>> createYields(
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

        return new TransformationResultFromStage2To3<>(unmodifiableList(yields), ctx);
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
            // concatOf: extract the aggregated expression and the ordering items.
            // The ordering items may reference properties of the source, hence have to be materialised.
            // The separator is always a constant, hence does not have to be materialised.
            case ConcatOf3 it -> StreamUtils.concat(Stream.of(it.operand1), IOrderBy3.onlyOperands(it.orderItems.stream()).map(IOrderBy3.Operand::operand));
            // `COUNT(*)` has no argument.
            case CountAll3 _ -> Stream.empty();
            default -> streamChildren(node).flatMap(this::extractAggregatedExpressions);
        };
    }

    /// Determines whether `node` contains an aggregate function at the level of the query source
    /// (i.e., not inside a subquery).
    ///
    private boolean containsAggregation(final ISingleOperand3 node) {
        return switch (node) {
            case AverageOf3 _, MinOf3 _, MaxOf3 _, SumOf3 _, CountOf3 _, CountAll3 _, ConcatOf3 _ -> true;
            default -> streamChildren(node).anyMatch(this::containsAggregation);
        };
    }

    /// Determines whether `node` contains a [subquery][SubQuery3] that would not be materialised by this transformation.
    ///
    /// Subqueries are not descended into by the replacement operation, so a subquery that is neither materialised itself
    /// nor part of a materialised expression retains its references to the original query source.
    /// After the transformation, such references would become dangling, as the original source moves into the source query.
    ///
    /// Arguments of aggregate functions are always materialised in full (including any subqueries they contain),
    /// and so is every node for which `isMaterialised` holds.
    ///
    /// @param isMaterialised  tests whether a node will be materialised as a column of the source query
    ///
    private boolean hasUnmaterialisedSubQuery(final ISingleOperand3 node, final Predicate<ISingleOperand3> isMaterialised) {
        if (isMaterialised.test(node)) {
            return false;
        }
        return switch (node) {
            case SubQuery3 _ -> true;
            // Arguments of aggregate functions are materialised in full, together with any subqueries they may contain.
            // For concatOf, this includes the operands of its order items.
            case AverageOf3 _, MinOf3 _, MaxOf3 _, SumOf3 _, CountOf3 _, CountAll3 _, ConcatOf3 _ -> false;
            // For case-when, also consider conditions, which may contain subqueries within predicates.
            case CaseWhen3 it -> it.whenThenPairs().stream()
                                         .anyMatch(pair -> pair.map((when, then) -> hasUnmaterialisedSubQuery(when, isMaterialised)
                                                                                    || hasUnmaterialisedSubQuery(then, isMaterialised)))
                                 || it.elseOperand().isPresent() && hasUnmaterialisedSubQuery(it.elseOperand().get(), isMaterialised);
            default -> streamChildren(node).anyMatch(child -> hasUnmaterialisedSubQuery(child, isMaterialised));
        };
    }

    private boolean hasUnmaterialisedSubQuery(final ICondition3 condition, final Predicate<ISingleOperand3> isMaterialised) {
        return switch (condition) {
            case ExistencePredicate3 _ -> true;
            // The right operand of a quantified predicate is always a subquery.
            case QuantifiedPredicate3 _ -> true;
            case SetPredicate3 it -> it.rightOperand() instanceof QueryBasedSet3
                                     || streamChildren(it).anyMatch(rand -> hasUnmaterialisedSubQuery(rand, isMaterialised));
            case Conditions3 it -> it.allConditionsAsDnf().stream()
                    .flatMap(List::stream)
                    .anyMatch(c -> hasUnmaterialisedSubQuery(c, isMaterialised));
            default -> streamChildren(condition).anyMatch(rand -> hasUnmaterialisedSubQuery(rand, isMaterialised));
        };
    }

    private Yield3 replaceAll(final Yield3 yield, final Replacements replacements) {
        return yield.setOperand(replace(yield.operand(), replacements));
    }

    private GroupBy3 replaceAll(final GroupBy3 groupBy, final Replacements replacements) {
        final var newOperand = replace(groupBy.operand(), replacements);
        return groupBy.setOperand(newOperand);
    }

    private IOrderBy3 replaceAll(final IOrderBy3 orderBy, final Replacements replacements) {
        return switch (orderBy) {
            case IOrderBy3.Operand operand -> operand.setOperand(replace(operand.operand(), replacements));
            case IOrderBy3.Yield yield -> yield;
        };
    }

    /*
    # The replacement operation on EQL AST nodes

    The code below implements the replacement operation on a subset of EQL AST nodes -- operands, represented by [ISingleOperand2].
    The replacement operation can be viewed as a function `replace(node, replacements)`, where `node` is an input node and
    `replacements` resolves an old node to the new node that should take its place.
    This operation produces a node equal to the input `node` but with every reachable node that `replacements` resolves
    replaced by its resolution.
    A node is resolved when it is alpha-equivalent to a materialised operand, so that occurrences
    that differ only in generated source identifiers are replaced by the same materialised column.

    In general, this operation could process the whole tree rooted at the input node.
    But for the purposes of this specific transformation, it does not descend into subquery nodes.
    */

    /// Resolves the materialised replacement for a node, or null if the node is not being materialised.
    /// Each call of the resulting [Supplier] produces a fresh replacement node to preserve the AST node-uniqueness invariant.
    ///
    @FunctionalInterface
    private interface Replacements {
        @Nullable Supplier<? extends ISingleOperand3> get(ISingleOperand3 node);
    }

    /// Reconstructs the tree rooted at `node` by replacing all reachable nodes that `replacements` resolves.
    ///
    /// @param replacements  resolves an old node to the new node that should take its place
    ///
    private ISingleOperand3 replace(
            final ISingleOperand3 node,
            final Replacements replacements)
    {
        final var mkNewNode = replacements.get(node);
        if (mkNewNode != null) {
            return mkNewNode.get();
        }

        final var replacedChildren = streamChildren(node)
                .map(child -> {
                    final var replacedChild = replace(child, replacements);
                    return replacedChild == child ? null : t2(child, replacedChild);
                })
                .filter(Objects::nonNull)
                // Use reference-based equality to ensure that equal nodes are each replaced with their own new node.
                // Replacing two or more old nodes by the same new node would violate the node uniqueness invariant.
                .collect(toMap((v1, _) -> v1, IdentityHashMap::new));
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
                                           it.elseOperand().map(elseOp -> replacements.getOrDefault(elseOp, elseOp)),
                                           it.typeCast());
            default -> node;
        };
    }

    private List<IOrderBy3> replaceChildren(
            final List<IOrderBy3> orderBys,
            final Map<ISingleOperand3, ISingleOperand3> replacements)
    {
        return orderBys.stream().anyMatch(orderBy -> orderBy instanceof IOrderBy3.Operand o && replacements.containsKey(o.operand()))
                ? orderBys.stream()
                          .map(orderBy -> switch (orderBy) {
                              case IOrderBy3.Operand o -> o.setOperand(replacements.getOrDefault(o.operand(), o.operand()));
                              case IOrderBy3.Yield y -> y;
                          })
                          .collect(toImmutableList())
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
            case ConcatOf3 it -> StreamUtils.concat(Stream.of(it.operand1, it.operand2), IOrderBy3.onlyOperands(it.orderItems.stream()).map(IOrderBy3.Operand::operand));
            case SingleOperandFunction3 it -> Stream.of(it.operand);
            case TwoOperandsFunction3 it -> Stream.of(it.operand1, it.operand2);
            case Expression3 it -> StreamUtils.concat(Stream.of(it.firstOperand), it.otherOperands.stream().map(CompoundSingleOperand3::operand));
            case Concat3 it -> it.operands().stream();
            // Case-when is special: operands within conditions are not immediate children but are included.
            case CaseWhen3 it -> StreamUtils.concat(
                    it.whenThenPairs().stream().flatMap(t2 -> t2.map((when, then) -> StreamUtils.concat(streamChildren(when), Stream.of(then)))),
                    it.elseOperand().stream());
            default -> Stream.empty();
        };
    }

    private Stream<? extends ISingleOperand3> streamChildren(final ICondition3 condition) {
        return switch (condition) {
            case ComparisonPredicate3 it -> Stream.of(it.leftOperand(), it.rightOperand());
            case NullPredicate3 it -> Stream.of(it.operand());
            case LikePredicate3 it -> Stream.of(it.matchOperand(), it.patternOperand());
            case SetPredicate3 it -> StreamUtils.concat(
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

    /// Collects operands into a list, discarding any operand that is alpha-equivalent to one already collected.
    ///
    private static List<ISingleOperand3> dedupByAlphaEq(final Stream<? extends ISingleOperand3> operands, final Operations operations) {
        final List<ISingleOperand3> result = new ArrayList<>();
        operands.forEach(rand -> {
            if (result.stream().noneMatch(existing -> operations.alphaEq(existing, rand))) {
                result.add(rand);
            }
        });
        return unmodifiableList(result);
    }

    private static boolean isPersistentProperty(final ISingleOperand3 rand) {
        // All stage 3 properties are persistent.
        // Calculated properties are expanded before stage 3.
        return rand instanceof Prop3;
    }

    private static Stream<String> generateAliases() {
        return IntStream.iterate(1, i -> i + 1).mapToObj(i -> "c" + i);
    }

}
