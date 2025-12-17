package ua.com.fielden.platform.error;

import org.junit.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;
import static ua.com.fielden.platform.error.Result.*;

public class ResultTestCase {

    @Test
    public void mapping_over_failure_returns_it_as_result() {
        final Result failure = Result.failure("exception");
        assertSame(failure, failure.map(r -> successful("success")));
    }

    @Test
    public void mapping_over_success_returns_the_result_of_mapping_function() {
        final Result success = successful("success");
        final Function<? super Result, ? extends Result> f = res -> successful("new success");
        assertEquals(f.apply(success), success.map(f));
    }

    @Test
    public void mapping_over_success_with_identity_returns_it_as_result() {
        final Result success = successful("success");
        final Function<? super Result, ? extends Result> id = res -> res;
        assertSame(id.apply(success), success.map(id));
    }

    @Test
    public void mapping_over_success_with_composition_of_functions_returns_the_result_equal_to_sequential_mapping_with_those_functions() {
        final Result success = successful("success");
        final Function<? super Result, ? extends Result> f = res -> successful("f success");
        final Function<? super Result, ? extends Result> g = res -> successful("g success");
        
        final Function<? super Result, ? extends Result> fAfterG = f.compose(g);
        final Function<? super Result, ? extends Result> gAfterF = g.compose(f);
        
        assertEquals(fAfterG.apply(success), success.map(g).map(f));
        assertEquals(gAfterF.apply(success), success.map(f).map(g));
    }

    @Test
    public void sequential_mapping_that_encounters_a_failure_in_the_chain_returns_that_failure() {
        final Result success = successful("success");
        final Function<? super Result, ? extends Result> f = res -> failure("f failure");
        final Function<? super Result, ? extends Result> g = res -> successful("g success");
        
        assertEquals(failure("f failure"), success.map(g).map(f).map(g));
        assertEquals(failure("f failure"), success.map(f).map(g));
    }

    @Test
    public void equals_is_reflexive_for_failure() {
        final Result f1 = failure("exception");
        assertTrue(f1.equals(f1));
        final Result f2 = failure(new Exception());
        assertTrue(f2.equals(f2));
        final Result f3 = failure(new Exception("exception"));
        assertTrue(f3.equals(f3));
        // instance argument is just a string value for the purpose of testing
        final Result f4 = failure("instance", new Exception());
        assertTrue(f4.equals(f4));
        final Result f5 = failure("instance", new Exception("exception"));
        assertTrue(f5.equals(f5));
        final Result f6 = failure("instance", "failure reasons");
        assertTrue(f6.equals(f6));
    }

    @Test
    public void equals_is_reflexive_for_success() {
        final Result success = successful("instance");
        assertTrue(success.equals(success));
        final Warning warning = warning("warning");
        assertTrue(warning.equals(warning));
        final Informative informative = informative("informative");
        assertTrue(informative.equals(informative));
    }

    @Test
    public void equals_is_symmetric_for_failure() {
        final Result f1 = failure("exception");
        final Result f2 = failure(new Exception("exception"));
        assertTrue(f1.equals(f2) && f2.equals(f1));
        
        final Result f3 = failure("instance", new Exception("failure reasons"));
        final Result f4 = failure("instance", "failure reasons");
        assertTrue(f3.equals(f4) && f4.equals(f3));
    }

    @Test
    public void equals_is_symmetric_for_success() {
        final Result success1 = successful("instance");
        final Result success2 = successful("instance");
        assertTrue(success1.equals(success2) && success2.equals(success1));
        
        final Warning warning1 = warning("warning");
        final Warning warning2 = warning("warning");
        assertTrue(warning1.equals(warning2) && warning2.equals(warning1));
        
        final Informative informative1 = informative("informative");
        final Informative informative2 = informative("informative");
        assertTrue(informative1.equals(informative2) && informative2.equals(informative1));
    }

    @Test
    public void equals_is_transitive_for_failure() {
        final Result f1 = failure("exception");
        final Result f2 = failure(new Exception("exception"));
        final Result f3 = failure(new Exception("exception"));
        assertTrue(f1.equals(f2) && f2.equals(f3) ? f1.equals(f3) : false);
        assertTrue(f1.hashCode() == f2.hashCode() && f2.hashCode() == f3.hashCode());
        
        final Result f4 = failure("instance", new Exception("failure reasons"));
        final Result f5 = failure("instance", new Exception("failure reasons"));
        final Result f6 = failure("instance", "failure reasons");
        assertTrue(f4.equals(f5) && f5.equals(f6) ? f4.equals(f6) : false);
        assertTrue(f4.hashCode() == f5.hashCode() && f5.hashCode() == f6.hashCode());
    }

