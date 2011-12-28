package ua.com.fielden.platform.swing.review.report.centre;

import static java.awt.Cursor.DEFAULT_CURSOR;
import static java.awt.Cursor.MOVE_CURSOR;
import static java.util.Arrays.asList;
import static org.apache.commons.lang.StringUtils.isEmpty;
import static ua.com.fielden.platform.swing.utils.DummyBuilder.invokeWhenGainedFocus;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JToggleButton;

import net.miginfocom.swing.MigLayout;
import ua.com.fielden.platform.basic.IValueMatcher;
import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.reflection.EntityDescriptor;
import ua.com.fielden.platform.swing.dnd.DnDSupport2;
import ua.com.fielden.platform.swing.dnd.DnDSupport2.DragFromSupport;
import ua.com.fielden.platform.swing.dnd.DnDSupport2.DragToSupport;
import ua.com.fielden.platform.swing.dnd.DndPanel.ComponentCopy;
import ua.com.fielden.platform.swing.ei.editors.IPropertyEditor;
import ua.com.fielden.platform.swing.ei.editors.RangePropertyEditor;
import ua.com.fielden.platform.swing.review.DynamicEntityQueryCriteria;
import ua.com.fielden.platform.swing.review.DynamicProperty;
import ua.com.fielden.platform.swing.review.PropertyPersistentObject;
import ua.com.fielden.platform.utils.Pair;

/**
 * Panel, which provides drag'n'drop support for label-editor pairs.
 * 
 * @author yura
 * 
 */
public class CriteriaDndPanel<T extends AbstractEntity, DAO extends IEntityDao<T>> extends JPanel {

    private static final long serialVersionUID = 586217758592148120L;

    /**
     * Mapping between list always holding three values (label, editor and property editor) and related position.
     */
    private final Map<IPropertyEditor, Position> positions = new HashMap<IPropertyEditor, Position>();

    private final Map<IPropertyEditor, CriteriaModificationLayer> layers = new HashMap<IPropertyEditor, CriteriaModificationLayer>();

    private int columns;

    private final Action changeLayoutAction;

    private final Action backToNormalAction;

    private final Action toggleAction;

    private List<CriteriaLayoutListener> layoutListeners = new ArrayList<CriteriaLayoutListener>();

    public CriteriaDndPanel(final MigLayout layout) {
	super(layout);
	DnDSupport2.installDnDSupport(this, createDragFromSupport(), createDragToSupport(), true);
	changeLayoutAction = createChangeLayoutAction();
	backToNormalAction = createBackToNormalAction();
	toggleAction = createToggleAction();
    }

    public CriteriaDndPanel(final Map<String, PropertyPersistentObject> persistentCriteriaProperties, final DynamicEntityQueryCriteria<T, DAO> dynamicCriteria, final int columns, final Map<String, IPropertyEditor> editors) {
	this(null);

	this.columns = columns;

	final int rows = getMaxRow(persistentCriteriaProperties); // should return max row index occupied by some criteria

	final BoolMatrix posMatrix = rows > -1 ? new BoolMatrix(rows + 1, getColumns()) : new BoolMatrix(getColumns());
	final Collection<PropertyPersistentObject> persistentObjects = persistentCriteriaProperties.values();
	posMatrix.fillMatrix(persistentObjects);
	posMatrix.removeEmptyRowsAndUpdate(persistentObjects);

	final List<Pair<IPropertyEditor, IPropertyEditor>> rangeAndBoolEditorPairs = getNewTwoCellEditors(persistentCriteriaProperties, true, dynamicCriteria, editors);
	rangeAndBoolEditorPairs.addAll(getNewTwoCellEditors(persistentCriteriaProperties, false, dynamicCriteria, editors));

	final List<IPropertyEditor> rangeAndBoolEditors = createOneCellRangeAndBoolEditors(rangeAndBoolEditorPairs);
	final List<CellPosition> rangeAndBoolPositions = posMatrix.findAndOccupy(1, rangeAndBoolEditors.size());

	final List<IPropertyEditor> singleEditors = getNewSingleCellEditors(persistentCriteriaProperties, dynamicCriteria, editors);
	final List<CellPosition> oneCellPositions = posMatrix.findAndOccupy(1, singleEditors.size());

	setLayout(new MigLayout("fill, insets 5", createMigColumnsString(getColumns(), maxLabelWidth(editors.values()), getMaxFromMinPossibleEditorWidth()), "[align center, :"
		+ getMaxFromMinPossibleEditorHeight() + ":]"));

	// editors adding:
	addAdjustedEditors(persistentCriteriaProperties, dynamicCriteria, editors); // adds persistent editors
	addOneCellEditors(rangeAndBoolEditors, rangeAndBoolPositions);
	addOneCellEditors(singleEditors, oneCellPositions);
	addPlaceholders(posMatrix);
    }

    /**
     * Returns the number of columns.
     * 
     * @return
     */
    public int getColumns() {
	return columns;
    }

    private static int maxLabelWidth(final Collection<IPropertyEditor> editors) {
	int maxLabelWidth = -1;
	for (final IPropertyEditor editor : editors) {
	    final boolean isNotSecondLabel = !editor.getPropertyName().endsWith(DynamicEntityQueryCriteria._TO)
		    && !editor.getPropertyName().endsWith(DynamicEntityQueryCriteria._NOT);
	    if (isNotSecondLabel && editor.getLabel().getPreferredSize().getWidth() > maxLabelWidth) {
		maxLabelWidth = (int) editor.getLabel().getPreferredSize().getWidth();
	    }
	}
	return maxLabelWidth;
    }

