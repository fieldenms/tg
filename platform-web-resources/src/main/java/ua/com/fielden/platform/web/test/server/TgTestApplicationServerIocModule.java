package ua.com.fielden.platform.web.test.server;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import ua.com.fielden.platform.basic.config.IApplicationDomainProvider;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.ioc.BasicWebServerIocModule;
import ua.com.fielden.platform.security.annotations.SessionCache;
import ua.com.fielden.platform.security.annotations.SessionHashingKey;
import ua.com.fielden.platform.security.annotations.TrustedDeviceSessionDuration;
import ua.com.fielden.platform.security.annotations.UntrustedDeviceSessionDuration;
import ua.com.fielden.platform.security.provider.ISecurityTokenNodeTransformation;
import ua.com.fielden.platform.security.session.UserSession;
import ua.com.fielden.platform.security.user.IAuthenticationModel;
import ua.com.fielden.platform.security.user.IUserProvider;
import ua.com.fielden.platform.security.user.impl.ThreadLocalUserProvider;
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
public class TgTestApplicationServerIocModule extends BasicWebServerIocModule {

    public TgTestApplicationServerIocModule(
            final IApplicationDomainProvider appDomainProvider,
            final List<Class<? extends AbstractEntity<?>>> domainEntityTypes,
            final Properties props)
    {
        super(appDomainProvider, domainEntityTypes, props);
    }

    @Override
    protected void configure() {
        super.configure();

        /////////////////////////////// application specific ////////////////////////////
        bind(IUserProvider.class).to(ThreadLocalUserProvider.class);
        bind(IAuthenticationModel.class).to(TgTestAppAuthenticationModel.class);
        bind(ISecurityTokenNodeTransformation.class).to(TgTestApplicationSecurityTokenNodeTransformation.class);

        requireBinding(IDates.class);
        requireBinding(IUniversalConstants.class);

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
