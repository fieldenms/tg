package ua.com.fielden.platform.domaintree.centre.analyses.impl;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.swing.event.EventListenerList;

import ua.com.fielden.platform.domaintree.centre.IWidthManager;
import ua.com.fielden.platform.domaintree.centre.analyses.IPivotDomainTreeManager;
import ua.com.fielden.platform.domaintree.centre.analyses.IPivotDomainTreeRepresentation;
import ua.com.fielden.platform.domaintree.centre.analyses.impl.PivotDomainTreeRepresentation.PivotAddToAggregationTickRepresentation;
import ua.com.fielden.platform.domaintree.centre.analyses.impl.PivotDomainTreeRepresentation.PivotAddToDistributionTickRepresentation;
import ua.com.fielden.platform.domaintree.impl.AbstractDomainTree;
import ua.com.fielden.platform.domaintree.impl.EnhancementPropertiesMap;
import ua.com.fielden.platform.domaintree.impl.EnhancementRootsMap;
import ua.com.fielden.platform.serialisation.api.ISerialiser;

/**
 * A domain tree manager for pivot analyses.
 *
 * @author TG Team
 *
 */
public class PivotDomainTreeManager extends AbstractAnalysisDomainTreeManager implements IPivotDomainTreeManager {
    /**
     * A <i>manager</i> constructor for the first time instantiation.
     *
     * @param serialiser
     * @param rootTypes
     */
    public PivotDomainTreeManager(final ISerialiser serialiser, final Set<Class<?>> rootTypes) {
	this(serialiser, new PivotDomainTreeRepresentation(serialiser, rootTypes),null, new PivotAddToDistributionTickManager(), new PivotAddToAggregationTickManager());
    }

    /**
     * A <i>manager</i> constructor.
     *
     * @param serialiser
     * @param dtr
     * @param firstTick
     * @param secondTick
     */
    protected PivotDomainTreeManager(final ISerialiser serialiser, final PivotDomainTreeRepresentation dtr, final Boolean visible, final PivotAddToDistributionTickManager firstTick, final PivotAddToAggregationTickManager secondTick) {
	super(serialiser, dtr, visible, firstTick, secondTick);
    }

    @Override
    public IPivotAddToDistributionTickManager getFirstTick() {
	return (IPivotAddToDistributionTickManager) super.getFirstTick();
    }

    @Override
    public IPivotAddToAggregationTickManager getSecondTick() {
	return (IPivotAddToAggregationTickManager) super.getSecondTick();
    }

    @Override
    public IPivotDomainTreeRepresentation getRepresentation() {
	return (IPivotDomainTreeRepresentation) super.getRepresentation();
    }

    public static class PivotAddToDistributionTickManager extends AbstractAnalysisAddToDistributionTickManager implements IPivotAddToDistributionTickManager {
	private final EnhancementPropertiesMap<Integer> propertiesWidths;

	private final EnhancementRootsMap<List<String>> rootsListsOfUsedProperties;
	private final transient IUsageManager columnUsageManager;

	/**
	 * Used for serialisation and for normal initialisation. IMPORTANT : To use this tick it should be passed into manager constructor, which will initialise "dtr" and "tr"
	 * fields.
	 */
	public PivotAddToDistributionTickManager() {
	    propertiesWidths = createPropertiesMap();
	    rootsListsOfUsedProperties = createRootsMap();
	    columnUsageManager = new ColumnUsageManager();
	}

	@Override
	public IUsageManager use(final Class<?> root, final String property, final boolean check) {
	    // inject an enhanced type into method implementation
	    final Class<?> managedType = managedType(root);

	    final List<String> listOfUsedProperties = getAndInitUsedProperties(managedType, property);
	    if (check && !listOfUsedProperties.contains(property)) {
		listOfUsedProperties.add(property);
		getSecondUsageManager().use(root, property, false);
	    } else if (!check) {
		listOfUsedProperties.remove(property);
	    }
	    usedPropertiesChanged(root, property, check);
	    return this;
	}

	@Override
	public int getWidth(final Class<?> root, final String property) {
	    AbstractDomainTree.illegalUnusedProperties(this, root, property, "Could not get a 'width' for 'unused' property [" + property + "] in type [" + root.getSimpleName() + "].");
	    return PivotDomainTreeRepresentation.getWidth(tr().getDtr(), root, property, propertiesWidths, tr().getWidthByDefault(root, property));
	}

