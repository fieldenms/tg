package ua.com.fielden.platform.swing.review.report.analysis.pivot;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.ListSelectionModel;
import javax.swing.SortOrder;
import javax.swing.tree.TreePath;

import net.miginfocom.swing.MigLayout;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.centre.IOrderingRepresentation.Ordering;
import ua.com.fielden.platform.domaintree.centre.analyses.IPivotDomainTreeManager;
import ua.com.fielden.platform.domaintree.centre.analyses.IPivotDomainTreeManager.IPivotAddToAggregationTickManager;
import ua.com.fielden.platform.domaintree.centre.analyses.IPivotDomainTreeManager.IPivotAddToDistributionTickManager;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.swing.checkboxlist.CheckboxList;
import ua.com.fielden.platform.swing.checkboxlist.CheckboxListCellRenderer;
import ua.com.fielden.platform.swing.checkboxlist.ListCheckingEvent;
import ua.com.fielden.platform.swing.checkboxlist.ListCheckingListener;
import ua.com.fielden.platform.swing.checkboxlist.SortObject;
import ua.com.fielden.platform.swing.checkboxlist.SortRangeChangedEvent;
import ua.com.fielden.platform.swing.checkboxlist.SorterChangedEvent;
import ua.com.fielden.platform.swing.checkboxlist.SorterEventListener;
import ua.com.fielden.platform.swing.checkboxlist.SortingCheckboxList;
import ua.com.fielden.platform.swing.checkboxlist.SortingCheckboxListCellRenderer;
import ua.com.fielden.platform.swing.components.blocking.BlockingIndefiniteProgressLayer;
import ua.com.fielden.platform.swing.menu.filter.IFilter;
import ua.com.fielden.platform.swing.menu.filter.WordFilter;
import ua.com.fielden.platform.swing.review.report.analysis.view.AbstractAnalysisReview;
import ua.com.fielden.platform.swing.review.report.centre.AbstractEntityCentre;
import ua.com.fielden.platform.swing.treetable.FilterableTreeTableModel;
import ua.com.fielden.platform.swing.treetable.FilterableTreeTablePanel;
import ua.com.fielden.platform.swing.utils.DummyBuilder;
import ua.com.fielden.platform.utils.Pair;

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



    public PivotAnalysisView(final PivotAnalysisModel<T> model, final BlockingIndefiniteProgressLayer progressLayer, final AbstractEntityCentre<T, ICentreDomainTreeManagerAndEnhancer> owner) {
	super(model, progressLayer, owner);

	this.distributionList = createDistributionList();
	this.aggregationList = createAggregationList();
	this.pivotTablePanel = createPivotTreeTablePanel();


	//DnDSupport2.installDnDSupport(distributionList, new AnalysisListDragFromSupport(distributionList), new PivotListDragToSupport<IDistributedProperty>(distributionList, createDistributionSwapper()), true);
	//DnDSupport2.installDnDSupport(aggregationList, new AnalysisListDragFromSupport(aggregationList), new PivotListDragToSupport<IAggregatedProperty>(aggregationList, createAggregationSwapper()), true);
	layoutComponents();
    }

    @Override
    public PivotAnalysisModel<T> getModel() {
	return (PivotAnalysisModel<T>)super.getModel();
    }

    @Override
    protected void enableRelatedActions(final boolean enable, final boolean navigate) {
	// TODO Auto-generated method stub

    }

    private CheckboxList<String> createDistributionList() {
	final DefaultListModel listModel = new DefaultListModel();

	final Class<T> root = getModel().getCriteria().getEntityClass();
	final IPivotAddToDistributionTickManager firstTick = getModel().adtm().getFirstTick();

	for (final String distributionProperty : firstTick.checkedProperties(root)) {
	    listModel.addElement(distributionProperty);
	}
	final CheckboxList<String> distributionList = new CheckboxList<String>(listModel);
	distributionList.setCellRenderer(new CheckboxListCellRenderer<String>(new JCheckBox()) {

	    private static final long serialVersionUID = 7712966992046861840L;


	    @Override
	    public Component getListCellRendererComponent(final JList list, final Object value, final int index, final boolean isSelected, final boolean cellHasFocus) {

		final Component rendererComponent = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
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
	final List<String> usedProperties = firstTick.usedProperties(root);
	distributionList.setCheckingValues(usedProperties.toArray(new String[0]));
	distributionList.getCheckingModel().addListCheckingListener(new ListCheckingListener<String>() {

	    @Override
	    public void valueChanged(final ListCheckingEvent<String> e) {
		firstTick.use(root, e.getValue(), e.isChecked());
		//TODO implement logic that updates the pivot table model.
	    }
	});
	return distributionList;
    }

    private SortingCheckboxList<String> createAggregationList() {
	final DefaultListModel listModel = new DefaultListModel();

	final Class<T> root = getModel().getCriteria().getEntityClass();
	final IPivotAddToAggregationTickManager secondTick = getModel().adtm().getSecondTick();

	for (final String distributionProperty : secondTick.checkedProperties(root)) {
	    listModel.addElement(distributionProperty);
	}
	final SortingCheckboxList<String> aggregationList = new SortingCheckboxList<String>(listModel);
	aggregationList.getSortingModel().setSingle(false);
	aggregationList.setCellRenderer(new SortingCheckboxListCellRenderer<String>(aggregationList, new JCheckBox()) {

	    private static final long serialVersionUID = -6751336113879821723L;

	    @Override
	    public Component getListCellRendererComponent(final JList list, final Object value, final int index, final boolean isSelected, final boolean cellHasFocus) {
		final Component rendererComponent = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
		if (secondTick.isUsed(root, value.toString())) {
		    arrow.setVisible(true);
		    if (aggregationList.getSortingModel().isSortable(value.toString())) {
			arrow.setSortOrder(aggregationList.getSortingModel().getSortOrder(value.toString()));
		    }
		}
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
		//TODO implement review update after property usage was changed.
	    }
	});
	aggregationList.getSortingModel().addSorterEventListener(new SorterEventListener<String>() {

	    @Override
	    public void valueChanged(final SorterChangedEvent<String> e) {
		secondTick.toggleOrdering(root, e.getSortObject());
		//TODO this implementation doesn't take in to account the checking model. Please review and reimplement.
	    }

	    @Override
	    public void sortingRangeChanged(final SortRangeChangedEvent e) {
		// TODO Auto-generated method stub
		//It seams that it won't be needed.
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

    private FilterableTreeTablePanel<PivotTreeTable> createPivotTreeTablePanel() {
	final FilterableTreeTablePanel<PivotTreeTable> pivotTablePanel = new FilterableTreeTablePanel<PivotTreeTable>(new PivotTreeTable(new FilterableTreeTableModel(getModel().getPivotModel())), createPivotFilter(), "find item");
	final PivotTreeTable treeTable = pivotTablePanel.getTreeTable();
	treeTable.addMouseListener(createDoubleClickListener(treeTable));
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
		    final TreePath treePath = treeTable.getPathForLocation(e.getX(), e.getY());
		    if (treePath.getPathCount() > 2) {
			TreePath newPath = new TreePath(treePath.getPathComponent(2));
			for (int index = 3; index < treePath.getPathCount(); index++) {
			    newPath = newPath.pathByAddingChild(treePath.getPathComponent(index));
			}
			//final List<Pair<IDistributedProperty, Object>> choosenProperty = createChoosenProperty(newPath);
			//TODO implement double click action here. This action must show the details.
			//getReportView().getModel().runDoubleClickAction(new AnalysisDoubleClickEvent(choosenProperty, e));
		    }
		}
	    }

	    private List<Pair<String, Object>> createChoosenProperty(final TreePath newPath) {
		final List<Pair<String, Object>> choosenItems = new ArrayList<Pair<String, Object>>();
		final List<String> categoryProperties = getModel().getPivotModel().categoryProperties();
		for (int index = 0; index < newPath.getPathCount(); index++) {
		    final PivotTreeTableNode node = (PivotTreeTableNode) newPath.getPathComponent(index);
		    final String distributionProperty = categoryProperties.get(index);
		    choosenItems.add(new Pair<String, Object>(distributionProperty, node.getUserObject()));
		}
		return choosenItems;
	    }
	};
    }


    //////////////////////Refactor code below//////////////////////////////////
    //Used for creating view in AnalysisReportMode.REPORT mode.
    //Pivot tree table.
    //    private final PivotAnalysisDataProvider<T, DAO> dataProvider;


    //TODO this methods is needed to change position of aggregation and distribution properties.
    //    private IValueSwaper createAggregationSwapper() {
    //	return new IValueSwaper() {
    //
    //	    @Override
    //	    public void swapValues(final int oldIndex, final int newindex) {
    //		pivotTablePanel.getTreeTable().swapTotalParameter(oldIndex, newindex);
    //
    //	    }
    //	};
    //    }
    //
    //    private IValueSwaper createDistributionSwapper() {
    //	return new IValueSwaper() {
    //
    //	    @Override
    //	    public void swapValues(final int oldIndex, final int newindex) {
    //		new BlockingLayerCommand<Void>("add/remove group", tabPaneLayer) {
    //
    //		    private static final long serialVersionUID = -628123890478386500L;
    //
    //		    @Override
    //		    protected Void action(final ActionEvent actionEvent) throws Exception {
    //			setMessage("regrouping data...");
    //			dataProvider.reload();
    //			return null;
    //		    }
    //
    //		    @Override
    //		    protected void postAction(final Void value) {
    //			pivotTablePanel.getTreeTable().swapGroupParameter(oldIndex, newindex);
    //			pivotTablePanel.getFilterControl().refresh();
    //			super.postAction(value);
    //		    }
    //		}.actionPerformed(null);
    //	    }
    //	};
    //
    //    }

    private void layoutComponents() {
	removeAll();
	setLayout(new MigLayout("fill, insets 0", "[fill,grow]", "[fill,grow]"));
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

	add(splitPane);
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
