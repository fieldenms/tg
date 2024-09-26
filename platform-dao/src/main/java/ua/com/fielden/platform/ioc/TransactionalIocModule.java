package ua.com.fielden.platform.ioc;

import com.google.inject.Key;
import com.google.inject.Provides;
import com.google.inject.name.Named;
import com.google.inject.name.Names;
import jakarta.inject.Singleton;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import ua.com.fielden.platform.basic.config.IApplicationDomainProvider;
import ua.com.fielden.platform.dao.ISessionEnabled;
import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.proxy.IIdOnlyProxiedEntityTypeCache;
import ua.com.fielden.platform.entity.query.IDbVersionProvider;
import ua.com.fielden.platform.entity.query.IdOnlyProxiedEntityTypeCache;
import ua.com.fielden.platform.eql.dbschema.HibernateMappingsGenerator;
import ua.com.fielden.platform.ioc.session.SessionInterceptor;
import ua.com.fielden.platform.meta.DomainMetadataBuilder;
import ua.com.fielden.platform.meta.IDomainMetadata;
import ua.com.fielden.platform.persistence.HibernateUtil;
import ua.com.fielden.platform.persistence.ProxyInterceptor;
import ua.com.fielden.platform.persistence.types.HibernateTypeMappings;

import java.util.Properties;

import static com.google.inject.matcher.Matchers.annotatedWith;
import static com.google.inject.matcher.Matchers.subclassesOf;
import static ua.com.fielden.platform.entity.query.IDbVersionProvider.constantDbVersion;
import static ua.com.fielden.platform.persistence.types.PlatformHibernateTypeMappings.PLATFORM_HIBERNATE_TYPE_MAPPINGS;

/**
 * Guice injector module for platform-wide Hibernate related injections such as transaction support and domain level validation configurations.
 *
 * @author TG Team
 *
 */
public abstract class TransactionalIocModule extends EntityIocModule {

    private static final String SESSION_FACTORY_FOR_SESSION_INTERCEPTOR = "SessionFactory for SessionInterceptor";

    private final Properties props;

    public TransactionalIocModule(final Properties props) {
        this.props = props;
    }

    @Override
    protected void configure() {
        super.configure();

        bind(HibernateTypeMappings.class).toInstance(PLATFORM_HIBERNATE_TYPE_MAPPINGS);
        bind(IIdOnlyProxiedEntityTypeCache.class).to(IdOnlyProxiedEntityTypeCache.class);

        // bind SessionRequired interceptor
        bindInterceptor(subclassesOf(ISessionEnabled.class), // match only instances implementing ISessionEnabled
                        annotatedWith(SessionRequired.class), // having methods annotated with @SessionRequired
                        new SessionInterceptor(getProvider(Key.get(SessionFactory.class,
                                                                   Names.named(SESSION_FACTORY_FOR_SESSION_INTERCEPTOR)))));

        bind(IDbVersionProvider.class).toInstance(constantDbVersion(HibernateConfigurationFactory.determineDbVersion(props)));
    }

    @Provides
    @Singleton
    IDomainMetadata provideDomainMetadata(final HibernateTypeMappings hibernateTypeMappings,
                                          final IApplicationDomainProvider appDomainProvider,
                                          final IDbVersionProvider dbVersionProvider) {
        return new DomainMetadataBuilder(hibernateTypeMappings, appDomainProvider.entityTypes(), dbVersionProvider)
                .build();
    }

    @Provides
    @Singleton
    Configuration provideHibernateConfiguration(final HibernateMappingsGenerator generator) {
        return new HibernateConfigurationFactory(props, generator).build();
    }

    @Provides
    @Singleton
    HibernateUtil provideHibernateUtil(final EntityFactory entityFactory, final Configuration configuration) {
        return new HibernateUtil(new ProxyInterceptor(entityFactory), configuration);
    }

    @Provides
    // Limit SessionFactory binding scope to SessionInterceptor by using a dedicated name
    @Named(SESSION_FACTORY_FOR_SESSION_INTERCEPTOR)
    // HibernateUtil#getSessionFactory() isn't pure, but this binding has singleton scope because that's how SessionInterceptor
    // has been initialised previously.
    @Singleton
    SessionFactory provideSessionFactoryForSessionInterceptor(final HibernateUtil hibernateUtil) {
        return hibernateUtil.getSessionFactory();
    }

}
