package ua.com.fielden.platform.rx;

import com.google.inject.Singleton;

import rx.Observable;
import rx.subjects.PublishSubject;
import rx.subjects.SerializedSubject;
import rx.subjects.Subject;

/**
 * Produces an observable as an instance of {@link SerializedSubject} wrapping an instance of {@link PublishSubject}.
 * <p>
 * The only reason this class is abstract is to enforce type-base differentiation between observables.
 * It should be extended to provide obserbables with a specific purpose.
 * <p>
 * For example, there could be <code>VehicleObservableKind</code>, which would be used in a form of an application wide instance for publishing events pertaining to vehicle changes.
 * Then any interested parties can subscribe to this instance to listen to vehicle changes.
 * The classes implementing {@link AbstractSubjectKind} that require an application wide scope should be annotated with annotation {@link Singleton}.
 * This ensures their instantiation by Guice in a singleton scope.
 * <p>
 * There could even be derived data streams of a local nature, resulting from transformations such as filtering, that observers would subscribe to.
 * For example, there could be a client interested only in changes of vehicles allocated to a specific station.
 * In this case, a new stream could be derived from an application wide instance of <code>VehicleObservableKind</code>,
 * and interested observers would subscribe to such new observable.
 *
 * @author TG Team
 *
 * @param <T>
 */
public abstract class AbstractSubjectKind<T> implements IObservableKind<T> {

    final Subject<T, T> observable = new SerializedSubject<>(PublishSubject.create());

    @Override
    public final Observable<T> asObservable() {
        return observable;
    }

}
