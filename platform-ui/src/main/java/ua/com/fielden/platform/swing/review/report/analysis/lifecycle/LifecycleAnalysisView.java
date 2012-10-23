package ua.com.fielden.platform.swing.review.report.analysis.lifecycle;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
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
import javax.swing.JSplitPane;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;
import javax.swing.border.EtchedBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import net.miginfocom.swing.MigLayout;

import org.jfree.chart.ChartMouseEvent;

import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.centre.analyses.IAbstractAnalysisDomainTreeManager.IUsageManager.IPropertyUsageListener;
import ua.com.fielden.platform.domaintree.centre.analyses.ILifecycleDomainTreeManager;
import ua.com.fielden.platform.domaintree.centre.analyses.ILifecycleDomainTreeManager.ILifecycleAddToCategoriesTickManager;
import ua.com.fielden.platform.domaintree.centre.analyses.ILifecycleDomainTreeManager.ILifecycleAddToDistributionTickManager;
import ua.com.fielden.platform.domaintree.centre.analyses.impl.LifecycleDomainTreeManager;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.equery.lifecycle.LifecycleModel;
import ua.com.fielden.platform.reflection.development.EntityDescriptor;
import ua.com.fielden.platform.selectioncheckbox.SelectionCheckBoxPanel.IAction;
import ua.com.fielden.platform.swing.actions.BlockingLayerCommand;
import ua.com.fielden.platform.swing.categorychart.ActionChartPanel;
import ua.com.fielden.platform.swing.categorychart.CategoryChartTypes;
import ua.com.fielden.platform.swing.categorychart.MultipleChartPanel;
import ua.com.fielden.platform.swing.categorychart.SwitchChartsModel;
import ua.com.fielden.platform.swing.checkboxlist.ListCheckingEvent;
import ua.com.fielden.platform.swing.checkboxlist.ListCheckingListener;
import ua.com.fielden.platform.swing.checkboxlist.ListCheckingModel;
import ua.com.fielden.platform.swing.checkboxlist.ListSortingModel;
import ua.com.fielden.platform.swing.checkboxlist.SorterChangedEvent;
import ua.com.fielden.platform.swing.checkboxlist.SorterEventListener;
import ua.com.fielden.platform.swing.checkboxlist.SortingCheckboxList;
import ua.com.fielden.platform.swing.checkboxlist.SortingCheckboxListCellRenderer;
import ua.com.fielden.platform.swing.components.bind.development.BoundedValidationLayer;
import ua.com.fielden.platform.swing.components.blocking.BlockingIndefiniteProgressLayer;
import ua.com.fielden.platform.swing.components.blocking.IBlockingLayerProvider;
import ua.com.fielden.platform.swing.components.smart.datepicker.DatePickerLayer;
import ua.com.fielden.platform.swing.review.report.analysis.lifecycle.configuration.LifecycleAnalysisConfigurationView;
import ua.com.fielden.platform.swing.review.report.analysis.view.AbstractAnalysisReview;
import ua.com.fielden.platform.swing.review.report.analysis.view.AnalysisDataEvent;
import ua.com.fielden.platform.swing.review.report.analysis.view.DomainTreeListCheckingModel;
import ua.com.fielden.platform.swing.review.report.analysis.view.DomainTreeListSortingModel;
import ua.com.fielden.platform.swing.review.report.events.SelectionEvent;
import ua.com.fielden.platform.swing.review.report.interfaces.ISelectionEventListener;
import ua.com.fielden.platform.swing.taskpane.TaskPanel;
import ua.com.fielden.platform.utils.Pair;
import ua.com.fielden.platform.utils.ResourceLoader;

/**
 * View panel for lifecycle analysis.
 *
 * @author TG Team
 *
 * @param <T>
 */
public class LifecycleAnalysisView<T extends AbstractEntity<?>> extends AbstractAnalysisReview<T, ICentreDomainTreeManagerAndEnhancer, ILifecycleDomainTreeManager, Void> {

    private static final long serialVersionUID = -4883473612285722738L;

    private final int normalDividerLocation = 207;

