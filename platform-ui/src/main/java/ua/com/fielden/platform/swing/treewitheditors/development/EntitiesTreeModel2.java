package ua.com.fielden.platform.swing.treewitheditors.development;

import it.cnr.imaa.essi.lablib.gui.checkboxtree.DefaultTreeCheckingModel;
import it.cnr.imaa.essi.lablib.gui.checkboxtree.TreeCheckingEvent;
import it.cnr.imaa.essi.lablib.gui.checkboxtree.TreeCheckingListener;
import it.cnr.imaa.essi.lablib.gui.checkboxtree.TreeCheckingModel;
import it.cnr.imaa.essi.lablib.gui.checkboxtree.TreeCheckingModel.CheckingMode;

import java.util.Arrays;
import java.util.List;

import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.tree.ExpandVetoException;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import ua.com.fielden.platform.domaintree.IDomainTreeManager.ChangedAction;
import ua.com.fielden.platform.domaintree.IDomainTreeManager.IDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.IDomainTreeManager.IPropertyStructureChangedListener;
import ua.com.fielden.platform.domaintree.IDomainTreeManager.ITickManager;
import ua.com.fielden.platform.domaintree.impl.AbstractDomainTree;
import ua.com.fielden.platform.domaintree.impl.EnhancementPropertiesMap;
import ua.com.fielden.platform.reflection.PropertyTypeDeterminator;
import ua.com.fielden.platform.reflection.TitlesDescsGetter;
import ua.com.fielden.platform.swing.dynamicreportstree.EntitiesTreeColumn;
import ua.com.fielden.platform.utils.Pair;

/**
 * A new tree model implementation for the 'entities tree' relying on {@link IDomainTreeManagerAndEnhancer}.
 *
 * @author TG Team
 *
 */
public class EntitiesTreeModel2 extends MultipleCheckboxTreeModel2 {
    private static final long serialVersionUID = -5156365765004770688L;
    public static final String ROOT_PROPERTY = "entities-root";

    private final IDomainTreeManagerAndEnhancer manager;
    private final EntitiesTreeNode2 rootNode;
    private final EnhancementPropertiesMap<EntitiesTreeNode2> nodesCache;
    private final EnhancementPropertiesMap<EntitiesTreeNode2> nodesForSimplePropertiesCache;
    private final TreeCheckingListener [] listeners;
    private final Logger logger = Logger.getLogger(getClass());

    /**
     * Creates a new tree model for the 'entities tree' relying on {@link IDomainTreeManagerAndEnhancer}.
     *
     * @param manager
     */
    public EntitiesTreeModel2(final IDomainTreeManagerAndEnhancer manager) {
	super(2);

	getCheckingModel(EntitiesTreeColumn.CRITERIA_COLUMN.getColumnIndex()).setCheckingMode(CheckingMode.SIMPLE);
	getCheckingModel(EntitiesTreeColumn.TABLE_HEADER_COLUMN.getColumnIndex()).setCheckingMode(CheckingMode.SIMPLE);

	this.manager = manager;
	this.listeners = new TreeCheckingListener [] { createTreeCheckingListener(this.manager.getFirstTick()), createTreeCheckingListener(this.manager.getSecondTick()) };
	this.rootNode = new EntitiesTreeNode2(createUserObject(EntitiesTreeModel2.class, ROOT_PROPERTY));
	setRoot(this.rootNode);
	this.nodesCache = AbstractDomainTree.createPropertiesMap();
	this.nodesForSimplePropertiesCache = AbstractDomainTree.createPropertiesMap();
	// initialise nodes according to included properties of the manager (these include "dummy" and "common properties" stuff)
	for (final Class<?> root : manager.getRepresentation().rootTypes()) {
	    final List<String> properties = manager.getRepresentation().includedProperties(root);
	    for (final String property : properties) {
		createAndAddNode(root, property);
		updateNodeState(manager, root, property, ChangedAction.ADDED);
	    }
	}

	// add the listener into manager to correctly reflect structural changes (property added / removed / checked / disabled / etc.) in this EntitiesTreeModel
	final IPropertyStructureChangedListener managerListener = new IPropertyStructureChangedListener() {
	    @Override
	    public void propertyStructureChanged(final Class<?> root, final String property, final ChangedAction changedAction) {
		if (ChangedAction.REMOVED.equals(changedAction)) {
		    removeNode(root, property);
		} else if (ChangedAction.ADDED.equals(changedAction)) {
		    createAndAddNode(root, property);
		}
		updateNodeState(manager, root, property, changedAction);
	    }
	};
	this.manager.addPropertyStructureChangedListener(managerListener);

	// Added tree checking listeners those listen tree node checking events and add checked property to the appropriate manager.
	addTreeCheckingListener(listeners[0], 0);
	addTreeCheckingListener(listeners[1], 1);
    }

