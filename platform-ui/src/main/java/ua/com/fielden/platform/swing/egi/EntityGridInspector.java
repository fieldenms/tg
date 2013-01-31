package ua.com.fielden.platform.swing.egi;

import static java.util.Collections.unmodifiableList;

import java.awt.Component;
import java.awt.KeyboardFocusManager;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.EventListener;
import java.util.EventObject;
import java.util.List;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.JToolTip;
import javax.swing.KeyStroke;
import javax.swing.event.ChangeEvent;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.reflection.TitlesDescsGetter;
import ua.com.fielden.platform.swing.egi.AbstractPropertyColumnMapping.CustomCellRenderer;
import ua.com.fielden.platform.swing.egi.coloring.EgiColoringScheme;
import ua.com.fielden.platform.swing.egi.events.CellMouseEvent;
import ua.com.fielden.platform.swing.egi.events.CellMouseEventListener;
import ua.com.fielden.platform.swing.egi.events.CellMouseMotionEventListener;
import ua.com.fielden.platform.swing.egi.models.PropertyTableModel;
import ua.com.fielden.platform.swing.egi.models.mappings.simplified.ITooltipGetter;
import ua.com.fielden.platform.swing.utils.DummyBuilder;
import ua.com.fielden.platform.utils.Pair;

import com.jidesoft.grid.CellChangeEvent;
import com.jidesoft.grid.CellStyleProvider;
import com.jidesoft.grid.FilterableTableModel;
import com.jidesoft.grid.HierarchicalTable;
import com.jidesoft.grid.JideCellEditorAdapter;
import com.jidesoft.grid.TableModelWrapperUtils;

/**
 * Table representing list of entities of particular type T
 *
 * @author TG Team
 */
public class EntityGridInspector<T extends AbstractEntity> extends HierarchicalTable {
    private static final long serialVersionUID = 1L;

    private final Logger logger = Logger.getLogger(getClass());

    /**
     * {@link EditorComponent} instance that is handling editing. Null, if table is not in edit mode. This property gains value, when table cell editor is obtained, and is assigned
     * to null in editingStopped() event handler
     */
    private EditorComponent<? extends JComponent, ? extends JComponent> currentEditorComponent = null;

    private PropertyTableModel<T> actualTableModel;

    /*
     * Cell mouse listeners
     */
    private final List<CellMouseEventListener<T>> cellMouseEventListeners = new ArrayList<CellMouseEventListener<T>>();

    private final List<CellMouseMotionEventListener<T>> cellMouseMotionEventListeners = new ArrayList<CellMouseMotionEventListener<T>>();

    /**
     * Directly invokes {@link EntityGridInspector#EntityGridInspector(PropertyTableModel, boolean)} with last parameter set to true
     *
     * @param tableModel
     */
    public EntityGridInspector(final PropertyTableModel<T> tableModel) {
	this(tableModel, true);
    }

    /**
     * Puts passed model inside {@link FilterableTableModel} and sets it as a model. If non-sortable {@link EntityGridInspector} should be created, use this constructor instead of
     * {@link #setSortable(boolean)} method invocation<br>
     * <br>
     * Note : in each mapping inside model, sets reference to itself (via private method setEntityGridInspector({@link EntityGridInspector}) in
     * {@link AbstractPropertyColumnMapping} class using Reflection API). It is strongly recommended to refer to {@link AbstractPropertyColumnMapping} JavaDoc, before using this
     * constructor.<br>
     * Note : sets passed {@link PropertyTableModel} instance as component factory for this {@link HierarchicalTable}. So, in order to provide hierarchical behavior, corresponding
     * methods in {@link PropertyTableModel} should be overridden
     *
     * @param tableModel
     */
    public EntityGridInspector(final PropertyTableModel<T> tableModel, final boolean sortable) {
	super(new FilterableTableModel(tableModel));

	this.actualTableModel = tableModel;

	addCellEditorListener(new JideCellEditorAdapter() {
	    @Override
	    public void editingStopped(final ChangeEvent evt) {
		if (evt instanceof CellChangeEvent) {
		    currentEditorComponent = null;
		}
	    }
	});

	super.setSortable(sortable);
	// adding mouse listeners to fire CellClickedEvents
	addCellMouseListeners();
	// binding property column mappings to columns
	afterModelSet(this.actualTableModel);
	// setting "magic" property to stop editing when focus moves out of the table
	putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
    }

