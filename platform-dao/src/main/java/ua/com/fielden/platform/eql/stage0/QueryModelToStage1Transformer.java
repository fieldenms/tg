package ua.com.fielden.platform.eql.stage0;

import static ua.com.fielden.platform.entity.query.fluent.enums.TokenCategory.ORDER_TOKENS;
import static ua.com.fielden.platform.entity.query.fluent.enums.TokenCategory.QUERY_TOKEN;
import static ua.com.fielden.platform.entity.query.fluent.enums.TokenCategory.SORT_ORDER;
import static ua.com.fielden.platform.eql.stage1.conditions.Conditions1.emptyConditions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.entity.query.IRetrievalModel;
import ua.com.fielden.platform.entity.query.fluent.enums.QueryTokens;
import ua.com.fielden.platform.entity.query.fluent.enums.TokenCategory;
import ua.com.fielden.platform.entity.query.model.ConditionModel;
import ua.com.fielden.platform.entity.query.model.OrderingModel;
import ua.com.fielden.platform.entity.query.model.QueryModel;
import ua.com.fielden.platform.eql.retrieval.QueryNowValue;
import ua.com.fielden.platform.eql.stage1.QueryComponents1;
import ua.com.fielden.platform.eql.stage1.conditions.Conditions1;
import ua.com.fielden.platform.eql.stage1.etc.OrderBys1;
import ua.com.fielden.platform.eql.stage1.operands.queries.ResultQuery1;
import ua.com.fielden.platform.eql.stage1.operands.queries.SourceQuery1;
import ua.com.fielden.platform.eql.stage1.operands.queries.SubQuery1;
import ua.com.fielden.platform.eql.stage1.operands.queries.SubQueryForExists1;
import ua.com.fielden.platform.eql.stage1.sources.ISource1;
import ua.com.fielden.platform.eql.stage1.sources.IJoinNode1;
import ua.com.fielden.platform.eql.stage2.sources.IJoinNode2;
import ua.com.fielden.platform.utils.Pair;

/**
 * Transforms EQL models in form of fluent API tokens to the stage 1 representation. 
 * 
 */
public class QueryModelToStage1Transformer {
    public final QueryNowValue nowValue;
    public final IFilter filter;
    public final String username;
    private final Map<String, Object> paramValues = new HashMap<>();
    
    public QueryModelToStage1Transformer(final IFilter filter, final String username, final QueryNowValue nowValue, final Map<String, Object> paramValues) {
        this.filter = filter;
        this.username = username;
        this.nowValue = nowValue;
        this.paramValues.putAll(paramValues);
    }
    
    private int sourceId = 0;
    
    public int nextSourceId() {
        sourceId = sourceId + 1;
        return sourceId;
    }
    
    public <T extends AbstractEntity<?>, Q extends QueryModel<T>> ResultQuery1 generateAsResultQuery(final QueryModel<T> qm, final OrderingModel orderModel, final IRetrievalModel<T> fetchModel) {
        return new ResultQuery1(parseTokensIntoComponents(qm, orderModel), qm.getResultType(), fetchModel);
    }

    public <T extends AbstractEntity<?>> SourceQuery1 generateAsCorrelatedSourceQuery(final QueryModel<T> qryModel) {
        return generateAsSourceQuery(qryModel, true);
    }
        
    public <T extends AbstractEntity<?>> SourceQuery1 generateAsUncorrelatedSourceQuery(final QueryModel<T> qryModel) {
        return generateAsSourceQuery(qryModel, false);
    }
    
    private <T extends AbstractEntity<?>> SourceQuery1 generateAsSourceQuery(final QueryModel<T> qryModel, final boolean isCorrelated) {
        return new SourceQuery1(parseTokensIntoComponents(qryModel, null), qryModel.getResultType(), isCorrelated);
    }

    public SubQuery1 generateAsSubquery(final QueryModel<?> qryModel) {
        return new SubQuery1(parseTokensIntoComponents(qryModel, null), qryModel.getResultType());
    }

    public SubQueryForExists1 generateAsTypelessSubquery(final QueryModel<?> qryModel) {
        return new SubQueryForExists1(parseTokensIntoComponents(qryModel, null));
    }

    private QueryComponents1 parseTokensIntoComponents(final QueryModel<?> qryModel, final OrderingModel orderModel) {
        final QrySourcesBuilder from = new QrySourcesBuilder(this);
        final ConditionsBuilder where = new ConditionsBuilder(null, this);
        final QryYieldsBuilder select = new QryYieldsBuilder(this);
        final QryGroupsBuilder groupBy = new QryGroupsBuilder(this);

        ITokensBuilder active = null;

        for (final Pair<TokenCategory, Object> pair : qryModel.getTokens()) {
            if (QUERY_TOKEN != pair.getKey()) {
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

        final IJoinNode1<? extends IJoinNode2<?>> fromModel = from.getModel();
        final Conditions1 udfModel = fromModel == null ? emptyConditions : generateUserDataFilteringCondition(qryModel.isFilterable(), filter, username, fromModel.mainSource());

        return new QueryComponents1(fromModel, where.getModel(), udfModel, select.getModel(), groupBy.getModel(), produceOrderBys(orderModel), qryModel.isYieldAll());
    }
    
    private Conditions1 generateUserDataFilteringCondition(final boolean filterable, final IFilter filter, final String username, final ISource1<?> mainSource) {
        if (filterable && filter != null) {
            // now there is no need to rely on the main source alias while processing UDF (that's why null can be used until alias parameter is removed from the enhance() method.
            final ConditionModel filteringCondition = filter.enhance(mainSource.sourceType(), null, username); 
            if (filteringCondition != null) {
                // LOGGER.debug("\nApplied user-driven-filter to query main source type [" + mainSource.sourceType().getSimpleName() + "]");
                return new StandAloneConditionBuilder(this, filteringCondition, false).getModel();
            }
        }

        return emptyConditions;
    }

    private List<Pair<TokenCategory, Object>> linearizeTokens(final List<Pair<TokenCategory, Object>> tokens) {
        final List<Pair<TokenCategory, Object>> result = new ArrayList<>();
        for (final Pair<TokenCategory, Object> pair : tokens) {
            if (ORDER_TOKENS == pair.getKey()) {
                result.addAll(linearizeTokens(((OrderingModel) pair.getValue()).getTokens()));
            } else {
                result.add(pair);
            }
        }

        return result;
    }

    private OrderBys1 produceOrderBys(final OrderingModel orderModel) {
        final QryOrderingsBuilder orderBy = new QryOrderingsBuilder(null, this);

        if (orderModel != null) {
            final List<Pair<TokenCategory, Object>> linearizedTokens = linearizeTokens(orderModel.getTokens());
            for (final Iterator<Pair<TokenCategory, Object>> iterator = linearizedTokens.iterator(); iterator.hasNext();) {
                final Pair<TokenCategory, Object> pair = iterator.next();
                if (SORT_ORDER == pair.getKey()) {
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
    
    public Object getParamValue(final String paramName) {
        return paramValues.get(paramName);
    }
}