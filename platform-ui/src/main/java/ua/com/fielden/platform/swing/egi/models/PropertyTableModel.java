/**
 *
 */
package ua.com.fielden.platform.swing.egi.models;

import static com.jidesoft.grid.TableModelWrapperUtils.getActualRowAt;
import static java.util.Arrays.asList;
import static ua.com.fielden.platform.swing.egi.models.mappings.ColumnTotals.GRAND_TOTALS_SEPARATE_FOOTER;

import java.awt.Component;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.zip.Deflater;

import javax.swing.JTable;
import javax.swing.event.CellEditorListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.helpers.DateTimeDateFormat;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.joda.time.DateTime;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.reflection.PropertyTypeDeterminator;
import ua.com.fielden.platform.serialisation.xstream.GZipOutputStreamEx;
import ua.com.fielden.platform.swing.egi.AbstractPropertyColumnMapping;
import ua.com.fielden.platform.swing.egi.EntityGridInspector;
import ua.com.fielden.platform.swing.egi.coloring.EgiColoringScheme;
import ua.com.fielden.platform.swing.egi.models.builders.PropertyTableModelBuilder;
import ua.com.fielden.platform.swing.egi.models.mappings.ColumnTotals;
import ua.com.fielden.platform.utils.EntityUtils;

import com.jidesoft.grid.HierarchicalTable;
import com.jidesoft.grid.HierarchicalTableComponentFactory;
import com.jidesoft.grid.HierarchicalTableModel;
import com.jidesoft.grid.NavigableModel;

/**
 * Represents {@link TableModel} which maps list of entities and their properties to table using {@link AbstractPropertyColumnMapping} instances. This model also implements
 * {@link HierarchicalTableModel} and {@link HierarchicalTableComponentFactory} interfaces, but implemented methods do not provide hierarchical functionality. If such functionality
 * is needed, then that methods should be overridden or {@link HierarchicalPropertyTableModel} should be used
 * 
 * @author TG Team
 */
@SuppressWarnings({ "serial", "rawtypes" })
public class PropertyTableModel<T extends AbstractEntity> extends AbstractTableModel implements NavigableModel, HierarchicalTableModel, HierarchicalTableComponentFactory {

    /**
     * Contains the actual instances which properties are displayed in the table governed by this model.
     */
    private final List<List<T>> groups = new ArrayList<>();

    private final List<AbstractPropertyColumnMapping<T>> propertyColumnMappings;
    private final Map<String, AbstractPropertyColumnMapping<T>> propertyColumnMappingsMap;

    private final GroupingAlgorithm groupingAlgorithm;

    private final List<Integer> nonDataRows = new ArrayList<Integer>();

    /**
     * This flag shows whether table should show grand totals for at least one column.
     */
    private final boolean hasGrandTotals;

    /**
     * This flag shows whether table should show group totals for at least one column.
     */
    private final boolean hasGroupTotals;

    /**
     * This flag shows whether table should show only group totals in separate footer row for at least one column.
     * 
     * @see ColumnTotals#GRAND_TOTALS_SEPARATE_FOOTER
     */
    private final boolean hasGrandTotalsSeparateFooter;

    private final EgiColoringScheme<T> egiColoringScheme;

