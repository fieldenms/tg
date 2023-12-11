package ua.com.fielden.platform.migration;

import static java.util.Collections.unmodifiableList;
import static java.util.stream.Collectors.toList;
import static ua.com.fielden.platform.entity.AbstractEntity.ID;
import static ua.com.fielden.platform.entity.AbstractEntity.KEY;
import static ua.com.fielden.platform.entity.AbstractEntity.VERSION;
import static ua.com.fielden.platform.eql.meta.EntityTypeInfo.getEntityTypeInfo;
import static ua.com.fielden.platform.utils.CollectionUtil.setOf;
import static ua.com.fielden.platform.utils.CollectionUtil.unmodifiableListOf;
import static ua.com.fielden.platform.utils.EntityUtils.isPersistedEntityType;
import static ua.com.fielden.platform.utils.EntityUtils.isUnionEntityType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.eql.meta.EqlPropertyMetadata;
import ua.com.fielden.platform.reflection.EntityMetadata;
import ua.com.fielden.platform.types.markers.IUtcDateTimeType;
import ua.com.fielden.platform.types.tuples.T2;
import ua.com.fielden.platform.utils.EntityUtils;

public class MigrationUtils {
    private static final Set<String> PROPS_TO_IGNORE = setOf(ID, VERSION);

    public static final EntityMd generateEntityMd(final String tableName, final Collection<EqlPropertyMetadata> propsMetadatas) {
        final var props = new ArrayList<PropMd>();
        for (final EqlPropertyMetadata el : propsMetadatas) {
            if (el.column != null && !PROPS_TO_IGNORE.contains(el.name)) {
                props.add(new PropMd(el.name, el.javaType, el.column.name, el.required,
                        el.hibType instanceof IUtcDateTimeType,
                        isPersistedEntityType(el.javaType)
                                ? keyPaths(el.name, (Class<? extends AbstractEntity<?>>) el.javaType)
                                : unmodifiableListOf(el.name)));
            } else if (!el.subitems.isEmpty()) {
                for (final EqlPropertyMetadata subitem : el.subitems) {
                    if (subitem.expressionModel == null) {
                        props.add(new PropMd(el.name + "." + subitem.name, subitem.javaType, subitem.column.name, isUnionEntityType(el.javaType) ? false : el.required,
                                subitem.hibType instanceof IUtcDateTimeType,
                                isPersistedEntityType(subitem.javaType)
                                        ? keyPaths(el.name + "." + subitem.name, (Class<? extends AbstractEntity<?>>) subitem.javaType)
                                        : unmodifiableListOf(el.name + "." + subitem.name)));
                    }
                }
            }

        }
        return new EntityMd(tableName, unmodifiableList(props));
    }

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

    public static <ET extends AbstractEntity<?>> List<String> keyPaths(final Class<ET> et) {
        final var result = new ArrayList<String>();
        final var keyMembers = getEntityTypeInfo(et).compositeKeyMembers;
        if (keyMembers.isEmpty()) {
            if (EntityUtils.isOneToOne(et)) {
                result.addAll(keyPaths(KEY, (Class<ET>) EntityMetadata.keyTypeInfo(et)));
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