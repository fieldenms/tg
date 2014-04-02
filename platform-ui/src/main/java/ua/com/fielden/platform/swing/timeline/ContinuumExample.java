package ua.com.fielden.platform.swing.timeline;

import java.awt.Color;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.Shape;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.LegendItem;
import org.jfree.chart.LegendItemCollection;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.SymbolAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYBarPainter;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.data.gantt.Task;
import org.jfree.data.gantt.TaskSeries;
import org.jfree.data.gantt.TaskSeriesCollection;
import org.jfree.data.gantt.XYTaskDataset;
import org.jfree.data.time.SimpleTimePeriod;

import ua.com.fielden.platform.swing.utils.SimpleLauncher;
import ua.com.fielden.platform.swing.utils.SwingUtilitiesEx;

public class ContinuumExample {

    public static void main(final String[] args) {

        try {
            SwingUtilitiesEx.installNimbusLnFifPossible();

            final TaskSeriesCollection dataset = new TaskSeriesCollection();

            final DateFormat dateFormat = new SimpleDateFormat("dd/MM/yy hh:mm a");

            final TaskSeries s1 = new TaskSeries("Team A");
            final TaskSeries s2 = new TaskSeries("Team B");
            final TaskSeries s3 = new TaskSeries("Team C");
            final TaskSeries s4 = new TaskSeries("Team D");
            final TaskSeries s5 = new TaskSeries("Team E");

            s1.add(new ColoredTask("Team A", new SimpleTimePeriod(dateFormat.parse("01/01/09 02:00 AM"), dateFormat.parse("01/01/09 03:00 AM")), Color.BLUE, "Value 1"));

            s2.add(new ColoredTask("Team B", new SimpleTimePeriod(dateFormat.parse("01/01/09 04:00 AM"), dateFormat.parse("01/01/09 05:00 AM")), Color.BLUE, "Value 1"));
            s2.add(new ColoredTask("Team B", new SimpleTimePeriod(dateFormat.parse("01/01/09 05:00 AM"), dateFormat.parse("01/01/09 11:00 AM")), Color.YELLOW, "Value 2"));

            s3.add(new ColoredTask("Team C", new SimpleTimePeriod(dateFormat.parse("01/01/09 07:00 AM"), dateFormat.parse("01/01/09 08:00 AM")), Color.BLUE, "Value 1"));
            s3.add(new ColoredTask("Team C", new SimpleTimePeriod(dateFormat.parse("01/01/09 10:00 AM"), dateFormat.parse("01/01/09 11:00 AM")), Color.BLUE, "Value 1"));

            final Task t = new ColoredTask("Team gdfueigui", new SimpleTimePeriod(dateFormat.parse("01/01/09 07:00 PM"), dateFormat.parse("01/01/09 10:00 PM")), Color.RED, "Value 3");
            //	    t.setPercentComplete(0.3);
            t.addSubtask(new ColoredTask("Team ed34ff", new SimpleTimePeriod(dateFormat.parse("01/01/09 07:45 PM"), dateFormat.parse("01/01/09 9:15 PM")), Color.YELLOW, "Value 2"));
            t.addSubtask(new ColoredTask("Team e324dff", new SimpleTimePeriod(dateFormat.parse("01/01/09 09:17 PM"), dateFormat.parse("01/01/09 9:23 PM")), Color.YELLOW, "Value 2"));
            s3.add(t);

            s4.add(new ColoredTask("Team D", new SimpleTimePeriod(dateFormat.parse("01/01/09 01:00 PM"), dateFormat.parse("01/01/09 02:00 PM")), Color.BLUE, "Value 1"));
            s4.add(new ColoredTask("Team D", new SimpleTimePeriod(dateFormat.parse("01/01/09 03:00 PM"), dateFormat.parse("01/01/09 04:00 PM")), Color.BLUE, "Value 1"));

            s5.add(new ColoredTask("Team E", new SimpleTimePeriod(dateFormat.parse("01/01/09 05:00 PM"), dateFormat.parse("01/01/09 06:00 PM")), Color.BLUE, "Value 1"));

            dataset.add(s1);
            dataset.add(s2);
            dataset.add(s3);
            dataset.add(s4);
            dataset.add(s5);

            final XYTaskDataset xyTaskDataset1 = new XYTaskDataset(dataset);
            xyTaskDataset1.setSeriesWidth(1.0);
            //		final PeriodAxis pAxis1 = new PeriodAxis("period");
            final DateAxis yAxis1 = new DateAxis("Date/Time");
            final SymbolAxis xAxis1 = new SymbolAxis("", new String[] { "ExIdle", "OvSpeed", "Stop", "Drive", "Fifth" });

            //	    final NumberAxis xAxis1 = new NumberAxis3D("number axis 3d");
            //	    xAxis1.setAutoTickUnitSelection(true);

            yAxis1.setRange(dateFormat.parse("01/01/09" + " 00:00 AM"), dateFormat.parse("01/01/09" + " 11:59 PM"));

            final XYBarRenderer renderer = new XYBarRenderer() { // 0.022
                private static final long serialVersionUID = -7141717471151835893L;

                @Override
                public Paint getItemPaint(final int row, final int column) {
                    final Task task = dataset.getSeries(row).get(column);
                    if (task instanceof ColoredTask) {
                        return ((ColoredTask) task).getColor();
                    }
                    return Color.BLACK;
                }

            };

            final XYPlot plot = new XYPlot(xyTaskDataset1, xAxis1, yAxis1, renderer);
            //	    final AxisSpace as = new AxisSpace();
            //	    as.setTop(50);
            //	    as.setRight(100);
            //	    as.setBottom(150);
            //	    as.setLeft(200);
            //	    plot.setFixedRangeAxisSpace(as);
            plot.setOrientation(PlotOrientation.HORIZONTAL);
            //		plot.setOrientation(PlotOrientation.VERTICAL);

            final JFreeChart chart = new JFreeChart("01/01/09", plot);
            chart.setBackgroundPaint(Color.white);
            ChartUtilities.applyCurrentTheme(chart);

            final XYPlot xyplot = (XYPlot) chart.getPlot();
            //	    xyplot.setInsets(new RectangleInsets(100,200,150,50), true);
            ((XYBarRenderer) xyplot.getRenderer()).setUseYInterval(true);

            //		((XYBarRenderer)xyplot.getRenderer()).setMargin(-0.3);
            ((XYBarRenderer) xyplot.getRenderer()).setDrawBarOutline(false); // true
            //	    ((XYBarRenderer) xyplot.getRenderer()).setBarPainter(new GradientXYBarPainter());
            ((XYBarRenderer) xyplot.getRenderer()).setBarPainter(new StandardXYBarPainter());
            ((XYBarRenderer) xyplot.getRenderer()).setShadowVisible(false);

            ((XYBarRenderer) xyplot.getRenderer()).setBarAlignmentFactor(0.5);

            //		renderer.setGradientPaintTransformer(null);
            //		renderer.setBarAlignmentFactor(0.5);
            //
            //		renderer.setBarPainter(new StandartGradientXYBarPainter());
            //		renderer.setBase(.4);

            //		String fileName = "image2";
            //		fileName += "12" + ".png";
            //
            //
            //		xyplot.getRenderer().setSeriesItemLabelsVisible(2, true);
            //		xyplot.getRenderer().setBaseItemLabelsVisible(true, true);
            //
            //		xyplot.getRenderer().setSeriesPaint(3, Color.GREEN);
            //		xyplot.getRenderer().setSeriesPaint(2, Color.RED);
            //		xyplot.getRenderer().setSeriesPaint(1, Color.BLUE);
            //		xyplot.getRenderer().setSeriesPaint(0, Color.MAGENTA);
            //
            //
            //		final ChartRenderingInfo info = new ChartRenderingInfo(new StandardEntityCollection());
            //		final File file1 = new File("../webapps/sample/js/" + fileName);
            //		ChartUtilities.saveChartAsPNG(file1, chart, 1100, 250, info);
            //

            final ChartPanel chartPanel = new ChartPanel(chart);
            //	    chartPanel.setPreferredSize(new java.awt.Dimension(500, 1000));
            //	    final JPanel panelT = new JPanel();
            //	    panelT.add(chartPanel);
            //	    final JScrollPane sPane = new JScrollPane(panelT);

            final LegendItemCollection chartLegend = new LegendItemCollection();
            final Shape shape = new Rectangle(10, 10);
            chartLegend.add(new LegendItem("label1", null, null, null, shape, Color.red));
            chartLegend.add(new LegendItem("label2", null, null, null, shape, Color.blue));
            plot.setFixedLegendItems(chartLegend);

            SimpleLauncher.show("XyDataSet example", chartPanel);
            //		SimpleLauncher.show("XyDataSet example", , new BorderLayout(), new JLabel());
        } catch (final Exception e) {
        }
    }

}
