package ua.com.fielden.platform.sample.domain;

import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.cond;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.expr;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetch;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetchAll;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetchOnly;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.from;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.orderBy;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;

import java.util.Map;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.dao.QueryExecutionModel;
import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.entity.fetch.IFetchProvider;
import ua.com.fielden.platform.entity.query.EntityAggregates;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.entity.query.fluent.fetch;
import ua.com.fielden.platform.entity.query.model.AggregatedResultQueryModel;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.entity.query.model.OrderingModel;
import ua.com.fielden.platform.sample.domain.mixin.TgPersistentEntityWithPropertiesMixin;
import ua.com.fielden.platform.sample.domain.observables.TgPersistentEntityWithPropertiesChangeSubject;
import ua.com.fielden.platform.swing.review.annotations.EntityType;
import ua.com.fielden.platform.utils.EntityUtils;

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

    private final TgPersistentEntityWithPropertiesMixin mixin;
    private final TgPersistentEntityWithPropertiesChangeSubject changeSubject;

    @Inject
    public TgPersistentEntityWithPropertiesDao(final TgPersistentEntityWithPropertiesChangeSubject changeSubject, final IFilter filter) {
        super(filter);

        this.changeSubject = changeSubject;
        mixin = new TgPersistentEntityWithPropertiesMixin(this);
    }

    /**
     * Overridden to publish entity change events to an application wide observable.
     */
    @Override
    @SessionRequired
    public TgPersistentEntityWithProperties save(final TgPersistentEntityWithProperties entity) {
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        final TgPersistentEntityWithProperties saved = super.save(entity);
        changeSubject.publish(saved);
        return saved;
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
                .with("producerInitProp"); //
    }
}