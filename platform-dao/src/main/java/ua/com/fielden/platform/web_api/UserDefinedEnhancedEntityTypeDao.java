package ua.com.fielden.platform.web_api;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.fetch.IFetchProvider;
import ua.com.fielden.platform.entity.query.fluent.fetch;
import ua.com.fielden.platform.types.either.Either;

import java.util.Optional;

/// DAO implementation for companion object [UserDefinedEnhancedEntityTypeCo].
///
@EntityType(UserDefinedEnhancedEntityType.class)
public class UserDefinedEnhancedEntityTypeDao extends CommonEntityDao<UserDefinedEnhancedEntityType> implements UserDefinedEnhancedEntityTypeCo {

    @Override
    protected IFetchProvider<UserDefinedEnhancedEntityType> createFetchProvider() {
        return FETCH_PROVIDER;
    }

    @Override
    @SessionRequired
    public Either<Long, UserDefinedEnhancedEntityType> save(final UserDefinedEnhancedEntityType entity, final Optional<fetch<UserDefinedEnhancedEntityType>> maybeFetch) {
        return super.save(entity, maybeFetch);
    }

}
