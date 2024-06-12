package ua.com.fielden.platform.security.user;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.query.IFilter;

/**
 * DAO implementation for companion object {@link ReUserCo}.
 *
 * @author TG Team
 */
@EntityType(ReUser.class)
public class ReUserDao extends CommonEntityDao<ReUser> implements ReUserCo {

}
