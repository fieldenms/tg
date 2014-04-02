package ua.com.fielden.platform.snappy;

//import java.awt.event.InputEvent;
//import java.awt.event.MouseEvent;
//import java.util.ArrayList;
//import java.util.List;
//
//import javax.swing.JTable;
//import javax.swing.RowSorter.SortKey;
//
//import ua.com.fielden.platform.entity.AbstractEntity;
//import ua.com.fielden.platform.reflection.Reflector;
//import ua.com.fielden.platform.swing.egi.AbstractPropertyColumnMapping;
//import ua.com.fielden.platform.swing.egi.models.PropertyTableModel;
//import ua.com.fielden.platform.swing.sortabletable.PropertyTableModelRowSorter;
//import ua.com.fielden.platform.swing.sortabletable.SorterHandler;
//import ua.com.fielden.snappy.model.rule.ConditionedHot.SortKeyByName;
//import ua.com.fielden.snappy.view.blocks.toplevel.ConditionedHotBlock;
//import ua.com.fielden.snappy.view.blocks.toplevel.RuleBlock;

/**
 * 
 * TODO Snappy integration logic has been commented until snappy related stuff will be migrated to TG platform. TODO Snappy integration logic has been commented until snappy
 * related stuff will be migrated to TG platform. TODO Snappy integration logic has been commented until snappy related stuff will be migrated to TG platform. TODO Snappy
 * integration logic has been commented until snappy related stuff will be migrated to TG platform. TODO Snappy integration logic has been commented until snappy related stuff will
 * be migrated to TG platform. TODO Snappy integration logic has been commented until snappy related stuff will be migrated to TG platform. TODO Snappy integration logic has been
 * commented until snappy related stuff will be migrated to TG platform. TODO Snappy integration logic has been commented until snappy related stuff will be migrated to TG
 * platform. TODO Snappy integration logic has been commented until snappy related stuff will be migrated to TG platform. TODO Snappy integration logic has been commented until
 * snappy related stuff will be migrated to TG platform. TODO Snappy integration logic has been commented until snappy related stuff will be migrated to TG platform. TODO Snappy
 * integration logic has been commented until snappy related stuff will be migrated to TG platform. TODO Snappy integration logic has been commented until snappy related stuff will
 * be migrated to TG platform. TODO Snappy integration logic has been commented until snappy related stuff will be migrated to TG platform. TODO Snappy integration logic has been
 * commented until snappy related stuff will be migrated to TG platform. TODO Snappy integration logic has been commented until snappy related stuff will be migrated to TG
 * platform. TODO Snappy integration logic has been commented until snappy related stuff will be migrated to TG platform.
 * 
 * @author TG Team
 * 
 */
public class SnappySorterHandler {
}
//extends SorterHandler<AbstractEntity> {
//
//    private final PropertyTableModel<?> model;
//    private final Class<?> rootKlass;
//    private final TgSnappyApplicationModel tgApplicationModel;
//
//    public SnappySorterHandler(final PropertyTableModel<?> model, final Class<?> rootKlass, final TgSnappyApplicationModel tgApplicationModel) {
//	this.model = model;
//	this.rootKlass = rootKlass;
//	this.tgApplicationModel = tgApplicationModel;
//    }
//
//    @Override
//    public void install(final JTable table, final PropertyTableModelRowSorter<AbstractEntity> rowSorter) {
//	super.install(table, rowSorter);
//
//	// remove ability of sorting from non-sortable columns:
//	for (final AbstractPropertyColumnMapping columnMapping : model.getPropertyColumnMappings()) {
//	    if (Reflector.notSortable(columnMapping.getColumnClass())) {
//		rowSorter.setSortable(columnMapping.getPropertyName(), false);
//	    }
//	}
//
//	// synchronization with existing block sort expressions :
//	if (mainHotBlock() != null) {
//	    rowSorter.setSortKeys(convertToSortKeysByColumn(mainHotBlock().getSortKeys()));
//	}
//    }
//
//    @Override
//    public void mouseClicked(final MouseEvent e) {
//	super.mouseClicked(e);
//
//	if (e.getClickCount() == 1 /*&& column != -1*/&& (e.getModifiers() & InputEvent.CTRL_MASK) != 0) {
//	    mainHotBlock().setSortKeys(convertToSortKeysByName(getRowSorter().getSortKeys()));
//	} else if (e.getClickCount() == 2) { // localization of the tree property corresponding to column:
//	    final int column = getTable().convertColumnIndexToModel(getTable().getColumnModel().getColumnIndexAtX(e.getX()));
//	    System.out.println("locating property [" + model.getPropertyColumnMappings().get(column).getPropertyName() + "]");
//	    tgApplicationModel.getComponentsActivator().locatePropertyInTree(model.getPropertyColumnMappings().get(column).getPropertyName(), rootKlass);
//	}
//    }
//
//    private List<SortKeyByName> convertToSortKeysByName(final List<? extends SortKey> sortKeys) {
//	final List<SortKeyByName> keysByName = new ArrayList<SortKeyByName>();
//	for (final SortKey key : sortKeys) {
//	    final AbstractPropertyColumnMapping keyMapping = model.getPropertyColumnMappings().get(key.getColumn());
//
//	    // TODO : if property is of "AE type" - it should be sorted by key. But what should we do when key of that "AE type" is
//	    // of AE type too? And so on?.. It should be sorted by ".key.key..." and all necessary sub-properties should be joined -
//	    // not trivial problem. Furthermore on the way could be composite key.. So this logic should be improved.
//	    final boolean shouldBeSortedByKey = AbstractEntity.class.isAssignableFrom(keyMapping.getColumnClass());
//	    keysByName.add(new SortKeyByName(keyMapping.getPropertyName(), key.getSortOrder(), shouldBeSortedByKey)); //
//	}
//	return keysByName;
//    }
//
//    private List<SortKey> convertToSortKeysByColumn(final List<SortKeyByName> sortKeysByName) {
//	final List<SortKey> keys = new ArrayList<SortKey>();
//	for (final SortKeyByName keyByName : sortKeysByName) {
//	    keys.add(new SortKey(model.getColumnForName(keyByName.getPropertyName()), keyByName.getSortOrder()));
//	}
//	return keys;
//    }
//
//    private ConditionedHotBlock<?> mainHotBlock() {
//	final RuleBlock<?> ruleBlock = tgApplicationModel.getRuleBlock(rootKlass);
//	return ruleBlock == null ? null : (ConditionedHotBlock<?>) ruleBlock.slots().get(0).block();
//    }
//}
