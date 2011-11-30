package ua.com.fielden.platform.example.dynamiccriteria;

import ua.com.fielden.platform.equery.fetch;
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
	return userDao.findByKeyAndFetch(new fetch<User>(User.class).with("basedOnUser"), User.system_users.SU.name());
    }

}
