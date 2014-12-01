package ua.com.fielden.platform.domaintree.centre.analyses.impl;

import java.nio.ByteBuffer;
import java.util.Set;

import ua.com.fielden.platform.domaintree.centre.analyses.IAnalysisDomainTreeRepresentation;
import ua.com.fielden.platform.domaintree.impl.EnhancementLinkedRootsSet;
import ua.com.fielden.platform.domaintree.impl.EnhancementSet;
import ua.com.fielden.platform.serialisation.api.ISerialiser;
import ua.com.fielden.platform.utils.Pair;

/**
 * A domain tree representation for analyses.
 * 
 * @author TG Team
 * 
 */
public class AnalysisDomainTreeRepresentation extends AbstractAnalysisDomainTreeRepresentation implements IAnalysisDomainTreeRepresentation {
    /**
     * A <i>representation</i> constructor for the first time instantiation.
     * 
     * @param serialiser
     * @param rootTypes
     */
    public AnalysisDomainTreeRepresentation(final ISerialiser serialiser, final Set<Class<?>> rootTypes) {
        this(serialiser, rootTypes, createSet(), new AnalysisAddToDistributionTickRepresentation(), new AnalysisAddToAggregationTickRepresentation());
    }

    /**
     * A <i>representation</i> constructor. Initialises also children references on itself.
     */
    protected AnalysisDomainTreeRepresentation(final ISerialiser serialiser, final Set<Class<?>> rootTypes, final Set<Pair<Class<?>, String>> excludedProperties, final AnalysisAddToDistributionTickRepresentation firstTick, final AnalysisAddToAggregationTickRepresentation secondTick) {
        super(serialiser, rootTypes, excludedProperties, firstTick, secondTick);
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
        /**
         * Used for serialisation and for normal initialisation. IMPORTANT : To use this tick it should be passed into representation constructor, which should initialise "dtr"
         * field.
         */
        public AnalysisAddToDistributionTickRepresentation() {
        }
    }

    public static class AnalysisAddToAggregationTickRepresentation extends AbstractAnalysisAddToAggregationTickRepresentation implements IAnalysisAddToAggregationTickRepresentation {
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
        public AnalysisDomainTreeRepresentationSerialiser(final ISerialiser serialiser) {
            super(serialiser);
        }

        @Override
        public AnalysisDomainTreeRepresentation read(final ByteBuffer buffer) {
            final EnhancementLinkedRootsSet rootTypes = readValue(buffer, EnhancementLinkedRootsSet.class);
            final EnhancementSet excludedProperties = readValue(buffer, EnhancementSet.class);
            final AnalysisAddToDistributionTickRepresentation firstTick = readValue(buffer, AnalysisAddToDistributionTickRepresentation.class);
            final AnalysisAddToAggregationTickRepresentation secondTick = readValue(buffer, AnalysisAddToAggregationTickRepresentation.class);
            return new AnalysisDomainTreeRepresentation(serialiser(), rootTypes, excludedProperties, firstTick, secondTick);
        }
    }
}