    /**
     * This method should return maximum value from all editors minimum widths. IPropertyEditors were analyzed and it seems that boolean double editor satisfies this condition.
     */
    private static int getMaxFromMinPossibleEditorWidth() {
	// final JPanel panel = new JPanel(new MigLayout("fill, insets 0", "[grow]5[]5[grow]", "[]"));
	final int maxCheckBoxSize = (int) new JCheckBox("yes").getMinimumSize().getWidth();
	return maxCheckBoxSize + 5 + ((int) new JLabel("to").getMinimumSize().getWidth()) + 5 + maxCheckBoxSize;
    }

    /**
     * This method should return maximum value from all editors minimum heights. IPropertyEditors were analyzed and it seems that any text editor satisfies this condition.
     */
    private static int getMaxFromMinPossibleEditorHeight() {
	return ((int) new JTextField().getMinimumSize().getHeight());
    }

    /**
     * Merges pairs of editors for range or boolean properties into single <code>IPropertyEditor</code>s for corresponding ranges/booleans.
     * 
     * @return
     */
    private List<IPropertyEditor> createOneCellRangeAndBoolEditors(final List<Pair<IPropertyEditor, IPropertyEditor>> rangeOrBoolEditors) {
	final List<IPropertyEditor> editors = new ArrayList<IPropertyEditor>();
	for (final Pair<IPropertyEditor, IPropertyEditor> doubleEditor : rangeOrBoolEditors) {
	    editors.add(createSinglePropertyEditor(doubleEditor.getKey(), doubleEditor.getValue()));
	}
	return editors;
    }

    /**
     * Merges two editors into single using one of labels and two editors separated by "TO" label.
     * 
     * @param doubleEditor
     * @return
     */
    private IPropertyEditor createSinglePropertyEditor(final IPropertyEditor leftEditor, final IPropertyEditor rightEditor) {
	return new RangePropertyEditor(leftEditor, rightEditor);
    }

    private Action createToggleAction() {
	return new AbstractAction("Design") {
	    private static final long serialVersionUID = 1L;

	    @Override
	    public void actionPerformed(final ActionEvent e) {
		final boolean selected = ((JToggleButton) e.getSource()).isSelected();
		if (selected) {
		    switchToLayoutEditingMode();
		} else {
		    switchToNormalMode();
		}
	    }
	};
    }

    private Action createBackToNormalAction() {
	final Action action = new AbstractAction("Back to normal") {
	    private static final long serialVersionUID = 1L;

	    @Override
	    public void actionPerformed(final ActionEvent e) {
		switchToNormalMode();

		getChangeLayoutAction().setEnabled(true);
		setEnabled(false);
	    }
	};
	action.setEnabled(false);
	return action;
    }

    private Action createChangeLayoutAction() {
	final Action action = new AbstractAction("Change layout") {
	    private static final long serialVersionUID = 1L;

	    @Override
	    public void actionPerformed(final ActionEvent e) {
		switchToLayoutEditingMode();

		getBackToNormalAction().setEnabled(true);
		setEnabled(false);
	    }
	};
	action.setEnabled(true);
	return action;
    }

    public Action getToggleAction() {
	return toggleAction;
    }

    /**
     * Switches panel to layout editing mode with enabled component dragging but disabled other component functionality (only concerns draggable components).
     */
    public void switchToLayoutEditingMode() {
	// creating runnable instance, which switches panel to layout editing mode
	final Runnable runnable = new Runnable() {
	    public void run() {
		for (final Entry<IPropertyEditor, Position> entry : positions.entrySet()) {
		    final CriteriaModificationLayer layer = layers.get(entry.getKey());
		    //removing label component
		    remove(entry.getKey().getLabel());
		    //creating linked copies
		    final LinkedComponentCopy<JLabel> labelCopy = new LinkedComponentCopy<JLabel>(entry.getKey(), entry.getKey().getLabel(), true);
		    LinkedComponentCopy<JComponent> editorCopy = null;
		    if (layer != null) {
			//removing editor component
			remove(layer);
			//creating editor component visual copy for criteria modification layer
			editorCopy = new LinkedComponentCopy<JComponent>(entry.getKey(), layer, false);
		    } else {
			//removing place holder
			remove(entry.getKey().getEditor());
			//creating linked copy for place holder component
			editorCopy = new LinkedComponentCopy<JComponent>(entry.getKey(), entry.getKey().getEditor(), false);
		    }
		    //linking label copy with editor copy.
		    labelCopy.linkWith(editorCopy);
		    // adding them on the same place
		    add(labelCopy, entry.getValue().getLabelConstraints());
		    add(editorCopy, entry.getValue().getEditorConstraints());
		}
		revalidate();
		invalidate();
		repaint();
		CriteriaDndPanel.this.setCursor(new Cursor(MOVE_CURSOR));
	    }
	};
	if (hasFocus()) {
	    // if this panel already has focus - switching to layout editing mode right now
	    runnable.run();
	} else if (requestFocusInWindow()) {
	    // otherwise requesting focus and if successful - waiting for focus gain and then switching
	    invokeWhenGainedFocus(this, runnable);
	}
    }

