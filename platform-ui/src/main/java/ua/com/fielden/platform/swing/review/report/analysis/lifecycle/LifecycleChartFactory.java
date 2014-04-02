package ua.com.fielden.platform.swing.review.report.analysis.lifecycle;

import java.awt.Color;
import java.awt.Font;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.FieldPosition;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.LegendItem;
import org.jfree.chart.LegendItemCollection;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.SymbolAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.labels.ItemLabelAnchor;
import org.jfree.chart.labels.ItemLabelPosition;
import org.jfree.chart.labels.XYItemLabelGenerator;
import org.jfree.chart.labels.XYToolTipGenerator;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StackedXYBarRenderer;
import org.jfree.chart.renderer.xy.StandardXYBarPainter;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.data.gantt.Task;
import org.jfree.data.gantt.TaskSeries;
import org.jfree.data.gantt.TaskSeriesCollection;
import org.jfree.data.gantt.XYTaskDataset;
import org.jfree.data.general.SeriesException;
import org.jfree.data.time.SimpleTimePeriod;
import org.jfree.data.time.TimePeriodValue;
import org.jfree.data.time.TimePeriodValues;
import org.jfree.data.time.TimePeriodValuesCollection;
import org.jfree.data.xy.DefaultTableXYDataset;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.ui.TextAnchor;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.Period;

import ua.com.fielden.platform.domaintree.centre.IOrderingRepresentation.Ordering;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.equery.lifecycle.EntityPropertyLifecycle;
import ua.com.fielden.platform.equery.lifecycle.IGroup;
import ua.com.fielden.platform.equery.lifecycle.IProgressUpdater;
import ua.com.fielden.platform.equery.lifecycle.LifecycleModel;
import ua.com.fielden.platform.equery.lifecycle.ValuedInterval;
import ua.com.fielden.platform.reflection.TitlesDescsGetter;
import ua.com.fielden.platform.swing.categorychart.CategoryChartTypes;
import ua.com.fielden.platform.swing.categorychart.IChartFactory;
import ua.com.fielden.platform.swing.timeline.ColoredTask;
import ua.com.fielden.platform.swing.timeline.TimePeriodValueWithInfo;
import ua.com.fielden.platform.types.ICategory;
import ua.com.fielden.platform.utils.EntityUtils;
import ua.com.fielden.platform.utils.Pair;

/**
 * Lifecycle charts creation factory.
 * 
 * @author Jhou
 * 
 * @param <T>
 */
public class LifecycleChartFactory<T extends AbstractEntity<?>> implements IChartFactory<LifecycleModel<T>, CategoryChartTypes> {
    protected static final String timelineAxisLabel = "Timeline", entitiesAxisLabel = "Entities", avalabilityCountAxisLabel = "Count", fractionsAxisLabel = "fractions";
    protected static final Shape calcValueShape = new Ellipse2D.Double(0, 0, 10, 10), categoryShape = new Rectangle(10, 10);

    protected static final NumberFormat percentageFormat = createPercentageFormat();
    private final Map<String, List<? extends IGroup<T>>> cache = new HashMap<String, List<? extends IGroup<T>>>();

    private final Logger logger = Logger.getLogger(getClass());

    protected final DateFormat timeDurationFormat = new DateFormat() {
        private static final long serialVersionUID = -3580158576738237649L;

        @Override
        public StringBuffer format(final Date date, final StringBuffer toAppendTo, final FieldPosition pos) {
            pos.setBeginIndex(0);
            pos.setEndIndex(0);
            final Long millis = date.getTime();
            return toAppendTo.append(/*"~" + */ValuedInterval.periodLengthStr(new Period(chartEntryModel == null ? new DateTime(0) : chartEntryModel.getLeftBoundary(), new Duration(millis)), true));
        }

        @Override
        public Date parse(final String source, final ParsePosition parsePosition) {
            return null;
        }
    };

    private LifecycleModel<T> chartEntryModel;

    private final LifecycleAnalysisModel<T> model;

    private TaskSeriesCollection mainDataSet;
    private TimePeriodValuesCollection availabilityDataSet;

    // "fractions" view related:
    private final Map<String, Info> columnMapping = new HashMap<String, Info>();
    private/*final */Map<String, XYSeries> seriesCollection = new HashMap<String, XYSeries>();
    private final List<ValuedInterval> catAndNonValues = new ArrayList<ValuedInterval>();
    private DefaultTableXYDataset percentageDataSet = new DefaultTableXYDataset();
    private List<? extends IGroup<T>> groups;

