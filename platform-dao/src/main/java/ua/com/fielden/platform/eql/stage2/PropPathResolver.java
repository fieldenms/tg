package ua.com.fielden.platform.eql.stage2;

import com.google.common.collect.ImmutableList;
import jakarta.inject.Inject;
import ua.com.fielden.platform.entity.exceptions.InvalidStateException;
import ua.com.fielden.platform.entity.query.fluent.enums.JoinType;
import ua.com.fielden.platform.entity.query.model.ExpressionModel;
import ua.com.fielden.platform.eql.antlr.EqlCompilationResult;
import ua.com.fielden.platform.eql.antlr.EqlCompiler;
import ua.com.fielden.platform.eql.exceptions.EqlStage2ProcessingException;
import ua.com.fielden.platform.eql.meta.QuerySourceInfoProvider;
import ua.com.fielden.platform.eql.meta.query.AbstractQuerySourceItem;
import ua.com.fielden.platform.eql.meta.query.QuerySourceItemForComponentType;
import ua.com.fielden.platform.eql.meta.query.QuerySourceItemForEntityType;
import ua.com.fielden.platform.eql.meta.query.QuerySourceItemForUnionType;
import ua.com.fielden.platform.eql.stage0.QueryModelToStage1Transformer;
import ua.com.fielden.platform.eql.stage1.PropResolutionProgress;
import ua.com.fielden.platform.eql.stage1.TransformationContextFromStage1To2;
import ua.com.fielden.platform.eql.stage1.operands.Expression1;
import ua.com.fielden.platform.eql.stage2.operands.Expression2;
import ua.com.fielden.platform.eql.stage2.operands.Prop2;
import ua.com.fielden.platform.eql.stage2.sources.ISource2;
import ua.com.fielden.platform.eql.stage2.sources.Source2BasedOnPersistentType;
import ua.com.fielden.platform.meta.IDomainMetadata;
import ua.com.fielden.platform.types.tuples.T2;
import ua.com.fielden.platform.utils.ImmutableMapUtils;

import java.util.*;

import static java.util.Collections.unmodifiableSequencedMap;
import static java.util.Comparator.comparingInt;
import static java.util.stream.Collectors.*;
import static ua.com.fielden.platform.types.tuples.T2.t2;
import static ua.com.fielden.platform.utils.StreamUtils.foldLeft;

// TODO Produce unique Expression2 instances with unique inner source IDs to respect the invariant on global source ID uniqueness.
// At present, this is not possible because the input is Set<Prop2> (equal Prop2 instances are deduplicated)
// and resolution in Prop2.transform uses (sourceId, propPath) as key.
// The result is that two occurrences of the same calculated property will share an Expression2 and also the inner
// source IDs if the expression contains queries.
// Sharing Expression2 is fine because it is immediately transformed into Expression3, each of which is a new instance.
// But sharing source IDs is not, because it violates the global invariant.
// This issue, however, does not at present affect correctness.

/// ## Design
///
/// A property path is resolved as a fold over its elements (terminal property paths):
/// - **Intermediate** elements produce implicit joins (`left JOIN right ON left.prop = right.id`).
/// - The **terminal** element produces a [Resolution] — a persistent [Resolution.Column] or a calculated property [Resolution.Expr].
///
/// ### Join order
///
/// Implicit joins are one-to-one FK joins, so independent joins commute.
/// The only ordering constraint on joins applies to intermediate calculated properties.
/// Given an intermediate calculated property `p1` that requires an implicit join `J`, if its expression references another
/// property `p2` that itself requires implicit joins `[G1, ..., Gn]`, the resulting order of joins is `G1, ..., Gn, J`.
/// That topological order is produced naturally by the implicit depth-first program structure and preserved via a [SequencedMap]
/// used for [State#joins].
///
/// ```
/// class Vehicle
///   @IsProperty @MapTo
///   OrgUnit station;
///
///   @IsProperty @Calculated
///   OrgUnit topStation = station.parent;
///
/// select(Vehicle.class).where().prop("topStation.name)
///
/// Step 1: topStation as a single join. Because topStation is calculated, its ON is an expression, not a column.
/// select o.name
/// from Vehicle v
///      join OrgUnit o on v.<topStation> = o.id -- on Vehicle.topStation
///
/// Step 2: expanding <topStation> = station.parent introduces Vehicle.station join, which must come first.
/// select o.name
/// from Vehicle v
///      join OrgUnit o1 on v.station = o1.id -- on Vehicle.station
///      join OrgUnit o  on o1.parent = o.id  -- on Vehicle.topStation = Vehicle.station.parent
///
/// Resulting order: [station, topStation]
/// ```
///
public class PropPathResolver implements IPropPathResolver {

