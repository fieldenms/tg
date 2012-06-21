package ua.com.fielden.platform.domaintree.centre.analyses.impl;

import java.nio.ByteBuffer;
import java.util.Map;
import java.util.Set;

import ua.com.fielden.platform.domaintree.IDomainTreeRepresentation;
import ua.com.fielden.platform.domaintree.centre.analyses.IPivotDomainTreeRepresentation;
import ua.com.fielden.platform.domaintree.impl.AbstractDomainTree;
import ua.com.fielden.platform.domaintree.impl.EnhancementLinkedRootsSet;
import ua.com.fielden.platform.domaintree.impl.EnhancementPropertiesMap;
import ua.com.fielden.platform.domaintree.impl.EnhancementRootsMap;
import ua.com.fielden.platform.domaintree.impl.EnhancementSet;
import ua.com.fielden.platform.serialisation.api.ISerialiser;
import ua.com.fielden.platform.serialisation.impl.TgKryo;
import ua.com.fielden.platform.utils.Pair;

/**
 * A domain tree representation for pivot analyses.
 *
 * @author TG Team
 *
 */
public class PivotDomainTreeRepresentation extends AbstractAnalysisDomainTreeRepresentation implements IPivotDomainTreeRepresentation {
    /**
     * A <i>representation</i> constructor for the first time instantiation.
     *
     * @param serialiser
     * @param rootTypes
     */
    public PivotDomainTreeRepresentation(final ISerialiser serialiser, final Set<Class<?>> rootTypes) {
	this(serialiser, rootTypes, createSet(), new PivotAddToDistributionTickRepresentation(), new PivotAddToAggregationTickRepresentation(), AbstractDomainTree.<ListenedArrayList>createRootsMap());
    }

    /**
     * A <i>representation</i> constructor. Initialises also children references on itself.
     */
    protected PivotDomainTreeRepresentation(final ISerialiser serialiser, final Set<Class<?>> rootTypes, final Set<Pair<Class<?>, String>> excludedProperties, final PivotAddToDistributionTickRepresentation firstTick, final PivotAddToAggregationTickRepresentation secondTick, final EnhancementRootsMap<ListenedArrayList> includedProperties) {
	super(serialiser, rootTypes, excludedProperties, firstTick, secondTick, includedProperties);
    }

    @Override
    public IPivotAddToDistributionTickRepresentation getFirstTick() {
	return (IPivotAddToDistributionTickRepresentation) super.getFirstTick();
    }

    @Override
    public IPivotAddToAggregationTickRepresentation getSecondTick() {
	return (IPivotAddToAggregationTickRepresentation) super.getSecondTick();
    }

    public static class PivotAddToDistributionTickRepresentation extends AbstractAnalysisAddToDistributionTickRepresentation implements IPivotAddToDistributionTickRepresentation {
	private final EnhancementPropertiesMap<Integer> propertiesWidthsByDefault;

	/**
	 * Used for serialisation and for normal initialisation. IMPORTANT : To use this tick it should be passed into representation constructor, which should initialise "dtr"
	 * field.
	 */
	public PivotAddToDistributionTickRepresentation() {
	    propertiesWidthsByDefault = createPropertiesMap();
	}

	@Override
	public int getWidthByDefault(final Class<?> root, final String property) {
	    illegalExcludedProperties(getDtr(), root, property, "Could not ask a 'width' for already 'excluded' property [" + property + "] in type [" + root.getSimpleName() + "].");
	    return (propertiesWidthsByDefault.containsKey(key(root, getDummySuffix()))) ? propertiesWidthsByDefault.get(key(root, getDummySuffix())) : 80;
	}

	@Override
	public void setWidthByDefault(final Class<?> root, final String property, final int width) {
	    illegalExcludedProperties(getDtr(), root, property, "Could not set a 'width' for already 'excluded' property [" + property + "] in type [" + root.getSimpleName() + "].");
	    propertiesWidthsByDefault.put(key(root, getDummySuffix()), width);
	}

	@Override
	public int hashCode() {
	    final int prime = 31;
	    int result = super.hashCode();
	    result = prime * result + ((propertiesWidthsByDefault == null) ? 0 : propertiesWidthsByDefault.hashCode());
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
	    final PivotAddToDistributionTickRepresentation other = (PivotAddToDistributionTickRepresentation) obj;
	    if (propertiesWidthsByDefault == null) {
		if (other.propertiesWidthsByDefault != null)
		    return false;
	    } else if (!propertiesWidthsByDefault.equals(other.propertiesWidthsByDefault))
		return false;
	    return true;
	}
    }

    public static class PivotAddToAggregationTickRepresentation extends AbstractAnalysisAddToAggregationTickRepresentation implements IPivotAddToAggregationTickRepresentation {
	private final EnhancementPropertiesMap<Integer> propertiesWidthsByDefault;