    private final Map<ICategory, XYSeries> seriesFulfilled = new LinkedHashMap<ICategory, XYSeries>();

    private LifecycleModel<T> currentModel = null;
    private Pair<ICategory, Ordering> currentOrdering = null;
    private List<String> currentCategoriesStrings = null;
    private String currentDistributionProperty = null;
    private Boolean currentTotalIndicator = null;

    private ICategory sortingCategory;
    private boolean noSorting;

    protected static class Info {
        private final Integer index;
        private BigDecimal normalMillis = EntityPropertyLifecycle.DEFAULT_ZERO, //
                selectedMillis = EntityPropertyLifecycle.DEFAULT_ZERO;

        public Info(final Integer index) {
            this.index = index;
        }

        public Integer getIndex() {
            return index;
        }

        public BigDecimal getNormalMillis() {
            return normalMillis;
        }

        public BigDecimal getSelectedMillis() {
            return selectedMillis;
        }

        public void setNormalMillis(final BigDecimal normalMillis) {
            this.normalMillis = normalMillis;
        }

        public void setSelectedMillis(final BigDecimal selectedMillis) {
            this.selectedMillis = selectedMillis;
        }

        @Override
        public String toString() {
            return "[info. i == " + index + ". " + normalMillis + " \\ " + selectedMillis + "]";
        }
    }

    public LifecycleChartFactory(final LifecycleAnalysisModel<T> model) {
        this.chartEntryModel = null;
        this.model = model;
    }

    private static NumberFormat createPercentageFormat() {
        final NumberFormat percentageFormat = NumberFormat.getPercentInstance();
        percentageFormat.setMinimumFractionDigits(1);
        percentageFormat.setMaximumFractionDigits(1);
        return percentageFormat;
    }

    @Override
    public void setModel(final LifecycleModel<T> model, final boolean all, final int... indexes) {
        chartEntryModel = model;
    }

    @Override
    public LifecycleModel<T> getModel() {
        return chartEntryModel;
    }

    protected static <T extends AbstractEntity<?>> TaskSeriesCollection createMainDataSet(final LifecycleModel<T> chartEntryModel) {
        final TaskSeriesCollection mainDataSet = new TaskSeriesCollection();
        if (chartEntryModel != null) {
            for (final EntityPropertyLifecycle<T> epl : chartEntryModel.getLifecycleData()) {
                final TaskSeries taskSeries = new TaskSeries(epl.getInfo(true));

                for (final ValuedInterval vi : epl.getIntervals()) {
                    taskSeries.add(new ColoredTask("ColoredTask", new SimpleTimePeriod(vi.getFrom().toDate(), vi.getTo().toDate()), vi.getColor(), vi.getInfo()));
                }
                mainDataSet.add(taskSeries);
            }
        }
        return mainDataSet;
    }

    public void initDatasets(final CategoryChartTypes type, final IProgressUpdater progressUpdater) {
        if (CategoryChartTypes.BAR_CHART.equals(type)) {
            mainDataSet = createMainDataSet(chartEntryModel);
        } else if (CategoryChartTypes.LINE_CHART.equals(type)) {
            availabilityDataSet = new TimePeriodValuesCollection();
            if (chartEntryModel != null) {
                final TimePeriodValues tpv = new TimePeriodValues("Summary availability");
                final List<ValuedInterval> summaryAvailability = chartEntryModel.getSummaryAvailability();
                for (final ValuedInterval interval : summaryAvailability) {
                    tpv.add(new TimePeriodValueWithInfo(new SimpleTimePeriod(interval.getFrom().toDate(), interval.getTo().toDate()), interval.getAvailability(), interval.getInfo()));
                }
                availabilityDataSet.addSeries(tpv);
            }
        } else if (CategoryChartTypes.STACKED_BAR_CHART.equals(type)) {
            initPercentageDataset(progressUpdater);
        }
    }

    private void updateCategoriesAndNonCatValues() {
        catAndNonValues.clear();
        // create categories/nonCatValue sequence based on pre-defined category order:
        catAndNonValues.addAll(chartEntryModel.getCategoriesInfoWithColors());
        catAndNonValues.addAll(chartEntryModel.getUncategorizedValuesWithColors());
    }

