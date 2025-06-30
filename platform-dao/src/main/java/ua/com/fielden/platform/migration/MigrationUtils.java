package ua.com.fielden.platform.migration;

import com.google.common.collect.ImmutableList;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.AbstractUnionEntity;
import ua.com.fielden.platform.meta.EntityMetadata;
import ua.com.fielden.platform.meta.IDomainMetadata;
import ua.com.fielden.platform.meta.PropertyMetadata;
import ua.com.fielden.platform.types.markers.IUtcDateTimeType;
import ua.com.fielden.platform.types.tuples.T2;
import ua.com.fielden.platform.utils.EntityUtils;

import javax.annotation.Nullable;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Stream;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static ua.com.fielden.platform.entity.AbstractEntity.*;
import static ua.com.fielden.platform.entity.AbstractUnionEntity.unionProperties;
import static ua.com.fielden.platform.eql.meta.EntityTypeInfo.getEntityTypeInfo;
import static ua.com.fielden.platform.meta.PropertyMetadataKeys.REQUIRED;
import static ua.com.fielden.platform.reflection.EntityMetadata.keyTypeInfo;
import static ua.com.fielden.platform.utils.CollectionUtil.setOf;
import static ua.com.fielden.platform.utils.EntityUtils.isPersistentEntityType;
import static ua.com.fielden.platform.utils.EntityUtils.isUnionEntityType;

// TODO This class uses both typeful metadata and reflection + EntityTypeInfo. The latter should be replaced.
public class MigrationUtils {
    private static final Set<String> PROPS_TO_IGNORE = setOf(ID, VERSION);

    public static EntityMd generateEntityMd(
            final String tableName,
            final Collection<PropertyMetadata> pms,
            final IDomainMetadata domainMetadata)
    {
        final var pmUtils = domainMetadata.propertyMetadataUtils();
        final var propMds = pms.stream()
                .filter(pm -> !pm.type().isCollectional())
                .flatMap(pm -> switch (pm) {
                    case PropertyMetadata.Persistent ppm when !PROPS_TO_IGNORE.contains(ppm.name()) -> {
                        final var leaves = pmUtils.isPropEntityType(ppm, EntityMetadata::isPersistent)
                                ? keyPaths(ppm, domainMetadata)
                                : List.of(pm.name());
                        yield Stream.of(new PropMd(ppm.name(), (Class<?>) ppm.type().javaType(), ppm.data().column().name,
                                                   ppm.is(REQUIRED), pm.hibType() instanceof IUtcDateTimeType, leaves));
                    }
                    default -> {
                        final boolean required = !pmUtils.isPropEntityType(pm, EntityMetadata::isUnion) && pm.is(REQUIRED);
                        yield pmUtils.subProperties(pm).stream()
                                .map(PropertyMetadata::asPersistent).flatMap(Optional::stream)
                                .map(spm -> {
                                    final var leaves = pmUtils.isPropEntityType(spm, EntityMetadata::isPersistent)
                                            ? keyPaths(spm, domainMetadata).stream().map(s -> pm.name() + "." + s).toList()
                                            : List.of(pm.name() + "." + spm.name());
                                    return new PropMd(pm.name() + "." + spm.name(), (Class<?>) spm.type().javaType(),
                                                      spm.data().column().name, required,
                                                      spm.hibType() instanceof IUtcDateTimeType, leaves);
                                });
                    }
                }).toList();

        return new EntityMd(tableName, propMds);
    }

    public static List<String> keyPaths(final Class<? extends AbstractEntity<?>> entityType, final IDomainMetadata domainMetadata) {
        final EntityMetadata em = domainMetadata.forEntity(entityType);
        final var keyMembers = domainMetadata.entityMetadataUtils().compositeKeyMembers(em);
        if (keyMembers.isEmpty()) {
            if (EntityUtils.isOneToOne(entityType)) {
                return keyPaths(em.property(KEY), domainMetadata);
            } else {
                return List.of(KEY);
            }
        } else {
            return keyPaths(null, keyMembers, domainMetadata);
        }
    }

    private static List<String> keyPaths(final PropertyMetadata pm, final IDomainMetadata domainMetadata) {
        return pm.type().asEntity().map(et -> {
            final var keyMembers = domainMetadata.entityMetadataUtils().compositeKeyMembers(domainMetadata.forEntity(et.javaType()));
            if (keyMembers.isEmpty()) {
                return List.of(pm.name());
            } else {
                return keyPaths(pm, keyMembers, domainMetadata);
            }
        }).orElseGet(List::of);
    }

    private static List<String> keyPaths(
            final @Nullable PropertyMetadata parentProp,
            final List<PropertyMetadata> keyMembers,
            final IDomainMetadata domainMetadata)
    {
        return keyMembers.stream()
                .map(km -> km.type().asEntity()
                        .flatMap(et -> domainMetadata.forEntityOpt(et.javaType()))
                        .map(em -> switch (em) {
                            case EntityMetadata.Union union -> domainMetadata.entityMetadataUtils()
                                    .unionMembers(union)
                                    .stream()
                                    .map(unionMember -> keyPaths(unionMember, domainMetadata))
                                    .flatMap(Collection::stream)
                                    .collect(toImmutableList());
                            case EntityMetadata.Persistent $ -> keyPaths(km, domainMetadata);
                            default -> ImmutableList.of(km.name());
                        })
                        .orElseGet(() -> ImmutableList.of(km.name())))
                .flatMap(Collection::stream)
                .map(s -> parentProp == null ? s : parentProp.name() + "." + s)
                .toList();
    }

