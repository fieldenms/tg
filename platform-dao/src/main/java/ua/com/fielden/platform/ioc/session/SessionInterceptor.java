package ua.com.fielden.platform.ioc.session;

import static java.util.UUID.randomUUID;

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

    private transient final Logger logger = Logger.getLogger(this.getClass());
    
    private ThreadLocal<String> transactionGuid = new ThreadLocal<>();

    public SessionInterceptor(final SessionFactory sessnioFactory) {
        this.sessionFactory = sessnioFactory;
    }

    @Override
    public Object invoke(final MethodInvocation invocation) throws Throwable {
        final ISessionEnabled invocationOwner = (ISessionEnabled) invocation.getThis();
        final Session session = sessionFactory.getCurrentSession();
        invocationOwner.setSession(session);
        final Transaction tr = session.getTransaction();
        /*
         * this variable indicates whether transaction should be handled in this method;
         * basically, if transaction is activated in this method then it should be committed only in this method.
         * therefore, shouldCommit is assigned true only when transaction is activated here.
        */
        final boolean shouldCommit = !tr.isActive();
        // activate transaction if it not active
        if (!tr.isActive()) {
            session.setFlushMode(FlushMode.COMMIT);
            logger.debug("Starting new DB transaction");
            tr.begin();
            logger.debug("Started new DB transaction");
            
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
        
        
        try {
            final Object result = invocation.proceed(); // this invocation could also be captured by SessionInterceptor
            if (shouldCommit && tr.isActive()) { // if this is the invocation that activated the current transaction then we should commit it
                logger.debug("Committing DB transaction");
                tr.commit();
                transactionGuid.remove();
                logger.debug("Committed DB transaction");
            } else if (session.isOpen()) {
                // should flush only if the current session is still open
                // this check was not needed before migrating off Hibernate 3.2.6 GA
                session.flush();
            }
            return result;
        } catch (final Exception e) {
            logger.warn(e);
            if (tr.isActive()) { // if transaction is active and there was an exception then it should be rollbacked
                logger.debug("Rolling back DB transaction");
                tr.rollback();
                transactionGuid.remove();
                logger.debug("Rolled back DB transaction");
            }
            throw e;
        }
    }
}
