package ua.com.fielden.platform.sample.domain;

import java.util.Map;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.entity.fetch.IFetchProvider;
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
    public IFetchProvider<TgPersistentEntityWithProperties> createFetchProvider() {
        return super.createFetchProvider()
                .with("key") // this property is "required" (necessary during saving) -- should be declared as fetching property
                .with("desc")
                .with("integerProp", "moneyProp", "bigDecimalProp", "stringProp", "booleanProp", "dateProp", "requiredValidatedProp")
                .with("domainInitProp", "nonConflictingProp", "conflictingProp")
                // .with("entityProp", EntityUtils.fetch(TgPersistentEntityWithProperties.class).with("key"))
                .with("userParam")
                .with("entityProp")
                //                .with("status")
                .with("critOnlyEntityProp")
                .with("compositeProp")
                // .with("producerInitProp", EntityUtils.fetch(TgPersistentEntityWithProperties.class).with("key")
                .with("producerInitProp"); //
    }
}