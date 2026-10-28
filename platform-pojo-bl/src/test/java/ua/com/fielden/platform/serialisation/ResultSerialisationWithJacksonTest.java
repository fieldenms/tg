package ua.com.fielden.platform.serialisation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.THROWABLE;
import static org.junit.Assert.*;
import static ua.com.fielden.platform.error.Result.*;
import static ua.com.fielden.platform.serialisation.api.SerialiserEngines.JACKSON;
import static ua.com.fielden.platform.serialisation.api.impl.Serialiser.createSerialiserWithJackson;
import static ua.com.fielden.platform.serialisation.jackson.serialisers.ThrowableJsonSerialiser.MAX_THROWABLES;

/// Unit tests to ensure correct [Result] serialisation / deserialisation using JACKSON engine.
///
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

    private JsonNode serialiseToTree(final Object value) {
        try {
            return new ObjectMapper().readTree(serialiser.serialise(value, JACKSON));
        } catch (final IOException ex) {
            throw new AssertionError(ex);
        }
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

    /// Asserts equality of `failure` and `actualFailure`, but ignores the fact that exception types may not be equal.
    ///
    /// Specifically, [ResultJsonDeserialiser] deserialises all root exceptions as [Exception].
    ///
    private static void assertFailureEqualsWithCustomException(final Result failure, final Result actualFailure) {
        assertEquals(failure.getMessage(), actualFailure.getMessage());
        assertEquals(failure.getInstance(), actualFailure.getInstance());

        assertNotNull(failure.getEx());
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

    /// Asserts equality of `failure` and `actualFailure` that contain [Throwable#getCause()].
    ///
    /// Ignores the fact that exception (and its cause) types may not be equal.
    /// Specifically, [ResultJsonDeserialiser] deserialises all root exceptions as [Exception] and all causes as [Throwable].
    ///
    private static void assertFailureEqualsWithCustomExceptionAndCause(final Result failure, final Result actualFailure) {
        assertFailureEqualsWithCustomException(failure, actualFailure);

        assertNotNull(failure.getEx().getCause());
        assertNotNull(actualFailure.getEx().getCause());

        assertEquals(failure.getEx().getCause().getMessage(), actualFailure.getEx().getCause().getMessage());
        assertEquals(Throwable.class, actualFailure.getEx().getCause().getClass());
    }

    @Test
    public void failure_Result_with_explicit_exception_and_cause_serialises_and_deserialises_to_equal_value() {
        final var result = failure(new RuntimeException("Failure.", new RuntimeException("Cause.")));
        assertFailureEqualsWithCustomExceptionAndCause(result, serialiseAndDeserialise(result));
    }

    // :::::::::::::::::::: Tests for https://github.com/fieldenms/tg/issues/2379 ::::::::::::::::::::

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

    // :::::::::::::::::::: Tests for https://github.com/fieldenms/tg/issues/2865 ::::::::::::::::::::

    @Test
    public void failure_Result_with_exception_that_refers_to_itself_through_a_suppressed_throwable_serialises_and_deserialises_with_the_cycle_cut() {
        // As try-with-resources does, if closing a resource fails with an exception that wraps the failure of the body.
        final var bodyFailure = new RuntimeException("Failure.");
        final var closeFailure = new RuntimeException("Close failure.", bodyFailure);
        bodyFailure.addSuppressed(closeFailure);

        final var actualResult = serialiseAndDeserialise(failure(bodyFailure));
        assertEquals("Failure.", actualResult.getEx().getMessage());
        assertThat(actualResult.getEx().getSuppressed())
                .singleElement(THROWABLE)
                .hasMessage("Close failure.")
                .hasNoCause();
    }

    @Test
    public void failure_Result_with_exception_that_refers_to_itself_through_a_cause_and_a_suppressed_throwable_serialises_and_deserialises_with_the_cycle_cut() {
        final var cause = new RuntimeException("Cause.");
        final var failure = new RuntimeException("Failure.", cause);
        cause.addSuppressed(failure);

        final var result = failure(failure);
        final var actualResult = serialiseAndDeserialise(result);
        assertFailureEqualsWithCustomExceptionAndCause(result, actualResult);
        assertThat(actualResult.getEx().getCause().getSuppressed()).isEmpty();
    }

    @Test
    public void serialised_exception_with_causes_and_suppressed_throwables_contains_no_stack_traces() {
        final var subFailure = new RuntimeException("Failure.", new IllegalStateException("Cause.", new IllegalArgumentException("Cause of cause.")));
        subFailure.addSuppressed(new RuntimeException("Suppressed."));
        assertThat(subFailure.getStackTrace()).isNotEmpty();
        final var failure = failure(subFailure);

        final JsonNode ex = serialiseToTree(failure).get(EX);
        assertEquals("Failure.", ex.get("message").asText());
        assertEquals("Cause.", ex.get("cause").get("message").asText());
        assertEquals("Cause of cause.", ex.get("cause").get("cause").get("message").asText());
        assertEquals("Suppressed.", ex.get("suppressed").get(0).get("message").asText());
        assertTrue(ex.findValues("stackTrace").isEmpty());
    }

    @Test
    public void serialised_exception_contains_only_its_type_and_the_standard_properties_of_Throwable() {
        final JsonNode ex = serialiseToTree(failure(new ExceptionWithAdditionalProperty("Failure."))).get(EX);
        assertThat(ex.fieldNames()).toIterable().containsExactly("typeStr", "message", "localizedMessage", "cause", "suppressed");
    }

    @Test
    public void serialised_exception_and_its_causes_and_suppressed_throwables_contain_the_names_of_their_types() {
        final var failure = new ExceptionWithAdditionalProperty("Failure.", new IllegalStateException("Cause."));
        failure.addSuppressed(new IllegalArgumentException("Suppressed."));

        final JsonNode ex = serialiseToTree(failure(failure)).get(EX);
        assertEquals(ExceptionWithAdditionalProperty.class.getName(), ex.get("typeStr").asText());
        assertEquals(IllegalStateException.class.getName(), ex.get("cause").get("typeStr").asText());
        assertEquals(IllegalArgumentException.class.getName(), ex.get("suppressed").get(0).get("typeStr").asText());
    }

    @Test
    public void the_number_of_throwables_serialised_for_a_Result_is_bounded() {
        Throwable chain = new RuntimeException("Cause 0.");
        for (int i = 1; i < 2 * MAX_THROWABLES; i++) {
            chain = new RuntimeException("Cause %s.".formatted(i), chain);
        }

        final var actualResult = serialiseAndDeserialise(failure(new RuntimeException("Failure.", chain)));
        final var count = Stream.iterate(actualResult.getEx(), Objects::nonNull, Throwable::getCause).count();
        // The serialised Result counts towards the bound.
        assertEquals(MAX_THROWABLES - 1, count);
    }

    @Test
    public void Results_sharing_an_exception_are_serialised_with_the_exception_each() {
        final var sharedFailure = new RuntimeException("Failure.");

        final JsonNode results = serialiseToTree(new ArrayList<>(List.of(failure(sharedFailure), failure(sharedFailure))));
        assertThat(results).hasSize(2);
        assertEquals("Failure.", results.get(0).get(EX).get("message").asText());
        assertEquals("Failure.", results.get(1).get(EX).get("message").asText());
    }

    @Test
    public void exception_of_a_Result_is_serialised_in_full_also_if_the_instance_of_the_Result_contains_it() {
        // As for a failed save, where the exception of the Result is also the validation result of a property of its instance.
        final var propertyFailure = failure("Property failure.");

        final JsonNode result = serialiseToTree(failure(List.of(propertyFailure), propertyFailure));
        assertEquals("Property failure.", result.get(INSTANCE).get(0).get(MESSAGE).asText());
        assertEquals("Property failure.", result.get(EX).get(MESSAGE).asText());
    }

    @Test
    public void Result_that_is_a_cause_is_serialised_with_its_exception() {
        // As for a Result thrown in a companion and wrapped by another exception.
        final var failure = new RuntimeException("Failure.", failure(new IllegalStateException("Result failure.")));

        final JsonNode cause = serialiseToTree(failure(failure)).get(EX).get("cause");
        assertEquals(Result.class.getName(), cause.get("@resultType").asText());
        assertEquals("Result failure.", cause.get(MESSAGE).asText());
        assertEquals("Result failure.", cause.get(EX).get(MESSAGE).asText());
    }

    @Test
    public void Result_that_is_a_cause_and_refers_back_to_the_throwable_it_is_the_cause_of_is_serialised_with_the_cycle_cut() {
        final var failure = new RuntimeException("Failure.");
        failure.initCause(failure(failure));

        final JsonNode cause = serialiseToTree(failure(failure)).get(EX).get("cause");
        assertEquals(Result.class.getName(), cause.get("@resultType").asText());
        assertTrue("The exception of the Result has been written already.", cause.get(EX).isNull());
    }

    @Test
    public void Result_that_is_a_suppressed_throwable_is_serialised_once() {
        final var resultFailure = failure("Result failure.");
        final var failure = new RuntimeException("Failure.");
        failure.addSuppressed(resultFailure);
        failure.addSuppressed(resultFailure);

        final JsonNode suppressed = serialiseToTree(failure(failure)).get(EX).get("suppressed");
        assertEquals("A suppressed Result that has been written already is omitted.", 1, suppressed.size());
        assertEquals(Result.class.getName(), suppressed.get(0).get("@resultType").asText());
        assertEquals("Result failure.", suppressed.get(0).get(MESSAGE).asText());
    }

    /// Exception type with a property of its own, [#getSql], which is expected to not be serialised.
    ///
    private static class ExceptionWithAdditionalProperty extends RuntimeException {

        public ExceptionWithAdditionalProperty(final String message) {
            super(message);
        }

        public ExceptionWithAdditionalProperty(final String message, final Throwable cause) {
            super(message, cause);
        }

        public String getSql() {
            return "select * from SECRET_";
        }

    }

    /// Exception type that self-references in getter ([#getCause()]).
    /// Exists for testing purposes only.
    ///
    /// See [Issue 2379](https://github.com/fieldenms/tg/issues/2379) for more details.
    ///
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
