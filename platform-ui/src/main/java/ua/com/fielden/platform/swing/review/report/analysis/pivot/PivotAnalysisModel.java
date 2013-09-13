package ua.com.fielden.platform.swing.review.report.analysis.pivot;

import java.awt.event.ActionEvent;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JOptionPane;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.helpers.DateTimeDateFormat;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.jdesktop.swingx.treetable.MutableTreeTableNode;
import org.joda.time.DateTime;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.centre.IOrderingRepresentation.Ordering;
import ua.com.fielden.platform.domaintree.centre.analyses.IPivotDomainTreeManager;
import ua.com.fielden.platform.domaintree.centre.analyses.IPivotDomainTreeManager.IPivotAddToAggregationTickManager;
import ua.com.fielden.platform.domaintree.centre.analyses.IPivotDomainTreeManager.IPivotAddToDistributionTickManager;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.ndimcube.EntitiesMultipleDimensionCubeData;
import ua.com.fielden.platform.ndimcube.EntitiesMultipleDimensionCubeModel;
import ua.com.fielden.platform.reflection.PropertyTypeDeterminator;
import ua.com.fielden.platform.reflection.TitlesDescsGetter;
import ua.com.fielden.platform.reflection.development.EntityDescriptor;
import ua.com.fielden.platform.report.query.generation.AnalysisResultClassBundle;
import ua.com.fielden.platform.report.query.generation.IReportQueryGenerator;
import ua.com.fielden.platform.report.query.generation.MultipleDimensionCubeQueryGenerator;
import ua.com.fielden.platform.swing.actions.BlockingLayerCommand;
import ua.com.fielden.platform.swing.checkboxlist.ListCheckingEvent;
import ua.com.fielden.platform.swing.checkboxlist.ListCheckingListener;
import ua.com.fielden.platform.swing.checkboxlist.ListCheckingModel;
import ua.com.fielden.platform.swing.checkboxlist.ListSortingModel;
import ua.com.fielden.platform.swing.checkboxlist.SorterChangedEvent;
import ua.com.fielden.platform.swing.checkboxlist.SorterEventListener;
import ua.com.fielden.platform.swing.review.development.EntityQueryCriteria;
import ua.com.fielden.platform.swing.review.report.analysis.view.AbstractAnalysisReviewModel;
import ua.com.fielden.platform.swing.review.report.analysis.view.DomainTreeListCheckingModel;
import ua.com.fielden.platform.swing.review.report.analysis.view.DomainTreeListSortingModel;
import ua.com.fielden.platform.swing.utils.SwingUtilitiesEx;
import ua.com.fielden.platform.utils.Pair;

public class PivotAnalysisModel<T extends AbstractEntity<?>> extends AbstractAnalysisReviewModel<T, ICentreDomainTreeManagerAndEnhancer, IPivotDomainTreeManager> {

    private final PivotTreeTableModelEx pivotModel;
    private final ListCheckingModel<String> rowDistributionCheckingModel;
    private final ListCheckingModel<String> columnDistributionCheckingModel;
    private final ListCheckingModel<String> aggregationCheckingModel;
    private final ListSortingModel<String> aggregationSortingModel;

