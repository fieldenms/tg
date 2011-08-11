package ua.com.fielden.platform.test.domain.entities;

import ua.com.fielden.platform.security.user.User;

/**
 * Represents an application user, which is also a system user.
 *
 * @author 01es
 *
 */
public class Person extends User {
    private static final long serialVersionUID = 1L;

    /**
     * Constructor for Hibernate.
     */
    protected Person() {
	super(null, null);
    }

    /**
     * The main constructor.
     * @param name
     * @param desc
     */
    public Person(final String name, final String desc) {
	super(name, desc);
    }
}