    private void updateSeriesByCatAndNonValues() {
        seriesCollection.clear();
        seriesCollection = new HashMap<String, XYSeries>();

        // create series based on categories :
        for (final ValuedInterval vi : catAndNonValues) {
            final ColoredXYSeries series = new ColoredXYSeries(vi.getInfo(), false, false);
            series.setDesc(vi.getDesc());
            series.setPaint(vi.getColor());
            seriesCollection.put(vi.getInfo(), series);
        }
    }

    private void updateEntityIndexing() {
        // create mapping between "entity" and its corresponding number.
        columnMapping.clear();
        Integer i = 0;
        noSorting = model.getOrdering() == null;
        sortingCategory = noSorting ? null : model.getOrdering().getKey();
        final List<? extends IGroup<T>> sortedByCategory = noSorting ? groups : sortByCategory(groups, sortingCategory, model.getOrdering().getValue());
        for (final IGroup<T> group : sortedByCategory) {
            columnMapping.put(group.getInfo(), new Info(i++));
        }
    }

    private void fillSeries(final IProgressUpdater progressUpdater) {
        int i = 0;
        // add millis durations to category/nonCatValue series into corresponding "entity":
        for (final IGroup<T> group : groups) {
            i++;
            progressUpdater.updateProgress("Filling..."
                    + EntityPropertyLifecycle.strPercentage(EntityPropertyLifecycle.roundedPercents(EntityPropertyLifecycle.divide(new BigDecimal(i).setScale(15), new BigDecimal(groups.size()).setScale(15)))));
            // use total category durations for time-distributed information and average for property distributed information.
            for (final ValuedInterval vi : group.getCategoryDurations(model.getTotal())) {
                // System.out.println("============" + new DateTime().getMillis());
                final XYSeries seriesForCategoryOrNonCategorizedValue = seriesCollection.get(vi.getInfo());
                // System.out.println("get series :" + new DateTime().getMillis());
                final Info entityInfo = columnMapping.get(group.getInfo());
                // System.out.println("get entity info :" + new DateTime().getMillis());
                final boolean isCategorySelected = model.getCurrentCategoriesStrings().contains(vi.getInfo());
                // System.out.println("get isCategorySelected :" + new DateTime().getMillis());
                if (isCategorySelected) {
                    entityInfo.setSelectedMillis(entityInfo.getSelectedMillis().add(vi.getSummary())); // add millis corresponding to selected categories into corresponding entity info.
                    if (vi.getNormal()) {
                        entityInfo.setNormalMillis(entityInfo.getNormalMillis().add(vi.getSummary())); // add millis corresponding to selected "normal" categories into corresponding entity info.
                    }
                }
                // System.out.println("update entity info :" + new DateTime().getMillis());
                try {
                    seriesForCategoryOrNonCategorizedValue.add(entityInfo.getIndex(), vi.getSummary());
                    // System.out.println("add series value :" + new DateTime().getMillis());
                } catch (final SeriesException se) {
                    try {
                        final Number value = seriesForCategoryOrNonCategorizedValue.getY(entityInfo.getIndex());
                        final String s = "\t\tChart index " + entityInfo.getIndex() + " for category [" + vi.getInfo() + "] has been already filled by value [" + value
                                + "]. Tried to fill with value [" + vi.getSummary() + "].";
                        logger.error(s);
                        System.err.println(s);
                    } catch (final Exception e) {
                        final String s = e.getMessage();
                        logger.error(s);
                        System.err.println(s);
                    }
                }

            }
        }
    }

    private void fillDataset() {
        seriesFulfilled.clear();
        percentageDataSet = new DefaultTableXYDataset(); // Note that dataset should be preferably recreated, not re-filled. The listeners and other stuff when communicating with new chart will be recreated. This will remove side-effects such as bad repainting etc.
        // add first "sort" series:
        if (!noSorting) {
            percentageDataSet.addSeries(seriesCollection.get(sortingCategory.getName()));

            seriesFulfilled.put(sortingCategory, seriesCollection.get(sortingCategory.getName()));
        }
        // add rest series:
        for (final ValuedInterval vi : catAndNonValues) {
            final boolean excludeSeries = !model.getCurrentCategoriesStrings().contains(vi.getInfo()) || (noSorting ? false : sortingCategory.getName().equals(vi.getInfo()));
            if (!excludeSeries) { // add only selected categories:
                final XYSeries series = seriesCollection.get(vi.getInfo());
                percentageDataSet.addSeries(series);

                seriesFulfilled.put(model.findCategoryByName(vi.getInfo()), series);
            }
        }
    }

    protected void updateGroups(final boolean total) {
    }

