package ua.com.fielden.platform.hibernate.session_context;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import org.apache.log4j.Logger;
import org.hibernate.ConnectionReleaseMode;
import org.hibernate.HibernateException;
import org.hibernate.SessionFactory;
import org.hibernate.classic.Session;
import org.hibernate.context.CurrentSessionContext;
import org.hibernate.engine.SessionFactoryImplementor;

/**
 * This is a conversational Hibernate session context, which provides Hibernate session in a specific
 *
 * @author TG Team
 *
 */
public class ConversationalCurrentSessionContext implements CurrentSessionContext {
    private static final long serialVersionUID = 1L;

    protected static final Logger log = Logger.getLogger(ConversationalCurrentSessionContext.class);
    private static final Class<?>[] SESS_PROXY_INTERFACES = new Class[] { //
        org.hibernate.classic.Session.class, //
        org.hibernate.engine.SessionImplementor.class,//
        org.hibernate.jdbc.JDBCContext.Context.class, //
        org.hibernate.event.EventSource.class };

    protected final SessionFactoryImplementor factory;

    public ConversationalCurrentSessionContext(final SessionFactoryImplementor factory) {
        this.factory = factory;
    }

    @Override
    public Session currentSession() throws HibernateException {
        Session current = getExistingSessionIfPresent(factory);
        if (current == null) {
            current = buildNewSession();
            // wrap the session in the transaction-protection proxy
            if (needsWrapping(current)) {
                current = wrap(current);
            }
            // then bind it
            doBind(current, factory);
        }
        return current;

        //        //HttpServletRequest request = null;
        //
        //        try {
        //            //request = JsfUtils.getCurrentHttpRequest();
        //        } catch (final Exception e) {
        //            log.debug("No current http request in faces context, returning default conversation.");
        //        }
        //
        //        if (request == null) {
        //            return (Session) ConversationManager.getDefaultConversationSession();
        //        } else {
        //            return (Session) ConversationManager.getSessionForRequest(request);
        //        }
    }

    private boolean needsWrapping(final Session session) {
        // try to make sure we don't wrap and already wrapped session
        return session != null && !Proxy.isProxyClass(session.getClass())
                || (Proxy.getInvocationHandler(session) != null && !(Proxy.getInvocationHandler(session) instanceof TransactionProtectionWrapper));
    }

    protected Session wrap(final Session session) {
        final TransactionProtectionWrapper wrapper = new TransactionProtectionWrapper(session);
        final Session wrapped = (Session) Proxy.newProxyInstance(Session.class.getClassLoader(), SESS_PROXY_INTERFACES, wrapper);
        // yick!  need this for proper serialization/deserialization handling...
        wrapper.setWrapped(wrapped);
        return wrapped;
    }

    /**
     * Unassociate a previously bound session from the current context of execution.
     *
     * @return The session which was unbound.
     */
    public static Session unbind(final SessionFactory factory) {
        return doUnbind(factory, true);
    }

    private static Session doUnbind(final SessionFactory factory, final boolean releaseMapIfEmpty) {
        return null;
        //        Map sessionMap = sessionMap();
        //        Session session = null;
        //        if ( sessionMap != null ) {
        //            session = ( Session ) sessionMap.remove( factory );
        //            if ( releaseMapIfEmpty && sessionMap.isEmpty() ) {
        //                context.set( null );
        //            }
        //        }
        //        return session;
    }

    private static void doBind(final org.hibernate.Session session, final SessionFactory factory) {
        /*        Map sessionMap = sessionMap();
                if ( sessionMap == null ) {
                    sessionMap = new HashMap();
                    context.set( sessionMap );
                }
                sessionMap.put( factory, session );*/
    }

    private static Session getExistingSessionIfPresent(final SessionFactory factory) {
        return null;
        //        Map sessionMap = sessionMap();
        //        if ( sessionMap == null ) {
        //            return null;
        //        }
        //        else {
        //            return ( Session ) sessionMap.get( factory );
        //        }
    }

