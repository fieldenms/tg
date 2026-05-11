package ua.com.fielden.platform.ddl;

import ua.com.fielden.platform.utils.DbUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/// A structured representation of database DDL, partitioned into phases that should be applied as separate execution batches.
///
/// The phases are ordered such that, when applied in sequence, all referenced objects exist by the time a phase that depends on them is executed.
/// In particular, all `CREATE TABLE` and primary-key statements complete before any `CREATE INDEX`, and all indices complete before any foreign keys.
/// Forcing this ordering at the JDBC batch level avoids name-resolution races in dialects (notably MS SQL Server with filtered indices)
/// where statements within a single submitted batch may be parsed before the metadata effects of preceding statements are visible.
///
/// @param tables
///     `CREATE TABLE` and primary-key constraint statements.
/// @param indices
///     `CREATE INDEX` and `CREATE UNIQUE INDEX` statements (including those for composite keys).
/// @param foreignKeys
///     Foreign-key constraint statements.
///     May be empty when foreign keys are deliberately omitted (e.g., for test database creation that supports out-of-order data insertion and table truncation).
///
public record PhasedDdl(
        List<String> tables,
        List<String> indices,
        List<String> foreignKeys)
{

    /// Flattens the phases into a single list of statements with [DbUtils#PHASE_BOUNDARY_MARKER] inserted between non-empty phases.
    ///
    /// The marker is recognised by [DbUtils#batchExecSql] as a forced batch boundary, ensuring each phase is submitted to the database in its own JDBC batch.
    ///
    public List<String> flattenWithMarkers() {
        final var result = new ArrayList<String>();
        boolean first = true;
        for (final List<String> phase : List.of(tables, indices, foreignKeys)) {
            if (phase.isEmpty()) {
                continue;
            }
            if (!first) {
                result.add(DbUtils.PHASE_BOUNDARY_MARKER);
            }
            result.addAll(phase);
            first = false;
        }
        return result;
    }

    /// Flattens the phases into a single list of statements without markers.
    ///
    public List<String> flatten() {
        return Stream.of(tables, indices, foreignKeys)
                .flatMap(List::stream)
                .toList();
    }

}