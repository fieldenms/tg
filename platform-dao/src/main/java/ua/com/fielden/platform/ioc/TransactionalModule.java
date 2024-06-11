package ua.com.fielden.platform.ioc;

import com.google.inject.Guice;
import com.google.inject.Key;
import com.google.inject.Provides;
import com.google.inject.name.Named;
import com.google.inject.name.Names;
import jakarta.inject.Singleton;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import ua.com.fielden.platform.dao.ISessionEnabled;
import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.ioc.EntityModule;
import ua.com.fielden.platform.entity.proxy.IIdOnlyProxiedEntityTypeCache;
import ua.com.fielden.platform.entity.query.IdOnlyProxiedEntityTypeCache;
import ua.com.fielden.platform.eql.dbschema.HibernateMappingsGenerator;
import ua.com.fielden.platform.ioc.session.SessionInterceptor;
import ua.com.fielden.platform.meta.DomainMetadataBuilder;
import ua.com.fielden.platform.meta.IDomainMetadata;
import ua.com.fielden.platform.persistence.HibernateUtil;
import ua.com.fielden.platform.persistence.ProxyInterceptor;

import java.util.List;
import java.util.Map;
import java.util.Properties;

import static com.google.inject.Scopes.SINGLETON;
import static com.google.inject.matcher.Matchers.annotatedWith;
import static com.google.inject.matcher.Matchers.subclassesOf;

/**
 * Guice injector module for platform-wide Hibernate related injections such as transaction support and domain level validation configurations.
 *
 * @author TG Team
 *
 */
public abstract class TransactionalModule extends EntityModule {

    private static final String SESSION_FACTORY_FOR_SESSION_INTERCEPTOR = "SessionFactory for SessionInterceptor";

    private final Properties props;
    private final Map<Class, Class> defaultHibernateTypes;
    private final List<Class<? extends AbstractEntity<?>>> applicationEntityTypes;

    public TransactionalModule(
            final Properties props,
            final Map<Class, Class> defaultHibernateTypes,
            final List<Class<? extends AbstractEntity<?>>> applicationEntityTypes) {
        this.props = props;
        this.defaultHibernateTypes = defaultHibernateTypes;
        this.applicationEntityTypes = applicationEntityTypes;
    }

    @Override
    protected void configure() {
        super.configure();

        bind(IIdOnlyProxiedEntityTypeCache.class).to(IdOnlyProxiedEntityTypeCache.class).in(SINGLETON);

        // bind SessionRequired interceptor
        bindInterceptor(subclassesOf(ISessionEnabled.class), // match only DAO derived from  CommonEntityDao
                        annotatedWith(SessionRequired.class), // having annotated methods
                        new SessionInterceptor(getProvider(Key.get(SessionFactory.class,
                                                                   Names.named(SESSION_FACTORY_FOR_SESSION_INTERCEPTOR)))));
    }

    @Provides
    @Singleton
    IDomainMetadata provideDomainMetadata() {
        return new DomainMetadataBuilder(defaultHibernateTypes,
                                         Guice.createInjector(new HibernateUserTypesModule()),
                                         applicationEntityTypes,
                                         HibernateConfigurationFactory.determineDbVersion(props))
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
