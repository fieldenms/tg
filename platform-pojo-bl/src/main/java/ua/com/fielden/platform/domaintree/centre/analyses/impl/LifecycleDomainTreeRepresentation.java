package ua.com.fielden.platform.domaintree.centre.analyses.impl;

import java.nio.ByteBuffer;
import java.util.Set;

import ua.com.fielden.platform.domaintree.centre.analyses.ILifecycleDomainTreeRepresentation;
import ua.com.fielden.platform.domaintree.impl.AbstractDomainTree;
import ua.com.fielden.platform.domaintree.impl.EnhancementLinkedRootsSet;
import ua.com.fielden.platform.domaintree.impl.EnhancementRootsMap;
import ua.com.fielden.platform.domaintree.impl.EnhancementSet;
import ua.com.fielden.platform.serialisation.api.ISerialiser;
import ua.com.fielden.platform.serialisation.impl.TgKryo;
import ua.com.fielden.platform.utils.Pair;

/**
 * A domain tree representation for lifecycle analyses.
 *
 * @author TG Team
 *
 */
public class LifecycleDomainTreeRepresentation extends AbstractAnalysisDomainTreeRepresentation implements ILifecycleDomainTreeRepresentation {
    /**
     * A <i>representation</i> constructor for the first time instantiation.
     *
     * @param serialiser
     * @param rootTypes
     */
    public LifecycleDomainTreeRepresentation(final ISerialiser serialiser, final Set<Class<?>> rootTypes) {
	this(serialiser, rootTypes, createSet(), new LifecycleAddToDistributionTickRepresentation(), new LifecycleAddToCategoriesTickRepresentation(), AbstractDomainTree.<ListenedArrayList>createRootsMap());
    }

    /**
     * A <i>representation</i> constructor. Initialises also children references on itself.
     */
    protected LifecycleDomainTreeRepresentation(final ISerialiser serialiser, final Set<Class<?>> rootTypes, final Set<Pair<Class<?>, String>> excludedProperties, final LifecycleAddToDistributionTickRepresentation firstTick, final LifecycleAddToCategoriesTickRepresentation secondTick, final EnhancementRootsMap<ListenedArrayList> includedProperties) {
	super(serialiser, rootTypes, excludedProperties, firstTick, secondTick, includedProperties);
    }

    @Override
    public ILifecycleAddToDistributionTickRepresentation getFirstTick() {
	return (ILifecycleAddToDistributionTickRepresentation) super.getFirstTick();
    }

    @Override
    public ILifecycleAddToCategoriesTickRepresentation getSecondTick() {
	return (ILifecycleAddToCategoriesTickRepresentation) super.getSecondTick();
    }

    public static class LifecycleAddToDistributionTickRepresentation extends AbstractAnalysisAddToDistributionTickRepresentation implements ILifecycleAddToDistributionTickRepresentation {
	/**
	 * Used for serialisation and for normal initialisation. IMPORTANT : To use this tick it should be passed into representation constructor, which should initialise "dtr"
	 * field.
	 */
	public LifecycleAddToDistributionTickRepresentation() {
	}
    }

    public static class LifecycleAddToCategoriesTickRepresentation extends AbstractAnalysisAddToAggregationTickRepresentation implements ILifecycleAddToCategoriesTickRepresentation {
	/**
	 * Used for serialisation and for normal initialisation. IMPORTANT : To use this tick it should be passed into representation constructor, which should initialise "dtr"
	 * field.
	 */
	public LifecycleAddToCategoriesTickRepresentation() {
	}
    }

    /**
     * A specific Kryo serialiser for {@link LifecycleDomainTreeRepresentation}.
     *
     * @author TG Team
     *
     */
    public static class LifecycleDomainTreeRepresentationSerialiser extends AbstractDomainTreeRepresentationSerialiser<LifecycleDomainTreeRepresentation> {
	public LifecycleDomainTreeRepresentationSerialiser(final TgKryo kryo) {
	    super(kryo);
	}

	@Override
	public LifecycleDomainTreeRepresentation read(final ByteBuffer buffer) {
	    final EnhancementLinkedRootsSet rootTypes = readValue(buffer, EnhancementLinkedRootsSet.class);
	    final EnhancementSet excludedProperties = readValue(buffer, EnhancementSet.class);
	    final LifecycleAddToDistributionTickRepresentation firstTick = readValue(buffer, LifecycleAddToDistributionTickRepresentation.class);
	    final LifecycleAddToCategoriesTickRepresentation secondTick = readValue(buffer, LifecycleAddToCategoriesTickRepresentation.class);
	    final EnhancementRootsMap<ListenedArrayList> includedProperties = readValue(buffer, EnhancementRootsMap.class);
	    return new LifecycleDomainTreeRepresentation(kryo(), rootTypes, excludedProperties, firstTick, secondTick, includedProperties);
	}
    }
}
