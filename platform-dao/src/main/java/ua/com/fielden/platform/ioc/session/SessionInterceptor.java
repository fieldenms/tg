package ua.com.fielden.platform.ioc.session;

import com.google.inject.Provider;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;
import org.hibernate.FlushMode;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import ua.com.fielden.platform.dao.ISessionEnabled;
import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.ioc.session.exceptions.SessionScopingException;
import ua.com.fielden.platform.ioc.session.exceptions.TransactionCommitException;
import ua.com.fielden.platform.ioc.session.exceptions.TransactionRollbackDueToThrowable;
import ua.com.fielden.platform.security.user.User;

import java.util.stream.Stream;

import static java.util.UUID.randomUUID;
import static org.apache.logging.log4j.LogManager.getLogger;
import static ua.com.fielden.platform.dao.annotations.SessionRequired.ERR_NESTED_SCOPE_INVOCATION_IS_DISALLOWED;

/// Intercepts methods annotated with [SessionRequired] to inject Hibernate session before the actual method execution.
/// Nested invocation of methods annotated with [SessionRequired] is supported.
/// For example, method `findAll()` could invoke method `findByCriteria()` — both with [SessionRequired].
///
/// A very important functionality provided by this interceptor is transaction management:
///   - If the current session has no active transaction then it is activated and the current method invocation is marked as the one that should commit it.
///   - If the current session has an active transaction then the current method invocation is marked as the one that should NOT commit it.
///   - In case of an exception, which could occur during method invocation, the transaction is rolled back if it is active and the exception is propagated up.
///
/// The last item ensures that any exception at any level of method invocation would ensure transaction rollback.
/// If transaction is not active at the time of rollback then that means it has already been rolled back.
/// Please note that transaction can be started outside of this interceptor, which means it will not be committed within it, and the transaction originator is responsible for commit.
/// At the same time, if an exception occurs then transaction will be rolled back.
///
/// A failure to *commit* is reported rather than swallowed.
/// Committing is the point at which a unit of work becomes durable, so a failure there means nothing was persisted, however successfully the method itself ran.
/// [TransactionCommitException] is thrown for this, and only after the session has been closed, so that cleanup is never skipped.
/// It is deliberately distinct from a business failure: the work was valid and its statements were accepted, but the transaction could not be made durable — typically because of an infrastructure failure, such as a database failover or a terminated connection.
///
/// Two further behaviours are not apparent from a call site:
///   - The session is put into [FlushMode#COMMIT], so Hibernate never auto-flushes.
///     DML reaches the database when something flushes it explicitly, and otherwise during the commit.
///   - If an intercepted method returns a [Stream], the transaction outlives that method call and is committed when the stream is closed.
///     Such a stream *must* be closed, ideally via try-with-resources, or its transaction and connection are never released.
///
/// Finally, a GUID is generated per transaction and assigned to the invocation owner, which is how a single unit of work is identified downstream — by auditing, for example.
///
public class SessionInterceptor implements MethodInterceptor {
    public static final String
            MSG_CLOSING_SESSION = "[%s] Closing session.",
            MSG_CLOSED_SESSION = "[%s] Closed session.",
            WARN_TRANSACTION_ROLLBACK = "[%s] Transaction completed (rolled back) with error.",
            ERR_COULD_NOT_CLOSE_SESSION = "[%s] Could not close session.",
            ERR_COULD_NOT_COMMIT = "[%s] Could not commit transaction.";

    private static final Logger LOGGER = getLogger(SessionInterceptor.class);

    private final Provider<? extends SessionFactory> sessionFactory;
    private final ThreadLocal<String> transactionGuid = new ThreadLocal<>();