    /**
     * Creates editable {@link TableModel}, where columns strictly refer to passed {@link AbstractPropertyColumnMapping} instances, and rows strictly refer to passed T entities.
     * 
     * @param instances
     * @param propertyColumnMappings
     * @param groupingAlgorithm
     *            - algorithm, which takes part in grouping. If null is passed, then {@link SingleGroupAlgorithm} is used.
     */
    public PropertyTableModel(final List<T> instances, final List<? extends AbstractPropertyColumnMapping<T>> propertyColumnMappings, final GroupingAlgorithm<T> groupingAlgorithm, final EgiColoringScheme<T> egiColoringScheme) {
        this.propertyColumnMappings = new ArrayList<AbstractPropertyColumnMapping<T>>();
        this.propertyColumnMappings.addAll(propertyColumnMappings);
        this.propertyColumnMappingsMap = new HashMap<>();

        this.groupingAlgorithm = groupingAlgorithm != null ? groupingAlgorithm : new SingleGroupAlgorithm<T>();
        this.egiColoringScheme = egiColoringScheme;

        boolean hasGrandTotals = false;
        boolean hasGroupTotals = false;
        boolean hasGrandTotalsSeparateFooter = false;
        for (final AbstractPropertyColumnMapping<T> mapping : propertyColumnMappings) {
            this.propertyColumnMappingsMap.put(mapping.getPropertyName(), mapping);

            if (mapping.getColumnTotals().hasGrandTotals()) {
                hasGrandTotals = true;
            }
            if (mapping.getColumnTotals().hasGroupTotals()) {
                hasGroupTotals = true;
            }
            if (GRAND_TOTALS_SEPARATE_FOOTER.equals(mapping.getColumnTotals())) {
                hasGrandTotalsSeparateFooter = true;
            }
        }
        this.hasGrandTotals = hasGrandTotals;
        this.hasGroupTotals = hasGroupTotals;
        this.hasGrandTotalsSeparateFooter = hasGrandTotalsSeparateFooter;
        regroup(instances);
    }

    public PropertyTableModel(final PropertyTableModelBuilder<T> builder, final List<T> instances) {
        this(instances, builder.getPropertyColumnMappings(), null,//
        new EgiColoringScheme<T>(builder.getRowColoringScheme(), builder.getPropertyColoringSchemes()));
    }

    /**
     * @return number of passed {@link AbstractPropertyColumnMapping} instances
     */
    @Override
    public int getColumnCount() {
        return propertyColumnMappings.size();
    }

    /**
     * Directly uses {@link AbstractPropertyColumnMapping#getPropertyTitle()} method for particular column
     */
    @Override
    public String getColumnName(final int columnIndex) {
        return propertyColumnMappings.get(columnIndex).getPropertyTitle();
    }

    /**
     * Directly uses {@link AbstractPropertyColumnMapping#getColumnClass()} method for particular column
     */
    @Override
    public Class<?> getColumnClass(final int columnIndex) {
        return propertyColumnMappings.get(columnIndex).getColumnClass();
    }

    /**
     * @return number of entities plus non-data rows (i.e. rows showing group, grand totals)
     */
    @Override
    public int getRowCount() {
        return instances().size() + nonDataRows.size();
    }

    /**
     * Directly uses {@link AbstractPropertyColumnMapping#getPropertyValue(Object)} method for particular column and entity (at rowIndex)
     */
    @Override
    public Object getValueAt(final int rowIndex, final int columnIndex) {
        if (isDataRow(rowIndex)) {
            final T instance = instance(rowIndex);
            return propertyColumnMappings.get(columnIndex).getPropertyValue(instance);
        } else {
            return null;
        }
    }

    /**
     * This method directly uses {@link PropertyColumnMapping#canSetProperty(Object)()} method for particular column and entity (at rowIndex)
     */
    @Override
    public boolean isCellEditable(final int rowIndex, final int columnIndex) {
        if (isDataRow(rowIndex)) {
            final T entity = instance(rowIndex);
            return getPropertyColumnMappings().get(columnIndex).isPropertyEditable(entity);
        } else {
            return false;
        }
    }

    /**
     * All value-setting-related functionality is implemented in {@link CellEditorListener#editingStopped(javax.swing.event.ChangeEvent)} method added to related
     * {@link EntityGridInspector}
     */
    @Override
    public void setValueAt(final Object value, final int rowIndex, final int columnIndex) {
    }

    /**
     * Directly uses {@link AbstractPropertyColumnMapping#isNavigableTo(Object)} method
     */
    @Override
    public boolean isNavigableAt(final int row, final int column) {
        if (isDataRow(row)) {
            final T entity = instance(row);
            return getPropertyColumnMappings().get(column).isNavigableTo(entity);
        } else {
            return false;
        }
    }

    @Override
    public boolean isNavigationOn() {
        return true;
    }

    /**
     * Adjusts preferred columns size of the provided table to defined in the model.
     * 
     * @param table
     */
    public void adjustDefaultColumnSize(final JTable table) {
        for (int index = 0; index < getPropertyColumnMappings().size(); index++) {
            final AbstractPropertyColumnMapping<T> mapping = getPropertyColumnMappings().get(index);
            if (mapping.getSize() != null) {
                table.getColumnModel().getColumn(index).setPreferredWidth(mapping.getSize());
            }
        }
    }

