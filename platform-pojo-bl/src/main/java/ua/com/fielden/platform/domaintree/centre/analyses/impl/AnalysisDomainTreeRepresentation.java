package ua.com.fielden.platform.domaintree.centre.analyses.impl;

import java.util.Set;

import ua.com.fielden.platform.domaintree.centre.analyses.IAnalysisDomainTreeRepresentation;
import ua.com.fielden.platform.entity.factory.EntityFactory;
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
     * @param entityFactory
     * @param rootTypes
     */
    public AnalysisDomainTreeRepresentation(final EntityFactory entityFactory, final Set<Class<?>> rootTypes) {
        this(entityFactory, rootTypes, createSet(), new AnalysisAddToDistributionTickRepresentation(), new AnalysisAddToAggregationTickRepresentation());
    }

    /**
     * A <i>representation</i> constructor. Initialises also children references on itself.
     */
    protected AnalysisDomainTreeRepresentation(final EntityFactory entityFactory, final Set<Class<?>> rootTypes, final Set<Pair<Class<?>, String>> excludedProperties, final AnalysisAddToDistributionTickRepresentation firstTick, final AnalysisAddToAggregationTickRepresentation secondTick) {
        super(entityFactory, rootTypes, excludedProperties, firstTick, secondTick);
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

}
