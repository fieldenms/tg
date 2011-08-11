package ua.com.fielden.platform.equery.interfaces;

import java.util.List;
import java.util.Map;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.equery.ColumnInfo;

public interface IEntityMapper {
    IMappingExtractor getMappingExtractor();

    Class getPropType();

    String getSqlAlias();

    String getFromClauseSql();

    boolean isReturned();

    boolean isSynthetic();

    Map<String, ColumnInfo> getPropertiesColumns();

    ColumnInfo getIdColumn();

    IEntityMapper getSubMapperForProperty(final IEntityMapper parentMapper, final String propName, final Class<? extends AbstractEntity> propType);

    IEntityMapper getParentMapper();

    String getPropName();

    Map<String, IEntityMapper> getSubMappers();

    String getTableName();

    List<String> getResultantPropertiesAliases();

    Boolean isLeftJoined();
}
