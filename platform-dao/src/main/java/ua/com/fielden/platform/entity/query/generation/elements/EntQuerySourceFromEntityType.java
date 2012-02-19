package ua.com.fielden.platform.entity.query.generation.elements;

import java.util.Collections;
import java.util.List;

import ua.com.fielden.platform.dao.MappingsGenerator;
import ua.com.fielden.platform.dao.PropertyPersistenceInfo;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.utils.EntityUtils;
import ua.com.fielden.platform.utils.Pair;
import static ua.com.fielden.platform.reflection.PropertyTypeDeterminator.determinePropertyType;

public class EntQuerySourceFromEntityType extends AbstractEntQuerySource {
    private final Class<? extends AbstractEntity> entityType;
    private final boolean generated;

    public EntQuerySourceFromEntityType(final Class<? extends AbstractEntity> entityType, final String alias, final MappingsGenerator mappingsGenerator) {
    	this(entityType, alias, false, mappingsGenerator);
    }

    public EntQuerySourceFromEntityType(final Class<? extends AbstractEntity> entityType, final String alias, final boolean generated, final MappingsGenerator mappingsGenerator) {
	super(alias, mappingsGenerator);
	this.entityType = entityType;
	this.generated = generated;

	generateSourceItems();
    }

    private void generateSourceItems() {
	for (final PropertyPersistenceInfo ppi : getMappingsGenerator().getEntityPPIs(sourceType())) {
	    sourceColumns.put(ppi.getName(), ppi);
	}
    }

//    private void generateSourceItems() {
//	// TODO need to replace with fully-functional logic from DdlGenerator (extracted there from MappingsGenerator)
//	for (final Field propField : EntityUtils.getPersistedProperties(entityType)) {
//	    if (propField.getName().equals("id") || propField.getName().equals("version")) {
//		sourceColumns.put(propField.getName(), "_" + propField.getName());
//	    } else {
//		sourceColumns.put(propField.getName(), propField.getName() + "_");
//	    }
//	}
//    }

    @Override
    public boolean generated() {
	return generated;
    }

    @Override
    public Class sourceType() {
	return entityType;
    }

    @Override
    public String sql() {
	return sourceType().getSimpleName().toUpperCase() + "_ AS " + sqlAlias + "/*" + (alias == null ? " " : alias) + "*/";
    }

    @Override
    Pair<PurePropInfo, Class> lookForProp(final String dotNotatedPropName) {
	try {
	    final String explicitPropPart = EntityUtils.splitPropByFirstDot(dotNotatedPropName).getKey();
	    return new Pair<PurePropInfo, Class>(new PurePropInfo(explicitPropPart, determinePropertyType(sourceType(), explicitPropPart)), determinePropertyType(sourceType(), dotNotatedPropName));
	} catch (final Exception e) {
	    return null;
	}
    }

    @Override
    public List<EntValue> getValues() {
	return Collections.emptyList();
    }

    @Override
    public String toString() {
        return entityType.getSimpleName() + "-table AS " + getAlias() + " /*" + (generated ? " GEN " : "") + "*/";
    }

    @Override
    public int hashCode() {
	final int prime = 31;
	int result = 1;
	result = prime * result + ((getAlias() == null) ? 0 : getAlias().hashCode());
	result = prime * result + ((entityType == null) ? 0 : entityType.hashCode());
	return result;
    }

    @Override
    public boolean equals(final Object obj) {
	if (this == obj) {
	    return true;
	}
	if (obj == null) {
	    return false;
	}
	if (!(obj instanceof EntQuerySourceFromEntityType)) {
	    return false;
	}
	final EntQuerySourceFromEntityType other = (EntQuerySourceFromEntityType) obj;
	if (getAlias() == null) {
	    if (other.getAlias() != null) {
		return false;
	    }
	} else if (!getAlias().equals(other.getAlias())) {
	    return false;
	}
	if (entityType == null) {
	    if (other.entityType != null) {
		return false;
	    }
	} else if (!entityType.equals(other.entityType)) {
	    return false;
	}
	return true;
    }

}