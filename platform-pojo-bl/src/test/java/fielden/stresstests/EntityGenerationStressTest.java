package fielden.stresstests;

import static java.util.concurrent.ThreadLocalRandom.current;
import static java.util.stream.Collectors.toList;
import static org.apache.logging.log4j.LogManager.getLogger;
import static ua.com.fielden.platform.reflection.asm.impl.DynamicEntityClassLoader.startModification;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.apache.logging.log4j.Logger;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.Entity;
import ua.com.fielden.platform.entity.annotation.Calculated;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.factory.CalculatedAnnotation;
import ua.com.fielden.platform.entity.annotation.factory.IsPropertyAnnotation;
import ua.com.fielden.platform.reflection.asm.api.NewProperty;
import ua.com.fielden.platform.reflection.asm.impl.DynamicEntityClassLoader;
import ua.com.fielden.platform.reflection.asm.impl.DynamicTypeNamingService;
import ua.com.fielden.platform.types.Money;

/**
 * A stress test for generation of entity types.
 * This test aims at simulating a contention problem that occurs when many threads are trying to generate new entity types concurrently, which could be related to the use of a single class loader, responsible for defining new types.
 * <p>
 * If the problem exists, it can be observed by running the test, attaching YourKit to that process and review the result of section "Threads". 
 * Lots of solid "red" segments (not the segments with flames) would indicate that a contention problem exists, and selecting one of those "red" sections should show the following stack trace:
 * <pre>
 * pool-2-thread-7  Blocked CPU usage on sample: 438ms
 * java.lang.ClassLoader.defineClass1(Native Method)
 * java.lang.ClassLoader.defineClass(ClassLoader.java:1013)
 * java.lang.ClassLoader.defineClass(ClassLoader.java:875)
 * ua.com.fielden.platform.reflection.asm.impl.DynamicEntityClassLoader.doDefineClass(DynamicEntityClassLoader.java:92)
 * </pre>   
 * If the problem does not exist then no "red" sections should be observed.
 * <p>
 * At the time when this test was created, we are using a singleton instance of {@link DynamicEntityClassLoader}, responsible for defining all generated types.
 * Because of this, intensive generation of new entities will experience a contention problem due to synchronous nature of class loaders.
 * However, in practice this should not be a problem if the number of concurrently generated entities if not significant.
 * <p>
 * One can use parameters {@code limitProducers}, {@code limitThreadsInPool}, {@code minGenDelay}, {@code maxGenDelay} and {@code timeUnit} to control the simulation for concurrent entity generation.
 * 
 * @author TG Team
 *
 */
public class EntityGenerationStressTest {

    private static final Logger LOGGER = getLogger(EntityGenerationStressTest.class);

    private EntityGenerationStressTest() {}

    public static void main(final String[] args) throws Exception {
        LOGGER.info("Starting the load test for generating new entities...");
        final Supplier<Class<? extends AbstractEntity<String>>> fun = () -> {
            try {
                final IsProperty atIsPropertyWithPrecision = new IsPropertyAnnotation(19, 4).newInstance();
                final Calculated atCalculated = new CalculatedAnnotation().contextualExpression("2 * 3 - [finalProperty]").newInstance();
                final NewProperty<Money> np1 = NewProperty.create("new_calculated_prop1", Money.class, "title", "description", atIsPropertyWithPrecision, atCalculated);
                final NewProperty<Money> np2 = NewProperty.create("new_calculated_prop2", Money.class, "title", "description", atIsPropertyWithPrecision, atCalculated);
                return startModification(Entity.class)
                        .addProperties(np1, np2)
                        .modifyTypeName(DynamicTypeNamingService.nextTypeName(Entity.class.getName()))
                        .endModification();
            } catch (final Exception ex) {
                LOGGER.error("Could not generate entity.", ex);
                return null;
            }
        };

        final var limitProducers = 25;
        final var limitThreadsInPool = 10;
        final var minGenDelay = 3;
        final var maxGenDelay = 10;
        final var timeUnit = TimeUnit.MILLISECONDS;
        LOGGER.info("Configuring [%s] producers to be executed by ScheduledExecutorService with [%s] threads.".formatted(limitProducers, limitThreadsInPool));
        final ScheduledExecutorService exec = Executors.newScheduledThreadPool(limitThreadsInPool);
        final List<ScheduledFuture<?>> producers = Stream.iterate(1, v -> v + 1)
                .limit(limitProducers)
                .map(v -> exec.scheduleWithFixedDelay(() ->fun.get(), 
                        /* initial generation delay */ current().nextInt(minGenDelay, maxGenDelay),
                                /* generation delay */ current().nextInt(minGenDelay, maxGenDelay), timeUnit))
                .collect(toList());

        final ScheduledFuture<?> logger = exec.scheduleWithFixedDelay(() -> LOGGER.info("Cache size [%s].".formatted(DynamicEntityClassLoader.size())), limitThreadsInPool, 2, TimeUnit.SECONDS);

        // put the main thread to sleep for the duration of the stress test
        final var testDurationInMillis = 1000*60*1;
        LOGGER.info("Test started and should run for %s minutes...".formatted(TimeUnit.MINUTES.convert(testDurationInMillis, TimeUnit.MILLISECONDS)));
        Thread.sleep(testDurationInMillis);
        // let's not terminate the whole process after the specified period of time and exit
        LOGGER.info("Shutting down producers...");
        var count = 0;
        for (final ScheduledFuture<?> future : producers) {
            future.cancel(true);
            LOGGER.info("shutdown %s".formatted(++count));
        }
        logger.cancel(true);
        exec.shutdown();
        LOGGER.info("Completed the test.");
        System.exit(0);
    }


}