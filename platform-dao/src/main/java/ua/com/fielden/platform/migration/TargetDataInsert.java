package ua.com.fielden.platform.migration;

import static java.util.Collections.unmodifiableList;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static ua.com.fielden.platform.migration.MigrationUtils.keyPathes;
import static ua.com.fielden.platform.migration.MigrationUtils.produceContainers;
import static ua.com.fielden.platform.migration.MigrationUtils.produceKeyFieldsIndices;
import static ua.com.fielden.platform.migration.MigrationUtils.transformValue;
import static ua.com.fielden.platform.types.tuples.T2.t2;
import static ua.com.fielden.platform.utils.EntityUtils.isOneToOne;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.types.tuples.T2;

/**
 * Generates an INSERT statement from field mappings and populates it with values from the legacy database.
 *
 * @author TG Team
 *
 */
public class TargetDataInsert {

    public final Class<? extends AbstractEntity<?>> retrieverEntityType;
    public final String insertStmt;
    public final List<PropInfo> containers;
    public final List<Integer> keyIndices;
    
    public TargetDataInsert(
            final Class<? extends AbstractEntity<?>> retrieverEntityType,
            final Map<String, Integer> retrieverResultFieldsIndices,
            final EntityMd entityMd) {
        
        this.retrieverEntityType = retrieverEntityType;
        this.containers = unmodifiableList(produceContainers(entityMd.props, keyPathes(retrieverEntityType), retrieverResultFieldsIndices, false));
        this.insertStmt = generateInsertStmt(containers.stream().map(f -> f.column).collect(toList()), entityMd.tableName, !isOneToOne(retrieverEntityType));
        this.keyIndices = unmodifiableList(produceKeyFieldsIndices(retrieverEntityType, retrieverResultFieldsIndices));
    }
    
    public static String generateInsertStmt(final List<String> columnNames, final String tableName, final boolean hasId) {
        // let's add version and id if necessary to a list of columns
        final var columns = new ArrayList<String>(columnNames);
        columns.add("_VERSION");
        if (hasId) {
            columns.add("_ID");
        }

        // let's form the INSERT statement
        final var sb = new StringBuilder();
        sb.append("INSERT INTO " + tableName);
        sb.append(" (" + columns.stream().collect(joining(", ")) + ") ");
        sb.append(" VALUES(" + Stream.generate(() -> "?").limit(columns.size()).collect(joining(", ")) + ") ");
        return sb.toString();
    }

    public List<T2<Object, Boolean>> transformValuesForInsert(final ResultSet legacyRs, final IdCache cache, final long id) throws SQLException {
        final var result = new ArrayList<T2<Object, Boolean>>();
        for (final var propInfo : containers) {
            final var values = propInfo.indices.stream().map(index -> {try {return legacyRs.getObject(index.intValue());} catch (Exception ex) {throw new DataMigrationException("Could not read data.", ex);}}).collect(toList());
            result.add(t2(transformValue(propInfo.propType, values, cache), propInfo.utcType));
        }

        result.add(t2(0, false)); // for version 
        if (!isOneToOne(retrieverEntityType)) {
            result.add(t2(id, false)); // for ID where applicable
        }

        return result;
    }

}