    /**
     * Removes all previously cached bounded editors and renderer's, so that they would be re-created when needed.<br>
     * <br>
     * Note : This method should be called, when it is not known exactly what changes took place (i.e. setting number of instances or addition or removal). If it is known what
     * changes took place (i.e. it is known, that particular entity was added, removed or particular new list was set) then it would be more correctly to call
     * {@link #addInstances(AbstractEntity)}, {@link #removeInstance(AbstractEntity)} or {@link #setInstances(List)}.
     */
    @Override
    public void fireTableDataChanged() {
        for (final AbstractPropertyColumnMapping<T> propertyColumnMapping : getPropertyColumnMappings()) {
            propertyColumnMapping.setNewEntities(new ArrayList<T>());
        }

        super.fireTableDataChanged();
    }

    /**
     * Simply adds passed instances and updates model using {@link #fireTableRowsInserted(int, int)}.<br>
     * <br>
     * Note : now it is illegal to add duplicate instances(i.e. instances which are equals in terms of {@link Object#equals(Object)}). Such actions may lead to unpredictable
     * behaviour. Please see ticket #121 for details
     * 
     * @param instances
     */
    @SuppressWarnings("unchecked")
    public PropertyTableModel<T> addInstances(final T... instances) {
        final List<T> currInstances = instances();
        currInstances.addAll(asList(instances));

        regroup(currInstances);
        return this;
    }

    /**
     * This method is a list-based version of method {@link #addInstances(AbstractEntity...)}.
     * 
     * @param instances
     */
    public PropertyTableModel<T> addInstances(final List<T> instances) {
        final List<T> currInstances = instances();
        currInstances.addAll(instances);

        regroup(currInstances);
        return this;
    }

    @SuppressWarnings("unchecked")
    private void regroup(final List<T> newInstances) {
        groups.clear();
        groups.addAll(groupingAlgorithm.group(newInstances));

        nonDataRows.clear();
        if (hasGroupTotals) {
            int instancesCount = 0;
            for (final List<T> group : groups) {
                instancesCount += group.size();
                nonDataRows.add(instancesCount + nonDataRows.size());
            }
        }
        if (hasGrandTotals) {
            nonDataRows.add(getRowCount());
        }

        fireTableDataChanged();
    }

    /**
     * Searches for the equal instance in the instances list and replaces it with the passed one.
     * 
     * @return
     */
    public PropertyTableModel<T> refresh(final T instance) {
        return refresh(Arrays.asList(instance));
    }

    /**
     * Refreshes each instance from instances in this table model see {@link #refresh(AbstractEntity)}.
     * 
     * @param instances
     * @return
     */
    public PropertyTableModel<T> refresh(final List<T> entities) {
        final List<T> instances = new ArrayList<T>();
        for (final List<T> group : groups) {
            for (final T currElem : group) {
                // if currElem is among entities to be refreshed, then simply adding that entity to the instances list
                // it is considered that entity ID is assigned from the same set for all entity types.
                // therefore condition currElem.getType().equals(instance.getType()) is not requried
                final int indexOf = EntityUtils.indexOfById(entities, currElem);
                if (indexOf != -1) {
                    instances.add(entities.get(indexOf));
                } else {
                    // otherwise adding currElem
                    instances.add(currElem);
                }
            }
        }

        regroup(instances);
        return this;
    }

    /**
     * See {@link #removeInstances(List)}.
     * 
     * @param instances
     */
    @SuppressWarnings("unchecked")
    public void removeInstances(final T... instances) {
        removeInstances(asList(instances));
    }

    /**
     * Removes the specified instance from table model.
     * 
     * @param instancesToRemove
     */
    public void removeInstances(final List<T> instancesToRemove) {
        final List<T> currInstances = instances();
        for (final Iterator<T> iter = currInstances.iterator(); iter.hasNext();) {
            final T instance = iter.next();
            for (final T instanceToRemove : instancesToRemove) {
                if (EntityUtils.areEqual(instance, instanceToRemove)) {
                    iter.remove();
                    break;
                }
            }
        }

        regroup(currInstances);

    }

