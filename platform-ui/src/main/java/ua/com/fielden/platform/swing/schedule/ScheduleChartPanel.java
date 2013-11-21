package ua.com.fielden.platform.swing.schedule;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.utils.EntityUtils;

public class ScheduleChartPanel<T extends AbstractEntity<?>, V extends Comparable<?>> extends ChartPanel {

    private static final long serialVersionUID = -3358861548848789512L;

    //TODO here must be used sorted list in order to be able to sort series by cut of factor in descendant order.
    private final Set<ScheduleSeries<T, V>> serieses;

    private final String chartName;
    private final String rangeAxisName;
    private final String domainAxisName;

    private final List<T> data = new ArrayList<>();

    private Class<V> currentValueType;
    private boolean wasSeriesAdded = false;

    public ScheduleChartPanel(final Class<V> valueType, final String chartName, final String rangeAxisName, final String domainAxisName) {
	super(null, 640, 480, 0, 0, Integer.MAX_VALUE, Integer.MAX_VALUE, true, true, true, true, true, true, true);
	this.chartName = chartName;
	this.rangeAxisName = rangeAxisName;
	this.domainAxisName = domainAxisName;
	this.serieses = new TreeSet<ScheduleSeries<T, V>>(createSeriesComparator());
	setChart(createChart());
    }

    /**
     * Returns the comparotor for series set that
     *
     * @return
     */
    private Comparator<? super ScheduleSeries<T, V>> createSeriesComparator() {
	return new Comparator<ScheduleSeries<T, V>>() {

	    @Override
	    public int compare(final ScheduleSeries<T, V> o1, final ScheduleSeries<T, V> o2) {
		if(o1.getCutOfFactor() > o2.getCutOfFactor()) {
		    return -1;
		} else if (o1.getCutOfFactor() < o2.getCutOfFactor()){
		    return 1;
		}
		return 0;
	    }
	};
    }

    private JFreeChart createChart() {

	final ValueAxis valueAxis = createValueAxis();
	//final ValueAxis domainAxis = createDomainAxis();
//	symbolaxis.setGridBandsVisible(false);
//
//	//generating renderer for first dataset.
//	final XYBarRenderer renderer0 = new XYBarRenderer();
//	renderer0.setUseYInterval(true);
//	renderer0.setMargin(0.7);
//	//generating renderer for second dataset.
//	final XYBarRenderer renderer1 = new XYBarRenderer();
//	renderer1.setUseYInterval(true);
//	renderer1.setMargin(0.9);
//
//	final XYPlot plot = new XYPlot(null, domainAxis, valueAxis, null);
//	plot.setOrientation(PlotOrientation.HORIZONTAL);
//	plot.setDataset(0, xyDataset[1]);
//	plot.setDataset(1, xyDataset[0]);
//	plot.setRenderer(0, renderer1);
//	plot.setRenderer(1, renderer0);
//
//	final JFreeChart chart = new JFreeChart("Work order schedule", JFreeChart.DEFAULT_TITLE_FONT, plot, true);
//	ChartUtilities.applyCurrentTheme(chart);
//	chart.setBackgroundPaint(Color.white);

	return null;
    }

//    private ValueAxis createDomainAxis() {
//	new SymbolAxis("Work orders", new String[] { wo1.getKey(), wo2.getKey() });
//	return null;
//    }

    private ValueAxis createValueAxis() {
	if (wasSeriesAdded && currentValueType != null) {
	    return Date.class.isAssignableFrom(currentValueType) ? new DateAxis(rangeAxisName) : new NumberAxis(rangeAxisName);
	}
	return new NumberAxis(rangeAxisName);
    }

    public ScheduleChartPanel<T, V> addScheduleSeries(final ScheduleSeries<T, V> scheduleSeries) {
	if (!wasSeriesAdded) {
	    currentValueType = scheduleSeries.getValueType();
	}
	if (!EntityUtils.safeEquals(currentValueType, scheduleSeries.getValueType())) {
	    throw new IllegalArgumentException("The value type of series is different then the other ones");
	}
	serieses.add(scheduleSeries);
	return this;
    }
}
