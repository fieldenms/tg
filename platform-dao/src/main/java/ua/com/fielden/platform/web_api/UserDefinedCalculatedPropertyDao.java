package ua.com.fielden.platform.web_api;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.fetch.IFetchProvider;
import ua.com.fielden.platform.entity.query.fluent.fetch;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.types.either.Either;

import java.util.Collection;
import java.util.List;
import java.util.Map;
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

    @Override
    @SessionRequired
    public int batchDelete(final List<UserDefinedCalculatedProperty> entities) {
        return defaultBatchDelete(entities);
    }

    @Override
    @SessionRequired
    public int batchDelete(final EntityResultQueryModel<UserDefinedCalculatedProperty> query) {
        return defaultBatchDelete(query);
    }

    @Override
    @SessionRequired
    public int batchDelete(
            final EntityResultQueryModel<UserDefinedCalculatedProperty> model,
            final Map<String, Object> paramValues)
    {
        return defaultBatchDelete(model, paramValues);
    }

}
