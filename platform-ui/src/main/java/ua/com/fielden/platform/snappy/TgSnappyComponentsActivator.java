package ua.com.fielden.platform.snappy;

//import java.awt.Color;
//import java.awt.Image;
//import java.awt.Toolkit;
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.Collections;
//import java.util.Enumeration;
//import java.util.List;
//
//import javax.swing.ImageIcon;
//import javax.swing.JFrame;
//import javax.swing.JTabbedPane;
//import javax.swing.JTree;
//import javax.swing.tree.DefaultMutableTreeNode;
//import javax.swing.tree.TreeNode;
//import javax.swing.tree.TreePath;
//
//import org.apache.log4j.Logger;
//
//import ua.com.fielden.platform.pagination.IPage;
//import ua.com.fielden.platform.reflection.Reflector;
//import ua.com.fielden.platform.reflection.TitlesDescsGetter;
//import ua.com.fielden.platform.snappy.RulePropertyCheckingListener.PropertyType;
//import ua.com.fielden.platform.snappy.TgSnappyApplicationPanel.SnappyButtonPanel;
//import ua.com.fielden.platform.swing.components.blocking.BlockingIndefiniteProgressPane;
//import ua.com.fielden.platform.treemodel.EntitiesTreeModel;
//import ua.com.fielden.platform.treemodel.EntitiesTreeModel.TitledObject;
//import ua.com.fielden.platform.utils.Pair;
//import ua.com.fielden.snappy.Result;
//import ua.com.fielden.snappy.Result.ResultState;
//import ua.com.fielden.snappy.pswing.PSwingCanvas;
//import ua.com.fielden.snappy.storing.ComponentsActivator;
//import ua.com.fielden.snappy.ui.StatusBar;
//import ua.com.fielden.snappy.view.block.BlockNode;
//import ua.com.fielden.snappy.view.block.Slot;
//import ua.com.fielden.snappy.view.blocks.properties.PropertyBlock;
//import ua.com.fielden.snappy.view.blocks.properties.PropertyHotBlock;
//import ua.com.fielden.snappy.view.blocks.properties.PropertyNamed;
//import ua.com.fielden.snappy.view.blocks.toplevel.ConditionedHotBlock;
//import ua.com.fielden.snappy.view.blocks.toplevel.RuleBlock;
//import edu.umd.cs.piccolo.PLayer;

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
public class TgSnappyComponentsActivator {}
//implements ComponentsActivator<TgSnappyApplicationModel> {
//
//    private final Logger logger = Logger.getLogger(this.getClass());
//
//    public TgSnappyComponentsActivator(final JFrame mainFrame, final PSwingCanvas canvas, final SnappyEntitiesTree entitiesTree, final StatusBar statusBar, final JTabbedPane rulesTabbedPane, final SnappyButtonPanel buttonPanel, final BlockingIndefiniteProgressPane blockingIndefiniteProgressPane) {
//	this.mainFrame = mainFrame;
//	this.canvas = canvas;
//	this.nodeLayer = canvas.getLayer();
//	this.entitiesTree = entitiesTree;
//	this.statusBar = statusBar;
//	this.rulesTabbedPane = rulesTabbedPane;
//
//	this.buttonPanel = buttonPanel;
//	this.blockingIndefiniteProgressPane = blockingIndefiniteProgressPane;
//    }
//
//    private static final Image successIcon = Toolkit.getDefaultToolkit().getImage(Thread.currentThread().getContextClassLoader().getResource("images/snappy/success-icon.png"));
//    private static final Image failureIcon = Toolkit.getDefaultToolkit().getImage(Thread.currentThread().getContextClassLoader().getResource("images/snappy/failure-icon.png"));
//    private static final Image warningIcon = Toolkit.getDefaultToolkit().getImage(Thread.currentThread().getContextClassLoader().getResource("images/snappy/warning-icon.png"));
//
//    private final PSwingCanvas canvas;
//    private final PLayer nodeLayer;
//
//    private final JFrame mainFrame;
//
//    private final SnappyEntitiesTree entitiesTree;
//    private final StatusBar statusBar;
//
//    private final JTabbedPane rulesTabbedPane;
//    private final SnappyButtonPanel buttonPanel;
//
//    private final BlockingIndefiniteProgressPane blockingIndefiniteProgressPane;
//
//    @Override
//    public void activateAll() {
//	entitiesTree.setEnabled(true);
//    }
//
//    @Override
//    public void enableButtons(final boolean enable) {
//	buttonPanel.getOpenNewButton().setEnabled(true);
//
//	buttonPanel.getSaveButton().setEnabled(enable);
//	buttonPanel.getReloadRulesButton().setEnabled(enable);
//	buttonPanel.getDiscardButton().setEnabled(enable);
//	buttonPanel.getRunButton().setEnabled(enable);
//
//	buttonPanel.getOpenNewButton().requestFocus();
//    }
//
//    @Override
//    public void changeStatus(final String statusText) {
//	System.out.println(statusText);
//	statusBar.setText(statusText);
//    }
//
//    @Override
//    public void changeTitle(final String newTitle) {
//	if (mainFrame != null) {
//	    mainFrame.setTitle(newTitle);
//	}
//    }
//
//    @Override
//    public String getTitle() {
//	return mainFrame == null ? "" : mainFrame.getTitle();
//    }
//
//    @Override
//    public void clearCanvas() {
//	nodeLayer.removeAllChildren();
//	canvas.setBackground(Color.WHITE);
//    }
//
//    @Override
//    public void clearResults() {
//	rulesTabbedPane.removeAll();
//    }
//
//    @Override
//    public void showResult(final Result result, final String ruleName) {
//	final RuleResultsTab ruleComponent = findRuleTab(rulesTabbedPane, ruleName);
//	if (ruleComponent == null) {
//	    throw new RuntimeException("Rules results tab for rule [" + ruleName + "] does not exist.");
//	} else {
//	    rulesTabbedPane.setIconAt(findRuleTabIndex(rulesTabbedPane, ruleName), new ImageIcon((result.state() == ResultState.SUCCESSED ? successIcon
//		    : (result.state() == ResultState.FAILED ? failureIcon : warningIcon))));
//	    ruleComponent.setResult(result);
//	    ruleComponent.refreshResult();
//	}
//    }
//
//    public void showResult(final Result result, final IPage page, final String ruleName) {
//	final RuleResultsTab ruleComponent = findRuleTab(rulesTabbedPane, ruleName);
//	if (ruleComponent == null) {
//	    throw new RuntimeException("Rules results tab for rule [" + ruleName + "] does not exist.");
//	} else {
//	    rulesTabbedPane.setIconAt(findRuleTabIndex(rulesTabbedPane, ruleName), new ImageIcon((result.state() == ResultState.SUCCESSED ? successIcon
//		    : (result.state() == ResultState.FAILED ? failureIcon : warningIcon))));
//	    ruleComponent.setResult(result);
//	    ruleComponent.setFirstPage(page);
//	    ruleComponent.refreshResult(true);
//	}
//    }
//
//    public void showFailure(final String ruleName) {
//	final RuleResultsTab ruleComponent = findRuleTab(rulesTabbedPane, ruleName);
//	if (ruleComponent == null) {
//	    throw new RuntimeException("Rules results tab for rule [" + ruleName + "] does not exist.");
//	} else {
//	    rulesTabbedPane.setIconAt(findRuleTabIndex(rulesTabbedPane, ruleName), new ImageIcon(warningIcon));
//	    ruleComponent.setResult(null);
//	    ruleComponent.removeAll();
//	}
//    }
//
//    public static int findRuleTabIndex(final JTabbedPane rulesTabbedPane, final String ruleName) {
//	for (int i = 0; i < rulesTabbedPane.getTabCount(); i++) {
//	    if (rulesTabbedPane.getTitleAt(i).equals(ruleName)) {
//		return i;
//	    }
//	}
//	return -1;
//    }
//
//    public static RuleResultsTab findRuleTab(final JTabbedPane rulesTabbedPane, final String ruleName) {
//	final int i = findRuleTabIndex(rulesTabbedPane, ruleName);
//	return (i == -1) ? null : (RuleResultsTab) rulesTabbedPane.getComponentAt(i);
//    }
//
//    /**
//     * Clears results table skeleton.
//     */
//    public void clearSkeletons() {
//	for (int i = 0; i < rulesTabbedPane.getTabCount(); i++) {
//	    final String title = rulesTabbedPane.getTitleAt(i);
//	    if (ruleBlockExists(title)) {
//		((RuleResultsTab) rulesTabbedPane.getComponentAt(i)).removeAllProperties();
//	    } else {
//		rulesTabbedPane.removeTabAt(i);
//	    }
//	}
//    }
//
//    private boolean ruleBlockExists(final String name) {
//	for (int i = 0; i < getNodeLayer().getChildrenCount(); i++) {
//	    if (getNodeLayer().getChild(i) instanceof RuleBlock) {
//		final RuleBlock<?> ruleBlock = (RuleBlock<?>) getNodeLayer().getChild(i);
//		if (name.equals(ruleBlock.getCaption())) {
//		    return true;
//		}
//	    }
//	}
//	return false;
//    }
//
//    /**
//     * Refreshes results table skeleton and fill results.
//     */
//    public void refreshSkeletonsAndResults() {
//	// refreshes results table skeleton and fill results :
//	for (int i = 0; i < rulesTabbedPane.getTabCount(); i++) {
//	    ((RuleResultsTab) rulesTabbedPane.getComponentAt(i)).refreshSkeleton(true);
//	    ((RuleResultsTab) rulesTabbedPane.getComponentAt(i)).refreshResult();
//	}
//    }
//
//    public void synchronizeAllInView(final Pair<List<TreePath>, List<TreePath>> allTreePaths) {
//	// "conditioned" paths synchronization (first checkbox)
//	entitiesTree.removeTreeCheckingListener(entitiesTree.getRulePropertyCheckingListener(), 0);
//	for (final TreePath path : allTreePaths.getKey()) {
//	    if (path != null) {
//		entitiesTree.addCheckingPath(path, 0);
//	    }
//	}
//	entitiesTree.addTreeCheckingListener(entitiesTree.getRulePropertyCheckingListener(), 0);
//
//	// "fetching" paths synchronization (second checkbox)
//	entitiesTree.removeTreeCheckingListener(entitiesTree.getResultPropertyCheckingListener(), 1);
//	for (final TreePath path : allTreePaths.getValue()) {
//	    if (path != null) {
//		entitiesTree.addCheckingPath(path, 1);
//		entitiesTree.getResultPropertyCheckingListener().addPathColumn1(path, false);
//	    }
//	}
//	entitiesTree.addTreeCheckingListener(entitiesTree.getResultPropertyCheckingListener(), 1);
//    }
//
//    private static List<String> createStrings(final TreePath path) {
//	final List<String> strings = new ArrayList<String>();
//	for (int i = 0; i < path.getPathCount(); i++) {
//	    strings.add(((TitledObject) ((DefaultMutableTreeNode) path.getPathComponent(i)).getUserObject()).getTitle());
//	}
//	return strings;
//    }
//
//    public List<TreePath> synchronize(final RuleBlock<?> ruleBlock, final TgSnappyApplicationModel applicationModel, final PropertyType propertyType) {
//	final List<TreePath> treePaths = new ArrayList<TreePath>();
//	if (ruleBlock.model() != null) {
//	    final Class<?> ruleKlass;
//	    try {
//		ruleKlass = Class.forName(ruleBlock.model().hots().get(0).fullCorrespondingClassName());
//	    } catch (final ClassNotFoundException e) {
//		e.printStackTrace();
//		throw new RuntimeException(e);
//	    }
//	    applicationModel.putExistingRuleBlock(ruleKlass, ruleBlock);
//
//	    final List<List<String>> pathsByNames = pathsForHot(Arrays.asList(EntitiesTreeModel.ROOT_CAPTION, TitlesDescsGetter.getEntityTitleAndDesc(ruleKlass).getKey()), ruleBlock.slots().get(0).block().slots(), propertyType);
//	    for (final List<String> pathByName : pathsByNames) {
//		logger.debug("PathByName = " + pathByName);
//
//		TreePath path = findByName(entitiesTree, pathByName.toArray());
//		if (path == null) {// possibly corresponding node was not loaded (e.g. Vehicle.replacing.DUMMY_TITLED_OBJECT was loaded, but Vehicle.replacing.replacedBy.status should be selected)
//		    entitiesTree.getEntitiesTreeModel().load(pathByName);
//		    path = findByName(entitiesTree, pathByName.toArray());
//		}
//		if (path == null) {
//		    throw new IllegalArgumentException("Could not find tree path for " + pathByName + ".");
//		}
//		treePaths.add(path);
//	    }
//	}
//	return treePaths;
//    }
//
//    /**
//     * Returns the list of paths in "list of propertyNames" representation.
//     *
//     * @param jointPartPath
//     *            - the prefix of all paths.
//     * @param slots
//     *            - the slots which will be traversed and checked for property or propertyHot to find corresponding path or paths.
//     * @return
//     */
//    private static List<List<String>> pathsForHot(final List<String> jointPartPath, final List<? extends Slot> slots, final PropertyType propertyType) {
//	final List<List<String>> paths = new ArrayList<List<String>>();
//	for (final Slot slot : slots) {
//	    if (slot.block() != null && (slot.block() instanceof PropertyNamed)) {
//		final String propertyName = ((PropertyNamed) slot.block()).getPropertyName();
//		final List<String> newJointPart = new ArrayList<String>(jointPartPath);
//		newJointPart.add(propertyName);
//		if (slot.block() instanceof PropertyHotBlock<?>) {
//		    final PropertyHotBlock<?> propertyHotBlock = (PropertyHotBlock<?>) slot.block();
//		    if (PropertyType.CONDITIONED == propertyType && !propertyHotBlock.isPossiblyFetching() || //
//			    PropertyType.FETCHING == propertyType && propertyHotBlock.isPossiblyFetching() && propertyHotBlock.isFetching()) {
//			paths.add(newJointPart);
//		    }
//		    paths.addAll(pathsForHot(newJointPart, slot.block().slots(), propertyType));
//		} else if (slot.block() instanceof PropertyBlock<?>) {
//		    final PropertyBlock<?> propertyBlock = (PropertyBlock<?>) slot.block();
//		    if (PropertyType.CONDITIONED == propertyType && !propertyBlock.isFetchingProperty() || //
//			    PropertyType.FETCHING == propertyType && propertyBlock.isFetchingProperty()) {
//			paths.add(newJointPart);
//		    }
//		}
//	    }
//	}
//	return paths;
//    }
//
//    private static List<String> reversedPathFor(final BlockNode<?> block, final List<String> partPath) {
//	final List<String> path = new ArrayList<String>(partPath);
//	if (block instanceof PropertyNamed) {
//	    path.add(((PropertyNamed) block).getPropertyName());
//	    if (block.getSlot() != null) {
//		return reversedPathFor(block.getSlot().parent(), path);
//	    } else {
//		return path;
//	    }
//	} else if (block instanceof ConditionedHotBlock) {
//	    final String conditionedHotTitle;
//	    try {
//		conditionedHotTitle = TitlesDescsGetter.getEntityTitleAndDesc(Class.forName(((ConditionedHotBlock<?>) block).getFullCorrespondingClassName())).getKey();
//	    } catch (final ClassNotFoundException e) {
//		e.printStackTrace();
//		throw new RuntimeException(e);
//	    }
//	    path.add(conditionedHotTitle);
//	    return path;
//	} else {
//	    return path;
//	}
//    }
//
//    /**
//     * Constructs string path for "block".
//     *
//     * @param block
//     * @return
//     */
//    private static List<String> pathFor(final BlockNode<?> block) {
//	final List<String> path = reversedPathFor(block, new ArrayList<String>());
//	path.add(EntitiesTreeModel.ROOT_CAPTION);
//	Collections.reverse(path);
//	return path;
//    }
//
//    /**
//     * Constructs string path for "dotNotatedName" and "klass".
//     *
//     * @param block
//     * @return
//     */
//    private static List<String> pathFor(final String dotNotation, final Class<?> klass) {
//	final List<String> path = new ArrayList<String>();
//	path.add(EntitiesTreeModel.ROOT_CAPTION);
//	path.add(TitlesDescsGetter.getEntityTitleAndDesc(klass).getKey());
//	path.addAll(Arrays.asList(dotNotation.split(Reflector.DOT_SPLITTER)));
//	return path;
//    }
//
//    /**
//     * Finds entities tree node that corresponds to dotNotation property name and root klass.
//     *
//     * @param dotNotation
//     * @param klass
//     */
//    public void locatePropertyInTree(final String dotNotation, final Class<?> klass) {
//	locatePropertyInTree(pathFor(dotNotation, klass).toArray());
//    }
//
//    /**
//     * Finds entities tree node that corresponds to "blockToLocate".
//     *
//     * @param blockToLocate
//     */
//    public void locatePropertyInTree(final BlockNode<?> blockToLocate) {
//	locatePropertyInTree(pathFor(blockToLocate).toArray());
//    }
//
//    /**
//     * Finds entities tree node that corresponds to node names.
//     *
//     * @param blockToLocate
//     */
//    private void locatePropertyInTree(final Object[] names) {
//	entitiesTree.setSelectionPath(findByName(entitiesTree, names));
//	entitiesTree.bringSelectedIntoView();
//	entitiesTree.requestFocus();
//    }
//
//    /**
//     * Finds the tree path corresponding to the array node names.
//     *
//     * @param tree
//     * @param names
//     * @return
//     */
//    private TreePath findByName(final JTree tree, final Object[] names) {
//	return find2(new TreePath(tree.getModel().getRoot()), names, 0, true);
//    }
//
//    private TreePath find2(final TreePath parent, final Object[] nodes, final int depth, final boolean byName) {
//	final DefaultMutableTreeNode node = (DefaultMutableTreeNode) parent.getLastPathComponent();
//	Object o = node;
//
//	if (node.getUserObject() instanceof TitledObject) {
//	    final TitledObject to = (TitledObject) node.getUserObject();
//	    if (to.getObject() != null && !(to.getObject() instanceof Class<?>)) {
//		o = ((TitledObject) node.getUserObject()).getObject();
//	    }
//	}
//
//	// If by name, convert node to a string
//	if (byName) {
//	    o = o.toString();
//	}
//
//	// If equal, go down the branch
//	if (o.equals(nodes[depth])) {
//	    // If at end, return match
//	    if (depth == nodes.length - 1) {
//		return parent;
//	    }
//
//	    // Traverse children
//	    if (node.getChildCount() >= 0) {
//		for (final Enumeration e = node.children(); e.hasMoreElements();) {
//		    final TreeNode n = (TreeNode) e.nextElement();
//		    final TreePath path = parent.pathByAddingChild(n);
//		    final TreePath result = find2(path, nodes, depth + 1, byName);
//		    // Found a match
//		    if (result != null) {
//			return result;
//		    }
//		}
//	    }
//	}
//	// No match at this branch
//	return null;
//    }
//
//    @Override
//    public PLayer getNodeLayer() {
//	return this.nodeLayer;
//    }
//
//    @Override
//    public void clearEntitiesTree() {
//	entitiesTree.removeTreeCheckingListener(entitiesTree.getRulePropertyCheckingListener(), 0);
//	entitiesTree.removeTreeCheckingListener(entitiesTree.getResultPropertyCheckingListener(), 1);
//	entitiesTree.clearChecking(0);
//	entitiesTree.clearChecking(1);
//	entitiesTree.addTreeCheckingListener(entitiesTree.getRulePropertyCheckingListener(), 0);
//	entitiesTree.addTreeCheckingListener(entitiesTree.getResultPropertyCheckingListener(), 1);
//    }
//
//    public BlockingIndefiniteProgressPane getBlockingIndefiniteProgressPane() {
//	return blockingIndefiniteProgressPane;
//    }
//}
