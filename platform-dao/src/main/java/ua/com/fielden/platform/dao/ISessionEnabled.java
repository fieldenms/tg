package ua.com.fielden.platform.dao;

import org.hibernate.Session;

import ua.com.fielden.platform.security.user.User;

/**
 * A contract for database transaction aware classes.
 *
 * @author TG Team
 *
 */
public interface ISessionEnabled {

    Session getSession();
    void setSession(final Session session);

    String getTransactionGuid();
    void setTransactionGuid(final String guid);

    /**
     * Returns current user. May return {@code null}.
     *
     * @return
     */
    User getUser();

}