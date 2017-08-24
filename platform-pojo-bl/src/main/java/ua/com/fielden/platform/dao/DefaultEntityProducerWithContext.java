package ua.com.fielden.platform.dao;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

import ua.com.fielden.platform.companion.IEntityReader;
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
public class DefaultEntityProducerWithContext<T extends AbstractEntity<?>> implements IEntityProducer<T>, IContextDecomposer {
    private final EntityFactory factory;
    protected final Class<T> entityType;
    private final Optional<IEntityDao<T>> companion;
    // optional context for context-dependent entity producing logic
    private CentreContext<? extends AbstractEntity<?>, AbstractEntity<?>> context;
    private final ICompanionObjectFinder coFinder;
    private final Map<Class<? extends AbstractEntity<?>>, IEntityReader<?>> coCache = new HashMap<>();
    private final Map<Class<? extends AbstractEntity<?>>, IEntityReader<?>> co$Cache = new HashMap<>();

    public DefaultEntityProducerWithContext(final EntityFactory factory, final Class<T> entityType, final ICompanionObjectFinder companionFinder) {
        this.factory = factory;
        this.entityType = entityType;
        this.coFinder = companionFinder;
        this.companion = Optional.ofNullable(coFinder.find(entityType));
    }
    
    /**
     * A convenient way to obtain companion instances by the types of corresponding entities, which read uninstrumented entities.
     * 
     * @param type -- entity type whose companion instance needs to be obtained
     * @return
     */
    @SuppressWarnings("unchecked")
    public <R extends IEntityReader<E>, E extends AbstractEntity<?>> R co(final Class<E> type) {
        IEntityReader<?> co = coCache.get(type);
        if (co == null) {
            co = coFinder.findAsReader(type, true);
            coCache.put(type, co);
        }
        return (R) co;
    }

    /**
     * A convenient way to obtain companion instances by the types of corresponding entities, which read instrumented entities.
     * 
     * @param type -- entity type whose companion instance needs to be obtained
     * @return
     */
    @SuppressWarnings("unchecked")
    public <R extends IEntityReader<E>, E extends AbstractEntity<?>> R co$(final Class<E> type) {
        IEntityReader<?> co = co$Cache.get(type);
        if (co == null) {
            co = coFinder.findAsReader(type, false);
            co$Cache.put(type, co);
        }
        return (R) co;
    }
    
    @Override
    public final T newEntity() {
        final T producedEntity;
        if (getMasterEntity() != null && EntityEditAction.class.isAssignableFrom(getMasterEntity().getClass())) {
            final EntityEditAction entityEditAction = (EntityEditAction) getMasterEntity();
            final Long editedEntityId = Long.valueOf(entityEditAction.getEntityId());
            producedEntity = provideDefaultValuesForStandardEdit(editedEntityId, entityEditAction);
        } else {
            final T entity = new_();
            
            if (entity instanceof AbstractFunctionalEntityWithCentreContext) {
                final AbstractFunctionalEntityWithCentreContext<?> funcEntity = (AbstractFunctionalEntityWithCentreContext<?>) entity;
                
                if (context != null) {
                    funcEntity.setContext(context);
                }
                
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
     * A helper function to instantiate a new entity using either companion if available or entity factory otherwise.
     *
     * @return
     */
    private T new_() {
        return companion
                .map(co -> co.new_())
                .orElseGet(() -> factory.newEntity(this.entityType));
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
     * <p>
     * Throws {@link NoSuchElementException} if the associated entity has no companion object, which this method tries to use for finding the entity by <code>id</code>.
     * 
     * @param entityId - the id of the edited entity
     * @return
     */
    protected T provideDefaultValuesForStandardEdit(final Long entityId, final EntityEditAction masterEntity) {
        return refetchInstrumentedEntityById(entityId);
    }
    
    /**
     * Refetches entity by its <code>entityId</code> using default fetch provider. Returns instrumented entity
     * that could be potentially used for those producers that return refetched instrumented entities instead 
     * of returning produced instances (dual-purpose producers).
     * 
     * @param entityId
     * @return
     */
    protected final T refetchInstrumentedEntityById(final Long entityId) {
        return companion.get().findById(entityId, companion.get().getFetchProvider().fetchModel());
    }
    
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
    
    @Override
    public CentreContext<? extends AbstractEntity<?>, AbstractEntity<?>> getContext() {
        return context;
    }
    
    @Override
    public void setContext(final CentreContext<? extends AbstractEntity<?>, AbstractEntity<?>> context) {
        this.context = context;
    }
}
