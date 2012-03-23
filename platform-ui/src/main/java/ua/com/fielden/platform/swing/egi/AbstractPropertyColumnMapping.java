package ua.com.fielden.platform.swing.egi;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractCellEditor;
import javax.swing.Action;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.swing.components.ValidationLayer;
import ua.com.fielden.platform.swing.components.bind.development.BoundedValidationLayer;
import ua.com.fielden.platform.swing.components.bind.development.ComponentFactory.IOnCommitAction;
import ua.com.fielden.platform.swing.egi.models.PropertyTableModel;
import ua.com.fielden.platform.swing.egi.models.mappings.AggregationFunction;
import ua.com.fielden.platform.swing.egi.models.mappings.ColumnTotals;
import ua.com.fielden.platform.swing.egi.models.mappings.simplified.DefaultTooltipGetter;
import ua.com.fielden.platform.swing.egi.models.mappings.simplified.ITooltipGetter;
import ua.com.fielden.platform.swing.utils.RenderingDecorator;

import com.jidesoft.grid.TableModelWrapperUtils;

import static com.jidesoft.grid.TableModelWrapperUtils.getActualRowAt;
import static javax.swing.SwingUtilities.invokeLater;
import static org.apache.commons.lang.StringUtils.isEmpty;
import static ua.com.fielden.platform.swing.utils.DummyBuilder.invokeWhenGainedFocus;
import static ua.com.fielden.platform.swing.utils.Utils2D.blend;

/**
 * Represents mapping between column and some read/write access property of class T.<br>
 * <br>
 * Note: one mapping could be associated only with one {@link EntityGridInspector}, i.e. to use the same mapping for different {@link EntityGridInspector}s, one should create new
 * mapping for each {@link EntityGridInspector} <br>
 * Note : to enable header tool-tip on column, represented by this class, {@link #getHeaderTooltip()} should be overridden
 *
 * @author Yura
 *
 * @param <T>
 *            - type of entity, property of which is mapped
 * @param <ColumnType>
 *            - type of value, shown in cell (for instance property type could be {@link Rotable} but it would be shown probably as String (i.e. only key))
 */
@SuppressWarnings("unchecked")
public abstract class AbstractPropertyColumnMapping<T extends AbstractEntity> {
    /**
     * Singleton renderer to draw totals
     */
    private transient DefaultTableCellRenderer totalsRenderer = new DefaultTableCellRenderer();

    private final String propertyName;

    private String propertyTitle;

    private final Class<?> columnClass;

    /**
     * Preferred column size
     */
    private Integer size;

    private transient EntityGridInspector<T> entityGridInspector;

    private String headerTooltip;

    private final ITooltipGetter<T> tooltipGetter;

    private transient Action clickAction;

    private transient ColumnTotals columnTotals;

    private transient AggregationFunction<T> aggregateFunc;

    protected final Map<T, EditorComponent<? extends BoundedValidationLayer<? extends JComponent>, ? extends JComponent>> editorComponents = new HashMap<T, EditorComponent<? extends BoundedValidationLayer<? extends JComponent>, ? extends JComponent>>();

    private transient TableCellEditor cellEditor = new CustomCellEditor();

    private transient TableCellRenderer cellRenderer = new CustomCellRenderer();

    /**
     * Creates instance of {@link AbstractPropertyColumnMapping} class, which should know how to map (i.e. get/set) certain property of T class to cells.
     *
     * @param propertyName
     * @param columnClass
     * @param columnName
     * @param prefSize
     * @param headerTooltip
     * @param tooltipGetter
     * @param clickAction
     * @param columnTotals
     * @param aggregateFunc
     */
    public AbstractPropertyColumnMapping(final String propertyName, final Class columnClass, final String columnName, final Integer prefSize, final String headerTooltip, final ITooltipGetter<T> tooltipGetter, final Action clickAction, final ColumnTotals columnTotals, final AggregationFunction<T> aggregateFunc) {
	this.propertyName = propertyName;
	this.propertyTitle = columnName;
	this.columnClass = columnClass;
	this.size = prefSize;
	this.headerTooltip = headerTooltip;
	this.tooltipGetter = tooltipGetter != null ? tooltipGetter : new DefaultTooltipGetter<T>(propertyName);
	this.clickAction = clickAction;

	this.columnTotals = columnTotals;
	this.aggregateFunc = aggregateFunc;
    }