    private void stage(final String message, final IProgressUpdater progressUpdater, final Runnable stage) {
        final Long curr = new Date().getTime();
        stage.run();
        final String done = message + "done in " + (new Date().getTime() - curr) + "ms.";
        System.out.println(done);
    }

    //    private String getProperty() {
    //	final String dp = model.getDistributionProperty();
    //	// final String propertyName = dp.getActualProperty();
    //	for (final GroupingPeriods period : GroupingPeriods.values()) {
    //	    if (propertyName.equals(period.getPropertyName())) {
    //		// time distribution:
    //		return period;
    //	    }
    //	}
    //	return new ValueProperty(dp.getActualProperty(), dp.toString(), dp.getTooltip());
    //    }

    /**
     * Re-calculates percentage dataset. Returns true if dataset has been changed.
     * 
     * @return
     */
    protected boolean initPercentageDataset(final IProgressUpdater progressUpdater) {
        if (chartEntryModel != null) {
            chartEntryModel.setProgressUpdater(progressUpdater);
            if (lifecycleModelChanged()) {
                stage("cat/non-cat values creation...", progressUpdater, new Runnable() {
                    @Override
                    public void run() {
                        updateCategoriesAndNonCatValues();
                    }
                });
            }

            if (lifecycleModelChanged() || totalIndicatorChanged()) {
                stage("cache clearing...", progressUpdater, new Runnable() {
                    @Override
                    public void run() {
                        cache.clear();
                    }
                });
            }

            if (lifecycleModelChanged() || groupingChanged()) {
                stage("entities re-grouping...", progressUpdater, new Runnable() {
                    @Override
                    public void run() {
                        final String property = model.getDistributionProperty();
                        if (!cache.containsKey(property)) {
                            cache.put(property, chartEntryModel.groupBy(property));
                        }
                        groups = cache.get(property);
                    }
                });
            }

            final boolean changed = lifecycleModelChanged() || groupingChanged() || orderingChanged() || currentCategoriesChanged() || totalIndicatorChanged();
            if (changed) {
                stage("entities groups updating...", progressUpdater, new Runnable() {
                    @Override
                    public void run() {
                        updateGroups(model.getTotal());
                    }
                });
                stage("entity re-indexing...", progressUpdater, new Runnable() {
                    @Override
                    public void run() {
                        updateEntityIndexing();
                    }
                });
                stage("empty series creation...", progressUpdater, new Runnable() {
                    @Override
                    public void run() {
                        updateSeriesByCatAndNonValues();
                    }
                });
                stage("series filling...", progressUpdater, new Runnable() {
                    @Override
                    public void run() {
                        fillSeries(progressUpdater);
                    }
                });
                stage("dataset filling...", progressUpdater, new Runnable() {
                    @Override
                    public void run() {
                        fillDataset();
                    }
                });
            }
            // update current "configuration":
            currentModel = chartEntryModel;
            currentDistributionProperty = model.getDistributionProperty();
            currentOrdering = model.getOrdering();
            currentCategoriesStrings = model.getCurrentCategoriesStrings();
            currentTotalIndicator = model.getTotal();
            return changed;
        } else {
            return false;
        }
    }

    private boolean lifecycleModelChanged() {
        return currentModel != chartEntryModel;
    }

    private boolean groupingChanged() {
        return !EntityUtils.equalsEx(currentDistributionProperty, model.getDistributionProperty());
    }

    private boolean totalIndicatorChanged() {
        return !EntityUtils.equalsEx(currentTotalIndicator, model.getTotal());
    }

    private boolean orderingChanged() {
        return currentOrdering != model.getOrdering();
    }

    private boolean currentCategoriesChanged() {
        return !model.getCurrentCategoriesStrings().equals(currentCategoriesStrings);
    }

    /**
     * Sorts entity property lifecycle by its category duration.
     * 
     * @param unsorted
     * @param category
     * @return
     */
    private List<IGroup<T>> sortByCategory(final List<? extends IGroup<T>> unsorted, final ICategory category, final Ordering ordering) {
        final int index = model.allCategories().indexOf(category);
        final List<IGroup<T>> sorted = new ArrayList<IGroup<T>>(unsorted);
        Collections.sort(sorted, new Comparator<IGroup<T>>() {
            @Override
            public int compare(final IGroup<T> group1, final IGroup<T> group2) {
                final BigDecimal duration1 = group1.getCategoryDurations(model.getTotal()).get(index).getSummary();
                final BigDecimal duration2 = group2.getCategoryDurations(model.getTotal()).get(index).getSummary();
                return (Ordering.ASCENDING == ordering ? 1 : (Ordering.DESCENDING == ordering ? -1 : 0)) * duration1.compareTo(duration2);
            }
        });
        return sorted;
    }

