package ua.com.fielden.platform.web_api;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import graphql.schema.GraphQLSchema;
import jakarta.annotation.Nullable;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.apache.logging.log4j.Logger;
import ua.com.fielden.platform.basic.config.IApplicationDomainProvider;
import ua.com.fielden.platform.domaintree.impl.DomainTreeEnhancer;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.exceptions.InvalidArgumentException;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.entity.query.QueryProcessingModel;
import ua.com.fielden.platform.eql.meta.query.AbstractQuerySourceItem;
import ua.com.fielden.platform.eql.retrieval.EqlQueryTransformer;
import ua.com.fielden.platform.eql.stage2.operands.Prop2;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.expression.EgTokenCategory;
import ua.com.fielden.platform.expression.ExpressionLexer;
import ua.com.fielden.platform.expression.automata.SequenceRecognitionFailed;
import ua.com.fielden.platform.meta.IDomainMetadata;
import ua.com.fielden.platform.security.IAuthorisationModel;
import ua.com.fielden.platform.security.provider.ISecurityTokenProvider;
import ua.com.fielden.platform.security.user.IUserProvider;
import ua.com.fielden.platform.utils.CollectionUtil;
import ua.com.fielden.platform.utils.ImmutableMapUtils;
import ua.com.fielden.platform.web_api.IWebApi.Enhancement;
import ua.com.fielden.platform.web_api.IWebApi.PropertyDefinition;
import ua.com.fielden.platform.web_api.exceptions.WebApiException;
import ua.com.fielden.platform.web_api.validators.EnhancedEntityTypeNameValidator;
import ua.com.fielden.platform.web_api.validators.UserDefinedCalculatedPropertyNameValidator;

import javax.lang.model.SourceVersion;
import java.util.*;
import java.util.regex.Pattern;

import static java.lang.String.format;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;
import static org.apache.commons.lang3.StringUtils.substringBefore;
import static org.apache.logging.log4j.LogManager.getLogger;
import static ua.com.fielden.platform.domaintree.ICalculatedProperty.CalculatedPropertyAttribute.NO_ATTR;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;
import static ua.com.fielden.platform.eql.retrieval.EqlQueryTransformer.Stage.stage2;
import static ua.com.fielden.platform.reflection.Finder.isPropertyPresent;
import static ua.com.fielden.platform.web_api.FieldVisibility.isModelReadable;
import static ua.com.fielden.platform.web_api.FieldVisibility.visibilityPredicate;
import static ua.com.fielden.platform.web_api.validators.EnhancedEntityTypeBaseTypeValidator.ERR_NO_SUCH_DOMAIN_TYPE;

/// Defines enhanced entity types, as specified by [IWebApi#enhanceEntityType], and instantiates them as Java classes.
///
/// An enhanced entity type is stored as a [UserDefinedEnhancedEntityType] record, with a [UserDefinedCalculatedProperty] record for each of its properties.
/// This class holds no state, so holding instantiated enhanced entity types is up to the caller.
///
@Singleton
public class EntityTypeEnhancer implements IEntityTypeEnhancement {

    private static final Logger LOGGER = getLogger(EntityTypeEnhancer.class);

    public static final String
            ERR_INVALID_NAME = "[%s] is not a valid name. A name must be a Java identifier other than a keyword, consisting of letters, digits and underscores, and must not start with a digit or with two underscores.",
            ERR_BASE_TYPE_REQUIRED = "Entity type [%s] does not exist, so a base type is required to create it.",
            ERR_BASE_TYPE_OF_EXISTING_TYPE = "Entity type [%s] exists, so a base type cannot be specified.",
            ERR_NAME_TAKEN_BY_TYPE = "Name [%s] cannot be used, as type [%s] exists.",
            ERR_NAME_TAKEN_BY_ROOT_FIELD = "Name [%s] cannot be used, as root field [%s] exists.",
            ERR_NO_SUCH_BASE_TYPE = "[%s] cannot be a base type. A base type must be a queryable domain entity type.",
            ERR_DUPLICATE_PROPERTY = "Property [%s] is specified more than once.",
            ERR_ADDED_PROPERTY_EXISTS = "Property [%s] cannot be added, as entity type [%s] has it. Remove it first.",
            ERR_REMOVED_PROPERTY_OF_BASE_TYPE = "Property [%s] cannot be removed, as it belongs to base type [%s].",
            ERR_PROPERTY_ADDED_AND_REMOVED = "Property [%s] cannot be both added and removed.",
            ERR_REMOVED_PROPERTY_IS_REFERENCED = "Property [%s] cannot be removed, as the expressions of properties [%s] refer to it.",
            ERR_PROPERTY_CANNOT_BE_ADDED = "Property [%s] cannot be added.",
            ERR_UNAUTHORISED_ACCESS = "The expression of property [%s] refers to properties that cannot be read: [%s].",
            ERR_ENTITY_TYPE_OF_ANOTHER_USER = "Entity type [%s] belongs to user [%s], so it cannot be instantiated for user [%s].",
            ERR_ENHANCEMENT_FAILED = "Entity type [%s] could not be enhanced by adding properties [%s] and removing properties [%s].";

