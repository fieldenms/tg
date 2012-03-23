package ua.com.fielden.platform.example.dynamiccriteria;

import ua.com.fielden.platform.entity.query.fetch;
import ua.com.fielden.platform.security.user.IUserDao2;
import ua.com.fielden.platform.security.user.User;

import com.google.inject.Inject;

public class NonBaseUserProvider extends EntitycentreUserProvider {

    @Inject
    public NonBaseUserProvider(final IUserDao2 userDao) {
	super(userDao);
    }

    @Override
    protected User initUser() {
	return userDao.findByKeyAndFetch(new fetch<User>(User.class).with("basedOnUser"), "DEMO");
    }

}