    public PivotAnalysisModel(final EntityQueryCriteria<ICentreDomainTreeManagerAndEnhancer, T, IEntityDao<T>> criteria, final IPivotDomainTreeManager adtme) {
	super(criteria, adtme);
	final Class<T> root = getCriteria().getEntityClass();
	final IPivotAddToDistributionTickManager firstTick = adtme().getFirstTick();
	final IPivotAddToAggregationTickManager secondTick = adtme().getSecondTick();

	pivotModel = new PivotTreeTableModelEx();
	pivotModel.addTableHeaderChangedListener(new PivotTableHeaderChanged() {

	    @Override
	    public void tableHeaderChanged(final PivotTableHeaderChangedEvent event) {
		//This is stub implementation.
	    }

	    @Override
	    public void columnOrderChanged(final PivotColumnOrderChangedEvent event) {
		final int fromIndex = pivotModel.cubeModel.getIdentifierIndex(event.getProperty());
		if(fromIndex < 0){
		    return;
		}
		final List<String> properties = secondTick.checkedProperties(root);
		int toIndex = 0;
		for(int checkedIndex = 0; checkedIndex < event.getTo(); checkedIndex++){
		    if(pivotModel.cubeModel.getIdentifierIndex(properties.get(checkedIndex)) >= 0){
			toIndex++;
		    }
		}
		pivotModel.cubeModel.moveColumnTo(fromIndex, toIndex);
	    }
	});

	rowDistributionCheckingModel = new DomainTreeListCheckingModel<T>(root, firstTick);
	columnDistributionCheckingModel = new DomainTreeListCheckingModel<T>(root, firstTick.getSecondUsageManager()){
	    @Override
	    public void checkValue(final String value, final boolean check) {
		try {
		    super.checkValue(value, check);
		} catch (final IllegalStateException e) {
		    JOptionPane.showMessageDialog(getAnalysisView(), e.getMessage(), "Warning", JOptionPane.WARNING_MESSAGE);
		}
	    }
	};

	aggregationCheckingModel = new DomainTreeListCheckingModel<T>(root, secondTick);
	aggregationCheckingModel.addListCheckingListener(new ListCheckingListener<String>() {

	    @Override
	    public void valueChanged(final ListCheckingEvent<String> e) {
		if (pivotModel.aggregatedProperties().contains(e.getItem()) && e.getNewCheck()) {
		    final List<String> properties = secondTick.checkedProperties(root);
		    final int to = properties.indexOf(e.getItem());
		    int toIndex = 0;
		    for (int checkedIndex = 0; checkedIndex < to; checkedIndex++) {
			if (pivotModel.cubeModel.getIdentifierIndex(properties.get(checkedIndex)) >= 0) {
			    toIndex++;
			}
		    }
		    pivotModel.cubeModel.addValueColumn(e.getItem(), toIndex);
		} else if(!e.getNewCheck()){
		    pivotModel.cubeModel.removeValueColumn(e.getItem(), false);
		}
		pivotModel.fireTableHeaderChangedEvent(new PivotTableHeaderChangedEvent(pivotModel, e.getItem(), e.getNewCheck()));
	    }
	});
	aggregationSortingModel = new DomainTreeListSortingModel<T>(root, secondTick, adtme().getRepresentation().getSecondTick());
	aggregationSortingModel.addSorterEventListener(new SorterEventListener<String>() {

	    @SuppressWarnings("unchecked")
	    @Override
	    public void valueChanged(final SorterChangedEvent<String> e) {
		new BlockingLayerCommand<Boolean>("", getAnalysisView().getOwner().getProgressLayer()) {

		    private static final long serialVersionUID = 8904506380751507330L;

		    @Override
		    protected Boolean action(final ActionEvent event) throws Exception {
			if (pivotModel.getRoot() != null && !pivotModel.isMultipleCube()) {
			    getProvider().getBlockingLayer().setText("Sorting...");
			    ((PivotTreeTableModelEx.PivotTreeTableNodeEx) pivotModel.getRoot()).sort();
			    return Boolean.TRUE;
			}
			return Boolean.FALSE;
		    }

		    @Override
		    protected void postAction(final Boolean value) {
			if(value){
			    pivotModel.fireSorterChageEvent(new PivotSorterChangeEvent(pivotModel, e.getNewSortObjectes()));
			}
			super.postAction(value);
		    };

		}.actionPerformed(null);

	    }
	});
    }

    public PivotTreeTableModel getPivotModel() {
        return pivotModel;
    }

    public ListCheckingModel<String> getRowDistributionCheckingModel() {
	return rowDistributionCheckingModel;
    }

    public ListCheckingModel<String> getColumnDistributionCheckingModel() {
	return columnDistributionCheckingModel;
    }

