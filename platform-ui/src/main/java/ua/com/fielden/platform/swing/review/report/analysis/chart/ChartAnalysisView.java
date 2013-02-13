package ua.com.fielden.platform.swing.review.report.analysis.chart;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.EtchedBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import net.miginfocom.swing.MigLayout;

import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.entity.CategoryItemEntity;
import org.jfree.chart.entity.CategoryLabelEntity;
import org.jfree.chart.entity.ChartEntity;

import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.centre.analyses.IAbstractAnalysisDomainTreeManager.IUsageManager.IPropertyUsageListener;
import ua.com.fielden.platform.domaintree.centre.analyses.IAnalysisDomainTreeManager;
import ua.com.fielden.platform.domaintree.centre.analyses.IAnalysisDomainTreeManager.IAnalysisAddToAggregationTickManager;
import ua.com.fielden.platform.domaintree.centre.analyses.IAnalysisDomainTreeManager.IAnalysisAddToDistributionTickManager;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.EntityAggregates;
import ua.com.fielden.platform.reflection.development.EntityDescriptor;
import ua.com.fielden.platform.reportquery.AnalysisModelChangedEvent;
import ua.com.fielden.platform.reportquery.AnalysisModelChangedListener;
import ua.com.fielden.platform.swing.categorychart.ActionChartPanel;
import ua.com.fielden.platform.swing.categorychart.AnalysisListDragFromSupport;
import ua.com.fielden.platform.swing.categorychart.AnalysisListDragToSupport;
import ua.com.fielden.platform.swing.categorychart.CategoryChartTypes;
import ua.com.fielden.platform.swing.categorychart.ChartAnalysisAggregationListDragToSupport;
import ua.com.fielden.platform.swing.categorychart.ChartPanelChangedEventObject;
import ua.com.fielden.platform.swing.categorychart.EntityWrapper;
import ua.com.fielden.platform.swing.categorychart.IChartPanelChangeListener;
import ua.com.fielden.platform.swing.categorychart.MultipleChartPanel;
import ua.com.fielden.platform.swing.categorychart.SwitchChartsModel;
import ua.com.fielden.platform.swing.chartscroll.CategoryChartScrollPanel;
import ua.com.fielden.platform.swing.checkboxlist.ListCheckingEvent;
import ua.com.fielden.platform.swing.checkboxlist.ListCheckingListener;
import ua.com.fielden.platform.swing.checkboxlist.ListCheckingModel;
import ua.com.fielden.platform.swing.checkboxlist.SortingCheckboxList;
import ua.com.fielden.platform.swing.checkboxlist.SortingCheckboxListCellRenderer;
import ua.com.fielden.platform.swing.components.blocking.BlockingIndefiniteProgressLayer;
import ua.com.fielden.platform.swing.components.blocking.IBlockingLayerProvider;
import ua.com.fielden.platform.swing.dnd.DnDSupport2;
import ua.com.fielden.platform.swing.review.details.AnalysisDetailsData;
import ua.com.fielden.platform.swing.review.report.analysis.chart.configuration.ChartAnalysisConfigurationView;
import ua.com.fielden.platform.swing.review.report.analysis.view.AbstractAnalysisReview;
import ua.com.fielden.platform.swing.review.report.analysis.view.AnalysisDataEvent;
import ua.com.fielden.platform.swing.review.report.analysis.view.DomainTreeListCheckingModel;
import ua.com.fielden.platform.swing.review.report.analysis.view.DomainTreeListSortingModel;
import ua.com.fielden.platform.swing.review.report.events.LoadEvent;
import ua.com.fielden.platform.swing.review.report.events.SelectionEvent;
import ua.com.fielden.platform.swing.review.report.interfaces.ILoadListener;
import ua.com.fielden.platform.swing.review.report.interfaces.ISelectionEventListener;
import ua.com.fielden.platform.swing.utils.DummyBuilder;
import ua.com.fielden.platform.utils.Pair;
import ua.com.fielden.platform.utils.ResourceLoader;

