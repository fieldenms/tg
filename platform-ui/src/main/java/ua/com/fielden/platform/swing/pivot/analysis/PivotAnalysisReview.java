package ua.com.fielden.platform.swing.pivot.analysis;

import java.io.File;
import java.util.List;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.reportquery.IAggregatedProperty;
import ua.com.fielden.platform.reportquery.IDistributedProperty;
import ua.com.fielden.platform.selectioncheckbox.SelectionCheckBoxPanel.IAction;
import ua.com.fielden.platform.swing.analysis.AbstractAnalysisReportModel;
import ua.com.fielden.platform.swing.analysis.AbstractAnalysisReportView;
import ua.com.fielden.platform.swing.analysis.IAnalysisReportPersistentObject;
import ua.com.fielden.platform.swing.categorychart.AnalysisReportMode;
import ua.com.fielden.platform.swing.components.blocking.BlockingIndefiniteProgressLayer;
import ua.com.fielden.platform.swing.pivot.analysis.persistence.PivotAnalysisPersistentObject;

public class PivotAnalysisReview<T extends AbstractEntity, DAO extends IEntityDao<T>> extends AbstractAnalysisReportView<T, DAO, PivotAnalysisWizardModel<T, DAO>, PivotAnalysisReportModel<T, DAO>> {

    private static final long serialVersionUID = -5466339200419876060L;

    public PivotAnalysisReview(final AbstractAnalysisReportModel<T, DAO> model, final BlockingIndefiniteProgressLayer tabPaneLayer, final IAnalysisReportPersistentObject pObj) {
	super(model, tabPaneLayer, pObj);

	if (model instanceof PivotAnalysisModel) {
	    ((PivotAnalysisModel) model).setAnalysisReview(this);
	}

	try {
	    setMode(AnalysisReportMode.REPORT, true);
	} catch (final IllegalStateException e) {
	    //JOptionPane.showMessageDialog(this, e.getMessage(), "Information", JOptionPane.INFORMATION_MESSAGE);
	    setMode(AnalysisReportMode.WIZARD, false);
	}
    }

    @Override
    public PivotAnalysisModel<T, DAO> getModel() {
	return (PivotAnalysisModel) super.getModel();
    }

    @Override
    protected PivotAnalysisWizardModel<T, DAO> createWizardModel(final BlockingIndefiniteProgressLayer tabPaneLayer, final IAnalysisReportPersistentObject pObj) {
	return new PivotAnalysisWizardModel<T, DAO>(this);
    }

    @Override
    protected PivotAnalysisReportModel<T, DAO> createReportModel(final BlockingIndefiniteProgressLayer tabPaneLayer, final IAnalysisReportPersistentObject pObj) {
	return new PivotAnalysisReportModel<T, DAO>(this, tabPaneLayer, pObj);
    }

    @Override
    public void commit() throws IllegalStateException {
	final List<IDistributedProperty> selectedDistribution = getAnalysisReportModel().getSelectedDistributionProperties();
	final List<IAggregatedProperty> aggregationProperties = getAnalysisReportModel().getSelectedAggregationProperties();
	getModel().updateCriteria(selectedDistribution, aggregationProperties);
    }

    @Override
    public IAnalysisReportPersistentObject save() {
	if (getMode() == AnalysisReportMode.WIZARD) {
	    getAnalysisReportModel().updateModel();
	}
	return new PivotAnalysisPersistentObject(getAnalysisReportModel().getAvailableDistributionProperties(), getAnalysisReportModel().getAvailableAggregationProperties()//
		, getAnalysisReportModel().getSelectedDistributionProperties(), getAnalysisReportModel().getColumnWidth()//
		, getAnalysisReportModel().getSortingAggregations(), getAnalysisReportModel().getSortableAggregations(), getAnalysisReportModel().getGroupColumnWidth());
    }

    /**
     * This report doesn't support pagination, therefore it always returns 0;
     */
    @Override
    public int getPageSize() {
	return 0;
    }

    @Override
    public void updateView(final Object data, final IAction afterUpdateAction) {
	getAnalysisReportModel().updateView((GroupItem) data, afterUpdateAction);
    }

    @Override
    public void resetView() {
	// TODO Auto-generated method stub
    }

    @Override
    public boolean isConfigurable() {
	return true;
    }

    @Override
    public String getInfo() {
	return "This panel provides 'pivot table' report feature";
    }

    @Override
    public boolean isPaginationSupport() {
	return false;
    }

    @Override
    public Result exportData(final File file) {
	return getModel().exportIntoFile(file);
    }

}
