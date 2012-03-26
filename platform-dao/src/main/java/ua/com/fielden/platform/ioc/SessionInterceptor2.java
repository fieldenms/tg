package ua.com.fielden.platform.ioc;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.hibernate.FlushMode;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.dao2.ISessionEnabled;

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
 * @author 01es
 *
 */
public class SessionInterceptor2 implements MethodInterceptor {
    private final SessionFactory sessionFactory;

    public SessionInterceptor2(final SessionFactory sessnioFactory) {
	this.sessionFactory = sessnioFactory;
    }

    @Override
    public Object invoke(final MethodInvocation invocation) throws Throwable {
	final ISessionEnabled dao = (ISessionEnabled) invocation.getThis();
	final Session session = sessionFactory.getCurrentSession();
	dao.setSession(session);
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
	    final Object result = invocation.proceed(); // this invocation could also be captured by SessionInterceptor
	    if (shouldCommit && tr.isActive()) { // if this is the invocation that activated the current transaction then we should commit it
		tr.commit();
	    } else {
		session.flush();
	    }
	    return result;
	} catch (final RuntimeException e) {
	    if (tr.isActive()) { // if transaction is active and there was an exception then it should be rollbacked
		tr.rollback();
	    }
	    throw e;
	}
    }
}
