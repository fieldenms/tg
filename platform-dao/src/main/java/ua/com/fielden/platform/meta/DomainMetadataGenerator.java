package ua.com.fielden.platform.meta;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import jakarta.annotation.Nullable;
import org.apache.logging.log4j.Logger;
import ua.com.fielden.platform.audit.AuditUtils;
import ua.com.fielden.platform.audit.InactiveAuditProperty;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.AbstractUnionEntity;
import ua.com.fielden.platform.entity.DynamicEntityKey;
import ua.com.fielden.platform.entity.annotation.*;
import ua.com.fielden.platform.entity.exceptions.EntityDefinitionException;
import ua.com.fielden.platform.entity.query.IDbVersionProvider;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.entity.query.model.ExpressionModel;
import ua.com.fielden.platform.eql.meta.PropColumn;
import ua.com.fielden.platform.eql.retrieval.EntityContainerEnhancer;
import ua.com.fielden.platform.expression.ExpressionText2ModelConverter;
import ua.com.fielden.platform.meta.PropertyMetadataKeys.KAuditProperty;
import ua.com.fielden.platform.meta.PropertyMetadataUtils.SubPropertyNaming;
import ua.com.fielden.platform.meta.exceptions.DomainMetadataGenerationException;
import ua.com.fielden.platform.persistence.types.HibernateTypeMappings;
import ua.com.fielden.platform.reflection.asm.impl.DynamicEntityClassLoader;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.time.Duration;
import java.util.*;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Stream;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static java.util.Collections.unmodifiableList;
import static java.util.Objects.requireNonNullElseGet;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static org.apache.logging.log4j.LogManager.getLogger;
import static ua.com.fielden.platform.audit.AuditUtils.isAuditEntityType;
import static ua.com.fielden.platform.audit.AuditUtils.isSynAuditEntityType;
import static ua.com.fielden.platform.domaintree.ICalculatedProperty.CalculatedPropertyCategory.AGGREGATED_EXPRESSION;
import static ua.com.fielden.platform.entity.AbstractEntity.*;
import static ua.com.fielden.platform.entity.AbstractUnionEntity.commonProperties;
import static ua.com.fielden.platform.entity.AbstractUnionEntity.unionProperties;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.expr;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;
import static ua.com.fielden.platform.entity.query.metadata.CompositeKeyEqlExpressionGenerator.generateCompositeKeyEqlExpression;
import static ua.com.fielden.platform.meta.EntityNature.*;
import static ua.com.fielden.platform.meta.PropertyMetadataImpl.Builder.*;
import static ua.com.fielden.platform.meta.PropertyMetadataKeys.KEY_MEMBER;
import static ua.com.fielden.platform.meta.PropertyMetadataKeys.UNION_MEMBER;
import static ua.com.fielden.platform.meta.PropertyTypeMetadata.COMPOSITE_KEY;
import static ua.com.fielden.platform.persistence.HibernateConstants.*;
import static ua.com.fielden.platform.reflection.AnnotationReflector.*;
import static ua.com.fielden.platform.reflection.Finder.*;
import static ua.com.fielden.platform.reflection.PropertyTypeDeterminator.*;
import static ua.com.fielden.platform.utils.EntityUtils.*;
import static ua.com.fielden.platform.utils.StreamUtils.distinct;
import static ua.com.fielden.platform.utils.StreamUtils.foldLeft;

/**
 * <h3> Property Metadata </h3>
 * Given a Java type that has properties (e.g., entity type or component type), metadata is optionally generated for each property.
 * <p>
 * For the purpose of metadata generation properties can be divided into <i>special</i> and <i>ordinary</i> groups.
 * <p>
 * Special properties include: "id", "version", "key", one-to-one associations.
 * <p>
 * Whether metadata is generated for a property depends on several factors: the nature of its enclosing type (e.g., persistent entity),
 * the nature and the type of the property itself.
 *
 * <h4> Property Type Metadata </h4>
 * Metadata is also generated for property types, see {@link PropertyTypeMetadata}.
 * <p>
 * Properties whose type cannot be modelled by metadata are skipped.
 * However, for some properties it is required that their type be modelled, otherwise the property's definition must be incorrect.
 * These include:
 * <ul>
 *   <li> Special properties.
 *   <li> Persistent properties.
 *   <li> Calculated properties.
 *   <li> CritOnly properties.
 * </ul>
 *
 * <h4> Collectional Properties </h4>
 * Although collectional properties are implicitly calculated, the nature of any given collectional property gets inferred from its definition (typically it is {@link PropertyNature.Plain}).
 * The calculation part is performed independently by {@link EntityContainerEnhancer}.
 *
 * <h4>General verification rules for entities</h4>
 * <ul>
 *   <li>Synthetic based on Persistent entities cannot have an entity-typed key.
 *   <li>Union entities cannot be used as keys or composite key members.
 * </ul>
 *
 */