    private final JCheckBox totalCheckBox;
    /**
     * "Categories" box.
     */
    private final SortingCheckboxList<String> categoriesList;
    /**
     * List for selecting distribution property to monitor its lifecycle.
     */
    private final JList<String> distributionPropertiesList;
    /**
     * The lifecycle's chart panel.
     */
    private final ActionChartPanel<LifecycleModel<T>, CategoryChartTypes> chartPanel;
    /**
     * The lifecycle's multiple chart panel.
     */
    private final MultipleChartPanel<LifecycleModel<T>, CategoryChartTypes> multipleChartPanel;
    /**
     * Contains lifecycle related action and controls.
     */
    private final JToolBar toolBar;
    /**
     * Panel that holds the distribution list and categories.
     */
    private final JPanel configurePanel;
    /**
     * Split area between configuration panel and chart.
     */
    private final JSplitPane splitPane;

    /**
     * Initiates this {@link LifecycleAnalysisView}
     *
     * @param model
     * @param owner
     */
    public LifecycleAnalysisView(final LifecycleAnalysisModel<T> model, final LifecycleAnalysisConfigurationView<T> owner) {
	super(model, owner);
	this.addSelectionEventListener(createLifecycleAnalysisSelectionListener());

	this.chartPanel = createChartPanel(0);
	this.totalCheckBox = createTotalCheckBox(chartPanel);
	this.categoriesList = createCategoryPropertiesList(chartPanel);
	this.distributionPropertiesList = createDistributionList();
	this.multipleChartPanel = createMultipleChartPanel(chartPanel);
	this.configurePanel = createConfigPanel(totalCheckBox, distributionPropertiesList, categoriesList);
	this.splitPane = createSplitPanel(configurePanel, multipleChartPanel);
	this.toolBar = createChartTypeBar(createSwitchChartModel(multipleChartPanel));

	model.addLifecycleModelUpdatedListener(new ILifecycleModelUpdated() {

	    @SuppressWarnings("unchecked")
	    @Override
	    public void lifecycleModelUpdated(final LifecycleModelUpdateEvent<?> event) {
		updateChart((LifecycleModel<T>)event.getLifecycleModel(), null);
	    }
	});

	layoutComponents();
    }

    /**
     * Creates the {@link JSplitPane} with {@link #configurePanel} and {@link #chartPanel}.
     *
     * @param configurePanel
     * @param chartPanel
     * @return
     */
    private JSplitPane createSplitPanel(final JPanel configurePanel, final MultipleChartPanel<LifecycleModel<T>, CategoryChartTypes> chartPanel) {
	final JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
	splitPane.setOneTouchExpandable(true);
	splitPane.setLeftComponent(configurePanel);
	splitPane.setRightComponent(chartPanel);
	splitPane.setDividerLocation(1.0);
	return splitPane;
    }

