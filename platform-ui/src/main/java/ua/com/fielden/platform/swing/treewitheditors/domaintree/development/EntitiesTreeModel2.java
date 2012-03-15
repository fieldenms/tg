package ua.com.fielden.platform.swing.treewitheditors.domaintree.development;

import it.cnr.imaa.essi.lablib.gui.checkboxtree.DefaultTreeCheckingModel;
import it.cnr.imaa.essi.lablib.gui.checkboxtree.TreeCheckingEvent;
import it.cnr.imaa.essi.lablib.gui.checkboxtree.TreeCheckingListener;
import it.cnr.imaa.essi.lablib.gui.checkboxtree.TreeCheckingModel;
import it.cnr.imaa.essi.lablib.gui.checkboxtree.TreeCheckingModel.CheckingMode;

import java.util.Arrays;
import java.util.List;

import javax.swing.Action;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.tree.ExpandVetoException;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import ua.com.fielden.platform.domaintree.IDomainTreeManager.IDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.IDomainTreeManager.ITickManager;
import ua.com.fielden.platform.domaintree.IDomainTreeManager.ITickManager.IPropertyCheckingListener;
import ua.com.fielden.platform.domaintree.IDomainTreeRepresentation.IPropertyListener;
import ua.com.fielden.platform.domaintree.IDomainTreeRepresentation.ITickRepresentation.IPropertyDisablementListener;
import ua.com.fielden.platform.domaintree.impl.AbstractDomainTree;
import ua.com.fielden.platform.domaintree.impl.EnhancementPropertiesMap;
import ua.com.fielden.platform.reflection.PropertyTypeDeterminator;
import ua.com.fielden.platform.reflection.TitlesDescsGetter;
import ua.com.fielden.platform.swing.dynamicreportstree.EntitiesTreeColumn;
import ua.com.fielden.platform.swing.menu.filter.FilterableTreeModel;
import ua.com.fielden.platform.swing.menu.filter.WordFilter;
import ua.com.fielden.platform.swing.treewitheditors.development.MultipleCheckboxTreeModel2;
import ua.com.fielden.platform.utils.EntityUtils;
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
    /** A cached map of nodes by its names (includes "dummy" and "common"). */
    private final EnhancementPropertiesMap<EntitiesTreeNode2> nodesCache;
    /** A cached map of nodes by its names (includes only real properties without "dummy" and "common" stuff). */
    private final EnhancementPropertiesMap<EntitiesTreeNode2> nodesForSimplePropertiesCache;
    private final TreeCheckingListener [] listeners;
    private final EntitiesTreeCellRenderer cellRenderer1, cellRenderer2;
    private final FilterableTreeModel filterableModel;
    private final Logger logger = Logger.getLogger(getClass());
    private final String firstTickCaption, secondTickCaption;

    /**
     * Creates a new tree model for the 'entities tree' relying on {@link IDomainTreeManagerAndEnhancer}.
     *
     * @param manager
     * @param firstTickCaption
     *            - the name of area corresponding to 0-check-box to which properties should be added/removed.
     * @param secondTickCaption
     * 		  - the name of area corresponding to 1-check-box to which properties should be added/removed.
     */
    public EntitiesTreeModel2(//
	    final IDomainTreeManagerAndEnhancer manager,//
	    final Action newAction,//
	    final Action editAction,//
	    final Action copyAction,//
	    final Action removeAction,//
	    final String firstTickCaption,//
	    final String secondTickCaption) {
	super(2);

	this.firstTickCaption = firstTickCaption;
	this.secondTickCaption = secondTickCaption;

	this.getCheckingModel(EntitiesTreeColumn.CRITERIA_COLUMN.getColumnIndex()).setCheckingMode(CheckingMode.SIMPLE);
	this.getCheckingModel(EntitiesTreeColumn.TABLE_HEADER_COLUMN.getColumnIndex()).setCheckingMode(CheckingMode.SIMPLE);

	this.manager = manager;
	this.listeners = new TreeCheckingListener [] { createTreeCheckingListener(this.manager.getFirstTick()), createTreeCheckingListener(this.manager.getSecondTick()) };
	this.setRoot(this.rootNode = new EntitiesTreeNode2(createUserObject(EntitiesTreeModel2.class, ROOT_PROPERTY)));
	this.nodesCache = AbstractDomainTree.createPropertiesMap();
	this.nodesForSimplePropertiesCache = AbstractDomainTree.createPropertiesMap();
	this.cellRenderer1 = new EntitiesTreeCellRenderer(this, //
		newAction, editAction, copyAction, removeAction);
	this.cellRenderer2 = new EntitiesTreeCellRenderer(this, //
		newAction, editAction, copyAction, removeAction);

	// initialise nodes according to included properties of the manager (these include "dummy" and "common properties" stuff)
	for (final Class<?> root : this.manager.getRepresentation().rootTypes()) {
	    // load checked properties for both ticks to make ticks fully operational before the user check any property
	    this.manager.getFirstTick().checkedProperties(root);
	    this.manager.getSecondTick().checkedProperties(root);
	    
	    final List<String> properties = this.manager.getRepresentation().includedProperties(root);
	    for (final String property : properties) {
		createAndAddNode(root, property);
		// updateNodeState(manager, root, property, ChangedAction.ADDED);
		if (isNotDummyAndNotCommonProperty(property)) { // Update the state of newly created node according to a property state in manager (ignore "dummy" due to its temporal nature)
		    provideNodeState(this.manager, root, AbstractDomainTree.reflectionProperty(property));
		}
	    }
	}

	// add the listener into manager's representation to correctly reflect 'structural' changes (property added / removed) in this EntitiesTreeModel
	this.manager.getRepresentation().addPropertyListener(new PropertyListener());
	// add two listeners into manager's representation's first and second ticks to correctly reflect 'disablement' changes (property disabled / enabled) in this EntitiesTreeModel
	this.manager.getRepresentation().getFirstTick().addPropertyDisablementListener(new PropertyDisablementListener(EntitiesTreeColumn.CRITERIA_COLUMN.getColumnIndex()));
	this.manager.getRepresentation().getSecondTick().addPropertyDisablementListener(new PropertyDisablementListener(EntitiesTreeColumn.TABLE_HEADER_COLUMN.getColumnIndex()));
	// add two listeners into manager's first and second tick to correctly reflect 'checking' changes (property checked / unchecked) in this EntitiesTreeModel
	this.manager.getFirstTick().addPropertyCheckingListener(new PropertyCheckingListener(EntitiesTreeColumn.CRITERIA_COLUMN.getColumnIndex()));
	this.manager.getSecondTick().addPropertyCheckingListener(new PropertyCheckingListener(EntitiesTreeColumn.TABLE_HEADER_COLUMN.getColumnIndex()));

	// add the listener into EntitiesTreeModel to correctly reflect changes (node checked / unchecked) in its manager
	this.addTreeCheckingListener(listeners[0], 0);
	this.addTreeCheckingListener(listeners[1], 1);

	this.filterableModel = createFilteringModel(this);
    }
    
    /**
     * A listener of manager's representation that correctly reflects 'structural' changes (property added / removed) in this {@link EntitiesTreeModel2}.
     * 
     * @author TG Team
     *
     */
    private class PropertyListener implements IPropertyListener {
	@Override
	public void propertyStateChanged(final Class<?> root, final String property, final Boolean wasAddedOrRemoved, final Boolean oldState) {
	    if (wasAddedOrRemoved) {
		createAndAddNode(root, property);
		// in this case property can be "dummy" or under "common-properties" umbrella
		if (isNotDummyAndNotCommonProperty(property)) { // Update the state of newly created node according to a property state in manager (ignore "dummy" due to its temporal nature)
		    provideNodeState(manager, root, AbstractDomainTree.reflectionProperty(property));
		}
	    } else {
		removeNode(root, property); // nothing to do with an useless item -- just remove it
	    }
	}
    }
    
    /**
     * A listener of manager's representation's tick that correctly reflects 'disablement' changes (property disabled / enabled) in this {@link EntitiesTreeModel2}. 
     * 
     * @author TG Team
     *
     */
    private class PropertyDisablementListener implements IPropertyDisablementListener {
	private final int modelIndex;
	
	/** Creates a listener of manager's representation's tick that correctly reflects 'disablement' changes (property disabled / enabled) in this {@link EntitiesTreeModel2}. */
	public PropertyDisablementListener(final int modelIndex) {
	    this.modelIndex = modelIndex;
	}

	@Override
	public void propertyStateChanged(final Class<?> root, final String property, final Boolean hasBeenDisabled, final Boolean oldState) {
	    getCheckingModel(modelIndex).setPathEnabled(path(root, AbstractDomainTree.reflectionProperty(property)), !hasBeenDisabled);
	}
    }
    
    /**
     * A listener of manager's tick that correctly reflects 'checking' changes (property checked / unchecked) in this {@link EntitiesTreeModel2}. 
     * 
     * @author TG Team
     *
     */
    private class PropertyCheckingListener implements IPropertyCheckingListener {
	private final int modelIndex;
	
	/** Creates a listener of manager's tick that correctly reflects 'checking' changes (property checked / unchecked) in this {@link EntitiesTreeModel2}. */
	public PropertyCheckingListener(final int modelIndex) {
	    this.modelIndex = modelIndex;
	}

	@Override
	public void propertyStateChanged(final Class<?> root, final String property, final Boolean hasBeenChecked, final Boolean oldState) {
	    provideCheckingPath(getCheckingModel(modelIndex), path(root, AbstractDomainTree.reflectionProperty(property)), hasBeenChecked);
	}
    }
    
    /**
     * Finds a path corresponding to a property (without any "dummy" naming).
     *
     * @param root
     * @param property
     * @return
     */
    private TreePath path(final Class<?> root, final String property) {
	return new TreePath(getPathToRoot(node(root, property, false)));
    }

    protected FilterableTreeModel createFilteringModel(final EntitiesTreeModel2 entitiesTreeModel2) {
	// wrap the model
	final FilterableTreeModel model = new FilterableTreeModel(entitiesTreeModel2);
	// filter by "containing words".
	model.addFilter(new WordFilter());
	return model;
    }

    /**
     * Creates a node for a property (can be "dummy" or "common") and adds to appropriate place of entities tree.
     *
     * @param root
     * @param property
     */
    private void createAndAddNode(final Class<?> root, final String property) {
	final EntitiesTreeNode2 parentNode = StringUtils.isEmpty(property) ? rootNode //
		: !PropertyTypeDeterminator.isDotNotation(property) ? node(root, "", true) //
			: node(root, PropertyTypeDeterminator.penultAndLast(property).getKey(), true);
	final EntitiesTreeNode2 node = new EntitiesTreeNode2(createUserObject(root, property));
	nodesCache.put(AbstractDomainTree.key(root, property), node);
	if (isNotDummyAndNotCommonProperty(property)) {
	    nodesForSimplePropertiesCache.put(AbstractDomainTree.key(root, AbstractDomainTree.reflectionProperty(property)), node);
	}
	this.insertNodeInto(node, parentNode, parentNode.getChildCount());
    }

    protected boolean isNotDummyAndNotCommonProperty(final String property) {
	return !AbstractDomainTree.isDummyMarker(property) && !AbstractDomainTree.isCommonBranch(property);
    }

    /**
     * Removes a node for a property (can be "dummy" or "common") from its place in entities tree.
     *
     * @param root
     * @param property
     */
    private void removeNode(final Class<?> root, final String property) {
	final EntitiesTreeNode2 node = node(root, property, true);
	this.removeNodeFromParent(node);
	nodesCache.remove(AbstractDomainTree.key(root, property));
	if (isNotDummyAndNotCommonProperty(property)) {
	    nodesForSimplePropertiesCache.remove(AbstractDomainTree.key(root, AbstractDomainTree.reflectionProperty(property)));
	}
    }

    /**
     * Provides a checking path in a model.
     *
     * @param model
     * @param path
     * @param checked
     */
    private void provideCheckingPath(final TreeCheckingModel model, final TreePath path, final boolean checked) {
	final List<TreePath> currentPaths = Arrays.asList(model.getCheckingPaths());
	if (checked) {
	    if (!currentPaths.contains(path)) {
		model.addCheckingPath(path);
	    }
	} else {
	    if (currentPaths.contains(path)) {
		model.removeCheckingPath(path);
	    }
	}
    }

    /**
     * Provides a state for a node corresponding to a property.
     *
     * @param manager
     * @param root
     * @param property
     */
    protected void provideNodeState(final IDomainTreeManagerAndEnhancer manager, final Class<?> root, final String property) {
	final TreePath path = new TreePath(getPathToRoot(node(root, property, false)));
	final int firstIndex = EntitiesTreeColumn.CRITERIA_COLUMN.getColumnIndex();
	final int secondIndex = EntitiesTreeColumn.TABLE_HEADER_COLUMN.getColumnIndex();

	provideCheckingPath(getCheckingModel(firstIndex), path, manager.getFirstTick().isChecked(root, property));
	provideCheckingPath(getCheckingModel(secondIndex), path, manager.getSecondTick().isChecked(root, property));
	getCheckingModel(firstIndex).setPathEnabled(path, !manager.getRepresentation().getFirstTick().isDisabledImmutably(root, property));
	getCheckingModel(secondIndex).setPathEnabled(path, !manager.getRepresentation().getSecondTick().isDisabledImmutably(root, property));
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

    /**
     * Creates a listener to perform connection Entities Tree Model => Domain Tree Manager (in sense of "checking").
     *
     * @param tickManager
     * @return
     */
    private TreeCheckingListener createTreeCheckingListener(final ITickManager tickManager) {
	return new TreeCheckingListener() {
	    @Override
	    public void valueChanged(final TreeCheckingEvent checkingEvent) {
		final EntitiesTreeNode2 node = (EntitiesTreeNode2) checkingEvent.getPath().getLastPathComponent();
		final Pair<Class<?>, String> userObject = node.getUserObject();
		final Class<?> root = userObject.getKey();
		final String property = userObject.getValue();
		if (!isNotDummyAndNotCommonProperty(property)) {
		    throw new IllegalArgumentException("The dummy / common property [" + property + "] for type [" + root.getSimpleName() + "] can not be [un]checked.");
		}
		tickManager.check(root, AbstractDomainTree.reflectionProperty(property), checkingEvent.isCheckedPath());
	    }
	};
    }

    /**
     * Finds a node corresponding to a property.
     *
     * @param root
     * @param property
     * @param withDummyNaming -- indicates whether a property can contain "dummy" or "common" properties
     * @return
     */
    private EntitiesTreeNode2 node(final Class<?> root, final String property, final boolean withDummyNaming) {
	final EnhancementPropertiesMap<EntitiesTreeNode2> cache = withDummyNaming ? nodesCache : nodesForSimplePropertiesCache;
	return cache.get(AbstractDomainTree.key(root, property));
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
		manager.getRepresentation().warmUp(rootAndProp.getKey(), rootAndProp.getValue());
	    }

	    @Override
	    public void treeWillCollapse(final TreeExpansionEvent event) throws ExpandVetoException {
	    }
	};
    }

    /**
     * Extracts title and description from a property (with a "dummy" contract).
     *
     * @param root
     * @param property
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
	    title = StringUtils.isEmpty(tad.getKey()) ? property : tad.getKey();
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
     * Creates a user object of the {@link EntitiesTreeNode2} with correct {@link #toString()} implementation to correctly reflect title of the node in the tree.
     *
     * @param root
     * @param property
     * @return
     */
    protected EntitiesTreeUserObject createUserObject(final Class<?> root, final String property) {
	return new EntitiesTreeUserObject(root, property);
    }

    /**
     * An user object containing title, label toolTip and two checkboxes toolTips.
     *
     * @author TG Team
     *
     */
    public class EntitiesTreeUserObject extends Pair<Class<?>, String> {
	private static final long serialVersionUID = 6190072664610668018L;
	private final String toStringTitle, labelTooltip, firstTickTooltip, secondTickTooltip;

	public EntitiesTreeUserObject(final Class<?> root, final String property) {
	    super(root, property);

	    final Class<?> managedRoot = getManager().getEnhancer().getManagedType(root);
	    final Pair<String, String> titleAndDesc = extractTitleAndDesc(managedRoot, property);
	    toStringTitle = titleAndDesc.getKey();
	    labelTooltip = titleAndDesc.getValue();
	    firstTickTooltip = createCriteriaCheckboxToolTipText(managedRoot, property);
	    secondTickTooltip = createResultSetCheckboxToolTipText(managedRoot, property);
	}

	@Override
	public final String toString() {
	    return toStringTitle;
	}

	public String getLabelTooltip() {
	    return labelTooltip;
	}

	public String getFirstTickTooltip() {
	    return firstTickTooltip;
	}

	public String getSecondTickTooltip() {
	    return secondTickTooltip;
	}

	private String createCriteriaCheckboxToolTipText(final Class<?> root, final String property) {
	    if (!EntitiesTreeModel2.ROOT_PROPERTY.equals(property) && !AbstractDomainTree.isCommonBranch(property) && manager.getRepresentation().getFirstTick().isDisabledImmutably(root, AbstractDomainTree.reflectionProperty(property))) { // no tooltip for disabled property
		return null;
	    }
	    if (EntityUtils.isUnionEntityType(PropertyTypeDeterminator.transform(root, AbstractDomainTree.reflectionProperty(property)).getKey())) { // parent is union entity
		return "<html>If not selected, then entities with <i><b>" + EntitiesTreeModel2.extractTitleAndDesc(root, property).getKey() + "</b></i> will be ignored</html>";
	    }
	    return "<html>Add/Remove <b>" + EntitiesTreeModel2.extractTitleAndDesc(root, property).getKey() + "</b> to/from " + firstTickCaption + "</html>";
	}

	private String createResultSetCheckboxToolTipText(final Class<?> root, final String property) {
	    if (!EntitiesTreeModel2.ROOT_PROPERTY.equals(property) && !AbstractDomainTree.isCommonBranch(property) && manager.getRepresentation().getSecondTick().isDisabledImmutably(root, AbstractDomainTree.reflectionProperty(property))) { // no tooltip for disabled property
		return null;
	    }
	    return "<html>Add/Remove <b>" + EntitiesTreeModel2.extractTitleAndDesc(root, property).getKey() + "</b> to/from " + secondTickCaption + "</html>";
	}
    }

    public EntitiesTreeCellRenderer getCellRenderer1() {
	return cellRenderer1;
    }

    public EntitiesTreeCellRenderer getCellRenderer2() {
	return cellRenderer2;
    }

    public FilterableTreeModel getFilterableModel() {
	return filterableModel;
    }
}
