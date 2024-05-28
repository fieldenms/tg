package ua.com.fielden.platform.meta;

import com.google.common.collect.ImmutableList;
import com.google.inject.Injector;
import org.hibernate.dialect.Dialect;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.exceptions.InvalidArgumentException;
import ua.com.fielden.platform.entity.query.DbVersion;
import ua.com.fielden.platform.entity.query.EntityBatchInsertOperation.TableStructForBatchInsertion;
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

import static com.google.common.collect.ImmutableList.toImmutableList;
import static java.lang.String.format;
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

    DomainMetadataImpl(final Map<Class<? extends AbstractEntity<?>>, EntityMetadata> entityMetadataMap,
                       final Map<Class<?>, TypeMetadata.Composite> compositeTypeMetadataMap,
                       final Collection<? extends Class<? extends AbstractEntity<?>>> entityTypes,
                       final DomainMetadataGenerator generator,
                       final DbVersion dbVersion) {
        this.entityMetadataMap = new ConcurrentHashMap<>(entityMetadataMap);
        this.compositeTypeMetadataMap = new ConcurrentHashMap<>(compositeTypeMetadataMap);
        this.entityTypes = entityTypes.stream().distinct().collect(toImmutableList());
        this.generator = generator;
        this.dbVersion = dbVersion;
        this.pmUtils = new PropertyMetadataUtilsImpl(this, generator);
        this.emUtils = new EntityMetadataUtilsImpl();
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

    private final Injector hibTypesInjector = null;
    private final Map<Class<?>, Object> hibTypesDefaults = Map.of();
    private final DbVersion dbVersion;
    // TODO populate
    private final ConcurrentMap<Class<? extends AbstractEntity<?>>, EqlTable> tables = new ConcurrentHashMap<>();
    // TODO populate
    private final ConcurrentMap<String, TableStructForBatchInsertion> tableStructsForBatchInsertion = new ConcurrentHashMap<>();

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
        // TODO
        return null;
    }

}
