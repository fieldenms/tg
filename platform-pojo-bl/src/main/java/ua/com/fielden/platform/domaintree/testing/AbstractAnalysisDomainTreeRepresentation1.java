package ua.com.fielden.platform.domaintree.testing;

import java.nio.ByteBuffer;
import java.util.Set;

import ua.com.fielden.platform.domaintree.centre.analyses.IAbstractAnalysisDomainTreeRepresentation;
import ua.com.fielden.platform.domaintree.centre.analyses.impl.AbstractAnalysisDomainTreeRepresentation;
import ua.com.fielden.platform.domaintree.impl.AbstractDomainTree;
import ua.com.fielden.platform.domaintree.impl.EnhancementLinkedRootsSet;
import ua.com.fielden.platform.domaintree.impl.EnhancementRootsMap;
import ua.com.fielden.platform.domaintree.impl.EnhancementSet;
import ua.com.fielden.platform.serialisation.api.ISerialiser;
import ua.com.fielden.platform.serialisation.impl.TgKryo;
import ua.com.fielden.platform.utils.Pair;

/**
 * Test implementation of abstract analysis representation.
 *
 * @author TG Team
 *
 */
public class AbstractAnalysisDomainTreeRepresentation1 extends AbstractAnalysisDomainTreeRepresentation implements IAbstractAnalysisDomainTreeRepresentation {
    private static final long serialVersionUID = 6796573637187428691L;

    /**
     * A <i>representation</i> constructor for the first time instantiation.
     *
     * @param serialiser
     * @param rootTypes
     */
    public AbstractAnalysisDomainTreeRepresentation1(final ISerialiser serialiser, final Set<Class<?>> rootTypes) {
	this(serialiser, rootTypes, createSet(), new AbstractAnalysisAddToDistributionTickRepresentation1(), new AbstractAnalysisAddToAggregationTickRepresentation1(), AbstractDomainTree.<ListenedArrayList>createRootsMap());
    }

    /**
     * A <i>representation</i> constructor. Initialises also children references on itself.
     */
    protected AbstractAnalysisDomainTreeRepresentation1(final ISerialiser serialiser, final Set<Class<?>> rootTypes, final Set<Pair<Class<?>, String>> excludedProperties, final AbstractAnalysisAddToDistributionTickRepresentation firstTick, final AbstractAnalysisAddToAggregationTickRepresentation secondTick, final EnhancementRootsMap<ListenedArrayList> includedProperties) {
	super(serialiser, rootTypes, excludedProperties, firstTick, secondTick, includedProperties);
    }

    @Override
    public AbstractAnalysisAddToDistributionTickRepresentation1 getFirstTick() {
	return (AbstractAnalysisAddToDistributionTickRepresentation1) super.getFirstTick();
    }

    @Override
    public AbstractAnalysisAddToAggregationTickRepresentation1 getSecondTick() {
	return (AbstractAnalysisAddToAggregationTickRepresentation1) super.getSecondTick();
    }

    public static class AbstractAnalysisAddToDistributionTickRepresentation1 extends AbstractAnalysisAddToDistributionTickRepresentation implements IAbstractAnalysisAddToDistributionTickRepresentation {
	private static final long serialVersionUID = 4243970952493957297L;

	/**
	 * Used for serialisation and for normal initialisation. IMPORTANT : To use this tick it should be passed into representation constructor, which should initialise "dtr"
	 * field.
	 */
	public AbstractAnalysisAddToDistributionTickRepresentation1() {
	}
    }

    public static class AbstractAnalysisAddToAggregationTickRepresentation1 extends AbstractAnalysisAddToAggregationTickRepresentation implements IAbstractAnalysisAddToAggregationTickRepresentation {
	private static final long serialVersionUID = 4629386477984565938L;

	/**
	 * Used for serialisation and for normal initialisation. IMPORTANT : To use this tick it should be passed into representation constructor, which should initialise "dtr"
	 * field.
	 */
	public AbstractAnalysisAddToAggregationTickRepresentation1() {
	}
    }

    /**
     * A specific Kryo serialiser for {@link AbstractAnalysisDomainTreeRepresentation1}.
     *
     * @author TG Team
     *
     */
    public static class AbstractAnalysisDomainTreeRepresentation1Serialiser extends AbstractDomainTreeRepresentationSerialiser<AbstractAnalysisDomainTreeRepresentation1> {
	public AbstractAnalysisDomainTreeRepresentation1Serialiser(final TgKryo kryo) {
	    super(kryo);
	}

	@Override
	public AbstractAnalysisDomainTreeRepresentation1 read(final ByteBuffer buffer) {
	    final EnhancementLinkedRootsSet rootTypes = readValue(buffer, EnhancementLinkedRootsSet.class);
	    final EnhancementSet excludedProperties = readValue(buffer, EnhancementSet.class);
	    final AbstractAnalysisAddToDistributionTickRepresentation1 firstTick = readValue(buffer, AbstractAnalysisAddToDistributionTickRepresentation1.class);
	    final AbstractAnalysisAddToAggregationTickRepresentation1 secondTick = readValue(buffer, AbstractAnalysisAddToAggregationTickRepresentation1.class);
	    final EnhancementRootsMap<ListenedArrayList> includedProperties = readValue(buffer, EnhancementRootsMap.class);
	    return new AbstractAnalysisDomainTreeRepresentation1(kryo(), rootTypes, excludedProperties, firstTick, secondTick, includedProperties);
	}
    }
}
