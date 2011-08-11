package ua.com.fielden.platform.equery;

import java.util.Iterator;
import java.util.List;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.equery.interfaces.IEntityMapper;
import ua.com.fielden.platform.equery.interfaces.IJoinEntityMapper;
import ua.com.fielden.platform.equery.interfaces.IMappingExtractor;
import ua.com.fielden.platform.equery.tokens.properties.PropertyOrigin;
import ua.com.fielden.platform.types.Money;

final class RealEntityMapper extends AbstractMapper implements IJoinEntityMapper {
    private Class<? extends AbstractEntity> propType;
    private String tableName;

    public RealEntityMapper(final List<String> resultantPropertiesAliases, final Class<? extends AbstractEntity> propType, final IMappingExtractor mappingExtractor,
	    final AliasNumerator aliasNumerator) {
	this.propType = propType;
	setAliasNumerator(aliasNumerator);
	setMappingExtractor(mappingExtractor);
	setResultantPropertiesAliases(resultantPropertiesAliases);
	getPropertiesColumns().putAll(mappingExtractor.getColumns(propType, this));
	assignColumnAliases();
	tableName = mappingExtractor.getTableClause(propType);
    }

    @Override
    protected String getSqlBody() {
	final StringBuffer sb = new StringBuffer();
	sb.append(getTableName());
	sb.append(" ");
	sb.append(getSqlAlias());
	return sb.toString();
    }

    @Override
    public IEntityMapper getParentMapperForEntityPropertyInSelect(final String propName) {
	IEntityMapper currMapper = this;

	for (final Iterator<String> it = splitDotProperty(propName).iterator(); it.hasNext();) {
	    final String prop = it.next();
	    if (it.hasNext()) {
		final Class<?> propType = getPropType(currMapper, prop);
		if (isEntity(propType)) {
		    currMapper = getSubMapperForProperty(currMapper, prop, (Class<? extends AbstractEntity>) propType);
		} else {
		    return null; // if property is not Entity then return null -- to indicate that requested property is not Entity and therefore doesn't have mapper
		}
	    }
	}

	return currMapper;
    }

    // this should be used with formulas/expressions in select, for conditions in where, for groupBy and orderBy
    public String getAliasedProperty(final PropertyOrigin propertyOrigin, final String propertyDotName) {

	// handling cases when ordering is done by calculated in the current select clause fields
	if (PropertyOrigin.ORDERBY.equals(propertyOrigin) && getResultantPropertiesAliases().contains(propertyDotName)) {
	    return propertyDotName;
	}

	IEntityMapper currMapper = this;
	final List<String> propNames = splitDotProperty(propertyDotName);

	int count = 0;
	for (final String prop : propNames) {
	    final Class<?> propType = getPropType(currMapper, prop);

	    if (isEntity(propType)) {
		count = count + 1;
		if (count != propNames.size()) {
		    currMapper = getSubMapperForProperty(currMapper, prop, (Class<? extends AbstractEntity>) propType);
		} else {
		    return currMapper.getPropertiesColumns().get(prop).getSqlColumn();
		}
	    } else {
		String longProp = prop;
		for (int i = count + 1; i < propNames.size(); i++) {
		    longProp = longProp + "." + propNames.get(i);
		}

		// if there is no such property in the entity type -- this means that we probably deal with some constant value and should return it as is.
		if (currMapper.getPropertiesColumns().get(longProp) != null) {
		    return currMapper.getPropertiesColumns().get(longProp).getSqlColumn();
		} else if (propType.equals(Money.class)) {
		    if (currMapper.getPropertiesColumns().get(longProp + ".amount") != null) {
			return currMapper.getPropertiesColumns().get(longProp + ".amount").getSqlColumn();
		    }
		}
	    }
	}

	throw new RuntimeException("Could not find property: " + propertyDotName + " in mapper: " + this);
    }

    @Override
    public boolean isSynthetic() {
	return false;
    }

    @Override
    public boolean isReturned() {
	return getResultantPropertiesAliases() != null;
    }

    @Override
    public Class<? extends AbstractEntity> getPropType() {
	return propType;
    }

    @Override
    public IEntityMapper getParentMapper() {
	return null;
    }

    @Override
    public String getTableName() {
	return tableName;
    }

    @Override
    public String getPropName() {
	return null;
    }
}