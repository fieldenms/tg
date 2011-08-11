package ua.com.fielden.platform.equery;

import java.util.List;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.Required;
import ua.com.fielden.platform.equery.interfaces.IEntityMapper;
import ua.com.fielden.platform.equery.interfaces.IMappingExtractor;
import ua.com.fielden.platform.reflection.Finder;

final class RealEntitySubMapper extends AbstractMapper implements IEntityMapper {
    private IEntityMapper parentMapper; // alias, which represent parent of the given alias property - e.g. model for make

    private String propName;
    private Class<? extends AbstractEntity> propType;
    private String tableName;

    RealEntitySubMapper(final IEntityMapper parentMapper, final String propName, final Class<? extends AbstractEntity> propType, final IMappingExtractor mappingExtractor, final AliasNumerator aliasNumerator) {
	this.parentMapper = parentMapper;
	this.propName = propName;
	this.propType = propType;
	this.setAliasNumerator(aliasNumerator);
	setMappingExtractor(mappingExtractor);
	getPropertiesColumns().putAll(mappingExtractor.getColumns(propType, this));
	assignColumnAliases();
	tableName = mappingExtractor.getTableClause(propType);
	setLeftJoined(parentMapper.isLeftJoined() || parentMapper.getPropType() == null ? true : !(isPropertyPartOfKey() || isPropertyRequired()));
    }

    private boolean isPropertyPartOfKey() {
	return Finder.getFieldNames(Finder.getKeyMembers(parentMapper.getPropType())).contains(propName);
    }

    private boolean isPropertyRequired() {
	return Finder.getFieldNames(Finder.findProperties(parentMapper.getPropType(), Required.class)).contains(propName);
    }

    @Override
    protected String getSqlBody() {
	final StringBuffer sb = new StringBuffer();
	sb.append(isLeftJoined() ? " LEFT" : " INNER");
	sb.append(" JOIN ");
	sb.append(getTableName());
	sb.append(" ");
	sb.append(getSqlAlias());
	sb.append(" ON ");
	sb.append(getSqlAlias() + "." + getIdColumn().getColumnName() + " = ");
	sb.append(getParentMapper().getSqlAlias() + ".");
	sb.append(getParentMapper().getPropertiesColumns().get(getPropName()).getColumnName());
	return sb.toString();
    }

    @Override
    public boolean isSynthetic() {
	return false;
    }

    @Override
    public boolean isReturned() {
	return false;
    }

    @Override
    public Class<? extends AbstractEntity> getPropType() {
	return propType;
    }

    @Override
    public IEntityMapper getParentMapper() {
	return parentMapper;
    }

    @Override
    public String getTableName() {
	return tableName;
    }

    @Override
    public List<String> getResultantPropertiesAliases() {
	return null;
    }

    @Override
    public String getPropName() {
	return propName;
    }
}