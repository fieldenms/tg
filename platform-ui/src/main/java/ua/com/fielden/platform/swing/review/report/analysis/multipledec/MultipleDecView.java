package ua.com.fielden.platform.swing.review.report.analysis.multipledec;

import java.awt.Color;
import java.awt.Component;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;
import javax.swing.border.EtchedBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import net.miginfocom.swing.MigLayout;

import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.entity.CategoryItemEntity;
import org.jfree.chart.entity.CategoryLabelEntity;
import org.jfree.chart.entity.ChartEntity;

import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.centre.analyses.IAbstractAnalysisDomainTreeManager.IUsageManager.IPropertyUsageListener;
import ua.com.fielden.platform.domaintree.centre.analyses.IAnalysisDomainTreeManager.IAnalysisAddToDistributionTickManager;
import ua.com.fielden.platform.domaintree.centre.analyses.IMultipleDecDomainTreeManager;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.reflection.development.EntityDescriptor;
import ua.com.fielden.platform.swing.categorychart.EntityWrapper;
import ua.com.fielden.platform.swing.review.details.AnalysisDetailsData;
import ua.com.fielden.platform.swing.review.report.analysis.multipledec.configuration.MultipleDecConfigurationView;
import ua.com.fielden.platform.swing.review.report.analysis.view.AbstractAnalysisReview;
import ua.com.fielden.platform.swing.review.report.analysis.view.AnalysisDataEvent;
import ua.com.fielden.platform.swing.review.report.events.SelectionEvent;
import ua.com.fielden.platform.swing.review.report.interfaces.ISelectionEventListener;
import ua.com.fielden.platform.swing.utils.DummyBuilder;
import ua.com.fielden.platform.utils.Pair;
import ua.com.fielden.platform.utils.ResourceLoader;

public class MultipleDecView<T extends AbstractEntity<?>> extends AbstractAnalysisReview<T, ICentreDomainTreeManagerAndEnhancer, IMultipleDecDomainTreeManager> {

    private static final long serialVersionUID = -8964933567376999746L;

    /**
     * Tool bar that allows to configure this analysis.
     */
    private final JToolBar toolBar;
    /**
     * The list of available distribution properties.
     */
    private final JList<String> distributionList;
    /**
     * The multiple dec panel.
     */
    private final NDecPanel<T> multipleDecView;

    public MultipleDecView(final MultipleDecModel<T> model, final MultipleDecConfigurationView<T> owner) {
	super(model, owner);
	this.toolBar = createToolBar();
	this.distributionList = createDistributionList();
	this.multipleDecView = createMultipleDecPanel();
	this.addSelectionEventListener(createMultipleDecSelectionListener());
	layoutComponents();
    }

    /**
     * Creates the multiple dec analysis tool bar.
     *
     * @return
     */
    private JToolBar createToolBar() {
	final JToolBar toolBar = new JToolBar(JToolBar.HORIZONTAL);
	toolBar.setFloatable(false);
	toolBar.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));

	getConfigureAction().putValue(Action.LARGE_ICON_KEY, ResourceLoader.getIcon("images/configure.png"));
	getConfigureAction().putValue(Action.SHORT_DESCRIPTION, "Configure analysis");

	toolBar.add(getConfigureAction());
	return toolBar;
    }

    @SuppressWarnings("unchecked")
    @Override
    public MultipleDecConfigurationView<T> getOwner() {
        return (MultipleDecConfigurationView<T>)super.getOwner();
    }

    /**
     * Creates the multiple dec panel.
     *
     * @return
     */
    private NDecPanel<T> createMultipleDecPanel() {
	final NDecPanel<T> multipleDecView = new NDecPanel<T>(getOwner().getMultipleDecModel(getModel().getChartModel()));
	multipleDecView.addAnalysisDoubleClickListener(createDoubleClickListener());
	return multipleDecView;
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
		    if (getModel().getChartModel().categoryProperties().contains(value)) {
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

    private void layoutComponents(){
	removeAll();

	//Configuring controls those allows to choose distribution properties.
	final JPanel leftPanel = new JPanel(new MigLayout("fill, insets 0", "[fill,grow]", "[][grow,fill]"));
	final JLabel distributionLabel = DummyBuilder.label("Distribution properties");
	leftPanel.add(distributionLabel, "wrap");
	leftPanel.add(new JScrollPane(distributionList));

	//Configuring main view panel.
	final JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
	splitPane.setOneTouchExpandable(true);
	splitPane.setLeftComponent(leftPanel);
	splitPane.setRightComponent(multipleDecView);

	setLayout(new MigLayout("insets 0, fill", "[fill,grow]", "[][fill,grow]"));
	add(toolBar, "wrap");
	add(splitPane, "grow");

	invalidate();
	validate();
	repaint();
    }

    @Override
    public MultipleDecModel<T> getModel() {
        return (MultipleDecModel<T>)super.getModel();
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
    private ISelectionEventListener createMultipleDecSelectionListener() {
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

    private IMultipleDecDoubleClickListener createDoubleClickListener() {
	return new IMultipleDecDoubleClickListener() {

	    @Override
	    public void doubleClick(final AnalysisDataEvent<ChartMouseEvent> event) {
		performCustomAction(event);
	    }
	};
    }

    private void performCustomAction(final AnalysisDataEvent<ChartMouseEvent> clickedData) {
	final ChartEntity entity = clickedData.getData().getEntity();
	if (entity instanceof CategoryItemEntity) {
	    getOwner().showDetails(createAnalysisData(((CategoryItemEntity) entity).getColumnKey()), AnalysisDetailsData.class);
	} else if (entity instanceof CategoryLabelEntity) {
	    getOwner().showDetails(createAnalysisData(((CategoryLabelEntity) entity).getKey()), AnalysisDetailsData.class);
	}
    }

    private AnalysisDetailsData<T> createAnalysisData(final Comparable<?> columnKey) {
	final List<Pair<String, Object>> linkPropValues = new ArrayList<>();
	final EntityWrapper entityWrapper = (EntityWrapper) columnKey;
	final List<String> categories = getModel().getChartModel().categoryProperties();
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