    /**
     * Switches panel to normal mode with disabled component dragging.
     */
    public void switchToNormalMode() {
	final Set<LinkedComponentCopy> alreadyUsedCopies = new HashSet<LinkedComponentCopy>();
	// creating copy of components array because we are going to add/remove components
	removeEmptyRows();
	setLayout(new MigLayout("fill, insets 5", createMigColumnsString(getColumns(), maxLabelWidth(positions.keySet()), getMaxFromMinPossibleEditorWidth()), "[align center, :"
		+ getMaxFromMinPossibleEditorHeight() + ":]"));
	for (final Component comp : asList(getComponents())) {
	    if (comp instanceof LinkedComponentCopy && !(alreadyUsedCopies.contains(comp))) {
		final LinkedComponentCopy copy = (LinkedComponentCopy) comp;
		// removing copy
		remove(copy);
		remove(copy.getLinkedCopy());
		// adding original on its place
		if (positions.containsKey(copy.getPropEditor())) {
		    add(copy.getPropEditor().getLabel(), positions.get(copy.getPropEditor()).getLabelConstraints());
		    if (layers.containsKey(copy.getPropEditor())) {
			add(layers.get(copy.getPropEditor()), positions.get(copy.getPropEditor()).getEditorConstraints());
		    } else {
			add(copy.getPropEditor().getEditor(), positions.get(copy.getPropEditor()).getEditorConstraints());
		    }
		}
		// adding to alreadyUsedCopies set in order to avoid iteration over already removed copies
		alreadyUsedCopies.add(copy);
		alreadyUsedCopies.add(copy.getLinkedCopy());
	    }
	}
	revalidate();
	invalidate();
	repaint();
	CriteriaDndPanel.this.setCursor(new Cursor(DEFAULT_CURSOR));
    }

    private void removeEmptyRows() {
	final int rowNumber = getRowNumber();
	final List<Boolean> emptyRows = new ArrayList<Boolean>(rowNumber);
	final List<IPropertyEditor> editorsToIncrement = new ArrayList<IPropertyEditor>();
	final List<IPropertyEditor> editorsToRemove = new ArrayList<IPropertyEditor>();
	for (int rowIndex = 0; rowIndex < rowNumber; rowIndex++) {
	    emptyRows.add(Boolean.TRUE);
	}
	for (final Map.Entry<IPropertyEditor, Position> entry : positions.entrySet()) {
	    emptyRows.set(entry.getValue().getRow(), emptyRows.get(entry.getValue().getRow()) & !layers.containsKey(entry.getKey()));
	}
	for (int rowIndex = 0; rowIndex < rowNumber; rowIndex++) {
	    if (emptyRows.get(rowIndex)) {
		for (final Map.Entry<IPropertyEditor, Position> entry : positions.entrySet()) {
		    if (rowIndex == entry.getValue().getRow()) {
			editorsToRemove.add(entry.getKey());
		    } else if (rowIndex < entry.getValue().getRow() && !emptyRows.get(entry.getValue().getRow())) {
			editorsToIncrement.add(entry.getKey());
		    }
		}
	    }
	}
	for (final IPropertyEditor editor : editorsToRemove) {
	    positions.remove(editor);
	    layers.remove(editor);
	}
	for (final IPropertyEditor editor : editorsToIncrement) {
	    final Position oldPos = positions.get(editor);
	    final Position newPos = new Position(oldPos.getColumn(), oldPos.getRow() - 1, oldPos.getLabelConstr(), oldPos.getEditorConstr());
	    positions.put(editor, newPos);
	}
    }

    private int getRowNumber() {
	int rowIndex = -1;
	for (final Position position : positions.values()) {
	    if (position.getRow() > rowIndex) {
		rowIndex = position.getRow();
	    }
	}
	return rowIndex + 1;
    }

    private DragToSupport createDragToSupport() {
	return new DragToSupport() {

	    private LinkedComponentCopy dropToComponent;

	    @Override
	    public boolean canDropTo(final Point point, final Object what, final JComponent draggedFrom) {
		// removing highlighting from previously hovered component
		if (dropToComponent != null) {
		    dropToComponent.setDrawBorder(false);
		    dropToComponent.getLinkedCopy().setDrawBorder(false);
		}

		final Component dropToComponentComp = getComponentAt(point);

		// we can only drag LinkedComponentCopy instances
		if (dropToComponentComp instanceof LinkedComponentCopy) {
		    dropToComponent = (LinkedComponentCopy) dropToComponentComp;
		    // highlighting hovered component
		    final boolean draggingToItself = what == dropToComponent || what == dropToComponent.getLinkedCopy();
		    if (!draggingToItself) {
			dropToComponent.setDrawBorder(true);
			dropToComponent.getLinkedCopy().setDrawBorder(false);
		    }
		    // no need to drop component to itself
		    return !draggingToItself;
		} else {
		    return false;
		}
	    }

	    @Override
	    public boolean dropTo(final Point point, final Object what, final JComponent draggedFrom) {
		// can only drop ComponentCopy instances
		if (what != null && what instanceof LinkedComponentCopy) {
		    final Component whereToDrop = getComponentAt(point);

		    // can only drop component to another instance of ComponentCopy
		    if (whereToDrop instanceof LinkedComponentCopy && what != whereToDrop && what != ((LinkedComponentCopy) whereToDrop).getLinkedCopy()) {
			// if dropping, removing highlighting from hovered component
			dropToComponent.setDrawBorder(false);
			dropToComponent.getLinkedCopy().setDrawBorder(false);
			// swapping component positions
			swapCopies((LinkedComponentCopy) whereToDrop, (LinkedComponentCopy) what);

			for (final CriteriaLayoutListener listener : layoutListeners) {
			    listener.layoutChanged();
			}

			return true;
		    } else {
			return false;
		    }
		} else {
		    return false;
		}
	    }
	};
    }

