package ua.com.fielden.platform.example.swing.schedule;

import java.awt.Color;
import java.awt.Dimension;
import java.util.Date;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

import net.miginfocom.swing.MigLayout;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.SymbolAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.data.gantt.Task;
import org.jfree.data.gantt.TaskSeries;
import org.jfree.data.gantt.TaskSeriesCollection;
import org.jfree.data.gantt.XYTaskDataset;
import org.jfree.data.time.SimpleTimePeriod;
import org.joda.time.DateTime;

import ua.com.fielden.platform.swing.utils.SwingUtilitiesEx;

import com.jidesoft.plaf.LookAndFeelFactory;

public class ScheduleChartDemo {

    private final static Date w1eStart = new DateTime(2013, 10, 18, 11, 0).toDate();
    private final static Date w1eFinish = new DateTime(2013, 10, 20, 13, 15).toDate();
    private final static Date w1aStart = new DateTime(2013, 10, 19, 14, 0).toDate();
    private final static Date w1aFinish = new DateTime(2013, 10, 21, 16, 0).toDate();
    private final static Date w2eStart = new DateTime(2013, 10, 25, 0, 0).toDate();
    private final static Date w2eFinish = new DateTime(2013, 10, 27, 13, 0).toDate();
    private final static Date w2aStart = new DateTime(2013, 10, 26, 15, 0).toDate();
    private final static Date w2aFinish = new DateTime(2013, 10, 27, 18, 0).toDate();
    private final static WorkOrderEntity wo1 = new WorkOrderEntity("Work order 1")//
    .setEarlyStart(w1eStart)//
    .setEarlyFinish(w1eFinish)//
    .setActualStart(w1aStart)//
    .setActualFinish(w1aFinish);
    private final static WorkOrderEntity wo2 = new WorkOrderEntity("Work order 2")//
    .setEarlyStart(w2eStart)//
    .setEarlyFinish(w2eFinish)//
    .setActualStart(w2aStart)//
    .setActualFinish(w2aFinish);

    public static void main(final String[] args) {
	SwingUtilitiesEx.invokeLater(new Runnable() {

	    @Override
	    public void run() {
		for (final LookAndFeelInfo laf : UIManager.getInstalledLookAndFeels()) {
		    if ("Nimbus".equals(laf.getName())) {
			try {
			    UIManager.setLookAndFeel(laf.getClassName());
			} catch (final Exception e) {
			    e.printStackTrace();
			}
		    }
		}
		com.jidesoft.utils.Lm.verifyLicense("Fielden Management Services", "Rollingstock Management System", "xBMpKdqs3vWTvP9gxUR4jfXKGNz9uq52");
		LookAndFeelFactory.installJideExtension();
		final JFrame frame = new JFrame("Scedule chart demo");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLayout(new MigLayout("fill, insets 0"));
		frame.add(createGantChart(), "growx");
		frame.setPreferredSize(new Dimension(640, 480));
		frame.pack();
		frame.setVisible(true);
	    }

	});
    }

    private static JComponent createGantChart() {
	final JFreeChart jfreechart = createChart(createDataset());
	final ChartPanel chartPanel = new ChartPanel(jfreechart);
	chartPanel.setMouseWheelEnabled(true);
	return chartPanel;
    }

    private static XYTaskDataset[] createDataset() {

	final TaskSeriesCollection taskseriescollection = new TaskSeriesCollection();
	final TaskSeries taskseries = new TaskSeries("test");
	taskseries.add(new Task("Early", new SimpleTimePeriod(wo1.getEarlyStart(), wo1.getEarlyFinish())));
	final TaskSeries taskseries1 = new TaskSeries(wo2.getKey());
	taskseries1.add(new Task("Early", new SimpleTimePeriod(wo2.getEarlyStart(), wo2.getEarlyFinish())));
	taskseriescollection.add(taskseries);
	taskseriescollection.add(taskseries1);

	final TaskSeriesCollection taskseriescollection1 = new TaskSeriesCollection();
	final TaskSeries taskseries2 = new TaskSeries("test1");
	taskseries2.add(new Task("Actual", new SimpleTimePeriod(wo1.getActualStart(), wo1.getActualFinish())));
	final TaskSeries taskseries3 = new TaskSeries(wo2.getKey());
	taskseries3.add(new Task("Actual", new SimpleTimePeriod(wo2.getActualStart(), wo2.getActualFinish())));
	taskseriescollection1.add(taskseries2);
	taskseriescollection1.add(taskseries3);

	final XYTaskDataset[] datasets = new XYTaskDataset[2];
	datasets[0] = new XYTaskDataset(taskseriescollection);
	datasets[1] = new XYTaskDataset(taskseriescollection1);
	return datasets;
    }

    private static JFreeChart createChart(final XYTaskDataset[] xyDataset) {
	final DateAxis rangeAxis = new DateAxis("time");
	final SymbolAxis symbolaxis = new SymbolAxis("Work orders", new String[] { wo1.getKey(), wo2.getKey() });
	symbolaxis.setGridBandsVisible(false);

	//generating renderer for first dataset.
	final XYBarRenderer renderer0 = new XYBarRenderer();
	renderer0.setUseYInterval(true);
	renderer0.setMargin(0.7);
	//generating renderer for second dataset.
	final XYBarRenderer renderer1 = new XYBarRenderer();
	renderer1.setUseYInterval(true);
	renderer1.setMargin(0.9);

	final XYPlot plot = new XYPlot(null, symbolaxis, rangeAxis, null);
	plot.setOrientation(PlotOrientation.HORIZONTAL);
	plot.setDataset(0, xyDataset[1]);
	plot.setDataset(1, xyDataset[0]);
	plot.setRenderer(0, renderer1);
	plot.setRenderer(1, renderer0);

	final JFreeChart chart = new JFreeChart("Work order schedule", JFreeChart.DEFAULT_TITLE_FONT, plot, true);
	ChartUtilities.applyCurrentTheme(chart);
	chart.setBackgroundPaint(Color.white);

	return chart;
    }
}
