package ua.com.fielden.platform.swing.analysis.ndec;

import java.io.File;
import java.util.List;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.equery.EntityAggregates;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.selectioncheckbox.SelectionCheckBoxPanel.IAction;
import ua.com.fielden.platform.swing.analysis.AbstractAnalysisReportView;
import ua.com.fielden.platform.swing.analysis.IAnalysisReportPersistentObject;
import ua.com.fielden.platform.swing.analysis.IAnalysisWizardModel;
import ua.com.fielden.platform.swing.components.blocking.BlockingIndefiniteProgressLayer;
import ua.com.fielden.platform.swing.review.report.ReportMode;

public class NDecAnalysisReview<T extends AbstractEntity, DAO extends IEntityDao<T>> extends AbstractAnalysisReportView<T, DAO, IAnalysisWizardModel, NDecAnalysisReportModel> {

    private static final long serialVersionUID = -7341201818247119130L;

    public NDecAnalysisReview(final NDecAnalysisModel<T, DAO> model, final BlockingIndefiniteProgressLayer tabPaneLayer, final IAnalysisReportPersistentObject pObj) {
	super(model, tabPaneLayer, pObj);
	setMode(ReportMode.REPORT, false);
    }

    @Override
    public NDecAnalysisModel<T, DAO> getModel() {
	return (NDecAnalysisModel<T, DAO>)super.getModel();
    }

    @Override
    protected IAnalysisWizardModel createWizardModel(final BlockingIndefiniteProgressLayer tabPaneLayer, final IAnalysisReportPersistentObject pObj) {
	return null;
    }

    @Override
    protected NDecAnalysisReportModel<T,DAO> createReportModel(final BlockingIndefiniteProgressLayer tabPaneLayer, final IAnalysisReportPersistentObject pObj) {
	return new NDecAnalysisReportModel<T, DAO>(this, tabPaneLayer);
    }

    @Override
    public void commit() throws IllegalStateException {
	// TODO Auto-generated method stub

    }

    @Override
    public NDecAnalysisPersistentObject save() {
	return new NDecAnalysisPersistentObject();
    }

    @Override
    public int getPageSize() {
	return 0;
    }

    @Override
    public void updateView(final Object data, final IAction afterUpdateAction) {
	getModel().getAggregationModel().setModel((List<EntityAggregates>) data);
    }


    @Override
    public void resetView() {
	//Shouldn't be implemented.
    }

    @Override
    public boolean isConfigurable() {
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

    @Override
    public String getInfo() {
	return "NDec view";
    }

}
