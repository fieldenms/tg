package ua.com.fielden.platform.migration;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import jakarta.annotation.Nullable;
import jakarta.inject.Singleton;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.meta.EntityMetadata;
import ua.com.fielden.platform.meta.IDomainMetadata;
import ua.com.fielden.platform.meta.PropertyMetadata;
import ua.com.fielden.platform.meta.PropertyTypeMetadata;
import ua.com.fielden.platform.types.markers.IUtcDateTimeType;
import ua.com.fielden.platform.types.tuples.T2;
import ua.com.fielden.platform.utils.CollectionUtil;
import ua.com.fielden.platform.utils.EntityUtils;

import java.sql.ResultSet;
import java.util.*;
import java.util.stream.Stream;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static java.util.Collections.unmodifiableMap;
import static java.util.function.Predicate.not;
import static ua.com.fielden.platform.entity.AbstractEntity.*;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.from;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;
import static ua.com.fielden.platform.meta.PropertyMetadataKeys.REQUIRED;
import static ua.com.fielden.platform.types.tuples.T2.t2;
import static ua.com.fielden.platform.utils.CollectionUtil.listCopy;
import static ua.com.fielden.platform.utils.CollectionUtil.setOf;
import static ua.com.fielden.platform.utils.EntityUtils.isOneToOne;
import static ua.com.fielden.platform.utils.EntityUtils.isPersistentEntityType;

@Singleton
final class MigrationUtils {

    public static final String
            ERR_READING_DATA = "Could not read data.",
            ERR_UNRECOGNISED_PROPERTIES = "Unrecognised properties were specified in the mapping: [%s].",
            ERR_MISSING_MAPPINGS_FOR_SOME_KEY_MEMBERS = "Mappings for some key members are missing: [%s].",
            ERR_MISSING_MAPPING_FOR_REQUIRED_PROPERTY = "Mapping for required property [%s] is missing.",
            ERR_MAPPING_IS_INCOMPLETE = "Mapping for property [%s] is incomplete, missing members: [%s].",
            ERR_NON_PERSISTENT_ENTITY = "Unable to generate a retriever job for non-persistent entity [%s].";

    private static final Logger LOGGER = LogManager.getLogger();

    private static final Set<String> PROPS_TO_IGNORE = setOf(ID, VERSION);

    private final IDomainMetadata domainMetadata;
    private final ICompanionObjectFinder coFinder;

    @Inject
    MigrationUtils(final IDomainMetadata domainMetadata, final ICompanionObjectFinder coFinder) {
        this.domainMetadata = domainMetadata;
        this.coFinder = coFinder;
    }

    public EntityMd generateEntityMd(final Class<? extends AbstractEntity<?>> entityType) {
        final var entityMetadata = domainMetadata.forEntity(entityType).asPersistent()
                .orElseThrow(() -> new DataMigrationException(ERR_NON_PERSISTENT_ENTITY.formatted(entityType.getSimpleName())));
        final var tableName = entityMetadata.data().tableName();
        final var pmUtils = domainMetadata.propertyMetadataUtils();
        final var propMds = entityMetadata.properties().stream()
                .filter(pm -> !PROPS_TO_IGNORE.contains(pm.name()))
                .map(PropertyMetadata::asPersistent).flatMap(Optional::stream)
                .flatMap(pm -> Optional.<Stream<PropMd>>empty()
                        // Component-typed property: expand into components, unless there is just a single component.
                        .or(() -> pm.type()
                                .asComponent()
                                .map(ct -> {
                                    final var subPms = pmUtils.subProperties(pm)
                                            .stream()
                                            .map(PropertyMetadata::asPersistent).flatMap(Optional::stream)
                                            .toList();
                                    if (subPms.size() > 1) {
                                        return subPms.stream()
                                                .map(spm -> generatePropMd(spm, pm));
                                    }
                                    // Special case: a component-typed property with a single component.
                                    // Expanding into sub-properties should be skipped, so that the property could be specified in retrievers by itself.
                                    // For example, `money` instead of `money.amount`.
                                    else {
                                        return Stream.of(generatePropMd(pm, null));
                                    }
                                }))
                        // Union-typed property: expand into union members.
                        .or(() -> pm.type()
                                .asEntity()
                                .flatMap(et -> domainMetadata.forEntityOpt(et.javaType()))
                                .flatMap(EntityMetadata::asUnion)
                                .map(union -> pmUtils.subProperties(pm)
                                        .stream()
                                        .map(PropertyMetadata::asPersistent).flatMap(Optional::stream)
                                        .map(spm -> generatePropMd(spm, pm))))
                        // Other properties
                        .orElseGet(() -> Stream.of(generatePropMd(pm, null))))
                .toList();

        return new EntityMd(tableName, propMds);
    }

    private PropMd generatePropMd(
            final PropertyMetadata.Persistent prop,
            final @Nullable PropertyMetadata parentProp)
    {
        final var name = combinePath(parentProp, prop.name());
        final var leaves = keyPaths(parentProp, prop);
        return new PropMd(name,
                          (Class<?>) prop.type().javaType(),
                          prop.data().column().name,
                          prop.is(REQUIRED),
                          prop.hibType() instanceof IUtcDateTimeType,
                          leaves);
    }

