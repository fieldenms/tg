package ua.com.fielden.platform.web_api;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import graphql.ExecutionInput;
import graphql.analysis.MaxQueryDepthInstrumentation;
import graphql.schema.*;
import graphql.schema.GraphQLObjectType.Builder;
import graphql.util.TraversalControl;
import graphql.util.TraverserContext;
import graphql.validation.QueryComplexityLimits;
import org.apache.logging.log4j.Logger;
import ua.com.fielden.platform.basic.config.IApplicationDomainProvider;
import ua.com.fielden.platform.domaintree.IDomainManager;
import ua.com.fielden.platform.domaintree.IDomainTreeEnhancer;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.reflection.PropertyTypeDeterminator;
import ua.com.fielden.platform.security.IAuthorisationModel;
import ua.com.fielden.platform.security.provider.ISecurityTokenProvider;
import ua.com.fielden.platform.security.user.IUserProvider;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.web_api.exceptions.WebApiException;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

import static graphql.GraphQL.newGraphQL;
import static graphql.schema.FieldCoordinates.coordinates;
import static graphql.schema.GraphQLCodeRegistry.newCodeRegistry;
import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;
import static graphql.schema.GraphQLObjectType.newObject;
import static graphql.schema.GraphQLSchema.newSchema;
import static graphql.schema.SchemaTransformer.transformSchema;
import static java.lang.String.format;
import static java.util.Comparator.comparing;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.stream.Collectors.*;
import static org.apache.logging.log4j.LogManager.getLogger;
import static ua.com.fielden.platform.domaintree.impl.AbstractDomainTree.reflectionProperty;
import static ua.com.fielden.platform.domaintree.impl.AbstractDomainTreeRepresentation.isExcluded;
import static ua.com.fielden.platform.reflection.TitlesDescsGetter.getEntityTitleAndDesc;
import static ua.com.fielden.platform.utils.EntityUtils.isUnionEntityType;
import static ua.com.fielden.platform.web_api.FieldSchema.*;
import static ua.com.fielden.platform.web_api.GraphQLCommon.QUERY_TYPE_NAME;
import static ua.com.fielden.platform.web_api.GraphQLPropertyDataFetcher.fetching;
import static ua.com.fielden.platform.web_api.WebApiUtils.*;

/// GraphQL-based implementation of the TG Web API using [graphql-java](https://github.com/graphql-java/graphql-java).
///
/// At this stage only the `query` operation type is supported.
///
/// ### Entity type enhancements
///
/// This implementation uses an [IDomainTreeEnhancer] singleton (bound to [IWebApi#WEB_API_DOMAIN_TREE_ENHANCER])
/// to access enhanced entity types.
///
/// Other parts of the system may use this [IDomainTreeEnhancer] instance to enhance entity types with extra properties,
/// which can then be queried through GraphQL.
///
/// After an entity type is enhanced, the GraphQL schema has to be rebuilt for the enhancements be reflected in the GraphQL types.
/// For example, if a property is added to `Vehicle`, the schema has to be rebuilt by adding a corresponding field to the GraphQL type `Vehicle`.
/// This is achieved by storing the schema in [AtomicReference] and rebuilding it when requested.
///
@Singleton
public class GraphQLService implements IWebApi {

    private static final Logger LOGGER = getLogger(GraphQLService.class);

    public static final int DEFAULT_MAX_QUERY_DEPTH = 15; // this is the lowest value needed to load schema in GraphiQL editor (for version >= 3.2.3)
    public static final int DEFAULT_MAX_PAGE_CAPACITY = 100;

    private static final String
            ERR_EXECUTING_QUERY = "Query [%s] execution completed with errors [%s].",
            ERR_EXECUTING_QUERY_WITH_EX = "Query [%s] execution completed with exception.";
    public static final String
            WARN_INSUFFICIENT_MAX_QUERY_DEPTH = "Web API maximum query depth [%s] is insufficient for GraphiQL editor. Minimum value [" + DEFAULT_MAX_QUERY_DEPTH + "] was used.";

    private final GraphQLSchema initSchema;
    private final ConcurrentHashMap<String, GraphQLSchema> schemas;
    private final ICompanionObjectFinder coFinder;
    private final IAuthorisationModel authorisationModel;
    private final ISecurityTokenProvider securityTokenProvider;
    private final EntityTypeIntrospection entityTypeIntrospection;
    private final EntityAggregation entityAggregation;
    private final FluentConditions fluentConditions;
    private final GraphQLCommon common;
    private final IDomainManager domainManager;
    private final IUserProvider userProvider;

