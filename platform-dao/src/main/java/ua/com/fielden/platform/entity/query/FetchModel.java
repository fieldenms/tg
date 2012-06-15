package ua.com.fielden.platform.entity.query;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import ua.com.fielden.platform.dao.DomainPersistenceMetadataAnalyser;
import ua.com.fielden.platform.dao.PropertyMetadata;
import ua.com.fielden.platform.dao.PropertyMetadata.PropertyCategory;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.fetch;
import ua.com.fielden.platform.entity.query.fluent.fetch.FetchCategory;

public class FetchModel<T extends AbstractEntity<?>> {
    private final fetch<T> originalFetch;
    private final Map<String, fetch<? extends AbstractEntity<?>>> entityProps = new HashMap<String, fetch<? extends AbstractEntity<?>>>();
    private final Set<String> primProps = new HashSet<String>();
    private DomainPersistenceMetadataAnalyser domainPersistenceMetadataAnalyser;

    public FetchModel(final fetch<T> originalFetch, final DomainPersistenceMetadataAnalyser domainPersistenceMetadataAnalyser) {
	this.originalFetch = originalFetch;
	this.domainPersistenceMetadataAnalyser = domainPersistenceMetadataAnalyser;

	if (!EntityAggregates.class.equals(getEntityType())) {
	    switch (originalFetch.getFetchCategory()) {
	    case ALL:
		includeAllFirstLevelProps();
		break;
	    case MINIMAL:
		includeAllFirstLevelPrimPropsAndKey();
		break;
	    case NONE:
		includeIdAndVersionOnly();
		break;
	    default:
		throw new IllegalStateException("Unknown fetch category [" + originalFetch.getFetchCategory() + "]");
	    }

	    for (final String propName : originalFetch.getExcludedProps()) {
		without(propName);
	    }
	}

	for (final String propName : originalFetch.getIncudedProps()) {
	    with(propName, false);
	}

	for (final Entry<String, fetch<? extends AbstractEntity<?>>> entry : originalFetch.getIncludedPropsWithModels().entrySet()) {
	    with(entry.getKey(), entry.getValue());
	}
    }

    private void includeAllFirstLevelPrimPropsAndKey() {
	for (final PropertyMetadata ppi : domainPersistenceMetadataAnalyser.getEntityPPIs(getEntityType())) {
	    if (!ppi.isCalculated()) {
		with(ppi.getName(), (ppi.getType().equals(PropertyCategory.ENTITY_MEMBER_OF_COMPOSITE_KEY) || ppi.getType().equals(PropertyCategory.ENTITY_KEY)) ? false : true);
	    }
	}
    }

    private void includeAllFirstLevelProps() {
	for (final PropertyMetadata ppi : domainPersistenceMetadataAnalyser.getEntityPPIs(getEntityType())) {
	    if (ppi.isUnionEntity()) {
		with(ppi.getName(), new fetch(ppi.getJavaType(), FetchCategory.ALL));
	    } else {
		with(ppi.getName(), false);
	    }
	}
    }

    private void includeIdAndVersionOnly() {
	with(AbstractEntity.ID, true);
	with(AbstractEntity.VERSION, true);
    }

    public boolean containsProp(final String propName) {
	return primProps.contains(propName) || entityProps.containsKey(propName);
    }

    private Class getPropType(final String propName) {
	final PropertyMetadata ppi = domainPersistenceMetadataAnalyser.getPropPersistenceInfoExplicitly(getEntityType(), propName);
	if (ppi != null) {
	    return ppi.getJavaType();
	} else {
	    throw new IllegalArgumentException("Trying fetch entity of type [" + getEntityType() + "] with non-existing property [" + propName + "]");
	}
    }

    private void without(final String propName) {
	final Class propType = getPropType(propName);
	if (propType == null) {
	    throw new IllegalStateException("Couldn't determine type of property " + propName + " of entity type " + getEntityType());
	}

	if (AbstractEntity.class.isAssignableFrom(propType)) {
	    final Object removalResult = entityProps.remove(propName);
	    if (removalResult == null) {
		throw new IllegalStateException("Couldn't find property [" + propName + "] to be excluded from fetched entity properties of entity type " + getEntityType());
	    }
	} else {
	    final boolean removalResult = primProps.remove(propName);
	    if (!removalResult) {
		throw new IllegalStateException("Couldn't find property [" + propName + "] to be excluded from fetched primitive properties of entity type " + getEntityType());
	    }
	}
    }

    private void with(final String propName, final boolean skipEntities) {
	if (EntityAggregates.class.equals(getEntityType())) {
	    primProps.add(propName);
	} else {
	    final Class propType = getPropType(propName);
	    if (propType == null) {
		throw new IllegalStateException("Couldn't determine type of property " + propName + " of entity type " + getEntityType());
	    }

	    if (AbstractEntity.class.isAssignableFrom(propType)) {
		if (!skipEntities) {
		    entityProps.put(propName, new fetch(propType, FetchCategory.MINIMAL));
		}
	    } else {
		primProps.add(propName);
	    }
	}
    }

    private void with(final String propName, final fetch<? extends AbstractEntity<?>> fetchModel) {
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
    }

    public Map<String, fetch<? extends AbstractEntity<?>>> getFetchModels() {
	return entityProps;
    }

    public Class<T> getEntityType() {
	return originalFetch.getEntityType();
    }
}