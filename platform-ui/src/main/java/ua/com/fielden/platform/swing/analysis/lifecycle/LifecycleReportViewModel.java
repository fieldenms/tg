package ua.com.fielden.platform.swing.analysis.lifecycle;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;

import javax.swing.AbstractListModel;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
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

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.equery.lifecycle.LifecycleModel;
import ua.com.fielden.platform.reportquery.IDistributedProperty;
import ua.com.fielden.platform.selectioncheckbox.SelectionCheckBoxPanel;
import ua.com.fielden.platform.selectioncheckbox.SelectionCheckBoxPanel.IAction;
import ua.com.fielden.platform.swing.actions.BlockingLayerCommand;
import ua.com.fielden.platform.swing.analysis.IAnalysisReportModel;
import ua.com.fielden.platform.swing.categorychart.ActionChartPanel;
import ua.com.fielden.platform.swing.categorychart.AnalysisDoubleClickEvent;
import ua.com.fielden.platform.swing.categorychart.CategoryChartTypes;
import ua.com.fielden.platform.swing.categorychart.MultipleChartPanel;
import ua.com.fielden.platform.swing.categorychart.SwitchChartsModel;
import ua.com.fielden.platform.swing.components.blocking.BlockingIndefiniteProgressLayer;
import ua.com.fielden.platform.swing.components.blocking.IBlockingLayerProvider;
import ua.com.fielden.platform.swing.components.smart.datepicker.DatePickerLayer;
import ua.com.fielden.platform.swing.review.OrderingArrow;
import ua.com.fielden.platform.swing.taskpane.TaskPanel;
import ua.com.fielden.platform.types.ICategory;
import ua.com.fielden.platform.utils.Pair;
import ua.com.fielden.platform.utils.ResourceLoader;

public class LifecycleReportViewModel<T extends AbstractEntity, DAO extends IEntityDao<T>> implements IAnalysisReportModel {

    private final LifecycleChartReviewModel<T, DAO> lifecycleViewModel;

    private final BlockingIndefiniteProgressLayer tabPaneLayer;

    /**
     * List for selecting lifecycle property to monitor its lifecycle.
     */
    private final JComboBox lifecyclePropertiesList;

    /**
     * List for selecting distribution property to monitor its lifecycle.
     */
    private final JList distributionPropertiesList;

    private final ActionChartPanel<LifecycleModel<T>, CategoryChartTypes> chartPanel;
    private final JLabel fromLabel, toLabel, lifecycleLabel;
    private final DatePickerLayer fromEditor, toEditor;

    private final JCheckBox totalCheckBox;

    private final SwitchChartsModel<LifecycleModel<T>, CategoryChartTypes> switchChartModel;

    private IDistributedProperty lifecycleProperty = null;

    private final JPanel configurePanel;

    private final LifecycleChartFactory<T, DAO> chartFactory;
    /**
     * Label for "categories" box.
     */
    //    private final JLabel categoriesLabel;
    /**
     * "Categories" box.
     */
    private final SelectionCheckBoxPanel<ICategory, IDistributedProperty> categoriesBoxPanel;

    private final TaskPanel distrPanel;

    protected IDistributedProperty distributionProperty = null;

    private final JSplitPane splitPane;

    private final JPanel mainPanel;

    private final MultipleChartPanel<LifecycleModel<T>, CategoryChartTypes> multipleChartPanel;

    public LifecycleReportViewModel(final LifecycleChartReview<T, DAO> lifecycleView, final BlockingIndefiniteProgressLayer tabPaneLayer) {
	this(lifecycleView.getModel(), tabPaneLayer, null);

	lifecycleView.setLayout(new MigLayout("fill, insets 0"));
	lifecycleView.add(mainPanel, "grow");
    }

