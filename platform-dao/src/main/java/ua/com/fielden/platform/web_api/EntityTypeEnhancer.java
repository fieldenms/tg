package ua.com.fielden.platform.web_api;

import graphql.schema.GraphQLSchema;
import jakarta.annotation.Nullable;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.apache.logging.log4j.Logger;
import ua.com.fielden.platform.basic.config.IApplicationDomainProvider;
import ua.com.fielden.platform.domaintree.impl.DomainTreeEnhancer;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.query.QueryProcessingModel;
import ua.com.fielden.platform.eql.meta.query.AbstractQuerySourceItem;
import ua.com.fielden.platform.eql.retrieval.EqlQueryTransformer;
import ua.com.fielden.platform.eql.stage2.operands.Prop2;
import ua.com.fielden.platform.meta.IDomainMetadata;
import ua.com.fielden.platform.security.IAuthorisationModel;
import ua.com.fielden.platform.security.provider.ISecurityTokenProvider;
import ua.com.fielden.platform.security.user.IUserProvider;
import ua.com.fielden.platform.utils.CollectionUtil;
import ua.com.fielden.platform.web_api.IWebApi.Enhancement;
import ua.com.fielden.platform.web_api.IWebApi.PropertyDefinition;
import ua.com.fielden.platform.web_api.exceptions.WebApiException;

import javax.lang.model.SourceVersion;
import java.util.*;
import java.util.regex.Pattern;

import static org.apache.logging.log4j.LogManager.getLogger;
import static ua.com.fielden.platform.domaintree.ICalculatedProperty.CalculatedPropertyAttribute.NO_ATTR;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;
import static ua.com.fielden.platform.eql.retrieval.EqlQueryTransformer.Stage.stage2;
import static ua.com.fielden.platform.reflection.Finder.isPropertyPresent;
import static ua.com.fielden.platform.web_api.FieldVisibility.isModelReadable;
import static ua.com.fielden.platform.web_api.FieldVisibility.visibilityPredicate;

/// Defines enhanced entity types, as specified by [IWebApi#enhanceEntityType].
///
/// This class decides what an enhanced entity type becomes; it holds no state, so storing the result is up to the caller.
///
@Singleton
public class EntityTypeEnhancer {

    private static final Logger LOGGER = getLogger(EntityTypeEnhancer.class);

    public static final String
            ERR_INVALID_NAME = "[%s] is not a valid name. A name must be a Java identifier other than a keyword, consisting of letters, digits and underscores, and must not start with a digit or with two underscores.",
            ERR_BASE_TYPE_REQUIRED = "Entity type [%s] does not exist, so a base type is required to create it.",
            ERR_BASE_TYPE_OF_EXISTING_TYPE = "Entity type [%s] exists, so a base type cannot be specified.",
            ERR_NAME_TAKEN_BY_TYPE = "Name [%s] cannot be used, as type [%s] exists.",
            ERR_NAME_TAKEN_BY_ROOT_FIELD = "Name [%s] cannot be used, as root field [%s] exists.",
            ERR_NO_SUCH_BASE_TYPE = "[%s] cannot be a base type. A base type must be a queryable domain entity type.",
            ERR_DUPLICATE_PROPERTY = "Property [%s] is specified more than once.",
            ERR_ADDED_PROPERTY_OF_BASE_TYPE = "Property [%s] cannot be added, as base type [%s] has it.",
            ERR_ADDED_PROPERTY_EXISTS = "Property [%s] cannot be added, as entity type [%s] has it. Remove it first.",
            ERR_REMOVED_PROPERTY_OF_BASE_TYPE = "Property [%s] cannot be removed, as it belongs to base type [%s].",
            ERR_PROPERTY_ADDED_AND_REMOVED = "Property [%s] cannot be both added and removed.",
            ERR_UNAUTHORISED_ACCESS = "The expression of property [%s] refers to properties that cannot be read: [%s].";

    public static final String
            WARN_NO_SUCH_PROPERTIES_TO_REMOVE = "Entity type [%s] has no properties [%s], so they were not removed.";

    private static final Pattern GRAPHQL_NAME = Pattern.compile("[_A-Za-z][_0-9A-Za-z]*");

    private final IApplicationDomainProvider appDomainProvider;
    private final GraphQLCommon common;
    private final EntityFactory entityFactory;
    private final EqlQueryTransformer eqlTransformer;
    private final IDomainMetadata domainMetadata;
    private final IAuthorisationModel authorisationModel;
    private final ISecurityTokenProvider securityTokenProvider;
    private final IUserProvider userProvider;

