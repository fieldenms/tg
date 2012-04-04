package ua.com.fielden.platform.swing.review;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fetch;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ICompleted;

public abstract class EntityQueryCriteriaExtender<T extends AbstractEntity<?>, DAO extends IEntityDao<T>, R> {

    private EntityQueryCriteria<T, DAO> baseCriteria;

    public final ICompleted getBaseQueryModel() {
	return baseCriteria.createQuery();
    }

    public final fetch<T> getBaseFetchModel() {
	return baseCriteria.createFetchModel();
    }

    public final EntityQueryCriteria<T, DAO> getBaseCriteria() {
	return baseCriteria;
    }

    public final void setBaseCriteria(final EntityQueryCriteria<T, DAO> baseCriteria) {
	this.baseCriteria = baseCriteria;
    }

    public abstract R runExtendedQuery(final int pageSize);

    public abstract R exportExtendedQueryData();

}
