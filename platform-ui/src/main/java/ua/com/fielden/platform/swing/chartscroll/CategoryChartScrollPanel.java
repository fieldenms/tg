package ua.com.fielden.platform.swing.chartscroll;

import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JPanel;
import javax.swing.JScrollBar;

import net.miginfocom.swing.MigLayout;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.data.Range;
import org.jfree.util.ObjectList;

import ua.com.fielden.platform.swing.categorychart.ActionChartPanel;
import ua.com.fielden.platform.swing.categorychart.CategoryChartTypes;
import ua.com.fielden.platform.swing.categorychart.ChartPanelChangedEventObject;
import ua.com.fielden.platform.swing.categorychart.IChartPanelChangeListener;
import ua.com.fielden.platform.swing.categorychart.MultipleChartPanel;
import ua.com.fielden.platform.swing.categorychart.MultipleChartPanelEvent;
import ua.com.fielden.platform.swing.categorychart.MultipleChartPanelListener;

public class CategoryChartScrollPanel extends JPanel {

    private static final long serialVersionUID = -8091474048211607676L;

    private final MultipleChartPanel<?, ?> multipleChartPanel;
    private final JScrollBar chartScroll;

    private final Map<ActionChartPanel<?, ?>, Map<CategoryChartTypes, ObjectList>> defaultRanges = new HashMap<ActionChartPanel<?, ?>, Map<CategoryChartTypes, ObjectList>>();

    private int currentExtentValue;

    public CategoryChartScrollPanel(final MultipleChartPanel<?, CategoryChartTypes> multipleChartPanel, final int extentSize) {
	super(new MigLayout("fill, insets 0", "[grow,fill]", "[grow,fill][]"));
	this.multipleChartPanel = multipleChartPanel;
	this.currentExtentValue = extentSize;
	int max = 0;
	for (int chartPanelIndex = 0; chartPanelIndex < multipleChartPanel.getChartPanelCount(); chartPanelIndex++) {
	    final ActionChartPanel<?, ?> chartPanel = multipleChartPanel.getChartPanel(chartPanelIndex);
	    if (chartPanel.getChart() != null) {
		final int newMax = chartPanel.getChart().getCategoryPlot().getCategories().size();
		if (newMax > max) {
		    max = newMax;
		}
	    }
	}
	this.chartScroll = new JScrollBar(JScrollBar.HORIZONTAL);
	setScrollBarProperties(0, currentExtentValue, 0, max);
	chartScroll.addAdjustmentListener(new AdjustmentListener() {

	    @Override
	    public void adjustmentValueChanged(final AdjustmentEvent e) {
		for (int chartPanelIndex = 0; chartPanelIndex < multipleChartPanel.getChartPanelCount(); chartPanelIndex++) {
		    final ActionChartPanel<?, CategoryChartTypes> chartPanel = multipleChartPanel.getChartPanel(chartPanelIndex);
		    if (chartPanel.getChart() != null) {
			updateChart(chartPanel.getChart(), chartScroll.getModel().getExtent(), chartScroll.getModel().getValue());
		    }
		}
	    }

	});
	for (int chartPanelIndex = 0; chartPanelIndex < multipleChartPanel.getChartPanelCount(); chartPanelIndex++) {
	    initChartPanel(multipleChartPanel.getChartPanel(chartPanelIndex));
	}
	multipleChartPanel.addMultipleChartPanelListener(new MultipleChartPanelListener() {

	    @Override
	    public void valueChanged(final MultipleChartPanelEvent event) {
		if (event.isRemoved()) {
		    defaultRanges.remove(event.getChartPanel());
		} else {
		    initChartPanel(event.getChartPanel());
		}
	    }
	});
	add(this.multipleChartPanel, "wrap");
	add(chartScroll);
    }

    private void initChartPanel(final ActionChartPanel<?, ?> chartPanel) {
	final Map<CategoryChartTypes, ObjectList> chartDefaultRanges = new HashMap<CategoryChartTypes, ObjectList>();
	for (final CategoryChartTypes chartTypes : CategoryChartTypes.values()) {
	    chartDefaultRanges.put(chartTypes, new ObjectList());
	}
	defaultRanges.put(chartPanel, chartDefaultRanges);
	initChart(chartPanel, chartScroll.getModel().getValue());
	chartPanel.addChartPanelChangedListener(new IChartPanelChangeListener() {

	    @Override
	    public void chartPanelChanged(final ChartPanelChangedEventObject event) {
		initChart(chartPanel, chartScroll.getModel().getValue());
	    }

	});
    }

