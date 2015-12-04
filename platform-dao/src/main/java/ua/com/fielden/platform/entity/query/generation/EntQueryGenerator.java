package ua.com.fielden.platform.entity.query.generation;

import java.util.Iterator;
import java.util.Map;

import ua.com.fielden.platform.dao.DomainMetadataAnalyser;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.DbVersion;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.entity.query.IRetrievalModel;
import ua.com.fielden.platform.entity.query.fluent.QuerySection;
import ua.com.fielden.platform.entity.query.fluent.TokenCategory;
import ua.com.fielden.platform.entity.query.generation.elements.EntQuery;
import ua.com.fielden.platform.entity.query.generation.elements.OrderBys;
import ua.com.fielden.platform.entity.query.generation.elements.QueryCategory;
import ua.com.fielden.platform.entity.query.model.OrderingModel;
import ua.com.fielden.platform.entity.query.model.QueryModel;
import ua.com.fielden.platform.utils.IUniversalConstants;
import ua.com.fielden.platform.utils.Pair;

public class EntQueryGenerator {
    public static final String NOW = "UC#NOW"; 
    
    private final DbVersion dbVersion;
    private final DomainMetadataAnalyser domainMetadataAnalyser;
    private final IUniversalConstants universalConstants;
    private final IFilter filter;
    private final String username;

    public EntQueryGenerator(final DomainMetadataAnalyser domainMetadataAnalyser, final IFilter filter, final String username, final IUniversalConstants universalConstants) {
        this.dbVersion = domainMetadataAnalyser.getDbVersion();
        this.domainMetadataAnalyser = domainMetadataAnalyser;
        this.filter = filter;
        this.username = username;
        this.universalConstants = universalConstants;
    }

    public <T extends AbstractEntity<?>, Q extends QueryModel<T>> EntQuery generateEntQueryAsResultQuery(final Q query, final OrderingModel orderModel, final Class<T> resultType, final IRetrievalModel<T> fetchModel, final Map<String, Object> paramValues) {
        if (universalConstants.now() != null) {
        	paramValues.put(NOW, universalConstants.now().toDate());	
        }
    	
        return generateEntQuery(query, orderModel, resultType, fetchModel, paramValues, QueryCategory.RESULT_QUERY, filter, username);
    }

    public EntQuery generateEntQueryAsSourceQuery(final QueryModel<?> qryModel, final Map<String, Object> paramValues, final Class resultType) {
        return generateEntQuery(qryModel, null, resultType, null, paramValues, QueryCategory.SOURCE_QUERY, filter, username);
    }

    public EntQuery generateEntQueryAsSubquery(final QueryModel<?> qryModel, final Map<String, Object> paramValues) {
        return generateEntQuery(qryModel, null, qryModel.getResultType(), null, paramValues, QueryCategory.SUB_QUERY, filter, username);
    }

    public EntQueryBlocks parseTokensIntoComponents(final QueryModel<?> qryModel, //
            final OrderingModel orderModel, //
            final Map<String, Object> paramValues) {
        final QrySourcesBuilder from = new QrySourcesBuilder(this, paramValues);
        final ConditionsBuilder where = new ConditionsBuilder(null, this, paramValues);
        final QryYieldsBuilder select = new QryYieldsBuilder(this, paramValues);
        final QryGroupsBuilder groupBy = new QryGroupsBuilder(this, paramValues);

        ITokensBuilder active = null;

        for (final Pair<TokenCategory, Object> pair : qryModel.getTokens()) {
            if (!TokenCategory.QUERY_TOKEN.equals(pair.getKey())) {
                if (active != null) {
                    active.add(pair.getKey(), pair.getValue());
                }
            } else {
                switch ((QuerySection) pair.getValue()) {
                case WHERE:
                    active = where;
                    where.setChild(new ConditionBuilder(where, this, paramValues));
                    break;
                case FROM:
                    active = from;
                    break;
                case SELECT:
                    active = select;
                    select.setChild(new YieldBuilder(select, this, paramValues));
                    break;
                case GROUP_BY:
                    active = groupBy;
                    groupBy.setChild(new GroupBuilder(groupBy, this, paramValues));
                    break;
                default:
                    break;
                }
            }
        }

        return new EntQueryBlocks(from.getModel(), //
        where.getModel(), //
        select.getModel(), //
        groupBy.getModel(), //
        produceOrderBys(orderModel, paramValues));
    }

    private EntQuery generateEntQuery(final QueryModel<?> qryModel, //
            final OrderingModel orderModel, //
            final Class resultType, //
            final IRetrievalModel fetchModel, //
            final Map<String, Object> paramValues, //
            final QueryCategory category, //
            final IFilter filter, //
            final String username) {
        return new EntQuery( //
        qryModel.isFilterable(), //
        parseTokensIntoComponents(qryModel, orderModel, /*fetchModel,*/ paramValues), //
        resultType, //
        category, //
        domainMetadataAnalyser, //
        filter, //
        username, //
        this, //
        fetchModel,
        paramValues);
    }

    private OrderBys produceOrderBys(final OrderingModel orderModel, final Map<String, Object> paramValues) {
        final QryOrderingsBuilder orderBy = new QryOrderingsBuilder(null, this, paramValues);

        if (orderModel != null) {
            for (final Iterator<Pair<TokenCategory, Object>> iterator = orderModel.getTokens().iterator(); iterator.hasNext();) {
                final Pair<TokenCategory, Object> pair = iterator.next();
                if (TokenCategory.SORT_ORDER.equals(pair.getKey())) {
                    orderBy.add(pair.getKey(), pair.getValue());
                    if (iterator.hasNext()) {
                        orderBy.setChild(new OrderByBuilder(orderBy, this, paramValues));
                    }
                } else {
                    if (orderBy.getChild() == null) {
                        orderBy.setChild(new OrderByBuilder(orderBy, this, paramValues));
                    }
                    orderBy.add(pair.getKey(), pair.getValue());
                }
            }
        }
        return orderBy.getModel();
    }

    public DbVersion getDbVersion() {
        return dbVersion;
    }

    public DomainMetadataAnalyser getDomainMetadataAnalyser() {
        return domainMetadataAnalyser;
    }
}
