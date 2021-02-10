package ua.com.fielden.platform.web_api;

import static com.helger.jcodemodel.JMod.FINAL;
import static com.helger.jcodemodel.JMod.PRIVATE;
import static com.helger.jcodemodel.JMod.PUBLIC;
import static com.helger.jcodemodel.JMod.STATIC;
import static graphql.ExecutionInput.newExecutionInput;
import static graphql.GraphQL.newGraphQL;
import static graphql.GraphqlErrorBuilder.newError;
import static graphql.schema.FieldCoordinates.coordinates;
import static graphql.schema.GraphQLCodeRegistry.newCodeRegistry;
import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;
import static graphql.schema.GraphQLObjectType.newObject;
import static graphql.schema.GraphQLSchema.newSchema;
import static java.lang.Class.forName;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang.StringUtils.uncapitalize;
import static ua.com.fielden.platform.domaintree.impl.AbstractDomainTree.reflectionProperty;
import static ua.com.fielden.platform.domaintree.impl.AbstractDomainTreeRepresentation.constructKeysAndProperties;
import static ua.com.fielden.platform.domaintree.impl.AbstractDomainTreeRepresentation.isExcluded;
import static ua.com.fielden.platform.error.Result.failure;
import static ua.com.fielden.platform.reflection.TitlesDescsGetter.getEntityTitleAndDesc;
import static ua.com.fielden.platform.security.tokens.web_api.WebApiTemplate.QUERY;
import static ua.com.fielden.platform.streaming.ValueCollectors.toLinkedHashMap;
import static ua.com.fielden.platform.utils.EntityUtils.isPersistedEntityType;
import static ua.com.fielden.platform.utils.EntityUtils.isSyntheticEntityType;
import static ua.com.fielden.platform.utils.Pair.pair;
import static ua.com.fielden.platform.web_api.FieldSchema.ORDER_ARGUMENT;
import static ua.com.fielden.platform.web_api.FieldSchema.PAGE_CAPACITY_ARGUMENT;
import static ua.com.fielden.platform.web_api.FieldSchema.PAGE_NUMBER_ARGUMENT;
import static ua.com.fielden.platform.web_api.FieldSchema.bold;
import static ua.com.fielden.platform.web_api.FieldSchema.createGraphQLFieldDefinition;
import static ua.com.fielden.platform.web_api.FieldSchema.titleAndDescRepresentation;
import static ua.com.fielden.platform.web_api.RootEntityFetcher.findToken;
import static ua.com.fielden.platform.web_api.RootEntityFetcher.queryPackageName;
import static ua.com.fielden.platform.web_api.RootEntityFetcher.queryTokenNameFor;
import static ua.com.fielden.platform.web_api.RootEntityFetcher.rootPackageName;
import static ua.com.fielden.platform.web_api.RootEntityUtils.QUERY_TYPE_NAME;
import static ua.com.fielden.platform.web_api.WebApiUtils.operationName;
import static ua.com.fielden.platform.web_api.WebApiUtils.query;
import static ua.com.fielden.platform.web_api.WebApiUtils.variables;

import java.io.File;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import org.apache.log4j.Logger;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.helger.jcodemodel.AbstractJClass;
import com.helger.jcodemodel.JCodeModel;
import com.helger.jcodemodel.JDefinedClass;
import com.helger.jcodemodel.JExpr;
import com.helger.jcodemodel.JFieldRef;
import com.helger.jcodemodel.JFieldVar;
import com.helger.jcodemodel.JPackage;

import graphql.ExecutionResultImpl;
import graphql.GraphQL;
import graphql.schema.GraphQLCodeRegistry;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLList;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLObjectType.Builder;
import graphql.schema.GraphQLSchema;
import graphql.schema.GraphQLType;
import graphql.schema.GraphQLTypeReference;
import ua.com.fielden.platform.attachment.Attachment;
import ua.com.fielden.platform.basic.config.IApplicationDomainProvider;
import ua.com.fielden.platform.basic.config.Workflows;
import ua.com.fielden.platform.domain.PlatformDomainTypes;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.AbstractPersistentEntity;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.reflection.TitlesDescsGetter;
import ua.com.fielden.platform.security.IAuthorisationModel;
import ua.com.fielden.platform.security.ISecurityToken;
import ua.com.fielden.platform.security.tokens.web_api.WebApiTemplate;
import ua.com.fielden.platform.security.tokens.web_api.WebApi_CanExecute_Token;
import ua.com.fielden.platform.security.user.SecurityRoleAssociation;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.security.user.UserAndRoleAssociation;
import ua.com.fielden.platform.security.user.UserRole;
import ua.com.fielden.platform.utils.IDates;
import ua.com.fielden.platform.utils.Pair;

