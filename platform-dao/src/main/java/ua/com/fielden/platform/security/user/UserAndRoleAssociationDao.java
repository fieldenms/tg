package ua.com.fielden.platform.security.user;

import jakarta.inject.Inject;
import ua.com.fielden.platform.basic.config.IApplicationSettings;
import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.fetch.IFetchProvider;
import ua.com.fielden.platform.entity.query.fluent.fetch;
import ua.com.fielden.platform.security.Authorise;
import ua.com.fielden.platform.security.exceptions.SecurityException;
import ua.com.fielden.platform.security.tokens.user.UserAndRoleAssociation_CanSave_Token;
import ua.com.fielden.platform.types.either.Either;
import ua.com.fielden.platform.utils.EntityUtils;

import java.util.Optional;

/// DAO implementation of the [UserAndRoleAssociationCo]
///
@EntityType(UserAndRoleAssociation.class)
public class UserAndRoleAssociationDao extends CommonEntityDao<UserAndRoleAssociation> implements UserAndRoleAssociationCo {

    public static final String ERR_SELF_CHANGING_ROLES = "Changing roles for yourself is not allowed.";

    private final IApplicationSettings appSettings;

    @Inject
    protected UserAndRoleAssociationDao(final IApplicationSettings appSettings) {
        this.appSettings = appSettings;
    }

    @Override
    public UserAndRoleAssociation new_() {
        return super.new_().setActive(true);
    }

    @Override
    @SessionRequired
    @Authorise(UserAndRoleAssociation_CanSave_Token.class)
    public Either<Long, UserAndRoleAssociation> save(final UserAndRoleAssociation entity, final Optional<fetch<UserAndRoleAssociation>> maybeFetch) {
        if (!appSettings.usersSelfEdit() && entity.isDirty() && EntityUtils.areEqual(getUser(), entity.getUser())) {
            throw new SecurityException(ERR_SELF_CHANGING_ROLES);
        }
        return super.save(entity, maybeFetch);
    }

    @Override
    public IFetchProvider<UserAndRoleAssociation> createFetchProvider() {
        return FETCH_PROVIDER;
    }

}
