package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.dao.QueryExecutionModel;
import ua.com.fielden.platform.dao.QueryExecutionModel.Builder;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.EntityAggregates;
import ua.com.fielden.platform.entity.query.fetch;
import ua.com.fielden.platform.entity.query.fetchAll;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IFromAlias;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IOrderingItem;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IStandAloneExprOperand;
import ua.com.fielden.platform.entity.query.model.AggregatedResultQueryModel;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;

public class EntityQueryUtils {
    public static <T extends AbstractEntity<?>> IFromAlias select(final Class<T> entityType) {
	return new FromAlias((new Tokens()).from(entityType));
    }

    public static <T extends AbstractEntity<?>> IFromAlias select(final EntityResultQueryModel<T> sourceQueryModel) {
	return new FromAlias((new Tokens()).from(sourceQueryModel));
    }

    public static <T extends AbstractEntity<?>> IFromAlias select(final EntityResultQueryModel<T>... sourceQueryModels) {
	return new FromAlias((new Tokens()).from(sourceQueryModels));
    }

    public static <T extends AbstractEntity<?>> IFromAlias select(final AggregatedResultQueryModel... sourceQueryModels) {
	return new FromAlias((new Tokens()).from(sourceQueryModels));
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public static IStandAloneExprOperand expr() {
	return new StandAloneExpOperand(new Tokens());
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public static IOrderingItem orderBy() {
	return new OrderingItem(new Tokens());
    }

    public static <T extends AbstractEntity<?>> Builder<T, EntityResultQueryModel<T>> from(final EntityResultQueryModel<T> queryModel) {
	return QueryExecutionModel.<T>from(queryModel);
    }

    public static Builder<EntityAggregates, AggregatedResultQueryModel> from(final AggregatedResultQueryModel queryModel) {
	return QueryExecutionModel.from(queryModel);
    }

    public static <T extends AbstractEntity<?>> fetch<T> fetch(final Class<T> entityType) {
	return new fetch<T>(entityType);
    }

    public static <T extends AbstractEntity<?>> fetchAll<T> fetchAll(final Class<T> entityType) {
	return new fetchAll<T>(entityType);
    }
}