    /**
     * @see CriteriaLayoutListener
     * @param listener
     */
    public void addLayoutListener(final CriteriaLayoutListener listener) {
	layoutListeners.add(listener);
    }

    /**
     * @see CriteriaLayoutListener
     * @param listener
     */
    public List<CriteriaLayoutListener> getLayoutListeners() {
	return Collections.unmodifiableList(layoutListeners);
    }

    /**
     * @see CriteriaLayoutListener
     * @param listener
     */
    public boolean removeLayoutListener(final CriteriaLayoutListener listener) {
	return layoutListeners.remove(listener);
    }

    private DragFromSupport createDragFromSupport() {
	return new DragFromSupport() {
	    @Override
	    public void dragNDropDone(final Object object, final JComponent dropTo, final int action) {
	    }

	    @Override
	    public Object getObject4DragAt(final Point point) {
		final Component comp = getComponentAt(point);
		// can only drag instances of ComponentCopy
		return comp instanceof LinkedComponentCopy ? comp : null;
	    }
	};
    }

    /**
     * Adds draggable label and editor at the specified position.
     * 
     * @param label
     * @param editor
     * @param position
     */
    private void addDraggable(final IPropertyEditor propertyEditor, final int column, final int row) {
	final Position position = new Position(column, row, "", "grow"); // grow

	final Pair<JLabel, CriteriaModificationLayer> pair = new Pair<JLabel, CriteriaModificationLayer>(propertyEditor.getLabel(), new CriteriaModificationLayer(propertyEditor));
	add(pair.getKey(), position.getLabelConstraints());
	add(pair.getValue(), position.getEditorConstraints());

	layers.put(propertyEditor, pair.getValue());
	positions.put(propertyEditor, position);
    }

    /**
     * Adds placeholder at the specified position.
     * 
     * @param column
     * @param row
     */
    private void addPlaceHolder(final int column, final int row) {
	final Position position = new Position(column, row, "", "grow"); // grow

	final Pair<JLabel, JComponent> placeholder = new Pair<JLabel, JComponent>(new JLabel(), new JPanel());
	add(placeholder.getKey(), position.getLabelConstraints());
	add(placeholder.getValue(), position.getEditorConstraints());

	// creating some dummy unique property name
	final String propertyName = String.valueOf(column) + String.valueOf(row);
	positions.put(new PlaceHolderPropertyEditor(placeholder.getKey(), placeholder.getValue(), propertyName), position);
    }

    /**
     * Returns position of editor with the specified propertyName. Key represents column, value - row.
     * 
     * @param propertyName
     * @return
     */
    public Pair<Integer, Integer> getPositionOf(final String propertyName) {
	// look for position for "_FROM"/"_TO" and single editors (conventional propertyName does not contain "from/to" suffixes):
	final String conventionalPropertyName = EntityDescriptor.removeSuffixes(propertyName);
	for (final Entry<IPropertyEditor, Position> entry : positions.entrySet()) {
	    if (conventionalPropertyName.equals(entry.getKey().getPropertyName())) {
		return new Pair<Integer, Integer>(entry.getValue().getColumn(), entry.getValue().getRow());
	    }
	}
	return null;
    }

    /**
     * Swaps positions of the specified component copies.
     * 
     * @param compCopy1
     * @param compCopy2
     */
    @SuppressWarnings("unchecked")
    private void swapCopies(final LinkedComponentCopy compCopy1, final LinkedComponentCopy compCopy2) {
	final Pair<LinkedComponentCopy<JLabel>, LinkedComponentCopy<JComponent>> copyPair1 = compCopy1.getCopyPair();
	final Pair<LinkedComponentCopy<JLabel>, LinkedComponentCopy<JComponent>> copyPair2 = compCopy2.getCopyPair();
	// removing label/editor copies
	remove(copyPair1.getKey());
	remove(copyPair1.getValue());
	remove(copyPair2.getKey());
	remove(copyPair2.getValue());

	// determining label/editor positions
	final Position pos1 = positions.get(compCopy1.getPropEditor());
	final Position pos2 = positions.get(compCopy2.getPropEditor());

	// adding them with swapped constraints
	add(copyPair1.getKey(), pos2.getLabelConstraints());
	add(copyPair1.getValue(), pos2.getEditorConstraints());
	positions.put(compCopy1.getPropEditor(), pos2);
	add(copyPair2.getKey(), pos1.getLabelConstraints());
	add(copyPair2.getValue(), pos1.getEditorConstraints());
	positions.put(compCopy2.getPropEditor(), pos1);

	// re-validating the container in order changes to take place
	revalidate();
    }

    /**
     * Class, representing position of a cell as row and column.
     * 
     * @author yura
     * 
     */
    public static class CellPosition {
	private final int column;

