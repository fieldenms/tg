package ua.com.fielden.platform.dao2;

import org.hibernate.Session;

public interface ISessionEnabled {
    Session getSession();

    void setSession(final Session session);
}
