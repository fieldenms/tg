package ua.com.fielden.platform.ddl;

import org.junit.Test;
import ua.com.fielden.platform.utils.DbUtils;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class PhasedDdlTest {

    @Test
    public void flatten_concatenates_phases_in_order() {
        final var ddl = new PhasedDdl(
                List.of("CREATE TABLE A;", "PK A;"),
                List.of("CREATE INDEX I1;"),
                List.of("FK A;"));
        assertEquals(List.of("CREATE TABLE A;", "PK A;", "CREATE INDEX I1;", "FK A;"),
                     ddl.flatten());
    }

    @Test
    public void flatten_with_markers_inserts_marker_between_non_empty_phases() {
        final var ddl = new PhasedDdl(
                List.of("CREATE TABLE A;", "PK A;"),
                List.of("CREATE INDEX I1;"),
                List.of("FK A;"));
        assertEquals(List.of("CREATE TABLE A;", "PK A;",
                             DbUtils.PHASE_BOUNDARY_MARKER,
                             "CREATE INDEX I1;",
                             DbUtils.PHASE_BOUNDARY_MARKER,
                             "FK A;"),
                     ddl.flattenWithMarkers());
    }

    @Test
    public void flatten_with_markers_skips_empty_phases() {
        // Empty indices phase between tables and FKs — should still produce a single boundary, not two.
        final var ddl = new PhasedDdl(
                List.of("CREATE TABLE A;"),
                List.of(),
                List.of("FK A;"));
        assertEquals(List.of("CREATE TABLE A;", DbUtils.PHASE_BOUNDARY_MARKER, "FK A;"),
                     ddl.flattenWithMarkers());
    }

    @Test
    public void flatten_with_markers_does_not_emit_marker_for_only_one_non_empty_phase() {
        final var ddl = new PhasedDdl(
                List.of("CREATE TABLE A;"),
                List.of(),
                List.of());
        assertEquals(List.of("CREATE TABLE A;"), ddl.flattenWithMarkers());
    }

    @Test
    public void flatten_with_markers_for_all_empty_phases_returns_empty() {
        final var ddl = new PhasedDdl(List.of(), List.of(), List.of());
        assertEquals(List.of(), ddl.flattenWithMarkers());
    }

}
