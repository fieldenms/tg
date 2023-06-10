package ua.com.fielden.platform.migration;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static ua.com.fielden.platform.migration.MigrationUtils.keyPaths;
import static ua.com.fielden.platform.migration.MigrationUtils.produceContainers;
import static ua.com.fielden.platform.migration.MigrationUtils.produceKeyFieldsIndices;
import static ua.com.fielden.platform.migration.MigrationUtils.transformValue;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import ua.com.fielden.platform.entity.AbstractEntity;

/**
 * Generates an UPDATE statement from field mappings and populates it with values from the legacy database.
 *
 * @author TG Team
 *
 */
public class TargetDataUpdate {
    public final Class<? extends AbstractEntity<?>> retrieverEntityType;
    public final String updateStmt;
    public final List<PropInfo> containers;
    public final List<Integer> keyIndices;
    
    public TargetDataUpdate(
            final Class<? extends AbstractEntity<?>> retrieverEntityType,
            final Map<String, Integer> retrieverResultFieldsIndices,
            final EntityMd entityMd) {
        this.retrieverEntityType = retrieverEntityType;
        this.containers = produceContainers(entityMd.props(), keyPaths(retrieverEntityType), retrieverResultFieldsIndices, true);
        this.updateStmt = generateUpdateStmt(containers.stream().map(f -> f.column()).collect(toList()), entityMd.tableName());
        this.keyIndices = produceKeyFieldsIndices(retrieverEntityType, retrieverResultFieldsIndices);
    }

    public static String generateUpdateStmt(final List<String> columns, final String tableName) {
        return "UPDATE " + tableName +
               " SET " + columns.stream().map(col -> col + " = ?").collect(joining(", ")) + " WHERE _ID = ?";
    }

    public List<Object> transformValuesForUpdate(final ResultSet legacyRs, final IdCache cache, final long id) {
        final var result = new ArrayList<>();
        for (final var propInfo : containers) {
            final var values = propInfo.indices().stream().map(index -> {try {return legacyRs.getObject(index.intValue());} catch (Exception ex) {throw new DataMigrationException("Could not read data.", ex);}}).collect(toList());
            result.add(transformValue(propInfo.propType(), values, cache));
        }
        result.add(id);
        return result;
    }

}