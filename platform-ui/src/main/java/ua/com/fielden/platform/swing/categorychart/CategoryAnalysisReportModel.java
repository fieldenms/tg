package ua.com.fielden.platform.swing.categorychart;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.SortOrder;
import javax.swing.border.EtchedBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.miginfocom.swing.MigLayout;

import org.jfree.chart.ChartMouseEvent;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.equery.EntityAggregates;
import ua.com.fielden.platform.reportquery.IAggregatedProperty;
import ua.com.fielden.platform.reportquery.IDistributedProperty;
import ua.com.fielden.platform.selectioncheckbox.SelectionCheckBoxPanel.IAction;
import ua.com.fielden.platform.swing.analysis.IAnalysisReportPersistentObject;
import ua.com.fielden.platform.swing.chartscroll.CategoryChartScrollPanel;
import ua.com.fielden.platform.swing.checkboxlist.ListCheckingEvent;
import ua.com.fielden.platform.swing.checkboxlist.ListCheckingListener;
import ua.com.fielden.platform.swing.checkboxlist.SortObject;
import ua.com.fielden.platform.swing.checkboxlist.SortingCheckboxList;
import ua.com.fielden.platform.swing.checkboxlist.SortingCheckboxListCellRenderer;
import ua.com.fielden.platform.swing.components.blocking.BlockingIndefiniteProgressLayer;
import ua.com.fielden.platform.swing.components.blocking.IBlockingLayerProvider;
import ua.com.fielden.platform.swing.dnd.DnDSupport2;
import ua.com.fielden.platform.swing.groupanalysis.GroupAnalysisReportModel;
import ua.com.fielden.platform.swing.review.AnalysisPersistentObject;
import ua.com.fielden.platform.swing.utils.DummyBuilder;
import ua.com.fielden.platform.utils.Pair;
import ua.com.fielden.platform.utils.ResourceLoader;

public class CategoryAnalysisReportModel<T extends AbstractEntity, DAO extends IEntityDao<T>> extends GroupAnalysisReportModel<T, DAO> {

    private final CategoryChartReview<T, DAO> reportView;
    private final BlockingIndefiniteProgressLayer tabPaneLayer;

    //Used for creating view in AnalysisReportMode.REPORT mode.
    private final JList distributionList;
    private final SortingCheckboxList<IAggregatedProperty> aggregationList;
    private final MultipleChartPanel<List<EntityAggregates>, CategoryChartTypes> chartPanel;
    private final CategoryChartScrollPanel chartScroller;
    private final JSpinner spinner;
    private final SwitchChartsModel<List<EntityAggregates>, CategoryChartTypes> switchChartModel;
    private final JToolBar toolBar;

    //Determines whether to split chart in to different series or not.
    private boolean split = false;

