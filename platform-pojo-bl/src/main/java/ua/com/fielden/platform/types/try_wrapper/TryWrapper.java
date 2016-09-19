package ua.com.fielden.platform.types.try_wrapper;

import ua.com.fielden.platform.types.either.Either;
import ua.com.fielden.platform.types.either.Left;
import ua.com.fielden.platform.types.either.Right;

/**
 * A convenient class for performing computations that return a value, but may throw an exception in a safe manner by suppressing the exception and returning an instance of {@link Either} instead.
 * If a value gets computed then an instance of {@link Right} is returned containing that value. 
 * Otherwise, an instance of {@link Left} is return containing the captured exception as its value.
 * 
 * @author TG Team
 *
 */
public class TryWrapper {
    
    private TryWrapper() {}
    
    public static <V> Either<Exception, V> Try(final FailableComputation<V> computation) {
        try {
            return new Right<Exception, V>(computation.get());
        } catch (final Exception ex) {
            return new Left<Exception, V>(ex);
        }
    }
}