    private void createAndAddNode(final Class<?> root, final String property) {
	final EntitiesTreeNode2 parentNode = StringUtils.isEmpty(property) ? rootNode //
		: !PropertyTypeDeterminator.isDotNotation(property) ? node(root, "") //
			: node(root, PropertyTypeDeterminator.penultAndLast(property).getKey());
	final EntitiesTreeNode2 node = new EntitiesTreeNode2(createUserObject(root, property));
	nodesCache.put(AbstractDomainTree.key(root, property), node);
	if (!AbstractDomainTree.isDummyMarker(property)) {
	    nodesForSimplePropertiesCache.put(AbstractDomainTree.key(root, AbstractDomainTree.reflectionProperty(property)), node);
	}
	parentNode.add(node);
    }

    private void removeNode(final Class<?> root, final String property) {
	node(root, property).removeFromParent();
	nodesCache.remove(AbstractDomainTree.key(root, property));
	if (!AbstractDomainTree.isDummyMarker(property)) {
	    nodesForSimplePropertiesCache.remove(AbstractDomainTree.key(root, AbstractDomainTree.reflectionProperty(property)));
	}
    }

    private void provideCheckingPath(final TreeCheckingModel model, final TreePath path, final boolean checked) {
	final List<TreePath> currentPaths = Arrays.asList(model.getCheckingPaths());
	if (checked) {
	    if (!currentPaths.contains(path)) {
		model.addCheckingPath(path);
	    } else {
		logger.warn("Currently checked path [" + path + "] is trying to be checked again.");
	    }
	} else {
	    if (!currentPaths.contains(path)) {
		logger.warn("Currently unchecked path [" + path + "] is trying to be unchecked again.");
	    } else {
		model.removeCheckingPath(path);
	    }
	}
    }

    private void updateNodeState(final IDomainTreeManagerAndEnhancer manager, final Class<?> root, final String property, final ChangedAction changedAction) {
	if (ChangedAction.REMOVED.equals(changedAction)) { // do nothing with an useless item
	    return;
	} else if (ChangedAction.ADDED.equals(changedAction)) { // in this case property can be "dummy" or under "common-properties" umbrella
	    if (!AbstractDomainTree.isDummyMarker(property)) { // Update the state of newly created node according to a property state in manager (ignore "dummy" due to its temporal nature)

		// TODO remove two ticks for "common-property" and second tick for uncommon union properties

		final EntitiesTreeNode2 node = node(root, property);
		provideNodeState(manager, root, AbstractDomainTree.reflectionProperty(property), new TreePath(getPathToRoot(node)));
	    }
	} else if (ChangedAction.ENABLEMENT_OR_CHECKING_CHANGED.equals(changedAction)) {
	    final EntitiesTreeNode2 node = nodeForSimpleProperty(root, property);
	    provideNodeState(manager, root, property, new TreePath(getPathToRoot(node)));
	}
    }

    private void provideNodeState(final IDomainTreeManagerAndEnhancer manager, final Class<?> root, final String property, final TreePath path) {
	final boolean firstChecked = manager.getFirstTick().isChecked(root, property);
	final boolean secondChecked = manager.getSecondTick().isChecked(root, property);
	final boolean firstDisabled = manager.getRepresentation().getFirstTick().isDisabledImmutably(root, property);
	final boolean secondDisabled = manager.getRepresentation().getSecondTick().isDisabledImmutably(root, property);

	final int firstIndex = EntitiesTreeColumn.CRITERIA_COLUMN.getColumnIndex();
	final int secondIndex = EntitiesTreeColumn.TABLE_HEADER_COLUMN.getColumnIndex();

	provideCheckingPath(getCheckingModel(firstIndex), path, firstChecked);
	provideCheckingPath(getCheckingModel(secondIndex), path, secondChecked);
	getCheckingModel(firstIndex).setPathEnabled(path, !firstDisabled);
	getCheckingModel(secondIndex).setPathEnabled(path, !secondDisabled);
    }

