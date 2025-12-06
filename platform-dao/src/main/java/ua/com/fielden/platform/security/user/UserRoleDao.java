package ua.com.fielden.platform.security.user;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.fetch.IFetchProvider;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.entity.query.model.OrderingModel;
import ua.com.fielden.platform.security.Authorise;
import ua.com.fielden.platform.security.tokens.user.UserRole_CanDelete_Token;
import ua.com.fielden.platform.security.tokens.user.UserRole_CanSave_Token;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.*;

/// DAO implementation of [UserRoleCo].
///
@EntityType(UserRole.class)
public class UserRoleDao extends CommonEntityDao<UserRole> implements UserRoleCo {

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
    public UserRole save(final UserRole entity) {
        return super.save(entity);
    }
    
    @Override
    @SessionRequired
    @Authorise(UserRole_CanDelete_Token.class)
    public int batchDelete(Collection<Long> entitiesIds) {
        return defaultBatchDelete(entitiesIds);
    }
    
    @Override
    public IFetchProvider<UserRole> createFetchProvider() {
        return super.createFetchProvider()
                .with("key") // this property is "required" (necessary during saving) -- should be declared as fetching property
                .with("desc");
    }

}