    public CategoryAnalysisReportModel(final CategoryChartReview<T, DAO> reportView, final BlockingIndefiniteProgressLayer tabPaneLayer, final IAnalysisReportPersistentObject pObj) {
	this.reportView = reportView;
	this.tabPaneLayer = tabPaneLayer;

	//Creating distribution and aggregation lists.
	this.distributionList = createDistributionList(pObj instanceof AnalysisPersistentObject ? ((AnalysisPersistentObject) pObj).getAvailableDistributionProperties()
		: new ArrayList<IDistributedProperty>());
	this.distributionList.setCellRenderer(new DefaultListCellRenderer() {

	    private static final long serialVersionUID = 7712966992046861840L;

	    @Override
	    public Component getListCellRendererComponent(final JList list, final Object value, final int index, final boolean isSelected, final boolean cellHasFocus) {
		final Component rendererComponent = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
		if (!isSelected) {
		    if (value.equals(reportView.getModel().getSelectedDistributionProeprty())) {
			rendererComponent.setBackground(new Color(175, 240, 208));
		    } else {
			rendererComponent.setBackground(Color.WHITE);
		    }
		}
		return rendererComponent;
	    }

	});
	this.distributionList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	DnDSupport2.installDnDSupport(distributionList, new AnalysisListDragFromSupport(distributionList), new AnalysisListDragToSupport(distributionList), true);
	this.aggregationList = createAggregationList(pObj instanceof AnalysisPersistentObject ? ((AnalysisPersistentObject) pObj).getAvailableAggregationProperties()
		: new ArrayList<IAggregatedProperty>());
	this.aggregationList.setCellRenderer(new SortingCheckboxListCellRenderer<IAggregatedProperty>(aggregationList, new JCheckBox()) {

	    private static final long serialVersionUID = -6751336113879821723L;

	    @Override
	    public Component getListCellRendererComponent(final JList list, final Object value, final int index, final boolean isSelected, final boolean cellHasFocus) {
		final Component rendererComponent = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
		final boolean isValueChosen = reportView.getModel().getSelectedAggregationProperties().contains(value);
		if (isValueChosen) {
		    arrow.setVisible(true);
		    if (aggregationList.getSortingModel().isSortable(((IAggregatedProperty) value))) {
			arrow.setSortOrder(aggregationList.getSortingModel().getSortOrder(((IAggregatedProperty) value)));
		    }
		}
		if (!isSelected) {
		    if (isValueChosen) {
			rendererComponent.setBackground(new Color(175, 240, 208));
		    } else {
			rendererComponent.setBackground(Color.WHITE);
		    }
		}
		return rendererComponent;
	    }

	});
	this.aggregationList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	if (pObj instanceof AnalysisPersistentObject) {
	    final AnalysisPersistentObject analysisPObj = (AnalysisPersistentObject) pObj;
	    if (analysisPObj.getSelectedDistributionProperty() != null) {
		this.distributionList.setSelectedValue(analysisPObj.getSelectedDistributionProperty(), true);
	    }
	    if (analysisPObj.getSelectedAggregationProperties() != null && analysisPObj.getSelectedAggregationProperties().size() > 0) {
		this.aggregationList.setCheckingValues(analysisPObj.getSelectedAggregationProperties().toArray(new IAggregatedProperty[0]));
	    }
	    if (analysisPObj.getOrderedProeprty() != null) {
		final List<SortObject<IAggregatedProperty>> sortParameters = new ArrayList<SortObject<IAggregatedProperty>>();
		sortParameters.add(new SortObject<IAggregatedProperty>(analysisPObj.getOrderedProeprty().getKey(), analysisPObj.getOrderedProeprty().getValue()));
		this.aggregationList.getSortingModel().setSortObjects(sortParameters, true);
	    }
	}

	//Creating chart panel
	this.chartPanel = new MultipleChartPanel<List<EntityAggregates>, CategoryChartTypes>();
	this.chartScroller = new CategoryChartScrollPanel(chartPanel, reportView.getModel().getVisibleCategoryCount());

	//Configuring aggregation list
	this.aggregationList.getCheckingModel().addListCheckingListener(new ListCheckingListener<IAggregatedProperty>() {

	    @Override
	    public void valueChanged(final ListCheckingEvent<IAggregatedProperty> e) {
		updateChart(reportView.getModel().getLoadedData(), null);
		chartScroller.resetScrollRanges();
	    }
	});
	DnDSupport2.installDnDSupport(aggregationList, new AnalysisListDragFromSupport(aggregationList), new AnalysisChartDragToSupport<T, DAO, IAggregatedProperty, List<EntityAggregates>, CategoryChartTypes>(aggregationList, chartPanel, reportView.getModel()), true);

	//Configuring spinner.
	this.spinner = new JSpinner(reportView.getModel().getColumnCountSpinnerModel());
	final Dimension prefSize = spinner.getPreferredSize();
	spinner.setPreferredSize(new Dimension(50, prefSize.height));
	spinner.setEnabled(chartPanel.getChartPanelsCount() > 0 && isAllChartAvailable());

	//Creating and configuring switch model and tool bar.
	this.switchChartModel = new SwitchChartsModel<List<EntityAggregates>, CategoryChartTypes>(chartPanel);
	updateChart(new ArrayList<EntityAggregates>(), null);
	this.toolBar = createChartTypeBar(switchChartModel);
    }

    private JList createDistributionList(final List<IDistributedProperty> availableProperties) {
	final DefaultListModel listModel = new DefaultListModel();
	if (availableProperties != null) {
	    for (final IDistributedProperty distributionProperty : availableProperties) {
		listModel.addElement(distributionProperty);
	    }
	}
	final JList distributionList = new JList(listModel);
	return distributionList;
    }

