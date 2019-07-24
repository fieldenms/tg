package ua.com.fielden.platform.rx.spikes;

import static rx.schedulers.Schedulers.from;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import rx.Scheduler;
import rx.Subscription;
import rx.subjects.PublishSubject;
import rx.subjects.SerializedSubject;
import rx.subjects.Subject;

/**
 * This is an example of using {@link PublishSubject} for message broadcasting.
 * Messages are send to the stream from multiple tasks running on different threads.
 * <p>
 * In order to make sending messages to the stream thread safely by directly calling {@link Subject#onNext(Object)}, the instance of {@link PublishSubject} that represents the stream, is wrapped
 * into {@link SerializedSubject}.
 * <p>
 * There are two tasks that send messages to the stream every 2 and 4 seconds respectively.
 * These tasks can be thought of as emulating concurrent execution of some companion object save method by different users.
 * <p>
 * There are three observers that are subscribed to the stream, where the second observer gets unsubscribed after some time.
 * So, there are only two observers left at the end of the application lifecycle.
 * This emulates the situation where some clients may choose stop receiving messages subscribed to earlier (e.g. application was closed).
 * <p>
 * There is a separate termination task that completes the stream and shutdowns all the message sending tasks.
 * This results in the natural application completion.
 *
 * @author TG Team
 *
 */
public class DemoOfSubjectWithMultipleObserversAndConcurrentEventSubmissions {
    public static void main(final String[] args) throws Exception {
        final ExecutorService subsribeOnExecutor = Executors.newSingleThreadExecutor();
        // the same scheduler is used for 3 different subscriptions
        // this example demonstrates that even in this case subscriptions are executed on different threads and
        // different executions of the same subscription can also be executed on different threads
        final Scheduler scheduler = from(subsribeOnExecutor);
        final Subject<Integer, Integer> observable = new SerializedSubject<>(PublishSubject.create());
        final Subscription subscirption1 = observable
                                           .subscribeOn(scheduler)
                                           .subscribe(v -> System.out.printf("observer 1: %s [%s]%n", v, Thread.currentThread().getName()), ex -> System.out.println("error 1: " + ex), () -> System.out.println("completed 1"));
        final Subscription subscirption2 = observable
                                           .subscribeOn(scheduler)
                                           .subscribe(v -> System.out.printf("observer 2: %s [%s]%n", v, Thread.currentThread().getName()), ex -> System.out.println("error 2: " + ex), () -> System.out.println("completed 2"));
        final Subscription subscirption3 = observable
                                           .subscribeOn(scheduler)
                                           .subscribe(v -> System.out.printf("observer 3: %s [%s]%n", v, Thread.currentThread().getName()), ex -> System.out.println("error 3: " + ex), () -> System.out.println("completed 3"));

        final ScheduledExecutorService exec = Executors.newScheduledThreadPool(5);
        final ScheduledFuture<?> feature1 = exec.scheduleWithFixedDelay(new EventProducer("Event Producer 1", observable), 1, 2, TimeUnit.SECONDS);
        final ScheduledFuture<?> feature2 = exec.scheduleWithFixedDelay(new EventProducer("Event Producer 2", observable), 1, 4, TimeUnit.SECONDS);
        exec.schedule(new SubjectTerminator(exec, subsribeOnExecutor, observable, feature1, feature2), 9, TimeUnit.SECONDS);

        // bonus feature -- unsubscription
        Thread.sleep(5000);
        subscirption2.unsubscribe();
    }

    /**
     * A task to terminate the specified subject by completing it.
     *
     */
    static final class SubjectTerminator implements Runnable {
        private final Subject<Integer, Integer> subject;
        private final List<ScheduledFuture<?>> toCancel = new ArrayList<>();
        private final ExecutorService exec;
        private final ExecutorService subsribeOnExecutor;

        private SubjectTerminator(final ExecutorService executor, final ExecutorService subsribeOnExecutor, final Subject<Integer, Integer> subject, final ScheduledFuture<?>... features) {
            this.subject = subject;
            this.toCancel.addAll(Arrays.asList(features));
            this.exec = executor;
            this.subsribeOnExecutor = subsribeOnExecutor;
        }

        @Override
        public void run() {
            System.out.println("Shutting down...");
            subject.onCompleted();
            for (final ScheduledFuture<?> future : toCancel) {
                future.cancel(false);
            }

            exec.shutdown();
            subsribeOnExecutor.shutdown();
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
            System.out.println("running " + name);
            subject.onNext((int) (Math.random() * 100));
        }
    };
}
