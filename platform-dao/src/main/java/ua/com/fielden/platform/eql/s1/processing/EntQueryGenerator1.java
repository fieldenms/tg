package ua.com.fielden.platform.eql.s1.processing;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import ua.com.fielden.platform.dao.DomainMetadataAnalyser;
import ua.com.fielden.platform.dao.QueryExecutionModel;
import ua.com.fielden.platform.entity.query.FetchModel;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.entity.query.fluent.LogicalOperator;
import ua.com.fielden.platform.entity.query.fluent.QueryTokens;
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
    private final IFilter filter;
    private final String username;

    public EntQueryGenerator1(final DomainMetadataAnalyser domainMetadataAnalyser, final IFilter filter, final String username) {
	this.domainMetadataAnalyser = domainMetadataAnalyser;
	this.filter = filter;
	this.username = username;
    }

    public EntQuery1 generateEntQueryAsResultQuery(final QueryExecutionModel<?, ?> qem) {
	return generateEntQuery(qem.getQueryModel(), qem.getOrderModel(), null, qem.getFetchModel(), qem.getParamValues(), QueryCategory.RESULT_QUERY, filter, username);
    }

    public EntQuery1 generateEntQueryAsSourceQuery(final QueryModel<?> qryModel, final Map<String, Object> paramValues, final Class resultType) {
	return generateEntQuery(qryModel, null, resultType, null, paramValues, QueryCategory.SOURCE_QUERY, filter, username);
    }

    public EntQuery1 generateEntQueryAsSubquery(final QueryModel<?> qryModel, final Map<String, Object> paramValues) {
	return generateEntQuery(qryModel, null, null, null, paramValues, QueryCategory.SUB_QUERY, filter, username);
    }

    public EntQueryBlocks1 parseTokensIntoComponents(final boolean filterable, final IFilter filter, //
	    final String username, final QueryModel<?> qryModel, //
	    final OrderingModel orderModel, //
	    final fetch fetchModel, //
	    final Map<String, Object> paramValues) {
	System.out.println("--------------------   filterable  = " + filterable);
	final QrySourcesBuilder1 from = new QrySourcesBuilder1(this, paramValues);
	final ConditionsBuilder1 where = new ConditionsBuilder1(null, this, paramValues);
	final QryYieldsBuilder1 select = new QryYieldsBuilder1(this, paramValues);
	final QryGroupsBuilder1 groupBy = new QryGroupsBuilder1(this, paramValues);

	ITokensBuilder1 active = null;

	for (final Pair<TokenCategory, Object> pair : qryModel.getTokens()) {
	    if (!TokenCategory.QUERY_TOKEN.equals(pair.getKey())) {
		if (active != null) {
		    active.add(pair.getKey(), pair.getValue());
		}
	    } else {
		switch ((QueryTokens) pair.getValue()) {
		case WHERE:
		    active = where;
		    where.setChild(new ConditionBuilder1(where, this, paramValues));
		    break;
		case FROM:
		    active = from;
		    break;
		case YIELD:
		    active = select;
		    select.setChild(new YieldBuilder1(select, this, paramValues));
		    break;
		case GROUP_BY:
		    active = groupBy;
		    groupBy.setChild(new GroupBuilder1(groupBy, this, paramValues));
		    break;
		default:
		    break;
		}
	    }
	}

	final Sources1 sources = from.getModel();
	final Conditions1 conditions = where.getModel();
	final Conditions1 enhancedConditions = filterable ? enhanceConditions(conditions, filter, username, sources.getMain(), this, paramValues) : conditions;

	return new EntQueryBlocks1(sources, //
		enhancedConditions, //
	select.getModel(), //
	groupBy.getModel(), //
	produceOrderBys(orderModel, paramValues));
    }

    private Conditions1 enhanceConditions(final Conditions1 originalConditions, final IFilter filter, //
	    final String username, final ISource1<? extends ISource2> mainSource, final EntQueryGenerator1 generator, final Map<String, Object> paramValues) {
	if (mainSource instanceof TypeBasedSource1 && filter != null) {
	final ConditionModel filteringCondition = filter.enhance(mainSource.sourceType(), mainSource.getAlias(), username);
	if (filteringCondition == null) {
	    return originalConditions;
	}
	//logger.debug("\nApplied user-driven-filter to query main source type [" + mainSource.sourceType().getSimpleName() +"]");
	final List<CompoundCondition1> others = new ArrayList<>();
	others.add(new CompoundCondition1(LogicalOperator.AND, originalConditions));
	final Conditions1 filteringConditions = new StandAloneConditionBuilder1(generator, paramValues, filteringCondition, false).getModel();
	return originalConditions.ignore() ? filteringConditions : new Conditions1(false, filteringConditions, others);
	} else {
	    return originalConditions;
	}
    }

    private EntQuery1 generateEntQuery(
	    final QueryModel<?> qryModel, //
	    final OrderingModel orderModel, //
	    final Class resultType, //
	    final fetch fetchModel, //
	    final Map<String, Object> paramValues, //
	    final QueryCategory category, //
	    final IFilter filter, //
	    final String username) {

	return new EntQuery1( //
		parseTokensIntoComponents(qryModel.isFilterable(), filter, username, qryModel, orderModel, fetchModel, paramValues), //
		resultType != null ? resultType : qryModel.getResultType(), //
		category, //
		domainMetadataAnalyser, //
		fetchModel == null ? null : new FetchModel(fetchModel, domainMetadataAnalyser), //
		paramValues);
    }

    private OrderBys1 produceOrderBys(final OrderingModel orderModel, final Map<String, Object> paramValues) {
	final QryOrderingsBuilder1 orderBy = new QryOrderingsBuilder1(null, this, paramValues);

	if (orderModel != null) {
	    for (final Iterator<Pair<TokenCategory, Object>> iterator = orderModel.getTokens().iterator(); iterator.hasNext();) {
		final Pair<TokenCategory, Object> pair = iterator.next();
		if (TokenCategory.SORT_ORDER.equals(pair.getKey())) {
		    orderBy.add(pair.getKey(), pair.getValue());
		    if (iterator.hasNext()) {
			orderBy.setChild(new OrderByBuilder1(orderBy, this, paramValues));
		    }
		} else {
		    if (orderBy.getChild() == null) {
			orderBy.setChild(new OrderByBuilder1(orderBy, this, paramValues));
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
