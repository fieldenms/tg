package ua.com.fielden.platform.dao;

import org.apache.commons.lang.StringUtils;

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
public class DefaultEntityProducerWithContext<T extends AbstractEntity<?>, C extends AbstractEntity<?>> implements IEntityProducer<T> {

    private final EntityFactory factory;
    protected final Class<T> entityType;
    private final IEntityDao<T> companion;
    // optional centre context for context-dependent entity producing logic
    private CentreContext<C, AbstractEntity<?>> centreContext;
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

        // TODO Assignment of context and chosen property below breaks execution of compound masters
        //      Currently it is not know as to why this is the case.
//        if (entity instanceof AbstractFunctionalEntityWithCentreContext) {
//            final AbstractFunctionalEntityWithCentreContext<?> funEntity = (AbstractFunctionalEntityWithCentreContext<?>) entity;
//            
//            if (getCentreContext() != null) {
//                funEntity.setContext(getCentreContext());
//            }
//            
//            if (!StringUtils.isEmpty(getChosenProperty())) {
//                funEntity.setChosenProperty(getChosenProperty());
//            }
//        }
        
        if (companion != null) {
            provideProxies(entity, companion.getFetchProvider());
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

    /**
     * Use this method in case when the centre context is required for entity instantiation.
     *
     * @return
     */
    protected CentreContext<C, AbstractEntity<?>> getCentreContext() {
        return centreContext;
    }

    public void setCentreContext(final CentreContext<C, AbstractEntity<?>> centreContext) {
        this.centreContext = centreContext;
    }

    /**
     * Use this method in case when the chosen property is required for entity instantiation.
     *
     * @return
     */
    protected String getChosenProperty() {
        return chosenProperty;
    }

    public void setChosenProperty(final String chosenProperty) {
        this.chosenProperty = chosenProperty;
    }

    public Long getCompoundMasterEntityId() {
        return compoundMasterEntityId;
    }

    public void setCompoundMasterEntityId(final Long compoundMasterEntityId) {
        this.compoundMasterEntityId = compoundMasterEntityId;
    }
}
