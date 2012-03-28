package ua.com.fielden.platform.snappy;

//import java.awt.event.ActionEvent;
//import java.awt.event.MouseEvent;
//import java.awt.event.MouseMotionAdapter;
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//import javax.swing.AbstractAction;
//import javax.swing.Action;
//import javax.swing.JButton;
//import javax.swing.JCheckBox;
//import javax.swing.JLabel;
//import javax.swing.JPanel;
//import javax.swing.JScrollPane;
//import javax.swing.JTable;
//import javax.swing.table.JTableHeader;
//import javax.swing.table.TableColumn;
//import javax.swing.table.TableColumnModel;
//
//import net.miginfocom.swing.MigLayout;
//import ua.com.fielden.platform.entity.AbstractEntity;
//import ua.com.fielden.platform.pagination.EmptyPage;
//import ua.com.fielden.platform.pagination.IPage;
//import ua.com.fielden.platform.reflection.TitlesDescsGetter;
//import ua.com.fielden.platform.swing.components.blocking.BlockingIndefiniteProgressLayer;
//import ua.com.fielden.platform.swing.egi.EntityGridInspector;
//import ua.com.fielden.platform.swing.egi.models.builders.PropertyTableModelBuilder;
//import ua.com.fielden.platform.swing.pagination.Paginator;
//import ua.com.fielden.platform.swing.pagination.Paginator.IPageModel;
//import ua.com.fielden.platform.swing.sortabletable.PropertyTableModelRowSorter;
//import ua.com.fielden.platform.swing.utils.SwingUtilitiesEx;
//import ua.com.fielden.platform.treemodel.EntitiesTreeModel.TitledObject;
//import ua.com.fielden.snappy.FiltResult;
//import ua.com.fielden.snappy.Result;
//import ua.com.fielden.snappy.RsAggrResult;

/**
 *
 * TODO Snappy integration logic has been commented until snappy related stuff will be migrated to TG platform.
 * TODO Snappy integration logic has been commented until snappy related stuff will be migrated to TG platform.
 * TODO Snappy integration logic has been commented until snappy related stuff will be migrated to TG platform.
 * TODO Snappy integration logic has been commented until snappy related stuff will be migrated to TG platform.
 * TODO Snappy integration logic has been commented until snappy related stuff will be migrated to TG platform.
 * TODO Snappy integration logic has been commented until snappy related stuff will be migrated to TG platform.
 * TODO Snappy integration logic has been commented until snappy related stuff will be migrated to TG platform.
 * TODO Snappy integration logic has been commented until snappy related stuff will be migrated to TG platform.
 * TODO Snappy integration logic has been commented until snappy related stuff will be migrated to TG platform.
 * TODO Snappy integration logic has been commented until snappy related stuff will be migrated to TG platform.
 * TODO Snappy integration logic has been commented until snappy related stuff will be migrated to TG platform.
 * TODO Snappy integration logic has been commented until snappy related stuff will be migrated to TG platform.
 * TODO Snappy integration logic has been commented until snappy related stuff will be migrated to TG platform.
 * TODO Snappy integration logic has been commented until snappy related stuff will be migrated to TG platform.
 * TODO Snappy integration logic has been commented until snappy related stuff will be migrated to TG platform.
 * TODO Snappy integration logic has been commented until snappy related stuff will be migrated to TG platform.
 * TODO Snappy integration logic has been commented until snappy related stuff will be migrated to TG platform.
 *
 * @author TG Team
 *
 */