public class ChartAnalysisView<T extends AbstractEntity<?>> extends AbstractAnalysisReview<T, ICentreDomainTreeManagerAndEnhancer, IAnalysisDomainTreeManager> {

    private static final long serialVersionUID = -6505281133387254406L;

    /**
     * The list of available distribution properties.
     */
    private final JList<String> distributionList;
    /**
     * The list of available aggregation properties.
     */
    private final SortingCheckboxList<String> aggregationList;

    /**
     * The chart panel that holds one or more charts.
     */
    private final MultipleChartPanel<List<T>, CategoryChartTypes> chartPanel;
    /**
     * The panel that allows to scroll charts placed on the {@link #chartPanel}.
     */
    private final CategoryChartScrollPanel chartScroller;

    /**
     * Allows one to choose the number of categories to be visible at a time.
     */
    private final JSpinner spinner;
    /**
     * Allows one to switch between different types of charts.
     */
    private final SwitchChartsModel<List<T>, CategoryChartTypes> switchChartModel;
    /**
     * Tool bar that allows to configure charts: choose between different types of charts, choose the number of visible categories e. t. c.
     */
    private final JToolBar toolBar;

    /**
     * Determines whether to split chart in to different series or not.
     */
    private boolean split = false;

    public ChartAnalysisView(final ChartAnalysisModel<T> model, final ChartAnalysisConfigurationView<T> owner) {
	super(model, owner);
	this.chartPanel = new MultipleChartPanel<List<T>, CategoryChartTypes>();
	this.chartScroller = new CategoryChartScrollPanel(chartPanel, getModel().adtme().getVisibleDistributedValuesNumber());
	this.distributionList = createDistributionList();
	this.aggregationList = createAggregationList();
	this.spinner = createColumnCounterSpinner();
	this.switchChartModel = new SwitchChartsModel<List<T>, CategoryChartTypes>(chartPanel);
	this.toolBar = createChartToolBar();
	this.addSelectionEventListener(createChartAnalysisSelectionListener());
	updateChart(new ArrayList<T>(), null);
	layoutComponents();

	DnDSupport2.installDnDSupport(aggregationList, new AnalysisListDragFromSupport(aggregationList), //
		new ChartAnalysisAggregationListDragToSupport<T>(//
			getModel().getCriteria().getEntityClass(), //
			getModel().adtme().getSecondTick(), //
			aggregationList, chartPanel, getModel().getChartAnalysisDataProvider()), true);
	DnDSupport2.installDnDSupport(distributionList, new AnalysisListDragFromSupport(distributionList), //
		new AnalysisListDragToSupport<T>(distributionList, getModel().getCriteria().getEntityClass(), getModel().adtme().getFirstTick()), true);

	//Add the chart updater.
	model.getChartAnalysisDataProvider().addAnalysisModelChangedListener(createModelUpdaterListener());
    }

    @SuppressWarnings("unchecked")
    @Override
    public ChartAnalysisConfigurationView<T> getOwner() {
	return (ChartAnalysisConfigurationView<T>)super.getOwner();
    }

    @Override
    public ChartAnalysisModel<T> getModel() {
	return (ChartAnalysisModel<T>)super.getModel();
    }

    @Override
    protected void enableRelatedActions(final boolean enable, final boolean navigate) {
	if(getCentre().getCriteriaPanel() != null){
	    getCentre().getDefaultAction().setEnabled(enable);
	}
	if(!navigate){
	    getCentre().getPaginator().setEnableActions(enable, !enable);
	}
	getCentre().getRunAction().setEnabled(enable);
    }

    /**
     * Returns the page size (i.e. the number of {@link EntityAggregates}s to be retrieved at once).
     *
     * @return
     */
    final int getPageSize() {
	final int groupSize = getAggregationsSize();
	if (groupSize != 0) {
	    final int size = chartPanel.getSize().width / (20 * groupSize);
	    if (size < 1) {
		return 1;
	    } else {
		return size;
	    }
	}
	return 0;
    }

