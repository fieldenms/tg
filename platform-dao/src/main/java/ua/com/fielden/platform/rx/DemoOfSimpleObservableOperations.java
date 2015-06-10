package ua.com.fielden.platform.rx;

import rx.Observable;

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
        .filter( v -> v.startsWith("M"))
        .map(v -> v.toUpperCase());

        stream.subscribe( s ->  System.out.println("Hello " + s + "!"));
        stream.subscribe( s ->  System.out.println("Goodby " + s + "!"));
    }

    public static void main(final String[] args) {
        hello("Mike", "Bruce", "Mark", "Frederic");
    }
}
