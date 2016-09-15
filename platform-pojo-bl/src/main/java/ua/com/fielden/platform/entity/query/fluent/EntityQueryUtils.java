package ua.com.fielden.platform.entity.query.fluent;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import ua.com.fielden.platform.dao.QueryExecutionModel;
import ua.com.fielden.platform.dao.QueryExecutionModel.Builder;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.EntityAggregates;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ICompoundCondition0;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IFromAlias;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IOrderingItem;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IStandAloneConditionOperand;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IStandAloneExprOperand;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IWhere0;
import ua.com.fielden.platform.entity.query.fluent.fetch.FetchCategory;
import ua.com.fielden.platform.entity.query.model.AggregatedResultQueryModel;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.reflection.Finder;

public class EntityQueryUtils {
    public static <T extends AbstractEntity<?>> IFromAlias<T> select(final Class<T> entityType) {
        return new FromAlias<T>((new Tokens()).from(entityType));
    }

    public static <T extends AbstractEntity<?>> IFromAlias<T> select(final EntityResultQueryModel<T>... sourceQueryModels) {
        return new FromAlias<T>((new Tokens()).from(sourceQueryModels));
    }

    public static <T extends AbstractEntity<?>> IFromAlias<T> select(final AggregatedResultQueryModel... sourceQueryModels) {
        return new FromAlias<T>((new Tokens()).from(sourceQueryModels));
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public static IStandAloneExprOperand expr() {
        return new StandAloneExpOperand(new Tokens());
    }

    public static <ET extends AbstractEntity<?>> IStandAloneConditionOperand<ET> cond() {
        return new StandAloneConditionOperand<ET>(new Tokens());
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public static IOrderingItem orderBy() {
        return new OrderingItem(new Tokens());
    }

    public static <T extends AbstractEntity<?>> Builder<T, EntityResultQueryModel<T>> from(final EntityResultQueryModel<T> queryModel) {
        return QueryExecutionModel.<T> from(queryModel);
    }

    public static Builder<EntityAggregates, AggregatedResultQueryModel> from(final AggregatedResultQueryModel queryModel) {
        return QueryExecutionModel.from(queryModel);
    }

    public static <T extends AbstractEntity<?>> fetch<T> fetchIdOnly(final Class<T> entityType) {
        return new fetch<T>(entityType, FetchCategory.ID);
    }

    public static <T extends AbstractEntity<?>> fetch<T> fetch(final Class<T> entityType) {
        return new fetch<T>(entityType, FetchCategory.DEFAULT);
    }

    public static <T extends AbstractEntity<?>> fetch<T> fetchAndInstrument(final Class<T> entityType) {
        return new fetch<T>(entityType, FetchCategory.DEFAULT, true);
    }

    public static <T extends AbstractEntity<?>> fetch<T> fetchAll(final Class<T> entityType) {
        return new fetch<T>(entityType, FetchCategory.ALL);
    }

    public static <T extends AbstractEntity<?>> fetch<T> fetchAllAndInstrument(final Class<T> entityType) {
        return new fetch<T>(entityType, FetchCategory.ALL, true);
    }

    public static <T extends AbstractEntity<?>> fetch<T> fetchAllInclCalc(final Class<T> entityType) {
        return new fetch<T>(entityType, FetchCategory.ALL_INCL_CALC);
    }

    public static <T extends AbstractEntity<?>> fetch<T> fetchAllInclCalcAndInstrument(final Class<T> entityType) {
        return new fetch<T>(entityType, FetchCategory.ALL_INCL_CALC, true);
    }

    public static <T extends AbstractEntity<?>> fetch<T> fetchOnly(final Class<T> entityType) {
        return new fetch<T>(entityType, FetchCategory.ID_AND_VERSTION);
    }

    public static <T extends AbstractEntity<?>> fetch<T> fetchOnlyAndInstrument(final Class<T> entityType) {
        return new fetch<T>(entityType, FetchCategory.ID_AND_VERSTION, true);
    }

    public static <T extends AbstractEntity<?>> fetch<T> fetchKeyAndDescOnly(final Class<T> entityType) {
        return new fetch<T>(entityType, FetchCategory.KEY_AND_DESC);
    }

    public static <T extends AbstractEntity<?>> fetch<T> fetchKeyAndDescOnlyAndInstrument(final Class<T> entityType) {
        return new fetch<T>(entityType, FetchCategory.KEY_AND_DESC, true);
    }

    public static fetch<EntityAggregates> fetchAggregates() {
        return new fetch<EntityAggregates>(EntityAggregates.class, FetchCategory.ID_AND_VERSTION);
    }
    
    /**
     * Creates a query for entities by their keys (simple or composite). If <code>entitiesWithKeys</code> are empty -- returns empty optional. 
     * 
     * @param entityType -- the entity type
     * @param entitiesWithKeys -- the entities with <b>all</b> key values correctly fetched / assigned
     * @return
     */
    public static <T extends AbstractEntity<?>> Optional<EntityResultQueryModel<T>> queryByKeys(final Class<T> entityType, final Collection<T> entitiesWithKeys) {
        IWhere0<T> partQ = select(entityType).where();
        final List<Field> keyMembers = Finder.getKeyMembers(entityType);
        for (final Iterator<T> iter = entitiesWithKeys.iterator(); iter.hasNext();) {
            final T entityWithKey = iter.next();
            
            ICompoundCondition0<T> and = null;
            for (final Iterator<Field> keyMemberIter = keyMembers.iterator(); keyMemberIter.hasNext();) {
                final String keyName = keyMemberIter.next().getName();
                
                and = partQ.prop(keyName).eq().val(entityWithKey.get(keyName));
                
                if (keyMemberIter.hasNext()) {
                    partQ = and.and();
                }
            }
            
            final ICompoundCondition0<T> or = and;

            if (iter.hasNext()) {
                partQ = or.or();
            } else {
                return Optional.of(or.model());
            }
        }

        return Optional.empty();
    }

}