    // TODO migrate to typeful metadata
    public static <ET extends AbstractEntity<?>> List<String> keyPaths(final Class<ET> entityType) {
        final var keyMembers = getEntityTypeInfo(entityType).compositeKeyMembers;
        if (keyMembers.isEmpty()) {
            if (EntityUtils.isOneToOne(entityType)) {
                return keyPaths(KEY, (Class<ET>) keyTypeInfo(entityType));
            } else {
                return ImmutableList.of(KEY);
            }
        } else {
            return keyPaths(null, keyMembers);
        }
    }

    // TODO migrate to typeful metadata
    private static <ET extends AbstractEntity<?>> List<String> keyPaths(final String parentPath, final Class<ET> entityType) {
        final List<T2<String, Class<?>>> keyMembers = getEntityTypeInfo(entityType).compositeKeyMembers;
        if (keyMembers.isEmpty()) {
            return ImmutableList.of(parentPath);
        }
        else {
            return keyPaths(parentPath, keyMembers);
        }
    }

    // TODO migrate to typeful metadata
    private static List<String> keyPaths(final @Nullable String parentPath, final List<T2<String, Class<?>>> keyMembers) {
        return keyMembers.stream()
                .map(keyMember -> keyMember.map((name, type) -> {
                    final var propPath = parentPath == null ? name : parentPath + "." + name;
                    if (isUnionEntityType(type)) {
                        return unionProperties((Class<? extends AbstractUnionEntity>) type)
                                .stream()
                                .map(unionMember -> keyPaths(propPath + "." + unionMember.getName(), (Class<? extends AbstractEntity<?>>) unionMember.getType()))
                                .flatMap(Collection::stream)
                                .collect(toImmutableList());
                    }
                    if (isPersistentEntityType(type)) {
                        return keyPaths(propPath, (Class<? extends AbstractEntity<?>>) type);
                    } else {
                        return List.of(propPath);
                    }
                }))
                .flatMap(Collection::stream)
                .collect(toImmutableList());
    }

    public static List<PropInfo> produceContainers(
            final List<PropMd> props,
            final List<String> keyMemberPaths,
            final Map<String, Integer> resultFieldIndices,
            final boolean isUpdater)
    {
        final var usedPaths = new HashSet<String>();

        final var propInfos = props.stream()
                .map(propMd -> {
                    final var indices = obtainIndices(propMd.leafProps(), resultFieldIndices);
                    // need to determine incomplete mapping for key members of entity property
                    // if the number of null values doesn't match the number of indices then mapping is incomplete
                    final long countOfNullValuedIndices = indices.values().stream().filter(Objects::isNull).count();
                    if (countOfNullValuedIndices > 0 && countOfNullValuedIndices != indices.size()) {
                        throw new DataMigrationException("Mapping for prop [" + propMd.name() + "] does not have all its members specified: " + indices.entrySet().stream().filter(entry -> entry.getValue() == null).map(Entry::getKey).toList());
                    } else if (!indices.containsValue(null)) {
                        usedPaths.addAll(propMd.leafProps());
                        return new PropInfo(propMd.name(), propMd.type(), propMd.column(), propMd.utcType(), ImmutableList.copyOf(indices.values()));
                    } else if (propMd.required() && !isUpdater) {
                        throw new DataMigrationException("prop [" + propMd.name() + "] is required");
                    }
                    else return null;
                })
                .filter(Objects::nonNull)
                .toList();

        for (final var keyMemberPath : keyMemberPaths) {
            if (!resultFieldIndices.containsKey(keyMemberPath)) {
                throw new DataMigrationException("Sql mapping for property [" + keyMemberPath + "] is required as it is a part of the key definition.");
            }
        }

        if (!resultFieldIndices.keySet().equals(usedPaths)) {
            final var declaredProps = new TreeSet<>(resultFieldIndices.keySet());
            declaredProps.removeAll(usedPaths); // compute the diff between the declared and used.
            throw new DataMigrationException("Used and declared props are different. The following props are specified but not used: " + declaredProps);
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

    public static List<Integer> produceKeyFieldsIndices(final Class<? extends AbstractEntity<?>> entityType, final Map<String, Integer> resultFieldIndices) {
        return new ArrayList<>(obtainIndices(keyPaths(entityType), resultFieldIndices).values());
    }

    public static Object transformValue(final Class<?> type, final List<Object> values, final IdCache cache) {
        if (!isPersistentEntityType(type)) {
            return values.getFirst();
        } else {
            final Map<Object, Long> cacheForType = cache.getCacheForType((Class<? extends AbstractEntity<?>>) type);
            final Object entityKeyObject = values.size() == 1 ? values.getFirst() : values;
            final Long result = cacheForType.get(entityKeyObject);
            if (values.size() == 1 && values.getFirst() != null && result == null) {
                System.out.println("           !!! can't find id for " + type.getSimpleName() + " with key: [" + values.getFirst() + "]");
            }
            if (values.size() > 1 && !containsOnlyNull(values) && result == null) {
                System.out.println("           !!! can't find id for " + type.getSimpleName() + " with key: " + values);
            }

            return result;
        }
    }

    private static boolean containsOnlyNull(final List<Object> values) {
        for (final Object object : values) {
            if (object != null) {
                return false;
            }
        }
        return true;
    }

}
