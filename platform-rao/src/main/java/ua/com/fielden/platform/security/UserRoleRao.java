package ua.com.fielden.platform.security;

import java.util.List;

import ua.com.fielden.platform.dao.IUserRoleDao;
import ua.com.fielden.platform.equery.interfaces.IQueryOrderedModel;
import ua.com.fielden.platform.rao.CommonEntityRao;
import ua.com.fielden.platform.rao.RestClientUtil;
import ua.com.fielden.platform.security.user.UserRole;
import ua.com.fielden.platform.swing.review.annotations.EntityType;

import com.google.inject.Inject;

import static ua.com.fielden.platform.equery.equery.select;

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
	final IQueryOrderedModel<UserRole> model = select(UserRole.class).orderBy("key").model();
	return getEntities(model);
    }

    @Override
    public List<UserRole> findByIds(final Long... ids) {
	final IQueryOrderedModel<UserRole> model = select(UserRole.class).where().prop("id").in().val(ids).orderBy("key").model();
	return getEntities(model);
    }
}
