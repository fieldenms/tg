package ua.com.fielden.platform.utils;

import org.junit.Test;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static ua.com.fielden.platform.utils.DbUtils.PHASE_BOUNDARY_MARKER;
import static ua.com.fielden.platform.utils.DbUtils.batchExecSql;

/// Unit tests for [DbUtils#batchExecSql], focusing on phase-boundary marker handling.
///
/// These tests do not require a database connection — they record JDBC interactions on a recording proxy.
///
public class DbUtilsBatchExecSqlTest {

    @Test
    public void empty_input_produces_no_batches() {
        final var result = run(List.of(), 10);
        assertEquals(List.of(), result.batches());
    }

    @Test
    public void statements_below_batch_size_produce_one_batch() {
        final var result = run(asList("a;", "b;", "c;"), 10);
        assertEquals(List.of(List.of("a;", "b;", "c;")), result.batches());
    }

    @Test
    public void statements_split_into_multiple_batches_by_size() {
        final var result = run(asList("a;", "b;", "c;", "d;", "e;"), 2);
        assertEquals(asList(asList("a;", "b;"), asList("c;", "d;"), List.of("e;")), result.batches());
    }

    @Test
    public void marker_forces_a_batch_boundary() {
        final var result = run(
                asList("CREATE TABLE A;", "CREATE TABLE B;", PHASE_BOUNDARY_MARKER, "CREATE INDEX A1;", "CREATE INDEX B1;"),
                1000);
        assertEquals(asList(asList("CREATE TABLE A;", "CREATE TABLE B;"),
                            asList("CREATE INDEX A1;", "CREATE INDEX B1;")),
                     result.batches());
    }

    @Test
    public void marker_is_not_added_to_any_batch() {
        final var result = run(asList("a;", PHASE_BOUNDARY_MARKER, "b;"), 1000);
        assertThat(result.batches())
                .allSatisfy(batch -> assertThat(batch).doesNotContain(PHASE_BOUNDARY_MARKER));
    }

    @Test
    public void leading_marker_does_not_emit_an_empty_batch() {
        final var result = run(asList(PHASE_BOUNDARY_MARKER, "a;", "b;"), 1000);
        assertEquals(List.of(asList("a;", "b;")), result.batches());
    }

    @Test
    public void trailing_marker_does_not_emit_an_empty_batch() {
        final var result = run(asList("a;", "b;", PHASE_BOUNDARY_MARKER), 1000);
        assertEquals(List.of(asList("a;", "b;")), result.batches());
    }

    @Test
    public void consecutive_markers_collapse_into_a_single_boundary() {
        final var result = run(asList("a;", PHASE_BOUNDARY_MARKER, PHASE_BOUNDARY_MARKER, "b;"), 1000);
        assertEquals(asList(List.of("a;"), List.of("b;")), result.batches());
    }

    @Test
    public void marker_combined_with_size_based_batching() {
        // batchSize=2 splits the first phase, marker forces a hard boundary regardless of size accumulator
        final var result = run(asList("a;", "b;", "c;", PHASE_BOUNDARY_MARKER, "x;", "y;", "z;"), 2);
        assertEquals(asList(asList("a;", "b;"),  // first 2 of phase 1
                            List.of("c;"),       // remainder of phase 1, flushed by marker
                            asList("x;", "y;"),  // first 2 of phase 2
                            List.of("z;")),      // remainder of phase 2
                     result.batches());
    }

    /// Records the sequence of `addBatch(...)` and `executeBatch()` calls on a mocked `Statement`.
    ///
    private record JdbcRecord(List<List<String>> batches) {

        public static JdbcRecord create(final RecordingHandler handler) {
            return new JdbcRecord(handler.batches);
        }

    }

    private static class RecordingHandler implements InvocationHandler {
        final List<List<String>> batches = new ArrayList<>();
        List<String> currentBatch = new ArrayList<>();

        @Override
        public Object invoke(final Object proxy, final Method method, final Object[] args) {
            return switch (method.getName()) {
            case "addBatch" -> { currentBatch.add((String) args[0]); yield null; }
            case "executeBatch" -> {
                final var snapshot = List.copyOf(currentBatch);
                batches.add(snapshot);
                currentBatch = new ArrayList<>();
                yield new int[snapshot.size()];
            }
            case "close" -> null;
            case "createStatement" -> Proxy.newProxyInstance(
                    getClass().getClassLoader(), new Class<?>[]{Statement.class}, this);
            case "equals" -> proxy == args[0];
            case "hashCode" -> System.identityHashCode(proxy);
            case "toString" -> "TG-Mock-JDBC";
            default -> throw new UnsupportedOperationException("unmocked: " + method.getName());
            };
        }
    }

    private static JdbcRecord run(final List<String> statements, final int batchSize) {
        final var handler = new RecordingHandler();
        final Connection conn = (Connection) Proxy.newProxyInstance(
                DbUtilsBatchExecSqlTest.class.getClassLoader(), new Class<?>[]{Connection.class}, handler);
        batchExecSql(statements, conn, batchSize);
        return JdbcRecord.create(handler);
    }

}