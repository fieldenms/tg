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
    Observable<T> asObservable();
}
