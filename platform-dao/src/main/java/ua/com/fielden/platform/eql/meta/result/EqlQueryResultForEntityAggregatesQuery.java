package ua.com.fielden.platform.eql.meta.result;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.EntityAggregates;

public class EqlQueryResultForEntityAggregatesQuery implements IEqlQueryResult {

    @Override
    public String getSqlIdentifier() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Class<? extends AbstractEntity<?>> getJavaType() {
        return EntityAggregates.class;
    }

    @Override
    public EqlPropResolutionProgress resolve(final EqlPropResolutionProgress resolutionProgress) {
        // TODO Auto-generated method stub
        return null;
    }
}
