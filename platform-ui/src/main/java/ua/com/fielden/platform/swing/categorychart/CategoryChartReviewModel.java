package ua.com.fielden.platform.swing.categorychart;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.swing.SortOrder;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;

import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.LegendItemCollection;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.entity.CategoryItemEntity;
import org.jfree.chart.entity.CategoryLabelEntity;
import org.jfree.chart.entity.ChartEntity;
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
import org.jfree.chart.renderer.category.GroupedStackedBarRenderer;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.chart.renderer.category.StackedBarRenderer;
import org.jfree.data.KeyToGroupMap;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.ui.TextAnchor;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.equery.AnalysisPropertyAggregationFunction;
import ua.com.fielden.platform.equery.EntityAggregates;
import ua.com.fielden.platform.equery.lifecycle.IProgressUpdater;
import ua.com.fielden.platform.reportquery.IAggregatedProperty;
import ua.com.fielden.platform.reportquery.ICategoryChartEntryModel;
import ua.com.fielden.platform.reportquery.IDistributedProperty;
import ua.com.fielden.platform.swing.analysis.DetailsFrame;
import ua.com.fielden.platform.swing.analysis.IAnalysisReportPersistentObject;
import ua.com.fielden.platform.swing.chartscroll.ScrollableCategoryPlot;
import ua.com.fielden.platform.swing.groupanalysis.GroupAnalysisModel;
import ua.com.fielden.platform.swing.review.AnalysisPersistentObject;
import ua.com.fielden.platform.swing.review.EntityQueryCriteria;
import ua.com.fielden.platform.swing.review.EntityReviewModel;
import ua.com.fielden.platform.swing.review.analysis.AnalysisReportQueryCriteriaExtender;
import ua.com.fielden.platform.utils.Pair;

/**
 * The Model that view the result of the {@link AggregationQueryCriteria} query in graphic way.
 * 
 * @author oleh
 * 
 */
public class CategoryChartReviewModel<T extends AbstractEntity, DAO extends IEntityDao<T>> extends GroupAnalysisModel<T, DAO> {

    private final AnalysisReportQueryCriteriaExtender<T, DAO> analysisReportQuery;
    /**
     * The {@link ICategoryChartEntryModel} implementation.
     */
    private final AggregationQueryDataModel<T, DAO> aggregationModel;

    /**
     * Are used to specify the number of visible columns at once.
     */
    private final SpinnerNumberModel spinnerModel;

    /**
     * Instantiates the {@link CategoryChartReviewModel} instance with {@link AggregationQueryCriteria} and after run actions
     * 
     * @param criteria
     * 
     * @param afterRunActions
     */
    public CategoryChartReviewModel(final EntityReviewModel<T, DAO, ? extends EntityQueryCriteria<T, DAO>> centerModel, final Map<String, Map<Object, DetailsFrame>> detailsFrame, final IAnalysisReportPersistentObject persistentObject, final String name, final String reportName) {
	super(centerModel, detailsFrame, name, reportName);
	this.analysisReportQuery = new AnalysisReportQueryCriteriaExtender<T, DAO>();
	this.analysisReportQuery.setBaseCriteria(centerModel.getCriteria());
	this.analysisReportQuery.setAggregationProperties(new ArrayList<IAggregatedProperty>());
	this.analysisReportQuery.setDistributionProperty(null);
	this.analysisReportQuery.setSortOrder(SortOrder.UNSORTED);
	this.analysisReportQuery.setSortingProperty(null);
	this.aggregationModel = new AggregationQueryDataModel<T, DAO>(analysisReportQuery);
	this.spinnerModel = new SpinnerNumberModel(persistentObject instanceof AnalysisPersistentObject ? ((AnalysisPersistentObject) persistentObject).getVisibleCategoriesCount()
		: AnalysisPersistentObject.DEFAULT_CATEGORY_COUNT, null, null, 1);
    }

