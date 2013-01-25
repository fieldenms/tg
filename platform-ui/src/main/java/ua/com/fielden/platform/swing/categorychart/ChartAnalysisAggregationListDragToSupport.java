package ua.com.fielden.platform.swing.categorychart;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JList;

import ua.com.fielden.platform.domaintree.centre.analyses.IAbstractAnalysisDomainTreeManager.IAbstractAnalysisAddToAggregationTickManager;
import ua.com.fielden.platform.domaintree.centre.analyses.IAbstractAnalysisDomainTreeManager.IUsageManager;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.swing.review.report.analysis.chart.ChartAnalysisModel;
import ua.com.fielden.platform.swing.review.report.analysis.chart.ICategoryAnalysisDataProvider;

/**
 * The {@link AnalysisListDragToSupport} for chart analysis. Supports dragging in to the aggregation properties list.
 *
 * @author TG Team
 *
 * @param <T>
 * @param <M>
 * @param <CT>
 */
public class ChartAnalysisAggregationListDragToSupport<T extends AbstractEntity<?>> extends AnalysisListDragToSupport<T> {

    private final IChartPositioner multipleChartPanel;
    private final ICategoryAnalysisDataProvider<Comparable<?>, Number, List<T>> categoryDataProvider;

    /**
     * Initialises this {@link ChartAnalysisAggregationListDragToSupport} with "drop to" list, chart panel container where charts will be reordered in case when the list reorders.
     * And {@link ChartAnalysisModel} instance that contains the information about the checked properties.
     *
     * @param list
     * @param multipleChartPanel
     * @param chartAnalysisModel
     */
    public ChartAnalysisAggregationListDragToSupport(final Class<T> root, final IAbstractAnalysisAddToAggregationTickManager tickManager, final JList<String> list, final IChartPositioner multipleChartPanel, final ICategoryAnalysisDataProvider<Comparable<?>, Number, List<T>> categoryDataProvider) {
	super(list, root, tickManager);
	this.multipleChartPanel = multipleChartPanel;
	this.categoryDataProvider  = categoryDataProvider;
    }

    @Override
    public boolean dropTo(final Point point, final Object what, final JComponent draggedFrom) {
	final int previousChartIndex = getSelectedOrderedProperties().indexOf(what);
	final boolean result = super.dropTo(point, what, draggedFrom);
	if (!result) {
	    return false;
	}
	final int newChartIndex = getSelectedOrderedProperties().indexOf(what);
	if (newChartIndex == previousChartIndex || previousChartIndex < 0 || newChartIndex < 0 || previousChartIndex >= multipleChartPanel.getChartPanelCount()
		|| newChartIndex >= multipleChartPanel.getChartPanelCount()) {
	    return true;
	}
	multipleChartPanel.positionChart(previousChartIndex, newChartIndex);
	return true;
    }

    /**
     * Returns the ordered list of checked and used properties. Those are active in data model for analysis.
     *
     * @return
     */
    protected List<String> getSelectedOrderedProperties() {

	final List<String> usedProperties = ((IUsageManager)tickManager).usedProperties(root);
	final List<String> actualProperties = categoryDataProvider.aggregatedProperties();
	final List<String> selectedOrderedProperties = new ArrayList<String>();
	for (final String property : usedProperties) {
	    if (actualProperties.contains(property)) {
		selectedOrderedProperties.add(property);
	    }
	}
	return selectedOrderedProperties;
    }

}
