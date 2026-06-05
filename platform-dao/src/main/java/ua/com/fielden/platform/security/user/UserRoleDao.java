package ua.com.fielden.platform.security.user;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.exceptions.InvalidStateException;
import ua.com.fielden.platform.entity.fetch.IFetchProvider;
import ua.com.fielden.platform.entity.query.fluent.fetch;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.entity.query.model.OrderingModel;
import ua.com.fielden.platform.security.Authorise;
import ua.com.fielden.platform.security.tokens.user.UserRole_CanDelete_Token;
import ua.com.fielden.platform.security.tokens.user.UserRole_CanSave_Token;
import ua.com.fielden.platform.types.either.Either;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static ua.com.fielden.platform.entity.AbstractEntity.ID;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.*;
import static ua.com.fielden.platform.security.user.SecurityRoleAssociation.ROLE;
import static ua.com.fielden.platform.security.user.UserAndRoleAssociation.USER_ROLE;

/// DAO implementation of [UserRoleCo].
///
@EntityType(UserRole.class)
public class UserRoleDao extends CommonEntityDao<UserRole> implements UserRoleCo {

    public static final String ERR_DELETING_USER_ROLES = "Only roles associated with neither users nor security tokens can be deleted. Otherwise, mark roles as inactive.";

    @Override
    public UserRole new_() {
        return super.new_().setActive(true);
    }

    @Override
    @SessionRequired
    public List<UserRole> findAll() {
        final EntityResultQueryModel<UserRole> model = select(UserRole.class).model();
        final OrderingModel orderBy = orderBy().prop(AbstractEntity.KEY).asc().model();
        return getAllEntities(from(model).with(orderBy).with(fetchAll(UserRole.class)).model());
    }

    @Override
    public List<UserRole> findByIds(final Long... ids) {
        if (ids == null || ids.length == 0) {
            return new ArrayList<UserRole>();
        }

        final EntityResultQueryModel<UserRole> model = select(UserRole.class).where().prop(AbstractEntity.ID).in().values(ids).model();
        final OrderingModel orderBy = orderBy().prop(AbstractEntity.KEY).asc().model();
        return getAllEntities(from(model).with(orderBy).model());
    }

    @Override
    @SessionRequired
    @Authorise(UserRole_CanSave_Token.class)
    public Either<Long, UserRole> save(final UserRole entity, final Optional<fetch<UserRole>> maybeFetch) {
        return super.save(entity, maybeFetch);
    }

    @Override
    @SessionRequired
    @Authorise(UserRole_CanDelete_Token.class)
    public int batchDelete(Collection<Long> userRoleIds) {
        final var qUserRoleAssociations = select(UserAndRoleAssociation.class).where().prop(USER_ROLE + "." +  ID).in().values(userRoleIds).model();
        final var qTokenRoleAssociations = select(SecurityRoleAssociation.class).where().prop(ROLE + "." +  ID).in().values(userRoleIds).model();
        if (co(UserAndRoleAssociation.class).exists(qUserRoleAssociations) || co(SecurityRoleAssociation.class).exists(qTokenRoleAssociations)) {
            throw new InvalidStateException(ERR_DELETING_USER_ROLES);
        }

        return defaultBatchDelete(userRoleIds);
    }
    
    @Override
    public IFetchProvider<UserRole> createFetchProvider() {
        return super.createFetchProvider()
                .with("key") // this property is "required" (necessary during saving) -- should be declared as fetching property
                .with("desc");
    }

}
