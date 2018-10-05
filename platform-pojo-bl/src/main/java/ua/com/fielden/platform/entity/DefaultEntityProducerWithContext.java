package ua.com.fielden.platform.entity;

import static java.lang.String.format;
import static ua.com.fielden.platform.reflection.TitlesDescsGetter.getEntityTitleAndDesc;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

import ua.com.fielden.platform.companion.IEntityReader;
import ua.com.fielden.platform.dao.IEntityDao;
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
        if (masterEntityInstanceOf(EntityEditAction.class)) {
            final EntityEditAction entityEditAction = masterEntity(EntityEditAction.class);
            final Long editedEntityId = Long.valueOf(entityEditAction.getEntityId());

            // let's try the default behaviour... which may return a null either due to a programming error (most likely) or a case of chosen property being clicked, but ID is from a corresponding master entity
//            try {
//                Thread.sleep(4000);
//            } catch (final InterruptedException e) {
//                // TODO Auto-generated catch block
//                e.printStackTrace();
//            }
            producedEntity = provideDefaultValuesForStandardEdit(editedEntityId, entityEditAction);

            // let's be a more bit protective and try to provide a meaningful exception in cases where entity could not be found instead of the inevitable NPE downstream
            if (producedEntity == null) {
                // before bailing out and throwing an exception, let's try to handle the case of edit-action being invoked on a chosen property for the current entity...
                // need to use ofMasterEntity() here as the useful context is only present in the EntityEditAction instance itself
                if (ofMasterEntity().currentEntityNotEmpty() && ofMasterEntity().chosenPropertyNotEmpty()) {
                    return Optional.ofNullable(ofMasterEntity().currentEntity().get(ofMasterEntity().chosenProperty()))
                            .filter(v -> v instanceof AbstractEntity)
                            .map(v -> ((AbstractEntity<?>) v).getId())
                            .map(id -> refetchInstrumentedEntityById(id))
                            .orElseThrow(() -> new EntityProducingException(format("Could not find %s.", getEntityTitleAndDesc(entityType).getKey())));
                } // else there could potentially be some other valid cases that should be handled here once become apparent...

                // otherwise let's report the problem...
                throw new EntityProducingException(format("Could not find entity of type [%s] with ID [%s].", entityEditAction.getEntityType(), entityEditAction.getEntityId()));
            }
        } else if (masterEntityInstanceOf(AbstractFunctionalEntityForCompoundMenuItem.class) && keyOfMasterEntityInstanceOf(entityType)) { // in the case of compound master's main view entity
            final T compoundMasterEntity = keyOfMasterEntity(entityType); // this entity must be taken from Open*MasterAction producer (set as the key of menu item functional entity)
            producedEntity = compoundMasterEntity.isPersisted() ? refetchInstrumentedEntityById(compoundMasterEntity.getId()) : compoundMasterEntity; // but refetched when it is persisted
            // please also note that no custom logic (provideDefaultValues) will be applied to that entity, the process of its initiation is a sole prerogative of compound master opener's producer -- this is the only place where it should be produced (or retrieved)
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

            if (masterEntityInstanceOf(EntityNewAction.class)) {
                producedEntity = provideDefaultValuesForStandardNew(entity, masterEntity(EntityNewAction.class));
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
     * Re-fetches entity using <code>property</code>'s fetch provider for the entity type behind this producer.
     * Returns uninstrumented instance.
     *
     * @param entity
     * @return
     */
    protected final <M extends AbstractEntity<?>> M refetch(final M entity, final String property) {
        return co((Class<M>) entity.getType()).findById(entity.getId(), companion.get().getFetchProvider().<M>fetchFor(property).fetchModel());
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
    public DefaultEntityProducerWithContext<T> setContext(final CentreContext<? extends AbstractEntity<?>, AbstractEntity<?>> context) {
        this.context = context;
        return this;
    }
}