    protected final AnalysisReportQueryCriteriaExtender<T, DAO> getAnalysisReportQuery() {
	return analysisReportQuery;
    }

    /**
     * Returns the {@link SpinnerModel} that is used to specify the number of visible columns at once.
     */
    public SpinnerNumberModel getColumnCountSpinnerModel() {
	return spinnerModel;
    }

    /**
     * 
     * 
     * @return
     */
    public int getVisibleCategoryCount() {
	return ((Integer) spinnerModel.getValue()).intValue();
    }

    /**
     * Returns the {@link IChartFactory} instance that builds chart for specified series indexes.
     * 
     * @return
     */
    public IChartFactory<List<EntityAggregates>, CategoryChartTypes> getChartFactory(final boolean all, final int... seriesIndexes) {
	return new CategoryChartFactory<T, DAO>(analysisReportQuery, aggregationModel, all, seriesIndexes);
    }

    void updateCriteria(final IDistributedProperty selectedDistributionProperty, final List<IAggregatedProperty> selectedAggregatedProperty, final Pair<IAggregatedProperty, SortOrder> selectedSorter)
    throws IllegalStateException {
	final IDistributedProperty distributionProperty = selectedDistributionProperty;
	if (distributionProperty == null) {
	    throw new IllegalStateException("Please choose distribution property");
	}
	analysisReportQuery.setDistributionProperty(distributionProperty);
	final List<IAggregatedProperty> properties = selectedAggregatedProperty;
	if (properties == null || properties.size() == 0) {
	    throw new IllegalStateException("Please choose aggregation property");
	}
	analysisReportQuery.setAggregationProperties(properties);
	analysisReportQuery.setSortingProperty(selectedSorter.getKey());
	analysisReportQuery.setSortOrder(selectedSorter.getValue());
    }

    /**
     * Returns the number of series in the group.
     * 
     * @return
     */
    public int getAggregationsSize(final CategoryChartTypes type) {
	int size = 0;
	int countSize = 0;
	for (final IAggregatedProperty aggregationProperty : analysisReportQuery.getAggregationProperties()) {
	    if (AnalysisPropertyAggregationFunction.DISTINCT_COUNT.equals(aggregationProperty.getAggregationFunction())) {
		countSize++;
	    } else {
		size++;
	    }
	}
	switch (type) {
	case BAR_CHART:
	    return size + countSize;
	case STACKED_BAR_CHART:
	    return (size > 0 ? 1 : size) + countSize;
	case LINE_CHART:
	    return size > 0 ? 1 : countSize > 0 ? 1 : 0;
	}
	return 0;
    }