    /// Creates a GraphQLService instance based on `applicationDomainProvider` which contains all entity types.
    ///
    /// @param authorisationModel  an auth model to authorise running of Web API queries and their [FieldVisibility]
    ///
    @Inject
    protected GraphQLService(
            final IApplicationDomainProvider applicationDomainProvider,
            final ICompanionObjectFinder coFinder,
            final IAuthorisationModel authorisationModel,
            final ISecurityTokenProvider securityTokenProvider,
            final EntityTypeIntrospection entityTypeIntrospection,
            final EntityAggregation entityAggregation,
            final FluentConditions fluentConditions,
            final GraphQLCommon common,
            final IDomainManager domainManager,
            final IUserProvider userProvider)
    {
        this.coFinder = coFinder;
        this.authorisationModel = authorisationModel;
        this.securityTokenProvider = securityTokenProvider;
        this.entityTypeIntrospection = entityTypeIntrospection;
        this.entityAggregation = entityAggregation;
        this.fluentConditions = fluentConditions;
        this.common = common;
        this.domainManager = domainManager;
        this.userProvider = userProvider;
        this.schemas = new ConcurrentHashMap<>();
        try {
            LOGGER.info("GraphQL Web API...");
            initSchema = buildSchema(common.streamVisibleTypes(applicationDomainProvider).map(WebApiEntityType::domainType).toList());
            LOGGER.info("GraphQL Web API...done");
        } catch (final Throwable t) {
            LOGGER.error("GraphQL Web API error.", t);
            throw t;
        }
    }

    /// Builds a schema that exposes `entityTypes`.
    ///
    /// Each entity type gets an object type, and those that are not unions also get root fields.
    /// Entity types without fields suitable for querying are omitted.
    ///
    private GraphQLSchema buildSchema(final List<WebApiEntityType> entityTypes) {
        final GraphQLCodeRegistry.Builder codeRegistryBuilder = newCodeRegistry();

        final var queryableTypes = entityTypes.stream().filter(ty -> !isUnionEntityType(ty.type())).toList();

        LOGGER.debug("\tBuilding dictionary...");
        // The dictionary must list all referenced types.
        final var dictionary = createDictionary(entityTypes);

        LOGGER.debug("\tBuilding query type...");
        final var queryType = createQueryType(queryableTypes, codeRegistryBuilder);

        LOGGER.debug("\tBuilding field visibility...");
        codeRegistryBuilder.fieldVisibility(new FieldVisibility(authorisationModel, queryableTypes, securityTokenProvider));

        LOGGER.debug("\tBuilding default data fetcher...");
        codeRegistryBuilder.defaultDataFetcher(env -> fetching(env.getFieldDefinition().getName()));

        LOGGER.debug("\tBuilding schema...");
        final var schema = newSchema()
                .codeRegistry(codeRegistryBuilder.build())
                .query(queryType)
                .additionalTypes(new LinkedHashSet<>(dictionary))
                .build();
        return fluentConditions.enhanceSchema(
                entityAggregation.enhanceSchema(
                        entityTypeIntrospection.enhanceSchema(schema, entityTypes),
                        entityTypes),
                entityTypes);
    }

    @Override
    public Map<String, Object> execute(final Map<String, Object> input) {
        try {
            final var schema = schemaForUser(currentUser().getKey());
            final var graphQL = newGraphQL(schema)
                    .queryExecutionStrategy(new GraphQLAsyncExecutionStrategy(new GraphQLSimpleDataFetcherExceptionHandler()))
                    .instrumentation(new MaxQueryDepthInstrumentation(common.maxQueryDepth()))
                    .build();
            // Some errors are thrown rather than captured in a result object, hence the try/catch.
            // E.g., OneOfTooManyKeysException.
            Map<String, Object> result;
            try {
                result = graphQL.execute(
                                ExecutionInput.newExecutionInput()
                                        .query(WebApiUtils.query(input))
                                        .operationName(WebApiUtils.operationName(input).orElse(null))
                                        .variables(WebApiUtils.variables(input))
                                        // Align graphql-java's default query-complexity limits (introduced in 26.0) with the configured maximum query depth,
                                        // so that raising `web.api.maxQueryDepth` above graphql-java's default of 100 is not silently capped at 100.
                                        // The field-count guard is retained at its default, as a safeguard against pathologically large queries.
                                        .graphQLContext(ctx -> ctx.put(
                                                QueryComplexityLimits.KEY,
                                                QueryComplexityLimits.newLimits()
                                                        .maxDepth(common.maxQueryDepth())
                                                        .maxFieldsCount(QueryComplexityLimits.DEFAULT_MAX_FIELDS_COUNT)
                                                        .build())))
                        .toSpecification();
            } catch (final Throwable e) {
                final var msg = e.getMessage() != null ? e.getMessage() : e.toString();
                result = mkResultWithErrors(List.of(mkError(msg)));
            }
            final var errors = errors(result);
            if (!errors.isEmpty()) {
                LOGGER.error(() -> ERR_EXECUTING_QUERY.formatted(input, errors));
            }
            return result;
        } catch (final Throwable throwable) {
            LOGGER.error(() -> ERR_EXECUTING_QUERY_WITH_EX.formatted(input), throwable);
            throw throwable;
        }
    }

