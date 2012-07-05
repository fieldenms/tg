package ua.com.fielden.platform.domaintree.centre.analyses.impl;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.centre.IOrderingRepresentation.Ordering;
import ua.com.fielden.platform.domaintree.centre.analyses.IAbstractAnalysisDomainTreeManager;
import ua.com.fielden.platform.domaintree.centre.analyses.IAbstractAnalysisDomainTreeRepresentation;
import ua.com.fielden.platform.domaintree.centre.analyses.IAbstractAnalysisDomainTreeRepresentation.IAbstractAnalysisAddToAggregationTickRepresentation;
import ua.com.fielden.platform.domaintree.impl.AbstractDomainTree;
import ua.com.fielden.platform.domaintree.impl.AbstractDomainTreeManager;
import ua.com.fielden.platform.domaintree.impl.EnhancementRootsMap;
import ua.com.fielden.platform.serialisation.api.ISerialiser;
import ua.com.fielden.platform.serialisation.impl.TgKryo;
import ua.com.fielden.platform.utils.Pair;

/**
 * Analyses domain tree manager. Includes support for checking (from base {@link AbstractDomainTreeManager}). <br>
 * <br>
 *
 * Includes implementation of "checking" logic, that contain: <br>
 * a) default mutable state management; <br>
 * a) manual state management; <br>
 * b) resolution of conflicts with excluded, disabled etc. properties; <br>
 *
 * @author TG Team
 *
 */
public abstract class AbstractAnalysisDomainTreeManager extends AbstractDomainTreeManager implements IAbstractAnalysisDomainTreeManager {
    private Boolean visible;

    private final transient ICentreDomainTreeManagerAndEnhancer parentCentreDomainTreeManager;

    @Override
    public IAbstractAnalysisAddToDistributionTickManager getFirstTick() {
	return (IAbstractAnalysisAddToDistributionTickManager) super.getFirstTick();
    }

    @Override
    public IAbstractAnalysisAddToAggregationTickManager getSecondTick() {
	return (IAbstractAnalysisAddToAggregationTickManager) super.getSecondTick();
    }

    @Override
    public IAbstractAnalysisDomainTreeRepresentation getRepresentation() {
	return (IAbstractAnalysisDomainTreeRepresentation) super.getRepresentation();
    }

    /**
     * A <i>manager</i> constructor.
     *
     * @param serialiser
     * @param dtr
     * @param firstTick
     * @param secondTick
     */
    protected AbstractAnalysisDomainTreeManager(final ISerialiser serialiser, final AbstractAnalysisDomainTreeRepresentation dtr, final Boolean visible, final AbstractAnalysisAddToDistributionTickManager firstTick, final AbstractAnalysisAddToAggregationTickManager secondTick) {
	super(serialiser, dtr, firstTick, secondTick);
	this.visible = visible;

	parentCentreDomainTreeManager = null; // as soon as this analysis wiil be added into centre manager -- this field should be initialised
    }

    @Override
    public ICentreDomainTreeManagerAndEnhancer parentCentreDomainTreeManager() {
	return parentCentreDomainTreeManager;
    }

    protected Class<?> managedType(final Class<?> root) {
	return parentCentreDomainTreeManager().getEnhancer().getManagedType(root);
    }

    @Override
    public boolean isVisible() {
	return visible != null ? visible : true; // should be visible by default;
    }

    @Override
    public IAbstractAnalysisDomainTreeManager setVisible(final boolean visible) {
	this.visible = visible;
	return this;
    }

    @Override
    public int hashCode() {
	final int prime = 31;
	int result = super.hashCode();
	result = prime * result + (visible == null ? 0 : visible.hashCode());
	return result;
    }

    @Override
    public boolean equals(final Object obj) {
	if (this == obj) {
	    return true;
	}
	if (!super.equals(obj)) {
	    return false;
	}
	if (getClass() != obj.getClass()) {
	    return false;
	}
	final AbstractAnalysisDomainTreeManager other = (AbstractAnalysisDomainTreeManager) obj;
	if (visible == null) {
	    if (other.visible != null) {
		return false;
	    }
	} else if (!visible.equals(other.visible)) {
	    return false;
	}
	return true;
    }

