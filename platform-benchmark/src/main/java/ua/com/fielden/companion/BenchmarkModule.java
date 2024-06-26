package ua.com.fielden.companion;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.inject.Module;
import com.google.inject.*;
import com.google.inject.util.Modules;
import ua.com.fielden.platform.entity.proxy.IIdOnlyProxiedEntityTypeCache;
import ua.com.fielden.platform.entity.query.NoDataFilter;
import ua.com.fielden.platform.ioc.BasicWebServerModule;
import ua.com.fielden.platform.security.annotations.SessionCache;
import ua.com.fielden.platform.security.annotations.SessionHashingKey;
import ua.com.fielden.platform.security.annotations.TrustedDeviceSessionDuration;
import ua.com.fielden.platform.security.annotations.UntrustedDeviceSessionDuration;
import ua.com.fielden.platform.security.provider.SecurityTokenProvider;
import ua.com.fielden.platform.security.session.UserSession;
import ua.com.fielden.platform.security.user.IUserProvider;
import ua.com.fielden.platform.security.user.impl.ThreadLocalUserProvider;
import ua.com.fielden.platform.serialisation.api.impl.DefaultSerialisationClassProvider;
import ua.com.fielden.platform.serialisation.api.impl.IdOnlyProxiedEntityTypeCacheForTests;

import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import static com.google.inject.Scopes.SINGLETON;

class BenchmarkModule extends BasicWebServerModule  {

    public static Module newBenchmarkModule(final Properties props) {
        return Modules.override(new BenchmarkModule(props))
                .with(new AbstractModule() {
                    @Override
                    protected void configure() {
                        // override by a test version because the main one breaks due to an error related to class loaders
                        bind(IIdOnlyProxiedEntityTypeCache.class).to(IdOnlyProxiedEntityTypeCacheForTests.class).in(SINGLETON);
                    }
                });
    }

    private BenchmarkModule(final Properties props) {
        super(List::of,
              List.of(),
              DefaultSerialisationClassProvider.class,
              NoDataFilter.class,
              SecurityTokenProvider.class,
              props);
    }

    @Override
    protected void configure() {
        super.configure();

        bindConstant().annotatedWith(SessionHashingKey.class).to("This is a hasing key, which is used to hash session data for a test server.");
        bindConstant().annotatedWith(TrustedDeviceSessionDuration.class).to(60 * 24 * 3); // three days
        bindConstant().annotatedWith(UntrustedDeviceSessionDuration.class).to(2); // five minutes
        bind(new TypeLiteral<Cache<String, UserSession>>() {}).annotatedWith(SessionCache.class)
                .toProvider(SessionCacheBuilder.class).in(Scopes.SINGLETON);

        bind(IUserProvider.class).to(ThreadLocalUserProvider.class);
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
