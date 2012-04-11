package ua.com.fielden.platform.swing.review.report.centre;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EventListener;
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
import javax.swing.event.EventListenerList;

import net.miginfocom.swing.MigLayout;
import ua.com.fielden.platform.basic.IValueMatcher;
import ua.com.fielden.platform.criteria.generator.impl.CriteriaReflector;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.IAddToCriteriaTickManager;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.swing.components.bind.development.BoundedValidationLayer;
import ua.com.fielden.platform.swing.dnd.DnDSupport2;
import ua.com.fielden.platform.swing.dnd.DnDSupport2.DragFromSupport;
import ua.com.fielden.platform.swing.dnd.DnDSupport2.DragToSupport;
import ua.com.fielden.platform.swing.dnd.DndPanel.ComponentCopy;
import ua.com.fielden.platform.swing.ei.editors.development.IPropertyEditor;
import ua.com.fielden.platform.swing.ei.editors.development.RangePropertyEditor;
import ua.com.fielden.platform.swing.review.development.EntityQueryCriteria;
import ua.com.fielden.platform.utils.Pair;
import static java.awt.Cursor.DEFAULT_CURSOR;
import static java.awt.Cursor.MOVE_CURSOR;
import static java.util.Arrays.asList;
import static org.apache.commons.lang.StringUtils.isEmpty;
import static ua.com.fielden.platform.swing.utils.DummyBuilder.invokeWhenGainedFocus;

/**
 * Panel, which provides drag'n'drop support for label-editor pairs.
 *
 * @author TG Team
 *
 */
public class CriteriaDndPanel extends StubCriteriaPanel {

    private static final long serialVersionUID = 586217758592148120L;

    /**
     * {@link EntityQueryCriteria} for which this {@link CriteriaDndPanel} is created.
     */
    private final EntityQueryCriteria<ICentreDomainTreeManagerAndEnhancer, ?, ?> eqc;

    /**
     * Contains editors those should be placed on this panel.
     */
    private final Map<String, IPropertyEditor> editors = new HashMap<String, IPropertyEditor>();

    /**
     * Identifies the number of columns filled with editors.
     */
    private final int columns;

    /**
     * Identifies the number of rows filled with editors.
     */
    private int rows;

    /**
     * Contains the actions those allows one to switch between design and review modes of selection criteria panel.
     */
    private final Action changeLayoutAction, backToNormalAction, toggleAction;

    /**
     * Contains the list of {@link CriteriaLayoutListener}s.
     */
    private final EventListenerList listeners = new EventListenerList();

    /**
     * Mapping between list always holding three values (label, editor and property editor) and related position.
     */
    private final Map<IPropertyEditor, Position> positions = new HashMap<IPropertyEditor, Position>();

    private final Map<IPropertyEditor, CriteriaModificationLayer> layers = new HashMap<IPropertyEditor, CriteriaModificationLayer>();

    public CriteriaDndPanel(final EntityQueryCriteria<ICentreDomainTreeManagerAndEnhancer, ?, ?> eqc, final Map<String, IPropertyEditor> editors, final MigLayout layout) {
	super(layout);
	this.eqc = eqc;
	this.editors.clear();
	if(editors != null){
	    this.editors.putAll(editors);
	}
	final IAddToCriteriaTickManager firstTick = eqc.getCentreDomainTreeMangerAndEnhancer().getFirstTick();
	final int elementsNumber = firstTick.checkedProperties(eqc.getEntityClass()).size();
	this.columns = firstTick.getColumnsNumber();
	if(elementsNumber % columns != 0){
	    throw new IllegalStateException("The number of checked elements is not correct! elements: " + elementsNumber + ", columns: " + columns);
	}
	this.rows = elementsNumber / columns;
	DnDSupport2.installDnDSupport(this, createDragFromSupport(), createDragToSupport(), true);
	changeLayoutAction = createChangeLayoutAction();
	backToNormalAction = createBackToNormalAction();
	toggleAction = createToggleAction();
    }

