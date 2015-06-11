package ua.com.fielden.platform.rx;

import rx.Observable;

/**
 * A contract that is used as an abstraction for providing observables (aka data streams) of a specific type.
 *
 * @author TG Team
 *
 * @param <T>
 */
public interface IObservableKind<T> {
    /**
     * Returns an actual Rx observable instance for the interested parties to be able to subscribe to it.
     *
     * @return
     */
    Observable<T> asObservable();

    /**
     * A method for publishing the value to this observable.
     *
     * @param value
     */
    void publish(final T value);

    /**
     * Completes the associated observable, which should be done at the end of its lifecycle.
     * This sends a relevant notification to all subscribers.
     */
    void complete();

    /**
     * Should be used to indicate an unrecoverable error in the observable operation.
     * This sends a relevant notification to all subscribers.
     *
     * @param ex -- the cause of the error
     */
    void error(final Throwable ex);
}