	private final int row;

	public CellPosition(final int column, final int row) {
	    this.column = column;
	    this.row = row;
	}

	public int getColumn() {
	    return column;
	}

	public int getRow() {
	    return row;
	}
    }

    /**
     * Class encapsulating positions of label and editor on the {@link CriteriaDndPanel}.
     * 
     * @author yura
     * 
     */
    private static class Position extends CellPosition {
	private final String labelConstr;

	private final String editorConstr;

	/**
	 * Creates {@link Position} instance with specified column and row of label component (editor component should be placed at cell with coordinates (column + 1;row)). Also
	 * additional label and editor layout constraints may be specified.
	 * 
	 * @param column
	 * @param row
	 * @param labelConstr
	 * @param editorConstr
	 */
	public Position(final int column, final int row, final String labelConstr, final String editorConstr) {
	    super(column, row);
	    this.labelConstr = labelConstr;
	    this.editorConstr = editorConstr;
	}

	public String getLabelConstraints() {
	    return "cell " + getColumn() + " " + getRow() + (isEmpty(labelConstr) ? "" : (", " + labelConstr));
	}

	public String getEditorConstraints() {
	    return "cell " + (getColumn() + 1) + " " + getRow() + (isEmpty(editorConstr) ? "" : (", " + editorConstr));
	}

	public String getLabelConstr() {
	    return labelConstr;
	}

	public String getEditorConstr() {
	    return editorConstr;
	}
    }

    /**
     * Represents visual copy of {@link JComponent}, which contains link to another {@link LinkedComponentCopy} instance. This was done in order to keep references between label
     * and editor copies.
     * 
     * @author yura
     * 
     * @param <T>
     */
    @SuppressWarnings("unchecked")
    public static class LinkedComponentCopy<T extends JComponent> extends ComponentCopy {

	private static final long serialVersionUID = 7138356064320181273L;

	private LinkedComponentCopy linkedCopy;

	private final IPropertyEditor propEditor;

	private final boolean isLabel;

	/**
	 * In case of {@link JLabel}, sets minimum size of this component to size of original component.
	 * 
	 * @param original
	 */
	public LinkedComponentCopy(final IPropertyEditor propEditor, final JComponent component, final boolean isLabel) {
	    super(isLabel ? component : createEditor(component));
	    this.propEditor = propEditor;
	    this.isLabel = isLabel;
	}

	/**
	 * Sets cross-references between this instance and the specified one.
	 * 
	 * @param compCopy
	 */
	public void linkWith(final LinkedComponentCopy compCopy) {
	    linkedCopy = compCopy;
	    compCopy.linkedCopy = this;
	}

	public JComponent getOriginalComponent() {
	    return getOriginal(); // isLabel ? propEditor.getLabel() : propEditor.getEditor();
	}

	/**
	 * @return pair of {@link LinkedComponentCopy}s, where key corresponds to label copy and value corresponds to editor copy.
	 */
	public Pair<LinkedComponentCopy<JLabel>, LinkedComponentCopy<JComponent>> getCopyPair() {
	    if (getOriginalComponent() instanceof JLabel) {
		return new Pair<LinkedComponentCopy<JLabel>, LinkedComponentCopy<JComponent>>((LinkedComponentCopy<JLabel>) this, linkedCopy);
	    } else {
		return new Pair<LinkedComponentCopy<JLabel>, LinkedComponentCopy<JComponent>>(linkedCopy, (LinkedComponentCopy<JComponent>) this);
	    }
	}

	/**
	 * Used for correct painting of component copy. (without black bounds)
	 * 
	 * 
	 * @param propertyEditor
	 * @return
	 */
	private static JComponent createEditor(final JComponent editor) {
	    final JPanel editorPanel = new JPanel(new MigLayout("fill, insets 0"));
	    editorPanel.add(editor, "grow");
	    return editorPanel;
	}

	public LinkedComponentCopy getLinkedCopy() {
	    return linkedCopy;
	}

	public IPropertyEditor getPropEditor() {
	    return propEditor;
	}

	public boolean isLabel() {
	    return isLabel;
	}
    }

    public Action getChangeLayoutAction() {
	return changeLayoutAction;
    }

    public Action getBackToNormalAction() {
	return backToNormalAction;
    }

    /**
     * Listener which is notified whenever layout is changed (criteria order has been changed via drag-n-drop) on the {@link CriteriaDndPanel}, to which this listener is attached.
     * 
     * @author yura
     * 
     */
    public static interface CriteriaLayoutListener {
	void layoutChanged();
    }

    // Methods ported from DynamicEntityReview (to make CriteriaDndPanel more independent from dynamic criteria)
    /**
     * Returns max row which is met among editors.
     * 
     * @param criteria
     * @return
     */
    private static int getMaxRow(final Map<String, PropertyPersistentObject> criteria) {
	int maxRow = -1;
	for (final Entry<String, PropertyPersistentObject> entry : criteria.entrySet()) {
	    if (entry.getValue().positionIsInitialised()) {
		maxRow = entry.getValue().getRow() > maxRow ? entry.getValue().getRow() : maxRow;
	    }
	}
	return maxRow;
    }

