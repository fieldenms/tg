package ua.com.fielden.platform.swing.review.analysis;

import java.util.Date;

import org.joda.time.DateTime;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.dao.ILifecycleDao;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.equery.interfaces.IQueryModel;
import ua.com.fielden.platform.equery.lifecycle.LifecycleModel;
import ua.com.fielden.platform.reportquery.IDistributedProperty;
import ua.com.fielden.platform.swing.review.EntityQueryCriteriaExtender;

public class LifecycleReportQueryCriteriaExtender<T extends AbstractEntity, DAO extends IEntityDao<T>> extends EntityQueryCriteriaExtender<T, DAO, LifecycleModel<T>> {

    /**
     * Current lifecycle property.
     */
    private IDistributedProperty lifecycleProperty;
    /**
     * Left/right boundaries for lifecycle reporting.
     */
    private Date from, to;

    private IQueryModel<T> getLifecycleQuery() {
	if (getLifecycleProperty() == null || getFrom() == null || getTo() == null) {
	    return null;
	} else {
	    return getBaseQueryModel().model();
	}
    }

    @Override
    public LifecycleModel<T> runExtendedQuery(final int pageSize) {
	final ILifecycleDao<T> entityLifecycleDao = getBaseCriteria().getDao() instanceof ILifecycleDao ? (ILifecycleDao<T>) getBaseCriteria().getDao() : null;
	return entityLifecycleDao == null ? null
		: entityLifecycleDao.getLifecycleInformation(getLifecycleQuery(), getLifecycleProperty().getActualProperty(), new DateTime(getFrom()), new DateTime(getTo()));
    }

    public IDistributedProperty getLifecycleProperty() {
	return lifecycleProperty;
    }

    public void setLifecycleProperty(final IDistributedProperty lifecycleProperty) {
	this.lifecycleProperty = lifecycleProperty;
    }

    public Date getFrom() {
	return from;
    }

    public void setFrom(final Date from) {
	this.from = from;
    }

    public Date getTo() {
	return to;
    }

    public void setTo(final Date to) {
	this.to = to;
    }

}
