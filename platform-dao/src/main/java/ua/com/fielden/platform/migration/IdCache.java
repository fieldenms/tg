package ua.com.fielden.platform.migration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import ua.com.fielden.platform.dao.DomainMetadataAnalyser;
import ua.com.fielden.platform.dao.DynamicEntityDao;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.reflection.Finder;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.from;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;

public class IdCache {
    private final Map<Class<?>, Map<Object, Integer>> cache = new HashMap<Class<?>, Map<Object, Integer>>();
    private final DomainMetadataAnalyser dma;
    private DynamicEntityDao dynamicDao;

    public IdCache(final DynamicEntityDao dynamicDao, final DomainMetadataAnalyser dma) {
        this.dynamicDao = dynamicDao;
        this.dma = dma;
    }

    protected void registerCacheForType(final Class<? extends AbstractEntity<?>> entityType) {
        if (!cache.containsKey(entityType)) {
            cache.put(entityType, new HashMap<Object, Integer>());
        }
    }

    protected Map<Object, Integer> getCacheForType(final Class<? extends AbstractEntity<?>> entityType) throws Exception {
        if (!cache.containsKey(entityType)) {
            cache.put(entityType, retrieveData(entityType));
        }

        return cache.get(entityType);
    }

    private SortedSet<String> getKeyFields(final Class<? extends AbstractEntity<?>> entityType) {
        final List<String> keyMembersFirstLevelProps = Finder.getFieldNames(Finder.getKeyMembers(entityType));
        return new TreeSet<String>(dma.getLeafPropsFromFirstLevelProps(null, entityType, new HashSet<String>(keyMembersFirstLevelProps)));
    }

    private Object prepareValueForCache(final AbstractEntity<?> entity, final SortedSet<String> fields) {
        if (fields.size() == 1) {
            return entity.getKey();
        } else {
            final List<Object> result = new ArrayList<>();
            for (final String field : fields) {
                result.add(entity.get(field).toString());
            }
            return result;
        }
    }

    private Map<Object, Integer> retrieveData(final Class<? extends AbstractEntity<?>> entityType) throws Exception {
        final Map<Object, Integer> result = new HashMap<>();
        dynamicDao.setEntityType(entityType);
        final List<AbstractEntity> entities = dynamicDao.getAllEntities(from(select(entityType).model()).model());

        final SortedSet<String> keyFields = getKeyFields(entityType);
        for (final AbstractEntity abstractEntity : entities) {
            result.put(prepareValueForCache(abstractEntity, keyFields), abstractEntity.getId().intValue());
        }

        return result;
    }
}