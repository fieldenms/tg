package ua.com.fielden.platform.sample.domain;

import com.google.inject.Inject;
import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.fetch.IFetchProvider;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.security.tokens.persistent.TgFuelType_CanDelete_Token;
import ua.com.fielden.platform.security.tokens.persistent.TgFuelType_CanSaveNew_Token;
import ua.com.fielden.platform.security.Authorise;

@EntityType(TgFuelType.class)
public class TgFuelTypeDao extends CommonEntityDao<TgFuelType> implements ITgFuelType {

    @Inject
    protected TgFuelTypeDao(final IFilter filter) {
        super(filter);
    }

    @Override
    @Authorise(TgFuelType_CanSaveNew_Token.class)
    public TgFuelType new_() {
        final var entity = super.new_().setGuardedIfPersisted(DEFAULT_VALUE_FOR_PROP_guarded).setGuardedEvenIfNotPersisted(DEFAULT_VALUE_FOR_PROP_guarded);
        entity.getProperty("guardedEvenIfNotPersisted").resetValues(); // this is to define an original value for "guardedEvenIfNotPersisted" while entity is not yet persisted.
        return entity;
    }

    @Override
    @SessionRequired
    @Authorise(TgFuelType_CanDelete_Token.class)
    public void delete(final TgFuelType entity) {
        defaultDelete(entity);
    }

    @Override
    protected IFetchProvider<TgFuelType> createFetchProvider() {
        return FETCH_PROVIDER_FOR_EDITING;
    }

}
