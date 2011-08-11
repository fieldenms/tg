package ua.com.fielden.platform.swing.categorychart;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.swing.groupanalysis.GroupAnalysisWizardModel;

public class CategoryAnalysisWizardModel<T extends AbstractEntity, DAO extends IEntityDao<T>> extends GroupAnalysisWizardModel<T, DAO, CategoryChartReview<T, DAO>> {

    public CategoryAnalysisWizardModel(final CategoryChartReview<T, DAO> reportView) {
	super(reportView, "analysis");
    }

    @Override
    protected void updateReportView() {
	if (getReportView().getAnalysisReportModel() != null) {
	    getReportView().getAnalysisReportModel().updateChart(getReportView().getModel().getLoadedData(), null);
	}
    }

}
