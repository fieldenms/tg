package ua.com.fielden.platform.meta;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.inject.Injector;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.AbstractUnionEntity;
import ua.com.fielden.platform.entity.DynamicEntityKey;
import ua.com.fielden.platform.entity.annotation.Calculated;
import ua.com.fielden.platform.entity.annotation.CritOnly;
import ua.com.fielden.platform.entity.annotation.*;
import ua.com.fielden.platform.entity.exceptions.EntityDefinitionException;
import ua.com.fielden.platform.entity.query.DbVersion;
import ua.com.fielden.platform.entity.query.exceptions.EqlException;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.entity.query.model.ExpressionModel;
import ua.com.fielden.platform.eql.exceptions.EqlMetadataGenerationException;
import ua.com.fielden.platform.eql.meta.PropColumn;
import ua.com.fielden.platform.eql.retrieval.EntityContainerEnhancer;
import ua.com.fielden.platform.reflection.exceptions.ReflectionException;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.Optional;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

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
import static ua.com.fielden.platform.meta.EntityNature.SYNTHETIC;
import static ua.com.fielden.platform.meta.EntityNature.UNION;
import static ua.com.fielden.platform.meta.HibernateTypeGenerator.*;
import static ua.com.fielden.platform.meta.PropertyMetadataImpl.Builder.*;
import static ua.com.fielden.platform.meta.PropertyMetadataKeys.UNION_MEMBER;
import static ua.com.fielden.platform.meta.PropertyNature.*;
import static ua.com.fielden.platform.meta.PropertyTypeMetadata.COMPOSITE_KEY;
import static ua.com.fielden.platform.reflection.AnnotationReflector.*;
import static ua.com.fielden.platform.reflection.Finder.*;
import static ua.com.fielden.platform.reflection.PropertyTypeDeterminator.isRequiredByDefinition;
import static ua.com.fielden.platform.utils.EntityUtils.*;

/* General verification rules for entities:
 * - Synthetic based on Persistent - can't have an entity-typed key.
 */

/**
 * <h3> Property Metadata </h3>
 * Given a Java type that has properties (e.g., entity type or composite type), metadata is optionally generated for each
 * property.
 * <p>
 * For the purpose of metadata generation properties can be divided into <i>special</i> and <i>ordinary</i> groups.
 * <p>
 * Special properties include: "id", "version", "key", one-to-one associations.
 * <p>
 * Whether metadata is generated for a property depends on several factors: the nature of its enclosing type (e.g.,
 * persistent entity), the nature and the type of the property itself.
 *
 * <h4> Property Type Metadata </h4>
 * Metadata is also generated for property types, see {@link PropertyTypeMetadata}.
 * <p>
 * Properties whose type cannot be modelled by metadata are skipped. However, for some properties it is required that
 * their type be modelled, otherwise the property's definition must be incorrect. These include:
 * <ul>
 *   <li> Special properties.
 *   <li> Persistent properties.
 *   <li> Calculated properties.
 *   <li> Crit-only properties.
 * </ul>
 *
 * <h4> Collectional Properties </h4>
 * Although collectional properties are implicitly calculated, the nature of any given collectional property is inferred
 * from its definition (typically it is {@link PropertyNature.Transient}). The calculation part is independently
 * performed by {@link EntityContainerEnhancer}.
 */
final class DomainMetadataGenerator {

    private static final Set<String> SPECIAL_PROPS = Set.of(ID, KEY, VERSION);

    private final PropertyTypeMetadataGenerator propTypeMetadataGenerator = new PropertyTypeMetadataGenerator();
    private final HibernateTypeGenerator hibTypeGenerator;
    private final Map<String, PropColumn> specialPropColumns;

    // TODO make this injectable
    DomainMetadataGenerator(final Injector hibTypesInjector, final Map<? extends Class, ? extends Class> hibTypesDefaults,
                            final DbVersion dbVersion) {
        // some columns are DB-dependent
        this.specialPropColumns = Map.of(
                ID, new PropColumn(dbVersion.idColumnName()),
                VERSION, new PropColumn(dbVersion.versionColumnName()));
        this.hibTypeGenerator = new HibernateTypeGenerator(hibTypesDefaults, hibTypesInjector);
    }

    // ****************************************
    // * Composite Type Metadata

    public Optional<TypeMetadata.Composite> forComposite(final Class<?> type) {
        if (!TypeRegistry.COMPOSITE_TYPES.contains(type)) {
            return Optional.empty();
        }

        final var builder = new CompositeTypeMetadataImpl.Builder(type);
        return Optional.of(builder.properties(buildProperties(builder)).build());
    }

