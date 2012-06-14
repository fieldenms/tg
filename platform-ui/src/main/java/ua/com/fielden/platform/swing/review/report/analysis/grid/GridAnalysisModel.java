package ua.com.fielden.platform.swing.review.report.analysis.grid;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.centre.analyses.IAbstractAnalysisDomainTreeManager.IAbstractAnalysisDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.pagination.IPage;
import ua.com.fielden.platform.swing.egi.AbstractPropertyColumnMapping;
import ua.com.fielden.platform.swing.egi.models.PropertyTableModel;
import ua.com.fielden.platform.swing.pagination.model.development.PageHolder;
import ua.com.fielden.platform.swing.review.DynamicPropertyAnalyser;
import ua.com.fielden.platform.swing.review.development.EntityQueryCriteria;
import ua.com.fielden.platform.swing.review.report.analysis.view.AbstractAnalysisReviewModel;

public class GridAnalysisModel<T extends AbstractEntity<?>, CDTME extends ICentreDomainTreeManagerAndEnhancer> extends AbstractAnalysisReviewModel<T, CDTME, IAbstractAnalysisDomainTreeManagerAndEnhancer, IPage<T>> {


    private GridAnalysisView<T, CDTME> analysisView;

    public GridAnalysisModel(final EntityQueryCriteria<CDTME, T, IEntityDao<T>> criteria, final PageHolder pageHolder) {
	super(criteria, null, pageHolder);
	this.analysisView = null;
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

    @Override
    protected IPage<T> executeAnalysisQuery() {
	final IPage<T> newPage = getCriteria().run(analysisView.getPageSize());
	getPageHolder().newPage(newPage);
	return newPage;
    }

    protected T getEntityById(final Long id) {
        return getCriteria().getEntityById(id);
    }

    @Override
    protected Result canLoadData() {
	return getCriteria().isValid();
    }

    @Override
    protected void exportData(final String fileName) throws IOException {
	final PropertyTableModel<T> tableModel = analysisView.getEgiPanel().getEgi().getActualModel();
	final List<String> propertyNames = new ArrayList<String>(tableModel.getPropertyColumnMappings().size());
	final List<String> propertyTitles = new ArrayList<String>(tableModel.getPropertyColumnMappings().size());
	for (final AbstractPropertyColumnMapping<T> mapping : tableModel.getPropertyColumnMappings()) {
	    final DynamicPropertyAnalyser propertyAnalyser = new DynamicPropertyAnalyser(getCriteria().getManagedType(), mapping.getPropertyName());
	    propertyNames.add(propertyAnalyser.getCriteriaFullName());
	    propertyTitles.add(mapping.getPropertyTitle());
	}
	getCriteria().export(fileName, propertyNames.toArray(new String[] {}), propertyTitles.toArray(new String[] {}));
    }

    @Override
    protected String[] getExportFileExtensions() {
	return new String[] {getDefaultExportFileExtension()};
    }

    @Override
    protected String getDefaultExportFileExtension() {
	return "xls";
    }
}