    private JPanel createConfigPanel(final JCheckBox totalCheckBox, final JList<String> distributionPropertiesList, final SortingCheckboxList<String> categoriesList) {
	final TaskPanel distrPanel = new TaskPanel(new MigLayout("insets 0, fill", "[fill,grow]", "[c]0[fill,grow]"));
	distrPanel.setTitle("Distribution properties");
	distrPanel.setAnimated(false);
	final JScrollPane pane = new JScrollPane(distributionPropertiesList);
	//	pane.setPreferredSize(new Dimension(pane.getPreferredSize().width, 300));
	distrPanel.add(pane); //
	distrPanel.getCollapsiblePanel().setCollapsed(false);

	distrPanel.getCollapsiblePanel().revalidate();
	distrPanel.revalidate();

	final TaskPanel categoriesPanel = new TaskPanel(new MigLayout("insets 0, fill", "[fill,grow]", "[c]0[fill,grow]"));
	categoriesPanel.setTitle("Categories");
	categoriesPanel.setAnimated(false);

	final JScrollPane categoriesPane = new JScrollPane(categoriesList, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
	categoriesPane.setPreferredSize(new Dimension((int) categoriesPane.getPreferredSize().getWidth(), (int) pane.getPreferredSize().getHeight()));

	categoriesPanel.add(categoriesPane);
	categoriesPanel.getCollapsiblePanel().setCollapsed(false);

	categoriesPanel.getCollapsiblePanel().revalidate();
	categoriesPanel.revalidate();

	final JPanel configurePanel = new JPanel(new MigLayout("fill, insets 3", "[fill, grow]", "[top][top][top, grow]")); // []
	configurePanel.add(totalCheckBox, "wrap");
	configurePanel.add(categoriesPanel, "wrap");
	configurePanel.add(distrPanel);

	return configurePanel;
    }

    private void layoutComponents() {
	setLayout(new MigLayout("insets 0, fill", "[fill,grow]", "[][fill,grow]"));
	add(toolBar, "wrap");
	add(splitPane, "grow");
    }

    @Override
    public LifecycleAnalysisModel<T> getModel() {
        return (LifecycleAnalysisModel<T>)super.getModel();
    }

    private JCheckBox createTotalCheckBox(final ActionChartPanel<LifecycleModel<T>,CategoryChartTypes> chartPanel) {
	final JCheckBox totalCheckBox = new JCheckBox("Bla-bla", getModel().getTotal());
	totalCheckBox.setAction(new BlockingLayerCommand<Void>("Total", getOwner().getProgressLayer()) {
	    private static final long serialVersionUID = 1L;

	    @Override
	    protected boolean preAction() {
		totalCheckBox.setEnabled(false);
		setMessage("Updating...");
		if (!getOwner().getProgressLayer().isIncrementalLocking()) {
		    getOwner().getProgressLayer().enableIncrementalLocking();
		}
		return super.preAction();
	    }

	    @Override
	    protected Void action(final ActionEvent e) throws Exception {
		return null;
	    }

	    @Override
	    protected void postAction(final Void value) {
		getModel().adtme().setTotal(totalCheckBox.isSelected());
		if (CategoryChartTypes.STACKED_BAR_CHART.equals(chartPanel.getChartType())) {
		    updateChart();
		}
		try {
		    totalCheckBox.setEnabled(true);
		    super.postAction(value);
		} catch (final Exception e) {
		    e.printStackTrace();
		    // do nothing
		}
	    }

	    /**
	     * After default exception handling executed, post-actions should be performed to enable all necessary buttons, unlock layer etc.
	     */
	    @Override
	    protected void handlePreAndPostActionException(final Throwable ex) {
		totalCheckBox.setEnabled(true);
		super.handlePreAndPostActionException(ex);
		super.postAction(null);
	    }
	});
	totalCheckBox.setToolTipText("<html>"
		+ //
		"Indicates if <b>total</b> or <b>average</b> non-percent values should be used.<br><br>"
		+ //
		"<i>1. <b>average by entity</b> : show groups average values (based on entities number).<br>"
		+ "	Usage : compare groups in perspective of <b>entity performance</b>.<br><br>"
		+ //
		"2. <b>total for all</b> : show group total values.<br>"
		+ //
		"	Usage : compare groups in perspective of <b>total group performance</b>(no care about how much entities assemble group).<br> "
		+ "	In this case selection criteria for entities/period should be formed <b>fully</b> to get comparable results<br>"
		+ "       (every group should contain all appropriate entities and representable period)." //
		+ "</i></html>");
	return totalCheckBox;
    }

    protected MultipleChartPanel<LifecycleModel<T>, CategoryChartTypes> createMultipleChartPanel(final ActionChartPanel<LifecycleModel<T>, CategoryChartTypes> chartPanel) {
	final MultipleChartPanel<LifecycleModel<T>, CategoryChartTypes> multPanel = new MultipleChartPanel<LifecycleModel<T>, CategoryChartTypes>();
	multPanel.addChartPanel(chartPanel);
	return multPanel;
    }

    protected SwitchChartsModel<LifecycleModel<T>, CategoryChartTypes> createSwitchChartModel(final MultipleChartPanel<LifecycleModel<T>, CategoryChartTypes> multipleChartPanel) {
	return new SwitchChartsModel<LifecycleModel<T>, CategoryChartTypes>(multipleChartPanel) {
	    @Override
	    public ItemListener createListenerForChartType(final CategoryChartTypes type) {
		return new ChartTypeChangeListener(type) {
		    @Override
		    public void itemStateChanged(final ItemEvent e) {
			super.itemStateChanged(e);
			showFractionsConfiguration(CategoryChartTypes.STACKED_BAR_CHART.equals(type));
		    }
		};
	    }
	};
    }

    /**
     * Shows/hides "fractions" view configuration.
     *
     * @param show
     */
    public void showFractionsConfiguration(final boolean show) {
	splitPane.setOneTouchExpandable(show);
	splitPane.setLeftComponent(show ? configurePanel : null);
	if (show) {
	    splitPane.setDividerLocation(normalDividerLocation);
	}

	splitPane.invalidate();
	splitPane.validate();
	splitPane.repaint();
    }




    /**
     * Returns the {@link SortingCheckboxList} of aggregation properties.
     * @param chartPanel
     *
     * @return
     */
    private SortingCheckboxList<String> createCategoryPropertiesList(final ActionChartPanel<LifecycleModel<T>,CategoryChartTypes> chartPanel) {
	final DefaultListModel<String> listModel = new DefaultListModel<String>();

	final Class<T> root = getModel().getCriteria().getEntityClass();
	final ILifecycleAddToCategoriesTickManager secondTick = getModel().adtme().getSecondTick();

	for (final String categoryProperty : secondTick.checkedProperties(root)) {
	    try {
		if (!LifecycleDomainTreeManager.isLifecycle(root, categoryProperty)) {
		    listModel.addElement(categoryProperty);
		}
	    } catch (final IllegalArgumentException e) {
		 listModel.addElement(categoryProperty);
	    }
	}
	final SortingCheckboxList<String> aggregationList = new SortingCheckboxList<String>(listModel);
	aggregationList.setCellRenderer(new SortingCheckboxListCellRenderer<String>(aggregationList, new JCheckBox()) {

	    private static final long serialVersionUID = -6751336113879821723L;

	    private final EntityDescriptor ed = new EntityDescriptor(getModel().getCriteria().getManagedType(), secondTick.checkedProperties(root));

	    @Override
	    public Component getListCellRendererComponent(final JList<? extends String> list, final String value, final int index, final boolean isSelected, final boolean cellHasFocus) {
		final Component rendererComponent = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
		final Pair<String, String> titleAndDesc = ed.getTitleAndDesc(value);
		defaultRenderer.setText(titleAndDesc.getKey());
		setToolTipText(titleAndDesc.getValue());
		return rendererComponent;
	    }
	});
	aggregationList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

	final ListCheckingModel<String> checkingModel = new DomainTreeListCheckingModel<T>(root, secondTick);
	checkingModel.addListCheckingListener(new ListCheckingListener<String>() {

	    @Override
	    public void valueChanged(final ListCheckingEvent<String> e) {
		if (CategoryChartTypes.STACKED_BAR_CHART.equals(chartPanel.getChartType())) {
		    updateChart();
		}
	    }
	});
	aggregationList.setCheckingModel(checkingModel);

	final ListSortingModel<String> sortingModel = new DomainTreeListSortingModel<T>(root, secondTick, getModel().adtme().getRepresentation().getSecondTick());
	sortingModel.addSorterEventListener(new SorterEventListener<String>() {

	    @Override
	    public void valueChanged(final SorterChangedEvent<String> e) {
		if (CategoryChartTypes.STACKED_BAR_CHART.equals(chartPanel.getChartType())) {
		    updateChart();
		}
	    }
	});
	aggregationList.setSortingModel(sortingModel);

	return aggregationList;
    }

    protected JToolBar createChartTypeBar(final SwitchChartsModel<LifecycleModel<T>, CategoryChartTypes> switchChartModel) {
	final JToolBar toolBar = new JToolBar(JToolBar.HORIZONTAL);
	toolBar.setFloatable(false);
	toolBar.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));

