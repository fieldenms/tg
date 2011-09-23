package ua.com.fielden.platform.swing.analysis;

import java.io.File;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.selectioncheckbox.SelectionCheckBoxPanel.IAction;
import ua.com.fielden.platform.swing.components.blocking.BlockingIndefiniteProgressLayer;
import ua.com.fielden.platform.swing.model.ICloseGuard;
import ua.com.fielden.platform.swing.review.report.ReportMode;
import ua.com.fielden.platform.swing.view.BasePanel;

public abstract class AbstractAnalysisReportView<T extends AbstractEntity, DAO extends IEntityDao<T>, WV extends IAnalysisWizardModel, RV extends IAnalysisReportModel> extends BasePanel {

    private static final long serialVersionUID = 4035958150588145744L;

    private final AbstractAnalysisReportModel<T, DAO> model;
    //Models for creating wizard and report views.
    private final WV analysisWizardModel;
    private final RV analysisReportModel;

    private ReportMode mode;
    private boolean reviewVisible = true;

    public AbstractAnalysisReportView(final AbstractAnalysisReportModel<T, DAO> model, final BlockingIndefiniteProgressLayer tabPaneLayer, final IAnalysisReportPersistentObject pObj) {
	this.model = model;
	this.analysisWizardModel = createWizardModel(tabPaneLayer, pObj);
	this.analysisReportModel = createReportModel(tabPaneLayer, pObj);
	if (pObj != null) {
	    this.reviewVisible = pObj.isVisible();
	}
    }

    public final void setMode(final ReportMode mode, final boolean shouldRestore) throws IllegalStateException {
	if (this.mode == mode) {
	    throw new IllegalStateException("The " + getModel().getName() + " report is already in the " + mode.name() + " mode");
	}
	switch (mode) {
	case WIZARD:
	    if (!isConfigurable()) {
		throw new IllegalStateException("This report is not configurable");
	    }
	    analysisWizardModel.createWizardView(this);
	    break;
	case REPORT:
	    if (shouldRestore) {
		analysisReportModel.restoreReportView(this);
	    } else {
		analysisReportModel.createReportView(this);
	    }
	    break;
	}
	this.mode = mode;
	getModel().getCenterModel().getRun().setEnabled(canRun(), false);
	getModel().getCenterModel().getExport().setEnabled(canExport(), false);
    }

    public final WV getAnalysisWizardModel() {
	return analysisWizardModel;
    }

    public final RV getAnalysisReportModel() {
	return analysisReportModel;
    }

    public final ReportMode getMode() {
	return mode;
    }

    public AbstractAnalysisReportModel<T, DAO> getModel() {
	return model;
    }

    @Override
    public final ICloseGuard canClose() {
	for (final DetailsFrame detailsFrame : getModel().getDetailsFrames()) {
	    final ICloseGuard guard = detailsFrame.canClose();
	    if (guard != null) {
		return guard;
	    }
	}
	return null;
    }

    @Override
    public final void close() {
	super.close();
	setReviewVisible(false);
	for (final DetailsFrame frame : getModel().getDetailsFrames()) {
	    frame.close();
	}
	getModel().closeAnalysiReview();
    }

    @Override
    public final String whyCannotClose() {
	for (final DetailsFrame frame : getModel().getDetailsFrames()) {
	    if (frame.canClose() != null) {
		frame.setVisible(true);
		return "<html><b>" + frame.getTitle() + ":</b> " + frame.whyCannotClose() + "</html>";
	    }
	}
	return "";
    }

    public final void setReviewVisible(final boolean reviewVisible) {
	this.reviewVisible = reviewVisible;
    }

    public final boolean isReviewVisible() {
	return reviewVisible;
    }

    /**
     * Returns value that indicates whether this report can be executed or not.
     * 
     * @return
     */
    public final boolean canRun(){
	return ReportMode.REPORT.equals(getMode());
    }

    /**
     * Returns value that indicates whether this report can be exported into the external file or not.
     * 
     * @return
     */
    public boolean canExport(){
	return ReportMode.REPORT.equals(getMode());
    }

    protected abstract WV createWizardModel(BlockingIndefiniteProgressLayer tabPaneLayer, IAnalysisReportPersistentObject pObj);

    protected abstract RV createReportModel(BlockingIndefiniteProgressLayer tabPaneLayer, IAnalysisReportPersistentObject pObj);

    public abstract void commit() throws IllegalStateException;

    public abstract IAnalysisReportPersistentObject save();

    public abstract int getPageSize();

    public abstract void updateView(Object data, IAction afterUpdateAction);

    public abstract void resetView();

    public abstract boolean isConfigurable();

    public abstract boolean isPaginationSupport();

    public abstract Result exportData(File file);
}
