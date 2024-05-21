package ua.com.fielden.platform.meta;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Injector;
import org.hibernate.type.*;
import org.hibernate.type.spi.TypeConfiguration;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.AbstractUnionEntity;
import ua.com.fielden.platform.entity.DynamicEntityKey;
import ua.com.fielden.platform.entity.annotation.*;
import ua.com.fielden.platform.entity.exceptions.EntityDefinitionException;
import ua.com.fielden.platform.entity.query.DbVersion;
import ua.com.fielden.platform.entity.query.exceptions.EqlException;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.entity.query.model.ExpressionModel;
import ua.com.fielden.platform.eql.exceptions.EqlMetadataGenerationException;
import ua.com.fielden.platform.eql.meta.PropColumn;
import ua.com.fielden.platform.reflection.exceptions.ReflectionException;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.Optional;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static java.lang.String.format;
import static java.util.Collections.unmodifiableList;
import static java.util.Objects.requireNonNull;
import static java.util.Objects.requireNonNullElseGet;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static ua.com.fielden.platform.domaintree.ICalculatedProperty.CalculatedPropertyCategory.AGGREGATED_EXPRESSION;
import static ua.com.fielden.platform.entity.AbstractEntity.*;
import static ua.com.fielden.platform.entity.AbstractUnionEntity.commonProperties;
import static ua.com.fielden.platform.entity.AbstractUnionEntity.unionProperties;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.expr;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;
import static ua.com.fielden.platform.entity.query.metadata.CompositeKeyEqlExpressionGenerator.generateCompositeKeyEqlExpression;
import static ua.com.fielden.platform.eql.meta.DomainMetadataUtils.extractExpressionModelFromCalculatedProperty;
import static ua.com.fielden.platform.meta.PropertyMetadataImpl.Builder.*;
import static ua.com.fielden.platform.meta.PropertyTypeMetadata.COMPOSITE_KEY;
import static ua.com.fielden.platform.reflection.AnnotationReflector.*;
import static ua.com.fielden.platform.reflection.Finder.*;
import static ua.com.fielden.platform.reflection.PropertyTypeDeterminator.isRequiredByDefinition;
import static ua.com.fielden.platform.utils.EntityUtils.*;

/* General verification rules for entities:
 * - Synthetic based on Persistent - can't have an entity-typed key.
 */

final class DomainMetadataGenerator {

    private static final org.hibernate.type.Type H_ENTITY = LongType.INSTANCE;
    private static final org.hibernate.type.Type H_LONG = LongType.INSTANCE;
    private static final org.hibernate.type.Type H_STRING = StringType.INSTANCE;
    private static final org.hibernate.type.Type H_BOOLEAN = YesNoType.INSTANCE;

    // NOTE This came from old code and it's unclear whether this is the right way of obtaining Hibernate types.
    private static final TypeConfiguration typeConfiguration = new TypeConfiguration();
    private static final TypeResolver typeResolver = new TypeResolver(typeConfiguration, new TypeFactory(typeConfiguration));

    private static final Set<String> SPECIAL_PROPS = Set.of(ID, KEY, VERSION);

    /** Class-to-instance map for Hibernate types. */
    private final Map<Class<?>, Object> hibTypesDefaults;
    private final Injector hibTypesInjector;

    private final PropertyTypeMetadataGenerator propTypeMetadataGenerator = new PropertyTypeMetadataGenerator();
    private final Map<String, PropColumn> specialPropColumns;

