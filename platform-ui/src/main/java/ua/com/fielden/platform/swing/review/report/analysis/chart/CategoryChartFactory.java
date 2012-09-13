package ua.com.fielden.platform.swing.review.report.analysis.chart;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.labels.AbstractCategoryItemLabelGenerator;
import org.jfree.chart.labels.CategoryItemLabelGenerator;
import org.jfree.chart.labels.CategorySeriesLabelGenerator;
import org.jfree.chart.labels.CategoryToolTipGenerator;
import org.jfree.chart.labels.ItemLabelAnchor;
import org.jfree.chart.labels.ItemLabelPosition;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.DefaultDrawingSupplier;
import org.jfree.chart.plot.DrawingSupplier;
import org.jfree.chart.renderer.category.AbstractCategoryItemRenderer;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.chart.renderer.category.CategoryItemRendererState;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.chart.renderer.category.StackedBarRenderer;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.ui.TextAnchor;

import ua.com.fielden.platform.domaintree.centre.analyses.ISentinelDomainTreeManager;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.equery.lifecycle.IProgressUpdater;
import ua.com.fielden.platform.javafx.dashboard.DashboardRow;
import ua.com.fielden.platform.reflection.development.EntityDescriptor;
import ua.com.fielden.platform.swing.categorychart.CategoryChartTypes;
import ua.com.fielden.platform.swing.categorychart.EntityWrapper;
import ua.com.fielden.platform.swing.categorychart.FixedCategoryAxis;
import ua.com.fielden.platform.swing.categorychart.IChartFactory;
import ua.com.fielden.platform.swing.chartscroll.ScrollableCategoryPlot;

//TODO this class should be removed later on.
class CategoryChartFactory<T extends AbstractEntity<?>> implements IChartFactory<List<T>, CategoryChartTypes> {

    private static final CommonCategoryRenderer commonRenderer = new CommonCategoryRenderer();

    private final String valueAxisLabel = "values";

    private final CategoryDataModel<T> chartEntryModel;

    private final List<Integer> seriesIndexes = new ArrayList<Integer>();

    private final ChartAnalysisModel<T> analysisModel;

    private final NumberFormat numberFormat;

    private DefaultCategoryDataset dataSet;

    public CategoryChartFactory(final ChartAnalysisModel<T> analysisModel, final boolean all, final int... indexes) {
	this.chartEntryModel = new CategoryDataModel<T>(analysisModel.getChartAnalysisDataProvider());
	this.analysisModel = analysisModel;
	this.numberFormat = new DecimalFormat("#,##0.00");
	numberFormat.setRoundingMode(RoundingMode.HALF_UP);
	setModel(null, all, indexes);
    }

    @Override
    public void initDatasets(final CategoryChartTypes chartType, final IProgressUpdater progressUpdater) {
	dataSet = new DefaultCategoryDataset();
	final int columnCount = chartEntryModel.getCategoryCount();
	for (int rowCounter = 0; rowCounter < seriesIndexes.size(); rowCounter++) {
	    final Comparable<?> series = chartEntryModel.getSeries(seriesIndexes.get(rowCounter));
	    for (int columnCounter = 0; columnCounter < columnCount; columnCounter++) {
		final Comparable<?> category = chartEntryModel.getCategory(columnCounter);
		dataSet.addValue(chartEntryModel.getValue(seriesIndexes.get(rowCounter), columnCounter), series, category);
	    }
	}
    }

    @Override
    public List<JFreeChart> getCharts(final CategoryChartTypes type) {
	return Arrays.asList(new JFreeChart("", createPlot(type)));
    }