    private SortingCheckboxList<IAggregatedProperty> createAggregationList(final List<IAggregatedProperty> availableAggregationProperties) {
	final DefaultListModel listModel = new DefaultListModel();
	if (availableAggregationProperties != null) {
	    for (final IAggregatedProperty distributionProperty : availableAggregationProperties) {
		listModel.addElement(distributionProperty);
	    }
	}
	final SortingCheckboxList<IAggregatedProperty> aggregationList = new SortingCheckboxList<IAggregatedProperty>(listModel);
	return aggregationList;
    }

    private boolean isAllChartAvailable() {
	for (int index = 0; index < chartPanel.getChartPanelsCount(); index++) {
	    if (chartPanel.getChartPanel(index).getChart() == null) {
		return false;
	    }
	}
	return true;
    }

    private JToolBar createChartTypeBar(final SwitchChartsModel<List<EntityAggregates>, CategoryChartTypes> switchChartModel) {
	final JToolBar toolBar = new JToolBar(JToolBar.HORIZONTAL);
	toolBar.setFloatable(false);
	toolBar.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
	final ButtonGroup group = new ButtonGroup();
	final JToggleButton barChart = createToggleButtonFor(switchChartModel, CategoryChartTypes.BAR_CHART, "Show bar chart", ResourceLoader.getIcon("images/chart_bar.png"));
	final JToggleButton stackedBarChart = createToggleButtonFor(switchChartModel, CategoryChartTypes.STACKED_BAR_CHART, "Show stacked bar chart", ResourceLoader.getIcon("images/chart_stacked_bar.png"));
	final JToggleButton lineChart = createToggleButtonFor(switchChartModel, CategoryChartTypes.LINE_CHART, "Show line chart", ResourceLoader.getIcon("images/chart_line.png"));
	final JToggleButton splitChartButton = new JToggleButton("Split chart");
	splitChartButton.addActionListener(new ActionListener() {

	    @Override
	    public void actionPerformed(final ActionEvent e) {
		split = splitChartButton.isSelected();
		updateChart(reportView.getModel().getLoadedData(), null);
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
	final List<IAggregatedProperty> selectedProperties = reportView.getModel().getSelectedAggregationProperties();
	int num = 0;
	for (final IAggregatedProperty property : selectedProperties) {
	    if (aggregationList.isValueChecked(property)) {
		num++;
	    }
	}
	return num;
    }

    private List<Integer> getSeriesOrder() {
	final List<IAggregatedProperty> actualAggregationList = reportView.getModel().getSelectedAggregationProperties();
	final List<IAggregatedProperty> orderedAggregationPropertiesList = aggregationList.getSelectedValuesInOrder();
	final List<Integer> selectedValuesOrder = new ArrayList<Integer>();
	for (final IAggregatedProperty aggregationProperty : orderedAggregationPropertiesList) {
	    final int index = actualAggregationList.indexOf(aggregationProperty);
	    if (index >= 0) {
		selectedValuesOrder.add(index);
	    }
	}
	return selectedValuesOrder;
    }

    private ActionChartPanel<List<EntityAggregates>, CategoryChartTypes> createChartPanel(final boolean all, final int... indexes) {
	final ActionChartPanel<List<EntityAggregates>, CategoryChartTypes> chartPanel = new ActionChartPanel<List<EntityAggregates>, CategoryChartTypes>(reportView.getModel().getChartFactory(all, indexes), new IBlockingLayerProvider() {
	    @Override
	    public BlockingIndefiniteProgressLayer getBlockingLayer() {
		return tabPaneLayer;
	    }
	}, switchChartModel.getCurrentType(), /* factory creates single chart -> use first chart for this chart panel. */0, 400, 300, 0, 0, Integer.MAX_VALUE, Integer.MAX_VALUE, true, true, true, true, true, true) {
	    private static final long serialVersionUID = -4006162899347838630L;

	    @Override
	    public void mouseDoubleClicked(final ChartMouseEvent chartEvent) {
		reportView.getModel().runDoubleClickAction(new AnalysisDoubleClickEvent(this, chartEvent));
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
			if (reportView.getModel().getVisibleCategoryCount() > maxValue || reportView.getModel().getVisibleCategoryCount() < 1) {
			    reportView.getModel().getColumnCountSpinnerModel().setValue(new Integer(maxValue));
			}
			reportView.getModel().getColumnCountSpinnerModel().setMaximum(new Integer(maxValue));
			reportView.getModel().getColumnCountSpinnerModel().setMinimum(new Integer(1));
		    }
		} else {
		    spinner.setEnabled(false);
		}

	    }

	});
	return chartPanel;
    }