    @Override
    public void propertiesAdded(final Class<? extends AbstractEntity<?>> entityType, final Set<? extends CharSequence> properties) {
        if (properties.isEmpty()) {
            return;
        }

        final var baseEntityType = PropertyTypeDeterminator.baseEntityType(entityType);

        updateSchemaForUser(currentUser().getKey(), oldSchema -> {
            final var oldGraphQlEntityType = common.getGraphQlEntityType(oldSchema, baseEntityType.getSimpleName());
            final var oldGraphQlEntityCondCondType = common.getCoercedType(oldSchema, common.graphQlTypeNameForCondEntityCond(baseEntityType), GraphQLInputObjectType.class);
            return transformSchema(oldSchema, new GraphQLTypeVisitorStub() {
                @Override
                public TraversalControl visitGraphQLObjectType(
                        final GraphQLObjectType node,
                        final TraverserContext<GraphQLSchemaElement> context)
                {
                    // Modifying the entity type also covers the aggregation type which is defined in terms of that type.
                    if (oldGraphQlEntityType.getName().equals(node.getName())) {
                        final var newFields = properties.stream()
                                .map(prop -> createGraphQLFieldDefinition(entityType, prop.toString())
                                             .orElseThrow(() -> new WebApiException(format("GraphQL field cannot be created for [%s.%s].", entityType.getTypeName(), prop))))
                                .toList();
                        final var newNode = node.transform(b -> b.fields(newFields));
                        return changeNode(context, newNode);
                    }
                    else {
                        return super.visitGraphQLObjectType(node, context);
                    }
                }

                @Override
                public TraversalControl visitGraphQLInputObjectType(
                        final GraphQLInputObjectType node,
                        final TraverserContext<GraphQLSchemaElement> context)
                {
                    // Entity_Cond_cond type: add the new properties so that they can be filtered on.
                    if (oldGraphQlEntityCondCondType.getName().equals(node.getName())) {
                        final var newFields = properties.stream()
                                .map(prop -> fluentConditions.createEntityCondCondField(entityType, prop, oldSchema))
                                .flatMap(Optional::stream)
                                .toList();
                        final var newNode = node.transform(b -> b.fields(newFields));
                        return changeNode(context, newNode);
                    }
                    else {
                        return super.visitGraphQLInputObjectType(node, context);
                    }
                }
            });
        });
    }

    @Override
    public void propertiesRemoved(
            final Class<? extends AbstractEntity<?>> entityType,
            final Set<? extends CharSequence> properties)
    {
        if (properties.isEmpty()) {
            return;
        }

        final var baseEntityType = PropertyTypeDeterminator.baseEntityType(entityType);

        updateSchemaForUser(currentUser().getKey(), oldSchema -> {
            final var oldGraphQlEntityType = common.getGraphQlEntityType(oldSchema, baseEntityType.getSimpleName());
            final var oldGraphQlEntityCondCondType = common.getCoercedType(oldSchema, common.graphQlTypeNameForCondEntityCond(baseEntityType), GraphQLInputObjectType.class);
            return transformSchema(oldSchema, new GraphQLTypeVisitorStub() {
                @Override
                public TraversalControl visitGraphQLObjectType(
                        final GraphQLObjectType node,
                        final TraverserContext<GraphQLSchemaElement> context)
                {
                    // Modifying the entity type also covers the aggregation type which is defined in terms of that type.
                    if (oldGraphQlEntityType.getName().equals(node.getName())) {
                        final var newFields = node.getFields().stream().filter(fld -> !properties.contains(fld.getName())).toList();
                        final var newNode = node.transform(b -> b.replaceFields(newFields));
                        return changeNode(context, newNode);
                    }
                    else {
                        return super.visitGraphQLObjectType(node, context);
                    }
                }

                @Override
                public TraversalControl visitGraphQLInputObjectType(
                        final GraphQLInputObjectType node,
                        final TraverserContext<GraphQLSchemaElement> context)
                {
                    // Entity_Cond_cond type.
                    if (oldGraphQlEntityCondCondType.getName().equals(node.getName())) {
                        final var namesOfFieldsToRemove = properties.stream().map(prop -> fluentConditions.entityCondCondFieldName(entityType, prop)).collect(toSet());
                        final var newFields = node.getFields().stream().filter(fld -> !namesOfFieldsToRemove.contains(fld.getName())).toList();
                        final var newNode = node.transform(b -> b.replaceFields(newFields));
                        return changeNode(context, newNode);
                    }
                    else {
                        return super.visitGraphQLInputObjectType(node, context);
                    }
                }
            });
        });
    }