    /**
     * Removes all existing instances and adds passed ones. Calls {@link AbstractPropertyColumnMapping#setNewEntities(List)} for each mapping.
     * 
     * @param newEntities
     */
    public PropertyTableModel<T> setInstances(final List<T> newEntities) {
        regroup(newEntities);

        if (getEntityGridInspector() != null) {
            getEntityGridInspector().scrollRowToVisible(0);
        }

        return this;
    }

    /**
     * Removes all instances from table.
     */
    public PropertyTableModel<T> clearInstances() {
        setInstances(new ArrayList<T>());
        return this;
    }

    /**
     * @return list, returned by {@link ReadonlyPropertyTableModel#getPropertyColumnMappings()} casted to {@link List} of {@link AbstractPropertyColumnMapping}
     */
    public List<? extends AbstractPropertyColumnMapping<T>> getPropertyColumnMappings() {
        return Collections.unmodifiableList(propertyColumnMappings);
    }

    /**
     * Return an unmodifiable map between property names and column mappings. It should be used where there is a need to get a column mapping for a specific property by its name.
     * 
     * @return
     */
    public final Map<String, AbstractPropertyColumnMapping<T>> getPropertyColumnMappingsMap() {
        return Collections.unmodifiableMap(propertyColumnMappingsMap);
    }

    /**
     * Returns {@link EntityGridInspector} related to this {@link PropertyTableModel} instance. To tell more precisely, this method returns {@link EntityGridInspector} associated
     * with first {@link AbstractPropertyColumnMapping} in this model.
     * 
     * @return
     */
    public EntityGridInspector<T> getEntityGridInspector() {
        return (getPropertyColumnMappings().size() > 0) ? getPropertyColumnMappings().get(0).getEntityGridInspector() : null;
    }

    //
    // HierarchicalTable-related methods
    //

    @Override
    public Object getChildValueAt(final int row) {
        return null;
    }

    @Override
    public boolean hasChild(final int row) {
        return false;
    }

    @Override
    public boolean isExpandable(final int row) {
        return false;
    }

    @Override
    public boolean isHierarchical(final int row) {
        return false;
    }

    @Override
    public Component createChildComponent(final HierarchicalTable table, final Object value, final int row) {
        return null;
    }

    @Override
    public void destroyChildComponent(final HierarchicalTable table, final Component component, final int row) {
    }

