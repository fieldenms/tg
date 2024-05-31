package ua.com.fielden.platform.meta;

import com.google.common.collect.ImmutableList;
import com.google.inject.Injector;
import org.hibernate.dialect.Dialect;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.DbVersion;
import ua.com.fielden.platform.entity.query.EntityBatchInsertOperation.TableStructForBatchInsertion;
import ua.com.fielden.platform.entity.query.EntityBatchInsertOperation.TableStructForBatchInsertion.PropColumnInfo;
import ua.com.fielden.platform.eql.dbschema.ColumnDefinitionExtractor;
import ua.com.fielden.platform.eql.dbschema.TableDdl;
import ua.com.fielden.platform.eql.exceptions.EqlMetadataGenerationException;
import ua.com.fielden.platform.eql.meta.EqlTable;
import ua.com.fielden.platform.eql.meta.QuerySourceInfoProvider;
import ua.com.fielden.platform.reflection.asm.impl.DynamicEntityClassLoader;
import ua.com.fielden.platform.utils.EntityUtils;
import ua.com.fielden.platform.utils.Pair;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static java.lang.String.format;
import static java.util.Objects.requireNonNullElseGet;
import static java.util.stream.Collectors.toConcurrentMap;
import static ua.com.fielden.platform.entity.AbstractEntity.ID;
import static ua.com.fielden.platform.entity.AbstractEntity.VERSION;
import static ua.com.fielden.platform.meta.HibernateTypeGenerator.H_BOOLEAN;
import static ua.com.fielden.platform.types.tuples.T2.t2;
import static ua.com.fielden.platform.utils.EntityUtils.isPersistedEntityType;
import static ua.com.fielden.platform.utils.StreamUtils.typeFilter;

final class DomainMetadataImpl implements IDomainMetadata {

    /** Mutable map, may be populated with entity types for which metadata is generated ad-hoc. */
    private final Map<Class<? extends AbstractEntity<?>>, EntityMetadata> entityMetadataMap;
    /** Mutable map, may be populated with composite types for which metadata is generated ad-hoc. */
    private final Map<Class<?>, TypeMetadata.Composite> compositeTypeMetadataMap;
    private final Collection<? extends Class<? extends AbstractEntity<?>>> entityTypes;
    private final DomainMetadataGenerator generator;
    private final PropertyMetadataUtils pmUtils;
    private final EntityMetadataUtils emUtils;
    private final QuerySourceInfoProvider querySourceInfoProvider;

    DomainMetadataImpl(final Map<Class<? extends AbstractEntity<?>>, EntityMetadata> entityMetadataMap,
                       final Map<Class<?>, TypeMetadata.Composite> compositeTypeMetadataMap,
                       final Collection<? extends Class<? extends AbstractEntity<?>>> entityTypes,
                       final DomainMetadataGenerator generator,
                       final Injector hibTypesInjector,
                       final @Nullable Map<? extends Class, ? extends Class> hibTypesDefaults,
                       final DbVersion dbVersion) {
        this.entityMetadataMap = new ConcurrentHashMap<>(entityMetadataMap);
        this.compositeTypeMetadataMap = new ConcurrentHashMap<>(compositeTypeMetadataMap);
        this.entityTypes = entityTypes.stream().distinct().collect(toImmutableList());
        this.generator = generator;
        this.pmUtils = new PropertyMetadataUtilsImpl(this, generator);
        this.emUtils = new EntityMetadataUtilsImpl();

        this.hibTypesInjector = hibTypesInjector;
        this.dbVersion = dbVersion;
        initBaggage(requireNonNullElseGet(hibTypesDefaults, Map::of), entityMetadataMap.values());
        this.querySourceInfoProvider = new QuerySourceInfoProvider(this);
    }

    @Override
    public PropertyMetadataUtils propertyMetadataUtils() {
        return pmUtils;
    }

    @Override
    public EntityMetadataUtils entityMetadataUtils() {
        return emUtils;
    }

    @Override
    public Collection<? extends TypeMetadata> allTypes() {
        // TODO optimise by changing the return type to Stream
        return ImmutableList.<TypeMetadata>builderWithExpectedSize(entityMetadataMap.size() + compositeTypeMetadataMap.size())
                .addAll(entityMetadataMap.values())
                .addAll(compositeTypeMetadataMap.values())
                .build();
    }