    /**
     * This method rebuilds table by new PropertyTableModel. Note that "listSelectionListener", "cellMouseListener" and "cellMouseMotionListener" have to be already added before
     * using this method (in constructor call).
     *
     * @param tableModel
     */
    public void setPropertyTableModel(final PropertyTableModel<T> tableModel) {
	this.setModel(new FilterableTableModel(tableModel));
	this.actualTableModel = tableModel;
	afterModelSet(this.actualTableModel);
    }

    /**
     * Provides a line-break symbols recursively for a text to make it with <code>maxColumns</code> columns.
     *
     * @param text
     * @param maxColumns
     * @return
     */
    private static String brdText(final String text, final int maxColumns) {
	if (text.length() <= maxColumns) {
	    return text;
	} else {
	    int firstWhiteSpaceWithinMaxWidth = maxColumns;
	    do {
		firstWhiteSpaceWithinMaxWidth--;
	    } while (firstWhiteSpaceWithinMaxWidth >= 0 && text.toCharArray()[firstWhiteSpaceWithinMaxWidth] != ' ');
	    if (firstWhiteSpaceWithinMaxWidth < 0) {
		return text.substring(0, maxColumns) + "<br>" + brdText(text.substring(maxColumns), maxColumns);
	    } else {
		return text.substring(0, firstWhiteSpaceWithinMaxWidth) + "<br>" + brdText(text.substring(firstWhiteSpaceWithinMaxWidth + 1), maxColumns);
	    }
	}
    }

    @Override
    public JToolTip createToolTip() {
	final JToolTip customTooltip = new JToolTip() {
	    @Override
	    public void setTipText(final String tipText) {
//		final String text = TitlesDescsGetter.removeHtmlTag(tipText);
//		final int maxColumns = 20;
//		final int index = text.length() - 1;
//		final StringBuilder sb = new StringBuilder();
//		do {
//
//		} while (index >= maxColumns || text.toCharArray()[index] != ' ');
		final String wrappedByHtml = TitlesDescsGetter.addHtmlTag(brdText(TitlesDescsGetter.removeHtmlTag(tipText == null ? "" : tipText), 50));
	        super.setTipText(wrappedByHtml);
	    }
//
//	    @Override
//	    public void updateUI() {
//	        super.updateUI();
//		setLayout(new MigLayout("debug, fill, insets 10", "[grow, fill, ::200]", "[grow]"));
//		invalidate();
//		revalidate();
//		repaint();
//	    }
	};
	// customTooltip.setLayout(new MigLayout("debug, fill, insets 10", "[grow, fill, ::200]", "[grow]"));
	return customTooltip;
    }

//    @Override
//    public JToolTip createToolTip() {
//	final int maximumWidth = 200;
//
//	final JLabel tipComponent = new JLabel();
//	tipComponent.setBackground(new Color(242, 242, 189));
//
//	final CustomTooltip customTooltip = new CustomTooltip(this, tipComponent, false) {
//	    private static final long serialVersionUID = -951889975845513426L;
//
//	    @Override
//	    public void updateTipText(final String tipText) {
//		final String wrappedByHtml = TitlesDescsGetter.addHtmlTag(TitlesDescsGetter.removeHtmlTag(tipText));
//		tipComponent.setText(wrappedByHtml);
//
//		//tipComponent.setMaximumSize(new Dimension(maximumWidth, 2000));
//
////		final Graphics2D g2 = (Graphics2D) EntityGridInspector.this.getGraphics();
////		final FontMetrics fm = g2.getFontMetrics();
////		final Rectangle2D textBounds = fm.getStringBounds(TitlesDescsGetter.removeHtml(wrappedByHtml), g2);
////		final int allWidth = (int) textBounds.getWidth();
////		final int oneRowHeight = (int) textBounds.getHeight();
////
////		final int rowCount = allWidth / maximumWidth + 1;
////		if (rowCount == 1) {
////		    // System.out.println("allWidth == " + allWidth +  "; rowCount == " + rowCount + "; allWidth % maximumWidth == " + allWidth % maximumWidth + "; oneRowHeight == " + oneRowHeight);
////		    tipComponent.setPreferredSize(new Dimension(allWidth % maximumWidth, oneRowHeight));
////		} else { // > 1
////		    // System.out.println("allWidth == " + allWidth +  "; rowCount == " + rowCount + "; maximumWidth == " + maximumWidth + "; oneRowHeight * rowCount == " + oneRowHeight * rowCount);
////		    tipComponent.setPreferredSize(new Dimension(maximumWidth, oneRowHeight * (rowCount + 2))); // quick fix -- provided additional two rows for multilined tooltips
////		}
//		// tipComponent.setPreferredSize(new Dimension(allWidth, oneRowHeight));
//	    }
//	};
//        return customTooltip;
//    }

