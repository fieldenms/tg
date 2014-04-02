package ua.com.fielden.platform.ioc;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.hibernate.FlushMode;
import org.hibernate.Hibernate;
import org.hibernate.LockMode;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import ua.com.fielden.platform.entity.AbstractEntity;

/**
 * Intercepter for handling accessors for entity property supporting proxying due to Hibernate lazy initialization.
 * 
 * IMPORTANT: This is still an experimental support for handling lazy loading by re-associating entity with a session.
 * 
 * @author 01es
 * 
 */
public class PropertyProxyInterceptor implements MethodInterceptor {
    private final SessionFactory sessionFactory;

    public PropertyProxyInterceptor(final SessionFactory sessnioFactory) {
        this.sessionFactory = sessnioFactory;
    }

    /**
     * Re-associates entity instance with Hibernate session.
     */
    @Override
    public Object invoke(final MethodInvocation invocation) throws Throwable {
        final AbstractEntity<?> entity = (AbstractEntity<?>) invocation.getThis(); // get the property owner
        // if entity is no yet persisted then no proxy handling is required
        if (!entity.isPersisted()) {
            return invocation.proceed();
        }

        // get current session and re-associated entity-owner with it by calling read lock
        final Session session = sessionFactory.getCurrentSession();
        final Transaction tr = session.getTransaction();
        /*
         * this variable indicates whether transaction should be handled in this method;
         * basically, if transaction is activated in this method then it should be committed only in this method.
         * therefore, shouldCommit is assigned true only when transaction is activated here.
        */
        final boolean shouldCommit = !tr.isActive() ? true : false;
        // activate transaction if it not active
        if (!tr.isActive()) {
            session.setFlushMode(FlushMode.COMMIT);
            tr.begin();
        }
        try {
            session.lock(entity, LockMode.READ); // read lock enforces reloading of the entity from the db rather than cache
            final Object result = invocation.proceed();
            if (!Hibernate.isInitialized(result)) {
                Hibernate.initialize(result);
            }
            if (shouldCommit && tr.isActive()) { // if this is the invocation that activated the current transaction then we should commit it
                tr.commit();
            }
            return result;
        } catch (final RuntimeException e) {
            if (tr.isActive()) { // if transaction is active and there was an exception then it should be rollbacked
                tr.rollback();
            }
            throw new RuntimeException(e);
        }
    }
}