	toolBar.add(getConfigureAction());
	toolBar.addSeparator();

	final JToggleButton barChart = createToggleButtonFor(switchChartModel, CategoryChartTypes.BAR_CHART, "Show availability", ResourceLoader.getIcon("images/chart_bar.png"));
	final JToggleButton stackedBarChart = createToggleButtonFor(switchChartModel, CategoryChartTypes.STACKED_BAR_CHART, "Show fractions", ResourceLoader.getIcon("images/chart_stacked_bar.png"));
	final JToggleButton lineChart = createToggleButtonFor(switchChartModel, CategoryChartTypes.LINE_CHART, "Show summary availability", ResourceLoader.getIcon("images/chart_line.png"));
	final ButtonGroup group = new ButtonGroup();
	group.add(barChart);
	group.add(stackedBarChart);
	group.add(lineChart);
	toolBar.add(barChart);
	toolBar.add(stackedBarChart);
	toolBar.add(lineChart);
	toolBar.addSeparator();

	final JLabel fromLabel = new JLabel("Period:");
	final JLabel toLabel = new JLabel("To");
	final BoundedValidationLayer<DatePickerLayer> fromEditor = getModel().getFromeEditor();
	fromEditor.getView().getUi().setCaption("choose period beginning...");
	final BoundedValidationLayer<DatePickerLayer> toEditor = getModel().getToEditor();
	toEditor.getView().getUi().setCaption("choose period ending...");

