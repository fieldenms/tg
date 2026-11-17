package ua.com.fielden.platform.web_api;

import jakarta.inject.Inject;
import ua.com.fielden.platform.basic.config.IApplicationDomainProvider;
import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.exceptions.InvalidArgumentException;
import ua.com.fielden.platform.entity.fetch.IFetchProvider;
import ua.com.fielden.platform.entity.query.fluent.fetch;
import ua.com.fielden.platform.types.either.Either;
import ua.com.fielden.platform.web_api.exceptions.WebApiException;

import java.util.Optional;

import static ua.com.fielden.platform.web_api.UserDefinedEnhancedEntityType.BASE_TYPE;
import static ua.com.fielden.platform.web_api.validators.EnhancedEntityTypeBaseTypeValidator.ERR_NO_SUCH_BASE_TYPE;

/// DAO implementation for companion object [UserDefinedEnhancedEntityTypeCo].
///
@EntityType(UserDefinedEnhancedEntityType.class)
public class UserDefinedEnhancedEntityTypeDao extends CommonEntityDao<UserDefinedEnhancedEntityType> implements UserDefinedEnhancedEntityTypeCo {

    private final IApplicationDomainProvider appDomainProvider;

    @Inject
    protected UserDefinedEnhancedEntityTypeDao(final IApplicationDomainProvider appDomainProvider) {
        this.appDomainProvider = appDomainProvider;
    }

    @Override
    protected IFetchProvider<UserDefinedEnhancedEntityType> createFetchProvider() {
        return FETCH_PROVIDER;
    }

    @Override
    @SessionRequired
    public Either<Long, UserDefinedEnhancedEntityType> save(final UserDefinedEnhancedEntityType entity, final Optional<fetch<UserDefinedEnhancedEntityType>> maybeFetch) {
        return super.save(entity, maybeFetch);
    }

    @Override
    public Class<? extends AbstractEntity<?>> baseTypeAsJavaType(final UserDefinedEnhancedEntityType instance) {
        return findBaseTypeAsJavaType(instance).orElseThrow(() -> new WebApiException(ERR_NO_SUCH_BASE_TYPE.formatted(instance.getBaseType())));
    }

    @Override
    public Optional<Class<? extends AbstractEntity<?>>> findBaseTypeAsJavaType(final UserDefinedEnhancedEntityType instance) {
        final String name = instance.getBaseType();
        if (name == null) {
            throw new InvalidArgumentException("[%s] is not assigned.".formatted(BASE_TYPE));
        }
        return appDomainProvider.entityTypes().stream().filter(type -> type.getName().equals(name)).findFirst();
    }

}
