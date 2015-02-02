package ua.com.fielden.platform.sample.domain;

import java.util.Map;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.entity.fetch.IEntityFetchStrategy;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.sample.domain.mixin.TgPersistentEntityWithPropertiesMixin;
import ua.com.fielden.platform.swing.review.annotations.EntityType;

import com.google.inject.Inject;

/**
 * DAO implementation for companion object {@link ITgPersistentEntityWithProperties}.
 *
 * @author Developers
 *
 */
@EntityType(TgPersistentEntityWithProperties.class)
public class TgPersistentEntityWithPropertiesDao extends CommonEntityDao<TgPersistentEntityWithProperties> implements ITgPersistentEntityWithProperties {
    private final TgPersistentEntityWithPropertiesMixin mixin;
    private IEntityFetchStrategy<TgPersistentEntityWithProperties> fetchStrategy;

    @Inject
    public TgPersistentEntityWithPropertiesDao(final IFilter filter) {
        super(filter);

        mixin = new TgPersistentEntityWithPropertiesMixin(this);
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
    public IEntityFetchStrategy<TgPersistentEntityWithProperties> createFetchStrategy() {
        return super.createFetchStrategy()
                .with("key") // this property is "required" (necessary during saving) -- should be declared as fetching property
                .with("integerProp", "moneyProp", "bigDecimalProp", "stringProp", "booleanProp", "dateProp")
                .with("domainInitProp", "nonConflictingProp", "conflictingProp")
                // .with("entityProp", efs(TgPersistentEntityWithProperties.class).with("key"))
                .with("entityProp", "entityProp.key")
                // .with("producerInitProp", efs(TgPersistentEntityWithProperties.class).with("key")
                .with("producerInitProp.key"); //
    }
}