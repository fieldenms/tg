package ua.com.fielden.platform.web_api;

import graphql.Scalars;
import graphql.schema.*;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
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

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import static com.google.common.collect.Streams.stream;
import static graphql.schema.FieldCoordinates.coordinates;
import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;
import static graphql.schema.GraphQLNonNull.nonNull;
import static graphql.schema.GraphQLObjectType.newObject;
import static java.util.stream.Collectors.toSet;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.*;
import static ua.com.fielden.platform.reflection.TitlesDescsGetter.getEntityTitle;
import static ua.com.fielden.platform.security.tokens.Template.READ;
import static ua.com.fielden.platform.utils.StreamUtils.foldLeft;
import static ua.com.fielden.platform.web_api.FieldVisibility.authoriseReadingOf;
import static ua.com.fielden.platform.web_api.GraphQLCommon.QUERY_TYPE_NAME;
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

    private final ICompanionObjectFinder coFinder;
    private final IAuthorisationModel authorisationModel;
    private final ISecurityTokenProvider securityTokenProvider;
    private final GraphQLCommon common;

    @Inject
    protected EntityAggregation(
            final ICompanionObjectFinder coFinder,
            final IAuthorisationModel authorisationModel,
            final ISecurityTokenProvider securityTokenProvider,
            final GraphQLCommon common)
    {
        this.coFinder = coFinder;
        this.authorisationModel = authorisationModel;
        this.securityTokenProvider = securityTokenProvider;
        this.common = common;
    }

    /// Enhances `schema` with aggregation over those of `entityTypes` that have root fields in `schema`.
    ///
    public GraphQLSchema enhanceSchema(final GraphQLSchema schema, final Collection<WebApiEntityType> entityTypes) {
        final var aggregatedTypes = entityTypes.stream()
                .filter(ty -> common.containsRootFieldForType(schema, ty))
                .toList();

        final var queryType = schema.getQueryType();
        final var codeRegistry = schema.getCodeRegistry();
        return schema.transform(builder -> builder
                .additionalTypes(aggregatedTypes.stream().map(this::mkGraphQlAggEntityType).collect(toSet()))
                .codeRegistry(enhanceCodeRegistry(codeRegistry, aggregatedTypes))
                .query(enhanceQueryType(queryType, aggregatedTypes)));
    }

    private GraphQLCodeRegistry enhanceCodeRegistry(
            final GraphQLCodeRegistry codeReg,
            final List<WebApiEntityType> entityTypes)
    {
        return codeReg.transform(builder -> {
            entityTypes.forEach(ty -> builder.dataFetcher(coordinates(QUERY_TYPE_NAME, common.rootFieldNameForAggEntity(ty)), aggEntityFetcher(ty.type())));
        });
    }

    private GraphQLObjectType enhanceQueryType(
            final GraphQLObjectType queryType,
            final List<WebApiEntityType> entityTypes)
    {
        return queryType.transform(builder -> {
            entityTypes.forEach(ty -> builder
                    .field(newFieldDefinition()
                                   .name(common.rootFieldNameForAggEntity(ty))
                                   .description("Aggregation over %s.".formatted(getEntityTitle(ty.type())))
                                   .type(new GraphQLList(new GraphQLTypeReference(common.graphQlTypeNameForAggEntity(ty))))
                                   // TODO Once paging is supported in the fetcher.
                                   //        .argument(PAGE_NUMBER_ARGUMENT)
                                   //        .argument(PAGE_CAPACITY_ARGUMENT)
                                   .build()));
        });
    }

    private GraphQLObjectType mkGraphQlAggEntityType(final WebApiEntityType entityType) {
        final var graphQlEntityTypeRef = new GraphQLTypeReference(entityType.name());
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
                .name(common.graphQlTypeNameForAggEntity(entityType))
                .description("Aggregation over %s.".formatted(getEntityTitle(entityType.type())))
                .fields(fields)
                .build();
    }

    private DataFetcher<?> aggEntityFetcher(final Class<? extends AbstractEntity<?>> type) {
        return new AggEntityFetcher(type, coFinder, authorisationModel, securityTokenProvider, common);
    }

    private record AggEntityFetcher(
            Class<? extends AbstractEntity<?>> entityType,
            ICompanionObjectFinder coFinder,
            IAuthorisationModel authorisationModel,
            ISecurityTokenProvider securityTokenProvider,
            GraphQLCommon common)
            implements DataFetcher<List<EntityAggregates>>
    {

        @Override
        public List<EntityAggregates> get(final DataFetchingEnvironment environment) {
            authoriseReadingOf(entityType, READ, authorisationModel, securityTokenProvider).ifFailure(Result::throwRuntime);
            validateDuplicateFields(common.allSelectedFields(environment), common);
            final var query = buildQuery(entityType, environment);
            final var qem = from(query).model();
            final var co = coFinder.find(EntityAggregates.class, true);
            return co.getAllEntities(qem);

            // TODO Supporting paging requires supporting dot-notated yield aliases in source queries whose result type
            // is `EntityAggregates`.
            // See the TODO and exception path in `QuerySourceInfoProvider.produceQuerySourceInfoForEntityType`.
            /*
            final var _pageCapacity = common.getPageCapacityArgument(environment);
            final var pageCapacity = _pageCapacity._1;
            final var maybePageCapacityError = _pageCapacity._2;

            final var pageNo = Optional.ofNullable(environment.getArgument(PAGE_NUMBER))
                    .map(x -> (int) x)
                    .filter(n -> n >= 0)
                    .orElse(DEFAULT_PAGE_NUMBER);

            final var result = DataFetcherResult.<List<EntityAggregates>>newResult()
                    .data(co.getPage(qem, pageNo, pageCapacity).data());
            maybePageCapacityError.ifPresent(it -> result.error(newError(environment).message(it).build()));
            return result.build();
            */
        }

        private AggregatedResultQueryModel buildQuery(
                final Class<? extends AbstractEntity<?>> entityType,
                final DataFetchingEnvironment environment)
        {
            return addYields(addGroupBy(select(entityType).where().condition(EntityCondToEqlCompiler.compile(environment, common)), environment),
                             environment)
                    .modelAsAggregate();
        }

        /// Enhances `query` with yields for the fields that were selected.
        ///
        /// @param query  [ICompleted] or [ISubsequentCompletedAndYielded]
        /// @param environment  contains selected fields
        ///
        private <T extends AbstractEntity<?>> ICompletedCommon<T> addYields(
                final ICompletedCommon<T> query,
                final DataFetchingEnvironment environment)
        {
            interface F<U extends AbstractEntity<?>> extends Function<ICompletedCommon<U>, ICompletedCommon<U>> {}

            final var immediateFields = environment.getSelectionSet().getImmediateFields();

            final F<T> fCount = q -> findField(immediateFields, Fields.count)
                    .map(_ -> addYield(q, expr().ifNull().expr(expr().countAll().model()).then().val(0).model(), Fields.count))
                    .orElse(q);
            final F<T> fAvg = q -> findField(immediateFields, Fields.avg)
                    .map(avg -> addYieldsForField(q, avg,
                                                  fld -> expr().avgOf().prop(common.toPropertyPath(fld)).model(),
                                                  fld -> Fields.avg + "." + common.toPropertyPath(fld)))
                    .orElse(q);
            final F<T> fSum = q -> findField(immediateFields, Fields.sum)
                    .map(sum -> addYieldsForField(q, sum,
                                                  fld -> expr().sumOf().prop(common.toPropertyPath(fld)).model(),
                                                  fld -> Fields.sum + "." + common.toPropertyPath(fld)))
                    .orElse(q);
            final F<T> fMin = q -> findField(immediateFields, Fields.min)
                    .map(min -> addYieldsForField(q, min,
                                                  fld -> expr().minOf().prop(common.toPropertyPath(fld)).model(),
                                                  fld -> Fields.min + "." + common.toPropertyPath(fld)))
                    .orElse(q);
            final F<T> fMax = q -> findField(immediateFields, Fields.max)
                    .map(max -> addYieldsForField(q, max,
                                                  fld -> expr().maxOf().prop(common.toPropertyPath(fld)).model(),
                                                  fld -> Fields.max + "." + common.toPropertyPath(fld)))
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
                final DataFetchingEnvironment environment)
        {
            final var maybeGroupBy = findField(environment.getSelectionSet().getImmediateFields(), Fields.groupBy);
            if (maybeGroupBy.isEmpty()) {
                return query;
            }
            final var groupBy = maybeGroupBy.get();
            final var subFields = common.terminalFields(groupBy.getSelectionSet());
            if (subFields.isEmpty()) {
                return query;
            }
            else {
                final var grouped = foldLeft(subFields, query, (acc, fld) -> acc.groupBy().prop(common.toPropertyPath(fld)));
                return addYieldsForField(grouped, groupBy,
                                         fld -> expr().prop(common.toPropertyPath(fld)).model(),
                                         fld -> Fields.groupBy + "." + common.toPropertyPath(fld));
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
                final SelectedField field,
                final Function<SelectedField, ExpressionModel> mkExpr,
                final Function<SelectedField, CharSequence> mkAlias)
        {
            final var subFields = common.terminalFields(field.getSelectionSet());
            if (subFields.isEmpty()) {
                return query;
            }
            return foldLeft(subFields, query, (acc, fld) -> addYield(acc, mkExpr.apply(fld), mkAlias.apply(fld)));
        }

        private static Optional<SelectedField> findField(final Iterable<SelectedField> fields, final CharSequence name) {
            return stream(fields).filter(f -> f.getName().contentEquals(name)).findFirst();
        }

    }

}
