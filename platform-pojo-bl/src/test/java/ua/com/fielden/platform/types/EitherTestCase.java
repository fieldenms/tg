package ua.com.fielden.platform.types;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static ua.com.fielden.platform.error.Result.failure;

import org.junit.Test;

import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.types.either.Either;
import ua.com.fielden.platform.types.either.Left;
import ua.com.fielden.platform.types.either.Right;

/**
 * A test case for type {@link Either}.
 *
 * @author TG Team
 *
 */
public class EitherTestCase {

    @Test
    public void left_factory_produces_Left_either() {
        final Result failure = failure("some error");
        final Left<Exception, String> left = Either.left(failure);
        assertNotNull(left);
        assertEquals(failure, left.value);
    }

    @Test
    public void right_factory_produces_Rigth_either() {
        final String value = "some value";
        final Right<Exception, String> right = Either.right(value);
        assertNotNull(right);
        assertEquals(value, right.value);
    }

    @Test
    public void isLeft_correctly_identifies_type_of_Either() {
        assertTrue(Either.left("some left value").isLeft());
        assertFalse(Either.right("some right value").isLeft());
    }

    @Test
    public void isRight_correctly_identifies_type_of_either() {
        assertTrue(Either.right("some right value").isRight());
        assertFalse(Either.left("some left value").isRight());
    }

    @Test
    public void getOrElse_returns_alternative_value_for_Left_either() {
        final String altValue = "alt value";
        assertEquals(altValue, Either.left("left value").getOrElse(() -> altValue));
    }

    @Test
    public void getOrElse_returns_Right_value_for_Right_either() {
        final String value = "value";
        final String altValue = "alt value";
        assertEquals(value, Either.right(value).getOrElse(() -> altValue));
    }

    @Test
    public void orElseThrow_returns_Right_value_for_Right_either() {
        final String value = "value";
        final Either<Exception, String> right = Either.right(value);
        assertEquals(value, right.orElseThrow(Result::asRuntime));
    }

    @Test
    public void orElseThrow_throws_runtime_exception_for_Left_either() {
        final Exception ex = new Exception("Exception");
        final Either<Exception, String> left = Either.left(ex);
        try {
            final String gotValue = left.orElseThrow(Result::asRuntime);
            fail("Should have thrown an exception");
        } catch (final Result r) {
            assertEquals(ex, r.getEx());
        }
    }

    @Test
    public void orElseThrow_works_as_designed_for_super_class_of_L() {
        final Result result = Result.failure("Exception");
        final Either<Result, String> left = Either.left(result);
        try {
            final String gotValue = left.orElseThrow(Result::asRuntime);
            fail("Should have thrown an exception");
        } catch (final Result r) {
            assertEquals(result, r);
        }
    }

    @Test
    public void map_for_Left_either_return_Left_with_the_same_value() {
        final Either<Exception, String> either = Either.left(failure("error"));
        assertEquals(either, either.map(str -> str.length()));
    }

    @Test
    public void map_for_Right_either_returns_Right_with_value_as_result_of_mapping() {
        final String value = "some value";
        final Either<Exception, String> either = Either.right(value);
        assertEquals(Integer.valueOf(value.length()), either.map(str -> str.length()).getOrElse(() -> 0));
    }

    @Test
    public void flatMap_for_Left_either_returns_Left_with_the_same_value() {
        final Either<Exception, String> either = Either.left(failure("error"));
        assertEquals(either, either.flatMap(str -> Either.right(str.length())));
    }

    @Test
    public void flatMap_for_Rith_either_returns_new_Right_when_mapping_is_Right() {
        final Either<Exception, Integer> either = Either.right(42);
        assertEquals(Either.right("42"), either.flatMap(num -> Either.right(num +"")));
    }

    @Test
    public void flatMap_for_Rith_either_returns_new_Left_when_mapping_is_Left() {
        final Either<Exception, Integer> either = Either.right(42);
        final String error = "some error in the function";
        assertEquals(Either.left(error), either.flatMap(num -> Either.left(error)));
    }

    @Test
    public void equals_is_false_for_nulls() {
        final Either<String, Integer> eitherR1 = Either.right(42);
        final Either<String, Integer> eitherL1 = Either.left("error");

        assertFalse(eitherR1.equals(null));
        assertFalse(eitherL1.equals(null));
    }

    @Test
    public void equals_is_reflexive() {
        final Either<String, Integer> eitherR1 = Either.right(42);
        final Either<String, Integer> eitherL1 = Either.left("error");

        assertTrue(eitherR1.equals(eitherR1));
        assertTrue(eitherL1.equals(eitherL1));
        assertFalse(eitherR1.equals(null));
        assertFalse(eitherL1.equals(null));
    }

    @Test
    public void equals_is_symmetric() {
        final Either<String, Integer> eitherR1 = Either.right(42);
        final Either<String, Integer> eitherR2 = Either.right(42);
        final Either<String, Integer> eitherL1 = Either.left("error");
        final Either<String, Integer> eitherL2 = Either.left("error");

        assertTrue(eitherR1.equals(eitherR2));
        assertTrue(eitherR2.equals(eitherR1));
        assertTrue(eitherL1.equals(eitherL2));
        assertTrue(eitherL2.equals(eitherL1));
    }

    @Test
    public void equals_is_transitive() {
        final Either<String, Integer> eitherR1 = Either.right(42);
        final Either<String, Integer> eitherR2 = Either.right(42);
        final Either<String, Integer> eitherR3 = Either.right(42);
        final Either<String, Integer> eitherL1 = Either.left("error");
        final Either<String, Integer> eitherL2 = Either.left("error");
        final Either<String, Integer> eitherL3 = Either.left("error");

        assertTrue(eitherR1.equals(eitherR2));
        assertTrue(eitherR2.equals(eitherR3));
        assertTrue(eitherR1.equals(eitherR3));
        assertTrue(eitherL1.equals(eitherL2));
        assertTrue(eitherL2.equals(eitherL3));
        assertTrue(eitherL1.equals(eitherL3));
    }

    @Test
    public void hashCode_is_consistent_with_equals() {
        final Either<String, Integer> eitherR1 = Either.right(42);
        final Either<String, Integer> eitherR2 = Either.right(42);
        final Either<String, Integer> eitherL1 = Either.left("error");
        final Either<String, Integer> eitherL2 = Either.left("error");

        assertTrue(eitherR1.equals(eitherR2) && eitherR1.hashCode() == eitherR2.hashCode());
        assertTrue(eitherL1.equals(eitherL2) && eitherL1.hashCode() == eitherL2.hashCode());
    }

    @Test
    public void equals_and_hashCode_correctly_handle_Either_with_null_values() {
        final Either<String, Integer> eitherR1 = Either.right(null);
        final Either<String, Integer> eitherR2 = Either.right(null);
        final Either<String, Integer> eitherL1 = Either.left(null);
        final Either<String, Integer> eitherL2 = Either.left(null);

        assertTrue(eitherR1.equals(eitherR2) && eitherR1.hashCode() == eitherR2.hashCode());
        assertTrue(eitherL1.equals(eitherL2) && eitherL1.hashCode() == eitherL2.hashCode());
    }
}
