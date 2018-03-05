package ua.com.fielden.platform.eql.meta.result;

import ua.com.fielden.platform.entity.AbstractEntity;

public interface IEqlQueryResult extends IEqlQueryResultParent{
    String getSqlIdentifier();
    Class<? extends AbstractEntity<?>> getJavaType();
}