    public ListCheckingModel<String> getAggregationCheckingModel() {
	return aggregationCheckingModel;
    }

    public ListSortingModel<String> getAggregationSortingModel() {
	return aggregationSortingModel;
    }

    @Override
    protected Result executeAnalysisQuery() {
	final Result analysisQueryExecutionResult = canLoadData();
	if(!analysisQueryExecutionResult.isSuccessful()){
	    return analysisQueryExecutionResult;
	}

	final Class<T> root = getCriteria().getEntityClass();
	final Class<T> managedType = getCriteria().getManagedType();

	final IReportQueryGenerator<T> pivotQueryGenerator = new MultipleDimensionCubeQueryGenerator<>(root,//
		getCriteria().getCentreDomainTreeManagerAndEnhnacerCopy(), //
		adtme());

	final AnalysisResultClassBundle<T> classBundle = pivotQueryGenerator.generateQueryModel();
	final List<String> aggregations = adtme().getSecondTick().checkedProperties(root);
	final EntityDescriptor aggregationDescriptor = new EntityDescriptor(managedType, aggregations);
	final EntitiesMultipleDimensionCubeData<T> data = new EntitiesMultipleDimensionCubeData<T>()//
		.setData(generateDataMap(root, classBundle))//
		.setRowDistributionProperties(adtme().getFirstTick().usedProperties(root))//
		.setColumnDistributionProperties(adtme().getFirstTick().getSecondUsageManager().usedProperties(root))//
		.setAggregationProperties(adtme().getSecondTick().usedProperties(root))//
		.setColumnNames(aggregationDescriptor.getTitles())//
		.setColumnToolTips(aggregationDescriptor.getDescs())//
		.setColumnClass(generateColumnTypes(managedType, aggregations));

	pivotModel.loadTree(data);
	return Result.successful(pivotModel);
    }

    /**
     * Generates the map between property name and it's type.
     *
     * @param managedType
     * @param aggregations
     * @return
     */
    private Map<String, Class<?>> generateColumnTypes(final Class<T> managedType, final List<String> aggregations) {
	final Map<String, Class<?>> columnTypes = new HashMap<>();
	for(final String property : aggregations){
	    columnTypes.put(property, PropertyTypeDeterminator.determineClass(managedType, property, true, true));
	}
	return columnTypes;
    }

    /**
     * Generates the data map needed for Multiple dimension cube model.
     *
     * @param root
     * @param classBundle
     * @return
     */
    private Map<List<String>, List<T>> generateDataMap(final Class<T> root, final AnalysisResultClassBundle<T> classBundle) {
	final Map<List<String>, List<T>> data = new HashMap<>();
	final List<String> rows = adtme().getFirstTick().usedProperties(root);
	final List<String> columns = adtme().getFirstTick().getSecondUsageManager().usedProperties(root);

	final List<String> rowGroup = new ArrayList<>();
	for(int rowIndex = -1; rowIndex < rows.size(); rowIndex++){
	    if(rowIndex >=0){
		rowGroup.add(rows.get(rowIndex));
	    }
	    final List<String> group = new ArrayList<>(rowGroup);
	    for(int columnIndex = -1; columnIndex < columns.size(); columnIndex++){
		if(columnIndex >= 0){
		    group.add(columns.get(columnIndex));
		}
		data.put(new ArrayList<>(group), getGroupList(classBundle, (rowIndex + 1) * (columns.size() + 1) + (columnIndex + 1)));
	    }
	}

	return data;
    }

    private Result canLoadData() {
	final Result result = getCriteria().isValid();
	if(!result.isSuccessful()){
	    return result;
	}
	final Class<T> entityClass = getCriteria().getEntityClass();
	if(adtme().getSecondTick().usedProperties(entityClass).isEmpty()){
	    return new Result(new IllegalStateException("Please choose aggregation properties"));
	}
	return Result.successful(this);
    }