    @Test
    public void equals_is_transitive_for_success() {
        final Result success1 = successful(List.of(1));
        final Result success2 = successful(List.of(1));
        final Result success3 = successful(List.of(1));
        assertTrue(success1.equals(success2) && success2.equals(success3) ? success1.equals(success3) : false);
        assertTrue(success1.hashCode() == success2.hashCode() && success2.hashCode() == success3.hashCode());
        
        final Warning warning1 = warning("warning");
        final Warning warning2 = warning("warning");
        final Warning warning3 = new Warning("warning");
        assertTrue(warning1.equals(warning2) && warning2.equals(warning3) ? warning1.equals(warning3) : false);
        assertTrue(warning1.hashCode() == warning2.hashCode() && warning2.hashCode() == warning3.hashCode());
        
        final Informative informative1 = informative("informative");
        final Informative informative2 = informative("informative");
        final Informative informative3 = new Informative("informative");
        assertTrue(informative1.equals(informative2) && informative2.equals(informative3) ? informative1.equals(informative3) : false);
        assertTrue(informative1.hashCode() == informative2.hashCode() && informative2.hashCode() == informative3.hashCode());
    }

    @Test
    public void difference_in_significant_fields_makes_failures_unequal() {
        final Result f1 = failure("msg");
        final Result f2 = failure("another msg");
        assertFalse(f1.equals(f2));
        assertFalse(f1.hashCode() == f2.hashCode());
        final Result f3 = failure(new Exception("msg"));
        final Result f4 = failure(new RuntimeException("msg"));
        assertFalse(f3.equals(f4));
        assertFalse(f3.hashCode() == f4.hashCode());
        final Result f5 = failure(new Exception("another msg"));
        assertFalse(f3.equals(f5));
        assertFalse(f3.hashCode() == f5.hashCode());
        final Result f6 = failure("instance", "msg");
        final Result f7 = failure("another instance", "msg");
        assertFalse(f6.equals(f7));
        assertFalse(f6.hashCode() == f7.hashCode());
    }

    @Test
    public void difference_in_significant_fields_makes_successes_unequal() {
        assertFalse(successful("instance").equals(successful("another instance")));
        assertFalse(warning("msg").equals(warning("another msg")));
    }

    @Test
    public void null_does_not_equal_failure_or_success() {
        assertFalse(failure("exception").equals(null));
        assertFalse(successful("instance").equals(null));
        assertFalse(warning("warning").equals(null));
    }
    
    @Test
    public void asRuntime_does_not_wrap_exception_of_runtime_type() {
        final Result runtimeFailure = failure("exception");
        assertSame(runtimeFailure, asRuntime(runtimeFailure));
    }

    @Test
    public void asRuntime_wraps_non_runtime_exception_into_Result() {
        assertTrue(asRuntime(new Exception("exception")) instanceof RuntimeException);
        assertTrue(asRuntime(new Exception("exception")) instanceof Result);
    }

    @Test
    public void ifFailure_is_applicable_only_for_Results_that_represent_failures() {
        final AtomicInteger integer = new AtomicInteger(0);
        failure("exception").ifFailure(ex -> integer.incrementAndGet());
        successful("success").ifFailure(ex -> integer.incrementAndGet());
        failure("another exception").ifFailure(ex -> integer.incrementAndGet());
        assertEquals(2, integer.get());
    }

    @Test
    public void ifFailure_can_be_used_for_thorwing_runtime_exceptions() {
        try {
            failure(new Exception("exception")).ifFailure(ex -> {throw asRuntime(ex);});
            fail("exception was expected");
        } catch (final Exception ex) {
            assertTrue(ex instanceof Result);
            assertEquals("exception", ex.getMessage());
        }
    }

    @Test
    public void throwRuntime_can_be_conveniently_used_in_ifFailurefor_thorwing_runtime_exceptions() {
        try {
            failure(new Exception("exception")).ifFailure(ex -> throwRuntime(ex));
            fail("exception was expected");
        } catch (final Exception ex) {
            assertTrue(ex instanceof Result);
            assertEquals("exception", ex.getMessage());
        }
    }
    
    @Test
    public void getInstanceOrElseThrow_returns_an_instance_if_result_is_successful() {
        final String value = "instance";
        final Result result = successful(value);
        assertEquals(value, result.getInstanceOrElseThrow());
        assertEquals(result.getInstance(), result.getInstanceOrElseThrow());
    }
    