/**
 * Represents GraphQL implementation of TG Web API using graphql-java library.
 * <p>
 * At this stage only GraphQL 'query' is supported. GraphQL 'mutation' support is on the next stages.
 * 
 * @author TG Team
 *
 */
public class GraphQLService implements IWebApi {
    private final Logger logger = Logger.getLogger(getClass());
    private final GraphQL graphQL;
    private final IAuthorisationModel authorisation;
    
    /**
     * Creates GraphQLService instance based on {@code applicationDomainProvider} which contains all entity types.
     * <p>
     * We start by building dictionary of all our custom GraphQL types from existing domain entity types.
     * Next, we create GraphQL type for quering (aka GraphQL 'query') and assign it to the schema.
     * 
     * @param applicationDomainProvider
     * @param coFinder
     * @param dates
     * @param authorisation
     * @param pathToSecurityTokens
     * @param securityTokensPackageName
     * @param workflow -- in case of {@link Workflows#development}, security tokens will be updated
     */
    @Inject
    public GraphQLService(
        final IApplicationDomainProvider applicationDomainProvider,
        final ICompanionObjectFinder coFinder,
        final IDates dates,
        final IAuthorisationModel authorisation,
        final @Named("tokens.path") String pathToSecurityTokens,
        final @Named("tokens.package") String securityTokensPackageName,
        final @Named("workflow") String workflow
    ) {
        try {
            logger.info("GraphQL Web API...");
            this.authorisation = authorisation;
            final GraphQLCodeRegistry.Builder codeRegistryBuilder = newCodeRegistry();
            logger.info("\tBuilding dictionary...");
            final Map<Class<? extends AbstractEntity<?>>, GraphQLType> dictionary = createDictionary(persistentAndSyntheticDomainTypes(applicationDomainProvider));
            final Set<Class<? extends AbstractEntity<?>>> dictionaryTypes = dictionary.keySet();
            if (Workflows.development.equals(Workflows.valueOf(workflow))) {
                logger.info("\tUpdating security tokens [DEVELOPMENT MODE]...");
                updateSecurityTokens(dictionaryTypes, pathToSecurityTokens, securityTokensPackageName);
            }
            logger.info("\tBuilding query type...");
            final GraphQLObjectType queryType = createQueryType(dictionaryTypes, coFinder, dates, codeRegistryBuilder, authorisation, securityTokensPackageName);
            logger.info("\tBuilding schema...");
            final GraphQLSchema schema = newSchema()
                    .codeRegistry(codeRegistryBuilder.build())
                    .query(queryType)
                    .additionalTypes(new LinkedHashSet<>(dictionary.values()))
                    .build();
            logger.info("\tBuilding service...");
            graphQL = newGraphQL(schema).build();
            logger.info("GraphQL Web API...done");
        } catch (final Throwable t) {
            logger.error("GraphQL Web API error.", t);
            throw t;
        }
    }
    
    /**
     * Updates Web API security tokens.
     * <p>
     * Generates root (top-level) token if not yet present.<br>
     * Generates tokens for entity types in {@code dictionaryTypes} if not yet present.
     * <p>
     * At this stage it does not remove tokens for the types that have been removed and does not update existing tokens.
     * All tokens can be deleted manually before starting server to achieve deletion / updating.
     * 
     * @param dictionaryTypes
     * @param pathToSecurityTokens
     * @param securityTokensPackageName
     */
    private void updateSecurityTokens(final Set<Class<? extends AbstractEntity<?>>> dictionaryTypes, final String pathToSecurityTokens, final String securityTokensPackageName) {
        final String rootPackageName = rootPackageName(securityTokensPackageName);
        final String rootTokenName = "WebApiToken";
        try {
            final Class<? extends ISecurityToken> rootTokenType = (Class<? extends ISecurityToken>) forName(rootPackageName + "." + rootTokenName);
            final boolean someGenerated = dictionaryTypes.stream()
                .map(entityType -> {
                    try {
                        findToken(securityTokensPackageName, entityType);
                        return false;
                    } catch (final ClassNotFoundException notFoundEx) {
                        genSecurityTokenFor(entityType, pathToSecurityTokens, queryPackageName(rootPackageName), queryTokenNameFor(entityType), rootTokenType);
                        return true;
                    }
                })
                .reduce(Boolean::logicalOr).orElse(false);
            if (someGenerated) {
                throw new IllegalStateException("Refresh pojo-bl module. Some Web API tokens have been added.");
            }
        } catch (final ClassNotFoundException notFoundEx) {
            genRootSecurityToken(pathToSecurityTokens, rootPackageName, rootTokenName);
            throw new IllegalStateException(format("Refresh pojo-bl module. Top-level Web API token [%s] has been generated.", rootTokenName));
        }
    }
    
