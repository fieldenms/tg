package ua.com.fielden.platform.swing.review.report.analysis.pivot;

import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.border.EtchedBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import net.miginfocom.swing.MigLayout;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.centre.analyses.IPivotDomainTreeManager;
import ua.com.fielden.platform.domaintree.centre.analyses.IPivotDomainTreeManager.IPivotAddToAggregationTickManager;
import ua.com.fielden.platform.domaintree.centre.analyses.IPivotDomainTreeManager.IPivotAddToDistributionTickManager;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.reflection.development.EntityDescriptor;
import ua.com.fielden.platform.swing.categorychart.AnalysisListDragFromSupport;
import ua.com.fielden.platform.swing.categorychart.AnalysisListDragToSupport;
import ua.com.fielden.platform.swing.checkboxlist.SortingCheckboxList;
import ua.com.fielden.platform.swing.checkboxlist.SortingCheckboxListCellRenderer;
import ua.com.fielden.platform.swing.dnd.DnDSupport2;
import ua.com.fielden.platform.swing.menu.filter.IFilter;
import ua.com.fielden.platform.swing.menu.filter.WordFilter;
import ua.com.fielden.platform.swing.review.details.AnalysisDetailsData;
import ua.com.fielden.platform.swing.review.report.analysis.pivot.configuration.PivotAnalysisConfigurationView;
import ua.com.fielden.platform.swing.review.report.analysis.view.AbstractAnalysisReview;
import ua.com.fielden.platform.swing.review.report.analysis.view.AnalysisDataEvent;
import ua.com.fielden.platform.swing.review.report.events.LoadEvent;
import ua.com.fielden.platform.swing.review.report.events.SelectionEvent;
import ua.com.fielden.platform.swing.review.report.interfaces.ILoadListener;
import ua.com.fielden.platform.swing.review.report.interfaces.ISelectionEventListener;
import ua.com.fielden.platform.swing.treetable.FilterableTreeTableModel;
import ua.com.fielden.platform.swing.treetable.FilterableTreeTablePanel;
import ua.com.fielden.platform.swing.utils.DummyBuilder;
import ua.com.fielden.platform.utils.Pair;
import ua.com.fielden.platform.utils.ResourceLoader;

/**
 * {@link AbstractAnalysisReview} implementation for pivot analysis.
 *
 * @author TG Team
 *
 * @param <T>
 */
public class PivotAnalysisView<T extends AbstractEntity<?>> extends AbstractAnalysisReview<T, ICentreDomainTreeManagerAndEnhancer, IPivotDomainTreeManager> {

    private static final long serialVersionUID = 8295216779213506230L;