    /**
     * Represents boolean matrix, which is used to represent positions on the {@link CriteriaDndPanel}. If cell at some row and column holds value of {@link Boolean#TRUE} it means
     * this cell is occupied.
     * 
     * @author yura
     * 
     */
    private static class BoolMatrix {

	private final List<List<Boolean>> matrix = new ArrayList<List<Boolean>>();

	private final int columns;

	/**
	 * Creates matrix with the specified number of columns and no rows.
	 * 
	 * @param columns
	 */
	public BoolMatrix(final int columns) {
	    this.columns = columns;
	}

	/**
	 * Creates matrix with the specified number of rows and columns filled with {@link Boolean#FALSE} values.
	 * 
	 * @param rows
	 * @param columns
	 */
	public BoolMatrix(final int rows, final int columns) {
	    this.columns = columns;
	    for (int i = 0; i < rows; i++) {
		final List<Boolean> row = new ArrayList<Boolean>();
		matrix.add(row);
		for (int j = 0; j < columns; j++) {
		    row.add(false);
		}
	    }
	}

	public int rows() {
	    return matrix.size();
	}

	public int columns() {
	    return columns;
	}

	/**
	 * Iterates over {@link PropertyPersistentObject}s and sets values of {@link Boolean#TRUE} for the occupied cells.
	 * 
	 * @param propertyPersistentObjects
	 */
	public void fillMatrix(final Collection<PropertyPersistentObject> propertyPersistentObjects) {
	    if (matrix.isEmpty()) {
		return;
	    }
	    for (final PropertyPersistentObject ppo : propertyPersistentObjects) {
		if (ppo.positionIsInitialised()) {
		    // PPO holds "real" columns (i.e. columns which may be occupied by label or editor), while this matrix holds columns, which are occupied both by label and
		    // editor
		    // that is why we should divide column by 2
		    matrix.get(ppo.getRow()).set(ppo.getColumn() / 2, true);
		}
	    }
	}

	/**
	 * Searches for a cells to place editorsCount number of editors, each of which has length of editorSize columns (this value usually is 1 or 2). It is assumed that there are
	 * sufficient space in matrix to put these editors. When place is found, it is marked as occupied.
	 * 
	 * @param startFromRow
	 * @param editorSize
	 * @param editorsCount
	 * @return
	 */
	private List<CellPosition> findAndOccupyFrom(final int startFromRow, final int editorSize, final int editorsCount) {
	    final List<CellPosition> cellPositions = new ArrayList<CellPosition>();
	    // different cases, when there are one column and several
	    if (columns > 1) {
		for (int i = startFromRow; i < matrix.size(); i++) {
		    for (int j = 0; j < columns - editorSize + 1; j++) {
			if (isCellsFree(i, j, editorSize)) {
			    cellPositions.add(new CellPosition(j, i));
			    markCells(i, j, editorSize, true);
			    if (cellPositions.size() == editorsCount) {
				return cellPositions;
			    }
			}
		    }
		}
	    } else {
		for (int i = startFromRow; i < matrix.size() - editorSize + 1; i++) {
		    if (isCellsFree(i, editorSize)) {
			cellPositions.add(new CellPosition(0, i));
			markCells(i, editorSize, true);
			if (cellPositions.size() == editorsCount) {
			    return cellPositions;
			}
		    }
		}
	    }
	    return cellPositions;
	}

	/**
	 * Searches for a cells to place editorsCount number of editors, each of which has length of editorSize columns (this value usually is 1 or 2). If there are insufficient
	 * space in the matrix, this method may add new rows.
	 * 
	 * @param editorSize
	 * @param editorsCount
	 * @return
	 */
	public List<CellPosition> findAndOccupy(final int editorSize, final int editorsCount) {
	    if (editorsCount == 0) {
		return new ArrayList<CellPosition>();
	    }
	    final List<CellPosition> cellPositions = findAndOccupyFrom(0, editorSize, editorsCount);
	    if (cellPositions.size() < editorsCount) {
		cellPositions.addAll(addAndOccupyFromNewRow(editorSize, editorsCount - cellPositions.size()));
	    }

	    return cellPositions;
	}

	/**
	 * This method adds new rows in order to place editorsCount number of editors, each of which occupies editorSize number of columns.
	 * 
	 * @param editorSize
	 * @param editorsCount
	 * @return
	 */
	public List<CellPosition> addAndOccupyFromNewRow(final int editorSize, final int editorsCount) {
	    if (editorsCount == 0) {
		return new ArrayList<CellPosition>();
	    }
	    final int editorsPerRow = (int) Math.floor(new Double(columns) / editorSize);
	    final int rowsNeeded = columns > 1 ? (int) Math.ceil(new Double(editorsCount) / editorsPerRow) : editorsCount * editorSize;
	    final int lastRowIndex = rows();
	    addRows(rowsNeeded);
	    return findAndOccupyFrom(lastRowIndex, editorSize, editorsCount);
	}

	/**
	 * Adds specified number of rows filled with {@link Boolean#FALSE} value.
	 * 
	 * @param rowsToAdd
	 */
	private void addRows(final int rowsToAdd) {
	    final int lastRowIndex = rows();
	    for (int i = lastRowIndex; i < lastRowIndex + rowsToAdd; i++) {
		final List<Boolean> row = new ArrayList<Boolean>();
		matrix.add(row);
		for (int j = 0; j < columns(); j++) {
		    row.add(false);
		}
	    }
	}

