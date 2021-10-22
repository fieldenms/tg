package ua.com.fielden.platform.migration;

import static java.util.stream.Collectors.toList;
import static ua.com.fielden.platform.migration.MigrationUtils.keyPathes;
import static ua.com.fielden.platform.migration.MigrationUtils.produceContainers;
import static ua.com.fielden.platform.migration.MigrationUtils.produceKeyFieldsIndices;
import static ua.com.fielden.platform.migration.MigrationUtils.transformValue;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import ua.com.fielden.platform.entity.AbstractEntity;

public class TargetDataUpdate {
    public final Class<? extends AbstractEntity<?>> retrieverEntityType;
    public final String updateStmt;
    public final List<PropInfo> containers;
    public final List<Integer> keyIndices;
    
    public TargetDataUpdate(//
            final Class<? extends AbstractEntity<?>> retrieverEntityType, //
            final Map<String, Integer> retrieverResultFieldsIndices, //
            final EntityMd entityMd) {
        
        this.retrieverEntityType = retrieverEntityType;
        this.containers = produceContainers(entityMd.props, keyPathes(retrieverEntityType), retrieverResultFieldsIndices, true);
        this.updateStmt = generateUpdateStmt(containers.stream().map(f -> f.column).collect(toList()), entityMd.tableName);
        this.keyIndices = produceKeyFieldsIndices(retrieverEntityType, retrieverResultFieldsIndices);
    }

    public static String generateUpdateStmt(final List<String> columns, final String tableName) {
        final StringBuffer sb = new StringBuffer();

        sb.append("UPDATE ");
        sb.append(tableName);
        sb.append(" SET ");
        for (final Iterator<String> iterator = columns.iterator(); iterator.hasNext();) {
            final String propColumnName = iterator.next();

            sb.append(propColumnName + " = ? ");
            sb.append(iterator.hasNext() ? ", " : "");
        }
        sb.append(" WHERE _ID = ?");

        return sb.toString();
    }

    public List<Object> transformValuesForUpdate(final ResultSet legacyRs, final IdCache cache, final long id) throws SQLException {
        final List<Object> result = new ArrayList<>();
        for (final PropInfo container : containers) {
            final List<Object> values = new ArrayList<>();
            for (final Integer index : container.indices) {
                values.add(legacyRs.getObject(index.intValue()));
            }
            result.add(transformValue(container.propType, values, cache));
        }
        
        result.add(id);

        return result;
    }
}