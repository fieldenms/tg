package ua.com.fielden.platform.migration;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.types.tuples.T2;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.util.Collections.nCopies;
import static ua.com.fielden.platform.migration.MigrationUtils.*;
import static ua.com.fielden.platform.types.tuples.T2.t2;
import static ua.com.fielden.platform.utils.EntityUtils.isOneToOne;

/**
 * Generates an INSERT statement from field mappings and populates it with values from the legacy database.
 *
 * @author TG Team
 *
 */
final class TargetDataInsert {

    public final Class<? extends AbstractEntity<?>> entityType;
    public final String insertStmt;
    public final List<PropInfo> containers;
    public final List<Integer> keyIndices;

    public TargetDataInsert(
            final Class<? extends AbstractEntity<?>> entityType,
            final Map<String, Integer> resultFieldIndices,
            final EntityMd entityMd)
    {
        this.entityType = entityType;
        this.containers = produceContainers(entityMd.props(), keyPaths(entityType), resultFieldIndices, false);
        this.insertStmt = generateInsertStmt(containers.stream().map(PropInfo::column).toList(),
                                             entityMd.tableName(),
                                             !isOneToOne(entityType));
        this.keyIndices = produceKeyFieldsIndices(entityType, resultFieldIndices);
    }

    public static String generateInsertStmt(final List<String> columnNames, final String tableName, final boolean hasId) {
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

    public List<T2<Object, Boolean>> transformValuesForInsert(final ResultSet legacyRs, final IdCache cache, final long id) {
        final var result = new ArrayList<T2<Object, Boolean>>();
        for (final var propInfo : containers) {
            final var values = propInfo.indices()
                    .stream()
                    .map(index -> {
                        try {
                            return legacyRs.getObject(index);
                        } catch (final Exception ex) {
                            throw new DataMigrationException("Could not read data.", ex);
                        }
                    })
                    .toList();
            result.add(t2(transformValue(propInfo.propType(), values, cache), propInfo.utcType()));
        }

        result.add(t2(0, false)); // for version
        if (!isOneToOne(entityType)) {
            result.add(t2(id, false)); // for ID where applicable
        }

        return result;
    }

}
