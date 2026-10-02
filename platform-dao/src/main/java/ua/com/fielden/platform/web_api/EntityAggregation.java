package ua.com.fielden.platform.web_api;

import graphql.Scalars;
import graphql.language.Field;
import graphql.language.FragmentDefinition;
import graphql.schema.*;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import ua.com.fielden.platform.basic.config.IApplicationDomainProvider;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.exceptions.InvalidStateException;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.entity.query.EntityAggregates;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ICompleted;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ICompletedCommon;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ISubsequentCompletedAndYielded;
import ua.com.fielden.platform.entity.query.model.AggregatedResultQueryModel;
import ua.com.fielden.platform.entity.query.model.ExpressionModel;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.security.IAuthorisationModel;
import ua.com.fielden.platform.security.provider.ISecurityTokenProvider;
import ua.com.fielden.platform.utils.CharSequenceEnum;
import ua.com.fielden.platform.utils.ImmutableListUtils;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import static graphql.schema.FieldCoordinates.coordinates;
import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;
import static graphql.schema.GraphQLNonNull.nonNull;
import static graphql.schema.GraphQLObjectType.newObject;
import static java.util.stream.Collectors.toSet;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.*;
import static ua.com.fielden.platform.reflection.TitlesDescsGetter.getEntityTitle;
import static ua.com.fielden.platform.security.tokens.Template.READ;
import static ua.com.fielden.platform.security.tokens.TokenUtils.authoriseReading;
import static ua.com.fielden.platform.utils.StreamUtils.foldLeft;
import static ua.com.fielden.platform.web_api.GraphQLCommon.*;
import static ua.com.fielden.platform.web_api.RootEntityUtils.QUERY_TYPE_NAME;
import static ua.com.fielden.platform.web_api.RootEntityUtils.validateDuplicateFields;

/// GraphQL definitions that add aggregation over entity types.
///
/// ## Access control
///
/// Due to the fact that [Fields#groupBy] and aggregating fields are typed with GraphQL entity types, the existing visibility
/// rules from [FieldVisibility] apply to them, sharing one control mechanism for both data and aggregation queries.
///
/// As in [RootEntityFetcher], fetching for aggregation queries includes authorisation checks.
/// If an entity type cannot be read by a user, aggregation queries for that entity type will also be rejected.
///
@Singleton
public class EntityAggregation {

    private enum Fields implements CharSequenceEnum {
        groupBy, count, avg, sum, max, min;
    }

    private final IApplicationDomainProvider appDomainProvider;
    private final ICompanionObjectFinder coFinder;
    private final IAuthorisationModel authorisationModel;
    private final ISecurityTokenProvider securityTokenProvider;

    @Inject
    protected EntityAggregation(
            final IApplicationDomainProvider appDomainProvider,
            final ICompanionObjectFinder coFinder,
            final IAuthorisationModel authorisationModel,
            final ISecurityTokenProvider securityTokenProvider)
    {
        this.appDomainProvider = appDomainProvider;
        this.coFinder = coFinder;
        this.authorisationModel = authorisationModel;
        this.securityTokenProvider = securityTokenProvider;
    }

    public GraphQLSchema enhanceSchema(final GraphQLSchema schema) {
        final var entityTypes = streamVisibleTypes(appDomainProvider)
                .filter(ty -> containsRootFieldForType(schema, ty))
                .toList();

        final var queryType = schema.getQueryType();
        final var codeRegistry = schema.getCodeRegistry();
        return schema.transform(builder -> builder
                .additionalTypes(entityTypes.stream().map(this::mkGraphQlAggEntityType).collect(toSet()))
                .codeRegistry(enhanceCodeRegistry(codeRegistry, entityTypes))
                .query(enhanceQueryType(queryType, entityTypes)));
    }

    private GraphQLCodeRegistry enhanceCodeRegistry(
            final GraphQLCodeRegistry codeReg,
            final List<Class<? extends AbstractEntity<?>>> entityTypes)
    {
        return codeReg.transform(builder -> {
            entityTypes.forEach(ty -> builder.dataFetcher(coordinates(QUERY_TYPE_NAME, rootFieldNameForAggEntity(ty)), aggEntityFetcher(ty)));
        });
    }

