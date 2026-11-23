package ua.com.fielden.platform.web_api;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSortedSet;
import graphql.schema.GraphQLSchema;
import jakarta.annotation.Nullable;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.apache.logging.log4j.Logger;
import ua.com.fielden.platform.basic.config.IApplicationDomainProvider;
import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.dao.session.WithTransaction;
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
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.types.tuples.T2;
import ua.com.fielden.platform.utils.CollectionUtil;
import ua.com.fielden.platform.utils.EntityUtils;
import ua.com.fielden.platform.web_api.IWebApi.Enhancement;
import ua.com.fielden.platform.web_api.IWebApi.PropertyDefinition;
import ua.com.fielden.platform.web_api.exceptions.WebApiException;

import javax.lang.model.SourceVersion;
import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static com.google.common.collect.ImmutableSortedMap.toImmutableSortedMap;
import static com.google.common.collect.ImmutableSortedSet.toImmutableSortedSet;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;
import static org.apache.commons.lang3.StringUtils.substringBefore;
import static org.apache.logging.log4j.LogManager.getLogger;
import static ua.com.fielden.platform.domaintree.ICalculatedProperty.CalculatedPropertyAttribute.NO_ATTR;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;
import static ua.com.fielden.platform.eql.retrieval.EqlQueryTransformer.Stage.stage2;
import static ua.com.fielden.platform.error.Result.failuref;
import static ua.com.fielden.platform.error.Result.successful;
import static ua.com.fielden.platform.reflection.Finder.streamProperties;
import static ua.com.fielden.platform.types.tuples.T2.t2;
import static ua.com.fielden.platform.web_api.FieldVisibility.visibilityPredicate;
import static ua.com.fielden.platform.web_api.validators.EnhancedEntityTypeBaseTypeValidator.ERR_NO_SUCH_BASE_TYPE;
import static ua.com.fielden.platform.web_api.validators.UserDefinedCalculatedPropertyNameValidator.ERR_INVALID_NAME;

/// Defines enhanced entity types, as specified by [IWebApi#enhanceEntityType], and instantiates them as Java classes.
///
/// An enhanced entity type is stored as a [UserDefinedEnhancedEntityType] record, with a [UserDefinedCalculatedProperty] record for each of its properties.
/// This class holds no state, so holding instantiated enhanced entity types is up to the caller.
///
@Singleton
public class EntityTypeEnhancer extends WithTransaction implements IEntityTypeEnhancement {

    private static final Logger LOGGER = getLogger(EntityTypeEnhancer.class);

    private static final Pattern GRAPHQL_NAME = Pattern.compile("[_A-Za-z][_0-9A-Za-z]*");

    public static final String
            ERR_BASE_TYPE_REQUIRED = "Entity type [%s] does not exist, so a base type is required to create it.",
            ERR_BASE_TYPE_OF_EXISTING_TYPE = "Entity type [%s] exists, so a base type cannot be specified.",
            ERR_NAME_TAKEN_BY_TYPE = "Name [%s] cannot be used, as type [%s] exists.",
            ERR_NAME_TAKEN_BY_ROOT_FIELD = "Name [%s] cannot be used, as root field [%s] exists.",
            ERR_DUPLICATE_PROPERTY = "Property [%s] is specified more than once.",
            ERR_ADDED_PROPERTY_EXISTS = "Property [%s] cannot be added, as entity type [%s] has it. Remove it first.",
            ERR_REMOVED_PROPERTY_OF_BASE_TYPE = "Property [%s] cannot be removed, as it belongs to base type [%s].",
            ERR_PROPERTY_ADDED_AND_REMOVED = "Property [%s] cannot be both added and removed.",
            ERR_REMOVED_PROPERTY_IS_REFERENCED = "Property [%s] cannot be removed, as the expressions of properties [%s] refer to it.",
            ERR_PROPERTY_CANNOT_BE_ADDED = "Property [%s] cannot be added.",
            ERR_UNAUTHORISED_ACCESS = "The expression of property [%s] refers to properties that cannot be read: [%s].",
            ERR_ENTITY_TYPE_OF_ANOTHER_USER = "Entity type [%s] belongs to user [%s], so it cannot be instantiated for user [%s].",
            ERR_INVALID_ENTITY_TYPE = "Enhanced entity type [%s] is invalid. %s",
            ERR_INVALID_PROPERTY = "Property [%s] of enhanced entity type [%s] is invalid. %s",
            ERR_ENHANCEMENT_FAILED = "Entity type [%s] could not be enhanced by adding properties [%s] and removing properties [%s].",
            ERR_ENHANCED_ENTITY_TYPE_NAME_MUST_NOT_BE_EMPTY = "Enhanced entity type name must not be empty.";