    /**
     * Generates [Can Query] security token for the specified entity type (GraphQL root fields).
     * 
     * @param entityType
     * @param pathToSecurityTokens
     * @param packageName -- package name for the token to be generated
     * @param tokenName - the name for the token
     * @param topLevelToken
     */
    public static void genSecurityTokenFor(final Class<? extends AbstractEntity<?>> entityType, final String pathToSecurityTokens, final String packageName, final String tokenName, final Class<? extends ISecurityToken> topLevelToken) {
        try {
            final JCodeModel cm = new JCodeModel();
            final JPackage jp = cm._package(packageName);
            
            final AbstractJClass jTitlesDescsGetter = cm.ref(TitlesDescsGetter.class.getName());
            final AbstractJClass jEntityType = cm.ref(entityType.getName());
            final AbstractJClass jStringType = cm.ref(String.class.getName());
            final AbstractJClass jTemplateType = cm.ref(WebApiTemplate.class.getName());
            final JFieldRef jTemplateValue = jTemplateType.staticRef(QUERY.name());
            
            final JDefinedClass tokenType = jp._class(tokenName);
            tokenType.javadoc().add(format("A security token for entity {@link %s} to guard Web API querying.", entityType.getSimpleName()));
            tokenType._extends(topLevelToken);
            
            final JFieldVar jEntityTitleField = 
                tokenType.field(PRIVATE | STATIC | FINAL, String.class, "ENTITY_TITLE", jTitlesDescsGetter.staticInvoke("getEntityTitleAndDesc").arg(JExpr.dotclass(jEntityType)).invoke("getKey"));
            tokenType.field(PUBLIC | STATIC | FINAL, String.class, "TITLE", jStringType.staticInvoke("format").arg(JExpr.invoke(jTemplateValue, "forTitle")).arg(jEntityTitleField));
            tokenType.field(PUBLIC | STATIC | FINAL, String.class, "DESC", jStringType.staticInvoke("format").arg(JExpr.invoke(jTemplateValue, "forDesc")).arg(jEntityTitleField));
            
            final File srcDir = new File(pathToSecurityTokens.replace("target/classes", "") + "src/main/java/");
            cm.build(srcDir);
        } catch (final Exception ex) {
            throw failure(entityType, ex);
        }
    }
    
    /**
     * Generates root (top-level) security token.
     * 
     * @param pathToSecurityTokens
     * @param packageName -- package name for the token to be generated
     * @param tokenName - the name for the token
     */
    public static void genRootSecurityToken(final String pathToSecurityTokens, final String packageName, final String tokenName) {
        try {
            final JCodeModel cm = new JCodeModel();
            final JPackage jp = cm._package(packageName);
            
            final JDefinedClass tokenType = jp._class(tokenName);
            tokenType.javadoc().add("Top level security token for all security tokens that belong to Web API.");
            tokenType._implements(ISecurityToken.class);
            
            tokenType.field(PUBLIC | STATIC | FINAL, String.class, "TITLE", JExpr.lit("Web API"));
            tokenType.field(PUBLIC | STATIC | FINAL, String.class, "DESC", JExpr.lit("Web API tokens."));
            
            final File srcDir = new File(pathToSecurityTokens.replace("target/classes", "") + "src/main/java/");
            cm.build(srcDir);
        } catch (final Exception ex) {
            throw failure(ex);
        }
    }
    
    /**
     * Returns all domain [non-platform] types of persistent / synthetic nature. This includes persistent with activatable nature,
     * synthetic based on persistent. This does not include union, functional and any other entities.
     * 
     * @return
     */
    private List<Class<? extends AbstractEntity<?>>> persistentAndSyntheticDomainTypes(final IApplicationDomainProvider applicationDomainProvider) {
        final List<Class<? extends AbstractPersistentEntity<? extends Comparable<?>>>> supportedPlatformTypes = asList(User.class, UserRole.class, UserAndRoleAssociation.class, SecurityRoleAssociation.class, Attachment.class);
        return applicationDomainProvider.entityTypes().stream()
            .filter(type -> 
                    (supportedPlatformTypes.stream().anyMatch(pType -> pType.isAssignableFrom(type)) || !PlatformDomainTypes.types.contains(type)) // includes supportedPlatformTypes OR non-platform domain types
                &&  (isSyntheticEntityType(type) || isPersistedEntityType(type) || type.getSimpleName().endsWith("GroupingProperty")) ) // '...GroupingProperty' is the naming pattern for enum-like entities for groupBy criteria
            .collect(toList());
    }
    