	final JPanel periodPanel = new JPanel(new MigLayout("fill, insets 5", "[:" + fromLabel.getMinimumSize().width + ":]" +
			"[grow,:" + 175 + ":][:" + toLabel.getMinimumSize().width + ":][grow,:" + 175 + ":]"));
	periodPanel.add(fromLabel);
	periodPanel.add(fromEditor, "growx");
	periodPanel.add(toLabel);
	periodPanel.add(toEditor, "growx");

	toolBar.add(periodPanel, "growx");
	barChart.setSelected(true);
	return toolBar;
    }

    private JToggleButton createToggleButtonFor(final SwitchChartsModel<LifecycleModel<T>, CategoryChartTypes> switchChartModel, final CategoryChartTypes type, final String toolTip, final Icon icon) {
	final JToggleButton chartTogle = new JToggleButton(icon);
	chartTogle.setToolTipText(toolTip);
	chartTogle.addItemListener(switchChartModel.createListenerForChartType(type));
	return chartTogle;
    }

    /**
     * Returns the {@link JList} of distribution properties.
     *
     * @return
     */
    private JList<String> createDistributionList() {
	final DefaultListModel<String> listModel = new DefaultListModel<String>();

	final Class<T> root = getModel().getCriteria().getEntityClass();
	final ILifecycleAddToDistributionTickManager firstTick = getModel().adtme().getFirstTick();

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

		return this;
	    }


	});

	//Selecting the first element in the distribution properties list.
	distributionList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	final List<String> usedProperties = firstTick.usedProperties(root);
	if (usedProperties.size() == 1) {
	    distributionList.setSelectedValue(usedProperties.get(0), true);
	}

	//Adds the listener that listens the property usage changes and synchronises them with ui model.
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
			updateChart();
		    }
		}
	    }
	});
	return distributionList;
    }

    protected ActionChartPanel<LifecycleModel<T>, CategoryChartTypes> createChartPanel(final int indexOfAppropriateChart) {
	final ActionChartPanel<LifecycleModel<T>, CategoryChartTypes> chartPanel = new ActionChartPanel<LifecycleModel<T>, CategoryChartTypes>(getModel().getChartFactory(), new IBlockingLayerProvider() {
	    @Override
	    public BlockingIndefiniteProgressLayer getBlockingLayer() {
		return getOwner().getProgressLayer();
	    }
	}, null, indexOfAppropriateChart, 400, 300, 0, 0, Integer.MAX_VALUE, Integer.MAX_VALUE, true, true, true, true, true, true) {
	    private static final long serialVersionUID = -4006162899347838630L;

	    @Override
	    public void mouseDoubleClicked(final ChartMouseEvent chartEvent) {
		performCustomAction(new AnalysisDataEvent<>(this, chartEvent));
	    }
	};
	chartPanel.setZoomFillPaint(new Color(255, 0, 0, 63));
	return chartPanel;
    }

    /**
     * Updates chartPanel by new specified life cycle data.
     *
     * @param data
     */
    public void updateChart(final LifecycleModel<T> data, final IAction postAction) {
	chartPanel.setPostAction(postAction);
	chartPanel.setChart(data, true);
    }

    protected void updateChart() {
	chartPanel.updateChart();
    }

    protected final MultipleChartPanel<LifecycleModel<T>, CategoryChartTypes> getMultipleChartPanel() {
	return multipleChartPanel;
    }

    @Override
    protected void enableRelatedActions(final boolean enable, final boolean navigate) {
	if(getCentre().getCriteriaPanel() != null){
	    getCentre().getDefaultAction().setEnabled(enable);
	}
	getCentre().getRunAction().setEnabled(enable);
    }

    /**
     * Returns the {@link ISelectionEventListener} that enables or disable appropriate actions when this analysis was selected.
     *
     * @return
     */
    private ISelectionEventListener createLifecycleAnalysisSelectionListener() {
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
		getCentre().getPaginator().setEnableActions(false, false);
		//Managing load and export enablements.
		getCentre().getExportAction().setEnabled(false);
		getCentre().getRunAction().setEnabled(true);
	    }
	};
    }

    private void performCustomAction(final AnalysisDataEvent<ChartMouseEvent> clickedData) {
	System.out.println("Custom lifecycle action");
    }
}