    /**
     * Returns true if generally this property could be set, false otherwise; this method is used directly in {@link PropertyTableModel#isCellEditable(int, int)} method
     *
     * @param entity
     */
    public abstract boolean isPropertyEditable(T entity);

    /**
     * Should return true, if cell, representing property of passed entity, could be navigated to. Otherwise should return false
     *
     * @param entity
     * @return
     */
    public abstract boolean isNavigableTo(T entity);

    public TableCellEditor getCellEditor() {
	// emulating transient-final behaviour using absence of setter and default initialisation during first access
	if (cellEditor == null) {
	    cellEditor = new CustomCellEditor();
	}
	return cellEditor;
    }

    public TableCellRenderer getCellRenderer() {
	// emulating transient-final behaviour using absence of setter and default initialisation during first access
	if (cellRenderer == null) {
	    cellRenderer = new CustomCellRenderer();
	}
	return cellRenderer;
    }

    /**
     * Should return true if {@link EntityGridInspector} should redirect {@link KeyEvent}s directly to {@link EditorComponent#getEditorItself()} component on editing starting, or
     * false if no action is required.<br>
     * <br>
     * Note : it is advised to return true in this method if editor component consists of several layers (e.g. {@link ValidationLayer} with {@link JCheckBox}), and return false
     * otherwise (when editor component consists of one component - e.g. {@link JTextField} only)<br>
     * Note : in case when this method returns true and editor component consists of one component, because of redirection {@link KeyEvent}s could be dispatched twice.
     *
     * @return
     */
    public boolean redispatchEventsToEditor() {
	return true;
    }

    /**
     * Should return true, if component returned by {@link #getCellEditor(Object)} as cell editor should be decorated using
     * {@link RenderingDecorator#decorateEditor(JComponent, JTable)} method. Should otherwise return false
     *
     * @return
     */
    public boolean decorateEditor() {
	return true;
    }

    /**
     * Should return {@link Component} which would be directly used as renderer for particular cell. Main parameters are entity and value, other parameters have the same meaning as
     * in the {@link TableCellRenderer#getTableCellRendererComponent(JTable, Object, boolean, boolean, int, int) method.<br>
     * <br> Note : if this method returns null, then this cell would be rendered as simple empty {@link JLabel} using {@link DefaultTableCellRenderer}
     *
     * @param entity
     * @param value
     * @param isSelected
     * @param hasFocus
     * @param table
     * @param row
     * @param column
     * @return
     */
    public abstract JComponent getCellRendererComponent(final T entity, final Object value, final boolean isSelected, final boolean hasFocus, final JTable table, final int row, final int column);

    protected DefaultTableCellRenderer getTotalsRenderer() {
	// emulating transient-final behaviour using absence of setter and default initialisation during first access
	if (totalsRenderer == null) {
	    totalsRenderer = new DefaultTableCellRenderer();
	}
	return totalsRenderer;
    }

    /**
     * Returns component for rendering non-data rows (i.e. group and grand totals). By default it returns {@link DefaultTableCellRenderer} with total value displayed on it.
     *
     * @param isSelected
     * @param hasFocus
     * @param row
     * @param column
     * @return
     */
    public JComponent getGrandTotalsRendererComponent(final boolean isSelected, final boolean hasFocus, final int row, final int column) {
	final String value = getColumnTotals().hasGrandTotals() ? calculateGrandTotal(row) : "";
	return (JComponent) getTotalsRenderer().getTableCellRendererComponent(getEntityGridInspector(), value, isSelected, hasFocus, row, column);
    }

    public JComponent getGroupTotalsRendererComponent(final boolean isSelected, final boolean hasFocus, final int row, final int column) {
	final String value = getColumnTotals().hasGroupTotals() ? calculateGroupTotal(row) : "";
	return (JComponent) getTotalsRenderer().getTableCellRendererComponent(getEntityGridInspector(), value, isSelected, hasFocus, row, column);
    }

    private String calculateGroupTotal(final int row) {
	if (!getColumnTotals().hasGroupTotals()) {
	    throw new IllegalStateException("This column is not marked for calculating group totals.");
	}
	final PropertyTableModel<T> tableModel = getEntityGridInspector().getActualModel();
	if (!tableModel.isGroupTotalsRow(row)) {
	    throw new IllegalStateException("Row " + row + " is not group totals row.");
	}
	return aggregateFunc.calc(tableModel.getGroup(row));
    }

