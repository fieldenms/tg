package ua.com.fielden.platform.migration;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.meta.EntityMetadata;
import ua.com.fielden.platform.meta.IDomainMetadata;
import ua.com.fielden.platform.meta.PropertyMetadata;
import ua.com.fielden.platform.types.markers.IUtcDateTimeType;
import ua.com.fielden.platform.types.tuples.T2;
import ua.com.fielden.platform.utils.EntityUtils;

import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Stream;

import static java.util.Collections.unmodifiableList;
import static java.util.stream.Collectors.toList;
import static ua.com.fielden.platform.entity.AbstractEntity.*;
import static ua.com.fielden.platform.eql.meta.EntityTypeInfo.getEntityTypeInfo;
import static ua.com.fielden.platform.meta.PropertyMetadataKeys.REQUIRED;
import static ua.com.fielden.platform.reflection.EntityMetadata.keyTypeInfo;
import static ua.com.fielden.platform.utils.CollectionUtil.setOf;
import static ua.com.fielden.platform.utils.EntityUtils.isPersistedEntityType;

// TODO This class uses both typeful metadata and reflection + EntityTypeInfo. The latter should be replaced.
public class MigrationUtils {
    private static final Set<String> PROPS_TO_IGNORE = setOf(ID, VERSION);

    public static EntityMd generateEntityMd(final String tableName, final Collection<PropertyMetadata> pms,
                                            final IDomainMetadata domainMetadata) {
        final var pmUtils = domainMetadata.propertyMetadataUtils();
        final var propMds = pms.stream()
                .filter(pm -> !pm.type().isCollectional())
                .flatMap(pm -> switch (pm) {
                    case PropertyMetadata.Persistent ppm when !PROPS_TO_IGNORE.contains(ppm.name()) -> {
                        final var leafs = pmUtils.isPropEntityType(ppm, EntityMetadata::isPersistent)
                                ? keyPaths(ppm, domainMetadata)
                                : List.of(pm.name());
                        yield Stream.of(new PropMd(ppm.name(), ppm.type().javaType(), ppm.data().column().name,
                                                   ppm.is(REQUIRED), pm.hibType() instanceof IUtcDateTimeType, leafs));
                    }
                    default -> {
                        final boolean required = !pmUtils.isPropEntityType(pm, EntityMetadata::isUnion) && pm.is(REQUIRED);
                        yield pmUtils.subProperties(pm).stream()
                                .map(PropertyMetadata::asPersistent).flatMap(Optional::stream)
                                .map(spm -> {
                                    final var leafs = pmUtils.isPropEntityType(spm, EntityMetadata::isPersistent)
                                            ? keyPaths(spm, domainMetadata).stream().map(s -> pm.name() + "." + s).toList()
                                            : List.of(pm.name() + "." + spm.name());
                                    return new PropMd(pm.name() + "." + spm.name(), spm.type().javaType(),
                                                      spm.data().column().name, required,
                                                      spm.hibType() instanceof IUtcDateTimeType, leafs);
                                });
                    }
                }).toList();

        return new EntityMd(tableName, propMds);
    }

    public static List<String> keyPaths(final PropertyMetadata pm, final IDomainMetadata domainMetadata) {
        return pm.type().asEntity().map(et -> {
            final var keyMembers = domainMetadata.entityMetadataUtils().keyMembers(domainMetadata.forEntity(et.javaType()));
            if (keyMembers.isEmpty()) {
                return List.of(pm.name());
            } else {
                return keyMembers.stream().flatMap(km -> {
                    if (domainMetadata.propertyMetadataUtils().isPropEntityType(km, EntityMetadata::isPersistent)) {
                        return keyPaths(km, domainMetadata).stream();
                    } else {
                        return Stream.of(km.name());
                    }
                }).map(s -> pm.name() + "." + s).toList();
            }
        }).orElseGet(List::of);
    }

    // TODO migrate to typeful metadata
    public static <ET extends AbstractEntity<?>> List<String> keyPaths(final String propName, final Class<ET> et) {
        final List<String> result = new ArrayList<>();
        final List<T2<String, Class<?>>> keyMembers = getEntityTypeInfo(et).compositeKeyMembers;
        if (keyMembers.isEmpty()) {
            result.add(propName);
        } else {
            for (final T2<String, Class<?>> keyMember : keyMembers) {
                if (!EntityUtils.isPersistedEntityType(keyMember._2)) {
                    result.add(propName + "." + keyMember._1);
                } else {
                    result.addAll(keyPaths(propName + "." + keyMember._1, (Class<? extends AbstractEntity<?>>) keyMember._2));
                }
            }
        }
        return unmodifiableList(result);
    }

