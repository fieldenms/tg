package ua.com.fielden.platform.swing.categorychart;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;

import ua.com.fielden.platform.domaintree.centre.analyses.IAnalysisDomainTreeManager.IAnalysisAddToAggregationTickManager;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.swing.checkboxlist.CheckboxList;
import ua.com.fielden.platform.swing.review.report.analysis.chart.ChartAnalysisModel;

/**
 * The {@link AnalysisListDragToSupport} for chart analysis. Supports dragging in to the aggregation properties list.
 *
 * @author TG Team
 *
 * @param <T>
 * @param <M>
 * @param <CT>
 */
public class ChartAnalysisAggregationListDragToSupport<T extends AbstractEntity<?>, M, CT> extends AnalysisListDragToSupport<T> {

    private final MultipleChartPanel<M, CT> multipleChartPanel;
    private final ChartAnalysisModel<T> chartAnalysisModel;

    /**
     * Initialises this {@link ChartAnalysisAggregationListDragToSupport} with "drop to" list, chart panel container where charts will be reordered in case when the list reorders.
     * And {@link ChartAnalysisModel} instance that contains the information about the checked properties.
     *
     * @param list
     * @param multipleChartPanel
     * @param chartAnalysisModel
     */
    public ChartAnalysisAggregationListDragToSupport(final CheckboxList<String> list, final MultipleChartPanel<M, CT> multipleChartPanel, final ChartAnalysisModel<T> chartAnalysisModel) {
	super(list, chartAnalysisModel.getCriteria().getEntityClass(), chartAnalysisModel.adtme().getSecondTick());
	this.multipleChartPanel = multipleChartPanel;
	this.chartAnalysisModel = chartAnalysisModel;
    }

    @Override
    public boolean dropTo(final Point point, final Object what, final JComponent draggedFrom) {
	final int previousChartIndex = getSelectedOrderedProperties().indexOf(what);
	final boolean result = super.dropTo(point, what, draggedFrom);
	if (!result) {
	    return false;
	}
	final int newChartIndex = getSelectedOrderedProperties().indexOf(what);
	if (newChartIndex == previousChartIndex || previousChartIndex < 0 || newChartIndex < 0 || previousChartIndex >= multipleChartPanel.getChartPanelsCount()
		|| newChartIndex >= multipleChartPanel.getChartPanelsCount()) {
	    return true;
	}
	multipleChartPanel.changeChartPosition(previousChartIndex, newChartIndex);
	multipleChartPanel.invalidate();
	multipleChartPanel.revalidate();
	multipleChartPanel.repaint();
	return true;
    }

    /**
     * Returns the ordered list of checked and used properties. Those are active in data model for analysis.
     *
     * @return
     */
    private List<String> getSelectedOrderedProperties() {
	final Class<T> root = chartAnalysisModel.getCriteria().getEntityClass();
	final IAnalysisAddToAggregationTickManager secondTick = chartAnalysisModel.adtme().getSecondTick();

	final List<String> usedProperties = secondTick.usedProperties(root);
	final List<String> actualProperties = chartAnalysisModel.getChartAnalysisDataProvider().aggregatedProperties();
	final List<String> selectedOrderedProperties = new ArrayList<String>();
	for (final String property : usedProperties) {
	    if (actualProperties.contains(property)) {
		selectedOrderedProperties.add(property);
	    }
	}
	return selectedOrderedProperties;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected CheckboxList<String> getList() {
	return (CheckboxList<String>) super.getList();
    }
}
