package ua.com.fielden.platform.migration;

import java.util.ArrayList;
import java.util.HashMap;
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

public abstract class AbstractRetrieverBatchStmtGenerator {
    private final DomainMetadataAnalyser dma;
    private final IRetriever<? extends AbstractEntity<?>> retriever;
    private final String insertStmt;
    private final List<Container> containers;
    private final EntityMetadata<? extends AbstractEntity<?>> emd;

    public AbstractRetrieverBatchStmtGenerator(final DomainMetadataAnalyser dma, final IRetriever<? extends AbstractEntity<?>> retriever) {
	this.dma = dma;
	this.retriever = retriever;
	this.emd = dma.getEntityMetadata(retriever.type());
	final List<PropertyMetadata> fields = extractFields();
	this.insertStmt = generateInsertStmt(getInsertFields(fields), emd.getTable());
	this.containers = produceContainers(fields);
    }

    protected  abstract List<PropertyMetadata> getInsertFields(final List<PropertyMetadata> fields);

    protected  abstract String generateInsertStmt(final List<PropertyMetadata> fields, final String tableName);

    protected abstract List<PropertyMetadata> extractFields();

    protected List<PropertyMetadata> extractAllFields(final EntityMetadata<? extends AbstractEntity<?>> emd) {
	final List<PropertyMetadata> result = new ArrayList<>();
	final SortedSet<String> fields = EntityUtils.getFirstLevelProps(retriever.resultFields().keySet());
	final SortedMap<String, PropertyMetadata> props = emd.getProps();
	for (final String string : fields) {
	    result.add(props.get(string));
	}
	return result;
    }

    protected List<Container> produceContainers(final List<PropertyMetadata> fields) {
	final List<Container> result = new ArrayList<>();

	final Map<String, Container> map = new HashMap<>();
	for (final PropertyMetadata firstLevelProp : fields) {
	    final Container container = new Container(firstLevelProp.getName(), firstLevelProp.getJavaType());
	    result.add(container);
	    map.put(firstLevelProp.getName(), container);
	}

	int index = 1;
	for (final String prop : retriever.resultFields().keySet()) {
	    final Container aa = map.get(EntityUtils.splitPropByFirstDot(prop).getKey());
	    if (aa != null) {
		aa.indices.add(index);
	    }
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

    protected Object transformValue(final Class type, final List<Object> values, final IdCache cache) throws Exception {
	if (EntityUtils.isPersistedEntityType(type)) {
	    final Map<Object, Integer> cacheForType = cache.getCacheForType(type);
	    final Object entityKeyObject = values.size() == 1 ? values.get(0) : values;
	    final Object result = cacheForType.get(entityKeyObject);
	    if (values.size() == 1 && values.get(0) != null  && result == null) {
		System.out.println("           !!! can't find id for " + type.getSimpleName() + " with key: " + values.get(0));
	    }
	    if (values.size() > 1 && !containsOnlyNull(values) && result == null) {
		System.out.println("           !!! can't find id for " + type.getSimpleName() + " with key: " + values);
	    }

	    return result;
	} else {
	    return values.get(0);
	}
    }

    private boolean containsOnlyNull(final List<Object> values) {
	for (final Object object : values) {
	    if (object != null) {
		return false;
	    }
	}
	return true;
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

    String getInsertStmt() {
	return insertStmt;
    }

    protected IRetriever<? extends AbstractEntity<?>> getRetriever() {
        return retriever;
    }

    protected List<Container> getContainers() {
        return containers;
    }

    protected EntityMetadata<? extends AbstractEntity<?>> getEmd() {
        return emd;
    }
}