    private String calculateGrandTotal(final int row) {
	if (!getColumnTotals().hasGrandTotals()) {
	    throw new IllegalStateException("This column is not marked for calculating grand totals.");
	}
	final PropertyTableModel<T> tableModel = getEntityGridInspector().getActualModel();
	if (!tableModel.isGrandTotalsRow(row)) {
	    throw new IllegalStateException("Row " + row + " is not grand totals row.");
	}
	return aggregateFunc.calc(tableModel.instances());
    }

    /**
     * Should return true, if component returned by {@link #getCellRendererComponent(Object, Object, boolean, boolean, JTable, int, int)} as cell renderer should be decorated using
     * {@link RenderingDecorator#decorateRenderer(JComponent, JTable, boolean, boolean, int, int)} method. Should otherwise return false
     *
     * @return
     */
    public boolean decorateRenderer() {
	return true;
    }

    /**
     * Should return true, if passed {@link EventObject} should start cell editing in table or not
     *
     * @param e
     * @return
     */
    public boolean startCellEditingOn(final EventObject e) {
	if (e instanceof MouseEvent) {
	    return ((MouseEvent) e).getClickCount() >= 2;
	}
	return true;
    }

    public String getHeaderTooltip() {
	return headerTooltip;
    }

    public String getPropertyName() {
	return propertyName;
    }

    /**
     * Returns value, that should be used to represent property of passed entity (i.e. when mapping property of {@link RotableClass} type to {@link String} cell, then we could
     * return {@link RotableClass#getKey()}); This value would be used in {@link TableCellRenderer} to render cell representing this property of passed entity
     *
     * @param entity
     */
    public Object getPropertyValue(final T entity) {
	return isEmpty(getPropertyName()) ? entity : entity.get(getPropertyName());
    }

    public String getPropertyTitle() {
	return propertyTitle;
    }

    public Class<?> getColumnClass() {
	return columnClass;
    }

    public Integer getSize() {
	return size;
    }

    /**
     * Returns bounded to property of passed entity {@link EditorComponent}, which was returned by {@link #createBoundedEditorFor(AbstractEntity)} method.
     */
    public EditorComponent<? extends ValidationLayer<? extends JComponent>, ? extends JComponent> getCellEditor(final T entity) {
	return getEditorComponent(entity);
    }

    /**
     * Creates (if needed) and returns {@link EditorComponent} for passed entity
     *
     * @param entity
     * @return
     */
    protected final EditorComponent<? extends BoundedValidationLayer<? extends JComponent>, ? extends JComponent> getEditorComponent(final T entity) {
	final EditorComponent<? extends BoundedValidationLayer<? extends JComponent>, ? extends JComponent> editorComponent = editorComponents.get(entity);
	if (editorComponent != null) {
	    return editorComponent;
	}
	final EditorComponent<? extends BoundedValidationLayer<? extends JComponent>, ? extends JComponent> newEditorComponent = createBoundedEditorFor(entity);

	// adding row update firing after data has been committed
	if (newEditorComponent.getCellEditorComponent().getOnCommitActionable() != null) {
	    newEditorComponent.getCellEditorComponent().addOnCommitAction(new IOnCommitAction() {
		@Override
		public void postCommitAction() {
		    final int rowIndex = getEntityGridInspector().getActualModel().getRowOf(entity);
		    getEntityGridInspector().getActualModel().fireTableRowsUpdated(rowIndex, rowIndex);
		}

		@Override
		public void postNotSuccessfulCommitAction() {
		}

		@Override
		public void postSuccessfulCommitAction() {
		}
	    });
	}

	// adding listener that moves focus back to grid, after TAB or ENTER has been pressed on editor component
	newEditorComponent.getEditorItself().addKeyListener(new KeyAdapter() {
	    @Override
	    public void keyPressed(final KeyEvent e) {
		if ((e.getKeyChar() == '\t' || e.getKeyCode() == KeyEvent.VK_ENTER) && !getEntityGridInspector().hasFocus()) {
		    getEntityGridInspector().requestFocusInWindow();
		    invokeWhenGainedFocus(getEntityGridInspector(), new Runnable() {
			public void run() {
			    getEntityGridInspector().removeEditor();
			}
		    });
		}
	    }
	});
	editorComponents.put(entity, newEditorComponent);

	return newEditorComponent;
    }

