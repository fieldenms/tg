package ua.com.fielden.platform.web_api;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.fetch.IFetchProvider;
import ua.com.fielden.platform.entity.query.fluent.fetch;
import ua.com.fielden.platform.types.either.Either;

import java.util.Collection;
import java.util.Optional;

/// DAO implementation for companion object [UserDefinedCalculatedPropertyCo].
///
@EntityType(UserDefinedCalculatedProperty.class)
public class UserDefinedCalculatedPropertyDao extends CommonEntityDao<UserDefinedCalculatedProperty> implements UserDefinedCalculatedPropertyCo {

    @Override
    protected IFetchProvider<UserDefinedCalculatedProperty> createFetchProvider() {
        return FETCH_PROVIDER;
    }

    @Override
    @SessionRequired
    public Either<Long, UserDefinedCalculatedProperty> save(final UserDefinedCalculatedProperty entity, final Optional<fetch<UserDefinedCalculatedProperty>> maybeFetch) {
        return super.save(entity, maybeFetch);
    }

    @Override
    @SessionRequired
    public int batchDelete(final Collection<Long> entitiesIds) {
        return defaultBatchDelete(entitiesIds);
    }

}
