package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IJoin;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IPlainJoin;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IStandAloneExprOperand;
import ua.com.fielden.platform.entity.query.model.AggregatedResultQueryModel;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;

public class query {
    public static <T extends AbstractEntity> IPlainJoin select(final Class<T> entityType) {
	return new PlainJoin((new Tokens()).from(entityType));
    }

    public static <T extends AbstractEntity> IPlainJoin select(final EntityResultQueryModel... sourceQueryModels) {
	return new PlainJoin((new Tokens()).from(null, sourceQueryModels));
    }

    public static <T extends AbstractEntity> IPlainJoin select(final AggregatedResultQueryModel... sourceQueryModels) {
	return new PlainJoin((new Tokens()).from(null, sourceQueryModels));
    }

//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public static <T extends AbstractEntity> IJoin select(final Class<T> entityType, final String alias) {
	return new Join((new Tokens()).from(entityType, alias));
    }

    public static IJoin select(final EntityResultQueryModel sourceQueryModel, final String alias) {
	return new Join((new Tokens()).from(alias, sourceQueryModel));
    }

    public static IJoin select(final AggregatedResultQueryModel sourceQueryModel, final String alias) {
	return new Join((new Tokens()).from(alias, sourceQueryModel));
    }

//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public static IStandAloneExprOperand expr() {
	return new StandAloneExpOperand(new Tokens());
    }
}
