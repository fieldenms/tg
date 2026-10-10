package ua.com.fielden.platform.test.ioc;

import jakarta.inject.Singleton;
import javax.transaction.Synchronization;
import org.hibernate.Session;
import ua.com.fielden.platform.dao.ISessionEnabled;
import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.security.session.ISsoSessionController;
import ua.com.fielden.platform.security.user.User;

/// An SSO session controller for testing, whose invalidation of SSO sessions can make the transaction within which it runs fail after it has been committed,
/// while enabled by [#failAfterCommitWith(RuntimeException)].
///
/// Invalidation runs within a session scope, nested in the scope of the caller if it has one,
/// as it would in an implementation that deletes SSO sessions through a companion.
/// Otherwise, the controller does nothing, as the default implementation.
///
/// The controller is a singleton, so that a test configures the same instance that companions use.
/// Without a scope, Guice would create a new instance for each injection point: the instance a test obtains from the injector would differ from the one injected into `UserSessionDao`,
/// and making it fail would have no effect on the companion.
/// As a consequence, the session and the transaction GUID that `SessionInterceptor` assigns to the controller, as to any [ISessionEnabled], are shared by all its users.
/// This is harmless in single-threaded tests: the controller reads its session only within `invalidate`, after the interceptor has assigned it for that invocation.
///
@Singleton
public class SsoSessionControllerForTesting implements ISsoSessionController, ISessionEnabled {

    private volatile RuntimeException afterCommitFailure;
    private Session session;
    private String transactionGuid;

    /// Makes the transaction within which SSO sessions are invalidated fail after it has been committed, with `failure` thrown by a synchronization that Hibernate notifies after the commit,
    /// or, if `failure` is `null`, makes invalidation succeed.
    ///
    public void failAfterCommitWith(final RuntimeException failure) {
        this.afterCommitFailure = failure;
    }

    @Override
    public Result refresh(final String sid) {
        return Result.successful(sid);
    }

    @Override
    @SessionRequired
    public void invalidate(final String sid) {
        final RuntimeException failure = afterCommitFailure;
        if (failure != null) {
            getSession().getTransaction().registerSynchronization(new Synchronization() {
                @Override
                public void beforeCompletion() {
                }

                @Override
                public void afterCompletion(final int status) {
                    throw failure;
                }
            });
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
