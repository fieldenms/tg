package ua.com.fielden.platform.example.entities;

import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.security.user.User;

/**
 * Represents an application user, which is also a system user.
 * 
 * @author 01es
 * 
 */
@KeyTitle(value = "Person No.", desc = "Person No. description")
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
     * 
     * @param name
     * @param desc
     */
    public Person(final String name, final String desc) {
        super(name, desc);
    }
}
