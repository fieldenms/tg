package ua.com.fielden.platform.ioc;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.hibernate.FlushMode;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.dao.annotations.Transactional;

/**
 * This intercepter is very similar in its behaviour to {@link SessionInterceptor} with major difference -- it does not require the invocation owner to be an instance of
 * {@link CommonEntityDao}, and it does not attempt to set a session to it. Should be associated strictly with methods annotated with {@link Transactional}.
 * <p>
 * So, the main and only objective of this class is to start a new transaction using a current Hibernate session if it has not been started yet, and commit it if the start was
 * initiated by the intercepter. This implementation handles nested method invocations annotated with {@link Transactional} gracefully.
 * <p>
 * Mixing annotations {@link Transactional} and {@link SessionRequired} is possible, but meaningless since intercepter for these annotated methods start a transaction if there no
 * active one.
 * 
 * @author 01es
 * 
 */
public class TransactionalInterceptor implements MethodInterceptor {
    private final SessionFactory sessionFactory;

    public TransactionalInterceptor(final SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public Object invoke(final MethodInvocation invocation) throws Throwable {
        final Session session = sessionFactory.getCurrentSession();
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
            tr.begin();
        }
        try {
            final Object result = invocation.proceed(); // this invocation could also be captured by TransactionalInterceptor
            if (shouldCommit && tr.isActive()) { // if this is the invocation that activated the current transaction then we should commit it
                tr.commit();
            }
            return result;
        } catch (final RuntimeException e) {
            if (tr.isActive()) { // if transaction is active and there was an exception then it should be rolled back
                tr.rollback();
            }
            e.printStackTrace();
            throw e;
        }
    }
}