    /// A comparator to ensure deterministic order of properties, which is relied on by tests.
    ///
    private static final Comparator<Prop2> prop2Comparator = comparingInt((Prop2 p) -> p.source.id()).thenComparing(p -> p.propPath);

    private final QuerySourceInfoProvider querySourceInfoProvider;
    private final IDomainMetadata domainMetadata;

    // TODO: Make protected once dependent EQL tests are refactored and use IoC.
    @Inject
    public PropPathResolver(final QuerySourceInfoProvider querySourceInfoProvider, final IDomainMetadata domainMetadata) {
        this.querySourceInfoProvider = querySourceInfoProvider;
        this.domainMetadata = domainMetadata;
    }

    @Override
    public Result resolve(final Set<Prop2> props, final QueryModelToStage1Transformer gen) {
        final var finalState = foldLeft(props.stream().sorted(prop2Comparator), State.empty, (acc, prop) -> resolveProp(acc, prop, gen));
        final var joins = finalState.joins().entrySet()
                .stream()
                .collect(groupingBy(entry -> entry.getKey()._1(), mapping(Map.Entry::getValue, toList())));
        final var resolutions = finalState.resolutions();
        return new Result(joins, resolutions);
    }

    /// @param joins  (source ID, terminal property path) -> an implicit join.
    ///     The key represents the left side of the join, with the property path's resolution as [JoinNode#leftOn].
    ///     [SequencedMap] is used to ensure that insertion order is preserved, as it determines correctness.
    /// @param resolutions  (source ID, property path) -> property resolution.
    ///     The keys correspond to [Prop2] instances in the query.
    /// @param expansions (source ID, terminal property path) -> property resolution.
    ///     Same form as [#resolutions], but plays the role of a cache, recording all encountered properties.
    ///     E.g., given `model.make.desc` in the query, [#resolutions] stores only the complete path's resolution,
    ///     while [#expansions] stores each property's resolution: `model`, `make`, `desc`.
    ///
    record State ( SequencedMap<T2<Integer, String>, JoinNode> joins,
                   Map<T2<Integer, String>, Resolution> resolutions,
                   Map<T2<Integer, String>, Resolution> expansions )
    {

        static final State empty = new State(unmodifiableSequencedMap(new LinkedHashMap<>()), Map.of(), Map.of());

    }

    private State resolveProp(final State acc, final Prop2 prop, final QueryModelToStage1Transformer gen) {
        final var key = t2(prop.source.id(), prop.propPath);
        return resolveProp_(acc, prop.source, splitPath(prop), key, gen);
    }

    private State resolveProp_(
            final State acc,
            final ISource2<?> source,
            final List<String> path,
            final T2<Integer, String> key,
            final QueryModelToStage1Transformer gen)
    {
        if (acc.resolutions().containsKey(key)) {
            return acc;
        }

        if (path.isEmpty()) {
            throw new InvalidStateException("Empty path");
        }
        else if (path.size() == 1) {
            final var hd = path.getFirst();
            return expand(source, hd, acc, gen).map((resol, acc1) -> addProperty(key, resol, acc1));
        }
        else {
            final var hd = path.getFirst();
            final var tl = rest(path);
            return createJoin(source, hd, acc, gen).map((join, acc1) -> resolveProp_(acc1, join.right(), tl, key, gen));
        }
    }

    /// Creates an implicit source `right` and a join: `leftSrc join right on leftSrc.leftProp = right.id`.
    /// Reuses an existing join and source if they were already created.
    ///
    /// @return (new or existing join, updated state)
    ///
    private T2<JoinNode, State> createJoin(final ISource2<?> leftSrc, final String leftProp, final State acc, final QueryModelToStage1Transformer gen) {
        final var pos = t2(leftSrc.id(), leftProp);
        final var existingJoin = acc.joins().get(pos);
        if (existingJoin != null) {
            return t2(existingJoin, acc);
        }
        else {
            final var lastItem = resolveQuerySourceItem(leftSrc, leftProp).getLast();
            if (!(lastItem instanceof QuerySourceItemForEntityType<?> lastItemEntityTyped)) {
                throw new InvalidStateException("Expected property [%s] to be entity-typed. Source: %s".formatted(leftProp, leftSrc));
            }
            final var rightSrc = new Source2BasedOnPersistentType(lastItemEntityTyped.querySourceInfo, gen.nextSourceId(), false /*isExplicit*/);
            return expand(leftSrc, leftProp, acc, gen).map((leftOn, acc1) -> {
                final var joinType = lastItemEntityTyped.nonnullable ? JoinType.IJ : JoinType.LJ;
                final var join = new JoinNode(leftSrc, rightSrc, leftOn, joinType);
                return t2(join, insertJoin(pos, join, acc1));
            });
        }
    }