    // TODO make this injectable
    DomainMetadataGenerator(final Injector hibTypesInjector, final Map<? extends Class, ? extends Class> hibTypesDefaults,
                            final DbVersion dbVersion) {
        // some columns are DB-dependent
        this.specialPropColumns = Map.of(
                ID, new PropColumn(dbVersion.idColumnName()),
                VERSION, new PropColumn(dbVersion.versionColumnName()));

        this.hibTypesInjector = hibTypesInjector;
        if (hibTypesDefaults != null) {
            final var map = new HashMap<Class<?>, Object>();
            hibTypesDefaults.forEach((javaType, hibType) -> {
                try {
                    map.put(javaType, hibType.getDeclaredField("INSTANCE").get(null));
                } catch (final Exception e) {
                    throw new EqlMetadataGenerationException("Couldn't instantiate Hibernate type [" + hibType + "].", e);
                }
            });
            // TODO old code, definitely a kludge, this class should not be responsible for establishing any mappings
            map.put(Boolean.class, H_BOOLEAN);
            map.put(boolean.class, H_BOOLEAN);
            this.hibTypesDefaults = Collections.unmodifiableMap(map);
        } else {
            this.hibTypesDefaults = ImmutableMap.of();
        }
    }

    public EntityMetadata forEntity(final Class<? extends AbstractEntity<?>> entityType) {
        final EntityMetadataBuilder<?, ?> entityBuilder;
        if (isUnionEntityType(entityType)) {
            final var unionEntityType = (Class<? extends AbstractUnionEntity>) entityType;
            entityBuilder = EntityMetadataBuilder.unionEntity(
                    unionEntityType, EntityNature.Union.data(produceUnionEntityModels(unionEntityType)));
        } else if (isPersistedEntityType(entityType)) {
            entityBuilder = EntityMetadataBuilder.persistentEntity(
                    entityType, EntityNature.Persistent.data(mkTableName(entityType)));
        } else if (isSyntheticEntityType(entityType)) {
            final var modelField = requireNonNull(findSyntheticModelFieldFor(entityType),
                                                  () -> "Synthetic entity [%s] has no model field.".formatted(entityType.getTypeName()));
            entityBuilder = EntityMetadataBuilder.syntheticEntity(
                    entityType, EntityNature.Synthetic.data(getEntityModelsOfQueryBasedEntityType(entityType, modelField)));
        } else {
            entityBuilder = EntityMetadataBuilder.otherEntity(entityType);
        }

        return entityBuilder.properties(buildProperties(entityBuilder)).build();
    }

    public Optional<TypeMetadata.Composite> forComposite(final Class<?> type) {
        // TODO
        throw new UnsupportedOperationException();
    }

    /**
     * Builds metadata for properties of a given entity.
     */
    private Iterable<? extends PropertyMetadata> buildProperties(final EntityMetadataBuilder<?, ?> entityBuilder) {
        // DO NOT MODIFY THE GIVEN BUILDER
        switch (entityBuilder) {
            case EntityMetadataBuilder.Union u -> {
                return ImmutableList.<PropertyMetadata>builder()
                        .addAll(generateUnionImplicitCalcSubprops(u.getJavaType()))
                        // union members
                        .addAll(unionProperties(u.getJavaType()).stream().map(field -> mkProp(field, u)).flatMap(Optional::stream)
                                        .iterator())
                        .build();
            }
            default -> {
                final var props = ImmutableList.<PropertyMetadata>builder();
                mkPropId(entityBuilder).ifPresent(props::add);
                mkPropKey(entityBuilder).ifPresent(props::add);
                mkPropVersion(entityBuilder).ifPresent(props::add);
                streamRealProperties(entityBuilder.getJavaType())
                        .filter(field -> !SPECIAL_PROPS.contains(field.getName()))
                        .map(field -> mkProp(field, entityBuilder))
                        .flatMap(Optional::stream)
                        .forEach(props::add);
                return props.build();
            }
        }
    }

    private Optional<PropertyMetadata> mkPropVersion(final EntityMetadataBuilder<?, ?> entityBuilder) {
        return switch (entityBuilder) {
            // NOTE old code also did NOT use isSyntheticBasedOnPersistentEntityType, which traverses the whole type hierarchy
            case EntityMetadataBuilder.Synthetic s when isPersistedEntityType(s.getJavaType().getSuperclass()) ->
                    Optional.of(persistentProp(VERSION, mkPropertyTypeOrThrow(Long.class), H_LONG,
                                               PropertyNature.Persistent.data(propColumn(VERSION)))
                                        .required(true).build());
            case EntityMetadataBuilder.Persistent $ ->
                    Optional.of(persistentProp(VERSION, mkPropertyTypeOrThrow(Long.class), H_LONG,
                                               PropertyNature.Persistent.data(propColumn(VERSION)))
                                        .required(true).build());
            default -> Optional.empty();
        };
    }