    private static String combinePath(final @Nullable PropertyMetadata a, final String b) {
        return a == null ? b : a.name() + "." + b;
    }

    public List<String> keyPaths(final Class<? extends AbstractEntity<?>> entityType) {
        final EntityMetadata em = domainMetadata.forEntity(entityType);
        final var keyMembers = domainMetadata.entityMetadataUtils().compositeKeyMembers(em);
        if (keyMembers.isEmpty()) {
            if (EntityUtils.isOneToOne(entityType)) {
                return keyPaths(em.property(KEY));
            } else {
                return List.of(KEY);
            }
        } else {
            return keyMembers.stream().map(this::keyPaths).flatMap(Collection::stream).collect(toImmutableList());
        }
    }

    private List<String> keyPaths(final PropertyMetadata pm) {
        return keyPaths(null, pm);
    }

    private List<String> keyPaths(final @Nullable PropertyMetadata parentProp, final PropertyMetadata prop) {
        final var paths = switch (prop.type()) {
            case PropertyTypeMetadata.Entity et ->
                    keyPaths_(et.javaType())
                            .map(it -> it.stream().map(p -> combinePath(prop, p)).toList())
                            .orElseGet(() -> List.of(prop.name()));
            default -> List.of(prop.name());
        };

        return paths.stream().map(p -> combinePath(parentProp, p)).collect(toImmutableList());
    }

    private Optional<List<String>> keyPaths_(final Class<? extends AbstractEntity<?>> entityType) {
        return domainMetadata.forEntityOpt(entityType)
                .flatMap(em -> switch (em) {
                    case EntityMetadata.Union union -> Optional.of(
                            domainMetadata.entityMetadataUtils()
                                    .unionMembers(union)
                                    .stream()
                                    .map(this::keyPaths)
                                    .flatMap(Collection::stream)
                                    .toList());
                    case EntityMetadata.Persistent persistent -> {
                        final var keyMembers = domainMetadata.entityMetadataUtils().compositeKeyMembers(domainMetadata.forEntity(persistent.javaType()));
                        yield keyMembers.isEmpty()
                                ? Optional.empty()
                                : Optional.of(keyMembers.stream().map(this::keyPaths).flatMap(Collection::stream).toList());
                    }
                    default -> Optional.empty();
                });
    }

    private List<PropInfo> produceContainers(
            final List<PropMd> props,
            final List<String> keyMemberPaths,
            final Map<String, Integer> resultFieldIndices,
            final boolean isUpdater)
    {
        final var usedPaths = new HashSet<String>();

        final var propInfos = props.stream()
                .map(propMd -> {
                    final var indices = obtainIndices(propMd.leafProps(), resultFieldIndices);
                    // If the number of null values doesn't match the number of indices, the mapping is incomplete.
                    final long nullCount = indices.values().stream().filter(Objects::isNull).count();
                    if (nullCount > 0 && nullCount != indices.size()) {
                        throw new DataMigrationException(ERR_MAPPING_IS_INCOMPLETE
                                                         .formatted(propMd.name(),
                                                                    CollectionUtil.toString(Maps.filterValues(indices, Objects::isNull).keySet(), ", ")));
                    }
                    else if (!indices.containsValue(null)) {
                        usedPaths.addAll(propMd.leafProps());
                        return new PropInfo(propMd.name(), propMd.type(), propMd.column(), propMd.utcType(), ImmutableList.copyOf(indices.values()));
                    }
                    else if (propMd.required() && !isUpdater) {
                        throw new DataMigrationException(ERR_MISSING_MAPPING_FOR_REQUIRED_PROPERTY.formatted(propMd.name()));
                    }
                    else return null;
                })
                .filter(Objects::nonNull)
                .toList();

        final var missingKeys = keyMemberPaths.stream().filter(not(resultFieldIndices::containsKey)).toList();
        if (!missingKeys.isEmpty()) {
            throw new DataMigrationException(ERR_MISSING_MAPPINGS_FOR_SOME_KEY_MEMBERS.formatted(CollectionUtil.toString(missingKeys, ", ")));
        }

        if (!resultFieldIndices.keySet().equals(usedPaths)) {
            final var diff = Sets.difference(resultFieldIndices.keySet(), usedPaths);
            throw new DataMigrationException(ERR_UNRECOGNISED_PROPERTIES.formatted(CollectionUtil.toString(diff, ", ")));
        }

        return propInfos;
    }

    private static LinkedHashMap<String, Integer> obtainIndices(final List<String> leafProps, final Map<String, Integer> resultFieldIndices) {
        final var result = new LinkedHashMap<String, Integer>();
        for (final var lp : leafProps) {
            result.put(lp, resultFieldIndices.get(lp));
        }
        return result;
    }

