package ua.com.fielden.platform.test.ioc;

import jakarta.inject.Singleton;
import org.hibernate.Session;
import ua.com.fielden.platform.dao.ISessionEnabled;
import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.security.session.ISsoSessionController;
import ua.com.fielden.platform.security.user.User;

/// An SSO session controller for testing, whose invalidation of SSO sessions can be made to fail with an error, while enabled by [#failInvalidationWith(Error)].
///
/// Invalidation runs within a session scope, nested in the scope of the caller if it has one,
/// as it would in an implementation that deletes SSO sessions through a companion.
/// Otherwise, the controller does nothing, as the default implementation.
///
/// The controller is a singleton, so that a test configures the same instance that companions use.
/// Without a scope, Guice would create a new instance for each injection point: the instance a test obtains from the injector would differ from the one injected into `UserSessionDao`,
/// and making it fail would have no effect on the companion.
/// As a consequence, the session and the transaction GUID that `SessionInterceptor` assigns to the controller, as to any [ISessionEnabled], are shared by all its users.
/// This is harmless: the interceptor only assigns them, and the controller never reads them, so no behaviour depends on which invocation assigned them last.
///
@Singleton
public class SsoSessionControllerForTesting implements ISsoSessionController, ISessionEnabled {

    private volatile Error invalidationError;
    private Session session;
    private String transactionGuid;

    /// Makes invalidation fail with `error`, or, if `error` is `null`, makes it succeed.
    ///
    public void failInvalidationWith(final Error error) {
        this.invalidationError = error;
    }

    @Override
    public Result refresh(final String sid) {
        return Result.successful(sid);
    }

    @Override
    @SessionRequired
    public void invalidate(final String sid) {
        final Error error = invalidationError;
        if (error != null) {
            throw error;
        }
    }

    @Override
    public Session getSession() {
        return session;
    }

    @Override
    public void setSession(final Session session) {
        this.session = session;
    }

    @Override
    public String getTransactionGuid() {
        return transactionGuid;
    }

    @Override
    public void setTransactionGuid(final String guid) {
        this.transactionGuid = guid;
    }

    @Override
    public User getUser() {
        return null;
    }

}
