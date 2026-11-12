package ua.com.fielden.platform.web_api;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import graphql.ExecutionInput;
import graphql.analysis.MaxQueryDepthInstrumentation;
import graphql.schema.GraphQLSchema;
import graphql.validation.QueryComplexityLimits;
import jakarta.annotation.Nullable;
import org.apache.logging.log4j.Logger;
import ua.com.fielden.platform.basic.config.IApplicationDomainProvider;
import ua.com.fielden.platform.security.user.IUserProvider;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.web_api.exceptions.WebApiException;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

import static graphql.GraphQL.newGraphQL;
import static org.apache.logging.log4j.LogManager.getLogger;
import static ua.com.fielden.platform.web_api.WebApiUtils.*;

/// GraphQL-based implementation of the TG Web API using [graphql-java](https://github.com/graphql-java/graphql-java).
///
/// At this stage only the `query` operation type is supported.
///
/// ### Enhanced entity types
///
/// Each user's enhanced entity types are held together with the schema that exposes them, which is built from the domain types and
/// those enhanced entity types.
/// A user without enhanced entity types is served by the schema of the domain types alone.
///
/// [#enhanceEntityType] runs one request of a user at a time, holding that user's entry while it validates the request and builds a new schema.
/// The entry is replaced only on success, so a failed request changes nothing, and concurrent requests cannot lose each other's changes.
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
            final IUserProvider userProvider)
    {
        this.schemaFactory = schemaFactory;
        this.common = common;
        this.entityTypeEnhancer = entityTypeEnhancer;
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
    public Enhancement enhanceEntityType(
            final String name,
            final @Nullable String baseType,
            final List<PropertyDefinition> addProperties,
            final List<String> removeProperties)
    {
        final var userKey = currentUser().getKey();
        final var enhancement = new AtomicReference<Enhancement>();
        userApis.compute(userKey, (_, userApi) -> {
            final var enhancedTypes = userApi == null ? Map.<String, EnhancedEntityType>of() : userApi.enhancedTypes();
            final var schema = userApi == null ? initSchema : userApi.schema();
            final var result = entityTypeEnhancer.enhance(schema, enhancedTypes, name, baseType, addProperties, removeProperties);
            final var newEnhancedTypes = new LinkedHashMap<>(enhancedTypes);
            newEnhancedTypes.put(name, result.entityType());
            final var newSchema = schemaFactory.create(Stream.concat(domainTypes.stream(),
                                                                     newEnhancedTypes.values().stream().map(EnhancedEntityType::webApiEntityType))
                                                               .toList());
            enhancement.set(result);
            return new UserApi(newEnhancedTypes, newSchema);
        });
        LOGGER.info(() -> "User [%s] enhanced entity type [%s]: %s".formatted(userKey, name, enhancement.get().entityType().properties()));
        return enhancement.get();
    }

    @Override
    public Optional<EnhancedEntityType> findEnhancedEntityType(final CharSequence name) {
        return Optional.ofNullable(userApis.get(currentUser().getKey()))
                .map(userApi -> userApi.enhancedTypes().get(name.toString()));
    }

    @Override
    public void discardEnhancedEntityTypes() {
        userApis.remove(currentUser().getKey());
    }

    private GraphQLSchema schemaForUser(final String userKey) {
        final var userApi = userApis.get(userKey);
        return userApi == null ? initSchema : userApi.schema();
    }

    private User currentUser() {
        final var user = userProvider.getUser();
        if (user == null) {
            throw new WebApiException("Current user is missing.");
        }
        return user;
    }

}
