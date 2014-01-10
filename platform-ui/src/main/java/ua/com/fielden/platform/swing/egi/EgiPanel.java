package ua.com.fielden.platform.swing.egi;

import static javax.swing.ListSelectionModel.MULTIPLE_INTERVAL_SELECTION;
import static javax.swing.ListSelectionModel.SINGLE_INTERVAL_SELECTION;
import static javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER;
import static javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED;
import static javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.SortOrder;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.EtchedBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableColumnModelListener;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import net.miginfocom.swing.MigLayout;
import ua.com.fielden.platform.actionpanelmodel.ActionPanelBuilder;
import ua.com.fielden.platform.domaintree.ICalculatedProperty;
import ua.com.fielden.platform.domaintree.ICalculatedProperty.CalculatedPropertyCategory;
import ua.com.fielden.platform.domaintree.IDomainTreeEnhancer;
import ua.com.fielden.platform.domaintree.IDomainTreeEnhancer.IncorrectCalcPropertyException;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.IAddToResultTickManager;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.centre.IOrderingRepresentation.Ordering;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.pagination.IPage;
import ua.com.fielden.platform.reflection.Reflector;
import ua.com.fielden.platform.reflection.development.EntityDescriptor;
import ua.com.fielden.platform.swing.egi.coloring.IColouringScheme;
import ua.com.fielden.platform.swing.egi.models.PropertyTableModel;
import ua.com.fielden.platform.swing.egi.models.builders.PropertyTableModelBuilder;
import ua.com.fielden.platform.swing.review.OrderingArrow;
import ua.com.fielden.platform.swing.verticallabel.DefaultTableHeaderCellRenderer;
import ua.com.fielden.platform.swing.verticallabel.MouseDefaultHeaderHandler;
import ua.com.fielden.platform.types.Money;
import ua.com.fielden.platform.utils.EntityUtils;
import ua.com.fielden.platform.utils.Pair;
import ua.com.fielden.platform.utils.ResourceLoader;

import com.jidesoft.grid.FilterableTableModel;
import com.jidesoft.grid.QuickTableFilterField;
import com.jidesoft.popup.JidePopup;

public class EgiPanel<T extends AbstractEntity<?>> extends JPanel {

    private static final long serialVersionUID = 366454499584851959L;

    /**
     * For now it's static field, however in the future it should be configured from outside
     */
    public static final int ROW_HEIGHT = 26;

    private final EntityGridInspector<T> egi;

    private final JScrollPane egiScrollPane;

    /**
     * Holds the total editors.
     */
    private final Map<String, JTextField> totalEditors = new HashMap<String, JTextField>();

    public EgiPanel(final Class<T> rootType, final ICentreDomainTreeManagerAndEnhancer cdtme) {
	this(rootType, cdtme, null);
    }

