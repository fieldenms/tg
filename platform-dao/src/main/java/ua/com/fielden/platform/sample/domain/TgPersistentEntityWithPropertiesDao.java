package ua.com.fielden.platform.sample.domain;

import java.util.Collection;
import java.util.Map;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.entity.fetch.IFetchProvider;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.sample.domain.observables.TgPersistentEntityWithPropertiesChangeSubject;
import ua.com.fielden.platform.swing.review.annotations.EntityType;

import com.google.inject.Inject;

/**
 * DAO implementation for companion object {@link ITgPersistentEntityWithProperties}.
 * It demos the use of {@link TgPersistentEntityWithPropertiesChangeSubject} for publishing change events to be propagated to the subscribed clients.
 *
 * @author Developers
 *
 */
@EntityType(TgPersistentEntityWithProperties.class)
public class TgPersistentEntityWithPropertiesDao extends CommonEntityDao<TgPersistentEntityWithProperties> implements ITgPersistentEntityWithProperties {

    private final TgPersistentEntityWithPropertiesChangeSubject changeSubject;

    @Inject
    public TgPersistentEntityWithPropertiesDao(final TgPersistentEntityWithPropertiesChangeSubject changeSubject, final IFilter filter) {
        super(filter);

        this.changeSubject = changeSubject;
    }

    /**
     * Overridden to publish entity change events to an application wide observable.
     */
    @Override
    @SessionRequired
    public TgPersistentEntityWithProperties save(final TgPersistentEntityWithProperties entity) {
        final boolean wasNew = false; // !entity.isPersisted();
        final TgPersistentEntityWithProperties saved = super.save(entity);
        changeSubject.publish(saved);

        // if the entity was new and just successfully saved then let's return a new entity to mimic "continuous" entry
        // otherwise simply return the same entity
        if (wasNew && saved.isValid().isSuccessful()) {
            final TgPersistentEntityWithProperties newEntity = saved.getEntityFactory().newEntity(TgPersistentEntityWithProperties.class);
            // the following two lines can be uncommented to simulate the situation of an invalid new entity returned from save
            //newEntity.setRequiredValidatedProp(1);
            //newEntity.setRequiredValidatedProp(null);
            return newEntity;
        } else {
            return saved;
        }
    }

    @Override
    public int batchDelete(final Collection<Long> entitiesIds) {
        return defaultBatchDelete(entitiesIds);
    }

    @Override
    @SessionRequired
    public void delete(final TgPersistentEntityWithProperties entity) {
        defaultDelete(entity);
    }

    @Override
    @SessionRequired
    public void delete(final EntityResultQueryModel<TgPersistentEntityWithProperties> model, final Map<String, Object> paramValues) {
        defaultDelete(model, paramValues);
    }

    @Override
    public IFetchProvider<TgPersistentEntityWithProperties> createFetchProvider() {
        return super.createFetchProvider()
                .with("key") // this property is "required" (necessary during saving) -- should be declared as fetching property
                .with("desc")
                .with("integerProp", "moneyProp", "bigDecimalProp", "stringProp", "booleanProp", "dateProp", "requiredValidatedProp")
                .with("domainInitProp", "nonConflictingProp", "conflictingProp")
                // .with("entityProp", EntityUtils.fetch(TgPersistentEntityWithProperties.class).with("key"))
                .with("userParam", "userParam.basedOnUser")
                .with("entityProp", "entityProp.entityProp", "entityProp.compositeProp", "entityProp.compositeProp.desc")
                //                .with("status")
                .with("critOnlyEntityProp")
                .with("compositeProp", "compositeProp.desc")
                // .with("producerInitProp", EntityUtils.fetch(TgPersistentEntityWithProperties.class).with("key")
                .with("producerInitProp", "status.key", "status.desc"); //
    }
}