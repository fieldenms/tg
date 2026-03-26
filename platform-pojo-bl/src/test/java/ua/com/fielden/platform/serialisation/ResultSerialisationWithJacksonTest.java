package ua.com.fielden.platform.serialisation;

import com.google.inject.Injector;
import com.google.inject.Module;
import org.junit.Test;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.error.ResultJsonDeserialiser;
import ua.com.fielden.platform.ioc.ApplicationInjectorFactory;
import ua.com.fielden.platform.serialisation.api.ISerialiser;
import ua.com.fielden.platform.serialisation.api.impl.IdOnlyProxiedEntityTypeCacheForTests;
import ua.com.fielden.platform.serialisation.api.impl.ProvidedSerialisationClassProvider;
import ua.com.fielden.platform.serialisation.api.impl.SerialisationTypeEncoder;
import ua.com.fielden.platform.test.CommonEntityTestIocModuleWithPropertyFactory;
import ua.com.fielden.platform.web.utils.PropertyConflict;

import static org.junit.Assert.*;
import static ua.com.fielden.platform.error.Result.*;
import static ua.com.fielden.platform.serialisation.api.SerialiserEngines.JACKSON;
import static ua.com.fielden.platform.serialisation.api.impl.Serialiser.createSerialiserWithJackson;

/**
 * Unit tests to ensure correct {@link Result} serialisation / deserialisation using JACKSON engine.
 *
 * @author TG Team
 */
public class ResultSerialisationWithJacksonTest {
    private final Module module = new CommonEntityTestIocModuleWithPropertyFactory();
    private final Injector injector = new ApplicationInjectorFactory().add(module).getInjector();
    private final EntityFactory factory = injector.getInstance(EntityFactory.class);
    private final ISerialiser serialiser = createSerialiserWithJackson(factory, new ProvidedSerialisationClassProvider(), new SerialisationTypeEncoder(), new IdOnlyProxiedEntityTypeCacheForTests());
    private final ISerialiser deserialiser = createSerialiserWithJackson(factory, new ProvidedSerialisationClassProvider(), new SerialisationTypeEncoder(), new IdOnlyProxiedEntityTypeCacheForTests());
    private final Object instance = "Instance";

    private Result serialiseAndDeserialise(final Result result) {
        return deserialiser.deserialise(serialiser.serialise(result, JACKSON), Result.class, JACKSON);
    }

    @Test
    public void null_Result_serialises_and_deserialises_to_equal_value() {
        assertNull(serialiseAndDeserialise(null));
    }

    @Test
    public void successful_Result_serialises_and_deserialises_to_equal_value() {
        final var result = successful();
        assertEquals(result, serialiseAndDeserialise(result));
    }

    @Test
    public void successful_Result_with_instance_serialises_and_deserialises_to_equal_value() {
        final var result = successful(instance);
        assertEquals(result, serialiseAndDeserialise(result));
    }

    @Test
    public void Informative_serialises_and_deserialises_to_equal_value() {
        final var result = informative("Informative.");
        assertEquals(result, serialiseAndDeserialise(result));
    }

    @Test
    public void Informative_with_instance_serialises_and_deserialises_to_equal_value() {
        final var result = informative(instance, "Informative.");
        assertEquals(result, serialiseAndDeserialise(result));
    }

    @Test
    public void Warning_serialises_and_deserialises_to_equal_value() {
        final var result = warning("Warning.");
        assertEquals(result, serialiseAndDeserialise(result));
    }

    @Test
    public void Warning_with_instance_serialises_and_deserialises_to_equal_value() {
        final var result = warning(instance, "Warning.");
        assertEquals(result, serialiseAndDeserialise(result));
    }

    @Test
    public void PropertyConflict_serialises_and_deserialises_to_equal_value() {
        final var result = new PropertyConflict(null, "Property conflict.");
        assertEquals(result, serialiseAndDeserialise(result));
    }

    @Test
    public void PropertyConflict_with_instance_serialises_and_deserialises_to_equal_value() {
        final var result = new PropertyConflict(instance, "Property conflict.");
        assertEquals(result, serialiseAndDeserialise(result));
    }