    @Override
    protected Result exportData(final String fileName) throws IOException {
	try {
	    final byte[] content = getContent((PivotTreeTableNode) pivotModel.getRoot());
	    FileOutputStream fo;
	    fo = new FileOutputStream(fileName);
	    fo.write(content);
	    fo.flush();
	    fo.close();
	} catch (final Exception e) {
	    return new Result(e);
	}
	return Result.successful(fileName);
    }


    private byte[] getContent(final PivotTreeTableNode rootNode) throws IOException {
	if(pivotModel.cubeModel.getRowDistributionProperties().isEmpty()
		&& pivotModel.cubeModel.getColumnDistributionProperties().isEmpty()
		&& pivotModel.cubeModel.getAggregationProperties().isEmpty()) {
	    throw new IllegalArgumentException("To export data into external file run query first!");
	}
	final HSSFWorkbook wb = new HSSFWorkbook();
	final HSSFSheet sheet = wb.createSheet("Exported Data");
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
	final String[] propertyTitles = initPropertyTitles();
	final String[] valueColumnTitles = initColumnValueTitles();
	final int distrColumnCount = pivotModel.rowCategoryProperties().size();
	final int rowsToMerge = (pivotModel.isMultipleCube() && valueColumnTitles.length > 1) ? 1 : 0;

	//Creating distribution header.
	mergeCellWithStyle(sheet, 0, 0, rowsToMerge + 1, distrColumnCount + 1, headerInnerCellStyle, TitlesDescsGetter.removeHtml(pivotModel.getColumnName(0)));

	//Creating aggregation column header
	final HSSFRow subHeader = sheet.getRow(1);
	for(int headerIndex = 0; headerIndex < propertyTitles.length; headerIndex++) {
	    final int valueIndex = distrColumnCount + 1 + headerIndex * (subHeader != null ? valueColumnTitles.length : 1);
	    mergeCellWithStyle(sheet, 0, valueIndex, 1, pivotModel.isMultipleCube() ? valueColumnTitles.length : 1, //
		    headerIndex < propertyTitles.length - 1 ? headerInnerCellStyle : headerCellStyle, propertyTitles[headerIndex]);
	    if(pivotModel.isMultipleCube() && valueColumnTitles.length > 1) {
		for(int subHeaderIndex = 0; subHeaderIndex < valueColumnTitles.length; subHeaderIndex++) {
		    final HSSFCell subHeaderCell = subHeader.createCell(valueIndex + subHeaderIndex);
		    subHeaderCell.setCellValue(valueColumnTitles[subHeaderIndex]);
		    subHeaderCell.setCellStyle(((headerIndex < propertyTitles.length - 1) || (subHeaderIndex < valueColumnTitles.length - 1))
			    ? headerInnerCellStyle : headerCellStyle);
		}
	    }

	}

	// let's make cell style to handle borders
	final HSSFCellStyle dataCellStyle = wb.createCellStyle();
	dataCellStyle.setBorderRight(HSSFCellStyle.BORDER_HAIR);
	//exporting created tree.
	traceTree(new TreePath(rootNode), sheet, dataCellStyle, pivotModel.isMultipleCube() ? valueColumnTitles.length : 1, pivotModel.rowCategoryProperties().size(), rowsToMerge + 1);

	final ByteArrayOutputStream oStream = new ByteArrayOutputStream();

	wb.write(oStream);

	oStream.flush();
	oStream.close();

	return oStream.toByteArray();
    }

    /**
     * Merges the specified number of rows and columns starting from row and col. Set specified cell style and vlue.
     *
     * @param sheet
     * @param row
     * @param col
     * @param rows
     * @param columns
     * @param style
     * @param value
     */
    private void mergeCellWithStyle(final HSSFSheet sheet, final int row, final int col, final int rows, final int columns, final HSSFCellStyle style, final String value) {
	for(int rowInd = 0; rowInd < rows; rowInd++) {
	    HSSFRow workRow = sheet.getRow(row + rowInd);
	    if(workRow == null) {
		workRow = sheet.createRow(row + rowInd);
	    }
	    for(int colInd = 0; colInd < columns; colInd++) {
		HSSFCell workCell = workRow.getCell(col + colInd);
		if(workCell == null) {
		    workCell = workRow.createCell(col + colInd);
		}
		workCell.setCellStyle(style);
	    }
	}
	sheet.getRow(row).getCell(col).setCellValue(value);
	sheet.addMergedRegion(new CellRangeAddress(row, row + rows - 1, col, col + columns - 1));
    }

