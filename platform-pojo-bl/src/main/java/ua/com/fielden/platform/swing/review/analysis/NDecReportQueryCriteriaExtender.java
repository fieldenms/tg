package ua.com.fielden.platform.swing.review.analysis;

import java.util.List;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.equery.EntityAggregates;
import ua.com.fielden.platform.equery.interfaces.IQueryOrderedModel;

public class NDecReportQueryCriteriaExtender<T extends AbstractEntity, DAO extends IEntityDao<T>> extends GroupReportQueryCriteriaExtender<T, DAO, List<EntityAggregates>> {

    @Override
    public List<EntityAggregates> runExtendedQuery(final int pageSize) {
	final IQueryOrderedModel<EntityAggregates> queryModel = createExtendedQuery().model(EntityAggregates.class);
	return getBaseCriteria().getEntityAggregatesDao().listAggregates(queryModel, createExtendedFetchModel());
    }

    @Override
    public List<EntityAggregates> exportExtendedQueryData() {
	throw new UnsupportedOperationException("The exporting is not supported yet for analysis reports.");
    }

}