    private GraphQLSchema schemaForUser(final String userKey) {
        return schemas.getOrDefault(userKey, initSchema);
    }

    private User currentUser() {
        final var user = userProvider.getUser();
        if (user == null) {
            throw new WebApiException("Current user is missing.");
        }
        return user;
    }

    private GraphQLSchema updateSchemaForUser(final String userKey, final Function<? super GraphQLSchema, GraphQLSchema> fn) {
        return schemas.compute(userKey, (_, s) -> fn.apply(s == null ? initSchema : s));
    }

    /// Creates a GraphQL dictionary (aka GraphQL "additional types") for the specified entity types.
    ///
    /// The set of resultant types can be smaller than the specified one.
    /// See [#createGraphQLTypeFor(WebApiEntityType)] for more details.
    ///
    private List<GraphQLObjectType> createDictionary(final List<WebApiEntityType> entityTypes) {
        return entityTypes.stream()
            .map(this::createGraphQLTypeFor)
            .flatMap(Optional::stream)
            .sorted(comparing(GraphQLObjectType::getName))
            .toList();
    }

    /// Creates the "query" type that will contain root fields.
    ///
    /// Root fields are named using [GraphQLCommon#rootFieldName].
    ///
    /// @param entityTypes         entity types to register as root fields
    /// @param codeRegistryBuilder a place to register root data fetchers
    ///
    private GraphQLObjectType createQueryType(
            final List<WebApiEntityType> entityTypes,
            final GraphQLCodeRegistry.Builder codeRegistryBuilder)
    {
        final Builder queryTypeBuilder = newObject().name(QUERY_TYPE_NAME).description("Query following **entities** represented as GraphQL root fields:");
        entityTypes.forEach(entityType -> {
            final String fieldName = common.rootFieldName(entityType);
            queryTypeBuilder.field(newFieldDefinition()
                .name(fieldName)
                .description(format("Query %s.", bold(getEntityTitleAndDesc(entityType.type()).getKey())))
                .argument(ORDER_ARGUMENT)
                .argument(PAGE_NUMBER_ARGUMENT)
                .argument(PAGE_CAPACITY_ARGUMENT)
                .type(new GraphQLList(new GraphQLTypeReference(entityType.name()))));
            codeRegistryBuilder.dataFetcher(coordinates(QUERY_TYPE_NAME, fieldName),
                                            new RootEntityFetcher<>(entityType.type(),
                                                                    coFinder,
                                                                    authorisationModel,
                                                                    securityTokenProvider,
                                                                    common,
                                                                    domainManager,
                                                                    userProvider));
        });
        return queryTypeBuilder.build();
    }

    /// Creates a GraphQL object type for querying `entityType`.
    /// The result is an empty optional if `entityType` has no suitable fields for querying.
    ///
    private Optional<GraphQLObjectType> createGraphQLTypeFor(final WebApiEntityType entityType) {
        final var type = entityType.type();
         // Generic type exclusion logic for root types (exclude abstract entity types, types without KeyType annotation, etc.).
        if (isExcluded(type, "")) {
            return empty();
        }
        final var graphQLFieldDefinitions = common.propertiesForGraphQlFields(type).stream()
            .filter(field -> !isExcluded(type, reflectionProperty(field.getName())))
            .map(field -> createGraphQLFieldDefinition(type, field.getName()))
            .flatMap(Optional::stream)
            .toList();
        if (!graphQLFieldDefinitions.isEmpty()) {
            return of(newObject()
                .name(entityType.name())
                .description(titleAndDescRepresentation(getEntityTitleAndDesc(type)))
                .fields(graphQLFieldDefinitions).build());
        }
        return empty();
    }

}
