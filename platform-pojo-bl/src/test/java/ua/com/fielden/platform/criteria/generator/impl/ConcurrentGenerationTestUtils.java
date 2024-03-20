package ua.com.fielden.platform.criteria.generator.impl;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.concurrent.Phaser;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

/**
 * Utilities for concurrent type generation tests.
 */
public class ConcurrentGenerationTestUtils {

    /**
     * Creates and starts multiple threads that generate exactly the same type with the same name in its {@link AbstractWorkerForTypeGenerationTests#doGenTypeWork()} method.
     * Checks for errors with JUnit assertions.
     * 
     * @param <W>
     * @param workerCreator
     * @throws InterruptedException
     */
    public static <W extends AbstractWorkerForTypeGenerationTests> void performConcurrentTypeGenerationTest(final Function<Integer, Function<Phaser, Function<AtomicInteger, Function<AtomicInteger, W>>>> workerCreator) throws InterruptedException {
        final AtomicInteger numberOfErrors = new AtomicInteger(0); // used to calculate the number of exceptions due to "There is no field delegate$"
        final AtomicInteger numberOfOtherErrors = new AtomicInteger(0); // used to calculate the number of exceptions not due to "There is no field delegate$"
        final Phaser phaser = new Phaser(); // phaser is employed here to start worker threads as simultaneous as possible
        phaser.register(); // register the phaser with the current thread

        // create and start worker threads (waiting for the phaser) that would perform identical {@link CriteriaGenerator#generateCriteriaType(Class, List, Class)}
        // such invocations lead to "There is no field delegate$..." errors.
        final var workers = new ArrayList<W>();
        for (int index = 1; index < 10; index ++) {
            final W worker = workerCreator.apply(index).apply(phaser).apply(numberOfErrors).apply(numberOfOtherErrors);
            worker.start();
            workers.add(worker);
        }

        // release the phaser to allow worker threads to do their thing
        phaser.arriveAndAwaitAdvance();
        for (final var worker : workers) {
            worker.join();
        }

        assertEquals("There were errors due to \"There is no field delegate$\".", 0, numberOfErrors.get());
        assertEquals("There were other errors.", 0, numberOfOtherErrors.get());
    }

    /**
     * A helper thread class that performs type generation.
     */
    public static abstract class AbstractWorkerForTypeGenerationTests extends Thread {
        private final Phaser phaser;
        private final AtomicInteger numberOfErrors;
        private final AtomicInteger numberOfOtherErrors;

        public AbstractWorkerForTypeGenerationTests(final String name, final Phaser phaser, final AtomicInteger numberOfErrors, final AtomicInteger numberOfOtherErrors) {
            this.phaser = phaser;
            phaser.register();
            setName(name);
            this.numberOfErrors = numberOfErrors;
            this.numberOfOtherErrors = numberOfOtherErrors;
        }

        @Override
        public void run() {
            try {
                phaser.arriveAndAwaitAdvance();
                // do actual work here...
                doGenTypeWork();
            } catch (final Exception ex) {
                if (ex.getMessage().startsWith("There is no field delegate$")) {
                    numberOfErrors.incrementAndGet();
                }
                else {
                    numberOfOtherErrors.incrementAndGet();
                }
            }
        }

        /**
         * Performs actual type generation.
         */
        public abstract void doGenTypeWork() throws Exception;

    }

}