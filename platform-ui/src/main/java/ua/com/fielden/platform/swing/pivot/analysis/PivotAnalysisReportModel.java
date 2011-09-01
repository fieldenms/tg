package ua.com.fielden.platform.swing.pivot.analysis;

import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.ListSelectionModel;
import javax.swing.tree.TreePath;

import net.miginfocom.swing.MigLayout;

import org.apache.log4j.helpers.DateTimeDateFormat;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.jdesktop.swingx.treetable.MutableTreeTableNode;
import org.joda.time.DateTime;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.IBindingEntity;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.reportquery.IAggregatedProperty;
import ua.com.fielden.platform.reportquery.IDistributedProperty;
import ua.com.fielden.platform.selectioncheckbox.SelectionCheckBoxPanel.IAction;
import ua.com.fielden.platform.swing.actions.BlockingLayerCommand;
import ua.com.fielden.platform.swing.analysis.IAnalysisReportPersistentObject;
import ua.com.fielden.platform.swing.categorychart.AnalysisDoubleClickEvent;
import ua.com.fielden.platform.swing.categorychart.AnalysisListDragFromSupport;
import ua.com.fielden.platform.swing.checkboxlist.CheckboxList;
import ua.com.fielden.platform.swing.checkboxlist.ListCheckingEvent;
import ua.com.fielden.platform.swing.checkboxlist.ListCheckingListener;
import ua.com.fielden.platform.swing.checkboxlist.SortObject;
import ua.com.fielden.platform.swing.checkboxlist.SortRangeChangedEvent;
import ua.com.fielden.platform.swing.checkboxlist.SorterChangedEvent;
import ua.com.fielden.platform.swing.checkboxlist.SorterEventListener;
import ua.com.fielden.platform.swing.checkboxlist.SortingCheckboxList;
import ua.com.fielden.platform.swing.components.blocking.BlockingIndefiniteProgressLayer;
import ua.com.fielden.platform.swing.dnd.DnDSupport2;
import ua.com.fielden.platform.swing.groupanalysis.GroupAnalysisReportModel;
import ua.com.fielden.platform.swing.menu.filter.IFilter;
import ua.com.fielden.platform.swing.menu.filter.WordFilter;
import ua.com.fielden.platform.swing.pivot.analysis.dnd.IValueSwaper;
import ua.com.fielden.platform.swing.pivot.analysis.dnd.PivotListDragToSupport;
import ua.com.fielden.platform.swing.pivot.analysis.persistence.PivotAnalysisPersistentObject;
import ua.com.fielden.platform.swing.pivot.analysis.treetable.AggregationSorter;
import ua.com.fielden.platform.swing.pivot.analysis.treetable.PivotTreeTable;
import ua.com.fielden.platform.swing.pivot.analysis.treetable.PivotTreeTableModel;
import ua.com.fielden.platform.swing.pivot.analysis.treetable.PivotTreeTableNode;
import ua.com.fielden.platform.swing.treetable.FilterableTreeTableModel;
import ua.com.fielden.platform.swing.treetable.FilterableTreeTablePanel;
import ua.com.fielden.platform.swing.utils.DummyBuilder;
import ua.com.fielden.platform.utils.Pair;

public class PivotAnalysisReportModel<T extends AbstractEntity, DAO extends IEntityDao<T>> extends GroupAnalysisReportModel<T, DAO> {

    private final PivotAnalysisReview<T, DAO> reportView;
    private final BlockingIndefiniteProgressLayer tabPaneLayer;

    //Used for creating view in AnalysisReportMode.REPORT mode.
    private final CheckboxList<IDistributedProperty> distributionList;
    private final SortingCheckboxList<IAggregatedProperty> aggregationList;
    //Pivot tree table.
    private final PivotAnalysisDataProvider<T, DAO> dataProvider;
    private final FilterableTreeTablePanel<PivotTreeTable> pivotTablePanel;
    private final PivotTreeTableModel treeTableModel;

