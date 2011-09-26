package ua.com.fielden.platform.treemodel.rules.criteria.analyses.impl;

import java.nio.ByteBuffer;
import java.util.Set;

import ua.com.fielden.platform.serialisation.api.ISerialiser;
import ua.com.fielden.platform.serialisation.impl.TgKryo;
import ua.com.fielden.platform.treemodel.rules.criteria.analyses.IAnalysisDomainTreeRepresentation;
import ua.com.fielden.platform.treemodel.rules.impl.AbstractDomainTree;
import ua.com.fielden.platform.treemodel.rules.impl.EnhancementLinkedRootsSet;
import ua.com.fielden.platform.treemodel.rules.impl.EnhancementRootsMap;
import ua.com.fielden.platform.treemodel.rules.impl.EnhancementSet;
import ua.com.fielden.platform.utils.Pair;

/**
 * A domain tree representation for analyses.
 *
 * @author TG Team
 *
 */
public class AnalysisDomainTreeRepresentation extends AbstractAnalysisDomainTreeRepresentation implements IAnalysisDomainTreeRepresentation {
    private static final long serialVersionUID = 6796573637187428691L;

    /**
     * A <i>representation</i> constructor for the first time instantiation.
     *
     * @param serialiser
     * @param rootTypes
     */
    public AnalysisDomainTreeRepresentation(final ISerialiser serialiser, final Set<Class<?>> rootTypes) {
	this(serialiser, rootTypes, createSet(), new AnalysisAddToDistributionTickRepresentation(), new AnalysisAddToAggregationTickRepresentation(), AbstractDomainTree.<ListenedArrayList>createRootsMap());
    }

    /**
     * A <i>representation</i> constructor. Initialises also children references on itself.
     */
    protected AnalysisDomainTreeRepresentation(final ISerialiser serialiser, final Set<Class<?>> rootTypes, final Set<Pair<Class<?>, String>> excludedProperties, final IAnalysisAddToDistributionTickRepresentation firstTick, final IAnalysisAddToAggregationTickRepresentation secondTick, final EnhancementRootsMap<ListenedArrayList> includedProperties) {
	super(serialiser, rootTypes, excludedProperties, firstTick, secondTick, includedProperties);
    }

    @Override
    public IAnalysisAddToDistributionTickRepresentation getFirstTick() {
	return (IAnalysisAddToDistributionTickRepresentation) super.getFirstTick();
    }

    @Override
    public IAnalysisAddToAggregationTickRepresentation getSecondTick() {
	return (IAnalysisAddToAggregationTickRepresentation) super.getSecondTick();
    }

    public static class AnalysisAddToDistributionTickRepresentation extends AbstractAnalysisAddToDistributionTickRepresentation implements IAnalysisAddToDistributionTickRepresentation {
	private static final long serialVersionUID = 4243970952493957297L;

	/**
	 * Used for serialisation and for normal initialisation. IMPORTANT : To use this tick it should be passed into representation constructor, which should initialise "dtr"
	 * field.
	 */
	public AnalysisAddToDistributionTickRepresentation() {
	}
    }

    public static class AnalysisAddToAggregationTickRepresentation extends AbstractAnalysisAddToAggregationTickRepresentation implements IAnalysisAddToAggregationTickRepresentation {
	private static final long serialVersionUID = 4629386477984565938L;

	/**
	 * Used for serialisation and for normal initialisation. IMPORTANT : To use this tick it should be passed into representation constructor, which should initialise "dtr"
	 * field.
	 */
	public AnalysisAddToAggregationTickRepresentation() {
	}
    }

    /**
     * A specific Kryo serialiser for {@link AnalysisDomainTreeRepresentation}.
     *
     * @author TG Team
     *
     */
    public static class AnalysisDomainTreeRepresentationSerialiser extends AbstractDomainTreeRepresentationSerialiser<AnalysisDomainTreeRepresentation> {
	public AnalysisDomainTreeRepresentationSerialiser(final TgKryo kryo) {
	    super(kryo);
	}

	@Override
	public AnalysisDomainTreeRepresentation read(final ByteBuffer buffer) {
	    final EnhancementLinkedRootsSet rootTypes = readValue(buffer, EnhancementLinkedRootsSet.class);
	    final EnhancementSet excludedProperties = readValue(buffer, EnhancementSet.class);
	    final AnalysisAddToDistributionTickRepresentation firstTick = readValue(buffer, AnalysisAddToDistributionTickRepresentation.class);
	    final AnalysisAddToAggregationTickRepresentation secondTick = readValue(buffer, AnalysisAddToAggregationTickRepresentation.class);
	    final EnhancementRootsMap<ListenedArrayList> includedProperties = readValue(buffer, EnhancementRootsMap.class);
	    return new AnalysisDomainTreeRepresentation(kryo(), rootTypes, excludedProperties, firstTick, secondTick, includedProperties);
	}
    }
}
