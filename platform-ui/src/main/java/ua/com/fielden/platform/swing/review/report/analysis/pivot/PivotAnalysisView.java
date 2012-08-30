package ua.com.fielden.platform.swing.review.report.analysis.pivot;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;
import javax.swing.border.EtchedBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableColumnModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;
import javax.swing.tree.TreePath;

import net.miginfocom.swing.MigLayout;

import org.apache.commons.lang.StringUtils;
import org.jdesktop.swingx.treetable.TreeTableNode;

import ua.com.fielden.platform.criteria.generator.ICriteriaGenerator;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.centre.analyses.IPivotDomainTreeManager;
import ua.com.fielden.platform.domaintree.centre.analyses.IPivotDomainTreeManager.IPivotAddToAggregationTickManager;
import ua.com.fielden.platform.domaintree.centre.analyses.IPivotDomainTreeManager.IPivotAddToDistributionTickManager;
import ua.com.fielden.platform.domaintree.impl.AbstractDomainTree;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.CritOnly;
import ua.com.fielden.platform.entity.annotation.CritOnly.Type;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.reflection.AnnotationReflector;
import ua.com.fielden.platform.reflection.PropertyTypeDeterminator;
import ua.com.fielden.platform.reflection.development.EntityDescriptor;
import ua.com.fielden.platform.swing.actions.BlockingLayerCommand;
import ua.com.fielden.platform.swing.analysis.DetailsFrame;
import ua.com.fielden.platform.swing.categorychart.AnalysisListDragFromSupport;
import ua.com.fielden.platform.swing.categorychart.AnalysisListDragToSupport;
import ua.com.fielden.platform.swing.checkboxlist.CheckboxList;
import ua.com.fielden.platform.swing.checkboxlist.CheckboxListCellRenderer;
import ua.com.fielden.platform.swing.checkboxlist.ListCheckingEvent;
import ua.com.fielden.platform.swing.checkboxlist.ListCheckingListener;
import ua.com.fielden.platform.swing.checkboxlist.ListCheckingModel;
import ua.com.fielden.platform.swing.checkboxlist.ListSortingModel;
import ua.com.fielden.platform.swing.checkboxlist.SortingCheckboxList;
import ua.com.fielden.platform.swing.checkboxlist.SortingCheckboxListCellRenderer;
import ua.com.fielden.platform.swing.components.blocking.BlockingIndefiniteProgressLayer;
import ua.com.fielden.platform.swing.dnd.DnDSupport2;
import ua.com.fielden.platform.swing.menu.filter.IFilter;
import ua.com.fielden.platform.swing.menu.filter.WordFilter;
import ua.com.fielden.platform.swing.pivot.analysis.dnd.PivotListDragToSupport;
import ua.com.fielden.platform.swing.review.IEntityMasterManager;
import ua.com.fielden.platform.swing.review.report.analysis.details.configuration.AnalysisDetailsConfigurationModel;
import ua.com.fielden.platform.swing.review.report.analysis.details.configuration.AnalysisDetailsConfigurationView;
import ua.com.fielden.platform.swing.review.report.analysis.pivot.configuration.PivotAnalysisConfigurationView;
import ua.com.fielden.platform.swing.review.report.analysis.view.AbstractAnalysisReview;
import ua.com.fielden.platform.swing.review.report.analysis.view.AnalysisDataEvent;
import ua.com.fielden.platform.swing.review.report.analysis.view.DomainTreeListCheckingModel;
import ua.com.fielden.platform.swing.review.report.analysis.view.DomainTreeListSortingModel;
import ua.com.fielden.platform.swing.review.report.centre.configuration.MultipleAnalysisEntityCentreConfigurationView;
import ua.com.fielden.platform.swing.review.report.events.LoadEvent;
import ua.com.fielden.platform.swing.review.report.events.SelectionEvent;
import ua.com.fielden.platform.swing.review.report.interfaces.ILoadListener;
import ua.com.fielden.platform.swing.review.report.interfaces.ISelectionEventListener;
import ua.com.fielden.platform.swing.treetable.FilterableTreeTableModel;
import ua.com.fielden.platform.swing.treetable.FilterableTreeTablePanel;
import ua.com.fielden.platform.swing.utils.DummyBuilder;
import ua.com.fielden.platform.swing.view.ICloseHook;
import ua.com.fielden.platform.utils.EntityUtils;
import ua.com.fielden.platform.utils.Pair;
import ua.com.fielden.platform.utils.ResourceLoader;

