package fielden.stresstests;

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
import ua.com.fielden.platform.test_entities.Entity;
import ua.com.fielden.platform.reflection.asm.impl.DynamicEntityClassLoader;
import ua.com.fielden.platform.reflection.asm.impl.DynamicTypeNamingService;

/**
 * A stress test for getting the original type from generated classes.
 * This test aims at simulating a thread-contention problem that occurs when many threads are trying to obtain the original type for the generated ones.
 * If the problem exists, it can be observed by running the test, attaching YourKit to that process and review the result of section "Threads". 
 * Lots of solid "red" segments (not the segments with flames) would indicate that problem exists, and selecting one of those "red" sections should show the following stack trace:
 * <pre>
 * pool-1-thread-4  Blocked CPU usage on sample: 189ms
 * java.lang.ClassLoader.loadClass(String, boolean) ClassLoader.java:404
 * java.lang.ClassLoader.loadClass(String) ClassLoader.java:357
 * ua.com.fielden.platform.reflection.asm.impl.DynamicEntityClassLoader.getOriginalType(Class) DynamicEntityClassLoader.java:142
 * </pre>   
 * If the problem does not exist (i.e. it was resolve) then no "red" sections should be observed.
 *
 * TODO: Refactor to a benchmark test in platform-benchmark.
 *
 * @author TG Team
 *
 */
public class GetOriginalTypeStressTest {

    private static final Logger LOGGER = getLogger(GetOriginalTypeStressTest.class);

    private GetOriginalTypeStressTest() {}

    public static void main(final String[] args) throws Exception {
        LOGGER.info("Starting the load test...");
        final Supplier<Class<? extends AbstractEntity<String>>> fun = () -> {
            try {
                return startModification(Entity.class)
                        .modifyTypeName(DynamicTypeNamingService.nextTypeName(Entity.class.getName())).endModification();
            } catch (final Exception e) {
                e.printStackTrace();
                return null;
            }
        };

        final var limitProducers = 25;
        final var limitThreadsInPool = 10;
        LOGGER.info("Configuring [%s] producers to be executed by ScheduledExecutorService with [%s] threads.".formatted(limitProducers, limitThreadsInPool));
        final ScheduledExecutorService exec = Executors.newScheduledThreadPool(limitThreadsInPool);
        final List<ScheduledFuture<?>> producers = Stream.iterate(1, v -> v + 1)
                .limit(limitProducers)
                .map(v -> exec.scheduleWithFixedDelay(new AccessOriginalType(fun.get()), ThreadLocalRandom.current().nextInt(3, 10), ThreadLocalRandom.current().nextInt(2, 10), TimeUnit.MILLISECONDS))
                .collect(toList());

        final ScheduledFuture<?> logger = exec.scheduleWithFixedDelay(() -> LOGGER.info("Cache size [%s].".formatted(DynamicEntityClassLoader.size())), limitThreadsInPool, 2, TimeUnit.SECONDS);

        // put the main thread to sleep for the duration of the stress test
        final var testDurationInMillis = 1000*60*5;
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

    /**
     * A task to access the original type multiple times.
     *
     */
    static class AccessOriginalType implements Runnable {
        private final Class<? extends AbstractEntity<String>> newType;
        
        public AccessOriginalType(final Class<? extends AbstractEntity<String>> newType) {
            this.newType = newType;
        }

        @Override
        public void run() {
            try {
                for (int index = 0; index < 1000; index++) {
                    DynamicEntityClassLoader.getOriginalType(newType).getSimpleName();
                }
                //System.out.println(DynamicEntityClassLoader.getOriginalType(newType).getSimpleName());
            } catch (final Exception ex) {
                
            }
        }
    }


}