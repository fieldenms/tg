package ua.com.fielden.platform.example.dynamiccriteria;

import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetch;
import ua.com.fielden.platform.security.provider.IUserController;
import ua.com.fielden.platform.security.user.IUserDao;
import ua.com.fielden.platform.security.user.User;

import com.google.inject.Inject;

public class BaseUserProvider extends EntitycentreUserProvider {

    @Inject
    public BaseUserProvider(final IUserDao userDao) {
        super(userDao);
    }

    @Override
    protected User initUser() {
        return userDao.findByKeyAndFetch(fetch(User.class).with("basedOnUser"), User.system_users.SU.name());
    }

    @Override
    public void setUsername(final String username, final IUserController controller) {
    }

}
