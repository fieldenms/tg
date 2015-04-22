package ua.com.fielden.platform.security.session;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.swing.review.annotations.EntityType;

import com.google.inject.Inject;

/**
 * DAO implementation for companion object {@link IUserSession}.
 *
 * @author Developers
 *
 */
@EntityType(UserSession.class)
public class UserSessionDao extends CommonEntityDao<UserSession> implements IUserSession {

    @Inject
    public UserSessionDao(final IFilter filter) {
        super(filter);

    }
}