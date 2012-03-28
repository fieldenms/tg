package ua.com.fielden.platform.snappy;

//import it.cnr.imaa.essi.lablib.gui.checkboxtree.TreeCheckingEvent;
//
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.List;
//
//import javax.swing.JTabbedPane;
//import javax.swing.tree.DefaultMutableTreeNode;
//import javax.swing.tree.TreePath;
//
//import ua.com.fielden.platform.entity.AbstractEntity;
//import ua.com.fielden.platform.reflection.TitlesDescsGetter;
//import ua.com.fielden.platform.treemodel.EntitiesTreeModel.TitledObject;
//import ua.com.fielden.snappy.model.rule.ConditionedHot.SortKeyByName;
//import ua.com.fielden.snappy.storing.ApplicationModel;
//import ua.com.fielden.snappy.view.blocks.toplevel.ConditionedHotBlock;

/**
 * This listener used to add/remove the blocks and result columns corresponding to added/removed checking path (immediate synchronization between rule blocks+columns and entities
 * tree).
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
 * @author Jhou
 *
 */
public class ResultPropertyCheckingListener {}
//extends RulePropertyCheckingListener {
//
//    public ResultPropertyCheckingListener(final JTabbedPane rulesTabbedPane, final TgSnappyApplicationModel tgApplicationModel) {
//	super(rulesTabbedPane, tgApplicationModel);
//    }
//
//    /**
//     * Adds/removes the columns and "fetching" property blocks corresponding to added/removed checking path (immediate synchronization between rule blocks+columns and entities
//     * tree).
//     */
//    @Override
//    public void valueChanged(final TreeCheckingEvent e) {
//	if (e.isCheckedPath()) {
//	    addPathBlocks(e.getPath(), PropertyType.FETCHING);
//	    addPathColumn(e.getPath());
//	} else {
//	    removePathBlocks(e.getPath(), PropertyType.FETCHING);
//	    removePathColumn(e.getPath());
//	}
//    }
//
//    /**
//     * Removes the column corresponding to the <code>path</code>.
//     *
//     * @param path
//     */
//    private void removePathColumn(final TreePath path) {
//	if (path.getPathCount() <= 2) { // ["Entities", klass] or ["Entities"] path
//	    return;
//	}
//
//	new SleepingBlockingCommand("Synchronization...", getTgApplicationModel().getComponentsActivator().getBlockingIndefiniteProgressPane()) {
//	    private static final long serialVersionUID = 985541110851542292L;
//
//	    @Override
//	    protected void postAction(final Boolean value) {
//		final Class<?> rootKlass = ((Class<?>) ((TitledObject) (((DefaultMutableTreeNode) path.getPathComponent(1)).getUserObject())).getObject());
//		final String entityTitle = TitlesDescsGetter.getEntityTitleAndDesc(rootKlass).getKey();
//		final String tabCaption = entityTitle + ApplicationModel.RULE_NAME_SUFFIX;
//
//		final RuleResultsTab rootTab = TgSnappyComponentsActivator.findRuleTab(rulesTabbedPane, tabCaption);
//		if (rootTab != null) {
//		    final String name = createDotNotationName(Arrays.copyOfRange(path.getPath(), 2, path.getPath().length));
//		    rootTab.removeProperty(name);
//		    rootTab.refreshSkeleton(true);
//
//		    ////////////////////////Remove corresponding ordering key from block//////////////////////////
//		    removeOrderingFromDeletedColumnIfExists((ConditionedHotBlock<?>) getTgApplicationModel().getOrCreateRuleBlock(rootKlass).slots().get(0).block(), name);
//		    //////////////////////////////////////////////////
//
//		    rootTab.refreshResult();
//		}
//		super.postAction(value);
//	    }
//	}.actionPerformed(null);
//    }
//
//    /**
//     * Removes ordering key from column that is deleting if this ordering key exists.
//     */
//    private void removeOrderingFromDeletedColumnIfExists(final ConditionedHotBlock<?> condHotBlock, final String dotNotPropertyName) {
//	final List<SortKeyByName> keys = new ArrayList<SortKeyByName>(condHotBlock.getSortKeys());
//	for (final SortKeyByName key : condHotBlock.getSortKeys()) {
//	    if (dotNotPropertyName.equals(key.getPropertyName())) {
//		keys.remove(key);
//		System.err.println("Ordering key " + key + " REMOVED.");
//	    }
//	}
//	condHotBlock.setSortKeys(keys);
//    }
//
//    /**
//     * Adds the column corresponding to the <code>path</code> in existing results tab (or creates a new one).
//     *
//     * @param path
//     */
//    void addPathColumn(final TreePath path) {
//	if (path.getPathCount() <= 1) { // ["Entities"] path
//	    return;
//	}
//	new SleepingBlockingCommand("Synchronization...", getTgApplicationModel().getComponentsActivator().getBlockingIndefiniteProgressPane()) {
//	    private static final long serialVersionUID = 985541110851542292L;
//
//	    @Override
//	    protected void postAction(final Boolean value) {
//		addPathColumn1(path, true);
//		super.postAction(value);
//	    }
//	}.actionPerformed(null);
//    }
//
//    public void addPathColumn1(final TreePath path, final boolean reinstallOrdering) {
//	final Class<? extends AbstractEntity<?>> rootKlass = ((Class<? extends AbstractEntity<?>>) ((TitledObject) (((DefaultMutableTreeNode) path.getPathComponent(1)).getUserObject())).getObject());
//	final String entityTitle = TitlesDescsGetter.getEntityTitleAndDesc(rootKlass).getKey();
//	final String tabCaption = entityTitle + ApplicationModel.RULE_NAME_SUFFIX;
//	RuleResultsTab rootTab = TgSnappyComponentsActivator.findRuleTab(rulesTabbedPane, tabCaption);
//	if (rootTab == null) {
//	    rulesTabbedPane.addTab(tabCaption, rootTab = new RuleResultsTab(rootKlass, getTgApplicationModel()));
//	}
//	final String dotNotName = createDotNotationName(Arrays.copyOfRange(path.getPath(), 2, path.getPath().length));
//	if (dotNotName != null) {
//	    final TitledObject prevTitleObject = (TitledObject) (((DefaultMutableTreeNode) path.getLastPathComponent()).getUserObject());
//	    rootTab.addProperty(new TitledObject(dotNotName, null, null, prevTitleObject.getType()));
//	    rootTab.refreshSkeleton(reinstallOrdering);
//	    rootTab.refreshResult();
//	}
//    }
//
//}