    public static final String
            WARN_NO_SUCH_PROPERTIES_TO_REMOVE = "Entity type [%s] has no properties [%s], so they were not removed.";

    public static String errEnhancementFailed(final String name, final String addedProperties, final String removedProperties, final String reason) {
        return ERR_ENHANCEMENT_FAILED.formatted(name, addedProperties, removedProperties) + "\n" + reason;
    }

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
    /// An exception thrown by this method should be reported to the API user only if it is an instance of [WebApiException].
    /// Otherwise, the exception should not be reported to avoid information disclosure.
    ///
    /// @param schema  the schema that will contain the enhanced type
    /// @param name  name of the enhanced entity type, case-insensitive
    /// @param baseType  name of the base type, case-sensitive
    /// @param addProperties  properties to add to the enhanced entity type, names are case-insensitive
    /// @param removeProperties  properties to remove from the enhanced entity type, names are case-insensitive
    ///
    @SessionRequired
    public Enhancement enhance(
            final GraphQLSchema schema,
            final String name,
            final @Nullable String baseType,
            final List<PropertyDefinition> addProperties,
            final List<String> removeProperties)
    {
        return enhance_(schema, name, baseType, addProperties, ImmutableSortedSet.copyOf(String.CASE_INSENSITIVE_ORDER, removeProperties));
    }