    private String[] initColumnValueTitles() {
	final String valuePropertyTitles[] = new String[pivotModel.cubeModel.getValueColumnCount()];
	for (int index = 0; index < valuePropertyTitles.length; index++) {
	    valuePropertyTitles[index] = TitlesDescsGetter.removeHtml(pivotModel.cubeModel.getValueColumnName(index));
	}
	return valuePropertyTitles;
    }

    private String[] initPropertyTitles() {
	final String propertyTitles[] = new String[pivotModel.getColumnCount() - 1];
	for (int index = 0; index < propertyTitles.length; index++) {
	    propertyTitles[index] = TitlesDescsGetter.removeHtml(pivotModel.getColumnName(index + 1));
	}
	return propertyTitles;
    }

    private int traceTree(final TreePath path, final HSSFSheet sheet, final HSSFCellStyle dataCellStyle, final int valueColumns, final int columnShift, final int rowNum) {
	//Exporting current node.
	final PivotTreeTableNode node = (PivotTreeTableNode) path.getLastPathComponent();
	final Object[] cellValues = new Object[(node.getColumnCount() - 1) * valueColumns  + columnShift + 1];
	for (int columnIndex = 0; columnIndex < node.getColumnCount() + columnShift; columnIndex++) {
	    if(columnIndex <= columnShift) {
		cellValues[columnIndex] = columnIndex == (path.getPathCount() - 1) ? node.getValueAt(0) : null;
	    } else {
		final Object[] values = (Object[])node.getValueAt(columnIndex - columnShift);
		for(int valInd = 0; valInd < valueColumns; valInd++) {
		    cellValues[(columnIndex - columnShift - 1) * valueColumns + columnShift + 1 + valInd] = values[valInd];
		}
	    }
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
	    rowCount = traceTree(newPath, sheet, dataCellStyle, valueColumns, columnShift, rowCount + 1);
	}

	//Returns row index;
	return rowCount;
    }

    @Override
    protected String[] getExportFileExtensions() {
        return new String[] {getDefaultExportFileExtension()};
    }

    @Override
    protected String getDefaultExportFileExtension() {
        return "xls";
    }

    /**
     * Returns the page for the pivot analysis query grouped by specified list of properties.
     *
     * @param groups
     * @return
     */
    private List<T> getGroupList(final AnalysisResultClassBundle<T> classBundle, final int index){
	return getCriteria().run(classBundle.getCdtmeWithWhichAnalysesQueryHaveBeenCreated(), classBundle.getQueries().get(index).composeQuery(), classBundle.getGeneratedClass(), classBundle.getGeneratedClassRepresentation());
    }

    private class PivotTreeTableModelEx extends PivotTreeTableModel {

	private EntitiesMultipleDimensionCubeModel<T> cubeModel = new EntitiesMultipleDimensionCubeModel<>();

	private final Comparator<PivotTreeTableNodeEx> sorter = new AggregationSorter();

	@Override
	public int getColumnCount() {
	    if (isMultipleCube()) {
		return 2 + cubeModel.getColumnRoot().getChildCount();
	    } else if (cubeModel.getValueColumnCount() != 0) {
		return 1 + cubeModel.getValueColumnCount();
	    }
	    return cubeModel.getRowDistributionProperties().isEmpty() ? 0 : 1;
	}

	@Override
	public Class<?> getColumnClass(final int column) {
	    return Object[].class;
	}

