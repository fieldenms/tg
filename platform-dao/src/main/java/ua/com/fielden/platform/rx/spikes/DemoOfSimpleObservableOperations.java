package ua.com.fielden.platform.rx.spikes;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import rx.Observable;
import rx.schedulers.Schedulers;

/**
 * This is a very simple demo that show the use of operations on streams.
 *
 * @author TG Team
 *
 */
public class DemoOfSimpleObservableOperations {
    public static void hello(final String... names) {
        final Observable<String> stream = Observable
        .from(names)
        .map(v -> v.toUpperCase())
        .filter( v -> v.startsWith("M"));

        final ExecutorService threadPoolExecutor = Executors.newSingleThreadExecutor(); 
        
        stream.subscribeOn(Schedulers.from(threadPoolExecutor)).subscribe( s ->  System.out.printf("Hello %s! -- %s%n", s, Thread.currentThread().getName()));
        stream.subscribeOn(Schedulers.from(threadPoolExecutor)).subscribe( s ->  System.out.printf("Goodby %s! -- %s%n", s, Thread.currentThread().getName()));
        
        threadPoolExecutor.shutdown();
    }

    public static void main(final String[] args) {
        hello("mike", "bruce", "Mark", "Frederic");
    }
}
