package ua.com.fielden.platform.migration;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import jakarta.inject.Singleton;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.meta.EntityMetadata;
import ua.com.fielden.platform.meta.IDomainMetadata;
import ua.com.fielden.platform.meta.PropertyMetadata;
import ua.com.fielden.platform.types.markers.IUtcDateTimeType;
import ua.com.fielden.platform.utils.EntityUtils;

import javax.annotation.Nullable;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Stream;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static ua.com.fielden.platform.entity.AbstractEntity.*;
import static ua.com.fielden.platform.meta.PropertyMetadataKeys.REQUIRED;
import static ua.com.fielden.platform.utils.CollectionUtil.setOf;
import static ua.com.fielden.platform.utils.EntityUtils.isPersistentEntityType;

@Singleton
final class MigrationUtils {
    private static final Set<String> PROPS_TO_IGNORE = setOf(ID, VERSION);

    private final IDomainMetadata domainMetadata;

    @Inject
    MigrationUtils(final IDomainMetadata domainMetadata) {
        this.domainMetadata = domainMetadata;
    }

    public EntityMd generateEntityMd(final Class<? extends AbstractEntity<?>> entityType) {
        final var entityMetadata = domainMetadata.forEntity(entityType).asPersistent()
                .orElseThrow(() -> new DataMigrationException("Unable to generate a retriever job for non-persistent entity [%s].".formatted(entityType.getSimpleName())));
        final var tableName = entityMetadata.data().tableName();
        final var pmUtils = domainMetadata.propertyMetadataUtils();
        final var propMds = entityMetadata.properties().stream()
                .filter(pm -> !PROPS_TO_IGNORE.contains(pm.name()))
                .map(PropertyMetadata::asPersistent).flatMap(Optional::stream)
                .flatMap(pm -> Optional.<Stream<PropMd>>empty()
                        // Component-typed property: expand into components, unless there is just one component.
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
                                    // Special case: component-typed property with a single component.
                                    // Do not expand into sub-properties so that this property can be specified in retrievers by itself.
                                    // E.g., `money` instead of `money.amount`.
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
        final var leaves = domainMetadata.propertyMetadataUtils().isPropEntityType(prop, EntityMetadata::isPersistent)
                ? keyPaths(prop).stream().map(s -> combinePath(parentProp, s)).toList()
                : List.of(name);
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
            return keyPaths(null, keyMembers);
        }
    }

    private List<String> keyPaths(final PropertyMetadata pm) {
        return pm.type().asEntity().map(et -> {
            final var keyMembers = domainMetadata.entityMetadataUtils().compositeKeyMembers(domainMetadata.forEntity(et.javaType()));
            if (keyMembers.isEmpty()) {
                return List.of(pm.name());
            } else {
                return keyPaths(pm, keyMembers);
            }
        }).orElseGet(List::of);
    }

    private List<String> keyPaths(
            final @Nullable PropertyMetadata parentProp,
            final List<PropertyMetadata> keyMembers)
    {
        return keyMembers.stream()
                .map(km -> km.type().asEntity()
                        .flatMap(et -> domainMetadata.forEntityOpt(et.javaType()))
                        .map(em -> switch (em) {
                            case EntityMetadata.Union union -> domainMetadata.entityMetadataUtils()
                                    .unionMembers(union)
                                    .stream()
                                    .map(this::keyPaths)
                                    .flatMap(Collection::stream)
                                    .collect(toImmutableList());
                            case EntityMetadata.Persistent $ -> keyPaths(km);
                            default -> ImmutableList.of(km.name());
                        })
                        .orElseGet(() -> ImmutableList.of(km.name())))
                .flatMap(Collection::stream)
                .map(s -> parentProp == null ? s : parentProp.name() + "." + s)
                .toList();
    }

    public List<PropInfo> produceContainers(
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

    public List<Integer> produceKeyFieldsIndices(final Class<? extends AbstractEntity<?>> entityType, final Map<String, Integer> resultFieldIndices) {
        return new ArrayList<>(obtainIndices(keyPaths(entityType), resultFieldIndices).values());
    }

    public Object transformValue(final Class<?> type, final List<Object> values, final IdCache cache) {
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
