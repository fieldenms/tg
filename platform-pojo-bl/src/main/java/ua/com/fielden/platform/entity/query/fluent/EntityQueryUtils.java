package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.dao.QueryExecutionModel;
import ua.com.fielden.platform.dao.QueryExecutionModel.Builder;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.EntityAggregates;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IFromAlias;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IFromNone;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IOrderingItem;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IStandAloneConditionOperand;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IStandAloneExprOperand;
import ua.com.fielden.platform.entity.query.fluent.fetch.FetchCategory;
import ua.com.fielden.platform.entity.query.model.AggregatedResultQueryModel;
import ua.com.fielden.platform.entity.query.model.ConditionModel;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;

public class EntityQueryUtils {
	public static <T extends AbstractEntity<?>> IFromAlias<T> select(final Class<T> entityType) {
		return new FromAlias<T>(new Tokens().from(entityType));
	}

	@SafeVarargs
    public static <T extends AbstractEntity<?>> IFromAlias<T> select(final EntityResultQueryModel<T>... sourceQueryModels) {
		return new FromAlias<T>(new Tokens().from(sourceQueryModels));
	}

	public static IFromAlias<EntityAggregates> select(final AggregatedResultQueryModel... sourceQueryModels) {
		return new FromAlias<EntityAggregates>(new Tokens().from(sourceQueryModels));
	}

    public static <T extends AbstractEntity<?>> IFromNone<T> select() {
        return new FromNone<T>(new Tokens().from());
    }

	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public static IStandAloneExprOperand expr() {
		return new StandAloneExpOperand(new Tokens());
	}

	public static <ET extends AbstractEntity<?>> IStandAloneConditionOperand<ET> cond() {
		return new StandAloneConditionOperand<>(new Tokens());
	}

	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public static IOrderingItem orderBy() {
		return new OrderingItem(new Tokens());
	}

	public static <T extends AbstractEntity<?>> Builder<T, EntityResultQueryModel<T>> from(
			final EntityResultQueryModel<T> queryModel) {
		return QueryExecutionModel.<T> from(queryModel);
	}

	public static Builder<EntityAggregates, AggregatedResultQueryModel> from(
			final AggregatedResultQueryModel queryModel) {
		return QueryExecutionModel.from(queryModel);
	}

	public static <T extends AbstractEntity<?>> fetch<T> fetchIdOnly(final Class<T> entityType) {
		return new fetch<>(entityType, FetchCategory.ID_ONLY);
	}

	public static <T extends AbstractEntity<?>> fetch<T> fetch(final Class<T> entityType) {
		return new fetch<>(entityType, FetchCategory.DEFAULT);
	}

	public static <T extends AbstractEntity<?>> fetch<T> fetchAndInstrument(final Class<T> entityType) {
		return new fetch<>(entityType, FetchCategory.DEFAULT, true);
	}

	public static <T extends AbstractEntity<?>> fetch<T> fetchAll(final Class<T> entityType) {
		return new fetch<>(entityType, FetchCategory.ALL);
	}

	public static <T extends AbstractEntity<?>> fetch<T> fetchAllAndInstrument(final Class<T> entityType) {
		return new fetch<>(entityType, FetchCategory.ALL, true);
	}

	public static <T extends AbstractEntity<?>> fetch<T> fetchAllInclCalc(final Class<T> entityType) {
		return new fetch<>(entityType, FetchCategory.ALL_INCL_CALC);
	}

	public static <T extends AbstractEntity<?>> fetch<T> fetchAllInclCalcAndInstrument(final Class<T> entityType) {
		return new fetch<>(entityType, FetchCategory.ALL_INCL_CALC, true);
	}

	public static <T extends AbstractEntity<?>> fetch<T> fetchOnly(final Class<T> entityType) {
		return new fetch<>(entityType, FetchCategory.ID_AND_VERSION);
	}

	public static <T extends AbstractEntity<?>> fetch<T> fetchNone(final Class<T> entityType) {
		return new fetch<>(entityType, FetchCategory.NONE);
	}

	public static <T extends AbstractEntity<?>> fetch<T> fetchOnlyAndInstrument(final Class<T> entityType) {
		return new fetch<>(entityType, FetchCategory.ID_AND_VERSION, true);
	}

	public static <T extends AbstractEntity<?>> fetch<T> fetchKeyAndDescOnly(final Class<T> entityType) {
		return new fetch<>(entityType, FetchCategory.KEY_AND_DESC);
	}

	public static <T extends AbstractEntity<?>> fetch<T> fetchKeyAndDescOnlyAndInstrument(final Class<T> entityType) {
		return new fetch<>(entityType, FetchCategory.KEY_AND_DESC, true);
	}

	public static fetch<EntityAggregates> fetchAggregates() {
		return new fetch<>(EntityAggregates.class, FetchCategory.NONE);
	}
	
	/**
	 * Generates empty condition model for EQL models. This condition is more of a convenience and is simply ignored as part of the condition processing.
	 * 
	 * @return
	 */
	public static ConditionModel emptyCondition() {
	    return cond().iVal(null).eq().iVal(null).model();
	}
}