    public static List<String> keyPaths(final Class<? extends AbstractEntity<?>> entityType, final IDomainMetadata domainMetadata) {
        final EntityMetadata em = domainMetadata.forEntity(entityType);
        final var keyMembers = domainMetadata.entityMetadataUtils().keyMembers(em);
        if (keyMembers.isEmpty()) {
            if (EntityUtils.isOneToOne(entityType)) {
                return keyPaths(em.property(KEY), domainMetadata);
            } else {
                return List.of(KEY);
            }
        } else {
            return keyMembers.stream().flatMap(km -> {
                if (domainMetadata.propertyMetadataUtils().isPropEntityType(km, EntityMetadata::isPersistent)) {
                    return keyPaths(km, domainMetadata).stream();
                } else {
                    return Stream.of(km.name());
                }
            }).toList();
        }
    }

    // TODO migrate to typeful metadata
    public static <ET extends AbstractEntity<?>> List<String> keyPaths(final Class<ET> et) {
        final var result = new ArrayList<String>();
        final var keyMembers = getEntityTypeInfo(et).compositeKeyMembers;
        if (keyMembers.isEmpty()) {
            if (EntityUtils.isOneToOne(et)) {
                result.addAll(keyPaths(KEY, (Class<ET>) keyTypeInfo(et)));
            } else {
                result.add(KEY);
            }
        } else {
            for (final T2<String, Class<?>> keyMember : keyMembers) {
                if (!EntityUtils.isPersistedEntityType(keyMember._2)) {
                    result.add(keyMember._1);
                } else {
                    result.addAll(keyPaths(keyMember._1, (Class<? extends AbstractEntity<?>>) keyMember._2));
                }
            }
        }
        return unmodifiableList(result);
    }

    public static List<PropInfo> produceContainers(final List<PropMd> props, final List<String> keyMemberPaths, final Map<String, Integer> retrieverResultFields, final boolean updater) {
        final var result = new ArrayList<PropInfo>();

        final var usedPaths = new HashSet<String>();
        for (final PropMd propMd : props) {
            final var indices = obtainIndices(propMd.leafProps(), retrieverResultFields);
            // need to determine incomplete mapping for key members of entity property
            // if the number of null values doesn't match the number of indices then mapping is incomplete
            final long countOfNullValuedIndices = indices.values().stream().filter(Objects::isNull).count();
            if (countOfNullValuedIndices > 0 && countOfNullValuedIndices != indices.size()) {
                throw new DataMigrationException("Mapping for prop [" + propMd.name() + "] does not have all its members specified: " + indices.entrySet().stream().filter(entry -> entry.getValue() == null).map(Entry::getKey).collect(toList()));
            } else if (!indices.values().contains(null)) {
                result.add(new PropInfo(propMd.name(), propMd.type(), propMd.column(), propMd.utcType(), new ArrayList<>(indices.values())));
                usedPaths.addAll(propMd.leafProps());
            } else if (propMd.required() && !updater) {
                throw new DataMigrationException("prop [" + propMd.name() + "] is required");
            }
        }

        for (final var keyMemberPath : keyMemberPaths) {
            if (!retrieverResultFields.containsKey(keyMemberPath)) {
                throw new DataMigrationException("Sql mapping for property [" + keyMemberPath + "] is required as it is a part of the key definition.");
            }
        }

        if (!retrieverResultFields.keySet().equals(usedPaths)) {
            final var declaredProps = new TreeSet<>(retrieverResultFields.keySet());
            declaredProps.removeAll(usedPaths); // compute the diff between the declared and used.
            throw new DataMigrationException("Used and declared props are different. The following props are specified but not used: " + declaredProps);
        }

        return unmodifiableList(result);
    }

    private static LinkedHashMap<String, Integer> obtainIndices(final List<String> leafProps, final Map<String, Integer> retrieverResultFields) {
        final var result = new LinkedHashMap<String, Integer>();
        for (final var lp : leafProps) {
            result.put(lp, retrieverResultFields.get(lp));
        }
        return result;
    }

    public static List<Integer> produceKeyFieldsIndices(final Class<? extends AbstractEntity<?>> entityType, final Map<String, Integer> retrieverResultFields) {
        return new ArrayList<>(obtainIndices(keyPaths(entityType), retrieverResultFields).values());
    }

    public static Object transformValue(final Class<?> type, final List<Object> values, final IdCache cache) {
        if (!isPersistedEntityType(type)) {
            return values.get(0);
        } else {
            final Map<Object, Long> cacheForType = cache.getCacheForType((Class<? extends AbstractEntity<?>>) type);
            final Object entityKeyObject = values.size() == 1 ? values.get(0) : values;
            final Long result = cacheForType.get(entityKeyObject);
            if (values.size() == 1 && values.get(0) != null && result == null) {
                System.out.println("           !!! can't find id for " + type.getSimpleName() + " with key: [" + values.get(0) + "]");
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