    public EgiPanel(final Class<T> rootType, final ICentreDomainTreeManagerAndEnhancer cdtme, final IColouringScheme<T> egiColouringScheme) {
	super();
	final Pair<List<Pair<String, Integer>>, Map<String, List<String>>> gridDataModel = createGridDataModel(rootType, cdtme);
	final Class<?> managedType = cdtme.getEnhancer().getManagedType(rootType);
	final PropertyTableModel<?> _tableModel = egiColouringScheme == null ? createGridModel(managedType, gridDataModel.getKey()) : createGridModelWithColouringScheme(managedType, gridDataModel.getKey(), egiColouringScheme);
	final QuickTableFilterField _filterField = createFilterField(_tableModel);

	this.egi = createEgi(_filterField.getDisplayTableModel());
	_filterField.setTable(egi);
	configureEgiWithOrdering(egi, rootType, cdtme);
	if (!gridDataModel.getValue().isEmpty()) {
	    setLayout(new MigLayout("fill, insets 0", "[]", "[grow]0[shrink 0]0[]"));

	    add(egiScrollPane = new JScrollPane(egi, VERTICAL_SCROLLBAR_AS_NEEDED, HORIZONTAL_SCROLLBAR_NEVER), //
		    "grow, wrap");

	    final JScrollPane footerPane = new JScrollPane(createFooterPanel(managedType, gridDataModel.getValue()), VERTICAL_SCROLLBAR_NEVER, HORIZONTAL_SCROLLBAR_NEVER);
	    add(footerPane, "growx, wrap");

	    add(egiScrollPane.getHorizontalScrollBar(), "growx");
	    enableFooterPaneScrolling(footerPane, egiScrollPane.getHorizontalScrollBar());
	} else {
	    setLayout(new MigLayout("fill, insets 0"));
	    add(egiScrollPane = new JScrollPane(egi), "grow");
	}

	final JidePopup _popup = createSearchPopup(_filterField);

	final KeyStroke showSearch = KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.CTRL_DOWN_MASK); // CTRL+F
	final String SHOW_SEARCH = "SHOW_SEARCH_PANEL";
	final Action showSearchAction = createShowSearchAction(_popup);
	getActionMap().put(SHOW_SEARCH, showSearchAction);
	getInputMap(WHEN_IN_FOCUSED_WINDOW).put(showSearch, SHOW_SEARCH);
    }

    /**
     * Returns the filter component to filter table.
     *
     * @param _tableModel
     * @return
     */
    private QuickTableFilterField createFilterField(final PropertyTableModel<?> _tableModel) {
	// FIXME NOTE: this is temporary fix for JIDE 2.3.0 bug when no border is specified in UiDefaults.
	// This is resolved in version 3.4.2. Please upgrade and remove temporary fix.
	final Object oldBorder = UIManager.get("TextField.border");
	if (oldBorder == null) {
	    UIManager.put("TextField.border", BorderFactory.createCompoundBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), BorderFactory.createEmptyBorder(2, 2, 2, 2)));
	}

	final QuickTableFilterField _filterField = new QuickTableFilterField(_tableModel);

	if (oldBorder == null) {
	    UIManager.put("TextField.border", oldBorder);
	}
	// END OF FIXME NOTE: this is temporary fix for JIDE 2.3.0 bug when no border is specified in UiDefaults.
	// This is resolved in version 3.4.2. Please upgrade and remove temporary fix.

	_filterField.setHintText("Type here to filter...");
	_filterField.getTextField().setEnabled(true);
	_filterField.getTextField().setEditable(true);
	_filterField.setBackground(Color.WHITE);
	return _filterField;
    }

    /**
     * Returns the pop up component with search field.
     * @param _filterField
     * @return
     */
    private JidePopup createSearchPopup(final QuickTableFilterField _filterField) {
	final ActionPanelBuilder _panelBuilder = new ActionPanelBuilder();
	final JidePopup _popup = com.jidesoft.popup.JidePopupFactory.getSharedInstance().createPopup();
	final Action hideSearchAction = createHideSearchAction(_popup);
	final KeyStroke hideSearch = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0); // ESC
	final String HIDE_SEARCH = "HIDE_SEARCH_PANEL";

	_panelBuilder.addButton(hideSearchAction);
	_panelBuilder.addComponent(_filterField);

	final JToolBar _toolBar = _panelBuilder.buildActionPanel();
	_toolBar.setFloatable(false);
	_toolBar.setBorder(BorderFactory.createEmptyBorder());

	_popup.setLayout(new MigLayout("fill, insets 0",  "[fill, grow]", "[fill, grow]"));
	_popup.setResizable(false);
	_popup.setMovable(false);
	_popup.add(_toolBar);
	_popup.add(_filterField);
	_popup.setTransient(false);
	_popup.getActionMap().put(HIDE_SEARCH, hideSearchAction);
	_popup.getInputMap(WHEN_IN_FOCUSED_WINDOW).put(hideSearch, HIDE_SEARCH);
	_popup.setDefaultFocusComponent(_filterField.getTextField());
	_popup.addPopupMenuListener(new PopupMenuListener() {

	    @Override
	    public void popupMenuWillBecomeVisible(final PopupMenuEvent e) {
		// TODO Auto-generated method stub

	    }

	    @Override
	    public void popupMenuWillBecomeInvisible(final PopupMenuEvent e) {
		_filterField.setSearchingText("");
	    }

	    @Override
	    public void popupMenuCanceled(final PopupMenuEvent e) {
		// TODO Auto-generated method stub

	    }
	});
	return _popup;
    }

    private Action createHideSearchAction(final JidePopup _popup) {
	return new AbstractAction() {

	    private static final long serialVersionUID = 8327840776664813556L;
	    {
		final Icon icon = ResourceLoader.getIcon("images/cross.png");
		putValue(Action.LARGE_ICON_KEY, icon);
		putValue(Action.SHORT_DESCRIPTION, "Close this search panel");
	    }

	    @Override
	    public void actionPerformed(final ActionEvent e) {
		_popup.hidePopup();
	    }
	};
    }

    private Action createShowSearchAction(final JidePopup _popup) {
	return new AbstractAction() {

	    private static final long serialVersionUID = 8327840776664813556L;

	    @Override
	    public void actionPerformed(final ActionEvent e) {
		_popup.showPopup(SwingConstants.SOUTH_WEST, EgiPanel.this);
	    }
	};
    }

    /**
     * Returns the {@link EntityGridInspector} associated with this panel.
     *
     * @return
     */
    public EntityGridInspector<T> getEgi() {
	return egi;
    }

    /**
     * Set passed data to the grid inspector and updates totals.
     *
     * @param page
     */
    public void setData(final IPage<T> page) {
	egi.getActualModel().setInstances(page == null ? new ArrayList<T>() : page.data());
	if (page != null && page.summary() != null) {
	    for (final Map.Entry<String, JTextField> totalEntry : totalEditors.entrySet()) {
		setValueForTotalEditor(page.summary().get(totalEntry.getKey()), totalEntry.getValue());
	    }
	}
    }

    /**
     * Set the specified value for the given editor.
     *
     * @param value
     * @param editor
     */
    private void setValueForTotalEditor(final Object value, final JTextField editor) {
	if (value != null) {
	    final Class<?> valueClass = value.getClass();
	    final String totalsStrValue = EntityUtils.toString(value, valueClass);
	    editor.setText(totalsStrValue);
	    editor.setCaretPosition(0);
	    if (Number.class.isAssignableFrom(valueClass) || Money.class.isAssignableFrom(valueClass) || valueClass == int.class || valueClass == double.class) {
		editor.setHorizontalAlignment(JTextField.RIGHT);
	    }
	}
    }

    /**
     * Configures the analysis entity grid inspector with ordering facility.
     */
    private void configureEgiWithOrdering(final EntityGridInspector<T> egi, final Class<T> root, final ICentreDomainTreeManagerAndEnhancer cdtme){
	final IAddToResultTickManager tickManager = cdtme.getSecondTick();
	egi.getColumnModel().addColumnModelListener(createColumnSwapModelListener(egi, root, tickManager));
	egi.getTableHeader().addMouseListener(createTableHeaderClickMouseListener(egi, root, tickManager));
	for (int columnIndex = 0; columnIndex < egi.getColumnCount(); columnIndex++) {
	    final TableColumn column =  egi.getColumnModel().getColumn(columnIndex);
	    column.setHeaderRenderer(new SortableTableHeaderCellRenderer(egi, root, tickManager));
	    column.addPropertyChangeListener(createColumnWidthChangeListener(egi, root, tickManager));
	}
	final MouseDefaultHeaderHandler mouseHandler = new MouseDefaultHeaderHandler();
	egi.getTableHeader().addMouseMotionListener(mouseHandler);
	egi.getTableHeader().addMouseListener(mouseHandler);
    }

    /**
     * Creates {@link PropertyChangeListener} that listens the column width change events.
     *
     * @param root
     * @param tickManager
     * @param egi
     * @return
     */
    private PropertyChangeListener createColumnWidthChangeListener(final EntityGridInspector<T> egi, final Class<T> root, final IAddToResultTickManager tickManager) {
	return new PropertyChangeListener() {

	    @Override
	    public void propertyChange(final PropertyChangeEvent evt) {
		if (evt.getPropertyName().equals("width")) {
		    final TableColumn tableColumn = (TableColumn)evt.getSource();
		    final String columnProperty = egi.getActualModel().getPropertyColumnMappings().get(tableColumn.getModelIndex()).getPropertyName();
		    tickManager.setWidth(root, columnProperty, ((Integer)evt.getNewValue()).intValue());
		}
	    }
	};
    }

    /**
     * Creates the mouse listener that listens the table header mouse click events.
     * @param tickManager
     * @param root
     *
     * @param egi
     * @return
     */
    private MouseListener createTableHeaderClickMouseListener(final EntityGridInspector<T> egi, final Class<T> root, final IAddToResultTickManager tickManager) {
	return new MouseAdapter() {
	    @Override
	    public void mouseClicked(final MouseEvent e) {
		final TableColumnModel columnModel = egi.getColumnModel();
		final int viewColumnIndex = columnModel.getColumnIndexAtX(e.getX());
		if (e.getClickCount() == 1 && viewColumnIndex >= 0 && (e.getModifiers() & InputEvent.CTRL_MASK) != 0) {
		    final TableColumn viewColumn = columnModel.getColumn(viewColumnIndex);
		    final String property = egi.getActualModel().getPropertyColumnMappings().get(viewColumn.getModelIndex()).getPropertyName();
		    tickManager.toggleOrdering(root, property);
		    egi.getTableHeader().repaint();
		}
	    }

	};
    }

    /**
     * Creates {@link TableColumnModelListener} instance that listens the column moved events.
     *
     * @param root
     * @param tickManager
     * @return
     */
    private TableColumnModelListener createColumnSwapModelListener(final EntityGridInspector<T> egi, final Class<T> root, final IAddToResultTickManager tickManager) {
	return new TableColumnModelListener() {

	    @Override
	    public void columnSelectionChanged(final ListSelectionEvent e) { }

	    @Override
	    public void columnRemoved(final TableColumnModelEvent e) { }

	    @Override
	    public void columnMarginChanged(final ChangeEvent e) { }

	    @Override
	    public void columnAdded(final TableColumnModelEvent e) { }

	    @Override
	    public void columnMoved(final TableColumnModelEvent e) {
		if(e.getFromIndex() != e.getToIndex()){
		    final TableColumn fromColumn = egi.getColumnModel().getColumn(e.getFromIndex());
		    final TableColumn toColumn = egi.getColumnModel().getColumn(e.getToIndex());
		    final String fromProperty = egi.getActualModel().getPropertyColumnMappings().get(fromColumn.getModelIndex()).getPropertyName();
		    final String toProperty = egi.getActualModel().getPropertyColumnMappings().get(toColumn.getModelIndex()).getPropertyName();
		    tickManager.swap(root, fromProperty, toProperty);
		}

	    }
	};
    }

    /**
     * Creates footer panel of totals for the specified entity grid inspector.
     *
     * @param entityGridInspector
     * @param totals
     * @return
     */
    private Component createFooterPanel(final Class<?> managedType, final Map<String, List<String>> totals) {
	final List<? extends AbstractPropertyColumnMapping<?>> columns = egi.getActualModel().getPropertyColumnMappings();
	final int rowNumber = determineTotalRows(totals);
	final int colNumber = columns.size();
	final JPanel footer = new JPanel(new MigLayout("nogrid, insets 0"));
	final List<JPanel> totalComponents = new ArrayList<JPanel>();
	totalEditors.clear();
	for(int columnIndex = 0; columnIndex < colNumber; columnIndex++){
	    final List<String> total = totals.get(columns.get(columnIndex).getPropertyName());
	    final JPanel totalPanel = createTotalPanel(managedType, total, rowNumber, columns.get(columnIndex).getSize(), totalEditors);
	    footer.add(totalPanel, "grow, gap 0 0 0 0");
	    totalComponents.add(totalPanel);
	}
	// adding last label so that it fill the space in the viewport under vertical scroll bar
	final JPanel stubPanel = createTotalPanel(managedType, null, rowNumber, 50, totalEditors);
	footer.add(stubPanel, "grow, gap 0 0 0 0");
	addResizingListener(footer, totalComponents);
	return footer;
    }


    /**
     * Creates panel with totals for the specified column.
     *
     * @param total
     * @param rowNumber
     * @param size
     * @param totalEditors2
     * @return
     */
    private static JPanel createTotalPanel(final Class<?> managedType, final List<String> total, final int rowNumber, final Integer size, final Map<String, JTextField> totalEditors) {
	final JPanel totalPanel = new JPanel(new MigLayout("fill, insets 0","[fill, grow]", "0[t]0"));
	totalPanel.setPreferredSize(new Dimension(size, 0));
	if (total != null) {
	    final EntityDescriptor ed = new EntityDescriptor(managedType, total);
	    for(int totalIndex = 0; totalIndex < total.size() - 1; totalIndex++){
		final JTextField totalEditor = createTotalEditor(total.get(totalIndex), size, ed);
		totalPanel.add(totalEditor, "wrap");
		totalEditors.put(total.get(totalIndex), totalEditor);
	    }
	    final JTextField totalEditor = createTotalEditor(total.get(total.size()-1), size, ed);
	    totalPanel.add(totalEditor);
	    totalEditors.put(total.get(total.size()-1), totalEditor);
	}
	return totalPanel;
    }

    /**
     * Returns the total editor with specified tool tip.
     * @param size
     *
     * @return
     */
    private static JTextField createTotalEditor(final String propertyName, final Integer size, final EntityDescriptor ed) {
	final JTextField totalsEditor = new JTextField();
	totalsEditor.setPreferredSize(new Dimension(size, ROW_HEIGHT));
	totalsEditor.setEditable(false);
//	final Pair<String, String> titleAndDesc = TitlesDescsGetter.getTitleAndDesc(propertyName, managedType);
//	String toolTip = "";
//	if(!StringUtils.isEmpty(titleAndDesc.getKey())){
//	    toolTip += titleAndDesc.getKey();
//	}
//	if(!StringUtils.isEmpty(titleAndDesc.getValue())){
//	    toolTip += StringUtils.isEmpty(toolTip) ? titleAndDesc.getValue() : "(" + titleAndDesc.getValue() + ")";
//	}
	totalsEditor.setToolTipText(ed.getTitleAndDesc(propertyName).getValue() /* toolTip */);
	return totalsEditor;
    }

    /**
     * Adds resize listener to table column that handles column size changed and column moved events.
     *
     * @param footer
     * @param totalComponents
     */
    private void addResizingListener(final JPanel footer, final List<JPanel> totalComponents) {
	egi.getColumnModel().addColumnModelListener(new TableColumnModelListener() {
	    @Override
	    public void columnAdded(final TableColumnModelEvent e) {
	    }

	    @Override
	    public void columnMarginChanged(final ChangeEvent e) {
		final TableColumn column = egi.getTableHeader().getResizingColumn();
		if (column != null) {
		    // obtained column that was resized
		    final JPanel columnComponents = totalComponents.get(egi.convertColumnIndexToView(column.getModelIndex()));
		    columnComponents.setPreferredSize(new Dimension(column.getWidth(), columnComponents.getHeight()));
		} else {
		    // couldn't determine the column that was resized - fitting all editors to column width's
		    final int columnNumber = egi.getActualModel().getPropertyColumnMappings().size();
		    for(int columnIndex = 0; columnIndex < columnNumber; columnIndex++){
			final JPanel totalPanel = totalComponents.get(columnIndex);
			totalPanel.setPreferredSize(new Dimension(egi.getColumnModel().getColumn(columnIndex).getWidth(), totalPanel.getHeight()));
		    }
		}
		footer.revalidate();
	    }

	    @Override
	    public void columnMoved(final TableColumnModelEvent e) {
		if(e.getFromIndex() != e.getToIndex()){

		    final JPanel fromColumn = totalComponents.remove(e.getFromIndex());
		    totalComponents.add(e.getToIndex(), fromColumn);

		    footer.removeAll();
		    for(final JPanel totalPanel : totalComponents){
			footer.add(totalPanel, "grow, gap 0 0 0 0");
		    }
		    footer.revalidate();
		}
	    }

	    @Override
	    public void columnRemoved(final TableColumnModelEvent e) {
	    }

	    @Override
	    public void columnSelectionChanged(final ListSelectionEvent e) {
	    }
	});
    }



    /**
     * Determines the number of total rows to insert.
     *
     * @param totals
     * @return
     */
    private int determineTotalRows(final Map<String, List<String>> totals) {
	int rows = -1;
	for(final List<String> total : totals.values()){
	    rows = total.size() > rows ? total.size() : rows;
	}
	return rows;
    }

    /**
     * Returns the pair of column names and totals map. The totals map - it is a map between column names and list of total names.
     *
     * @param rootType
     * @param cdtme
     * @return
     */
    private Pair<List<Pair<String, Integer>>, Map<String, List<String>>> createGridDataModel(final Class<T> rootType, final ICentreDomainTreeManagerAndEnhancer cdtme) {
	final List<Pair<String, Integer>> columns = new ArrayList<Pair<String, Integer>>();
	final Map<String, List<String>> totals = new HashMap<String, List<String>>();
	final IDomainTreeEnhancer enhancer = cdtme.getEnhancer();
	final List<String> checkedProperties = cdtme.getSecondTick().checkedProperties(rootType);
	for(final String property : checkedProperties){
	    try {
		final ICalculatedProperty calcProperty = enhancer.getCalculatedProperty(rootType, property);
		if(calcProperty.category() == CalculatedPropertyCategory.AGGREGATED_EXPRESSION && calcProperty.getOriginationProperty() != null){
		    final String originProperty = Reflector.fromRelative2AbsotulePath(calcProperty.getContextPath(), calcProperty.getOriginationProperty());
		    if(checkedProperties.contains(originProperty)){
			List<String> totalList = totals.get(originProperty);
			    if(totalList == null){
				totalList = new ArrayList<String>();
				totals.put(originProperty, totalList);
			    }
			    totalList.add(property);
		    }
		}else{
		    columns.add(new Pair<String, Integer>(property, Integer.valueOf(cdtme.getSecondTick().getWidth(rootType, property))));
		}
	    } catch(final IncorrectCalcPropertyException ex){
		columns.add(new Pair<String, Integer>(property, Integer.valueOf(cdtme.getSecondTick().getWidth(rootType, property))));
	    }
	}
	return new Pair<List<Pair<String, Integer>>, Map<String,List<String>>>(columns, totals);
    }

    /**
     * Creates PropertyTableModel for the specified {@link ICentreDomainTreeManagerAndEnhancer} instance.
     * @param rootType
     *
     * @param properties
     * @return
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    protected PropertyTableModel<?> createGridModel(final Class<?> managedType, final List<Pair<String, Integer>> properties) {
	final PropertyTableModelBuilder<?> tableModelBuilder = new PropertyTableModelBuilder(managedType);
	for(final Pair<String, Integer> property : properties){
	    tableModelBuilder.addReadonly(property.getKey(), property.getValue());
	}
	return tableModelBuilder.build(new ArrayList());
    }

    /**
     * Creates PropertyTableModel for the specified {@link ICentreDomainTreeManagerAndEnhancer} instance.
     * @param rootType
     *
     * @param properties
     * @return
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    protected PropertyTableModel<?> createGridModelWithColouringScheme(final Class<?> managedType, final List<Pair<String, Integer>> properties, final IColouringScheme egiColouringScheme) {
	final PropertyTableModelBuilder<?> tableModelBuilder = new PropertyTableModelBuilder(managedType);
	for(final Pair<String, Integer> property : properties){
	    tableModelBuilder.addReadonly(property.getKey(), property.getValue());
	}
	return tableModelBuilder.//
		setRowColoringScheme(egiColouringScheme).//
		build(new ArrayList());
    }

    /**
     * Creates single selection {@link EntityGridInspector} for the specified {@link PropertyTableModel}.
     *
     * @param model
     * @return
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    protected EntityGridInspector<T> createEgi(final FilterableTableModel model) {
	final EntityGridInspector<T> egi = new EntityGridInspector(model, false);
	egi.setRowHeight(ROW_HEIGHT);
	egi.setSelectionMode(MULTIPLE_INTERVAL_SELECTION);
	egi.setSingleExpansion(true);
	egi.getColumnModel().getSelectionModel().setSelectionMode(SINGLE_INTERVAL_SELECTION);
	return egi;
    }

    /**
     * Enables total panel scrolling.
     *
     * @param footerPane
     * @param egiHorizScrollBar
     */
    private void enableFooterPaneScrolling(final JScrollPane footerPane, final JScrollBar egiHorizScrollBar) {
	egiHorizScrollBar.addAdjustmentListener(new AdjustmentListener() {
	    @Override
	    public void adjustmentValueChanged(final AdjustmentEvent e) {
		SwingUtilities.invokeLater(new Runnable() {
		    public void run() {
			footerPane.getViewport().setViewPosition(new Point(egiHorizScrollBar.getValue(), 0));
		    }
		});
	    }
	});
    }

    /**
     * Table header cell renderer that draws sorting arrows for the concrete table column.
     *
     * @author TG Team
     *
     */
    public static class SortableTableHeaderCellRenderer<T extends AbstractEntity<?>> extends DefaultTableHeaderCellRenderer{

	private static final long serialVersionUID = -6294136148685562497L;

	/**
	 * The ordering arrow to be drawn on the table column.
	 */
	private final OrderingIcon orderingIcon = new OrderingIcon();

	/**
	 * Table for which this cell renderer will be created.
	 */
	private final EntityGridInspector<T> egi;

	/**
	 * The entity type for which this analysis was created.
	 */
	private final Class<T> root;

	/**
	 * The {@link IAddToResultTickManager} instance needed to determine checked properties and ordering properties.
	 */
	private final IAddToResultTickManager tickManager;

	private final static int ICON_LEFT_INSETS = 3;
	private final static int ICON_TOP_INSETS = 3;
	private final static int ICON_RIGHT_INSETS = -3;
	private final static int ICON_BOTTOM_INSETS = 3;

	/**
	 * Creates new table header cell renderer with ordering icon
	 * @param tickManager
	 * @param root
	 */
	public SortableTableHeaderCellRenderer(final EntityGridInspector<T> egi, final Class<T> root, final IAddToResultTickManager tickManager) {
	    this.egi = egi;
	    this.root = root;
	    this.tickManager = tickManager;
	    setHorizontalAlignment(LEFT);
	    setHorizontalTextPosition(RIGHT);
	}

	@Override
	protected Icon getIcon(final JTable table, final int column) {
	    final String property = egi.getActualModel().getPropertyColumnMappings().get(egi.getColumnModel().getColumn(column).getModelIndex()).getPropertyName();

	    final List<Pair<String, Ordering>> sortKeys = tickManager.orderedProperties(root);
	    orderingIcon.setSortOrder(SortOrder.UNSORTED);
	    for (int counter = 0; counter < sortKeys.size(); counter++) {
		if (EntityUtils.equalsEx(property, sortKeys.get(counter).getKey())) {
		    orderingIcon.setOrder(counter + 1);
		    switch(sortKeys.get(counter).getValue()){
		    case ASCENDING:
			orderingIcon.setSortOrder(SortOrder.ASCENDING);
			break;
		    case DESCENDING:
			orderingIcon.setSortOrder(SortOrder.DESCENDING);
			break;
		    }
		}
	    }
	    return orderingIcon;

	}

	/**
	 * Icon that represents ordering arrow on the table header
	 *
	 * @author TG Team
	 *
	 */
	private class OrderingIcon implements Icon {

	    private final OrderingArrow orderingArrow;

	    /**
	     * Creates {@link OrderingIcon} and {@link OrderingArrow}
	     */
	    public OrderingIcon() {
		orderingArrow = new OrderingArrow();
	    }

	    @Override
	    public int getIconHeight() {
		return (int) Math.ceil(orderingArrow.getActualHeight(getGraphics()) + ICON_TOP_INSETS + ICON_BOTTOM_INSETS);
	    }

	    @Override
	    public int getIconWidth() {
		return (int) Math.ceil(orderingArrow.getActualWidth(getGraphics()) + ICON_LEFT_INSETS + ICON_RIGHT_INSETS);
	    }

	    @Override
	    public void paintIcon(final Component c, final Graphics g, final int x, final int y) {
		g.translate(x + ICON_LEFT_INSETS, y + ICON_TOP_INSETS);
		orderingArrow.paintComponent(g);
		g.translate(-x - ICON_LEFT_INSETS, -y - ICON_TOP_INSETS);
	    }

	    /**
	     * Set the {@link SortOrder} for the {@link OrderingArrow}
	     *
	     * @param sortOrder
	     */
	    public void setSortOrder(final SortOrder sortOrder) {
		orderingArrow.setSortOrder(sortOrder);
	    }

	    /**
	     * Set the order value for the {@link OrderingArrow} associated with this {@link OrderingIcon}
	     *
	     * @param order
	     */
	    public void setOrder(final int order) {
		orderingArrow.setOrder(order);
	    }

	    /**
	     * Set the indicator that determines whether {@link OrderingArrow} is highlighted or not
	     *
	     * @param mouseOver
	     */
	    public void setMouseOver(final boolean mouseOver) {
		orderingArrow.setMouseOver(mouseOver);
	    }

	}

	@Override
	public Dimension getPreferredSize() {
	    final Dimension labelDimension = super.getPreferredSize();
	    return new Dimension(labelDimension.width, orderingIcon.getIconHeight());
	}

	@Override
	public void setMouseOver(final boolean mouseOver) {
	    super.setMouseOver(mouseOver);
	    orderingIcon.setMouseOver(mouseOver);
	}

	@Override
	public Insets getInsets() {
	    return new Insets(3, 3, 3, 3 + (getIcon() != null ? ICON_LEFT_INSETS : 0));
	}

	@Override
	public Insets getInsets(final Insets insets) {
	    return getInsets();
	}
    }

    public JScrollPane getEgiScrollPane() {
	return egiScrollPane;
    }
}