    @Inject
    protected EntityTypeEnhancer(
            final IApplicationDomainProvider appDomainProvider,
            final GraphQLCommon common,
            final EntityFactory entityFactory,
            final EqlQueryTransformer eqlTransformer,
            final IDomainMetadata domainMetadata,
            final IAuthorisationModel authorisationModel,
            final ISecurityTokenProvider securityTokenProvider,
            final IUserProvider userProvider)
    {
        this.appDomainProvider = appDomainProvider;
        this.common = common;
        this.entityFactory = entityFactory;
        this.eqlTransformer = eqlTransformer;
        this.domainMetadata = domainMetadata;
        this.authorisationModel = authorisationModel;
        this.securityTokenProvider = securityTokenProvider;
        this.userProvider = userProvider;
    }

    /// Defines enhanced entity type `name` for the current user, as specified by [IWebApi#enhanceEntityType].
    ///
    /// @param schema         the current user's schema
    /// @param enhancedTypes  the current user's enhanced entity types by name
    /// @throws WebApiException  if the request is rejected
    ///
    public Enhancement enhance(
            final GraphQLSchema schema,
            final Map<String, EnhancedEntityType> enhancedTypes,
            final String name,
            final @Nullable String baseType,
            final List<PropertyDefinition> addProperties,
            final List<String> removeProperties)
    {
        validateName(name);
        final var existingType = enhancedTypes.get(name);
        final Class<? extends AbstractEntity<?>> baseEntityType;
        if (existingType == null) {
            if (baseType == null) {
                throw new WebApiException(ERR_BASE_TYPE_REQUIRED.formatted(name));
            }
            baseEntityType = findBaseType(schema, baseType);
            validateNameIsFree(schema, new WebApiEntityType(name, baseEntityType));
        }
        else if (baseType != null) {
            throw new WebApiException(ERR_BASE_TYPE_OF_EXISTING_TYPE.formatted(name));
        }
        else {
            baseEntityType = existingType.baseType();
        }

        final Map<String, String> existingProperties = existingType == null ? Map.of() : existingType.properties();
        final var addedNames = new HashSet<String>();
        for (final var property : addProperties) {
            validateName(property.name());
            if (!addedNames.add(property.name())) {
                throw new WebApiException(ERR_DUPLICATE_PROPERTY.formatted(property.name()));
            }
            if (isPropertyPresent(baseEntityType, property.name())) {
                throw new WebApiException(ERR_ADDED_PROPERTY_OF_BASE_TYPE.formatted(property.name(), baseEntityType.getSimpleName()));
            }
            if (existingProperties.containsKey(property.name())) {
                throw new WebApiException(ERR_ADDED_PROPERTY_EXISTS.formatted(property.name(), name));
            }
        }
        final var absentRemovedNames = new LinkedHashSet<String>();
        for (final var property : removeProperties) {
            if (isPropertyPresent(baseEntityType, property)) {
                throw new WebApiException(ERR_REMOVED_PROPERTY_OF_BASE_TYPE.formatted(property, baseEntityType.getSimpleName()));
            }
            if (addedNames.contains(property)) {
                throw new WebApiException(ERR_PROPERTY_ADDED_AND_REMOVED.formatted(property));
            }
            if (!existingProperties.containsKey(property)) {
                absentRemovedNames.add(property);
            }
        }

        final var properties = new LinkedHashMap<>(existingProperties);
        removeProperties.forEach(properties::remove);
        addProperties.forEach(property -> properties.put(property.name(), property.expression()));
        // TODO Detect cases when a removed property remains referenced.
        //      Currently, this fails with a generic error that does not indicate the underlying cause.

        final var enhancedType = generate(name, baseEntityType, properties);

        // The type has been generated, but this method may still fail, which will result in the generated type being retained indefinitely.
        // TODO Clear the type in case of failure (this requires changes around DynamicEntityClassLoader).

        for (final var property : addProperties) {
            final var unauthorisedPaths = findUnauthorisedPaths(enhancedType.type(), property.name());
            if (!unauthorisedPaths.isEmpty()) {
                throw new WebApiException(ERR_UNAUTHORISED_ACCESS.formatted(property.name(), CollectionUtil.toString(unauthorisedPaths, ", ")));
            }
        }

        final List<String> warnings = absentRemovedNames.isEmpty()
                ? List.of()
                : List.of(WARN_NO_SUCH_PROPERTIES_TO_REMOVE.formatted(name, CollectionUtil.toString(absentRemovedNames, ", ")));
        return new Enhancement(enhancedType, warnings);
    }

    /// Generates an enhanced entity type.
    ///
    /// Properties are added in iteration order of `properties`, so that the expression of each one may refer to those added before it.
    ///
    /// @param properties  expressions of calculated properties by property name
    /// @throws WebApiException  if a property cannot be added
    ///
    private EnhancedEntityType generate(
            final String name,
            final Class<? extends AbstractEntity<?>> baseType,
            final Map<String, String> properties)
    {
        final var enhancer = new DomainTreeEnhancer(entityFactory, Set.of(baseType));
        properties.forEach((property, expression) -> {
            try {
                enhancer.addCalculatedProperty(baseType, "", property, expression, property, expression, NO_ATTR, null, null, null);
                // Apply must be called after adding each property so that it is visible to subsequent additions.
                enhancer.apply();
            } catch (final RuntimeException ex) {
                throw new WebApiException("Property [%s] cannot be added.".formatted(property), ex);
            }
        });
        return new EnhancedEntityType(name, baseType, properties, (Class<? extends AbstractEntity<?>>) enhancer.getManagedType(baseType));
    }