    @Override
    public <T extends TypeMetadata> Collection<T> allTypes(final Class<T> metadataType) {
        // TODO optimise by changing the return type to Stream
        if (metadataType == EntityMetadata.class) {
            return (Collection<T>) entityMetadataMap.values();
        }
        else if (metadataType == TypeMetadata.Composite.class) {
            return (Collection<T>) compositeTypeMetadataMap.values();
        }
        else {
            return entityMetadataMap.values().stream()
                    .mapMulti(typeFilter(metadataType))
                    .toList();
        }
    }

    @Override
    public Optional<? extends TypeMetadata> forType(final Class<?> javaType) {
        if (AbstractEntity.class.isAssignableFrom(javaType)) {
            return forEntityOpt((Class<? extends AbstractEntity<?>>) javaType);
        }
        return forComposite(javaType);
    }

    @Override
    public EntityMetadata forEntity(final Class<? extends AbstractEntity<?>> entityType) {
        return forEntityOpt(entityType)
                .orElseThrow(() -> new EqlMetadataGenerationException(
                        format("Could not generate metadata for entity [%s]", entityType.getTypeName())));
    }

    @Override
    public Optional<EntityMetadata> forEntityOpt(final Class<? extends AbstractEntity<?>> entityType) {
        final var em = entityMetadataMap.get(entityType);
        if (em != null) {
            return Optional.of(em);
        }

        // TODO cache?
        return generator.forEntity(entityType);
    }

    @Override
    public Optional<TypeMetadata.Composite> forComposite(final Class<?> javaType) {
        return Optional.ofNullable(compositeTypeMetadataMap.get(javaType))
                .or(() -> generator.forComposite(javaType));
    }

    @Override
    public Optional<PropertyMetadata> forProperty(final Class<?> enclosingType, final CharSequence propPath) {
        return forType(enclosingType).flatMap(tm -> {
            final Pair<String, String> head_tail = EntityUtils.splitPropByFirstDot(propPath.toString());
            final var optHeadPm = propertyFromType(tm, head_tail.getKey());
            final @Nullable String tail = head_tail.getValue();
            return tail == null ? optHeadPm : optHeadPm.flatMap(h -> forProperty_(h, tail));
        });
    }

    private Optional<PropertyMetadata> forProperty_(final PropertyMetadata pm, final String propPath) {
        return switch (pm.type()) {
            case PropertyTypeMetadata.Composite ct -> forProperty(ct.javaType(), propPath);
            case PropertyTypeMetadata.Entity et -> forProperty(et.javaType(), propPath);
            default -> Optional.empty();
        };
    }

    private Optional<PropertyMetadata> propertyFromType(final TypeMetadata tm, final String simpleProp) {
        return switch (tm) {
            case EntityMetadata em -> em.property(simpleProp);
            case TypeMetadata.Composite cm -> cm.property(simpleProp);
        };
    }

    // ****************************************
    // * Temporary baggage from old metadata that can't be moved until dependency injection is properly configured.

    private final Injector hibTypesInjector;
    private final Map<Class<?>, Object> hibTypesDefaults = new HashMap<>();
    private final DbVersion dbVersion;
    private final ConcurrentMap<Class<? extends AbstractEntity<?>>, EqlTable> tables = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, TableStructForBatchInsertion> tableStructsForBatchInsertion = new ConcurrentHashMap<>();

    private void initBaggage(final Map<? extends Class, ? extends Class> hibTypesDefaults,
                             final Collection<? extends EntityMetadata> entityMetadataMap) {
        initHibTypesDefaults(hibTypesDefaults);
        entityMetadataMap.parallelStream().map(EntityMetadata::asPersistent).flatMap(Optional::stream)
                .forEach(em -> {
                    tables.put(em.javaType(), generateEqlTable(em));
                    tableStructsForBatchInsertion.put(em.javaType().getName(), generateTableStructForBatchInsertion(em));
                });
    }

    private void initHibTypesDefaults(final Map<? extends Class, ? extends Class> hibTypesDefaults) {
        hibTypesDefaults.forEach((javaType, hibType) -> {
            try {
                this.hibTypesDefaults.put(javaType, hibType.getDeclaredField("INSTANCE").get(null));
            } catch (final Exception e) {
                throw new EqlMetadataGenerationException("Couldn't instantiate Hibernate type [%s]".formatted(hibType), e);
            }
        });

        this.hibTypesDefaults.put(Boolean.class, H_BOOLEAN);
        this.hibTypesDefaults.put(boolean.class, H_BOOLEAN);
    }