    private Optional<PropertyMetadata> mkPropKey(final EntityMetadataBuilder<?, ?> entityBuilder) {
        final Class<? extends Comparable<?>> keyType = getKeyType(entityBuilder.getJavaType());
        if (keyType == null) {
            throw new EntityDefinitionException("Property [key] not found in entity [%s]".formatted(entityBuilder.getJavaType().getTypeName()));
        }

        if (isOneToOne(entityBuilder.getJavaType())) {
            return switch (entityBuilder) {
                case EntityMetadataBuilder.Persistent $ ->
                        Optional.of(persistentProp(KEY, mkPropertyTypeOrThrow(keyType), H_ENTITY, PropertyNature.Persistent.data(propColumn(ID)))
                                            .required(true).build());
                case EntityMetadataBuilder.Synthetic $ ->
                        Optional.of(transientProp(KEY, mkPropertyTypeOrThrow(keyType), H_ENTITY).required(true).build());
                default -> Optional.empty();
            };
        } else {
            if (DynamicEntityKey.class.equals(keyType)) {
                final var entityType = (Class<? extends AbstractEntity<DynamicEntityKey>>) entityBuilder.getJavaType();
                return Optional.of(calculatedProp(KEY, COMPOSITE_KEY, H_STRING,
                                                  PropertyNature.Calculated.data(generateCompositeKeyEqlExpression(entityType), true, false))
                                           // TODO why required?
                                           .required(true).build());
            } else {
                final var keyColumn = new PropColumn("KEY_");
                final Object keyHibType = typeResolver.basic(keyType.getName());
                return switch (entityBuilder) {
                    case EntityMetadataBuilder.Persistent $ ->
                            Optional.of(persistentProp(KEY, mkPropertyTypeOrThrow(keyType), keyHibType,
                                                       PropertyNature.Persistent.data(keyColumn))
                                                .required(true).build());
                    case EntityMetadataBuilder.Synthetic s ->
                            isSyntheticBasedOnPersistentEntityType(s.getJavaType())
                                    ? Optional.of(persistentProp(KEY, mkPropertyTypeOrThrow(keyType), keyHibType,
                                                                 PropertyNature.Persistent.data(keyColumn))
                                                          .required(true).build())
                                    : Optional.of(transientProp(KEY, mkPropertyTypeOrThrow(keyType), keyHibType)
                                                          .required(true).build());
                    default -> Optional.empty();
                };
            }
        }
    }

    /* "id" - depends on the enclosing entity's nature:
     * - Persistent - included as persistent.
     * - Synthetic:
     *   - Based on Persistent - included as persistent.
     *   - With entity-typed key - implicitly calculated making it equal to "key".
     *   - Else - included as persistent (something to reconsider).
     * - Else - excluded.
     */
    private Optional<PropertyMetadata> mkPropId(final EntityMetadataBuilder<?, ?> entityBuilder) {
        final PropertyMetadata propId = persistentProp(ID, mkPropertyTypeOrThrow(Long.class), H_ENTITY,
                                                       PropertyNature.Persistent.data(propColumn(ID)))
                .required(true).build();

        return switch (entityBuilder) {
            case EntityMetadataBuilder.Persistent $ -> Optional.of(propId);
            case EntityMetadataBuilder.Synthetic s -> {
                if (isSyntheticBasedOnPersistentEntityType(s.getJavaType())) {
                    if (isEntityType(getKeyType(s.getJavaType()))) {
                        throw new EntityDefinitionException(format("Entity [%s] is recognised as synthetic based on a persistent type with an entity-typed key. This is not supported.",
                                                                   s.getJavaType().getTypeName()));
                    }
                    yield Optional.of(propId);
                } else if (isEntityType(getKeyType(s.getJavaType()))) {
                    yield Optional.of(calculatedProp(ID, mkPropertyTypeOrThrow(Long.class), H_ENTITY,
                                                     PropertyNature.Calculated.data(expr().prop(KEY).model(), true, false))
                                              .build());
                } else {
                    // FIXME reconsider this implementation taking into account its role combined with actual yields
                    // information in the process of getting final EntityPropInfo for Synthetic Entity
                    yield Optional.of(propId);
                }
            }
            default -> Optional.empty();
        };
    }