    /**
     * Builds metadata for properties of a given composite type.
     */
    private Iterable<? extends PropertyMetadata> buildProperties(final CompositeTypeMetadataImpl.Builder typeBuilder) {
        // DO NOT MODIFY THE GIVEN BUILDER
        return Arrays.stream(typeBuilder.getJavaType().getDeclaredFields())
                .map(fld -> mkPropForComposite(fld, typeBuilder))
                .flatMap(Optional::stream)
                .toList();
    }

    private Optional<PropertyMetadata> mkPropForComposite(final Field field, final CompositeTypeMetadataImpl.Builder typeBuilder) {
        final IsProperty atIsProperty = getAnnotation(field, IsProperty.class);
        if (atIsProperty == null) {
            return Optional.empty();
        }

        final PropertyTypeMetadata propTypeMd = mkPropertyTypeOrThrow(field);
        final MapTo atMapTo = getAnnotation(field, MapTo.class);
        final PropertyMetadataImpl.Builder<?, ?> builder;

        // PERSISTENT
        if (atMapTo != null) {
            final String columnName = mkColumnName(field.getName(), atMapTo);
            builder = persistentProp(field.getName(), propTypeMd,
                                     hibTypeGenerator.generate(PropertyNature.PERSISTENT, propTypeMd, typeBuilder).use(field).get(),
                                     PropertyNature.Persistent.data(propColumn(columnName, atIsProperty)));
        }
        // TRANSIENT
        else {
            builder = transientProp(field.getName(), propTypeMd,
                                    hibTypeGenerator.generate(PropertyNature.TRANSIENT, propTypeMd, typeBuilder).use(field).get());
        }

        return Optional.of(builder.build());
    }

    // ****************************************
    // * Entity Metadata

    public Optional<EntityMetadata> forEntity(final Class<? extends AbstractEntity<?>> entityType) {
        final Optional<EntityMetadataBuilder<?, ?>> entityBuilder;

        switch (inferEntityNature(entityType)) {
            case EntityNature.Union $ -> {
                final var unionEntityType = (Class<? extends AbstractUnionEntity>) entityType;
                entityBuilder = Optional.of(EntityMetadataBuilder.unionEntity(
                        unionEntityType, EntityNature.Union.data(produceUnionEntityModels(unionEntityType))));
            }
            case EntityNature.Persistent $ ->
                entityBuilder = Optional.of(EntityMetadataBuilder.persistentEntity(
                        entityType, EntityNature.Persistent.data(mkTableName(entityType))));
            case EntityNature.Synthetic $ -> {
                final var modelField = requireNonNull(findSyntheticModelFieldFor(entityType),
                                                      () -> "Synthetic entity [%s] has no model field.".formatted(
                                                              entityType.getTypeName()));
                entityBuilder = Optional.of(EntityMetadataBuilder.syntheticEntity(
                        entityType,
                        EntityNature.Synthetic.data(getEntityModelsOfQueryBasedEntityType(entityType, modelField))));
            }
            case EntityNature.Other $ -> entityBuilder = Optional.empty();

        }

        return entityBuilder.map(b -> b.properties(buildProperties(b)).build());
    }

