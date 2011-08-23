package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IFromAlias;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IStandAloneExprOperand;
import ua.com.fielden.platform.entity.query.model.AggregatedResultQueryModel;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;

public class query {
    public static <T extends AbstractEntity> IFromAlias select(final Class<T> entityType) {
	return new FromAlias((new Tokens()).from(entityType));
    }

    public static <T extends AbstractEntity> IFromAlias select(final EntityResultQueryModel... sourceQueryModels) {
	return new FromAlias((new Tokens()).from(sourceQueryModels));
    }

    public static <T extends AbstractEntity> IFromAlias select(final AggregatedResultQueryModel... sourceQueryModels) {
	return new FromAlias((new Tokens()).from(sourceQueryModels));
    }

//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public static IStandAloneExprOperand expr() {
	return new StandAloneExpOperand(new Tokens());
    }
}
