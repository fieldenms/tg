package ua.com.fielden.platform.security.dao;

import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.cond;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.expr;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetch;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetchAggregates;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetchAll;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetchAllInclCalc;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetchKeyAndDescOnly;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetchOnly;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.from;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.orderBy;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;

import java.util.ArrayList;
import java.util.List;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.dao.IUserRoleDao;
import ua.com.fielden.platform.dao.QueryExecutionModel;
import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.fetch.IFetchProvider;
import ua.com.fielden.platform.entity.query.EntityAggregates;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.entity.query.fluent.fetch;
import ua.com.fielden.platform.entity.query.model.AggregatedResultQueryModel;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.entity.query.model.OrderingModel;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.security.user.UserRole;
import ua.com.fielden.platform.swing.review.annotations.EntityType;

import com.google.inject.Inject;

/**
 * Db driven implementation of the {@link IUserRoleDao}.
 * 
 * @author TG Team
 * 
 */
@EntityType(UserRole.class)
public class UserRoleDao extends CommonEntityDao<UserRole> implements IUserRoleDao {

    @Inject
    protected UserRoleDao(final IFilter filter) {
        super(filter);
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
    public IFetchProvider<UserRole> createFetchProvider() {
        return super.createFetchProvider()
                .with("key") // this property is "required" (necessary during saving) -- should be declared as fetching property
                .with("desc");
    }

}