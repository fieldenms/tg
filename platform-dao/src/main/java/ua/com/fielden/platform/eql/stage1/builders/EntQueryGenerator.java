package ua.com.fielden.platform.eql.stage1.builders;

import static ua.com.fielden.platform.entity.query.fluent.enums.TokenCategory.QUERY_TOKEN;
import static ua.com.fielden.platform.entity.query.fluent.enums.TokenCategory.SORT_ORDER;
import static ua.com.fielden.platform.eql.meta.QueryCategory.RESULT_QUERY;
import static ua.com.fielden.platform.eql.meta.QueryCategory.SOURCE_QUERY;
import static ua.com.fielden.platform.eql.meta.QueryCategory.SUB_QUERY;

import java.util.Iterator;
import java.util.Map;
import java.util.Optional;

import ua.com.fielden.platform.dao.QueryExecutionModel;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.IRetrievalModel;
import ua.com.fielden.platform.entity.query.fluent.enums.QueryTokens;
import ua.com.fielden.platform.entity.query.fluent.enums.TokenCategory;
import ua.com.fielden.platform.entity.query.model.OrderingModel;
import ua.com.fielden.platform.entity.query.model.QueryModel;
import ua.com.fielden.platform.eql.meta.EntityInfo;
import ua.com.fielden.platform.eql.meta.QueryCategory;
import ua.com.fielden.platform.eql.stage1.elements.OrderBys1;
import ua.com.fielden.platform.eql.stage1.elements.operands.EntQuery1;
import ua.com.fielden.platform.utils.Pair;

public class EntQueryGenerator {
    
    private int contextId = 0;
    public final Map<String, EntityInfo> dm;
    
    public EntQueryGenerator(final Map<String, EntityInfo> dm) {
        this.dm = dm;
    }
    
    
    public int nextCondtextId() {
        contextId = contextId + 1;
        return contextId;
    }

    public <T extends AbstractEntity<?>, Q extends QueryModel<T>> EntQuery1 generateEntQueryAsResultQuery(final QueryExecutionModel<T, Q> qem, final IRetrievalModel<T> fetchModel) {
        return generateEntQuery(qem.getQueryModel(), qem.getOrderModel(), Optional.empty(), fetchModel, RESULT_QUERY);
    }

    public EntQuery1 generateEntQueryAsSourceQuery(final QueryModel<?> qryModel, final Optional<Class<?>> resultType) {
        return generateEntQuery(qryModel, null, resultType, null, SOURCE_QUERY);
    }

    public EntQuery1 generateEntQueryAsSubquery(final QueryModel<?> qryModel) {
        return generateEntQuery(qryModel, null, Optional.empty(), null, SUB_QUERY);
    }

    public EntQueryBlocks parseTokensIntoComponents(final QueryModel<?> qryModel, final OrderingModel orderModel) {
        final QrySourcesBuilder from = new QrySourcesBuilder(this);
        final ConditionsBuilder where = new ConditionsBuilder(null, this);
        final QryYieldsBuilder select = new QryYieldsBuilder(this);
        final QryGroupsBuilder groupBy = new QryGroupsBuilder(this);

        ITokensBuilder active = null;

        for (final Pair<TokenCategory, Object> pair : qryModel.getTokens()) {
            if (!QUERY_TOKEN.equals(pair.getKey())) {
                if (active != null) {
                    active.add(pair.getKey(), pair.getValue());
                }
            } else {
                switch ((QueryTokens) pair.getValue()) {
                case WHERE:
                    active = where;
                    where.setChild(new ConditionBuilder(where, this));
                    break;
                case FROM:
                    active = from;
                    break;
                case YIELD:
                    active = select;
                    select.setChild(new YieldBuilder(select, this));
                    break;
                case GROUP_BY:
                    active = groupBy;
                    groupBy.setChild(new GroupBuilder(groupBy, this));
                    break;
                default:
                    break;
                }
            }
        }

        return new EntQueryBlocks(from.getModel(), where.getModel(), select.getModel(), groupBy.getModel(), produceOrderBys(orderModel));
    }

    private EntQuery1 generateEntQuery(final QueryModel<?> qryModel, 
            final OrderingModel orderModel, 
            final Optional<Class<?>> resultType, 
            final IRetrievalModel fetchModel,  
            final QueryCategory category) {

        return new EntQuery1( 
        parseTokensIntoComponents(qryModel, orderModel), 
        resultType.orElse(qryModel.getResultType()).getName(), 
        category, 
        qryModel.isFilterable(),
        fetchModel,
        nextCondtextId());
    }

    private OrderBys1 produceOrderBys(final OrderingModel orderModel) {
        final QryOrderingsBuilder orderBy = new QryOrderingsBuilder(null, this);

        if (orderModel != null) {
            for (final Iterator<Pair<TokenCategory, Object>> iterator = orderModel.getTokens().iterator(); iterator.hasNext();) {
                final Pair<TokenCategory, Object> pair = iterator.next();
                if (SORT_ORDER.equals(pair.getKey())) {
                    orderBy.add(pair.getKey(), pair.getValue());
                    if (iterator.hasNext()) {
                        orderBy.setChild(new OrderByBuilder(orderBy, this));
                    }
                } else {
                    if (orderBy.getChild() == null) {
                        orderBy.setChild(new OrderByBuilder(orderBy, this));
                    }
                    orderBy.add(pair.getKey(), pair.getValue());
                }
            }
        }
        return orderBy.getModel();
    }
}