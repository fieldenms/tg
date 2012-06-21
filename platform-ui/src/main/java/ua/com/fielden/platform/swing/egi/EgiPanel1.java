package ua.com.fielden.platform.swing.egi;

import static javax.swing.ListSelectionModel.SINGLE_INTERVAL_SELECTION;
import static javax.swing.ListSelectionModel.SINGLE_SELECTION;
import static javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER;
import static javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED;
import static javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SortOrder;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableColumnModelListener;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import net.miginfocom.swing.MigLayout;
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
import ua.com.fielden.platform.swing.egi.models.PropertyTableModel;
import ua.com.fielden.platform.swing.egi.models.builders.PropertyTableModelBuilder;
import ua.com.fielden.platform.swing.review.OrderingArrow;
import ua.com.fielden.platform.swing.verticallabel.DefaultTableHeaderCellRenderer;
import ua.com.fielden.platform.swing.verticallabel.MouseDefaultHeaderHandler;
import ua.com.fielden.platform.types.Money;
import ua.com.fielden.platform.utils.EntityUtils;
import ua.com.fielden.platform.utils.Pair;

public class EgiPanel1<T extends AbstractEntity<?>> extends JPanel {

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

    public EgiPanel1(final Class<T> rootType, final ICentreDomainTreeManagerAndEnhancer cdtme) {
	super();
	final Pair<List<Pair<String, Integer>>, Map<String, List<String>>> gridDataModel = createGridDataModel(rootType, cdtme);
	final Class<?> managedType = cdtme.getEnhancer().getManagedType(rootType);
	this.egi = createEgi(createGridModel(managedType, gridDataModel.getKey()));

	configureEgiWithOrdering(rootType, cdtme);

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
		final Object value = page.summary().get(totalEntry.getKey());
		final Class<?> valueClass = value.getClass();
		final String totalsStrValue = EntityUtils.toString(value, valueClass);
		final JTextField editor = totalEntry.getValue();
		editor.setText(totalsStrValue);
		editor.setCaretPosition(0);
		if (Number.class.isAssignableFrom(valueClass) || Money.class.isAssignableFrom(valueClass) || valueClass == int.class || valueClass == double.class) {
		    editor.setHorizontalAlignment(JTextField.RIGHT);
		}
	    }
	}
    }

    /**
     * Configures the analysis entity grid inspector with ordering facility.
     */
    private void configureEgiWithOrdering(final Class<T> root, final ICentreDomainTreeManagerAndEnhancer cdtme){
	final IAddToResultTickManager tickManager = cdtme.getSecondTick();
	egi.getColumnModel().addColumnModelListener(createColumnSwapModelListener(root, tickManager));
	egi.getTableHeader().addMouseListener(createTableHeaderClickMouseListener(root, tickManager));
	for (int columnIndex = 0; columnIndex < egi.getColumnCount(); columnIndex++) {
	    final TableColumn column =  egi.getColumnModel().getColumn(columnIndex);
	    column.setHeaderRenderer(new SortableTableHeaderCellRenderer(root, tickManager));
	    column.addPropertyChangeListener(createColumnWidthChangeListener(root, tickManager));
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
    private PropertyChangeListener createColumnWidthChangeListener(final Class<T> root, final IAddToResultTickManager tickManager) {
	return new PropertyChangeListener() {

	    @Override
	    public void propertyChange(final PropertyChangeEvent evt) {
		if (evt.getPropertyName().equals("width")) {
		    final TableColumn tableColumn = (TableColumn)evt.getSource();
		    final String columnProperty = tickManager.checkedProperties(root).get(egi.getColumnModel().getColumnIndex(tableColumn.getIdentifier()));
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
    private MouseListener createTableHeaderClickMouseListener(final Class<T> root, final IAddToResultTickManager tickManager) {
	return new MouseAdapter() {
	    @Override
	    public void mouseClicked(final MouseEvent e) {
		final TableColumnModel columnModel = egi.getColumnModel();
		final int viewColumn = columnModel.getColumnIndexAtX(e.getX());
		if (e.getClickCount() == 1 && viewColumn >= 0 && (e.getModifiers() & InputEvent.CTRL_MASK) != 0) {
		    final String property = tickManager.checkedProperties(root).get(viewColumn);
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
    private TableColumnModelListener createColumnSwapModelListener(final Class<T> root, final IAddToResultTickManager tickManager) {
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
		    final List<String> checkedProperties = tickManager.checkedProperties(root);
		    final String fromProperty = checkedProperties.get(e.getFromIndex());
		    final String toProperty = checkedProperties.get(e.getToIndex());
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
		final JTextField totalEditor = createTotalEditor(managedType, total.get(totalIndex), size, ed);
		totalPanel.add(totalEditor, "wrap");
		totalEditors.put(total.get(totalIndex), totalEditor);
	    }
	    final JTextField totalEditor = createTotalEditor(managedType, total.get(total.size()-1), size, ed);
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
    private static JTextField createTotalEditor(final Class<?> managedType, final String propertyName, final Integer size, final EntityDescriptor ed) {
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
		final String originProperty = Reflector.fromRelative2AbsotulePath(calcProperty.getContextPath(), calcProperty.getOriginationProperty());
		if(calcProperty.category() == CalculatedPropertyCategory.AGGREGATED_EXPRESSION){
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
    private PropertyTableModel<?> createGridModel(final Class<?> managedType, final List<Pair<String, Integer>> properties) {
	final PropertyTableModelBuilder<?> tableModelBuilder = new PropertyTableModelBuilder(managedType);
	for(final Pair<String, Integer> property : properties){
	    tableModelBuilder.addReadonly(property.getKey(), property.getValue());
	}
	return tableModelBuilder.build(new ArrayList());
    }

    /**
     * Creates single selection {@link EntityGridInspector} for the specified {@link PropertyTableModel}.
     *
     * @param model
     * @return
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    private EntityGridInspector<T> createEgi(final PropertyTableModel<?> model) {
	final EntityGridInspector<T> egi = new EntityGridInspector(model, false);
	egi.setRowHeight(ROW_HEIGHT);
	egi.setSelectionMode(SINGLE_SELECTION);
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
    private class SortableTableHeaderCellRenderer extends DefaultTableHeaderCellRenderer{

	private static final long serialVersionUID = -6294136148685562497L;

	/**
	 * The ordering arrow to be drawn on the table column.
	 */
	private final OrderingIcon orderingIcon = new OrderingIcon();

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
	public SortableTableHeaderCellRenderer(final Class<T> root, final IAddToResultTickManager tickManager) {
	    this.root = root;
	    this.tickManager = tickManager;
	    setHorizontalAlignment(LEFT);
	    setHorizontalTextPosition(RIGHT);
	}

	@Override
	protected Icon getIcon(final JTable table, final int column) {
	    final String property = tickManager.checkedProperties(root).get(column);

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
}
