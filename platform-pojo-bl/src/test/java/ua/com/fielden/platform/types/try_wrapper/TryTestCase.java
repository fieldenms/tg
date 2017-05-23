package ua.com.fielden.platform.types.try_wrapper;

import static org.junit.Assert.assertTrue;
import static ua.com.fielden.platform.types.try_wrapper.TryWrapper.Try;

import org.junit.Test;

import ua.com.fielden.platform.types.either.Either;
import ua.com.fielden.platform.types.either.Left;
import ua.com.fielden.platform.types.either.Right;

public class TryTestCase {

    @Test
    public void try_returns_Right_with_computed_value_for_computations_that_complete_without_exception() {
        final Either<Exception, Integer> result = Try(() -> 2 + 4);
        
        assertTrue(result instanceof Right);
        final Right<Exception, Integer> right = (Right<Exception, Integer>) result;
        assertTrue(right.value == 6);
    }

    @Test
    public void try_returns_Left_with_exception_for_computations_that_get_interrupted_with_exception() {
        final Either<Exception, Integer> result = Try(() -> {throw new IllegalArgumentException();});
        
        assertTrue(result instanceof Left);
        final Left<Exception, Integer> left = (Left<Exception, Integer>) result;
        assertTrue(left.value instanceof IllegalArgumentException);
    }

}
