package ua.com.fielden.platform.equery;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.AbstractUnionEntity;
import ua.com.fielden.platform.equery.interfaces.IEntityMapper;
import ua.com.fielden.platform.equery.interfaces.IJoinEntityMapper;
import ua.com.fielden.platform.equery.interfaces.IMappingExtractor;
import ua.com.fielden.platform.equery.tokens.properties.PropertyOrigin;
import ua.com.fielden.platform.types.Money;

final class SyntheticMapper extends AbstractMapper implements IJoinEntityMapper {
    private Class<? extends AbstractUnionEntity> propType;
    private ModelResult sourceModelResult;

    public SyntheticMapper(final List<String> resultantPropertiesAliases, final ModelResult sourceModelResult, final Class<? extends AbstractUnionEntity> propType, final IMappingExtractor mappingExtractor, final AliasNumerator aliasNumerator) {
	this.sourceModelResult = sourceModelResult;
	this.propType = propType;
	setAliasNumerator(aliasNumerator);
	setMappingExtractor(mappingExtractor);
	setResultantPropertiesAliases(resultantPropertiesAliases);
	getSubMappers().putAll(propType == null ? extractSubAliases(sourceModelResult) : extractSubAliasesForUnionEntity(sourceModelResult));
	getPropertiesColumns().putAll(propType == null ? extractPropertiesColumns(sourceModelResult) : extractPropertiesColumnsForUnionEntity(sourceModelResult));
	assignColumnAliases();
    }

    private Map<String, IEntityMapper> extractSubAliases(final ModelResult sourceModelResult) {
	final Map<String, IEntityMapper> result = new HashMap<String, IEntityMapper>();
	for (final Map.Entry<String, IEntityMapper> propEntry : sourceModelResult.getEntityPropsMappers().entrySet()) {
	    result.put(propEntry.getKey(), generateSyntheticSubMapper(this, propEntry.getValue(), propEntry.getKey()));
	}
	return result;
    }

    private Map<String, IEntityMapper> extractSubAliasesForUnionEntity(final ModelResult sourceModelResult) {
	final Map<String, IEntityMapper> result = new HashMap<String, IEntityMapper>();

	final Map<String, Map<String, ColumnInfo>> propColumnsMap = new HashMap<String, Map<String, ColumnInfo>>();

	for (final Map.Entry<String, ColumnInfo> propAliasEntry : sourceModelResult.getPrimitivePropsAliases().entrySet()) {
	    if (!propAliasEntry.getKey().startsWith("_")) {
		final String mapperName = propAliasEntry.getKey().substring(0, propAliasEntry.getKey().indexOf("_"));
		if (!propColumnsMap.containsKey(mapperName)) {
		    propColumnsMap.put(mapperName, new HashMap<String, ColumnInfo>());
		}
		final String propName = propAliasEntry.getKey().substring(propAliasEntry.getKey().indexOf("_") + 1);
		propColumnsMap.get(mapperName).put(propName, propAliasEntry.getValue().clon(propAliasEntry.getValue().getColumnAlias(), this));
	    }
	}
	final List<Field> unions = AbstractUnionEntity.unionProperties(propType);

	for (final Field field : unions) {
	    result.put(field.getName(), new SyntheticSubMapper(this, field.getName(), (Class<? extends AbstractEntity>) field.getType(), getMappingExtractor(), propColumnsMap.get(field.getName()), new HashMap<String, IEntityMapper>(), getAliasNumerator()));
	}
	return result;
    }

    private Map<String, ColumnInfo> extractPropertiesColumns(final ModelResult sourceModelResult) {
	final Map<String, ColumnInfo> result = new HashMap<String, ColumnInfo>();
//	for (final Map.Entry<String, IEntityMapper> entry : getSubMappers().entrySet()) {
//	    result.put(entry.getKey(), new ColumnInfo(entry.getValue().getIdColumn()));
//	}
	for (final Map.Entry<String, ColumnInfo> propAliasEntry : sourceModelResult.getPrimitivePropsAliases().entrySet()) {
	    result.put(propAliasEntry.getKey(), propAliasEntry.getValue().clon(propAliasEntry.getValue().getColumnAlias(), this));
	}
	return result;
    }