	@Override
	public String getColumnName(final int column) {
	    if (column == 0) {
		return "<html><i>Distribution properties</i></html>";
	    } else if(isMultipleCube()){
		DefaultMutableTreeNode treeNode = null;
		if(column == cubeModel.getColumnRoot().getChildCount() + 1){
		    treeNode = cubeModel.getColumnRoot();
		} else {
		    treeNode = (DefaultMutableTreeNode)cubeModel.getColumnRoot().getChildAt(column - 1);
		}
		return treeNode.getUserObject() == null ? PivotTreeTableNode.NULL_USER_OBJECT : treeNode.getUserObject().toString();
	    } else {
		return cubeModel.getValueColumnName(column-1);
	    }
	}

	@Override
	TreePath getPathForColumn(final int column) {
	    TreeNode treeNode = cubeModel.getColumnRoot();
	    if (isMultipleCube()) {
		if (column == cubeModel.getColumnRoot().getChildCount() + 1) {
		    treeNode = cubeModel.getColumnRoot();
		} else if (column > 0) {
		    treeNode = cubeModel.getColumnRoot().getChildAt(column - 1);
		}
	    }
	    return new TreePath(cubeModel.getColumnModel().getPathToRoot(treeNode));
	}

	@Override
	void setColumnWidth(final int column, final int width) {
	    final Class<T> root = getCriteria().getEntityClass();
	    final IPivotAddToDistributionTickManager firstTick = adtme().getFirstTick();
	    final IPivotAddToAggregationTickManager secondTick = adtme().getSecondTick();
	    if(column == 0 && cubeModel.getRowDistributionProperties().size() > 0){
		firstTick.setWidth(root, cubeModel.getRowDistributionProperties().get(0), width);
	    } else if(column > 0 && !isMultipleCube()) {
		secondTick.setWidth(root, cubeModel.getColumnIdentifier(column-1).toString(), width);
	    }
	}

	@Override
	int getColumnWidth(final int column) {
	    final Class<T> root = getCriteria().getEntityClass();
	    final IPivotAddToDistributionTickManager firstTick = adtme().getFirstTick();
	    final IPivotAddToAggregationTickManager secondTick = adtme().getSecondTick();
	    if(column == 0 && cubeModel.getRowDistributionProperties().size() > 0){
		return firstTick.getWidth(root, cubeModel.getRowDistributionProperties().get(0));
	    } else if(column > 0 && !isMultipleCube()) {
		return secondTick.getWidth(root, cubeModel.getColumnIdentifier(column-1).toString());
	    }
	    return 0;
	}

	@Override
	public Class<?>[] getColumnTypes(final int column) {
	    if (column == 0) {
		return new Class<?>[] {String.class};
	    } else if(isMultipleCube()){
		final Class<?>[] types = new Class<?>[cubeModel.getValueColumnCount()];
		for(int columnIndex = 0; columnIndex < cubeModel.getValueColumnCount(); columnIndex++) {
		    types[columnIndex] = cubeModel.getValueColumnClass(columnIndex);
		}
		return types;
	    } else {
		return new Class<?>[] {cubeModel.getValueColumnClass(column-1)};
	    }
	}

	@Override
	String getColumnTooltipAt(final int column) {
	    return getColumnName(column);
	}

	@Override
	List<String> rowCategoryProperties() {
	    return Collections.unmodifiableList(cubeModel.getRowDistributionProperties());
	}

	@Override
	List<String> columnCategoryProperties() {
	    return Collections.unmodifiableList(cubeModel.getColumnDistributionProperties());
	}

	@Override
	List<String> aggregatedProperties() {
	    return Collections.unmodifiableList(cubeModel.getAggregationProperties());
	}

	/**
	 * Returns the value that indicates whether this pivot model is multiple dimension cube or not.
	 *
	 * @return
	 */
	public boolean isMultipleCube() {
	    return !cubeModel.getColumnDistributionProperties().isEmpty();
	}


