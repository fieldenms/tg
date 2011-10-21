package ua.com.fielden.platform.swing.analysis.ndec.dec;

import java.awt.Color;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.CategoryToolTipGenerator;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;

import ua.com.fielden.platform.reportquery.ICategoryChartEntryModel;
import ua.com.fielden.platform.swing.categorychart.EntityWrapper;
import ua.com.fielden.platform.swing.categorychart.FixedCategoryAxis;
import ua.com.fielden.platform.utils.Pair;

public class DecModel {

    private final ICategoryChartEntryModel chartModel;
    private final int series;
    private final Color color;
    private final String chartName;
    private final String domainAxisName;
    private final String valueAxisName;

    private final List<CalculatedNumber> calcNumbers;

    private final List<Pair<ILineCalculator, Color>> lineCalculators;

    public DecModel(final ICategoryChartEntryModel chartModel, final int series, final Color color, final String chartName, final String domainAxisName, final String valueAxisName){
	this.chartModel = chartModel;
	this.series = series;
	this.color = color;
	this.chartName = chartName;
	this.domainAxisName = domainAxisName;
	this.valueAxisName = valueAxisName;

	this.calcNumbers = new ArrayList<CalculatedNumber>();
	this.lineCalculators = new ArrayList<Pair<ILineCalculator, Color>>();
    }

    protected ICategoryChartEntryModel getChartModel() {
	return chartModel;
    }

    public DecModel addLine(final ILineCalculator lineCalculator, final Color color){
	lineCalculators.add(new Pair<ILineCalculator, Color>(lineCalculator, color));
	return this;
    }

    public DecModel addCalculatedValue(final CalculatedNumber number){
	calcNumbers.add(number);
	return this;
    }

    public int getCalcValuesNumber(){
	return calcNumbers.size();
    }

    public List<CalculatedNumber> getCalcNumbers() {
	return Collections.unmodifiableList(calcNumbers);
    }

    public JFreeChart createChart() {
	return new JFreeChart(null, JFreeChart.DEFAULT_TITLE_FONT, createPlot(),  false);
    }

    private Pair<CategoryDataset, CategoryDataset> createDatasets() {
	final DefaultCategoryDataset mainDataset = new DefaultCategoryDataset();

	final Comparable<?> seriesValue = chartModel.getSeries(series);
	for (int columnCounter = 0; columnCounter < chartModel.getCategoryCount(); columnCounter++) {
	    final Comparable<?> category = chartModel.getCategory(columnCounter);
	    mainDataset.addValue(chartModel.getValue(series, columnCounter), seriesValue, category);
	}

	final DefaultCategoryDataset lineDataset = lineCalculators.size() == 0 ? null : new DefaultCategoryDataset();

	for(int lineIndex = 0; lineIndex < lineCalculators.size(); lineIndex++){
	    for (int columnCounter = 0; columnCounter < chartModel.getCategoryCount(); columnCounter++) {
		final Comparable<?> category = chartModel.getCategory(columnCounter);
		final ILineCalculator lineCalc = lineCalculators.get(lineIndex).getKey();
		lineDataset.addValue(lineCalc.calculateLineValue(chartModel, columnCounter), chartModel.getSeries(lineCalc.getRelatedSeriesIndex()), category);
	    }
	}

	return new Pair<CategoryDataset, CategoryDataset>(mainDataset, lineDataset);
    }

    private CategoryPlot createPlot() {

	final FixedCategoryAxis domainAxis = new FixedCategoryAxis(domainAxisName);
	/*                    */domainAxis.setCategoryLabelPositions(CategoryLabelPositions.UP_90);

	final NumberAxis valueAxis = new NumberAxis(valueAxisName);
	/*             */valueAxis.setUpperMargin(0.1);

	final Pair<CategoryDataset, CategoryDataset> datasets = createDatasets();
	final CategoryItemRenderer barRenderer = createBarRenderer();

	final CategoryPlot plot = new CategoryPlot();
	/*               */plot.setDataset(0, datasets.getKey());
	/*               */plot.setRenderer(0, barRenderer);
	/*               */plot.setRangeAxis(valueAxis);
	/*               */plot.setDomainAxis(domainAxis);
	/*               */plot.setDomainAxisLocation(AxisLocation.BOTTOM_OR_RIGHT);
	if(datasets.getValue() != null){
	    plot.setDataset(1, datasets.getValue());
	    plot.setRenderer(1, createLineRenderer());
	}

	initToolTips(plot);
	return plot;
    }

    private CategoryItemRenderer createLineRenderer(){
	final LineAndShapeRenderer lineRenderer = new LineAndShapeRenderer();
	for(int lineIndex = 0; lineIndex < lineCalculators.size(); lineIndex++){
	    lineRenderer.setSeriesPaint(lineIndex, lineCalculators.get(lineIndex).getValue());
	}
	lineRenderer.setBaseShapesVisible(true);
	lineRenderer.setDrawOutlines(true);
	lineRenderer.setUseFillPaint(true);
	lineRenderer.setBaseFillPaint(Color.white);
	lineRenderer.setBaseItemLabelGenerator(new StandardCategoryItemLabelGenerator());
	return lineRenderer;
    }

    private CategoryItemRenderer createBarRenderer() {
	final CategoryItemRenderer renderer = new BarRenderer();
	renderer.setSeriesPaint(0, color);
	renderer.setBaseItemLabelGenerator(new StandardCategoryItemLabelGenerator());

	return renderer;
    }

    private void initToolTips(final CategoryPlot categoryPlot) {
	for(int renderIndex = 0; renderIndex < categoryPlot.getRendererCount(); renderIndex++){
	    categoryPlot.getRenderer(renderIndex).setBaseToolTipGenerator(new CategoryToolTipGenerator() {
		private final NumberFormat numberFormat = NumberFormat.getInstance();

		@Override
		public String generateToolTip(final CategoryDataset dataset, final int row, final int column) {
		    final String toolTip = "<html>" + numberFormat.format(dataset.getValue(row, column)) + " (" + dataset.getRowKey(row).toString() + ")<br>" //
		    + "<b>" + getKeyFor(dataset, column) + "</b><br>"//
		    + "<i>" + getDescFor(dataset, column) + "</i></html>";
		    return toolTip;
		}

	    });
	}
    }

    private String getKeyFor(final CategoryDataset dataset, final int column) {
	return ((EntityWrapper) dataset.getColumnKey(column)).toString();
    }

    private String getDescFor(final CategoryDataset dataset, final int column) {
	return ((EntityWrapper) dataset.getColumnKey(column)).getDesc();
    }

    public String getChartName() {
	return chartName;
    }
}
