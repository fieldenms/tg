package ua.com.fielden.platform.dao;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.AbstractFunctionalEntityWithCentreContext;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.entity.fetch.IFetchProvider;
import ua.com.fielden.platform.web.centre.CentreContext;

/**
 * Provides default {@link EntityFactory} based implementation for creation of new entity instances.
 *
 * @author TG Team
 *
 * @param <T>
 */
public class DefaultEntityProducerWithContext<T extends AbstractEntity<?>> implements IEntityProducer<T> {

    private final EntityFactory factory;
    protected final Class<T> entityType;
    private final IEntityDao<T> companion;
    // optional centre context for context-dependent entity producing logic
    private CentreContext<? extends AbstractEntity<?>, AbstractEntity<?>> centreContext;
    private AbstractEntity<?> masterEntity;
    private Long compoundMasterEntityId;
    private String chosenProperty;

    public DefaultEntityProducerWithContext(final EntityFactory factory, final Class<T> entityType) {
        this(factory, entityType, null);
    }

    public DefaultEntityProducerWithContext(final EntityFactory factory, final Class<T> entityType, final ICompanionObjectFinder companionFinder) {
        this.factory = factory;
        this.entityType = entityType;
        this.companion = companionFinder == null ? null : companionFinder.<IEntityDao<T>, T> find(this.entityType);
    }

    @Override
    public final T newEntity() {
        final T entity = factory.newEntity(entityType);
        
        if (companion != null) {
            provideProxies(entity, companion.getFetchProvider());
        }
        
        if (entity instanceof AbstractFunctionalEntityWithCentreContext) {
            final AbstractFunctionalEntityWithCentreContext<?> funcEntity = (AbstractFunctionalEntityWithCentreContext<?>) entity;
            
            if (centreContext != null) {
                funcEntity.setContext(centreContext);
            }
            
            if (chosenProperty != null) {
                funcEntity.setChosenProperty(chosenProperty);
            }
            
            if (String.class.isAssignableFrom(entity.getKeyType())) {
                ((AbstractFunctionalEntityWithCentreContext<String>) funcEntity).setKey("dummy");
            }
            // resetting of meta-state makes the functional entity not dirty for the properties, changed above. This is important not to treat them as changed when going to client application.
            funcEntity.resetMetaState();
        }
        
        return provideDefaultValues(entity);
    }

    /**
     * Provides <code>entity</code>'s proxies for the properties which do not take part in <code>fetchStrategy</code>.
     *
     * @param entity
     * @param fetchStrategy
     */
    private T provideProxies(final T entity, final IFetchProvider<T> fetchStrategy) {
        // TODO implement automatic "proxying" for the properties, which do not take part in fetchStrategy --
        // -- it provides consistency between newly created entities and fetched entities and also reduces the
        // size of the JSON data transmitting from server to the client
        return entity;
    }

    /**
     * Provides domain-driven <code>entity</code>'s default values for the properties.
     *
     * @param entity
     */
    protected T provideDefaultValues(final T entity) {
        return entity;
    }

    protected EntityFactory factory() {
        return factory;
    }

    protected IEntityDao<T> companion() {
        return companion;
    }

    /**
     * Use this method in case when the master context (as functional entity) is required for entity instantiation.
     *
     * @return
     */
    protected AbstractEntity<?> getMasterEntity() {
        return masterEntity;
    }

    public void setMasterEntity(final AbstractEntity<?> masterEntity) {
        this.masterEntity = masterEntity;
    }

    public void setCentreContext(final CentreContext<? extends AbstractEntity<?>, AbstractEntity<?>> centreContext) {
        this.centreContext = centreContext;
    }

    public void setChosenProperty(final String chosenProperty) {
        this.chosenProperty = chosenProperty;
    }

    protected Long getCompoundMasterEntityId() {
        return compoundMasterEntityId;
    }

    public void setCompoundMasterEntityId(final Long compoundMasterEntityId) {
        this.compoundMasterEntityId = compoundMasterEntityId;
    }
}