    /**
     * Returns the {@link AnalysisModelChangedListener} implementation that updates the chart according to the model changes.
     *
     * @return
     */
    private AnalysisModelChangedListener createModelUpdaterListener() {
        return new AnalysisModelChangedListener() {

            @Override
            public void cahrtModelChanged(final AnalysisModelChangedEvent event) {
        	updateChart(getModel().getChartAnalysisDataProvider().getLoadedData(), null);
            }
        };
    }

    /**
     * Returns the number of series in the group.
     *
     * @return
     */
    private int getAggregationsSize() {
	final int size = getModel().adtme().getSecondTick().usedProperties(getModel().getCriteria().getEntityClass()).size();
	return size > 0 ? size : 1;
    }

    /**
     * Returns the {@link ISelectionEventListener} that enables or disable appropriate actions when this analysis was selected.
     *
     * @return
     */
    private ISelectionEventListener createChartAnalysisSelectionListener() {
	return new ISelectionEventListener() {

	    @Override
	    public void viewWasSelected(final SelectionEvent event) {
		//Managing the default, design and custom action changer button enablements.
		getCentre().getDefaultAction().setEnabled(getCentre().getCriteriaPanel() != null);
		if (getCentre().getCriteriaPanel() != null && getCentre().getCriteriaPanel().canConfigure()) {
		    getCentre().getCriteriaPanel().getSwitchAction().setEnabled(true);
		}
		if (getCentre().getCustomActionChanger() != null) {
		    getCentre().getCustomActionChanger().setEnabled(true);
		}
		//Managing the paginator's enablements.
		getCentre().getPaginator().setEnableActions(true, false);
		//Managing load and export enablements.
		getCentre().getExportAction().setEnabled(false);
		getCentre().getRunAction().setEnabled(true);
	    }
	};
    }

    /**
     * Returns the {@link JList} of distribution properties.
     *
     * @return
     */
    private JList<String> createDistributionList() {
	final DefaultListModel<String> listModel = new DefaultListModel<String>();

	final Class<T> root = getModel().getCriteria().getEntityClass();
	final IAnalysisAddToDistributionTickManager firstTick = getModel().adtme().getFirstTick();

	for (final String distributionProperty : firstTick.checkedProperties(root)) {
	    listModel.addElement(distributionProperty);
	}
	final JList<String> distributionList = new JList<String>(listModel);
	distributionList.setCellRenderer(new DefaultListCellRenderer() {

	    private static final long serialVersionUID = 7712966992046861840L;

	    private final EntityDescriptor ed = new EntityDescriptor(getModel().getCriteria().getManagedType(), firstTick.checkedProperties(root));

	    @Override
	    public Component getListCellRendererComponent(final JList<?> list, final Object value, final int index, final boolean isSelected, final boolean cellHasFocus) {
		super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

		final Pair<String, String> titleAndDesc = ed.getTitleAndDesc(value.toString());
		setText(titleAndDesc.getKey());
		setToolTipText(titleAndDesc.getValue());

		if (!isSelected) {
		    if (getModel().getChartAnalysisDataProvider().categoryProperties().contains(value)) {
			setBackground(new Color(175, 240, 208));
		    } else {
			setBackground(Color.WHITE);
		    }
		}
		return this;
	    }


	});
	distributionList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	final List<String> usedProperties = firstTick.usedProperties(root);
	if (usedProperties.size() == 1) {
	    distributionList.setSelectedValue(usedProperties.get(0), true);
	}
	/**
	 * Adds the listener that listens the property usage changes and synchronises them with ui model.
	 */
	firstTick.addPropertyUsageListener(new IPropertyUsageListener() {

	    @Override
	    public void propertyStateChanged(final Class<?> root, final String property, final Boolean hasBeenUsed, final Boolean oldState) {
		final boolean isSelected = property.equals(distributionList.getSelectedValue());
		if(isSelected != hasBeenUsed){
		    distributionList.setSelectedValue(property, hasBeenUsed);
		}
	    }
	});
	distributionList.addListSelectionListener(new ListSelectionListener() {

	    @Override
	    public void valueChanged(final ListSelectionEvent e) {

		if(!e.getValueIsAdjusting()){
		    final Object selectedValue = distributionList.getSelectedValue();
		    final boolean hasSelection = selectedValue != null;
		    if(!hasSelection && firstTick.usedProperties(root).size() != 0){
			for(final String usedProperty : firstTick.usedProperties(root)){
			    firstTick.use(root, usedProperty, false);
			}
		    }else if(hasSelection && !firstTick.isUsed(root, selectedValue.toString())){
			firstTick.use(root, selectedValue.toString(), true);
		    }
		}
	    }
	});
	return distributionList;
    }