final class DomainMetadataGenerator {

    private static final Logger LOGGER = getLogger(DomainMetadataGenerator.class);

    private static final Set<String> SPECIAL_PROPS = Set.of(ID, KEY, VERSION);

    public static final String ERR_COULD_NOT_LOAD_TYPE = "Could not load type [%s].";
    public static final String ERR_CANNOT_OBTAIN_EXPRESSION_MODEL_FOR_CALCULATED_PROPERTY = "Cannot obtain expression model for calculated property [%s].";
    public static final String ERR_UNION_ENTITY_HAS_NO_UNION_MEMBERS = "Ill-defined union entity [%s] has no union members.";
    public static final String ERR_UNION_ENTITY_PROPERTY_NAME_CONFLICT =
    """
    Ill-defined union entity: %s
    Common property and union property share the same name [%s].\
    """;
    public static final String ERR_TABLE_NAME_NOT_DETERMINED = "Could not determine table name for entity [%s].";
    public static final String ERR_SYNTHETIC_ENTITY_WITH_UNSUPPORTED_KEY_TYPE = "Entity [%s] is recognised as synthetic-based-on-persistent having an entity-typed key. This is not supported.";

    private final PropertyTypeMetadataGenerator propTypeMetadataGenerator = new PropertyTypeMetadataGenerator();
    private final HibernateTypeGenerator hibTypeGenerator;
    private final Map<String, PropColumn> specialPropColumns;

    /** Long-lasting (but not necessarily permanent) cache for entity types. */
    private final Cache<Class<? extends AbstractEntity<?>>, EntityMetadata> entityMetadataCache;
    /** Temporary cache for entity types. */
    private final Cache<Class<? extends AbstractEntity<?>>, EntityMetadata> tmpEntityMetadataCache;
    /** Permanent cache for component types. */
    private final Cache<Class<?>, TypeMetadata.Component> componentTypeMetadataCache;

    DomainMetadataGenerator(final HibernateTypeMappings hibernateTypeMappings, final IDbVersionProvider dbVersionProvider) {
        // some columns are DB-dependent
        this.specialPropColumns = Map.of(
                ID, new PropColumn(dbVersionProvider.dbVersion().idColumnName()),
                VERSION, new PropColumn(dbVersionProvider.dbVersion().versionColumnName()));
        this.hibTypeGenerator = new HibernateTypeGenerator(hibernateTypeMappings);
        this.entityMetadataCache = CacheBuilder.newBuilder()
                .concurrencyLevel(50)
                .maximumSize(8192)
                .expireAfterAccess(Duration.ofDays(1))
                .build();
        this.tmpEntityMetadataCache = CacheBuilder.newBuilder()
                .concurrencyLevel(50)
                .maximumSize(8192)
                .weakKeys()
                .expireAfterAccess(Duration.ofMinutes(5))
                .build();
        this.componentTypeMetadataCache = CacheBuilder.newBuilder()
                .concurrencyLevel(50)
                .maximumSize(128)
                .build();
    }

    ////////////////////////////////////
    ////// Component Type Metadata /////
    ////////////////////////////////////

    public Optional<TypeMetadata.Component> forComponent(final Class<?> type) {
        final var cached = componentTypeMetadataCache.getIfPresent(type);
        if (cached != null) {
            return Optional.of(cached);
        }

        if (!TypeRegistry.COMPONENT_TYPES.contains(type)) {
            return Optional.empty();
        }

        final var builder = new ComponentTypeMetadataImpl.Builder(type);
        final var metadata = builder.properties(buildProperties(builder)).build();
        componentTypeMetadataCache.put(type, metadata);
        return Optional.of(metadata);
    }

    /**
     * Builds metadata for properties of a given component type.
     */
    private Iterable<? extends PropertyMetadata> buildProperties(final ComponentTypeMetadataImpl.Builder typeBuilder) {
        // DO NOT MODIFY THE GIVEN BUILDER
        return Arrays.stream(typeBuilder.getJavaType().getDeclaredFields())
                .map(fld -> mkPropForComponent(fld, typeBuilder))
                .flatMap(Optional::stream)
                .toList();
    }

    private Optional<PropertyMetadata> mkPropForComponent(final Field field, final ComponentTypeMetadataImpl.Builder typeBuilder) {
        final IsProperty atIsProperty = getAnnotation(field, IsProperty.class);
        if (atIsProperty == null) {
            return Optional.empty();
        }

        final PropertyTypeMetadata propTypeMd = mkPropertyTypeOrThrow(field);
        final MapTo atMapTo = getAnnotation(field, MapTo.class);
        final PropertyMetadataImpl.Builder<?, ?> builder;

        // PERSISTENT
        if (atMapTo != null) {
            final String columnName = propColumnName(field.getDeclaringClass(), field.getName());
            builder = persistentProp(field.getName(), propTypeMd,
                                     hibTypeGenerator.generate(propTypeMd).use(field).get(),
                                     PropertyNature.Persistent.data(propColumn(columnName, atIsProperty)));
        }
        // TRANSIENT
        else {
            builder = plainProp(field.getName(), propTypeMd,
                                hibTypeGenerator.generate(propTypeMd).use(field).getOpt().orElse(null));
        }

        return Optional.of(builder.build());
    }

