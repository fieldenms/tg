package ua.com.fielden.platform.eql.meta.utils;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import ua.com.fielden.platform.entity.query.exceptions.EqlException;
import ua.com.fielden.platform.entity.query.model.ExpressionModel;
import ua.com.fielden.platform.eql.antlr.EqlCompilationResult;
import ua.com.fielden.platform.eql.antlr.EqlCompiler;
import ua.com.fielden.platform.eql.exceptions.EqlStage2ProcessingException;
import ua.com.fielden.platform.eql.meta.QuerySourceInfoProvider;
import ua.com.fielden.platform.eql.meta.query.AbstractQuerySourceItem;
import ua.com.fielden.platform.eql.meta.query.QuerySourceInfo;
import ua.com.fielden.platform.eql.meta.query.QuerySourceItemForComponentType;
import ua.com.fielden.platform.eql.meta.query.QuerySourceItemForUnionType;
import ua.com.fielden.platform.eql.stage0.QueryModelToStage1Transformer;
import ua.com.fielden.platform.eql.stage1.TransformationContextFromStage1To2;
import ua.com.fielden.platform.eql.stage2.sources.Source2BasedOnPersistentType;
import ua.com.fielden.platform.meta.IDomainMetadata;
import ua.com.fielden.platform.types.tuples.T2;
import ua.com.fielden.platform.utils.CollectionUtil;

import java.util.*;
import java.util.stream.Stream;

import static java.lang.String.format;
import static java.util.stream.Collectors.toCollection;
import static java.util.stream.Collectors.toMap;
import static ua.com.fielden.platform.entity.AbstractEntity.KEY;
import static ua.com.fielden.platform.eql.meta.utils.TopologicalSort.sortTopologically;
import static ua.com.fielden.platform.types.tuples.T2.t2;

/// Verifies that an entity type has no cyclic dependencies between its calculated properties.
///
/// A calculated property depends on another if its expression refers to it -- whether as a whole
/// value or by navigating into it.
/// Dependencies are analysed over *all* calculated properties of the entity, regardless of type.
///
/// A cyclic dependency is a domain-definition error, so this verifier throws early.
/// It is intended to be installed as a startup verification service via [com.google.inject.AbstractModule#requestStaticInjection].
///
@Singleton
public class DependentCalcPropsVerifier {

    public static final String
            ERR_IN_EXPRESSION_FOR_CALCULATED_PROPERTY = "There is an error in the expression for calculated property [%s.%s].",
            ERR_CYCLIC_DEPENDENCIES_BETWEEN_CALCULATED_PROPERTIES = "There are cyclic dependencies between calculated properties of [%s]: [%s].";

    private final QuerySourceInfoProvider querySourceInfoProvider;
    private final IDomainMetadata domainMetadata;

    @Inject
    protected DependentCalcPropsVerifier(final QuerySourceInfoProvider querySourceInfoProvider, final IDomainMetadata domainMetadata) {
        this.querySourceInfoProvider = querySourceInfoProvider;
        this.domainMetadata = domainMetadata;
    }

    /// Verifies each of the given query source infos.
    ///
    public void verify(final Iterable<QuerySourceInfo<?>> querySourceInfos) {
        querySourceInfos.forEach(this::verify);
    }

    /// Throws [EqlException] if the calculated properties of the given query source info have cyclic dependencies.
    ///
    public void verify(final QuerySourceInfo<?> querySourceInfo) {
        try {
            // The topological order is irrelevant; the sort throws with the offending cycle if one exists.
            sortTopologically(dependencyGraph(querySourceInfo));
        } catch (final TopologicalSortException ex) {
            throw new EqlException(format(ERR_CYCLIC_DEPENDENCIES_BETWEEN_CALCULATED_PROPERTIES,
                                          querySourceInfo.javaType().getSimpleName(),
                                          CollectionUtil.toString(ex.cycle(), " -> ")));
        }
    }

