package ua.com.fielden.platform.web.resources.webui;

import org.apache.log4j.Logger;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.dao.IEntityProducer;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.entity.fetch.IEntityFetchStrategy;
import ua.com.fielden.platform.web.resources.RestServerUtil;

/**
 * This utility class contains the methods that are shared across {@link EntityResource} and {@link EntityValidationResource}.
 *
 * @author TG Team
 *
 */
public class EntityResourceMixin<T extends AbstractEntity<?>> {
    private final EntityFactory entityFactory;
    private final Logger logger = Logger.getLogger(getClass());
    private final Class<T> entityType;
    private final IEntityFetchStrategy<T> fetchStrategy;
    private final IEntityDao<T> dao;
    private final IEntityProducer<T> entityProducer;
    private final ICompanionObjectFinder companionFinder;

    public EntityResourceMixin(final Class<T> entityType, final IEntityProducer<T> entityProducer, final EntityFactory entityFactory, final RestServerUtil restUtil, final ICompanionObjectFinder companionFinder) {
        this.entityType = entityType;
        this.companionFinder = companionFinder;
        this.dao = companionFinder.<IEntityDao<T>, T> find(this.entityType);

        this.fetchStrategy = this.dao.getFetchStrategy();
        this.entityFactory = entityFactory;
        this.entityProducer = entityProducer;
    }

    /**
     * Initialises the entity for retrieval.
     *
     * @param id
     *            -- entity identifier
     * @return
     */
    public T createEntityForRetrieval(final Long id) {
        final T entity;
        if (id != null) {
            entity = dao.findById(id, fetchStrategy.fetchModel());
        } else {
            entity = entityProducer.newEntity();
        }
        return entity;
    }

    public Class<T> getEntityType() {
        return entityType;
    }

    public ICompanionObjectFinder getCompanionFinder() {
        return companionFinder;
    }

    public IEntityFetchStrategy<T> getFetchStrategy() {
        return fetchStrategy;
    }
}
