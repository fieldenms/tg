package ua.com.fielden.platform.swing.review.report.analysis.multipledec;

import java.awt.Color;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.AbstractCategoryItemLabelGenerator;
import org.jfree.chart.labels.CategoryItemLabelGenerator;
import org.jfree.chart.labels.CategoryToolTipGenerator;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.swing.categorychart.EntityWrapper;
import ua.com.fielden.platform.swing.categorychart.FixedCategoryAxis;
import ua.com.fielden.platform.swing.review.report.analysis.chart.ICategoryAnalysisDataProvider;
import ua.com.fielden.platform.utils.Pair;

public class DecModel<T extends AbstractEntity<?>> {

    private final ICategoryAnalysisDataProvider<Comparable<?>, Number, List<T>> chartModel;
    private final String series;
    private final Color color;
    private final String chartName;
    private final String domainAxisName;
    private final String valueAxisName;
    private final NumberFormat numberFormat;

    private final List<CalculatedNumber<T>> calcNumbers;

    private final List<Line<T>> lineCalculators;

    public DecModel(final ICategoryAnalysisDataProvider<Comparable<?>, Number, List<T>> chartModel, final String series, final Color color, final String chartName, final String domainAxisName, final String valueAxisName){
	this.chartModel = chartModel;
	this.series = series;
	this.color = color;
	this.chartName = chartName;
	this.domainAxisName = domainAxisName;
	this.valueAxisName = valueAxisName;

	this.numberFormat = new DecimalFormat("#,##0.00");
	numberFormat.setRoundingMode(RoundingMode.HALF_UP);

	this.calcNumbers = new ArrayList<CalculatedNumber<T>>();
	this.lineCalculators = new ArrayList<Line<T>>();
    }

    protected ICategoryAnalysisDataProvider<Comparable<?>, Number, List<T>> getChartModel() {
	return chartModel;
    }

    public DecModel<T> addLine(final ILineCalculator<T> lineCalculator, final Color color, final boolean labelVisible, final boolean shapesVisible, final String description){
	lineCalculators.add(new Line<T>(lineCalculator, color, shapesVisible, labelVisible, description));
	return this;
    }

    public DecModel<T> addCalculatedValue(final CalculatedNumber<T> number){
	calcNumbers.add(number);
	return this;
    }

    public int getCalcValuesNumber(){
	return calcNumbers.size();
    }

    public List<CalculatedNumber<T>> getCalcNumbers() {
	return Collections.unmodifiableList(calcNumbers);
    }

    public JFreeChart createChart() {
	return new JFreeChart(null, JFreeChart.DEFAULT_TITLE_FONT, createPlot(),  false);
    }

