package ua.com.fielden.companion;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.inject.AbstractModule;
import com.google.inject.Module;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.util.Modules;
import ua.com.fielden.platform.entity.proxy.IIdOnlyProxiedEntityTypeCache;
import ua.com.fielden.platform.ioc.BasicWebServerModule;
import ua.com.fielden.platform.security.annotations.SessionCache;
import ua.com.fielden.platform.security.annotations.SessionHashingKey;
import ua.com.fielden.platform.security.annotations.TrustedDeviceSessionDuration;
import ua.com.fielden.platform.security.annotations.UntrustedDeviceSessionDuration;
import ua.com.fielden.platform.security.session.UserSession;
import ua.com.fielden.platform.security.user.IUserProvider;
import ua.com.fielden.platform.security.user.impl.ThreadLocalUserProvider;
import ua.com.fielden.platform.serialisation.api.impl.IdOnlyProxiedEntityTypeCacheForTests;

import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

class BenchmarkModule extends BasicWebServerModule  {

    public static Module newBenchmarkModule(final Properties props) {
        return Modules.override(new BenchmarkModule(props))
                .with(new AbstractModule() {
                    @Override
                    protected void configure() {
                        // override by a test version because the main one breaks due to an error related to class loaders
                        bind(IIdOnlyProxiedEntityTypeCache.class).to(IdOnlyProxiedEntityTypeCacheForTests.class);
                    }
                });
    }

    private BenchmarkModule(final Properties props) {
        super(List::of,
              List.of(),
              props);
    }

    @Override
    protected void configure() {
        super.configure();

        bindConstant().annotatedWith(SessionHashingKey.class).to("This is a hasing key, which is used to hash session data for a test server.");
        bindConstant().annotatedWith(TrustedDeviceSessionDuration.class).to(60 * 24 * 3); // three days
        bindConstant().annotatedWith(UntrustedDeviceSessionDuration.class).to(2); // five minutes

        bind(IUserProvider.class).to(ThreadLocalUserProvider.class);
    }

    @Provides
    @Singleton
    @SessionCache Cache<String, UserSession> provideSessionCache() {
        return CacheBuilder.newBuilder().expireAfterWrite(2, TimeUnit.MINUTES).build();
    }

}
