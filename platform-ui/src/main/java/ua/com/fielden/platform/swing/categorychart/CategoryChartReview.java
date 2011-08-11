package ua.com.fielden.platform.swing.categorychart;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.equery.EntityAggregates;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.reportquery.IAggregatedProperty;
import ua.com.fielden.platform.reportquery.IDistributedProperty;
import ua.com.fielden.platform.selectioncheckbox.SelectionCheckBoxPanel.IAction;
import ua.com.fielden.platform.swing.analysis.AbstractAnalysisReportModel;
import ua.com.fielden.platform.swing.analysis.AbstractAnalysisReportView;
import ua.com.fielden.platform.swing.analysis.IAnalysisReportPersistentObject;
import ua.com.fielden.platform.swing.components.blocking.BlockingIndefiniteProgressLayer;
import ua.com.fielden.platform.swing.review.AnalysisPersistentObject;

public class CategoryChartReview<T extends AbstractEntity, DAO extends IEntityDao<T>> extends AbstractAnalysisReportView<T, DAO, CategoryAnalysisWizardModel<T, DAO>, CategoryAnalysisReportModel<T, DAO>> {

    private static final long serialVersionUID = -6805951450043036141L;

    public CategoryChartReview(final AbstractAnalysisReportModel<T, DAO> model, final BlockingIndefiniteProgressLayer tabPaneLayer, final IAnalysisReportPersistentObject persistentObject) {
	super(model, tabPaneLayer, persistentObject);

	try {
	    setMode(AnalysisReportMode.REPORT, true);
	} catch (final IllegalStateException e) {
	    //JOptionPane.showMessageDialog(this, e.getMessage(), "Information", JOptionPane.INFORMATION_MESSAGE);
	    setMode(AnalysisReportMode.WIZARD, false);
	}
    }

    @Override
    public String getInfo() {
	return "This panel provides graphics report for choosen criteria";
    }

    @Override
    public void commit() throws IllegalStateException {
	final IDistributedProperty selectedDistribution = getAnalysisReportModel().getSelectedDistributionProperty();
	final IAggregatedProperty[] aggregationProperties = getAnalysisReportModel().getSelectedAggregationProperties();
	final List<IAggregatedProperty> aggregations = (aggregationProperties != null && aggregationProperties.length > 0) ? Arrays.asList(aggregationProperties)
		: new ArrayList<IAggregatedProperty>();
	getModel().updateCriteria(selectedDistribution, aggregations, getAnalysisReportModel().getSortingParameters());
    }

    @Override
    public CategoryChartReviewModel<T, DAO> getModel() {
	return (CategoryChartReviewModel<T, DAO>) super.getModel();
    }

    /**
     * Returns the page size (i.e. the number of {@link EntityAggregates}s to be retrieved at once).
     * 
     * @return
     */
    @Override
    public int getPageSize() {
	final int groupSize = getModel().getAggregationsSize(getAnalysisReportModel().getCurrentChartType());
	if (groupSize != 0) {
	    final int size = getSize().width / (20 * groupSize);
	    if (size < 1) {
		return 1;
	    } else {
		return size;
	    }
	}
	return 0;
    }

    @Override
    public AnalysisPersistentObject save() {
	if (getMode() == AnalysisReportMode.WIZARD) {
	    getAnalysisReportModel().updateModel();
	}
	final IDistributedProperty selectedDistribution = getAnalysisReportModel().getSelectedDistributionProperty();
	final IAggregatedProperty[] aggregationProperties = getAnalysisReportModel().getSelectedAggregationProperties();
	final List<IAggregatedProperty> aggregations = (aggregationProperties != null && aggregationProperties.length > 0) ? Arrays.asList(aggregationProperties)
		: new ArrayList<IAggregatedProperty>();
	return new AnalysisPersistentObject(selectedDistribution, aggregations, getAnalysisReportModel().getSortingParameters(), getAnalysisReportModel().getAvailableDistributionProperties(), getAnalysisReportModel().getAvailableAggregationProperties(), getModel().getVisibleCategoryCount(), isReviewVisible());
    }

    @Override
    public void updateView(final Object data, final IAction afterUpdateAction) {
	getAnalysisReportModel().updateChart((List<EntityAggregates>) data, afterUpdateAction);
    }

    @Override
    public void resetView() {
	getAnalysisReportModel().resetChartScroller();
    }

    @Override
    public boolean isConfigurable() {
	return true;
    }

    @Override
    protected CategoryAnalysisWizardModel<T, DAO> createWizardModel(final BlockingIndefiniteProgressLayer tabPaneLayer, final IAnalysisReportPersistentObject pObj) {
	return new CategoryAnalysisWizardModel<T, DAO>(this);
    }

    @Override
    protected CategoryAnalysisReportModel<T, DAO> createReportModel(final BlockingIndefiniteProgressLayer tabPaneLayer, final IAnalysisReportPersistentObject pObj) {
	return new CategoryAnalysisReportModel<T, DAO>(this, tabPaneLayer, pObj);
    }

    @Override
    public boolean canExport() {
	return false;
    }

    @Override
    public boolean isPaginationSupport() {
	return true;
    }

    @Override
    public Result exportData(final File file) {
	throw new UnsupportedOperationException("Export action isn't supported yet.");
    }
}
