package ua.com.fielden.platform.dao;

import jakarta.annotation.Nullable;
import org.hibernate.Session;

import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.security.user.User;

/// A contract for database transaction aware classes.
///
/// This interface must be implemented for a class to be able to use [SessionRequired].
///
public interface ISessionEnabled {

    Session getSession();

    void setSession(final Session session);

    String getTransactionGuid();

    void setTransactionGuid(final String guid);

    /// Returns the current user, which may be missing.
    ///
    @Nullable User getUser();

}