	/**
	 * Mark cells column-by-column in the same row with the specified value (occupied) starting from column till column + editorSize.
	 * 
	 * @param row
	 * @param column
	 * @param editorSize
	 * @param occupied
	 */
	private void markCells(final int row, final int column, final int editorSize, final boolean occupied) {
	    for (int i = column; i < column + editorSize; i++) {
		matrix.get(row).set(i, occupied);
	    }
	}

	/**
	 * Marks cells row-by-row in the 0 column with the specified value (occupied) starting from row till row + editorSize.
	 * 
	 * @param row
	 * @param editorSize
	 * @param occupied
	 */
	private void markCells(final int row, final int editorSize, final boolean occupied) {
	    for (int i = row; i < row + editorSize; i++) {
		matrix.get(i).set(0, occupied);
	    }
	}

	/**
	 * Returns true if in the specified row all cells between column inclusively and column + editorSize exclusively are free.
	 * 
	 * @param row
	 * @param column
	 * @param editorSize
	 * @return
	 */
	private boolean isCellsFree(final int row, final int column, final int editorSize) {
	    if (column + editorSize - 1 > columns()) {
		return false;
	    }
	    boolean free = true;
	    for (int j = column; j < column + editorSize; j++) {
		free &= !matrix.get(row).get(j);
	    }
	    return free;
	}

	/**
	 * Returns true if in the 0 column all cells between row inclusively and column + editorSize exclusively are free.
	 * 
	 * @param row
	 * @param editorSize
	 * @return
	 */
	private boolean isCellsFree(final int row, final int editorSize) {
	    if (row + editorSize - 1 > rows()) {
		return false;
	    }
	    boolean free = true;
	    for (int i = row; i < row + editorSize; i++) {
		free &= !matrix.get(i).get(0);
	    }
	    return free;
	}

	/**
	 * Removes empty rows and updates (shifts up) already aligned editors.
	 * 
	 * @param persistentObjects
	 */
	public void removeEmptyRowsAndUpdate(final Collection<PropertyPersistentObject> persistentObjects) {
	    final List<List<Boolean>> emptyRows = new ArrayList<List<Boolean>>();
	    final List<PropertyPersistentObject> editorsToShift = new ArrayList<PropertyPersistentObject>();
	    for (int i = 0; i < rows(); i++) {
		boolean isEmpty = true;
		for (int j = 0; j < columns; j++) {
		    isEmpty &= !matrix.get(i).get(j);
		}
		if (isEmpty) {
		    emptyRows.add(matrix.get(i));
		    // shifting up each editor which is above the row to be removed
		    for (final PropertyPersistentObject persistentObject : persistentObjects) {
			if (persistentObject.positionIsInitialised() && persistentObject.getRow() > i) {
			    editorsToShift.add(persistentObject);
			}
		    }
		}
	    }

	    for (final PropertyPersistentObject persistentObject : editorsToShift) {
		persistentObject.setPosition(new Pair<Integer, Integer>(persistentObject.getColumn(), persistentObject.getRow() - 1));
	    }

	    for (final List<Boolean> row : emptyRows) {
		matrix.remove(row);
	    }
	}
    }

    // adding editors:
    /**
     * Fills unoccupied cells with place-holders.
     * 
     * @param panel
     * @param posMatrix
     */
    private void addPlaceholders(final BoolMatrix posMatrix) {
	for (int i = 0; i < posMatrix.rows(); i++) {
	    for (int j = 0; j < posMatrix.columns(); j++) {
		if (posMatrix.isCellsFree(i, j, 1)) {
		    addPlaceHolder(j * 2, i);
		    posMatrix.markCells(i, j, 1, true);
		}
	    }
	}
    }

    /**
     * Adds one cell editors.
     * 
     * @param panel
     * @param collectionalEditors
     * @param oneCellPositions
     */
    private void addOneCellEditors(final List<IPropertyEditor> collectionalEditors, final List<CellPosition> oneCellPositions) {
	for (final IPropertyEditor propEditor : collectionalEditors) {
	    final CellPosition cellPosition = oneCellPositions.remove(0);

	    addDraggable(propEditor, cellPosition.getColumn() * 2, cellPosition.getRow()); // false,
	}
    }

    /**
     * Adds editors, which positions were already adjusted.
     * 
     * @param panel
     * @param criteria
     */
    private void addAdjustedEditors(final Map<String, PropertyPersistentObject> criteria, final DynamicEntityQueryCriteria<T, DAO> dynamicCriteria, final Map<String, IPropertyEditor> editors) {
	for (final Entry<String, PropertyPersistentObject> entry : criteria.entrySet()) {
	    final PropertyPersistentObject ppo = entry.getValue();

	    final boolean isRangeFirstSubeditor = ppo.getPropertyName().endsWith(DynamicEntityQueryCriteria._FROM)
		    || ppo.getPropertyName().endsWith(DynamicEntityQueryCriteria._IS);
	    final boolean isRangeSecondSubeditor = ppo.getPropertyName().endsWith(DynamicEntityQueryCriteria._TO)
		    || ppo.getPropertyName().endsWith(DynamicEntityQueryCriteria._NOT);
	    if (!ppo.positionIsInitialised() || isRangeSecondSubeditor) {
		continue;
	    }
	    final IPropertyEditor editor;
	    if (isRangeFirstSubeditor) {
		final String correspondingRangePropertyName = ppo.getPropertyName().endsWith(DynamicEntityQueryCriteria._FROM) //
			? (EntityDescriptor.replaceLast(ppo.getPropertyName(), DynamicEntityQueryCriteria._FROM, "") + DynamicEntityQueryCriteria._TO)
				: (EntityDescriptor.replaceLast(ppo.getPropertyName(), DynamicEntityQueryCriteria._IS, "") + DynamicEntityQueryCriteria._NOT);
			editor = createSinglePropertyEditor(editors.get(ppo.getPropertyName()), editors.get(correspondingRangePropertyName));
	    } else {
		editor = editors.get(ppo.getPropertyName());
	    }

	    addDraggable(editor, ppo.getColumn(), ppo.getRow());
	}
    }

