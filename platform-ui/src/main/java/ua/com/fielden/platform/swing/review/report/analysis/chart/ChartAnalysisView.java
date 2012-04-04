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
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;
import javax.swing.SortOrder;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.EtchedBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import net.miginfocom.swing.MigLayout;

import org.jfree.chart.ChartMouseEvent;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.centre.IOrderingRepresentation.Ordering;
import ua.com.fielden.platform.domaintree.centre.analyses.IAnalysisDomainTreeManager;
import ua.com.fielden.platform.domaintree.centre.analyses.IAnalysisDomainTreeManager.IAnalysisAddToAggregationTickManager;
import ua.com.fielden.platform.domaintree.centre.analyses.IAnalysisDomainTreeManager.IAnalysisAddToDistributionTickManager;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.EntityAggregates;
import ua.com.fielden.platform.selectioncheckbox.SelectionCheckBoxPanel.IAction;
import ua.com.fielden.platform.swing.categorychart.ActionChartPanel;
import ua.com.fielden.platform.swing.categorychart.CategoryChartTypes;
import ua.com.fielden.platform.swing.categorychart.ChartPanelChangedEventObject;
import ua.com.fielden.platform.swing.categorychart.IChartPanelChangeListener;
import ua.com.fielden.platform.swing.categorychart.MultipleChartPanel;
import ua.com.fielden.platform.swing.categorychart.SwitchChartsModel;
import ua.com.fielden.platform.swing.chartscroll.CategoryChartScrollPanel;
import ua.com.fielden.platform.swing.checkboxlist.ListCheckingEvent;
import ua.com.fielden.platform.swing.checkboxlist.ListCheckingListener;
import ua.com.fielden.platform.swing.checkboxlist.SortObject;
import ua.com.fielden.platform.swing.checkboxlist.SortingCheckboxList;
import ua.com.fielden.platform.swing.checkboxlist.SortingCheckboxListCellRenderer;
import ua.com.fielden.platform.swing.components.blocking.BlockingIndefiniteProgressLayer;
import ua.com.fielden.platform.swing.components.blocking.IBlockingLayerProvider;
import ua.com.fielden.platform.swing.review.report.analysis.view.AbstractAnalysisReview;
import ua.com.fielden.platform.swing.review.report.centre.AbstractEntityCentre;
import ua.com.fielden.platform.swing.review.report.events.SelectionEvent;
import ua.com.fielden.platform.swing.review.report.interfaces.ISelectionEventListener;
import ua.com.fielden.platform.swing.utils.DummyBuilder;
import ua.com.fielden.platform.utils.Pair;
import ua.com.fielden.platform.utils.ResourceLoader;

public class ChartAnalysisView<T extends AbstractEntity<?>> extends AbstractAnalysisReview<T, ICentreDomainTreeManagerAndEnhancer ,IAnalysisDomainTreeManager, Void> {

    private static final long serialVersionUID = -6505281133387254406L;

    private final CategoryDataModel dataModel;
    /**
     * The list of available distribution properties.
     */
    private final JList distributionList;
    /**
     * The list of available aggregation properties.
     */
    private final SortingCheckboxList<String> aggregationList;

    /**
     * The chart panel that holds one or more charts.
     */
    private final MultipleChartPanel<List<EntityAggregates>, CategoryChartTypes> chartPanel;
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
    private final SwitchChartsModel<List<EntityAggregates>, CategoryChartTypes> switchChartModel;
    /**
     * Tool bar that allows to configure charts: choose between different types of charts, choose the number of visible categories e. t. c.
     */
    private final JToolBar toolBar;

    /**
     * Determines whether to split chart in to different series or not.
     */
    private boolean split = false;

