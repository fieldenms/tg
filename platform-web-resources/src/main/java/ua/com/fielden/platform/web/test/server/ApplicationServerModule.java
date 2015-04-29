package ua.com.fielden.platform.web.test.server;

import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import ua.com.fielden.platform.basic.config.IApplicationDomainProvider;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.security.annotations.PasswordHashingKey;
import ua.com.fielden.platform.security.annotations.SessionCache;
import ua.com.fielden.platform.security.annotations.SessionHashingKey;
import ua.com.fielden.platform.security.annotations.TrustedDeviceSessionDuration;
import ua.com.fielden.platform.security.annotations.UntrustedDeviceSessionDuration;
import ua.com.fielden.platform.security.session.UserSession;
import ua.com.fielden.platform.serialisation.api.ISerialisationClassProvider;
import ua.com.fielden.platform.utils.IUniversalConstants;
import ua.com.fielden.platform.web.ioc.BasicWebApplicationServerModule;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Scopes;
import com.google.inject.TypeLiteral;

/**
 * Guice injector module for Web UI Testing Server.
 *
 * @author TG Team
 *
 */
public class ApplicationServerModule extends BasicWebApplicationServerModule {

    public ApplicationServerModule(final Map<Class, Class> defaultHibernateTypes, final IApplicationDomainProvider applicationDomainProvider, final List<Class<? extends AbstractEntity<?>>> domainTypes, final Class<? extends ISerialisationClassProvider> serialisationClassProviderType, final Class<? extends IFilter> automaticDataFilterType, final Properties props) throws Exception {
        super(defaultHibernateTypes, applicationDomainProvider, domainTypes, serialisationClassProviderType, automaticDataFilterType, props, new WebApp());
    }

    public ApplicationServerModule(final Map<Class, Class> defaultHibernateTypes, final IApplicationDomainProvider applicationDomainProvider, final List<Class<? extends AbstractEntity<?>>> domainTypes, final Class<? extends ISerialisationClassProvider> serialisationClassProviderType, final Class<? extends IFilter> automaticDataFilterType, final Class<? extends IUniversalConstants> universalConstantsType, final Properties props) throws Exception {
        super(defaultHibernateTypes, applicationDomainProvider, domainTypes, serialisationClassProviderType, automaticDataFilterType, universalConstantsType, props, new WebApp());
    }

    @Override
    protected void configure() {
        super.configure();

        // the following bindings are well suited for a test server
        bindConstant().annotatedWith(SessionHashingKey.class).to("This is a hasing key, which is used to hash session data for a test server.");
        bindConstant().annotatedWith(PasswordHashingKey.class).to("This is a hasing key, which is used to hash user passwords for a test server.");
        bindConstant().annotatedWith(TrustedDeviceSessionDuration.class).to(60 * 24 * 3); // three days
        bindConstant().annotatedWith(UntrustedDeviceSessionDuration.class).to(5); // five minutes
        bind(new TypeLiteral<Cache<String, UserSession>>(){}).annotatedWith(SessionCache.class).toProvider(SessionCacheBuilder.class).in(Scopes.SINGLETON);;
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