	@Override
	public IPivotAddToDistributionTickManager setWidth(final Class<?> root, final String property, final int width) {
	    AbstractDomainTree.illegalUnusedProperties(this, root, property, "Could not set a 'width' for 'unused' property [" + property + "] in type [" + root.getSimpleName() + "].");
	    // update ALL existing widths for used properties with the same value (single column "treatment")
	    for (final String usedProperty : usedProperties(root)) {
		PivotDomainTreeRepresentation.setWidth(tr().getDtr(), root, usedProperty, width, propertiesWidths);
	    }
	    return this;
	}

	@Override
	protected PivotAddToDistributionTickRepresentation tr() {
	    return (PivotAddToDistributionTickRepresentation) super.tr();
	}

	@Override
	public int hashCode() {
	    final int prime = 31;
	    int result = super.hashCode();
	    result = prime * result + ((propertiesWidths == null) ? 0 : propertiesWidths.hashCode());
	    result = prime * result + (rootsListsOfUsedProperties == null ? 0 : rootsListsOfUsedProperties.hashCode());
	    return result;
	}

	@Override
	public boolean equals(final Object obj) {
	    if (this == obj)
		return true;
	    if (!super.equals(obj))
		return false;
	    if (getClass() != obj.getClass())
		return false;
	    final PivotAddToDistributionTickManager other = (PivotAddToDistributionTickManager) obj;
	    if (propertiesWidths == null) {
		if (other.propertiesWidths != null)
		    return false;
	    } else if (!propertiesWidths.equals(other.propertiesWidths))
		return false;
	    if (rootsListsOfUsedProperties == null) {
		if (other.rootsListsOfUsedProperties != null) {
		    return false;
		}
	    } else if (!rootsListsOfUsedProperties.equals(other.rootsListsOfUsedProperties)) {
		return false;
	    }
	    return true;
	}

	@Override
	public IUsageManager getSecondUsageManager() {
	    return columnUsageManager;
	}

	private class ColumnUsageManager implements IUsageManager{

	    private final transient EventListenerList propertyUsageListeners;

	    public ColumnUsageManager(){
		propertyUsageListeners = new EventListenerList();
	    }

	    private List<String> getAndInitSecondUsedProperties(final Class<?> root, final String property) {
		illegalUncheckedProperties(PivotAddToDistributionTickManager.this, root, property, "It's illegal to use/unuse the specified property [" + property + "] if it is not 'checked' in type [" + root.getSimpleName() + "].");
		if (!rootsListsOfUsedProperties.containsKey(root)) {
		    rootsListsOfUsedProperties.put(root, new ArrayList<String>());
		}
		return rootsListsOfUsedProperties.get(root);
	    }

	    @Override
	    public boolean isUsed(final Class<?> root, final String property) {
		// inject an enhanced type into method implementation
		final Class<?> managedType = managedType(root);

		illegalUncheckedProperties(PivotAddToDistributionTickManager.this, managedType, property, "It's illegal to ask whether the specified property [" + property+ "] is 'used' if it is not 'checked' in type [" + managedType.getSimpleName() + "].");
		return rootsListsOfUsedProperties.containsKey(managedType) && rootsListsOfUsedProperties.get(managedType).contains(property);
	    }

	    @Override
	    public IUsageManager use(final Class<?> root, final String property, final boolean check) {
		// inject an enhanced type into method implementation
		final Class<?> managedType = managedType(root);

		final List<String> listOfUsedProperties = getAndInitSecondUsedProperties(managedType, property);
		if (check && !listOfUsedProperties.contains(property)) {
		    listOfUsedProperties.clear();
		    listOfUsedProperties.add(property);
		    PivotAddToDistributionTickManager.this.use(root, property, false);
		} else if (!check) {
		    listOfUsedProperties.remove(property);
		}
		for (final IPropertyUsageListener listener : propertyUsageListeners.getListeners(IPropertyUsageListener.class)) {
		    listener.propertyStateChanged(managedType, property, check, null);
		}
		return this;
	    }