    private Map<String, ColumnInfo> extractPropertiesColumnsForUnionEntity(final ModelResult sourceModelResult) {
	final Map<String, ColumnInfo> result = new HashMap<String, ColumnInfo>();
	//	for (final Map.Entry<String, IEntityMapper> entry : getSubMappers().entrySet()) {
	//	    result.put(entry.getKey(), new ColumnInfo(entry.getValue().getIdColumn()));
	//	}
	for (final Map.Entry<String, ColumnInfo> propAliasEntry : sourceModelResult.getPrimitivePropsAliases().entrySet()) {
	    if (propAliasEntry.getKey().startsWith("_")) {
		result.put(propAliasEntry.getKey().substring(1), propAliasEntry.getValue().clon(propAliasEntry.getValue().getColumnAlias(), this));
	    }
	}
	return result;
    }

    private IEntityMapper generateSyntheticSubMapper(final IEntityMapper commonParentMapper, final IEntityMapper originalMapper, final String propName) {
	final Map<String, ColumnInfo> propColumns = new HashMap<String, ColumnInfo>();
	for (final Map.Entry<String, ColumnInfo> entry : originalMapper.getPropertiesColumns().entrySet()) {
	    propColumns.put(entry.getKey(), entry.getValue().clon(entry.getValue().getColumnAlias(), commonParentMapper));
	}
	final Map<String, IEntityMapper> syntheticSubMappers = new HashMap<String, IEntityMapper>();
	for (final IEntityMapper subMapper : originalMapper.getSubMappers().values()) {
	    syntheticSubMappers.put(subMapper.getPropName(), generateSyntheticSubMapper(commonParentMapper, subMapper, subMapper.getPropName()));
	}
	return new SyntheticSubMapper(commonParentMapper, propName, originalMapper.getPropType(), getMappingExtractor(), propColumns, syntheticSubMappers, getAliasNumerator());
    }

    @Override
    protected String getSqlBody() {
	final StringBuffer sb = new StringBuffer();
	sb.append("/*---*/ (");
	sb.append(sourceModelResult.getSql());
	sb.append(")");
	sb.append(" AS ");
	sb.append(getSqlAlias() + " /*---*/ ");
	return sb.toString();
    }

    @Override
    public IEntityMapper getParentMapperForEntityPropertyInSelect(final String propName) {
	// should be invoked only on the main alias

	final String [] props = splitDotProperty(propName).toArray(new String[]{});

	if (props.length == 1) {
	    return this;
	} else {
		IEntityMapper currMapper = null;
		if (getPropertiesColumns().get(props[0]) != null && getPropertiesColumns().get(props[0]) instanceof ColumnInfoForEntityProp) {
		    try {
			currMapper = getSubMapperForProperty(this, props[0], (Class<? extends AbstractEntity>) Class.forName(((ColumnInfoForEntityProp) getPropertiesColumns().get(props[0])).getHibernateEntityType()));
		    } catch (final ClassNotFoundException e) {
			e.printStackTrace();
		    }
		} else {
		    currMapper = getSubMappers().get(props[0]);
		}

		if (currMapper == null) {
		    return null;
		}

		for (int i = 1; i < props.length - 1; i++) {
		    final String prop = props[i];
		    final Class<?> propType = getPropType(currMapper, prop); //
		    if (isEntity(propType)) {
			currMapper = getSubMapperForProperty(currMapper, prop, (Class<? extends AbstractEntity>) propType);
		    } else {
			return null;
		    }
		}
		return currMapper;
	}
    }

