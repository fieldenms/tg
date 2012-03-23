package ua.com.fielden.platform.example.dynamiccriteria;

import ua.com.fielden.platform.security.user.IUserDao2;
import ua.com.fielden.platform.security.user.IUserProvider;
import ua.com.fielden.platform.security.user.User;

public abstract class EntitycentreUserProvider implements IUserProvider {

    protected final IUserDao2 userDao;

    private User user = null;

    public EntitycentreUserProvider(final IUserDao2 userDao){
	this.userDao = userDao;
    }

    @Override
    public final User getUser() {
	if(user == null){
	    user = initUser();
	}
	return user;
    }

    abstract protected User initUser();

}