    private GraphQLObjectType enhanceQueryType(
            final GraphQLObjectType queryType,
            final List<Class<? extends AbstractEntity<?>>> entityTypes)
    {
        return queryType.transform(builder -> {
            entityTypes.forEach(ty -> builder
                    .field(newFieldDefinition()
                                   .name(rootFieldNameForAggEntity(ty))
                                   .description("Aggregation over %s.".formatted(getEntityTitle(ty)))
                                   .type(new GraphQLList(new GraphQLTypeReference(graphQlTypeNameForAggEntity(ty))))
                                   .build()));
        });
    }

    private GraphQLObjectType mkGraphQlAggEntityType(final Class<? extends AbstractEntity<?>> entityType) {
        final var graphQlEntityTypeRef = new GraphQLTypeReference(graphQlTypeNameForEntity(entityType));
        final var fields = List.of(
                newFieldDefinition()
                        .name(Fields.groupBy.name())
                        .description("Grouping key.")
                        .type(graphQlEntityTypeRef)
                        .build(),
                newFieldDefinition()
                        .name(Fields.count.name())
                        .description("Count within each group.")
                        .type(nonNull(Scalars.GraphQLInt))
                        .build(),
                newFieldDefinition()
                        .name(Fields.avg.name())
                        .description("Average of selected fields within each group.")
                        .type(graphQlEntityTypeRef)
                        .build(),
                newFieldDefinition()
                        .name(Fields.sum.name())
                        .description("Sum of selected fields within each group.")
                        .type(graphQlEntityTypeRef)
                        .build(),
                newFieldDefinition()
                        .name(Fields.max.name())
                        .description("Max of selected fields within each group.")
                        .type(graphQlEntityTypeRef)
                        .build(),
                newFieldDefinition()
                        .name(Fields.min.name())
                        .description("Min of selected fields within each group.")
                        .type(graphQlEntityTypeRef)
                        .build()
        );
        return newObject()
                .name(graphQlTypeNameForAggEntity(entityType))
                .description("Aggregation over %s.".formatted(getEntityTitle(entityType)))
                .fields(fields)
                .build();
    }

    private DataFetcher<?> aggEntityFetcher(final Class<? extends AbstractEntity<?>> type) {
        return new AggEntityFetcher(type, coFinder, authorisationModel, securityTokenProvider);
    }

