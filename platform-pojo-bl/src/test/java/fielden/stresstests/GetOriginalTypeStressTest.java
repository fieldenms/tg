package fielden.stresstests;

import static java.util.stream.Collectors.toList;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.apache.log4j.Logger;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.Entity;
import ua.com.fielden.platform.reflection.asm.impl.DynamicEntityClassLoader;
import ua.com.fielden.platform.reflection.asm.impl.DynamicTypeNamingService;

/**
 * A stress test for the getting the original type from generated classes.
 * This test aims at simulating a thread-contention problem that occurs when many threads are trying to obtain the original type for the generated ones.
 * If the problem exists, it can be observed by running the test, attaching YourKit to that process and review the result of section "Threads". 
 * Lots of "red" would indicate that problem exists, and selecting one of those red sections should show the following stack trace:
 * <pre>
 * pool-1-thread-4  Blocked CPU usage on sample: 189ms
 * java.lang.ClassLoader.loadClass(String, boolean) ClassLoader.java:404
 * java.lang.ClassLoader.loadClass(String) ClassLoader.java:357
 * ua.com.fielden.platform.classloader.TgSystemClassLoader.loadClass(String) TgSystemClassLoader.java:74
 * ua.com.fielden.platform.reflection.asm.impl.DynamicEntityClassLoader.getOriginalType(Class) DynamicEntityClassLoader.java:107
 * </pre>   
 * If the problem does not exist (i.e. it was resolve) then no "red" sections should be observed.
 * @author TG Team
 *
 */
public class GetOriginalTypeStressTest {

    private static final Logger LOGGER = Logger.getLogger(GetOriginalTypeStressTest.class);

    private GetOriginalTypeStressTest() {}
    
    public static void main(final String[] args) throws Exception {
        final Supplier<Class<? extends AbstractEntity<String>>> fun = () -> {
            try {
                return (Class<? extends AbstractEntity<String>>) DynamicEntityClassLoader.getInstance(ClassLoader.getSystemClassLoader()).startModification(Entity.class)
                        .modifyTypeName(DynamicTypeNamingService.nextTypeName(Entity.class.getName())).endModification();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                return null;
            }
        };

        final ScheduledExecutorService exec = Executors.newScheduledThreadPool(10);
        final List<ScheduledFuture<?>> producers = Stream.iterate(1, v -> v + 1)
                .limit(10)
                .map(v -> exec.scheduleWithFixedDelay(new AccessOriginalType(fun.get()), (int) (Math.random() * 10) + 1, (int) (Math.random() * 10) + 1, TimeUnit.MILLISECONDS))
                .collect(toList());
          
        // put the main thread to sleep for the duration of the stress test
        Thread.sleep(1000*60*5);
        // let's not terminate the whole process after the specified period of time and exit
        LOGGER.info("Shutting down...");
        for (final ScheduledFuture<?> future : producers) {
            future.cancel(true);
            LOGGER.info("cancelling");
        }
        exec.shutdown();
        System.exit(0);
    }

    /**
     * A task to access original type multiple times.
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