    /**
     * A {@link DefaultTreeCheckingModelWithoutLosingChecking} with listeners removal (to correctly perform check/uncheck) when {@link #setTreeModel(TreeModel)} invokes.
     *
     * @author TG Team
     *
     */
    protected class DefaultTreeCheckingModelWithoutLosingCheckingWithListenersRemoval extends DefaultTreeCheckingModelWithoutLosingChecking {
	private final int index;

	public DefaultTreeCheckingModelWithoutLosingCheckingWithListenersRemoval(final TreeModel model, final int index) {
	    super(model);
	    this.index = index;
	}

	@Override
	public void setTreeModel(final TreeModel newModel) {
	    if (listeners != null) {
		EntitiesTreeModel2.this.removeTreeCheckingListener(listeners[index], index);
	    }
	    super.setTreeModel(newModel);
	    if (listeners != null) {
		EntitiesTreeModel2.this.addTreeCheckingListener(listeners[index], index);
	    }
	}
    }

    /**
     * Creates a {@link DefaultTreeCheckingModelWithoutLosingChecking} with listeners removal (to correctly perform check/uncheck) when {@link #setTreeModel(TreeModel)} invokes.
     */
    @Override
    protected DefaultTreeCheckingModel createCheckingModel(final int index) {
	return new DefaultTreeCheckingModelWithoutLosingCheckingWithListenersRemoval(this, index);
    }

    private TreeCheckingListener createTreeCheckingListener(final ITickManager tickManager) {
	return new TreeCheckingListener() {
	    @Override
	    public void valueChanged(final TreeCheckingEvent e) {
		// TODO should be rectified (dummy, common-properties etc)
		// TODO should be rectified (dummy, common-properties etc)
		// TODO should be rectified (dummy, common-properties etc)
		// TODO should be rectified (dummy, common-properties etc)
		final EntitiesTreeNode2 node = (EntitiesTreeNode2) e.getPath().getLastPathComponent();
		final Pair<Class<?>, String> userObject = node.getUserObject();
		final Class<?> root = userObject.getKey();
		final String property = AbstractDomainTree.reflectionProperty(userObject.getValue());
		if(!isRoot(node)){
		    tickManager.check(root, property, e.isCheckedPath());
		}
	    }
	};
    }

    private boolean isRoot(final EntitiesTreeNode2 node){
	return EntitiesTreeModel2.ROOT_PROPERTY.equals(node.getUserObject().getValue());
    }

    /**
     * Finds a node corresponding to a property.
     *
     * @param root
     * @param property
     * @return
     */
    private EntitiesTreeNode2 node(final Class<?> root, final String property) {
	return nodesCache.get(AbstractDomainTree.key(root, property));
    }

    /**
     * Finds a node corresponding to a simple property (no "dummy" or "common-properties" stuff).
     *
     * @param root
     * @param property
     * @return
     */
    private EntitiesTreeNode2 nodeForSimpleProperty(final Class<?> root, final String property) {
	return nodesForSimplePropertiesCache.get(AbstractDomainTree.key(root, property));
    }

    /**
     * Creates a {@link TreeWillExpandListener} that "warms up" the manager's property (loads children), which node is trying to be expanded.
     *
     * @return
     */
    public TreeWillExpandListener createTreeWillExpandListener() {
	return new TreeWillExpandListener() {
	    @Override
	    public void treeWillExpand(final TreeExpansionEvent event) throws ExpandVetoException {
		final EntitiesTreeNode2 node = (EntitiesTreeNode2) event.getPath().getLastPathComponent();
		final Pair<Class<?>, String> rootAndProp = node.getUserObject();
		if (!rootNode.equals(node)) {
		    manager.getRepresentation().warmUp(rootAndProp.getKey(), rootAndProp.getValue());
		}
	    }

	    @Override
	    public void treeWillCollapse(final TreeExpansionEvent event) throws ExpandVetoException {
	    }
	};
    }