    private <Type> List<Type> getDataList(final ListModel list, final Class<Type> clazz) {
	final List<Type> elements = new ArrayList<Type>();
	for (int elementIndex = 0; elementIndex < list.getSize(); elementIndex++) {
	    elements.add((Type) list.getElementAt(elementIndex));
	}
	return elements;
    }

    private <ElementType> DefaultListModel getNewModelFor(final List<ElementType> selectedElements, final List<ElementType> oldElements) {
	final Iterator<ElementType> elementIterator = oldElements.iterator();
	while (elementIterator.hasNext()) {
	    final ElementType element = elementIterator.next();
	    if (selectedElements.contains(element)) {
		selectedElements.remove(element);
	    } else {
		elementIterator.remove();
	    }
	}
	oldElements.addAll(selectedElements);
	final DefaultListModel listModel = new DefaultListModel();
	for (final ElementType element : oldElements) {
	    listModel.addElement(element);
	}
	return listModel;
    }

    private void layoutComponents(final Container container) {
	container.removeAll();
	container.setLayout(new MigLayout("fill, insets 0", "[fill,grow]", "[fill,grow]"));
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

	container.add(splitPane);
	container.invalidate();
	container.validate();
	container.repaint();
    }

    @Override
    public void restoreReportView(final Container container) throws IllegalStateException {
	if (canRestoreReportView()) {
	    layoutComponents(container);
	} else {
	    throw new IllegalStateException("The report view cannot be build, because distribution properties list or aggreagation properties list are empty");
	}
    }

    @Override
    public void createReportView(final Container container) throws IllegalStateException {
	if (reportView.getAnalysisWizardModel().isValidToBuildReportView()) {
	    updateModel();
	    layoutComponents(container);
	} else {
	    throw new IllegalStateException("Please choose distribution and aggregation properties");
	}
    }

    public void updateModel() {
	final DefaultListModel listModel = getNewModelFor(reportView.getAnalysisWizardModel().getSelectedDistributionProperties(), getDataList(distributionList.getModel(), IDistributedProperty.class));
	final Object selectedValue = distributionList.getSelectedValue();
	distributionList.setModel(listModel);
	distributionList.setSelectedValue(selectedValue, true);
	distributionList.invalidate();
	distributionList.revalidate();
	aggregationList.setModel(getNewModelFor(reportView.getAnalysisWizardModel().getSelectedAggregationProperties(), aggregationList.getVectorListData()));
	aggregationList.invalidate();
	aggregationList.revalidate();
    }

    @Override
    public List<IAggregatedProperty> getAvailableAggregationProperties() {
	return aggregationList.getVectorListData();
    }

    @Override
    public List<IDistributedProperty> getAvailableDistributionProperties() {
	return getDataList(distributionList.getModel(), IDistributedProperty.class);
    }

    public IDistributedProperty getSelectedDistributionProperty() {
	return (IDistributedProperty) distributionList.getSelectedValue();
    }

    public IAggregatedProperty[] getSelectedAggregationProperties() {
	return aggregationList.getSelectedValuesInOrder().toArray(new IAggregatedProperty[0]);
    }

    public Pair<IAggregatedProperty, SortOrder> getSortingParameters() {
	final List<SortObject<IAggregatedProperty>> sortObjects = aggregationList.getSortingModel().getSortObjects();
	if (sortObjects.isEmpty()) {
	    return new Pair<IAggregatedProperty, SortOrder>(null, SortOrder.UNSORTED);
	}
	return new Pair<IAggregatedProperty, SortOrder>(sortObjects.get(0).getSortObject(), sortObjects.get(0).getSortOrder());
    }

    public CategoryChartTypes getCurrentChartType() {
	return switchChartModel.getCurrentType();
    }

    public void updateChart(final List<EntityAggregates> data, final IAction postAction) {
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

    public void resetChartScroller() {
	chartScroller.resetScrollRanges();
    }

    /**
     * Returns the value that indicates whether report view can be restored or not.
     * 
     * @return
     */
    @Override
    public boolean canRestoreReportView() {
	return distributionList.getModel().getSize() > 0 && aggregationList.getModel().getSize() > 0;
    }

}
