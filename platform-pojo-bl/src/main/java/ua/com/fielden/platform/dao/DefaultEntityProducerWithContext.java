package ua.com.fielden.platform.dao;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.AbstractFunctionalEntityWithCentreContext;
import ua.com.fielden.platform.entity.EntityEditAction;
import ua.com.fielden.platform.entity.EntityNewAction;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
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
    
    // optional centre context for context-dependent entity producing logic
    private CentreContext<? extends AbstractEntity<?>, AbstractEntity<?>> centreContext;
    private Long compoundMasterEntityId;
    private String chosenProperty;

    private ICompanionObjectFinder coFinder;
    private final Map<Class<? extends AbstractEntity<?>>, IEntityDao<?>> coCache = new HashMap<>();

    public DefaultEntityProducerWithContext(final EntityFactory factory, final Class<T> entityType, final ICompanionObjectFinder companionFinder) {
        this.factory = factory;
        this.entityType = entityType;
        this.coFinder = companionFinder;
    }

    
    /**
     * A convenient way to obtain companion instances by the types of corresponding entities.
     * 
     * @param type -- entity type whose companion instance needs to be obtained
     * @return
     */
    @SuppressWarnings("unchecked")
    public <C extends IEntityDao<E>, E extends AbstractEntity<?>> C co(final Class<E> type) {
        IEntityDao<?> co = coCache.get(type);
        if (co == null) {
            co = coFinder.find(type);
            coCache.put(type, co);
        }
        return (C) co;
    }

    
    @Override
    public final T newEntity() {
        final T producedEntity;
        if (getMasterEntity() != null && EntityEditAction.class.isAssignableFrom(getMasterEntity().getClass())) {
            final EntityEditAction entityEditAction = (EntityEditAction) getMasterEntity();
            final Long editedEntityId = Long.valueOf(entityEditAction.getEntityId());
            producedEntity = provideDefaultValuesForStandardEdit(editedEntityId, entityEditAction);
        } else {
            final IEntityDao<T> companion = co(this.entityType);
            final T entity;
            if (companion != null) {
                entity = companion.new_();
            } else {
                entity = factory().newEntity(this.entityType);
            }
            
            if (entity instanceof AbstractFunctionalEntityWithCentreContext) {
                final AbstractFunctionalEntityWithCentreContext<?> funcEntity = (AbstractFunctionalEntityWithCentreContext<?>) entity;
                
                if (String.class.isAssignableFrom(entity.getKeyType())) {
                    ((AbstractFunctionalEntityWithCentreContext<String>) funcEntity).setKey("dummy");
                }
            }
            
            if (getMasterEntity() != null && EntityNewAction.class.isAssignableFrom(getMasterEntity().getClass())) {
                producedEntity = provideDefaultValuesForStandardNew(entity, (EntityNewAction) getMasterEntity());
            } else {
                producedEntity = provideDefaultValues(entity);
            }
        }
        // Resetting of meta-state makes the entity not dirty for the properties, changed above. This is important not to treat them as changed when going to client application.
        // However, in some rare cases it is possible to specify which property should skip resetting of its state (see method skipPropertiesForMetaStateResetting()).
        producedEntity.nonProxiedProperties().filter(mp -> !skipPropertiesForMetaStateResetting().contains(mp.getName())).forEach(mp -> mp.resetState());
        return producedEntity;
    }
    
    /**
     * In rare cases where there is a need not to reset meta-state of the property -- this property needs to be listed in this method.
     * 
     * @return
     */
    protected List<String> skipPropertiesForMetaStateResetting() {
        return Arrays.asList();
    }
    
    /**
     * Override this method in case where some additional initialisation is needed for the entity, edited by standard {@link EntityEditAction}.
     * <p>
     * Please, note that most likely it is needed to invoke super implementation. However, if the other, more specific, fetchModel needs to be specified -- the complete override 
     * is applicable.
     * 
     * @param entityId - the id of the edited entity
     * @return
     */
    protected T provideDefaultValuesForStandardEdit(final Long entityId, final EntityEditAction masterEntity) {
        return companion().findById(entityId, companion().getFetchProvider().fetchModel());
    };
    
    /**
     * Override this method in case where some additional initialisation is needed for the new entity, edited by standard {@link EntityNewAction}.
     * 
     * @param entity
     * @param masterEntity -- {@link EntityNewAction} instance that contains context
     * @return
     */
    protected T provideDefaultValuesForStandardNew(final T entity, final EntityNewAction masterEntity) {
        return entity;
    };

    /**
     * Override this method to provide domain-driven <code>entity</code>'s default values for the properties.
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
        return co(this.entityType);
    }

    /**
     * Use this method in case when the master context (as functional entity) is required for entity instantiation.
     *
     * @return
     */
    protected AbstractEntity<?> getMasterEntity() {
        return centreContext == null ? null : centreContext.getMasterEntity();
    }
    
    protected CentreContext<? extends AbstractEntity<?>, AbstractEntity<?>> getContext() {
        return centreContext;
    }

    public void setCentreContext(final CentreContext<? extends AbstractEntity<?>, AbstractEntity<?>> centreContext) {
        this.centreContext = centreContext;
    }

    protected String getChosenProperty() {
        return chosenProperty;
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
