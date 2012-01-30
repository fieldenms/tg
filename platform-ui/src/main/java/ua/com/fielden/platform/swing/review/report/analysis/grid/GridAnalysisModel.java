package ua.com.fielden.platform.swing.review.report.analysis.grid;

import java.util.ArrayList;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.IAddToResultTickManager;
import ua.com.fielden.platform.domaintree.centre.analyses.IAbstractAnalysisDomainTreeManager;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.pagination.IPage;
import ua.com.fielden.platform.swing.egi.models.PropertyTableModel;
import ua.com.fielden.platform.swing.egi.models.builders.PropertyTableModelBuilder;
import ua.com.fielden.platform.swing.pagination.model.development.IPageChangedListener;
import ua.com.fielden.platform.swing.pagination.model.development.PageChangedEvent;
import ua.com.fielden.platform.swing.pagination.model.development.PageHolder;
import ua.com.fielden.platform.swing.review.development.EntityQueryCriteria;
import ua.com.fielden.platform.swing.review.report.analysis.view.AbstractAnalysisReviewModel;

public class GridAnalysisModel<T extends AbstractEntity, DTM extends ICentreDomainTreeManager> extends AbstractAnalysisReviewModel<T, DTM, IAbstractAnalysisDomainTreeManager, IPage<T>> {

    private final PropertyTableModel<T> gridModel;

    public GridAnalysisModel(final EntityQueryCriteria<DTM, T, IEntityDao<T>> criteria, final PageHolder pageHolder) {
	super(criteria, null, pageHolder);
	this.gridModel = createTableModel();
	getPageHolder().addPageChangedListener(new IPageChangedListener() {

	    @SuppressWarnings("unchecked")
	    @Override
	    public void pageChanged(final PageChangedEvent e) {
		getGridModel().setInstances(e.getNewPage() == null ? new ArrayList<T>() : ((IPage<T>)e.getNewPage()).data());
	    }
	});
	getPageHolder().newPage(null);
    }

    private PropertyTableModel<T> createTableModel() {
	final Class<T> entityClass = getCriteria().getEntityClass();
	final PropertyTableModelBuilder<T> tableModelBuilder = new PropertyTableModelBuilder<T> (entityClass);
	final IAddToResultTickManager resultTickManager = getCriteria().getDomainTreeManger().getSecondTick();
	for(final String propertyName : resultTickManager.checkedProperties(entityClass)){
	    tableModelBuilder.addReadonly(propertyName, resultTickManager.getWidth(entityClass, propertyName));
	}
	return tableModelBuilder.build(new ArrayList<T>());
    }

    public final PropertyTableModel<T> getGridModel(){
	return gridModel;
    }

    @Override
    protected IPage<T> executeAnalysisQuery() {
	// TODO Auto-generated method stub
	//getPageHolder().newPage(/*newPage*/);
	return null;
    }

    @Override
    protected Result canLoadData() {
	//TODO consider this implementation.
	return getCriteria().isValid();
    }
}
