package ua.com.fielden.platform.migration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.commons.lang.StringUtils;

import ua.com.fielden.platform.dao.DomainMetadataAnalyser;
import ua.com.fielden.platform.dao.EntityMetadata;
import ua.com.fielden.platform.dao.PropertyMetadata;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.Updater;
import ua.com.fielden.platform.reflection.AnnotationReflector;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.utils.EntityUtils;
import ua.com.fielden.platform.utils.Pair;

public class RetrieverDeadReferencesSeeker {
    private final DomainMetadataAnalyser dma;
    private RetrieverSqlProducer rsp;

    public RetrieverDeadReferencesSeeker(final DomainMetadataAnalyser dma) {
	this.dma = dma;
	this.rsp = new RetrieverSqlProducer(dma);
    }

    public Map<Class<? extends AbstractEntity<?>>, String> determineUsers(final List<IRetriever<? extends AbstractEntity<?>>> allRetrievers) {
	final Map<Class<? extends AbstractEntity<?>>, String> result = new HashMap<Class<? extends AbstractEntity<?>>, String>();
	final Map<EntityMetadata<? extends AbstractEntity<?>>, List<RetrieverProps>> grouped = groupRetrieversByType(allRetrievers);
	final Map<Class<? extends AbstractEntity<?>>, List<EntityTypeReference>> doResult = do1(grouped);

	for (final Entry<Class<? extends AbstractEntity<?>>, List<EntityTypeReference>> entry : doResult.entrySet()) {
	    if (entry.getKey() != User.class) {
		result.put(entry.getKey(), getUnionSql(entry.getValue(), entry.getKey(), grouped.get(dma.getEntityMetadata(entry.getKey()))));
	    }
	}

	return result;
    }

    private String getUnionSql(final List<EntityTypeReference> references, final Class<? extends AbstractEntity<?>> referencedType, final List<RetrieverProps> referenceSources) {
	final StringBuffer sb = new StringBuffer();
	sb.append("SELECT COUNT(*) FROM (");
	for (final Iterator<EntityTypeReference> iterator = references.iterator(); iterator.hasNext();) {
	    final EntityTypeReference entityTypeReference = iterator.next();
	    sb.append(getSelectFieldsOnlySql(entityTypeReference) + "\n");
	    sb.append(iterator.hasNext() ? "UNION" : "");
	}
	sb.append(") AS TTT WHERE NOT ("); //NOT EXISTS (SELECT * FROM " + referencedType.getSimpleName() + " WHERE ...)");

	final Map<String, String> criteria = new HashMap<String, String>();
	for (final Iterator<String> iterator = rsp.getKeyProps(referencedType, dma).iterator(); iterator.hasNext();) {
	    final String keyProp = iterator.next();
	    sb.append("TTT.\"" + keyProp + "\" IS NULL");
	    sb.append(iterator.hasNext() ? " AND " : "");
	    criteria.put(keyProp, "TTT.\"" + keyProp + "\"");
	}

//	sb.append(") AND NOT EXISTS (SELECT * " + rsp.getCoreSqlWithCriteria(referenceSources.iterator().next().retriever, criteria) + ")");
	sb.append(")");
	for (final RetrieverProps retrieverProps : referenceSources) {
	    if (!AnnotationReflector.isAnnotationPresentForClass(Updater.class, retrieverProps.retriever.getClass())) {
		sb.append(" AND NOT EXISTS (SELECT * " + rsp.getCoreSqlWithCriteria(retrieverProps.retriever, criteria) + ")");
	    }
	}
	return sb.toString();
    }

    private String getSelectFieldsOnlySql(final EntityTypeReference entityTypeReference) {
	final StringBuffer sb = new StringBuffer();
	sb.append(rsp.getDistinctYieldsSql(getSpecifiedPropsYields2(entityTypeReference)));
	sb.append(rsp.getCoreSql(entityTypeReference.retriever));
	return sb.toString();
    }

    private SortedMap<String, String> getSpecifiedPropsYields2(final EntityTypeReference entityTypeReference) {
	final SortedMap<String, String> result = new TreeMap<String, String>();
	for (final Map.Entry<String, String> resultField : entityTypeReference.retriever.resultFields().entrySet()) {
	    if (entityTypeReference.getProps().containsKey(resultField.getKey())) {
		result.put(entityTypeReference.getProps().get(resultField.getKey()), resultField.getValue());
	    }
	}
	result.put("retriever_", "'" + entityTypeReference.getFullReferenceName() + "'");
	return result;
    }