    /**
     * Executes Web API query by using internal {@link GraphQL} service with predefined schema.
     * <p>
     * {@inheritDoc}
     */
    @Override
    public Map<String, Object> execute(final Map<String, Object> input) {
        final Result authResult = authorisation.authorise(WebApi_CanExecute_Token.class);
        if (!authResult.isSuccessful()) {
            return new ExecutionResultImpl(newError().message(authResult.getMessage()).build()).toSpecification();
        }
        return graphQL.execute(newExecutionInput()
            .query(query(input))
            .operationName(operationName(input).orElse(null))
            .variables(variables(input))
        ).toSpecification();
    }
    
    /**
     * Creates map of GraphQL dictionary (aka GraphQL "additional types") and corresponding entity types.
     * <p>
     * The set of resultant types can be smaller than those derived upon. See {@link #createGraphQLTypeFor(Class)} for more details.
     * 
     * @param entityTypes
     * @return
     */
    private static Map<Class<? extends AbstractEntity<?>>, GraphQLType> createDictionary(final List<Class<? extends AbstractEntity<?>>> entityTypes) {
        return entityTypes.stream()
            .map(GraphQLService::createGraphQLTypeFor)
            .flatMap(optType -> optType.map(Stream::of).orElseGet(Stream::empty))
            .sorted((pair1, pair2) -> pair1.getKey().getSimpleName().compareTo(pair2.getKey().getSimpleName()))
            .collect(toLinkedHashMap(Pair::getKey, Pair::getValue));
    }
    
    /**
     * Creates type for GraphQL 'query' operation to query entities from {@code dictionary}.
     * <p>
     * All query field names are represented as uncapitalised entity type simple names. All sub-field names are represented as entity property names.
     * 
     * @param dictionary -- list of supported GraphQL entity types
     * @param coFinder
     * @param dates
     * @param codeRegistryBuilder -- a place to register root data fetchers
     * @param authorisation
     * @param securityTokensPackageName
     * @return
     */
    private static GraphQLObjectType createQueryType(final Set<Class<? extends AbstractEntity<?>>> dictionary, final ICompanionObjectFinder coFinder, final IDates dates, final GraphQLCodeRegistry.Builder codeRegistryBuilder, final IAuthorisationModel authorisation, final String securityTokensPackageName) {
        final Builder queryTypeBuilder = newObject().name(QUERY_TYPE_NAME).description("Query following **entities** represented as GraphQL root fields:");
        dictionary.stream().forEach(entityType -> {
            final String simpleTypeName = entityType.getSimpleName();
            final String fieldName = uncapitalize(simpleTypeName);
            queryTypeBuilder.field(newFieldDefinition()
                .name(fieldName)
                .description(format("Query %s.", bold(getEntityTitleAndDesc(entityType).getKey())))
                .argument(ORDER_ARGUMENT)
                .argument(PAGE_NUMBER_ARGUMENT)
                .argument(PAGE_CAPACITY_ARGUMENT)
                .type(new GraphQLList(new GraphQLTypeReference(simpleTypeName)))
            );
            codeRegistryBuilder.dataFetcher(coordinates(QUERY_TYPE_NAME, fieldName), new RootEntityFetcher<>((Class<AbstractEntity<?>>) entityType, coFinder, dates, authorisation, securityTokensPackageName));
        });
        return queryTypeBuilder.build();
    }
    
    /**
     * Creates {@link Optional} GraphQL object type for {@code entityType}d entities querying.
     * <p>
     * The {@code entityType} will not have corresponding {@link GraphQLObjectType} only if there are no suitable fields for querying.
     * 
     * @param entityType
     * @return
     */
    private static Optional<Pair<Class<? extends AbstractEntity<?>>, GraphQLObjectType>> createGraphQLTypeFor(final Class<? extends AbstractEntity<?>> entityType) {
        final List<GraphQLFieldDefinition> graphQLFieldDefinitions = constructKeysAndProperties(entityType, true).stream()
            .filter(field -> !isExcluded(entityType, reflectionProperty(field.getName())))
            .map(field -> createGraphQLFieldDefinition(entityType, field.getName()))
            .flatMap(optField -> optField.map(Stream::of).orElseGet(Stream::empty))
            .collect(toList());
        if (!graphQLFieldDefinitions.isEmpty()) { // ignore types that have no GraphQL field equivalents; we can not use such types for any purpose including querying
            // GraphQL type names are used to reference the types in other ones when the type is not created yet.
            // We argue that simple names of domain types are unique across application domain.
            // So these will be used for GraphQL type naming.
            return of(pair(entityType, newObject()
                .name(entityType.getSimpleName())
                .description(titleAndDescRepresentation(getEntityTitleAndDesc(entityType)))
                .fields(graphQLFieldDefinitions).build()
            ));
        }
        return empty();
    }
    
}