    public static class ColoredXYSeries extends XYSeries {
        private static final long serialVersionUID = -8924544742595509358L;

        public ColoredXYSeries(final Comparable<?> key, final boolean autoSort, final boolean allowDuplicateXValues) {
            super(key, autoSort, allowDuplicateXValues);
        }

        private String desc;
        private Color paint;

        public Color getPaint() {
            return paint;
        }

        public void setPaint(final Color paint) {
            this.paint = paint;
        }

        public String getDesc() {
            return desc;
        }

        public void setDesc(final String desc) {
            this.desc = desc;
        }

        @Override
        public String toString() {
            return "ColoredXYSeries [" + getKey() + "]";
        }
    }

    private static <T extends AbstractEntity<?>> String[] createEntityCaptions(final LifecycleModel<T> model, final boolean withAvailability) {
        if (model == null) {
            return new String[0];
        }
        final String[] s = new String[model.getLifecycleData().size()];
        for (int i = 0; i < model.getLifecycleData().size(); i++) {
            s[i] = model.getLifecycleData().get(i).getInfo(withAvailability);
        }
        return s;
    }

    protected static String[] createEntityCaptions(final Map<String, Info> columnMapping) {
        final String[] s = new String[columnMapping.size()];
        for (final Entry<String, Info> entry : columnMapping.entrySet()) {
            s[entry.getValue().getIndex()] = entry.getKey();
        }
        return s;
    }

    protected static <T extends AbstractEntity<?>> JFreeChart createAvailabilityChart(final LifecycleModel<T> chartEntryModel, final TaskSeriesCollection mainDataSet) {
        // 1. axes initialization:
        final DateAxis dateAxis = new DateAxis(timelineAxisLabel);
        final Date l = chartEntryModel == null ? new DateTime(2008, 1, 2, 0, 0, 0, 0).toDate() : chartEntryModel.getLeftBoundary().toDate(), //
        r = chartEntryModel == null ? new DateTime(2009, 1, 2, 0, 0, 0, 0).toDate() : chartEntryModel.getRightBoundary().toDate();
        dateAxis.setRange(l, r);
        final SymbolAxis entitiesAxis = new SymbolAxis(entitiesAxisLabel, createEntityCaptions(chartEntryModel, true));
        // monospaced font for good label layout:
        entitiesAxis.setTickLabelFont(new Font(Font.MONOSPACED, entitiesAxis.getTickLabelFont().getStyle(), entitiesAxis.getTickLabelFont().getSize()));

        // 2. renderer initialization:
        final XYBarRenderer renderer = new XYBarRenderer() {
            private static final long serialVersionUID = -7141717471151835893L;

            @Override
            public Paint getItemPaint(final int row, final int column) {
                final Task task = mainDataSet.getSeries(row).get(column);
                if (task instanceof ColoredTask) {
                    return ((ColoredTask) task).getColor();
                }
                return Color.BLACK;
            }
        };
        renderer.setUseYInterval(true);
        renderer.setDrawBarOutline(false); // true
        renderer.setBarPainter(new StandardXYBarPainter());
        renderer.setShadowVisible(false);
        renderer.setBarAlignmentFactor(0.5);
        renderer.setBaseToolTipGenerator(new XYToolTipGenerator() {
            @Override
            public String generateToolTip(final XYDataset dataset, final int series, final int item) {
                final Task task = mainDataSet.getSeries(series).get(item);
                if (task instanceof ColoredTask) {
                    return ((ColoredTask) task).getInfo();
                }
                return null;
            }
        });

        // 3. dataset initialization:
        final XYTaskDataset dataset = new XYTaskDataset(mainDataSet);
        dataset.setSeriesWidth(1.0);

        // 4. plot initialization:
        final XYPlot plot = new XYPlot(dataset, entitiesAxis, dateAxis, renderer);
        plot.setOrientation(PlotOrientation.HORIZONTAL);
        // panning:
        plot.setDomainPannable(true);
        plot.setRangePannable(true);

        if (chartEntryModel != null) {
            // 5. legend initialization:
            plot.setFixedLegendItems(createLegendItemsForAvailabilityView(chartEntryModel));
        }

        final JFreeChart chart = new JFreeChart(plot); // "Lifecycle chart"
        return chart;
    }

