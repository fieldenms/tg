package ua.com.fielden.platform.migration;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import ua.com.fielden.platform.dao.DomainMetadataAnalyser;
import ua.com.fielden.platform.dao.PropertyMetadata;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.reflection.Finder;

public class RetrieverBatchUpdateStmtGenerator extends AbstractRetrieverBatchStmtGenerator {
    public RetrieverBatchUpdateStmtGenerator(final DomainMetadataAnalyser dma, final IRetriever<? extends AbstractEntity<?>> retriever) {
	super(dma, retriever);
    }

    @Override
    protected  List<PropertyMetadata> getInsertFields(final List<PropertyMetadata> fields) {
	final List<PropertyMetadata> result = new ArrayList<>();
	result.addAll(fields);
	return result;
    }

    @Override
    protected String generateInsertStmt(final List<PropertyMetadata> fields, final String tableName) {
	final StringBuffer sb = new StringBuffer();

	sb.append("UPDATE ");
	sb.append(tableName);
	sb.append(" SET ");
	for (final Iterator<PropertyMetadata> iterator = fields.iterator(); iterator.hasNext();) {
	    final PropertyMetadata propName = iterator.next();

	    sb.append(propName.getColumn() + " = ? ");
	    sb.append(iterator.hasNext() ? ", " : "");
	}
	sb.append(" WHERE _ID = ?");

	return sb.toString();
    }

    List<Object> transformValues(final ResultSet rs, final IdCache cache, final int id) throws Exception {
	final List<Object> result = new ArrayList<>();
	for (final Container container : getContainers()) {
	    final List<Object> values = new ArrayList<>();
	    for (final Integer index : container.indices) {
		values.add(rs.getObject(index.intValue()));
	    }
	    result.add(transformValue(container.propType, values, cache));
	}
	result.add(id);

	return result;
    }

    @Override
    protected List<PropertyMetadata> extractFields() {
	final List<String> keyMembersFirstLevelProps = Finder.getFieldNames(Finder.getKeyMembers(getRetriever().type()));
	final List<PropertyMetadata> result = new ArrayList<>();
	for (final PropertyMetadata pmd : extractAllFields(getEmd())) {
	    if (!keyMembersFirstLevelProps.contains(pmd.getName())) {
		result.add(pmd);
	    }
	}
	return result;
    }
}