    public ChartAnalysisView(final ChartAnalysisModel<T> model, final BlockingIndefiniteProgressLayer progressLayer, final AbstractEntityCentre<T, ICentreDomainTreeManagerAndEnhancer> owner) {
	super(model, progressLayer, owner);
	this.dataModel = new CategoryDataModel(getModel().getChartAnalysisDataProvider());
	this.chartPanel = new MultipleChartPanel<List<EntityAggregates>, CategoryChartTypes>();
	this.chartScroller = new CategoryChartScrollPanel(chartPanel, getModel().adtm().getVisibleDistributedValuesNumber());
	this.distributionList = createDistributionList();
	this.aggregationList = createAggregationList();
	this.spinner = createColumnCounterSpinner();
	this.switchChartModel = new SwitchChartsModel<List<EntityAggregates>, CategoryChartTypes>(chartPanel);
	//	updateChart(new ArrayList<EntityAggregates>(), null);
	this.toolBar = createChartToolBar();
	this.addSelectionEventListener(createChartAnalysisSelectionListener());
	layoutComponents();


	//DnDSupport2.installDnDSupport(aggregationList, new AnalysisListDragFromSupport(aggregationList), new AnalysisChartDragToSupport<T, DAO, IAggregatedProperty, List<EntityAggregates>, CategoryChartTypes>(aggregationList, chartPanel, reportView.getModel()), true);
	//DnDSupport2.installDnDSupport(distributionList, new AnalysisListDragFromSupport(distributionList), new AnalysisListDragToSupport(distributionList), true);
    }

    @Override
    public ChartAnalysisModel<T> getModel() {
	return (ChartAnalysisModel<T>)super.getModel();
    }

    @Override
    protected void enableRelatedActions(final boolean enable, final boolean navigate) {
	if(getModel().getCriteria().isDefaultEnabled()){
	    getOwner().getDefaultAction().setEnabled(enable);
	}
	if(!navigate){
	    getOwner().getPaginator().setEnableActions(enable, !enable);
	}
	getOwner().getRunAction().setEnabled(enable);
    }

