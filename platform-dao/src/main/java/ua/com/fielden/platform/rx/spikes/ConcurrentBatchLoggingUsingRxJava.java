package ua.com.fielden.platform.rx.spikes;

import static java.util.stream.Collectors.toList;
import static rx.schedulers.Schedulers.from;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import rx.Subscription;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.subjects.PublishSubject;
import rx.subjects.SerializedSubject;
import rx.subjects.Subject;

/**
 * This example demonstrates how a concurrent batch logging can be implemented using RxJava (multiple producers and one consumer scenario).
 * <p>
 * The notion of {@code batch} indicates the fact that a single subscriber processes a batch of observed events.
 * This happens either after the expected number of events have been accumulated or the specified wait time has lapsed (at this point there could be 0 or more events buffered.     
 *
 * @author TG Team
 *
 */
public class ConcurrentBatchLoggingUsingRxJava {
    public static void main(final String[] args) throws Exception {
        final ExecutorService subsribeOnExecutor = Executors.newSingleThreadExecutor();
        final Subject<Integer, Integer> observable = new SerializedSubject<>(PublishSubject.create());
        final EventProcessor proc = new EventProcessor();
        final Subscription subscirption = observable
                                          .onBackpressureDrop()
                                          .buffer(1, TimeUnit.MILLISECONDS, 30, from(subsribeOnExecutor))
                                          .subscribe(proc, ex -> System.out.println("error 1: " + ex), proc);

        final ScheduledExecutorService exec = Executors.newScheduledThreadPool(20);
        final List<ScheduledFuture<?>> producers = Stream.iterate(1, v -> v + 1)
                .limit(10)
                .map(v -> exec.scheduleWithFixedDelay(new EventProducer("Event Producer 1", observable), (int) (Math.random() * 200) + 1, (int) (Math.random() * 1000) + 1, TimeUnit.MILLISECONDS))
                .collect(toList());
        // let's terminate the whole process after the specified period of time
        exec.schedule(new SubjectTerminator(exec, subsribeOnExecutor, observable, subscirption, producers.toArray(new ScheduledFuture<?>[0])), 10, TimeUnit.SECONDS);
        
    }

    /**
     * Handles both normal and completion events. 
     *
     */
    static final class EventProcessor implements Action1<List<Integer>>, Action0 {
        
        @Override
        public void call(final List<Integer> buffer) {
            try {
                Thread.currentThread().sleep((long) (Math.random() * 1000));
            } catch (InterruptedException e) {
            }

            final String str = buffer.stream().map(n -> n.toString()).collect(Collectors.joining(","));
            System.out.printf("batch processed: %s {%s} [%s]%n", buffer.size(), str, Thread.currentThread().getName());
        }

        @Override
        public void call() {
            System.out.printf("completion: [%s]%n", Thread.currentThread().getName());
        }
        
    }

    /**
     * A task to terminate the specified subject by completing it.
     *
     */
    static final class SubjectTerminator implements Runnable {
        private final Subject<Integer, Integer> subject;
        private final Subscription subscirption;
        private final List<ScheduledFuture<?>> toCancel = new ArrayList<>();
        private final ExecutorService exec;
        private final ExecutorService subsribeOnExecutor;

        private SubjectTerminator(final ExecutorService executor, final ExecutorService subsribeOnExecutor, final Subject<Integer, Integer> subject, final Subscription subscirption, final ScheduledFuture<?>... features) {
            this.subject = subject;
            this.subscirption = subscirption;
            this.toCancel.addAll(Arrays.asList(features));
            this.exec = executor;
            this.subsribeOnExecutor = subsribeOnExecutor;
        }

        @Override
        public void run() {
            System.out.println("Shutting down...");
            for (final ScheduledFuture<?> future : toCancel) {
                future.cancel(true);
                System.out.println("cancelling");
            }
            
            subject.onCompleted();
            subscirption.unsubscribe();
            subsribeOnExecutor.shutdown();
            exec.shutdown();
        }
    }

    /**
     * A task to post values to the provided subject stream.
     *
     */
    static class EventProducer implements Runnable {
        private final Subject<Integer, Integer> subject;
        private final String name;

        public EventProducer(final String name, final Subject<Integer, Integer> subject) {
            this.name = name;
            this.subject = subject;
        }

        @Override
        public void run() {
            final int v = (int) (Math.random() * 100);
            //System.out.printf("running %s -- %s [%s]%n", name, v, Thread.currentThread().getName());
            try {
                subject.onNext(v);
            } catch (final Exception ex) {
                System.out.println("Could not perform onNext");
            }
        }
    };
}