    protected abstract static class AbstractAnalysisAddToDistributionTickManager extends TickManager implements IAbstractAnalysisAddToDistributionTickManager {
	private final EnhancementRootsMap<List<String>> rootsListsOfUsedProperties;
	private final transient List<IPropertyUsageListener> propertyUsageListeners;

	private final transient ICentreDomainTreeManagerAndEnhancer parentCentreDomainTreeManager;

	/**
	 * Used for serialisation and for normal initialisation. IMPORTANT : To use this tick it should be passed into manager constructor, which will initialise "dtr" and "tr"
	 * fields.
	 */
	public AbstractAnalysisAddToDistributionTickManager() {
	    super();
	    rootsListsOfUsedProperties = createRootsMap();
	    propertyUsageListeners = new ArrayList<IPropertyUsageListener>();

	    parentCentreDomainTreeManager = null; // as soon as this analysis wiil be added into centre manager -- this field should be initialised
	}

	private ICentreDomainTreeManagerAndEnhancer parentCentreDomainTreeManager() {
	    return parentCentreDomainTreeManager;
	}

	protected Class<?> managedType(final Class<?> root) {
	    return parentCentreDomainTreeManager().getEnhancer().getManagedType(root);
	}

	@Override
	public boolean isChecked(final Class<?> root, final String property) {
	    // inject an enhanced type into method implementation
	    return super.isChecked(managedType(root), property);
	}

	@Override
	public void check(final Class<?> root, final String property, final boolean check) {
	    // inject an enhanced type into method implementation
	    super.check(managedType(root), property, check);
	}

	@Override
	public List<String> checkedProperties(final Class<?> root) {
	    // inject an enhanced type into method implementation
	    return super.checkedProperties(managedType(root));
	}

	@Override
	public synchronized List<String> checkedPropertiesMutable(final Class<?> rootPossiblyEnhanced) {
	    // inject an enhanced type into method implementation
	    return super.checkedPropertiesMutable(managedType(rootPossiblyEnhanced));
	}

	@Override
	public void swap(final Class<?> root, final String property1, final String property2) {
	    // inject an enhanced type into method implementation
	    super.swap(managedType(root), property1, property2);
	}

	@Override
	public void move(final Class<?> root, final String what, final String beforeWhat) {
	    // inject an enhanced type into method implementation
	    super.move(managedType(root), what, beforeWhat);
	}

	@Override
	public void moveToTheEnd(final Class<?> root, final String what) {
	    // inject an enhanced type into method implementation
	    super.moveToTheEnd(managedType(root), what);
	}

	@Override
	public final boolean isUsed(final Class<?> root, final String property) {
	    // inject an enhanced type into method implementation
	    final Class<?> managedType = managedType(root);

	    illegalUncheckedProperties(this, managedType, property, "It's illegal to ask whether the specified property [" + property + "] is 'used' if it is not 'checked' in type [" + managedType.getSimpleName() + "].");
	    return rootsListsOfUsedProperties.containsKey(managedType) && rootsListsOfUsedProperties.get(managedType).contains(property);
	}

	@Override
	public void use(final Class<?> root, final String property, final boolean check) {
	    // inject an enhanced type into method implementation
	    final Class<?> managedType = managedType(root);

	    final List<String> listOfUsedProperties = getAndInitUsedProperties(managedType, property);
	    if (check && !listOfUsedProperties.contains(property)) {
		listOfUsedProperties.add(property);
	    } else if (!check) {
		listOfUsedProperties.remove(property);
	    }
	    for (final IPropertyUsageListener listener : propertyUsageListeners) {
		listener.propertyStateChanged(managedType, property, check, null);
	    }
	}

	@Override
	public boolean addPropertyUsageListener(final IPropertyUsageListener listener) {
	    return propertyUsageListeners.add(listener);
	}

	@Override
	public boolean removePropertyUsageListener(final IPropertyUsageListener listener) {
	    return propertyUsageListeners.remove(listener);
	}