    /**
     * Builds metadata for properties of a given entity.
     */
    private Iterable<? extends PropertyMetadata> buildProperties(final EntityMetadataBuilder<?, ?> entityBuilder) {
        // DO NOT MODIFY THE GIVEN BUILDER
        switch (entityBuilder) {
            case EntityMetadataBuilder.Union u -> {
                return ImmutableList.<PropertyMetadata>builder()
                        .addAll(generateUnionImplicitCalcSubprops(u.getJavaType(), entityBuilder))
                        // union members
                        .addAll(unionProperties(u.getJavaType()).stream()
                                        .map(field -> mkProp(field, u)).flatMap(Optional::stream)
                                        .map(bld -> bld.with(UNION_MEMBER, true))
                                        .map(PropertyMetadataImpl.Builder::build)
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
                        .map(PropertyMetadataImpl.Builder::build)
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
                final PropertyTypeMetadata propTypeMd = mkPropertyTypeOrThrow(keyType);
                final Function<PropertyNature, Object> getHibType =
                        propNature -> hibTypeGenerator.generate(propNature, propTypeMd, entityBuilder).get();
                return switch (entityBuilder) {
                    case EntityMetadataBuilder.Persistent $ ->
                            Optional.of(persistentProp(KEY, propTypeMd, getHibType.apply(PERSISTENT),
                                                       PropertyNature.Persistent.data(keyColumn))
                                                .required(true).build());
                    case EntityMetadataBuilder.Synthetic s ->
                            isSyntheticBasedOnPersistentEntityType(s.getJavaType())
                                    ? Optional.of(persistentProp(KEY, propTypeMd, getHibType.apply(PERSISTENT),
                                                                 PropertyNature.Persistent.data(keyColumn))
                                                          .required(true).build())
                                    : Optional.of(transientProp(KEY, propTypeMd, getHibType.apply(TRANSIENT))
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
    Optional<PropertyMetadataImpl.Builder<?, ?>> mkProp(final Field field, final EntityMetadataBuilder<?, ?> entityBuilder) {
        final IsProperty atIsProperty = getAnnotation(field, IsProperty.class);
        if (atIsProperty == null) {
            return Optional.empty();
        }

        final Class<? extends AbstractEntity<?>> enclosingEntityType = entityBuilder.getJavaType();

        if (isOne2One_association(enclosingEntityType, field.getName())) {
            return mkOne2OneProp(field, entityBuilder);
        }

        final MapTo atMapTo = getAnnotation(field, MapTo.class);
        final Calculated atCalculated = getAnnotation(field, Calculated.class);

        final Optional<PropertyMetadataImpl.Builder<?, ?>> builder;

        // CRIT-ONLY
        if (isAnnotationPresent(field, CritOnly.class)) {
            builder = Optional.of(critOnlyProp(field.getName(), mkPropertyTypeOrThrow(field), null));
        }
        // PERSISTENT
        // old code: last 2 conditions are to overcome incorrect metadata combinations
        // TODO throw an exception for incorrect definitions ?
        else if (atMapTo != null && !isSyntheticEntityType(enclosingEntityType) && atCalculated == null) {
            final String columnName = mkColumnName(field.getName(), atMapTo);
            final var propTypeMd = mkPropertyTypeOrThrow(field);
            builder = Optional.of(
                    persistentProp(field.getName(), propTypeMd,
                                   hibTypeGenerator.generate(PropertyNature.PERSISTENT, propTypeMd, entityBuilder).use(field).get(),
                                   PropertyNature.Persistent.data(propColumn(columnName, atIsProperty))));
        }
        // CALCULATED
        else if (atCalculated != null) {
            final boolean aggregatedExpression = AGGREGATED_EXPRESSION == atCalculated.category();
            final var data = PropertyNature.Calculated.data(
                    extractExpressionModelFromCalculatedProperty(enclosingEntityType, field), false, aggregatedExpression);
            final var propTypeMd = mkPropertyTypeOrThrow(field);
            builder = Optional.of(calculatedProp(field.getName(), propTypeMd,
                                                 hibTypeGenerator.generate(CALCULATED, propTypeMd, entityBuilder).use(field).get(),
                                                 data));
        }
        // TRANSIENT
        else {
            // skip properties that have an unknown type
            builder = mkPropertyType(field)
                    .map(propTypeMd -> transientProp(field.getName(), propTypeMd,
                                                     hibTypeGenerator.generate(TRANSIENT, propTypeMd, entityBuilder).use(field).get()));
        }

        return builder
                // Scan the property for any additional metadata
                .map(bld -> bld.required(isRequiredByDefinition(field, enclosingEntityType)))
                .map(bld -> isAnnotationPresent(field, CompositeKeyMember.class)
                        ? bld.with(PropertyMetadataKeys.KEY_MEMBER, true)
                        : bld);
    }

    private Optional<PropertyMetadataImpl.Builder<?, ?>> mkOne2OneProp(final Field field, final EntityMetadataBuilder<?, ?> entityBuilder) {
        // TODO optional metadata Key<Boolean> to indicate that this property is one2one?
        final var propType = (Class<? extends AbstractEntity<?>>) field.getType();
        // one2one is not required to exist -- that's why need longer formula -- that's why one2one is in fact implicitly calculated nullable prop
        final ExpressionModel expressionModel = expr()
                .model(select(propType).where().prop(KEY).eq().extProp(ID).model())
                .model();
        final PropertyTypeMetadata typeMetadata = mkPropertyTypeOrThrow(field);
        return Optional.of(calculatedProp(field.getName(), typeMetadata,
                                          hibTypeGenerator.generate(CALCULATED, typeMetadata, entityBuilder).use(field).get(),
                                          PropertyNature.Calculated.data(expressionModel, true, false)));
    }

    public Optional<PropertyTypeMetadata> mkPropertyType(final Field field) {
        final var primitivePropTypeMetadataCacheKey = field.getGenericType();
        final var cacheEntry = primitivePropTypeMetadataCache.get(primitivePropTypeMetadataCacheKey);
        if (cacheEntry != null) {
            return Optional.of(cacheEntry);
        }

        final Optional<PropertyTypeMetadata> optPtm = propTypeMetadataGenerator.generate(field);
        optPtm.ifPresent(ptm -> {
            if (ptm instanceof PropertyTypeMetadata.Primitive p) {
                primitivePropTypeMetadataCache.put(primitivePropTypeMetadataCacheKey, p);
            }
        });
        return optPtm;
    }
    // Cache for primitive property types that exists throughout the lifetime of this generator
    private final ConcurrentHashMap<Type, PropertyTypeMetadata.Primitive> primitivePropTypeMetadataCache = new ConcurrentHashMap<>();

    public Optional<PropertyTypeMetadata> mkPropertyType(final Type type) {
        final var cacheEntry = primitivePropTypeMetadataCache.get(type);
        if (cacheEntry != null) {
            return Optional.of(cacheEntry);
        }

        final Optional<PropertyTypeMetadata> optPtm = propTypeMetadataGenerator.generate(type);
        optPtm.ifPresent(ptm -> {
            if (ptm instanceof PropertyTypeMetadata.Primitive p) {
                primitivePropTypeMetadataCache.put(type, p);
            }
        });
        return optPtm;
    }

    private PropertyTypeMetadata mkPropertyTypeOrThrow(final Field field) {
        return mkPropertyType(field)
                .orElseThrow(() -> new EqlMetadataGenerationException(
                        "Failed to generate metadata for type of property [%s]".formatted(field.toGenericString())));
    }

    private PropertyTypeMetadata mkPropertyTypeOrThrow(final Type type) {
        return mkPropertyType(type)
                .orElseThrow(() -> new EqlMetadataGenerationException(
                        "Failed to generate metadata for property type [%s]".formatted(type.getTypeName())));
    }

    // ****************************************
    // * Persistent Entity

    private static String mkColumnName(final String propName, final MapTo mapTo) {
        return isNotEmpty(mapTo.value()) ? mapTo.value() : propName.toUpperCase() + "_";
    }

    PropColumn propColumn(final String columnName, final Optional<IsProperty> optIsProperty) {
        return optIsProperty
                .map(isProperty -> propColumn(columnName, isProperty))
                .orElseGet(() -> propColumn(columnName));
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
                                                                     @Nullable final String contextPropName,
                                                                     final EntityMetadataBuilder<?, ?> entityBuilder) {
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
            final PropertyTypeMetadata typeMetadata = mkPropertyTypeOrThrow(commonPropField);
            props.add(calculatedProp(commonProp, typeMetadata,
                                     hibTypeGenerator.generate(CALCULATED, typeMetadata, entityBuilder).use(commonPropField).get(),
                                     PropertyNature.Calculated.data(generateUnionEntityPropertyContextualExpression(unionMembersNames, commonProp, contextPropName), true, false))
                              .build());
        }

        return unmodifiableList(props);
    }

    private List<PropertyMetadata> generateUnionImplicitCalcSubprops(final Class<? extends AbstractUnionEntity> unionType,
                                                                     final EntityMetadataBuilder<?, ?> entityBuilder) {
        return generateUnionImplicitCalcSubprops(unionType, null, entityBuilder);
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

    // ****************************************
    // * Misc. utilities

    static EntityNature inferEntityNature(final Class<? extends AbstractEntity<?>> entityType) {
        if (isUnionEntityType(entityType)) {
            return UNION;
        }
        else if (isPersistedEntityType(entityType)) {
            return EntityNature.PERSISTENT;
        }
        else if (isSyntheticEntityType(entityType)) {
            return SYNTHETIC;
        }
        else {
            return EntityNature.OTHER;
        }
    }

    static boolean hasNature(final Class<? extends AbstractEntity<?>> entityType, final EntityNature nature) {
        return nature.equals(inferEntityNature(entityType));
    }

    static boolean hasAnyNature(final Class<? extends AbstractEntity<?>> entityType, final Iterable<? extends EntityNature> natures) {
        return Iterables.any(natures, nature -> hasNature(entityType, nature));
    }

}