/**
 * {@link AbstractAnalysisReview} implementation for pivot analysis.
 *
 * @author TG Team
 *
 * @param <T>
 */
public class PivotAnalysisView<T extends AbstractEntity<?>> extends AbstractAnalysisReview<T, ICentreDomainTreeManagerAndEnhancer, IPivotDomainTreeManager, Void> {

    private static final long serialVersionUID = 8295216779213506230L;

    /**
     * Represents the list of available distribution properties.
     */
    private final CheckboxList<String> distributionList;
    /**
     * Represents the list of available aggregation properties.
     */
    private final SortingCheckboxList<String> aggregationList;
    /**
     * Pivot table that displays the analysis result.
     */
    private final FilterableTreeTablePanel<PivotTreeTable> pivotTablePanel;
    /**
     * Tool bar that contain "configure analysis" button.
     */
    private final JToolBar toolBar;

    /**
     * Initialises {@link PivotAnalysisView} with appropriate model and {@link PivotAnalysisConfigurationView} instance.
     *
     * @param model
     * @param owner
     */
    public PivotAnalysisView(final PivotAnalysisModel<T> model, final PivotAnalysisConfigurationView<T> owner) {
	super(model, owner);

	this.distributionList = createDistributionList();
	this.aggregationList = createAggregationList();
	this.pivotTablePanel = createPivotTreeTablePanel();
	this.toolBar = createPivotToolBar();

	DnDSupport2.installDnDSupport(distributionList, new AnalysisListDragFromSupport(distributionList),//
		new AnalysisListDragToSupport<T>(distributionList, getModel().getCriteria().getEntityClass(), getModel().adtme().getFirstTick()), true);
	DnDSupport2.installDnDSupport(aggregationList, new AnalysisListDragFromSupport(aggregationList), //
		new PivotListDragToSupport<T>(aggregationList, pivotTablePanel.getTreeTable(), getModel()), true);
	this.addSelectionEventListener(createPivotAnalysisSelectionListener());
	layoutComponents();
    }

    @Override
    public PivotAnalysisModel<T> getModel() {
	return (PivotAnalysisModel<T>)super.getModel();
    }

    @Override
    protected void enableRelatedActions(final boolean enable, final boolean navigate) {
	if(getCentre().getCriteriaPanel() != null){
	    getCentre().getDefaultAction().setEnabled(enable);
	}
	getCentre().getExportAction().setEnabled(enable);
	getCentre().getRunAction().setEnabled(enable);
    }