    /// Builds the direct-dependency graph over all calculated properties of the entity:
    /// each calc prop maps to the calc props its expression references.
    ///
    private Map<String, Set<String>> dependencyGraph(final QuerySourceInfo<?> querySourceInfo) {
        // TODO Use Source2BasedOnQueries for synthetic entity types.
        final var gen = new QueryModelToStage1Transformer();
        final var source = new Source2BasedOnPersistentType(querySourceInfo, gen.nextSourceId(), true);

        final Map<String, Set<String>> graph = calculatedProperties(querySourceInfo)
                .collect(toMap(T2::_1, t2 -> directDeps(source, t2, querySourceInfo, gen).collect(toCollection(HashSet::new))));
        // Keep the graph closed (every dependency is also a node) -- relied upon by cycle extraction.
        // This drops references to any calculated property not enumerated as a node (e.g. a calculated key).
        final var nodes = graph.keySet();
        graph.values().forEach(deps -> deps.retainAll(nodes));
        return graph;
    }

    /// The calculated properties of this entity referenced directly by the given calc prop's expression.
    ///
    /// Only the *first terminal* of each reference matters: a path like `a.b.c` is rooted at this entity,
    /// so its first terminal `a` is one of this entity's own properties, whereas `b`, `c`, ... belong to the
    /// target types reached by navigation -- other entities' sources.
    /// Hence only the first terminal can name a calculated property of *this* entity and thus form an intra-entity edge.
    /// The `source.id()` guard first keeps only references rooted at this entity (discarding, e.g., references inside subqueries).
    ///
    private Stream<String> directDeps(
            final Source2BasedOnPersistentType source,
            final T2<String, ExpressionModel> calcProp,
            final QuerySourceInfo<?> querySourceInfo,
            final QueryModelToStage1Transformer gen)
    {
        try {
            final var exp1 = new EqlCompiler(gen).compile(calcProp._2().tokens(), EqlCompilationResult.StandaloneExpression.class).model();
            final var context = TransformationContextFromStage1To2.mkContext(querySourceInfoProvider, domainMetadata).cloneWithAdded(source);
            final var exp2 = exp1.transform(context);
            return exp2.collectProps()
                    .stream()
                    .filter(p -> p.source.id().equals(source.id()))
                    .map(p -> firstTerminal(p.getPath()).map((fst, isCalc) -> isCalc ? fst : null))
                    .filter(Objects::nonNull);
        } catch (final Exception ex) {
            throw new EqlException(ERR_IN_EXPRESSION_FOR_CALCULATED_PROPERTY.formatted(querySourceInfo.javaType().getTypeName(), calcProp._1()), ex);
        }
    }

    /// Enlists prop chunks for all calculated properties of the given entity type (top-level and component subprops).
    ///
    private static Stream<T2<String, ExpressionModel>> calculatedProperties(final QuerySourceInfo<?> qsi) {
        return qsi.getProps().values().stream()
                .flatMap(prop -> {
                    if (prop.expression != null && !prop.name.equals(KEY)) {
                        return Stream.of(t2(prop.name, prop.expression.expressionModel()));
                    }
                    else if (prop.hasExpression() && prop instanceof QuerySourceItemForComponentType<?> component) {
                        return component.getSubitems().values().stream()
                                .filter(AbstractQuerySourceItem::hasExpression)
                                .map(subProp -> t2(prop.name + "." + subProp.name, subProp.expression.expressionModel()));
                    }
                    else return Stream.of();
                });
    }

    /// The first terminal property sub-path of a path: the leading single logical property unit
    /// (skipping component/union headers), paired with whether it is a calculated property.
    ///
    private static T2<String, Boolean> firstTerminal(final List<AbstractQuerySourceItem<?>> propPath) {
        String name = null;
        for (final var item : propPath) {
            name = (name != null) ? name + "." + item.name : item.name;
            if (!isHeaderProperty(item)) {
                return t2(name, item.hasExpression());
            }
        }
        throw new EqlStage2ProcessingException("Property path is not terminal: %s".formatted(CollectionUtil.toString(propPath, it -> it.name, ".")));
    }

    /// Determines whether `item` represents a header for a component or union type property.
    ///
    private static boolean isHeaderProperty(final AbstractQuerySourceItem<?> item) {
        return item instanceof QuerySourceItemForComponentType<?> || item instanceof QuerySourceItemForUnionType<?>;
    }

}
