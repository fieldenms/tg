package ua.com.fielden.platform.swing.review.report.analysis.grid;

import java.util.ArrayList;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.IAddToResultTickManager;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.centre.analyses.IAbstractAnalysisDomainTreeManager.IAbstractAnalysisDomainTreeManagerAndEnhancer;
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

public class GridAnalysisModel<T extends AbstractEntity<?>, CDTME extends ICentreDomainTreeManagerAndEnhancer> extends AbstractAnalysisReviewModel<T, CDTME, IAbstractAnalysisDomainTreeManagerAndEnhancer, IPage<T>> {

    private final PropertyTableModel<?> gridModel;

    private GridAnalysisView<T, CDTME> analysisView;

    public GridAnalysisModel(final EntityQueryCriteria<CDTME, T, IEntityDao<T>> criteria, final PageHolder pageHolder) {
	super(criteria, null, pageHolder);
	this.analysisView = null;
	this.gridModel = createTableModel();
	getPageHolder().addPageChangedListener(new IPageChangedListener() {

	    @SuppressWarnings({ "unchecked", "rawtypes" })
	    @Override
	    public void pageChanged(final PageChangedEvent e) {
		getGridModel().setInstances(e.getNewPage() == null ? new ArrayList() : ((IPage)e.getNewPage()).data());
	    }
	});
	getPageHolder().newPage(null);
    }

    /**
     * Set the analysis view for this model.
     * Please note that one can set analysis view only once.
     * Otherwise The {@link IllegalStateException} will be thrown.
     * 
     * @param analysisView
     */
    final void setAnalysisView(final GridAnalysisView<T, CDTME> analysisView){
	if(this.analysisView != null){
	    throw new IllegalStateException("The analysis view can be set only once!");
	}
	this.analysisView = analysisView;
    }

    public final PropertyTableModel<?> getGridModel(){
	return gridModel;
    }

    @Override
    protected IPage<T> executeAnalysisQuery() {
	final IPage<T> newPage = getCriteria().run(analysisView.getPageSize());
	getPageHolder().newPage(newPage);
	return newPage;
    }

    @Override
    protected Result canLoadData() {
	return getCriteria().isValid();
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private PropertyTableModel<?> createTableModel() {
	final Class<T> entityClass = getCriteria().getEntityClass();
	final Class<?> managedType = getCriteria().getCentreDomainTreeMangerAndEnhancer().getEnhancer().getManagedType(entityClass);
	final PropertyTableModelBuilder<?> tableModelBuilder = new PropertyTableModelBuilder(managedType);
	final IAddToResultTickManager resultTickManager = getCriteria().getCentreDomainTreeMangerAndEnhancer().getSecondTick();
	for(final String propertyName : resultTickManager.checkedProperties(entityClass)){
	    tableModelBuilder.addReadonly(propertyName, resultTickManager.getWidth(entityClass, propertyName));
	}
	return tableModelBuilder.build(new ArrayList());
    }
}