    // this should be used with formulas in select, for conditions in where, for groupBy and orderBy
    public String getAliasedProperty(final PropertyOrigin propertyOrigin, final String propertyDotName) {

	// handling cases when ordering is done by calculated in the current select clause fields
	if (PropertyOrigin.ORDERBY.equals(propertyOrigin) && getResultantPropertiesAliases().contains(propertyDotName)) {
	    return propertyDotName;
	}

	IEntityMapper currMapper = this;
	final List<String> propNames = splitDotProperty(propertyDotName);

	int count = 0;
	for (final String prop : propNames) {
	    if (count != 0) { // if this is not the first portion or currMaper is not SyntheticRoot
		final Class<?> propType = getPropType(currMapper, prop);

		// If Entity and not PropertyDescriptor then
		if (isEntity(propType)) {
		    count = count + 1;
		    if (/*propertyOrigin.equals(PropertyOrigin.SELECT) ||*/ count != propNames.size()) {
			currMapper = getSubMapperForProperty(currMapper, prop, (Class<? extends AbstractEntity>) propType);
		    } else {
			return currMapper.getPropertiesColumns().get(prop).getSqlColumn();
		    }
		} else {
		    String longProp = prop;
		    for (int i = count + 1; i < propNames.size(); i++) {
			longProp = longProp + "." + propNames.get(i);
		    }
		    if (currMapper.isSynthetic()) {
			return currMapper.getPropertiesColumns().get(longProp).getSqlColumn();
		    } else {
			// if there is no such property in the entity type -- this means that we probably deal with some constant value and should return it as is.
			if (currMapper.getPropertiesColumns().get(longProp) != null) {
			    return currMapper.getPropertiesColumns().get(longProp).getSqlColumn();
			} else if (propType.equals(Money.class)){
				if (currMapper.getPropertiesColumns().get(longProp + ".amount") != null) {
				    return currMapper.getPropertiesColumns().get(longProp + ".amount").getSqlColumn();
				} else {
				    throw new RuntimeException("Should not reach here!");
				}
			} else {
			    throw new RuntimeException("Should not reach here!"); //return null;
			}
		    }
		}
	    } else {
		count = count + 1;
		if (getPropertiesColumns().get(prop) != null && getPropertiesColumns().get(prop) instanceof ColumnInfoForEntityProp && count != propNames.size()) {
		    try {
			currMapper = getSubMapperForProperty(currMapper, prop, (Class<? extends AbstractEntity>) Class.forName(((ColumnInfoForEntityProp) getPropertiesColumns().get(prop)).getHibernateEntityType()));
		    } catch (final ClassNotFoundException e) {
			throw new RuntimeException(e);
		    }
		} else {
		    currMapper = getSubMappers().get(prop);
		}

		if (currMapper == null) {
		    if (count == propNames.size()) {
			if (getPropertiesColumns().get(prop) != null) {
			    return getPropertiesColumns().get(prop).getSqlColumn();
			} else if (getPropertiesColumns().get(prop + ".amount") != null){
				    return getPropertiesColumns().get(prop + ".amount").getSqlColumn();
			} else {
			    throw new RuntimeException("could not find property: " + prop + " - " + sourceModelResult.getResultType() + " -- in properties: " + getPropertiesColumns() + " \n" + sourceModelResult.getSql());
			}

		    } else {
			throw new RuntimeException("could not find alias for property: " + propertyDotName);
		    }
		} else if (count == propNames.size()){
		    return currMapper.getPropertiesColumns().get("id").getSqlColumn();
		}
	    }
	}

	return currMapper.getParentMapper().getPropertiesColumns().get(currMapper.getPropName()).getSqlColumn();
    }

    @Override
    public boolean isSynthetic() {
	return true;
    }

    @Override
    public boolean isReturned() {
	return getResultantPropertiesAliases() != null;
    }

    @Override
    public Class<? extends AbstractEntity> getPropType() {
	return sourceModelResult.getResultType();//null;
    }

    @Override
    public IEntityMapper getParentMapper() {
        return null;
    }

    @Override
    public String getTableName() {
        return null;
    }

    @Override
    public String getPropName() {
        return null;
    }
}