package ua.com.fielden.platform.swing.treewitheditors.domaintree.development;

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

import ua.com.fielden.platform.domaintree.IDomainTreeManager;
import ua.com.fielden.platform.domaintree.IDomainTreeManager.IDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.IDomainTreeManager.ITickManager;
import ua.com.fielden.platform.domaintree.IDomainTreeManager.ITickManager.IPropertyCheckingListener;
import ua.com.fielden.platform.domaintree.IDomainTreeRepresentation.IPropertyListener;
import ua.com.fielden.platform.domaintree.IDomainTreeRepresentation.ITickRepresentation;
import ua.com.fielden.platform.domaintree.IDomainTreeRepresentation.ITickRepresentation.IPropertyDisablementListener;
import ua.com.fielden.platform.domaintree.centre.analyses.IAbstractAnalysisDomainTreeManager;
import ua.com.fielden.platform.domaintree.impl.AbstractDomainTree;
import ua.com.fielden.platform.domaintree.impl.AbstractDomainTreeManager.ITickManagerWithMutability;
import ua.com.fielden.platform.domaintree.impl.AbstractDomainTreeManager.ITickRepresentationWithMutability;
import ua.com.fielden.platform.domaintree.impl.EnhancementPropertiesMap;
import ua.com.fielden.platform.reflection.PropertyTypeDeterminator;
import ua.com.fielden.platform.reflection.TitlesDescsGetter;
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
public class EntitiesTreeModel2<DTM extends IDomainTreeManager> extends MultipleCheckboxTreeModel2 {
    private static final Logger logger = Logger.getLogger(EntitiesTreeModel2.class);
    private static final long serialVersionUID = -5156365765004770688L;
    public static final String ROOT_PROPERTY = "entities-root";

