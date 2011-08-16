package ua.com.fielden.platform.treemodel.rules;

import java.util.List;
import java.util.Map;

import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.ExpandVetoException;

import ua.com.fielden.platform.reflection.PropertyTypeDeterminator;
import ua.com.fielden.platform.treemodel.rules.IDomainTreeManager.IDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.treemodel.rules.IDomainTreeRepresentation.IStructureChangedListener;
import ua.com.fielden.platform.treemodel.rules.impl.AbstractDomainTree;
import ua.com.fielden.platform.utils.Pair;

/**
 * A new tree model implementation for the EntitiesTree relying on {@link IDomainTreeManagerAndEnhancer}.
 *
 * @author TG Team
 *
 */
public class EntitiesTreeModel2 extends DefaultTreeModel {
    private static final long serialVersionUID = -5156365765004770688L;
    private static final String /* ROOT_CAPTION = "Entities",*/ ROOT_PROPERTY = "entities-root";
    // private final Logger logger = Logger.getLogger(this.getClass());

    private final IDomainTreeManagerAndEnhancer manager;
    private final DefaultMutableTreeNode rootNode;
    private final Map<Pair<Class<?>, String>, DefaultMutableTreeNode> nodesCache;

    public EntitiesTreeModel2(final IDomainTreeManagerAndEnhancer manager) {
	super(null);
	this.manager = manager;
	this.rootNode = new DefaultMutableTreeNode(new Pair<Class<?>, String>(EntitiesTreeModel2.class, ROOT_PROPERTY));
	setRoot(rootNode);

	nodesCache = AbstractDomainTree.createPropertiesMap();
	for (final Class<?> root : manager.getRepresentation().rootTypes()) {
	    addNode(root, "");

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

    private void addNode(final Class<?> root, final String property) {
	if (property == null) {
	    throw new IllegalArgumentException();
	}
	final DefaultMutableTreeNode parentNode = "".equals(property) ? rootNode : (PropertyTypeDeterminator.isDotNotation(property) ? node(root, PropertyTypeDeterminator.penultAndLast(property).getKey()) : node(root, ""));

	final DefaultMutableTreeNode node = new DefaultMutableTreeNode(new Pair<Class<?>, String>(root, property));
	nodesCache.put(AbstractDomainTree.key(root, property), node);
	parentNode.add(node);
	// TODO PROPERTY titles
	// final Pair<String, String> tad = TitlesDescsGetter.getTitleAndDesc(field.getName(), clazz);
	// final Pair<String, String> titleAndDesc = new Pair<String, String>(tad.getKey(), TitlesDescsGetter.italic("<b>" + tad.getValue() + "</b>"));
	// new TitledObject(field.getName(), titleAndDesc.getKey(), titleAndDesc.getValue(), propertyType)

	// TODO ENTITY titles
	// new TitledObject(klass, TitlesDescsGetter.getEntityTitleAndDesc(klass).getKey(), TitlesDescsGetter.italic("<b>" + TitlesDescsGetter.getEntityTitleAndDesc(klass).getValue() + "</b>"), klass)
    }

    /**
     * Finds a node corresponding to a property.
     *
     * @param root
     * @param property
     * @return
     */
    private DefaultMutableTreeNode node(final Class<?> root, final String property) {
	return nodesCache.get(AbstractDomainTree.key(root, property));
    }

    public TreeWillExpandListener createTreeWillExpandListener() {
	return new TreeWillExpandListener() {
	    @Override
	    public void treeWillExpand(final TreeExpansionEvent event) throws ExpandVetoException {
		// if (entitiesTreeModel.loadProperties((DefaultMutableTreeNode) event.getPath().getLastPathComponent())) {
		//     TODO 1 ? providePathsEnablement();
		//     TODO 2 ? checkSubtreeFromPath(EntitiesTreeColumn.CRITERIA_COLUMN, event.getPath(), true);
		// }
		final DefaultMutableTreeNode node = (DefaultMutableTreeNode) event.getPath().getLastPathComponent();
		final Pair<Class<?>, String> rootAndProp = (Pair<Class<?>, String>) node.getUserObject();
		if (!rootNode.equals(node)) {
		    manager.getRepresentation().warmUp(rootAndProp.getKey(), rootAndProp.getValue());
		}
	    }

	    @Override
	    public void treeWillCollapse(final TreeExpansionEvent event) throws ExpandVetoException {
	    }
	};
    }
}
