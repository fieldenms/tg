package ua.com.fielden.platform.migration;

import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.from;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;

public class IdCache {
    private final Map<Class<?>, Map<Object, Long>> cache = new HashMap<>();
    private final ICompanionObjectFinder coFinder;

    public IdCache(final ICompanionObjectFinder coFinder) {
        this.coFinder = coFinder;
    }

    protected void registerCacheForType(final Class<? extends AbstractEntity<?>> entityType) {
        if (!cache.containsKey(entityType)) {
            cache.put(entityType, new HashMap<>());
        }
    }

    protected Map<Object, Long> getCacheForType(final Class<? extends AbstractEntity<?>> entityType) {
        if (!cache.containsKey(entityType)) {
            cache.put(entityType, retrieveData(entityType));
        }

        return cache.get(entityType);
    }

    private Object prepareValueForCache(final AbstractEntity<?> entity, final List<String> fields) {
        if (fields.size() == 1) {
            return entity.get(fields.get(0));
        } else {
            final List<Object> result = new ArrayList<>();
            for (final String field : fields) {
                result.add(entity.get(field));
            }
            return result;
        }
    }

    private <ET extends AbstractEntity<?>> Map<Object, Long> retrieveData(final Class<ET> entityType) {
        final IEntityDao<ET> co = coFinder.find(entityType);

        final Map<Object, Long> result = new HashMap<>();
        final List<ET> entities;
        try {
            entities = co.getAllEntities(from(select(entityType).model()).model());
        } catch (final Exception ex) {
            System.out.println("Exception in retrieveData(" + entityType.getName() + ")");
            throw ex;
        }

        final List<String> keyFields = MigrationUtils.keyPaths(entityType);

        for (final AbstractEntity<?> abstractEntity : entities) {
            result.put(prepareValueForCache(abstractEntity, keyFields), abstractEntity.getId());
        }

        return result;
    }
}
