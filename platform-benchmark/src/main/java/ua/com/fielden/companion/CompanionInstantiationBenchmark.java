package ua.com.fielden.companion;

import com.google.inject.Injector;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import ua.com.fielden.platform.basic.config.Workflows;
import ua.com.fielden.platform.ioc.ApplicationInjectorFactory;
import ua.com.fielden.platform.ioc.NewUserNotifierMockBindingModule;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

import static org.openjdk.jmh.annotations.Threads.MAX;
import static ua.com.fielden.companion.BenchmarkModule.newBenchmarkModule;

/**
 * <h3> Running this benchmark </h3>
 *
 * The following command should be used to run this benchmark, assuming the current working directory is this module (platform-benchmark).
 * <pre>
java -jar target/benchmarks.jar \
    -p propertiesFile="src/main/resources/CompanionInstantiationBenchmark.properties" \
    -prof gc \
    "ua.com.fielden.companion.CompanionInstantiationBenchmark"
 </pre>
 */
@Fork(value = 3)
@Warmup(iterations = 2)
@Measurement(iterations = 5)
@Threads(MAX)
@State(Scope.Benchmark)
public class CompanionInstantiationBenchmark {

    // required for correct initialisation of Guice modules
    @Param("") String propertiesFile;

    private Injector injector;

    @Setup(Level.Trial)
    public void setup() throws IOException {
        if (!Files.isReadable(Path.of(propertiesFile))) {
            throw new IllegalStateException("Can't read file: %s".formatted(propertiesFile));
        }

        final var properties = new Properties();
        try (final var in = new FileInputStream(propertiesFile)) {
            properties.load(in);
        }

        injector = new ApplicationInjectorFactory(Workflows.development)
                .add(newBenchmarkModule(properties))
                .add(new NewUserNotifierMockBindingModule())
                .getInjector();

        // Need to initialise singleton dependencies of companions. Can't enable eager singletons for the whole injector
        // because stuff starts breaking due to incomplete module configuration. Therefore, manually instantiata a companion
        // here to force initialisation of singletons.
        injector.getInstance(Entity1Dao.class);
    }

    private <T> T getInstance(final Class<T> type) {
        return injector.getInstance(type);
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @Measurement(iterations = 20, time = 5)
    public void lightCo(final Blackhole blackhole) {
        for (int i = 0; i < 1000; i++) {
            blackhole.consume(getInstance(Entity1Dao.class));
        }
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @Measurement(iterations = 20, time = 5)
    public void heavyCo(final Blackhole blackhole) {
        for (int i = 0; i < 1000; i++) {
            blackhole.consume(getInstance(Entity2Dao.class));
        }
    }

}