    private Enhancement enhance_(
            final GraphQLSchema schema,
            final String name,
            final String baseType,
            final List<PropertyDefinition> addProperties,
            final Set<String> removeProperties)
    {
        if (StringUtils.isEmpty(name)) {
            throw new InvalidArgumentException(ERR_ENHANCED_ENTITY_TYPE_NAME_MUST_NOT_BE_EMPTY);
        }

        try {
            final var user = userProvider.getUser();
            if (user == null) {
                throw new WebApiException("Current user is missing.");
            }

            final UserDefinedEnhancedEntityTypeCo co$EntityType = coFinder.find(UserDefinedEnhancedEntityType.class);
            final UserDefinedCalculatedPropertyCo co$Property = coFinder.find(UserDefinedCalculatedProperty.class);
            final var existingType = co$EntityType.findByKeyAndFetchOptional(UserDefinedEnhancedEntityTypeCo.FETCH_PROVIDER.fetchModel(), name, user);
            // Messages name an existing type as stored, which may differ in case from `name`.
            final var typeName = existingType.map(UserDefinedEnhancedEntityType::getName).orElse(name);
            final Class<? extends AbstractEntity<?>> baseEntityType;
            if (existingType.isEmpty()) {
                if (baseType == null) {
                    throw new WebApiException(ERR_BASE_TYPE_REQUIRED.formatted(name));
                }
                baseEntityType = getDomainType(baseType);
                validateNameIsFree(schema, new WebApiEntityType(name, baseEntityType, baseEntityType));
            }
            else if (baseType != null) {
                throw new WebApiException(ERR_BASE_TYPE_OF_EXISTING_TYPE.formatted(typeName));
            }
            else {
                baseEntityType = co$EntityType.baseTypeAsJavaType(existingType.get());
            }

            firstDuplicate(addProperties, PropertyDefinition::name, String.CASE_INSENSITIVE_ORDER).ifPresent(prop -> {
                throw new WebApiException(ERR_DUPLICATE_PROPERTY.formatted(prop.name()));
            });

            // (case-insensitive name, property)
            final Map<String, UserDefinedCalculatedProperty> existingProperties = existingType.map(co$Property::findByEntityType).orElse(List.of())
                    .stream().collect(toImmutableSortedMap(String.CASE_INSENSITIVE_ORDER, UserDefinedCalculatedProperty::getName, identity()));

            addProperties.stream().map(prop -> existingProperties.get(prop.name())).filter(Objects::nonNull).findFirst().ifPresent(prop -> {
                throw new WebApiException(ERR_ADDED_PROPERTY_EXISTS.formatted(prop.getName(), typeName));
            });

            removeProperties.stream().filter(rmP -> addProperties.stream().anyMatch(addP -> addP.name().equalsIgnoreCase(rmP))).findFirst().ifPresent(rmP -> {
                throw new WebApiException(ERR_PROPERTY_ADDED_AND_REMOVED.formatted(rmP));
            });

            final var baseEntityTypePropNames = streamProperties(baseEntityType).map(Field::getName).collect(toImmutableSortedSet(String.CASE_INSENSITIVE_ORDER));
            removeProperties.stream()
                    .filter(baseEntityTypePropNames::contains)
                    .findFirst().ifPresent(prop -> {
                        throw new WebApiException(ERR_REMOVED_PROPERTY_OF_BASE_TYPE.formatted(prop, baseEntityType.getSimpleName()));
                    });

            final var absentRemovedNames = removeProperties.stream().filter(prop -> !existingProperties.containsKey(prop)).toList();

            // (property, {referencedProperties})
            // Includes all properties that will be present in the enhanced entity type after this operation.
            final var references = Stream.concat(existingProperties.entrySet().stream()
                                                         .filter(entry -> !removeProperties.contains(entry.getKey()))
                                                         .map(entry -> t2(entry.getValue().getName(), entry.getValue().getExpression())),
                                                 addProperties.stream().map(prop -> t2(prop.name(), prop.expression())))
                    .map(t2-> t2.map2(EntityTypeEnhancer::referencedProperties))
                    .toList();

            removeProperties.stream().map(existingProperties::get).filter(Objects::nonNull).map(UserDefinedCalculatedProperty::getName).forEach(rmProp -> {
                final var referringProperties = references.stream()
                        .filter(t2 -> t2._2.contains(rmProp))
                        .map(T2::_1)
                        .sorted()
                        .toList();
                if (!referringProperties.isEmpty()) {
                    throw new WebApiException(ERR_REMOVED_PROPERTY_IS_REFERENCED.formatted(rmProp, CollectionUtil.toString(referringProperties, ", ")));
                }
            });

            final var entityType = existingType
                    .orElseGet(() -> {
                        final var instance = co$EntityType.new_().setName(name).setUser(user).setBaseType(baseEntityType.getCanonicalName());
                        instance.isValid().ifFailure(ex -> {
                            throw new WebApiException(ERR_INVALID_ENTITY_TYPE.formatted(name, ex.getMessage()), ex);
                        });
                        return co$EntityType.save(instance, Optional.of(UserDefinedEnhancedEntityTypeCo.FETCH_PROVIDER.fetchModel())).asRight().value();
                    });
            final var removedIds = removeProperties.stream().map(existingProperties::get).filter(Objects::nonNull).map(AbstractEntity::getId).toList();
            if (!removedIds.isEmpty()) {
                co$Property.batchDelete(removedIds);
            }
            for (final var property : addProperties) {
                final var instance = co$Property.new_().setEntityType(entityType).setName(property.name()).setExpression(property.expression());
                instance.isValid().ifFailure(ex -> {
                    throw new WebApiException(ERR_INVALID_PROPERTY.formatted(property.name(), typeName, ex.getMessage()), ex);
                });
                co$Property.save(instance, Optional.empty());
            }

            final var enhancedType = javaTypeFor(entityType);

            // The type has been generated, but the request may still fail, which will result in the generated type being retained indefinitely.
            // TODO Clear the type in case of failure (this requires changes around DynamicEntityClassLoader).

            final List<String> warnings = absentRemovedNames.isEmpty()
                    ? List.of()
                    : List.of(WARN_NO_SUCH_PROPERTIES_TO_REMOVE.formatted(typeName, CollectionUtil.toString(absentRemovedNames, ", ")));
            return new Enhancement(enhancedType, warnings);
        } catch (final Exception ex) {
            final var addedNames = CollectionUtil.toString(addProperties, PropertyDefinition::name, ", ");
            final var removedNames = CollectionUtil.toString(removeProperties, ", ");
            final var msg = ERR_ENHANCEMENT_FAILED.formatted(name, addedNames, removedNames);
            LOGGER.error(msg, ex);
            throw switch (ex) {
                case WebApiException it -> new WebApiException(errEnhancementFailed(name, addedNames, removedNames, it.getMessage()), it);
                default -> new WebApiException(msg, ex);
            };
        }
    }

