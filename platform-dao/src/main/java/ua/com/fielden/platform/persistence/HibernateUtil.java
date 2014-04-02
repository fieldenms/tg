package ua.com.fielden.platform.persistence;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Interceptor;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;

/**
 * Basic Hibernate helper class for Hibernate configuration and startup.
 * <p>
 * Uses an initialiser to read startup options and initialise <tt>Configuration</tt> and <tt>SessionFactory</tt>.
 * </p>
 * <p>
 * This class also tries to figure out if JNDI binding of the <tt>SessionFactory</tt> is used, otherwise it falls back to a global static variable (Singleton). If you use this
 * helper class to obtain a <tt>SessionFactory</tt> in your code, you are shielded from these deployment differences.
 * </p>
 * <p>
 * Another advantage of this class is access to the <tt>Configuration</tt> object that was used to build the current <tt>SessionFactory</tt>. You can access mapping metadata
 * programmatically with this API, and even change it and rebuild the <tt>SessionFactory</tt>.
 * </p>
 * <p>
 * Note: This class supports annotations if you replace the line that creates a Configuration object.
 * </p>
 * <p>
 * Note: This class supports only one data store. Support for several <tt>SessionFactory</tt> instances can be easily added (through a static <tt>Map</tt>, for example). You could
 * then lookup a <tt>SessionFactory</tt> by its name.
 * </p>
 * <p>
 * This implementation is taken from CaveatEmptor Hibernate demo application (developed by Christian Bauer).
 * </p>
 * 
 * @author 01es
 */
public class HibernateUtil {
    private static Log log = LogFactory.getLog(HibernateUtil.class);

    private Interceptor interceptor;
    /**
     * At first by default using plain configuration from hibernate.cfg.xml file
     */
    private Configuration configuration;
    private SessionFactory sessionFactory = null;
    private SessionFactory managedSessionFactory = null;

    /**
     * Constructs utility with custom intercepter and configuration.
     * 
     * @param interceptor
     */
    public HibernateUtil(final Interceptor interceptor, final Configuration configuration) {
        config(interceptor, configuration);
    }

    private void config(final Interceptor interceptor, final Configuration configuration) {
        this.interceptor = interceptor;
        this.configuration = rebuildSessionFactory(configuration);
    }

    public HibernateUtil addSupprtForManagedSessionFactory(final Configuration configuration) {
        configuration.setInterceptor(interceptor);
        managedSessionFactory = configuration.buildSessionFactory();
        return this;
    }

    /**
     * Returns the Hibernate configuration that was used to build the SessionFactory.
     * 
     * @return Configuration
     */
    public Configuration getConfiguration() {
        return configuration;
    }

    /**
     * Returns the global SessionFactory either from a static variable or a JNDI lookup.
     * 
     * @return SessionFactory
     */
    public SessionFactory getSessionFactory() {
        final String sfName = configuration.getProperty(Environment.SESSION_FACTORY_NAME);
        if (sfName != null) {
            log.debug("Looking up SessionFactory in JNDI");
            try {
                return (SessionFactory) new InitialContext().lookup(sfName);
            } catch (final NamingException ex) {
                throw new RuntimeException(ex);
            }
        } else if (sessionFactory == null) {
            rebuildSessionFactory();
        }
        return sessionFactory;
    }

    public SessionFactory getManagedSessionFactory() {
        if (managedSessionFactory == null) {
            throw new IllegalStateException("No support for managed sussion was added.");
        }
        return managedSessionFactory;
    }

    /**
     * Closes the current SessionFactory and releases all resources.
     * <p>
     * The only other method that can be called on HibernateUtil after this one is rebuildSessionFactory(Configuration).
     * </p>
     */
    public void shutdown() {
        // checking whether sessionFactory property was initialized
        checkSessionFactory();

        log.debug("Shutting down Hibernate");
        // close caches and connection pools
        getSessionFactory().close();
        // clear static variables
        sessionFactory = null;
    }

    /**
     * Rebuild the SessionFactory with the static Configuration.
     * <p>
     * Note that this method should only be used with static SessionFactory management, not with JNDI or any other external registry. This method also closes the old static
     * variable SessionFactory before, if it is still open.
     * </p>
     */
    protected void rebuildSessionFactory() {
        log.debug("Using current Configuration to rebuild SessionFactory");
        rebuildSessionFactory(configuration);
    }

    /**
     * Rebuild the SessionFactory with the given Hibernate Configuration.
     * <p>
     * HibernateUtil does not configure() the given Configuration object, it directly calls buildSessionFactory(). This method also closes the old static variable SessionFactory
     * before, if it is still open.
     * </p>
     * 
     * @param cfg
     */
    protected Configuration rebuildSessionFactory(final Configuration cfg) {
        cfg.setInterceptor(interceptor);
        // The listeners for updating meta-information while the instance loads.
        cfg.setListener("post-load", new MetaPostLoadListener());
        cfg.setListener("load-collection", new MetaInitializeCollectionListener());

        log.debug("Rebuilding the SessionFactory from given Configuration");
        if (sessionFactory != null && !sessionFactory.isClosed()) {
            sessionFactory.close();
        }
        if (cfg.getProperty(Environment.SESSION_FACTORY_NAME) != null) {
            log.debug("Managing SessionFactory in JNDI");
            cfg.buildSessionFactory();
        } else {
            log.debug("Holding SessionFactory in static variable");
            sessionFactory = cfg.buildSessionFactory();
        }
        return cfg;
    }

    /**
     * Checks if sessionFactory property was set
     */
    private void checkSessionFactory() {
        if (sessionFactory == null) {
            throw new IllegalStateException("SessionFactory instance was not built. Call either rebuildSessionFactory() or rebuildSessionFactory(Configuration) method beforehand");
        }
    }

    public Interceptor getInterceptor() {
        return interceptor;
    }
}