    protected static <T extends AbstractEntity<?>> LegendItemCollection createLegendItemsForAvailabilityView(final LifecycleModel<T> chartEntryModel) {
        // legend based on reserved categories and uncategorized values :
        final LegendItemCollection chartLegend = new LegendItemCollection();
        chartLegend.add(new LegendItem("Average relative availability : " + EntityPropertyLifecycle.roundedPercents(chartEntryModel.getAverageRelativeAvailability()) + "%", null, null, null, calcValueShape, Color.DARK_GRAY));

        for (final ValuedInterval vi : chartEntryModel.getCategoriesInfoWithColors()) {
            chartLegend.add(new LegendItem(vi.getInfo(), null, null, null, categoryShape, vi.getColor()));
        }
        for (final ValuedInterval vi : chartEntryModel.getUncategorizedValuesWithColors()) {
            chartLegend.add(new LegendItem(vi.getInfo(), null, null, null, categoryShape, vi.getColor()));
        }
        return chartLegend;
    }

    private JFreeChart createFractionsChart() {
        final SymbolAxis entitiesAxis = new SymbolAxis(entitiesAxisLabel, createEntityCaptions(columnMapping));
        entitiesAxis.setVerticalTickLabels(true);
        final DateAxis timeDurationAxis = new DateAxis(fractionsAxisLabel);
        timeDurationAxis.setDateFormatOverride(timeDurationFormat);

        // average relative availability legend item:
        final LegendItemCollection extraItems = new LegendItemCollection();
        if (chartEntryModel != null) {
            // average relative availability calculation: (client-side operation)
            BigDecimal normalMillisSum = EntityPropertyLifecycle.DEFAULT_ZERO, //
            selectedMillisSum = EntityPropertyLifecycle.DEFAULT_ZERO;
            for (final Info info : columnMapping.values()) {
                // validate normal/selected millis:
                EntityPropertyLifecycle.divide(info.getNormalMillis(), info.getSelectedMillis());
                // if millis are successfully validated :
                normalMillisSum = normalMillisSum.add(info.getNormalMillis());
                selectedMillisSum = selectedMillisSum.add(info.getSelectedMillis());
            }
            final BigDecimal aver = EntityPropertyLifecycle.divide(normalMillisSum, selectedMillisSum);
            extraItems.add(new LegendItem("Average relative availability : " + percentageFormat.format(aver), null, null, null, calcValueShape, Color.DARK_GRAY));
        }

        final FractionsItemInformationGenerator generator = new FractionsItemInformationGenerator(groups) {
            @Override
            protected String valueRepresentation(final BigDecimal itemSummary) {
                return /*"~" + */ValuedInterval.periodLengthStr(new Period(getChartEntryModel().getLeftBoundary(), new Duration(itemSummary.longValue())), false);
            }
        };
        return createFractionsChart(entitiesAxis, timeDurationAxis, percentageDataSet, generator, generator, extraItems);
    }

    /**
     * Creates customizable chart for "fractions" view.
     * 
     * @param groupsAxis
     * @param valuesAxis
     * @param percentageDataSet
     * @param labelGenerator
     * @param tooltipGenerator
     * @param extraLegendItems
     * @return
     */
    protected static JFreeChart createFractionsChart(final ValueAxis groupsAxis, final ValueAxis valuesAxis, final DefaultTableXYDataset percentageDataSet, final XYItemLabelGenerator labelGenerator, final XYToolTipGenerator tooltipGenerator, final LegendItemCollection extraLegendItems) {
        // 2. renderer initialization:
        final StackedXYBarRenderer renderer = new StackedXYBarRenderer();
        for (int i = 0; i < percentageDataSet.getSeriesCount(); i++) { // colors of categories and non-categorized values:
            renderer.setSeriesPaint(i, ((ColoredXYSeries) percentageDataSet.getSeries(i)).getPaint(), true);
        }
        renderer.setBarPainter(new StandardXYBarPainter());
        renderer.setBaseItemLabelsVisible(true);
        renderer.setBasePositiveItemLabelPosition(new ItemLabelPosition(ItemLabelAnchor.CENTER, TextAnchor.HALF_ASCENT_CENTER));
        renderer.setDrawBarOutline(false);
        renderer.setShadowVisible(false);
        renderer.setBarAlignmentFactor(0.5);

        // 4. plot initialization:
        final XYPlot xyplot = new XYPlot(percentageDataSet, groupsAxis, valuesAxis, renderer);

        xyplot.setDomainPannable(true);
        xyplot.setRangePannable(true);

        if (extraLegendItems != null) {
            final LegendItemCollection allItems = new LegendItemCollection();
            allItems.addAll(extraLegendItems);
            allItems.addAll(xyplot.getLegendItems());
            xyplot.setFixedLegendItems(allItems);
        }
        final JFreeChart chart = new JFreeChart(xyplot);
        xyplot.getRenderer().setBaseItemLabelGenerator(labelGenerator);
        xyplot.getRenderer().setBaseToolTipGenerator(tooltipGenerator);
        return chart;
    }

