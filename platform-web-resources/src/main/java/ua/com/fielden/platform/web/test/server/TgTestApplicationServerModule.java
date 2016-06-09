package ua.com.fielden.platform.web.test.server;

import static java.lang.String.format;

import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Scopes;
import com.google.inject.TypeLiteral;

import ua.com.fielden.platform.basic.config.IApplicationDomainProvider;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.ioc.BasicWebServerModule;
import ua.com.fielden.platform.reflection.CompanionObjectAutobinder;
import ua.com.fielden.platform.security.annotations.SessionCache;
import ua.com.fielden.platform.security.annotations.SessionHashingKey;
import ua.com.fielden.platform.security.annotations.TrustedDeviceSessionDuration;
import ua.com.fielden.platform.security.annotations.UntrustedDeviceSessionDuration;
import ua.com.fielden.platform.security.session.UserSession;
import ua.com.fielden.platform.security.user.IAuthenticationModel;
import ua.com.fielden.platform.security.user.INewUserNotifier;
import ua.com.fielden.platform.security.user.IUserProvider;
import ua.com.fielden.platform.security.user.NewUserNotifierByEmail;
import ua.com.fielden.platform.security.user.impl.ThreadLocalUserProvider;
import ua.com.fielden.platform.serialisation.api.ISerialisationClassProvider;
import ua.com.fielden.platform.utils.IUniversalConstants;
import ua.com.fielden.platform.web.annotations.AppUri;

/**
 * Guice injector module for TG Testing Server.
 *
 * @author TG Team
 *
 */
public class TgTestApplicationServerModule extends BasicWebServerModule {
    private final Class<? extends IUniversalConstants> universalConstantsType;
    private final List<Class<? extends AbstractEntity<?>>> domainTypes;

    /**
     * The constructor with the largest number of arguments.
     *
     * @param defaultHibernateTypes
     * @param applicationEntityTypes
     * @param domainTypes
     * @param serialisationClassProviderType
     * @param automaticDataFilterType
     * @param universalConstantsType
     * @param props
     * @throws Exception
     */
    public TgTestApplicationServerModule(//
    final Map<Class, Class> defaultHibernateTypes, //
            final IApplicationDomainProvider applicationDomainProvider,//
            final List<Class<? extends AbstractEntity<?>>> domainTypes,//
            final Class<? extends ISerialisationClassProvider> serialisationClassProviderType, //
            final Class<? extends IFilter> automaticDataFilterType, //
            final Class<? extends IUniversalConstants> universalConstantsType,//
            final Properties props) throws Exception {
        super(defaultHibernateTypes, applicationDomainProvider, serialisationClassProviderType, automaticDataFilterType, null, props);
        this.universalConstantsType = universalConstantsType;
        this.domainTypes = domainTypes;
    }

    /**
     * An argument list reduced version of the above constructor, where <code>universalConstantsType</code> is specified as <code>null</code>.
     *
     * @param defaultHibernateTypes
     * @param applicationEntityTypes
     * @param domainTypes
     * @param serialisationClassProviderType
     * @param automaticDataFilterType
     * @param props
     * @throws Exception
     */
    public TgTestApplicationServerModule(final Map<Class, Class> defaultHibernateTypes, //
            final IApplicationDomainProvider applicationDomainProvider,//
            final List<Class<? extends AbstractEntity<?>>> domainTypes,//
            final Class<? extends ISerialisationClassProvider> serialisationClassProviderType, //
            final Class<? extends IFilter> automaticDataFilterType, //
            final Properties props) throws Exception {
        this(defaultHibernateTypes, applicationDomainProvider, domainTypes, serialisationClassProviderType, automaticDataFilterType, null, props);
    }

    @Override
    protected void configure() {
        super.configure();

        /////////////////////////////// application specific ////////////////////////////
        // bind IUserProvider
        bind(IUserProvider.class).to(ThreadLocalUserProvider.class).in(Scopes.SINGLETON);
        // bind authentication model
        bind(IAuthenticationModel.class).to(TgTestAppAuthenticationModel.class);

        if (universalConstantsType != null) {
            bind(IUniversalConstants.class).to(universalConstantsType).in(Scopes.SINGLETON);
        }

        // dynamically bind DAO implementations for all companion objects
        for (final Class<? extends AbstractEntity<?>> entityType : domainTypes) {
            CompanionObjectAutobinder.bindDao(entityType, binder());
        }

        // the following bindings are well suited for a test server
        bindConstant().annotatedWith(SessionHashingKey.class).to("This is a hasing key, which is used to hash session data for a test server.");
        bindConstant().annotatedWith(TrustedDeviceSessionDuration.class).to(60 * 24 * 3); // three days
        bindConstant().annotatedWith(UntrustedDeviceSessionDuration.class).to(2); // five minutes
        bind(new TypeLiteral<Cache<String, UserSession>>() {
        }).annotatedWith(SessionCache.class).toProvider(SessionCacheBuilder.class).in(Scopes.SINGLETON);
        
        bindConstant().annotatedWith(AppUri.class).to(format("https://%s:%s%s", getProps().get("web.domain"), getProps().get("port"), getProps().get("web.path")));
    }

    private static class SessionCacheBuilder implements Provider<Cache<String, UserSession>> {

        private final Cache<String, UserSession> cache;

        @Inject
        public SessionCacheBuilder(final @UntrustedDeviceSessionDuration int untrustedDeviceSessionDurationMins) {
            cache = CacheBuilder.newBuilder().expireAfterWrite(untrustedDeviceSessionDurationMins / 2, TimeUnit.MINUTES).build();
        }

        @Override
        public Cache<String, UserSession> get() {
            return cache;
        }

    }
}
