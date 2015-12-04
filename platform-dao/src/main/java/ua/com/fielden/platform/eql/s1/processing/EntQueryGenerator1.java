package ua.com.fielden.platform.eql.s1.processing;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import ua.com.fielden.platform.dao.DomainMetadataAnalyser;
import ua.com.fielden.platform.dao.QueryExecutionModel;
import ua.com.fielden.platform.entity.query.EntityRetrievalModel;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.entity.query.fluent.LogicalOperator;
import ua.com.fielden.platform.entity.query.fluent.QuerySection;
import ua.com.fielden.platform.entity.query.fluent.TokenCategory;
import ua.com.fielden.platform.entity.query.fluent.fetch;
import ua.com.fielden.platform.entity.query.model.ConditionModel;
import ua.com.fielden.platform.entity.query.model.OrderingModel;
import ua.com.fielden.platform.entity.query.model.QueryModel;
import ua.com.fielden.platform.eql.meta.QueryCategory;
import ua.com.fielden.platform.eql.s1.elements.CompoundCondition1;
import ua.com.fielden.platform.eql.s1.elements.Conditions1;
import ua.com.fielden.platform.eql.s1.elements.EntQuery1;
import ua.com.fielden.platform.eql.s1.elements.ISource1;
import ua.com.fielden.platform.eql.s1.elements.OrderBys1;
import ua.com.fielden.platform.eql.s1.elements.Sources1;
import ua.com.fielden.platform.eql.s1.elements.TypeBasedSource1;
import ua.com.fielden.platform.eql.s2.elements.ISource2;
import ua.com.fielden.platform.utils.Pair;

public class EntQueryGenerator1 {
    private final DomainMetadataAnalyser domainMetadataAnalyser;

    public EntQueryGenerator1(final DomainMetadataAnalyser domainMetadataAnalyser) {
        this.domainMetadataAnalyser = domainMetadataAnalyser;
    }

    public EntQuery1 generateEntQueryAsResultQuery(final QueryExecutionModel<?, ?> qem) {
        return generateEntQuery(qem.getQueryModel(), qem.getOrderModel(), null, qem.getFetchModel(), QueryCategory.RESULT_QUERY);
    }

    public EntQuery1 generateEntQueryAsSourceQuery(final QueryModel<?> qryModel, final Class resultType) {
        return generateEntQuery(qryModel, null, resultType, null, QueryCategory.SOURCE_QUERY);
    }

    public EntQuery1 generateEntQueryAsSubquery(final QueryModel<?> qryModel) {
        return generateEntQuery(qryModel, null, null, null, QueryCategory.SUB_QUERY);
    }

    public EntQueryBlocks1 parseTokensIntoComponents(final QueryModel<?> qryModel, //
            final OrderingModel orderModel, //
            final fetch fetchModel) {
        final QrySourcesBuilder1 from = new QrySourcesBuilder1(this);
        final ConditionsBuilder1 where = new ConditionsBuilder1(null, this);
        final QryYieldsBuilder1 select = new QryYieldsBuilder1(this);
        final QryGroupsBuilder1 groupBy = new QryGroupsBuilder1(this);

        ITokensBuilder1 active = null;

        for (final Pair<TokenCategory, Object> pair : qryModel.getTokens()) {
            if (!TokenCategory.QUERY_TOKEN.equals(pair.getKey())) {
                if (active != null) {
                    active.add(pair.getKey(), pair.getValue());
                }
            } else {
                switch ((QuerySection) pair.getValue()) {
                case WHERE:
                    active = where;
                    where.setChild(new ConditionBuilder1(where, this));
                    break;
                case FROM:
                    active = from;
                    break;
                case SELECT:
                    active = select;
                    select.setChild(new YieldBuilder1(select, this));
                    break;
                case GROUP_BY:
                    active = groupBy;
                    groupBy.setChild(new GroupBuilder1(groupBy, this));
                    break;
                default:
                    break;
                }
            }
        }

        final Sources1 sources = from.getModel();
        //	final Conditions1 conditions = where.getModel();
        //	final Conditions1 enhancedConditions = filterable ? enhanceConditions(conditions, filter, username, sources.getMain(), this) : conditions;

        return new EntQueryBlocks1(sources, //
        where.getModel(), //enhancedConditions, //
        select.getModel(), //
        groupBy.getModel(), //
        produceOrderBys(orderModel));
    }

    private Conditions1 enhanceConditions(final Conditions1 originalConditions, final IFilter filter, //
            final String username, final ISource1<? extends ISource2> mainSource, final EntQueryGenerator1 generator) {
        if (mainSource instanceof TypeBasedSource1 && filter != null) {
            final ConditionModel filteringCondition = filter.enhance(mainSource.sourceType(), mainSource.getAlias(), username);
            if (filteringCondition == null) {
                return originalConditions;
            }
            //logger.debug("\nApplied user-driven-filter to query main source type [" + mainSource.sourceType().getSimpleName() +"]");
            final List<CompoundCondition1> others = new ArrayList<>();
            others.add(new CompoundCondition1(LogicalOperator.AND, originalConditions));
            final Conditions1 filteringConditions = new StandAloneConditionBuilder1(generator, filteringCondition, false).getModel();
            return originalConditions.isEmpty() ? filteringConditions : new Conditions1(false, filteringConditions, others);
        } else {
            return originalConditions;
        }
    }

    private EntQuery1 generateEntQuery(final QueryModel<?> qryModel, //
            final OrderingModel orderModel, //
            final Class resultType, //
            final fetch fetchModel, //
            final QueryCategory category) {

        return new EntQuery1( //
        parseTokensIntoComponents(qryModel, orderModel, fetchModel), //
        resultType != null ? resultType : qryModel.getResultType(), //
        category, //
        fetchModel == null ? null : new EntityRetrievalModel(fetchModel, domainMetadataAnalyser), qryModel.isFilterable());
    }

    private OrderBys1 produceOrderBys(final OrderingModel orderModel) {
        final QryOrderingsBuilder1 orderBy = new QryOrderingsBuilder1(null, this);

        if (orderModel != null) {
            for (final Iterator<Pair<TokenCategory, Object>> iterator = orderModel.getTokens().iterator(); iterator.hasNext();) {
                final Pair<TokenCategory, Object> pair = iterator.next();
                if (TokenCategory.SORT_ORDER.equals(pair.getKey())) {
                    orderBy.add(pair.getKey(), pair.getValue());
                    if (iterator.hasNext()) {
                        orderBy.setChild(new OrderByBuilder1(orderBy, this));
                    }
                } else {
                    if (orderBy.getChild() == null) {
                        orderBy.setChild(new OrderByBuilder1(orderBy, this));
                    }
                    orderBy.add(pair.getKey(), pair.getValue());
                }
            }
        }
        return orderBy.getModel();
    }

    public DomainMetadataAnalyser getDomainMetadataAnalyser() {
        return domainMetadataAnalyser;
    }
}