    public CriteriaDndPanel(final EntityQueryCriteria<ICentreDomainTreeManagerAndEnhancer, ?, ?> eqc, final Map<String, IPropertyEditor> editors) {
	this(eqc, editors, (MigLayout)null);

	final int maxRow = getRows() - 1; // should return max row index occupied by some criteria

	final BoolMatrix posMatrix = maxRow > -1 ? new BoolMatrix(maxRow + 1, getColumns()) : new BoolMatrix(getColumns());
	final List<String> checkedElements = eqc.getCentreDomainTreeMangerAndEnhancer().getFirstTick().checkedProperties(eqc.getEntityClass());
	posMatrix.fillMatrix(checkedElements);

	setLayout(new MigLayout("fill, insets 5", createMigColumnsString(getColumns(), maxLabelWidth(editors.values()), getMaxFromMinPossibleEditorWidth()), "[align center, :"
		+ getMaxFromMinPossibleEditorHeight() + ":]"));

	// editors adding:
	layoutEditors(); // adds persistent editors
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

    /**
     * Returns the number of rows.
     *
     * @return
     */
    public int getRows() {
	return rows;
    }

    /**
     * Returns the maximum label width among specified collection of property editors.
     *
     * @param editors
     * @return
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    private static int maxLabelWidth(final Collection<IPropertyEditor> editors) {
	int maxLabelWidth = -1;
	for (final IPropertyEditor editor : editors) {
	    final boolean isNotSecondLabel = editor instanceof RangePropertyEditor ? true : !CriteriaReflector.isSecondParam(editor.getEntity().getClass(), editor.getPropertyName());
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

    @Override
    public Action getSwitchAction() {
	return toggleAction;
    }
    
    @SuppressWarnings("rawtypes")
    @Override
    public void updateModel() {
	for (final IPropertyEditor component : editors.values()) {
	    if (component.getEditor() instanceof BoundedValidationLayer) {
		final BoundedValidationLayer bvl = (BoundedValidationLayer) component.getEditor();
		if (bvl.canCommit()) {
		    bvl.commit();
		}
	    }
	}
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
		    final LinkedComponentCopy labelCopy = new LinkedComponentCopy(entry.getKey(), entry.getKey().getLabel(), true);
		    LinkedComponentCopy editorCopy = null;
		    if (layer != null) {
			//removing editor component
			remove(layer);
			//creating editor component visual copy for criteria modification layer
			editorCopy = new LinkedComponentCopy(entry.getKey(), layer, false);
		    } else {
			//removing place holder
			remove(entry.getKey().getEditor());
			//creating linked copy for place holder component
			editorCopy = new LinkedComponentCopy(entry.getKey(), entry.getKey().getEditor(), false);
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

    //TODO This might be romoved later after the ICentreDomainTreeManager will be enhanced with listeners those listen the checked property layout changes.
    private void removeEmptyRows() {
	final int rowNumber = getRows();
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

			for (final CriteriaLayoutListener listener : listeners.getListeners(CriteriaLayoutListener.class)) {
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
     * Registers the listener that listens the "layout change" events.
     *
     * @see CriteriaLayoutListener
     * @param listener
     */
    public void addLayoutListener(final CriteriaLayoutListener listener) {
	listeners.add(CriteriaLayoutListener.class, listener);
    }

    /**
     * Removes the specified {@link CriteriaLayoutListener} from the list of registered listeners.
     *
     * @see CriteriaLayoutListener
     * @param listener
     */
    public void removeLayoutListener(final CriteriaLayoutListener listener) {
	listeners.remove(CriteriaLayoutListener.class, listener);
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
     * Swaps positions of the specified component copies.
     *
     * @param compCopy1
     * @param compCopy2
     */
    //TODO MUST CONCIDER WHEHTER THIS METHOD SHOULD BE REMOVED AFTER LISTENER WILL BE ADDED TO FIRST TICK THAT LISTENS THE PROPERTY SWAP ACTION.
    private void swapCopies(final LinkedComponentCopy compCopy1, final LinkedComponentCopy compCopy2) {
	final Pair<LinkedComponentCopy, LinkedComponentCopy> copyPair1 = compCopy1.getCopyPair();
	final Pair<LinkedComponentCopy, LinkedComponentCopy> copyPair2 = compCopy2.getCopyPair();
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

	swapProperties(compCopy1, compCopy2);

	// re-validating the container in order changes to take place
	revalidate();
    }

    @SuppressWarnings("rawtypes")
    private void swapProperties(final LinkedComponentCopy compCopy1, final LinkedComponentCopy compCopy2) {
	final IAddToCriteriaTickManager firstTick = eqc.getCentreDomainTreeMangerAndEnhancer().getFirstTick();
	final String propName1 = compCopy1.getPropEditor() instanceof RangePropertyEditor ? ((RangePropertyEditor)compCopy1.getPropEditor()).getFromEditor().getPropertyName() : compCopy1.getPropEditor().getPropertyName();
	final String propName2 = compCopy2.getPropEditor() instanceof RangePropertyEditor ? ((RangePropertyEditor)compCopy2.getPropEditor()).getFromEditor().getPropertyName() : compCopy2.getPropEditor().getPropertyName();
	final Class<? extends EntityQueryCriteria> entityType = eqc.getClass();
	final String firstProperty = CriteriaReflector.getCriteriaProperty(entityType, propName1);
	final String secondProperty = CriteriaReflector.getCriteriaProperty(entityType, propName2);
	firstTick.swap(eqc.getEntityClass(), firstProperty, secondProperty);
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
     * @author TG Team
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
     * @author TG Team
     *
     */
    public static class LinkedComponentCopy extends ComponentCopy {

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
	    return getOriginal();
	}

	/**
	 * @return pair of {@link LinkedComponentCopy}s, where key corresponds to label copy and value corresponds to editor copy.
	 */
	public Pair<LinkedComponentCopy, LinkedComponentCopy> getCopyPair() {
	    if (getOriginalComponent() instanceof JLabel) {
		return new Pair<LinkedComponentCopy, LinkedComponentCopy>(this, linkedCopy);
	    } else {
		return new Pair<LinkedComponentCopy, LinkedComponentCopy>(linkedCopy, this);
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
     * @author TG Team
     *
     */
    public static interface CriteriaLayoutListener extends EventListener{
	void layoutChanged();
    }

    /**
     * Represents boolean matrix, which is used to represent positions on the {@link CriteriaDndPanel}. If cell at some row and column holds value of {@link Boolean#TRUE} it means
     * this cell is occupied.
     *
     * @author TG Team
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
	 * Iterates over the elements of this boolean matrix and sets value of {@link Boolean#TRUE} for the occupied value.
	 *
	 * @param checkedElements - the list of elements those identify the occupied cells.
	 */
	public void fillMatrix(final List<String> checkedElements) {
	    if (matrix.isEmpty()) {
		return;
	    }

	    for(int row = 0; row < rows(); row++){
		for(int column = 0; column < columns(); column++){
		    final int index = column * rows() + row;
		    if(checkedElements.get(index) != null){
			matrix.get(row).set(column, true);
		    }
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
    }

    // adding empty editors:
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
     * Layouts components on this panel.
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    private void layoutEditors() {
	final Class<? extends EntityQueryCriteria> criteriaClass = (Class<? extends EntityQueryCriteria>) eqc.getType();
	final List<String> checkedProperties = eqc.getCentreDomainTreeMangerAndEnhancer().getFirstTick().checkedProperties(eqc.getEntityClass());
	for(final Entry<String, IPropertyEditor> entry : editors.entrySet()){
	    final String propertyName = entry.getKey();
	    if(!CriteriaReflector.isSecondParam(criteriaClass, propertyName)){
		final IPropertyEditor editor;
		if(CriteriaReflector.isFirstParam(criteriaClass, propertyName)){
		    final String correspondingRangePropertyName = CriteriaReflector.getSecondParamFor(criteriaClass,propertyName);
		    editor = createSinglePropertyEditor(entry.getValue(), editors.get(correspondingRangePropertyName));
		}else{
		    editor = entry.getValue();
		}
		final String criteriaParameters = CriteriaReflector.getCriteriaProperty(criteriaClass, propertyName);
		final int index = checkedProperties.indexOf(criteriaParameters);
		addDraggable(editor, (index / getRows()) * 2, index % getRows());
	    }
	}
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
	    migColumnsStr += (i == 1 ? "" : "20") + "[align left, :" + maxLabelWidth + ":][grow, :" + maxFromMinPossibleEditorWidth + ":]"; //
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