    // //////////////////////////////////////////////
    /**
     * Returns list of collectional editors, which positions were not yet specified.
     * 
     * @param criteria
     * @return
     */
    private List<IPropertyEditor> getNewSingleCellEditors(final Map<String, PropertyPersistentObject> criteria, final DynamicEntityQueryCriteria<T, DAO> dynamicCriteria, final Map<String, IPropertyEditor> editors) {
	final List<IPropertyEditor> singleEditors = new ArrayList<IPropertyEditor>();
	for (final Entry<String, PropertyPersistentObject> entry : criteria.entrySet()) {
	    if (entry.getValue().positionIsInitialised()) {
		continue;
	    }
	    final DynamicProperty dynamicProperty = dynamicCriteria.getEditableProperty(entry.getKey());
	    if (dynamicProperty.isEntityProperty() || dynamicProperty.isStringProperty()) {
		singleEditors.add(editors.get(entry.getKey()));
	    }
	}
	return singleEditors;
    }

    /**
     * Returns list of either range editors (if range parameter is true) or boolean editors (otherwise), which are new (i.e. they were just added in the property tree and their
     * positions are not yet initialised).
     * 
     * @param criteria
     * @param range
     * @return
     */
    private List<Pair<IPropertyEditor, IPropertyEditor>> getNewTwoCellEditors(final Map<String, PropertyPersistentObject> criteria, final boolean range, final DynamicEntityQueryCriteria<T, DAO> dynamicCriteria, final Map<String, IPropertyEditor> editors) {
	final String firstEditorKeyEnding = range ? DynamicEntityQueryCriteria._FROM : DynamicEntityQueryCriteria._IS;
	final String secondEditorKeyEnding = range ? DynamicEntityQueryCriteria._TO : DynamicEntityQueryCriteria._NOT;
	final List<Pair<IPropertyEditor, IPropertyEditor>> rangeEditors = new ArrayList<Pair<IPropertyEditor, IPropertyEditor>>();
	for (final Entry<String, PropertyPersistentObject> entry : criteria.entrySet()) {
	    if (entry.getValue().positionIsInitialised()) {
		// if position of editor is initialised, then it is not new
		continue;
	    }
	    final DynamicProperty dynamicProperty = dynamicCriteria.getEditableProperty(entry.getKey());
	    final boolean propSatisfies = range ? dynamicProperty.isRangeProperty() : dynamicProperty.isBoolProperty();
	    if (propSatisfies) {
		if (entry.getKey().endsWith(secondEditorKeyEnding)) {
		    continue;
		}
		final IPropertyEditor toEditor = editors.get(entry.getKey().substring(0, entry.getKey().lastIndexOf(firstEditorKeyEnding)) + secondEditorKeyEnding);
		rangeEditors.add(new Pair<IPropertyEditor, IPropertyEditor>(editors.get(entry.getKey()), toEditor));
	    }
	}
	return rangeEditors;
    }

    /**
     * Creates string for mig layout of columns.
     * 
     * @param columns
     * @return
     */
    private static String createMigColumnsString(final int columns, final int maxLabelWidth, final int maxFromMinPossibleEditorWidth) {
	String migColumnsStr = "";
	for (int i = 1; i <= columns; i++) {
	    migColumnsStr += (i == 1 ? "" : "20") + "[align left, :" + maxLabelWidth + ":][grow,:" + maxFromMinPossibleEditorWidth + ":]"; //
	}
	return migColumnsStr;
    }

    private static class PlaceHolderPropertyEditor implements IPropertyEditor {

	private final JComponent editor;

	private final JLabel label;

	private final String propertyName;

	public PlaceHolderPropertyEditor(final JLabel label, final JComponent editor, final String propertyName) {
	    this.editor = editor;
	    this.label = label;
	    this.propertyName = propertyName;
	}

	@Override
	public void bind(final AbstractEntity<?> entity) {
	    throw new UnsupportedOperationException();
	}

	@Override
	public JPanel getDefaultLayout() {
	    throw new UnsupportedOperationException();
	}

	@Override
	public JComponent getEditor() {
	    return editor;
	}

	@Override
	public AbstractEntity<?> getEntity() {
	    throw new UnsupportedOperationException();
	}

	@Override
	public JLabel getLabel() {
	    return label;
	}

	@Override
	public String getPropertyName() {
	    return propertyName;
	}

	@Override
	public IValueMatcher<?> getValueMatcher() {
	    throw new UnsupportedOperationException();
	}

	@Override
	public boolean isIgnored() {
	    return true;
	}

    }

}
