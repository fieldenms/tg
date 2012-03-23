package ua.com.fielden.platform.swing.review.report.analysis.grid;

import java.util.ArrayList;

import ua.com.fielden.platform.dao2.IEntityDao2;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.IAddToResultTickManager;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.centre.analyses.IAbstractAnalysisDomainTreeManager;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.pagination.IPage2;
import ua.com.fielden.platform.swing.egi.models.PropertyTableModel;
import ua.com.fielden.platform.swing.egi.models.builders.PropertyTableModelBuilder;
import ua.com.fielden.platform.swing.pagination.model.development.IPageChangedListener;
import ua.com.fielden.platform.swing.pagination.model.development.PageChangedEvent;
import ua.com.fielden.platform.swing.pagination.model.development.PageHolder;
import ua.com.fielden.platform.swing.review.development.EntityQueryCriteria;
import ua.com.fielden.platform.swing.review.report.analysis.grid.configuration.GridConfigurationModel;
import ua.com.fielden.platform.swing.review.report.analysis.view.AbstractAnalysisReviewModel;

public class GridAnalysisModel<T extends AbstractEntity<?>, CDTME extends ICentreDomainTreeManagerAndEnhancer> extends AbstractAnalysisReviewModel<T, CDTME, IAbstractAnalysisDomainTreeManager, IPage2<T>> {

    private final PropertyTableModel<T> gridModel;

    public GridAnalysisModel(final GridConfigurationModel<T, CDTME> configurationModel, final EntityQueryCriteria<CDTME, T, IEntityDao2<T>> criteria, final PageHolder pageHolder) {
	super(configurationModel, criteria, null, pageHolder);
	this.gridModel = createTableModel();
	getPageHolder().addPageChangedListener(new IPageChangedListener() {

	    @SuppressWarnings("unchecked")
	    @Override
	    public void pageChanged(final PageChangedEvent e) {
		getGridModel().setInstances(e.getNewPage() == null ? new ArrayList<T>() : ((IPage2<T>)e.getNewPage()).data());
	    }
	});
	getPageHolder().newPage(null);
    }

    @Override
    public GridConfigurationModel<T, CDTME> getConfigurationModel() {
	return (GridConfigurationModel<T, CDTME>)super.getConfigurationModel();
    }

    private PropertyTableModel<T> createTableModel() {
	final Class<T> entityClass = getCriteria().getEntityClass();
	final PropertyTableModelBuilder<T> tableModelBuilder = new PropertyTableModelBuilder<T> (entityClass);
	final IAddToResultTickManager resultTickManager = getCriteria().getCentreDomainTreeMangerAndEnhancer().getSecondTick();
	for(final String propertyName : resultTickManager.checkedProperties(entityClass)){
	    tableModelBuilder.addReadonly(propertyName, resultTickManager.getWidth(entityClass, propertyName));
	}
	return tableModelBuilder.build(new ArrayList<T>());
    }

    public final PropertyTableModel<T> getGridModel(){
	return gridModel;
    }

    @Override
    protected IPage2<T> executeAnalysisQuery() {
	// TODO implement
	//getPageHolder().newPage(/*newPage*/);
	return null;
    }

    @Override
    protected Result canLoadData() {
	//TODO consider this implementation.
	return getCriteria().isValid();
    }
}
