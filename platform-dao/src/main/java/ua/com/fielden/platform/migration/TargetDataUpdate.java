package ua.com.fielden.platform.migration;

import ua.com.fielden.platform.entity.AbstractEntity;

import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.joining;

/// Details of an UPDATE statement compiled from a [retriever][IRetriever].
///
/// @see MigrationUtils#targetDataUpdate(Class, Map, EntityMd)
///
record TargetDataUpdate (Class<? extends AbstractEntity<?>> entityType,
                         List<PropInfo> containers,
                         String updateStmt,
                         List<Integer> keyIndices)
{

    static String generateUpdateStmt(final List<String> columns, final String tableName) {
        return "UPDATE " + tableName +
               " SET " + columns.stream().map(col -> col + " = ?").collect(joining(", ")) + " WHERE _ID = ?";
    }

}