    private void initChart(final ActionChartPanel<?, ?> chartPanel, final int position) {
	if (chartPanel == null || chartPanel.getChart() == null) {
	    return;
	}
	final JFreeChart chart = chartPanel.getChart();
	final CategoryPlot categoryPlot = chart.getCategoryPlot();
	setScrollBarProperties(position, currentExtentValue, chartScroll.getMinimum(), categoryPlot.getCategories().size());
	if (categoryPlot instanceof ScrollableCategoryPlot) {
	    ((ScrollableCategoryPlot) categoryPlot).addAutoRangeChangedListener(new IAutoRangeChangedListener() {

		@Override
		public void autoRangeChanged(final AutoRangeChangedEvent event) {
		    setDefaultRange(chartPanel, event.getAxisIndex(), event.getNewRange());
		}

	    });
	    for (int axisCounter = 0; axisCounter < categoryPlot.getRangeAxisCount(); axisCounter++) {
		((ScrollableCategoryPlot) categoryPlot).combineAutoRange(axisCounter, getDefaultRange(chartPanel, axisCounter));
	    }
	}
	for (int datasetCounter = 0; datasetCounter < categoryPlot.getDatasetCount(); datasetCounter++) {
	    if (!(categoryPlot.getDataset(datasetCounter) instanceof ScrollableDataset)) {
		final ScrollableDataset scrollableDataset = new ScrollableDataset(categoryPlot.getDataset(datasetCounter));
		scrollableDataset.setScrollProperties(chartScroll.getModel().getExtent(), position);
		categoryPlot.setDataset(datasetCounter, scrollableDataset);
	    } else {
		final ScrollableDataset scrollableDataset = (ScrollableDataset) categoryPlot.getDataset(datasetCounter);
		scrollableDataset.setScrollProperties(chartScroll.getModel().getExtent(), position);
	    }
	}
    }

    private void updateChart(final JFreeChart chart, final int extentSize, final int position) {
	final CategoryPlot categoryPlot = chart.getCategoryPlot();
	for (int datasetCounter = 0; datasetCounter < categoryPlot.getDatasetCount(); datasetCounter++) {
	    if (categoryPlot.getDataset(datasetCounter) instanceof ScrollableDataset) {
		final ScrollableDataset scrollableDataset = (ScrollableDataset) categoryPlot.getDataset(datasetCounter);
		scrollableDataset.setScrollProperties(extentSize, position);
	    }
	}
    }

    private Range getDefaultRange(final ActionChartPanel<?, ?> chartPanel, final int index) {
	final CategoryPlot categoryPlot = chartPanel.getChart().getCategoryPlot();
	final Map<CategoryChartTypes, ObjectList> typeRanges = defaultRanges.get(chartPanel);
	final ObjectList chartRanges = typeRanges == null ? null : typeRanges.get(chartPanel.getChartType());
	if (chartRanges == null) {
	    return null;
	}
	if (chartRanges.get(index) == null) {
	    chartRanges.set(index, categoryPlot.getDataRange(categoryPlot.getRangeAxis(index)));
	}
	return (Range) chartRanges.get(index);
    }

    private void setDefaultRange(final ActionChartPanel<?, ?> chartPanel, final int index, final Range range) {
	final Map<CategoryChartTypes, ObjectList> typeRanges = defaultRanges.get(chartPanel);
	final ObjectList chartRanges = typeRanges == null ? null : typeRanges.get(chartPanel.getChartType());
	if (chartRanges == null) {
	    return;
	}
	chartRanges.set(index, range);
    }

    public void updateScroll(final int categoryCount) {
	currentExtentValue = categoryCount;
	setScrollBarProperties(chartScroll.getValue(), currentExtentValue, chartScroll.getMinimum(), chartScroll.getMaximum());
	for (int chartPanelIndex = 0; chartPanelIndex < multipleChartPanel.getChartPanelCount(); chartPanelIndex++) {
	    final ActionChartPanel<?, ?> chartPanel = multipleChartPanel.getChartPanel(chartPanelIndex);
	    if (chartPanel.getChart() != null) {
		updateChart(chartPanel.getChart(), chartScroll.getModel().getExtent(), chartScroll.getModel().getValue());
	    }
	}

    }

    private void setScrollBarProperties(final int value, final int extent, final int min, final int max) {
	if (min > max) {
	    throw new IllegalArgumentException(min + ">" + max);
	}
	if (min < 0 || max < 0 || extent < 0 || value < 0) {
	    throw new IllegalArgumentException("Some of the arguments are les then zero");
	}
	int newValue = value, newExtent = extent;
	if (newValue + newExtent > max) {
	    newValue = max - newExtent;
	    if (newValue < 0) {
		newValue = 0;
		newExtent = max;
	    }
	}
	chartScroll.getModel().setRangeProperties(newValue, newExtent, min, max, chartScroll.getModel().getValueIsAdjusting());
	chartScroll.setBlockIncrement(newExtent);
	if (chartScroll.getModel().getMaximum() == chartScroll.getModel().getExtent()) {
	    chartScroll.setVisible(false);
	} else {
	    chartScroll.setVisible(true);
	}
    }

    public int getActualCategoriesCount() {
	final CategoryPlot plot = multipleChartPanel.getChartPanel(0).getChart().getCategoryPlot();
	if (plot.getDataset() instanceof ScrollableDataset) {
	    return ((ScrollableDataset) plot.getDataset()).getOriginDataset().getColumnCount();
	} else {
	    return plot.getCategories().size();
	}
    }

    /**
     * Resets the default ranges of the chart.
     */
    public void resetScrollRanges() {
	for (final Map<CategoryChartTypes, ObjectList> value : defaultRanges.values()) {
	    for (final CategoryChartTypes chartType : CategoryChartTypes.values()) {
		final ObjectList chartRanges = value.get(chartType);
		for (int rangeIndex = 0; rangeIndex < chartRanges.size(); rangeIndex++) {
		    chartRanges.set(rangeIndex, null);
		}
	    }
	}
    }
}
