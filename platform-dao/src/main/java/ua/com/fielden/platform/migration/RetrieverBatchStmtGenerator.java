package ua.com.fielden.platform.migration;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
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

    List<Container> produceContainers(final IRetriever<? extends AbstractEntity<?>> retriever) {
	final List<Container> result = new ArrayList<>();
	final EntityMetadata<? extends AbstractEntity<?>> emd = dma.getEntityMetadata(retriever.type());
	final SortedMap<String, PropertyMetadata> props = emd.getProps();
	final Map<String, Container> map = new HashMap<>();
	for (final String firstLevelProp : EntityUtils.getFirstLevelProps(retriever.resultFields().keySet())) {
	    final Container container = new Container(firstLevelProp, props.get(firstLevelProp).getJavaType());
	    result.add(container);
	    map.put(firstLevelProp, container);
	}

	int index = 1;
	for (final String prop : retriever.resultFields().keySet()) {
	    map.get(EntityUtils.splitPropByFirstDot(prop).getKey()).indices.add(index);
	    index = index + 1;
	}

	return result;
    }

    List<Object> transformValues(final ResultSet rs, final List<Container> containers, final Map<Class<?>, Map<Object, Integer>> cache) throws Exception {
	final List<Object> result = new ArrayList<>();
	for (final Container container : containers) {
	    final List<Object> values = new ArrayList<>();
	    for (final Integer index : container.indices) {
		values.add(rs.getObject(index.intValue()));
	    }
	    result.add(transformValue(container.propType, values, cache));
	}

	return result;
    }

    Object transformValue(final Class type, final List<Object> values, final Map<Class<?>, Map<Object, Integer>> cache) {
	if (EntityUtils.isPersistedEntityType(type)) {
	    return cache.get(type).get(values.size() == 1 ? values.get(0) : values);
	} else {
	    return values.get(0);
	}
    }

    public static class Container {
	String propName;
	Class propType;
	List<Integer> indices = new ArrayList<>();

	public Container(final String propName, final Class propType) {
	    this.propName = propName;
	    this.propType = propType;
	}
    }
}