package ua.com.fielden.platform.migration;

import static java.util.Collections.unmodifiableList;
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
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.types.tuples.T2;

public class TargetDataInsert {

    public final Class<? extends AbstractEntity<?>> retrieverEntityType;
    public final String insertStmt;
    public final List<PropInfo> containers;
    public final List<Integer> keyIndices;
    
    public TargetDataInsert( //
            final Class<? extends AbstractEntity<?>> retrieverEntityType, //
            final Map<String, Integer> retrieverResultFieldsIndices, //
            final EntityMd entityMd) {
        
        this.retrieverEntityType = retrieverEntityType;
        this.containers = unmodifiableList(produceContainers(entityMd.props, keyPathes(retrieverEntityType), retrieverResultFieldsIndices, false));
        this.insertStmt = generateInsertStmt(containers.stream().map(f -> f.column).collect(toList()), entityMd.tableName, !isOneToOne(retrieverEntityType));
        this.keyIndices = unmodifiableList(produceKeyFieldsIndices(retrieverEntityType, retrieverResultFieldsIndices));
    }
    
    public static String generateInsertStmt(final List<String> columns, final String tableName, final boolean hasId) {
        final StringBuilder sb = new StringBuilder();

        sb.append("INSERT INTO ");
        sb.append(tableName);
        sb.append(" (");
        final StringBuilder sbValues = new StringBuilder();
        sbValues.append(" VALUES(");
        for (final Iterator<String> iterator = columns.iterator(); iterator.hasNext();) {
            final String propColumnName = iterator.next();

            sb.append(propColumnName);
            sb.append(iterator.hasNext() ? ", " : "");

            sbValues.append("?");
            sbValues.append(iterator.hasNext() ? ", " : "");
        }
        sbValues.append(", ?");
        sb.append(", _VERSION");
        if (hasId) {
            sbValues.append(", ?");
            sb.append(", _ID");
        }

        sb.append(") ");
        sbValues.append(") ");
        sb.append(sbValues.toString());

        return sb.toString();
    }

    public List<T2<Object, Boolean>> transformValuesForInsert(final ResultSet legacyRs, final IdCache cache, final long id) throws SQLException {
        final List<T2<Object, Boolean>> result = new ArrayList<>();
        for (final PropInfo container : containers) {
            final List<Object> values = new ArrayList<>();
            for (final Integer index : container.indices) {
                values.add(legacyRs.getObject(index.intValue()));
            }
            result.add(t2(transformValue(container.propType, values, cache), container.utcType));
        }

        result.add(t2(0, false)); // for version 
        if (!isOneToOne(retrieverEntityType)) {
            result.add(t2(id, false)); // for ID where applicable
        }

        return result;
    }
}