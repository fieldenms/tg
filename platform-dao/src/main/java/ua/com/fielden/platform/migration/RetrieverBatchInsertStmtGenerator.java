package ua.com.fielden.platform.migration;

import static ua.com.fielden.platform.entity.AbstractEntity.ID;
import static ua.com.fielden.platform.entity.AbstractEntity.VERSION;
import static ua.com.fielden.platform.utils.EntityUtils.isOneToOne;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.SortedMap;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.metadata.DomainMetadataAnalyser;
import ua.com.fielden.platform.entity.query.metadata.PersistedEntityMetadata;
import ua.com.fielden.platform.entity.query.metadata.PropertyMetadata;

public class RetrieverBatchInsertStmtGenerator extends AbstractRetrieverBatchStmtGenerator {
    private final List<PropertyMetadata> extraFields;

    public RetrieverBatchInsertStmtGenerator(final DomainMetadataAnalyser dma, final IRetriever<? extends AbstractEntity<?>> retriever) {
        super(dma, retriever);
        extraFields = extractSystemFields(getEmd());
    }

    @Override
    protected List<PropertyMetadata> getInsertFields(final List<PropertyMetadata> fields) {
        final List<PropertyMetadata> result = new ArrayList<>();
        result.addAll(fields);
        result.addAll(extractSystemFields(getEmd()));
        return result;
    }

    private List<PropertyMetadata> extractSystemFields(final PersistedEntityMetadata<? extends AbstractEntity<?>> emd) {
        final List<PropertyMetadata> result = new ArrayList<>();
        final SortedMap<String, PropertyMetadata> props = emd.getProps();
        result.add(props.get(VERSION));
        if (!isOneToOne(emd.getType())) {
            result.add(props.get(ID));
        }
        return result;
    }

    @Override
    protected String generateInsertStmt(final List<PropertyMetadata> fields, final String tableName) {
        final StringBuilder sb = new StringBuilder();

        sb.append("INSERT INTO ");
        sb.append(tableName);
        sb.append(" (");
        final StringBuilder sbValues = new StringBuilder();
        sbValues.append(" VALUES(");
        for (final Iterator<PropertyMetadata> iterator = fields.iterator(); iterator.hasNext();) {
            final PropertyMetadata propName = iterator.next();

            sb.append(propName.getColumn());
            sb.append(iterator.hasNext() ? ", " : "");

            sbValues.append("?");
            sbValues.append(iterator.hasNext() ? ", " : "");
        }
        sb.append(") ");
        sbValues.append(") ");
        sb.append(sbValues.toString());

        return sb.toString();
    }

    List<Object> transformValuesForInsert(final ResultSet rs, final IdCache cache, final long id) throws SQLException {
        final List<Object> result = new ArrayList<>();
        for (final Container container : getContainers()) {
            final List<Object> values = new ArrayList<>();
            for (final Integer index : container.indices) {
                values.add(rs.getObject(index.intValue()));
            }
            result.add(transformValue(container.propType, values, cache));
        }

        for (final PropertyMetadata propMetadata : extraFields) {
            result.add(propMetadata.getName().equals(ID) ? id : 0);
        }

        return result;
    }

    @Override
    protected List<PropertyMetadata> extractFields() {
        return super.extractAllFields(getEmd());
    }
}