	protected List<String> getAndInitUsedProperties(final Class<?> root, final String property) {
	    illegalUncheckedProperties(this, root, property, "It's illegal to use/unuse the specified property [" + property + "] if it is not 'checked' in type [" + root.getSimpleName() + "].");
	    if (!rootsListsOfUsedProperties.containsKey(root)) {
		rootsListsOfUsedProperties.put(root, new ArrayList<String>());
	    }
	    return rootsListsOfUsedProperties.get(root);
	}

	@Override
	public final List<String> usedProperties(final Class<?> root) {
	    // inject an enhanced type into method implementation
	    final Class<?> managedType = managedType(root);

	    final List<String> checkedProperties = checkedProperties(managedType);
	    final List<String> usedProperties = new ArrayList<String>();
	    for (final String property : checkedProperties) {
		if (isUsed(managedType, property)) {
		    usedProperties.add(property);
		}
	    }
	    return usedProperties;
	}

	@Override
	public int hashCode() {
	    final int prime = 31;
	    int result = super.hashCode();
	    result = prime * result + (rootsListsOfUsedProperties == null ? 0 : rootsListsOfUsedProperties.hashCode());
	    return result;
	}

	@Override
	public boolean equals(final Object obj) {
	    if (this == obj) {
		return true;
	    }
	    if (!super.equals(obj)) {
		return false;
	    }
	    if (getClass() != obj.getClass()) {
		return false;
	    }
	    final AbstractAnalysisAddToDistributionTickManager other = (AbstractAnalysisAddToDistributionTickManager) obj;
	    if (rootsListsOfUsedProperties == null) {
		if (other.rootsListsOfUsedProperties != null) {
		    return false;
		}
	    } else if (!rootsListsOfUsedProperties.equals(other.rootsListsOfUsedProperties)) {
		return false;
	    }
	    return true;
	}
    }

    protected abstract static class AbstractAnalysisAddToAggregationTickManager extends TickManager implements IAbstractAnalysisAddToAggregationTickManager {
	private final EnhancementRootsMap<List<Pair<String, Ordering>>> rootsListsOfOrderings;
	private final EnhancementRootsMap<List<String>> rootsListsOfUsedProperties;
	private final transient List<IPropertyUsageListener> propertyUsageListeners;
	private final transient List<IPropertyOrderingListener> propertyOrderingListeners;

	private final transient ICentreDomainTreeManagerAndEnhancer parentCentreDomainTreeManager;

	@Override
	protected IAbstractAnalysisAddToAggregationTickRepresentation tr() {
	    return (IAbstractAnalysisAddToAggregationTickRepresentation) super.tr();
	}

	/**
	 * Used for serialisation and for normal initialisation. IMPORTANT : To use this tick it should be passed into manager constructor, which will initialise "dtr" and "tr"
	 * fields.
	 */
	public AbstractAnalysisAddToAggregationTickManager() {
	    super();
	    rootsListsOfOrderings = createRootsMap();
	    rootsListsOfUsedProperties = createRootsMap();
	    propertyUsageListeners = new ArrayList<IPropertyUsageListener>();
	    propertyOrderingListeners = new ArrayList<IPropertyOrderingListener>();

	    parentCentreDomainTreeManager = null; // as soon as this analysis wiil be added into centre manager -- this field should be initialised
	}

	private ICentreDomainTreeManagerAndEnhancer parentCentreDomainTreeManager() {
	    return parentCentreDomainTreeManager;
	}

	protected Class<?> managedType(final Class<?> root) {
	    return parentCentreDomainTreeManager().getEnhancer().getManagedType(root);
	}

	@Override
	public boolean isChecked(final Class<?> root, final String property) {
	    // inject an enhanced type into method implementation
	    return super.isChecked(managedType(root), property);
	}

	@Override
	public void check(final Class<?> root, final String property, final boolean check) {
	    // inject an enhanced type into method implementation
	    super.check(managedType(root), property, check);
	}

	@Override
	public List<String> checkedProperties(final Class<?> root) {
	    // inject an enhanced type into method implementation
	    return super.checkedProperties(managedType(root));
	}