    public static final String
            WARN_NO_SUCH_PROPERTIES_TO_REMOVE = "Entity type [%s] has no properties [%s], so they were not removed.";

    private static final Pattern GRAPHQL_NAME = Pattern.compile("[_A-Za-z][_0-9A-Za-z]*");

    private final IApplicationDomainProvider appDomainProvider;
    private final GraphQLCommon common;
    private final EntityFactory entityFactory;
    private final ICompanionObjectFinder coFinder;
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
            final ICompanionObjectFinder coFinder,
            final EqlQueryTransformer eqlTransformer,
            final IDomainMetadata domainMetadata,
            final IAuthorisationModel authorisationModel,
            final ISecurityTokenProvider securityTokenProvider,
            final IUserProvider userProvider)
    {
        this.appDomainProvider = appDomainProvider;
        this.common = common;
        this.entityFactory = entityFactory;
        this.coFinder = coFinder;
        this.eqlTransformer = eqlTransformer;
        this.domainMetadata = domainMetadata;
        this.authorisationModel = authorisationModel;
        this.securityTokenProvider = securityTokenProvider;
        this.userProvider = userProvider;
    }

    /// Defines enhanced entity type `name` for the current user, as specified by [IWebApi#enhanceEntityType], and stores it.
    ///
    /// This method must run in a transaction, so that the records it changes are restored if it, or its caller, fails.
    ///
    /// @param schema  the current user's schema
    /// @throws WebApiException  if the request is rejected or fails for any other reason, such as a record that cannot be saved
    ///
    public Enhancement enhance(
            final GraphQLSchema schema,
            final String name,
            final @Nullable String baseType,
            final List<PropertyDefinition> addProperties,
            final List<String> removeProperties)
    {
        try {
            final var user = userProvider.getUser();
            if (user == null) {
                throw new WebApiException("Current user is missing.");
            }

            validateName(name);

            final UserDefinedEnhancedEntityTypeCo co$EntityType = coFinder.find(UserDefinedEnhancedEntityType.class);
            final UserDefinedCalculatedPropertyCo co$Property = coFinder.find(UserDefinedCalculatedProperty.class);
            final var existingType = co$EntityType.findByKeyAndFetchOptional(UserDefinedEnhancedEntityTypeCo.FETCH_PROVIDER.fetchModel(), name, user);
            final Class<? extends AbstractEntity<?>> baseEntityType;
            if (existingType.isEmpty()) {
                if (baseType == null) {
                    throw new WebApiException(ERR_BASE_TYPE_REQUIRED.formatted(name));
                }
                baseEntityType = findBaseType(schema, baseType);
                validateNameIsFree(schema, new WebApiEntityType(name, baseEntityType, baseEntityType));
            }
            else if (baseType != null) {
                throw new WebApiException(ERR_BASE_TYPE_OF_EXISTING_TYPE.formatted(name));
            }
            else {
                baseEntityType = findBaseType(existingType.get());
            }

            final Map<String, UserDefinedCalculatedProperty> existingProperties = existingType.map(co$Property::findByEntityType).orElse(List.of())
                    .stream().collect(toMap(UserDefinedCalculatedProperty::getName, identity()));
            final var addedNames = new HashSet<String>();
            for (final var property : addProperties) {
                validateName(property.name());
                if (!addedNames.add(property.name())) {
                    throw new WebApiException(ERR_DUPLICATE_PROPERTY.formatted(property.name()));
                }
                UserDefinedCalculatedPropertyNameValidator.validate(property.name(), baseEntityType).ifFailure(Result::throwRuntime);
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

            // (property, expression)
            // Properties that will be present in the enhanced entity type after this operation.
            final var expressions = ImmutableMapUtils.unionLeft(
                    Maps.transformValues(Maps.filterKeys(existingProperties, p -> !removeProperties.contains(p)), UserDefinedCalculatedProperty::getExpression),
                    addProperties.stream().collect(toMap(PropertyDefinition::name, PropertyDefinition::expression)));

            removeProperties.stream().filter(existingProperties::containsKey).forEach(property -> {
                final var referringProperties = expressions.entrySet().stream()
                        .filter(entry -> referencedProperties(entry.getValue()).contains(property))
                        .map(Map.Entry::getKey)
                        .sorted()
                        .toList();
                if (!referringProperties.isEmpty()) {
                    throw new WebApiException(ERR_REMOVED_PROPERTY_IS_REFERENCED.formatted(property, CollectionUtil.toString(referringProperties, ", ")));
                }
            });

            final var entityType = existingType
                    .orElseGet(() -> co$EntityType.save(co$EntityType.new_()
                                                                .setName(name)
                                                                .setUser(user)
                                                                .setBaseType(baseEntityType.getName()),
                                                        Optional.of(UserDefinedEnhancedEntityTypeCo.FETCH_PROVIDER.fetchModel()))
                                                  .asRight().value());
            final var removedIds = removeProperties.stream().map(existingProperties::get).filter(Objects::nonNull).map(AbstractEntity::getId).toList();
            if (!removedIds.isEmpty()) {
                co$Property.batchDelete(removedIds);
            }
            for (final var property : addProperties) {
                co$Property.save(co$Property.new_()
                                         .setEntityType(entityType)
                                         .setName(property.name())
                                         .setExpression(property.expression()),
                                 Optional.empty());
            }

            final var enhancedType = javaTypeFor(entityType);

            // The type has been generated, but the request may still fail, which will result in the generated type being retained indefinitely.
            // TODO Clear the type in case of failure (this requires changes around DynamicEntityClassLoader).

            final List<String> warnings = absentRemovedNames.isEmpty()
                    ? List.of()
                    : List.of(WARN_NO_SUCH_PROPERTIES_TO_REMOVE.formatted(name, CollectionUtil.toString(absentRemovedNames, ", ")));
            return new Enhancement(enhancedType, warnings);
        } catch (final Exception ex) {
            final var msg = format(ERR_ENHANCEMENT_FAILED,
                                   name,
                                   CollectionUtil.toString(addProperties, PropertyDefinition::name, ", "),
                                   CollectionUtil.toString(removeProperties, ", "));
            throw switch (ex) {
                case WebApiException it -> it;
                // TODO Develop a comprehensive approach of reporting the reasons of failures to the client.
                //      Only the message of a validation result is reported, as it is meant for users, while the messages of other exceptions may reveal server-side information.
                case Result it -> new WebApiException("%s %s".formatted(msg, it.getMessage()), it);
                default -> new WebApiException(msg, ex);
            };
        }
    }

    @Override
    public EnhancedEntityType javaTypeFor(final UserDefinedEnhancedEntityType instance) {
        if (!instance.isPersisted()) {
            throw new InvalidArgumentException("[instance] must be persisted.");
        }

        final var user = userProvider.getUser();
        if (user == null || !user.getId().equals(instance.getUser().getId())) {
            throw new WebApiException(ERR_ENTITY_TYPE_OF_ANOTHER_USER.formatted(instance.getName(), instance.getUser(), user));
        }
        EnhancedEntityTypeNameValidator.validate(instance.getName(), appDomainProvider).ifFailure(Result::throwRuntime);
        final var baseType = findBaseType(instance);
        final UserDefinedCalculatedPropertyCo coProperty = coFinder.find(UserDefinedCalculatedProperty.class, true);
        final var properties = coProperty.findByEntityType(instance);
        properties.forEach(property -> UserDefinedCalculatedPropertyNameValidator.validate(property.getName(), baseType).ifFailure(Result::throwRuntime));

        final var enhancedType = generate(instance.getName(),
                                          baseType,
                                          properties.stream().collect(toMap(UserDefinedCalculatedProperty::getName, UserDefinedCalculatedProperty::getExpression)));
        for (final var property : enhancedType.properties().keySet()) {
            // findUnathorisedPaths requires the entity type to exist, so it is called after generating the type.
            // TODO Consider if findUnathorisedPaths can be changed so that it can be called before generating the type.
            //      Perhaps reuse the implementation of orderByReferences.
            final var unauthorisedPaths = findUnauthorisedPaths(enhancedType.type(), property);
            if (!unauthorisedPaths.isEmpty()) {
                throw new WebApiException(ERR_UNAUTHORISED_ACCESS.formatted(property, CollectionUtil.toString(unauthorisedPaths, ", ")));
            }
        }
        return enhancedType;
    }

    /// Generates an enhanced entity type, or uses `baseType` if `properties` is empty.
    ///
    /// Properties are added in the order of [#orderByReferences], so that the properties that an expression refers to are added before it.
    ///
    /// @param name  public name of the enhanced entity type, unrelated to the system name of the generated Java class
    /// @param properties  expressions of calculated properties by property name
    /// @throws WebApiException  if a property cannot be added
    ///
    private EnhancedEntityType generate(
            final String name,
            final Class<? extends AbstractEntity<?>> baseType,
            final Map<String, String> properties)
    {
        if (properties.isEmpty()) {
            return new EnhancedEntityType(name, baseType, ImmutableMap.of(), baseType);
        }
        final var enhancer = new DomainTreeEnhancer(entityFactory, Set.of(baseType));
        for (final var prop : orderByReferences(properties)) {
            final var expr = properties.get(prop);
            try {
                enhancer.addCalculatedProperty(baseType, "", prop, expr, prop, expr, NO_ATTR, null, null, null);
                // Apply must be called after adding each property so that it is visible to subsequent additions.
                enhancer.apply();
            } catch (final RuntimeException ex) {
                throw new WebApiException(ERR_PROPERTY_CANNOT_BE_ADDED.formatted(prop), ex);
            }
        }
        return new EnhancedEntityType(name, baseType, properties, (Class<? extends AbstractEntity<?>>) enhancer.getManagedType(baseType));
    }

    /// Orders properties by name, except that each property follows the properties that its expression refers to.
    ///
    /// If every remaining property refers to another remaining one, which can happen only if references form a cycle,
    /// the first remaining property is taken, and it fails to be added.
    ///
    /// @param expressions  expressions of properties by property name
    ///
    private static List<String> orderByReferences(final Map<String, String> expressions) {
        final var references = new HashMap<String, Set<String>>();
        expressions.forEach((property, expression) -> {
            final var referenced = new HashSet<>(referencedProperties(expression));
            referenced.retainAll(expressions.keySet());
            referenced.remove(property);
            references.put(property, referenced);
        });
        final var ordered = new ArrayList<String>();
        final var remaining = new TreeSet<>(expressions.keySet());
        while (!remaining.isEmpty()) {
            final var next = remaining.stream()
                    .filter(property -> ordered.containsAll(references.get(property)))
                    .findFirst()
                    .orElseGet(remaining::first);
            ordered.add(next);
            remaining.remove(next);
        }
        return ordered;
    }

    /// Returns the names of properties that `expression` refers to, which are the first elements of the property paths in it.
    ///
    /// An expression that cannot be tokenised refers to no properties, and it fails to be added.
    ///
    private static Set<String> referencedProperties(final String expression) {
        try {
            return Arrays.stream(new ExpressionLexer(expression).tokenize())
                    .filter(token -> token.category == EgTokenCategory.NAME)
                    .map(token -> substringBefore(token.text, "."))
                    .collect(toSet());
        } catch (final SequenceRecognitionFailed ex) {
            return Set.of();
        }
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

    /// Finds the base type of a stored enhanced entity type.
    ///
    private Class<? extends AbstractEntity<?>> findBaseType(final UserDefinedEnhancedEntityType entityType) {
        final String name = entityType.getBaseType();
        return appDomainProvider.entityTypes().stream().filter(type -> type.getName().equals(name)).findFirst()
                .orElseThrow(() -> new WebApiException(ERR_NO_SUCH_DOMAIN_TYPE.formatted(entityType.getBaseType())));
    }

    /// Finds the paths in the expression of calculated `property` of `entityType` that the current user cannot read, in alphabetical order.
    ///
    /// The expression is checked as transformed into EQL stage 2, where calculated properties that it refers to are not yet expanded.
    /// A domain calculated property is guarded by its own authorisation rules, and each calculated property of an enhanced entity type
    /// is checked on its own.
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