    @Test
    public void getInstanceOrElseThrow_throws_exception_associated_with_unsuccessful_result() {
        final String errMsg = "error message";
        final Result result = Result.failure(errMsg);
        try {
            result.getInstanceOrElseThrow();
            fail();
        } catch(final Exception ex) {
            assertEquals(errMsg, ex.getMessage());
        }
    }

    @Test
    public void failure_captures_a_stack_trace() {
        final var result = failure("We underwent upgrade.");
        assertThat(result.getStackTrace()).isNotEmpty();
        final var resultWithEx = failure(new Exception());
        assertThat(resultWithEx.getStackTrace()).isNotEmpty();
        final var resultWithValue = failure(123, "We underwent upgrade");
        assertThat(resultWithValue.getStackTrace()).isNotEmpty();
        final var resultWithValueAndEx = failure(123, new Exception());
        assertThat(resultWithValueAndEx.getStackTrace()).isNotEmpty();
        final var resultExt = failureEx("We underwent upgrade.", "An upgrade was undergone.");
        assertThat(resultExt.getStackTrace()).isNotEmpty();
        final var resultExtWithValue = failureEx(123, "We underwent upgrade.", "An upgrade was undergone.");
        assertThat(resultExtWithValue.getStackTrace()).isNotEmpty();
        final var resultf = failuref("%s underwent upgrade.", "We");
        assertThat(resultf.getStackTrace()).isNotEmpty();
    }

    @Test
    public void successful_result_does_not_capture_a_stack_trace() {
        final var result = successful();
        assertThat(result.getStackTrace()).isEmpty();;

        final var resultWithValue = successful(123);
        assertThat(resultWithValue.getStackTrace()).isEmpty();;
    }

    @Test
    public void warning_does_not_capture_a_stack_trace() {
        final var warning = warning("Prolonged sitting is likely to cause back pain.");
        assertThat(warning.getStackTrace()).isEmpty();;
        final var warningWithValue = warning(123, "Prolonged sitting is likely to cause back pain.");
        assertThat(warningWithValue.getStackTrace()).isEmpty();;
        final var warningEx = warningEx("Get up from the chair!", "Prolonged sitting is likely to cause back pain.");
        assertThat(warningEx.getStackTrace()).isEmpty();;
        final var warningExWithValue = warningEx(123, "Get up from the chair!", "Prolonged sitting is likely to cause back pain.");
        assertThat(warningExWithValue.getStackTrace()).isEmpty();;
        final var warningf = warningf("Get up from %s", "the chair");
        assertThat(warningf.getStackTrace()).isEmpty();;
    }

    @Test
    public void informative_does_not_capture_a_stack_trace() {
        final var info = informative("Java is 30 years old.");
        assertThat(info.getStackTrace()).isEmpty();;
        final var infoWithValue = informative(30, "Java is 30 years old.");
        assertThat(infoWithValue.getStackTrace()).isEmpty();;
        final var infoEx = informativeEx("Java is 30.", "The Java programming language is 30 years old.");
        assertThat(infoEx.getStackTrace()).isEmpty();;
        final var infoExWithValue = informativeEx(30, "Java is 30.", "The Java programming language is 30 years old.");
        assertThat(infoExWithValue.getStackTrace()).isEmpty();;
        final var infof = informativef("Java is %s years old.", 30);
        assertThat(infof.getStackTrace()).isEmpty();;
    }

    @Test
    public void copyWith_copies_the_stack_trace() {
        final var failure = failure("We underwent upgrade.");
        assertThat(failure.getStackTrace()).isNotEmpty();
        final var failureCopy = failure.copyWith(40);
        assertThat(failureCopy.getStackTrace()).isNotEmpty();

        final var success = successful();
        assertThat(success.getStackTrace()).isEmpty();
        final var successCopy = success.copyWith(40);
        assertThat(successCopy.getStackTrace()).isEmpty();

        final var warning = warning("We underwent upgrade.");
        assertThat(warning.getStackTrace()).isEmpty();
        final var warningCopy = warning.copyWith(40);
        assertThat(warningCopy.getStackTrace()).isEmpty();

        final var info = informative("We underwent upgrade.");
        assertThat(info.getStackTrace()).isEmpty();
        final var infoCopy = info.copyWith(40);
        assertThat(infoCopy.getStackTrace()).isEmpty();
    }

}
