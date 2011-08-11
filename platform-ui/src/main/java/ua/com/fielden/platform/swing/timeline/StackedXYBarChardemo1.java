package ua.com.fielden.platform.swing.timeline;

import java.awt.Dimension;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StackedXYBarRenderer;
import org.jfree.data.xy.DefaultTableXYDataset;
import org.jfree.data.xy.TableXYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;

public class StackedXYBarChardemo1 extends ApplicationFrame {

    public StackedXYBarChardemo1(final String s) {
	super(s);
	final TableXYDataset tablexydataset = createDataset();
	final JFreeChart jfreechart = createChart(tablexydataset);
	final ChartPanel chartpanel = new ChartPanel(jfreechart);
	chartpanel.setPreferredSize(new Dimension(500, 270));
	setContentPane(chartpanel);
    }

    private TableXYDataset createDataset() {
	final DefaultTableXYDataset defaulttablexydataset = new DefaultTableXYDataset();
	final XYSeries xyseries = new XYSeries("Series 1", true, false);
	xyseries.add(1.0D, 5D);
	xyseries.add(2D, 15.5D);
	xyseries.add(3D, 9.5D);
	xyseries.add(4D, 7.5D);
	defaulttablexydataset.addSeries(xyseries);
	final XYSeries xyseries1 = new XYSeries("Series 2", true, false);
	xyseries1.add(1.0D, 5D);
	xyseries1.add(2D, 15.5D);
	xyseries1.add(3D, 9.5D);
	xyseries1.add(4D, 3.5D);
	defaulttablexydataset.addSeries(xyseries1);
	return defaulttablexydataset;
    }

    private JFreeChart createChart(final TableXYDataset tablexydataset) {
	final NumberAxis numberaxis = new NumberAxis("X");
	numberaxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
	final NumberAxis numberaxis1 = new NumberAxis("Y");
	final StackedXYBarRenderer stackedxybarrenderer = new StackedXYBarRenderer(0.10000000000000001D);
	stackedxybarrenderer.setDrawBarOutline(false);
	final XYPlot xyplot = new XYPlot(tablexydataset, numberaxis, numberaxis1, stackedxybarrenderer);
	final JFreeChart jfreechart = new JFreeChart("Stacked XY Bar Chart demo 1", xyplot);
	return jfreechart;
    }

    public static void main(final String args[]) {
	final StackedXYBarChardemo1 stackedxybarchartdemo1 = new StackedXYBarChardemo1("Stacked XY Bar Chart demo 1");
	stackedxybarchartdemo1.pack();
	RefineryUtilities.centerFrameOnScreen(stackedxybarchartdemo1);
	stackedxybarchartdemo1.setVisible(true);
    }
}