	    @Override
	    public List<String> usedProperties(final Class<?> root) {
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
	    public IUsageManager addPropertyUsageListener(final IPropertyUsageListener listener) {
		removeEmptyPropertyUsageListeners();
		propertyUsageListeners.add(IPropertyUsageListener.class, listener);
		return this;
	    }

	    @Override
	    public IUsageManager addWeakPropertyUsageListener(final IPropertyUsageListener listener) {
		removeEmptyPropertyUsageListeners();
		propertyUsageListeners.add(IPropertyUsageListener.class, new WeakPropertyUsageListener(this, listener));
		return this;
	    }

	    @Override
	    public IUsageManager removePropertyUsageListener(final IPropertyUsageListener listener) {
		for (final IPropertyUsageListener obj : propertyUsageListeners.getListeners(IPropertyUsageListener.class)) {
		    if (listener == obj) {
			propertyUsageListeners.remove(IPropertyUsageListener.class, listener);
		    } else if (obj instanceof WeakPropertyUsageListener) {
			final IPropertyUsageListener weakRef = ((WeakPropertyUsageListener) obj).getRef();
			if (weakRef == listener || weakRef == null) {
			    propertyUsageListeners.remove(IPropertyUsageListener.class, obj);
			}
		    }
		}
		return this;
	    }

	    private void removeEmptyPropertyUsageListeners() {
		for (final IPropertyUsageListener obj : propertyUsageListeners.getListeners(IPropertyUsageListener.class)) {
		    if (obj instanceof WeakPropertyUsageListener && ((WeakPropertyUsageListener) obj).getRef() == null) {
			propertyUsageListeners.remove(IPropertyUsageListener.class, obj);
		    }
		}
	    }
	}
    }

    public static class PivotAddToAggregationTickManager extends AbstractAnalysisAddToAggregationTickManager implements IPivotAddToAggregationTickManager {
	private final EnhancementPropertiesMap<Integer> propertiesWidths;

	/**
	 * Used for serialisation and for normal initialisation. IMPORTANT : To use this tick it should be passed into manager constructor, which will initialise "dtr" and "tr"
	 * fields.
	 */
	public PivotAddToAggregationTickManager() {
	    propertiesWidths = createPropertiesMap();
	}

	@Override
	public int getWidth(final Class<?> root, final String property) {
	    AbstractDomainTree.illegalUnusedProperties(this, root, property, "Could not get a 'width' for 'unused' property [" + property + "] in type [" + root.getSimpleName() + "].");
	    return PivotDomainTreeRepresentation.getWidth(tr().getDtr(), root, property, propertiesWidths, tr().getWidthByDefault(root, property));
	}

	@Override
	public IWidthManager setWidth(final Class<?> root, final String property, final int width) {
	    AbstractDomainTree.illegalUnusedProperties(this, root, property, "Could not set a 'width' for 'unused' property [" + property + "] in type [" + root.getSimpleName() + "].");
	    PivotDomainTreeRepresentation.setWidth(tr().getDtr(), root, property, width, propertiesWidths);
	    return this;
	}

	@Override
	protected PivotAddToAggregationTickRepresentation tr() {
	    return (PivotAddToAggregationTickRepresentation) super.tr();
	}

	@Override
	public IUsageManager use(final Class<?> root, final String property, final boolean check) {
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
	    usedPropertiesChanged(root, property, check);
	    return this;
	}

	@Override
	public int hashCode() {
	    final int prime = 31;
	    int result = super.hashCode();
	    result = prime * result + ((propertiesWidths == null) ? 0 : propertiesWidths.hashCode());
	    return result;
	}

	@Override
	public boolean equals(final Object obj) {
	    if (this == obj)
		return true;
	    if (!super.equals(obj))
		return false;
	    if (getClass() != obj.getClass())
		return false;
	    final PivotAddToAggregationTickManager other = (PivotAddToAggregationTickManager) obj;
	    if (propertiesWidths == null) {
		if (other.propertiesWidths != null)
		    return false;
	    } else if (!propertiesWidths.equals(other.propertiesWidths))
		return false;
	    return true;
	}
    }

    /**
     * A specific Kryo serialiser for {@link PivotDomainTreeManager}.
     *
     * @author TG Team
     *
     */
    public static class PivotDomainTreeManagerSerialiser extends AbstractAnalysisDomainTreeManagerSerialiser<PivotDomainTreeManager> {
	public PivotDomainTreeManagerSerialiser(final ISerialiser kryo) {
	    super(kryo);
	}

	@Override
	public PivotDomainTreeManager read(final ByteBuffer buffer) {
	    final PivotDomainTreeRepresentation dtr = readValue(buffer, PivotDomainTreeRepresentation.class);
	    final PivotAddToDistributionTickManager firstTick = readValue(buffer, PivotAddToDistributionTickManager.class);
	    final PivotAddToAggregationTickManager secondTick = readValue(buffer, PivotAddToAggregationTickManager.class);
	    final Boolean visible = readValue(buffer, Boolean.class);
	    return new PivotDomainTreeManager(kryo(), dtr, visible, firstTick, secondTick);
	}
    }
}