    /**
     * The most general property metadata generation method.
     * <p>
     * Special cases:
     * <ul>
     *   <li> One-to-one association - implicitly calculated.
     * </ul>
     */
    Optional<PropertyMetadata> mkProp(final Field field, final EntityMetadataBuilder<?, ?> entityBuilder) {
        final IsProperty atIsProperty = getAnnotation(field, IsProperty.class);
        if (atIsProperty == null) {
            return Optional.empty();
        }

        final Class<? extends AbstractEntity<?>> enclosingEntityType = entityBuilder.getJavaType();

        if (isOne2One_association(enclosingEntityType, field.getName())) {
            return mkOne2OneProp(field, entityBuilder);
        }

        final PropertyTypeMetadata propTypeMd = mkPropertyTypeOrThrow(field);
        final Object hibType = getHibernateType(field);
        final MapTo atMapTo = getAnnotation(field, MapTo.class);
        final Calculated atCalculated = getAnnotation(field, Calculated.class);

        final PropertyMetadataImpl.Builder<?, ?> builder;

        // CRIT-ONLY
        if (isAnnotationPresent(field, CritOnly.class)) {
            builder = critOnlyProp(field.getName(), propTypeMd, null);
        }
        // PERSISTENT
        // old code: last 2 conditions are to overcome incorrect metadata combinations
        // TODO throw an exception for incorrect definitions ?
        else if (atMapTo != null && !isSyntheticEntityType(enclosingEntityType) && atCalculated == null) {
            final String columnName = mkColumnName(field.getName(), atMapTo);
            builder = persistentProp(field.getName(), propTypeMd, hibType,
                                     PropertyNature.Persistent.data(propColumn(columnName, atIsProperty)));
        }
        // CALCULATED
        else if (atCalculated != null) {
            final boolean aggregatedExpression = AGGREGATED_EXPRESSION == atCalculated.category();
            final var data = PropertyNature.Calculated.data(
                    extractExpressionModelFromCalculatedProperty(enclosingEntityType, field), false, aggregatedExpression);
            builder = calculatedProp(field.getName(), propTypeMd, hibType, data);
        }
        // TRANSIENT
        else {
            builder = transientProp(field.getName(), propTypeMd, hibType);
        }

        // Scan the property for any additional metadata
        builder.required(isRequiredByDefinition(field, enclosingEntityType));

        if (isAnnotationPresent(field, CompositeKeyMember.class)) {
            builder.with(PropertyMetadataKeys.KEY_MEMBER, true);
        }

        return Optional.of(builder.build());
    }

    private Optional<PropertyMetadata> mkOne2OneProp(final Field field, final EntityMetadataBuilder<?, ?> entityBuilder) {
        // TODO optional metadata Key<Boolean> to indicate that this property is one2one?
        final var propType = (Class<? extends AbstractEntity<?>>) field.getType();
        // one2one is not required to exist -- that's why need longer formula -- that's why one2one is in fact implicitly calculated nullable prop
        final ExpressionModel expressionModel = expr()
                .model(select(propType).where().prop(KEY).eq().extProp(ID).model())
                .model();
        return Optional.of(calculatedProp(field.getName(), mkPropertyTypeOrThrow(field), getHibernateType(field),
                                          PropertyNature.Calculated.data(expressionModel, true, false))
                                   .build());
    }