    /**
     * Extracts title / desc from node (if "desc" == true then extracts description otherwise - title).
     *
     * @param treeNode
     * @param isDesc
     * @return
     */
    public static Pair<String, String> extractTitleAndDesc(final Class<?> root, final String property) {
	final String title, desc;
	if (EntitiesTreeModel2.ROOT_PROPERTY.equals(property)) { // root node
	    title = "Entities";
	    desc = "<b>Available entities</b>";
	} else if (AbstractDomainTree.isCommonBranch(property)) { // common group
	    title = "Common";
	    desc = TitlesDescsGetter.italic("<b>Common properties</b>");
	} else { // entity node
	    final Pair<String, String> tad = "".equals(property) ? TitlesDescsGetter.getEntityTitleAndDesc(root) : TitlesDescsGetter.getTitleAndDesc(AbstractDomainTree.reflectionProperty(property), root);
	    title = tad.getKey();
	    desc = TitlesDescsGetter.italic("<b>" + tad.getValue() + "</b>");
	}
	return new Pair<String, String>(title, desc);
    }

    /**
     * Returns the {@link IDomainTreeManagerAndEnhancer} instance associated with this {@link EntitiesTreeModel2}.
     *
     * @return
     */
    public IDomainTreeManagerAndEnhancer getManager() {
	return manager;
    }

//    /**
//     * Creates a new node for the property and adds it to the appropriate place on the hierarchy.
//     * <p>
//     * The parent is determined from property dot-notation name and the property will be added to the end of parent's children list (so the order of adding is important).
//     *
//     * @param root
//     * @param property
//     */
//    protected void addNode(final Class<?> root, final String property) {
//	if (property == null) {
//	    throw new IllegalArgumentException();
//	}
//	final EntitiesTreeNode2 parentNode = "".equals(property) ? rootNode : (PropertyTypeDeterminator.isDotNotation(property) ? node(root, PropertyTypeDeterminator.penultAndLast(property).getKey()) : node(root, ""));
//
//	final EntitiesTreeNode2 node = new EntitiesTreeNode2(createUserObject(root, property));
//	nodesCache.put(AbstractDomainTree.key(root, property), node);
//	parentNode.add(node);
//
//	// TODO remove two ticks for "common-property" and second tick for uncommon union properties
//	final TreePath path = new TreePath(getPathToRoot(node));
//	final String reflectionProperty = AbstractDomainTree.reflectionProperty(property);
//	if (!AbstractDomainTree.isDummyMarker(property) && manager.getFirstTick().isChecked(root, reflectionProperty)) {
//	    getCheckingModel(EntitiesTreeColumn.CRITERIA_COLUMN.getColumnIndex()).addCheckingPath(path);
//	}
//	if (!AbstractDomainTree.isDummyMarker(property) && manager.getSecondTick().isChecked(root, reflectionProperty)) {
//	    getCheckingModel(EntitiesTreeColumn.TABLE_HEADER_COLUMN.getColumnIndex()).addCheckingPath(path);
//	}
//	if (!AbstractDomainTree.isDummyMarker(property) && manager.getRepresentation().getFirstTick().isDisabledImmutably(root, reflectionProperty)) {
//	    getCheckingModel(EntitiesTreeColumn.CRITERIA_COLUMN.getColumnIndex()).setPathEnabled(path, false);
//	}
//	if (!AbstractDomainTree.isDummyMarker(property) && manager.getRepresentation().getSecondTick().isDisabledImmutably(root, reflectionProperty)) {
//	    getCheckingModel(EntitiesTreeColumn.TABLE_HEADER_COLUMN.getColumnIndex()).setPathEnabled(path, false);
//	}
//    }

    private Pair<Class<?>, String> createUserObject(final Class<?> root, final String property) {
	return new Pair<Class<?>, String>(root, property) {
	    private static final long serialVersionUID = -7106027050288695731L;

	    @Override
	    public String toString() {
		return extractTitleAndDesc(getManager().getEnhancer().getManagedType(root), property).getKey();
	    }
	};
    }
}
