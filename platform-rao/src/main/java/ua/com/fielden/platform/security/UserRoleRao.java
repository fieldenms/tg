package ua.com.fielden.platform.security;

import java.util.List;

import ua.com.fielden.platform.dao.IUserRoleDao;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.entity.query.model.OrderingModel;
import ua.com.fielden.platform.rao.CommonEntityRao;
import ua.com.fielden.platform.rao.RestClientUtil;
import ua.com.fielden.platform.security.user.UserRole;
import ua.com.fielden.platform.swing.review.annotations.EntityType;

import com.google.inject.Inject;

import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.from;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.orderBy;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;

/**
 * RAO implementation of the {@link IUserRoleDao}.
 *
 * @author TG Team
 *
 */
@EntityType(UserRole.class)
public class UserRoleRao extends CommonEntityRao<UserRole> implements IUserRoleDao {

    @Inject
    public UserRoleRao(final RestClientUtil restUtil) {
	super(restUtil);
    }

    @Override
    public List<UserRole> findAll() {
	final EntityResultQueryModel<UserRole> model = select(UserRole.class).model();
	final OrderingModel orderBy = orderBy().prop("key").asc().model();
	return getAllEntities(from(model).with(orderBy).model());
    }

    @Override
    public List<UserRole> findByIds(final Long... ids) {
	final EntityResultQueryModel<UserRole> model = select(UserRole.class).where().prop("id").in().values(ids).model();
	final OrderingModel orderBy = orderBy().prop("key").asc().model();
	return getAllEntities(from(model).with(orderBy).model());
    }
}