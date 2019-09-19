package ua.com.fielden.platform.domaintree.testing;

import java.util.Set;

import ua.com.fielden.platform.domaintree.centre.analyses.IAbstractAnalysisDomainTreeRepresentation;
import ua.com.fielden.platform.domaintree.centre.analyses.impl.AbstractAnalysisDomainTreeRepresentation;
import ua.com.fielden.platform.serialisation.api.ISerialiser;
import ua.com.fielden.platform.utils.Pair;

/**
 * Test implementation of abstract analysis representation.
 * 
 * @author TG Team
 * 
 */
public class AbstractAnalysisDomainTreeRepresentation1 extends AbstractAnalysisDomainTreeRepresentation implements IAbstractAnalysisDomainTreeRepresentation {
    /**
     * A <i>representation</i> constructor for the first time instantiation.
     * 
     * @param serialiser
     * @param rootTypes
     */
    public AbstractAnalysisDomainTreeRepresentation1(final ISerialiser serialiser, final Set<Class<?>> rootTypes) {
        this(serialiser, rootTypes, createSet(), new AbstractAnalysisAddToDistributionTickRepresentation1(), new AbstractAnalysisAddToAggregationTickRepresentation1());
    }

    /**
     * A <i>representation</i> constructor. Initialises also children references on itself.
     */
    protected AbstractAnalysisDomainTreeRepresentation1(final ISerialiser serialiser, final Set<Class<?>> rootTypes, final Set<Pair<Class<?>, String>> excludedProperties, final AbstractAnalysisAddToDistributionTickRepresentation firstTick, final AbstractAnalysisAddToAggregationTickRepresentation secondTick) {
        super(serialiser, rootTypes, excludedProperties, firstTick, secondTick);
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
        /**
         * Used for serialisation and for normal initialisation. IMPORTANT : To use this tick it should be passed into representation constructor, which should initialise "dtr"
         * field.
         */
        public AbstractAnalysisAddToDistributionTickRepresentation1() {
        }
    }

    public static class AbstractAnalysisAddToAggregationTickRepresentation1 extends AbstractAnalysisAddToAggregationTickRepresentation implements IAbstractAnalysisAddToAggregationTickRepresentation {
        /**
         * Used for serialisation and for normal initialisation. IMPORTANT : To use this tick it should be passed into representation constructor, which should initialise "dtr"
         * field.
         */
        public AbstractAnalysisAddToAggregationTickRepresentation1() {
        }
    }
}
