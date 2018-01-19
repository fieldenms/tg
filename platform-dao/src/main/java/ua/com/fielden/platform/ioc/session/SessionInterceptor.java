package ua.com.fielden.platform.ioc.session;

import static java.lang.String.format;
import static java.util.UUID.randomUUID;
import static ua.com.fielden.platform.dao.annotations.SessionRequired.ERR_NESTED_SCOPE_INVOCATION_IS_DISALLOWED;

import java.util.stream.Stream;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.FlushMode;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import ua.com.fielden.platform.dao.ISessionEnabled;
import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.ioc.session.exceptions.SessionScopingException;
import ua.com.fielden.platform.ioc.session.exceptions.TransactionRollbackDueToThrowable;

/**
 * Intercepts methods annotated with {@link SessionRequired} to inject Hibernate session before the actual method execution. Nested invocation of methods annotated with
 * {@link SessionRequired} is supported. For example, method <code>findAll()</code> could invoke method <code>findByCriteria()</code> -- both with {@link SessionRequired}.
 * <p>
 * A very important functionality provided by this intercepter is transaction management:
 * <ul>
 * <li>If the current session has no active transaction then it is activated and the current method invocation is marked as the one that should commit it.</li>
 * <li>If the current session has an active transaction then the current method invocation is marked as the one that should NOT commit it.</li>
 * <li>In case of an exception, which could occur during method invocation, the transaction is rollbacked if it is active and the exception is propagated up.</li> *
 * </ul>
 *
 * The last item ensures that any exception at any level of method invocation would ensure transaction rollback. If transaction is not active at the time of rollback then that
 * means it has already been rollbacked.
 *
 * Please note that transaction can be started outside of this intercepter, which means it will not be committed within it, and the transaction originator is responsible for
 * commit. At the same time, if an exception occurs then transaction will be rollbacked.
 *
 * @author TG Team
 *
 */
public class SessionInterceptor implements MethodInterceptor {
    private final SessionFactory sessionFactory;

    private static final Logger LOGGER = Logger.getLogger(SessionInterceptor.class);
    
    private ThreadLocal<String> transactionGuid = new ThreadLocal<>();

    public SessionInterceptor(final SessionFactory sessnioFactory) {
        this.sessionFactory = sessnioFactory;
    }

    @Override
    public Object invoke(final MethodInvocation invocation) throws Throwable {
        final ISessionEnabled invocationOwner = (ISessionEnabled) invocation.getThis();
        final Session session = sessionFactory.getCurrentSession();
        final Transaction tr = session.getTransaction();
        
        try {
            // This variable indicates whether transaction commit should be handled in this method invocation.
            // Basically, if transaction is activated in this method then it should be committed only in this method.
            // Therefore, shouldCommit is assigned true only when transaction is activated here.
            final boolean shouldCommit = initTransaction(invocationOwner, session, tr);
            
            // if should not commit, which means the session was initiated earlier in the calls stack, 
            // and support for nested calls is not allowed then an exception should be thrown.
            if (!shouldCommit && !invocation.getStaticPart().getAnnotation(SessionRequired.class).allowNestedScope()) {
                throw new SessionScopingException(format(ERR_NESTED_SCOPE_INVOCATION_IS_DISALLOWED, invocation.getMethod().getDeclaringClass().getName(), invocation.getMethod().getName()));
            }
            
            // now let's proceed with the actual method invocation, which may throw an exception... or even a throwable...
            final Object result = invocation.proceed();
            
            // if this is the invocation that activated the current transaction then we should commit it
            // but only of the result of invocation is not a stream -- in that case closing of the session is the responsibility of that stream
            if (shouldCommit && tr.isActive()) {
                // if the result is a stream than the current transaction becomes associated with that stream
                // and needs to be committed once the stream has been processed
                if (result instanceof Stream) {
                    ((Stream<?>) result).onClose(() -> {
                        try {
                            LOGGER.debug("Committing DB transaction on stream close.");
                            commitTransactionAndCloseSession(session, tr);
                            LOGGER.debug("Committed DB transaction on stream close.");
                        } catch (final Exception ex) {
                            LOGGER.fatal("Could not commit DB transaction on stream close.", ex);
                            throw ex;
                        }
                    });
                } else { // otherwise, commit the current transaction
                    LOGGER.debug("Committing DB transaction");
                    commitTransactionAndCloseSession(session, tr);
                    LOGGER.debug("Committed DB transaction");
                }
            } else if (session.isOpen()) { 
                // this is the case of a nested transaction
                // should flush only if the current session is still open
                // this check was not needed before migrating off Hibernate 3.2.6 GA
                session.flush();
            }
            return result;
        } catch (final Throwable e) {
            throw completeTransactionWithError(session, tr, e);
        }
    }

    private boolean initTransaction(final ISessionEnabled invocationOwner, final Session session, final Transaction tr) {
        invocationOwner.setSession(session);
        final boolean shouldCommit = !tr.isActive();
        if (!tr.isActive()) {
            session.setFlushMode(FlushMode.COMMIT);
            LOGGER.debug("Starting new DB transaction");
            tr.begin();
            LOGGER.debug("Started new DB transaction");
            
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

    private Exception completeTransactionWithError(final Session session, final Transaction tr, final Throwable e) {
        LOGGER.warn(e);
        transactionGuid.remove();
        if (tr.isActive()) { // if transaction is active and there was an exception then it should be rollbacked
            LOGGER.debug("Rolling back DB transaction");
            rollbackTransactionAndCloseSession(session, tr);
            LOGGER.debug("Rolled back DB transaction");
        }
        
        return e instanceof Exception ? (Exception) e : new TransactionRollbackDueToThrowable(e);
    }

    private void commitTransactionAndCloseSession(final Session session, final Transaction tr) {
        try {
            if (tr.isActive()) {
                tr.commit();
            }
        } catch (final Exception ex) {
            LOGGER.error("Could not commit transaction.", ex);
        } finally {
            transactionGuid.remove();
        }
        
        try {
            LOGGER.debug("Closing session.");
            if (session.isOpen()) {
                session.close();
            }
            LOGGER.debug("Closed session.");
        } catch (final Exception ex) {
            LOGGER.error("Could not close session.", ex);
        }
    }
    
    private static void rollbackTransactionAndCloseSession(final Session session, final Transaction tr) {
        try {
            if (tr.isActive()) {
                tr.rollback();
            }
        } catch (final Exception ex) {
            LOGGER.error("Could not commit transaction.", ex);
        }
        
        try {
            LOGGER.debug("Closing session.");
            if (session.isOpen()) {
                session.close();
            }
            LOGGER.debug("Closed session.");
        } catch (final Exception ex) {
            LOGGER.error("Could not close session.", ex);
        }
    }
    
}
