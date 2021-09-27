package ua.com.fielden.platform.migration;

import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.from;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.entity.query.metadata.DomainMetadataAnalyser;
import ua.com.fielden.platform.reflection.Finder;

public class IdCache {
    private final Map<Class<?>, Map<Object, Long>> cache = new HashMap<>();
    private final DomainMetadataAnalyser dma;
    private final ICompanionObjectFinder coFinder;

    public IdCache(final ICompanionObjectFinder coFinder, final DomainMetadataAnalyser dma) {
        this.coFinder = coFinder;
        this.dma = dma;
    }

    protected void registerCacheForType(final Class<? extends AbstractEntity<?>> entityType) {
        if (!cache.containsKey(entityType)) {
            cache.put(entityType, new HashMap<Object, Long>());
        }
    }

    protected Map<Object, Long> getCacheForType(final Class<? extends AbstractEntity<?>> entityType) {
        if (!cache.containsKey(entityType)) {
            cache.put(entityType, retrieveData(entityType));
        }

        return cache.get(entityType);
    }

    private static SortedSet<String> getKeyFields(final Class<? extends AbstractEntity<?>> entityType, final DomainMetadataAnalyser dma) {
        final List<String> keyMembersFirstLevelProps = Finder.getFieldNames(Finder.getKeyMembers(entityType));
        return new TreeSet<>(dma.getLeafPropsFromFirstLevelProps(null, entityType, new HashSet<>(keyMembersFirstLevelProps)));
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

    private Map<Object, Long> retrieveData(final Class<? extends AbstractEntity<?>> entityType) {
        final IEntityDao co = coFinder.find(entityType);

        final Map<Object, Long> result = new HashMap<>();
        final List<AbstractEntity<?>> entities;
        try {
            entities = co.getAllEntities(from(select(entityType).model()).model());
        } catch (final Exception ex) {
            System.out.println("Exception in retrieveData(" + entityType.getName() + ")");
            throw ex;
        }

        final SortedSet<String> keyFields = getKeyFields(entityType, dma);
        for (final AbstractEntity<?> abstractEntity : entities) {
            result.put(prepareValueForCache(abstractEntity, keyFields), abstractEntity.getId());
        }

        return result;
    }
}