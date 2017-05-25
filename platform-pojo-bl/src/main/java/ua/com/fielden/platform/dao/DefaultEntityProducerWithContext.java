package ua.com.fielden.platform.dao;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

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
    // optional context for context-dependent entity producing logic
    private CentreContext<? extends AbstractEntity<?>, AbstractEntity<?>> context;
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
        final IEntityDao<T> companion = co(this.entityType);
        if (companion != null) {
            return companion.new_();
        } else {
            return factory.newEntity(this.entityType);
        }
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
        return context == null ? null : context.getMasterEntity();
    }
    
    protected CentreContext<? extends AbstractEntity<?>, AbstractEntity<?>> getContext() {
        return context;
    }

    public void setContext(final CentreContext<? extends AbstractEntity<?>, AbstractEntity<?>> context) {
        this.context = context;
    }

    protected String getChosenProperty() {
        return context == null ? null : context.getChosenProperty();
    }

    @Deprecated
    protected Long getCompoundMasterEntityId() {
        return context == null ? null : context.getCompoundMasterEntityId();
    }
    
    
    
    
    
    
    
    private AbstractEntity<?> getCurrEntity() {
        return context == null ? null :
               context.getSelectedEntities().size() == 1 ? context.getCurrEntity() : null;
    }
    
    private Optional<Function<AbstractFunctionalEntityWithCentreContext<?>, Object>> getComputation() {
        return context == null ? Optional.empty() : context.getComputation();
    }
    
    private List<AbstractEntity<?>> getSelectedEntities() {
        return context == null ? Collections.unmodifiableList(new ArrayList<>()) : context.getSelectedEntities();
    }
    
    ////////////////////////////////// CONTEXT DECOMPOSITION API //////////////////////////////////
    
    // MASTER ENTITY:
    protected AbstractEntity<?> masterEntity() {
        return getMasterEntity();
    }
    
    protected <M extends AbstractEntity<?>> M masterEntity(final Class<M> type) {
        return (M) masterEntity();
    }
    
    protected boolean masterEntityEmpty() {
        return masterEntity() == null;
    }
    
    protected boolean masterEntityNotEmpty() {
        return !masterEntityEmpty();
    }
    
    protected <M extends AbstractEntity<?>> boolean masterEntityInstanceOf(final Class<M> type) {
        return masterEntityNotEmpty() && type.isAssignableFrom(masterEntity().getClass());
    }
    
    protected <M extends AbstractEntity<?>> boolean masterEntityKeyOfMasterEntityInstanceOf(final Class<M> type) {
        if (masterEntityNotEmpty()) {
            final AbstractEntity<?> masterEntity = masterEntity();
            if (AbstractFunctionalEntityWithCentreContext.class.isAssignableFrom(masterEntity.getClass())) {
                final AbstractFunctionalEntityWithCentreContext masterFuncEntity = (AbstractFunctionalEntityWithCentreContext) masterEntity;
                if (masterFuncEntity.getContext() != null && masterFuncEntity.getContext().getMasterEntity() != null) {
                    final AbstractEntity<?> masterEntityOfMasterEntity = masterFuncEntity.getContext().getMasterEntity();
                    if (masterEntityOfMasterEntity.get("key") != null) {
                        return type.isAssignableFrom(masterEntityOfMasterEntity.get("key").getClass());
                    }
                }
            }
        }
        return false;
    }
    
    protected <M extends AbstractEntity<?>> M masterEntityKeyOfMasterEntity(final Class<M> type) {
        final AbstractEntity<?> masterEntity = masterEntity();
        final AbstractFunctionalEntityWithCentreContext masterFuncEntity = (AbstractFunctionalEntityWithCentreContext) masterEntity;
        final AbstractEntity<?> masterEntityOfMasterEntity = masterFuncEntity.getContext().getMasterEntity();
        return (M) masterEntityOfMasterEntity.get("key");
    }
    
    // CHOSEN PROPERTY:
    /**
     * Returns chosen property value: <code>null</code> if chosen property is not applicable,
     * "" if property action for 'this' column was actioned, and non-empty property name otherwise
     * (master property editor actions and centre column actions).
     * 
     * @return
     */
    protected String chosenProperty() {
        return getChosenProperty();
    }
    
    /**
     * Returns <code>true</code> if chosen property is not applicable, <code>false</code> otherwise.
     * 
     * @return
     */
    protected boolean chosenPropertyEmpty() {
        return chosenProperty() == null;
    }
    
    /**
     * Returns <code>true</code> if chosen property is applicable ("" or non-empty string), <code>false</code> otherwise.
     * 
     * @return
     */
    protected boolean chosenPropertyNotEmpty() {
        return !chosenPropertyEmpty();
    }
    
    /**
     * Returns <code>true</code> if chosen property equals to concrete non-null value, <code>false</code> otherwise.
     * 
     * @param value
     * @return
     */
    protected boolean chosenPropertyEqualsTo(final String value) {
        if (value == null) {
            throw new EntityProducingException("Null value is not permitted.");
        }
        return value.equals(chosenProperty());
    }
    
    /**
     * Returns <code>true</code> if action for 'this' column has been clicked, <code>false</code> otherwise.
     * 
     * @return
     */
    protected boolean chosenPropertyRepresentsThisColumn() {
        return chosenPropertyEqualsTo("");
    }
    
    // CURRENT ENTITY:
    protected AbstractEntity<?> currentEntity() {
        return getCurrEntity();
    }
    
    protected <M extends AbstractEntity<?>> M currentEntity(final Class<M> type) {
        return (M) currentEntity();
    }
    
    protected boolean currentEntityEmpty() {
        return currentEntity() == null;
    }
    
    protected boolean currentEntityNotEmpty() {
        return !currentEntityEmpty();
    }
    
    protected <M extends AbstractEntity<?>> boolean currentEntityInstanceOf(final Class<M> type) {
        return currentEntityNotEmpty() && type.isAssignableFrom(currentEntity().getClass());
    }
    
    // COMPUTATION:
    protected Optional<Function<AbstractFunctionalEntityWithCentreContext<?>, Object>> computation() {
        return getComputation();
    }
    
    // SELECTED ENTITIES:
    private List<AbstractEntity<?>> selectedEntities() {
        return getSelectedEntities();
    }
    
    protected boolean selectedEntitiesEmpty() {
        return selectedEntities().isEmpty();
    }
    
    protected boolean selectedEntitiesNonEmpty() {
        return !selectedEntitiesEmpty();
    }
    
    protected boolean selectedEntitiesOnlyOne() {
        return selectedEntities().size() == 1;
    }
    
    protected boolean selectedEntitiesMoreThanOne() {
        return selectedEntities().size() > 1;
    }
}
