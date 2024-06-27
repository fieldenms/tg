package ua.com.fielden.platform.web.test.server;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.inject.Provides;
import com.google.inject.Scopes;
import com.google.inject.Singleton;
import ua.com.fielden.platform.basic.config.IApplicationDomainProvider;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.ioc.BasicWebServerModule;
import ua.com.fielden.platform.security.annotations.SessionCache;
import ua.com.fielden.platform.security.annotations.SessionHashingKey;
import ua.com.fielden.platform.security.annotations.TrustedDeviceSessionDuration;
import ua.com.fielden.platform.security.annotations.UntrustedDeviceSessionDuration;
import ua.com.fielden.platform.security.session.UserSession;
import ua.com.fielden.platform.security.user.IAuthenticationModel;
import ua.com.fielden.platform.security.user.IUserProvider;
import ua.com.fielden.platform.security.user.impl.ThreadLocalUserProvider;
import ua.com.fielden.platform.serialisation.api.ISerialisationClassProvider;
import ua.com.fielden.platform.utils.IDates;
import ua.com.fielden.platform.utils.IUniversalConstants;
import ua.com.fielden.platform.web.annotations.AppUri;

import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import static java.lang.String.format;

/**
 * Guice injector module for TG Testing Server.
 *
 * @author TG Team
 *
 */
public class TgTestApplicationServerModule extends BasicWebServerModule {
    private final Class<? extends IUniversalConstants> universalConstantsImplType;
    private final Class<? extends IDates> datesImplType;

    public TgTestApplicationServerModule(
            final IApplicationDomainProvider appDomainProvider,
            final List<Class<? extends AbstractEntity<?>>> domainEntityTypes,
            final Class<? extends ISerialisationClassProvider> serialisationClassProviderType,
            final Class<? extends IUniversalConstants> universalConstantsImplType,
            final Class<? extends IDates> datesImplType,
            final Properties props)
    {
        super(appDomainProvider, domainEntityTypes, serialisationClassProviderType, props);
        if (universalConstantsImplType == null) {
            throw new IllegalArgumentException("Missing implemementation for IUniversalConstants.");
        }
        if (datesImplType == null) {
            throw new IllegalArgumentException("Missing implemementation for IDates.");
        }

        this.universalConstantsImplType = universalConstantsImplType;
        this.datesImplType = datesImplType;
    }

    @Override
    protected void configure() {
        super.configure();

        /////////////////////////////// application specific ////////////////////////////
        // bind IUserProvider
        bind(IUserProvider.class).to(ThreadLocalUserProvider.class).in(Scopes.SINGLETON);
        // bind authentication model
        bind(IAuthenticationModel.class).to(TgTestAppAuthenticationModel.class);

        bind(IDates.class).to(datesImplType).in(Scopes.SINGLETON);
        bind(IUniversalConstants.class).to(universalConstantsImplType).in(Scopes.SINGLETON);

        // the following bindings are well suited for a test server
        bindConstant().annotatedWith(SessionHashingKey.class).to("This is a hasing key, which is used to hash session data for a test server.");
        bindConstant().annotatedWith(TrustedDeviceSessionDuration.class).to(60 * 24 * 3); // three days
        bindConstant().annotatedWith(UntrustedDeviceSessionDuration.class).to(2); // five minutes

        bindConstant().annotatedWith(AppUri.class).to(format("https://%s:%s%s", getProps().get("web.domain"), getProps().get("port"), getProps().get("web.path")));
    }

    @Provides
    @Singleton
    @SessionCache Cache<String, UserSession> provideSessionCache(final @UntrustedDeviceSessionDuration int untrustedDeviceSessionDurationMins) {
        return CacheBuilder.newBuilder()
                .expireAfterWrite(untrustedDeviceSessionDurationMins / 2, TimeUnit.MINUTES)
                .build();
    }

}
