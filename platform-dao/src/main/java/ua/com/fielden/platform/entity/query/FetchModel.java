package ua.com.fielden.platform.entity.query;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import ua.com.fielden.platform.dao.DomainPersistenceMetadataAnalyser;
import ua.com.fielden.platform.dao.PropertyPersistenceInfo;
import ua.com.fielden.platform.dao.PropertyPersistenceInfo.PropertyPersistenceType;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.fetch;

public class FetchModel<T extends AbstractEntity<?>> {
    private final fetch<T> originalFetch;
    private final Map<String, fetch<? extends AbstractEntity<?>>> entityProps = new HashMap<String, fetch<? extends AbstractEntity<?>>>();
    private final Set<String> primProps = new HashSet<String>();
    private DomainPersistenceMetadataAnalyser domainPersistenceMetadataAnalyser;

    public FetchModel(final fetch<T> originalFetch, final DomainPersistenceMetadataAnalyser domainPersistenceMetadataAnalyser) {
	this.originalFetch = originalFetch;
	this.domainPersistenceMetadataAnalyser = domainPersistenceMetadataAnalyser;

	if (!EntityAggregates.class.equals(getEntityType())) {
	    if (originalFetch.isAllIncluded()) {
		withAll();
	    } else {
		includeAllFirstLevelPrimPropsAndKey();
	    }
	}

	for (final String propName : originalFetch.getIncudedProps()) {
	    with(propName, false);
	}

	for (final Entry<String, fetch<? extends AbstractEntity<?>>> entry : originalFetch.getIncludedPropsWithModels().entrySet()) {
	    with(entry.getKey(), entry.getValue());
	}
    }

    public boolean containsProp(final String propName) {
	return primProps.contains(propName) || entityProps.containsKey(propName);
    }

    private void includeAllFirstLevelPrimPropsAndKey() {
	for (final PropertyPersistenceInfo ppi : domainPersistenceMetadataAnalyser.getEntityPPIs(getEntityType())) {
	    if (!ppi.isCalculated()) {
		with(ppi.getName(), ppi.getType().equals(PropertyPersistenceType.ENTITY_MEMBER_OF_COMPOSITE_KEY) ? false : true);
	    }
	}
    }

    protected void withAll() {
	for (final PropertyPersistenceInfo ppi : domainPersistenceMetadataAnalyser.getEntityPPIs(getEntityType())) {
	    with(ppi.getName(), false);
	}
    }

    private Class getPropType(final String propName) {
	final PropertyPersistenceInfo ppi = domainPersistenceMetadataAnalyser.getPropPersistenceInfoExplicitly(getEntityType(), propName);
	if (ppi != null) {
	    return ppi.getJavaType();
	} else {
	    throw new IllegalArgumentException("Trying fetch entity of type [" + getEntityType() + "] with non-existing property [" + propName + "]");
	}
    }

    private FetchModel<T> with(final String propName, final boolean skipEntities) {
	if (EntityAggregates.class.equals(getEntityType())) {
	    primProps.add(propName);
	} else {
	    final Class propType = getPropType(propName);
	    if (propType == null) {
		throw new IllegalStateException("Couldn't determine type of property " + propName + " of entity type " + getEntityType());
	    }

	    if (AbstractEntity.class.isAssignableFrom(propType)) {
		if (!skipEntities) {
		    entityProps.put(propName, new fetch(propType));
		}
	    } else {
		primProps.add(propName);
	    }
	}

	return this;
    }

    private FetchModel<T> with(final String propName, final fetch<? extends AbstractEntity<?>> fetchModel) {
	if (getEntityType() != EntityAggregates.class) {
	    final Class propType = getPropType(propName);

	    if (propType != fetchModel.getEntityType()) {
		throw new IllegalArgumentException("Mismatch between actual type of property and its fetch model type!");
	    }
	}

	if (AbstractEntity.class.isAssignableFrom(fetchModel.getEntityType())) {
	    entityProps.put(propName, fetchModel);
	} else {
	    throw new IllegalArgumentException(propName + " has fetch model for type " + fetchModel.getEntityType().getName() + ". Fetch model with entity type is required.");
	}
	return this;
    }

    public Map<String, fetch<? extends AbstractEntity<?>>> getFetchModels() {
	return entityProps;
    }

    public Class<T> getEntityType() {
	return originalFetch.getEntityType();
    }

//    @Override
//    public String toString() {
//	return getString("     ");
//    }
//
//    private String getString(final String offset) {
//	final StringBuffer sb = new StringBuffer();
//	sb.append("\n");
//	for (final Map.Entry<String, fetch<?>> fetchModel : entityProps.entrySet()) {
//	    sb.append(offset + fetchModel.getKey() + fetchModel.getValue().getString(offset + "   "));
//	}
//
//	return sb.toString();
//    }
}