	@Override
	public synchronized List<String> checkedPropertiesMutable(final Class<?> rootPossiblyEnhanced) {
	    // inject an enhanced type into method implementation
	    return super.checkedPropertiesMutable(managedType(rootPossiblyEnhanced));
	}

	@Override
	public void swap(final Class<?> root, final String property1, final String property2) {
	    // inject an enhanced type into method implementation
	    super.swap(managedType(root), property1, property2);
	}

	@Override
	public void move(final Class<?> root, final String what, final String beforeWhat) {
	    // inject an enhanced type into method implementation
	    super.move(managedType(root), what, beforeWhat);
	}

	@Override
	public void moveToTheEnd(final Class<?> root, final String what) {
	    // inject an enhanced type into method implementation
	    super.moveToTheEnd(managedType(root), what);
	}

	@Override
	public final boolean isUsed(final Class<?> root, final String property) {
	    // inject an enhanced type into method implementation
	    final Class<?> managedType = managedType(root);

	    illegalUncheckedProperties(this, managedType, property, "It's illegal to ask whether the specified property [" + property + "] is 'used' if it is not 'checked' in type [" + managedType.getSimpleName() + "].");
	    return rootsListsOfUsedProperties.containsKey(managedType) && rootsListsOfUsedProperties.get(managedType).contains(property);
	}

	@Override
	public void use(final Class<?> root, final String property, final boolean check) {
	    // inject an enhanced type into method implementation
	    final Class<?> managedType = managedType(root);

	    final List<String> listOfUsedProperties = getAndInitUsedProperties(managedType, property);
	    if (check && !listOfUsedProperties.contains(property)) {
		listOfUsedProperties.add(property);
	    } else if (!check && listOfUsedProperties.contains(property)) {
		// before successful removal of the Usage -- the Ordering should be removed
		while (isOrdered(property, orderedProperties(managedType))) {
		    toggleOrdering(managedType, property);
		}
		// perform actual removal
		listOfUsedProperties.remove(property);
	    }
	    for (final IPropertyUsageListener listener : propertyUsageListeners) {
		listener.propertyStateChanged(managedType, property, check, null);
	    }
	}

	@Override
	public final List<String> usedProperties(final Class<?> root) {
	    // inject an enhanced type into method implementation
	    final Class<?> managedType = managedType(root);

	    final List<String> checkedProperties = checkedProperties(managedType);
	    final List<String> usedProperties = new ArrayList<String>();
	    for (final String property : checkedProperties) {
		if (isUsed(managedType, property)) {
		    usedProperties.add(property);
		}
	    }
	    return usedProperties;
	}

	private static boolean isOrdered(final String property, final List<Pair<String, Ordering>> orderedProperties) {
	    for (final Pair<String, Ordering> pair : orderedProperties) {
		if (property.equals(pair.getKey())) {
		    return true;
		}
	    }
	    return false;
	}

	@Override
	public boolean addPropertyUsageListener(final IPropertyUsageListener listener) {
	    return propertyUsageListeners.add(listener);
	}

	@Override
	public boolean removePropertyUsageListener(final IPropertyUsageListener listener) {
	    return propertyUsageListeners.remove(listener);
	}

	protected List<String> getAndInitUsedProperties(final Class<?> root, final String property) {
	    illegalUncheckedProperties(this, root, property, "It's illegal to use/unuse the specified property [" + property + "] if it is not 'checked' in type [" + root.getSimpleName() + "].");
	    if (!rootsListsOfUsedProperties.containsKey(root)) {
		rootsListsOfUsedProperties.put(root, new ArrayList<String>());
	    }
	    return rootsListsOfUsedProperties.get(root);
	}

	@Override
	public List<Pair<String, Ordering>> orderedProperties(final Class<?> root) {
	    // inject an enhanced type into method implementation
	    final Class<?> managedType = managedType(root);

	    if (rootsListsOfOrderings.containsKey(managedType)) {
		return rootsListsOfOrderings.get(managedType);
	    } else {
		return tr().orderedPropertiesByDefault(managedType);
	    }
	}