    /**
     * Prefer {@link #mkPropertyType(Field)} whenever possible.
     */
    public Optional<PropertyTypeMetadata> mkPropertyType(final Type type) {
        final var cacheEntry = primitivePropTypeMetadataCache.get(type);
        if (cacheEntry != null) {
            return Optional.of(cacheEntry);
        }

        final Optional<PropertyTypeMetadata> optPtm = propTypeMetadataGenerator.fromType(type);
        optPtm.ifPresent(ptm -> {
            if (ptm instanceof PropertyTypeMetadata.Primitive p) {
                primitivePropTypeMetadataCache.put(type, p);
            }
        });
        return optPtm;
    }
    // Cache for primitive property types that exists throughout the lifetime of this generator
    private final ConcurrentHashMap<Type, PropertyTypeMetadata.Primitive> primitivePropTypeMetadataCache = new ConcurrentHashMap<>();

    public Optional<PropertyTypeMetadata> mkPropertyType(final Field field) {
        return mkPropertyType(field.getGenericType());
    }

    /**
     * Prefer {@link #mkPropertyType(Field)} whenever possible.
     */
    private PropertyTypeMetadata mkPropertyTypeOrThrow(final Type type) {
        return mkPropertyType(type)
                .orElseThrow(() -> new EqlMetadataGenerationException("Failed to generate metadata for property type [%s]".formatted(type.getTypeName())));
    }

    private PropertyTypeMetadata mkPropertyTypeOrThrow(final Field field) {
        return mkPropertyTypeOrThrow(field.getGenericType());
    }

    // TODO old code; merge with HibernateTypeDeterminer
    /**
     * Determines hibernate type instance (Type/UserType/CustomUserType) for entity property based on provided property's meta information.
     */
    private Object getHibernateType(final Field propField) {
        final String propName = propField.getName();
        final Class<?> propType = propField.getType();

        if (isPersistedEntityType(propType) || isUnionEntityType(propType) || isSyntheticEntityType(propType)) {
            return H_ENTITY;
        }

        final PersistentType persistentType = getAnnotation(propField, PersistentType.class);

        if (persistentType == null) {
            final Object defaultHibType = hibTypesDefaults.get(propType);
            if (defaultHibType != null) { // default is provided for given property java type
                return defaultHibType;
            } else { // trying to mimic hibernate logic when no type has been specified - use hibernate's map of defaults
                final BasicType result = typeResolver.basic(propType.getName());
                if (result == null) {
                    throw new EqlMetadataGenerationException(format("Could not determined Hibernate type for property [%s : %s].",
                                                                    propName, propType.getTypeName()));
                }
                return result;
            }
        } else {
            final String hibernateTypeName = persistentType.value();
            final Class<?> hibernateUserTypeImplementor = persistentType.userType();
            if (isNotEmpty(hibernateTypeName)) {
                final BasicType result = typeResolver.basic(hibernateTypeName);
                if (result == null) {
                    throw new EqlMetadataGenerationException(propName + " of type " + propType.getName() + " has no hibType (2)");
                }
                return result;
            } else if (hibTypesInjector != null && !Void.class.equals(hibernateUserTypeImplementor)) { // Hibernate type is definitely either IUserTypeInstantiate or ICompositeUserTypeInstantiate
                try {
                    return hibTypesInjector.getInstance(hibernateUserTypeImplementor).getClass().getDeclaredField("INSTANCE").get(null); // need to have the same instance in use for the unit tests sake
                } catch (final Exception e) {
                    throw new EqlMetadataGenerationException("Couldn't obtain instance of hibernate type [" + hibernateUserTypeImplementor + "] due to: " + e);
                }
            } else {
                throw new EqlMetadataGenerationException("Persistent annotation doesn't provide intended information.");
            }
        }
    }

    // ****************************************
    // * Persistent Entity