    /**
     * Strictly provided for subclassing purposes; specifically to allow long-session support.
     * <p/>
     * This implementation always just opens a new session.
     *
     * @return the built or (re)obtained session.
     */
    protected Session buildNewSession() {
        return factory.openSession(null, isAutoFlushEnabled(), isAutoCloseEnabled(), getConnectionReleaseMode());
    }

    /**
     * Mainly for subclass usage. This impl always returns true.
     *
     * @return Whether or not the the session should be closed by transaction completion.
     */
    protected boolean isAutoCloseEnabled() {
        return true;
    }

    /**
     * Mainly for subclass usage. This impl always returns true.
     *
     * @return Whether or not the the session should be flushed prior transaction completion.
     */
    protected boolean isAutoFlushEnabled() {
        return true;
    }

    /**
     * Mainly for subclass usage. This impl always returns after_transaction.
     *
     * @return The connection release mode for any built sessions.
     */
    protected ConnectionReleaseMode getConnectionReleaseMode() {
        return factory.getSettings().getConnectionReleaseMode();
    }

    /**
     * This is an interceptor (pure Java) for Session object, which ensures that all relevant methods are invoked on an active transaction.
     * Otherwise, an exception is thrown.
     */
    private class TransactionProtectionWrapper implements InvocationHandler, Serializable {
        private static final long serialVersionUID = 1L;

        private final Session realSession;
        private Session wrappedSession;

        public TransactionProtectionWrapper(final Session realSession) {
            this.realSession = realSession;
        }

        @Override
        public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
            try {
                // If close() is called, guarantee unbind()
                if ("close".equals(method.getName())) {
                    unbind(realSession.getSessionFactory());
                } else if ("toString".equals(method.getName()) || "equals".equals(method.getName()) || "hashCode".equals(method.getName())
                        || "getStatistics".equals(method.getName()) || "isOpen".equals(method.getName())) {
                    // allow these to go through the the real session no matter what
                } else if (!realSession.isOpen()) {
                    // essentially, if the real session is closed allow any
                    // method call to pass through since the real session
                    // will complain by throwing an appropriate exception;
                    // NOTE that allowing close() above has the same basic effect,
                    //   but we capture that there simply to perform the unbind...
                } else if (!realSession.getTransaction().isActive()) {
                    // limit the methods available if no transaction is active
                    if ("beginTransaction".equals(method.getName()) || "getTransaction".equals(method.getName()) || "isTransactionInProgress".equals(method.getName())
                            || "setFlushMode".equals(method.getName()) || "getSessionFactory".equals(method.getName())) {
                        log.trace("allowing method [" + method.getName() + "] in non-transacted context");
                    } else if ("reconnect".equals(method.getName()) || "disconnect".equals(method.getName())) {
                        // allow these (deprecated) methods to pass through
                    } else {
                        throw new HibernateException(method.getName() + " is not valid without active transaction");
                    }
                }
                log.trace("allowing proxied method [" + method.getName() + "] to proceed to real session");
                return method.invoke(realSession, args);
            } catch (final InvocationTargetException e) {
                if (e.getTargetException() instanceof RuntimeException) {
                    throw (RuntimeException) e.getTargetException();
                } else {
                    throw e;
                }
            }
        }

        public void setWrapped(final Session wrapped) {
            this.wrappedSession = wrapped;
        }

        // serialization ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

        private void writeObject(final ObjectOutputStream oos) throws IOException {
            // if a ThreadLocalSessionContext-bound session happens to get
            // serialized, to be completely correct, we need to make sure
            // that unbinding of that session occurs.
            oos.defaultWriteObject();

            if (getExistingSessionIfPresent(factory) == wrappedSession) {
                unbind(factory);
            }
        }

        private void readObject(final ObjectInputStream ois) throws IOException, ClassNotFoundException {
            // on the inverse, it makes sense that if a ThreadLocalSessionContext-
            // bound session then gets deserialized to go ahead and re-bind it to
            // the ThreadLocalSessionContext session map.
            ois.defaultReadObject();
            doBind(wrappedSession, factory);
        }
    }

}