    /*
     *
     * Method for handling mouse and mouse motion events on table cells
     */
    /**
     * Adds mouse listener which creates CellMouseEvents from mouse click ones and forwards them to cell mouse event listeners.
     *
     * Also adds mouse listener which creates CellMouseEvents from mouse motion ones and forwards them to cell event listeners.
     */
    private void addCellMouseListeners() {
	// adding mouse listener
	addMouseListener(new MouseAdapter() {
	    /*
	     * No need to listen to mouseEntered/Exited events because they won't occur on every cell, they'll occur when mouse enters and exits whole table.
	     */
	    @Override
	    public void mouseClicked(final MouseEvent e) {
		handleMouseEvent(new MethodInvoker<CellMouseEventListener<T>, CellMouseEvent<T>>() {
		    @Override
		    public void invoke(final CellMouseEventListener<T> obj, final CellMouseEvent<T> param) {
			obj.cellClicked(param);
		    }
		}, e, cellMouseEventListeners);
	    }

	    @Override
	    public void mousePressed(final MouseEvent e) {
		handleMouseEvent(new MethodInvoker<CellMouseEventListener<T>, CellMouseEvent<T>>() {
		    @Override
		    public void invoke(final CellMouseEventListener<T> obj, final CellMouseEvent<T> param) {
			obj.cellPressed(param);
		    }
		}, e, cellMouseEventListeners);
	    }

	    @Override
	    public void mouseReleased(final MouseEvent e) {
		handleMouseEvent(new MethodInvoker<CellMouseEventListener<T>, CellMouseEvent<T>>() {
		    @Override
		    public void invoke(final CellMouseEventListener<T> obj, final CellMouseEvent<T> param) {
			obj.cellReleased(param);
		    }
		}, e, cellMouseEventListeners);
	    }

	});
	// adding mouse motion listener
	addMouseMotionListener(new MouseMotionListener() {
	    @Override
	    public void mouseDragged(final MouseEvent e) {
		handleMouseEvent(new MethodInvoker<CellMouseMotionEventListener<T>, CellMouseEvent<T>>() {
		    @Override
		    public void invoke(final CellMouseMotionEventListener<T> obj, final CellMouseEvent<T> param) {
			obj.mouseDragged(param);
		    }
		}, e, cellMouseMotionEventListeners);
	    }

	    @Override
	    public void mouseMoved(final MouseEvent e) {
		handleMouseEvent(new MethodInvoker<CellMouseMotionEventListener<T>, CellMouseEvent<T>>() {
		    @Override
		    public void invoke(final CellMouseMotionEventListener<T> obj, final CellMouseEvent<T> param) {
			obj.mouseMoved(param);
		    }
		}, e, cellMouseMotionEventListeners);
	    }
	});
    }