    /**
     * Returns the {@link SortingCheckboxList} of aggregation properties.
     *
     * @return
     */
    private SortingCheckboxList<String> createAggregationList() {
	final DefaultListModel<String> listModel = new DefaultListModel<String>();

	final Class<T> root = getModel().getCriteria().getEntityClass();
	final IAnalysisAddToAggregationTickManager secondTick = getModel().adtme().getSecondTick();

	for (final String aggregationProperty : secondTick.checkedProperties(root)) {
	    listModel.addElement(aggregationProperty);
	}
	final SortingCheckboxList<String> aggregationList = new SortingCheckboxList<String>(listModel, 1);
	aggregationList.setCellRenderer(new SortingCheckboxListCellRenderer<String>(aggregationList) {

	    private static final long serialVersionUID = -6751336113879821723L;

	    private final EntityDescriptor ed = new EntityDescriptor(getModel().getCriteria().getManagedType(), secondTick.checkedProperties(root));

	    @Override
	    public Component getListCellRendererComponent(final JList<? extends String> list, final String value, final int index, final boolean isSelected, final boolean cellHasFocus) {
		final Component rendererComponent = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
		final Pair<String, String> titleAndDesc = ed.getTitleAndDesc(value);
		defaultRenderer.setText(titleAndDesc.getKey());
		setToolTipText(titleAndDesc.getValue());
		if (!isSelected) {
		    if (getModel().getChartAnalysisDataProvider().aggregatedProperties().contains(value)) {
			rendererComponent.setBackground(new Color(175, 240, 208));
		    } else {
			rendererComponent.setBackground(Color.WHITE);
		    }
		}
		return rendererComponent;
	    }

	    @Override
	    public boolean isSortingAvailable(final String element) {
		return aggregationList.isValueChecked(element, 0) && aggregationList.isSortable(element);
	    }
	});
	aggregationList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	final ListCheckingModel<String> checkingModel = new DomainTreeListCheckingModel<T>(root, secondTick);
	checkingModel.addListCheckingListener(new ListCheckingListener<String>() {

	    @Override
	    public void valueChanged(final ListCheckingEvent<String> e) {
		updateChart(getModel().getChartAnalysisDataProvider().getLoadedData(), null);
		chartScroller.resetScrollRanges();
	    }
	});
	aggregationList.setCheckingModel(checkingModel, 0);
	aggregationList.setSortingModel(new DomainTreeListSortingModel<T>(root, secondTick, getModel().adtme().getRepresentation().getSecondTick()));

	return aggregationList;
    }

    private JSpinner createColumnCounterSpinner() {
	final JSpinner spinner = new JSpinner(new SpinnerNumberModel(getModel().adtme().getVisibleDistributedValuesNumber(), null, null, 1));
	final Dimension prefSize = spinner.getPreferredSize();
	spinner.setPreferredSize(new Dimension(50, prefSize.height));
	spinner.setEnabled(chartPanel.getChartPanelCount() > 0 && isAllChartAvailable());
	return spinner;
    }

    private boolean isAllChartAvailable() {
	for (int index = 0; index < chartPanel.getChartPanelCount(); index++) {
	    if (chartPanel.getChartPanel(index).getChart() == null) {
		return false;
	    }
	}
	return true;
    }