    ////////////////////////////////////
    /////// Entity Type Metadata ///////
    ////////////////////////////////////

    public Optional<EntityMetadata> forEntity(final Class<? extends AbstractEntity<?>> entityType) {
        final var cached = getCachedEntityMetadata(entityType);
        if (cached != null) {
            return Optional.of(cached);
        }

        final EntityMetadata metadata;
        try {
            metadata = forEntity_(entityType);
        } catch (final Exception ex) {
            // rethrow to facilitate debugging
            throw new DomainMetadataGenerationException("Failed to generate metadata for entity [%s].".formatted(entityType), ex);
        }

        if (metadata == null) {
            return Optional.empty();
        }

        entityCacheFor(entityType).put(entityType, metadata);
        return Optional.of(metadata);
    }

    private EntityMetadata requireForEntity(final Class<? extends AbstractEntity<?>> entityType) {
        return forEntity(entityType).orElseThrow(() -> new DomainMetadataGenerationException(
                "Could not generate metadata for entity [%s].".formatted(entityType.getTypeName())));
    }

    private @Nullable EntityMetadata getCachedEntityMetadata(final Class<? extends AbstractEntity<?>> entityType) {
        final var cached = entityMetadataCache.getIfPresent(entityType);
        if (cached != null) {
            return cached;
        }
        return tmpEntityMetadataCache.getIfPresent(entityType);
    }

    private Cache<Class<? extends AbstractEntity<?>>, EntityMetadata> entityCacheFor(final Class<? extends AbstractEntity<?>> entityType) {
        if (isProxied(entityType) || isMockNotFoundType(entityType)) {
            return tmpEntityMetadataCache;
        }
        return entityMetadataCache;
    }

    private @Nullable EntityMetadata forEntity_(final Class<? extends AbstractEntity<?>> entityType) {
        final EntityMetadataBuilder<?, ?> entityBuilder;

        // Note: if entityType is a generated entity type with the same nature as its original type, then it is possible to reuse the data from the original type's nature
        switch (inferEntityNature(entityType)) {
            case EntityNature.Union $ -> {
                final var unionEntityType = (Class<? extends AbstractUnionEntity>) entityType;
                entityBuilder = EntityMetadataBuilder.unionEntity(
                        unionEntityType,
                        metadataForParentOfGenerated(entityType).flatMap(EntityMetadata::asUnion).map(EntityMetadata.Union::data)
                                .orElseGet(() -> EntityNature.Union.data(produceUnionEntityModels(unionEntityType))));
            }
            case EntityNature.Persistent $ -> {
                entityBuilder = EntityMetadataBuilder.persistentEntity(
                        entityType,
                        metadataForParentOfGenerated(entityType).flatMap(EntityMetadata::asPersistent).map(EntityMetadata.Persistent::data)
                                .orElseGet(() -> EntityNature.Persistent.data(mkTableName(entityType))));
            }
            case EntityNature.Synthetic $ -> entityBuilder = EntityMetadataBuilder.syntheticEntity(entityType);
            case EntityNature.Other $ -> entityBuilder = null;
        }

        return entityBuilder == null ? null : entityBuilder.properties(buildProperties(entityBuilder)).build();
    }

    /**
     * If {@code entityType} is generated, returns the metadata for its parent type, otherwise returns an empty optional.
     */
    private Optional<EntityMetadata> metadataForParentOfGenerated(final Class<? extends AbstractEntity<?>> entityType) {
        if (isGenerated(entityType)) {
            final var parentEntityType = (Class<? extends AbstractEntity<?>>) entityType.getSuperclass();
            return Optional.of(requireForEntity(parentEntityType));
        }

        return Optional.empty();
    }

    private Iterable<? extends PropertyMetadata> buildProperties(final EntityMetadataBuilder<?, ?> entityBuilder) {
        // if an entity type is generated and has the same nature as its parent entity type, we can reuse parent's properties metadata
        return metadataForParentOfGenerated(entityBuilder.getJavaType())
                .filter(parentEntityMetadata -> parentEntityMetadata.nature().equals(entityBuilder.getNature()))
                .map(parentEntityMetadata -> buildPropertiesForGeneratedEntity(entityBuilder, parentEntityMetadata))
                .orElseGet(() -> buildPropertiesFull(entityBuilder));
    }