    private JFreeChart createSummaryAvailabilityChart() {
        // 1. axes initialization:
        final DateAxis xAxis = new DateAxis(timelineAxisLabel);
        final NumberAxis yAxis = new NumberAxis(avalabilityCountAxisLabel);

        final Color availableColor = new Color(67, 130, 255);
        // 2. renderer initialization:
        final XYBarRenderer renderer = new XYBarRenderer() {
            private static final long serialVersionUID = 1L;

            @Override
            public Paint getItemPaint(final int row, final int column) {
                return availableColor;
            }
        };
        renderer.setDrawBarOutline(false); // true
        renderer.setBarPainter(new StandardXYBarPainter());
        renderer.setShadowVisible(false);
        renderer.setBaseToolTipGenerator(new XYToolTipGenerator() {
            @Override
            public String generateToolTip(final XYDataset dataset, final int series, final int item) {
                final TimePeriodValue tpv = availabilityDataSet.getSeries(series).getDataItem(item);
                if (tpv instanceof TimePeriodValueWithInfo) {
                    return ((TimePeriodValueWithInfo) tpv).getInfo();
                }
                return null;
            }
        });

        // 4. plot initialization:
        final XYPlot xyplot = new XYPlot(availabilityDataSet, xAxis, yAxis, renderer);
        xyplot.setDomainPannable(true);
        xyplot.setRangePannable(true);

        if (chartEntryModel != null) {
            // 5. legend initialization:
            // legend based on reserved categories and uncategorized values :
            final LegendItemCollection chartLegend = new LegendItemCollection();
            final Double roundedAvailableCount = Math.round(chartEntryModel.getAverageSummaryAvailability() * 100.0d) / 100.0;
            chartLegend.add(new LegendItem("Average summary availability : " + roundedAvailableCount, null, null, null, calcValueShape, Color.DARK_GRAY));
            chartLegend.add(new LegendItem("Maximum summary availability : " + chartEntryModel.getMaxSummaryAvailability(), null, null, null, calcValueShape, Color.DARK_GRAY));
            chartLegend.add(new LegendItem("Minimum summary availability : " + chartEntryModel.getMinSummaryAvailability(), null, null, null, calcValueShape, Color.DARK_GRAY));
            xyplot.setFixedLegendItems(chartLegend);
        }
        final JFreeChart chart = new JFreeChart(xyplot);
        return chart;
    }

    @Override
    public List<JFreeChart> getCharts(final CategoryChartTypes type) {
        if (CategoryChartTypes.BAR_CHART.equals(type)) { // "fragmentation" single chart
            return Arrays.asList(createAvailabilityChart(chartEntryModel, mainDataSet));
        } else if (CategoryChartTypes.LINE_CHART.equals(type)) { // "count of available" single chart
            return Arrays.asList(createSummaryAvailabilityChart());
        } else if (CategoryChartTypes.STACKED_BAR_CHART.equals(type)) { // "fractions" single chart. (count of charts could be > 1 in descendants)
            return Arrays.asList(createFractionsChart());
        } else {
            return null;
        }
    }

    protected static BigDecimal entityChosenCategoriesDurationInMillis(final DefaultTableXYDataset dataSet, final int item) {
        BigDecimal summaryMillis = EntityPropertyLifecycle.DEFAULT_ZERO;
        for (int i = 0; i < dataSet.getSeriesCount(); i++) {
            final ColoredXYSeries ser = ((ColoredXYSeries) dataSet.getSeries(i));
            summaryMillis = summaryMillis.add((BigDecimal) ser.getY(item));
        }
        return summaryMillis;
    }

    public List<? extends IGroup<T>> getGroups() {
        return groups;
    }

