package ua.com.fielden.platform.migration;

import ua.com.fielden.platform.entity.AbstractEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.util.Collections.nCopies;

/// Details of an INSERT statement compiled from a [retriever][IRetriever].
///
/// @see MigrationUtils#targetDataInsert(Class, Map, EntityMd)
///
record TargetDataInsert (Class<? extends AbstractEntity<?>> entityType,
                         List<PropInfo> containers,
                         String insertStmt,
                         List<Integer> keyIndices)
{

    static String generateInsertStmt(final List<String> columnNames, final String tableName, final boolean hasId) {
        // All peristent entity types have `version`, but not all have `id`.
        final var columns = new ArrayList<>(columnNames);
        columns.add("_VERSION");
        if (hasId) {
            columns.add("_ID");
        }

        return "INSERT INTO %s (%s) VALUES(%s) ".formatted(
                tableName,
                String.join(", ", columns),
                String.join(", ", nCopies(columns.size(), "?")));
    }

}