    /**
     * Method which creates {@CellMouseEvent} out of {@MouseEvent} and sends it to handler represented by {@MethodInvoker} class.
     *
     * @param eventHandler
     * @param e
     * @param entity
     * @param propertyName
     */
    private <CellMouseEventListenerType extends EventListener> void handleMouseEvent(final MethodInvoker<CellMouseEventListenerType, CellMouseEvent<T>> eventHandler, final MouseEvent e, final List<CellMouseEventListenerType> eventListeners) {
	if (eventListeners.isEmpty()) {
	    return;
	}
	final Pair<T, String> entityAndPropertyName = getEntityAndPropertyName(e);
	if (entityAndPropertyName == null) {
	    return;
	}

	final CellMouseEvent<T> event = new CellMouseEvent<T>(e, entityAndPropertyName.getKey(), entityAndPropertyName.getValue());
	for (final CellMouseEventListenerType eventListener : eventListeners) {
	    eventHandler.invoke(eventListener, event);
	}
    }

    /**
     * Interface for invoking one-parameter method on objects.
     *
     * @author Yura
     *
     * @param <T>
     * @param <P>
     */
    private static interface MethodInvoker<T, P> {
	void invoke(T obj, P param);
    }

    /**
     * Returns null if mouse event is not over particular property of entity. Otherwise returns entity and property name.
     *
     * @param e
     * @return
     */
    private Pair<T, String> getEntityAndPropertyName(final MouseEvent e) {
	final int row = TableModelWrapperUtils.getActualRowAt(getModel(), rowAtPoint(e.getPoint()));
	final int column = getColumnModel().getColumn(columnAtPoint(e.getPoint())).getModelIndex();
	if (row == -1 || column == -1) {
	    return null;
	}
	return new Pair<T, String>(getActualModel().instance(row), getActualModel().getPropertyColumnMappings().get(column).getPropertyName());
    }

    public void addCellMouseEventListener(final CellMouseEventListener<T> eventListener) {
	cellMouseEventListeners.add(eventListener);
    }

    public boolean removeCellMouseEventListener(final CellMouseEventListener<T> eventListener) {
	return cellMouseEventListeners.remove(eventListener);
    }

    public List<CellMouseEventListener<T>> getCellMouseEventListeners() {
	return unmodifiableList(cellMouseEventListeners);
    }

    public void addCellMouseMotionEventListener(final CellMouseMotionEventListener<T> eventListener) {
	cellMouseMotionEventListeners.add(eventListener);
    }

    public void removeCellMouseMotionEventListener(final CellMouseMotionEventListener<T> eventListener) {
	cellMouseMotionEventListeners.remove(eventListener);
    }

    public List<CellMouseMotionEventListener<T>> getCellMouseMotionEventListeners() {
	return unmodifiableList(cellMouseMotionEventListeners);
    }

    /*
     * Methods for handling mouse and mouse motion events on table cells
     */

    /*
     *
     * EGI tool-tip management methods
     */
    @Override
    public String getTableHeaderToolTipText(final int column) {
	final AbstractPropertyColumnMapping<T> mappings = getActualModel().getPropertyColumnMappings().get(column);
	final String headerTooltip = mappings.getHeaderTooltip();
	return !StringUtils.isEmpty(headerTooltip) ? headerTooltip : super.getTableHeaderToolTipText(column);
    }

    /**
     * This method determines entity and its property in EGI from passed {@link MouseEvent} instance, gets tool-tip for it and returns it as {@link JTable} tool-tip.
     */
    @Override
    public String getToolTipText(final MouseEvent event) {
	super.getToolTipText(event);

	final Point p = event.getPoint();
	final int column = getColumnModel().getColumn(columnAtPoint(p)).getModelIndex();
	final ITooltipGetter tooltipGetter = getActualModel().getPropertyColumnMappings().get(column).getTooltipGetter();

	if (tooltipGetter != null) {
	    final int row = TableModelWrapperUtils.getActualRowAt(getModel(), rowAtPoint(p));
	    if (row == -1 || !getActualModel().isDataRow(row)) {
		return getToolTipText();
	    }

	    final AbstractEntity entity = getActualModel().instance(row);
	    if (entity == null) {
		return getToolTipText();
	    }

	    final String tooltip = tooltipGetter.getTooltip(entity);

	    if (tooltip != null) {
		return tooltip;
	    }
	}
	return getToolTipText();
    }

    /*
     * EGI tool-tip management methods
     */

