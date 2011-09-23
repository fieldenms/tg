package ua.com.fielden.platform.swing.analysis.lifecycle;

import java.io.File;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.equery.lifecycle.LifecycleModel;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.selectioncheckbox.SelectionCheckBoxPanel.IAction;
import ua.com.fielden.platform.swing.analysis.AbstractAnalysisReportModel;
import ua.com.fielden.platform.swing.analysis.AbstractAnalysisReportView;
import ua.com.fielden.platform.swing.analysis.IAnalysisReportPersistentObject;
import ua.com.fielden.platform.swing.analysis.IAnalysisWizardModel;
import ua.com.fielden.platform.swing.components.blocking.BlockingIndefiniteProgressLayer;
import ua.com.fielden.platform.swing.review.LifecycleAnalysisPersistentObject;
import ua.com.fielden.platform.swing.review.report.ReportMode;

/**
 * Chart review for entities which contain "monitoring" properties (which state changes could be monitored during lifecycle).
 * 
 * @author Jhou
 * 
 * @param <T>
 * @param <DAO>
 */
public class LifecycleChartReview<T extends AbstractEntity, DAO extends IEntityDao<T>> extends AbstractAnalysisReportView<T, DAO, IAnalysisWizardModel, LifecycleReportViewModel<T, DAO>> {
    private static final long serialVersionUID = 4306716330048824518L;

    public LifecycleChartReview(final AbstractAnalysisReportModel<T, DAO> model, final BlockingIndefiniteProgressLayer tabPaneLayer, final IAnalysisReportPersistentObject pObj) {
	super(model, tabPaneLayer, pObj);
	setMode(ReportMode.REPORT, false);
    }

    @Override
    public String getInfo() {
	return "This panel provides lifecycle report for some entity property";
    }

    @Override
    public void commit() throws IllegalStateException {
	getAnalysisReportModel().updateModel();
	getModel().updateCriteria();
    }

    @Override
    public LifecycleChartReviewModel<T, DAO> getModel() {
	return (LifecycleChartReviewModel<T, DAO>) super.getModel();
    }

    @Override
    protected IAnalysisWizardModel createWizardModel(final BlockingIndefiniteProgressLayer tabPaneLayer, final IAnalysisReportPersistentObject pObj) {
	return null;
    }

    @Override
    protected LifecycleReportViewModel<T, DAO> createReportModel(final BlockingIndefiniteProgressLayer tabPaneLayer, final IAnalysisReportPersistentObject pObj) {
	return new LifecycleReportViewModel<T, DAO>(this, tabPaneLayer);
    }

    @Override
    public IAnalysisReportPersistentObject save() {
	getAnalysisReportModel().updateModel();
	return new LifecycleAnalysisPersistentObject(getModel().getLifecycleProperty(), getModel().getDistributionProperty(), getModel().getFrom(), getModel().getTo(), getModel().getOrdering(), getModel().getCategoriesFor(getModel().getLifecycleProperty()), getModel().getTotal(), isReviewVisible());
    }

    @Override
    public int getPageSize() {
	return 0;
    }

    @Override
    public void updateView(final Object data, final IAction afterUpdateAction) {
	getAnalysisReportModel().updateChart((LifecycleModel<T>) data, afterUpdateAction);
    }

    @Override
    public void resetView() {

    }

    @Override
    public boolean isConfigurable() {
	return false;
    }

    @Override
    public boolean canExport() {
	return false;
    }

    @Override
    public boolean isPaginationSupport() {
	return false;
    }

    @Override
    public Result exportData(final File file) {
	throw new UnsupportedOperationException("Export action isn't supported yet.");
    }

}