    private Map<EntityMetadata<? extends AbstractEntity<?>>, List<RetrieverProps>> groupRetrieversByType(final List<IRetriever<? extends AbstractEntity<?>>> allRetrievers) {
	final Map<EntityMetadata<? extends AbstractEntity<?>>, List<RetrieverProps>> result = new HashMap<EntityMetadata<? extends AbstractEntity<?>>, List<RetrieverProps>>();
	for (final IRetriever<? extends AbstractEntity<?>> iRetriever : allRetrievers) {
	    final EntityMetadata<? extends AbstractEntity<?>> emd = dma.getEntityMetadata(iRetriever.type());
	    final List<RetrieverProps> searchResult = result.get(emd);
	    final List<RetrieverProps> list = (searchResult != null ? searchResult : new ArrayList<RetrieverProps>());
	    list.add(getProps(iRetriever));
	    if (searchResult == null) {
		result.put(emd, list);
	    }
	}

	return result;
    }

    private RetrieverProps getProps(final IRetriever<? extends AbstractEntity<?>> retriever) {
	final Map<String, SortedSet<String>> props = new HashMap<String, SortedSet<String>>();
	for (final String name : retriever.resultFields().keySet()) {
	    final Pair<String, String> nameSplitted = EntityUtils.splitPropByFirstDot(name);
	    final SortedSet<String> searchResult = props.get(nameSplitted.getKey());
	    final SortedSet<String> subprops = (searchResult != null ? searchResult : new TreeSet<String>());
	    if (!StringUtils.isEmpty(nameSplitted.getValue())) {
		subprops.add(nameSplitted.getValue());
	    }

	    if (searchResult == null) {
		props.put(nameSplitted.getKey(), subprops);
	    }
	}

	return new RetrieverProps(retriever, props);
    }

    private Map<Class<? extends AbstractEntity<?>>, List<EntityTypeReference>> do1(final Map<EntityMetadata<? extends AbstractEntity<?>>, List<RetrieverProps>> groupedRetrievers) {
	final Map<Class<? extends AbstractEntity<?>>, List<EntityTypeReference>> result = new HashMap<Class<? extends AbstractEntity<?>>, List<EntityTypeReference>>();

	for (final Entry<EntityMetadata<? extends AbstractEntity<?>>, List<RetrieverProps>> entry : groupedRetrievers.entrySet()) {
	    for (final PropertyMetadata pmd : entry.getKey().getProps().values()) {
		if (pmd.isEntityOfPersistedType()) {
		    for (final RetrieverProps retrieverProps : entry.getValue()) {
			for (final Entry<String, SortedSet<String>> name : retrieverProps.props.entrySet()) {
			    if (name.getKey().equalsIgnoreCase(pmd.getName())) {
				final List<EntityTypeReference> searchResult = result.get(pmd.getJavaType());
				final List<EntityTypeReference> list = (searchResult != null ? searchResult : new ArrayList<EntityTypeReference>());
				list.add(new EntityTypeReference(retrieverProps.retriever, name.getKey(), name.getValue()));
				if (searchResult == null) {
				    result.put(pmd.getJavaType(), list);
				}
			    }
			}
		    }
		}
	    }
	}

	return result;
    }

    static class RetrieverProps {
	final IRetriever<? extends AbstractEntity<?>> retriever;
	Map<String, SortedSet<String>> props;

	public RetrieverProps(final IRetriever<? extends AbstractEntity<?>> retriever, final Map<String, SortedSet<String>> props) {
	    this.retriever = retriever;
	    this.props = props;
	}
    }

    static class EntityTypeReference {
	final IRetriever<? extends AbstractEntity<?>> retriever;
	final String firstLevelPropName;
	final SortedSet<String> subprops;

	public EntityTypeReference(final IRetriever<? extends AbstractEntity<?>> retriever, final String firstLevelPropName, final SortedSet<String> subprops) {
	    this.retriever = retriever;
	    this.firstLevelPropName = firstLevelPropName;
	    this.subprops = subprops;
	}

	public SortedMap<String, String> getProps() {
	    final SortedMap<String, String> result = new TreeMap<String, String>();
	    if (subprops.size() == 0) {
		result.put(firstLevelPropName, "key");
	    } else {
		for (final String name : subprops) {
		    result.put(firstLevelPropName + "." + name, name);
		}
	    }
	    return result;
	}

	private String getRetriverName() {
	    return retriever.getClass().getSimpleName().indexOf("$$") > 0 ? retriever.getClass().getSimpleName().substring(0, retriever.getClass().getSimpleName().indexOf("$$"))
		    : retriever.getClass().getSimpleName();
	}

	private String getFullReferenceName() {
	    return getRetriverName() + "." + firstLevelPropName;
	}

	@Override
	public String toString() {
	    return getRetriverName() + "." + firstLevelPropName + (subprops.size() > 0 ? subprops : "");
	}
    }
}