    /**
     * Returns previously loaded data
     * 
     * @return
     */
    protected List<EntityAggregates> getLoadedData() {
	return aggregationModel.getModel();
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

    private static class CategoryChartFactory<T extends AbstractEntity, DAO extends IEntityDao<T>> implements IChartFactory<List<EntityAggregates>, CategoryChartTypes> {

	private static final CommonCategoryRenderer commonRenderer = new CommonCategoryRenderer();

	private final String valueAxisLabel = "values", countAxisLabel = "Count values";

	private final AggregationQueryDataModel<T, DAO> chartEntryModel;

	private final List<Integer> seriesIndexes = new ArrayList<Integer>();

	private final AnalysisReportQueryCriteriaExtender<T, DAO> criteria;

	private DefaultCategoryDataset mainDataSet, countDataSet;

	public CategoryChartFactory(final AnalysisReportQueryCriteriaExtender<T, DAO> criteria, final AggregationQueryDataModel<T, DAO> chartEntryModel, final boolean all, final int... indexes) {
	    this.chartEntryModel = chartEntryModel;
	    this.criteria = criteria;
	    setModel(chartEntryModel.getModel(), all, indexes);
	}

	@Override
	public void initDatasets(final CategoryChartTypes chartType, final IProgressUpdater progressUpdater) {
	    if (containsCount(true)) {
		mainDataSet = null;
	    } else {
		mainDataSet = new DefaultCategoryDataset();
	    }
	    if (containsCount(false)) {
		countDataSet = new DefaultCategoryDataset();
	    } else {
		countDataSet = null;
	    }
	    final int columnCount = chartEntryModel.getCategoryCount();
	    for (int rowCounter = 0; rowCounter < seriesIndexes.size(); rowCounter++) {
		final IAggregatedProperty aggregationProperty = criteria.getAggregationProperties().get(seriesIndexes.get(rowCounter));
		final Comparable<?> series = chartEntryModel.getSeries(seriesIndexes.get(rowCounter));
		for (int columnCounter = 0; columnCounter < columnCount; columnCounter++) {
		    final Comparable<?> category = chartEntryModel.getCategory(columnCounter);
		    if (aggregationProperty.getAggregationFunction().isDifferentValueAxis()) {
			if (mainDataSet != null) {
			    mainDataSet.addValue(null, series, category);
			}
			countDataSet.addValue(chartEntryModel.getValue(seriesIndexes.get(rowCounter), columnCounter), series, category);
		    } else {
			mainDataSet.addValue(chartEntryModel.getValue(seriesIndexes.get(rowCounter), columnCounter), series, category);
			if (countDataSet != null) {
			    countDataSet.addValue(null, series, category);
			}
		    }
		}
	    }
	}

	private boolean containsCount(final boolean containsAll) {
	    for (int rowCounter = 0; rowCounter < seriesIndexes.size(); rowCounter++) {
		final IAggregatedProperty aggregationProperty = criteria.getAggregationProperties().get(seriesIndexes.get(rowCounter));
		if (aggregationProperty.getAggregationFunction().isDifferentValueAxis()) {
		    if (!containsAll) {
			return true;
		    }
		} else if (containsAll) {
		    return false;
		}
	    }
	    return containsAll;
	}

	private List<Integer> getCountSeriesIndeces(final boolean count) {
	    final List<Integer> countSeries = new ArrayList<Integer>();
	    for (int rowCounter = 0; rowCounter < seriesIndexes.size(); rowCounter++) {
		final IAggregatedProperty aggregationProperty = criteria.getAggregationProperties().get(seriesIndexes.get(rowCounter));
		if (aggregationProperty.getAggregationFunction().isDifferentValueAxis() && count) {
		    countSeries.add(seriesIndexes.get(rowCounter));
		} else if (!aggregationProperty.getAggregationFunction().isDifferentValueAxis() && !count) {
		    countSeries.add(seriesIndexes.get(rowCounter));
		}
	    }
	    return countSeries;
	}

	@Override
	public List<JFreeChart> getCharts(final CategoryChartTypes type) {
	    return Arrays.asList(new JFreeChart("", createPlot(type)));
	}

	private CategoryPlot createPlot(final CategoryChartTypes type) {
	    CategoryPlot plot = null;
	    final CategoryItemRenderer renderer = createCategoryRenderer(type);
	    final FixedCategoryAxis domainAxis = new FixedCategoryAxis(criteria.getDistributionProperty() != null ? criteria.getDistributionProperty().toString() : "Categories");
	    domainAxis.setCategoryLabelPositions(CategoryLabelPositions.UP_90);
	    if (mainDataSet == null || countDataSet == null) {
		String rangeAxisName = valueAxisLabel;
		CategoryDataset dataSet = new DefaultCategoryDataset();
		if (mainDataSet != null) {
		    dataSet = mainDataSet;
		} else if (countDataSet != null) {
		    rangeAxisName = countAxisLabel;
		    dataSet = countDataSet;
		}
		if (seriesIndexes.size() == 1 && criteria.getAggregationProperties() != null && criteria.getAggregationProperties().size() > 0) {
		    rangeAxisName = criteria.getAggregationProperties().get(seriesIndexes.get(0)).toString();
		}
		final NumberAxis valueAxis = new NumberAxis(rangeAxisName);
		valueAxis.setUpperMargin(0.1);
		plot = new ScrollableCategoryPlot();
		plot.setDataset(0, dataSet);
		plot.setRangeAxis(0, valueAxis);
		plot.mapDatasetToRangeAxis(0, 0);
		plot.setRenderer(0, renderer);
	    } else {
		plot = new ScrollableCategoryPlot() {

		    private static final long serialVersionUID = 4983105241794357184L;

		    /**
		     * Override the getLegendItems() method to handle special case.
		     * 
		     * @return the legend items.
		     */
		    @Override
		    public LegendItemCollection getLegendItems() {

			return getRenderer().getLegendItems();

		    }
		};
		final List<Integer> otherSeries = getCountSeriesIndeces(false);
		final NumberAxis valueAxis = new NumberAxis(otherSeries.size() == 1 ? criteria.getAggregationProperties().get(otherSeries.get(0)).toString() : valueAxisLabel);
		final List<Integer> countSeries = getCountSeriesIndeces(true);
		final NumberAxis countAxis = new NumberAxis(countSeries.size() == 1 ? criteria.getAggregationProperties().get(countSeries.get(0)).toString() : countAxisLabel);
		valueAxis.setUpperMargin(0.1);
		countAxis.setUpperMargin(0.1);
		plot.setDataset(0, mainDataSet);
		plot.setDataset(1, countDataSet);
		plot.setRangeAxis(0, valueAxis);
		plot.setRangeAxis(1, countAxis);
		plot.setRenderer(0, renderer);
		plot.setRenderer(1, renderer);
		plot.mapDatasetToRangeAxis(0, 0);
		plot.mapDatasetToRangeAxis(1, 1);
	    }
	    plot.setDomainAxis(domainAxis);
	    plot.setDomainAxisLocation(AxisLocation.BOTTOM_OR_RIGHT);
	    initToolTips(plot);
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
		};
		break;
	    case STACKED_BAR_CHART:
		if (mainDataSet != null && countDataSet != null) {
		    final GroupedStackedBarRenderer groupBarRenderer = new GroupedStackedBarRenderer() {

			private static final long serialVersionUID = -1415233380188782824L;

			@Override
			public Paint lookupSeriesPaint(final int series) {
			    return commonRenderer.lookupSeriesPaint(Collections.unmodifiableList(seriesIndexes), series);
			}

		    };
		    final KeyToGroupMap map = createGroupMap();
		    groupBarRenderer.setSeriesToGroupMap(map);
		    groupBarRenderer.setPositiveItemLabelPositionFallback(new ItemLabelPosition(ItemLabelAnchor.OUTSIDE12, TextAnchor.BOTTOM_CENTER));
		    groupBarRenderer.setNegativeItemLabelPositionFallback(new ItemLabelPosition(ItemLabelAnchor.OUTSIDE6, TextAnchor.TOP_CENTER));
		    renderer = groupBarRenderer;
		} else if (mainDataSet != null && countDataSet == null) {
		    final StackedBarRenderer stackedBarRenderer = new StackedBarRenderer() {

			private static final long serialVersionUID = 461865474130896818L;

			@Override
			public Paint lookupSeriesPaint(final int series) {
			    return commonRenderer.lookupSeriesPaint(Collections.unmodifiableList(seriesIndexes), series);
			}

		    };
		    stackedBarRenderer.setPositiveItemLabelPositionFallback(new ItemLabelPosition(ItemLabelAnchor.OUTSIDE12, TextAnchor.BOTTOM_CENTER));
		    stackedBarRenderer.setNegativeItemLabelPositionFallback(new ItemLabelPosition(ItemLabelAnchor.OUTSIDE6, TextAnchor.TOP_CENTER));
		    renderer = stackedBarRenderer;
		} else {
		    renderer = new BarRenderer() {

			private static final long serialVersionUID = 1698833805267510547L;

			@Override
			public Paint lookupSeriesPaint(final int series) {
			    return commonRenderer.lookupSeriesPaint(Collections.unmodifiableList(seriesIndexes), series);
			}

		    };
		}
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

	/**
	 * Creates map to group series of the stacked bar chart.
	 */
	private KeyToGroupMap createGroupMap() {
	    final String groupKey = "Group_";
	    int groupCount = 0;
	    final KeyToGroupMap map = new KeyToGroupMap(groupKey + 0);
	    for (int rowCounter = 0; rowCounter < seriesIndexes.size(); rowCounter++) {
		final IAggregatedProperty aggregationProperty = criteria.getAggregationProperties().get(seriesIndexes.get(rowCounter));
		if (!aggregationProperty.getAggregationFunction().isDifferentValueAxis()) {
		    map.mapKeyToGroup(chartEntryModel.getSeries(seriesIndexes.get(rowCounter)), groupKey + 0);
		} else {
		    map.mapKeyToGroup(chartEntryModel.getSeries(seriesIndexes.get(rowCounter)), groupKey + (++groupCount));
		}
	    }
	    return map;
	}

	private void initToolTips(final CategoryPlot categoryPlot) {
	    categoryPlot.getRenderer().setBaseToolTipGenerator(new CategoryToolTipGenerator() {
		private final NumberFormat numberFormat = NumberFormat.getInstance();

		@Override
		public String generateToolTip(final CategoryDataset dataset, final int row, final int column) {
		    final String toolTip = "<html>" + numberFormat.format(dataset.getValue(row, column)) + " (" + dataset.getRowKey(row).toString() + ")<br>" //
		    + "<b>" + getKeyFor(dataset, row, column) + "</b><br>"//
		    + "<i>" + getDescFor(dataset, row, column) + "</i></html>";
		    return toolTip;
		}

	    });
	}

	@Override
	public void setModel(final List<EntityAggregates> model, final boolean all, final int... indexes) {
	    chartEntryModel.setModel(model);
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
	public List<EntityAggregates> getModel() {
	    return chartEntryModel.getModel();
	}
    }

    @Override
    public Object runAnalysisQuery(final int pageSize) {
	return analysisReportQuery.runExtendedQuery(pageSize);
    }

    public List<IAggregatedProperty> getSelectedAggregationProperties() {
	return new ArrayList<IAggregatedProperty>(analysisReportQuery.getAggregationProperties());
    }

    public IDistributedProperty getSelectedDistributionProeprty() {
	return analysisReportQuery.getDistributionProperty();
    }

    public Pair<IAggregatedProperty, SortOrder> getSortingParameter() {
	return new Pair<IAggregatedProperty, SortOrder>(analysisReportQuery.getSortingProperty(), analysisReportQuery.getSortOrder());
    }

    @Override
    public void runDoubleClickAction(final AnalysisDoubleClickEvent doubleClickEvent) {
	final ChartMouseEvent chartEvent = (ChartMouseEvent) doubleClickEvent.getSourceMouseEvent();
	final ChartEntity entity = chartEvent.getEntity();
	if (entity instanceof CategoryItemEntity) {
	    createDoubleClickAction(createChoosenItem(((CategoryItemEntity) entity).getColumnKey())).actionPerformed(null);
	} else if (entity instanceof CategoryLabelEntity) {
	    createDoubleClickAction(createChoosenItem(((CategoryLabelEntity) entity).getKey())).actionPerformed(null);
	}
    }

    private List<Pair<IDistributedProperty, Object>> createChoosenItem(final Comparable columnKey) {
	final EntityWrapper entityWrapper = (EntityWrapper) columnKey;
	final List<Pair<IDistributedProperty, Object>> choosenItems = new ArrayList<Pair<IDistributedProperty, Object>>();
	choosenItems.add(new Pair<IDistributedProperty, Object>(analysisReportQuery.getDistributionProperty(), entityWrapper.getEntity()));
	return choosenItems;
    }

}