    /**
     * Builds metadata for properties of a given entity.
     */
    private Iterable<PropertyMetadata> buildPropertiesFull(final EntityMetadataBuilder<?, ?> entityBuilder) {
        // DO NOT MODIFY THE GIVEN BUILDER
        switch (entityBuilder) {
            case EntityMetadataBuilder.Union u -> {
                return ImmutableList.<PropertyMetadata>builder()
                        .addAll(generateUnionImplicitCalcSubprops(u.getJavaType(), entityBuilder, SubPropertyNaming.SIMPLE))
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

    private Iterable<PropertyMetadata> buildPropertiesForGeneratedEntity(final EntityMetadataBuilder<?, ?> entityBuilder,
                                                                         final EntityMetadata parentEntityMetadata) {
        return distinct(Stream.concat(
                        streamDeclaredProperties(entityBuilder.getJavaType())
                                .filter(field -> !SPECIAL_PROPS.contains(field.getName()))
                                .map(field -> mkProp(field, entityBuilder))
                                .flatMap(Optional::stream)
                                .map(PropertyMetadataImpl.Builder::build),
                        parentEntityMetadata.properties().stream()),
                PropertyMetadata::name)
                .collect(toImmutableList());
    }

    private Optional<PropertyMetadata> mkPropVersion(final EntityMetadataBuilder<?, ?> entityBuilder) {
        return switch (entityBuilder) {
            case EntityMetadataBuilder.Synthetic s when isSyntheticBasedOnPersistentEntityType(s.getJavaType()) ->
                    Optional.of(plainProp(VERSION, mkPropertyTypeOrThrow(Long.class), H_LONG)
                                        .required(true).build());
            case EntityMetadataBuilder.Persistent $ ->
                    Optional.of(persistentProp(VERSION, mkPropertyTypeOrThrow(Long.class), H_LONG,
                                               PropertyNature.Persistent.data(propColumn(propColumnName(entityBuilder.getJavaType(), VERSION))))
                                        .required(true).build());
            default -> Optional.empty();
        };
    }

    private Optional<PropertyMetadata> mkPropKey(final EntityMetadataBuilder<?, ?> entityBuilder) {
        final Class<? extends Comparable<?>> keyType = getKeyType(entityBuilder.getJavaType());
        if (keyType == null) {
            throw new EntityDefinitionException("Cannot determine key type of [%s].".formatted(entityBuilder.getJavaType().getTypeName()));
        }

        if (isOneToOne(entityBuilder.getJavaType())) {
            return switch (entityBuilder) {
                case EntityMetadataBuilder.Persistent $ ->
                        Optional.of(persistentProp(KEY, mkPropertyTypeOrThrow(keyType), H_ENTITY,
                                                   PropertyNature.Persistent.data(propColumn(propColumnName(entityBuilder.getJavaType(), ID))))
                                            .required(true).build());
                case EntityMetadataBuilder.Synthetic $ ->
                        Optional.of(plainProp(KEY, mkPropertyTypeOrThrow(keyType), H_ENTITY).required(true).build());
                default -> Optional.empty();
            };
        }
        else if (DynamicEntityKey.class.equals(keyType)) {
            final var entityType = (Class<? extends AbstractEntity<DynamicEntityKey>>) entityBuilder.getJavaType();
            return Optional.of(calculatedProp(KEY, COMPOSITE_KEY, H_STRING,
                                              PropertyNature.Calculated.data(generateCompositeKeyEqlExpression(entityType), true, false))
                                       // TODO: Why required?
                                       //       Most likely this indicates that a composite key (not just a member) would always have a value.
                                       //       Need to better understand how this information is used when transpiling from EQL to SQL.
                                       .required(true).build());
        } else {
            final var keyColumn = new PropColumn("KEY_");
            final PropertyTypeMetadata propTypeMd = mkPropertyTypeOrThrow(keyType);
            final var getHibType = hibTypeGenerator.generate(propTypeMd);
            return switch (entityBuilder) {
                case EntityMetadataBuilder.Persistent $ ->
                        Optional.of(persistentProp(KEY, propTypeMd, getHibType.get(), PropertyNature.Persistent.data(keyColumn))
                                            .required(true).build());
                case EntityMetadataBuilder.Synthetic s ->
                        isSyntheticBasedOnPersistentEntityType(s.getJavaType())
                                ? Optional.of(plainProp(KEY, propTypeMd, getHibType.get())
                                                      .required(true).build())
                                : Optional.of(plainProp(KEY, propTypeMd, getHibType.getOpt().orElse(null))
                                                      .required(true).build());
                default -> Optional.empty();
            };
        }
    }

    /**
     * Semantics of {@code id} depends on the enclosing entity's nature:
     * <ul>
     *   <li> Persistent - included as persistent.
     *   <li> Synthetic:
     *     <ul>
     *       <li> Entity-typed key - implicitly calculated making it equal to {@code key}.
     *       <li> Else - included as plain.
     *     </ul
     *   <li> Else - excluded.
     * </ul>
     */
    private Optional<PropertyMetadata> mkPropId(final EntityMetadataBuilder<?, ?> entityBuilder) {
        final PropertyMetadata propId = persistentProp(ID, mkPropertyTypeOrThrow(Long.class), H_ENTITY,
                                                       PropertyNature.Persistent.data(propColumn(propColumnName(entityBuilder.getJavaType(), ID))))
                .required(true).build();

        return switch (entityBuilder) {
            case EntityMetadataBuilder.Persistent $ -> Optional.of(propId);
            case EntityMetadataBuilder.Synthetic s -> {
                if (isSyntheticBasedOnPersistentEntityType(s.getJavaType())) {
                    if (isEntityType(getKeyType(s.getJavaType()))) {
                        throw new EntityDefinitionException(ERR_SYNTHETIC_ENTITY_WITH_UNSUPPORTED_KEY_TYPE.formatted(s.getJavaType().getTypeName()));
                    }
                    yield Optional.of(plainProp(ID, mkPropertyTypeOrThrow(Long.class), H_ENTITY).build());
                } else if (isEntityType(getKeyType(s.getJavaType()))) {
                    yield Optional.of(calculatedProp(ID, mkPropertyTypeOrThrow(Long.class), H_ENTITY,
                                                     PropertyNature.Calculated.data(expr().prop(KEY).model(), true, false))
                                              .build());
                } else {
                    // Unconditionally include ID for other synthetic entities.
                    // Whether it would actually be yielded in the underlying model will be known by QuerySourceInfoProvider,
                    // which is used to determine the need to fetch ID.
                    yield Optional.of(plainProp(ID, mkPropertyTypeOrThrow(Long.class), H_ENTITY).build());
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
     *   <li> One-2-one association - implicitly calculated with an EQL expression generated dynamically and included as part of the property metadata.
     *   <li> One-2-many associations - represented by collectional properties, which could also be recognised as implicitly calculated, but are recognised as transient (plain), with {@link EntityContainerEnhancer} completing their processing at the time of query execution. </li>
     * </ul>
     */
    Optional<PropertyMetadataImpl.Builder<?, ?>> mkProp(final Field field, final EntityMetadataBuilder<?, ?> entityBuilder) {
        try {
            return mkProp_(field, entityBuilder);
        } catch (final Exception ex) {
            // rethrow to facilitate debugging
            throw new DomainMetadataGenerationException("Failed to generate metadata for property [%s].".formatted(field), ex);
        }
    }

    private Optional<PropertyMetadataImpl.Builder<?, ?>> mkProp_(final Field field, final EntityMetadataBuilder<?, ?> entityBuilder) {
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
            final var propTypeMd = mkPropertyTypeOrThrow(field);
            builder = Optional.of(critOnlyProp(field.getName(), propTypeMd,
                                               hibTypeGenerator.generate(propTypeMd).use(field).getOpt().orElse(null)));
        }
        // PERSISTENT
        // old code: last 2 conditions are to overcome incorrect metadata combinations
        // TODO: Should an exception be thrown for incorrect definitions?
        //       It is probably best to delegate verification of property declarations to the compile time verifier.
        else if (atMapTo != null && !entityBuilder.getNature().isSynthetic() && atCalculated == null) {
            final var propTypeMd = mkPropertyTypeOrThrow(field);
            builder = Optional.of(
                    persistentProp(field.getName(), propTypeMd,
                                   hibTypeGenerator.generate(propTypeMd).use(field).get(),
                                   PropertyNature.Persistent.data(propColumn(propColumnName(field.getDeclaringClass(), field.getName()),
                                                                             atIsProperty))));
        }
        // CALCULATED
        else if (atCalculated != null) {
            final boolean aggregatedExpression = AGGREGATED_EXPRESSION == atCalculated.category();
            final var data = PropertyNature.Calculated.data(
                    extractExpressionModelForCalculatedProperty(enclosingEntityType, field, atCalculated), false, aggregatedExpression);
            final var propTypeMd = mkPropertyTypeOrThrow(field);
            builder = Optional.of(calculatedProp(field.getName(), propTypeMd,
                                                 hibTypeGenerator.generate(propTypeMd).use(field).get(),
                                                 data));
        }
        // TRANSIENT
        else {
            // skip properties that have an unknown type
            final var optPropType = mkPropertyType(field);
            if (optPropType.isEmpty()) {
                LOGGER.debug(() -> "Skipping metadata generation for property [%s] due to its unrecognised type.".formatted(field));
            }
            builder = optPropType
                    .map(propTypeMd -> plainProp(field.getName(), propTypeMd,
                                                 hibTypeGenerator.generate(propTypeMd).use(field).getOpt().orElse(null)));
        }

        return builder
                // Scan the property for any additional metadata
                .map(bld -> bld.required(isRequiredByDefinition(field, enclosingEntityType)))
                .map(bld -> getAnnotationOptionally(field, CompositeKeyMember.class).map(annot -> bld.with(KEY_MEMBER, annot)).orElse(bld))
                .map(builder1 -> enhanceWithAuditingData(builder1, entityBuilder));
    }

    private Optional<PropertyMetadataImpl.Builder<?, ?>> mkOne2OneProp(final Field field, final EntityMetadataBuilder<?, ?> entityBuilder) {
        // TODO: Optional metadata Key<Boolean> to indicate that this property is one-2-one?
        final var propType = (Class<? extends AbstractEntity<?>>) field.getType();
        // Properties representing one-2-one associations are implicitly calculated nullable properties.
        // Instances of one-2-one are not required to exist, but in practice they always do get created and saved together with the main entity.
        final ExpressionModel expressionModel = expr()
                .model(select(propType).where().prop(KEY).eq().extProp(ID).model())
                .model();
        final PropertyTypeMetadata typeMetadata = mkPropertyTypeOrThrow(field);
        return Optional.of(calculatedProp(field.getName(), typeMetadata,
                                          hibTypeGenerator.generate(typeMetadata).use(field).get(),
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
    // A cache for primitive property types that exists throughout the lifetime of this generator
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
                .orElseThrow(() -> new DomainMetadataGenerationException(
                        "Failed to generate metadata for type of property [%s].".formatted(field.toGenericString())));
    }

    private PropertyTypeMetadata mkPropertyTypeOrThrow(final Type type) {
        return mkPropertyType(type)
                .orElseThrow(() -> new DomainMetadataGenerationException(
                        "Failed to generate metadata for property type [%s].".formatted(type.getTypeName())));
    }

    ///////////////////////////
    //// Persistent Entity ////
    ///////////////////////////

    String propColumnName(final Class<?> owner, final String propName) {
        return Optional.ofNullable(specialPropColumns.get(propName))
                .map(propColumn -> propColumn.name)
                .orElseGet(() -> getPropertyAnnotationOptionally(MapTo.class, owner, propName)
                        .filter(atMapTo -> isNotEmpty(atMapTo.value()))
                        .map(MapTo::value)
                        .orElseGet(() -> propName.toUpperCase() + "_"));
    }

    String propColumnNameForUnion(final String propColumnName, final String memberColumnName) {
        final var result = propColumnName + "_" + memberColumnName;
        return result.endsWith("_") ? result.substring(0, result.length() - 1) : result;
    }

    String propColumnNameForComponent(final String propColumnName, final String componentColumnName) {
        final var result =  propColumnName + (propColumnName.endsWith("_") ? "" : "_") + componentColumnName;
        return result.endsWith("_") ? result.substring(0, result.length() - 1) : result;
    }

    PropColumn propColumn(final String columnName, final Optional<IsProperty> optIsProperty) {
        return optIsProperty
                .map(isProperty -> propColumn(columnName, isProperty))
                .orElseGet(() -> propColumn(columnName));
    }

    PropColumn propColumn(final String columnName) {
        return requireNonNullElseGet(
                specialPropColumns.getOrDefault(columnName, null),
                () -> new PropColumn(columnName));
    }

    PropColumn propColumn(final String columnName, final IsProperty isProperty) {
        return requireNonNullElseGet(
                specialPropColumns.getOrDefault(columnName, null),
                () -> {
                    final var length = isProperty.length() > 0 ? isProperty.length() : null;
                    final var precision = isProperty.precision() >= 0 ? isProperty.precision() : null;
                    final var scale = isProperty.scale() >= 0 ? isProperty.scale() : null;
                    return new PropColumn(columnName, length, precision, scale);
                });
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
            throw new DomainMetadataGenerationException(ERR_TABLE_NAME_NOT_DETERMINED.formatted(entityType.getTypeName()), ex);
        }
    }

    ///////////////////////////////////
    ////////// Union Entity ///////////
    ///////////////////////////////////

    /**
     * Generates EQL models to retrieve each union-property, defined in {@code unionType}.
     *
     * @param unionType
     * @return
     * @param <ET>
     */
    private static <ET extends AbstractUnionEntity> List<EntityResultQueryModel<ET>>
    produceUnionEntityModels(final Class<ET> unionType) {
        final List<Field> unionProps = unionProperties(unionType);
        return unionProps.stream()
                .map(unionProp -> generateModelForUnionEntityProperty(unionProps, unionProp).modelAsEntity(unionType))
                .toList();
    }

    /**
     * Given a list of union properties and the current property from that list, constructs a query that yields {@code id}
     * under the selected property's name, and {@code null} under names of other properties.
     *
     * {@snippet lang=none :
     * ["a", "b", "c"], "b"
     * ->
     * select(B).yield().val(null).as("a")
     *          .yield().prop("id").as("b")
     *          .yield().val(null).as("c")
     * }
     * where {@code B} is the type of property {@code "b"}.
     */
    private static EntityQueryProgressiveInterfaces.ISubsequentCompletedAndYielded<?>
    generateModelForUnionEntityProperty(final List<Field> unionProps, final Field currProp)
    {
        final var startWith = select((Class<? extends AbstractEntity<?>>) currProp.getType());
        final var firstUnionProp = unionProps.getFirst();
        final var initialModel = firstUnionProp.equals(currProp)
                                 ? startWith.yield().prop(ID).as(firstUnionProp.getName())
                                 : startWith.yield().val(null).as(firstUnionProp.getName());
        return foldLeft(unionProps.stream().skip(1),
                        initialModel,
                        (m, f) -> f.equals(currProp)
                                  ? m.yield().prop(ID).as(f.getName())
                                  : m.yield().val(null).as(f.getName()));
    }

    List<PropertyMetadata> generateUnionImplicitCalcSubprops(
            final Class<? extends AbstractUnionEntity> unionType,
            @Nullable final String contextPropName,
            final EntityMetadataBuilder<?, ?> entityBuilder,
            final SubPropertyNaming naming)
    {
        final List<Field> unionMembers = unionProperties(unionType);
        if (unionMembers.isEmpty()) {
            throw new EntityDefinitionException(ERR_UNION_ENTITY_HAS_NO_UNION_MEMBERS.formatted(unionType.getTypeName()));
        }

        final Function<String, String> makeName = contextPropName == null
                ? Function.identity()
                : subPropName -> naming.apply(contextPropName, subPropName);

        final List<String> unionMembersNames = unionMembers.stream().map(Field::getName).toList();
        final List<PropertyMetadata> props = new ArrayList<>();
        props.add(calculatedProp(makeName.apply(KEY), mkPropertyTypeOrThrow(String.class), H_STRING,
                                 PropertyNature.Calculated.data(generateUnionEntityPropertyContextualExpression(unionMembersNames, KEY, contextPropName), true, false))
                          .build());
        props.add(calculatedProp(makeName.apply(ID), mkPropertyTypeOrThrow(Long.class), H_ENTITY,
                                 PropertyNature.Calculated.data(generateUnionEntityPropertyContextualExpression(unionMembersNames, ID, contextPropName), true, false))
                          .build());
        props.add(calculatedProp(makeName.apply(DESC), mkPropertyTypeOrThrow(String.class), H_STRING,
                                 PropertyNature.Calculated.data(generateUnionCommonDescPropExpressionModel(unionMembers, contextPropName), true, false))
                          .build());

        final Class<?> firstUnionEntityPropType = unionMembers.getFirst().getType(); // e.g., WagonSlot in TgBogieLocation
        for (final String commonProp : commonProperties(unionType).stream().filter(n -> !DESC.equals(n) && !KEY.equals(n)).toList()) {
            if (unionMembersNames.contains(commonProp)) {
                throw new EntityDefinitionException(ERR_UNION_ENTITY_PROPERTY_NAME_CONFLICT.formatted(unionType.getTypeName(), commonProp));
            }
            final Field commonPropField = findFieldByName(firstUnionEntityPropType, commonProp);
            final PropertyTypeMetadata typeMetadata = mkPropertyTypeOrThrow(commonPropField);
            props.add(calculatedProp(makeName.apply(commonProp),
                                     typeMetadata,
                                     hibTypeGenerator.generate(typeMetadata).use(commonPropField).get(),
                                     PropertyNature.Calculated.data(generateUnionEntityPropertyContextualExpression(unionMembersNames, commonProp, contextPropName), true, false))
                              .build());
        }

        return unmodifiableList(props);
    }

    private List<PropertyMetadata> generateUnionImplicitCalcSubprops(
            final Class<? extends AbstractUnionEntity> unionType,
            final EntityMetadataBuilder<?, ?> entityBuilder,
            final SubPropertyNaming naming)
    {
        return generateUnionImplicitCalcSubprops(unionType, null, entityBuilder, naming);
    }

    private ExpressionModel generateUnionCommonDescPropExpressionModel(final List<Field> unionMembers, final @Nullable String contextPropName) {
        final List<String> unionMembersNames = unionMembers.stream()
                .filter(et -> hasDescProperty((Class<? extends AbstractEntity<?>>) et.getType()))
                .map(Field::getName)
                .toList();
        return generateUnionEntityPropertyContextualExpression(unionMembersNames, DESC, contextPropName);
    }

    private static ExpressionModel generateUnionEntityPropertyContextualExpression(
            final List<String> unionMembers,
            final String commonSubpropName,
            final @Nullable String contextPropName)
    {
        if (unionMembers.isEmpty()) {
            return expr().val(null).model();
        }
        final Iterator<String> iterator = unionMembers.iterator();
        final String firstUnionPropName = (contextPropName == null ? "" :  contextPropName + ".") + iterator.next();
        var expressionModelInProgress = expr()
                .caseWhen().prop(firstUnionPropName).isNotNull().then().prop(firstUnionPropName + "." + commonSubpropName);
        while (iterator.hasNext()) {
            final String unionPropName = (contextPropName == null ? "" :  contextPropName + ".") + iterator.next();
            expressionModelInProgress = expressionModelInProgress.when().prop(unionPropName).isNotNull().then().prop(unionPropName + "." + commonSubpropName);
        }

        return expressionModelInProgress.end().model();
    }

    //////////////////////////////////////////
    ////// Calculated property utilities /////
    //////////////////////////////////////////

    private static ExpressionModel extractExpressionModelForCalculatedProperty
            (final Class<? extends AbstractEntity<?>> entityType, final Field prop, final Calculated atCalculated)
    {
        try {
            if (isNotEmpty(atCalculated.value())) {
                return createExpressionText2ModelConverter(entityType, atCalculated).convert().getModel();
            } else {
                final Field exprField = getFieldByName(entityType, prop.getName() + "_");
                exprField.setAccessible(true);
                return (ExpressionModel) exprField.get(null);
            }
        } catch (final Exception ex) {
            throw new DomainMetadataGenerationException(ERR_CANNOT_OBTAIN_EXPRESSION_MODEL_FOR_CALCULATED_PROPERTY.formatted(prop), ex);
        }
    }

    private static ExpressionText2ModelConverter createExpressionText2ModelConverter
            (final Class<? extends AbstractEntity<?>> entityType, final Calculated atCalculated)
    {
        if (isContextual(atCalculated)) {
            return new ExpressionText2ModelConverter(getRootType(atCalculated), atCalculated.contextPath(), atCalculated.value());
        } else {
            return new ExpressionText2ModelConverter(entityType, atCalculated.value());
        }
    }

    private static Class<? extends AbstractEntity<?>> getRootType(final Calculated atCalculated) {
        try {
            return (Class<? extends AbstractEntity<?>>) DynamicEntityClassLoader.loadType(atCalculated.rootTypeName());
        } catch(final Exception ex) {
            throw new DomainMetadataGenerationException(ERR_COULD_NOT_LOAD_TYPE.formatted(atCalculated.rootTypeName()), ex);
        }
    }

    /////////////////////////////
    ////// Auditing ////////////
    ////////////////////////////

    private <N extends PropertyNature, D extends PropertyNature.Data<N>> PropertyMetadataImpl.Builder<N, D> enhanceWithAuditingData(
            final PropertyMetadataImpl.Builder<N, D> propBuilder,
            final EntityMetadataBuilder<?, ?> entityBuilder)
    {
        if (isAuditEntityType(entityBuilder.getJavaType()) || isSynAuditEntityType(entityBuilder.getJavaType())) {
            // Inactive audit properties are annotated.
            if (AuditUtils.isAuditProperty(propBuilder.name())) {
                final var active = getPropertyAnnotation(InactiveAuditProperty.class, entityBuilder.getJavaType(), propBuilder.name()) == null;
                return propBuilder.with(PropertyMetadataKeys.AUDIT_PROPERTY, new KAuditProperty.Data(active));
            }
            else {
                return propBuilder;
            }
        }
        else {
            return propBuilder;
        }
    }

    /////////////////////////////
    ////// Misc. utilities /////
    ////////////////////////////

    static EntityNature inferEntityNature(final Class<? extends AbstractEntity<?>> entityType) {
        if (isPersistentEntityType(entityType)) {
            return PERSISTENT;
        }
        else if (isSyntheticEntityType(entityType)) {
            return SYNTHETIC;
        }
        else if (isUnionEntityType(entityType)) {
            return UNION;
        }
        else {
            return OTHER;
        }
    }

    static boolean hasNature(final Class<? extends AbstractEntity<?>> entityType, final EntityNature nature) {
        return nature.equals(inferEntityNature(entityType));
    }

    static boolean hasAnyNature(final Class<? extends AbstractEntity<?>> entityType, final Iterable<? extends EntityNature> natures) {
        return Iterables.any(natures, nature -> hasNature(entityType, nature));
    }

    private static boolean isGenerated(final Class<?> type) {
        // NOTE: it would be nice if all generated types implemented a marker interface (e.g., IGenerated)
        return DynamicEntityClassLoader.isGenerated(type)
                || isInstrumented(type)
                || isProxied(type)
                || isMockNotFoundType(type);
    }

}