    /**
     * Should return {@link EditorComponent} bounded to passed entity. This method would be called only once for each entity (however, order and time of calls is not strictly
     * determined)
     *
     * @see #getCellEditor(AbstractEntity)
     * @param entity
     * @return
     */
    public abstract EditorComponent<? extends BoundedValidationLayer<? extends JComponent>, ? extends JComponent> createBoundedEditorFor(T entity);

    /**
     * Sets reference to corresponding {@link EntityGridInspector}.<br>
     * <br>
     * <br>
     * Note : currently it is used only in {@link EntityGridInspector}'s constructor, manual re-setting of {@link EntityGridInspector}'s reference is discouraged.
     *
     * @param tableModel
     */
    protected void setEntityGridInspector(final EntityGridInspector<T> entityGridInspector) {
	if (this.entityGridInspector != null) {
	    editorComponents.clear();
	}
	this.entityGridInspector = entityGridInspector;
    }

    /**
     * Returns {@link EntityGridInspector}, in which this mapping is used
     *
     * @return
     */
    public EntityGridInspector<T> getEntityGridInspector() {
	return entityGridInspector;
    }

    public ITooltipGetter<T> getTooltipGetter() {
	return tooltipGetter;
    }

    /**
     * Re-binds available editors and renderer's to passed list of entities. Remaining editors and renderer's (if they exist) are unbounded from their entities. Mappings between
     * entities and their bounded editors and renderer's are updated.
     *
     * This method should not be called directly. Use {@link PropertyTableModel#setInstances(List)} instead.
     */
    public void setNewEntities(final List<T> newEntities) {
	final List<EditorComponent<? extends BoundedValidationLayer<? extends JComponent>, ? extends JComponent>> oldBoundedEditors = new ArrayList<EditorComponent<? extends BoundedValidationLayer<? extends JComponent>, ? extends JComponent>>(editorComponents.values());
	editorComponents.clear();

	if (newEntities.isEmpty()) {
	    // un-bounding all editors
	    for (final EditorComponent<? extends BoundedValidationLayer<? extends JComponent>, ? extends JComponent> oldBoundedEditor : oldBoundedEditors) {
		oldBoundedEditor.getCellEditorComponent().unbound();
	    }
	} else {
	    // re-binding available editors and un-bounding remaining ones
	    final int numberOfAvailableBoundedEditors = Math.min(newEntities.size(), editorComponents.size());
	    for (int i = 0; i < oldBoundedEditors.size(); i++) {
		if (i < numberOfAvailableBoundedEditors) {
		    oldBoundedEditors.get(i).getCellEditorComponent().rebindTo(newEntities.get(i));
		    editorComponents.put(newEntities.get(i), oldBoundedEditors.get(i));
		} else {
		    oldBoundedEditors.get(i).getCellEditorComponent().unbound();
		}
	    }
	}
    }

    AbstractPropertyColumnMapping<T> setSize(final int prefSize) {
	this.size = prefSize;
	return this;
    }

    /**
     * Removes instance and un-bounds corresponding editors and renderer's (if this instance is in map). This method should not be called directly, use
     * {@link PropertyTableModel#removeInstance(AbstractEntity)} instead.
     *
     * @param instance
     */
    public void removeInstance(final T instance) {
	final EditorComponent<? extends BoundedValidationLayer<? extends JComponent>, ? extends JComponent> editorComponent = editorComponents.remove(instance);
	if (editorComponent != null) {
	    editorComponent.getCellEditorComponent().unbound();
	}
    }

    public Action getClickAction() {
	return clickAction;
    }

    public ColumnTotals getColumnTotals() {
	if (columnTotals == null) {
	    columnTotals = ColumnTotals.NO_TOTALS;
	}
	return columnTotals;
    }

    /**
     * Cell editor which is associated with particular {@link AbstractPropertyColumnMapping} from enclosing instance of {@link EntityGridInspector} and returns its
     * {@link EditorComponent} as editor.
     *
     * @author Yura
     */
    private class CustomCellEditor extends AbstractCellEditor implements TableCellEditor {

	private static final long serialVersionUID = -5460847470168438302L;