    /*
     *
     * EGI data model retrieving methods
     */
    public <TableModelType extends TableModel> TableModelType getModelOf(final Class<TableModelType> tableModelClass) {
	return (TableModelType) TableModelWrapperUtils.getActualTableModel(getModel(), tableModelClass);
    }

    public PropertyTableModel<T> getActualModel() {
	return actualTableModel;
    }

    /*
     * EGI data model retrieving methods
     */

    @Override
    protected boolean isNavigationKey(final KeyStroke key) {
	if (KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0, false).equals(key)) {
	    return false;
	}
	return super.isNavigationKey(key);
    }

    @Override
    public void setSortable(final boolean sortable) {
	throw new UnsupportedOperationException("Cannot invoke this method after EntityGridInspector has been created. Please use constructor with boolean parameter named 'sortable'");
    }

    /**
     * Initializes all renderer's and editors; sets passed {@link PropertyTableModel} as component factory for this {@link HierarchicalTable}, disables column reordering. Also,
     * sets several other properties of less importance.<br>
     * <br>
     * Note: this method should be called before {@link #addCellMouseListeners()}
     *
     * @param tableModel
     */
    private void afterModelSet(final PropertyTableModel<T> tableModel) {
	// initializing editors and renderer's
	for (int i = 0; i < getColumnCount(); i++) {
	    final AbstractPropertyColumnMapping<T> propertyColumnMapping = getActualModel().getPropertyColumnMappings().get(i);
	    propertyColumnMapping.setEntityGridInspector(this);

	    final TableColumn tableColumn = getColumnModel().getColumn(i);

	    tableColumn.setCellEditor(propertyColumnMapping.getCellEditor());
	    tableColumn.setCellRenderer(propertyColumnMapping.getCellRenderer());

	    addCellMouseEventListener(new CellMouseEventListener<T>() {
		@Override
		public void cellClicked(final CellMouseEvent<T> event) {
		    if (propertyColumnMapping.getClickAction() != null && event.getInitiatorEvent().getClickCount() == 2
			    && event.getPropertyName().equals(propertyColumnMapping.getPropertyName())) {
			propertyColumnMapping.getClickAction().actionPerformed(new ActionEvent(event.getEntity(), 1, "double-click"));
		    }
		}

		@Override
		public void cellPressed(final CellMouseEvent<T> event) {
		}

		@Override
		public void cellReleased(final CellMouseEvent<T> event) {
		}
	    });
	}

	// HierarchicalTable-related settings
	setHierarchicalColumn(-1);
	setComponentFactory(tableModel);
	// adjusting column sizes
	setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
	setColumnResizable(true);
	setColumnAutoResizable(true);
	tableModel.adjustDefaultColumnSize(this);
    }

    /**
     * @return list of property column mappings in the order they appear in the table. Please also note that this method sets preferred sizes of the columns to current column
     *         widths.
     */
    @SuppressWarnings("unchecked")
    public List<AbstractPropertyColumnMapping<T>> getCurrentColumnsState() {
	final List<AbstractPropertyColumnMapping<T>> mappings = new ArrayList<AbstractPropertyColumnMapping<T>>();
	for (int i = 0; i < getColumnModel().getColumnCount(); i++) {
	    final TableColumn column = getColumnModel().getColumn(i);
	    final CustomCellRenderer renderer = (CustomCellRenderer) column.getCellRenderer();
	    mappings.add(renderer.getPropertyColumnMapping().setSize(column.getWidth()));
	}
	return mappings;
    }

    @Override
    public boolean editCellAt(final int row, final int column, final EventObject e) {
	final boolean result = super.editCellAt(row, column, e);

	final int actualColumn = getColumnModel().getColumn(column).getModelIndex();
	final AbstractPropertyColumnMapping<T> mapping = getActualModel().getPropertyColumnMappings().get(actualColumn);
	if (result && mapping.redispatchEventsToEditor()) {
	    final JComponent editorItself = currentEditorComponent.getEditorItself();
	    if (!currentEditorComponent.getEditorItself().hasFocus()) {
		if (e instanceof KeyEvent) {
		    editorItself.requestFocusInWindow();
		    DummyBuilder.invokeWhenGainedFocus(editorItself, new Runnable() {
			public void run() {
			    dispatchFullKeyEvent(editorItself, (KeyEvent) e);
			}
		    });
		} else if (e instanceof MouseEvent) {
		    // special case for JCheckBoxes editors - 'requestFocusEnabled' should be set to false on editing
		    if (editorItself instanceof JCheckBox && editorItself.isRequestFocusEnabled()) {
			editorItself.setRequestFocusEnabled(false);
		    }
		    // dispatching MouseEvent directly to JCheckBox editor
		    dispatchFullMouseEvent(editorItself, (MouseEvent) e, row, column);
		}
	    }
	}
	return result;
    }

    void setCurrentEditorComponent(final EditorComponent<? extends JComponent, ? extends JComponent> currentEditorComponent) {
	this.currentEditorComponent = currentEditorComponent;
    }

    EditorComponent<? extends JComponent, ? extends JComponent> getCurrentEditorComponent() {
	return currentEditorComponent;
    }

    /**
     * Creates three copies of passed keyEvent (key pressed, released and typed) and dispatches them to passed component in respective order
     *
     * @param component
     * @param keyEvent
     */
    private static void dispatchFullKeyEvent(final Component component, final KeyEvent keyEvent) {
	// generating key pressed event
	KeyEvent newKeyEvent = new KeyEvent((Component) keyEvent.getSource(), KeyEvent.KEY_PRESSED, System.currentTimeMillis(), keyEvent.getModifiers(), keyEvent.getKeyCode(), keyEvent.getKeyChar(), keyEvent.getKeyLocation());
	KeyboardFocusManager.getCurrentKeyboardFocusManager().redispatchEvent(component, newKeyEvent);

	// generating key released event
	newKeyEvent = new KeyEvent((Component) keyEvent.getSource(), KeyEvent.KEY_RELEASED, System.currentTimeMillis(), keyEvent.getModifiers(), keyEvent.getKeyCode(), keyEvent.getKeyChar(), keyEvent.getKeyLocation());
	KeyboardFocusManager.getCurrentKeyboardFocusManager().redispatchEvent(component, newKeyEvent);

	// generating key typed event
	newKeyEvent = new KeyEvent((Component) keyEvent.getSource(), KeyEvent.KEY_TYPED, System.currentTimeMillis(), keyEvent.getModifiers(), KeyEvent.VK_UNDEFINED, keyEvent.getKeyChar(), KeyEvent.KEY_LOCATION_UNKNOWN);
	KeyboardFocusManager.getCurrentKeyboardFocusManager().redispatchEvent(component, newKeyEvent);
    }

    /**
     * Creates three copies of passed mouseEvent (mouse pressed, released and clicked) and dispatches them to passed component in respective order
     *
     * @param component
     * @param mouseEvent
     */
    private void dispatchFullMouseEvent(final Component component, final MouseEvent mouseEvent, final int row, final int column) {
	final Rectangle cellRectangle = getCellRect(row, column, true);
	final int trueX = (int) (mouseEvent.getX() - cellRectangle.getX());
	final int trueY = (int) (mouseEvent.getY() - cellRectangle.getY());

	final MouseEvent newMouseEvent = new MouseEvent(component, MouseEvent.MOUSE_RELEASED, System.currentTimeMillis(), mouseEvent.getModifiers(), trueX, trueY, mouseEvent.getClickCount(), mouseEvent.isPopupTrigger(), mouseEvent.getButton());
	component.dispatchEvent(newMouseEvent);
    }

    /**
     * Overrided this method in order to warn users of {@link EgiColoringScheme} functionality corruption.
     */
    @Override
    public void setCellStyleProvider(final CellStyleProvider csp) {
	logger.warn("Setting of specific Jide Cell Style Provider may corrupt " + EgiColoringScheme.class.getName()
		+ " functionality. Please note, that EGI automatically supports gray-white row painting. For custom row/column painting please use "
		+ EgiColoringScheme.class.getName() + " functionality.");
	super.setCellStyleProvider(csp);
    }

}
