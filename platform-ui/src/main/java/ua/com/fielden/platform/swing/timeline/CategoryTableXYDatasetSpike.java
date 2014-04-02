package ua.com.fielden.platform.swing.timeline;

import javax.swing.JFrame;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.IntervalMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StackedXYBarRenderer;
import org.jfree.data.time.Day;
import org.jfree.data.xy.CategoryTableXYDataset;
import org.jfree.date.MonthConstants;
import org.jfree.ui.Layer;

public class CategoryTableXYDatasetSpike {

    public static void main(final String[] argv) {
        final CategoryTableXYDataset l_dataset = new CategoryTableXYDataset();
        l_dataset.setAutoWidth(false);
        l_dataset.setIntervalPositionFactor(0.0);
        l_dataset.setIntervalWidth(1);

        l_dataset.add(new Integer((new Day(1, MonthConstants.FEBRUARY, 2007)).getDayOfMonth()), new Integer(50), "Normal", false);

        l_dataset.add(new Integer((new Day(2, MonthConstants.FEBRUARY, 2007)).getDayOfMonth()), new Integer(75), "Normal", false);

        l_dataset.add(new Integer((new Day(3, MonthConstants.FEBRUARY, 2007)).getDayOfMonth()), new Integer(100), "Normal", false);

        l_dataset.add(new Integer((new Day(4, MonthConstants.FEBRUARY, 2007)).getDayOfMonth()), new Integer(100), "Normal", false);
        l_dataset.add(new Integer((new Day(4, MonthConstants.FEBRUARY, 2007)).getDayOfMonth()), new Integer(25), "Overload", false);

        l_dataset.add(new Integer((new Day(5, MonthConstants.FEBRUARY, 2007)).getDayOfMonth()), new Integer(100), "Normal", false);
        l_dataset.add(new Integer((new Day(5, MonthConstants.FEBRUARY, 2007)).getDayOfMonth()), new Integer(50), "Overload", false);

        l_dataset.add(new Integer((new Day(6, MonthConstants.FEBRUARY, 2007)).getDayOfMonth()), new Integer(75), "Normal", false);

        l_dataset.add(new Integer((new Day(7, MonthConstants.FEBRUARY, 2007)).getDayOfMonth()), new Integer(100), "Normal", false);
        l_dataset.add(new Integer((new Day(7, MonthConstants.FEBRUARY, 2007)).getDayOfMonth()), new Integer(25), "Overload", false);

        l_dataset.add(new Integer((new Day(8, MonthConstants.FEBRUARY, 2007)).getDayOfMonth()), new Integer(50), "Normal", false);

        final StackedXYBarRenderer l_renderer = new StackedXYBarRenderer();

        final NumberAxis l_domain = new NumberAxis("day");
        l_domain.setAutoRangeIncludesZero(false);

        final XYPlot l_plot = new XYPlot(l_dataset, l_domain, new NumberAxis("usage"), l_renderer);
        l_plot.addDomainMarker(new IntervalMarker(6, 8), Layer.BACKGROUND);

        JFrame.setDefaultLookAndFeelDecorated(true);

        final JFrame l_frame = new JFrame("Usage");
        l_frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        l_frame.getContentPane().add(new ChartPanel(new JFreeChart("Usage", JFreeChart.DEFAULT_TITLE_FONT, l_plot, true)));
        l_frame.pack();
        l_frame.setVisible(true);
    }

}