    private static void validateName(final @Nullable String name) {
        if (name == null
            || !SourceVersion.isIdentifier(name)
            || SourceVersion.isKeyword(name)
            || !GRAPHQL_NAME.matcher(name).matches()
            || name.startsWith("__"))
        {
            throw new WebApiException(ERR_INVALID_NAME.formatted(name));
        }
    }

    /// Ensures that none of the GraphQL definitions for a new entity type would clash with an existing one.
    /// These definitions are named after the name of `entityType` alone.
    ///
    private void validateNameIsFree(final GraphQLSchema schema, final WebApiEntityType entityType) {
        for (final var typeName : List.of(entityType.name(),
                                          common.graphQlTypeNameForAggEntity(entityType),
                                          common.graphQlTypeNameForCondEntity(entityType),
                                          common.graphQlTypeNameForCondEntityCond(entityType))) {
            if (schema.getType(typeName) != null) {
                throw new WebApiException(ERR_NAME_TAKEN_BY_TYPE.formatted(entityType.name(), typeName));
            }
        }
        for (final var rootFieldName : List.of(common.rootFieldName(entityType), common.rootFieldNameForAggEntity(entityType))) {
            if (schema.getQueryType().getFieldDefinition(rootFieldName) != null) {
                throw new WebApiException(ERR_NAME_TAKEN_BY_ROOT_FIELD.formatted(entityType.name(), rootFieldName));
            }
        }
    }

    /// Finds a domain type named `name` that can be a base type for the current user.
    ///
    private Class<? extends AbstractEntity<?>> findBaseType(final GraphQLSchema schema, final String name) {
        return common.streamQueryableTypes(appDomainProvider)
                .filter(type -> type.getSimpleName().equals(name))
                .filter(type -> common.containsRootFieldForType(schema, WebApiEntityType.domainType(type)))
                .filter(type -> isModelReadable(type, authorisationModel, securityTokenProvider))
                .findFirst()
                .orElseThrow(() -> new WebApiException(ERR_NO_SUCH_BASE_TYPE.formatted(name)));
    }

    /// Finds the paths in the expression of calculated `property` of `entityType` that the current user cannot read, in alphabetical order.
    ///
    /// The expression is checked as transformed into EQL stage 2, where calculated properties that it refers to are not yet expanded.
    /// A domain calculated property is guarded by its own authorisation rules, and a calculated property of an enhanced entity type
    /// was checked when it was added.
    ///
    private List<String> findUnauthorisedPaths(final Class<? extends AbstractEntity<?>> entityType, final String property) {
        final var expressionModel = domainMetadata.forProperty(entityType, property).asCalculated()
                .orElseThrow(() -> new WebApiException("Property [%s.%s] must be calculated.".formatted(entityType.getTypeName(), property)))
                .data().expressionModel();
        final var query = select(entityType).yield().expr(expressionModel).as("expr").modelAsAggregate();
        final var qpm = new QueryProcessingModel<>(query, null, null, Map.of(), true);
        return eqlTransformer.transformToStage(qpm, userProvider.getUsername(), stage2)
                .collectProps().stream()
                .map(this::findUnauthorisedPrefix)
                .flatMap(Optional::stream)
                .map(path -> CollectionUtil.toString(path, it -> it.name, "."))
                .distinct()
                .sorted()
                .toList();
    }

    /// Finds the shortest prefix of the path of `prop` whose last element the current user cannot read.
    ///
    /// Each element is checked by the rule of [FieldVisibility], as a property of its owner type.
    /// If the check fails with an exception, the whole path is assumed to be unreadable.
    ///
    @SuppressWarnings("unchecked")
    private Optional<List<AbstractQuerySourceItem<?>>> findUnauthorisedPrefix(final Prop2 prop) {
        final var path = prop.getPath();
        try {
            Class<?> ownerType = prop.source.sourceType();
            for (int i = 0; i < path.size(); i++) {
                final var item = path.get(i);
                final var entityType = (Class<? extends AbstractEntity<?>>) ownerType;
                if (!visibilityPredicate(entityType, authorisationModel, securityTokenProvider).test(item.name)) {
                    return Optional.of(path.subList(0, i + 1));
                }
                ownerType = item.javaType();
            }
            return Optional.empty();
        } catch (final Exception ex) {
            LOGGER.warn(() -> "Suppressed error while checking authorisation for [%s.%s]. Assuming unauthorised.".formatted(
                                prop.source.sourceType().getTypeName(), prop.propPath),
                        ex);
            return Optional.of(path);
        }
    }

}