    /// Analyse all properties within an expression.
    ///
    private State analyse(final Expression2 expr, final State acc, final QueryModelToStage1Transformer gen) {
        return foldLeft(expr.collectProps().stream().sorted(prop2Comparator), acc, (acc_, p) -> resolveProp(acc_, p, gen));
    }

    private T2<Resolution, State> expand(final ISource2<?> source, final String prop, final State acc, final QueryModelToStage1Transformer gen) {
        final var key = t2(source.id(), prop);
        final var existing = acc.expansions().get(key);
        if (existing != null) {
            return t2(existing, acc);
        }

        final var lastItem = resolveQuerySourceItem(source, prop).getLast();
        if (lastItem.hasExpression()) {
            final var expr = new Resolution.Expr(compile(source, lastItem.expression.expressionModel(), gen));
            return t2(expr, analyse(expr.expr(), addExpansion(key, expr, acc), gen));
        }
        else {
            final var column = new Resolution.Column(source.id(), prop);
            return t2(column, addExpansion(key, column, acc));
        }
    }

    private static List<AbstractQuerySourceItem<?>> resolveQuerySourceItem(final ISource2<?> source, final String prop) {
        final var resolved = source.querySourceInfo().resolve(new PropResolutionProgress(prop));
        if (!resolved.isSuccessful()) {
            throw new InvalidStateException("Could not resolve property [%s] against source [%s].".formatted(prop, source));
        }
        return resolved.getResolved();
    }

    private Expression2 compile(final ISource2<?> source, final ExpressionModel model, final QueryModelToStage1Transformer gen) {
        final Expression1 exp1 = new EqlCompiler(gen).compile(model.tokens(), EqlCompilationResult.StandaloneExpression.class).model();
        final var context = TransformationContextFromStage1To2.mkContext(querySourceInfoProvider, domainMetadata, List.of(List.of(source)));
        return exp1.transform(context);
    }

    /// Splits a property path into terminal properties.
    ///
    private List<String> splitPath(final Prop2 prop) {
        final var terminals = ImmutableList.<String>builder();

        String curr = null;
        for (final var querySourceInfoItem : prop.getPath()) {
            curr = (curr != null) ? curr + "." + querySourceInfoItem.name : querySourceInfoItem.name;
            if (!isHeaderProperty(querySourceInfoItem)) {
                terminals.add(curr);
                curr = null;
            }
        }

        if (curr != null) {
            throw new EqlStage2ProcessingException("Property path is not terminal: %s".formatted(prop.propPath));
        }

        return terminals.build();
    }

    private boolean isHeaderProperty(final AbstractQuerySourceItem<?> item) {
        return item instanceof QuerySourceItemForUnionType<?> || item instanceof QuerySourceItemForComponentType<?>;
    }

    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    // : State updates

    private State insertJoin(final T2<Integer, String> pos, final JoinNode join, final State acc) {
        final var newJoins = new LinkedHashMap<>(acc.joins());
        newJoins.putLast(pos, join);
        return new State(unmodifiableSequencedMap(newJoins), acc.resolutions(), acc.expansions());
    }

    private State addProperty(final T2<Integer, String> key, final Resolution resolution, final State acc) {
        final var newResolutions = ImmutableMapUtils.insert(acc.resolutions(), key, resolution);
        return new State(acc.joins(), newResolutions, acc.expansions());
    }

    private State addExpansion(final T2<Integer, String> key, final Resolution resolution, final State acc) {
        final var newExpansions = ImmutableMapUtils.insert(acc.expansions(), key, resolution);
        return new State(acc.joins(), acc.resolutions(), newExpansions);
    }

    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    // : Utilities

    private static <X> List<X> rest(final List<X> list) {
        return list.subList(1, list.size());
    }

    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

}
