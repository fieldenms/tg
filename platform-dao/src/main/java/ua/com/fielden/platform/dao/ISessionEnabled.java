package ua.com.fielden.platform.dao;

import org.hibernate.Session;

public interface ISessionEnabled {
    
    Session getSession();
    void setSession(final Session session);
    
    String getTransactionGuid();
    void setTransactionGuid(final String guid);
    
}
