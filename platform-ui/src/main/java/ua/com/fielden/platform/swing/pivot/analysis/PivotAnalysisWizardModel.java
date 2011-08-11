package ua.com.fielden.platform.swing.pivot.analysis;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.swing.dynamicreportstree.EntitiesTreeColumn;
import ua.com.fielden.platform.swing.groupanalysis.GroupAnalysisWizardModel;

public class PivotAnalysisWizardModel<T extends AbstractEntity, DAO extends IEntityDao<T>> extends GroupAnalysisWizardModel<T, DAO, PivotAnalysisReview<T, DAO>> {

    public PivotAnalysisWizardModel(final PivotAnalysisReview<T, DAO> reportView) {
	super(reportView, "pivot");
    }

    @Override
    public boolean isValidToBuildReportView() {
	return configureTree != null && configureTree.getCheckingPaths(EntitiesTreeColumn.CRITERIA_COLUMN.getColumnIndex()).length > 0
	/*&& configureTree.getCheckingPaths(EntitiesTreeColumn.TABLE_HEADER_COLUMN.getColumnIndex()).length > 0*/;
    }

    @Override
    protected void updateReportView() {
	// TODO Auto-generated method stub

    }

}