    /**
     * Represents the list of available distribution properties.
     */
    private final SortingCheckboxList<String> distributionList;
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
	return (PivotAnalysisModel<T>) super.getModel();
    }

    @Override
    protected void enableRelatedActions(final boolean enable, final boolean navigate) {
	if (getCentre().getCriteriaPanel() != null) {
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
    private SortingCheckboxList<String> createDistributionList() {
	final DefaultListModel<String> listModel = new DefaultListModel<String>();

	final Class<T> root = getModel().getCriteria().getEntityClass();
	final IPivotAddToDistributionTickManager firstTick = getModel().adtme().getFirstTick();

	for (final String distributionProperty : firstTick.checkedProperties(root)) {
	    listModel.addElement(distributionProperty);
	}
	final SortingCheckboxList<String> distributionList = new SortingCheckboxList<String>(listModel, 2);
	distributionList.setCellRenderer(new SortingCheckboxListCellRenderer<String>(distributionList) {

	    private static final long serialVersionUID = 7712966992046861840L;

	    private final EntityDescriptor ed = new EntityDescriptor(getModel().getCriteria().getManagedType(), firstTick.checkedProperties(root));

	    @Override
	    public Component getListCellRendererComponent(final JList<? extends String> list, final String value, final int index, final boolean isSelected, final boolean cellHasFocus) {

		final Component rendererComponent = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

		final Pair<String, String> titleAndDesc = ed.getTitleAndDesc(value.toString());
		defaultRenderer.setText(titleAndDesc.getKey());
		setToolTipText(titleAndDesc.getValue());
		return rendererComponent;
	    }

	    @Override
	    public boolean isSortingAvailable(final String element) {
		return false;
	    }
	});
	distributionList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	distributionList.setCheckingModel(getModel().getRowDistributionCheckingModel(), 0);
	distributionList.setCheckingModel(getModel().getColumnDistributionCheckingModel(), 1);
	distributionList.setSortingModel(null);
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
		return rendererComponent;
	    }

	    @Override
	    public boolean isSortingAvailable(final String element) {
		return aggregationList.isValueChecked(element, 0) && aggregationList.isSortable(element);
	    }
	});
	aggregationList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	aggregationList.setCheckingModel(getModel().getAggregationCheckingModel(), 0);
	aggregationList.setSortingModel(getModel().getAggregationSortingModel());

	return aggregationList;
    }

    private FilterableTreeTablePanel<PivotTreeTable> createPivotTreeTablePanel() {
	final PivotTreeTable treeTable = new PivotTreeTable(new FilterableTreeTableModel(getModel().getPivotModel()));
	final FilterableTreeTablePanel<PivotTreeTable> pivotTablePanel = new FilterableTreeTablePanel<PivotTreeTable>(treeTable, createPivotFilter(), "find item");
	getModel().getPivotModel().addPivotDataLoadedListener(new PivotDataLoadedListener() {

	    @Override
	    public void pivotDataLoaded(final PivotDataLoadedEvent event) {
		SwingUtilities.invokeLater(new Runnable() {

		    @Override
		    public void run() {
			pivotTablePanel.getFilterControl().refresh();
		    }
		});
	    }
	});
	treeTable.addMouseListener(createDoubleClickListener(treeTable));
	refreshPivotTable(treeTable);
	return pivotTablePanel;
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
		    final TreePath rowPath = generatePathWithoutRoot(treeTable.getRowPathForLocation(e.getX(), e.getY()));
		    final TreePath columnPath = generatePathWithoutRoot(treeTable.getColumnPathForLocation(e.getX(), e.getY()));
		    final List<Pair<String, Object>> choosenProperty = createChoosenProperty(rowPath, columnPath);
		    performCustomAction(new AnalysisDataEvent<>(PivotAnalysisView.this, choosenProperty));
		}
	    }

	    /**
	     * Returns the tree path without root node.
	     *
	     * @param treePath
	     * @return
	     */
	    private TreePath generatePathWithoutRoot(final TreePath treePath) {
		if (treePath.getPathCount() > 1) {
		    TreePath newPath = new TreePath(treePath.getPathComponent(1));
		    for (int index = 2; index < treePath.getPathCount(); index++) {
			newPath = newPath.pathByAddingChild(treePath.getPathComponent(index));
		    }
		    return newPath;
		}
		return null;
	    }

	    /**
	     * Returns the list of property name - value pairs of the selected value.
	     *
	     * @param rowPath - the path for selected row.
	     * @param columnPath - the path for selected column.
	     * @return
	     */
	    private List<Pair<String, Object>> createChoosenProperty(final TreePath rowPath, final TreePath columnPath) {
		final List<Pair<String, Object>> choosenItems = new ArrayList<>(createChoosenProperty(rowPath, getModel().getPivotModel().rowCategoryProperties()));
		choosenItems.addAll(createChoosenProperty(columnPath, getModel().getPivotModel().columnCategoryProperties()));
		return choosenItems;
	    }

	    /**
	     * Returns the List of selected property name and it's value.
	     *
	     * @param path - path of tree nodes with values.
	     * @param distributions - the list of distribution properties.
	     * @return
	     */
	    private List<Pair<String, Object>> createChoosenProperty(final TreePath path, final List<String> distributions){
		final List<Pair<String, Object>> choosenItems = new ArrayList<>();
		for (int index = 0; path != null && index < path.getPathCount(); index++) {
		    final DefaultMutableTreeNode node = getUnderlyingNode(path.getPathComponent(index));
		    final String distributionProperty = distributions.get(index);
		    final Object value = PivotTreeTableNode.NULL_USER_OBJECT.equals(node.getUserObject())? null : node.getUserObject();
		    choosenItems.add(new Pair<>(distributionProperty, value));
		}
		return choosenItems;
	    }

	    /**
	     * Returns the underlying node for the specified node.
	     *
	     * @param node
	     * @return
	     */
	    private DefaultMutableTreeNode getUnderlyingNode(final Object node){
		if (node instanceof PivotTreeTableNode) {
		    return (DefaultMutableTreeNode) ((PivotTreeTableNode) node).getUserObject();
		} else {
		    return (DefaultMutableTreeNode)node;
		}
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

    @SuppressWarnings("unchecked")
    private void performCustomAction(final AnalysisDataEvent<?> clickedData) {
	final ICentreDomainTreeManagerAndEnhancer baseCdtme = getModel().getCriteria().getCentreDomainTreeManagerAndEnhnacerCopy();
	baseCdtme.setRunAutomatically(true);
	final AnalysisDetailsData<T> analysisDetailsData = new AnalysisDetailsData<>(//
	getModel().getCriteria().getEntityClass(), //
	getOwner().getOwner().getModel().getName(), //
	getOwner().getModel().getName(), //
	baseCdtme, //
	getModel().adtme(),//
	(List<Pair<String, Object>>) clickedData.getData());
	getOwner().showDetails(analysisDetailsData, AnalysisDetailsData.class);
    }

    /**
     * Refreshes the pivot tree table.
     *
     * @param treeTable
     */
    private static void refreshPivotTable(final PivotTreeTable treeTable) {
	final TreePath selectedPath = treeTable.getPathForRow(treeTable.getSelectedRow());
	((AbstractTableModel) treeTable.getModel()).fireTableStructureChanged();
	treeTable.getSelectionModel().setSelectionInterval(0, treeTable.getRowForPath(selectedPath));
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
    //	//TODO after modifying pivot tree table, change this implementation.
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
