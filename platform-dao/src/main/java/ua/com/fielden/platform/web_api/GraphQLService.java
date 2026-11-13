package ua.com.fielden.platform.web_api;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import graphql.ExecutionInput;
import graphql.analysis.MaxQueryDepthInstrumentation;
import graphql.schema.GraphQLSchema;
import graphql.validation.QueryComplexityLimits;
import jakarta.annotation.Nullable;
import org.apache.logging.log4j.Logger;
import ua.com.fielden.platform.basic.config.IApplicationDomainProvider;
import ua.com.fielden.platform.dao.session.TransactionalExecution;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.security.user.IUserProvider;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.types.tuples.T2;
import ua.com.fielden.platform.utils.CollectionUtil;
import ua.com.fielden.platform.utils.ImmutableMapUtils;
import ua.com.fielden.platform.web_api.exceptions.WebApiException;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

import static graphql.GraphQL.newGraphQL;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;
import static org.apache.logging.log4j.LogManager.getLogger;
import static ua.com.fielden.platform.types.tuples.T2.t2;
import static ua.com.fielden.platform.web_api.WebApiUtils.*;

/// GraphQL-based implementation of the TG Web API using [graphql-java](https://github.com/graphql-java/graphql-java).
///
/// At this stage only the `query` operation type is supported.
///
/// ### Enhanced entity types
///
/// Enhanced entity types are stored in the database.
/// For each user, they are instantiated when that user first needs them.
/// They are then held in memory, until evicted, together with the schema that exposes them, which is built from the domain types and
/// those enhanced entity types.
/// An enhanced entity type that cannot be instantiated, or whose registration in the schema causes an error, is left out of the schema,
/// so requests that use it fail.
/// If the error cannot be attributed to a specific enhanced entity type, all of them are left out.
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

    /// Domain types exposed by the Web API.
    private final List<WebApiEntityType> domainTypes;
    /// The schema of [#domainTypes].
    private final GraphQLSchema initSchema;
    /// Enhanced entity types and schemas by user key.
    private final ConcurrentHashMap<String, UserApi> userApis;
    private final GraphQLSchemaFactory schemaFactory;
    private final GraphQLCommon common;
    private final EntityTypeEnhancer entityTypeEnhancer;
    private final ICompanionObjectFinder coFinder;
    private final Provider<TransactionalExecution> transactionalExecution;
    private final IUserProvider userProvider;

    /// The enhanced entity types of a user by name, and the schema that exposes them.
    ///
    private record UserApi(Map<String, EnhancedEntityType> enhancedTypes, GraphQLSchema schema) {
        UserApi {
            enhancedTypes = ImmutableMap.copyOf(enhancedTypes);
        }
    }

    /// Creates a GraphQLService instance based on `applicationDomainProvider` which contains all entity types.
    ///
    @Inject
    protected GraphQLService(
            final IApplicationDomainProvider applicationDomainProvider,
            final GraphQLSchemaFactory schemaFactory,
            final GraphQLCommon common,
            final EntityTypeEnhancer entityTypeEnhancer,
            final ICompanionObjectFinder coFinder,
            final Provider<TransactionalExecution> transactionalExecution,
            final IUserProvider userProvider)
    {
        this.schemaFactory = schemaFactory;
        this.common = common;
        this.entityTypeEnhancer = entityTypeEnhancer;
        this.coFinder = coFinder;
        this.transactionalExecution = transactionalExecution;
        this.userProvider = userProvider;
        this.userApis = new ConcurrentHashMap<>();
        try {
            LOGGER.info("GraphQL Web API...");
            domainTypes = common.streamVisibleTypes(applicationDomainProvider).map(WebApiEntityType::domainType).toList();
            initSchema = schemaFactory.create(domainTypes);
            LOGGER.info("GraphQL Web API...done");
        } catch (final Throwable t) {
            LOGGER.error("GraphQL Web API error.", t);
            throw t;
        }
    }

    @Override
    public Map<String, Object> execute(final Map<String, Object> input) {
        try {
            final var schema = loadUserApi(currentUser()).schema();
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
    public Enhancement enhanceEntityType(
            final String name,
            final @Nullable String baseType,
            final List<PropertyDefinition> addProperties,
            final List<String> removeProperties)
    {
        final var user = currentUser();
        final var enhancement = new AtomicReference<Enhancement>();
        userApis.compute(user.getKey(), (_, cachedUserApi) -> {
            final var userApi = cachedUserApi == null ? mkUserApi(user) : cachedUserApi;
            return transactionalExecution.get().exec(() -> {
                final var result = entityTypeEnhancer.enhance(userApi.schema(), name, baseType, addProperties, removeProperties);
                final var enhancedTypes = ImmutableMapUtils.insert(userApi.enhancedTypes(), name, result.entityType());
                enhancement.set(result);
                return mkUserApi(enhancedTypes);
            });
        });
        LOGGER.info(() -> "User [%s] enhanced entity type [%s]: %s".formatted(user.getKey(), name, enhancement.get().entityType().properties()));
        return enhancement.get();
    }

    @Override
    public Optional<EnhancedEntityType> findEnhancedEntityType(final CharSequence name) {
        return Optional.ofNullable(loadUserApi(currentUser()).enhancedTypes().get(name.toString()));
    }

    @Override
    public void evictUserEnhancedEntityTypes() {
        userApis.remove(currentUser().getKey());
    }

    private UserApi loadUserApi(final User user) {
        return userApis.computeIfAbsent(user.getKey(), _ -> mkUserApi(user));
    }

    /// Instantiates the enhanced entity types of `user`, which must be the current user, and builds the schema that exposes them.
    ///
    /// If the schema cannot be built, each enhanced entity type is registered in a schema on its own to find those that cause the error.
    ///
    private UserApi mkUserApi(final User user) {
        final UserDefinedEnhancedEntityTypeCo coEntityType = coFinder.find(UserDefinedEnhancedEntityType.class, true);
        final var enhancedTypes = coEntityType.findByUser(user).stream()
                .map(entityType -> {
                    try {
                        return t2(entityType.getName(), entityTypeEnhancer.javaTypeFor(entityType));
                    } catch (final Exception ex) {
                        LOGGER.warn(() -> "Entity type [%s] of user [%s] cannot be instantiated.".formatted(entityType.getName(), user.getKey()), ex);
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(T2.toMap());
        return tryMkUserApi(user, enhancedTypes)
                .or(() -> tryMkUserApi(user, enhancedTypes.values().stream()
                                             // TODO Optimise: incrementally add each type instead of rebuilding the whole schema.
                                             // Requires a new API: GraphQLSchemaFactory.addType(GraphQLSchema, WebApiEntityType).
                                             .filter(type -> tryMkUserApi(user, Map.of(type.name(), type)).isPresent())
                                             .collect(toMap(EnhancedEntityType::name, identity()))))
                .orElseGet(() -> mkUserApi(Map.of()));
    }

    /// Returns an empty optional if the schema cannot be built.
    ///
    private Optional<UserApi> tryMkUserApi(final User user, final Map<String, EnhancedEntityType> enhancedTypes) {
        try {
            return Optional.of(mkUserApi(enhancedTypes));
        } catch (final Exception ex) {
            LOGGER.warn(() -> "Schema of user [%s] cannot be built with entity types [%s].".formatted(user.getKey(), CollectionUtil.toString(enhancedTypes.keySet(), ", ")), ex);
            return Optional.empty();
        }
    }

    private UserApi mkUserApi(final Map<String, EnhancedEntityType> enhancedTypes) {
        if (enhancedTypes.isEmpty()) {
            return new UserApi(Map.of(), initSchema);
        }
        final var schema = schemaFactory.create(Stream.concat(domainTypes.stream(),
                                                              enhancedTypes.values().stream().map(EnhancedEntityType::webApiEntityType))
                                                        .toList());
        return new UserApi(enhancedTypes, schema);
    }

    private User currentUser() {
        final var user = userProvider.getUser();
        if (user == null) {
            throw new WebApiException("Current user is missing.");
        }
        return user;
    }

}