    private JToolBar createChartToolBar() {
	final JToolBar toolBar = new JToolBar(JToolBar.HORIZONTAL);
	toolBar.setFloatable(false);
	toolBar.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));

	getConfigureAction().putValue(Action.LARGE_ICON_KEY, ResourceLoader.getIcon("images/configure.png"));
	getConfigureAction().putValue(Action.SHORT_DESCRIPTION, "Configure analysis");

	final ButtonGroup group = new ButtonGroup();
	final JToggleButton barChart = createToggleButtonFor(switchChartModel, CategoryChartTypes.BAR_CHART, "Show bar chart", ResourceLoader.getIcon("images/chart_bar.png"));
	final JToggleButton stackedBarChart = createToggleButtonFor(switchChartModel, CategoryChartTypes.STACKED_BAR_CHART, "Show stacked bar chart", ResourceLoader.getIcon("images/chart_stacked_bar.png"));
	final JToggleButton lineChart = createToggleButtonFor(switchChartModel, CategoryChartTypes.LINE_CHART, "Show line chart", ResourceLoader.getIcon("images/chart_line.png"));
	final JToggleButton splitChartButton = new JToggleButton("Split chart");
	splitChartButton.addActionListener(new ActionListener() {

	    @Override
	    public void actionPerformed(final ActionEvent e) {
		split = splitChartButton.isSelected();
		updateChart(getModel().getChartAnalysisDataProvider().getLoadedData(), null);
		chartScroller.resetScrollRanges();
	    }

	});
	spinner.addChangeListener(new ChangeListener() {

	    @Override
	    public void stateChanged(final ChangeEvent e) {
		chartScroller.updateScroll(((Integer) spinner.getValue()).intValue());
	    }

	});
	group.add(barChart);
	group.add(stackedBarChart);
	group.add(lineChart);
	toolBar.add(getConfigureAction());
	toolBar.addSeparator();
	toolBar.add(barChart);
	toolBar.add(stackedBarChart);
	toolBar.add(lineChart);
	toolBar.addSeparator();
	toolBar.add(splitChartButton);
	toolBar.addSeparator();
	toolBar.add(DummyBuilder.label("Chart width"));
	toolBar.add(spinner);
	barChart.setSelected(true);
	return toolBar;
    }

    private JToggleButton createToggleButtonFor(final SwitchChartsModel<List<T>, CategoryChartTypes> switchChartModel, final CategoryChartTypes type, final String toolTip, final Icon icon) {
	final JToggleButton chartTogle = new JToggleButton(icon);
	chartTogle.setToolTipText(toolTip);
	chartTogle.addItemListener(switchChartModel.createListenerForChartType(type));
	return chartTogle;
    }

    private int getNumOfSelectedWithoutNew() {
	final Class<T> root = getModel().getCriteria().getEntityClass();
	final IAnalysisAddToAggregationTickManager secondTick = getModel().adtme().getSecondTick();

	final List<String> usedProperties = secondTick.usedProperties(root);
	int num = 0;
	for (final String property : getModel().getChartAnalysisDataProvider().aggregatedProperties()) {
	    if (usedProperties.contains(property)) {
		num++;
	    }
	}
	return num;
    }

    private List<Integer> getSeriesOrder() {
	final Class<T> root = getModel().getCriteria().getEntityClass();
	final IAnalysisAddToAggregationTickManager secondTick = getModel().adtme().getSecondTick();

	final List<String> actualAggregationList = getModel().getChartAnalysisDataProvider().aggregatedProperties();
	final List<Integer> selectedValuesOrder = new ArrayList<Integer>();
	for (final String aggregationProperty : secondTick.usedProperties(root)) {
	    final int index = actualAggregationList.indexOf(aggregationProperty);
	    if (index >= 0) {
		selectedValuesOrder.add(index);
	    }
	}
	return selectedValuesOrder;
    }

    private void updateChart(final List<T> data, final Runnable postAction) {
	final List<Integer> selectedOrder = getSeriesOrder();
	if (split) {
	    final int numOfSelectedWithoutNew = getNumOfSelectedWithoutNew();
	    if (chartPanel.getChartPanelCount() < numOfSelectedWithoutNew) {
		final int howManyToAdd = numOfSelectedWithoutNew - chartPanel.getChartPanelCount();
		for (int chartIndex = 0; chartIndex < howManyToAdd; chartIndex++) {
		    chartPanel.addChartPanel(createChartPanel(true));
		}
	    } else {
		final int howManyToRemove = chartPanel.getChartPanelCount() - numOfSelectedWithoutNew;
		for (int chartIndex = 0; chartIndex < howManyToRemove; chartIndex++) {
		    chartPanel.removeChartPanel(chartPanel.getChartPanelCount() - 1);
		}
	    }
	    if (chartPanel.getChartPanelCount() > 0) {
		chartPanel.getChartPanel(0).setPostAction(new Runnable() {

		    @Override
		    public void run() {
			for (int index = 1; index < chartPanel.getChartPanelCount(); index++) {
			    final ActionChartPanel<List<T>, CategoryChartTypes> panel = chartPanel.getChartPanel(index);
			    panel.setPostAction(null);
			    panel.setChart(data, false, selectedOrder.get(index));
			}
			if (postAction != null) {
			    postAction.run();
			}
		    }

		});
		chartPanel.getChartPanel(0).setChart(data, false, selectedOrder.get(0));
	    }
	} else {
	    ActionChartPanel<List<T>, CategoryChartTypes> panel = null;
	    if (chartPanel.getChartPanelCount() > 0) {
		panel = chartPanel.getChartPanel(0);
		final int countToRemove = chartPanel.getChartPanelCount();
		for (int index = 1; index < countToRemove; index++) {
		    chartPanel.removeChartPanel(1);
		}
	    } else {
		panel = createChartPanel(true);
		chartPanel.addChartPanel(panel);
	    }
	    panel.setPostAction(postAction);
	    final int[] visibleSeries = new int[selectedOrder.size()];
	    for (int index = 0; index < visibleSeries.length; index++) {
		visibleSeries[index] = selectedOrder.get(index);
	    }
	    panel.setChart(data, false, visibleSeries);
	}
	chartPanel.revalidate();
	chartPanel.repaint();
    }

    private ActionChartPanel<List<T>, CategoryChartTypes> createChartPanel(final boolean all, final int... indexes) {
	final ActionChartPanel<List<T>, CategoryChartTypes> chartPanel = new ActionChartPanel<List<T>, CategoryChartTypes>(new CategoryChartFactory<T>(getModel(), all, indexes), new IBlockingLayerProvider() {
	    @Override
	    public BlockingIndefiniteProgressLayer getBlockingLayer() {
		return getOwner().getProgressLayer();
	    }
	}, switchChartModel.getCurrentType(), /* factory creates single chart -> use first chart for this chart panel. */0, 400, 300, 0, 0, Integer.MAX_VALUE, Integer.MAX_VALUE, true, true, true, true, true, true) {
	    private static final long serialVersionUID = -4006162899347838630L;

	    @Override
	    public void mouseDoubleClicked(final ChartMouseEvent chartEvent) {
		performCustomAction(new AnalysisDataEvent<>(ChartAnalysisView.this, chartEvent));
	    }
	};
	chartPanel.addChartPanelChangedListener(new IChartPanelChangeListener() {

	    @Override
	    public void chartPanelChanged(final ChartPanelChangedEventObject event) {
		if (event.getNewChart() != null) {
		    final int maxValue = chartScroller.getActualCategoriesCount();
		    if (maxValue <= 0) {
			spinner.setEnabled(false);
		    } else {
			spinner.setEnabled(true);
			final int visibleCategoryCount = ((Integer) spinner.getModel().getValue()).intValue();
			if (visibleCategoryCount > maxValue || visibleCategoryCount < 1) {
			    spinner.getModel().setValue(new Integer(maxValue));
			}

			((SpinnerNumberModel)spinner.getModel()).setMaximum(new Integer(maxValue));
			((SpinnerNumberModel)spinner.getModel()).setMinimum(new Integer(1));
		    }
		} else {
		    spinner.setEnabled(false);
		}

	    }

	});
	return chartPanel;
    }

    private void layoutComponents() {

	removeAll();
	setLayout(new MigLayout("fill, insets 0", "[fill,grow]", "[fill,grow]"));
	final JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
	final JSplitPane leftPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
	addLoadListener(new ILoadListener() {

	    @Override
	    public void viewWasLoaded(final LoadEvent event) {
		leftPane.setDividerLocation(0.5);
	    }
	});

	//Configuring controls those allows to choose distribution properties.
	final JPanel leftTopPanel = new JPanel(new MigLayout("fill, insets 0", "[fill,grow]", "[][grow,fill]"));
	final JLabel distributionLabel = DummyBuilder.label("Distribution properties");
	leftTopPanel.add(distributionLabel, "wrap");
	leftTopPanel.add(new JScrollPane(distributionList));

	//Configuring controls those allows to choose aggregation properties.
	final JPanel leftDownPanel = new JPanel(new MigLayout("fill, insets 0", "[fill,grow]", "[][grow,fill]"));
	final JLabel aggregationLabel = DummyBuilder.label("Aggregation properties");
	leftDownPanel.add(aggregationLabel, "wrap");
	leftDownPanel.add(new JScrollPane(aggregationList));

	//Configuring controls for chart review panel.
	final JPanel rightPanel = new JPanel(new MigLayout("fill, insets 3", "[fill,grow]", "[][fill,grow]"));
	rightPanel.add(toolBar, "wrap");
	rightPanel.add(chartScroller);

	//Configuring left panel with distribution and aggregation list properties.
	leftPane.setOneTouchExpandable(true);
	leftPane.setTopComponent(leftTopPanel);
	leftPane.setBottomComponent(leftDownPanel);

	//Configuring main view panel.
	splitPane.setOneTouchExpandable(true);
	splitPane.setLeftComponent(leftPane);
	splitPane.setRightComponent(rightPanel);

	add(splitPane);
	invalidate();
	validate();
	repaint();
    }

    private void performCustomAction(final AnalysisDataEvent<?> clickedData) {
	final ChartEntity entity = ((ChartMouseEvent)clickedData.getData()).getEntity();
	if (entity instanceof CategoryItemEntity) {
	    getOwner().showDetails(createAnalysisData(((CategoryItemEntity) entity).getColumnKey()), AnalysisDetailsData.class);
	} else if (entity instanceof CategoryLabelEntity) {
	    getOwner().showDetails(createAnalysisData(((CategoryLabelEntity) entity).getKey()), AnalysisDetailsData.class);
	}
    }

    private AnalysisDetailsData<T> createAnalysisData(final Comparable<?> columnKey) {
	final List<Pair<String, Object>> linkPropValues = new ArrayList<>();
	final EntityWrapper entityWrapper = (EntityWrapper) columnKey;
	final List<String> categories = getModel().getChartAnalysisDataProvider().categoryProperties();
	if(categories.size() == 1){
	    linkPropValues.add(new Pair<String, Object>(categories.get(0), entityWrapper.getEntity()));
	}
	final ICentreDomainTreeManagerAndEnhancer baseCdtme = getModel().getCriteria().getCentreDomainTreeManagerAndEnhnacerCopy();
	baseCdtme.setRunAutomatically(true);

	return new AnalysisDetailsData<>(
		getModel().getCriteria().getEntityClass(), //
		getOwner().getOwner().getModel().getName(), //
		getOwner().getModel().getName(), //
		baseCdtme, //
		getModel().adtme(),//
		linkPropValues);
    }
}
