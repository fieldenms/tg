package ua.com.fielden.eql;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.inject.AbstractModule;
import com.google.inject.Module;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.util.Modules;
import ua.com.fielden.platform.basic.config.IApplicationDomainProvider;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.proxy.IIdOnlyProxiedEntityTypeCache;
import ua.com.fielden.platform.ioc.BasicWebServerIocModule;
import ua.com.fielden.platform.security.annotations.SessionCache;
import ua.com.fielden.platform.security.annotations.SessionHashingKey;
import ua.com.fielden.platform.security.annotations.TrustedDeviceSessionDuration;
import ua.com.fielden.platform.security.annotations.UntrustedDeviceSessionDuration;
import ua.com.fielden.platform.security.session.UserSession;
import ua.com.fielden.platform.security.user.IUserProvider;
import ua.com.fielden.platform.security.user.impl.ThreadLocalUserProvider;
import ua.com.fielden.platform.serialisation.api.impl.IdOnlyProxiedEntityTypeCacheForTests;
import ua.com.fielden.platform.web.annotations.AppUri;

import java.time.Duration;
import java.util.List;
import java.util.Properties;

import static java.lang.String.format;

/// Standard IoC module for EQL benchmarks.
///
/// To use this module or a module that extends this one, it is necessary to pass it to [#benchmarkModule] first.
///
class BenchmarkIocModule extends BasicWebServerIocModule  {

    /// Completes the configuration in `module`.
    ///
    public static Module benchmarkModule(final BenchmarkIocModule module) {
        return Modules.override(module)
                .with(new AbstractModule() {
                    @Override
                    protected void configure() {
                        // override by a test version because the main one breaks due to an error related to class loaders
                        bind(IIdOnlyProxiedEntityTypeCache.class).to(IdOnlyProxiedEntityTypeCacheForTests.class);
                    }
                });
    }

    public BenchmarkIocModule(
            final Properties props,
            final IApplicationDomainProvider appDomain,
            final List<Class<? extends AbstractEntity<?>>> domainTypes)
    {
        super(appDomain, domainTypes, props);
    }

    @Override
    protected void configure() {
        super.configure();

        bindConstant().annotatedWith(SessionHashingKey.class).to("This is a hashing key, which is used to hash session data for a test server.");
        bindConstant().annotatedWith(TrustedDeviceSessionDuration.class).to(60 * 24 * 3); // three days
        bindConstant().annotatedWith(UntrustedDeviceSessionDuration.class).to(2); // five minutes
        bindConstant().annotatedWith(AppUri.class).to(format("https://%s:%s%s", getProps().get("web.domain"), getProps().get("port"), getProps().get("web.path")));

        bind(IUserProvider.class).to(ThreadLocalUserProvider.class);
    }

    @Provides
    @Singleton
    @SessionCache Cache<String, UserSession> provideSessionCache() {
        return CacheBuilder.newBuilder().expireAfterWrite(Duration.ofMinutes(2)).build();
    }

}