public class RuleResultsTab {}
//extends JPanel {
//    private static final long serialVersionUID = 1L;
//
//    private final TgSnappyApplicationModel tgApplicationModel;
//
//    private final EntityGridInspector<AbstractEntity> egi;
//    private Paginator paginator;
//    private final JLabel filteredLabel = new JLabel("<html><b>Filtered entities:</b></html>");
//    private final JLabel aggregatedLabel = new JLabel("<html><b>Aggregated values:</b></html>");
//    private final BlockingIndefiniteProgressLayer filteredEntitiesBlockingLayer;
//    private final List<TitledObject> dotNotatedProperties = new ArrayList<TitledObject>();
//    private final List<TitledObject> notFetchedAEproperties = new ArrayList<TitledObject>();
//    private final JLabel jlFeedback = new JLabel("Page 0 of 0");
//    private final Class<? extends AbstractEntity<?>> rootClass;
//
//    private SnappySorterHandler sorterHandler = null;
//
//    private IPage firstPage;
//
//    private JCheckBox processFilteringCheckBox;
//    private Result result = null;
//
//    public RuleResultsTab(final Class<? extends AbstractEntity<?>> rootClass, final TgSnappyApplicationModel tgApplicationModel) {
//	super(new MigLayout("insets 0, fill", "[c]", "[]0[]0[c,grow,fill]"));
//	this.rootClass = rootClass;
//	this.tgApplicationModel = tgApplicationModel;
//
//	final PropertyTableModelBuilder entityTableModelBuilder = new PropertyTableModelBuilder(this.rootClass);
//	for (final TitledObject dotProperty : dotNotatedProperties) {
//	    entityTableModelBuilder.addReadonly((String) dotProperty.getObject(), 200);
//	}
//	egi = new EntityGridInspector(entityTableModelBuilder.build(new ArrayList()), false);
//	egi.setRowHeight(20);
//
//	egi.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
//
//	paginator = new Paginator(new IPageModel() {
//
//	    @Override
//	    public void loadPage(final IPage<?> page) {
//		egi.getActualModel().clearInstances();
//		egi.getActualModel().addInstances(page.data().toArray(new AbstractEntity[] {}));
//		egi.getActualModel().fireTableDataChanged();
//	    }
//
//	}, new Paginator.IPageChangeFeedback() {
//	    @Override
//	    public void feedback(final IPage<?> page) {
//		jlFeedback.setText(page.toString());
//	    }
//
//	    @Override
//	    public void enableFeedback(final boolean enable) {
//		jlFeedback.setEnabled(enable);
//	    }
//	}, this.filteredEntitiesBlockingLayer = new BlockingIndefiniteProgressLayer(new JScrollPane(egi), ""), null);
//	addFilteredComponents();
//    }
//
//    public boolean addProperty(final TitledObject dotNotatedProperty) {
//	if (dotNotatedProperties.indexOf(dotNotatedProperty) < 0) {
//	    if (AbstractEntity.class.isAssignableFrom(dotNotatedProperty.getType())) {
//		notFetchedAEproperties.add(dotNotatedProperty);
//	    }
//	    return dotNotatedProperties.add(dotNotatedProperty);
//	} else {
//	    return false;
//	}
//    }
//
//    public boolean removeProperty(final String dotNotatedProperty) {
//	for (final TitledObject p : dotNotatedProperties) {
//	    if (p.getObject().equals(dotNotatedProperty)) {
//		if (AbstractEntity.class.isAssignableFrom(p.getType())) {
//		    notFetchedAEproperties.remove(p);
//		}
//		return dotNotatedProperties.remove(p);
//	    }
//	}
//	return false;
//    }
//
//    public void removeAllProperties() {
//	notFetchedAEproperties.clear();
//	dotNotatedProperties.clear();
//    }
//
//    public void setResult(final Result result) {
//	this.result = result;
//    }
//
//    public Action createFilteringProcessingAction() {
//	return new AbstractAction("<html><b>Filtered entities:</b></html>") {
//	    public void actionPerformed(final ActionEvent evt) {
//		final JCheckBox cb = (JCheckBox) evt.getSource();
//		if (cb.isSelected()) {
//		    SwingUtilitiesEx.invokeLater(new Runnable() {
//			public void run() {
//			    cb.setEnabled(false);
//			    paginator.setCurrentPage(firstPage);
//			}
//		    });
//		}
//	    }
//	};
//    }
//
//    public void refreshResult() {
//	refreshResult(false);
//    }
//
//    private void initHeaderToolTips(final JTable table, final Object[] aggrAccessors) {
//	final ColumnHeaderToolTips tips = new ColumnHeaderToolTips();
//	for (int c = 0; c < table.getColumnCount(); c++) {
//	    final TableColumn col = table.getColumnModel().getColumn(c);
//	    tips.setToolTip(col, (String) aggrAccessors[c]);
//	}
//	table.getTableHeader().addMouseMotionListener(tips);
//    }
//
//    public void refreshResult(final boolean firstTime) {
//	if (firstTime) { // if new fresh result was obtained - all properties should be fetched:
//	    notFetchedAEproperties.clear();
//	}
//	if (notFetchedAEproperties.size() == 0 && result != null) {
//	    removeAll();
//	    if (result instanceof FiltResult) {
//		setLayout(new MigLayout("insets 0, fill", "[c]", "[]0[]0[grow,fill]"));
//		addFilteredComponents();
//		paginator.setCurrentPage(firstPage);
//	    } else if (result instanceof RsAggrResult) {
//		setLayout(new MigLayout("insets 0, fill", "[c]", "[]0[]0[]0[]0[]0[grow,fill]"));
//		add(aggregatedLabel, "wrap");
//		// aggr values label and one-row table
//		final JTable aggrResultsTable = new JTable(constructDataValues((RsAggrResult) result), removeHtml(((RsAggrResult) result).getAggrAccessors().toArray()));
//		initHeaderToolTips(aggrResultsTable, ((RsAggrResult) result).getAggrAccessors().toArray());
//		add(aggrResultsTable.getTableHeader(), "grow, wrap");
//		add(aggrResultsTable, "grow, wrap");
//
//		if (!firstTime) {
//		    addFilteredComponents();
//		    paginator.setCurrentPage(firstPage);
//		} else {
//		    addFilteredComponents(createFilteringProcessingAction());
//		    paginator.setCurrentPage(new EmptyPage());
//		}
//	    }
//	}
//    }
//
//    private Object[] removeHtml(final Object[] strs) {
//	for (int i = 0; i < strs.length; i++) {
//	    strs[i] = TitlesDescsGetter.removeHtml((String)strs[i]);
//	}
//	return strs;
//    }
//
//    private Object[][] constructDataValues(final RsAggrResult aggrResult) {
//	if (!aggrResult.aggregatedValues().isEmpty()) {
//	    final Object dataValues[][] = { aggrResult.aggregatedValues().toArray() };
//	    return dataValues;
//	} else {
//	    final Object dataValues[][] = {};
//	    return dataValues;
//	}
//    }
//
//    public void refreshSkeleton(final boolean reinstallOrdering) {
//	if (reinstallOrdering) {
//	    if (sorterHandler != null) {
//		// uninstall previous sorting before changing resultant egi skeleton
//		sorterHandler.uninstall();
//	    }
//	}
//
//	final PropertyTableModelBuilder entityTableModelBuilder = new PropertyTableModelBuilder(rootClass);
//	for (final TitledObject dotProperty : dotNotatedProperties) {
//	    entityTableModelBuilder.addReadonly((String) dotProperty.getObject(), 200);
//	}
//	egi.setPropertyTableModel(entityTableModelBuilder.build(new ArrayList()));
//	egi.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
//
//	if (reinstallOrdering) {
//	    // sorting installing :
//	    sorterHandler = new SnappySorterHandler(egi.getActualModel(), rootClass, tgApplicationModel);
//	    sorterHandler.install(egi, new PropertyTableModelRowSorter(egi.getActualModel()));
//	}
//
//	paginator = new Paginator(new IPageModel() {
//
//	    @Override
//	    public void loadPage(final IPage<?> page) {
//		egi.getActualModel().clearInstances();
//		egi.getActualModel().addInstances(page.data().toArray(new AbstractEntity[] {}));
//		egi.getActualModel().fireTableDataChanged();
//	    }
//
//	}, new Paginator.IPageChangeFeedback() {
//	    @Override
//	    public void feedback(final IPage<?> page) {
//		jlFeedback.setText(page.toString());
//	    }
//
//	    @Override
//	    public void enableFeedback(final boolean enable) {
//		jlFeedback.setEnabled(enable);
//	    }
//	}, filteredEntitiesBlockingLayer, null);
//	revalidate();
//    }
//
//    private void addFilteredComponents() {
//	add((result instanceof RsAggrResult) ? processFilteringCheckBox : filteredLabel, "wrap");
//	add(createButtonPanel(), "wrap");
//	add(filteredEntitiesBlockingLayer, "grow");
//    }
//
//    private void addFilteredComponents(final Action action) {
//	add(processFilteringCheckBox = new JCheckBox(action), "wrap");
//	add(createButtonPanel(), "wrap");
//	add(filteredEntitiesBlockingLayer, "grow");
//    }
//
//    /**
//     * Creates panel with pagination buttons
//     */
//    protected JPanel createButtonPanel() {
//	final JPanel buttonPanel = new JPanel(new MigLayout("fill, insets 0", "push[][][][]20[]push", "[c]"));
//	buttonPanel.add(navButton(paginator.getFirst()));
//	buttonPanel.add(navButton(paginator.getPrev()));
//	buttonPanel.add(navButton(paginator.getNext()));
//	buttonPanel.add(navButton(paginator.getLast()));
//	buttonPanel.add(jlFeedback);
//	return buttonPanel;
//    }
//
//    /**
//     * Creates non-focusable {@link JButton} for passed action
//     *
//     * @param action
//     * @return
//     */
//    protected JButton navButton(final Action action) {
//	final JButton button = new JButton(action);
//	button.setFocusable(false);
//	return button;
//    }
//
//    public IPage getFirstPage() {
//	return firstPage;
//    }
//
//    public void setFirstPage(final IPage firstPage) {
//	this.firstPage = firstPage;
//    }
//
//    public class ColumnHeaderToolTips extends MouseMotionAdapter {
//	// Current column whose tooltip is being displayed.
//	// This variable is used to minimize the calls to setToolTipText().
//	TableColumn curCol;
//
//	// Maps TableColumn objects to tooltips
//	Map tips = new HashMap();
//
//	// If tooltip is null, removes any tooltip text.
//	public void setToolTip(final TableColumn col, final String tooltip) {
//	    if (tooltip == null) {
//		tips.remove(col);
//	    } else {
//		tips.put(col, tooltip);
//	    }
//	}
//
//	@Override
//	public void mouseMoved(final MouseEvent evt) {
//	    TableColumn col = null;
//	    final JTableHeader header = (JTableHeader) evt.getSource();
//	    final JTable table = header.getTable();
//	    final TableColumnModel colModel = table.getColumnModel();
//	    final int vColIndex = colModel.getColumnIndexAtX(evt.getX());
//
//	    // Return if not clicked on any column header
//	    if (vColIndex >= 0) {
//		col = colModel.getColumn(vColIndex);
//	    }
//
//	    if (col != curCol) {
//		header.setToolTipText((String) tips.get(col));
//		curCol = col;
//	    }
//	}
//    }
//
//}