    private CategoryPlot createPlot(final CategoryChartTypes type) {
	CategoryPlot plot = null;
	final ICategoryAnalysisDataProvider<Comparable<?>, Number, List<T>> dataProvider = analysisModel.getChartAnalysisDataProvider();
	final CategoryItemRenderer renderer = createCategoryRenderer(type);
	final Class<T> root = analysisModel.getCriteria().getEntityClass();
	final Class<T> managedType = analysisModel.getCriteria().getManagedType();
	final List<String> aggregationCheckedProperties =  analysisModel.adtme().getSecondTick().checkedProperties(root);
	final List<String> distributionCheckedProperties = analysisModel.adtme().getFirstTick().checkedProperties(root);
	final EntityDescriptor distributionDescriptor = new EntityDescriptor(managedType, distributionCheckedProperties);
	final EntityDescriptor aggregationDescriptor = new EntityDescriptor(managedType, aggregationCheckedProperties);
	final List<String> distributionProperty = dataProvider.categoryProperties();
	final FixedCategoryAxis domainAxis = new FixedCategoryAxis(distributionProperty.size() == 1 ? distributionDescriptor.getTitle(distributionProperty.get(0).toString()) : "Categories");
	domainAxis.setCategoryLabelPositions(CategoryLabelPositions.UP_90);
	if (dataSet != null) {
	    String rangeAxisName = valueAxisLabel;
	    if (seriesIndexes.size() == 1 && dataProvider.aggregatedProperties().size() > 0) {
		rangeAxisName = aggregationDescriptor.getTitle(dataProvider.aggregatedProperties().get(seriesIndexes.get(0)).toString());
	    }
	    final NumberAxis valueAxis = new NumberAxis(rangeAxisName);
	    valueAxis.setUpperMargin(0.1);
	    plot = new ScrollableCategoryPlot();
	    plot.setDataset(0, dataSet);
	    plot.setRangeAxis(0, valueAxis);
	    plot.mapDatasetToRangeAxis(0, 0);
	    plot.setRenderer(0, renderer);
	}
	plot.setDomainAxis(domainAxis);
	plot.setDomainAxisLocation(AxisLocation.BOTTOM_OR_RIGHT);
	initToolTips(plot, aggregationDescriptor);
	initItemLabels(plot);
	initLegendItemText(plot, aggregationDescriptor);
	return plot;
    }

    private CategoryItemRenderer createCategoryRenderer(final CategoryChartTypes type) {
	CategoryItemRenderer renderer = null;
	switch (type) {
	case BAR_CHART:
	    renderer = new BarRenderer() {

		private static final long serialVersionUID = -7328812945362517852L;

		@Override
		public Paint lookupSeriesPaint(final int series) {
		    return commonRenderer.lookupSeriesPaint(Collections.unmodifiableList(seriesIndexes), series);
		}

		@Override
		public Paint getItemPaint(final int row, final int column) {
		    if (analysisModel.adtme() instanceof ISentinelDomainTreeManager) {
			return getSentinelBarsColour(column);
		    }
		    return super.getItemPaint(row, column);
		}

	    };
	    break;
	case STACKED_BAR_CHART:
	    final StackedBarRenderer stackedBarRenderer = new StackedBarRenderer() {

		private static final long serialVersionUID = 461865474130896818L;

		@Override
		public Paint lookupSeriesPaint(final int series) {
		    return commonRenderer.lookupSeriesPaint(Collections.unmodifiableList(seriesIndexes), series);
		}

		@Override
		public Paint getItemPaint(final int row, final int column) {
		    if (analysisModel.adtme() instanceof ISentinelDomainTreeManager) {
			return getSentinelBarsColour(column);
		    }
		    return super.getItemPaint(row, column);
		}

	    };
	    stackedBarRenderer.setPositiveItemLabelPositionFallback(new ItemLabelPosition(ItemLabelAnchor.OUTSIDE12, TextAnchor.BOTTOM_CENTER));
	    stackedBarRenderer.setNegativeItemLabelPositionFallback(new ItemLabelPosition(ItemLabelAnchor.OUTSIDE6, TextAnchor.TOP_CENTER));
	    renderer = stackedBarRenderer;
	    break;
	case LINE_CHART:
	    final LineAndShapeRenderer lineRenderer = new LineAndShapeRenderer() {

		private static final long serialVersionUID = 8665032193251908969L;

		@Override
		public Paint lookupSeriesPaint(final int series) {
		    return commonRenderer.lookupSeriesPaint(Collections.unmodifiableList(seriesIndexes), series);
		}

		@Override
		public Shape lookupSeriesShape(final int series) {
		    return commonRenderer.lookupSeriesShape(Collections.unmodifiableList(seriesIndexes), series);
		}

	    };
	    lineRenderer.setBaseShapesVisible(true);
	    lineRenderer.setDrawOutlines(true);
	    lineRenderer.setUseFillPaint(true);
	    lineRenderer.setBaseFillPaint(Color.white);
	    renderer = lineRenderer;
	}
	if (renderer != null) {
	    renderer.setBaseItemLabelGenerator(new StandardCategoryItemLabelGenerator());
	}
	return renderer;
    }