    /**
     * Exports the content, including headers, of this model as an array of bytes produced by GZip output stream representing content of a MS Excel file.
     * <p>
     * The first row corresponds to the header and is styled with bold and bottom border. Inner columns are separated with a thin border.
     * 
     * @param sheetName
     *            -- the name of the sheet to be added to Excel workbook.
     * @throws IOException
     */
    public byte[] exportToExcel(final String sheetName) throws IOException {
        final HSSFWorkbook wb = new HSSFWorkbook();
        final HSSFSheet sheet = wb.createSheet(sheetName);
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
        // Create cells and put column names there
        for (int index = 0; index < getColumnCount(); index++) {
            final HSSFCell cell = headerRow.createCell(index);
            cell.setCellValue(getColumnName(index));
            cell.setCellStyle(index < getColumnCount() - 1 ? headerInnerCellStyle : headerCellStyle);
        }
        // iterate through table rows, add corresponding rows to the sheet and export the data
        // but first let's make cell style to handle borders
        final HSSFCellStyle dataCellStyle = wb.createCellStyle();
        dataCellStyle.setBorderRight(HSSFCellStyle.BORDER_HAIR);
        for (short rowIndex = 0; rowIndex < getRowCount(); rowIndex++) {
            final HSSFRow row = sheet.createRow(rowIndex + 1); // new row starting with 1
            // iterate through values in the current table row and populate the sheet row
            for (int colIndex = 0; colIndex < getColumnCount(); colIndex++) {
                final HSSFCell cell = row.createCell(colIndex); // create new cell
                if (colIndex < getColumnCount() - 1) { // the last column should not have right border
                    cell.setCellStyle(dataCellStyle);
                }
                final Object value = getValueAt(rowIndex, colIndex); // get the value
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
        }

        final ByteArrayOutputStream oStream = new ByteArrayOutputStream();
        final GZipOutputStreamEx zOut = new GZipOutputStreamEx(oStream, Deflater.BEST_COMPRESSION);

        wb.write(zOut);

        zOut.flush();
        zOut.close();
        oStream.flush();
        oStream.close();

        return oStream.toByteArray();
    }

    /**
     * Select the specified row.
     * 
     * @param rowIndex
     */
    public void selectRow(final int rowIndex) {
        getEntityGridInspector().addRowSelectionInterval(rowIndex, rowIndex);
    }

    public void deselectRows() {
        getEntityGridInspector().getSelectionModel().removeSelectionInterval(0, 0);
    }

    public void deselectRows2() {
        getEntityGridInspector().getSelectionModel().clearSelection();
    }

    /**
     * Return an index of the selected row.
     * 
     * @return
     */
    public int getSelectedRow() {
        return getEntityGridInspector().getSelectedRow();
    }

    /**
     * Returns selected entity (or first from selected entities) or null if no entity is selected.
     * 
     * @return
     */
    public T getSelectedEntity() {
        return getEntityAt(getSelectedRow());
    }

    /**
     * Returns null if its not valid row number or it is group or grand totals row number.
     * 
     * @param row
     * @return
     */
    public T getEntityAt(final int row) {
        return row < 0 || isGroupTotalsRow(row) || isGrandTotalsRow(row) ? null : instance(getActualRowAt(getEntityGridInspector().getModel(), row));
    }

    /**
     * Returns list of selected entities.
     * 
     * @return
     */
    public List<T> getSelectedEntities() {
        final List<T> selectedEntities = new ArrayList<T>();
        for (final int selectedRow : getEntityGridInspector().getSelectedRows()) {
            final T selectedEntity = getEntityAt(selectedRow);
            if (selectedEntity != null) {
                selectedEntities.add(selectedEntity);
            }
        }
        return selectedEntities;
    }

    /**
     * Returns true if row, represented as row index, holds instance. False otherwise (for example, row holds totals value).
     * 
     * @param row
     * @return
     */
    public boolean isDataRow(final int row) {
        return !nonDataRows.contains(row);
    }

    /**
     * Returns true if grand totals should be displayed for at least one column and passed row is grand totals row. Returns false otherwise.
     * 
     * @param row
     * @return
     */
    public boolean isGrandTotalsRow(final int row) {
        final int nonDataRowsIndex = nonDataRows.indexOf(row);
        return hasGrandTotals ? nonDataRowsIndex == nonDataRows.size() - 1 : false;
    }

    /**
     * Returns true if group totals should be displayed for at least one column and passed row is group totals row. Returns false otherwise.
     * 
     * @param row
     * @return
     */
    public boolean isGroupTotalsRow(final int row) {
        final int nonDataRowsIndex = nonDataRows.indexOf(row);
        return hasGrandTotals ? nonDataRowsIndex > -1 && nonDataRowsIndex < nonDataRows.size() - 1 : nonDataRows.contains(row);
    }

    public void select(final T instance) {
        final int indexOf = EntityUtils.indexOfById(instances(), instance);
        if (indexOf != -1) {
            selectRow(indexOf);
        }
    }

    /**
     * Returns row of the passed instance in the table. If passed instance is not shown on the table, then -1 value is returned.
     * 
     * @param instance
     * @return
     */
    public int getRowOf(final T instance) {
        return getRow(EntityUtils.indexOfById(instances(), instance));
    }

    /**
     * Returns row of the instance, represented as index in the instance list.
     * 
     * @param instanceIndex
     * @return
     */
    public int getRow(final int instanceIndex) {
        if (instanceIndex < 0 || instanceIndex >= instances().size()) {
            return -1;
        }
        int numberOfNonDataRowsBefore = 0;
        for (final Integer nonDataRow : nonDataRows) {
            if (instanceIndex >= nonDataRow) {
                numberOfNonDataRowsBefore++;
            }
        }
        return instanceIndex + numberOfNonDataRowsBefore;
    }

    /**
     * Returns index of the instance, represented as row in the table. If row contains non instance-related data, -1
     * 
     * @param instanceIndex
     * @return
     */
    public int getIndex(final int row) {
        if (row < 0 || row >= getRowCount() || nonDataRows.contains(row)) {
            return -1;
        } else {
            int numberOfNonDataRowsBefore = 0;
            for (final Integer nonDataRow : nonDataRows) {
                if (row > nonDataRow) {
                    numberOfNonDataRowsBefore++;
                }
            }
            return row - numberOfNonDataRowsBefore;
        }
    }

    public T instance(final int row) {
        final int instanceIndex = getIndex(row);
        return instanceIndex != -1 ? instances().get(instanceIndex) : null;
    }

    /**
     * Returns the list of this table model instances those have component equal to the given value.
     * 
     * @param value
     * @return
     */
    public <E extends AbstractEntity<?>> List<T> instancesForComponent(final E value) {
        final List<T> instancesWithComponent = new ArrayList<>();

        for (final List<T> group : groups) {
            for (final T currElem : group) {

                if (EntityUtils.areEqual(currElem, value) || instanceContainsComponent(currElem, value)) {
                    instancesWithComponent.add(currElem);
                }
            }
        }

        return instancesWithComponent;
    }

    /**
     * Returns the value that indicates whether given instance has {@link AbstractEntity} property equal to the given component or not.
     * 
     * @param instance
     * @param component
     * @return
     */
    private boolean instanceContainsComponent(final AbstractEntity<?> instance, final AbstractEntity<?> component) {
        for (int column = 0; column < getColumnCount(); column++) {
            if (componentForPropertyIsEqualInstance(component, propertyColumnMappings.get(column).getPropertyName(), instance)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns value that indicates whether given instance has property or sub-property equal to the component.
     * 
     * @param component
     * @param propertyName
     * @param instance
     * @return
     */
    private boolean componentForPropertyIsEqualInstance(final AbstractEntity<?> component, final String propertyName, final AbstractEntity<?> instance) {
        final Object value = StringUtils.isEmpty(propertyName) ? instance : instance.get(propertyName);
        if (value instanceof AbstractEntity) {
            return EntityUtils.areEqual(component, (AbstractEntity) value);
        } else {
            return componentForPropertyIsEqualInstance(//
            component, //
                    PropertyTypeDeterminator.isDotNotation(propertyName) ? PropertyTypeDeterminator.penultAndLast(propertyName).getKey() : "",//
                    instance);
        }
    }

    /**
     * Returns unmodifiable list of instances shown on this table model.
     * 
     * @return
     */
    public List<T> instances() {
        final List<T> instances = new ArrayList<T>();
        for (final List<T> group : groups) {
            instances.addAll(group);
        }
        return instances;
    }

    /** A convenient method to check whether model has no entity instances in it. */
    public boolean isEmpty() {
        for (final List<T> group : groups) {
            if (!group.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns group of instances to which passed row belongs.
     * 
     * @param row
     * @return
     */
    public List<T> getGroup(final int row) {
        if (row == getRowCount() - 1) {
            // if grand totals row
            return null;
        }
        for (int i = nonDataRows.size() - 1; i >= 0; i--) {
            if (nonDataRows.get(i) == row) {
                return groups.get(i);
            }
            // first non-data row before specified is the row corresponding to previous group
            if (nonDataRows.get(i) < row) {
                return groups.get(i + 1);
            }
        }
        return null;
    }

    private static class SingleGroupAlgorithm<T> implements GroupingAlgorithm<T> {
        @Override
        public List<List<T>> group(final List<T> elements) {
            final List<List<T>> groups = new ArrayList<List<T>>();
            groups.add(elements);
            return groups;
        }
    }

    public boolean hasGrandTotalsSeparateFooter() {
        return hasGrandTotalsSeparateFooter;
    }

    /**
     * Returns the column index for the property name specified with {@code name}
     * 
     * @param name
     * @return
     */
    public int getColumnForName(final String name) {
        for (int counter = 0; counter < propertyColumnMappings.size(); counter++) {
            if (propertyColumnMappings.get(counter).getPropertyName().equals(name)) {
                return counter;
            }
        }
        return -1;
    }

    public EgiColoringScheme<T> getEgiColoringScheme() {
        return egiColoringScheme;
    }

}
