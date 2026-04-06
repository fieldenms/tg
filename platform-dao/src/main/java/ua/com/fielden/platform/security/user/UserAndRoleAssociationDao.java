package ua.com.fielden.platform.security.user;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.fetch.IFetchProvider;
import ua.com.fielden.platform.entity.query.fluent.fetch;
import ua.com.fielden.platform.security.Authorise;
import ua.com.fielden.platform.security.tokens.user.UserAndRoleAssociation_CanSave_Token;
import ua.com.fielden.platform.types.either.Either;

import java.util.Optional;

/// DAO implementation of the [UserAndRoleAssociationCo]
///
@EntityType(UserAndRoleAssociation.class)
public class UserAndRoleAssociationDao extends CommonEntityDao<UserAndRoleAssociation> implements UserAndRoleAssociationCo {

    @Override
    public UserAndRoleAssociation new_() {
        return super.new_().setActive(true);
    }

    @Override
    @SessionRequired
    @Authorise(UserAndRoleAssociation_CanSave_Token.class)
    public Either<Long, UserAndRoleAssociation> save(final UserAndRoleAssociation entity, final Optional<fetch<UserAndRoleAssociation>> maybeFetch) {
        return super.save(entity, maybeFetch);
    }

    @Override
    public IFetchProvider<UserAndRoleAssociation> createFetchProvider() {
        return FETCH_PROVIDER;
    }

}