    public SessionInterceptor(final Provider<? extends SessionFactory> sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public Object invoke(final MethodInvocation invocation) throws Throwable {
        final ISessionEnabled invocationOwner = (ISessionEnabled) invocation.getThis();
        final Session session = sessionFactory.get().getCurrentSession();
        final Transaction tr = session.getTransaction();
        final User user = invocationOwner.getUser();

        try {
            // This variable indicates whether a transaction commit should be handled in this method invocation.
            // Basically, if a transaction is activated in this method, then it should be committed only in this method.
            // Therefore, shouldCommit is assigned true only when the transaction is activated here.
            final boolean shouldCommit = initTransaction(invocationOwner, session, tr, user);
            
            // if should not commit, which means the session was initiated earlier in the call stack,
            // and support for nested calls is not allowed, then an exception should be thrown.
            if (!shouldCommit && !invocation.getStaticPart().getAnnotation(SessionRequired.class).allowNestedScope()) {
                throw new SessionScopingException(ERR_NESTED_SCOPE_INVOCATION_IS_DISALLOWED.formatted(invocation.getMethod().getDeclaringClass().getName(), invocation.getMethod().getName()));
            }
            
            // now let's proceed with the actual method invocation, which may throw an exception... or even a throwable...
            final Object result = invocation.proceed();
            
            // if this is the invocation that activated the current transaction, then we should commit it,
            // but only if the result of invocation is not a stream -- in that case, closing of the session is the responsibility of that stream
            if (shouldCommit && tr.isActive()) {
                // if the result is a stream, then the current transaction becomes associated with that stream
                // and needs to be committed once the stream has been processed
                if (result instanceof Stream<?> stream) {
                    return stream.onClose(() -> {
                        try {
                            LOGGER.debug(() -> "[%s] Committing DB transaction on stream close.".formatted(user));
                            commitTransactionAndCloseSession(session, tr, user);
                            LOGGER.debug(() -> "[%s] Committed DB transaction on stream close.".formatted(user));
                        } catch (final Exception ex) {
                            LOGGER.fatal(() -> "[%s] Could not commit DB transaction on stream close.".formatted(user), ex);
                            throw ex;
                        }
                    });
                }
                // otherwise, commit the current transaction
                else {
                    LOGGER.debug(() -> "[%s] Committing DB transaction".formatted(user));
                    commitTransactionAndCloseSession(session, tr, user);
                    LOGGER.debug(() -> "[%s] Committed DB transaction".formatted(user));
                    return result;
                }
            }
            // otherwise, this is the case of a nested transaction
            // should flush only if the current session is still open
            // this check was not needed before migrating off Hibernate 3.2.6 GA
            if (session.isOpen()) {
                session.flush();
            }
            return result;
        } catch (final Throwable e) {
            throw completeTransactionWithError(session, tr, e, user);
        }
    }

    private boolean initTransaction(final ISessionEnabled invocationOwner, final Session session, final Transaction tr, final User user) {
        invocationOwner.setSession(session);
        final boolean shouldCommit = !tr.isActive();
        if (!tr.isActive()) {
            LOGGER.debug(() -> "[%s] Starting new DB transaction".formatted(user));
            tr.begin();
            session.setHibernateFlushMode(FlushMode.COMMIT);
            LOGGER.debug(() -> "[%s] Started new DB transaction".formatted(user));
            
            // generate a GUID for the current transaction
            if (!StringUtils.isEmpty(transactionGuid.get())) {
                throw new SessionScopingException("There should have been no transaction GUID assigned yet for a new session scope."); 
            }
            final String guid = randomUUID().toString();
            transactionGuid.set(guid);
            invocationOwner.setTransactionGuid(guid);
        } else {
            // assigned a transaction GUID, which should already be generated
            final String guid = transactionGuid.get();
            if (StringUtils.isEmpty(guid)) {
                throw new SessionScopingException("A nested session scope is missing a transaction GUID."); 
            }
            
            invocationOwner.setTransactionGuid(guid);
        }
        
        return shouldCommit;
    }

    private Exception completeTransactionWithError(final Session session, final Transaction tr, final Throwable ex, final User user) {
        if (ex instanceof Result) {
            LOGGER.debug(() -> WARN_TRANSACTION_ROLLBACK.formatted(user), ex);  // most Result exceptions are validation errors, which are more relevant for debug messages
        } else if (ex instanceof SessionScopingException) {
            LOGGER.error(() -> WARN_TRANSACTION_ROLLBACK.formatted(user), ex); // transactional scoping errors should be reported as errors
        } else {
            LOGGER.warn(() -> WARN_TRANSACTION_ROLLBACK.formatted(user), ex); // otherwise, warning
        }
        try {
            if (tr.isActive()) { // if transaction is active and there was an exception then it should be rollbacked
                LOGGER.debug(() -> "[%s] Rolling back DB transaction".formatted(user));
                rollbackTransactionAndCloseSession(session, tr, user);
                LOGGER.debug(() -> "[%s] Rolled back DB transaction".formatted(user));
            }
        } finally {
            transactionGuid.remove();
        }
        return ex instanceof Exception ? (Exception) ex : new TransactionRollbackDueToThrowable(ex);
    }

    private void commitTransactionAndCloseSession(final Session session, final Transaction tr, final User user) {
        try {
            if (tr.isActive()) {
                tr.commit();
            }
        } catch (final Exception ex) {
            LOGGER.error(() -> ERR_COULD_NOT_COMMIT.formatted(user), ex);
        } finally {
            transactionGuid.remove();
        }
        
        try {
            LOGGER.debug(() -> MSG_CLOSING_SESSION.formatted(user));
            if (session.isOpen()) {
                session.close();
            }
            LOGGER.debug(() -> MSG_CLOSED_SESSION.formatted(user));
        } catch (final Exception ex) {
            LOGGER.error(format("[%s] Could not close session.", user), ex);
        }
    }
    
    private static void rollbackTransactionAndCloseSession(final Session session, final Transaction tr, final User user) {
        try {
            if (tr.isActive()) {
                tr.rollback();
            }
        } catch (final Exception ex) {
            LOGGER.error(() -> "[%s] Could not rollback transaction. Transaction active: [%s].".formatted(user, tr.isActive()), ex);
        }
        
        try {
            LOGGER.debug(() -> MSG_CLOSING_SESSION.formatted(user));
            if (session.isOpen()) {
                session.close();
            }
            LOGGER.debug(() -> MSG_CLOSED_SESSION.formatted(user));
        } catch (final Exception ex) {
            LOGGER.error(() -> ERR_COULD_NOT_CLOSE_SESSION.formatted(user), ex);
        }
    }

}
