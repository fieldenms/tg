package ua.com.fielden.platform.security.user;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.query.IFilter;

/**
 * DAO implementation of {@link IUserSecret}.
 *
 * @author TG Team
 *
 */
@EntityType(UserSecret.class)
public class UserSecretDao extends CommonEntityDao<UserSecret> implements IUserSecret {

    @Inject
    public UserSecretDao( final IFilter filter) {
        super(filter);
    }

}