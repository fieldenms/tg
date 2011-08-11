package ua.com.fielden.web.security;

import ua.com.fielden.platform.security.user.User;

/**
 * Test user entity class.
 *
 * @author TG Team
 *
 */
public class DummyUser extends User {
    private static final long serialVersionUID = 1L;

    protected DummyUser() {
	super(null, null);
    }

    public DummyUser(final String name, final String desc) {
	super(name, desc);

    }

}
