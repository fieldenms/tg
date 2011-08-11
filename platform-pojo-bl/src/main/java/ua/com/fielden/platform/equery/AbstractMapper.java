package ua.com.fielden.platform.equery;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.meta.PropertyDescriptor;
import ua.com.fielden.platform.equery.interfaces.IEntityMapper;
import ua.com.fielden.platform.equery.interfaces.IMappingExtractor;
import ua.com.fielden.platform.reflection.PropertyTypeDeterminator;

public abstract class AbstractMapper implements IEntityMapper {
    private Map<String, ColumnInfo> propertiesColumns = new HashMap<String, ColumnInfo>(); // key = property name (real or synthetic); value = column in the table or column alias in the "derived" table

    private IMappingExtractor mappingExtractor;
    private List<String> resultantPropertiesAliases;
    private Map<String, IEntityMapper> subMappers = new HashMap<String, IEntityMapper>(); // one for each property of Entity type
    private AliasNumerator aliasNumerator;
    private String sqlAlias;
    private Boolean leftJoined = false;

    public Boolean isLeftJoined() {
        return leftJoined;
    }

    protected void setLeftJoined(final Boolean leftJoined) {
        this.leftJoined = leftJoined;
    }

    protected void assignColumnAliases() {
	for (final ColumnInfo columnInfo : propertiesColumns.values()) {
	    columnInfo.setColumnAlias("C" + aliasNumerator.getNextNumber());
	}
    }

    public Map<String, ColumnInfo> getPropertiesColumns() {
        return propertiesColumns;
    }

    public Map<String, IEntityMapper> getSubMappers() {
        return subMappers;
    }

    public IMappingExtractor getMappingExtractor() {
	return mappingExtractor;
    }

    protected void setMappingExtractor(final IMappingExtractor mappingExtractor) {
        this.mappingExtractor = mappingExtractor;
    }

    public List<String> getResultantPropertiesAliases() {
        return resultantPropertiesAliases;
    }

    protected void setResultantPropertiesAliases(final List<String> resultantPropertiesAliases) {
        this.resultantPropertiesAliases = resultantPropertiesAliases;
    }

    public abstract Class getPropType();

    public abstract boolean isReturned();
    public abstract boolean isSynthetic();
    public abstract IEntityMapper getParentMapper();
    public abstract String getPropName();
    public abstract String getTableName();
    protected abstract String getSqlBody();

    public String getFromClauseSql() {
	final StringBuffer sb = new StringBuffer();

	sb.append(getSqlBody());

	for (final IEntityMapper subMapper : getSubMappers().values()) {
	    sb.append(subMapper.getFromClauseSql());
	}

	return sb.toString();
    }

    protected Class<?> getPropType(final IEntityMapper currMapper, final String prop) {
	final Class<?> propTypeJava = PropertyTypeDeterminator.determinePropertyType(currMapper.getPropType(), prop);

	if (currMapper.getPropertiesColumns().get(prop) == null) { // handles the case of custom user types - e.g. money.amount, money.currency
	    return propTypeJava;
	}

	final String propHibernateEntityType = currMapper.getPropertiesColumns().get(prop) instanceof ColumnInfoForEntityProp ? ((ColumnInfoForEntityProp) currMapper
		.getPropertiesColumns().get(prop)).getHibernateEntityType() : null;
	try {
	    return propHibernateEntityType != null ? Class.forName(propHibernateEntityType) : propTypeJava;
	} catch (final Exception e) {
	    throw new RuntimeException(propTypeJava + " Couldn't get prop type from curr mapper prop data: " + propHibernateEntityType + " due to: " + e);
	}
    }

    /**
     * Splits given dot-notated property name to separate tokens.
     *
     * @param propertyDotName
     * @return
     */
    protected List<String> splitDotProperty(final String propertyDotName) {
	return Arrays.asList(propertyDotName.split("\\."));
    }

    protected boolean isEntity(final Class propType) {
	return AbstractEntity.class.isAssignableFrom(propType) && !PropertyDescriptor.class.isAssignableFrom(propType);
    }

    public IEntityMapper getSubMapperForProperty(final IEntityMapper parentMapper, final String propName, final Class<? extends AbstractEntity> propType) {
	final IEntityMapper foundMapper = parentMapper.getSubMappers().get(propName);
	if (foundMapper != null) {
	    return foundMapper;
	} else {
	    final IEntityMapper createdMapper = new RealEntitySubMapper(parentMapper, propName, propType, getMappingExtractor(), getAliasNumerator());
	    parentMapper.getSubMappers().put(propName, createdMapper);
	    return createdMapper;
	}
    }

    public ColumnInfo getIdColumn() {
	return getPropertiesColumns().get("id");
    }

    @Override
    public String toString() {
	final StringBuffer sb = new StringBuffer();
	sb.append("EntityAlias:\n");
	sb.append("  isSynthetic: " + isSynthetic() + "\n");
	sb.append(getParentMapper() != null ? "  parentAlias: " + getParentMapper().getSqlAlias() + "\n" : "");
	sb.append(getPropName() != null ? "  propName: " + getPropName() + "\n" : "");
	sb.append(getPropType() != null ? "  propType: " + getPropType() + "\n" : "");
	sb.append(getTableName() != null ? "  tableName: " + getTableName() + "\n" : "");
	sb.append("  propertiesColumns: " + getPropertiesColumns() + "\n");
	sb.append("  subAliases:\n  " + getSubMappers() + "\n");

	return sb.toString();
    }

    public AliasNumerator getAliasNumerator() {
        return aliasNumerator;
    }

    public void setAliasNumerator(final AliasNumerator aliasNumerator) {
        this.aliasNumerator = aliasNumerator;
        this.sqlAlias = "T" + aliasNumerator.getNextNumber();
    }

    public String getSqlAlias() {
	return sqlAlias;
    };
}