    //    /**
    //     * Enables or disables the paginator's actions without enabling or disabling blocking layer.
    //     *
    //     * @param enable
    //     */
    //    private void enablePaginatorActionsWithoutBlockingLayer(final boolean enable){
    //	getOwner().getPaginator().getFirst().setEnabled(enable, false);
    //	getOwner().getPaginator().getPrev().setEnabled(enable, false);
    //	getOwner().getPaginator().getNext().setEnabled(enable, false);
    //	getOwner().getPaginator().getLast().setEnabled(enable, false);
    //	if(getOwner().getPaginator().getFeedback() != null){
    //	    getOwner().getPaginator().getFeedback().enableFeedback(false);
    //	}
    //    }

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
		getOwner().getDefaultAction().setEnabled(getModel().getCriteria().isDefaultEnabled());
		getOwner().getCriteriaPanel().getSwitchAction().setEnabled(getOwner().getCriteriaPanel().canConfigure());
		getOwner().getCustomActionChanger().setEnabled(getOwner().getCustomActionChanger() != null);
		//Managing the paginator's enablements.
		getOwner().getPaginator().setEnableActions(true, false);
		//Managing load and export enablements.
		getOwner().getExportAction().setEnabled(false);
		getOwner().getRunAction().setEnabled(true);
	    }
	};
    }

    private JList createDistributionList() {
	final DefaultListModel listModel = new DefaultListModel();

	final Class<T> root = getModel().getCriteria().getEntityClass();
	final IAnalysisAddToDistributionTickManager firstTick = getModel().adtm().getFirstTick();

	for (final String distributionProperty : firstTick.checkedProperties(root)) {
	    listModel.addElement(distributionProperty);
	}
	final JList distributionList = new JList(listModel);
	distributionList.setCellRenderer(new DefaultListCellRenderer() {

	    private static final long serialVersionUID = 7712966992046861840L;

	    @Override
	    public Component getListCellRendererComponent(final JList list, final Object value, final int index, final boolean isSelected, final boolean cellHasFocus) {

		final Component rendererComponent = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
		if (!isSelected) {
		    if (getModel().getChartAnalysisDataProvider().categoryProperties().contains(value)) {
			rendererComponent.setBackground(new Color(175, 240, 208));
		    } else {
			rendererComponent.setBackground(Color.WHITE);
		    }
		}
		return rendererComponent;
	    }

	});
	distributionList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	final List<String> usedProperties = firstTick.usedProperties(root);
	if (usedProperties.size() == 1) {
	    distributionList.setSelectedValue(usedProperties.get(0), true);
	}
	distributionList.addListSelectionListener(new ListSelectionListener() {

	    @Override
	    public void valueChanged(final ListSelectionEvent e) {

		if(!e.getValueIsAdjusting()){
		    final Object selectionValues[] = distributionList.getSelectedValues();
		    if(selectionValues.length == 0){
			firstTick.use(root, distributionList.getModel().getElementAt(e.getLastIndex()).toString(), false);
		    }else if (selectionValues.length == 1){
			firstTick.use(root, selectionValues[0].toString(), true);
		    } else {
			throw new IllegalStateException("The list of distribution properties must be in single selection mode!");
		    }

		}
	    }
	});
	return distributionList;
    }

    private SortingCheckboxList<String> createAggregationList() {
	final DefaultListModel listModel = new DefaultListModel();

	final Class<T> root = getModel().getCriteria().getEntityClass();
	final IAnalysisAddToAggregationTickManager secondTick = getModel().adtm().getSecondTick();

	for (final String distributionProperty : secondTick.checkedProperties(root)) {
	    listModel.addElement(distributionProperty);
	}
	final SortingCheckboxList<String> aggregationList = new SortingCheckboxList<String>(listModel);
	aggregationList.setCellRenderer(new SortingCheckboxListCellRenderer<String>(aggregationList, new JCheckBox()) {

	    private static final long serialVersionUID = -6751336113879821723L;

	    @Override
	    public Component getListCellRendererComponent(final JList list, final Object value, final int index, final boolean isSelected, final boolean cellHasFocus) {
		final Component rendererComponent = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
		final boolean isValueChosen = secondTick.isUsed(root, value.toString());
		if (secondTick.isUsed(root, value.toString())) {
		    arrow.setVisible(true);
		    if (aggregationList.getSortingModel().isSortable(value.toString())) {
			arrow.setSortOrder(aggregationList.getSortingModel().getSortOrder(value.toString()));
		    }
		}
		if (!isSelected) {
		    if (getModel().getChartAnalysisDataProvider().aggregatedProperties().contains(value)) {
			rendererComponent.setBackground(new Color(175, 240, 208));
		    } else {
			rendererComponent.setBackground(Color.WHITE);
		    }
		}
		return rendererComponent;
	    }

	});
	aggregationList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

	final List<String> usedProperties = secondTick.usedProperties(root);
	aggregationList.setCheckingValues(usedProperties.toArray(new String[0]));

	final List<SortObject<String>> sortParameters = new ArrayList<SortObject<String>>();
	for(final Pair<String, Ordering> orderPair : secondTick.orderedProperties(root)){
	    sortParameters.add(new SortObject<String>(orderPair.getKey(), sortOrder(orderPair.getValue())));
	}
	aggregationList.getSortingModel().setSortObjects(sortParameters, true);
	aggregationList.getCheckingModel().addListCheckingListener(new ListCheckingListener<String>() {

	    @Override
	    public void valueChanged(final ListCheckingEvent<String> e) {
		secondTick.use(root, e.getValue(), e.isChecked());
		updateChart(getModel().getChartAnalysisDataProvider().getLoadedData().data(), null);
		chartScroller.resetScrollRanges();
	    }
	});
	return aggregationList;
    }

    private SortOrder sortOrder(final Ordering value) {
	switch (value) {
	case ASCENDING:
	    return SortOrder.ASCENDING;
	case DESCENDING:
	    return SortOrder.DESCENDING;
	}
	return null;
    }

    private JSpinner createColumnCounterSpinner() {
	final JSpinner spinner = new JSpinner(new SpinnerNumberModel(getModel().adtm().getVisibleDistributedValuesNumber(), null, null, 1));
	final Dimension prefSize = spinner.getPreferredSize();
	spinner.setPreferredSize(new Dimension(50, prefSize.height));
	spinner.setEnabled(chartPanel.getChartPanelsCount() > 0 && isAllChartAvailable());
	return spinner;
    }

    private boolean isAllChartAvailable() {
	for (int index = 0; index < chartPanel.getChartPanelsCount(); index++) {
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
		updateChart(getModel().getChartAnalysisDataProvider().getLoadedData().data(), null);
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

    private JToggleButton createToggleButtonFor(final SwitchChartsModel<List<EntityAggregates>, CategoryChartTypes> switchChartModel, final CategoryChartTypes type, final String toolTip, final Icon icon) {
	final JToggleButton chartTogle = new JToggleButton(icon);
	chartTogle.setToolTipText(toolTip);
	chartTogle.addItemListener(switchChartModel.createListenerForChartType(type));
	return chartTogle;
    }

    private int getNumOfSelectedWithoutNew() {
	final Class<T> root = getModel().getCriteria().getEntityClass();
	final IAnalysisAddToAggregationTickManager secondTick = getModel().adtm().getSecondTick();

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
	final IAnalysisAddToAggregationTickManager secondTick = getModel().adtm().getSecondTick();

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

    private void updateChart(final List<EntityAggregates> data, final IAction postAction) {
	final List<Integer> selectedOrder = getSeriesOrder();
	if (split) {
	    final int numOfSelectedWithoutNew = getNumOfSelectedWithoutNew();
	    if (chartPanel.getChartPanelsCount() < numOfSelectedWithoutNew) {
		final int howManyToAdd = numOfSelectedWithoutNew - chartPanel.getChartPanelsCount();
		for (int chartIndex = 0; chartIndex < howManyToAdd; chartIndex++) {
		    chartPanel.addChartPanel(createChartPanel(true));
		}
	    } else {
		final int howManyToRemove = chartPanel.getChartPanelsCount() - numOfSelectedWithoutNew;
		for (int chartIndex = 0; chartIndex < howManyToRemove; chartIndex++) {
		    chartPanel.removeChartPanel(chartPanel.getChartPanelsCount() - 1);
		}
	    }
	    if (chartPanel.getChartPanelsCount() > 0) {
		chartPanel.getChartPanel(0).setPostAction(new IAction() {

		    @Override
		    public void action() {
			for (int index = 1; index < chartPanel.getChartPanelsCount(); index++) {
			    final ActionChartPanel<List<EntityAggregates>, CategoryChartTypes> panel = chartPanel.getChartPanel(index);
			    panel.setPostAction(null);
			    panel.setChart(data, false, selectedOrder.get(index));
			}
			if (postAction != null) {
			    postAction.action();
			}
		    }

		});
		chartPanel.getChartPanel(0).setChart(data, false, selectedOrder.get(0));
	    }
	} else {
	    ActionChartPanel<List<EntityAggregates>, CategoryChartTypes> panel = null;
	    if (chartPanel.getChartPanelsCount() > 0) {
		panel = chartPanel.getChartPanel(0);
		final int countToRemove = chartPanel.getChartPanelsCount();
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
    /////////////////////////Refactor the code below//////////////////////////////////////////


    private ActionChartPanel<List<EntityAggregates>, CategoryChartTypes> createChartPanel(final boolean all, final int... indexes) {
	final ActionChartPanel<List<EntityAggregates>, CategoryChartTypes> chartPanel = new ActionChartPanel<List<EntityAggregates>, CategoryChartTypes>(new CategoryChartFactory<T, IEntityDao<T>>(getModel().getChartAnalysisDataProvider(), dataModel, all, indexes), new IBlockingLayerProvider() {
	    @Override
	    public BlockingIndefiniteProgressLayer getBlockingLayer() {
		return getProgressLayer();
	    }
	}, switchChartModel.getCurrentType(), /* factory creates single chart -> use first chart for this chart panel. */0, 400, 300, 0, 0, Integer.MAX_VALUE, Integer.MAX_VALUE, true, true, true, true, true, true) {
	    private static final long serialVersionUID = -4006162899347838630L;

	    @Override
	    public void mouseDoubleClicked(final ChartMouseEvent chartEvent) {
		//TODO implement double click action that loads detail view for choosen category.
		//reportView.getModel().runDoubleClickAction(new AnalysisDoubleClickEvent(this, chartEvent));
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
	//	final TaskPanel leftPanel = new TaskPanel(new MigLayout("insets 3, fill", "[fill,grow,:190:]", "[:230:]"));
	//	final TaskPanel downPanel = new TaskPanel(new MigLayout("insets 3, fill", "[fill,grow,:190:]", "[:100:]"));
	//	leftPanel.setTitle("Distribution properties");
	//	downPanel.setTitle("Aggregation properties");
	//	leftPanel.setAnimated(false);
	//	downPanel.setAnimated(false);

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
}