    private EqlTable generateEqlTable(final EntityMetadata.Persistent entityMetadata) {
        final Map<String, String> columns = entityMetadata.properties().stream()
                .map(PropertyMetadata::asPersistent).flatMap(Optional::stream)
                .flatMap(prop -> {
                    if (prop.type().isComposite() || pmUtils.isPropEntityType(prop, EntityMetadata::isUnion)) {
                        return pmUtils.subProperties(prop).stream()
                                .map(PropertyMetadata::asPersistent).flatMap(Optional::stream)
                                .map(subProp -> t2(prop.name() + "." + subProp.name(), subProp.data().column().name));
                    } else {
                        return Stream.of(t2(prop.name(), prop.data().column().name));
                    }
                })
                .collect(toConcurrentMap(t2 -> t2._1, t2 -> t2._2));

        return new EqlTable(entityMetadata.data().tableName(), columns);
    }

    private TableStructForBatchInsertion generateTableStructForBatchInsertion(final EntityMetadata.Persistent entityMetadata) {
        // a way to do inner helper methods (avoids pollution of the outer class method namespace)
        class $ {
            static String mkColumnName(final PropertyMetadataUtils pmUtils, final PropertyMetadata prop) {
                return prop.name() + (pmUtils.isPropEntityType(prop, EntityMetadata::isPersistent) ? ("." + ID) : "");
            }
        }

        final var columns = entityMetadata.properties().stream()
                .filter(prop -> !ID.equals(prop.name()) && !VERSION.equals(prop.name()))
                .map(PropertyMetadata::asPersistent).flatMap(Optional::stream)
                .flatMap(prop -> {
                    if (prop.type().isComposite()) {
                        final var subColumnNames = pmUtils.subProperties(prop).stream()
                                .map(PropertyMetadata::asPersistent).flatMap(Optional::stream)
                                .map(p -> p.data().column().name)
                                .toList();
                        return subColumnNames.isEmpty()
                                ? Stream.of()
                                : Stream.of(new PropColumnInfo(prop.name(), subColumnNames, prop.hibType()));
                    }
                    else if (pmUtils.isPropEntityType(prop, EntityMetadata::isUnion)) {
                        return pmUtils.subProperties(prop).stream()
                                .map(PropertyMetadata::asPersistent).flatMap(Optional::stream)
                                .map(subProp -> {
                                    final String colName = prop.name() + "." + $.mkColumnName(pmUtils, subProp);
                                    return new PropColumnInfo(colName, subProp.data().column().name, subProp.hibType());
                                });
                    }
                    else {
                        return Stream.of(new PropColumnInfo($.mkColumnName(pmUtils, prop), prop.data().column().name, prop.hibType()));
                    }
                })
                .toList();

        return new TableStructForBatchInsertion(entityMetadata.data().tableName(), columns);
    }

    /**
     * Generates DDL statements for creating tables, primary keys, indices and foreign keys for all persistent entity types,
     * which includes domain entities and auxiliary platform entities.
     */
    @Override
    public List<String> generateDatabaseDdl(final Dialect dialect) {
        final ColumnDefinitionExtractor columnDefinitionExtractor = new ColumnDefinitionExtractor(hibTypesInjector, hibTypesDefaults);

        final List<Class<? extends AbstractEntity<?>>> persistentTypes = entityTypes.stream().filter(et -> isPersistedEntityType(et)).collect(
                Collectors.toList());

        final Set<String> ddlTables = new LinkedHashSet<>();
        final Set<String> ddlFKs = new LinkedHashSet<>();

        for (final Class<? extends AbstractEntity<?>> entityType : persistentTypes) {
            final TableDdl tableDefinition = new TableDdl(columnDefinitionExtractor, entityType);
            ddlTables.add(tableDefinition.createTableSchema(dialect, ""));
            ddlTables.add(tableDefinition.createPkSchema(dialect));
            ddlTables.addAll(tableDefinition.createIndicesSchema(dialect));
            ddlFKs.addAll(tableDefinition.createFkSchema(dialect));
        }
        final List<String> ddl = new LinkedList<>();
        ddl.addAll(ddlTables);
        ddl.addAll(ddlFKs);
        return ddl;
    }

    @Override
    public EqlTable getTableForEntityType(final Class<? extends AbstractEntity<?>> entityType) {
        return tables.get(DynamicEntityClassLoader.getOriginalType(entityType));
    }

    @Override
    public TableStructForBatchInsertion getTableStructsForBatchInsertion(final Class<? extends AbstractEntity<?>> entityType) {
        return tableStructsForBatchInsertion.get(entityType.getName());
    }

    @Override
    public DbVersion dbVersion() {
        return dbVersion;
    }

    @Override
    public QuerySourceInfoProvider querySourceInfoProvider() {
        return querySourceInfoProvider;
    }

}