    public LifecycleReportViewModel(final LifecycleChartReviewModel<T, DAO> lifecycleViewModel, final BlockingIndefiniteProgressLayer tabPaneLayer, final LifecycleChartFactory<T, DAO> chartFactory) {
	this.lifecycleViewModel = lifecycleViewModel;
	this.chartFactory = chartFactory;
	this.tabPaneLayer = tabPaneLayer;
	this.categoriesBoxPanel = createCategoryCheckBoxes();

	lifecycleLabel = new JLabel("Lifecycle by:");
	lifecyclePropertiesList = createLifecyclePropertiesList();
	fromLabel = new JLabel("Period:");
	toLabel = new JLabel("To");
	fromEditor = new DatePickerLayer("choose period beginning...", Locale.getDefault(), true, lifecycleViewModel.getFrom(), 0L);
	toEditor = new DatePickerLayer("choose period ending...", Locale.getDefault(), true, lifecycleViewModel.getTo(), DatePickerLayer.defaultTimePortionMillisForTheEndOfDay());

	distrPanel = new TaskPanel(new MigLayout("insets 0, fill", "[fill,grow]", "[c]0[fill,grow]"));
	distrPanel.setTitle("Distribution properties");
	distrPanel.setAnimated(false);
	final JScrollPane pane = new JScrollPane(distributionPropertiesList = createDistributionPropertiesList());
	//	pane.setPreferredSize(new Dimension(pane.getPreferredSize().width, 300));
	distrPanel.add(pane); //
	distrPanel.getCollapsiblePanel().setCollapsed(false);

	distrPanel.getCollapsiblePanel().revalidate();
	distrPanel.revalidate();

	final TaskPanel categoriesPanel = new TaskPanel(new MigLayout("insets 0, fill", "[fill,grow]", "[c]0[fill,grow]"));
	categoriesPanel.setTitle("Categories");
	categoriesPanel.setAnimated(false);

	final JScrollPane categoriesPane = new JScrollPane(categoriesBoxPanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
	categoriesPane.setPreferredSize(new Dimension((int) categoriesPane.getPreferredSize().getWidth(), (int) pane.getPreferredSize().getHeight()));

	categoriesPanel.add(categoriesPane);
	categoriesPanel.getCollapsiblePanel().setCollapsed(false);

	categoriesPanel.getCollapsiblePanel().revalidate();
	categoriesPanel.revalidate();

	configurePanel = new JPanel(new MigLayout("fill, insets 3", "[fill, grow]", "[top][top][top, grow]")); // []
	configurePanel.add(totalCheckBox = createTotalCheckBox(lifecycleViewModel, tabPaneLayer), "wrap");
	configurePanel.add(categoriesPanel, "wrap");
	configurePanel.add(distrPanel);

	chartPanel = createChartPanel(0);
	multipleChartPanel = createMultipleChartPanel();

	splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
	splitPane.setOneTouchExpandable(true);
	splitPane.setLeftComponent(configurePanel);
	splitPane.setRightComponent(multipleChartPanel);

	switchChartModel = createSwitchChartModel();

	final JToolBar toolBar = createChartTypeBar(switchChartModel);
	splitPane.setDividerLocation(1.0);

	if (lifecycleViewModel.getLifecycleProperty() != null) {
	    lifecyclePropertiesList.setSelectedItem(lifecycleViewModel.getLifecycleProperty());
	} else {
	    lifecyclePropertiesList.setSelectedIndex(0);
	}

	if (lifecycleViewModel.getDistributionProperty() != null) {
	    distributionPropertiesList.setSelectedValue(lifecycleViewModel.getDistributionProperty(), true);
	} else {
	    distributionPropertiesList.setSelectedIndex(0);
	}

	mainPanel = new JPanel(new MigLayout("insets 0, fill", "[fill,grow]", "[][fill,grow]"));
	mainPanel.add(toolBar, "wrap");
	mainPanel.add(splitPane, "grow");

	if (this.chartFactory != null) {
	    lifecyclePropertiesList.setEnabled(false);
	    fromEditor.setEditable(false);
	    toEditor.setEditable(false);
	}
    }

    private JCheckBox createTotalCheckBox(final LifecycleChartReviewModel<T, DAO> lifecycleViewModel, final BlockingIndefiniteProgressLayer tabPaneLayer) {
	final JCheckBox totalCheckBox = new JCheckBox("Bla-bla", lifecycleViewModel.getTotal());
	totalCheckBox.setAction(new BlockingLayerCommand<Void>("Total", tabPaneLayer) {
	    private static final long serialVersionUID = 1L;

	    @Override
	    protected boolean preAction() {
		totalCheckBox.setEnabled(false);
		setMessage("Updating...");
		return super.preAction();
	    }

	    @Override
	    protected Void action(final ActionEvent e) throws Exception {
		return null;
	    }

	    @Override
	    protected void postAction(final Void value) {
		if (CategoryChartTypes.STACKED_BAR_CHART.equals(chartPanel.getChartType())) {
		    updateModel();
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

    protected MultipleChartPanel<LifecycleModel<T>, CategoryChartTypes> createMultipleChartPanel() {
	final MultipleChartPanel<LifecycleModel<T>, CategoryChartTypes> multPanel = new MultipleChartPanel<LifecycleModel<T>, CategoryChartTypes>();
	multPanel.addChartPanel(chartPanel);
	return multPanel;
    }

    protected SwitchChartsModel<LifecycleModel<T>, CategoryChartTypes> createSwitchChartModel() {
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

    private final int normalDividerLocation = 207;

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

    private SelectionCheckBoxPanel<ICategory, IDistributedProperty> createCategoryCheckBoxes() {
	return new SelectionCheckBoxPanel<ICategory, IDistributedProperty>(lifecycleViewModel.getOrdering(), new ICategory[] {}, new IAction() {
	    @Override
	    public void action() {
		if (CategoryChartTypes.STACKED_BAR_CHART.equals(chartPanel.getChartType())) {
		    updateModel();
		    updateChart();
		}
	    }
	}) {
	    private static final long serialVersionUID = 6510439178025269286L;

	    @Override
	    public void reloadTokens(final ICategory[] newTokens) {
		super.reloadTokens(newTokens);
		// provide rich tooltips for categories:
		for (final Entry<ICategory, Pair<CheckBox<ICategory>, OrderingArrow>> entry : getAvailableItems().entrySet()) {
		    entry.getValue().getKey().setToolTipText("<html><i>" + entry.getValue().getKey().getToken().getDesc() + "</i></html>");
		}
	    }
	};
    }

    public void updateModel() {
	lifecycleViewModel.setFrom(fromEditor.getDate());
	lifecycleViewModel.setTo(toEditor.getDate());
	lifecycleViewModel.setLifecycleProperty(lifecycleProperty);
	lifecycleViewModel.setDistributionProperty(distributionProperty);

	lifecycleViewModel.setOrdering(categoriesBoxPanel.getOrdering());
	lifecycleViewModel.setCategoriesFor(lifecycleProperty, categoriesBoxPanel.getSelectedItem());
	lifecycleViewModel.setTotal(totalCheckBox.isSelected());
    }

    protected JToolBar createChartTypeBar(final SwitchChartsModel<LifecycleModel<T>, CategoryChartTypes> switchChartModel) {
	final JToolBar toolBar = new JToolBar(JToolBar.HORIZONTAL);
	toolBar.setFloatable(false);
	toolBar.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));

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

	final JPanel periodPanel = new JPanel(new MigLayout("fill, insets 5", "[:" + lifecycleLabel.getMinimumSize().width + ":][grow,:" + 100 + ":][:"
		+ fromLabel.getMinimumSize().width + ":][grow,:" + 175 + ":][:" + toLabel.getMinimumSize().width + ":][grow,:" + 175 + ":]"));
	periodPanel.add(lifecycleLabel);
	periodPanel.add(lifecyclePropertiesList, "growx"); //new JScrollPane(
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

    private JComboBox createLifecyclePropertiesList() {
	final List<IDistributedProperty> propertiesVector = lifecycleViewModel.getLifecycleProperties();
	Collections.sort(propertiesVector);
	final JComboBox lifecycleList = new JComboBox(propertiesVector.toArray()) {
	    private static final long serialVersionUID = 1L;
	    //	    @Override
	    //	    public String getToolTipText(final MouseEvent evt) {
	    //	        final IDistributedProperty property = (IDistributedProperty) getModel().getElementAt(locationToIndex(evt.getPoint()));
	    //	        return property.getDesc();
	    //	    }
	};
	lifecycleList.setSelectedIndex(-1);
	lifecycleList.addItemListener(new ItemListener() {
	    private int selectedIndex = -1;

	    @Override
	    public void itemStateChanged(final ItemEvent e) {
		//		if (e.getValueIsAdjusting()) {
		//		    return;
		//		}
		final int currentlySelected = lifecycleList.getSelectedIndex();
		categoriesBoxPanel.setEnabled(currentlySelected >= 0);

		if (selectedIndex == currentlySelected) {
		    return;
		}
		if (selectedIndex >= 0) {
		    lifecycleViewModel.setCategoriesFor((IDistributedProperty) lifecycleList.getModel().getElementAt(selectedIndex), categoriesBoxPanel.getSelectedItem());
		}
		selectedIndex = currentlySelected;
		if (selectedIndex < 0) {
		    return;
		}
		final IDistributedProperty lifecycleProperty = (IDistributedProperty) lifecycleList.getModel().getElementAt(selectedIndex);
		LifecycleReportViewModel.this.lifecycleProperty = lifecycleProperty;

		lifecycleList.setToolTipText(lifecycleProperty.getTooltip());

		categoriesBoxPanel.reloadTokens(lifecycleViewModel.allCategories(LifecycleReportViewModel.this.lifecycleProperty));

		final List<ICategory> selectedItems = lifecycleViewModel.getCategoriesFor(lifecycleProperty);
		categoriesBoxPanel.setSelectedCheckBoxes(selectedItems, lifecycleProperty);

		//		SimpleLauncher.show("", null, categoriesBoxPanel);

		final List<IDistributedProperty> distributedProperties = lifecycleViewModel.distributionProperties(LifecycleReportViewModel.this.lifecycleProperty);
		distributionPropertiesList.setModel(new AbstractListModel() {
		    private static final long serialVersionUID = 1L;

		    @Override
		    public int getSize() {
			return distributedProperties.size();
		    }

		    @Override
		    public Object getElementAt(final int index) {
			return distributedProperties.get(index);
		    }
		});
		if (distributionPropertiesList.getModel().getSize() > 0) {
		    if (lifecycleViewModel.getDistributionProperty() != null) {
			distributionPropertiesList.setSelectedValue(lifecycleViewModel.getDistributionProperty(), true);
		    } else {
			distributionPropertiesList.setSelectedIndex(0);
		    }
		}

		splitPane.setDividerLocation(normalDividerLocation);
		splitPane.invalidate();
		splitPane.validate();
		splitPane.repaint();

		showFractionsConfiguration(CategoryChartTypes.STACKED_BAR_CHART.equals(chartPanel.getChartType()));
	    }
	});
	return lifecycleList;
    }

    private JList createDistributionPropertiesList() {
	final List<IDistributedProperty> propertiesVector = lifecycleViewModel.getCurrentDistributionProperties();
	//	Collections.sort(propertiesVector);
	final JList distributionPropertiesList = new JList(propertiesVector == null ? new IDistributedProperty[0] : propertiesVector.toArray()) {
	    private static final long serialVersionUID = 1L;

	    @Override
	    public String getToolTipText(final MouseEvent evt) {
		final IDistributedProperty property = (IDistributedProperty) getModel().getElementAt(locationToIndex(evt.getPoint()));
		return property.getTooltip();
	    }
	};
	distributionPropertiesList.setSelectedIndex(-1);
	distributionPropertiesList.addListSelectionListener(new ListSelectionListener() {

	    private int selectedIndex = -1;

	    @Override
	    public void valueChanged(final ListSelectionEvent e) {
		if (e.getValueIsAdjusting()) {
		    return;
		}
		final int currentlySelected = distributionPropertiesList.getSelectedIndex();
		if (selectedIndex == currentlySelected) {
		    return;
		}
		selectedIndex = currentlySelected;
		if (selectedIndex < 0) {
		    return;
		}
		final IDistributedProperty distributionProperty = (IDistributedProperty) distributionPropertiesList.getModel().getElementAt(selectedIndex);
		LifecycleReportViewModel.this.distributionProperty = distributionProperty;

		updateModel();
		updateChart();
	    }

	});
	distributionPropertiesList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	//	distributionPropertiesList.setMinimumSize(new Dimension(distributionPropertiesList.getWidth(), 400));
	return distributionPropertiesList;
    }

    private LifecycleChartFactory<T, DAO> getChartFactory() {
	return (chartFactory == null) ? lifecycleViewModel.getChartFactory(true) : chartFactory;
    }

    protected ActionChartPanel<LifecycleModel<T>, CategoryChartTypes> createChartPanel(final int indexOfAppropriateChart) {
	final ActionChartPanel<LifecycleModel<T>, CategoryChartTypes> chartPanel = new ActionChartPanel<LifecycleModel<T>, CategoryChartTypes>(getChartFactory(), new IBlockingLayerProvider() {
	    @Override
	    public BlockingIndefiniteProgressLayer getBlockingLayer() {
		return tabPaneLayer;
	    }
	}, null, indexOfAppropriateChart, 400, 300, 0, 0, Integer.MAX_VALUE, Integer.MAX_VALUE, true, true, true, true, true, true) {
	    private static final long serialVersionUID = -4006162899347838630L;

	    @Override
	    public void mouseDoubleClicked(final ChartMouseEvent chartEvent) {
		lifecycleViewModel.runDoubleClickAction(new AnalysisDoubleClickEvent(this, chartEvent));
	    }
	};
	chartPanel.setZoomFillPaint(new Color(255, 0, 0, 63));
	return chartPanel;
    }

    /**
     * Updates chartPanel by new specified lifecycle data.
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

    @Override
    public void restoreReportView(final Container container) throws IllegalStateException {
	throw new UnsupportedOperationException("Restoring lifecycle report view is unsupported yet.");
    }

    @Override
    public void createReportView(final Container container) throws IllegalStateException {
    }

    public JPanel getMainPanel() {
	return mainPanel;
    }

    public MultipleChartPanel<LifecycleModel<T>, CategoryChartTypes> getMultipleChartPanel() {
	return multipleChartPanel;
    }

    public LifecycleChartReviewModel<T, DAO> getLifecycleViewModel() {
	return lifecycleViewModel;
    }

}