    public List<Integer> produceKeyFieldsIndices(final Class<? extends AbstractEntity<?>> entityType, final Map<String, Integer> resultFieldIndices) {
        return listCopy(obtainIndices(keyPaths(entityType), resultFieldIndices).values());
    }

    public Map<Object, Long> cacheForType(final IdCache cache, final Class<? extends AbstractEntity<?>> entityType) {
        return cache.cacheFor(entityType, () -> retrieveDataForCache(entityType));
    }

    private <ET extends AbstractEntity<?>> Map<Object, Long> retrieveDataForCache(final Class<ET> entityType) {
        final var co = coFinder.find(entityType);
        final var keyPaths = keyPaths(entityType);
        final var result = new HashMap<Object, Long>();
        try (final var stream = co.stream(from(select(entityType).model()).model(), 1000)) {
            stream.forEach(entity -> result.put(entityToCacheKey(entity, keyPaths), entity.getId()));
        }
        return unmodifiableMap(result);
    }

    private Object entityToCacheKey(final AbstractEntity<?> entity, final List<String> keyPaths) {
        if (keyPaths.size() == 1) {
            return entity.get(keyPaths.getFirst());
        }
        else {
            return keyPaths.stream().map(entity::get).toList();
        }
    }

    public TargetDataInsert targetDataInsert(
            final Class<? extends AbstractEntity<?>> entityType,
            final Map<String, Integer> resultFieldIndices,
            final EntityMd entityMd)
    {
        final var containers = produceContainers(entityMd.props(), keyPaths(entityType), resultFieldIndices, false);
        final var insertStmt = TargetDataInsert.generateInsertStmt(containers.stream().map(PropInfo::column).toList(),
                                                                   entityMd.tableName(),
                                                                   !isOneToOne(entityType));
        final var keyIndices = produceKeyFieldsIndices(entityType, resultFieldIndices);
        return new TargetDataInsert(entityType, containers, insertStmt, keyIndices);
    }

    public List<T2<Object, Boolean>> transformValuesForInsert(
            final TargetDataInsert tdi,
            final ResultSet resultSet,
            final IdCache cache,
            final long id)
    {
        final var result = new ArrayList<T2<Object, Boolean>>();
        for (final var propInfo : tdi.containers()) {
            final var values = propInfo.indices()
                    .stream()
                    .map(index -> {
                        try {
                            return resultSet.getObject(index);
                        } catch (final Exception ex) {
                            throw new DataMigrationException(ERR_READING_DATA, ex);
                        }
                    })
                    .toList();
            result.add(t2(transformValue(propInfo.propType(), values, cache), propInfo.utcType()));
        }

        result.add(t2(0, false)); // for version
        if (!isOneToOne(tdi.entityType())) {
            result.add(t2(id, false)); // for ID where applicable
        }

        return result;
    }

    private Object transformValue(final Class<?> type, final List<Object> values, final IdCache cache) {
        if (!isPersistentEntityType(type)) {
            return values.getFirst();
        }
        else {
            final var cacheForType = cacheForType(cache, (Class<? extends AbstractEntity<?>>) type);
            final var entityKeyObject = values.size() == 1 ? values.getFirst() : values;
            final var id = cacheForType.get(entityKeyObject);

            if (id == null && values.size() == 1 && values.getFirst() != null) {
                LOGGER.warn(() -> "Could not find ID for [%s] with key [%s]".formatted(type.getSimpleName(), values.getFirst()));
            }
            if (id == null && values.size() > 1 && values.stream().anyMatch(Objects::nonNull)) {
                LOGGER.warn(() -> "Could not find ID for [%s] with key values: %s".formatted(type.getSimpleName(), CollectionUtil.toString(values, "[%s]"::formatted, ", ")));
            }

            return id;
        }
    }

    public TargetDataUpdate targetDataUpdate(
            final Class<? extends AbstractEntity<?>> entityType,
            final Map<String, Integer> retrieverResultFieldsIndices,
            final EntityMd entityMd)
    {
        final var containers = produceContainers(entityMd.props(), keyPaths(entityType), retrieverResultFieldsIndices, true);
        final var updateStmt = TargetDataUpdate.generateUpdateStmt(containers.stream().map(PropInfo::column).toList(), entityMd.tableName());
        final var keyIndices = produceKeyFieldsIndices(entityType, retrieverResultFieldsIndices);
        return new TargetDataUpdate(entityType, containers, updateStmt, keyIndices);
    }

    public List<Object> transformValuesForUpdate(final TargetDataUpdate tdu, final ResultSet legacyRs, final IdCache cache, final long id) {
        final var result = new ArrayList<>();
        for (final var propInfo : tdu.containers()) {
            final var values = propInfo.indices()
                    .stream()
                    .map(index -> {
                        try {
                            return legacyRs.getObject(index);
                        } catch (final Exception ex) {
                            throw new DataMigrationException(ERR_READING_DATA, ex);
                        }
                    })
                    .toList();
            result.add(transformValue(propInfo.propType(), values, cache));
        }
        result.add(id);
        return result;
    }

}
