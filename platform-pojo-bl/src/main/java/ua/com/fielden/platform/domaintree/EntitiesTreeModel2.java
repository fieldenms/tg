package ua.com.fielden.platform.domaintree;

import java.util.List;

import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.ExpandVetoException;

import ua.com.fielden.platform.domaintree.IDomainTreeManager.IDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.IDomainTreeRepresentation.IStructureChangedListener;
import ua.com.fielden.platform.domaintree.impl.AbstractDomainTree;
import ua.com.fielden.platform.domaintree.impl.EnhancementPropertiesMap;
import ua.com.fielden.platform.reflection.PropertyTypeDeterminator;
import ua.com.fielden.platform.reflection.TitlesDescsGetter;
import ua.com.fielden.platform.utils.Pair;

/**
 * A new tree model implementation for the 'entities tree' relying on {@link IDomainTreeManagerAndEnhancer}.
 *
 * @author TG Team
 *
 */
public class EntitiesTreeModel2 extends DefaultTreeModel {
    private static final long serialVersionUID = -5156365765004770688L;
    public static final String ROOT_PROPERTY = "entities-root";

    private final IDomainTreeManagerAndEnhancer manager;
    private final EntitiesTreeNode rootNode;
    private final EnhancementPropertiesMap<EntitiesTreeNode> nodesCache;

    /**
     * Creates a new tree model for the 'entities tree' relying on {@link IDomainTreeManagerAndEnhancer}.
     *
     * @param manager
     */
    public EntitiesTreeModel2(final IDomainTreeManagerAndEnhancer manager) {
	super(null);
	this.manager = manager;
	this.rootNode = new EntitiesTreeNode(createUserObject(EntitiesTreeModel2.class, ROOT_PROPERTY));
	setRoot(rootNode);

	nodesCache = AbstractDomainTree.createPropertiesMap();
	for (final Class<?> root : manager.getRepresentation().rootTypes()) {
	    final List<String> properties = manager.getRepresentation().includedProperties(root);
	    for (final String property : properties) {
		addNode(root, property);
	    }
	}

	// add the listener into manager to correctly reflect includedProperties changes in this EntityTreeModel
	final IStructureChangedListener listener = new IStructureChangedListener() {
	    @Override
	    public void propertyRemoved(final Class<?> root, final String property) {
		node(root, property).removeFromParent();
		nodesCache.remove(AbstractDomainTree.key(root, property));
		// TODO send a "structure changed" message to a tree?
	    }

	    @Override
	    public void propertyAdded(final Class<?> root, final String property) {
		addNode(root, property);
		// TODO send a "structure changed" message to a tree?
	    }
	};
	this.manager.getRepresentation().addStructureChangedListener(listener);
    }

    /**
     * Finds a node corresponding to a property.
     *
     * @param root
     * @param property
     * @return
     */
    private EntitiesTreeNode node(final Class<?> root, final String property) {
	return nodesCache.get(AbstractDomainTree.key(root, property));
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
		// if (entitiesTreeModel.loadProperties((DefaultMutableTreeNode) event.getPath().getLastPathComponent())) {
		//     TODO 1 ? providePathsEnablement();
		//     TODO 2 ? checkSubtreeFromPath(EntitiesTreeColumn.CRITERIA_COLUMN, event.getPath(), true);
		// }
		final EntitiesTreeNode node = (EntitiesTreeNode) event.getPath().getLastPathComponent();
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

    /**
     * Creates a new node for the property and adds it to the appropriate place on the hierarchy.
     * <p>
     * The parent is determined from property dot-notation name and the property will be added to the end of parent's children list (so the order of adding is important).
     *
     * @param root
     * @param property
     */
    protected void addNode(final Class<?> root, final String property) {
	if (property == null) {
	    throw new IllegalArgumentException();
	}
	final EntitiesTreeNode parentNode = "".equals(property) ? rootNode : (PropertyTypeDeterminator.isDotNotation(property) ? node(root, PropertyTypeDeterminator.penultAndLast(property).getKey()) : node(root, ""));

	final EntitiesTreeNode node = new EntitiesTreeNode(createUserObject(root, property));
	nodesCache.put(AbstractDomainTree.key(root, property), node);
	parentNode.add(node);

	//////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/////////////// to implement next section - multiple checkbox tree model should be separated /////////////////
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////

	// TODO remove two ticks for "common-property" and second tick for uncommon union properties
	//	if (manager.getFirstTick().isChecked(root, property)) {
	//	    // TODO check a first tick in appropriate node
	//	}
	//	if (manager.getSecondTick().isChecked(root, property)) {
	//	    // TODO check a second tick in appropriate node
	//	}
	//	if (manager.getRepresentation().getFirstTick().isDisabledImmutably(root, property)) {
	//	    // TODO disable a first tick in appropriate node
	//	}
	//	if (manager.getRepresentation().getSecondTick().isDisabledImmutably(root, property)) {
	//	    // TODO disable a second tick in appropriate node
	//	}
    }

    private Pair<Class<?>, String> createUserObject(final Class<?> root, final String property) {
	return new Pair<Class<?>, String>(root, property) {
	    private static final long serialVersionUID = -7106027050288695731L;

	    @Override
	    public String toString() {
		return extractTitleAndDesc(root, property).getKey();
	    }
	};
    }
}