    private final DTM manager;
    private final EntitiesTreeNode2<DTM> rootNode;
    /** A cached map of nodes by its names (includes "dummy" and "common"). */
    private final EnhancementPropertiesMap<EntitiesTreeNode2<DTM>> nodesCache;
    /** A cached map of nodes by its names (includes only real properties without "dummy" and "common" stuff). */
    private final EnhancementPropertiesMap<EntitiesTreeNode2<DTM>> nodesForSimplePropertiesCache;
    private final TreeCheckingListener [] listeners;
    private final FilterableTreeModel filterableModel;
    //private final Logger logger = Logger.getLogger(getClass());
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
	    final DTM manager,//
	    final String firstTickCaption,//
	    final String secondTickCaption) {
	super(2);

	this.firstTickCaption = firstTickCaption;
	this.secondTickCaption = secondTickCaption;

	this.getCheckingModel(0).setCheckingMode(CheckingMode.SIMPLE);
	this.getCheckingModel(1).setCheckingMode(CheckingMode.SIMPLE);

	this.manager = manager;
	this.listeners = new TreeCheckingListener [] { createTreeCheckingListener(this.manager.getFirstTick()), createTreeCheckingListener(this.manager.getSecondTick()) };
	this.setRoot(this.rootNode = new EntitiesTreeNode2<DTM>(createUserObject(EntitiesTreeModel2.class, ROOT_PROPERTY)));
	this.nodesCache = AbstractDomainTree.createPropertiesMap();
	this.nodesForSimplePropertiesCache = AbstractDomainTree.createPropertiesMap();

	// initialise nodes according to included properties of the manager (these include "dummy" and "common properties" stuff)
	for (final Class<?> root : this.manager.getRepresentation().rootTypes()) {
	    // load checked properties for both ticks to make ticks fully operational before the user check any property
	    this.manager.getFirstTick().checkedProperties(root);
	    this.manager.getSecondTick().checkedProperties(root);

	    final List<String> properties = this.manager.getRepresentation().includedProperties(root);
	    for (final String property : properties) {
		createAndAddNode(root, property);
		if (isNotDummyAndNotCommonProperty(property)) { // Update the state of newly created node according to a property state in manager (ignore "dummy" due to its temporal nature)
		    provideNodeState(this.manager, root, AbstractDomainTree.reflectionProperty(property));
		}
	    }
	}

	// add the listener into manager's representation to correctly reflect 'structural' changes (property added / removed) in this EntitiesTreeModel
	this.manager.getRepresentation().addPropertyListener(new PropertyListener());
	// add two listeners into manager's representation's first and second ticks to correctly reflect 'disablement' changes (property disabled / enabled) in this EntitiesTreeModel
	this.manager.getRepresentation().getFirstTick().addPropertyDisablementListener(new PropertyDisablementListener(0));
	this.manager.getRepresentation().getSecondTick().addPropertyDisablementListener(new PropertyDisablementListener(1));
	// add two listeners into manager's first and second tick to correctly reflect 'checking' changes (property checked / unchecked) in this EntitiesTreeModel
	this.manager.getFirstTick().addPropertyCheckingListener(new PropertyCheckingListener(0));
	this.manager.getSecondTick().addPropertyCheckingListener(new PropertyCheckingListener(1));

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
		    /* performance bottleneck */provideNodeState(manager, root, AbstractDomainTree.reflectionProperty(property));
		}
	    } else {
		removeNode(root, property); // nothing to do with an useless item -- just remove it
	    }
	}

	@Override
	public boolean isInternal() {
	    return false;
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
	    if (path(root, AbstractDomainTree.reflectionProperty(property)) != null) { // if path does not exist already -- ignore "unchecking"
		provideEnablementForPath(getCheckingModel(modelIndex), path(root, AbstractDomainTree.reflectionProperty(property)), !hasBeenDisabled);
	    }
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
	public void propertyStateChanged(final Class<?> root, final String property, final Boolean hasBeenChecked, final Boolean oldState, final int index) {
	    final String reflectionProperty = AbstractDomainTree.reflectionProperty(property);
	    if (!AbstractDomainTree.isPlaceholder(reflectionProperty)) {
		if (path(root, reflectionProperty) != null) { // if path does not exist already -- ignore "unchecking"
		    provideCheckingForPath(getCheckingModel(modelIndex), path(root, reflectionProperty), hasBeenChecked);
		}
	    }
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
	final EntitiesTreeNode2<DTM> node = node(root, property, false);
	if (node == null) {
	    return null;
	}
	return new TreePath(getPathToRoot(node));
    }

    protected FilterableTreeModel createFilteringModel(final EntitiesTreeModel2<DTM> entitiesTreeModel2) {
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
	// logger.info("Started createAndAddNode for property [" + property + "].");
	// logger.info("\tget parentNode.");
	final EntitiesTreeNode2<DTM> parentNode = StringUtils.isEmpty(property) ? rootNode //
		: !PropertyTypeDeterminator.isDotNotation(property) ? node(root, "", true) //
			: node(root, PropertyTypeDeterminator.penultAndLast(property).getKey(), true);
	// logger.info("\tcreate new user object.");
	final EntitiesTreeUserObject<DTM> uo = createUserObject(root, property);
	// logger.info("\tcreate new node.");
	final EntitiesTreeNode2<DTM> node = new EntitiesTreeNode2<DTM>(uo);
	// logger.info("\tput new node to cache.");
	nodesCache.put(AbstractDomainTree.key(root, property), node);
	if (isNotDummyAndNotCommonProperty(property)) {
	    nodesForSimplePropertiesCache.put(AbstractDomainTree.key(root, AbstractDomainTree.reflectionProperty(property)), node);
	}
	// logger.info("\tinsert new node into.");

	// parentNode.add(node);
	this.insertNodeInto(node, parentNode, parentNode.getChildCount());
	// logger.info("Ended createAndAddNode for property [" + property + "].");
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
	final EntitiesTreeNode2<DTM> node = node(root, property, true);
	this.removeNodeFromParent(node);
	nodesCache.remove(AbstractDomainTree.key(root, property));
	if (isNotDummyAndNotCommonProperty(property)) {
	    nodesForSimplePropertiesCache.remove(AbstractDomainTree.key(root, AbstractDomainTree.reflectionProperty(property)));
	}
    }

    /**
     * Provides a checking for path in a {@link EntitiesTreeModel2}.
     *
     * @param model
     * @param path
     * @param newChecked
     */
    private void provideCheckingForPath(final TreeCheckingModel model, final TreePath path, final boolean newChecked) {
	// it is very important to determine whether UI is synchronized with Domain Tree model not to cycle change events firing
	final boolean isSynchronized = newChecked == Arrays.asList(model.getCheckingPaths()).contains(path);
	if (!isSynchronized) {
	    if (newChecked) {
		model.addCheckingPath(path);
	    } else {
		model.removeCheckingPath(path);
	    }
	}
    }

    /**
     * Provides an enablement for path in a {@link EntitiesTreeModel2}.
     *
     * @param model
     * @param path
     * @param newEnabled
     */
    private void provideEnablementForPath(final TreeCheckingModel model, final TreePath path, final boolean newEnabled) {
	// it is very important to determine whether UI is synchronized with Domain Tree model not to cycle change events firing
	final boolean isSynchronized = newEnabled == model.isPathEnabled(path);
	if (!isSynchronized) {
	    model.setPathEnabled(path, newEnabled);
	}
    }

    /**
     * Provides a state for a node corresponding to a property (checking and enablement for both ticks).
     *
     * @param manager
     * @param root
     * @param property
     */
    protected void provideNodeState(final DTM manager, final Class<?> root, final String property) {
	final TreePath path = new TreePath(getPathToRoot(node(root, property, false)));
	final TreeCheckingModel firstCheckingModel = getCheckingModel(0);
	final TreeCheckingModel secondCheckingModel = getCheckingModel(1);

	provideCheckingForPath(firstCheckingModel, path, ((ITickManagerWithMutability) manager.getFirstTick()).isCheckedLightweight(root, property));
	provideCheckingForPath(secondCheckingModel, path, ((ITickManagerWithMutability) manager.getSecondTick()).isCheckedLightweight(root, property));

	provideEnablementForPath(firstCheckingModel, path, !((ITickRepresentationWithMutability) manager.getRepresentation().getFirstTick()).isDisabledImmutablyLightweight(root, property));
	provideEnablementForPath(secondCheckingModel, path, !((ITickRepresentationWithMutability) manager.getRepresentation().getSecondTick()).isDisabledImmutablyLightweight(root, property));
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
	    @SuppressWarnings("unchecked")
	    @Override
	    public void valueChanged(final TreeCheckingEvent checkingEvent) {
		final EntitiesTreeNode2<DTM> node = (EntitiesTreeNode2<DTM>) checkingEvent.getPath().getLastPathComponent();
		final EntitiesTreeUserObject<DTM> userObject = node.getUserObject();
		final Class<?> root = userObject.getKey();
		final String property = userObject.getValue();
		if (!isNotDummyAndNotCommonProperty(property)) {
		    throw new IllegalArgumentException("The dummy / common property [" + property + "] for type [" + root.getSimpleName() + "] can not be [un]checked.");
		}
		// it is very important to determine whether UI is synchronized with Domain Tree model not to cycle change events firing
		final boolean isSynchronized = checkingEvent.isCheckedPath() == tickManager.isChecked(root, AbstractDomainTree.reflectionProperty(property));
		if (!isSynchronized) {
		    tickManager.check(root, AbstractDomainTree.reflectionProperty(property), checkingEvent.isCheckedPath());
		}
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
    private EntitiesTreeNode2<DTM> node(final Class<?> root, final String property, final boolean withDummyNaming) {
	final EnhancementPropertiesMap<EntitiesTreeNode2<DTM>> cache = withDummyNaming ? nodesCache : nodesForSimplePropertiesCache;
	return cache.get(AbstractDomainTree.key(root, property));
    }

    /**
     * Creates a {@link TreeWillExpandListener} that "warms up" the manager's property (loads children), which node is trying to be expanded.
     *
     * @return
     */
    public TreeWillExpandListener createTreeWillExpandListener() {
	return new TreeWillExpandListener() {
	    @SuppressWarnings("unchecked")
	    @Override
	    public void treeWillExpand(final TreeExpansionEvent event) throws ExpandVetoException {
		final EntitiesTreeNode2<DTM> node = (EntitiesTreeNode2<DTM>) event.getPath().getLastPathComponent();
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
     * @param managedType
     * @param property
     * @return
     */
    public static Pair<String, String> extractTitleAndDesc(final Class<?> root, final Class<?> managedType, final String property) {
	final String title, desc;
	if (EntitiesTreeModel2.ROOT_PROPERTY.equals(property)) { // root node
	    title = "Entities";
	    desc = "<b>Available entities</b>";
	} else if (AbstractDomainTree.isCommonBranch(property)) { // common group
	    title = "Common";
	    desc = TitlesDescsGetter.italic("<b>Common properties</b>");
	} else { // entity node
	    final Pair<String, String> tad = "".equals(property) ? TitlesDescsGetter.getEntityTitleAndDesc(root) : TitlesDescsGetter.getTitleAndDesc(AbstractDomainTree.reflectionProperty(property), managedType);
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
    public DTM getManager() {
	return manager;
    }

    /**
     * Creates a user object of the {@link EntitiesTreeNode2} with correct {@link #toString()} implementation to correctly reflect title of the node in the tree.
     *
     * @param root
     * @param property
     * @return
     */
    protected EntitiesTreeUserObject<DTM> createUserObject(final Class<?> root, final String property) {
	return new EntitiesTreeUserObject<DTM>(getManager(), root, property, firstTickCaption, secondTickCaption);
    }

    /**
     * An user object containing title, label toolTip and two checkboxes toolTips.
     *
     * @author TG Team
     *
     */
    public static class EntitiesTreeUserObject<DTM extends IDomainTreeManager> extends Pair<Class<?>, String> {
	private static final long serialVersionUID = 6190072664610668018L;
	private final String toStringTitle, labelTooltip, firstTickTooltip, secondTickTooltip;

	private final String firstTickCaption, secondTickCaption;
	private final DTM manager;

	public EntitiesTreeUserObject(final DTM manager, final Class<?> root, final String property, final String firstTickCaption,//
		    final String secondTickCaption) {
	    super(root, property);
	    this.manager= manager;
	    this.firstTickCaption = firstTickCaption;
	    this.secondTickCaption = secondTickCaption;
	    // logger.info("\t\tRetrieval of managedType.");
	    final Class<?> managedRoot = EntitiesTreeModel2.ROOT_PROPERTY.equals(property) ? root : (manager instanceof IDomainTreeManagerAndEnhancer ? ((IDomainTreeManagerAndEnhancer)manager).getEnhancer().getManagedType(root) : (manager instanceof IAbstractAnalysisDomainTreeManager ? ((IAbstractAnalysisDomainTreeManager) manager).parentCentreDomainTreeManager().getEnhancer().getManagedType(root) : root));
	    // logger.info("\t\textractTitleAndDesc.");
	    final Pair<String, String> titleAndDesc = extractTitleAndDesc(root, managedRoot, property);
	    toStringTitle = titleAndDesc.getKey();
	    labelTooltip = titleAndDesc.getValue();
	    // logger.info("\t\tcreateCriteriaCheckboxToolTipText.");
	    firstTickTooltip = createCriteriaCheckboxToolTipText(root, managedRoot, property);
	    // logger.info("\t\tcreateResultCheckboxToolTipText.");
	    secondTickTooltip = createResultSetCheckboxToolTipText(root, managedRoot, property);
	    // logger.info("\t\tend.");
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

	private static boolean isDisabled(final ITickRepresentation tr, final Class<?> root, final String property) {
	    return !EntitiesTreeModel2.ROOT_PROPERTY.equals(property) && !AbstractDomainTree.isCommonBranch(property) && ((ITickRepresentationWithMutability) tr).isDisabledImmutablyLightweight(root, AbstractDomainTree.reflectionProperty(property));
	}

	private String createCriteriaCheckboxToolTipText(final Class<?> root, final Class<?> managedRoot, final String property) {
	    if (isDisabled(manager.getRepresentation().getFirstTick(), root, property)) { // no tooltip for disabled property
		return null;
	    }
	    if (EntityUtils.isUnionEntityType(PropertyTypeDeterminator.transform(managedRoot, AbstractDomainTree.reflectionProperty(property)).getKey())) { // parent is union entity
		return "<html>If not selected, then entities with <i><b>" + toStringTitle + "</b></i> will be ignored</html>";
	    }
	    return "<html>Add/Remove <b>" + toStringTitle + "</b> to/from " + firstTickCaption + "</html>";
	}

	private String createResultSetCheckboxToolTipText(final Class<?> root, final Class<?> managedRoot, final String property) {
	    if (isDisabled(manager.getRepresentation().getSecondTick(), root, property)) { // no tooltip for disabled property
		return null;
	    }
	    return "<html>Add/Remove <b>" + toStringTitle + "</b> to/from " + secondTickCaption + "</html>";
	}
    }

    public FilterableTreeModel getFilterableModel() {
	return filterableModel;
    }
}