    private static String mkColumnName(final String propName, final MapTo mapTo) {
        return isNotEmpty(mapTo.value()) ? mapTo.value() : propName.toUpperCase() + "_";
    }

    PropColumn propColumn(final String columnName) {
        return requireNonNullElseGet(
                specialPropColumns.getOrDefault(columnName, null),
                () -> new PropColumn(removeObsoleteUnderscore(columnName)));
    }

    PropColumn propColumn(final String columnName, final IsProperty isProperty) {
        return requireNonNullElseGet(
                specialPropColumns.getOrDefault(columnName, null),
                () -> {
                    final var length = isProperty.length() > 0 ? isProperty.length() : null;
                    final var precision = isProperty.precision() >= 0 ? isProperty.precision() : null;
                    final var scale = isProperty.scale() >= 0 ? isProperty.scale() : null;
                    return new PropColumn(removeObsoleteUnderscore(columnName), length, precision, scale);
                });
    }

    private static String removeObsoleteUnderscore(final String name) {
        return name.endsWith("_") && name.substring(0, name.length() - 1).contains("_")
                ? name.substring(0, name.length() - 1)
                : name;
    }

    private static String mkTableName(final Class<? extends AbstractEntity<?>> entityType) {
        try {
            final MapEntityTo mapEntityToAnnotation = getAnnotation(entityType, MapEntityTo.class);
            if (mapEntityToAnnotation == null || isEmpty(mapEntityToAnnotation.value())) {
                return entityType.getSimpleName().toUpperCase() + "_";
            } else {
                return mapEntityToAnnotation.value();
            }
        } catch (final Exception ex) {
            throw new EqlException(format("Could not determine table name for entity [%s].", entityType.getTypeName()), ex);
        }
    }

    // ****************************************
    // * Synthetic Entity

    /**
     * Returns a list of query models defined by a synthetic entity.
     */
    static <T extends AbstractEntity<?>> List<EntityResultQueryModel<T>> getEntityModelsOfQueryBasedEntityType
        (final Class<T> entityType, final Field modelField)
    {
        try {
            final var name = modelField.getName();
            modelField.setAccessible(true);
            final Object value = modelField.get(null);
            if ("model_".equals(name)) {
                return ImmutableList.of((EntityResultQueryModel<T>) value);
            } else {
                // this must be "models_"
                return ImmutableList.copyOf((List<EntityResultQueryModel<T>>) modelField.get(null));
            }
        } catch (final Exception ex) {
            if (ex instanceof ReflectionException) {
                throw (ReflectionException) ex;
            } else {
                throw new ReflectionException("Could not obtain the model for synthetic entity [%s].".formatted(entityType.getSimpleName()), ex);
            }
        }
    }

    // ****************************************
    // * Union Entity

    private static <ET extends AbstractUnionEntity> List<EntityResultQueryModel<ET>> produceUnionEntityModels(final Class<ET> entityType) {
        final List<Field> unionProps = unionProperties(entityType);
        return unionProps.stream()
                .map(currProp -> generateModelForUnionEntityProperty(unionProps, currProp).modelAsEntity(entityType))
                .toList();
    }

    private static EntityQueryProgressiveInterfaces.ISubsequentCompletedAndYielded<?>
    generateModelForUnionEntityProperty(final List<Field> unionProps, final Field currProp) {
        final var startWith = select((Class<? extends AbstractEntity<?>>) currProp.getType());
        final Field firstUnionProp = unionProps.getFirst();
        final var initialModel = firstUnionProp.equals(currProp)
                ? startWith.yield().prop(ID).as(firstUnionProp.getName())
                : startWith.yield().val(null).as(firstUnionProp.getName());
        // TODO just use foldLeft (implement it first)
        return unionProps.stream()
                .skip(1)
                .reduce(initialModel,
                        (m, f) -> f.equals(currProp)
                                ? m.yield().prop(ID).as(f.getName())
                                : m.yield().val(null).as(f.getName()),
                        (m1, m2) -> {throw new UnsupportedOperationException("Combining is not applicable here.");});
    }

