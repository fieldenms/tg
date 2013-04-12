package ua.com.fielden.platform.migration;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.SortedSet;

import ua.com.fielden.platform.dao.DomainMetadataAnalyser;
import ua.com.fielden.platform.dao.EntityMetadata;
import ua.com.fielden.platform.dao.PropertyMetadata;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.reflection.Finder;
import ua.com.fielden.platform.utils.EntityUtils;

public class RetrieverBatchStmtGenerator {
    private final DomainMetadataAnalyser dma;
    private final IRetriever<? extends AbstractEntity<?>> retriever;
    private final String insertStmt;
    private final List<Container> containers;
    private List<PropertyMetadata> extraFields;

    public RetrieverBatchStmtGenerator(final DomainMetadataAnalyser dma, final IRetriever<? extends AbstractEntity<?>> retriever) {
	this.dma = dma;
	this.retriever = retriever;
	final EntityMetadata<? extends AbstractEntity<?>> emd = dma.getEntityMetadata(retriever.type());
	final List<PropertyMetadata> fields = extractFields(emd);
	this.extraFields = extractExtraFields(emd);
	final List<PropertyMetadata> insertFields = new ArrayList<>();
	insertFields.addAll(fields);
	insertFields.addAll(extraFields);
	this.insertStmt = generateInsertStmt(insertFields, emd.getTable());
	this.containers = produceContainers(fields);
    }

    private List<PropertyMetadata> extractExtraFields(final EntityMetadata<? extends AbstractEntity<?>> emd) {
	final List<PropertyMetadata> result = new ArrayList<>();
	final SortedMap<String, PropertyMetadata> props = emd.getProps();
	result.add(props.get("version"));
	if (!emd.isOneToOne()) {
	    result.add(props.get("id"));
	}
	return result;
    }

    private List<PropertyMetadata> extractFields(final EntityMetadata<? extends AbstractEntity<?>> emd) {
	final List<PropertyMetadata> result = new ArrayList<>();
	final SortedSet<String> fields = EntityUtils.getFirstLevelProps(retriever.resultFields().keySet());
	final SortedMap<String, PropertyMetadata> props = emd.getProps();
	for (final String string : fields) {
	    result.add(props.get(string));
	}
	return result;
    }

    private String generateInsertStmt(final List<PropertyMetadata> fields, final String tableName) {
	final StringBuffer sb = new StringBuffer();

	sb.append("INSERT INTO ");
	sb.append(tableName);
	sb.append(" (");
	final StringBuffer sbValues = new StringBuffer();
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

    private List<Container> produceContainers(final List<PropertyMetadata> fields) {
	final List<Container> result = new ArrayList<>();

	final Map<String, Container> map = new HashMap<>();
	for (final PropertyMetadata firstLevelProp : fields) {
	    final Container container = new Container(firstLevelProp.getName(), firstLevelProp.getJavaType());
	    result.add(container);
	    map.put(firstLevelProp.getName(), container);
	}

	int index = 1;
	for (final String prop : retriever.resultFields().keySet()) {
	    map.get(EntityUtils.splitPropByFirstDot(prop).getKey()).indices.add(index);
	    index = index + 1;
	}

	return result;
    }

    public List<Integer> produceKeyFieldsIndices() {
	final List<Integer> result = new ArrayList<>();
	final List<String> keyMembersFirstLevelProps = Finder.getFieldNames(Finder.getKeyMembers(retriever.type()));

	int index = 1;
	for (final String prop : retriever.resultFields().keySet()) {
	    if (keyMembersFirstLevelProps.contains(EntityUtils.splitPropByFirstDot(prop).getKey())) {
		result.add(index);
	    }
	    index = index + 1;
	}

	return result;
    }

    List<Object> transformValues(final ResultSet rs, final Map<Class<?>, Map<Object, Integer>> cache, final int id) throws Exception {
	final List<Object> result = new ArrayList<>();
	for (final Container container : containers) {
	    final List<Object> values = new ArrayList<>();
	    for (final Integer index : container.indices) {
		values.add(rs.getObject(index.intValue()));
	    }
	    result.add(transformValue(container.propType, values, cache));
	}

	for (final PropertyMetadata propMetadata : extraFields) {
	    result.add(propMetadata.getName().equals("id") ? id : 0);
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

    public String getInsertStmt() {
	return insertStmt;
    }
}