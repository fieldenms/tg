package ua.com.fielden.platform.example.dynamiccriteria;

import ua.com.fielden.platform.security.user.IUserDao;
import ua.com.fielden.platform.security.user.User;

import com.google.inject.Inject;

import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetch;

public class NonBaseUserProvider extends EntitycentreUserProvider {

    @Inject
    public NonBaseUserProvider(final IUserDao userDao) {
	super(userDao);
    }

    @Override
    protected User initUser() {
	return userDao.findByKeyAndFetch(fetch(User.class).with("basedOnUser"), "DEMO");
    }

}
