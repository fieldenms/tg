package ua.com.fielden.platform.migration;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.SortedMap;

import ua.com.fielden.platform.dao.DomainMetadataAnalyser;
import ua.com.fielden.platform.dao.EntityMetadata;
import ua.com.fielden.platform.dao.PropertyMetadata;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.utils.EntityUtils;

public class RetrieverBatchStmtGenerator {
    private final DomainMetadataAnalyser dma;

    public RetrieverBatchStmtGenerator(final DomainMetadataAnalyser dma) {
	this.dma = dma;
    }

    public String generateInsertStmt(final IRetriever<? extends AbstractEntity<?>> retriever) {
	final StringBuffer sb = new StringBuffer();
	final EntityMetadata<? extends AbstractEntity<?>> emd = dma.getEntityMetadata(retriever.type());
	final SortedMap<String, PropertyMetadata> props = emd.getProps();
	sb.append("INSERT INTO ");
	sb.append(emd.getTable());
	sb.append(" (_ID, _VERSION, ");
	final StringBuffer sbValues = new StringBuffer();
	sbValues.append(" VALUES(?, ?, ");
	for (final Iterator<String> iterator = EntityUtils.getFirstLevelProps(retriever.resultFields().keySet()).iterator(); iterator.hasNext();) {
	    final String propName = iterator.next();
	    sb.append(props.get(propName).getColumn());
	    sbValues.append("?");
	    sb.append(iterator.hasNext() ? ", " : "");
	    sbValues.append(iterator.hasNext() ? ", " : "");
	}
	sb.append(") ");
	sbValues.append(") ");
	sb.append(sbValues.toString());

	return sb.toString();
    }

    List<Object> transformValues(final ResultSet rs) {
	final List<Object> result = new ArrayList<Object>();
	final int index = 1;
//	for (final String propName : retriever.resultFields().keySet()) {
//	    result.add(rs.getObject(index));
//	    index = index + 1;
//	}
	return result;
    }
}