    private void initToolTips(final CategoryPlot categoryPlot, final EntityDescriptor aggregationDescriptor) {
	categoryPlot.getRenderer().setBaseToolTipGenerator(new CategoryToolTipGenerator() {

	    @Override
	    public String generateToolTip(final CategoryDataset dataset, final int row, final int column) {
		final String toolTip = "<html>" + numberFormat.format(dataset.getValue(row, column)) + " (" + aggregationDescriptor.getTitle(dataset.getRowKey(row).toString()) + ")<br>" //
			+ "<b>" + getKeyFor(dataset, row, column) + "</b><br>"//
			+ "<i>" + getDescFor(dataset, row, column) + "</i></html>";
		return toolTip;
	    }

	});
    }

    private void initItemLabels(final CategoryPlot categoryPlot){
	categoryPlot.getRenderer().setBaseItemLabelGenerator(new AnalysisChartLabelGenerator(numberFormat));
    }

    private void initLegendItemText(final CategoryPlot categoryPlot, final EntityDescriptor aggregationDescriptor) {
	final AbstractCategoryItemRenderer renderer = (AbstractCategoryItemRenderer) categoryPlot.getRenderer();
	renderer.setLegendItemLabelGenerator(new CategorySeriesLabelGenerator() {

	    @Override
	    public String generateLabel(final CategoryDataset categoryDataset, final int row) {
		return aggregationDescriptor.getTitle(categoryDataset.getRowKey(row).toString());
	    }
	});
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

    @Override
    public void setModel(final List<T> model, final boolean all, final int... indexes) {
	seriesIndexes.clear();
	if (all) {
	    for (int seriesIndex = 0; seriesIndex < chartEntryModel.getSeriesCount(); seriesIndex++) {
		seriesIndexes.add(seriesIndex);
	    }
	} else {
	    for (int index = 0; index < indexes.length; index++) {
		final int value = indexes[index];
		if (value < 0 || value >= chartEntryModel.getSeriesCount()) {
		    //throw new IllegalArgumentException("The indexes array has index that is less then 0 or greater then series count.");
		} else {
		    seriesIndexes.add(value);
		}
	    }
	}
    }

    /**
     * Returns the key for the specified category item.
     *
     * @param dataset
     * @param row
     * @param column
     * @return
     */
    private String getKeyFor(final CategoryDataset dataset, final int row, final int column) {
	return ((EntityWrapper) dataset.getColumnKey(column)).toString();
    }

    /**
     * Returns description for the specified category item.
     *
     * @param dataset
     * @param row
     * @param column
     * @return
     */
    private String getDescFor(final CategoryDataset dataset, final int row, final int column) {
	return ((EntityWrapper) dataset.getColumnKey(column)).getDesc();
    }

    @Override
    public List<T> getModel() {
	return analysisModel.getChartAnalysisDataProvider().getLoadedData();
    }

    private Color getSentinelBarsColour(final int column) {
	final String status = ((EntityWrapper) dataSet.getColumnKey(column)).toString();
	final javafx.scene.paint.Color javafxColor = DashboardRow.getColour(status);
	return new java.awt.Color((float) javafxColor.getRed(), (float) javafxColor.getGreen(), (float) javafxColor.getBlue(), (float) javafxColor.getOpacity());
    }

    private static class CommonCategoryRenderer extends AbstractCategoryItemRenderer {

	private static final long serialVersionUID = -5374125078200966455L;

	private final DrawingSupplier supplier = new DefaultDrawingSupplier();

	@Override
	public void drawItem(final Graphics2D g2, final CategoryItemRendererState state, final Rectangle2D dataArea, final CategoryPlot plot, final CategoryAxis domainAxis, final ValueAxis rangeAxis, final CategoryDataset dataset, final int row, final int column, final int pass) {
	    //this won't be needed.
	}

	@Override
	public DrawingSupplier getDrawingSupplier() {
	    return supplier;
	}

	public Paint lookupSeriesPaint(final List<Integer> seriesIndexes, final int series) {
	    final int actualRow = (series < 0 || series >= seriesIndexes.size()) ? series : seriesIndexes.get(series);
	    return super.lookupSeriesPaint(actualRow);
	}

	public Shape lookupSeriesShape(final List<Integer> seriesIndexes, final int series) {
	    final int actualRow = (series < 0 || series >= seriesIndexes.size()) ? series : seriesIndexes.get(series);
	    return super.lookupSeriesShape(actualRow);
	}

    }
}