	/**
	 * Builds and sets the root node for this tree table model.
	 *
	 * @param data
	 */
	private void loadTree(final EntitiesMultipleDimensionCubeData<T> data) {

	    final EntitiesMultipleDimensionCubeModel<T> newCubeModel = new EntitiesMultipleDimensionCubeModel<>(data);
	    final PivotTreeTableNodeEx root = generateRoot((DefaultMutableTreeNode)newCubeModel.getRowModel().getRoot());

	    SwingUtilitiesEx.invokeLater(new Runnable() {

		@Override
		public void run() {
		    cubeModel = newCubeModel;
		    setRoot(root);
		    firePivotDataLoaded(new PivotDataLoadedEvent(PivotTreeTableModelEx.this));
		}
	    });
	}

	/**
	 * Generates the tree table node for the specified {@link DefaultMutableTreeNode} instance.
	 *
	 * @param root
	 * @return
	 */
	private PivotTreeTableNodeEx generateRoot(final DefaultMutableTreeNode root) {
	    final PivotTreeTableNodeEx node = new PivotTreeTableNodeEx(root);
	    for(int childIndex = 0; childIndex < root.getChildCount(); childIndex++){
		node.add(generateRoot((DefaultMutableTreeNode)root.getChildAt(childIndex)));
	    }
	    return node;
	}

	/**
	 * The tree table node for pivot analysis.
	 *
	 * @author TG Team
	 *
	 */
	private class PivotTreeTableNodeEx extends PivotTreeTableNode {

	    public PivotTreeTableNodeEx(final DefaultMutableTreeNode treeNode){
		super(treeNode);
	    }

	    @Override
	    public DefaultMutableTreeNode getUserObject() {
	        return (DefaultMutableTreeNode)super.getUserObject();
	    }

	    /**
	     * Returns the user object of the wrapped {@link DefaultMutableTreeNode} instance.
	     *
	     * @return
	     */
	    public Object getWrappedUserObject(){
		return getUserObject().getUserObject();
	    }

	    @Override
	    public int getColumnCount() {
		return PivotTreeTableModelEx.this.getColumnCount();
	    }

	    @Override
	    public Object getValueAt(final int column) {
		if (column == 0) {
		    if (getWrappedUserObject() instanceof AbstractEntity) {
			final AbstractEntity<?> entity = (AbstractEntity<?>) getWrappedUserObject();
			return entity.getKey().toString() + (StringUtils.isEmpty(entity.getDesc()) ? "" : " - " + entity.getDesc());
		    }
		    return getWrappedUserObject() == null ? PivotTreeTableNode.NULL_USER_OBJECT : getWrappedUserObject();
		} else if(isMultipleCube()){
		    DefaultMutableTreeNode treeNode = null;
		    if(column == cubeModel.getColumnRoot().getChildCount() + 1){
			treeNode = cubeModel.getColumnRoot();
		    } else {
			treeNode = (DefaultMutableTreeNode)cubeModel.getColumnRoot().getChildAt(column -1);
		    }
		    final Object[] values = new Object[cubeModel.getValueColumnCount()];
		    for(int valueIndex = 0; valueIndex < values.length; valueIndex++) {
			values[valueIndex] = cubeModel.getValueAt(getUserObject(), treeNode, valueIndex);
		    }
		    return values;
		} else {
		    return new Object[] {cubeModel.getValueAt(getUserObject(), cubeModel.getColumnRoot(), column-1)};
		}
	    }

	    /**
	     * Sort children of this node, using comparator defined in the model.
	     *
	     * @param treeTableSorter
	     */
	    @SuppressWarnings("unchecked")
	    private synchronized void sort() {
		for (final MutableTreeTableNode child : children) {
		    ((PivotTreeTableNodeEx) child).sort();
		}
		final List<PivotTreeTableNodeEx> childrenCopy = Collections.list((Enumeration<PivotTreeTableNodeEx>)children());
		Collections.sort(childrenCopy, sorter);
		children.clear();
		children.addAll(childrenCopy);
	    }