    /**
     * Returns the {@link ISelectionEventListener} that enables or disable appropriate actions when this analysis was selected.
     *
     * @return
     */
    private ISelectionEventListener createPivotAnalysisSelectionListener() {
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
		getCentre().getExportAction().setEnabled(true);
		getCentre().getRunAction().setEnabled(true);
	    }
	};
    }

    /**
     * Creates the list of distribution properties. That list supports highlighting of the queried properties and drag&drop.
     *
     * @return
     */
    private CheckboxList<String> createDistributionList() {
	final DefaultListModel<String> listModel = new DefaultListModel<String>();

	final Class<T> root = getModel().getCriteria().getEntityClass();
	final IPivotAddToDistributionTickManager firstTick = getModel().adtme().getFirstTick();

	for (final String distributionProperty : firstTick.checkedProperties(root)) {
	    listModel.addElement(distributionProperty);
	}
	final CheckboxList<String> distributionList = new CheckboxList<String>(listModel);
	distributionList.setCellRenderer(new CheckboxListCellRenderer<String>(new JCheckBox()) {

	    private static final long serialVersionUID = 7712966992046861840L;

	    private final EntityDescriptor ed = new EntityDescriptor(getModel().getCriteria().getManagedType(), firstTick.checkedProperties(root));

	    @Override
	    public Component getListCellRendererComponent(final JList<? extends String> list, final String value, final int index, final boolean isSelected, final boolean cellHasFocus) {

		final Component rendererComponent = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

		final Pair<String, String> titleAndDesc = ed.getTitleAndDesc(value.toString());
		defaultRenderer.setText(titleAndDesc.getKey());
		setToolTipText(titleAndDesc.getValue());
		if (!isSelected) {
		    if (getModel().getPivotModel().categoryProperties().contains(value)) {
			rendererComponent.setBackground(new Color(175, 240, 208));
		    } else {
			rendererComponent.setBackground(Color.WHITE);
		    }
		}
		return rendererComponent;
	    }
	});
	distributionList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	final ListCheckingModel<String> checkingModel = new DomainTreeListCheckingModel<T>(root, firstTick);
	checkingModel.addListCheckingListener(new ListCheckingListener<String>() {

	    @Override
	    public void valueChanged(final ListCheckingEvent<String> e) {
		refreshPivotTable(pivotTablePanel.getTreeTable());
	    }
	});
	distributionList.setCheckingModel(checkingModel);
	return distributionList;
    }

    /**
     * Creates the list of checked aggregation properties. That list supports Drag&Drop and highlighting of queried properties.
     *
     * @return
     */
    private SortingCheckboxList<String> createAggregationList() {
	final DefaultListModel<String> listModel = new DefaultListModel<String>();

	final Class<T> root = getModel().getCriteria().getEntityClass();
	final IPivotAddToAggregationTickManager secondTick = getModel().adtme().getSecondTick();

	for (final String aggregationProperty : secondTick.checkedProperties(root)) {
	    listModel.addElement(aggregationProperty);
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
		if (!isSelected) {
		    if (getModel().getPivotModel().aggregatedProperties().contains(value)) {
			rendererComponent.setBackground(new Color(175, 240, 208));
		    } else {
			rendererComponent.setBackground(Color.WHITE);
		    }
		}
		return rendererComponent;
	    }
	});
	aggregationList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	final ListCheckingModel<String> checkingModel= new DomainTreeListCheckingModel<T>(root, secondTick);
	checkingModel.addListCheckingListener(new ListCheckingListener<String>() {

	    @Override
	    public void valueChanged(final ListCheckingEvent<String> e) {
		refreshPivotTable(pivotTablePanel.getTreeTable());
	    }
	});
	aggregationList.setCheckingModel(checkingModel);
	final ListSortingModel<String> sortingModel = new DomainTreeListSortingModel<T>(root, secondTick, getModel().adtme().getRepresentation().getSecondTick());
	aggregationList.setSortingModel(sortingModel);

	getModel().getPivotModel().addSorterChangeListener(new PivotTableSorterListener() {

	    @Override
	    public void sorterChanged(final PivotSorterChangeEvent event) {
		final PivotTreeTable pivotTable = pivotTablePanel.getTreeTable();
		final FilterableTreeTableModel filterableModel = pivotTable.getFilterableModel();
		final TreeTableNode rootNode = filterableModel.getOriginModel().getRoot();
		final TreePath selectedPath = pivotTable.getPathForRow(pivotTable.getSelectedRow());
		if (rootNode != null) {
		    final Enumeration<?> expandedPaths = pivotTable.getExpandedDescendants(new TreePath(rootNode));
		    filterableModel.reload();
		    while (expandedPaths != null && expandedPaths.hasMoreElements()) {
			final TreePath path = (TreePath) expandedPaths.nextElement();
			pivotTable.expandPath(path);
		    }
		}
		pivotTable.scrollPathToVisible(selectedPath);
		pivotTable.getSelectionModel().setSelectionInterval(0, pivotTable.getRowForPath(selectedPath));
	    }
	});
	return aggregationList;
    }

    private FilterableTreeTablePanel<PivotTreeTable> createPivotTreeTablePanel() {
	final PivotTreeTable treeTable = new PivotTreeTable(new FilterableTreeTableModel(getModel().getPivotModel()));
	final FilterableTreeTablePanel<PivotTreeTable> pivotTablePanel = new FilterableTreeTablePanel<PivotTreeTable>(treeTable, createPivotFilter(), "find item");
	treeTable.addMouseListener(createDoubleClickListener(treeTable));
	treeTable.getColumnModel().addColumnModelListener(createColumnModelListener(treeTable, createColumnWidthChangeListener(treeTable)));
	refreshPivotTable(treeTable);
	return pivotTablePanel;
    }

    /**
     * Creates the table column listener for the tree table model that updates the column's width.
     *
     * @param columnWidthChangeListener
     * @return
     */
    private TableColumnModelListener createColumnModelListener(final PivotTreeTable treeTable, final PropertyChangeListener columnWidthChangeListener) {
	return new TableColumnModelListener() {

	    @Override
	    public void columnSelectionChanged(final ListSelectionEvent e) {}

	    @Override
	    public void columnRemoved(final TableColumnModelEvent e) {}

	    @Override
	    public void columnMoved(final TableColumnModelEvent e) {}

	    @Override
	    public void columnMarginChanged(final ChangeEvent e) {}

	    @Override
	    public void columnAdded(final TableColumnModelEvent e) {
		final int columnIndex = e.getToIndex();
		final TableColumn column = treeTable.getColumnModel().getColumn(columnIndex);
		final Class<T> root = getModel().getCriteria().getEntityClass();
		final IPivotAddToDistributionTickManager firstTick = getModel().adtme().getFirstTick();
		final IPivotAddToAggregationTickManager secondTick = getModel().adtme().getSecondTick();
		final List<String> firstTickUsed = firstTick.usedProperties(root);
		int width = 0;
		if(treeTable.isHierarchical(columnIndex) && firstTickUsed.size() > 0){
		    width = firstTick.getWidth(root, firstTick.usedProperties(root).get(0));
		}else if(columnIndex > 0){
		    width = secondTick.getWidth(root, secondTick.usedProperties(root).get(columnIndex - 1));
		}
		if(width > 0){
		    column.setPreferredWidth(width);
		}
		column.addPropertyChangeListener(columnWidthChangeListener);
	    }
	};
    }

    /**
     * Creates the column's width property change listener. Updates model property's width.
     *
     * @param pivotTable
     * @return
     */
    private PropertyChangeListener createColumnWidthChangeListener(final PivotTreeTable pivotTable) {
	return new PropertyChangeListener() {

	    @Override
	    public void propertyChange(final PropertyChangeEvent evt) {
		if (evt.getPropertyName().equals("width")) {
		    final Class<T> root = getModel().getCriteria().getEntityClass();
		    final IPivotAddToDistributionTickManager firstTick = getModel().adtme().getFirstTick();
		    final IPivotAddToAggregationTickManager secondTick = getModel().adtme().getSecondTick();
		    final List<String> usedDistributionProperties = firstTick.usedProperties(root);
		    final int columnIndex = pivotTable.getColumnModel().getColumnIndex(((TableColumn)evt.getSource()).getIdentifier());
		    if (pivotTable.isHierarchical(columnIndex) && !usedDistributionProperties.isEmpty()) {
			firstTick.setWidth(root, firstTick.usedProperties(root).get(0), ((Integer) evt.getNewValue()).intValue());
		    } else if (!pivotTable.isHierarchical(columnIndex)) {
			secondTick.setWidth(root, secondTick.usedProperties(root).get(columnIndex - 1), ((Integer) evt.getNewValue()).intValue());
		    }
		}
	    }
	};
    }

    private IFilter createPivotFilter() {
	return new WordFilter() {
	    @Override
	    public boolean filter(final Object value, final String valuefilterCrit) {
		return super.filter(((PivotTreeTableNode) value).getValueAt(0), valuefilterCrit);
	    }
	};
    }

    private MouseListener createDoubleClickListener(final PivotTreeTable treeTable) {
	return new MouseAdapter() {

	    @Override
	    public void mouseClicked(final MouseEvent e) {
		super.mouseClicked(e);
		if (e.getClickCount() == 2) {
		    final TreePath treePath = treeTable.getPathForLocation(e.getX(), e.getY());
		    if (treePath.getPathCount() > 2) {
			TreePath newPath = new TreePath(treePath.getPathComponent(2));
			for (int index = 3; index < treePath.getPathCount(); index++) {
			    newPath = newPath.pathByAddingChild(treePath.getPathComponent(index));
			}
			final List<Pair<String, Object>> choosenProperty = createChoosenProperty(newPath);
			performCustomAction(new AnalysisDataEvent<>(PivotAnalysisView.this, choosenProperty));
		    }
		}
	    }

	    private List<Pair<String, Object>> createChoosenProperty(final TreePath newPath) {
		final List<Pair<String, Object>> choosenItems = new ArrayList<>();
		final List<String> distributionProperties = getModel().getPivotModel().categoryProperties();
		for (int index = 0; index < newPath.getPathCount(); index++) {
		    final PivotTreeTableNode node = (PivotTreeTableNode) newPath.getPathComponent(index);
		    final String distributionProperty = distributionProperties.get(index);
		    final Object value = node.getUserObject().equals(PivotTreeTableNode.NULL_USER_OBJECT) ? null : node.getUserObject();
		    choosenItems.add(new Pair<>(distributionProperty, value));
		}
		return choosenItems;
	    }

	};
    }

    /**
     * Creates the tool bar with "configure analysis" button.
     *
     * @return
     */
    private JToolBar createPivotToolBar() {
	final JToolBar toolBar = new JToolBar(JToolBar.HORIZONTAL);
	toolBar.setFloatable(false);
	toolBar.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));

	getConfigureAction().putValue(Action.LARGE_ICON_KEY, ResourceLoader.getIcon("images/configure.png"));
	getConfigureAction().putValue(Action.SHORT_DESCRIPTION, "Configure analysis");

	toolBar.add(getConfigureAction());
	return toolBar;
    }


    //////////////////////Refactor code below//////////////////////////////////
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
	final JPanel leftDownPanel = new JPanel(new MigLayout("fill, insets 0", "[fill,grow]", "[][fill,grow]"));
	final JLabel aggregationLabel = DummyBuilder.label("Aggregation properties");
	leftDownPanel.add(aggregationLabel, "wrap");
	leftDownPanel.add(new JScrollPane(aggregationList));

	//Configuring controls for pivot tree table.
	final JPanel rightPanel = new JPanel(new MigLayout("fill, insets 3", "[fill,grow]", "[][fill,grow]"));
	rightPanel.add(toolBar, "wrap");
	rightPanel.add(pivotTablePanel);

	//Configuring left panel with distribution and aggregation list properties.
	leftPane.setOneTouchExpandable(true);
	leftPane.setTopComponent(leftTopPanel);
	leftPane.setBottomComponent(leftDownPanel);

	//Configuring main view panel.
	splitPane.setOneTouchExpandable(true);
	splitPane.setLeftComponent(leftPane);
	splitPane.setRightComponent(rightPanel);

	add(splitPane);
    }

    /**
     * Refreshes the pivot tree table.
     * @param treeTable
     */
    private static void refreshPivotTable(final PivotTreeTable treeTable){
	final TreePath selectedPath = treeTable.getPathForRow(treeTable.getSelectedRow());
	((AbstractTableModel) treeTable.getModel()).fireTableStructureChanged();
	treeTable.getSelectionModel().setSelectionInterval(0, treeTable.getRowForPath(selectedPath));
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void performCustomAction(final AnalysisDataEvent<?> clickedData) {
	createDoubleClickAction((List<Pair<String, Object>>)clickedData.getData()).actionPerformed(null);
    }

    protected final Action createDoubleClickAction(final List<Pair<String, Object>> choosenItems) {
   	return new BlockingLayerCommand<Void>("Details", getOwner().getProgressLayer()) {

   	    private static final long serialVersionUID = 1986658954874008023L;

   	    @Override
   	    protected final Void action(final ActionEvent e) throws Exception {
   		return null;
   	    }

   	    @SuppressWarnings("unchecked")
   	    @Override
   	    protected void postAction(final Void value) {
   		super.postAction(value);
   		final List<Object> entityKey = createKey(choosenItems);
   		DetailsFrame detailsFrame = getOwner().getDetailsFrame(entityKey);
   		if (detailsFrame == null) {
   		    final Class<T> root = getModel().getCriteria().getEntityClass();

   		    final EntityFactory entityFactory = getModel().getCriteria().getEntityFactory();

   		    final ICriteriaGenerator criteriaGenerator = ((MultipleAnalysisEntityCentreConfigurationView<T>)getOwner().getOwner().getOwner()).getModel().getCriteriaGenerator();

   		    final IEntityMasterManager masterManager = getOwner().getOwner().getModel().getMasterManager();

   		    final ICentreDomainTreeManagerAndEnhancer cdtme = getModel().getCriteria().getCentreDomainTreeManagerAndEnhnacerCopy();
   		    cdtme.setRunAutomatically(true);
   		    setValueFor(root, cdtme, choosenItems);

   		    final String frameTitle = createFrameTitle(choosenItems);

   		    final AnalysisDetailsConfigurationModel<T> detailsConfigModel = new AnalysisDetailsConfigurationModel<>(//
   			    getModel().getCriteria().getEntityClass(),//
   			    frameTitle,//
   			    entityFactory,//
   			    criteriaGenerator, masterManager, cdtme);
   		    final BlockingIndefiniteProgressLayer progressLayer = new BlockingIndefiniteProgressLayer(null, "Loading");
   		    final AnalysisDetailsConfigurationView<T> detailsConfigView = new AnalysisDetailsConfigurationView<>(detailsConfigModel, progressLayer);
   		    progressLayer.setView(detailsConfigView);
   		    detailsFrame = new DetailsFrame(entityKey,//
   			    frameTitle,//
   			    progressLayer, new ICloseHook<DetailsFrame>() {

   			@Override
   			public void closed(final DetailsFrame frame) {
   			    getOwner().removeDetailsFrame(frame);
   			}

   		    });
   		    getOwner().addDetailsFrame(detailsFrame);
   		    detailsConfigView.open();
   		}
   		detailsFrame.setVisible(true);

   	    }

	    private void setValueFor(final Class<T> root, final ICentreDomainTreeManagerAndEnhancer newCdtme, final List<Pair<String, Object>> choosenItems) {
		for (final Pair<String, Object> choosenItem : choosenItems) {
		    setValueFor(root, newCdtme, choosenItem);
		}
	    }

   	    @SuppressWarnings("unchecked")
   	    private void setValueFor(final Class<T> root, final ICentreDomainTreeManagerAndEnhancer newCdtme, final Pair<String, Object> choosenItem){
   		final Class<T> managedType = (Class<T>)newCdtme.getEnhancer().getManagedType(root);
   		final boolean isEntityItself = "".equals(choosenItem.getKey()); // empty property means "entity itself"
   		final Class<?> propertyType = isEntityItself ? managedType : PropertyTypeDeterminator.determinePropertyType(managedType, choosenItem.getKey());
   		final CritOnly critOnlyAnnotation = isEntityItself ? null : AnnotationReflector.getPropertyAnnotation(CritOnly.class, managedType, choosenItem.getKey());
   		final boolean isEntity = EntityUtils.isEntityType(propertyType);
   		final boolean isSingle = critOnlyAnnotation != null && Type.SINGLE.equals(critOnlyAnnotation.value());
   		final Class<?> newPropertyType = isEntity ? (isSingle ? propertyType : List.class) : (EntityUtils.isBoolean(propertyType) ? Boolean.class : propertyType);

   		Object value = choosenItem.getValue();
   		if (List.class.isAssignableFrom(newPropertyType) && choosenItem.getValue() != null) {
   		    final List<Object> values = new ArrayList<>();
   		    values.add(choosenItem.getValue().toString());
   		    value = values;
   		}

   		if(choosenItem.getValue() == null){
   		    newCdtme.getFirstTick().setOrNull(root, choosenItem.getKey(), true);
   		}

   		newCdtme.getFirstTick().setValue(root, choosenItem.getKey(), value);

   		if(AbstractDomainTree.isDoubleCriterion(managedType, choosenItem.getKey())){
   		    newCdtme.getFirstTick().setValue2(root, choosenItem.getKey(), value);
   		}

   		if(EntityUtils.isBoolean(propertyType)){
   		    if(choosenItem.getValue() != null){
   			newCdtme.getFirstTick().setValue2(root, choosenItem.getKey(), !(Boolean)value);
   		    }else{
   			newCdtme.getFirstTick().setValue2(root, choosenItem.getKey(), null);
   		    }
   		}


   	    }

   	    private String createFrameTitle(final List<Pair<String, Object>> choosenItems) {
   		final String reportName = getReportName();
   		return "Details for " + createDistributionPropertyTitle(choosenItems) + " " + createDistributionEntitiesTitle(choosenItems) + " (" + (StringUtils.isEmpty(reportName) ?
   			"" :reportName + ": ") + getOwner().getModel().getName() + ")";
   	    }

   	    private String getReportName() {
   		final String name = getOwner().getOwner().getModel().getName();
   		return StringUtils.isEmpty(name) ? "" : name;
   	    }

   	    private String createDistributionEntitiesTitle(final List<Pair<String, Object>> choosenItems) {
   		String titles = "";

		for (final Pair<String, Object> pair : choosenItems) {
		    titles += ", " + createPairString(pair.getValue());
		}
		return titles.isEmpty() ? titles : titles.substring(2);
   	    }

   	    private String createPairString(final Object value) {
   		if (value instanceof AbstractEntity) {
   		    return ((AbstractEntity<?>) value).getKey().toString() + " \u2012 " + ((AbstractEntity<?>) value).getDesc();
   		} else if (value != null) {
   		    return value.toString() + " \u2012 " + value.toString();
   		} else {
   		    return "UNKNOWN \u2012 UNKNOWN";
   		}
   	    }

   	    private String createDistributionPropertyTitle(final List<Pair<String, Object>> choosenItems) {
   		String name = "";
   		final Class<T> managedType = getModel().getCriteria().getManagedType();
   		final EntityDescriptor ed = new EntityDescriptor(managedType, getModel().getPivotModel().categoryProperties());

		for (final Pair<String, Object> pair : choosenItems) {
		    name += '\u2192' + "(" + ed.getTitle(pair.getKey()) + ")";
		}
		return name.isEmpty() ? name : name.substring(1);
   	    }

	    private List<Object> createKey(final List<Pair<String, Object>> choosenItems) {
		final List<Object> keyList = new ArrayList<Object>();
		for (final Pair<String, Object> pair : choosenItems) {
		    keyList.add(pair.getValue());
		}
		return keyList;
	    }
   	};
       }


    //TODO needed for exporting data in to excel file.
    //    public Result exportDataIntoFile(final File file, final List<? extends IBindingEntity> analysisData) {
    //	final GroupItem rootItem = dataProvider.createBasicTreeFrom(analysisData);
    //	final PivotTreeTableNode rootNode = rootItem.createTree("Grand totals", treeTableModel);
    //	rootNode.sort(treeTableModel.getTreeTableSorter());
    //	try {
    //	    final byte[] content = getContent(rootNode);
    //	    FileOutputStream fo;
    //	    fo = new FileOutputStream(file);
    //	    fo.write(content);
    //	    fo.flush();
    //	    fo.close();
    //	} catch (final Exception e) {
    //	    return new Result(e);
    //	}
    //	return Result.successful(file);
    //    }
    //
    //    private byte[] getContent(final PivotTreeTableNode rootNode) throws IOException {
    //	final HSSFWorkbook wb = new HSSFWorkbook();
    //	final HSSFSheet sheet = wb.createSheet("Exported Data");
    //	// Create a header row.
    //	final HSSFRow headerRow = sheet.createRow(0);
    //	// Create a new font and alter it
    //	final HSSFFont font = wb.createFont();
    //	font.setFontHeightInPoints((short) 12);
    //	font.setFontName("Courier New");
    //	font.setBoldweight((short) 1000);
    //	// Fonts are set into a style so create a new one to use
    //	final HSSFCellStyle headerCellStyle = wb.createCellStyle();
    //	headerCellStyle.setFont(font);
    //	headerCellStyle.setBorderBottom(HSSFCellStyle.BORDER_THIN);
    //	final HSSFCellStyle headerInnerCellStyle = wb.createCellStyle();
    //	headerInnerCellStyle.setFont(font);
    //	headerInnerCellStyle.setBorderBottom(HSSFCellStyle.BORDER_THIN);
    //	headerInnerCellStyle.setBorderRight(HSSFCellStyle.BORDER_HAIR);
    //	final String propertyTitles[] = initPropertyTitles();
    //	// Create cells and put column names there
    //	for (int index = 0; index < propertyTitles.length; index++) {
    //	    final HSSFCell cell = headerRow.createCell(index);
    //	    cell.setCellValue(propertyTitles[index]);
    //	    cell.setCellStyle(index < propertyTitles.length - 1 ? headerInnerCellStyle : headerCellStyle);
    //	}
    //
    //	// let's make cell style to handle borders
    //	final HSSFCellStyle dataCellStyle = wb.createCellStyle();
    //	dataCellStyle.setBorderRight(HSSFCellStyle.BORDER_HAIR);
    //
    //	//exporting created tree.
    //	traceTree(new TreePath(rootNode), sheet, dataCellStyle, getSelectedDistributionProperties().size(), 1);
    //
    //	final ByteArrayOutputStream oStream = new ByteArrayOutputStream();
    //
    //	wb.write(oStream);
    //
    //	oStream.flush();
    //	oStream.close();
    //
    //	return oStream.toByteArray();
    //    }
    //
    //    private int traceTree(final TreePath path, final HSSFSheet sheet, final HSSFCellStyle dataCellStyle, final int columnShift, final int rowNum) {
    //	//TODO after modifying pivot tree table change this implementation.
    //
    //	//Exporting current node.
    //	final PivotTreeTableNode node = (PivotTreeTableNode) path.getLastPathComponent();
    //	final Object[] cellValues = new Object[node.getColumnCount() + columnShift];
    //	for (int columnIndex = 0; columnIndex < cellValues.length; columnIndex++) {
    //	    cellValues[columnIndex] = columnIndex <= columnShift ? (columnIndex == (path.getPathCount() - 1) ? node.getValueAt(0) : null) //
    //		    : node.getValueAt(columnIndex - columnShift);
    //	}
    //	final HSSFRow row = sheet.createRow(rowNum);
    //
    //	for (int index = 0; index < cellValues.length; index++) {
    //	    final HSSFCell cell = row.createCell(index); // create new cell
    //	    if (index < cellValues.length - 1) { // the last column should not have right border
    //		cell.setCellStyle(dataCellStyle);
    //	    }
    //	    final Object value = cellValues[index]; // get the value
    //	    // need to try to do the best job with types
    //	    if (value instanceof Date) {
    //		cell.setCellValue(DateTimeDateFormat.getDateTimeInstance().format(value));
    //	    } else if (value instanceof DateTime) {
    //		cell.setCellValue(DateTimeDateFormat.getDateTimeInstance().format(value));
    //	    } else if (value instanceof Number) {
    //		cell.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
    //		cell.setCellValue(((Number) value).doubleValue());
    //	    } else if (value instanceof Boolean) {
    //		cell.setCellType(HSSFCell.CELL_TYPE_BOOLEAN);
    //		cell.setCellValue((Boolean) value);
    //	    } else if (value == null) { // if null then leave call blank
    //		cell.setCellType(HSSFCell.CELL_TYPE_BLANK);
    //	    } else { // otherwise treat value as String
    //		cell.setCellType(HSSFCell.CELL_TYPE_STRING);
    //		cell.setCellValue(value.toString());
    //	    }
    //	}
    //
    //	//Exporting nodes children.
    //	int rowCount = rowNum;
    //	final Enumeration<? extends MutableTreeTableNode> childrenEnum = node.children();
    //	while (childrenEnum.hasMoreElements()) {
    //	    final MutableTreeTableNode nextChild = childrenEnum.nextElement();
    //	    final TreePath newPath = path.pathByAddingChild(nextChild);
    //	    rowCount = traceTree(newPath, sheet, dataCellStyle, columnShift, rowCount + 1);
    //	}
    //
    //	//Returns row index;
    //	return rowCount;
    //    }
    //
    //    private String[] initPropertyTitles() {
    //	final List<IDistributedProperty> distributionProperties = getSelectedDistributionProperties();
    //	final List<IAggregatedProperty> aggregationProperties = getSelectedAggregationProperties();
    //	final String propertyTitles[] = new String[1 + distributionProperties.size() + aggregationProperties.size()];
    //	propertyTitles[0] = "Grand totals";
    //	for (int index = 0; index < distributionProperties.size(); index++) {
    //	    final IDistributedProperty diProperty = distributionProperties.get(index);
    //	    propertyTitles[index + 1] = diProperty.toString();
    //	}
    //	for (int index = 0; index < aggregationProperties.size(); index++) {
    //	    final IAggregatedProperty agProperty = aggregationProperties.get(index);
    //	    propertyTitles[index + 1 + distributionProperties.size()] = agProperty.toString();
    //	}
    //	return propertyTitles;
    //    }

}