	@Override
	public void toggleOrdering(final Class<?> root, final String property) {
	    // inject an enhanced type into method implementation
	    final Class<?> managedType = managedType(root);

	    AbstractDomainTree.illegalUnusedProperties(this, managedType, property, "Could not toggle 'ordering' for 'unused' property [" + property + "] in type [" + managedType.getSimpleName() + "].");
	    if (!rootsListsOfOrderings.containsKey(managedType)) {
		rootsListsOfOrderings.put(managedType, new ArrayList<Pair<String, Ordering>>(tr().orderedPropertiesByDefault(managedType)));
	    }
	    final List<Pair<String, Ordering>> list = new ArrayList<Pair<String, Ordering>>(rootsListsOfOrderings.get(managedType));
	    for (final Pair<String, Ordering> pair : list) {
		if (pair.getKey().equals(property)) {
		    final int index = rootsListsOfOrderings.get(managedType).indexOf(pair);
		    if (Ordering.ASCENDING.equals(pair.getValue())) {
			rootsListsOfOrderings.get(managedType).get(index).setValue(Ordering.DESCENDING);
		    } else { // Ordering.DESCENDING
			rootsListsOfOrderings.get(managedType).remove(index);
		    }
		    for (final IPropertyOrderingListener listener : propertyOrderingListeners) {
			listener.propertyStateChanged(managedType, property, new ArrayList<Pair<String, Ordering>>(orderedProperties(managedType)), null);
		    }
		    return;
		}
	    } // if the property does not have an Ordering assigned -- put a ASC ordering to it (into the end of the list)
	    rootsListsOfOrderings.get(managedType).add(new Pair<String, Ordering>(property, Ordering.ASCENDING));

	    for (final IPropertyOrderingListener listener : propertyOrderingListeners) {
		listener.propertyStateChanged(managedType, property, new ArrayList<Pair<String, Ordering>>(orderedProperties(managedType)), null);
	    }
	}

	@Override
	public boolean addPropertyOrderingListener(final IPropertyOrderingListener listener) {
	    return propertyOrderingListeners.add(listener);
	}

	@Override
	public boolean removePropertyOrderingListener(final IPropertyOrderingListener listener) {
	    return propertyOrderingListeners.remove(listener);
	}

	@Override
	public int hashCode() {
	    final int prime = 31;
	    int result = super.hashCode();
	    result = prime * result + (rootsListsOfOrderings == null ? 0 : rootsListsOfOrderings.hashCode());
	    result = prime * result + (rootsListsOfUsedProperties == null ? 0 : rootsListsOfUsedProperties.hashCode());
	    return result;
	}

	@Override
	public boolean equals(final Object obj) {
	    if (this == obj) {
		return true;
	    }
	    if (!super.equals(obj)) {
		return false;
	    }
	    if (getClass() != obj.getClass()) {
		return false;
	    }
	    final AbstractAnalysisAddToAggregationTickManager other = (AbstractAnalysisAddToAggregationTickManager) obj;
	    if (rootsListsOfOrderings == null) {
		if (other.rootsListsOfOrderings != null) {
		    return false;
		}
	    } else if (!rootsListsOfOrderings.equals(other.rootsListsOfOrderings)) {
		return false;
	    }
	    if (rootsListsOfUsedProperties == null) {
		if (other.rootsListsOfUsedProperties != null) {
		    return false;
		}
	    } else if (!rootsListsOfUsedProperties.equals(other.rootsListsOfUsedProperties)) {
		return false;
	    }
	    return true;
	}
    }

    /**
     * A specific Kryo serialiser for {@link AbstractDomainTreeManager}.
     *
     * @author TG Team
     *
     */
    protected abstract static class AbstractAnalysisDomainTreeManagerSerialiser<T extends AbstractAnalysisDomainTreeManager> extends AbstractDomainTreeManagerSerialiser<T> {
	public AbstractAnalysisDomainTreeManagerSerialiser(final TgKryo kryo) {
	    super(kryo);
	}

	@Override
	public void write(final ByteBuffer buffer, final T manager) {
	    super.write(buffer, manager);
	    writeValue(buffer, manager.getVisible());
	}
    }

    public Boolean getVisible() {
	return visible;
    }
}