	/**
	 * Used for serialisation and for normal initialisation. IMPORTANT : To use this tick it should be passed into representation constructor, which should initialise "dtr"
	 * field.
	 */
	public PivotAddToAggregationTickRepresentation() {
	    propertiesWidthsByDefault = createPropertiesMap();
	}

	@Override
	public int getWidthByDefault(final Class<?> root, final String property) {
	    return PivotDomainTreeRepresentation.getWidth(getDtr(), root, property, propertiesWidthsByDefault, 80);
	}

	@Override
	public void setWidthByDefault(final Class<?> root, final String property, final int width) {
	    PivotDomainTreeRepresentation.setWidth(getDtr(), root, property, width, propertiesWidthsByDefault);
	}

	@Override
	public int hashCode() {
	    final int prime = 31;
	    int result = super.hashCode();
	    result = prime * result + ((propertiesWidthsByDefault == null) ? 0 : propertiesWidthsByDefault.hashCode());
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
	    final PivotAddToAggregationTickRepresentation other = (PivotAddToAggregationTickRepresentation) obj;
	    if (propertiesWidthsByDefault == null) {
		if (other.propertiesWidthsByDefault != null)
		    return false;
	    } else if (!propertiesWidthsByDefault.equals(other.propertiesWidthsByDefault))
		return false;
	    return true;
	}
    }

    /**
     * Returns property's (column's) <i>width</i>. <br><br>
     *
     * Throws {@link IllegalArgumentException}. if the specified property is excluded.<br><br>
     *
     * @param dtr -- specified {@link IDomainTreeRepresentation} that allows one to determine whether specified property is excluded or not.
     * @param root -- a root type that contains property.
     * @param property -- a dot-notation expression that defines a property.
     * @param propertiesWidths -- holds all specified properties' widths
     * @param defaultWidthForAll -- used to return width if no specific width exists in <code>propertiesWidths</code>
     * @return
     */
    protected static int getWidth(final IDomainTreeRepresentation dtr, final Class<?> root, final String property, final Map<Pair<Class<?>, String>, Integer> propertiesWidths, final int defaultWidthForAll) {
	illegalExcludedProperties(dtr, root, property, "Could not ask a 'width' for already 'excluded' property [" + property + "] in type [" + root.getSimpleName() + "].");
	return (propertiesWidths.containsKey(key(root, property))) ? propertiesWidths.get(key(root, property)) : defaultWidthForAll;
    }

    /**
     * Sets the <i>width</i> for specified property (column). <br><br>
     *
     * Throws {@link IllegalArgumentException}. if the specified property is excluded.<br><br>
     *
     * @param dtr -- specified {@link IDomainTreeRepresentation} that allows one to determine whether specified property is excluded or not.
     * @param root -- a root type that contains property.
     * @param property -- a dot-notation expression that defines a property.
     * @param width -- specified width to set.
     * @param propertiesWidths -- holds all specified properties' widths
     *
     */
    protected static void setWidth(final IDomainTreeRepresentation dtr, final Class<?> root, final String property, final int width, final Map<Pair<Class<?>, String>, Integer> propertiesWidths) {
	illegalExcludedProperties(dtr, root, property, "Could not ask a 'width' for already 'excluded' property [" + property + "] in type [" + root.getSimpleName() + "].");
	propertiesWidths.put(key(root, property), width);
    }

    /**
     * A specific Kryo serialiser for {@link PivotDomainTreeRepresentation}.
     *
     * @author TG Team
     *
     */
    public static class PivotDomainTreeRepresentationSerialiser extends AbstractDomainTreeRepresentationSerialiser<PivotDomainTreeRepresentation> {
	public PivotDomainTreeRepresentationSerialiser(final TgKryo kryo) {
	    super(kryo);
	}

	@Override
	public PivotDomainTreeRepresentation read(final ByteBuffer buffer) {
	    final EnhancementLinkedRootsSet rootTypes = readValue(buffer, EnhancementLinkedRootsSet.class);
	    final EnhancementSet excludedProperties = readValue(buffer, EnhancementSet.class);
	    final PivotAddToDistributionTickRepresentation firstTick = readValue(buffer, PivotAddToDistributionTickRepresentation.class);
	    final PivotAddToAggregationTickRepresentation secondTick = readValue(buffer, PivotAddToAggregationTickRepresentation.class);
	    final EnhancementRootsMap<ListenedArrayList> includedProperties = readValue(buffer, EnhancementRootsMap.class);
	    return new PivotDomainTreeRepresentation(kryo(), rootTypes, excludedProperties, firstTick, secondTick, includedProperties);
	}
    }
}