    @Test
    public void failure_Result_serialises_and_deserialises_to_equal_value() {
        final var result = failure("Failure.");
        assertEquals(result, serialiseAndDeserialise(result));
    }

    @Test
    public void failure_Result_with_instance_serialises_and_deserialises_to_equal_value() {
        final var result = failure(instance, "Failure.");
        assertEquals(result, serialiseAndDeserialise(result));
    }

    /**
     * Asserts equality of {@code failure} and {@code actualFailure}, but ignores the fact that exception types may not be equal.
     * <p>
     * Specifically, {@link ResultJsonDeserialiser}
     * deserialises all root exceptions as {@link Exception}.
     */
    private static void assertFailureEqualsWithCustomException(final Result failure, final Result actualFailure) {
        assertEquals(failure.getMessage(), actualFailure.getMessage());
        assertEquals(failure.getInstance(), actualFailure.getInstance());

        assertNotNull(actualFailure.getEx());
        assertNotNull(actualFailure.getEx());

        assertEquals(Exception.class, actualFailure.getEx().getClass());
        assertEquals(failure.getEx().getMessage(), actualFailure.getEx().getMessage());
    }

    @Test
    public void failure_Result_with_explicit_exception_serialises_and_deserialises_to_equal_value() {
        final var result = failure(new RuntimeException("Failure."));
        assertFailureEqualsWithCustomException(result, serialiseAndDeserialise(result));
    }

    @Test
    public void failure_Result_with_explicit_exception_and_instance_serialises_and_deserialises_to_equal_value() {
        final var result = failure(instance, new RuntimeException("Failure."));
        assertFailureEqualsWithCustomException(result, serialiseAndDeserialise(result));
    }

    /**
     * Asserts equality of {@code failure} and {@code actualFailure} that contain {@link Throwable#getCause()}.
     * <p>
     * Ignores the fact that exception (and its cause) types may not be equal.
     * Specifically, {@link ResultJsonDeserialiser}
     * deserialises all root exceptions as {@link Exception} and all causes as {@link Throwable}.
     */
    private static void assertFailureEqualsWithCustomExceptionAndCause(final Result failure, final Result actualFailure) {
        assertFailureEqualsWithCustomException(failure, actualFailure);

        assertNotNull(actualFailure.getEx().getCause());
        assertNotNull(actualFailure.getEx().getCause());

        assertEquals(failure.getEx().getCause().getMessage(), actualFailure.getEx().getCause().getMessage());
        assertEquals(Throwable.class, actualFailure.getEx().getCause().getClass());
    }

    @Test
    public void failure_Result_with_explicit_exception_and_cause_serialises_and_deserialises_to_equal_value() {
        final var result = failure(new RuntimeException("Failure.", new RuntimeException("Cause.")));
        assertFailureEqualsWithCustomExceptionAndCause(result, serialiseAndDeserialise(result));
    }

    // tests for https://github.com/fieldenms/tg/issues/2379

    @Test
    public void failure_Result_with_explicit_self_referencing_exception_serialises_and_deserialises_to_equal_value() {
        final var result = failure(new SelfReferencingException("Failure."));
        final var actualResult = serialiseAndDeserialise(result);
        assertFailureEqualsWithCustomException(result, actualResult);
        assertNull(actualResult.getEx().getCause()); // check actual deserialised self-reference
    }

    @Test
    public void failure_Result_with_explicit_exception_and_self_referencing_cause_serialises_and_deserialises_to_equal_value() {
        final var result = failure(new RuntimeException("Failure.", new SelfReferencingException("Cause.")));
        final var actualResult = serialiseAndDeserialise(result);
        assertFailureEqualsWithCustomExceptionAndCause(result, actualResult);
        assertNull(actualResult.getEx().getCause().getCause()); // check actual deserialised self-reference
    }

    /**
     * Exception type that self-references in getter ({@link #getCause()}).
     * Exist for testing purposes only.
     * <p>
     * See <a href="https://github.com/fieldenms/tg/issues/2379">Issue 2379<a> for more details.
     */
    private static class SelfReferencingException extends RuntimeException {

        public SelfReferencingException(final String message) {
            super(message);
        }

        @Override
        public synchronized Throwable getCause() {
            return this;
        }

    }

}