    private <T, K> Optional<T> firstDuplicate(final Iterable<T> xs, final Function<T, K> getKey, final Comparator<K> comparator) {
        final var keys = new TreeSet<>(comparator);
        for (final var x : xs) {
            final var key = getKey.apply(x);
            if (keys.contains(key)) {
                return Optional.of(x);
            }
            keys.add(key);
        }
        return Optional.empty();
    }

    /// An exception thrown by this method should be reported to the API user only if it is an instance of [WebApiException].
    /// Otherwise, the exception should not be reported to avoid information disclosure.
    ///
    @Override
    public EnhancedEntityType javaTypeFor(final UserDefinedEnhancedEntityType instance) {
        if (!instance.isPersisted()) {
            throw new InvalidArgumentException("[instance] must be persisted.");
        }

        final var user = userProvider.getUser();
        if (user == null || !user.getId().equals(instance.getUser().getId())) {
            throw new WebApiException(ERR_ENTITY_TYPE_OF_ANOTHER_USER.formatted(instance.getName(), instance.getUser(), user));
        }

        instrumentedCopy(instance).isValid().ifFailure(ex -> {
            throw new WebApiException(ERR_INVALID_ENTITY_TYPE.formatted(instance.getName(), ex.getMessage()), ex);
        });

        final UserDefinedEnhancedEntityTypeCo coUserDefinedEnhancedEntityType = coFinder.find(UserDefinedEnhancedEntityType.class, true);
        final var baseType = coUserDefinedEnhancedEntityType.baseTypeAsJavaType(instance);

        final UserDefinedCalculatedPropertyCo coProperty = coFinder.find(UserDefinedCalculatedProperty.class, true);
        final var properties = coProperty.findByEntityType(instance);
        properties.forEach(property -> instrumentedCopy(property).isValid().ifFailure(ex -> {
            throw new WebApiException(ERR_INVALID_PROPERTY.formatted(property.getName(), instance.getName(), ex.getMessage()), ex);
        }));

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

    @Override
    public Result validateName(final CharSequence name) {
        if (!SourceVersion.isIdentifier(name)
            || SourceVersion.isKeyword(name)
            || !GRAPHQL_NAME.matcher(name).matches()
            || Strings.CS.startsWith(name, "__"))
        {
            return failuref(ERR_INVALID_NAME, name);
        }
        else {
            return successful();
        }
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

    /// Returns a domain type whose simple name is `name`.
    ///
    private Class<? extends AbstractEntity<?>> getDomainType(final String name) {
        return appDomainProvider.entityTypes().stream().filter(ty -> name.equals(ty.getSimpleName())).findFirst()
                .orElseThrow(() -> new WebApiException(ERR_NO_SUCH_BASE_TYPE.formatted(name)));
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

    private <E extends AbstractEntity<?>> E instrumentedCopy(final E instance) {
        final var co$ = (IEntityDao<E>) coFinder.find(instance.getType());
        return EntityUtils.copy(instance, co$.new_());
    }

    @Override
    public User getUser() {
        return userProvider.getUser();
    }

}
