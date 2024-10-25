package ua.com.fielden.platform.ioc.session;

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
import ua.com.fielden.platform.ioc.session.exceptions.TransactionRollbackDueToThrowable;
import ua.com.fielden.platform.security.user.User;

import java.util.stream.Stream;

import static java.lang.String.format;
import static java.util.UUID.randomUUID;
import static org.apache.logging.log4j.LogManager.getLogger;
import static ua.com.fielden.platform.dao.annotations.SessionRequired.ERR_NESTED_SCOPE_INVOCATION_IS_DISALLOWED;

/**
 * Intercepts methods annotated with {@link SessionRequired} to inject Hibernate session before the actual method execution. Nested invocation of methods annotated with
 * {@link SessionRequired} is supported. For example, method <code>findAll()</code> could invoke method <code>findByCriteria()</code> -- both with {@link SessionRequired}.
 * <p>
 * A very important functionality provided by this interceptor is transaction management:
 * <ul>
 * <li>If the current session has no active transaction then it is activated and the current method invocation is marked as the one that should commit it.</li>
 * <li>If the current session has an active transaction then the current method invocation is marked as the one that should NOT commit it.</li>
 * <li>In case of an exception, which could occur during method invocation, the transaction is rollbacked if it is active and the exception is propagated up.</li> *
 * </ul>
 *
 * The last item ensures that any exception at any level of method invocation would ensure transaction rollback. If transaction is not active at the time of rollback then that
 * means it has already been rollbacked.
 *
 * Please note that transaction can be started outside of this interceptor, which means it will not be committed within it, and the transaction originator is responsible for
 * commit. At the same time, if an exception occurs then transaction will be rolled back.
 *
 * @author TG Team
 *
 */
public class SessionInterceptor implements MethodInterceptor {
    private final SessionFactory sessionFactory;

    public static final String WARN_TRANSACTION_ROLLBACK = "[%s] Transaction completed (rolled back) with error.";
    private static final Logger LOGGER = getLogger(SessionInterceptor.class);
    
    private final ThreadLocal<String> transactionGuid = new ThreadLocal<>();

    public SessionInterceptor(final SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public Object invoke(final MethodInvocation invocation) throws Throwable {
        final ISessionEnabled invocationOwner = (ISessionEnabled) invocation.getThis();
        final Session session = sessionFactory.getCurrentSession();
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
                throw new SessionScopingException(format(ERR_NESTED_SCOPE_INVOCATION_IS_DISALLOWED, invocation.getMethod().getDeclaringClass().getName(), invocation.getMethod().getName()));
            }
            
            // now let's proceed with the actual method invocation, which may throw an exception... or even a throwable...
            final Object result = invocation.proceed();
            
            // if this is the invocation that activated the current transaction, then we should commit it,
            // but only if the result of invocation is not a stream -- in that case, closing of the session is the responsibility of that stream
            if (shouldCommit && tr.isActive()) {
                // if the result is a stream, then the current transaction becomes associated with that stream
                // and needs to be committed once the stream has been processed
                if (result instanceof Stream stream) {
                    return stream.onClose(() -> {
                        try {
                            LOGGER.debug(format("[%s] Committing DB transaction on stream close.", user));
                            commitTransactionAndCloseSession(session, tr, user);
                            LOGGER.debug(format("[%s] Committed DB transaction on stream close.", user));
                        } catch (final Exception ex) {
                            LOGGER.fatal(format("[%s] Could not commit DB transaction on stream close.", user), ex);
                            throw ex;
                        }
                    });
                }
                // otherwise, commit the current transaction
                else {
                    LOGGER.debug(format("[%s] Committing DB transaction", user));
                    commitTransactionAndCloseSession(session, tr, user);
                    LOGGER.debug(format("[%s] Committed DB transaction", user));
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
            LOGGER.debug(format("[%s] Starting new DB transaction", user));
            tr.begin();
            session.setHibernateFlushMode(FlushMode.COMMIT);
            LOGGER.debug(format("[%s] Started new DB transaction", user));
            
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
            LOGGER.debug(format(WARN_TRANSACTION_ROLLBACK, user), ex);  // most Result exceptions are validation errors, which are more relevant for debug messages
        } else if (ex instanceof SessionScopingException) {
            LOGGER.error(format(WARN_TRANSACTION_ROLLBACK, user), ex); // transactional scoping errors should be reported as errors
        } else {
            LOGGER.warn(format(WARN_TRANSACTION_ROLLBACK, user), ex); // otherwise, warning
        }
        try {
            if (tr.isActive()) { // if transaction is active and there was an exception then it should be rollbacked
                LOGGER.debug(format("[%s] Rolling back DB transaction", user));
                rollbackTransactionAndCloseSession(session, tr, user);
                LOGGER.debug(format("[%s] Rolled back DB transaction", user));
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
            LOGGER.error(format("[%s] Could not commit transaction.", user), ex);
        } finally {
            transactionGuid.remove();
        }
        
        try {
            LOGGER.debug(format("[%s] Closing session.", user));
            if (session.isOpen()) {
                session.close();
            }
            LOGGER.debug(format("[%s] Closed session.", user));
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
            LOGGER.error(format("[%s] Could not rollback transaction. Transaction active: [%s].", user, tr.isActive()), ex);
        }
        
        try {
            LOGGER.debug(format("[%s] Closing session.", user));
            if (session.isOpen()) {
                session.close();
            }
            LOGGER.debug(format("[%s] Closed session.", user));
        } catch (final Exception ex) {
            LOGGER.error(format("[%s] Could not close session.", user), ex);
        }
    }

}