    protected Map<ICategory, XYSeries> getSeriesFulfilled() {
        return seriesFulfilled;
    }

    protected LifecycleModel<T> getChartEntryModel() {
        return chartEntryModel;
    }

    protected Map<String, Info> getColumnMapping() {
        return columnMapping;
    }

    protected ICategory getSortingCategory() {
        return sortingCategory;
    }

    /**
     * Tooltip/label generator for "fractions" chart items.
     * 
     * @author TG Team
     * 
     */
    protected abstract class FractionsItemInformationGenerator implements XYItemLabelGenerator, XYToolTipGenerator {
        private final List<? extends IGroup<T>> groups;

        public FractionsItemInformationGenerator(final List<? extends IGroup<T>> groups) {
            this.groups = groups;
        }

        protected Pair<BigDecimal, BigDecimal> pair(final DefaultTableXYDataset dataset, final int series, final int item) {
            final ColoredXYSeries ser = ((ColoredXYSeries) dataset.getSeries(series));
            final BigDecimal itemSummary = (BigDecimal) ser.getY(item), allSummary = entityChosenCategoriesDurationInMillis(dataset, item);
            final Pair<BigDecimal, BigDecimal> pair = new Pair<BigDecimal, BigDecimal>(itemSummary, allSummary);
            return pair;
        }

        protected String fractionRepresentation(final Pair<BigDecimal, BigDecimal> pair) {
            return percentageFormat.format(EntityPropertyLifecycle.divide(pair.getKey(), pair.getValue()));
        }

        @Override
        public String generateLabel(final XYDataset xyDataset, final int series, final int item) {
            final DefaultTableXYDataset dataset = (DefaultTableXYDataset) xyDataset;
            final Pair<BigDecimal, BigDecimal> pair = pair(dataset, series, item);
            // show percentage only in case of more than one chosen categories.
            return (dataset.getSeriesCount() > 1) ? //
            fractionRepresentation(pair)
                    : //
                    TitlesDescsGetter.removeHtml(valueRepresentation(pair.getKey()));
        }

        private final int n = 3;

        @Override
        public String generateToolTip(final XYDataset xyDataset, final int series, final int item) {
            final DefaultTableXYDataset dataset = (DefaultTableXYDataset) xyDataset;
            final Pair<BigDecimal, BigDecimal> pair = pair(dataset, series, item);
            final ColoredXYSeries ser = ((ColoredXYSeries) dataset.getSeries(series));
            if (item < groups.size()) {
                try {
                    final IGroup<T> foundGroup = groups.get(item);
                    final int groupSize = foundGroup.size();
                    return "<html>" //
                            + "<b>Category:</b><br>" //
                            + ser.getKey() + "<br>" + "<i>" + ser.getDesc()
                            + "</i><br>" //
                            + "<br>" //
                            + "<b>Group:</b><br>" //
                            + ValuedInterval.valueRepresentation(foundGroup.getValue(), foundGroup.getInfo())
                            + "<br>" //
                            + groupSize + " entities "
                            + ((groupSize > n) ? (foundGroup.getEntityKeys().subList(0, n).toString().replaceAll("]", "...]")) : foundGroup.getEntityKeys()) //
                            + "<br>" + "<br>" //
                            + "<b>Value:</b><br>" //
                            + "fraction = <b>" + fractionRepresentation(pair) + "</b><br>" //
                            + "average  = <b>" + valueRepresentation(average(pair.getKey(), foundGroup)) + "</b><br>" //
                            + "total    = <b>" + valueRepresentation(total(pair.getKey(), foundGroup)) + "</b>" //
                            + "</html>"; //
                } catch (final Exception e) {
                    e.printStackTrace();
                    // TODO : still some undesired exceptions appear, for now just ignore them.
                    return "";
                }
            } else {
                return "";
            }
        }

        protected BigDecimal average(final BigDecimal value, final IGroup<T> foundGroup) {
            return model.getTotal() ? EntityPropertyLifecycle.divide(value, new BigDecimal(foundGroup.size()).setScale(EntityPropertyLifecycle.DEFAULT_SCALE)) : value;
        }

        protected BigDecimal total(final BigDecimal value, final IGroup<T> foundGroup) {
            return model.getTotal() ? value : value.multiply(new BigDecimal(foundGroup.size()));
        }

        protected abstract String valueRepresentation(final BigDecimal itemSummary);
    }

    protected LifecycleAnalysisModel<T> getLifecycleAnalysisModel() {
        return model;
    }
}
