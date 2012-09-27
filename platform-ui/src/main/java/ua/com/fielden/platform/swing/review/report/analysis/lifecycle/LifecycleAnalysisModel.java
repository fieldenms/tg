package ua.com.fielden.platform.swing.review.report.analysis.lifecycle;

import java.io.IOException;
import java.util.List;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.centre.analyses.ILifecycleDomainTreeManager;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.reportquery.DistributionProperty;
import ua.com.fielden.platform.reportquery.IDistributedProperty;
import ua.com.fielden.platform.swing.review.development.EntityQueryCriteria;
import ua.com.fielden.platform.swing.review.report.analysis.view.AbstractAnalysisReviewModel;
import ua.com.fielden.platform.types.ICategory;
import ua.com.fielden.platform.types.Ordering;

public class LifecycleAnalysisModel<T extends AbstractEntity<?>> extends AbstractAnalysisReviewModel<T, ICentreDomainTreeManagerAndEnhancer, ILifecycleDomainTreeManager, Void>{

    public LifecycleAnalysisModel(final EntityQueryCriteria<ICentreDomainTreeManagerAndEnhancer, T, IEntityDao<T>> criteria, final ILifecycleDomainTreeManager adtme) {
	super(criteria, adtme);
	// TODO Auto-generated constructor stub
    }

    @Override
    protected Void executeAnalysisQuery() {
	//	final ILifecycleDao<T> entityLifecycleDao = getBaseCriteria().getDao() instanceof ILifecycleDao ? (ILifecycleDao<T>) getBaseCriteria().getDao() : null;
	//	return entityLifecycleDao == null ? null
	//		: entityLifecycleDao.getLifecycleInformation(getLifecycleQuery(), getLifecycleProperty().getActualProperty(), new DateTime(getFrom()), new DateTime(getTo()));
	//
	return null;
    }

    @Override
    protected void exportData(final String fileName) throws IOException {
	throw new UnsupportedOperationException("The data exporting for lifecycle analysis is not supported!");
    }

    @Override
    protected Result canLoadData() {
	return getCriteria().isValid();
    }

    @Override
    protected String[] getExportFileExtensions() {
	throw new UnsupportedOperationException("The data exporting for lifecycle analysis is not supported!");
    }

    @Override
    protected String getDefaultExportFileExtension() {
	throw new UnsupportedOperationException("The data exporting for lifecycle analysis is not supported!");
    }

    //////////////////////// Recently added stubs ////////////////////////

    public Ordering<ICategory, IDistributedProperty> getOrdering() {
	// TODO Auto-generated method stub
	return null;
    }

    public boolean getTotal() {
	// TODO Auto-generated method stub
	return false;
    }

    public List<String> getCurrentCategoriesStrings() {
	// TODO Auto-generated method stub
	return null;
    }

    public DistributionProperty getDistributionProperty() {
	// TODO Auto-generated method stub
	return null;
    }

    public ICategory findCategoryByName(final String info) {
	// TODO Auto-generated method stub
	return null;
    }

    public Object getLifecycleProperty() {
	// TODO Auto-generated method stub
	return null;
    }

    public Object allCategories(final Object lifecycleProperty) {
	// TODO Auto-generated method stub
	return null;
    }

}
