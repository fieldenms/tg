package ua.com.fielden.platform.swing.timeline;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.data.category.CategoryDataset;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;

public class StackedBarChart3DDemo extends ApplicationFrame {

    /**
     * Creates a new demo.
     * 
     * @param title
     *            the frame title.
     */
    public StackedBarChart3DDemo(final String title) {

        super(title);
        final CategoryDataset dataset = DemoDatasetFactory.createCategoryDataset();
        final JFreeChart chart = createChart(dataset);
        final ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new java.awt.Dimension(500, 270));
        setContentPane(chartPanel);

    }

    /**
     * Creates a chart.
     * 
     * @param dataset
     *            the dataset.
     * 
     * @return The chart.
     */
    private JFreeChart createChart(final CategoryDataset dataset) {

        final JFreeChart chart = ChartFactory.createStackedBarChart3D("Stacked Bar Chart 3D Demo", // chart title
                "Category", // domain axis label
                "Value", // range axis label
                dataset, // data
                PlotOrientation.HORIZONTAL, // the plot orientation
                true, // include legend
                true, // tooltips
                false // urls
        );
        //        final StandardLegend legend = (StandardLegend) chart.getLegend();
        //        legend.setRenderingOrder(LegendRenderingOrder.REVERSE);
        final CategoryPlot plot = (CategoryPlot) chart.getPlot();
        final CategoryItemRenderer renderer = plot.getRenderer();
        //        renderer.setLabelGenerator(new StandardCategoryItemLabelGenerator());
        //        renderer.setItemLabelsVisible(true);
        //        renderer.setPositiveItemLabelPosition(
        //            new ItemLabelPosition(ItemLabelAnchor.CENTER, TextAnchor.CENTER)
        //        );
        //        renderer.setNegativeItemLabelPosition(
        //            new ItemLabelPosition(ItemLabelAnchor.CENTER, TextAnchor.CENTER)
        //        );
        return chart;

    }

    // ****************************************************************************
    // * JFREECHART DEVELOPER GUIDE                                               *
    // * The JFreeChart Developer Guide, written by David Gilbert, is available   *
    // * to purchase from Object Refinery Limited:                                *
    // *                                                                          *
    // * http://www.object-refinery.com/jfreechart/guide.html                     *
    // *                                                                          *
    // * Sales are used to provide funding for the JFreeChart project - please    *
    // * support us so that we can continue developing free software.             *
    // ****************************************************************************

    /**
     * Starting point for the demonstration application.
     * 
     * @param args
     *            ignored.
     */
    public static void main(final String[] args) {

        final StackedBarChart3DDemo demo = new StackedBarChart3DDemo("Stacked Bar Chart 3D Demo");
        demo.pack();
        RefineryUtilities.centerFrameOnScreen(demo);
        demo.setVisible(true);

    }

}