    private List<PropertyMetadata> generateUnionImplicitCalcSubprops(final Class<? extends AbstractUnionEntity> unionType,
                                                                     @Nullable final String contextPropName) {
        final List<Field> unionMembers = unionProperties(unionType);
        if (unionMembers.isEmpty()) {
            throw new EntityDefinitionException("Ill-defined union entity [%s] has no union members.".formatted(unionType.getTypeName()));
        }
        final List<String> unionMembersNames = unionMembers.stream().map(Field::getName).toList();
        final List<PropertyMetadata> props = new ArrayList<>();
        props.add(calculatedProp(KEY, mkPropertyTypeOrThrow(String.class), H_STRING,
                                 PropertyNature.Calculated.data(generateUnionEntityPropertyContextualExpression(unionMembersNames, KEY, contextPropName), true, false))
                          .build());
        props.add(calculatedProp(ID, mkPropertyTypeOrThrow(Long.class), H_ENTITY,
                                 PropertyNature.Calculated.data(generateUnionEntityPropertyContextualExpression(unionMembersNames, ID, contextPropName), true, false))
                          .build());
        props.add(calculatedProp(DESC, mkPropertyTypeOrThrow(String.class), H_STRING,
                                 PropertyNature.Calculated.data(generateUnionCommonDescPropExpressionModel(unionMembers, contextPropName), true, false))
                          .build());

        final Class<?> firstUnionEntityPropType = unionMembers.getFirst().getType(); // e.g. WagonSlot in TgBogieLocation
        for (final String commonProp : commonProperties(unionType).stream().filter(n -> !DESC.equals(n) && !KEY.equals(n)).toList()) {
            if (unionMembersNames.contains(commonProp)) {
                throw new EntityDefinitionException(format("""
                                                           Ill-defined union entity: %s
                                                           Common property and union member share the same name [%s].""",
                                                           unionType.getTypeName(), commonProp));
            }
            final Field commonPropField = findFieldByName(firstUnionEntityPropType, commonProp);
            props.add(calculatedProp(commonProp, mkPropertyTypeOrThrow(commonPropField), getHibernateType(commonPropField),
                                     PropertyNature.Calculated.data(generateUnionEntityPropertyContextualExpression(unionMembersNames, commonProp, contextPropName), true, false))
                              .build());
        }

        return unmodifiableList(props);
    }

    private List<PropertyMetadata> generateUnionImplicitCalcSubprops(final Class<? extends AbstractUnionEntity> unionType) {
        return generateUnionImplicitCalcSubprops(unionType, null);
    }

    private ExpressionModel generateUnionCommonDescPropExpressionModel(final List<Field> unionMembers, final String contextPropName) {
        final List<String> unionMembersNames = unionMembers.stream().filter(et -> hasDescProperty((Class<? extends AbstractEntity<?>>) et.getType())).map(et -> et.getName()).collect(toList());
        return generateUnionEntityPropertyContextualExpression(unionMembersNames, DESC, contextPropName);
    }

    private static ExpressionModel generateUnionEntityPropertyContextualExpression(final List<String> unionMembers, final String commonSubpropName, final String contextPropName) {
        if (unionMembers.isEmpty()) {
            return expr().val(null).model();
        }
        final Iterator<String> iterator = unionMembers.iterator();
        final String firstUnionPropName = (contextPropName == null ? "" :  contextPropName + ".") + iterator.next();
        var expressionModelInProgress = expr()
                .caseWhen().prop(firstUnionPropName).isNotNull().then().prop(firstUnionPropName + "." + commonSubpropName);
        for (; iterator.hasNext();) {
            final String unionPropName = (contextPropName == null ? "" :  contextPropName + ".") + iterator.next();
            expressionModelInProgress = expressionModelInProgress.when().prop(unionPropName).isNotNull().then().prop(unionPropName + "." + commonSubpropName);
        }

        return expressionModelInProgress.end().model();
    }

}