    public PivotAnalysisReportModel(final PivotAnalysisReview<T, DAO> reportView, final BlockingIndefiniteProgressLayer tabPaneLayer, final IAnalysisReportPersistentObject pObj) {
	this.reportView = reportView;
	this.tabPaneLayer = tabPaneLayer;

	//Creating distribution and aggregation lists.
	this.distributionList = createDistributionList(pObj instanceof PivotAnalysisPersistentObject ? ((PivotAnalysisPersistentObject) pObj).getAvailableDistributionProperties()
		: new ArrayList<IDistributedProperty>());

	this.distributionList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	DnDSupport2.installDnDSupport(distributionList, new AnalysisListDragFromSupport(distributionList), new PivotListDragToSupport<IDistributedProperty>(distributionList, createDistributionSwapper()), true);
	this.aggregationList = createAggregationList(pObj instanceof PivotAnalysisPersistentObject ? ((PivotAnalysisPersistentObject) pObj).getAvailableAggregationProperties()
		: new ArrayList<IAggregatedProperty>());
	this.aggregationList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

	int groupColumnWidth = 0;
	if (pObj instanceof PivotAnalysisPersistentObject) {
	    final PivotAnalysisPersistentObject analysisPObj = (PivotAnalysisPersistentObject) pObj;
	    this.distributionList.setCheckingValues(analysisPObj.getSelectedDistributionProperties().toArray(new IDistributedProperty[] {}));
	    this.aggregationList.setCheckingValues(analysisPObj.getSelectedAggregationProperties().toArray(new IAggregatedProperty[] {}));
	    this.aggregationList.getSortingModel().setSortObjects(analysisPObj.getSortedAggregations(), true);
	    this.aggregationList.getSortingModel().setSortable(analysisPObj.getSortableAggregations());
	    groupColumnWidth = analysisPObj.getDistributionColumnWidth();
	}

	DnDSupport2.installDnDSupport(aggregationList, new AnalysisListDragFromSupport(aggregationList), new PivotListDragToSupport<IAggregatedProperty>(aggregationList, createAggregationSwapper()), true);

	distributionList.addListCheckingListener(createDistributionCheckingListener());

	dataProvider = new PivotAnalysisDataProvider<T, DAO>(this);

	treeTableModel = new PivotTreeTableModel(groupColumnWidth, dataProvider);
	aggregationList.addListCheckingListener(createAggregationCheckingModel(treeTableModel));
	aggregationList.getSortingModel().addSorterEventListener(createSortEventListener());
	pivotTablePanel = new FilterableTreeTablePanel<PivotTreeTable>(new PivotTreeTable(new FilterableTreeTableModel(treeTableModel)), createPivotFilter(), "find item");
	final PivotTreeTable treeTable = pivotTablePanel.getTreeTable();
	treeTable.addMouseListener(createDoubleClickListener(treeTable));
	final List<IDistributedProperty> selectedDistributionProperties = getSelectedDistributionProperties();
	for (int index = 0; index < selectedDistributionProperties.size(); index++) {
	    treeTable.addGroupParameter(selectedDistributionProperties.get(index), index);
	}
	final List<Pair<IAggregatedProperty, Integer>> availableAggregationProperties = pObj instanceof PivotAnalysisPersistentObject ? ((PivotAnalysisPersistentObject) pObj).getSelectedAggregationPropertiesWithWidth()
		: new ArrayList<Pair<IAggregatedProperty, Integer>>();
	for (int index = 0; index < availableAggregationProperties.size(); index++) {
	    treeTable.addTotalColumn(availableAggregationProperties.get(index), index);
	}
	treeTable.setTreeTableSorter(new AggregationSorter<T, DAO>(this));
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
			final List<Pair<IDistributedProperty, Object>> choosenProperty = createChoosenProperty(newPath);
			getReportView().getModel().runDoubleClickAction(new AnalysisDoubleClickEvent(choosenProperty, e));
		    }
		}
	    }

	    private List<Pair<IDistributedProperty, Object>> createChoosenProperty(final TreePath newPath) {
		final List<Pair<IDistributedProperty, Object>> choosenItems = new ArrayList<Pair<IDistributedProperty, Object>>();
		final List<IDistributedProperty> distributionProperties = getSelectedDistributionProperties();
		for (int index = 0; index < newPath.getPathCount(); index++) {
		    final PivotTreeTableNode node = (PivotTreeTableNode) newPath.getPathComponent(index);
		    final IDistributedProperty distributionProperty = distributionProperties.get(index);
		    final Object value = node.getUserObject().equals(PivotTreeTableNode.NULL_USER_OBJECT) ? null : node.getUserObject();
		    choosenItems.add(new Pair<IDistributedProperty, Object>(distributionProperty, value));
		}
		return choosenItems;
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

    private IValueSwaper createAggregationSwapper() {
	return new IValueSwaper() {

	    @Override
	    public void swapValues(final int oldIndex, final int newindex) {
		pivotTablePanel.getTreeTable().swapTotalParameter(oldIndex, newindex);

	    }
	};
    }

    private IValueSwaper createDistributionSwapper() {
	return new IValueSwaper() {

	    @Override
	    public void swapValues(final int oldIndex, final int newindex) {
		new BlockingLayerCommand<Void>("add/remove group", tabPaneLayer) {

		    private static final long serialVersionUID = -628123890478386500L;

		    @Override
		    protected Void action(final ActionEvent actionEvent) throws Exception {
			setMessage("regrouping data...");
			dataProvider.reload();
			return null;
		    }

		    @Override
		    protected void postAction(final Void value) {
			pivotTablePanel.getTreeTable().swapGroupParameter(oldIndex, newindex);
			pivotTablePanel.getFilterControl().refresh();
			super.postAction(value);
		    }
		}.actionPerformed(null);
	    }
	};

    }

    private SorterEventListener<IAggregatedProperty> createSortEventListener() {
	return new SorterEventListener<IAggregatedProperty>() {

	    @Override
	    public void valueChanged(final SorterChangedEvent<IAggregatedProperty> e) {
		if (e.isSortOrderChanged()) {
		    pivotTablePanel.getTreeTable().toggleSorter();
		}
	    }

	    @Override
	    public void sortingRangeChanged(final SortRangeChangedEvent e) {
		//do nothing;
	    }
	};
    }

    private ListCheckingListener<IAggregatedProperty> createAggregationCheckingModel(final PivotTreeTableModel pivotModel) {
	return new ListCheckingListener<IAggregatedProperty>() {

	    private final Map<IAggregatedProperty, Integer> removedColumns = new HashMap<IAggregatedProperty, Integer>();

	    @Override
	    public void valueChanged(final ListCheckingEvent<IAggregatedProperty> e) {
		new BlockingLayerCommand<Void>("add/remove column", tabPaneLayer) {

		    private static final long serialVersionUID = 89699294731242281L;

		    @Override
		    protected Void action(final ActionEvent actionEvent) throws Exception {
			if (e.isChecked()) {
			    dataProvider.calculate(e.getValue());
			}
			return null;
		    }

		    @Override
		    protected void postAction(final Void value) {
			final int index = getIndexOf(e.getValue(), aggregationList);
			if (index >= 0 && e.isChecked()) {
			    final Integer columnWidth = removedColumns.get(e.getValue());
			    pivotTablePanel.getTreeTable().addTotalColumn(new Pair<IAggregatedProperty, Integer>(e.getValue(), columnWidth == null ? Integer.valueOf(0)
				    : columnWidth), index);
			} else {
			    removedColumns.put(e.getValue(), Integer.valueOf(pivotModel.getColumnWidthAt(index + 1)));
			    pivotTablePanel.getTreeTable().removeTotalColumn(e.getValue());
			}
			super.postAction(value);
		    }
		}.actionPerformed(null);

	    }

	};
    }

    private ListCheckingListener<IDistributedProperty> createDistributionCheckingListener() {
	return new ListCheckingListener<IDistributedProperty>() {

	    @Override
	    public void valueChanged(final ListCheckingEvent<IDistributedProperty> e) {
		new BlockingLayerCommand<Void>("add/remove group", tabPaneLayer) {

		    private static final long serialVersionUID = -628123890478386500L;

		    @Override
		    protected Void action(final ActionEvent actionEvent) throws Exception {
			setMessage("regrouping data...");
			dataProvider.reload();
			return null;
		    }

		    @Override
		    protected void postAction(final Void value) {
			final int index = getIndexOf(e.getValue(), distributionList);
			if (e.isChecked() && index >= 0) {
			    pivotTablePanel.getTreeTable().addGroupParameter(e.getValue(), index);
			} else {
			    pivotTablePanel.getTreeTable().removeGroupParameter(e.getValue());
			}
			pivotTablePanel.getFilterControl().refresh();
			super.postAction(value);
		    }
		}.actionPerformed(null);

	    }

	};
    }

    private <DP extends IDistributedProperty> int getIndexOf(final DP value, final CheckboxList<DP> checkBoxList) {
	int index = -1;
	for (final DP distributionProperty : checkBoxList.getVectorListData()) {
	    final boolean isChecked = checkBoxList.isValueChecked(distributionProperty);
	    if (isChecked) {
		index++;
	    }
	    if (value.equals(distributionProperty)) {
		if (isChecked) {
		    return index;
		}
		return ++index;
	    }
	}
	return -1;
    }

    private CheckboxList<IDistributedProperty> createDistributionList(final List<IDistributedProperty> availableProperties) {
	final DefaultListModel listModel = new DefaultListModel();
	if (availableProperties != null) {
	    for (final IDistributedProperty distributionProperty : availableProperties) {
		listModel.addElement(distributionProperty);
	    }
	}
	final CheckboxList<IDistributedProperty> distributionList = new CheckboxList<IDistributedProperty>(listModel);
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
	aggregationList.getSortingModel().setSingle(false);
	return aggregationList;
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
	final JPanel rightPanel = new JPanel(new MigLayout("fill, insets 3", "[fill,grow]", "[fill,grow]"));
	rightPanel.add(pivotTablePanel);

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
	distributionList.setModel(getNewModelFor(reportView.getAnalysisWizardModel().getSelectedDistributionProperties(), distributionList.getVectorListData()));
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
	return distributionList.getVectorListData();
    }

    public List<IDistributedProperty> getSelectedDistributionProperties() {
	return distributionList.getSelectedValuesInOrder();
    }

    public List<IAggregatedProperty> getSelectedAggregationProperties() {
	return aggregationList.getSelectedValuesInOrder();
    }

    public List<Pair<IAggregatedProperty, Integer>> getColumnWidth() {
	final List<Pair<IAggregatedProperty, Integer>> columns = new ArrayList<Pair<IAggregatedProperty, Integer>>();
	return pivotTablePanel.getTreeTable().getAggregationColumnsWidth();
    }

    public int getGroupColumnWidth() {
	return pivotTablePanel.getTreeTable().getHierarchicalColumnWidth();
    }

    public List<SortObject<IAggregatedProperty>> getSortingAggregations() {
	return aggregationList.getSortingModel().getSortObjects();
    }

    public Set<IAggregatedProperty> getSortableAggregations() {
	return aggregationList.getSortingModel().getSortableValues();
    }

    /**
     * Returns the value that indicates whether report view can be restored or not.
     * 
     * @return
     */
    @Override
    public boolean canRestoreReportView() {
	return distributionList.getModel().getSize() > 0 /*&& aggregationList.getModel().getSize() > 0*/;
    }

    public void updateView(final GroupItem group, final IAction afterUpdateAction) {
	pivotTablePanel.getTreeTable().loadData(group);
	pivotTablePanel.getFilterControl().refresh();
    }

    public PivotAnalysisReview<T, DAO> getReportView() {
	return reportView;
    }

    public PivotAnalysisDataProvider<T, DAO> getDataProvider() {
	return dataProvider;
    }

    public Result exportDataIntoFile(final File file, final List<? extends IBindingEntity> analysisData) {
	final GroupItem rootItem = dataProvider.createBasicTreeFrom(analysisData);
	final PivotTreeTableNode rootNode = rootItem.createTree("Grand totals", treeTableModel);
	rootNode.sort(treeTableModel.getTreeTableSorter());
	try {
	    final byte[] content = getContent(rootNode);
	    FileOutputStream fo;
	    fo = new FileOutputStream(file);
	    fo.write(content);
	    fo.flush();
	    fo.close();
	} catch (final Exception e) {
	    return new Result(e);
	}
	return Result.successful(file);
    }

    private byte[] getContent(final PivotTreeTableNode rootNode) throws IOException {
	final HSSFWorkbook wb = new HSSFWorkbook();
	final HSSFSheet sheet = wb.createSheet("Exported Data");
	// Create a header row.
	final HSSFRow headerRow = sheet.createRow(0);
	// Create a new font and alter it
	final HSSFFont font = wb.createFont();
	font.setFontHeightInPoints((short) 12);
	font.setFontName("Courier New");
	font.setBoldweight((short) 1000);
	// Fonts are set into a style so create a new one to use
	final HSSFCellStyle headerCellStyle = wb.createCellStyle();
	headerCellStyle.setFont(font);
	headerCellStyle.setBorderBottom(HSSFCellStyle.BORDER_THIN);
	final HSSFCellStyle headerInnerCellStyle = wb.createCellStyle();
	headerInnerCellStyle.setFont(font);
	headerInnerCellStyle.setBorderBottom(HSSFCellStyle.BORDER_THIN);
	headerInnerCellStyle.setBorderRight(HSSFCellStyle.BORDER_HAIR);
	final String propertyTitles[] = initPropertyTitles();
	// Create cells and put column names there
	for (int index = 0; index < propertyTitles.length; index++) {
	    final HSSFCell cell = headerRow.createCell(index);
	    cell.setCellValue(propertyTitles[index]);
	    cell.setCellStyle(index < propertyTitles.length - 1 ? headerInnerCellStyle : headerCellStyle);
	}

	// let's make cell style to handle borders
	final HSSFCellStyle dataCellStyle = wb.createCellStyle();
	dataCellStyle.setBorderRight(HSSFCellStyle.BORDER_HAIR);

	//exporting created tree.
	traceTree(new TreePath(rootNode), sheet, dataCellStyle, getSelectedDistributionProperties().size(), 1);

	final ByteArrayOutputStream oStream = new ByteArrayOutputStream();

	wb.write(oStream);

	oStream.flush();
	oStream.close();

	return oStream.toByteArray();
    }

    private int traceTree(final TreePath path, final HSSFSheet sheet, final HSSFCellStyle dataCellStyle, final int columnShift, final int rowNum) {
	//TODO after modifying pivot tree table change this implementation.

	//Exporting current node.
	final PivotTreeTableNode node = (PivotTreeTableNode) path.getLastPathComponent();
	final Object[] cellValues = new Object[node.getColumnCount() + columnShift];
	for (int columnIndex = 0; columnIndex < cellValues.length; columnIndex++) {
	    cellValues[columnIndex] = columnIndex <= columnShift ? (columnIndex == (path.getPathCount() - 1) ? node.getValueAt(0) : null) //
		    : node.getValueAt(columnIndex - columnShift);
	}
	final HSSFRow row = sheet.createRow(rowNum);

	for (int index = 0; index < cellValues.length; index++) {
	    final HSSFCell cell = row.createCell(index); // create new cell
	    if (index < cellValues.length - 1) { // the last column should not have right border
		cell.setCellStyle(dataCellStyle);
	    }
	    final Object value = cellValues[index]; // get the value
	    // need to try to do the best job with types
	    if (value instanceof Date) {
		cell.setCellValue(DateTimeDateFormat.getDateTimeInstance().format(value));
	    } else if (value instanceof DateTime) {
		cell.setCellValue(DateTimeDateFormat.getDateTimeInstance().format(value));
	    } else if (value instanceof Number) {
		cell.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
		cell.setCellValue(((Number) value).doubleValue());
	    } else if (value instanceof Boolean) {
		cell.setCellType(HSSFCell.CELL_TYPE_BOOLEAN);
		cell.setCellValue((Boolean) value);
	    } else if (value == null) { // if null then leave call blank
		cell.setCellType(HSSFCell.CELL_TYPE_BLANK);
	    } else { // otherwise treat value as String
		cell.setCellType(HSSFCell.CELL_TYPE_STRING);
		cell.setCellValue(value.toString());
	    }
	}

	//Exporting nodes children.
	int rowCount = rowNum;
	final Enumeration<? extends MutableTreeTableNode> childrenEnum = node.children();
	while (childrenEnum.hasMoreElements()) {
	    final MutableTreeTableNode nextChild = childrenEnum.nextElement();
	    final TreePath newPath = path.pathByAddingChild(nextChild);
	    rowCount = traceTree(newPath, sheet, dataCellStyle, columnShift, rowCount + 1);
	}

	//Returns row index;
	return rowCount;
    }

    private String[] initPropertyTitles() {
	final List<IDistributedProperty> distributionProperties = getSelectedDistributionProperties();
	final List<IAggregatedProperty> aggregationProperties = getSelectedAggregationProperties();
	final String propertyTitles[] = new String[1 + distributionProperties.size() + aggregationProperties.size()];
	propertyTitles[0] = "Grand totals";
	for (int index = 0; index < distributionProperties.size(); index++) {
	    final IDistributedProperty diProperty = distributionProperties.get(index);
	    propertyTitles[index + 1] = diProperty.toString();
	}
	for (int index = 0; index < aggregationProperties.size(); index++) {
	    final IAggregatedProperty agProperty = aggregationProperties.get(index);
	    propertyTitles[index + 1 + distributionProperties.size()] = agProperty.toString();
	}
	return propertyTitles;
    }
}
