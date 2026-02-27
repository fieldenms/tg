package ua.com.fielden.benchmark.entity;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.inject.*;
import com.google.inject.Module;
import com.google.inject.util.Modules;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import ua.com.fielden.platform.basic.config.Workflows;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.IEntityTypeVerifier;
import ua.com.fielden.platform.entity.exceptions.EntityDefinitionException;
import ua.com.fielden.platform.entity.proxy.IIdOnlyProxiedEntityTypeCache;
import ua.com.fielden.platform.ioc.ApplicationInjectorFactory;
import ua.com.fielden.platform.ioc.BasicWebServerIocModule;
import ua.com.fielden.platform.ioc.NewUserEmailNotifierTestIocModule;
import ua.com.fielden.platform.security.annotations.SessionCache;
import ua.com.fielden.platform.security.annotations.SessionHashingKey;
import ua.com.fielden.platform.security.annotations.TrustedDeviceSessionDuration;
import ua.com.fielden.platform.security.annotations.UntrustedDeviceSessionDuration;
import ua.com.fielden.platform.security.session.UserSession;
import ua.com.fielden.platform.security.user.IUserProvider;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.security.user.impl.ThreadLocalUserProvider;
import ua.com.fielden.platform.serialisation.api.impl.IdOnlyProxiedEntityTypeCacheForTests;
import ua.com.fielden.platform.web.annotations.AppUri;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import static java.lang.String.format;
import static org.openjdk.jmh.annotations.Threads.MAX;

/// ###  Running this benchmark
///
/// To run from the IDE, launch this class by specifying the following arguments:
/// 1. `propertiesFile` -- path to the `.properties` file to configure the application used by the benchmark.
///     This argument is optional, and its default value is [#DEFAULT_PROPERTIES_FILE].
///
/// To run from the command line:
/// 1. Build the JAR using `mvn package`.
/// 2. Execute the JAR within directory `platform-benchmark`:
///    ```
///    java -jar target/benchmarks.jar -p propertiesFile="src/main/resources/benchmark-application.properties" -prof gc "ua.com.fielden.benchmark.entity.EntityInstantiationBenchmark"
///    ```
///
@Fork(value = 3)
@Warmup(iterations = 2)
@Measurement(iterations = 5)
@Threads(MAX)
@State(Scope.Benchmark)
public class EntityInstantiationBenchmark {

    private static final String DEFAULT_PROPERTIES_FILE = "src/main/resources/benchmark-application.properties";

    public static void main(final String[] args) throws Exception {
        final var propertiesFile = args.length >= 1 ? args[0] : DEFAULT_PROPERTIES_FILE;

        var opt = new OptionsBuilder()
                .include(EntityInstantiationBenchmark.class.getSimpleName())
                .addProfiler("gc")
                .param("propertiesFile", propertiesFile)
                .build();

        new Runner(opt).run();
    }

    // Required for correct initialisation of Guice modules.
    @Param(DEFAULT_PROPERTIES_FILE) String propertiesFile;

    private Injector injector;
    private Injector injectorWithoutVerification;

    @Setup(Level.Trial)
    public void setup() throws IOException {
        if (propertiesFile.isEmpty()) {
            throw new IllegalStateException("Parameter [propertiesFile] is missing.");
        }
        if (!Files.isReadable(Path.of(propertiesFile))) {
            throw new IllegalStateException("Can't read file: %s".formatted(propertiesFile));
        }

        final var properties = new Properties();
        try (final var in = new FileInputStream(propertiesFile)) {
            properties.load(in);
        }

        injector = new ApplicationInjectorFactory(Workflows.development)
                .add(IocModule.newBenchmarkModule(properties))
                .add(new NewUserEmailNotifierTestIocModule())
                .getInjector();
        injectorWithoutVerification = new ApplicationInjectorFactory(Workflows.development)
                .add(IocModule.newBenchmarkModule(properties))
                .add(new NewUserEmailNotifierTestIocModule())
                .add(new AbstractModule() {
                    @Override
                    protected void configure() {
                        bind(IEntityTypeVerifier.class).to(NoopEntityTypeVerifier.class);
                    }
                })
                .getInjector();
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @Measurement(iterations = 20, time = 5)
    public void instrumentedUser(final Blackhole blackhole) {
        blackhole.consume(injector.getInstance(User.class));
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @Measurement(iterations = 20, time = 5)
    public void instrumentedUserWithoutVerification(final Blackhole blackhole) {
        blackhole.consume(injectorWithoutVerification.getInstance(User.class));
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @Measurement(iterations = 20, time = 5)
    public void uninstrumentedUser(final Blackhole blackhole)
            throws Exception
    {
        final var constructor = User.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        blackhole.consume(constructor.newInstance());
    }

    static class IocModule extends BasicWebServerIocModule {

        public static Module newBenchmarkModule(final Properties props) {
            return Modules.override(new IocModule(props))
                    .with(new AbstractModule() {
                        @Override
                        protected void configure() {
                            // override by a test version because the main one breaks due to an error related to class loaders
                            bind(IIdOnlyProxiedEntityTypeCache.class).to(IdOnlyProxiedEntityTypeCacheForTests.class);
                        }
                    });
        }

        IocModule(final Properties props) {
            super(List::of,
                  List.of(),
                  props);
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
        @SessionCache
        Cache<String, UserSession> provideSessionCache() {
            return CacheBuilder.newBuilder().expireAfterWrite(2, TimeUnit.MINUTES).build();
        }

    }

    @jakarta.inject.Singleton
    static class NoopEntityTypeVerifier implements IEntityTypeVerifier {

        @Override
        public void verify(final Class<? extends AbstractEntity<?>> entityType) throws EntityDefinitionException {}

    }

}