    private record AggEntityFetcher(
            Class<? extends AbstractEntity<?>> entityType,
            ICompanionObjectFinder coFinder,
            IAuthorisationModel authorisationModel,
            ISecurityTokenProvider securityTokenProvider)
            implements DataFetcher<List<EntityAggregates>>
    {

        @Override
        public List<EntityAggregates> get(final DataFetchingEnvironment environment) {
            authoriseReading(entityType.getSimpleName(), READ, authorisationModel, securityTokenProvider).ifFailure(Result::throwRuntime);
            validateDuplicateFields(environment.getField().getSelectionSet(), environment.getFragmentsByName());
            final var query = buildQuery(entityType, environment);
            return coFinder.find(EntityAggregates.class, true).getAllEntities(from(query).model());
        }

        private AggregatedResultQueryModel buildQuery(
                final Class<? extends AbstractEntity<?>> entityType,
                final DataFetchingEnvironment environment)
        {
            final var fields = streamFields(environment.getField().getSelectionSet(), environment.getFragmentsByName()).toList();
            return addYields(addGroupBy(select(entityType).where().condition(EntityCondToEqlCompiler.compile(environment)),
                                        fields, environment),
                             fields, environment)
                    .modelAsAggregate();
        }

        /// Enhances `query` with yields for the fields that were selected.
        ///
        /// @param query  [ICompleted] or [ISubsequentCompletedAndYielded]
        /// @param fields  selection set of the root field, with fragments expanded
        /// @param environment  contains selected fields
        ///
        private <T extends AbstractEntity<?>> ICompletedCommon<T> addYields(
                final ICompletedCommon<T> query,
                final List<Field> fields,
                final DataFetchingEnvironment environment)
        {
            interface F<U extends AbstractEntity<?>> extends Function<ICompletedCommon<U>, ICompletedCommon<U>> {}

            final F<T> fCount = q -> findField(fields, Fields.count)
                    .map(_ -> addYield(q, expr().ifNull().expr(expr().countAll().model()).then().val(0).model(), Fields.count))
                    .orElse(q);
            final F<T> fAvg = q -> findField(fields, Fields.avg)
                    .map(avg -> addYieldsForField(q, avg, path -> expr().avgOf().prop(toQualifiedName(tail(path))).model(),
                                                  environment))
                    .orElse(q);
            final F<T> fSum = q -> findField(fields, Fields.sum)
                    .map(sum -> addYieldsForField(q, sum, path -> expr().sumOf().prop(toQualifiedName(tail(path))).model(),
                                                  environment))
                    .orElse(q);
            final F<T> fMin = q -> findField(fields, Fields.min)
                    .map(min -> addYieldsForField(q, min, path -> expr().minOf().prop(toQualifiedName(tail(path))).model(),
                                                  environment))
                    .orElse(q);
            final F<T> fMax = q -> findField(fields, Fields.max)
                    .map(max -> addYieldsForField(q, max, path -> expr().maxOf().prop(toQualifiedName(tail(path))).model(),
                                                  environment))
                    .orElse(q);

            return fCount
                    .andThen(fAvg)
                    .andThen(fSum)
                    .andThen(fMin)
                    .andThen(fMax)
                    .apply(query);
        }

        private <T extends AbstractEntity<?>> ICompletedCommon<T> addGroupBy(
                final ICompleted<T> query,
                final List<Field> fields,
                final DataFetchingEnvironment environment)
        {
            final var maybeGroupBy = findField(fields, Fields.groupBy);
            if (maybeGroupBy.isEmpty()) {
                return query;
            }
            final var groupBy = maybeGroupBy.get();
            final var paths = paths(groupBy, environment.getFragmentsByName());
            if (paths.isEmpty()) {
                return query;
            }
            else {
                final var grouped = foldLeft(paths, query, (acc, path) -> acc.groupBy().prop(toQualifiedName(tail(path))));
                return addYieldsForField(grouped, groupBy, path -> expr().prop(toQualifiedName(tail(path))).model(),
                                         environment);
            }
        }

        private <T extends AbstractEntity<?>> ICompletedCommon<T> addYield(
                final ICompletedCommon<T> query,
                final ExpressionModel expr,
                final CharSequence alias)
        {
            return switch (query) {
                case ICompleted<T> it                     -> it.yield().expr(expr).as(alias);
                case ISubsequentCompletedAndYielded<T> it -> it.yield().expr(expr).as(alias);
                default -> throw new InvalidStateException(query.getClass().getName());
            };
        }

        private <T extends AbstractEntity<?>> ICompletedCommon<T> addYieldsForField(
                final ICompletedCommon<T> query,
                final Field field,
                final Function<List<Field>, ExpressionModel> mkExpr,
                final DataFetchingEnvironment environment)
        {
            final var paths = paths(field, environment.getFragmentsByName());
            if (paths.isEmpty()) {
                return query;
            }
            return foldLeft(paths, query, (acc, path) -> addYield(acc, mkExpr.apply(path), toQualifiedName(path)));
        }

        private static <X> List<X> tail(final List<X> xs) {
            return xs.isEmpty() ? xs : xs.subList(1, xs.size());
        }

        private static String toQualifiedName(final List<Field> path) {
            return path.stream().map(Field::getName).collect(Collectors.joining("."));
        }

        /// Returns all root-to-leaf paths, where the root is given by `field`.
        ///
        private static List<List<Field>> paths(final Field field, final Map<String, FragmentDefinition> fragments) {
            final var ss = field.getSelectionSet();
            if (ss == null) {
                return List.of(List.of(field));
            }

            final var result = streamFields(ss, fragments)
                    .flatMap(f -> paths(f, fragments).stream().map(p -> ImmutableListUtils.prepend(field, p)))
                    .toList();
            return result.isEmpty() ? List.of(List.of(field)) : result;
        }

        private static Optional<Field> findField(final List<Field> fields, final CharSequence name) {
            return fields.stream().filter(f -> f.getName().contentEquals(name)).findFirst();
        }

    }

}