    private Pair<CategoryDataset, CategoryDataset> createDatasets() {
	final DefaultCategoryDataset mainDataset = new DefaultCategoryDataset();
	final String categoryName = chartModel.categoryProperties().size() != 1 ? null : chartModel.categoryProperties().get(0);

	for (int columnCounter = 0; columnCounter < chartModel.getCategoryDataEntryCount(); columnCounter++) {
	    final Comparable<?> category = new EntityWrapper(chartModel.getCategoryDataValue(columnCounter, categoryName));
	    mainDataset.addValue(chartModel.getAggregatedDataValue(columnCounter, series), series, category);
	}

	final DefaultCategoryDataset lineDataset = lineCalculators.size() == 0 ? null : new DefaultCategoryDataset();

	for(int lineIndex = 0; lineIndex < lineCalculators.size(); lineIndex++){
	    for (int columnCounter = 0; columnCounter < chartModel.getCategoryDataEntryCount(); columnCounter++) {
		final Comparable<?> category = new EntityWrapper(chartModel.getCategoryDataValue(columnCounter, categoryName));
		final ILineCalculator<T> lineCalc = lineCalculators.get(lineIndex).lineCalculator;
		lineDataset.addValue(lineCalc.calculateLineValue(chartModel, columnCounter), lineCalc.getRelatedSeries(), category);
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

	final List<IDescriptionRetriever> descriptionRetrievers = new ArrayList<IDescriptionRetriever>();
	/*				*/descriptionRetrievers.add(createBarChartDescriptionRetriever());
	/*				*/descriptionRetrievers.add(createLineChartDescriptionRetriever());
	initToolTips(plot, descriptionRetrievers);
	initItemLabels(plot);
	return plot;
    }

    private CategoryItemRenderer createLineRenderer(){
	final LineAndShapeRenderer lineRenderer = new LineAndShapeRenderer();
	for(int lineIndex = 0; lineIndex < lineCalculators.size(); lineIndex++){
	    lineRenderer.setSeriesPaint(lineIndex, lineCalculators.get(lineIndex).color);
	    if(!lineCalculators.get(lineIndex).labelVisible){
		lineRenderer.setSeriesItemLabelsVisible(lineIndex, false);
	    }
	    lineRenderer.setSeriesShapesVisible(lineIndex, lineCalculators.get(lineIndex).shapesVisible);
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

    private void initToolTips(final CategoryPlot categoryPlot, final List<IDescriptionRetriever> descriptionRetrievers) {
	for(int renderIndex = 0; renderIndex < categoryPlot.getRendererCount(); renderIndex++){
	    final IDescriptionRetriever descriptionRetriever = descriptionRetrievers.get(renderIndex);
	    categoryPlot.getRenderer(renderIndex).setBaseToolTipGenerator(new CategoryToolTipGenerator() {

		@Override
		public String generateToolTip(final CategoryDataset dataset, final int row, final int column) {
		    final String toolTip = "<html>" + numberFormat.format(dataset.getValue(row, column)) + " (" + descriptionRetriever.getDescription(dataset, row, column) + ")<br>" //
			    + "<b>" + getKeyFor(dataset, column) + "</b><br>"//
			    + "<i>" + getDescFor(dataset, column) + "</i></html>";
		    return toolTip;
		}

	    });
	}
    }

    private void initItemLabels(final CategoryPlot categoryPlot){
	for(int renderIndex = 0; renderIndex < categoryPlot.getRendererCount(); renderIndex++){
	    categoryPlot.getRenderer(renderIndex).setBaseItemLabelGenerator(new AnalysisChartLabelGenerator(numberFormat));
	}
    }

    private static class AnalysisChartLabelGenerator extends AbstractCategoryItemLabelGenerator implements CategoryItemLabelGenerator{

	private static final long serialVersionUID = -5658827075252974472L;

	protected AnalysisChartLabelGenerator(final NumberFormat formatter) {
	    super("", formatter);
	}

	@Override
	public String generateLabel(final CategoryDataset dataset, final int row, final int column) {
	    return getNumberFormat().format(dataset.getValue(row, column));
	}

    }

    private String getKeyFor(final CategoryDataset dataset, final int column) {
	return ((EntityWrapper) dataset.getColumnKey(column)).toString();
    }

    private String getDescFor(final CategoryDataset dataset, final int column) {
	return ((EntityWrapper) dataset.getColumnKey(column)).getDesc();
    }

    private IDescriptionRetriever createBarChartDescriptionRetriever() {
	return new IDescriptionRetriever() {

	    @Override
	    public String getDescription(final CategoryDataset dataset, final int row, final int column) {
		return dataset.getRowKey(row).toString();
	    }
	};
    }

    private IDescriptionRetriever createLineChartDescriptionRetriever() {
	return new IDescriptionRetriever() {

	    @Override
	    public String getDescription(final CategoryDataset dataset, final int row, final int column) {
		return lineCalculators.get(row).description;
	    }
	};
    }

    public String getChartName() {
	return chartName;
    }

    private static class Line<T extends AbstractEntity<?>>{

	public final boolean labelVisible;

	public final boolean shapesVisible;

	public final Color color;

	public final ILineCalculator<T> lineCalculator;

	public final String description;

	public Line(final ILineCalculator<T> lineCalculator, final Color color, final boolean shapesVisible, final boolean labelVisible, final String description){
	    this.lineCalculator = lineCalculator;
	    this.color = color;
	    this.labelVisible = labelVisible;
	    this.shapesVisible = shapesVisible;
	    this.description = description;
	}

    }

    /**
     * Contract that allows to retrieve description for category data set.
     *
     * @author TG Team
     *
     */
    private interface IDescriptionRetriever{

	/**
	 * Retrieves the description for the specified data set, row and column.
	 *
	 * @param dataset
	 * @param row
	 * @param column
	 * @return
	 */
	String getDescription(final CategoryDataset dataset, final int row, final int column);
    }
}