	    @SuppressWarnings("rawtypes")
	    @Override
	    public String getTooltipAt(final int column) {
		if (column == 0) {
		    if (getWrappedUserObject() instanceof AbstractEntity) {
			return ((AbstractEntity) getWrappedUserObject()).getDesc();
		    }
		    return getWrappedUserObject() == null ? PivotTreeTableNode.NULL_USER_OBJECT : getWrappedUserObject().toString();
		}
		final Object[] value = (Object[]) getValueAt(column);
		final StringBuilder tooltipBuilder = new StringBuilder();
		tooltipBuilder.append("<html>");
		for (int colInd = 0; colInd < value.length; colInd++) {
		    tooltipBuilder.append(cubeModel.getValueColumnName(colInd));
		    tooltipBuilder.append(" = ");
		    tooltipBuilder.append(value[colInd]);
		    if (colInd < value.length - 1) {
			tooltipBuilder.append("<br>");
		    }
		}
		tooltipBuilder.append("</html>");
		return tooltipBuilder.toString();
	    }
	}

	private class AggregationSorter implements Comparator<PivotTreeTableNodeEx> {

	    @SuppressWarnings("rawtypes")
	    @Override
	    public int compare(final PivotTreeTableNodeEx o1, final PivotTreeTableNodeEx o2) {

		final Class<T> root = getCriteria().getEntityClass();
		final IPivotAddToAggregationTickManager secondTick = adtme().getSecondTick();

		final List<Pair<String, Ordering>> sortObjects = secondTick.orderedProperties(root);
		if (sortObjects == null || sortObjects.isEmpty()) {
		    return defaultCompare(o1, o2);
		}
		final List<Pair<Integer, Ordering>> sortOrders = new ArrayList<Pair<Integer, Ordering>>();
		for (final Pair<String, Ordering> aggregationProperty : sortObjects) {
		    final int sortOrder = cubeModel.getIdentifierIndex(aggregationProperty.getKey());
		    if (sortOrder >= 0) {
			sortOrders.add(new Pair<Integer, Ordering>(Integer.valueOf(sortOrder), aggregationProperty.getValue()));
		    }
		}
		if (sortOrders.isEmpty()) {
		    return defaultCompare(o1, o2);
		}
		for (final Pair<Integer, Ordering> sortingParam : sortOrders) {
		    final Comparable<?> value1 = (Comparable) ((Object[])o1.getValueAt(sortingParam.getKey().intValue() + 1))[0];
		    final Comparable<?> value2 = (Comparable) ((Object[])o2.getValueAt(sortingParam.getKey().intValue() + 1))[0];
		    final int sortMultiplier = sortingParam.getValue() == Ordering.ASCENDING ? 1 : (sortingParam.getValue() == Ordering.DESCENDING ? -1 : 0);
		    int result = 0;
		    if (value1 == null) {
			if (value2 != null) {
			    return -1 * sortMultiplier;
			}
		    } else {
			if (value2 == null) {
			    return 1 * sortMultiplier;
			} else {
			    result = compareValues(value1, value2, sortMultiplier);
			}
		    }
		    if (result != 0) {
			return result;
		    }
		}
		return defaultCompare(o1, o2);
	    }

	    @SuppressWarnings({ "rawtypes", "unchecked" })
	    private int compareValues(final Comparable value1, final Comparable value2, final int sortMultiplier) {
		return value1.compareTo(value2) * sortMultiplier;
	    }

	    private int defaultCompare(final PivotTreeTableNodeEx o1, final PivotTreeTableNodeEx o2) {
		if (o1.getWrappedUserObject() == null) {
		    if (o2.getWrappedUserObject() == null) {
			return 0;
		    } else {
			return -1;
		    }
		} else {
		    if (o2.getWrappedUserObject() == null) {
			return 1;
		    } else {
			return o1.getWrappedUserObject().toString().compareTo(o2.getWrappedUserObject().toString());
		    }
		}

	    }
	}
    }
}
