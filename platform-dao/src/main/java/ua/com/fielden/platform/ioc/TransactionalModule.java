package ua.com.fielden.platform.ioc;

import static com.google.inject.matcher.Matchers.annotatedWith;
import static com.google.inject.matcher.Matchers.subclassesOf;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import ua.com.fielden.platform.dao.ISessionEnabled;
import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.ioc.EntityModule;
import ua.com.fielden.platform.entity.proxy.IIdOnlyProxiedEntityTypeCache;
import ua.com.fielden.platform.entity.query.IdOnlyProxiedEntityTypeCache;
import ua.com.fielden.platform.entity.query.metadata.DomainMetadata;
import ua.com.fielden.platform.ioc.session.SessionInterceptor;
import ua.com.fielden.platform.persistence.HibernateUtil;
import ua.com.fielden.platform.persistence.ProxyInterceptor;

/**
 * Guice injector module for platform-wide Hibernate related injections such as transaction support and domain level validation configurations.
 *
 * @author TG Team
 *
 */
public abstract class TransactionalModule extends EntityModule {
    protected final SessionFactory sessionFactory;
    private final DomainMetadata domainMetadata;
    private final IdOnlyProxiedEntityTypeCache idOnlyProxiedEntityTypeCache;
    private final ProxyInterceptor interceptor;
    private final HibernateUtil hibernateUtil;
    private final List<Class<? extends AbstractEntity<?>>> applicationEntityTypes;

    /**
     * Creates transactional module, which holds references to instances of {@link SessionFactory} and {@link DomainMetadata}. All descending classes needs to provide those two
     * parameters.
     *
     * @param props
     * @param defaultHibernateTypes
     * @param applicationEntityTypes
     */
    public TransactionalModule(
            final Properties props, 
            final Map<Class, Class> defaultHibernateTypes, 
            final List<Class<? extends AbstractEntity<?>>> applicationEntityTypes) {

        final HibernateConfigurationFactory hcf = new HibernateConfigurationFactory(//
                props, //
                defaultHibernateTypes, //
                applicationEntityTypes);
        
        final Configuration cfg = hcf.build();

        interceptor = new ProxyInterceptor();
        hibernateUtil = new HibernateUtil(interceptor, cfg);

        this.sessionFactory = hibernateUtil.getSessionFactory();
        this.domainMetadata = hcf.getDomainMetadata();
        this.idOnlyProxiedEntityTypeCache = hcf.getIdOnlyProxiedEntityTypeCache();
        this.applicationEntityTypes = applicationEntityTypes;
        
    }

    protected void initHibernateConfig(final EntityFactory factory) {
        interceptor.setFactory(factory);
    }

    @Override
    protected void configure() {
        super.configure();
        // entity aggregates transformer
        if (domainMetadata != null) {
            bind(DomainMetadata.class).toInstance(domainMetadata);
        }

        if (idOnlyProxiedEntityTypeCache != null) {
            bind(IdOnlyProxiedEntityTypeCache.class).toInstance(idOnlyProxiedEntityTypeCache);
            bind(IIdOnlyProxiedEntityTypeCache.class).toInstance(idOnlyProxiedEntityTypeCache);
        }
        
        // hibernate util
        if (hibernateUtil != null) {
            bind(HibernateUtil.class).toInstance(hibernateUtil);
        }

        // bind SessionRequired injector
        bindInterceptor(subclassesOf(ISessionEnabled.class), // match only DAO derived from  CommonEntityDao
                annotatedWith(SessionRequired.class), // having annotated methods
                new SessionInterceptor(sessionFactory) // the intercepter
        );
    }

    public DomainMetadata getDomainMetadata() {
        return domainMetadata;
    }
    
    public IdOnlyProxiedEntityTypeCache getIdOnlyProxiedEntityTypeCache() {
        return idOnlyProxiedEntityTypeCache;
    }

    protected List<Class<? extends AbstractEntity<?>>> getApplicationEntityTypes() {
        return Collections.unmodifiableList(applicationEntityTypes);
    }

}
