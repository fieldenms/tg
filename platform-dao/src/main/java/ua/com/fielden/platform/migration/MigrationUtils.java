package ua.com.fielden.platform.migration;

import static ua.com.fielden.platform.entity.AbstractEntity.ID;
import static ua.com.fielden.platform.entity.AbstractEntity.KEY;
import static ua.com.fielden.platform.entity.AbstractEntity.VERSION;
import static ua.com.fielden.platform.entity.query.metadata.EntityTypeInfo.getEntityTypeInfo;
import static ua.com.fielden.platform.utils.CollectionUtil.listOf;
import static ua.com.fielden.platform.utils.EntityUtils.isPersistedEntityType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.eql.meta.EqlPropertyMetadata;
import ua.com.fielden.platform.reflection.EntityMetadata;
import ua.com.fielden.platform.types.markers.IUtcDateTimeType;
import ua.com.fielden.platform.types.tuples.T2;
import ua.com.fielden.platform.utils.EntityUtils;

public class MigrationUtils {

    public static final EntityMd generateEntityMd(final String tableName, final List<EqlPropertyMetadata> propsMetadatas) {
        final List<PropMd> props = new ArrayList<>();
        for (final EqlPropertyMetadata el : propsMetadatas) {

            if (el.column != null && !el.name.equals(ID) && !el.name.equals(VERSION)) {
                props.add(new PropMd(el.name, el.javaType, el.column.name, el.required, 
                        el.hibType instanceof IUtcDateTimeType,
                        isPersistedEntityType(el.javaType)
                                ? keyPathes(el.name, (Class<? extends AbstractEntity<?>>) el.javaType)
                                : listOf(el.name)));
            } else if (!el.subitems().isEmpty()) {
                for (final EqlPropertyMetadata subitem : el.subitems()) {
                    if (subitem.expressionModel == null) {
                        props.add(new PropMd(el.name + "." + subitem.name, subitem.javaType, subitem.column.name, subitem.required, 
                                subitem.hibType instanceof IUtcDateTimeType, 
                                isPersistedEntityType(subitem.javaType)
                                        ? keyPathes(el.name + "." + subitem.name, (Class<? extends AbstractEntity<?>>) subitem.javaType)
                                        : listOf(el.name + "." + subitem.name)));
                    }
                }
            }

        }

        return new EntityMd(tableName, props);
    }

    public static <ET extends AbstractEntity<?>> List<String> keyPathes(final String propName, final Class<ET> et) {
        final List<String> result = new ArrayList<>();
        final List<T2<String, Class<?>>> keyMembers = getEntityTypeInfo(et).compositeKeyMembers;
        if (keyMembers.isEmpty()) {
            result.add(propName);
        } else {
            for (final T2<String, Class<?>> keyMember : keyMembers) {
                if (!EntityUtils.isPersistedEntityType(keyMember._2)) {
                    result.add(propName + "." + keyMember._1);
                } else {
                    result.addAll(keyPathes(propName + "." + keyMember._1, (Class<? extends AbstractEntity<?>>) keyMember._2));
                }
            }
        }
        return result;

    }
    
    public static <ET extends AbstractEntity<?>> List<String> keyPathes(final Class<ET> et) {
        final List<String> result = new ArrayList<>();
        final List<T2<String, Class<?>>> keyMembers = getEntityTypeInfo(et).compositeKeyMembers;
        if (keyMembers.isEmpty()) {
            if (EntityUtils.isOneToOne(et)) {
                result.addAll(keyPathes(KEY, (Class<ET>) EntityMetadata.keyTypeInfo(et)));
            } else {
                result.add(KEY);
            }
        } else {
            for (final T2<String, Class<?>> keyMember : keyMembers) {
                if (!EntityUtils.isPersistedEntityType(keyMember._2)) {
                    result.add(keyMember._1);
                } else {
                    result.addAll(keyPathes(keyMember._1, (Class<? extends AbstractEntity<?>>) keyMember._2));
                }
            }
        }
        return result;
    }

    public static List<PropInfo> produceContainers(final List<PropMd> props, final List<String> keyMemberPaths, final Map<String, Integer> retrieverResultFields, final boolean updater) {
        // TODO need to ensure that all retrieverResultFields are consumed 
        final List<PropInfo> result = new ArrayList<>();

        for (final PropMd propMd : props) {
            final List<Integer> indices = obtainIndices(propMd.leafProps, retrieverResultFields);
            if (!indices.contains(null)) {
                result.add(new PropInfo(propMd.name, propMd.type, propMd.column, propMd.utcType, indices));
            } else if (propMd.required && !updater) {
                throw new IllegalStateException("prop " + propMd.name + " is required");
            }
        }

        for (String keyMemberPath : keyMemberPaths) {
            if (!retrieverResultFields.containsKey(keyMemberPath)) {
                throw new IllegalStateException("Sql mapping for property [" + keyMemberPath + "] is required (as it is part of key definition).");
            }
        }
        
        return result;
    }

    private static List<Integer> obtainIndices(final List<String> leafProps, final Map<String, Integer> retrieverResultFields) {
        final List<Integer> result = new ArrayList<>();
        for (final String lp : leafProps) {
            result.add(retrieverResultFields.get(lp));
        }
        return result;
    }

    public static List<Integer> produceKeyFieldsIndices(final Class<? extends AbstractEntity<?>> entityType, final Map<String, Integer> retrieverResultFields) {
        return obtainIndices(keyPathes(entityType), retrieverResultFields);
    }

    public static Object transformValue(final Class<?> type, final List<Object> values, final IdCache cache) {
        if (!isPersistedEntityType(type)) {
            return values.get(0);
        } else {
            final Map<Object, Long> cacheForType = cache.getCacheForType((Class<? extends AbstractEntity<?>>) type);
            final Object entityKeyObject = values.size() == 1 ? values.get(0) : values;
            return cacheForType.get(entityKeyObject);
        }
    }
}