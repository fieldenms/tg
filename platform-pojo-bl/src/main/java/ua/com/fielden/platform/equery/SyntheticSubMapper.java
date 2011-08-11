package ua.com.fielden.platform.equery;

import java.util.List;
import java.util.Map;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.equery.interfaces.IEntityMapper;
import ua.com.fielden.platform.equery.interfaces.IMappingExtractor;

final class SyntheticSubMapper extends AbstractMapper implements IEntityMapper {
    private IEntityMapper parentMapper; // alias, which represent parent of the given alias property - e.g. model for make

    private String propName;
    private Class<? extends AbstractEntity> propType;

    @Override
    public String getSqlAlias() {
	return getParentMapper().getSqlAlias();
    }

    @Override
    protected String getSqlBody() {
	return " /* " + propName + " */  ";
    }

    SyntheticSubMapper(final IEntityMapper parentMapper, final String propName, final Class<? extends AbstractEntity> propType, final IMappingExtractor mappingExtractor, final Map<String, ColumnInfo> propertiesColumns, final Map<String, IEntityMapper> subMappers, final AliasNumerator aliasNumerator) {
	this.parentMapper = parentMapper;
	this.propName = propName;
	this.propType = propType;
	setAliasNumerator(aliasNumerator);
	setMappingExtractor(mappingExtractor);
	getPropertiesColumns().putAll(propertiesColumns);
	getSubMappers().putAll(subMappers);
	assignColumnAliases();
    }

    @Override
    public boolean isSynthetic() {
	return true;
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
	return null;
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