package ua.com.fielden.platform.swing.timeline;

import java.awt.Color;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberAxis3D;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.data.time.SimpleTimePeriod;
import org.jfree.data.time.TimePeriodValues;
import org.jfree.data.time.TimePeriodValuesCollection;

import ua.com.fielden.platform.swing.utils.SimpleLauncher;

public class TimePeriodSpike {

    public static void main(final String[] args) {
        try {
            final DateFormat dateFormat = new SimpleDateFormat("dd/MM/yy hh:mm a");

            final TimePeriodValues tpv = new TimePeriodValues("TimePeriodValues");
            tpv.add(new SimpleTimePeriod(dateFormat.parse("01/01/09 04:00 AM"), dateFormat.parse("01/01/09 05:00 AM")), 15);
            tpv.add(new SimpleTimePeriod(dateFormat.parse("01/01/09 05:00 AM"), dateFormat.parse("01/01/09 11:00 AM")), 10);
            final TimePeriodValuesCollection dataset = new TimePeriodValuesCollection(tpv);

            final DateAxis xAxis = new DateAxis("Date/Time");
            final NumberAxis yAxis = new NumberAxis3D("number axis 3d");

            xAxis.setRange(dateFormat.parse("01/01/09" + " 00:00 AM"), dateFormat.parse("01/01/09" + " 11:59 PM"));
            final XYPlot plot = new XYPlot(dataset, xAxis, yAxis, new XYBarRenderer());

            final JFreeChart chart = new JFreeChart("01/01/09", plot);
            chart.setBackgroundPaint(Color.white);
            ChartUtilities.applyCurrentTheme(chart);

            final ChartPanel chartPanel = new ChartPanel(chart);
            SimpleLauncher.show("XyDataSet example", chartPanel);
        } catch (final Exception e) {
        }
    }

}