	@Override
	public Component getTableCellEditorComponent(final JTable table, final Object value, final boolean isSelected, final int row, final int column) {
	    final int actualRow = getActualRowAt(table.getModel(), row);
	    final T entity = getEntityGridInspector().getActualModel().instance(actualRow);

	    getEntityGridInspector().setCurrentEditorComponent(getCellEditor(entity));
	    final JComponent editorComponent = getEntityGridInspector().getCurrentEditorComponent().getCellEditorComponent();
	    if (decorateEditor()) {
		// invoking decoration in this way, because otherwise editor is not decorated properly
		invokeLater(new Runnable() {
		    public void run() {
			RenderingDecorator.decorateEditor(editorComponent, table);
		    }
		});
	    }

	    return editorComponent;
	}

	@Override
	public Object getCellEditorValue() {
	    // don't know what this method is for
	    return null;
	}

	@Override
	public boolean isCellEditable(final EventObject anEvent) {
	    return startCellEditingOn(anEvent);
	}
    }

    /**
     * Cell renderer which picks particular {@link AbstractPropertyColumnMapping} from enclosing instance of {@link EntityGridInspector} and returns renderer {@link Component}
     *
     * @author Yura
     */
    class CustomCellRenderer implements TableCellRenderer {

	AbstractPropertyColumnMapping<T> getPropertyColumnMapping() {
	    return AbstractPropertyColumnMapping.this;
	}

	@Override
	public Component getTableCellRendererComponent(final JTable table, final Object value, final boolean isSelected, final boolean hasFocus, final int row, final int column) {
	    final PropertyTableModel<T> propertyTableModel = getEntityGridInspector().getActualModel();
	    if (propertyTableModel.isDataRow(row)) {
		return drawDataCell(table, value, isSelected, hasFocus, row, column);
	    } else if (propertyTableModel.isGroupTotalsRow(row)) {
		return getGroupTotalsRendererComponent(isSelected, hasFocus, row, column);
	    } else {
		return getGrandTotalsRendererComponent(isSelected, hasFocus, row, column);
	    }
	}

	private JComponent drawDataCell(final JTable table, final Object value, final boolean isSelected, final boolean hasFocus, final int row, final int column) {
	    final int actualRow = TableModelWrapperUtils.getActualRowAt(table.getModel(), row);
	    final T entity = getEntityGridInspector().getActualModel().instance(actualRow);

	    JComponent rendererComponent = getCellRendererComponent(entity, value, isSelected, hasFocus, table, row, column);

	    if (rendererComponent == null) {
		rendererComponent = (JComponent) new DefaultTableCellRenderer().getTableCellRendererComponent(table, "", isSelected, hasFocus, row, column);
	    } else {
		if (decorateRenderer()) {
		    RenderingDecorator.decorateRenderer(rendererComponent, table, isSelected, hasFocus, row, column);
		}
	    }

	    final Color fgColor = getEntityGridInspector().getActualModel().getEgiColoringScheme().getFgColour(entity, propertyName);
	    if (fgColor != null) {
		// if renderer's background colour is not null then need to blend the defined foreground colour with it
		rendererComponent.setForeground(rendererComponent.getBackground() == null ? //
			fgColor	: blend(fgColor, rendererComponent.getBackground(), 0.5));
	    }

	    final Color backgroundColor = getEntityGridInspector().getActualModel().getEgiColoringScheme().getBgColour(entity, propertyName);
	    if (backgroundColor != null) {
		// if renderer's previous background colour is not null then blend the defined colour with it
		rendererComponent.setBackground(rendererComponent.getBackground() == null ? //
		backgroundColor	: blend(backgroundColor, rendererComponent.getBackground(), 0.5));
	    }

	    return rendererComponent;
	}
    }

    public void setPropertyTitle(final String propertyTitle) {
	this.propertyTitle = propertyTitle;
    }

    public void setHeaderTooltip(final String headerTooltip) {
	this.headerTooltip = headerTooltip;
    }

    /**
     * Sets click action, that will be performed upon clicks on this column.
     *
     * @param clickAction
     */
    public void setClickAction(final Action clickAction) {
	this.clickAction = clickAction;
    }

    public void setAggregateFunc(final AggregationFunction<T> aggregateFunc) {
	this.aggregateFunc = aggregateFunc;
    }

    /**
     * Currently it is only permitted and supported to set {@link #columnTotals} to {@link ColumnTotals#GRAND_TOTALS}. Also, this method should be invoked before
     * {@link PropertyTableModel} is created. Otherwise, set this parameter at your own risk.
     *
     * @param columnTotals
     */
    public void setColumnTotals(final ColumnTotals columnTotals) {
	this.columnTotals = columnTotals;
    }

}
